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

import static com.google.common.base.Preconditions.checkNotNull;

import com.voxelgenesis.despector.core.ast.method.Local;
import com.voxelgenesis.despector.core.ast.method.insn.Instruction;

/**
 * An assignment statement for assigning a value to a local.
 */
public class LocalAssignment extends Assignment {

    private Local local;

    public LocalAssignment(Local local, Instruction val) {
        super(val);
        this.local = checkNotNull(local, "local");
    }

    /**
     * Gets the local to which the value is being assigned.
     */
    public Local getLocal() {
        return this.local;
    }

    /**
     * Sets the local to which the value is being assigned.
     */
    public void setLocal(Local local) {
        this.local = checkNotNull(local, "local");
    }

    @Override
    public String toString() {
        return this.local + " = " + this.val + ";";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LocalAssignment)) {
            return false;
        }
        LocalAssignment insn = (LocalAssignment) obj;
        return this.val.equals(insn.val) && this.local.equals(insn.local);
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.val.hashCode();
        h = h * 37 + this.local.hashCode();
        return h;
    }

}
