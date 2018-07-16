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
package org.spongepowered.despector.parallel;

import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.generic.MethodSignature;
import org.spongepowered.despector.ast.insn.cst.StringConstant;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.stmt.invoke.New;
import org.spongepowered.despector.ast.stmt.misc.Comment;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.config.LibraryConfiguration;
import org.spongepowered.despector.decompiler.BaseDecompiler;
import org.spongepowered.despector.decompiler.Decompilers;
import org.spongepowered.despector.decompiler.BaseDecompiler.BootstrapMethod;
import org.spongepowered.despector.decompiler.BaseDecompiler.UnfinishedMethod;
import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.loader.BytecodeTranslator;
import org.spongepowered.despector.decompiler.loader.ClassConstantPool;
import org.spongepowered.despector.decompiler.method.MethodDecompiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MethodDecompileTask implements Runnable {

    private final TypeEntry entry;
    private final ClassConstantPool pool;
    private final List<UnfinishedMethod> unfinished_methods;
    private final BytecodeTranslator bytecode;
    private final List<BootstrapMethod> bootstrap_methods;

    public MethodDecompileTask(TypeEntry entry, ClassConstantPool pool, List<UnfinishedMethod> unfinished_methods, BytecodeTranslator bytecode,
            List<BootstrapMethod> bootstrap_methods) {
        this.entry = entry;
        this.pool = pool;
        this.unfinished_methods = unfinished_methods;
        this.bytecode = bytecode;
        this.bootstrap_methods = bootstrap_methods;
    }

    public TypeEntry getEntry() {
        return this.entry;
    }

    @Override
    public void run() {
        for (UnfinishedMethod unfinished : this.unfinished_methods) {
            if (unfinished.code == null) {
                continue;
            }
            LibraryConfiguration.total_method_count++;
            MethodEntry mth = unfinished.mth;
            try {
                mth.setIR(this.bytecode.createIR(mth.getMethodSignature(), unfinished.code, mth.getLocals(), unfinished.catch_regions, this.pool,
                        this.bootstrap_methods));

                if (unfinished.parameter_annotations != null) {
                    for (Map.Entry<Integer, List<Annotation>> e : unfinished.parameter_annotations.entrySet()) {
                        Local loc = mth.getLocals().getLocal(e.getKey());
                        loc.getInstance(0).getAnnotations().addAll(e.getValue());
                    }
                }

                if (BaseDecompiler.DUMP_IR_ON_LOAD) {
                    System.out.println("Instructions of " + mth.getName() + " " + mth.getDescription());
                    System.out.println(mth.getIR());
                }
                MethodDecompiler mth_decomp = Decompilers.JAVA_METHOD;
                if (this.entry.getLanguage() == Language.KOTLIN) {
                    mth_decomp = Decompilers.KOTLIN_METHOD;
                }
                StatementBlock block = mth_decomp.decompile(mth);
                mth.setInstructions(block);

                if (this.entry instanceof EnumEntry && mth.getName().equals("<clinit>")) {
                    EnumEntry e = (EnumEntry) this.entry;
                    Set<String> names = new HashSet<>(e.getEnumConstants());
                    e.getEnumConstants().clear();
                    for (Statement stmt : block) {
                        if (names.isEmpty() || !(stmt instanceof StaticFieldAssignment)) {
                            break;
                        }
                        StaticFieldAssignment assign = (StaticFieldAssignment) stmt;
                        if (!names.remove(assign.getFieldName())) {
                            break;
                        }
                        New val = (New) assign.getValue();
                        StringConstant cst = (StringConstant) val.getParameters()[0];
                        e.addEnumConstant(cst.getConstant());
                    }
                    if (!names.isEmpty()) {
                        System.err.println("Warning: Failed to find names for all enum constants in " + this.entry.getName());
                    }
                }
            } catch (Exception ex) {
                if (!LibraryConfiguration.quiet) {
                    System.err.println("Error decompiling method body for " + this.entry.getName() + " " + mth.toString());
                    ex.printStackTrace();
                }
                LibraryConfiguration.failed_method_count++;
                StatementBlock insns = new StatementBlock(StatementBlock.Type.METHOD);
                if (ConfigManager.getConfig().print_opcodes_on_error) {
                    List<String> text = new ArrayList<>();
                    text.add("Error decompiling block");
                    if (mth.getIR() != null) {
                        for (Insn next : mth.getIR()) {
                            text.add(next.toString());
                        }
                    } else {
                        mth.getLocals().bakeInstances(new MethodSignature(), Collections.emptyList());
                    }
                    insns.append(new Comment(text));
                } else {
                    insns.append(new Comment("Error decompiling block"));
                }
                mth.setInstructions(insns);
            }
        }
    }

}
