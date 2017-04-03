/*
 * The MIT License (MIT)
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.despector.emitter;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.AstEntry;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.ast.members.insn.misc.Return;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.format.EmitterFormat;
import org.spongepowered.despector.emitter.format.EmitterFormat.WrappingStyle;
import org.spongepowered.despector.emitter.special.AnnotationEmitter;
import org.spongepowered.despector.emitter.special.GenericsEmitter;
import org.spongepowered.despector.emitter.special.PackageEmitter;
import org.spongepowered.despector.emitter.special.PackageInfoEmitter;
import org.spongepowered.despector.util.TypeHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EmitterContext {

    private EmitterSet set;

    private EmitterFormat format;
    private Writer output;
    private StringBuilder buffer = null;
    private Set<LocalInstance> defined_locals = Sets.newHashSet();
    private Set<String> imports = null;

    private TypeEntry type = null;
    private TypeEntry outer_type = null;
    private MethodEntry method;
    private FieldEntry field;
    private Statement statement;
    private Deque<Instruction> insn_stack = new ArrayDeque<>();

    private int indentation = 0;
    private int offs = 0;

    private boolean semicolons = true;

    private int line_length = 0;
    private int wrap_point = -1;
    private StringBuilder line_buffer = new StringBuilder();
    private boolean is_wrapped = false;

    public EmitterContext(Writer output, EmitterFormat format) {
        this.output = output;
        this.format = format;

        this.imports = Sets.newHashSet();
    }

    public EmitterSet getEmitterSet() {
        return this.set;
    }

    public void setEmitterSet(EmitterSet set) {
        this.set = set;
    }

    public TypeEntry getType() {
        return this.type;
    }

    public void setType(TypeEntry type) {
        this.type = type;
    }

    public TypeEntry getOuterType() {
        return this.outer_type;
    }

    public void setOuterType(TypeEntry t) {
        this.outer_type = t;
    }

    public MethodEntry getMethod() {
        return this.method;
    }

    public void setMethod(MethodEntry mth) {
        this.method = mth;
    }

    public FieldEntry getField() {
        return this.field;
    }

    public void setField(FieldEntry fld) {
        this.field = fld;
    }

    public Statement getStatement() {
        return this.statement;
    }

    public void setStatement(Statement stmt) {
        this.statement = stmt;
    }

    public EmitterFormat getFormat() {
        return this.format;
    }

    public void setSemicolons(boolean state) {
        this.semicolons = state;
    }

    public Deque<Instruction> getCurrentInstructionStack() {
        return this.insn_stack;
    }

    public boolean isDefined(LocalInstance local) {
        return this.defined_locals.contains(local);
    }

    public void markDefined(LocalInstance local) {
        this.defined_locals.add(local);
    }

    public <T extends AstEntry> void emitOuterType(T obj) {
        if (obj instanceof TypeEntry) {
            TypeEntry type = (TypeEntry) obj;
            if (type.getName().endsWith("package-info")) {
                PackageInfoEmitter emitter = this.set.getSpecialEmitter(PackageInfoEmitter.class);
                emitter.emit(this, (InterfaceEntry) type);
                return;
            }
        }
        enableBuffer();
        emit(obj);
        outputBuffer();
    }

    @SuppressWarnings("unchecked")
    public <T extends AstEntry> boolean emit(T obj) {
        if (obj instanceof TypeEntry) {
            TypeEntry type = (TypeEntry) obj;
            if (type.isSynthetic()) {
                return false;
            }
            this.type = (TypeEntry) obj;
        } else if (obj instanceof FieldEntry) {
            this.field = (FieldEntry) obj;
        }
        AstEmitter<T> emitter = (AstEmitter<T>) this.set.getAstEmitter(obj.getClass());
        if (emitter == null) {
            throw new IllegalArgumentException("No emitter for ast entry " + obj.getClass().getName());
        }
        boolean state = emitter.emit(this, obj);
        if (obj instanceof FieldEntry) {
            this.field = null;
        }
        return state;
    }

    public EmitterContext emitBody(StatementBlock instructions) {
        emitBody(instructions, 0);
        return this;
    }

    public EmitterContext emitBody(StatementBlock instructions, int start) {
        if (instructions.getType() == StatementBlock.Type.METHOD) {
            this.defined_locals.clear();
        }
        boolean last_success = false;
        boolean should_indent = true;
        for (int i = start; i < instructions.getStatements().size(); i++) {
            Statement insn = instructions.getStatements().get(i);
            if (insn instanceof Return && !((Return) insn).getValue().isPresent()
                    && instructions.getType() == StatementBlock.Type.METHOD && i == instructions.getStatements().size() - 1) {
                break;
            }
            if (last_success) {
                newLine();
            }
            last_success = true;
            if (should_indent) {
                printIndentation();
            }
            should_indent = true;
            int mark = this.offs;
            emit(insn, this.semicolons);
            if (this.offs == mark) {
                should_indent = false;
                last_success = false;
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Statement> EmitterContext emit(T obj, boolean semicolon) {
        StatementEmitter<T> emitter = (StatementEmitter<T>) this.set.getStatementEmitter(obj.getClass());
        if (emitter == null) {
            throw new IllegalArgumentException("No emitter for statement " + obj.getClass().getName());
        }
        Statement last = getStatement();
        setStatement(obj);
        emitter.emit(this, obj, semicolon);
        setStatement(last);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Instruction> EmitterContext emit(T obj, TypeSignature type) {
        InstructionEmitter<T> emitter = (InstructionEmitter<T>) this.set.getInstructionEmitter(obj.getClass());
        if (emitter == null) {
            throw new IllegalArgumentException("No emitter for instruction " + obj.getClass().getName());
        }
        this.insn_stack.push(obj);
        if (type == null) {
            type = obj.inferType();
        }
        emitter.emit(this, obj, type);
        this.insn_stack.pop();
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Condition> EmitterContext emit(T condition) {
        ConditionEmitter<T> emitter = (ConditionEmitter<T>) this.set.getConditionEmitter(condition.getClass());
        if (emitter == null) {
            throw new IllegalArgumentException("No emitter for condition " + condition.getClass().getName());
        }
        emitter.emit(this, condition);
        return this;
    }

    public EmitterContext emit(Annotation anno) {
        AnnotationEmitter emitter = this.set.getSpecialEmitter(AnnotationEmitter.class);
        if (emitter == null) {
            throw new IllegalArgumentException("No emitter for annotations");
        }
        emitter.emit(this, anno);
        return this;
    }

    public EmitterContext indent() {
        this.indentation++;
        return this;
    }

    public EmitterContext dedent() {
        this.indentation--;
        return this;
    }

    public EmitterContext printIndentation() {
        if (this.format.indent_with_spaces) {
            for (int i = 0; i < this.indentation * this.format.indentation_size; i++) {
                printString(" ");
            }
        } else {
            for (int i = 0; i < this.indentation; i++) {
                printString("\t");
            }
        }
        return this;
    }

    public EmitterContext emitType(TypeSignature sig) {
        GenericsEmitter generics = this.set.getSpecialEmitter(GenericsEmitter.class);
        generics.emitTypeSignature(this, sig);
        return this;
    }

    public EmitterContext emitType(String name) {
        emitTypeClassName(TypeHelper.descToType(name).replace('/', '.'));
        return this;
    }

    public EmitterContext emitTypeName(String name) {
        emitTypeClassName(name.replace('/', '.'));
        return this;
    }

    public EmitterContext emitTypeClassName(String name) {
        if (name.endsWith("[]")) {
            emitTypeClassName(name.substring(0, name.length() - 2));
            printString("[]");
            return this;
        }
        if (name.indexOf('.') != -1) {
            if (name.startsWith("java.lang.") && name.lastIndexOf('.') == 9) {
                name = name.substring("java.lang.".length());
            } else if (this.type != null) {
                String this_package = "";
                String target_package = name;
                String this$name = this.type.getName().replace('/', '.');
                if (this$name.indexOf('.') != -1) {
                    this_package = this$name.substring(0, this$name.lastIndexOf('.'));
                    target_package = name.substring(0, name.lastIndexOf('.'));
                }
                if (this_package.equals(target_package)) {
                    name = name.substring(name.lastIndexOf('.') + 1);
                } else if (checkImport(name)) {
                    name = name.substring(name.lastIndexOf('.') + 1);
                }
            } else if (checkImport(name)) {
                name = name.substring(name.lastIndexOf('.') + 1);
            }
        }
        printString(name.replace('$', '.'));
        return this;
    }

    public EmitterContext printString(String line, boolean condition) {
        if (condition) {
            printString(line);
        }
        return this;
    }

    public EmitterContext newLine(int count) {
        for (int i = 0; i < count; i++) {
            newLine();
        }
        return this;
    }

    public EmitterContext newLine() {
        __newLine();
        if (this.is_wrapped) {
            dedent();
            dedent();
            this.is_wrapped = false;
        }
        return this;
    }

    public void flush() {
        if (this.buffer == null) {
            try {
                this.output.write(this.line_buffer.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.buffer.append(this.line_buffer.toString());
        }
    }

    private void __newLine() {
        flush();
        this.offs += 1;
        if (this.buffer == null) {
            try {
                this.output.write('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.buffer.append('\n');
        }
        this.line_length = 0;
        this.wrap_point = -1;
        this.line_buffer.setLength(0);
    }

    public void newIndentedLine() {
        newLine();
        printIndentation();
    }

    public EmitterContext printString(String line) {
        checkArgument(line.indexOf('\n') == -1);
        this.offs += this.line_buffer.length();
        this.line_length += line.length();
        this.line_buffer.append(line);
        if (this.line_length > this.format.line_split) {
            if (this.wrap_point != -1) {
                String existing = this.line_buffer.toString();
                String next = existing.substring(this.wrap_point);
                this.line_buffer.setLength(this.wrap_point);
                this.wrap_point = -1;
                newLine();
                if (!this.is_wrapped) {
                    this.is_wrapped = true;
                    indent();
                    indent();
                }
                printIndentation();
                printString(next);
            }
        }
        return this;
    }

    public EmitterContext markWrapPoint() {
        markWrapPoint(WrappingStyle.WRAP_WHEN_NEEDED, 0);
        return this;
    }

    public EmitterContext markWrapPoint(WrappingStyle style, int index) {
        this.wrap_point = this.line_length;
        return this;
    }

    public boolean checkImport(String type) {
        if (type.indexOf('$') != -1) {
            type = type.substring(0, type.indexOf('$'));
        }
        if (this.imports == null) {
            return false;
        }
        if (TypeHelper.isPrimative(type)) {
            return true;
        }
        this.imports.add(type);
        return true;
    }

    public void resetImports() {
        this.imports.clear();
    }

    public void emitImports() {
        List<String> imports = Lists.newArrayList(this.imports);
        for (int i = 0; i < this.format.import_order.size(); i++) {
            String group = this.format.import_order.get(i);
            if (group.startsWith("/#")) {
                // don't have static imports yet
                continue;
            }
            List<String> group_imports = Lists.newArrayList();
            for (Iterator<String> it = imports.iterator(); it.hasNext();) {
                String import_ = it.next();
                if (import_.startsWith(group)) {
                    group_imports.add(import_);
                    it.remove();
                }
            }
            Collections.sort(group_imports);
            for (String import_ : group_imports) {
                printString("import ");
                printString(import_);
                if (this.semicolons) {
                    printString(";");
                }
                newLine();
            }
            if (!group_imports.isEmpty() && i < this.format.import_order.size() - 1) {
                for (int o = 0; o < this.format.blank_lines_between_import_groups; o++) {
                    newLine();
                }
            }
        }
    }

    public void enableBuffer() {
        this.buffer = new StringBuilder();
    }

    public void outputBuffer() {

        // Then once we have finished emitting the class contents we detach the
        // buffer and then sort and emit the imports into the actual output and
        // then finally replay the buffer into the output.

        StringBuilder buf = this.buffer;
        if (this.line_buffer.length() != 0) {
            buf.append(this.line_buffer.toString());
        }
        this.buffer = null;
        PackageEmitter pkg_emitter = this.set.getSpecialEmitter(PackageEmitter.class);
        String pkg = this.type.getName();
        int last = pkg.lastIndexOf('/');
        if (last != -1) {
            for (int i = 0; i < this.format.blank_lines_before_package; i++) {
                newLine();
            }
            pkg = pkg.substring(0, last).replace('/', '.');
            pkg_emitter.emitPackage(this, pkg);
            newLine();
        }
        int lines = Math.max(this.format.blank_lines_after_package, this.format.blank_lines_before_imports);
        for (int i = 0; i < lines; i++) {
            newLine();
        }

        emitImports();

        if (!this.imports.isEmpty()) {
            for (int i = 0; i < this.format.blank_lines_after_imports - 1; i++) {
                newLine();
            }
        }

        String data = buf.toString();

        // Replay the buffer.
        try {
            this.output.write(data);
            if (this.format.insert_new_line_at_end_of_file_if_missing) {
                if (data.charAt(data.length() - 1) != '\n') {
                    this.output.write('\n');
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.imports = null;
        this.buffer = null;
    }

}
