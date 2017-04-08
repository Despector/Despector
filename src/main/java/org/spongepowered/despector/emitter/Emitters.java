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
package org.spongepowered.despector.emitter;

import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.kotlin.Elvis;
import org.spongepowered.despector.ast.kotlin.When;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Comment;
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
import org.spongepowered.despector.ast.members.insn.function.DynamicInvokeHandle;
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
import org.spongepowered.despector.emitter.instruction.DynamicInvokeEmitter;
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
import org.spongepowered.despector.emitter.kotlin.condition.KotlinBooleanConditionEmitter;
import org.spongepowered.despector.emitter.kotlin.condition.KotlinCompareConditionEmitter;
import org.spongepowered.despector.emitter.kotlin.instruction.ElvisEmitter;
import org.spongepowered.despector.emitter.kotlin.instruction.KotlinCastEmitter;
import org.spongepowered.despector.emitter.kotlin.instruction.KotlinInstanceOfEmitter;
import org.spongepowered.despector.emitter.kotlin.instruction.KotlinOperatorEmitter;
import org.spongepowered.despector.emitter.kotlin.instruction.KotlinTernaryEmitter;
import org.spongepowered.despector.emitter.kotlin.instruction.WhenEmitter;
import org.spongepowered.despector.emitter.kotlin.instruction.method.KotlinInstanceMethodInvokeEmitter;
import org.spongepowered.despector.emitter.kotlin.instruction.method.KotlinStaticMethodInvokeEmitter;
import org.spongepowered.despector.emitter.kotlin.special.KotlinCompanionClassEmitter;
import org.spongepowered.despector.emitter.kotlin.special.KotlinDataClassEmitter;
import org.spongepowered.despector.emitter.kotlin.special.KotlinGenericsEmitter;
import org.spongepowered.despector.emitter.kotlin.special.KotlinPackageEmitter;
import org.spongepowered.despector.emitter.kotlin.statement.KotlinForEachEmitter;
import org.spongepowered.despector.emitter.kotlin.statement.KotlinForEmitter;
import org.spongepowered.despector.emitter.kotlin.statement.KotlinInvokeEmitter;
import org.spongepowered.despector.emitter.kotlin.statement.KotlinLocalAssignmentEmitter;
import org.spongepowered.despector.emitter.kotlin.type.KotlinClassEntryEmitter;
import org.spongepowered.despector.emitter.kotlin.type.KotlinEnumEntryEmitter;
import org.spongepowered.despector.emitter.kotlin.type.KotlinMethodEntryEmitter;
import org.spongepowered.despector.emitter.special.AnnotationEmitter;
import org.spongepowered.despector.emitter.special.AnonymousClassEmitter;
import org.spongepowered.despector.emitter.special.GenericsEmitter;
import org.spongepowered.despector.emitter.special.PackageEmitter;
import org.spongepowered.despector.emitter.special.PackageInfoEmitter;
import org.spongepowered.despector.emitter.statement.ArrayAssignmentEmitter;
import org.spongepowered.despector.emitter.statement.BreakEmitter;
import org.spongepowered.despector.emitter.statement.CommentEmitter;
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

import java.util.EnumMap;

/**
 * Standard emitters.
 */
public final class Emitters {

    public static final EmitterSet JAVA_SET = new EmitterSet();
    public static final EmitterSet KOTLIN_SET = new EmitterSet();

    public static final Emitter JAVA = new JavaEmitter();
    public static final Emitter KOTLIN = new KotlinEmitter();
    public static final Emitter WILD = new WildEmitter();

    private static final EnumMap<Language, Emitter> EMITTERS = new EnumMap<>(Language.class);

    static {

        JAVA_SET.setSpecialEmitter(AnnotationEmitter.class, new AnnotationEmitter());
        JAVA_SET.setSpecialEmitter(GenericsEmitter.class, new GenericsEmitter());
        JAVA_SET.setSpecialEmitter(AnonymousClassEmitter.class, new AnonymousClassEmitter());
        JAVA_SET.setSpecialEmitter(PackageInfoEmitter.class, new PackageInfoEmitter());
        JAVA_SET.setSpecialEmitter(PackageEmitter.class, new PackageEmitter());

        JAVA_SET.setAstEmitter(ClassEntry.class, new ClassEntryEmitter());
        JAVA_SET.setAstEmitter(EnumEntry.class, new EnumEntryEmitter());
        JAVA_SET.setAstEmitter(InterfaceEntry.class, new InterfaceEntryEmitter());

        JAVA_SET.setAstEmitter(FieldEntry.class, new FieldEntryEmitter());
        JAVA_SET.setAstEmitter(MethodEntry.class, new MethodEntryEmitter());

        JAVA_SET.setStatementEmitter(ArrayAssignment.class, new ArrayAssignmentEmitter());
        JAVA_SET.setStatementEmitter(Break.class, new BreakEmitter());
        JAVA_SET.setStatementEmitter(Comment.class, new CommentEmitter());
        JAVA_SET.setStatementEmitter(DoWhile.class, new DoWhileEmitter());
        FieldAssignmentEmitter fld_assign = new FieldAssignmentEmitter();
        JAVA_SET.setStatementEmitter(FieldAssignment.class, fld_assign);
        JAVA_SET.setStatementEmitter(InstanceFieldAssignment.class, fld_assign);
        JAVA_SET.setStatementEmitter(StaticFieldAssignment.class, fld_assign);
        JAVA_SET.setStatementEmitter(For.class, new ForEmitter());
        JAVA_SET.setStatementEmitter(ForEach.class, new ForEachEmitter());
        JAVA_SET.setStatementEmitter(If.class, new IfEmitter());
        JAVA_SET.setStatementEmitter(Increment.class, new IncrementEmitter());
        JAVA_SET.setStatementEmitter(InvokeStatement.class, new InvokeEmitter());
        JAVA_SET.setStatementEmitter(LocalAssignment.class, new LocalAssignmentEmitter());
        JAVA_SET.setStatementEmitter(Return.class, new ReturnEmitter());
        JAVA_SET.setStatementEmitter(Switch.class, new SwitchEmitter());
        JAVA_SET.setStatementEmitter(Throw.class, new ThrowEmitter());
        JAVA_SET.setStatementEmitter(TryCatch.class, new TryCatchEmitter());
        JAVA_SET.setStatementEmitter(While.class, new WhileEmitter());

        JAVA_SET.setInstructionEmitter(ArrayAccess.class, new ArrayLoadEmitter());
        JAVA_SET.setInstructionEmitter(Cast.class, new CastEmitter());
        JAVA_SET.setInstructionEmitter(NumberCompare.class, new CompareEmitter());
        JAVA_SET.setInstructionEmitter(DoubleConstant.class, new DoubleConstantEmitter());
        FieldEmitter fld = new FieldEmitter();
        JAVA_SET.setInstructionEmitter(InstanceFieldAccess.class, fld);
        JAVA_SET.setInstructionEmitter(FloatConstant.class, new FloatConstantEmitter());
        JAVA_SET.setInstructionEmitter(InstanceMethodInvoke.class, new InstanceMethodInvokeEmitter());
        JAVA_SET.setInstructionEmitter(InstanceOf.class, new InstanceOfEmitter());
        JAVA_SET.setInstructionEmitter(IntConstant.class, new IntConstantEmitter());
        JAVA_SET.setInstructionEmitter(LocalAccess.class, new LocalEmitter());
        JAVA_SET.setInstructionEmitter(LongConstant.class, new LongConstantEmitter());
        JAVA_SET.setInstructionEmitter(NegativeOperator.class, new NegativeEmitter());
        JAVA_SET.setInstructionEmitter(NewArray.class, new NewArrayEmitter());
        JAVA_SET.setInstructionEmitter(New.class, new NewEmitter());
        JAVA_SET.setInstructionEmitter(NullConstant.class, new NullConstantEmitter());
        OperatorEmitter op = new OperatorEmitter();
        JAVA_SET.setInstructionEmitter(Operator.class, op);
        JAVA_SET.setInstructionEmitter(StaticMethodInvoke.class, new StaticMethodInvokeEmitter());
        JAVA_SET.setInstructionEmitter(StringConstant.class, new StringConstantEmitter());
        JAVA_SET.setInstructionEmitter(Ternary.class, new TernaryEmitter());
        JAVA_SET.setInstructionEmitter(TypeConstant.class, new TypeConstantEmitter());
        JAVA_SET.setInstructionEmitter(StaticFieldAccess.class, fld);
        JAVA_SET.setInstructionEmitter(DynamicInvokeHandle.class, new DynamicInvokeEmitter());

        JAVA_SET.setConditionEmitter(AndCondition.class, new AndConditionEmitter());
        JAVA_SET.setConditionEmitter(OrCondition.class, new OrConditionEmitter());
        JAVA_SET.setConditionEmitter(InverseCondition.class, new InverseConditionEmitter());
        JAVA_SET.setConditionEmitter(CompareCondition.class, new CompareConditionEmitter());
        JAVA_SET.setConditionEmitter(BooleanCondition.class, new BooleanConditionEmitter());

        KOTLIN_SET.clone(JAVA_SET);

        KOTLIN_SET.setAstEmitter(ClassEntry.class, new KotlinClassEntryEmitter());
        KOTLIN_SET.setAstEmitter(EnumEntry.class, new KotlinEnumEntryEmitter());
        KOTLIN_SET.setAstEmitter(MethodEntry.class, new KotlinMethodEntryEmitter());

        KOTLIN_SET.setSpecialEmitter(KotlinDataClassEmitter.class, new KotlinDataClassEmitter());
        KOTLIN_SET.setSpecialEmitter(KotlinCompanionClassEmitter.class, new KotlinCompanionClassEmitter());
        KOTLIN_SET.setSpecialEmitter(PackageEmitter.class, new KotlinPackageEmitter());
        KOTLIN_SET.setSpecialEmitter(GenericsEmitter.class, new KotlinGenericsEmitter());

        KOTLIN_SET.setStatementEmitter(InvokeStatement.class, new KotlinInvokeEmitter());
        KOTLIN_SET.setStatementEmitter(LocalAssignment.class, new KotlinLocalAssignmentEmitter());
        KOTLIN_SET.setStatementEmitter(ForEach.class, new KotlinForEachEmitter());
        KOTLIN_SET.setStatementEmitter(For.class, new KotlinForEmitter());

        KOTLIN_SET.setInstructionEmitter(InstanceMethodInvoke.class, new KotlinInstanceMethodInvokeEmitter());
        KOTLIN_SET.setInstructionEmitter(StaticMethodInvoke.class, new KotlinStaticMethodInvokeEmitter());
        KOTLIN_SET.setInstructionEmitter(Ternary.class, new KotlinTernaryEmitter());
        KOTLIN_SET.setInstructionEmitter(InstanceOf.class, new KotlinInstanceOfEmitter());
        KOTLIN_SET.setInstructionEmitter(Cast.class, new KotlinCastEmitter());
        KOTLIN_SET.setInstructionEmitter(Elvis.class, new ElvisEmitter());
        KOTLIN_SET.setInstructionEmitter(When.class, new WhenEmitter());
        KOTLIN_SET.setInstructionEmitter(Operator.class, new KotlinOperatorEmitter());

        KOTLIN_SET.setConditionEmitter(BooleanCondition.class, new KotlinBooleanConditionEmitter());
        KOTLIN_SET.setConditionEmitter(CompareCondition.class, new KotlinCompareConditionEmitter());

        EMITTERS.put(Language.JAVA, JAVA);
        EMITTERS.put(Language.KOTLIN, KOTLIN);
        EMITTERS.put(Language.ANY, WILD);
    }

    /**
     * Gets the emitter for the given language.
     */
    public static Emitter get(Language lang) {
        return EMITTERS.get(lang);
    }

    private Emitters() {

    }

}
