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
package org.spongepowered.despector.ast.stmt.invoke;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.InstructionVisitor;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;

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
    public void accept(AstVisitor visitor) {
        if (visitor instanceof InstructionVisitor) {
            ((InstructionVisitor) visitor).visitInstanceMethodInvoke(this);
        }
        this.callee.accept(visitor);
        super.accept(visitor);
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(6);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_INSTANCE_INVOKE);
        pack.writeString("name").writeString(this.method_name);
        pack.writeString("owner").writeString(this.method_owner);
        pack.writeString("desc").writeString(this.method_desc);
        pack.writeString("params").startArray(this.params.length);
        for (Instruction insn : this.params) {
            insn.writeTo(pack);
        }
        pack.writeString("callee");
        this.callee.writeTo(pack);
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
        if (!super.equals(obj)) {
            return false;
        }
        InstanceMethodInvoke insn = (InstanceMethodInvoke) obj;
        return this.callee.equals(insn.callee);
    }

    @Override
    public int hashCode() {
        int h = super.hashCode();
        h = h * 37 + this.callee.hashCode();
        return h;
    }

}
