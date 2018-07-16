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
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.InstructionVisitor;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;

/**
 * A dynamic method invoke instruction.
 */
public class Lambda implements Instruction {

    private TypeSignature type;
    private String name;
    private String lambda_owner;
    private String lambda_method;
    private String lambda_desc;

    public Lambda(String owner, String method, String desc, TypeSignature type, String name) {
        this.lambda_owner = checkNotNull(owner, "owner");
        this.lambda_method = checkNotNull(method, "method");
        this.lambda_desc = checkNotNull(desc, "desc");
        this.type = checkNotNull(type, "type");
        this.name = checkNotNull(name, "name");
    }

    /**
     * Gets the owner of the lambda.
     */
    public String getLambdaOwner() {
        return this.lambda_owner;
    }

    /**
     * Gets the lambda method.
     */
    public String getLambdaMethod() {
        return this.lambda_method;
    }

    /**
     * Gets the lambda description.
     */
    public String getLambdaDescription() {
        return this.lambda_desc;
    }

    /**
     * Gets the lambda return type.
     */
    public TypeSignature getType() {
        return this.type;
    }

    /**
     * Gets the lambda name.
     */
    public String getName() {
        return this.name;
    }

    @Override
    public TypeSignature inferType() {
        return this.type;
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (visitor instanceof InstructionVisitor) {
            ((InstructionVisitor) visitor).visitDynamicInvoke(this);
        }
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(6);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_DYNAMIC_INVOKE);
        pack.writeString("type");
        this.type.writeTo(pack);
        pack.writeString("name").writeString(this.name);
        pack.writeString("owner").writeString(this.lambda_owner);
        pack.writeString("method").writeString(this.lambda_method);
        pack.writeString("desc").writeString(this.lambda_desc);
        pack.endMap();
    }

}
