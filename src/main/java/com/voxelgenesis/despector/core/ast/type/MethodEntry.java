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
package com.voxelgenesis.despector.core.ast.type;

import com.voxelgenesis.despector.core.ast.method.Locals;
import com.voxelgenesis.despector.core.ast.method.StatementBlock;
import com.voxelgenesis.despector.core.ast.signature.TypeSignature;
import com.voxelgenesis.despector.core.ir.InsnBlock;
import com.voxelgenesis.despector.jvm.loader.JvmHelper;
import org.spongepowered.despector.util.TypeHelper;

import java.util.ArrayList;
import java.util.List;

public class MethodEntry {

    private final String name;
    private final String signature;

    private List<TypeSignature> param_types;
    private TypeSignature return_type;

    private boolean is_static;

    private InsnBlock ir;
    private StatementBlock statements;

    private Locals locals;

    public MethodEntry(String name, String signature) {
        this.name = name;
        this.signature = signature;

        this.locals = new Locals();

        this.return_type = JvmHelper.of(TypeHelper.getRet(this.signature));
        this.param_types = new ArrayList<>();
        for (String param : TypeHelper.splitSig(this.signature)) {
            this.param_types.add(JvmHelper.of(param));
        }
    }

    public String getName() {
        return this.name;
    }

    public String getSignature() {
        return this.signature;
    }

    public List<TypeSignature> getParamTypes() {
        return this.param_types;
    }

    public TypeSignature getReturnType() {
        return this.return_type;
    }

    public boolean isStatic() {
        return this.is_static;
    }

    public void setStatic(boolean state) {
        this.is_static = state;
    }

    public InsnBlock getIR() {
        return this.ir;
    }

    public void setIR(InsnBlock ir) {
        this.ir = ir;
    }

    public StatementBlock getStatements() {
        return this.statements;
    }

    public void setStatements(StatementBlock block) {
        this.statements = block;
    }

    public Locals getLocals() {
        return this.locals;
    }

}
