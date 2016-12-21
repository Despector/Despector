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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.despector.ast.io.insn.Locals.Local;
import org.spongepowered.despector.ast.io.insn.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.util.TypeHelper;

import java.util.List;

/**
 * A builder for creating the AST from an instruction list.
 */
public final class InstructionTreeBuilder {

    @SuppressWarnings("unchecked")
    public static StatementBlock build(MethodEntry entry, MethodNode asm) {
        if (asm.instructions.size() == 0) {
            return null;
        }
        Locals locals = new Locals();
        for (LocalVariableNode node : (List<LocalVariableNode>) asm.localVariables) {
            Local local = locals.getLocal(node.index);
            local.addLVT(node);
        }
        int offs = ((asm.access & Opcodes.ACC_STATIC) != 0) ? 0 : 1;
        List<String> param_types = TypeHelper.splitSig(asm.desc);
        for (int i = 0; i < param_types.size() + offs; i++) {
            Local local = locals.getLocal(i);
            local.setAsParameter();
            if (local.getLVT().isEmpty()) {
                if (i < offs) {
                    local.setParameterInstance(new LocalInstance(local, "this", entry.getOwner(), -1, -1));
                } else {
                    local.setParameterInstance(new LocalInstance(local, "param" + i, param_types.get(i - offs), -1, -1));
                }
            } else {
                LocalVariableNode lvt = local.getLVT().get(0);
                local.setParameterInstance(new LocalInstance(local, lvt.name, lvt.desc, -1, -1));
            }
        }
        DecompilerOptions options = new DecompilerOptions();
        return OpcodeDecompiler.decompile(asm.instructions, locals, asm.tryCatchBlocks, options);
    }

    @SuppressWarnings("unchecked")
    public static StatementBlock build(MethodNode asm) {
        if (asm.instructions.size() == 0) {
            return null;
        }
        Locals locals = new Locals();
        for (LocalVariableNode node : (List<LocalVariableNode>) asm.localVariables) {
            Local local = locals.getLocal(node.index);
            local.addLVT(node);
        }
        int offs = ((asm.access & Opcodes.ACC_STATIC) != 0) ? 0 : 1;
        List<String> param_types = TypeHelper.splitSig(asm.desc);
        for (int i = 0; i < param_types.size() + offs; i++) {
            Local local = locals.getLocal(i);
            local.setAsParameter();
            if (local.getLVT().isEmpty()) {
                if (i < offs) {
                    local.setParameterInstance(new LocalInstance(local, "this", "Ljava/lang/Object;", -1, -1));
                } else {
                    local.setParameterInstance(new LocalInstance(local, "param" + i, param_types.get(i - offs), -1, -1));
                }
            } else {
                LocalVariableNode lvt = local.getLVT().get(0);
                local.setParameterInstance(new LocalInstance(local, lvt.name, lvt.desc, -1, -1));
            }
        }
        DecompilerOptions options = new DecompilerOptions();
        return OpcodeDecompiler.decompile(asm.instructions, locals, asm.tryCatchBlocks, options);
    }

    private InstructionTreeBuilder() {
    }

}
