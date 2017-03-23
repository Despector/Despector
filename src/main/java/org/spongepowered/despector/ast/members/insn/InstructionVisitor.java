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
import org.spongepowered.despector.ast.members.insn.arg.operator.AddOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.DivideOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.MultiplyOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.NegativeOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.RemainderOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.ShiftLeftOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.ShiftRightOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.SubtractOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.UnsignedShiftRightOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.bitwise.AndOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.bitwise.OrOperator;
import org.spongepowered.despector.ast.members.insn.arg.operator.bitwise.XorOperator;
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

public abstract class InstructionVisitor {

    public void visitAddOperatorArg(AddOperator addArg) {
    }

    public void visitAndCondition(AndCondition andCondition) {
    }

    public void visitAndOperatorArg(AndOperator andArg) {
    }

    public void visitArrayAssign(ArrayAssignment arrayAssign) {
    }

    public void visitArrayLoadArg(ArrayAccess arrayLoadArg) {
    }

    public void visitBooleanCondition(BooleanCondition booleanCondition) {
    }

    public void visitBreak(Break breakStatement) {
    }

    public void visitCastArg(Cast castArg) {
    }

    public void visitCatchBlock(CatchBlock catchBlock) {
    }

    public void visitCompareArg(NumberCompare compareArg) {
    }

    public void visitCompareCondition(CompareCondition compareCondition) {
    }

    public void visitDivideOperatorArg(DivideOperator divideArg) {
    }

    public void visitDoubleConstantArg(DoubleConstant doubleConstantArg) {
    }

    public void visitDoWhileLoop(DoWhile doWhileLoop) {
    }

    public void visitElifBlock(Elif elseBlock) {
    }

    public void visitElseBlock(Else elseBlock) {
    }

    public void visitFloatConstantArg(FloatConstant floatConstantArg) {
    }

    public void visitForEachLoop(ForEach forLoop) {
    }

    public void visitForLoop(For forLoop) {
    }

    public void visitIfBlock(If ifBlock) {
    }

    public void visitIncrement(Increment incrementStatement) {
    }

    public void visitInstanceFieldArg(InstanceFieldAccess instanceFieldArg) {
    }

    public void visitInstanceFieldAssign(InstanceFieldAssignment instanceFieldAssign) {
    }

    public void visitInstanceMethodCall(InstanceMethodInvoke instanceMethodCall) {
    }

    public void visitInstanceOfArg(InstanceOf instanceOfArg) {
    }

    public void visitIntConstantArg(IntConstant intConstantArg) {
    }

    public void visitInverseCondition(InverseCondition inverseCondition) {
    }

    public void visitLocal(LocalInstance local) {
    }

    public void visitLocalArg(LocalAccess localArg) {
    }

    public void visitLocalAssign(LocalAssignment localAssign) {
    }

    public void visitLongConstantArg(LongConstant longConstantArg) {
    }

    public void visitMultiplyOperatorArg(MultiplyOperator multiplyArg) {
    }

    public void visitNegArg(NegativeOperator negArg) {
    }

    public void visitNewArrayArg(NewArray newArrayArg) {
    }

    public void visitNewInstance(New newInstance) {
    }

    public void visitNullConstantArg(NullConstant nullConstantArg) {
    }

    public void visitOrCondition(OrCondition orCondition) {
    }

    public void visitOrOperatorArg(OrOperator orArg) {
    }

    public void visitRemainderOperatorArg(RemainderOperator remainerArg) {
    }

    public void visitShiftLeftOperatorArg(ShiftLeftOperator shiftLeftArg) {
    }

    public void visitShiftRightOperatorArg(ShiftRightOperator shiftRightArg) {
    }

    public void visitStaticFieldArg(StaticFieldAccess staticFieldArg) {
    }

    public void visitStaticFieldAssign(StaticFieldAssignment staticFieldAssign) {
    }

    public void visitStaticMethodCall(StaticMethodInvoke staticMethodCall) {
    }

    public void visitStringConstantArg(StringConstant stringConstantArg) {
    }

    public void visitSubtractOperatorArg(SubtractOperator subtractArg) {
    }

    public void visitSwitch(Switch tableSwitch) {
    }

    public void visitSwitchCase(Switch.Case case1) {
    }

    public void visitTernary(Ternary ternary) {
    }

    public void visitThrowException(Throw throwException) {
    }

    public void visitTryBlock(TryCatch tryBlock) {
    }

    public void visitTypeConstantArg(TypeConstant typeConstantArg) {
    }

    public void visitUnsignedRightShiftOperatorArg(UnsignedShiftRightOperator unsignedShiftRightArg) {
    }

    public void visitValueReturn(Return returnValue) {
    }

    public void visitWhileLoop(While whileLoop) {
    }

    public void visitXorOperatorArg(XorOperator xorArg) {
    }

}
