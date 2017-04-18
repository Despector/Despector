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
package org.spongepowered.despector.ast.insn.var;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.util.TypeHelper;

/**
 * An instruction which loads a value from a field.
 */
public abstract class FieldAccess implements Instruction {

    protected String field_name;
    protected TypeSignature field_desc;
    protected String owner_type;

    public FieldAccess(String name, TypeSignature desc, String owner) {
        this.field_name = checkNotNull(name, "name");
        this.field_desc = checkNotNull(desc, "desc");
        this.owner_type = checkNotNull(owner, "owner");
    }

    /**
     * Gets the accessed field name.
     */
    public String getFieldName() {
        return this.field_name;
    }

    /**
     * Sets the accessed field name.
     */
    public void setFieldName(String name) {
        this.field_name = checkNotNull(name, "name");
    }

    /**
     * Gets the accessed field descriptor.
     */
    public TypeSignature getTypeDescriptor() {
        return this.field_desc;
    }

    /**
     * Sets the accessed field descriptor.
     */
    public void setTypeDescriptor(TypeSignature desc) {
        this.field_desc = checkNotNull(desc, "desc");
    }

    /**
     * Gets the accessed field owner's type.
     */
    public String getOwnerType() {
        return this.owner_type;
    }

    /**
     * Sets the accessed field owner's type.
     */
    public void setOwnerType(String owner) {
        this.owner_type = checkNotNull(owner, "owner");
    }

    /**
     * Gets the accessed field owner's internal name.
     */
    public String getOwnerName() {
        return TypeHelper.descToType(this.owner_type);
    }

    @Override
    public TypeSignature inferType() {
        return this.field_desc;
    }

}
