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
package org.spongepowered.despector.util;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.ast.members.insn.Statement;
import org.spongepowered.despector.ast.members.insn.arg.Instruction;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;

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
     * {@link #_resetTextifier()} to reset label indices.</p>
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
    public static void _resetTextifier() {
        printer = new Textifier();
    }

    private static Printer printer = new Textifier();
    private static TraceMethodVisitor mp = new TraceMethodVisitor(printer);

    /**
     * Gets the change in the stack size caused by the given
     * {@link AbstractInsnNode}.
     */
    public static int getStackRequirementsSize(AbstractInsnNode next) {
        if (next == null) {
            return 0;
        }
        switch (next.getOpcode()) {
        case -1:
            return 0;
        case IASTORE:
        case LASTORE:
        case FASTORE:
        case DASTORE:
        case AASTORE:
        case BASTORE:
        case CASTORE:
        case SASTORE:
            return 3;
        case DUP2:
        case DUP2_X1:
        case DUP2_X2:
        case IALOAD:
        case LALOAD:
        case FALOAD:
        case DALOAD:
        case AALOAD:
        case BALOAD:
        case CALOAD:
        case SALOAD:
        case IADD:
        case LADD:
        case FADD:
        case DADD:
        case ISUB:
        case LSUB:
        case FSUB:
        case DSUB:
        case IMUL:
        case LMUL:
        case FMUL:
        case DMUL:
        case IDIV:
        case LDIV:
        case FDIV:
        case DDIV:
        case IREM:
        case LREM:
        case FREM:
        case DREM:
        case ISHL:
        case LSHL:
        case ISHR:
        case LSHR:
        case IUSHR:
        case LUSHR:
        case IAND:
        case LAND:
        case IOR:
        case LOR:
        case IXOR:
        case LXOR:
        case LCMP:
        case FCMPL:
        case FCMPG:
        case DCMPL:
        case DCMPG:
        case POP2:
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ACMPEQ:
        case IF_ACMPNE:
        case PUTFIELD:
            return 2;
        case DUP:
        case DUP_X1:
        case DUP_X2:
        case SWAP:
        case INEG:
        case LNEG:
        case FNEG:
        case DNEG:
        case L2I:
        case F2I:
        case D2I:
        case I2L:
        case F2L:
        case D2L:
        case I2F:
        case L2F:
        case D2F:
        case I2D:
        case F2D:
        case L2D:
        case I2B:
        case I2C:
        case I2S:
        case GETFIELD:
        case NEWARRAY:
        case ANEWARRAY:
        case ARRAYLENGTH:
        case CHECKCAST:
        case INSTANCEOF:
        case ISTORE:
        case LSTORE:
        case FSTORE:
        case DSTORE:
        case ASTORE:
        case POP:
        case IFEQ:
        case IFNE:
        case IFLT:
        case IFGE:
        case IFGT:
        case IFLE:
        case IFNULL:
        case IFNONNULL:
        case IRETURN:
        case LRETURN:
        case FRETURN:
        case DRETURN:
        case ARETURN:
        case PUTSTATIC:
        case ATHROW:
        case TABLESWITCH:
        case LOOKUPSWITCH:
            return 1;
        case ACONST_NULL:
        case ICONST_M1:
        case ICONST_0:
        case ICONST_1:
        case ICONST_2:
        case ICONST_3:
        case ICONST_4:
        case ICONST_5:
        case LCONST_0:
        case LCONST_1:
        case FCONST_0:
        case FCONST_1:
        case FCONST_2:
        case DCONST_0:
        case DCONST_1:
        case BIPUSH:
        case SIPUSH:
        case LDC:
        case ILOAD:
        case LLOAD:
        case FLOAD:
        case DLOAD:
        case ALOAD:
        case GETSTATIC:
        case NEW:
        case NOP:
        case IINC:
        case GOTO:
        case JSR:
        case RET:
        case RETURN:
        case INVOKEDYNAMIC:
            return 0;
        case INVOKESPECIAL:
        case INVOKEVIRTUAL:
        case INVOKEINTERFACE: {
            MethodInsnNode method = (MethodInsnNode) next;
            int count = TypeHelper.paramCount(method.desc);
            count++; // the object ref
            return count;
        }
        case INVOKESTATIC: {
            MethodInsnNode method = (MethodInsnNode) next;
            int count = TypeHelper.paramCount(method.desc);
            return count;
        }
        case MONITORENTER:
        case MONITOREXIT:
        case MULTIANEWARRAY:
            // TODO
            throw new IllegalArgumentException();
        default:
            System.err.println("Unsupported opcode: " + next.getOpcode());
            throw new IllegalStateException();
        }
    }

    public static int getStackResultSize(AbstractInsnNode next) {
        if (next == null) {
            return 0;
        }
        switch (next.getOpcode()) {
        case -1:
            return 0;
        case DUP2:
        case DUP2_X1:
        case DUP2_X2:
            return 2;
        case ACONST_NULL:
        case ICONST_M1:
        case ICONST_0:
        case ICONST_1:
        case ICONST_2:
        case ICONST_3:
        case ICONST_4:
        case ICONST_5:
        case LCONST_0:
        case LCONST_1:
        case FCONST_0:
        case FCONST_1:
        case FCONST_2:
        case DCONST_0:
        case DCONST_1:
        case BIPUSH:
        case SIPUSH:
        case LDC:
        case ILOAD:
        case LLOAD:
        case FLOAD:
        case DLOAD:
        case ALOAD:
        case DUP:
        case DUP_X1:
        case DUP_X2:
        case GETSTATIC:
        case NEW:
        case SWAP:
        case INEG:
        case LNEG:
        case FNEG:
        case DNEG:
        case L2I:
        case F2I:
        case D2I:
        case I2L:
        case F2L:
        case D2L:
        case I2F:
        case L2F:
        case D2F:
        case I2D:
        case F2D:
        case L2D:
        case I2B:
        case I2C:
        case I2S:
        case GETFIELD:
        case NEWARRAY:
        case ANEWARRAY:
        case ARRAYLENGTH:
        case CHECKCAST:
        case INSTANCEOF:
        case IALOAD:
        case LALOAD:
        case FALOAD:
        case DALOAD:
        case AALOAD:
        case BALOAD:
        case CALOAD:
        case SALOAD:
        case IADD:
        case LADD:
        case FADD:
        case DADD:
        case ISUB:
        case LSUB:
        case FSUB:
        case DSUB:
        case IMUL:
        case LMUL:
        case FMUL:
        case DMUL:
        case IDIV:
        case LDIV:
        case FDIV:
        case DDIV:
        case IREM:
        case LREM:
        case FREM:
        case DREM:
        case ISHL:
        case LSHL:
        case ISHR:
        case LSHR:
        case IUSHR:
        case LUSHR:
        case IAND:
        case LAND:
        case IOR:
        case LOR:
        case IXOR:
        case LXOR:
        case LCMP:
        case FCMPL:
        case FCMPG:
        case DCMPL:
        case DCMPG:
        case INVOKEDYNAMIC:
            return 1;
        case NOP:
        case IINC:
        case GOTO:
        case JSR:
        case RET:
        case RETURN:
        case ISTORE:
        case LSTORE:
        case FSTORE:
        case DSTORE:
        case ASTORE:
        case POP:
        case IFEQ:
        case IFNE:
        case IFLT:
        case IFGE:
        case IFGT:
        case IFLE:
        case IFNULL:
        case IFNONNULL:
        case IRETURN:
        case LRETURN:
        case FRETURN:
        case DRETURN:
        case ARETURN:
        case PUTSTATIC:
        case ATHROW:
        case TABLESWITCH:
        case LOOKUPSWITCH:
        case POP2:
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ACMPEQ:
        case IF_ACMPNE:
        case PUTFIELD:
        case IASTORE:
        case LASTORE:
        case FASTORE:
        case DASTORE:
        case AASTORE:
        case BASTORE:
        case CASTORE:
        case SASTORE:
            return 0;
        case INVOKESPECIAL:
        case INVOKEVIRTUAL:
        case INVOKESTATIC:
        case INVOKEINTERFACE: {
            MethodInsnNode method = (MethodInsnNode) next;
            if (!TypeHelper.getRet(method.desc).equals("V")) {
                return 1;
            }
            return 0;
        }
        case MONITORENTER:
        case MONITOREXIT:
        case MULTIANEWARRAY:
            // TODO
            throw new IllegalArgumentException();
        default:
            System.err.println("Unsupported opcode: " + next.getOpcode());
            throw new IllegalStateException();
        }
    }

    public static int getStackDelta(AbstractInsnNode next) {
        return getStackResultSize(next) - getStackRequirementsSize(next);
    }

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

    public static boolean hasStartingRequirement(List<AbstractInsnNode> opcodes) {
        return getStackRequirementsSize(getFirstOpcode(opcodes)) > 0;
    }

    /**
     * Returns the index of the opcode that is the start of the last statement
     * in the given list of opcodes.
     */
    public static int findStartLastStatement(List<AbstractInsnNode> opcodes) {
        int required_stack = getStackDelta(opcodes.get(opcodes.size() - 1));
        for (int index = opcodes.size() - 2; index >= 0; index--) {
            if (required_stack == 0) {
                return index + 1;
            }
            AbstractInsnNode next = opcodes.get(index);
            if (next instanceof LabelNode) {
                continue;
            } else if (next instanceof FrameNode) {
                continue;
            } else if (next instanceof LineNumberNode) {
                continue;
            }
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

    public static boolean references(Statement insn, LocalInstance local) {
        LocalFinder visitor = new LocalFinder(local);
        insn.accept(visitor);
        return visitor.isFound();
    }

    public static boolean references(Instruction insn, LocalInstance local) {
        LocalFinder visitor = new LocalFinder(local);
        insn.accept(visitor);
        return visitor.isFound();
    }

    public static boolean references(Condition condition, LocalInstance local) {
        LocalFinder visitor = new LocalFinder(local);
        condition.accept(visitor);
        return visitor.isFound();
    }

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
