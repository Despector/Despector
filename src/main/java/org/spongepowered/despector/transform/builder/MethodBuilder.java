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
package org.spongepowered.despector.transform.builder;

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.misc.Return;
import org.spongepowered.despector.ast.stmt.misc.Throw;
import org.spongepowered.despector.ast.type.MethodEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MethodBuilder {

    private Consumer<MethodEntry> callback;
    private String name;
    private String desc;

    private boolean is_static;
    private boolean is_abstract;

    private final List<Statement> statements = new ArrayList<>();
    private Locals locals;
    private int next_local;

    public MethodBuilder() {

        reset();
    }

    MethodBuilder(Consumer<MethodEntry> callback) {
        this();
        this.callback = callback;
    }

    public LocalInstance createLocal(String name, TypeSignature type) {
        Local local = this.locals.getLocal(this.next_local++);
        LocalInstance instance = new LocalInstance(local, name, type, -1, -1);
        local.addInstance(instance);
        return instance;
    }

    public MethodBuilder name(String name) {
        this.name = name;
        return this;
    }

    public MethodBuilder desc(String desc) {
        this.desc = desc;
        return this;
    }

    public MethodBuilder setStatic(boolean state) {
        this.is_static = state;
        return this;
    }

    public MethodBuilder setAbstract(boolean state) {
        this.is_abstract = state;
        return this;
    }

    public MethodBuilder statement(Statement stmt) {
        this.statements.add(stmt);
        return this;
    }

    public MethodBuilder reset() {
        this.name = null;
        this.is_static = false;
        this.is_abstract = false;
        this.statements.clear();
        this.desc = null;
        this.locals = new Locals(true);
        this.next_local = 1;
        return this;
    }

    public MethodEntry build(SourceSet set) {
        MethodEntry mth = new MethodEntry(set);
        mth.setName(this.name);
        mth.setStatic(this.is_static);
        mth.setAbstract(this.is_abstract);
        mth.setDescription(this.desc);
        mth.setLocals(this.locals);

        if (!this.is_abstract) {
            StatementBlock block = new StatementBlock(StatementBlock.Type.METHOD);
            for (Statement stmt : this.statements) {
                block.append(stmt);
            }
            mth.setInstructions(block);
            if (this.statements.isEmpty() || !(this.statements.get(this.statements.size() - 1) instanceof Return)
                    && !(this.statements.get(this.statements.size() - 1) instanceof Throw)) {
                if (this.desc.endsWith("V")) {
                    block.append(new Return());
                } else {
                    throw new IllegalStateException("Last statement must be return or throw");
                }
            }
        }

        if (this.callback != null) {
            this.callback.accept(mth);
        }
        return mth;
    }

}
