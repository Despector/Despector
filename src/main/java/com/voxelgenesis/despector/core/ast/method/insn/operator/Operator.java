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
package com.voxelgenesis.despector.core.ast.method.insn.operator;

import static com.google.common.base.Preconditions.checkNotNull;

import com.voxelgenesis.despector.core.ast.method.insn.Instruction;
import com.voxelgenesis.despector.core.ast.signature.TypeSignature;
import com.voxelgenesis.despector.jvm.loader.JvmHelper;

/**
 * An abstract instruction arg for binary operator.
 */
public class Operator implements Instruction {

    private static final String PRIMATIVE_ORDERING = "ZBCSIJFD";

    protected OperatorType operator;
    protected Instruction left;
    protected Instruction right;

    public Operator(OperatorType type, Instruction left, Instruction right) {
        this.operator = checkNotNull(type, "operator");
        this.left = checkNotNull(left, "left");
        this.right = checkNotNull(right, "right");
    }

    public OperatorType getOperator() {
        return this.operator;
    }

    public void setOperator(OperatorType type) {
        this.operator = checkNotNull(type, "operator");
    }

    /**
     * Gets the left operand.
     */
    public Instruction getLeftOperand() {
        return this.left;
    }

    /**
     * Sets the left operand.
     */
    public void setLeftOperand(Instruction left) {
        this.left = checkNotNull(left, "left");
    }

    /**
     * Gets the right operand.
     */
    public Instruction getRightOperand() {
        return this.right;
    }

    /**
     * Sets the right operand.
     */
    public void setRightOperand(Instruction right) {
        this.right = checkNotNull(right, "right");
    }

    @Override
    public TypeSignature inferType() {
        int left_index = PRIMATIVE_ORDERING.indexOf(this.left.inferType().toString().charAt(0));
        int right_index = PRIMATIVE_ORDERING.indexOf(this.right.inferType().toString().charAt(0));
        return JvmHelper.of(String.valueOf(PRIMATIVE_ORDERING.charAt(Math.max(left_index, right_index))));
    }

    @Override
    public String toString() {
        return this.left.toString() + " " + this.operator.getSymbol() + " " + this.right.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        Operator insn = (Operator) obj;
        return this.left.equals(insn.left) && this.right.equals(insn.right) && this.operator == insn.operator;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.left.hashCode();
        h = h * 37 + this.right.hashCode();
        h = h * 37 + this.operator.hashCode();
        return h;
    }

}
