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
import org.spongepowered.despector.ast.members.insn.assign.FieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.members.insn.assign.LocalAssignment;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.members.insn.branch.DoWhile;
import org.spongepowered.despector.ast.members.insn.branch.For;
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
import org.spongepowered.despector.emitter.statement.ArrayAssignmentEmitter;
import org.spongepowered.despector.emitter.statement.DoWhileEmitter;
import org.spongepowered.despector.emitter.statement.FieldAssignmentEmitter;
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

    static {

        JAVA.setAnnotationEmitter(new AnnotationEmitter());

        JAVA.setAstEmitter(ClassEntry.class, new ClassEntryEmitter());
        JAVA.setAstEmitter(EnumEntry.class, new EnumEntryEmitter());
        JAVA.setAstEmitter(InterfaceEntry.class, new InterfaceEntryEmitter());

        JAVA.setAstEmitter(FieldEntry.class, new FieldEntryEmitter());
        JAVA.setAstEmitter(MethodEntry.class, new MethodEntryEmitter());

        JAVA.setStatementEmitter(ArrayAssignment.class, new ArrayAssignmentEmitter());
        JAVA.setStatementEmitter(DoWhile.class, new DoWhileEmitter());
        FieldAssignmentEmitter fld_assign = new FieldAssignmentEmitter();
        JAVA.setStatementEmitter(FieldAssignment.class, fld_assign);
        JAVA.setStatementEmitter(InstanceFieldAssignment.class, fld_assign);
        JAVA.setStatementEmitter(StaticFieldAssignment.class, fld_assign);
        JAVA.setStatementEmitter(For.class, new ForEmitter());
        JAVA.setStatementEmitter(If.class, new IfEmitter());
        JAVA.setStatementEmitter(Increment.class, new IncrementEmitter());
        JAVA.setStatementEmitter(InvokeStatement.class, new InvokeEmitter());
        JAVA.setStatementEmitter(LocalAssignment.class, new LocalAssignmentEmitter());
        JAVA.setStatementEmitter(Return.class, new ReturnEmitter());
        JAVA.setStatementEmitter(Switch.class, new SwitchEmitter());
        JAVA.setStatementEmitter(Throw.class, new ThrowEmitter());
        JAVA.setStatementEmitter(TryCatch.class, new TryCatchEmitter());
        JAVA.setStatementEmitter(While.class, new WhileEmitter());

        JAVA.setInstructionEmitter(ArrayLoadArg.class, new ArrayLoadEmitter());
        JAVA.setInstructionEmitter(CastArg.class, new CastEmitter());
        JAVA.setInstructionEmitter(CompareArg.class, new CompareEmitter());
        JAVA.setInstructionEmitter(DoubleConstantArg.class, new DoubleConstantEmitter());
        FieldEmitter fld = new FieldEmitter();
        JAVA.setInstructionEmitter(InstanceFieldArg.class, fld);
        JAVA.setInstructionEmitter(FloatConstantArg.class, new FloatConstantEmitter());
        JAVA.setInstructionEmitter(InstanceMethodInvoke.class, new InstanceMethodInvokeEmitter());
        JAVA.setInstructionEmitter(InstanceOfArg.class, new InstanceOfEmitter());
        JAVA.setInstructionEmitter(IntConstantArg.class, new IntConstantEmitter());
        JAVA.setInstructionEmitter(LocalArg.class, new LocalEmitter());
        JAVA.setInstructionEmitter(LongConstantArg.class, new LongConstantEmitter());
        JAVA.setInstructionEmitter(NegArg.class, new NegativeEmitter());
        JAVA.setInstructionEmitter(NewArrayArg.class, new NewArrayEmitter());
        JAVA.setInstructionEmitter(New.class, new NewEmitter());
        JAVA.setInstructionEmitter(NullConstantArg.class, new NullConstantEmitter());
        OperatorEmitter op = new OperatorEmitter();
        JAVA.setInstructionEmitter(AddArg.class, op);
        JAVA.setInstructionEmitter(SubtractArg.class, op);
        JAVA.setInstructionEmitter(MultiplyArg.class, op);
        JAVA.setInstructionEmitter(DivideArg.class, op);
        JAVA.setInstructionEmitter(RemainderInstruction.class, op);
        JAVA.setInstructionEmitter(AndInstruction.class, op);
        JAVA.setInstructionEmitter(OrArg.class, op);
        JAVA.setInstructionEmitter(ShiftLeftArg.class, op);
        JAVA.setInstructionEmitter(ShiftRightArg.class, op);
        JAVA.setInstructionEmitter(UnsignedShiftRightArg.class, op);
        JAVA.setInstructionEmitter(XorArg.class, op);
        JAVA.setInstructionEmitter(StaticMethodInvoke.class, new StaticMethodInvokeEmitter());
        JAVA.setInstructionEmitter(StringConstantArg.class, new StringConstantEmitter());
        JAVA.setInstructionEmitter(Ternary.class, new TernaryEmitter());
        JAVA.setInstructionEmitter(TypeConstantArg.class, new TypeConstantEmitter());
        JAVA.setInstructionEmitter(StaticFieldArg.class, fld);

        JAVA.setConditionEmitter(AndCondition.class, new AndConditionEmitter());
        JAVA.setConditionEmitter(OrCondition.class, new OrConditionEmitter());
        JAVA.setConditionEmitter(InverseCondition.class, new InverseConditionEmitter());
        JAVA.setConditionEmitter(CompareCondition.class, new CompareConditionEmitter());
        JAVA.setConditionEmitter(BooleanCondition.class, new BooleanConditionEmitter());
    }

}
