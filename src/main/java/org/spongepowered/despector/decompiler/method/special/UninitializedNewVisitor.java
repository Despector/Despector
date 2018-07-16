package org.spongepowered.despector.decompiler.method.special;

import org.spongepowered.despector.ast.Locals.LocalInstance;
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
import org.spongepowered.despector.ast.insn.misc.MultiNewArray;
import org.spongepowered.despector.ast.insn.misc.NewArray;
import org.spongepowered.despector.ast.insn.misc.NumberCompare;
import org.spongepowered.despector.ast.insn.misc.Ternary;
import org.spongepowered.despector.ast.insn.op.NegativeOperator;
import org.spongepowered.despector.ast.insn.op.Operator;
import org.spongepowered.despector.ast.insn.var.ArrayAccess;
import org.spongepowered.despector.ast.insn.var.InstanceFieldAccess;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.ast.insn.var.StaticFieldAccess;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.ast.stmt.invoke.Lambda;
import org.spongepowered.despector.ast.stmt.invoke.MethodReference;
import org.spongepowered.despector.ast.stmt.invoke.New;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;

public class UninitializedNewVisitor implements InstructionVisitor {

    @Override
    public void visitArrayAccess(ArrayAccess insn) {
    }

    @Override
    public void visitCast(Cast insn) {
    }

    @Override
    public void visitDoubleConstant(DoubleConstant insn) {
    }

    @Override
    public void visitDynamicInvoke(Lambda insn) {
    }

    @Override
    public void visitFloatConstant(FloatConstant insn) {
    }

    @Override
    public void visitInstanceFieldAccess(InstanceFieldAccess insn) {
    }

    @Override
    public void visitInstanceMethodInvoke(InstanceMethodInvoke insn) {
    }

    @Override
    public void visitInstanceOf(InstanceOf insn) {
    }

    @Override
    public void visitIntConstant(IntConstant insn) {
    }

    @Override
    public void visitLocalAccess(LocalAccess insn) {
    }

    @Override
    public void visitLocalInstance(LocalInstance local) {
    }

    @Override
    public void visitLongConstant(LongConstant insn) {
    }

    @Override
    public void visitMultiNewArray(MultiNewArray insn) {
    }

    @Override
    public void visitNegativeOperator(NegativeOperator insn) {
    }

    @Override
    public void visitNew(New insn) {
    }

    @Override
    public void visitNewArray(NewArray insn) {
    }

    @Override
    public void visitNullConstant(NullConstant insn) {
    }

    @Override
    public void visitNumberCompare(NumberCompare insn) {
    }

    @Override
    public void visitOperator(Operator insn) {
    }

    @Override
    public void visitStaticFieldAccess(StaticFieldAccess insn) {
    }

    @Override
    public void visitStaticMethodInvoke(StaticMethodInvoke insn) {
    }

    @Override
    public void visitStringConstant(StringConstant insn) {
    }

    @Override
    public void visitTernary(Ternary insn) {
    }

    @Override
    public void visitTypeConstant(TypeConstant insn) {
    }

    @Override
    public void visitMethodReference(MethodReference methodReference) {
    }

}
