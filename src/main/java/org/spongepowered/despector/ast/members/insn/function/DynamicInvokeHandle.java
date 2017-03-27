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

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;

public class DynamicInvokeHandle implements Instruction {

    private String type;
    private String name;
    private String lambda_owner;
    private String lambda_method;
    private String lambda_desc;

    public DynamicInvokeHandle(String owner, String method, String desc, String type, String name) {
        this.lambda_owner = owner;
        this.lambda_method = method;
        this.lambda_desc = desc;
        this.type = type;
        this.name = name;
    }

    public String getLambdaOwner() {
        return this.lambda_owner;
    }

    public String getLambdaMethod() {
        return this.lambda_method;
    }

    public String getLambdaDescription() {
        return this.lambda_desc;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String inferType() {
        return null;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
    }

}
