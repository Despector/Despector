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
package org.spongepowered.despector.ast.members.insn.arg.field;

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.util.TypeHelper;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;

/**
 * An instruction for accessing a static field.
 */
public class StaticFieldAccess extends FieldAccess {

    public StaticFieldAccess(String name, String desc, String owner) {
        super(name, desc, owner);
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitStaticFieldAccess(this);
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(5);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_STATIC_FIELD_ACCESS);
        pack.writeString("name").writeString(this.field_name);
        pack.writeString("desc").writeString(this.field_desc);
        pack.writeString("owner").writeString(this.owner_type);
        pack.writeString("signature");
        this.signature.writeTo(pack);
    }

    @Override
    public String toString() {
        return TypeHelper.descToTypeName(this.owner_type) + "." + this.field_name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StaticFieldAccess)) {
            return false;
        }
        StaticFieldAccess insn = (StaticFieldAccess) obj;
        return this.field_desc.equals(insn.field_desc) && this.field_name.equals(insn.field_name) && this.owner_type.equals(insn.owner_type);
    }

}
