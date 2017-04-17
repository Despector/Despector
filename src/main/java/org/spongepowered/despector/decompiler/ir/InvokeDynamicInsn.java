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
package org.spongepowered.despector.decompiler.ir;

import com.google.common.base.MoreObjects;

public class InvokeDynamicInsn extends Insn {

    private String lambda_owner;
    private String lambda_name;
    private String lambda_desc;
    private String name;
    private String type;

    public InvokeDynamicInsn(int op, String lambda_owner, String lambda_name, String lambda_desc, String name, String type) {
        super(op);
        this.lambda_owner = lambda_owner;
        this.lambda_name = lambda_name;
        this.lambda_desc = lambda_desc;
        this.name = name;
        this.type = type;
    }

    public String getLambdaOwner() {
        return this.lambda_owner;
    }

    public String getLambdaName() {
        return this.lambda_name;
    }

    public String getLambdaDescription() {
        return this.lambda_desc;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("lambda_owner", this.lambda_owner)
                .add("lambda_name", this.lambda_name)
                .add("lambda_desc", this.lambda_desc)
                .add("name", this.name)
                .add("type", this.type)
                .toString();
    }

}
