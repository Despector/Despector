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

import com.google.common.collect.Lists;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LocalVariableNode;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.util.TypeHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    public void bakeInstances(Map<Label, Integer> label_indices) {
        for (Local local : this.locals) {
            local.bakeInstances(label_indices);
        }
    }

    /**
     * A helper object for a specific local.
     */
    public static class Local {

        private final int index;
        private boolean parameter = false;
        private LocalInstance parameter_instance = null;
        private List<LocalVariableNode> lvt = Lists.newArrayList();
        private List<LocalInstance> instances = Lists.newArrayList();

        public Local(int i, String type) {
            this.index = i;
        }

        public int getIndex() {
            return this.index;
        }

        public boolean isParameter() {
            return this.parameter;
        }

        public void setAsParameter() {
            this.parameter = true;
        }

        public void addLVT(LocalVariableNode node) {
            this.lvt.add(node);
        }

        public List<LocalVariableNode> getLVT() {
            return this.lvt;
        }

        public void bakeInstances(Map<Label, Integer> label_indices) {
            for (LocalVariableNode l : this.lvt) {
                int start = label_indices.get(l.start.getLabel());
                int end = label_indices.get(l.end.getLabel());
                LocalInstance insn = new LocalInstance(this, l.name, l.desc, start, end);
                if (l.signature != null) {
                    String[] generics = TypeHelper.getGenericContents(l.signature);
                    insn.setGenericTypes(generics);
                }
                this.instances.add(insn);
            }
        }

        public LocalInstance getInstance(int index) {
            if (this.parameter_instance != null) {
                return this.parameter_instance;
            }
            for (LocalInstance insn : this.instances) {
                if (index >= insn.getStart() - 1 && index <= insn.getEnd()) {
                    return insn;
                }
            }
            return null;
        }

        public LocalInstance getParameterInstance() {
            return this.parameter_instance;
        }

        public void setParameterInstance(LocalInstance insn) {
            this.parameter_instance = insn;
        }

    }

    public static class DummyLocalInstance extends LocalInstance {

        public DummyLocalInstance(Local l) {
            super(l, null, null, -1, -1);
        }

    }

    public static class LocalInstance {

        private final Local local;
        private String name;
        private String type;
        private Instruction value;
        private int start;
        private int end;
        private String[] generics = null;

        public LocalInstance(Local l, String n, String t, int start, int end) {
            this.local = l;
            this.name = n;
            this.type = t;
            this.start = start;
            this.end = end;
        }

        public Local getLocal() {
            return this.local;
        }

        public int getIndex() {
            return this.local.getIndex();
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

        public String getTypeName() {
            return TypeHelper.descToType(this.type);
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getStart() {
            return this.start;
        }

        public int getEnd() {
            return this.end;
        }

        public String[] getGenericTypes() {
            return this.generics;
        }

        public void setGenericTypes(String[] generic_types) {
            this.generics = generic_types;
        }

    }

}
