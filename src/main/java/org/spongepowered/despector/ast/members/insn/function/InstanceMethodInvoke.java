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
package org.spongepowered.despector.ast.members.insn.function;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;

import java.util.Arrays;

/**
 * A statement calling an instance method.
 */
public class InstanceMethodInvoke extends MethodInvoke {

    private Instruction callee;

    public InstanceMethodInvoke(String name, String desc, String owner, Instruction[] args, Instruction call) {
        super(name, desc, owner, args);
        this.callee = checkNotNull(call, "callee");
    }

    /**
     * Gets the object that the method is onvoked on.
     */
    public Instruction getCallee() {
        return this.callee;
    }

    /**
     * Sets the object that the method is onvoked on.
     */
    public void setCallee(Instruction callee) {
        this.callee = checkNotNull(callee, "callee");
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitInstanceMethodInvoke(this);
        this.callee.accept(visitor);
        super.accept(visitor);
    }

    @Override
    public String toString() {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < this.params.length; i++) {
            params.append(this.params[i]);
            if (i < this.params.length - 1) {
                params.append(", ");
            }
        }
        return this.callee + "." + this.method_name + "(" + params + ");";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof InstanceMethodInvoke)) {
            return false;
        }
        InstanceMethodInvoke insn = (InstanceMethodInvoke) obj;
        return this.callee.equals(insn.callee) && this.method_desc.equals(insn.method_desc) && this.method_name.equals(insn.method_name)
                && this.method_owner.equals(insn.method_owner) && Arrays.equals(this.params, insn.params);
    }

}
