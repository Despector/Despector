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

import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.util.TypeHelper;

public abstract class FieldAssign extends Assignment {

    protected final String field_name;
    protected final String type_desc;
    protected final String owner;

    public FieldAssign(String field, String type_desc, String owner, Instruction val) {
        super(val);
        this.field_name = field;
        this.type_desc = type_desc;
        this.owner = owner;
    }

    public String getFieldName() {
        return this.field_name;
    }

    public String getTypeDescription() {
        return this.type_desc;
    }

    public String getOwner() {
        return this.owner;
    }
    
    public String getOwnerName() {
        return TypeHelper.descToType(this.owner);
    }

//    @Override
//    public TypeEntry inferType() {
//        return this.field.getType();
//    }

}
