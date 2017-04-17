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
package org.spongepowered.despector.util;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.ir.MethodInsn;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Various utilities for working with AST elements.
 */
public final class AstUtil {

    /**
     * Converts a type opcode to the string equivalent.
     * 
     * <p>eg. {@link Opcodes#T_INT} converts to 'I'.</p>
     */
    public static String opcodeToType(int op) {
        switch (op) {
        case T_BOOLEAN:
            return "Z";
        case T_BYTE:
            return "B";
        case T_CHAR:
            return "C";
        case T_DOUBLE:
            return "D";
        case T_FLOAT:
            return "F";
        case T_INT:
            return "I";
        case T_LONG:
            return "J";
        case T_SHORT:
            return "S";
        default:
        }
        throw new IllegalArgumentException("Unknown primative array type: " + op);
    }

    /**
     * Converts a type opcode to the equivalent store opcode.
     * 
     * <p>eg. {@link Opcodes#T_DOUBLE} converts to {@link Opcodes#DASTORE}.</p>
     */
    public static int opcodeToArrayStore(int op) {
        switch (op) {
        case T_BOOLEAN:
            return BASTORE;
        case T_BYTE:
            return BASTORE;
        case T_CHAR:
            return CASTORE;
        case T_DOUBLE:
            return DASTORE;
        case T_FLOAT:
            return FASTORE;
        case T_INT:
            return IASTORE;
        case T_LONG:
            return LASTORE;
        case T_SHORT:
            return SASTORE;
        default:
        }
        throw new IllegalArgumentException("Unknown primative array type: " + op);
    }

    /**
     * Converts an asm {@link AbstractInsnNode} to a string for debugging.
     * 
     * <p>This uses a static {@link Textifier} which means that all label
     * numbers will be constantly incrementing as the program continues. Call
     * {@link #__resetTextifier()} to reset label indices.</p>
     */
    public static String insnToString(AbstractInsnNode insn) {
        insn.accept(mp);
        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();
        String s = sw.toString();
        if (s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * Resets the {@link Textifier} instance used by
     * {@link #insnToString(AbstractInsnNode)} to reset the label names.
     */
    public static void __resetTextifier() {
        printer = new Textifier();
    }

    private static Printer printer = new Textifier();
    private static TraceMethodVisitor mp = new TraceMethodVisitor(printer);

    /**
     * Gets the count of values consumed from the stack by the given opcode.
     */
    public static int getStackRequirementsSize(Insn next) {
        if (next == null) {
            return 0;
        }
        switch (next.getOpcode()) {
        case -1:
            return 0;
        case Insn.ARRAY_STORE:
            return 3;
        case Insn.DUP2:
        case Insn.DUP2_X1:
        case Insn.DUP2_X2:
        case Insn.ARRAY_LOAD:
        case Insn.ADD:
        case Insn.SUB:
        case Insn.MUL:
        case Insn.DIV:
        case Insn.REM:
        case Insn.SHL:
        case Insn.SHR:
        case Insn.USHR:
        case Insn.AND:
        case Insn.OR:
        case Insn.XOR:
        case Insn.CMP:
        case Insn.IF_CMPEQ:
        case Insn.IF_CMPNE:
        case Insn.IF_CMPLT:
        case Insn.IF_CMPGE:
        case Insn.IF_CMPGT:
        case Insn.IF_CMPLE:
        case Insn.PUTFIELD:
            return 2;
        case Insn.DUP:
        case Insn.DUP_X1:
        case Insn.DUP_X2:
        case Insn.SWAP:
        case Insn.NEG:
        case Insn.GETFIELD:
        case Insn.NEWARRAY:
        case Insn.CAST:
        case Insn.INSTANCEOF:
        case Insn.LOCAL_STORE:
        case Insn.POP:
        case Insn.IFEQ:
        case Insn.IFNE:
        case Insn.ARETURN:
        case Insn.PUTSTATIC:
        case Insn.THROW:
        case Insn.SWITCH:
            return 1;
        case Insn.PUSH:
        case Insn.ICONST:
        case Insn.LCONST:
        case Insn.FCONST:
        case Insn.DCONST:
        case Insn.LOCAL_LOAD:
        case Insn.GETSTATIC:
        case Insn.NEW:
        case Insn.NOOP:
        case Insn.IINC:
        case Insn.GOTO:
        case Insn.RETURN:
        case Insn.INVOKEDYNAMIC:
            return 0;
        case Insn.INVOKE: {
            MethodInsn method = (MethodInsn) next;
            int count = TypeHelper.paramCount(method.getDescription());
            // the object ref
            count++;
            return count;
        }
        case Insn.INVOKESTATIC: {
            MethodInsn method = (MethodInsn) next;
            int count = TypeHelper.paramCount(method.getDescription());
            return count;
        }
        default:
            System.err.println("Unsupported opcode: " + next.getOpcode());
            throw new IllegalStateException();
        }
    }

    /**
     * Gets the count of values pushed to the stack by the given opcode.
     */
    public static int getStackResultSize(Insn next) {
        if (next == null) {
            return 0;
        }
        switch (next.getOpcode()) {
        case -1:
            return 0;
        case Insn.DUP2:
        case Insn.DUP2_X1:
        case Insn.DUP2_X2:
            return 3;
        case Insn.DUP:
        case Insn.DUP_X1:
        case Insn.DUP_X2:
            return 2;
        case Insn.PUSH:
        case Insn.ICONST:
        case Insn.LCONST:
        case Insn.FCONST:
        case Insn.DCONST:
        case Insn.LOCAL_LOAD:
        case Insn.GETSTATIC:
        case Insn.NEW:
        case Insn.SWAP:
        case Insn.NEG:
        case Insn.GETFIELD:
        case Insn.NEWARRAY:
        case Insn.CAST:
        case Insn.INSTANCEOF:
        case Insn.ARRAY_LOAD:
        case Insn.ADD:
        case Insn.SUB:
        case Insn.MUL:
        case Insn.DIV:
        case Insn.REM:
        case Insn.SHL:
        case Insn.SHR:
        case Insn.USHR:
        case Insn.AND:
        case Insn.OR:
        case Insn.XOR:
        case Insn.CMP:
        case Insn.INVOKEDYNAMIC:
            return 1;
        case Insn.NOOP:
        case Insn.IINC:
        case Insn.GOTO:
        case Insn.RETURN:
        case Insn.LOCAL_STORE:
        case Insn.POP:
        case Insn.IFEQ:
        case Insn.IFNE:
        case Insn.ARETURN:
        case Insn.PUTSTATIC:
        case Insn.THROW:
        case Insn.SWITCH:
        case Insn.IF_CMPEQ:
        case Insn.IF_CMPNE:
        case Insn.IF_CMPLT:
        case Insn.IF_CMPGE:
        case Insn.IF_CMPGT:
        case Insn.IF_CMPLE:
        case Insn.PUTFIELD:
        case Insn.ARRAY_STORE:
            return 0;
        case Insn.INVOKE:
        case Insn.INVOKESTATIC: {
            MethodInsn method = (MethodInsn) next;
            if (!TypeHelper.getRet(method.getDescription()).equals("V")) {
                return 1;
            }
            return 0;
        }
        default:
            System.err.println("Unsupported opcode: " + next.getOpcode());
            throw new IllegalStateException();
        }
    }

    /**
     * Gets the change in stack size from the given opcode.
     */
    public static int getStackDelta(Insn next) {
        return getStackResultSize(next) - getStackRequirementsSize(next);
    }

    /**
     * Gets the first non-meta opcode in the given list of opcodes.
     */
    public static AbstractInsnNode getFirstOpcode(List<AbstractInsnNode> opcodes) {
        for (int index = 0; index < opcodes.size(); index++) {
            AbstractInsnNode next = opcodes.get(index);
            if (next instanceof LabelNode) {
                continue;
            } else if (next instanceof FrameNode) {
                continue;
            } else if (next instanceof LineNumberNode) {
                continue;
            }
            return next;
        }
        return null;
    }

    /**
     * Gets if the given list of opcodes requires values on the stack from
     * before it starts.
     */
    public static boolean hasStartingRequirement(List<Insn> opcodes) {
        int size = 0;
        for (int i = 0; i < opcodes.size(); i++) {
            Insn next = opcodes.get(i);
            size += getStackDelta(next);
            if (size < 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index of the opcode that is the start of the last statement
     * in the given list of opcodes.
     */
    public static int findStartLastStatement(List<Insn> opcodes) {
        int required_stack = getStackDelta(opcodes.get(opcodes.size() - 1));
        for (int index = opcodes.size() - 2; index >= 0; index--) {
            if (required_stack == 0) {
                return index + 1;
            }
            Insn next = opcodes.get(index);
            required_stack += getStackDelta(next);
        }
        return 0;
    }

    /**
     * Gets if a given list of opcodes contains purely supporting types and no
     * actual opcodes. (eg. if the list contains only nodes that are one of
     * {@link FrameNode}, {@link LabelNode}, or {@link LineNumberNode}).
     */
    public static boolean isEmptyOfLogic(List<AbstractInsnNode> opcodes) {
        for (int i = 0; i < opcodes.size(); i++) {
            AbstractInsnNode next = opcodes.get(i);
            if (next instanceof FrameNode || next instanceof LabelNode || next instanceof LineNumberNode) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Gets if the given list of opcodes is only metadata nodes.
     */
    public static boolean isEmptyOfLogic(List<AbstractInsnNode> opcodes, int size) {
        for (int i = 0; i < size; i++) {
            AbstractInsnNode next = opcodes.get(i);
            if (next instanceof FrameNode || next instanceof LabelNode || next instanceof LineNumberNode) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Gets if the given statement references the given local.
     */
    public static boolean references(Statement insn, LocalInstance local) {
        LocalFinder visitor = new LocalFinder(local);
        insn.accept(visitor);
        return visitor.isFound();
    }

    /**
     * Gets if the given instruction references the given local.
     */
    public static boolean references(Instruction insn, LocalInstance local) {
        LocalFinder visitor = new LocalFinder(local);
        insn.accept(visitor);
        return visitor.isFound();
    }

    /**
     * Gets if the given condition references the given local.
     */
    public static boolean references(Condition condition, LocalInstance local) {
        LocalFinder visitor = new LocalFinder(local);
        condition.accept(visitor);
        return visitor.isFound();
    }

    /**
     * A visitor that looks for references to a given local.
     */
    private static class LocalFinder extends InstructionVisitor {

        private final LocalInstance local;
        private boolean found = false;

        public LocalFinder(LocalInstance l) {
            this.local = l;
        }

        public boolean isFound() {
            return this.found;
        }

        @Override
        public void visitLocalInstance(LocalInstance local) {
            if (this.local == local || (this.local == null && local.getIndex() > 0)) {
                this.found = true;
            }
        }

    }

    private AstUtil() {
    }
}
