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
package com.voxelgenesis.despector.core.ast.method.insn.var;

import static com.google.common.base.Preconditions.checkNotNull;

import com.voxelgenesis.despector.core.ast.method.insn.Instruction;
import com.voxelgenesis.despector.core.ast.signature.TypeSignature;
import com.voxelgenesis.despector.core.ast.visitor.AstVisitor;
import com.voxelgenesis.despector.core.ast.visitor.InstructionVisitor;

/**
 * An instruction for accessing a field from an object.
 */
public class InstanceFieldAccess extends FieldAccess {

    protected Instruction owner;

    public InstanceFieldAccess(String name, TypeSignature desc, String owner_type, Instruction owner) {
        super(name, desc, owner_type);
        this.owner = checkNotNull(owner, "owner");
    }

    /**
     * Gets the object the field is referenced from.
     */
    public Instruction getFieldOwner() {
        return this.owner;
    }

    /**
     * Sets the object the field is referenced from.
     */
    public void setFieldOwner(Instruction owner) {
        this.owner = checkNotNull(owner, "owner");
    }

    @Override
    public void accept(AstVisitor visitor) {
        if(visitor instanceof InstructionVisitor) {
            ((InstructionVisitor) visitor).visitInstanceFieldAccess(this);
        }
        this.owner.accept(visitor);
    }

    @Override
    public String toString() {
        return this.owner + "." + this.field_name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof InstanceFieldAccess)) {
            return false;
        }
        InstanceFieldAccess insn = (InstanceFieldAccess) obj;
        return this.field_desc.equals(insn.field_desc) && this.field_name.equals(insn.field_name) && this.owner_type.equals(insn.owner_type)
                && this.owner.equals(insn.owner);
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.field_desc.hashCode();
        h = h * 37 + this.field_name.hashCode();
        h = h * 37 + this.owner_type.hashCode();
        h = h * 37 + this.owner.hashCode();
        return h;
    }

}
