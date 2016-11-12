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

import com.google.common.collect.Sets;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.despector.ast.io.insn.Locals.Local;
import org.spongepowered.despector.ast.members.insn.StatementBlock;
import org.spongepowered.despector.util.TypeHelper;

import java.util.List;
import java.util.Set;

/**
 * A builder for creating the AST from an instruction list.
 */
public final class InstructionTreeBuilder {

    @SuppressWarnings("unchecked")
    public static StatementBlock build(MethodNode asm) {
        if (asm.instructions.size() == 0) {
            return null;
        }
        Locals locals = new Locals();
        Set<String> names = Sets.newHashSet();
        for (LocalVariableNode node : (List<LocalVariableNode>) asm.localVariables) {
            Local local = locals.getLocal(node.index);
            String name = node.name;
            if ("☃".equals(name)) {
                name = "local";
            }
            if (names.contains(name)) {
                String possible_name = name + "1";
                int i = 1;
                while (names.contains(possible_name)) {
                    possible_name = name + (++i);
                }
                name = possible_name;
            }
            local.setName(name);
            names.add(name);
            local.setType(node.desc);
            if (node.signature != null) {
                String[] generics = TypeHelper.getGenericContents(node.signature);
                local.setGenericTypes(generics);
            }
        }
        int offs = ((asm.access & Opcodes.ACC_STATIC) != 0) ? 1 : 0;
        for (int i = 0; i <= TypeHelper.paramCount(asm.desc) - offs; i++) {
            locals.getLocal(i).setAsParameter();
        }
        DecompilerOptions options = new DecompilerOptions();
        return new OpcodeDecompiler(options).decompile(asm.instructions, locals);
    }

    private InstructionTreeBuilder() {
    }

}
