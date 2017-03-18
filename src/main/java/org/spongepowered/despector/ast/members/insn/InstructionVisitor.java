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
import org.spongepowered.despector.ast.members.insn.arg.CastArg;
import org.spongepowered.despector.ast.members.insn.arg.CompareArg;
import org.spongepowered.despector.ast.members.insn.arg.InstanceOfArg;
import org.spongepowered.despector.ast.members.insn.arg.NewArrayArg;
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
import org.spongepowered.despector.ast.members.insn.arg.operator.NegArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.RemainderInstruction;
import org.spongepowered.despector.ast.members.insn.arg.operator.ShiftLeftArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.ShiftRightArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.SubtractArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.UnsignedShiftRightArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.bitwise.AndInstruction;
import org.spongepowered.despector.ast.members.insn.arg.operator.bitwise.OrArg;
import org.spongepowered.despector.ast.members.insn.arg.operator.bitwise.XorArg;
import org.spongepowered.despector.ast.members.insn.assign.ArrayAssignment;
import org.spongepowered.despector.ast.members.insn.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.members.insn.branch.DoWhile;
import org.spongepowered.despector.ast.members.insn.branch.For;
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
    
    public void visitAddOperatorArg(AddArg addArg) {
    }

    public void visitAndCondition(AndCondition andCondition) {
    }

    public void visitAndOperatorArg(AndInstruction andArg) {
    }

    public void visitArrayAssign(ArrayAssignment arrayAssign) {
    }

    public void visitArrayLoadArg(ArrayLoadArg arrayLoadArg) {
    }

    public void visitBooleanCondition(BooleanCondition booleanCondition) {
    }

    public void visitCastArg(CastArg castArg) {
    }

    public void visitCatchBlock(CatchBlock catchBlock) {
    }

    public void visitCompareArg(CompareArg compareArg) {
    }

    public void visitCompareCondition(CompareCondition compareCondition) {
    }

    public void visitDivideOperatorArg(DivideArg divideArg) {
    }

    public void visitDoubleConstantArg(DoubleConstantArg doubleConstantArg) {
    }

    public void visitDoWhileLoop(DoWhile doWhileLoop) {
    }

    public void visitElifBlock(Elif elseBlock) {
    }

    public void visitElseBlock(Else elseBlock) {
    }

    public void visitFloatConstantArg(FloatConstantArg floatConstantArg) {
    }

    public void visitForLoop(For forLoop) {
    }

    public void visitIfBlock(If ifBlock) {
    }

    public void visitIncrement(Increment incrementStatement) {
    }

    public void visitInstanceFieldArg(InstanceFieldArg instanceFieldArg) {
    }

    public void visitInstanceFieldAssign(InstanceFieldAssignment instanceFieldAssign) {
    }

    public void visitInstanceMethodCall(InstanceMethodInvoke instanceMethodCall) {
    }

    public void visitInstanceOfArg(InstanceOfArg instanceOfArg) {
    }

    public void visitIntConstantArg(IntConstantArg intConstantArg) {
    }

    public void visitInverseCondition(InverseCondition inverseCondition) {
    }

    public void visitLocal(LocalInstance local) {
    }

    public void visitLocalArg(LocalArg localArg) {
    }

    public void visitLocalAssign(LocalAssignment localAssign) {
    }

    public void visitLongConstantArg(LongConstantArg longConstantArg) {
    }

    public void visitMultiplyOperatorArg(MultiplyArg multiplyArg) {
    }

    public void visitNegArg(NegArg negArg) {
    }

    public void visitNewArrayArg(NewArrayArg newArrayArg) {
    }

    public void visitNewInstance(New newInstance) {
    }

    public void visitNullConstantArg(NullConstantArg nullConstantArg) {
    }

    public void visitOrCondition(OrCondition orCondition) {
    }

    public void visitOrOperatorArg(OrArg orArg) {
    }

    public void visitRemainderOperatorArg(RemainderInstruction remainerArg) {
    }

    public void visitShiftLeftOperatorArg(ShiftLeftArg shiftLeftArg) {
    }

    public void visitShiftRightOperatorArg(ShiftRightArg shiftRightArg) {
    }

    public void visitStaticFieldArg(StaticFieldArg staticFieldArg) {
    }

    public void visitStaticFieldAssign(StaticFieldAssignment staticFieldAssign) {
    }

    public void visitStaticMethodCall(StaticMethodInvoke staticMethodCall) {
    }

    public void visitStringConstantArg(StringConstantArg stringConstantArg) {
    }

    public void visitSubtractOperatorArg(SubtractArg subtractArg) {
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

    public void visitTypeConstantArg(TypeConstantArg typeConstantArg) {
    }

    public void visitUnsignedRightShiftOperatorArg(UnsignedShiftRightArg unsignedShiftRightArg) {
    }

    public void visitValueReturn(Return returnValue) {
    }

    public void visitWhileLoop(While whileLoop) {
    }

    public void visitXorOperatorArg(XorArg xorArg) {
    }

}
