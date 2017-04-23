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

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.InstructionVisitor;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.ast.insn.cst.DoubleConstant;
import org.spongepowered.despector.ast.insn.cst.FloatConstant;
import org.spongepowered.despector.ast.insn.cst.IntConstant;
import org.spongepowered.despector.ast.insn.cst.LongConstant;
import org.spongepowered.despector.ast.insn.cst.NullConstant;
import org.spongepowered.despector.ast.insn.cst.StringConstant;
import org.spongepowered.despector.ast.insn.cst.TypeConstant;
import org.spongepowered.despector.ast.insn.misc.Cast;
import org.spongepowered.despector.ast.insn.misc.InstanceOf;
import org.spongepowered.despector.ast.insn.misc.NewArray;
import org.spongepowered.despector.ast.insn.misc.NumberCompare;
import org.spongepowered.despector.ast.insn.misc.Ternary;
import org.spongepowered.despector.ast.insn.op.NegativeOperator;
import org.spongepowered.despector.ast.insn.op.Operator;
import org.spongepowered.despector.ast.insn.var.ArrayAccess;
import org.spongepowered.despector.ast.insn.var.InstanceFieldAccess;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.ast.insn.var.StaticFieldAccess;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.invoke.DynamicInvoke;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.ast.stmt.invoke.New;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;
import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.ir.InvokeInsn;

import java.util.List;

/**
 * Various utilities for working with AST elements.
 */
public final class AstUtil {

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
            InvokeInsn method = (InvokeInsn) next;
            int count = TypeHelper.paramCount(method.getDescription());
            // the object ref
            count++;
            return count;
        }
        case Insn.INVOKESTATIC: {
            InvokeInsn method = (InvokeInsn) next;
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
            InvokeInsn method = (InvokeInsn) next;
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
    private static class LocalFinder implements InstructionVisitor {

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

        @Override
        public void visitArrayAccess(ArrayAccess insn) {
        }

        @Override
        public void visitCast(Cast insn) {
        }

        @Override
        public void visitDoubleConstant(DoubleConstant insn) {
        }

        @Override
        public void visitDynamicInvoke(DynamicInvoke insn) {
        }

        @Override
        public void visitFloatConstant(FloatConstant insn) {
        }

        @Override
        public void visitInstanceFieldAccess(InstanceFieldAccess insn) {
        }

        @Override
        public void visitInstanceMethodInvoke(InstanceMethodInvoke insn) {
        }

        @Override
        public void visitInstanceOf(InstanceOf insn) {
        }

        @Override
        public void visitIntConstant(IntConstant insn) {
        }

        @Override
        public void visitLocalAccess(LocalAccess insn) {
        }

        @Override
        public void visitLongConstant(LongConstant insn) {
        }

        @Override
        public void visitNegativeOperator(NegativeOperator insn) {
        }

        @Override
        public void visitNew(New insn) {
        }

        @Override
        public void visitNewArray(NewArray insn) {
        }

        @Override
        public void visitNullConstant(NullConstant insn) {
        }

        @Override
        public void visitNumberCompare(NumberCompare insn) {
        }

        @Override
        public void visitOperator(Operator insn) {
        }

        @Override
        public void visitStaticFieldAccess(StaticFieldAccess insn) {
        }

        @Override
        public void visitStaticMethodInvoke(StaticMethodInvoke insn) {
        }

        @Override
        public void visitStringConstant(StringConstant insn) {
        }

        @Override
        public void visitTernary(Ternary insn) {
        }

        @Override
        public void visitTypeConstant(TypeConstant insn) {
        }

    }

    private AstUtil() {
    }
}
