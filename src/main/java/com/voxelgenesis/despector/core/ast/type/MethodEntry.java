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
import com.voxelgenesis.despector.core.ast.visitor.AstVisitor;
import com.voxelgenesis.despector.core.ast.visitor.TypeVisitor;
import com.voxelgenesis.despector.core.ir.InsnBlock;

import java.util.List;

public class MethodEntry {

    private final String name;

    private List<TypeSignature> param_types;
    private TypeSignature return_type;

    private boolean is_static;
    private boolean is_native;

    private InsnBlock ir;
    private StatementBlock statements;

    private Locals locals;

    public MethodEntry(String name, List<TypeSignature> param_types, TypeSignature returntype) {
        this.name = name;

        this.locals = new Locals();

        this.return_type = returntype;
        this.param_types = param_types;
    }

    public String getName() {
        return this.name;
    }

    public String getSignature() {
        StringBuilder sig = new StringBuilder("(");
        for (TypeSignature param : this.param_types) {
            sig.append(param.getDescriptor());
        }
        sig.append(")");
        sig.append(this.return_type.getDescriptor());
        return sig.toString();
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

    public boolean isNative() {
        return this.is_native;
    }

    public void setNative(boolean state) {
        this.is_native = state;
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

    public void accept(AstVisitor visitor) {
        if (visitor instanceof TypeVisitor) {
            ((TypeVisitor) visitor).visitMethod(this);
        }
        if (this.statements != null) {
            this.statements.accept(visitor);
        }
    }

}
