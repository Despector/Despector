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
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.util.TypeHelper;

/**
 * A statement instantiating a new instance of a type.
 */
public class NewInstance implements Statement {

    private String type;
    private String ctor;
    private Instruction[] params;

    public NewInstance(String type, String ctor_desc, Instruction[] args) {
        this.type = checkNotNull(type, "type");
        this.ctor = checkNotNull(ctor_desc, "ctor_desc");
        this.params = checkNotNull(args, "args");
    }

    public String getCtorDescription() {
        return this.ctor;
    }

    public void setCtorDescription(String desc) {
        this.ctor = checkNotNull(desc, "ctor_desc");
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = checkNotNull(type, "type");
    }

    public Instruction[] getParams() {
        return this.params;
    }

    public void setParameters(Instruction... args) {
        this.params = checkNotNull(args, "args");
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitNewInstance(this);
        for (Instruction insn : this.params) {
            insn.accept(visitor);
        }
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
        return "new " + TypeHelper.descToType(this.type) + "(" + params + ");";
    }

}
