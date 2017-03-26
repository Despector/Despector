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
package org.spongepowered.despector.ast;

import com.google.common.collect.Lists;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LocalVariableNode;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.util.SignatureParser;
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
            for (int o = old; o <= i; o++) {
                this.locals[o] = new Local(o);
            }
        }
        return this.locals[i];
    }

    public void bakeInstances(Map<Label, Integer> label_indices) {
        for (Local local : this.locals) {
            local.bakeInstances(label_indices);
        }
    }

    public String getNonConflictingName(String name, int index) {
        int i = 1;
        while (true) {
            boolean conflict = false;
            for (Local local : this.locals) {
                LocalInstance insn = local.getInstance(index);
                if (insn != null && insn.getName().equals(name)) {
                    conflict = true;
                    break;
                }
            }
            if (!conflict) {
                break;
            }
            name = name + (i++);
        }
        return name;
    }

    public LocalInstance findLocal(Label start, String type) {
        for (Local local : this.locals) {
            LocalInstance i = local.find(start, type);
            if (i != null) {
                return i;
            }
        }
        return null;
    }

    /**
     * A helper object for a specific local.
     */
    public static class Local {

        private final int index;
        private boolean parameter = false;
        private LocalInstance parameter_instance = null;
        private final List<LocalVariableNode> lvt = Lists.newArrayList();
        private final List<LocalInstance> instances = Lists.newArrayList();

        public Local(int i) {
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
            if (this.lvt.isEmpty()) {
                if (this.index == 0) {
                    this.instances.add(new LocalInstance(this, null, "this", "", -1, Short.MAX_VALUE));
                }
            }
            for (LocalVariableNode l : this.lvt) {
                int start = label_indices.get(l.start.getLabel());
                int end = label_indices.get(l.end.getLabel());
                LocalInstance insn = new LocalInstance(this, l, l.name, l.desc, start - 1, end);
                if (l.signature != null) {
                    insn.setGenericTypes(SignatureParser.parseFieldTypeSignature(l.signature));
                }
                this.instances.add(insn);
            }
        }

        public LocalInstance getLVTInstance(int index) {
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

        public LocalInstance getInstance(int index) {
            if (this.parameter_instance != null) {
                return this.parameter_instance;
            }
            if (this.instances.isEmpty()) {
                // No LVT entry for this local!
                this.parameter_instance = new LocalInstance(this, null, "param" + this.index, null, -1, -1);
                return this.parameter_instance;
            }
            for (LocalInstance insn : this.instances) {
                if (index >= insn.getStart() - 1 && index <= insn.getEnd()) {
                    return insn;
                }
            }
            return null;
        }

        public void addInstance(LocalInstance insn) {
            if (insn.getStart() == -1 && insn.getEnd() == -1) {
                this.parameter_instance = insn;
            } else {
                this.instances.add(insn);
            }
        }

        public LocalInstance getParameterInstance() {
            return this.parameter_instance;
        }

        public void setParameterInstance(LocalInstance insn) {
            this.parameter_instance = insn;
        }

        public LocalInstance find(Label start, String type) {
            for (LocalVariableNode lvn : this.lvt) {
                if (lvn.start.getLabel() == start && lvn.desc.equals(type)) {
                    for (LocalInstance i : this.instances) {
                        if (lvn == i.getLVN()) {
                            return i;
                        }
                    }
                    throw new IllegalStateException();
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return "Local" + this.index;
        }

    }

    public static class LocalInstance {

        private final Local local;
        private String name;
        private String type;
        private int start;
        private int end;
        private LocalVariableNode lvn;
        private TypeSignature signature;
        private boolean effectively_final = false;

        public LocalInstance(Local l, LocalVariableNode lvn, String n, String t, int start, int end) {
            this.local = l;
            this.name = n;
            this.type = t;
            this.start = start;
            this.end = end;
            this.lvn = lvn;
        }

        public Local getLocal() {
            return this.local;
        }

        public LocalVariableNode getLVN() {
            return this.lvn;
        }

        /**
         * Gets the local index of this instance.
         */
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

        public TypeSignature getSignature() {
            return this.signature;
        }

        public void setGenericTypes(TypeSignature sig) {
            this.signature = sig;
        }

        public boolean isEffectivelyFinal() {
            return this.effectively_final;
        }

        public void setEffectivelyFinal(boolean state) {
            this.effectively_final = state;
        }

        @Override
        public String toString() {
            return this.name != null ? this.name : this.local.toString();
        }

    }

}
