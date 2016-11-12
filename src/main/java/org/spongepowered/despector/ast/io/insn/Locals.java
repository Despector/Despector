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
package org.spongepowered.despector.ast.io.insn;

import org.spongepowered.despector.ast.members.insn.arg.Instruction;

import java.util.Arrays;

/**
 * A tracker of local variables.
 */
public class Locals {

    private Local[] locals;

    public Locals() {
        this.locals = new Local[0];
    }

    /**
     * Creates a new {@link Locals} instance using the same locals as the given
     * object.
     */
    public Locals(Locals other) {
        this.locals = new Local[other.locals.length];
        for (int i = 0; i < other.locals.length; i++) {
            this.locals[i] = other.locals[i];
        }
    }

    public int getLocalCount() {
        return this.locals.length;
    }

    /**
     * Sets the given local index to the instruction.
     */
    public void set(int i, Instruction arg) {
        if (i >= this.locals.length) {
            int old = this.locals.length;
            this.locals = Arrays.copyOf(this.locals, i + 1);
            for (int o = old; o < i + 1; o++) {
                this.locals[o] = new Local(o, null);
            }
        }
        this.locals[i].set(arg);
    }

    public Instruction get(int i) {
        return this.locals[i].get();
    }

    /**
     * Gets the local variable at the given index.
     */
    public Local getLocal(int i) {
        if (i >= this.locals.length) {
            int old = this.locals.length;
            this.locals = Arrays.copyOf(this.locals, i + 1);
            for (int o = old; o < i + 1; o++) {
                this.locals[o] = new Local(o, null);
            }
        }
        return this.locals[i];
    }

    /**
     * A helper object for a specific local.
     */
    public static class Local {

        private final int index;
        private String type;
        private Instruction value;
        private String name;
        private boolean parameter = false;
        private String[] generics = null;

        public Local(int i, String type) {
            this.index = i;
            this.type = type;
            this.name = "local" + i;
        }

        public int getIndex() {
            return this.index;
        }

        public Instruction get() {
            return this.value;
        }

        /**
         * Sets the value of this local variable.
         */
        public void set(Instruction val) {
            this.value = val;
            if (this.name == null) {
                this.name = "local" + this.index;
            }
            if (this.type == null) {
                this.type = val.inferType();
            }
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return this.type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isParameter() {
            return this.parameter;
        }

        public void setAsParameter() {
            this.parameter = true;
        }

        public String[] getGenericTypes() {
            return this.generics;
        }

        public void setGenericTypes(String[] generic_types) {
            this.generics = generic_types;
        }

    }

}
