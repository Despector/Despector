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
package org.spongepowered.despector.ast.members.insn.assign;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.util.TypeHelper;

public abstract class FieldAssign extends Assignment {

    protected String field_name;
    protected String type_desc;
    protected String owner_type;

    public FieldAssign(String field, String type_desc, String owner, Instruction val) {
        super(val);
        this.field_name = checkNotNull(field, "field");
        this.type_desc = checkNotNull(type_desc, "field_desc");
        this.owner_type = checkNotNull(owner, "owner");
    }

    public String getFieldName() {
        return this.field_name;
    }

    public void setFieldName(String name) {
        this.field_name = checkNotNull(name, "name");
    }

    public String getFieldDescription() {
        return this.type_desc;
    }

    public void setFieldDescription(String desc) {
        this.type_desc = checkNotNull(desc, "desc");
    }

    public String getOwnerType() {
        return this.owner_type;
    }

    public String getOwnerName() {
        return TypeHelper.descToType(this.owner_type);
    }

    public void setOwner(String owner) {
        this.owner_type = checkNotNull(owner, "owner");
    }

}
