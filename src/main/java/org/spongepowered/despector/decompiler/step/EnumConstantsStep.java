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
package org.spongepowered.despector.decompiler.step;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.members.insn.function.New;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.decompiler.DecompilerStep;
import org.spongepowered.despector.util.TypeHelper;

import java.util.Iterator;

public class EnumConstantsStep implements DecompilerStep {

    @Override
    public void process(ClassNode cn, TypeEntry entry) {
        if (entry instanceof EnumEntry) {
            MethodEntry clinit = entry.getStaticMethodSafe("<clinit>");
            if (clinit != null && clinit.getInstructions() != null) {
                Iterator<Statement> initializers = clinit.getInstructions().getStatements().iterator();
                while (initializers.hasNext()) {
                    Statement next = initializers.next();
                    if (!(next instanceof StaticFieldAssignment)) {
                        break;
                    }
                    StaticFieldAssignment assign = (StaticFieldAssignment) next;
                    if (!TypeHelper.descToType(assign.getOwnerType()).equals(entry.getName()) || !(assign.getValue() instanceof New)) {
                        break;
                    }
                    ((EnumEntry) entry).addEnumConstant(assign.getFieldName());
                }
            }
        }
    }

}
