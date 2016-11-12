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
import com.google.common.collect.Sets;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.io.insn.Locals.Local;
import org.spongepowered.despector.ast.io.insn.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.ast.members.insn.arg.CastArg;
import org.spongepowered.despector.ast.members.insn.arg.InstanceFunctionArg;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.NewArrayArg;
import org.spongepowered.despector.ast.members.insn.arg.NewRefArg;
import org.spongepowered.despector.ast.members.insn.arg.StaticFunctionArg;
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
import org.spongepowered.despector.ast.members.insn.arg.operator.OperatorArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.SubtractArg;
import org.spongepowered.despector.ast.members.insn.assign.ArrayAssign;
import org.spongepowered.despector.ast.members.insn.assign.FieldAssign;
import org.spongepowered.despector.ast.members.insn.assign.InstanceFieldAssign;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssign;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssign;
import org.spongepowered.despector.ast.members.insn.branch.CatchBlock;
import org.spongepowered.despector.ast.members.insn.branch.DoWhileLoop;
import org.spongepowered.despector.ast.members.insn.branch.ElseBlock;
import org.spongepowered.despector.ast.members.insn.branch.ForLoop;
import org.spongepowered.despector.ast.members.insn.branch.IfBlock;
import org.spongepowered.despector.ast.members.insn.branch.TableSwitch;
import org.spongepowered.despector.ast.members.insn.branch.TableSwitch.Case;
import org.spongepowered.despector.ast.members.insn.branch.Ternary;
import org.spongepowered.despector.ast.members.insn.branch.TryBlock;
import org.spongepowered.despector.ast.members.insn.branch.WhileLoop;
import org.spongepowered.despector.ast.members.insn.branch.condition.AndCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.ast.members.insn.branch.condition.InverseCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.OrCondition;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodCall;
import org.spongepowered.despector.ast.members.insn.function.NewInstance;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodCall;
import org.spongepowered.despector.ast.members.insn.misc.IncrementStatement;
import org.spongepowered.despector.ast.members.insn.misc.ReturnValue;
import org.spongepowered.despector.ast.members.insn.misc.ReturnVoid;
import org.spongepowered.despector.ast.members.insn.misc.ThrowException;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.util.TypeHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An emitter which emits valid java source code.
 */
public class SourceEmitter implements ClassEmitter {

    // TODO rewrite as Vistor pattern

    private static final String[] indentations = new String[] {"", "    ", "        ", "            ", "                ", "                    ",};

    private Writer output;
    private StringBuilder buffer = null;
    private Set<LocalInstance> defined_locals = Sets.newHashSet();
    private Set<String> imports = null;
    private TypeEntry this$ = null;
    private MethodEntry this$method;

    private int indentation = 0;

    public SourceEmitter(Writer output) {
        this.output = output;
    }

    // @Expansion: Should make this follow a configurable format (perhaps an
    // eclipse formatter export file)

    @Override
    public void emitType(TypeEntry type) {
        this.buffer = new StringBuilder();
        this.imports = Sets.newHashSet();
        this.this$ = type;

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
        List<String> sorted_imports = Lists.newArrayList(this.imports);
        Collections.sort(sorted_imports);

        StringBuilder buf = this.buffer;
        this.buffer = null;

        String pkg = type.getName();
        int last = pkg.lastIndexOf('.');
        if (last != -1) {
            pkg = pkg.substring(0, last);
            printString("package ");
            printString(pkg);
            printString(";\n\n");
        }

        for (String import_ : sorted_imports) {
            printString("import ");
            printString(import_);
            printString(";\n");
        }
        if (!sorted_imports.isEmpty()) {
            printString("\n");
        }

        try {
            this.output.write(buf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.imports = null;
        this.buffer = null;
        this.this$ = null;
    }

    protected void emitClass(ClassEntry type) {
        printString(type.getAccessModifier().identifier());
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
        String name = type.getName();
        if (name.indexOf('.') != -1) {
            name = name.substring(name.lastIndexOf('.') + 1, name.length());
        }
        name = name.replace('$', '.');
        printString(name);
        printString(" ");
        if (!type.getSuperclass().equals("Ljava/lang/Object;")) {
            printString("extends ");
            emitTypeName(type.getSuperclassName());
            printString(" ");
        }
        if (!type.getInterfaces().isEmpty()) {
            printString("implements ");
            for (int i = 0; i < type.getInterfaces().size(); i++) {
                emitType(type.getInterfaces().get(i));
                if (i < type.getInterfaces().size() - 1) {
                    printString(", ");
                }
            }
            printString(" ");
        }
        printString("{\n\n");

        this.indentation++;
        if (!type.getStaticFields().isEmpty()) {
            for (FieldEntry field : type.getStaticFields()) {
                printIndentation();
                emitField(field);
                printString(";\n");
            }
            printString("\n");
        }
        if (!type.getStaticMethods().isEmpty()) {
            for (MethodEntry mth : type.getStaticMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                printIndentation();
                emitMethod(mth);
                printString("\n\n");
            }
        }
        if (!type.getFields().isEmpty()) {
            for (FieldEntry field : type.getFields()) {
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
                printIndentation();
                emitMethod(mth);
                printString("\n\n");
            }
        }
        this.indentation--;

        printString("}");

    }

    protected void emitField(FieldEntry field) {
        printString(field.getAccessModifier().identifier());
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
        printString(type.getAccessModifier().identifier());
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
        String name = type.getName();
        if (name.indexOf('.') != -1) {
            name = name.substring(name.lastIndexOf('.') + 1, name.length());
        }
        name = name.replace('$', '.');
        printString(name);
        printString(" ");
        if (!type.getInterfaces().isEmpty()) {
            printString(" implements");
            for (int i = 0; i < type.getInterfaces().size(); i++) {
                emitType(type.getInterfaces().get(i));
                if (i < type.getInterfaces().size() - 1) {
                    printString(", ");
                }
            }
            printString(" ");
        }
        printString("{\n\n");

        this.indentation++;

        MethodEntry clinit = type.getStaticMethod("<clinit>");
        List<Statement> remaining = Lists.newArrayList();
        Set<String> found = Sets.newHashSet();
        if (clinit != null) {
            Iterator<Statement> initializers = clinit.getInstructions().getStatements().iterator();
            while (initializers.hasNext()) {
                Statement next = initializers.next();
                if (!(next instanceof StaticFieldAssign)) {
                    break;
                }
                StaticFieldAssign assign = (StaticFieldAssign) next;
                if (!TypeHelper.descToType(assign.getOwner()).equals(type.getName()) || !(assign.getValue() instanceof NewRefArg)) {
                    remaining.add(assign);
                    break;
                }
                NewRefArg val = (NewRefArg) assign.getValue();
                printIndentation();
                printString(assign.getFieldName());
                found.add(assign.getFieldName());
                if (val.getParams().length == 2) {
                    printString(",\n");
                } else {
                    printString("(");
                    // TODO ctor params
                    for (int i = 2; i < val.getParams().length; i++) {
                        emitArg(val.getParams()[i], null);
                        if (i < val.getParams().length - 1) {
                            printString(", ");
                        }
                    }
                    printString("),\n");
                }
            }
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
                if (field.getName().equals("$VALUES")) {
                    continue;
                }
                if (found.contains(field.getName())) {
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
                    continue;
                } else if (mth.isSynthetic()) {
                    continue;
                }
                printIndentation();
                emitMethod(mth);
                printString("\n\n");
            }
        }
        if (!type.getFields().isEmpty()) {
            for (FieldEntry field : type.getFields()) {
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
                if (mth.getName().equals("<init>") && mth.getInstructions().getStatements().size() == 1) {
                    continue;
                }
                printIndentation();
                emitMethod(mth);
                printString("\n\n");
            }
        }
        this.indentation--;

        printString("}");

    }

    protected void emitInterface(InterfaceEntry type) {
        String name = type.getName();
        if (name.indexOf('.') != -1) {
            name = name.substring(name.lastIndexOf('.') + 1, name.length());
        }
        name = name.replace('$', '.');
        if (!(name.contains(".") && type.getAccessModifier() == AccessModifier.PUBLIC)) {
            printString(type.getAccessModifier().identifier());
            if (type.getAccessModifier() != AccessModifier.PACKAGE_PRIVATE) {
                printString(" ");
            }
        }
        printString("interface ");
        printString(name);
        printString(" ");
        if (!type.getInterfaces().isEmpty()) {
            printString(" extends");
            for (int i = 0; i < type.getInterfaces().size(); i++) {
                emitType(type.getInterfaces().get(i));
                if (i < type.getInterfaces().size() - 1) {
                    printString(", ");
                }
            }
            printString(" ");
        }
        printString("{\n\n");

        this.indentation++;
        if (!type.getStaticFields().isEmpty()) {
            for (FieldEntry field : type.getStaticFields()) {
                printIndentation();
                emitField(field);
                printString(";\n");
            }
            printString("\n");
        }
        if (!type.getStaticMethods().isEmpty()) {
            for (MethodEntry mth : type.getStaticMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                printIndentation();
                emitMethod(mth);
                printString("\n\n");
            }
        }
        if (!type.getMethods().isEmpty()) {
            for (MethodEntry mth : type.getMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                printIndentation();
                emitMethod(mth);
                printString("\n\n");
            }
        }
        this.indentation--;

        printString("}");

    }

    protected boolean checkImport(String type) {
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
    public void emitMethod(MethodEntry method) {
        this.this$method = method;
        if (method.getName().equals("<clinit>")) {
            printString("static {\n");
            this.indentation++;
            emitBody(method.getInstructions());
            this.indentation--;
            printIndentation();
            printString("}");
            return;
        }
        if (!(this.this$ instanceof InterfaceEntry) && !(this.this$ instanceof EnumEntry && method.getName().equals("<init>"))) {
            printString(method.getAccessModifier().identifier());
            if (method.getAccessModifier() != AccessModifier.PACKAGE_PRIVATE) {
                printString(" ");
            }
        }
        if (method.getName().equals("<init>")) {
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
        int start = this.this$ instanceof EnumEntry ? 2 : 0;
        for (int i = start; i < method.getParamTypes().size(); i++) {
            emitType(method.getParamTypes().get(i));
            printString(" ");
            if (block == null) {
                printString("local" + (i + 1));
            } else {
                Local local = block.getLocals().getLocal(i + 1);
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
            this.indentation--;
            printIndentation();
            printString("}");
        } else {
            printString(";");
        }
        this.this$method = null;
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
            if (insn instanceof ReturnVoid && instructions.getType() == StatementBlock.Type.METHOD && i == instructions.getStatements().size() - 1) {
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
        if (insn instanceof LocalAssign) {
            emitLocalAssign((LocalAssign) insn);
        } else if (insn instanceof StaticMethodCall) {
            emitStaticFunction((StaticMethodCall) insn);
        } else if (insn instanceof InstanceMethodCall) {
            success = emitInstanceFunction((InstanceMethodCall) insn);
        } else if (insn instanceof NewInstance) {
            emitNew((NewInstance) insn);
        } else if (insn instanceof IncrementStatement) {
            emitIinc((IncrementStatement) insn);
        } else if (insn instanceof IfBlock) {
            emitIfBlock((IfBlock) insn);
        } else if (insn instanceof ReturnVoid) {
            emitReturn((ReturnVoid) insn);
        } else if (insn instanceof ArrayAssign) {
            emitArrayAssign((ArrayAssign) insn);
        } else if (insn instanceof ReturnValue) {
            emitValueReturn((ReturnValue) insn);
        } else if (insn instanceof ThrowException) {
            emitThrow((ThrowException) insn);
        } else if (insn instanceof ForLoop) {
            emitForLoop((ForLoop) insn);
        } else if (insn instanceof WhileLoop) {
            emitWhileLoop((WhileLoop) insn);
        } else if (insn instanceof DoWhileLoop) {
            emitDoWhileLoop((DoWhileLoop) insn);
        } else if (insn instanceof FieldAssign) {
            emitFieldAssign((FieldAssign) insn);
        } else if (insn instanceof TableSwitch) {
            emitTableSwitch((TableSwitch) insn);
        } else if (insn instanceof TryBlock) {
            emitTryBlock((TryBlock) insn);
        } else {
            throw new IllegalStateException("Unknown statement: " + insn);
        }
        if (success && withSemicolon && !(insn instanceof ForLoop) && !(insn instanceof IfBlock) && !(insn instanceof WhileLoop)
                && !(insn instanceof TryBlock)) {
            printString(";");
        }
        return success;
    }

    protected void emitLocalAssign(LocalAssign insn) {
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
                val = ((CastArg) val).getVal();
            } else if (val instanceof AddArg) {
                AddArg add = (AddArg) val;
                if (add.getLeft() instanceof LocalArg) {
                    LocalArg local = (LocalArg) add.getLeft();
                    if (local.getLocal().getIndex() == insn.getLocal().getIndex()) {
                        printString(insn.getLocal().getName());
                        if (add.getRight() instanceof IntConstantArg) {
                            IntConstantArg right = (IntConstantArg) add.getRight();
                            if (right.getConstant() == 1) {
                                printString("++");
                                return;
                            } else if (right.getConstant() == -1) {
                                printString("--");
                                return;
                            }
                        }
                        printString(" += ");
                        emitArg(add.getRight(), null);
                        return;
                    }
                }
            } else if (val instanceof SubtractArg) {
                SubtractArg sub = (SubtractArg) val;
                if (sub.getLeft() instanceof LocalArg) {
                    LocalArg local = (LocalArg) sub.getLeft();
                    if (local.getLocal().getIndex() == insn.getLocal().getIndex()) {
                        printString(insn.getLocal().getName());
                        if (sub.getRight() instanceof IntConstantArg) {
                            IntConstantArg right = (IntConstantArg) sub.getRight();
                            if (right.getConstant() == 1) {
                                printString("--");
                                return;
                            } else if (right.getConstant() == -1) {
                                printString("++");
                                return;
                            }
                        }
                        printString(" += ");
                        emitArg(sub.getRight(), local.getLocal().getType());
                        return;
                    }
                }
            }
        }
        printString(insn.getLocal().getName());
        printString(" = ");
        emitArg(insn.getValue(), insn.getLocal().getType());
    }

    protected void emitFieldAssign(FieldAssign insn) {
        if (insn instanceof StaticFieldAssign) {
            if (!((StaticFieldAssign) insn).getOwner().equals(this.this$.getDescriptor())) {
                emitTypeName(((StaticFieldAssign) insn).getOwnerName());
                printString(".");
            }
        } else if (insn instanceof InstanceFieldAssign) {
            emitArg(((InstanceFieldAssign) insn).getOwnerInsn(), insn.getOwner());
            printString(".");
        }

        printString(insn.getFieldName());
        Instruction val = insn.getValue();
        if (val instanceof OperatorArg) {
            Instruction left = ((OperatorArg) val).getLeft();
            Instruction right = ((OperatorArg) val).getRight();
            String op = " " + ((OperatorArg) val).getOperator() + "= ";
            if (left instanceof InstanceFieldArg) {
                InstanceFieldArg left_field = (InstanceFieldArg) left;
                Instruction owner = left_field.getOwnerInsn();
                if (owner instanceof LocalArg && ((LocalArg) owner).getLocal().getIndex() == 0) {
                    if (left_field.getFieldName().equals(insn.getFieldName())) {
                        printString(op);
                        if (insn.getTypeDescription().equals("Z")) {
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
                        emitArg(right, insn.getTypeDescription());
                        return;
                    }
                }
            }
        }
        printString(" = ");
        emitArg(val, insn.getTypeDescription());
    }

    protected void emitStaticFunction(StaticMethodCall insn) {
        emitType(insn.getOwner());
        printString(".");
        printString(insn.getMethodName());
        printString("(");
        // TODO param types from ast
        for (int i = 0; i < insn.getParams().length; i++) {
            Instruction param = insn.getParams()[i];
            emitArg(param, null);
            if (i < insn.getParams().length - 1) {
                printString(", ");
            }
        }
        printString(")");
    }

    protected boolean emitInstanceFunction(InstanceMethodCall insn) {
        if (insn.getMethodName().equals("<init>")) {
            if (insn.getOwner().equals(this.this$.getDescriptor())) {
                printString("this(");
            } else {
                if (insn.getParams().length == 0 || this.this$ instanceof EnumEntry) {
                    return false;
                }
                printString("super(");
            }
        } else {
            emitArg(insn.getCallee(), insn.getOwner());
            printString(".");
            printString(insn.getMethodName());
            printString("(");
        }
        // TODO param types from ast
        for (int i = 0; i < insn.getParams().length; i++) {
            Instruction param = insn.getParams()[i];
            emitArg(param, null);
            if (i < insn.getParams().length - 1) {
                printString(", ");
            }
        }
        printString(")");
        return true;
    }

    protected void emitNew(NewInstance insn) {
        printString("new ");
        emitType(insn.getType());
        printString("(");
        // TODO param types from ast
        for (int i = 0; i < insn.getParams().length; i++) {
            Instruction param = insn.getParams()[i];
            emitArg(param, null);
            if (i < insn.getParams().length - 1) {
                printString(", ");
            }
        }
        printString(")");
    }

    protected void emitIinc(IncrementStatement insn) {
        printString(insn.getLocal().getName());
        if (insn.getIncrementVal() == 1) {
            printString("++");
            return;
        } else if (insn.getIncrementVal() == -1) {
            printString("--");
            return;
        }
        printString(" += ");
        printString(String.valueOf(insn.getIncrementVal()));
    }

    protected void emitIfBlock(IfBlock insn) {
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
        ElseBlock else_ = insn.getElseBlock();
        if (else_ == null) {
            printString("}");
        } else {
            StatementBlock else_block = else_.getElseBody();
            if (else_block.getStatements().size() == 1 && else_block.getStatements().get(0) instanceof IfBlock) {
                printString("} else ");
                emitIfBlock((IfBlock) else_block.getStatements().get(0));
                return;
            }
            printString("} else {\n");
            if (!else_.getElseBody().getStatements().isEmpty()) {
                this.indentation++;
                emitBody(else_.getElseBody());
                this.indentation--;
                printString("\n");
            }
            printIndentation();
            printString("}");
        }
    }

    protected void emitForLoop(ForLoop loop) {
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

    protected void emitWhileLoop(WhileLoop loop) {
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

    protected void emitDoWhileLoop(DoWhileLoop loop) {
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

    protected void emitTableSwitch(TableSwitch tswitch) {
        printString("switch (");
        emitArg(tswitch.getSwitchVar(), "I");
        printString(") {\n");
        for (Case cs : tswitch.getCases()) {
            for (int i = 0; i < cs.getIndices().size(); i++) {
                printIndentation();
                printString("case ");
                printString(cs.getIndices().get(i) + ":\n");
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
                printString("break;\n");
            }
            this.indentation--;
            printString("\n");
        }
        printIndentation();
        printString("}");
    }

    protected void emitReturn(ReturnVoid insn) {
        printString("return");
    }

    protected void emitArrayAssign(ArrayAssign insn) {
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

    protected void emitValueReturn(ReturnValue insn) {
        printString("return ");
        String type = null;
        if (this.this$method != null) {
            type = TypeHelper.getRet(this.this$method.getSignature());
        }
        emitArg(insn.getValue(), type);
    }

    protected void emitThrow(ThrowException insn) {
        printString("throw ");
        emitArg(insn.getException(), null);
    }

    protected void emitTryBlock(TryBlock try_block) {
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
            printString(c.getExceptionLocal().getName());
            this.defined_locals.add(c.getExceptionLocal());
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
        } else if (arg instanceof InstanceFunctionArg) {
            emitInstanceFunctionArg((InstanceFunctionArg) arg);
        } else if (arg instanceof LocalArg) {
            emitLocalArg((LocalArg) arg);
        } else if (arg instanceof NewRefArg) {
            emitNewRefArg((NewRefArg) arg);
        } else if (arg instanceof StaticFunctionArg) {
            emitStaticFunctionArg((StaticFunctionArg) arg);
        } else if (arg instanceof OperatorArg) {
            emitOperator((OperatorArg) arg);
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
        }
        if (name.indexOf('.') != -1) {
            if (name.startsWith("java.lang.")) {
                name = name.substring("java.lang.".length());
            } else if (this.this$ != null) {
                String this_package = this.this$.getName().substring(0, this.this$.getName().lastIndexOf('.'));
                String target_package = name.substring(0, name.lastIndexOf('.'));
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
            emitArg(((InstanceFieldArg) arg).getOwnerInsn(), arg.getOwner());
            printString(".");
            printString(arg.getFieldName());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    protected void emitInstanceFunctionArg(InstanceFunctionArg arg) {
        if (arg.getOwner().equals("Ljava/lang/StringBuilder;") && arg.getMethodName().equals("toString")) {
            boolean valid = true;
            Instruction callee = arg.getCallee();
            List<Instruction> constants = Lists.newArrayList();
            while (callee != null) {
                if (callee instanceof InstanceFunctionArg) {
                    InstanceFunctionArg call = (InstanceFunctionArg) callee;
                    if (call.getParams().length == 1) {
                        constants.add(0, call.getParams()[0]);
                        callee = call.getCallee();
                        continue;
                    }
                } else if (callee instanceof NewRefArg) {
                    NewRefArg ref = (NewRefArg) callee;
                    if ("Ljava/lang/StringBuilder;".equals(ref.getType())) {
                        if (ref.getParams().length == 1) {
                            Instruction initial = ref.getParams()[0];
                            if (initial instanceof StaticFunctionArg) {
                                StaticFunctionArg valueof = (StaticFunctionArg) initial;
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
        emitArg(arg.getCallee(), arg.getOwner());
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

    protected void emitLocalArg(LocalArg arg) {
        printString(arg.getLocal().getName());
    }

    protected void emitNewRefArg(NewRefArg arg) {
        printString("new ");
        emitType(arg.getType());
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

    protected void emitStaticFunctionArg(StaticFunctionArg arg) {
        String owner = TypeHelper.descToType(arg.getOwner());
        if (arg.getMethodName().startsWith("access$") && this.this$ != null) {
            // synthetic accessor
            TypeEntry owner_type = this.this$.getSource().get(owner);
            if (owner_type != null) {
                MethodEntry accessor = owner_type.getStaticMethod(arg.getMethodName());
                if (accessor.getReturnType().equals("V")) {
                    // setter
                    FieldAssign assign = (FieldAssign) accessor.getInstructions().getStatements().get(0);
                    FieldAssign replacement = null;
                    if (arg.getParams().length == 2) {
                        replacement = new InstanceFieldAssign(assign.getFieldName(), assign.getTypeDescription(), assign.getOwner(),
                                arg.getParams()[0], arg.getParams()[1]);
                    } else {
                        replacement = new StaticFieldAssign(assign.getFieldName(), assign.getTypeDescription(), assign.getOwner(),
                                arg.getParams()[0]);
                    }
                    emitFieldAssign(replacement);
                    return;
                }
                // getter
                ReturnValue ret = (ReturnValue) accessor.getInstructions().getStatements().get(0);
                FieldArg getter = (FieldArg) ret.getValue();
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

    protected void emitOperator(OperatorArg arg) {
        emitArg(arg.getLeft(), null);
        printString(" " + arg.getOperator() + " ");
        emitArg(arg.getRight(), null);
    }

    protected void emitCastArg(CastArg arg) {
        printString("((");
        emitType(arg.getType());
        printString(") ");
        emitArg(arg.getVal(), null);
        printString(")");
    }

    protected void emitNegArg(NegArg arg) {
        printString("-");
        if (arg.getOperand() instanceof OperatorArg) {
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
                printString(compare.getOp().inverse().asString());
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
            printString(compare.getOp().asString());
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
            IntConstantArg true_value = (IntConstantArg) ternary.getTrueValue();
            IntConstantArg false_value = (IntConstantArg) ternary.getFalseValue();
            if (true_value.getConstant() == 1 && false_value.getConstant() == 0) {
                emitCondition(ternary.getCondition());
                return;
            } else if (true_value.getConstant() == 0 && false_value.getConstant() == 1) {
                emitCondition(ConditionSimplifier.invert(ternary.getCondition()));
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
        if (this.indentation < indentations.length) {
            printString(indentations[this.indentation]);
        } else {
            for (int i = 0; i < this.indentation; i++) {
                printString("    ");
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
