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

import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
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
import org.spongepowered.despector.ast.members.insn.assign.FieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.members.insn.branch.Break;
import org.spongepowered.despector.ast.members.insn.branch.DoWhile;
import org.spongepowered.despector.ast.members.insn.branch.For;
import org.spongepowered.despector.ast.members.insn.branch.ForEach;
import org.spongepowered.despector.ast.members.insn.branch.If;
import org.spongepowered.despector.ast.members.insn.branch.Switch;
import org.spongepowered.despector.ast.members.insn.branch.Ternary;
import org.spongepowered.despector.ast.members.insn.branch.TryCatch;
import org.spongepowered.despector.ast.members.insn.branch.While;
import org.spongepowered.despector.ast.members.insn.branch.condition.AndCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
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
import org.spongepowered.despector.emitter.condition.AndConditionEmitter;
import org.spongepowered.despector.emitter.condition.BooleanConditionEmitter;
import org.spongepowered.despector.emitter.condition.CompareConditionEmitter;
import org.spongepowered.despector.emitter.condition.InverseConditionEmitter;
import org.spongepowered.despector.emitter.condition.OrConditionEmitter;
import org.spongepowered.despector.emitter.instruction.ArrayLoadEmitter;
import org.spongepowered.despector.emitter.instruction.CastEmitter;
import org.spongepowered.despector.emitter.instruction.CompareEmitter;
import org.spongepowered.despector.emitter.instruction.DoubleConstantEmitter;
import org.spongepowered.despector.emitter.instruction.FieldEmitter;
import org.spongepowered.despector.emitter.instruction.FloatConstantEmitter;
import org.spongepowered.despector.emitter.instruction.InstanceMethodInvokeEmitter;
import org.spongepowered.despector.emitter.instruction.InstanceOfEmitter;
import org.spongepowered.despector.emitter.instruction.IntConstantEmitter;
import org.spongepowered.despector.emitter.instruction.LocalEmitter;
import org.spongepowered.despector.emitter.instruction.LongConstantEmitter;
import org.spongepowered.despector.emitter.instruction.NegativeEmitter;
import org.spongepowered.despector.emitter.instruction.NewArrayEmitter;
import org.spongepowered.despector.emitter.instruction.NewEmitter;
import org.spongepowered.despector.emitter.instruction.NullConstantEmitter;
import org.spongepowered.despector.emitter.instruction.OperatorEmitter;
import org.spongepowered.despector.emitter.instruction.StaticMethodInvokeEmitter;
import org.spongepowered.despector.emitter.instruction.StringConstantEmitter;
import org.spongepowered.despector.emitter.instruction.TernaryEmitter;
import org.spongepowered.despector.emitter.instruction.TypeConstantEmitter;
import org.spongepowered.despector.emitter.kotlin.type.KotlinClassEntryEmitter;
import org.spongepowered.despector.emitter.kotlin.type.KotlinCompanionClassEmitter;
import org.spongepowered.despector.emitter.kotlin.type.KotlinDataClassEmitter;
import org.spongepowered.despector.emitter.kotlin.type.KotlinEnumEntryEmitter;
import org.spongepowered.despector.emitter.kotlin.type.KotlinMethodEntryEmitter;
import org.spongepowered.despector.emitter.special.AnnotationEmitter;
import org.spongepowered.despector.emitter.special.AnonymousClassEmitter;
import org.spongepowered.despector.emitter.special.GenericsEmitter;
import org.spongepowered.despector.emitter.special.PackageInfoEmitter;
import org.spongepowered.despector.emitter.statement.ArrayAssignmentEmitter;
import org.spongepowered.despector.emitter.statement.BreakEmitter;
import org.spongepowered.despector.emitter.statement.DoWhileEmitter;
import org.spongepowered.despector.emitter.statement.FieldAssignmentEmitter;
import org.spongepowered.despector.emitter.statement.ForEachEmitter;
import org.spongepowered.despector.emitter.statement.ForEmitter;
import org.spongepowered.despector.emitter.statement.IfEmitter;
import org.spongepowered.despector.emitter.statement.IncrementEmitter;
import org.spongepowered.despector.emitter.statement.InvokeEmitter;
import org.spongepowered.despector.emitter.statement.LocalAssignmentEmitter;
import org.spongepowered.despector.emitter.statement.ReturnEmitter;
import org.spongepowered.despector.emitter.statement.SwitchEmitter;
import org.spongepowered.despector.emitter.statement.ThrowEmitter;
import org.spongepowered.despector.emitter.statement.TryCatchEmitter;
import org.spongepowered.despector.emitter.statement.WhileEmitter;
import org.spongepowered.despector.emitter.type.ClassEntryEmitter;
import org.spongepowered.despector.emitter.type.EnumEntryEmitter;
import org.spongepowered.despector.emitter.type.FieldEntryEmitter;
import org.spongepowered.despector.emitter.type.InterfaceEntryEmitter;
import org.spongepowered.despector.emitter.type.MethodEntryEmitter;

public class Emitters {

    public static final EmitterSet JAVA = new EmitterSet();
    public static final EmitterSet KOTLIN = new EmitterSet();

    static {

        JAVA.setSpecialEmitter(AnnotationEmitter.class, new AnnotationEmitter());
        JAVA.setSpecialEmitter(GenericsEmitter.class, new GenericsEmitter());
        JAVA.setSpecialEmitter(AnonymousClassEmitter.class, new AnonymousClassEmitter());
        JAVA.setSpecialEmitter(PackageInfoEmitter.class, new PackageInfoEmitter());

        JAVA.setAstEmitter(ClassEntry.class, new ClassEntryEmitter());
        JAVA.setAstEmitter(EnumEntry.class, new EnumEntryEmitter());
        JAVA.setAstEmitter(InterfaceEntry.class, new InterfaceEntryEmitter());

        JAVA.setAstEmitter(FieldEntry.class, new FieldEntryEmitter());
        JAVA.setAstEmitter(MethodEntry.class, new MethodEntryEmitter());

        JAVA.setStatementEmitter(ArrayAssignment.class, new ArrayAssignmentEmitter());
        JAVA.setStatementEmitter(Break.class, new BreakEmitter());
        JAVA.setStatementEmitter(DoWhile.class, new DoWhileEmitter());
        FieldAssignmentEmitter fld_assign = new FieldAssignmentEmitter();
        JAVA.setStatementEmitter(FieldAssignment.class, fld_assign);
        JAVA.setStatementEmitter(InstanceFieldAssignment.class, fld_assign);
        JAVA.setStatementEmitter(StaticFieldAssignment.class, fld_assign);
        JAVA.setStatementEmitter(For.class, new ForEmitter());
        JAVA.setStatementEmitter(ForEach.class, new ForEachEmitter());
        JAVA.setStatementEmitter(If.class, new IfEmitter());
        JAVA.setStatementEmitter(Increment.class, new IncrementEmitter());
        JAVA.setStatementEmitter(InvokeStatement.class, new InvokeEmitter());
        JAVA.setStatementEmitter(LocalAssignment.class, new LocalAssignmentEmitter());
        JAVA.setStatementEmitter(Return.class, new ReturnEmitter());
        JAVA.setStatementEmitter(Switch.class, new SwitchEmitter());
        JAVA.setStatementEmitter(Throw.class, new ThrowEmitter());
        JAVA.setStatementEmitter(TryCatch.class, new TryCatchEmitter());
        JAVA.setStatementEmitter(While.class, new WhileEmitter());

        JAVA.setInstructionEmitter(ArrayAccess.class, new ArrayLoadEmitter());
        JAVA.setInstructionEmitter(Cast.class, new CastEmitter());
        JAVA.setInstructionEmitter(NumberCompare.class, new CompareEmitter());
        JAVA.setInstructionEmitter(DoubleConstant.class, new DoubleConstantEmitter());
        FieldEmitter fld = new FieldEmitter();
        JAVA.setInstructionEmitter(InstanceFieldAccess.class, fld);
        JAVA.setInstructionEmitter(FloatConstant.class, new FloatConstantEmitter());
        JAVA.setInstructionEmitter(InstanceMethodInvoke.class, new InstanceMethodInvokeEmitter());
        JAVA.setInstructionEmitter(InstanceOf.class, new InstanceOfEmitter());
        JAVA.setInstructionEmitter(IntConstant.class, new IntConstantEmitter());
        JAVA.setInstructionEmitter(LocalAccess.class, new LocalEmitter());
        JAVA.setInstructionEmitter(LongConstant.class, new LongConstantEmitter());
        JAVA.setInstructionEmitter(NegativeOperator.class, new NegativeEmitter());
        JAVA.setInstructionEmitter(NewArray.class, new NewArrayEmitter());
        JAVA.setInstructionEmitter(New.class, new NewEmitter());
        JAVA.setInstructionEmitter(NullConstant.class, new NullConstantEmitter());
        OperatorEmitter op = new OperatorEmitter();
        JAVA.setInstructionEmitter(AddOperator.class, op);
        JAVA.setInstructionEmitter(SubtractOperator.class, op);
        JAVA.setInstructionEmitter(MultiplyOperator.class, op);
        JAVA.setInstructionEmitter(DivideOperator.class, op);
        JAVA.setInstructionEmitter(RemainderOperator.class, op);
        JAVA.setInstructionEmitter(AndOperator.class, op);
        JAVA.setInstructionEmitter(OrOperator.class, op);
        JAVA.setInstructionEmitter(ShiftLeftOperator.class, op);
        JAVA.setInstructionEmitter(ShiftRightOperator.class, op);
        JAVA.setInstructionEmitter(UnsignedShiftRightOperator.class, op);
        JAVA.setInstructionEmitter(XorOperator.class, op);
        JAVA.setInstructionEmitter(StaticMethodInvoke.class, new StaticMethodInvokeEmitter());
        JAVA.setInstructionEmitter(StringConstant.class, new StringConstantEmitter());
        JAVA.setInstructionEmitter(Ternary.class, new TernaryEmitter());
        JAVA.setInstructionEmitter(TypeConstant.class, new TypeConstantEmitter());
        JAVA.setInstructionEmitter(StaticFieldAccess.class, fld);

        JAVA.setConditionEmitter(AndCondition.class, new AndConditionEmitter());
        JAVA.setConditionEmitter(OrCondition.class, new OrConditionEmitter());
        JAVA.setConditionEmitter(InverseCondition.class, new InverseConditionEmitter());
        JAVA.setConditionEmitter(CompareCondition.class, new CompareConditionEmitter());
        JAVA.setConditionEmitter(BooleanCondition.class, new BooleanConditionEmitter());

        KOTLIN.clone(JAVA);

        KOTLIN.setAstEmitter(ClassEntry.class, new KotlinClassEntryEmitter());
        KOTLIN.setAstEmitter(EnumEntry.class, new KotlinEnumEntryEmitter());
        KOTLIN.setAstEmitter(MethodEntry.class, new KotlinMethodEntryEmitter());
        
        KOTLIN.setSpecialEmitter(KotlinDataClassEmitter.class, new KotlinDataClassEmitter());
        KOTLIN.setSpecialEmitter(KotlinCompanionClassEmitter.class, new KotlinCompanionClassEmitter());
    }

}
