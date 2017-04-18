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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.InstructionVisitor;
import org.spongepowered.despector.util.TypeHelper;

/**
 * An abstract statement for making method invocations.
 */
public abstract class MethodInvoke implements Instruction {

    protected String method_name;
    protected String method_desc;
    protected String method_owner;
    protected Instruction[] params;

    public MethodInvoke(String name, String desc, String owner, Instruction[] args) {
        this.method_name = checkNotNull(name, "name");
        this.method_desc = checkNotNull(desc, "desc");
        this.method_owner = checkNotNull(owner, "owner");
        checkNotNull(args, "args");
        checkArgument(TypeHelper.paramCount(this.method_desc) == args.length);
        this.params = args;
    }

    /**
     * Gets the name of the method being invoked.
     */
    public String getMethodName() {
        return this.method_name;
    }

    /**
     * Sets the name of the method being invoked.
     */
    public void setMethodName(String name) {
        this.method_name = checkNotNull(name, "name");
    }

    /**
     * Gets the description of the method being invoked.
     */
    public String getMethodDescription() {
        return this.method_desc;
    }

    /**
     * Gets the return type of the method being invoked.
     */
    public String getReturnType() {
        return TypeHelper.getRet(this.method_desc);
    }

    /**
     * Sets the description of the method being invoked.
     */
    public void setMethodDescription(String desc) {
        this.method_desc = checkNotNull(desc, "desc");
    }

    /**
     * Gets the type description of the owner of the method being invoked.
     */
    public String getOwner() {
        return this.method_owner;
    }

    /**
     * Gets the internal name of the owner of the method being invoked.
     */
    public String getOwnerType() {
        return TypeHelper.descToType(this.method_owner);
    }

    /**
     * Sets the type description of the owner of the method being invoked.
     */
    public void setOwner(String type) {
        this.method_owner = checkNotNull(type, "owner");
    }

    /**
     * Gets the parameters of this method call.
     */
    public Instruction[] getParams() {
        return this.params;
    }

    /**
     * Sets the parameters of this method call.
     */
    public void setParameters(Instruction... args) {
        checkNotNull(args, "args");
        checkArgument(TypeHelper.paramCount(this.method_desc) == args.length);
        this.params = args;
    }

    @Override
    public TypeSignature inferType() {
        return ClassTypeSignature.of(getReturnType());
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (visitor instanceof InstructionVisitor) {
            for (Instruction insn : this.params) {
                insn.accept(visitor);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        MethodInvoke invoke = (MethodInvoke) o;
        if (this.params.length != invoke.params.length) {
            return false;
        }
        for (int i = 0; i < this.params.length; i++) {
            if (!this.params[i].equals(invoke.params[i])) {
                return false;
            }
        }
        return this.method_desc.equals(invoke.method_desc) && this.method_name.equals(invoke.method_name)
                && this.method_owner.equals(invoke.method_owner);
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.method_desc.hashCode();
        h = h * 37 + this.method_name.hashCode();
        h = h * 37 + this.method_owner.hashCode();
        for (Instruction insn : this.params) {
            h = h * 37 + insn.hashCode();
        }
        return h;
    }

}
