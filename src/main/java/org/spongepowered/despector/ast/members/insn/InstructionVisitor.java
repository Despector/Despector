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
package org.spongepowered.despector.ast.members.insn;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.arg.Cast;
import org.spongepowered.despector.ast.members.insn.arg.InstanceOf;
import org.spongepowered.despector.ast.members.insn.arg.NewArray;
import org.spongepowered.despector.ast.members.insn.arg.NumberCompare;
import org.spongepowered.despector.ast.members.insn.arg.cst.DoubleConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.FloatConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.LongConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.NullConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.StringConstant;
import org.spongepowered.despector.ast.members.insn.arg.cst.TypeConstant;
import org.spongepowered.despector.ast.members.insn.arg.field.ArrayAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.InstanceFieldAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.StaticFieldAccess;
import org.spongepowered.despector.ast.members.insn.arg.operator.NegativeOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.Operator;
import org.spongepowered.despector.ast.members.insn.assign.ArrayAssignment;
import org.spongepowered.despector.ast.members.insn.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.members.insn.branch.Break;
import org.spongepowered.despector.ast.members.insn.branch.DoWhile;
import org.spongepowered.despector.ast.members.insn.branch.For;
import org.spongepowered.despector.ast.members.insn.branch.ForEach;
import org.spongepowered.despector.ast.members.insn.branch.If;
import org.spongepowered.despector.ast.members.insn.branch.If.Elif;
import org.spongepowered.despector.ast.members.insn.branch.If.Else;
import org.spongepowered.despector.ast.members.insn.branch.Switch;
import org.spongepowered.despector.ast.members.insn.branch.Ternary;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch.CatchBlock;
import org.spongepowered.despector.ast.members.insn.branch.While;
import org.spongepowered.despector.ast.members.insn.branch.condition.AndCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.InverseCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.OrCondition;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;
import org.spongepowered.despector.ast.members.insn.function.New;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodInvoke;
import org.spongepowered.despector.ast.members.insn.misc.Increment;
import org.spongepowered.despector.ast.members.insn.misc.Return;
import org.spongepowered.despector.ast.members.insn.misc.Throw;

/**
 * A visitor which may be used to traverse the AST.
 */
public abstract class InstructionVisitor {

    public void visitAndCondition(AndCondition andCondition) {
    }

    public void visitArrayAssignment(ArrayAssignment arrayAssign) {
    }

    public void visitArrayAccess(ArrayAccess arrayLoadArg) {
    }

    public void visitBooleanCondition(BooleanCondition booleanCondition) {
    }

    public void visitBreak(Break breakStatement) {
    }

    public void visitCast(Cast castArg) {
    }

    public void visitCatchBlock(CatchBlock catchBlock) {
    }

    public void visitNumberCompare(NumberCompare compareArg) {
    }

    public void visitCompareCondition(CompareCondition compareCondition) {
    }

    public void visitDoubleConstant(DoubleConstant doubleConstantArg) {
    }

    public void visitDoWhile(DoWhile doWhileLoop) {
    }

    public void visitElif(Elif elseBlock) {
    }

    public void visitElse(Else elseBlock) {
    }

    public void visitFloatConstant(FloatConstant floatConstantArg) {
    }

    public void visitForEach(ForEach forLoop) {
    }

    public void visitFor(For forLoop) {
    }

    public void visitIf(If ifBlock) {
    }

    public void visitIncrement(Increment incrementStatement) {
    }

    public void visitInstanceFieldAccess(InstanceFieldAccess instanceFieldArg) {
    }

    public void visitInstanceFieldAssignment(InstanceFieldAssignment instanceFieldAssign) {
    }

    public void visitInstanceMethodInvoke(InstanceMethodInvoke instanceMethodCall) {
    }

    public void visitInstanceOf(InstanceOf instanceOfArg) {
    }

    public void visitIntConstant(IntConstant intConstantArg) {
    }

    public void visitInverseCondition(InverseCondition inverseCondition) {
    }

    public void visitLocalInstance(LocalInstance local) {
    }

    public void visitLocalAccess(LocalAccess localArg) {
    }

    public void visitLocalAssignment(LocalAssignment localAssign) {
    }

    public void visitLongConstant(LongConstant longConstantArg) {
    }

    public void visitNegativeOperator(NegativeOperator negArg) {
    }

    public void visitNewArray(NewArray newArrayArg) {
    }

    public void visitNew(New newInstance) {
    }

    public void visitNullConstant(NullConstant nullConstantArg) {
    }

    public void visitOrCondition(OrCondition orCondition) {
    }

    public void visitOperator(Operator orArg) {
    }

    public void visitStaticFieldAccess(StaticFieldAccess staticFieldArg) {
    }

    public void visitStaticFieldAssignment(StaticFieldAssignment staticFieldAssign) {
    }

    public void visitStaticMethodInvoke(StaticMethodInvoke staticMethodCall) {
    }

    public void visitStringConstant(StringConstant stringConstantArg) {
    }

    public void visitSwitch(Switch tableSwitch) {
    }

    public void visitSwitchCase(Switch.Case case1) {
    }

    public void visitTernary(Ternary ternary) {
    }

    public void visitThrow(Throw throwException) {
    }

    public void visitTryCatch(TryCatch tryBlock) {
    }

    public void visitTypeConstant(TypeConstant typeConstantArg) {
    }

    public void visitReturn(Return returnValue) {
    }

    public void visitWhile(While whileLoop) {
    }

}
