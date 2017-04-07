/*
 * The MIT License (MIT)
 *
 * Copyright (c) Despector <https://despector.voxelgenesis.com>
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
import org.spongepowered.despector.ast.members.insn.branch.DoWhile;
import org.spongepowered.despector.ast.members.insn.branch.For;
import org.spongepowered.despector.ast.members.insn.branch.ForEach;
import org.spongepowered.despector.ast.members.insn.branch.If;
import org.spongepowered.despector.ast.members.insn.branch.Switch;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch;
import org.spongepowered.despector.ast.members.insn.branch.While;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.ast.members.insn.misc.Return;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.format.EmitterFormat;
import org.spongepowered.despector.emitter.format.EmitterFormat.BracePosition;
import org.spongepowered.despector.emitter.format.EmitterFormat.WrappingStyle;
import org.spongepowered.despector.emitter.output.ImportManager;
import org.spongepowered.despector.emitter.special.AnnotationEmitter;
import org.spongepowered.despector.emitter.special.GenericsEmitter;
import org.spongepowered.despector.emitter.special.PackageEmitter;
import org.spongepowered.despector.emitter.special.PackageInfoEmitter;
import org.spongepowered.despector.util.TypeHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class EmitterContext {

    private final ImportManager import_manager = new ImportManager();
    private EmitterSet set;

    private EmitterFormat format;
    private Writer output;
    private Set<LocalInstance> defined_locals = Sets.newHashSet();

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

    private final Set<Class<? extends Statement>> block_statements = new HashSet<>();

    public EmitterContext(Writer output, EmitterFormat format) {
        this.output = output;
        this.format = format;

        this.block_statements.add(DoWhile.class);
        this.block_statements.add(While.class);
        this.block_statements.add(For.class);
        this.block_statements.add(ForEach.class);
        this.block_statements.add(If.class);
        this.block_statements.add(Switch.class);
        this.block_statements.add(TryCatch.class);
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

    public boolean usesSemicolons() {
        return this.semicolons;
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

    public void markBlockStatement(Class<? extends Statement> type) {
        this.block_statements.add(type);
    }

    public void emitOuterType(TypeEntry type) {
        if (type.getName().endsWith("package-info")) {
            PackageInfoEmitter emitter = this.set.getSpecialEmitter(PackageInfoEmitter.class);
            emitter.emit(this, (InterfaceEntry) type);
            return;
        }

        this.import_manager.reset();
        this.import_manager.calculateImports(type);
        this.outer_type = type;
        PackageEmitter pkg_emitter = this.set.getSpecialEmitter(PackageEmitter.class);
        String pkg = this.outer_type.getName();
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

        this.import_manager.emitImports(this);

        emit(type);
        this.outer_type = null;
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
        if (obj instanceof TypeEntry) {
            this.type = this.outer_type;
        } else if (obj instanceof FieldEntry) {
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
            if (this.block_statements.contains(insn.getClass())) {
                if (i < instructions.getStatementCount() - 1) {
                    if (instructions.getType() == StatementBlock.Type.METHOD && i == instructions.getStatementCount() - 2) {
                        if (((Return) instructions.getStatement(instructions.getStatementCount() - 1)).getValue().isPresent()) {
                            newLine();
                        }
                    } else {
                        newLine();
                    }
                }
            }
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

    public String getType(String name) {
        return getTypeName(TypeHelper.descToType(name));
    }

    public String getTypeName(String name) {
        if (name.endsWith("[]")) {
            String n = getTypeName(name.substring(0, name.length() - 2));
            if (this.format.insert_space_before_opening_bracket_in_array_type_reference) {
                n += " ";
            }
            if (this.format.insert_space_between_brackets_in_array_type_reference) {
                n += "[ ]";
            } else {
                n += "[]";
            }
            return n;
        }
        if (name.indexOf('/') != -1) {
            if (this.import_manager.checkImport(name)) {
                name = name.substring(name.lastIndexOf('/') + 1);
            } else if (this.type != null) {
                String this_package = "";
                String target_package = name;
                String this$name = this.type.getName();
                String outer_name = null;
                if (this$name.indexOf('/') != -1) {
                    this_package = this$name.substring(0, this$name.lastIndexOf('/'));
                    outer_name = this$name.substring(this_package.length() + 1);
                    if (outer_name.indexOf('$') != -1) {
                        outer_name = outer_name.substring(0, outer_name.indexOf('$'));
                    }
                    target_package = name.substring(0, name.lastIndexOf('/'));
                }
                if (this_package.equals(target_package)) {
                    name = name.substring(name.lastIndexOf('/') + 1);
                    if (name.startsWith(outer_name + "$")) {
                        name = name.substring(outer_name.length() + 1);
                    }
                }
            }
        }
        return name.replace('/', '.').replace('$', '.');
    }

    public EmitterContext emitType(TypeSignature sig) {
        GenericsEmitter generics = this.set.getSpecialEmitter(GenericsEmitter.class);
        generics.emitTypeSignature(this, sig);
        return this;
    }

    public EmitterContext emitType(String name) {
        emitTypeName(TypeHelper.descToType(name));
        return this;
    }

    public EmitterContext emitTypeName(String name) {
        printString(getTypeName(name));
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
            for (int i = 0; i < this.format.continuation_indentation; i++) {
                dedent();
            }
            this.is_wrapped = false;
        }
        return this;
    }

    public void flush() {
        try {
            this.output.write(this.line_buffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void __newLine() {
        flush();
        this.offs += 1;
        try {
            this.output.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.line_length = 0;
        this.wrap_point = -1;
        this.line_buffer.setLength(0);
    }

    public void newIndentedLine() {
        newLine();
        printIndentation();
    }

    public int getCurrentLength() {
        return this.line_length;
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
                    for (int i = 0; i < this.format.continuation_indentation; i++) {
                        indent();
                    }
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

    private void wrap(boolean indent) {
        __newLine();
        if (!this.is_wrapped && indent) {
            this.is_wrapped = true;
            for (int i = 0; i < this.format.continuation_indentation; i++) {
                indent();
            }
        }
        printIndentation();
    }

    public EmitterContext markWrapPoint(WrappingStyle style, int index) {
        switch (style) {
        case DO_NOT_WRAP:
            break;
        case WRAP_ALL:
            wrap(false);
            break;
        case WRAP_ALL_AND_INDENT:
            wrap(true);
            break;
        case WRAP_ALL_EXCEPT_FIRST:
            if (index != 0) {
                wrap(true);
            }
            break;
        case WRAP_FIRST_OR_NEEDED:
            if (index == 0) {
                wrap(true);
            } else {
                this.wrap_point = this.line_length;
            }
            break;
        case WRAP_WHEN_NEEDED:
            this.wrap_point = this.line_length;
        default:
            break;
        }
        return this;
    }

    public void emitBrace(BracePosition pos, boolean wrapped, boolean with_space) {
        switch (pos) {
        case NEXT_LINE:
            newLine();
            printIndentation();
            printString("{");
            indent();
            break;
        case NEXT_LINE_ON_WRAP:
            if (wrapped) {
                newLine();
                printIndentation();
            } else if (with_space) {
                printString(" ");
            }
            printString("{");
            indent();
            break;
        case NEXT_LINE_SHIFTED:
            newLine();
            indent();
            printIndentation();
            printString("{");
            break;
        case SAME_LINE:
        default:
            if (with_space) {
                printString(" ");
            }
            printString("{");
            indent();
            break;
        }
    }

}
