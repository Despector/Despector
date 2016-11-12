/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.despector.ast.io.emitter;

import com.google.common.collect.Lists;
import org.spongepowered.despector.ast.members.insn.branch.condition.AndCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.ast.members.insn.branch.condition.InverseCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.OrCondition;

import java.util.List;

public class ConditionSimplifier {

    public static Condition invert(Condition val) {
        if (val instanceof InverseCondition) {
            return ((InverseCondition) val).getConditionValue();
        } else if (val instanceof BooleanCondition) {
            BooleanCondition bval = (BooleanCondition) val;
            bval.setInverse(!bval.isInverse());
            return bval;
        } else if (val instanceof CompareCondition) {
            CompareCondition compare = (CompareCondition) val;
            compare.setOp(compare.getOp().inverse());
            return compare;
        } else if (val instanceof AndCondition) {
            AndCondition and = (AndCondition) val;
            List<Condition> operands = Lists.newArrayList();
            for (Condition c : and.getOperands()) {
                operands.add(invert(c));
            }
            return new OrCondition(operands);
        } else if (val instanceof OrCondition) {
            OrCondition and = (OrCondition) val;
            List<Condition> operands = Lists.newArrayList();
            for (Condition c : and.getOperands()) {
                operands.add(invert(c));
            }
            return new AndCondition(operands);
        }
        return new InverseCondition(val);
    }

    public static Condition simplify(Condition condition) {
        if (condition instanceof InverseCondition) {
            Condition val = simplify(((InverseCondition) condition).getConditionValue());
            if (val instanceof InverseCondition) {
                return ((InverseCondition) val).getConditionValue();
            } else if (val instanceof BooleanCondition) {
                BooleanCondition bval = (BooleanCondition) val;
                bval.setInverse(!bval.isInverse());
                return bval;
            } else if (val instanceof CompareCondition) {
                CompareCondition compare = (CompareCondition) val;
                compare.setOp(compare.getOp().inverse());
                return compare;
            } else if (val instanceof AndCondition) {
                AndCondition and = (AndCondition) val;
                List<Condition> operands = Lists.newArrayList();
                for (Condition c : and.getOperands()) {
                    operands.add(invert(c));
                }
                return new OrCondition(operands);
            } else if (val instanceof OrCondition) {
                OrCondition and = (OrCondition) val;
                List<Condition> operands = Lists.newArrayList();
                for (Condition c : and.getOperands()) {
                    operands.add(invert(c));
                }
                return new AndCondition(operands);
            }
            ((InverseCondition) condition).setConditionValue(val);
        }
        // TODO can do other simplifications like resolving tautologies but the
        // compiler usually does those so its not that important until this
        // supports class gen better
        return condition;
    }

}
