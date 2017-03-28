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
package org.spongepowered.despector.transform.matcher;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.arg.Cast;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.arg.cst.IntConstant;
import org.spongepowered.despector.ast.members.insn.arg.field.ArrayAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.InstanceFieldAccess;
import org.spongepowered.despector.ast.members.insn.arg.field.LocalAccess;
import org.spongepowered.despector.ast.members.insn.function.InstanceMethodInvoke;

public class InstructionMatcher {

    public static boolean isInstanceMethodInvoke(Instruction insn, String method_name, String method_desc) {
        return isInstanceMethodInvoke(insn, method_name, method_desc, null);
    }

    public static boolean isInstanceMethodInvoke(Instruction insn, String method_name, String method_desc, String method_owner) {
        if (!(insn instanceof InstanceMethodInvoke)) {
            return false;
        }
        InstanceMethodInvoke invoke = (InstanceMethodInvoke) insn;
        if (!invoke.getMethodName().equals(method_name)) {
            return false;
        }
        if (method_owner != null && !invoke.getOwner().equals(method_owner)) {
            return false;
        }
        return invoke.getMethodDescription().equals(method_desc);
    }

    public static InstanceMethodInvoke requireInstanceMethodInvoke(Instruction insn, String method_name, String method_desc) {
        if (!(insn instanceof InstanceMethodInvoke)) {
            return null;
        }
        InstanceMethodInvoke invoke = (InstanceMethodInvoke) insn;
        if (method_name != null && !invoke.getMethodName().equals(method_name)) {
            return null;
        }
        if (method_desc != null && !invoke.getMethodDescription().equals(method_desc)) {
            return null;
        }
        return invoke;
    }

    public static boolean isLocalAccess(Instruction callee, LocalInstance local) {
        if (!(callee instanceof LocalAccess)) {
            return false;
        }
        if (!((LocalAccess) callee).getLocal().equals(local)) {
            return false;
        }
        return true;
    }

    public static Instruction unwrapCast(Instruction insn) {
        if (insn instanceof Cast) {
            return unwrapCast(((Cast) insn).getValue());
        }
        if (insn instanceof InstanceMethodInvoke) {
            InstanceMethodInvoke invoke = (InstanceMethodInvoke) insn;
            if (isInstanceMethodInvoke(invoke, "byteValue", "()B", "Ljava/lang/Number;")) {
                return unwrapCast(invoke.getCallee());
            }
            if (isInstanceMethodInvoke(invoke, "shortValue", "()S", "Ljava/lang/Number;")) {
                return unwrapCast(invoke.getCallee());
            }
            if (isInstanceMethodInvoke(invoke, "intValue", "()I", "Ljava/lang/Number;")) {
                return unwrapCast(invoke.getCallee());
            }
            if (isInstanceMethodInvoke(invoke, "longValue", "()J", "Ljava/lang/Number;")) {
                return unwrapCast(invoke.getCallee());
            }
            if (isInstanceMethodInvoke(invoke, "floatValue", "()F", "Ljava/lang/Number;")) {
                return unwrapCast(invoke.getCallee());
            }
            if (isInstanceMethodInvoke(invoke, "doubleValue", "()D", "Ljava/lang/Number;")) {
                return unwrapCast(invoke.getCallee());
            }
        }
        return insn;
    }

    public static boolean isIntConstant(Instruction insn, int i) {
        if (!(insn instanceof IntConstant)) {
            return false;
        }
        return ((IntConstant) insn).getConstant() == i;
    }

    public static boolean isInstanceFieldAccess(Instruction insn, String field, Instruction obj) {
        if (!(insn instanceof InstanceFieldAccess)) {
            return false;
        }
        InstanceFieldAccess acc = (InstanceFieldAccess) insn;
        if (!acc.getFieldName().equals(field)) {
            return false;
        }
        return acc.getFieldOwner().equals(obj);
    }

    public static boolean isArrayAccess(Instruction value, LocalAccess array, LocalAccess index) {
        if (!(value instanceof ArrayAccess)) {
            return false;
        }
        ArrayAccess acc = (ArrayAccess) value;
        if (!acc.getArrayVar().equals(array)) {
            return false;
        }
        if (!acc.getIndex().equals(index)) {
            return false;
        }
        return true;
    }

}
