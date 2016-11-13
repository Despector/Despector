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

import org.spongepowered.despector.ast.members.insn.arg.CastArg;
import org.spongepowered.despector.ast.members.insn.arg.InstanceFunctionArg;
import org.spongepowered.despector.ast.members.insn.arg.InstanceOfArg;
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
import org.spongepowered.despector.ast.members.insn.arg.field.InstanceFieldArg;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalArg;
import org.spongepowered.despector.ast.members.insn.arg.field.StaticFieldArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.AddArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.DivideArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.MultiplyArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.RemainerArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.ShiftLeftArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.ShiftRightArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.SubtractArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.UnsignedShiftRightArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.bitwise.AndArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.bitwise.OrArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.bitwise.XorArg;
import org.spongepowered.despector.ast.members.insn.assign.ArrayAssign;
import org.spongepowered.despector.ast.members.insn.assign.InstanceFieldAssign;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssign;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssign;
import org.spongepowered.despector.ast.members.insn.branch.CatchBlock;
import org.spongepowered.despector.ast.members.insn.branch.DoWhileLoop;
import org.spongepowered.despector.ast.members.insn.branch.ElseBlock;
import org.spongepowered.despector.ast.members.insn.branch.ForLoop;
import org.spongepowered.despector.ast.members.insn.branch.IfBlock;
import org.spongepowered.despector.ast.members.insn.branch.TableSwitch;
import org.spongepowered.despector.ast.members.insn.branch.Ternary;
import org.spongepowered.despector.ast.members.insn.branch.TryBlock;
import org.spongepowered.despector.ast.members.insn.branch.WhileLoop;
import org.spongepowered.despector.ast.members.insn.branch.condition.AndCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.InverseCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.OrCondition;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodCall;
import org.spongepowered.despector.ast.members.insn.function.NewInstance;
import org.spongepowered.despector.ast.members.insn.function.StaticMethodCall;
import org.spongepowered.despector.ast.members.insn.misc.IncrementStatement;
import org.spongepowered.despector.ast.members.insn.misc.ReturnValue;
import org.spongepowered.despector.ast.members.insn.misc.ReturnVoid;
import org.spongepowered.despector.ast.members.insn.misc.ThrowException;

public abstract class InstructionVisitor {

    public void visitThrowException(ThrowException throwException) {
    }

    public void visitReturn(ReturnVoid returnVoid) {
    }

    public void visitValueReturn(ReturnValue returnValue) {
    }

    public void visitIncrement(IncrementStatement incrementStatement) {
    }

    public void visitStaticMethodCall(StaticMethodCall staticMethodCall) {
    }

    public void visitNewInstance(NewInstance newInstance) {
    }

    public void visitInstanceMethodCall(InstanceMethodCall instanceMethodCall) {
    }

    public void visitDoWhileLoop(DoWhileLoop doWhileLoop) {
    }

    public void visitForLoop(ForLoop forLoop) {
    }

    public void visitIfBlock(IfBlock ifBlock) {
    }

    public void visitElseBlock(ElseBlock elseBlock) {
    }

    public void visitSwitch(TableSwitch tableSwitch) {
    }

    public void visitSwitchCase(TableSwitch.Case case1) {
    }

    public void visitTernary(Ternary ternary) {
    }

    public void visitWhileLoop(WhileLoop whileLoop) {
    }

    public void visitInverseCondition(InverseCondition inverseCondition) {
    }

    public void visitOrCondition(OrCondition orCondition) {
    }

    public void visitCompareCondition(CompareCondition compareCondition) {
    }

    public void visitBooleanCondition(BooleanCondition booleanCondition) {
    }

    public void visitAndCondition(AndCondition andCondition) {
    }

    public void visitArrayAssign(ArrayAssign arrayAssign) {
    }

    public void visitInstanceFieldAssign(InstanceFieldAssign instanceFieldAssign) {
    }

    public void visitLocalAssign(LocalAssign localAssign) {
    }

    public void visitStaticFieldAssign(StaticFieldAssign staticFieldAssign) {
    }

    public void visitStaticFunctionArg(StaticFunctionArg staticFunctionArg) {
    }

    public void visitNewRefArg(NewRefArg newRefArg) {
    }

    public void visitNewArrayArg(NewArrayArg newArrayArg) {
    }

    public void visitInstanceOfArg(InstanceOfArg instanceOfArg) {
    }

    public void visitInstanceFunctionArg(InstanceFunctionArg instanceFunctionArg) {
    }

    public void visitCastArg(CastArg castArg) {
    }

    public void visitDoubleConstantArg(DoubleConstantArg doubleConstantArg) {
    }

    public void visitFloatConstantArg(FloatConstantArg floatConstantArg) {
    }

    public void visitIntConstantArg(IntConstantArg intConstantArg) {
    }

    public void visitLongConstantArg(LongConstantArg longConstantArg) {
    }

    public void visitNullConstantArg(NullConstantArg nullConstantArg) {
    }

    public void visitStringConstantArg(StringConstantArg stringConstantArg) {
    }

    public void visitTypeConstantArg(TypeConstantArg typeConstantArg) {
    }

    public void visitArrayLoadArg(ArrayLoadArg arrayLoadArg) {
    }

    public void visitInstanceFieldArg(InstanceFieldArg instanceFieldArg) {
    }

    public void visitLocalArg(LocalArg localArg) {
    }

    public void visitStaticFieldArg(StaticFieldArg staticFieldArg) {
    }

    public void visitUnsignedRightShiftOperatorArg(UnsignedShiftRightArg unsignedShiftRightArg) {
    }

    public void visitSubtractOperatorArg(SubtractArg subtractArg) {
    }

    public void visitShiftRightOperatorArg(ShiftRightArg shiftRightArg) {
    }

    public void visitShiftLeftOperatorArg(ShiftLeftArg shiftLeftArg) {
    }

    public void visitRemainderOperatorArg(RemainerArg remainerArg) {
    }

    public void visitMultiplyOperatorArg(MultiplyArg multiplyArg) {
    }

    public void visitDivideOperatorArg(DivideArg divideArg) {
    }

    public void visitAddOperatorArg(AddArg addArg) {
    }

    public void visitAndOperatorArg(AndArg andArg) {
    }

    public void visitOrOperatorArg(OrArg orArg) {
    }

    public void visitXorOperatorArg(XorArg xorArg) {
    }

    public void visitTryBlock(TryBlock tryBlock) {
    }

    public void visitCatchBlock(CatchBlock catchBlock) {
    }

}
