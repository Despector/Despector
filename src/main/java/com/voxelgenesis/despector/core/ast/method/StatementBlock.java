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
package com.voxelgenesis.despector.core.ast.method;

import com.voxelgenesis.despector.core.ast.method.stmt.Statement;
import com.voxelgenesis.despector.core.ast.visitor.AstVisitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StatementBlock implements Iterable<Statement> {

    private final List<Statement> statements;

    public StatementBlock() {
        this.statements = new ArrayList<>();
    }

    public int size() {
        return this.statements.size();
    }

    public Statement get(int i) {
        return this.statements.get(i);
    }

    public void append(Statement stmt) {
        this.statements.add(stmt);
    }

    public List<Statement> getStatements() {
        return this.statements;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Statement stmt : this.statements) {
            str.append(stmt.toString()).append("\n");
        }
        return str.toString();
    }

    @Override
    public Iterator<Statement> iterator() {
        return new Itr();
    }

    public void accept(AstVisitor visitor) {
        for (Statement stmt : this.statements) {
            stmt.accept(visitor);
        }
    }

    private class Itr implements Iterator<Statement> {

        private int index;

        public Itr() {

        }

        @Override
        public boolean hasNext() {
            return StatementBlock.this.size() > this.index;
        }

        @Override
        public Statement next() {
            return StatementBlock.this.get(this.index++);
        }
    }

}
