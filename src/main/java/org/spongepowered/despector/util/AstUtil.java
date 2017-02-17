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

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.spongepowered.despector.ast.members.insn.branch.condition.AndCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.ast.members.insn.branch.condition.InverseCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.OrCondition;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Various utilities for working with AST elements.
 */
public final class AstUtil {

    public static Condition inverse(Condition condition) {
        if (condition instanceof BooleanCondition) {
            BooleanCondition b = (BooleanCondition) condition;
            b.setInverse(!b.isInverse());
        } else if (condition instanceof AndCondition) {
            AndCondition and = (AndCondition) condition;
            List<Condition> args = new ArrayList<>();
            for (Condition arg : and.getOperands()) {
                args.add(inverse(arg));
            }
            return new OrCondition(args);
        } else if (condition instanceof OrCondition) {
            OrCondition and = (OrCondition) condition;
            List<Condition> args = new ArrayList<>();
            for (Condition arg : and.getOperands()) {
                args.add(inverse(arg));
            }
            return new AndCondition(args);
        } else if (condition instanceof CompareCondition) {
            CompareCondition cmp = (CompareCondition) condition;
            return new CompareCondition(cmp.getLeft(), cmp.getRight(), cmp.getOp().inverse());
        } else {
            return new InverseCondition(condition);
        }
        return condition;
    }

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

    public static int opcodeToStore(int op) {
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

    private static Printer printer = new Textifier();
    private static TraceMethodVisitor mp = new TraceMethodVisitor(printer);

    public static int getStackDelta(AbstractInsnNode next) {
        switch (next.getOpcode()) {
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
        case IALOAD:
        case LALOAD:
        case FALOAD:
        case DALOAD:
        case AALOAD:
        case BALOAD:
        case CALOAD:
        case SALOAD:
        case DUP:
        case DUP_X1:
        case DUP_X2:
        case GETSTATIC:
        case NEW:
            return 1;
        case NOP:
        case SWAP:
        case INEG:
        case LNEG:
        case FNEG:
        case DNEG:
        case IINC:
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
        case GOTO:
        case JSR:
        case RET:
        case RETURN:
        case GETFIELD:
        case NEWARRAY:
        case ANEWARRAY:
        case ARRAYLENGTH:
        case CHECKCAST:
        case INSTANCEOF:
            return 0;
        case ISTORE:
        case LSTORE:
        case FSTORE:
        case DSTORE:
        case ASTORE:
        case POP:
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
            return -1;
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
            return -2;
        case IASTORE:
        case LASTORE:
        case FASTORE:
        case DASTORE:
        case AASTORE:
        case BASTORE:
        case CASTORE:
        case SASTORE:
            return -3;
        case INVOKESPECIAL:
        case INVOKEVIRTUAL:
        case INVOKEINTERFACE: {
            MethodInsnNode method = (MethodInsnNode) next;
            int count = TypeHelper.paramCount(method.desc);
            count++; // the object ref
            if (!TypeHelper.getRet(method.desc).equals("V")) {
                count--;
            }
            return count;
        }
        case INVOKESTATIC: {
            MethodInsnNode method = (MethodInsnNode) next;
            int count = TypeHelper.paramCount(method.desc);
            if (!TypeHelper.getRet(method.desc).equals("V")) {
                count--;
            }
            return count;
        }
        case INVOKEDYNAMIC:
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

    public static int findStartLastStatement(List<AbstractInsnNode> opcodes) {
        return findStartLastStatement(opcodes, opcodes.size() - 1, opcodes.get(opcodes.size() - 1));
    }

    public static int findStartLastStatement(List<AbstractInsnNode> opcodes, AbstractInsnNode last) {
        return findStartLastStatement(opcodes, opcodes.size(), last);
    }

    private static int findStartLastStatement(List<AbstractInsnNode> opcodes, int size, AbstractInsnNode last) {
        int required_stack = getStackDelta(last);
        for (int index = size - 1; index >= 0; index--) {
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
        if (required_stack == 0) {
            return 0;
        }
        throw new IllegalStateException();
    }

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

    private AstUtil() {
    }
}
