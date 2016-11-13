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
