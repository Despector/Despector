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
package org.spongepowered.despector.ast.members.insn.branch;

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.List;

/**
 * A statement breaking control flow out of a surrounding loop.
 */
public class Break implements Statement {

    private Breakable loop;
    private Type type;
    private boolean nested;

    public Break(Breakable loop, Type type, boolean nested) {
        this.loop = loop;
        this.type = type;
        this.nested = nested;
    }

    /**
     * Gets the loop being broken.
     */
    public Breakable getLoop() {
        return this.loop;
    }

    /**
     * Sets the loop being broken.
     */
    public void setLoop(Breakable loop) {
        this.loop = loop;
    }

    /**
     * Gets whether this is a break or a continue statement.
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Sets whether this is a break or a continue statement.
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets if this break is breaking a loop other than the immediate
     * surrounding loop and will therefore require a label on the outer loop.
     */
    public boolean isNested() {
        return this.nested;
    }

    /**
     * Sets if this break is breaking a loop other than the immediate
     * surrounding loop and will therefore require a label on the outer loop.
     */
    public void setNested(boolean state) {
        this.nested = state;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitBreak(this);
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(4);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_BREAK);
        pack.writeString("type").writeInt(this.type.ordinal());
        pack.writeString("nested").writeBool(this.nested);
        pack.writeString("break_id").writeInt(((Object) this).hashCode());
    }

    /**
     * A marker interface for a statement which may be broken.
     */
    public static interface Breakable {

        List<Break> getBreaks();

    }

    /**
     * The type of the break statement.
     */
    public static enum Type {
        BREAK("break"),
        CONTINUE("continue");

        private final String keyword;

        Type(String keyword) {
            this.keyword = keyword;
        }

        /**
         * Gets the java keyword for this type.
         */
        public String getKeyword() {
            return this.keyword;
        }
    }

}
