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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat.Encoding;

/**
 * Various utilities for working with AST elements.
 */
public final class AstUtil {

    public static Condition inverse(Condition condition) {
        if (condition instanceof BooleanCondition) {
            BooleanCondition b = (BooleanCondition) condition;
            return new BooleanCondition(b.getConditionValue(), !b.isInverse());
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
        }
        return new InverseCondition(condition);
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

    private static boolean isInverse(Condition a, Condition other) {
        if (other instanceof BooleanCondition && a instanceof BooleanCondition) {
            BooleanCondition ab = (BooleanCondition) a;
            BooleanCondition ob = (BooleanCondition) other;
            return ab.getConditionValue().equals(ob.getConditionValue()) && ab.isInverse() != ob.isInverse();
        }
        if (other instanceof CompareCondition && a instanceof CompareCondition) {
            CompareCondition ab = (CompareCondition) a;
            CompareCondition ob = (CompareCondition) other;
            return ab.getLeft().equals(ob.getLeft()) && ab.getRight().equals(ob.getRight()) && ab.getOp() == ob.getOp().inverse();
        }
        return false;
    }

    private static int getMapping(Map<Condition, Integer> mapping, Condition c) {
        if (mapping.containsKey(c)) {
            return mapping.get(c);
        }
        int highest = 1;
        for (Condition key : mapping.keySet()) {
            int kvalue = mapping.get(key);
            if (isInverse(c, key)) {
                mapping.put(c, -kvalue);
                return -kvalue;
            }
            if (c.equals(key)) {
                mapping.put(c, kvalue);
                return kvalue;
            }
            if (kvalue >= highest) {
                highest = kvalue + 1;
            }
        }
        mapping.put(c, highest);
        return highest;
    }

    private static int[] encode(AndCondition and, Map<Condition, Integer> mapping) {
        int[] encoding = new int[and.getOperands().size()];
        int i = 0;
        for (Condition c : and.getOperands()) {
            int m = getMapping(mapping, c);
            encoding[i++] = m;
        }
        return encoding;
    }

    /**
     * Gets is the first param contains the inverse of the second param.
     */
    private static boolean containsInverse(int[] a, int[] b) {
        outer: for (int i = 0; i < b.length; i++) {
            int next = b[i];
            for (int o = 0; o < a.length; o++) {
                if (a[o] == -next) {
                    continue outer;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Gets is the first param contains the second param.
     */
    private static boolean contains(int[] a, int[] b) {
        outer: for (int i = 0; i < b.length; i++) {
            int next = b[i];
            for (int o = 0; o < a.length; o++) {
                if (a[o] == next) {
                    continue outer;
                }
            }
            return false;
        }
        return true;
    }

    private static Pair<int[], int[]> simplifyCommonSubparts(int[] a, int[] b, List<int[]> encodings) {
        if (a.length < b.length) {
            return null;
        }
        int[] common = new int[Math.max(a.length, b.length)];
        int common_length = 0;

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                if (a[i] == b[j]) {
                    common[common_length++] = a[i];
                    break;
                }
            }
        }
        if (common_length == 0) {
            return null;
        }

        int[] a_b = new int[a.length - common_length];
        int o = 0;
        int u = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] == common[o]) {
                o++;
            } else {
                a_b[u++] = a[i];
            }
        }
        int[] b_a = new int[b.length - common_length];
        o = 0;
        u = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] == common[o]) {
                o++;
            } else {
                b_a[u++] = b[i];
            }
        }

        if (containsInverse(a_b, b_a)) {
            if (a_b.length == b_a.length) {
                int[] m = new int[common_length];
                for (int i = 0; i < common_length; i++) {
                    m[i] = common[i];
                }
                return new Pair(m, null);
            }
            int[] new_m = new int[a.length - b_a.length];
            int d = 0;
            outer: for (int j = 0; j < a.length; j++) {
                for (int v = 0; v < b_a.length; v++) {
                    if (a[j] == -b_a[v]) {
                        continue outer;
                    }
                }
                new_m[d++] = a[j];
            }
            return new Pair(new_m, null);
        } else if (a_b.length == b_a.length && a_b.length == 1) {
            int[] new_m = new int[common_length];
            for (int i = 0; i < common_length; i++) {
                new_m[i] = common[i];
            }

            int[] dm = new int[] {-a_b[0], -b_a[0]};

            for (int i = 0; i < encodings.size(); i++) {
                int[] n = encodings.get(i);
                if (n == a || n == b) {
                    continue;
                }
                if (n.length == 2 && contains(n, dm)) {
                    return new Pair(null, new_m);
                }
            }

        }
        return null;
    }

    private static Condition reverse(Map<Condition, Integer> mapping, int val) {
        for (Map.Entry<Condition, Integer> e : mapping.entrySet()) {
            if (e.getValue() == val) {
                return e.getKey();
            }
        }
        return null;
    }

    private static List<Condition> reverseEncoding(List<int[]> encodings, Map<Condition, Integer> mapping) {
        List<Condition> reverse = new ArrayList<>();
        for (int i = 0; i < encodings.size(); i++) {
            List<Condition> partial = new ArrayList<>();
            int[] next = encodings.get(i);
            for (int o = 0; o < next.length; o++) {
                int val = next[o];
                Condition p = reverse(mapping, val);
                if (p == null) {
                    p = inverse(reverse(mapping, -val));
                    if (p == null) {
                        throw new IllegalStateException();
                    }
                }
                partial.add(p);
            }
            if (partial.size() == 1) {
                reverse.add(partial.get(0));
            } else {
                reverse.add(new AndCondition(partial));
            }
        }
        return reverse;
    }

    private static final boolean DEBUG_SIMPLIFICATION = Boolean.valueOf(System.getProperty("despect.debug.simplification", "true"));

    public static Condition simplifyCondition(Condition condition) {
        // A brute force simplification of sum-of-products expressions
        if (condition instanceof OrCondition) {
            OrCondition or = (OrCondition) condition;
            List<int[]> encodings = new ArrayList<>(or.getOperands().size());
            Map<Condition, Integer> mapping = new HashMap<>();
            for (int i = 0; i < or.getOperands().size(); i++) {
                Condition c = or.getOperands().get(i);
                if (c instanceof AndCondition) {
                    encodings.add(encode((AndCondition) c, mapping));
                } else {
                    encodings.add(new int[] {getMapping(mapping, c)});
                }
            }
            if (DEBUG_SIMPLIFICATION) {
                for (Map.Entry<Condition, Integer> e : mapping.entrySet()) {
                    System.out.println(e.getKey() + " : " + e.getValue());
                }
                System.out.print("Exp: ");
                for (int[] e : encodings) {
                    for (int i = 0; i < e.length; i++) {
                        System.out.print(e[i]);
                    }
                    System.out.print(" | ");
                }
                System.out.println();
            }
            for (int j = 0; j < encodings.size(); j++) {
                for (int k = 0; k < encodings.size(); k++) {
                    int[] n = encodings.get(k);
                    for (Iterator<int[]> it = encodings.iterator(); it.hasNext();) {
                        int[] m = it.next();
                        if (m == n || m.length < n.length) {
                            continue;
                        }
                        if (contains(m, n)) {
                            it.remove();
                            if (DEBUG_SIMPLIFICATION) {
                                System.out.println("Removed expression containing other expression");
                                System.out.print("Exp: ");
                                for (int[] e : encodings) {
                                    for (int i = 0; i < e.length; i++) {
                                        System.out.print(e[i]);
                                    }
                                    System.out.print(" | ");
                                }
                                System.out.println();
                            }
                        }
                    }
                    for (int l = 0; l < encodings.size(); l++) {
                        if (l == k) {
                            continue;
                        }
                        int[] m = encodings.get(l);
                        if (m.length < n.length) {
                            continue;
                        }
                        if (containsInverse(m, n)) {
                            int[] new_m = new int[m.length - n.length];
                            int d = 0;
                            outer: for (int u = 0; u < m.length; u++) {
                                for (int v = 0; v < n.length; v++) {
                                    if (m[u] == -n[v]) {
                                        continue outer;
                                    }
                                }
                                new_m[d++] = m[u];
                            }
                            encodings.set(l, new_m);
                            if (DEBUG_SIMPLIFICATION) {
                                System.out.println("Removed inverse of other expression from expression");
                                System.out.print("Exp: ");
                                for (int[] e : encodings) {
                                    for (int i = 0; i < e.length; i++) {
                                        System.out.print(e[i]);
                                    }
                                    System.out.print(" | ");
                                }
                                System.out.println();
                            }
                        } else {
                            Pair<int[], int[]> s = simplifyCommonSubparts(m, n, encodings);
                            if (s != null) {
                                if (s.getFirst() != null) {
                                    encodings.set(l, s.getFirst());
                                    if (DEBUG_SIMPLIFICATION) {
                                        System.out.println("Applied inverse removal to common sub part");
                                        System.out.print("Exp: ");
                                        for (int[] e : encodings) {
                                            for (int i = 0; i < e.length; i++) {
                                                System.out.print(e[i]);
                                            }
                                            System.out.print(" | ");
                                        }
                                        System.out.println();
                                    }
                                } else {
                                    encodings.set(k, s.getSecond());
                                    encodings.remove(l);
                                    if (DEBUG_SIMPLIFICATION) {
                                        System.out.println("Applied De Morgans law to common sub part");
                                        System.out.print("Exp: ");
                                        for (int[] e : encodings) {
                                            for (int i = 0; i < e.length; i++) {
                                                System.out.print(e[i]);
                                            }
                                            System.out.print(" | ");
                                        }
                                        System.out.println();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            List<Condition> reverse = reverseEncoding(encodings, mapping);
            if (encodings.size() == 1) {
                return reverse.get(0);
            }
            return new OrCondition(reverse);
        }
        return condition;
    }

    private AstUtil() {
    }
}
