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
package org.spongepowered.despector.ast.stmt.misc;

import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementVisitor;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A comment statement.
 */
public class Comment implements Statement {

    private final List<String> comment_text = new ArrayList<>();

    public Comment(String text) {
        for (String line : text.split("\n")) {
            this.comment_text.add(line);
        }
    }

    public Comment(List<String> texts) {
        for (String text : texts) {
            for (String line : text.split("\n")) {
                this.comment_text.add(line);
            }
        }
    }

    public List<String> getCommentText() {
        return this.comment_text;
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (visitor instanceof StatementVisitor) {
            ((StatementVisitor) visitor).visitComment(this);
        }
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(2);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_COMMENT);
        pack.writeString("comment").startArray(this.comment_text.size());
        for (String line : this.comment_text) {
            pack.writeString(line);
        }
    }

    @Override
    public String toString() {
        if (this.comment_text.isEmpty()) {
            return "";
        }
        if (this.comment_text.size() == 1) {
            return "// " + this.comment_text.get(0);
        }
        StringBuilder str = new StringBuilder();
        str.append("/*\n");
        for (String line : this.comment_text) {
            str.append(" * ").append(line).append("\n");
        }
        str.append(" */");
        return str.toString();
    }

}
