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
package org.spongepowered.despector.source.java;

import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.source.ast.SourceFile;
import org.spongepowered.despector.source.ast.SourceFileSet;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParseState {

    private final SourceFile file;
    private final SourceFileSet set;

    private TypeEntry current_type;
    private MethodEntry current_method;
    private Locals current_locals;

    private final Map<FieldEntry, Instruction> initializers = new LinkedHashMap<>();

    public ParseState(SourceFileSet set, SourceFile src) {
        this.file = src;
        this.set = set;
    }

    public SourceFile getSource() {
        return this.file;
    }

    public SourceFileSet getSourceSet() {
        return this.set;
    }

    public TypeEntry getCurrentType() {
        return this.current_type;
    }

    public void setCurrentType(TypeEntry type) {
        this.current_type = type;
    }

    public MethodEntry getCurrentMethod() {
        return this.current_method;
    }

    public void setCurrentMethod(MethodEntry mth) {
        this.current_method = mth;
    }

    public Locals getCurrentLocals() {
        return this.current_locals;
    }

    public void setCurrentLocals(Locals l) {
        this.current_locals = l;
    }

    public void setFieldInitializer(FieldEntry fld, Instruction insn) {
        this.initializers.put(fld, insn);
    }

    public Instruction getFieldInitializer(FieldEntry fld) {
        return this.initializers.get(fld);
    }

    public Map<FieldEntry, Instruction> getFieldInitializers() {
        return this.initializers;
    }

}
