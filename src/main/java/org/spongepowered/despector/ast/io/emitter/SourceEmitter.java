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
package org.spongepowered.despector.ast.io.emitter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.io.emitter.format.EmitterFormat;
import org.spongepowered.despector.ast.io.insn.Locals.Local;
import org.spongepowered.despector.ast.io.insn.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.CastArg;
import org.spongepowered.despector.ast.members.insn.arg.CompareArg;
import org.spongepowered.despector.ast.members.insn.arg.InstanceOfArg;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.NewArrayArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.DoubleConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.FloatConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.LongConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.NullConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.StringConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.cst.TypeConstantArg;
import org.spongepowered.despector.ast.members.insn.arg.field.ArrayLoadArg;
import org.spongepowered.despector.ast.members.insn.arg.field.FieldArg;
import org.spongepowered.despector.ast.members.insn.arg.field.InstanceFieldArg;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalArg;
import org.spongepowered.despector.ast.members.insn.arg.field.StaticFieldArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.AddArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.NegArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.OperatorInstruction;
import org.spongepowered.despector.ast.members.insn.arg.operator.SubtractArg;
import org.spongepowered.despector.ast.members.insn.assign.ArrayAssignment;
import org.spongepowered.despector.ast.members.insn.assign.FieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.members.insn.branch.DoWhile;
import org.spongepowered.despector.ast.members.insn.branch.For;
import org.spongepowered.despector.ast.members.insn.branch.If;
import org.spongepowered.despector.ast.members.insn.branch.If.Elif;
import org.spongepowered.despector.ast.members.insn.branch.If.Else;
import org.spongepowered.despector.ast.members.insn.branch.Switch;
import org.spongepowered.despector.ast.members.insn.branch.Switch.Case;
import org.spongepowered.despector.ast.members.insn.branch.Ternary;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch.CatchBlock;
import org.spongepowered.despector.ast.members.insn.branch.While;
import org.spongepowered.despector.ast.members.insn.branch.condition.AndCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.ast.members.insn.branch.condition.InverseCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.OrCondition;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;
import org.spongepowered.despector.ast.members.insn.function.InvokeStatement;
import org.spongepowered.despector.ast.members.insn.function.New;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.ast.members.insn.misc.Increment;
import org.spongepowered.despector.ast.members.insn.misc.Return;
import org.spongepowered.despector.ast.members.insn.misc.Throw;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.util.ConditionUtil;
import org.spongepowered.despector.util.TypeHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An emitter which emits valid java source code.
 */
public class SourceEmitter implements ClassEmitter {

    private EmitterFormat format;
    private Writer output;
    private StringBuilder buffer = null;
    private Set<LocalInstance> defined_locals = Sets.newHashSet();
    private Set<String> imports = null;
    private TypeEntry this$ = null;
    private MethodEntry this$method;

    private int indentation = 0;

    public SourceEmitter(Writer output, EmitterFormat format) {
        this.output = output;
        this.format = format;
    }

    @Override
    public void emitType(TypeEntry type) {
        this.buffer = new StringBuilder();
        this.imports = Sets.newHashSet();
        this.this$ = type;

        // We set a buffer for this part and emit the entire class contents into
        // that buffer so that we can collect required imports as we go.

        if (type instanceof ClassEntry) {
            emitClass((ClassEntry) type);
        } else if (type instanceof EnumEntry) {
            emitEnum((EnumEntry) type);
        } else if (type instanceof InterfaceEntry) {
            emitInterface((InterfaceEntry) type);
        } else {
            throw new IllegalStateException();
        }
        printString("\n");

        // Then once we have finished emitting the class contents we detach the
        // buffer and then sort and emit the imports into the actual output and
        // then finally replay the buffer into the output.

        StringBuilder buf = this.buffer;
        this.buffer = null;

        String pkg = type.getName();
        int last = pkg.lastIndexOf('.');
        if (last != -1) {
            for (int i = 0; i < this.format.blank_lines_before_package; i++) {
                printString("\n");
            }
            pkg = pkg.substring(0, last);
            printString("package ");
            printString(pkg);
            printString(";\n");
        }
        int lines = Math.max(this.format.blank_lines_after_package, this.format.blank_lines_before_imports);
        for (int i = 0; i < lines; i++) {
            printString("\n");
        }

        emitImports();

        if (!this.imports.isEmpty()) {
            for (int i = 0; i < this.format.blank_lines_after_imports; i++) {
                printString("\n");
            }
        }

        // Replay the buffer.
        try {
            this.output.write(buf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.imports = null;
        this.buffer = null;
        this.this$ = null;
    }

    protected void emitImports() {
        List<String> imports = Lists.newArrayList(this.imports);
        for (String group : this.format.import_order) {
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
                printString(";\n");
            }
            if (!group_imports.isEmpty()) {
                for (int i = 0; i < this.format.blank_lines_between_import_groups; i++) {
                    printString("\n");
                }
            }
        }
    }

    protected void emitClass(ClassEntry type) {
        printString(type.getAccessModifier().asString());
        if (type.getAccessModifier() != AccessModifier.PACKAGE_PRIVATE) {
            printString(" ");
        }
//        if (type.isStatic()) {
//            printString("static ");
//        }
        if (type.isFinal()) {
            printString("final ");
        }
//        if (type.isAbstract()) {
//            printString("abstract ");
//        }
        printString("class ");
        String name = type.getName().replace('/', '.');
        if (name.indexOf('.') != -1) {
            name = name.substring(name.lastIndexOf('.') + 1, name.length());
        }
        name = name.replace('$', '.');
        printString(name);
        if (!type.getSuperclass().equals("Ljava/lang/Object;")) {
            printString(" extends ");
            emitTypeName(type.getSuperclassName());
        }
        if (!type.getInterfaces().isEmpty()) {
            printString(" implements ");
            for (int i = 0; i < type.getInterfaces().size(); i++) {
                emitType(type.getInterfaces().get(i));
                if (i < type.getInterfaces().size() - 1) {
                    if (this.format.insert_space_before_comma_in_superinterfaces) {
                        printString(" ");
                    }
                    printString(",");
                    if (this.format.insert_space_after_comma_in_superinterfaces) {
                        printString(" ");
                    }
                }
            }
        }
        if (this.format.insert_space_before_opening_brace_in_type_declaration) {
            printString(" ");
        }
        printString("{\n");
        for (int i = 0; i < this.format.blank_lines_before_first_class_body_declaration; i++) {
            printString("\n");
        }

        // Ordering is static fields -> static methods -> instance fields ->
        // instance methods

        this.indentation++;
        if (!type.getStaticFields().isEmpty()) {
            boolean at_least_one = false;
            for (FieldEntry field : type.getStaticFields()) {
                if (field.isSynthetic()) {
                    continue;
                }
                at_least_one = true;
                printIndentation();
                emitField(field);
                printString(";\n");
            }
            if (at_least_one) {
                printString("\n");
            }
        }
        if (!type.getStaticMethods().isEmpty()) {
            for (MethodEntry mth : type.getStaticMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                emitMethod(mth);
                printString("\n\n");
            }
        }
        if (!type.getFields().isEmpty()) {
            boolean at_least_one = false;
            for (FieldEntry field : type.getFields()) {
                if (field.isSynthetic()) {
                    continue;
                }
                at_least_one = true;
                printIndentation();
                emitField(field);
                printString(";\n");
            }
            if (at_least_one) {
                printString("\n");
            }
        }
        if (!type.getMethods().isEmpty()) {
            for (MethodEntry mth : type.getMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                if (emitMethod(mth)) {
                    printString("\n\n");
                }
            }
        }
        this.indentation--;

        printString("}");

    }

    protected void emitField(FieldEntry field) {
        printString(field.getAccessModifier().asString());
        printString(" ");
        if (field.isStatic()) {
            printString("static ");
        }
        if (field.isFinal()) {
            printString("final ");
        }
        emitTypeName(field.getTypeName());
        printString(" ");
        printString(field.getName());
    }

    protected void emitEnum(EnumEntry type) {
        printString(type.getAccessModifier().asString());
        if (type.getAccessModifier() != AccessModifier.PACKAGE_PRIVATE) {
            printString(" ");
        }
//        if (type.isStatic()) {
//            printString("static ");
//        }
        if (type.isFinal()) {
            printString("final ");
        }
        printString("enum ");
        String name = type.getName().replace('/', '.');
        if (name.indexOf('.') != -1) {
            name = name.substring(name.lastIndexOf('.') + 1, name.length());
        }
        name = name.replace('$', '.');
        printString(name);
        if (!type.getInterfaces().isEmpty()) {
            printString(" implements ");
            for (int i = 0; i < type.getInterfaces().size(); i++) {
                emitType(type.getInterfaces().get(i));
                if (i < type.getInterfaces().size() - 1) {
                    if (this.format.insert_space_before_comma_in_superinterfaces) {
                        printString(" ");
                    }
                    printString(",");
                    if (this.format.insert_space_after_comma_in_superinterfaces) {
                        printString(" ");
                    }
                }
            }
        }
        if (this.format.insert_space_before_opening_brace_in_type_declaration) {
            printString(" ");
        }
        printString("{\n\n");

        this.indentation++;

        // we look through the class initializer to find the enum constant
        // initializers so that we can emit those specially before the rest of
        // the class contents.

        MethodEntry clinit = type.getStaticMethod("<clinit>");
        List<Statement> remaining = Lists.newArrayList();
        Set<String> found = Sets.newHashSet();
        if (clinit != null) {
            Iterator<Statement> initializers = clinit.getInstructions().getStatements().iterator();
            boolean first = true;
            while (initializers.hasNext()) {
                Statement next = initializers.next();
                if (!(next instanceof StaticFieldAssignment)) {
                    break;
                }
                StaticFieldAssignment assign = (StaticFieldAssignment) next;
                if (!TypeHelper.descToType(assign.getOwnerType()).equals(type.getName()) || !(assign.getValue() instanceof New)) {
                    remaining.add(assign);
                    break;
                }
                if (!first) {
                    printString(",\n");
                }
                New val = (New) assign.getValue();
                printIndentation();
                printString(assign.getFieldName());
                found.add(assign.getFieldName());
                if (val.getParameters().length != 2) {
                    printString("(");
                    // TODO ctor params
                    for (int i = 2; i < val.getParameters().length; i++) {
                        emitArg(val.getParameters()[i], null);
                        if (i < val.getParameters().length - 1) {
                            printString(", ");
                        }
                    }
                    printString(")");
                }
                first = false;
            }
            if (!first) {
                printString(";\n");
            }
            // We store any remaining statements to be emitted later
            while (initializers.hasNext()) {
                remaining.add(initializers.next());
            }
        }
        if (!found.isEmpty()) {
            printString("\n");
        }

        if (!type.getStaticFields().isEmpty()) {
            boolean at_least_one = false;
            for (FieldEntry field : type.getStaticFields()) {
                if (field.isSynthetic()) {
                    // Skip the values array.
                    continue;
                }
                if (found.contains(field.getName())) {
                    // Skip the fields for any of the enum constants that we
                    // found earlier.
                    continue;
                }
                printIndentation();
                emitField(field);
                printString(";\n");
                at_least_one = true;
            }
            if (at_least_one) {
                printString("\n");
            }
        }
        if (!remaining.isEmpty()) {
            // if we found any additional statements in the class initializer
            // while looking for enum constants we emit them here
            printIndentation();
            printString("static {\n");
            this.indentation++;
            for (Statement stmt : remaining) {
                printIndentation();
                emitInstruction(stmt, true);
                printString("\n");
            }
            this.indentation--;
            printIndentation();
            printString("}\n");
        }
        if (!type.getStaticMethods().isEmpty()) {
            for (MethodEntry mth : type.getStaticMethods()) {
                if (mth.getName().equals("valueOf") || mth.getName().equals("values") || mth.getName().equals("<clinit>")) {
                    // Can skip these boilerplate methods and the class
                    // initializer
                    continue;
                } else if (mth.isSynthetic()) {
                    continue;
                }
                emitMethod(mth);
                printString("\n\n");
            }
        }
        if (!type.getFields().isEmpty()) {
            for (FieldEntry field : type.getFields()) {
                if (field.isSynthetic()) {
                    continue;
                }
                printIndentation();
                emitField(field);
                printString(";\n");
            }
            printString("\n");
        }
        if (!type.getMethods().isEmpty()) {
            for (MethodEntry mth : type.getMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                if (mth.getName().equals("<init>") && mth.getInstructions().getStatements().size() == 2) {
                    // If the initializer contains only two statement (which
                    // will be the invoke of the super constructor and the void
                    // return) then we can
                    // skip emitting it
                    continue;
                }
                emitMethod(mth);
                printString("\n\n");
            }
        }
        this.indentation--;

        printString("}");

    }

    protected void emitInterface(InterfaceEntry type) {
        String name = type.getName().replace('/', '.');
        if (name.indexOf('.') != -1) {
            name = name.substring(name.lastIndexOf('.') + 1, name.length());
        }
        name = name.replace('$', '.');
        if (!(name.contains(".") && type.getAccessModifier() == AccessModifier.PUBLIC)) {
            printString(type.getAccessModifier().asString());
            if (type.getAccessModifier() != AccessModifier.PACKAGE_PRIVATE) {
                printString(" ");
            }
        }
        printString("interface ");
        printString(name);
        if (!type.getInterfaces().isEmpty()) {
            printString(" extends ");
            for (int i = 0; i < type.getInterfaces().size(); i++) {
                emitType(type.getInterfaces().get(i));
                if (i < type.getInterfaces().size() - 1) {
                    if (this.format.insert_space_before_comma_in_superinterfaces) {
                        printString(" ");
                    }
                    printString(",");
                    if (this.format.insert_space_after_comma_in_superinterfaces) {
                        printString(" ");
                    }
                }
            }
        }
        if (this.format.insert_space_before_opening_brace_in_type_declaration) {
            printString(" ");
        }
        printString("{\n\n");

        this.indentation++;
        if (!type.getStaticFields().isEmpty()) {
            boolean at_least_one = false;
            for (FieldEntry field : type.getStaticFields()) {
                if (field.isSynthetic()) {
                    continue;
                }
                at_least_one = true;
                printIndentation();
                emitField(field);
                printString(";\n");
            }
            if (at_least_one) {
                printString("\n");
            }
        }
        if (!type.getStaticMethods().isEmpty()) {
            for (MethodEntry mth : type.getStaticMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                emitMethod(mth);
                printString("\n\n");
            }
        }
        if (!type.getMethods().isEmpty()) {
            for (MethodEntry mth : type.getMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                // TODO need something for emitting 'default' for default
                // methods
                emitMethod(mth);
                printString("\n\n");
            }
        }
        this.indentation--;

        printString("}");

    }

    protected boolean checkImport(String type) {
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

    @Override
    public boolean emitMethod(MethodEntry method) {
        this.this$method = method;
        if (method.getName().equals("<clinit>")) {
            printIndentation();
            printString("static {\n");
            this.indentation++;
            emitBody(method.getInstructions());
            printString("\n");
            this.indentation--;
            printIndentation();
            printString("}");
            return true;
        }
        if ("<init>".equals(method.getName()) && method.getAccessModifier() == AccessModifier.PUBLIC && method.getParamTypes().isEmpty()
                && method.getInstructions().getStatements().size() == 2) {
            return false;
        }
        printIndentation();
        if (!(this.this$ instanceof InterfaceEntry) && !(this.this$ instanceof EnumEntry && method.getName().equals("<init>"))) {
            printString(method.getAccessModifier().asString());
            if (method.getAccessModifier() != AccessModifier.PACKAGE_PRIVATE) {
                printString(" ");
            }
        }
        if ("<init>".equals(method.getName())) {
            emitTypeName(method.getOwnerName());
        } else {
            if (method.isStatic()) {
                printString("static ");
            }
            if (method.isFinal()) {
                printString("final ");
            }
            if (method.isAbstract() && !(this.this$ instanceof InterfaceEntry)) {
                printString("abstract ");
            }
            emitType(method.getReturnType());
            printString(" ");
            printString(method.getName());
        }
        printString("(");
        StatementBlock block = method.getInstructions();
        int start = 0;
        if ("<init>".equals(method.getName()) && this.this$ instanceof EnumEntry) {
            // If this is an enum type then we skip the first two ctor
            // parameters
            // (which are the index and name of the enum constant)
            start += 2;
        }
        for (int i = start; i < method.getParamTypes().size(); i++) {
            int param_index = i;
            if (!method.isStatic()) {
                param_index++;
            }
            emitType(method.getParamTypes().get(i));
            printString(" ");
            if (block == null) {
                printString("local" + param_index);
            } else {
                Local local = block.getLocals().getLocal(param_index);
                printString(local.getParameterInstance().getName());
            }
            if (i < method.getParamTypes().size() - 1) {
                printString(", ");
            }
        }
        printString(")");
        if (!method.isAbstract()) {
            printString(" {\n");
            this.indentation++;
            emitBody(block);
            printString("\n");
            this.indentation--;
            printIndentation();
            printString("}");
        } else {
            printString(";");
        }
        this.this$method = null;
        return true;
    }

    /**
     * Emits the given instruction block from this emitter.
     */
    public void emitBody(MethodEntry method, TypeEntry type) {
        this.this$ = type;
        this.this$method = method;
        emitBody(method.getInstructions());
        this.this$method = null;
        this.this$ = null;
    }

    public void emitBody(StatementBlock instructions) {
        if (instructions == null) {
            printIndentation();
            printString("// Error decompiling block");
            return;
        }
        this.defined_locals.clear();
        boolean last_success = false;
        for (int i = 0; i < instructions.getStatements().size(); i++) {
            Statement insn = instructions.getStatements().get(i);
            if (insn instanceof Return && !((Return) insn).getValue().isPresent()
                    && instructions.getType() == StatementBlock.Type.METHOD && i == instructions.getStatements().size() - 1) {
                break;
            }
            if (last_success) {
                printString("\n");
            }
            printIndentation();
            last_success = emitInstruction(insn, true);
        }
    }

    protected boolean emitInstruction(Statement insn, boolean withSemicolon) {
        boolean success = true;
        if (insn instanceof LocalAssignment) {
            emitLocalAssign((LocalAssignment) insn);
        } else if (insn instanceof InvokeStatement) {
            emitInvoke((InvokeStatement) insn);
        } else if (insn instanceof New) {
            emitNew((New) insn);
        } else if (insn instanceof Increment) {
            emitIinc((Increment) insn);
        } else if (insn instanceof If) {
            emitIfBlock((If) insn);
        } else if (insn instanceof ArrayAssignment) {
            emitArrayAssign((ArrayAssignment) insn);
        } else if (insn instanceof Return) {
            emitValueReturn((Return) insn);
        } else if (insn instanceof Throw) {
            emitThrow((Throw) insn);
        } else if (insn instanceof For) {
            emitForLoop((For) insn);
        } else if (insn instanceof While) {
            emitWhileLoop((While) insn);
        } else if (insn instanceof DoWhile) {
            emitDoWhileLoop((DoWhile) insn);
        } else if (insn instanceof FieldAssignment) {
            emitFieldAssign((FieldAssignment) insn);
        } else if (insn instanceof Switch) {
            emitTableSwitch((Switch) insn);
        } else if (insn instanceof TryCatch) {
            emitTryBlock((TryCatch) insn);
        } else {
            throw new IllegalStateException("Unknown statement: " + insn);
        }
        if (success && withSemicolon && !(insn instanceof For) && !(insn instanceof If) && !(insn instanceof While)
                && !(insn instanceof TryCatch) && !(insn instanceof Switch)) {
            printString(";");
        }
        return success;
    }

    protected void emitLocalAssign(LocalAssignment insn) {
        if (!insn.getLocal().getLocal().isParameter() && !this.defined_locals.contains(insn.getLocal())) {
            LocalInstance local = insn.getLocal();
            emitTypeName(local.getTypeName());
            if (local.getGenericTypes() != null) {
                printString("<");
                for (int i = 0; i < local.getGenericTypes().length; i++) {
                    emitTypeName(local.getGenericTypes()[i]);
                    if (i < local.getGenericTypes().length - 1) {
                        printString(",");
                    }
                }
                printString(">");
            }
            printString(" ");
            this.defined_locals.add(insn.getLocal());
        } else {
            // TODO replace with more generic handling from FieldAssign
            Instruction val = insn.getValue();
            if (val instanceof CastArg) {
                val = ((CastArg) val).getValue();
            } else if (val instanceof AddArg) {
                AddArg add = (AddArg) val;
                if (add.getLeftOperand() instanceof LocalArg) {
                    LocalArg local = (LocalArg) add.getLeftOperand();
                    if (local.getLocal().getIndex() == insn.getLocal().getIndex()) {
                        printString(insn.getLocal().getName());
                        if (add.getRightOperand() instanceof IntConstantArg) {
                            IntConstantArg right = (IntConstantArg) add.getRightOperand();
                            if (right.getConstant() == 1) {
                                printString("++");
                                return;
                            } else if (right.getConstant() == -1) {
                                printString("--");
                                return;
                            }
                        }
                        printString(" += ");
                        emitArg(add.getRightOperand(), null);
                        return;
                    }
                }
            } else if (val instanceof SubtractArg) {
                SubtractArg sub = (SubtractArg) val;
                if (sub.getLeftOperand() instanceof LocalArg) {
                    LocalArg local = (LocalArg) sub.getLeftOperand();
                    if (local.getLocal().getIndex() == insn.getLocal().getIndex()) {
                        printString(insn.getLocal().getName());
                        if (sub.getRightOperand() instanceof IntConstantArg) {
                            IntConstantArg right = (IntConstantArg) sub.getRightOperand();
                            if (right.getConstant() == 1) {
                                printString("--");
                                return;
                            } else if (right.getConstant() == -1) {
                                printString("++");
                                return;
                            }
                        }
                        printString(" += ");
                        emitArg(sub.getRightOperand(), local.getLocal().getType());
                        return;
                    }
                }
            }
        }
        printString(insn.getLocal().getName());
        printString(" = ");
        emitArg(insn.getValue(), insn.getLocal().getType());
    }

    protected void emitFieldAssign(FieldAssignment insn) {
        if (insn instanceof StaticFieldAssignment) {
            if (!((StaticFieldAssignment) insn).getOwnerType().equals(this.this$.getDescriptor())) {
                emitTypeName(((StaticFieldAssignment) insn).getOwnerName());
                printString(".");
            }
        } else if (insn instanceof InstanceFieldAssignment) {
            emitArg(((InstanceFieldAssignment) insn).getOwner(), insn.getOwnerType());
            printString(".");
        }

        printString(insn.getFieldName());
        Instruction val = insn.getValue();
        if (val instanceof OperatorInstruction) {
            Instruction left = ((OperatorInstruction) val).getLeftOperand();
            Instruction right = ((OperatorInstruction) val).getRightOperand();
            String op = " " + ((OperatorInstruction) val).getOperator() + "= ";
            if (left instanceof InstanceFieldArg) {
                InstanceFieldArg left_field = (InstanceFieldArg) left;
                Instruction owner = left_field.getFieldOwner();
                if (owner instanceof LocalArg && ((LocalArg) owner).getLocal().getIndex() == 0) {
                    // If the field assign is of the form 'field = field + x'
                    // where + is any operator then we collapse it to the '+='
                    // form of the assignment.
                    if (left_field.getFieldName().equals(insn.getFieldName())) {
                        printString(op);
                        if (insn.getFieldDescription().equals("Z")) {
                            if (val instanceof IntConstantArg) {
                                IntConstantArg cst = (IntConstantArg) insn.getValue();
                                if (cst.getConstant() == 1) {
                                    printString("true");
                                } else {
                                    printString("false");
                                }
                                return;
                            }
                        }
                        emitArg(right, insn.getFieldDescription());
                        return;
                    }
                }
            }
        }
        printString(" = ");
        emitArg(val, insn.getFieldDescription());
    }

    protected void emitInvoke(InvokeStatement insn) {
        Instruction i = insn.getInstruction();
        if (i instanceof New) {
            emitNew((New) i);
        } else if (i instanceof InstanceMethodInvoke) {
            emitInstanceMethodInvoke((InstanceMethodInvoke) i);
        } else if (i instanceof StaticMethodInvoke) {
            emitStaticMethodInvoke((StaticMethodInvoke) i);
        }
    }

    protected void emitIinc(Increment insn) {
        printString(insn.getLocal().getName());
        if (insn.getIncrementValue() == 1) {
            printString("++");
            return;
        } else if (insn.getIncrementValue() == -1) {
            printString("--");
            return;
        }
        printString(" += ");
        printString(String.valueOf(insn.getIncrementValue()));
    }

    protected void emitIfBlock(If insn) {
        printString("if (");
        emitCondition(insn.getCondition());
        printString(") {\n");
        if (!insn.getIfBody().getStatements().isEmpty()) {
            this.indentation++;
            emitBody(insn.getIfBody());
            this.indentation--;
            printString("\n");
        }
        printIndentation();
        Else else_ = insn.getElseBlock();
        for (int i = 0; i < insn.getElifBlocks().size(); i++) {
            Elif elif = insn.getElifBlocks().get(i);
            printString("} else if (");
            emitCondition(elif.getCondition());
            printString(") {\n");
            this.indentation++;
            emitBody(elif.getBody());
            this.indentation--;
            printString("\n");
            printIndentation();
        }
        if (else_ == null) {
            printString("}");
        } else {
            StatementBlock else_block = else_.getElseBody();
            printString("} else {\n");
            if (!else_block.getStatements().isEmpty()) {
                this.indentation++;
                emitBody(else_block);
                this.indentation--;
                printString("\n");
            }
            printIndentation();
            printString("}");
        }
    }

    protected void emitForLoop(For loop) {
        printString("for (");
        if (loop.getInit() != null) {
            emitInstruction(loop.getInit(), false);
        }
        printString("; ");
        emitCondition(loop.getCondition());
        printString("; ");
        if (loop.getIncr() != null) {
            emitInstruction(loop.getIncr(), false);
        }
        printString(") {\n");
        if (!loop.getBody().getStatements().isEmpty()) {
            this.indentation++;
            emitBody(loop.getBody());
            this.indentation--;
            printString("\n");
        }
        printIndentation();
        printString("}");
    }

    protected void emitWhileLoop(While loop) {
        printString("while (");
        emitCondition(loop.getCondition());
        printString(") {\n");
        if (!loop.getBody().getStatements().isEmpty()) {
            this.indentation++;
            emitBody(loop.getBody());
            this.indentation--;
            printString("\n");
        }
        printIndentation();
        printString("}");
    }

    protected void emitDoWhileLoop(DoWhile loop) {
        printString("do {\n");
        if (!loop.getBody().getStatements().isEmpty()) {
            this.indentation++;
            emitBody(loop.getBody());
            this.indentation--;
            printString("\n");
        }
        printIndentation();
        printString("} while (");
        emitCondition(loop.getCondition());
        printString(")");
    }

    private Map<Integer, String> buildSwitchTable(MethodEntry mth) {
        Map<Integer, String> table = Maps.newHashMap();

        for (Statement stmt : mth.getInstructions().getStatements()) {
            if (stmt instanceof TryCatch) {
                TryCatch next = (TryCatch) stmt;
                ArrayAssignment assign = (ArrayAssignment) next.getTryBlock().getStatements().get(0);
                int jump_index = ((IntConstantArg) assign.getValue()).getConstant();
                InstanceMethodInvoke ordinal = (InstanceMethodInvoke) assign.getIndex();
                StaticFieldArg callee = (StaticFieldArg) ordinal.getCallee();
                table.put(jump_index, callee.getFieldName());
            }
        }

        return table;
    }

    protected void emitTableSwitch(Switch tswitch) {
        Map<Integer, String> table = null;
        printString("switch (");
        boolean synthetic = false;
        if (tswitch.getSwitchVar() instanceof ArrayLoadArg) {
            ArrayLoadArg var = (ArrayLoadArg) tswitch.getSwitchVar();
            if (var.getArrayVar() instanceof StaticMethodInvoke) {
                StaticMethodInvoke arg = (StaticMethodInvoke) var.getArrayVar();
                if (arg.getMethodName().contains("$SWITCH_TABLE$") && this.this$ != null) {
                    MethodEntry mth = this.this$.getStaticMethod(arg.getMethodName(), arg.getMethodDescription());
                    table = buildSwitchTable(mth);
                    String enum_type = arg.getMethodName().substring("$SWITCH_TABLE$".length()).replace('$', '/');
                    emitArg(((InstanceMethodInvoke) var.getIndex()).getCallee(), "L" + enum_type + ";");
                    synthetic = true;
                }
            }
        }
        if (!synthetic) {
            emitArg(tswitch.getSwitchVar(), "I");
        }
        printString(") {\n");
        for (Case cs : tswitch.getCases()) {
            for (int i = 0; i < cs.getIndices().size(); i++) {
                printIndentation();
                printString("case ");
                int index = cs.getIndices().get(i);
                if (table != null) {
                    String label = table.get(index);
                    if (label == null) {
                        printString(String.valueOf(index));
                    } else {
                        printString(label);
                    }
                } else {
                    printString(String.valueOf(cs.getIndices().get(i)));
                }
                printString(":\n");
            }
            if (cs.isDefault()) {
                printIndentation();
                printString("default:\n");
            }
            this.indentation++;
            emitBody(cs.getBody());
            if (!cs.getBody().getStatements().isEmpty()) {
                printString("\n");
            }
            if (cs.doesBreak()) {
                printIndentation();
                printString("break;");
                printString("\n");
            }
            this.indentation--;
        }
        printIndentation();
        printString("}");
    }

    protected void emitArrayAssign(ArrayAssignment insn) {
        // TODO need equality methods for all ast elements to do this
        // optimization
//        InsnArg val = insn.getValue();
//        if (val instanceof CastArg) {
//            val = ((CastArg) val).getVal();
//        }
//        if (val instanceof AddArg) {
//            AddArg add = (AddArg) val;
//            if (add.getLeft() instanceof LocalArg) {
//                LocalArg local = (LocalArg) add.getLeft();
//                if (local.getLocal().getIndex() == insn.getLocal().getIndex()) {
//                    printString(insn.getLocal().getName());
//                    printString(" += ");
//                    emitArg(add.getRight());
//                    printString(";");
//                    return;
//                }
//            }
//        }
//        if (val instanceof SubArg) {
//            SubArg sub = (SubArg) val;
//            if (sub.getLeft() instanceof LocalArg) {
//                LocalArg local = (LocalArg) sub.getLeft();
//                if (local.getLocal().getIndex() == insn.getLocal().getIndex()) {
//                    printString(insn.getLocal().getName());
//                    printString(" += ");
//                    emitArg(sub.getRight());
//                    printString(";");
//                    return;
//                }
//            }
//        }
        emitArg(insn.getArray(), null);
        printString("[");
        emitArg(insn.getIndex(), "I");
        printString("] = ");
        emitArg(insn.getValue(), null);
    }

    protected void emitValueReturn(Return insn) {
        printString("return");
        if (insn.getValue().isPresent()) {
            String type = null;
            if (this.this$method != null) {
                type = TypeHelper.getRet(this.this$method.getSignature());
            }
            printString(" ");
            emitArg(insn.getValue().get(), type);
        }
    }

    protected void emitThrow(Throw insn) {
        printString("throw ");
        emitArg(insn.getException(), null);
    }

    protected void emitTryBlock(TryCatch try_block) {
        printString("try {\n");
        this.indentation++;
        emitBody(try_block.getTryBlock());
        this.indentation--;
        printString("\n");
        for (CatchBlock c : try_block.getCatchBlocks()) {
            printIndentation();
            printString("} catch (");
            for (int i = 0; i < c.getExceptions().size(); i++) {
                emitType(c.getExceptions().get(i));
                if (i < c.getExceptions().size() - 1) {
                    printString(" | ");
                }
            }
            printString(" ");
            if (c.getExceptionLocal() != null) {
                printString(c.getExceptionLocal().getName());
                this.defined_locals.add(c.getExceptionLocal());
            } else {
                printString(c.getDummyName());
            }
            printString(") {\n");
            this.indentation++;
            emitBody(c.getBlock());
            this.indentation--;
            printString("\n");
        }
        printIndentation();
        printString("}");
    }

    protected void emitArg(Instruction arg, String inferred_type) {
        if (arg instanceof StringConstantArg) {
            printString("\"");
            printString(((StringConstantArg) arg).getConstant());
            printString("\"");
        } else if (arg instanceof FieldArg) {
            emitFieldArg((FieldArg) arg);
        } else if (arg instanceof InstanceMethodInvoke) {
            emitInstanceMethodInvoke((InstanceMethodInvoke) arg);
        } else if (arg instanceof LocalArg) {
            emitLocalArg((LocalArg) arg);
        } else if (arg instanceof New) {
            emitNew((New) arg);
        } else if (arg instanceof StaticMethodInvoke) {
            emitStaticMethodInvoke((StaticMethodInvoke) arg);
        } else if (arg instanceof OperatorInstruction) {
            emitOperator((OperatorInstruction) arg);
        } else if (arg instanceof CastArg) {
            emitCastArg((CastArg) arg);
        } else if (arg instanceof NegArg) {
            emitNegArg((NegArg) arg);
        } else if (arg instanceof NewArrayArg) {
            emitNewArray((NewArrayArg) arg);
        } else if (arg instanceof ArrayLoadArg) {
            emitArrayLoad((ArrayLoadArg) arg);
        } else if (arg instanceof NullConstantArg) {
            printString("null");
        } else if (arg instanceof IntConstantArg) {
            emitIntConstant(((IntConstantArg) arg).getConstant(), inferred_type);
        } else if (arg instanceof LongConstantArg) {
            printString(String.valueOf(((LongConstantArg) arg).getConstant()));
        } else if (arg instanceof FloatConstantArg) {
            printString(String.valueOf(((FloatConstantArg) arg).getConstant()));
        } else if (arg instanceof DoubleConstantArg) {
            printString(String.valueOf(((DoubleConstantArg) arg).getConstant()));
        } else if (arg instanceof TypeConstantArg) {
            emitTypeClassName(((TypeConstantArg) arg).getConstant().getClassName());
            printString(".class");
        } else if (arg instanceof Ternary) {
            emitTernary((Ternary) arg, inferred_type);
        } else if (arg instanceof CompareArg) {
            // A fallback, compare args should be optimized out of conditions
            // where they commonly appear
            printString("Integer.signum(");
            emitArg(((CompareArg) arg).getRightOperand(), arg.inferType());
            printString(" - ");
            emitArg(((CompareArg) arg).getLeftOperand(), arg.inferType());
            printString(")");
        } else if (arg instanceof InstanceOfArg) {
            emitInstanceOf((InstanceOfArg) arg);
        } else {
            throw new IllegalStateException("Unknown arg type " + arg.getClass().getName() + " : " + arg.toString());
        }
    }

    protected void emitType(String name) {
        emitTypeClassName(TypeHelper.descToType(name).replace('/', '.'));
    }

    protected void emitTypeName(String name) {
        emitTypeClassName(name.replace('/', '.'));
    }

    protected void emitTypeClassName(String name) {
        if (name.endsWith("[]")) {
            emitTypeClassName(name.substring(0, name.length() - 2));
            printString("[]");
            return;
        }
        if (name.indexOf('.') != -1) {
            if (name.startsWith("java.lang.")) {
                name = name.substring("java.lang.".length());
            } else if (this.this$ != null) {
                String this_package = "";
                String target_package = name;
                String this$name = this.this$.getName().replace('/', '.');
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
    }

    protected void emitIntConstant(int cst, String inferred_type) {
        // Some basic constant replacement, TODO should probably make this
        // better
        if (cst == Integer.MAX_VALUE) {
            printString("Integer.MAX_VALUE");
            return;
        } else if (cst == Integer.MIN_VALUE) {
            printString("Integer.MIN_VALUE");
            return;
        }
        if ("Z".equals(inferred_type)) {
            if (cst == 0) {
                printString("false");
            } else {
                printString("true");
            }
            return;
        }
        printString(String.valueOf(cst));
    }

    protected void emitFieldArg(FieldArg arg) {
        if (arg instanceof StaticFieldArg) {
            emitTypeName(((StaticFieldArg) arg).getOwnerName());
            printString(".");
            printString(arg.getFieldName());
        } else if (arg instanceof InstanceFieldArg) {
            emitArg(((InstanceFieldArg) arg).getFieldOwner(), arg.getOwner());
            printString(".");
            printString(arg.getFieldName());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    protected void emitInstanceMethodInvoke(InstanceMethodInvoke arg) {
        if (arg.getOwner().equals("Ljava/lang/StringBuilder;") && arg.getMethodName().equals("toString")) {
            // We detect and collapse string builder chains used to perform
            // string concatentation into simple "foo" + "bar" form
            boolean valid = true;
            Instruction callee = arg.getCallee();
            List<Instruction> constants = Lists.newArrayList();
            // We add all the constants to the front of this list as we have to
            // replay them in the reverse of the ordering that we will encounter
            // them in
            while (callee != null) {
                if (callee instanceof InstanceMethodInvoke) {
                    InstanceMethodInvoke call = (InstanceMethodInvoke) callee;
                    if (call.getParams().length == 1) {
                        constants.add(0, call.getParams()[0]);
                        callee = call.getCallee();
                        continue;
                    }
                } else if (callee instanceof New) {
                    New ref = (New) callee;
                    if ("Ljava/lang/StringBuilder;".equals(ref.getType())) {
                        if (ref.getParameters().length == 1) {
                            Instruction initial = ref.getParameters()[0];
                            if (initial instanceof StaticMethodInvoke) {
                                StaticMethodInvoke valueof = (StaticMethodInvoke) initial;
                                if (valueof.getMethodName().equals("valueOf") && valueof.getOwner().equals("Ljava/lang/String;")) {
                                    Instruction internal = valueof.getParams()[0];
                                    if (internal instanceof StringConstantArg) {
                                        initial = internal;
                                    } else if (internal instanceof LocalArg) {
                                        LocalArg local = (LocalArg) internal;
                                        if (local.getLocal().getType().equals("Ljava/lang/String;")) {
                                            initial = local;
                                        }
                                    }
                                }
                            }
                            constants.add(0, initial);
                        }
                        break;
                    }
                    valid = false;
                    break;
                }
                valid = false;
            }
            if (valid) {
                for (int i = 0; i < constants.size(); i++) {
                    emitArg(constants.get(i), "Ljava/lang/String;");
                    if (i < constants.size() - 1) {
                        printString(" + ");
                    }
                }
                return;
            }
        }
        if(arg.getMethodName().equals("<init>")) {
            if(this.this$ != null) {
                if(arg.getOwner().equals(this.this$.getName())) {
                    printString("this");
                } else {
                    printString("super");
                }
            } else {
                printString("super");
            }
        } else {
            emitArg(arg.getCallee(), arg.getOwner());
            printString(".");
            printString(arg.getMethodName());
        }
        printString("(");
        // TODO get param types if we have the ast
        for (int i = 0; i < arg.getParams().length; i++) {
            Instruction param = arg.getParams()[i];
            emitArg(param, null);
            if (i < arg.getParams().length - 1) {
                printString(", ");
            }
        }
        printString(")");
    }

    protected void emitLocalArg(LocalArg arg) {
        printString(arg.getLocal().getName());
    }

    protected void emitNew(New arg) {
        printString("new ");
        emitType(arg.getType());
        printString("(");
        // TODO get param types if we have the ast
        for (int i = 0; i < arg.getParameters().length; i++) {
            Instruction param = arg.getParameters()[i];
            emitArg(param, null);
            if (i < arg.getParameters().length - 1) {
                printString(", ");
            }
        }
        printString(")");
    }

    protected void emitStaticMethodInvoke(StaticMethodInvoke arg) {
        String owner = TypeHelper.descToType(arg.getOwner());
        if (arg.getMethodName().startsWith("access$") && this.this$ != null) {
            // synthetic accessor
            // we resolve these to the field that they are accessing directly
            TypeEntry owner_type = this.this$.getSource().get(owner);
            if (owner_type != null) {
                MethodEntry accessor = owner_type.getStaticMethod(arg.getMethodName());
                if (accessor.getReturnType().equals("V")) {
                    // setter
                    FieldAssignment assign = (FieldAssignment) accessor.getInstructions().getStatements().get(0);
                    FieldAssignment replacement = null;
                    if (arg.getParams().length == 2) {
                        replacement = new InstanceFieldAssignment(assign.getFieldName(), assign.getFieldDescription(), assign.getOwnerType(),
                                arg.getParams()[0], arg.getParams()[1]);
                    } else {
                        replacement = new StaticFieldAssignment(assign.getFieldName(), assign.getFieldDescription(), assign.getOwnerType(),
                                arg.getParams()[0]);
                    }
                    emitFieldAssign(replacement);
                    return;
                }
                // getter
                Return ret = (Return) accessor.getInstructions().getStatements().get(0);
                FieldArg getter = (FieldArg) ret.getValue().get();
                FieldArg replacement = null;
                if (arg.getParams().length == 1) {
                    replacement = new InstanceFieldArg(getter.getFieldName(), getter.getTypeDescriptor(), getter.getOwner(), arg.getParams()[0]);
                } else {
                    replacement = new StaticFieldArg(getter.getFieldName(), getter.getTypeDescriptor(), getter.getOwner());
                }
                emitFieldArg(replacement);
                return;
            }
        }
        emitTypeName(owner);
        printString(".");
        printString(arg.getMethodName());
        printString("(");
        // TODO get param types if we have the ast
        for (int i = 0; i < arg.getParams().length; i++) {
            Instruction param = arg.getParams()[i];
            emitArg(param, null);
            if (i < arg.getParams().length - 1) {
                printString(", ");
            }
        }
        printString(")");
    }

    protected void emitOperator(OperatorInstruction arg) {
        emitArg(arg.getLeftOperand(), null);
        printString(" " + arg.getOperator() + " ");
        emitArg(arg.getRightOperand(), null);
    }

    protected void emitCastArg(CastArg arg) {
        printString("((");
        emitType(arg.getType());
        printString(") ");
        emitArg(arg.getValue(), null);
        printString(")");
    }

    protected void emitNegArg(NegArg arg) {
        printString("-");
        if (arg.getOperand() instanceof OperatorInstruction) {
            printString("(");
            emitArg(arg.getOperand(), null);
            printString(")");
        } else {
            emitArg(arg.getOperand(), null);
        }
    }

    protected void emitNewArray(NewArrayArg arg) {
        printString("new ");
        emitType(arg.getType());
        if (arg.getInitializer().length == 0) {
            printString("[");
            emitArg(arg.getSize(), "I");
            printString("]");
        } else {
            printString("[] {");
            for (int i = 0; i < arg.getInitializer().length; i++) {
                emitArg(arg.getInitializer()[i], arg.getType());
                if (i < arg.getInitializer().length - 1) {
                    printString(", ");
                }
            }
            printString("}");
        }
    }

    protected void emitArrayLoad(ArrayLoadArg arg) {
        emitArg(arg.getArrayVar(), null);
        printString("[");
        emitArg(arg.getIndex(), "I");
        printString("]");
    }

    protected void emitInstanceOf(InstanceOfArg arg) {
        emitArg(arg.getCheckedValue(), null);
        printString(" instanceof ");
        emitType(arg.getType());
    }

    protected void emitCondition(Condition condition) {
        if (condition instanceof BooleanCondition) {
            BooleanCondition bool = (BooleanCondition) condition;
            if (bool.isInverse()) {
                printString("!");
            }
            emitArg(bool.getConditionValue(), "Z");
        } else if (condition instanceof InverseCondition) {
            InverseCondition inv = (InverseCondition) condition;
            Condition cond = inv.getConditionValue();
            if (cond instanceof InverseCondition) {
                emitCondition(((InverseCondition) cond).getConditionValue());
                return;
            } else if (cond instanceof BooleanCondition) {
                BooleanCondition bool = (BooleanCondition) cond;
                if (!bool.isInverse()) {
                    printString("!");
                }
                emitArg(bool.getConditionValue(), "Z");
                return;
            } else if (cond instanceof CompareCondition) {
                CompareCondition compare = (CompareCondition) cond;
                emitArg(compare.getLeft(), null);
                printString(compare.getOperator().inverse().asString());
                emitArg(compare.getRight(), null);
                return;
            }
            printString("!");
            printString("(");
            emitCondition(cond);
            printString(")");
        } else if (condition instanceof CompareCondition) {
            CompareCondition compare = (CompareCondition) condition;
            emitArg(compare.getLeft(), null);
            printString(compare.getOperator().asString());
            emitArg(compare.getRight(), null);
        } else if (condition instanceof AndCondition) {
            AndCondition and = (AndCondition) condition;
            for (int i = 0; i < and.getOperands().size(); i++) {
                Condition cond = and.getOperands().get(i);
                if (cond instanceof OrCondition) {
                    printString("(");
                    emitCondition(cond);
                    printString(")");
                } else {
                    emitCondition(cond);
                }
                if (i < and.getOperands().size() - 1) {
                    printString(" && ");
                }
            }
        } else if (condition instanceof OrCondition) {
            OrCondition and = (OrCondition) condition;
            for (int i = 0; i < and.getOperands().size(); i++) {
                emitCondition(and.getOperands().get(i));
                if (i < and.getOperands().size() - 1) {
                    printString(" || ");
                }
            }
        } else {
            throw new IllegalArgumentException("Unknown condition type " + condition.getClass());
        }
    }

    protected void emitTernary(Ternary ternary, String inferred_type) {
        if ("Z".equals(inferred_type) && ternary.getTrueValue() instanceof IntConstantArg && ternary.getFalseValue() instanceof IntConstantArg) {
            // if the ternary contains simple boolean constants on both sides
            // then we can simplify it to simply be the condition
            IntConstantArg true_value = (IntConstantArg) ternary.getTrueValue();
            IntConstantArg false_value = (IntConstantArg) ternary.getFalseValue();
            if (true_value.getConstant() == 1 && false_value.getConstant() == 0) {
                emitCondition(ternary.getCondition());
                return;
            } else if (true_value.getConstant() == 0 && false_value.getConstant() == 1) {
                emitCondition(ConditionUtil.inverse(ternary.getCondition()));
                return;
            }
        }
        if (ternary.getCondition() instanceof CompareCondition) {
            printString("(");
            emitCondition(ternary.getCondition());
            printString(")");
        } else {
            emitCondition(ternary.getCondition());
        }
        printString(" ? ");
        emitArg(ternary.getTrueValue(), inferred_type);
        printString(" : ");
        emitArg(ternary.getFalseValue(), inferred_type);
    }

    protected void printIndentation() {
        if (this.format.indent_with_spaces) {
            for (int i = 0; i < this.indentation * this.format.indentation_size; i++) {
                printString(" ");
            }
        } else {
            for (int i = 0; i < this.indentation; i++) {
                printString("\t");
            }
        }
    }

    protected void printString(String line) {
        if (this.buffer == null) {
            try {
                this.output.write(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.buffer.append(line);
        }
    }

}
