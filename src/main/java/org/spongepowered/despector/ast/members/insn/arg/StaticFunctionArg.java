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
package org.spongepowered.despector.ast.members.insn.arg;

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.util.TypeHelper;

public class StaticFunctionArg implements Instruction {

    private final String method_name;
    private final String method_desc;
    private final String method_owner;
    private final Instruction[] params;

    public StaticFunctionArg(String name, String desc, String owner, Instruction[] args) {
        this.method_name = name;
        this.method_desc = desc;
        this.method_owner = owner;
        this.params = args;
    }

    public String getMethodName() {
        return this.method_name;
    }

    public String getMethodDescription() {
        return this.method_desc;
    }

    public String getReturnType() {
        return TypeHelper.descToType(this.method_desc);
    }

    public String getOwner() {
        return this.method_owner;
    }

    public Instruction[] getParams() {
        return this.params;
    }

    @Override
    public String inferType() {
        return getReturnType();
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitStaticFunctionArg(this);
        for(Instruction i: this.params) {
            i.accept(visitor);
        }
    }

    @Override
    public String toString() {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < this.params.length; i++) {
            params.append(this.params[i].toString());
            if (i < this.params.length - 1) {
                params.append(", ");
            }
        }
        return this.method_name + "(" + params.toString() + ")";
    }

}
