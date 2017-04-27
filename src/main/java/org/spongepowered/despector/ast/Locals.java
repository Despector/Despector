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
package org.spongepowered.despector.ast;

import com.google.common.collect.Lists;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.util.SignatureParser;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A tracker of local variables.
 */
public class Locals {

    private Local[] locals;
    private final boolean is_static;

    public Locals(boolean is_static) {
        this.locals = new Local[0];
        this.is_static = is_static;
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
        this.is_static = other.is_static;
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
                this.locals[o] = new Local(o, this.is_static);
            }
        }
        return this.locals[i];
    }

    /**
     * Bakes the local instances using the given label indices.
     */
    public void bakeInstances(List<Integer> label_indices) {
        for (Local local : this.locals) {
            local.bakeInstances(label_indices);
        }
    }

    /**
     * Gets a name for the variable that does not conflict with any other names.
     */
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

    /**
     * Fines a local from the given start point with the given type.
     */
    public LocalInstance findLocal(int start, String type) {
        for (Local local : this.locals) {
            LocalInstance i = local.find(start, type);
            if (i != null) {
                return i;
            }
        }
        return null;
    }

    public List<LocalInstance> getAllInstances() {
        List<LocalInstance> res = new ArrayList<>();
        for (Local loc : this.locals) {
            res.addAll(loc.getInstances());
            if (loc.getParameterInstance() != null) {
                res.add(loc.getParameterInstance());
            }
        }
        return res;
    }

    /**
     * Writes this locals set to the given {@link MessagePacker}.
     */
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startArray(this.locals.length);
        for (Local loc : this.locals) {
            pack.startMap(2);
            pack.writeString("index").writeInt(loc.getIndex());
            int sz = loc.getInstances().size();
            if (loc.isParameter()) {
                sz++;
            }
            pack.writeString("instances").startArray(sz);
            for (LocalInstance insn : loc.getInstances()) {
                insn.writeTo(pack);
            }
            if (loc.isParameter()) {
                loc.getParameterInstance().writeTo(pack);
            }
            pack.endArray();
            pack.endMap();
        }
        pack.endArray();
    }

    /**
     * A helper object for a specific local.
     */
    public static class Local {

        private final boolean is_static;
        private final int index;
        private LocalInstance parameter_instance = null;
        private final List<LVT> lvt = Lists.newArrayList();
        private final List<LocalInstance> instances = Lists.newArrayList();

        public Local(int i, boolean is_static) {
            this.index = i;
            this.is_static = is_static;
        }

        public int getIndex() {
            return this.index;
        }

        public boolean isParameter() {
            return this.parameter_instance != null;
        }

        public void addLVT(int s, int l, String n, String d) {
            this.lvt.add(new LVT(s, l, n, d));
        }

        public LVT getLVT(int s) {
            for (LVT l : this.lvt) {
                if (l.start_pc == s) {
                    return l;
                }
            }
            throw new IllegalStateException();
        }

        /**
         * Bakes the instances of this local.
         */
        public void bakeInstances(List<Integer> label_indices) {
            for (LVT l : this.lvt) {
                int start = label_indices.indexOf(l.start_pc);
                int end = label_indices.indexOf(l.start_pc + l.length);
                if (end == -1) {
                    end = label_indices.get(label_indices.size() - 1);
                }
                TypeSignature sig = null;
                if (l.signature == null) {
                    sig = ClassTypeSignature.of(l.desc);
                } else {
                    sig = SignatureParser.parseFieldTypeSignature(l.signature);
                }
                LocalInstance insn = new LocalInstance(this, l.name, sig, start - 1, end);

                if (start == 0) {
                    this.parameter_instance = insn;
                } else {
                    this.instances.add(insn);
                }
            }
        }

        /**
         * Gets the local instance for the given index.
         */
        public LocalInstance getInstance(int index) {
            for (LocalInstance insn : this.instances) {
                if (index >= insn.getStart() - 1 && index <= insn.getEnd()) {
                    return insn;
                }
            }
            if (this.parameter_instance != null) {
                return this.parameter_instance;
            }
            // No LVT entry for this local!
            String n = "param" + this.index;
            if (!this.is_static && this.index == 0) {
                n = "this";
            }
            this.parameter_instance = new LocalInstance(this, n, null, -1, -1);
            return this.parameter_instance;
        }

        /**
         * Adds the given instance to this local.
         */
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

        /**
         * Finds an instance for the given label and type.
         */
        public LocalInstance find(int start, String type) {
            if (start == -1 && this.parameter_instance != null) {
                return this.parameter_instance;
            }
            for (LocalInstance local : this.instances) {
                if (local.getStart() == start) {
                    if (local.getType() == null || (local.getType().getDescriptor().equals(type))) {
                        return local;
                    }
                }
            }
            return null;
        }

        public List<LocalInstance> getInstances() {
            return this.instances;
        }

        @Override
        public String toString() {
            return "Local" + this.index;
        }

    }

    /**
     * An instance of a local.
     */
    public static class LocalInstance {

        private final Local local;
        private String name;
        private TypeSignature type;
        private int start;
        private int end;
        private boolean effectively_final = false;

        private final List<Annotation> annotations = new ArrayList<>();

        public LocalInstance(Local l, String n, TypeSignature t, int start, int end) {
            this.local = l;
            this.name = n;
            this.type = t;
            this.start = start;
            this.end = end;
        }

        public Local getLocal() {
            return this.local;
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

        public TypeSignature getType() {
            return this.type;
        }

        public String getTypeName() {
            return this.type.getName();
        }

        public void setType(TypeSignature type) {
            this.type = type;
        }

        public int getStart() {
            return this.start;
        }

        public int getEnd() {
            return this.end;
        }

        public boolean isEffectivelyFinal() {
            return this.effectively_final;
        }

        public void setEffectivelyFinal(boolean state) {
            this.effectively_final = state;
        }

        public List<Annotation> getAnnotations() {
            return this.annotations;
        }

        /**
         * Writes the simple form of this instance.
         */
        public void writeToSimple(MessagePacker pack) throws IOException {
            pack.startMap(3);
            pack.writeString("local").writeInt(this.local.getIndex());
            pack.writeString("start").writeInt(this.start);
            pack.writeString("type");
            if (this.type != null) {
                pack.writeString(this.type.getDescriptor());
            } else {
                pack.writeNil();
            }
            pack.endMap();
        }

        /**
         * Writes the complete form of this instance.
         */
        public void writeTo(MessagePacker pack) throws IOException {
            pack.startMap(6);
            pack.writeString("name").writeString(this.name);
            pack.writeString("type");
            if (this.type != null) {
                this.type.writeTo(pack);
            } else {
                pack.writeNil();
            }
            pack.writeString("start").writeInt(this.start);
            pack.writeString("end").writeInt(this.end);
            pack.writeString("final").writeBool(this.effectively_final);
            pack.writeString("annotations").startArray(this.annotations.size());
            for (Annotation anno : this.annotations) {
                anno.writeTo(pack);
            }
            pack.endArray();
            pack.endMap();
        }

        @Override
        public String toString() {
            return this.name != null ? this.name : this.local.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof LocalInstance)) {
                return false;
            }
            LocalInstance l = (LocalInstance) o;
            if (this.type != null && !this.type.equals(l.type)) {
                return false;
            }
            return this.name.equals(l.name) && this.start == l.start && this.end == l.end;
        }

    }

    public static class LVT {

        public int start_pc;
        public int length;
        public String name;
        public String desc;
        public String signature;

        public LVT(int s, int l, String n, String d) {
            this.start_pc = s;
            this.length = l;
            this.name = n;
            this.desc = d;
        }

        public void setSignature(String sig) {
            this.signature = sig;
        }
    }

}
