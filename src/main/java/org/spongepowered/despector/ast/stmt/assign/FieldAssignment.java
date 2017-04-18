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
package org.spongepowered.despector.ast.stmt.assign;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.util.TypeHelper;

/**
 * An assignment to an instance or static field.
 */
public abstract class FieldAssignment extends Assignment {

    protected String field_name;
    protected String type_desc;
    protected String owner_type;

    protected boolean initializer = false;

    public FieldAssignment(String field, String type_desc, String owner, Instruction val) {
        super(val);
        this.field_name = checkNotNull(field, "field");
        this.type_desc = checkNotNull(type_desc, "field_desc");
        this.owner_type = checkNotNull(owner, "owner");
    }

    /**
     * Gets the field name.
     */
    public String getFieldName() {
        return this.field_name;
    }

    /**
     * Sets the field name.
     */
    public void setFieldName(String name) {
        this.field_name = checkNotNull(name, "name");
    }

    /**
     * Gets the field's type description.
     */
    public String getFieldDescription() {
        return this.type_desc;
    }

    /**
     * Sets the field's type description.
     */
    public void setFieldDescription(String desc) {
        this.type_desc = checkNotNull(desc, "desc");
    }

    /**
     * Gets the field owner's type description.
     */
    public String getOwnerType() {
        return this.owner_type;
    }

    /**
     * Gets the field owner's type internal name.
     */
    public String getOwnerName() {
        return TypeHelper.descToType(this.owner_type);
    }

    /**
     * Sets the field owner's type description.
     */
    public void setOwner(String owner) {
        this.owner_type = checkNotNull(owner, "owner");
    }

    /**
     * Gets if this field assignment is an initializer and therefore should be
     * ignored when encountered in a method.
     */
    public boolean isInitializer() {
        return this.initializer;
    }

    /**
     * Sets if this field assignment is an initializer.
     * 
     * @see #isInitializer()
     */
    public void setInitializer(boolean state) {
        this.initializer = state;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !o.getClass().equals(getClass())) {
            return false;
        }
        FieldAssignment assign = (FieldAssignment) o;
        return this.field_name.equals(assign.field_name) && this.owner_type.equals(assign.owner_type) && this.type_desc.equals(assign.type_desc);
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.field_name.hashCode();
        h = h * 37 + this.owner_type.hashCode();
        h = h * 37 + this.type_desc.hashCode();
        return h;
    }

}
