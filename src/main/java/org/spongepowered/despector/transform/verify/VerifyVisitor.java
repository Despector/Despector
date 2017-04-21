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
package org.spongepowered.despector.transform.verify;

import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.InstructionVisitor;
import org.spongepowered.despector.ast.insn.cst.DoubleConstant;
import org.spongepowered.despector.ast.insn.cst.FloatConstant;
import org.spongepowered.despector.ast.insn.cst.IntConstant;
import org.spongepowered.despector.ast.insn.cst.LongConstant;
import org.spongepowered.despector.ast.insn.cst.NullConstant;
import org.spongepowered.despector.ast.insn.cst.StringConstant;
import org.spongepowered.despector.ast.insn.cst.TypeConstant;
import org.spongepowered.despector.ast.insn.misc.Cast;
import org.spongepowered.despector.ast.insn.misc.InstanceOf;
import org.spongepowered.despector.ast.insn.misc.NewArray;
import org.spongepowered.despector.ast.insn.misc.NumberCompare;
import org.spongepowered.despector.ast.insn.misc.Ternary;
import org.spongepowered.despector.ast.insn.op.NegativeOperator;
import org.spongepowered.despector.ast.insn.op.Operator;
import org.spongepowered.despector.ast.insn.var.ArrayAccess;
import org.spongepowered.despector.ast.insn.var.InstanceFieldAccess;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.ast.insn.var.StaticFieldAccess;
import org.spongepowered.despector.ast.stmt.StatementVisitor;
import org.spongepowered.despector.ast.stmt.assign.ArrayAssignment;
import org.spongepowered.despector.ast.stmt.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.stmt.assign.LocalAssignment;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.stmt.branch.Break;
import org.spongepowered.despector.ast.stmt.branch.DoWhile;
import org.spongepowered.despector.ast.stmt.branch.For;
import org.spongepowered.despector.ast.stmt.branch.ForEach;
import org.spongepowered.despector.ast.stmt.branch.If;
import org.spongepowered.despector.ast.stmt.branch.If.Elif;
import org.spongepowered.despector.ast.stmt.branch.If.Else;
import org.spongepowered.despector.ast.stmt.branch.Switch;
import org.spongepowered.despector.ast.stmt.branch.Switch.Case;
import org.spongepowered.despector.ast.stmt.branch.TryCatch;
import org.spongepowered.despector.ast.stmt.branch.TryCatch.CatchBlock;
import org.spongepowered.despector.ast.stmt.branch.While;
import org.spongepowered.despector.ast.stmt.invoke.DynamicInvoke;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.ast.stmt.invoke.InvokeStatement;
import org.spongepowered.despector.ast.stmt.invoke.New;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;
import org.spongepowered.despector.ast.stmt.misc.Comment;
import org.spongepowered.despector.ast.stmt.misc.Increment;
import org.spongepowered.despector.ast.stmt.misc.Return;
import org.spongepowered.despector.ast.stmt.misc.Throw;
import org.spongepowered.despector.ast.type.AnnotationEntry;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.ast.type.TypeVisitor;

import java.util.HashSet;
import java.util.Set;

public class VerifyVisitor implements TypeVisitor, StatementVisitor, InstructionVisitor {

    private static boolean isNumber(TypeSignature sig) {
        return sig == ClassTypeSignature.INT || sig == ClassTypeSignature.LONG || sig == ClassTypeSignature.FLOAT || sig == ClassTypeSignature.DOUBLE
                || sig == ClassTypeSignature.BYTE || sig == ClassTypeSignature.SHORT;
    }

    private TypeEntry type;
    private MethodEntry mth;
    private int line;
    private Set<LocalInstance> defined_locals = new HashSet<>();

    private void check(boolean state, String msg) {
        if (!state) {
            String header = "Verification in " + this.type.getName();
            if (this.mth != null) {
                header += "#" + this.mth.getName() + this.mth.getDescription();
                header += " (line: " + this.line + ")";
            }
            header += ": ";
            throw new VerificationFailedException(header + msg);
        }
    }

    @Override
    public void visitArrayAccess(ArrayAccess insn) {
        check(insn.getIndex().inferType().equals(ClassTypeSignature.INT),
                "Array index must be an integer found: " + insn.getIndex().inferType().toString());
    }

    @Override
    public void visitCast(Cast insn) {
        // TODO warn if types seem incompatible
    }

    @Override
    public void visitDoubleConstant(DoubleConstant insn) {
    }

    @Override
    public void visitDynamicInvoke(DynamicInvoke insn) {
        // TODO check handle is valid
    }

    @Override
    public void visitFloatConstant(FloatConstant insn) {
    }

    @Override
    public void visitInstanceFieldAccess(InstanceFieldAccess insn) {
        // TODO check field exists
    }

    @Override
    public void visitInstanceMethodInvoke(InstanceMethodInvoke insn) {
        // TODO check methode exists
    }

    @Override
    public void visitInstanceOf(InstanceOf insn) {
        // TODO warn if types seem incompatible
    }

    @Override
    public void visitIntConstant(IntConstant insn) {
    }

    @Override
    public void visitLocalAccess(LocalAccess insn) {
        check(this.defined_locals.contains(insn.getLocal()), "Access local not yet definde");
    }

    @Override
    public void visitLocalInstance(LocalInstance local) {
    }

    @Override
    public void visitLongConstant(LongConstant insn) {
    }

    @Override
    public void visitNegativeOperator(NegativeOperator insn) {
        check(isNumber(insn.getOperand().inferType()), "Can only negate number types found: " + insn.getOperand().inferType().toString());
    }

    @Override
    public void visitNew(New insn) {
        // TODO check type is valid
        // TODO check params matches inferred type
        // TODO check return type matches inferred type
    }

    @Override
    public void visitNewArray(NewArray insn) {
        check(insn.getSize().inferType().equals(ClassTypeSignature.INT),
                "Array size must be an integer found: " + insn.getSize().inferType().toString());
    }

    @Override
    public void visitNullConstant(NullConstant insn) {
    }

    @Override
    public void visitNumberCompare(NumberCompare insn) {
        check(isNumber(insn.getLeftOperand().inferType()), "Can only compare number types found: " + insn.getLeftOperand().inferType().toString());
        check(isNumber(insn.getRightOperand().inferType()), "Can only compare number types found: " + insn.getRightOperand().inferType().toString());
    }

    @Override
    public void visitOperator(Operator insn) {
        check(isNumber(insn.getLeftOperand().inferType()),
                "Can only use operator on number types found: " + insn.getLeftOperand().inferType().toString());
        check(isNumber(insn.getRightOperand().inferType()),
                "Can only use operator on number types found: " + insn.getRightOperand().inferType().toString());
    }

    @Override
    public void visitStaticFieldAccess(StaticFieldAccess insn) {
        // TODO check field exists

    }

    @Override
    public void visitStaticMethodInvoke(StaticMethodInvoke insn) {
        // TODO check method exists

    }

    @Override
    public void visitStringConstant(StringConstant insn) {
    }

    @Override
    public void visitTernary(Ternary insn) {
        // TODO check type of each side is equivalent
    }

    @Override
    public void visitTypeConstant(TypeConstant insn) {
        // TODO check type is valid
    }

    @Override
    public void visitArrayAssignment(ArrayAssignment stmt) {
        check(stmt.getIndex().inferType().equals(ClassTypeSignature.INT),
                "Array index must be an integer found: " + stmt.getIndex().inferType().toString());
    }

    @Override
    public void visitBreak(Break breakStatement) {

    }

    @Override
    public void visitCatchBlock(CatchBlock stmt) {

    }

    @Override
    public void visitComment(Comment comment) {
    }

    @Override
    public void visitDoWhile(DoWhile doWhileLoop) {

    }

    @Override
    public void visitElif(Elif elseBlock) {

    }

    @Override
    public void visitElse(Else elseBlock) {

    }

    @Override
    public void visitFor(For forLoop) {

    }

    @Override
    public void visitForEach(ForEach forLoop) {

    }

    @Override
    public void visitIf(If ifBlock) {

    }

    @Override
    public void visitIncrement(Increment stmt) {
        check(isNumber(stmt.getLocal().getType()), "Can only increment local containing a number found: " + stmt.getLocal().getType().toString());
    }

    @Override
    public void visitInstanceFieldAssignment(InstanceFieldAssignment stmt) {
        // TODO check field exists
    }

    @Override
    public void visitInvoke(InvokeStatement stmt) {
    }

    @Override
    public void visitLocalAssignment(LocalAssignment stmt) {
        this.defined_locals.add(stmt.getLocal());
        // TODO check value matches local type
    }

    @Override
    public void visitReturn(Return returnValue) {
        // TODO check value matches return type
    }

    @Override
    public void visitStaticFieldAssignment(StaticFieldAssignment staticFieldAssign) {
        // TODO check field exists
    }

    @Override
    public void visitSwitch(Switch tableSwitch) {
        check(tableSwitch.getSwitchVar().inferType().equals(ClassTypeSignature.INT),
                "Array size must be an integer found: " + tableSwitch.getSwitchVar().inferType().toString());

    }

    @Override
    public void visitSwitchCase(Case case1) {

    }

    @Override
    public void visitThrow(Throw throwException) {

    }

    @Override
    public void visitTryCatch(TryCatch tryBlock) {

    }

    @Override
    public void visitWhile(While whileLoop) {
    }

    @Override
    public void visitClassEntry(ClassEntry type) {
        this.type = type;
    }

    @Override
    public void visitEnumEntry(EnumEntry type) {
        this.type = type;
    }

    @Override
    public void visitInterfaceEntry(InterfaceEntry type) {
        this.type = type;
    }

    @Override
    public void visitAnnotation(Annotation annotation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitMethod(MethodEntry mth) {
        this.mth = mth;
    }

    @Override
    public void visitField(FieldEntry fld) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitMethodEnd() {
        this.mth = null;
    }

    @Override
    public void visitTypeEnd() {
        this.type = null;
    }

    @Override
    public void visitAnnotationEntry(AnnotationEntry type) {
        // TODO Auto-generated method stub
        
    }

}
