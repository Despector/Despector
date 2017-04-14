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
package com.voxelgenesis.despector.core.ast.method.stmt.assign;

import com.voxelgenesis.despector.core.ast.method.insn.Instruction;
import com.voxelgenesis.despector.core.ast.visitor.AstVisitor;
import com.voxelgenesis.despector.core.ast.visitor.StatementVisitor;
import org.spongepowered.despector.util.TypeHelper;

/**
 * An assignment to a static field.
 */
public class StaticFieldAssignment extends FieldAssignment {

    public StaticFieldAssignment(String field, String type_desc, String owner, Instruction val) {
        super(field, type_desc, owner, val);
    }

    @Override
    public void accept(AstVisitor visitor) {
        if(visitor instanceof StatementVisitor) {
            ((StatementVisitor) visitor).visitStaticFieldAssignment(this);
        }
    }

    @Override
    public String toString() {
        return TypeHelper.descToTypeName(this.owner_type) + "." + this.field_name + " = " + this.val + ";";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        StaticFieldAssignment insn = (StaticFieldAssignment) obj;
        return this.val.equals(insn.val);
    }

    @Override
    public int hashCode() {
        int h = super.hashCode();
        h = h * 37 + this.val.hashCode();
        return h;
    }

}
