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
package org.spongepowered.despector.source.java;

import static org.spongepowered.despector.source.parse.TokenType.*;

import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.cst.StringConstant;
import org.spongepowered.despector.source.parse.Lexer;
import org.spongepowered.despector.source.parse.ParseToken;
import org.spongepowered.despector.source.parse.TokenType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ExpressionParser {

    private final JavaParser parser;

    public ExpressionParser(JavaParser p) {
        this.parser = p;
    }

    public Instruction parseExpression(ParseState state, Lexer lexer) {
        List<ExpressionPart> tokens = new ArrayList<>();
        while (lexer.hasNext()) {
            if (lexer.peekType() == STRING_CONSTANT || lexer.peekType() == INTEGER || lexer.peekType() == DOUBLE || lexer.peekType() == LONG
                    || lexer.peekType() == TokenType.HEXADECIMAL || lexer.peekType() == FLOAT) {
                tokens.add(new TokenPart(lexer.pop()));
            } else if (lexer.peekType() == PLUS) {
                lexer.pop();
                if (lexer.peekType() == PLUS) {
                    lexer.pop();
                    tokens.add(new OperatorPart(Operator.INCREMENT));
                } else {
                    tokens.add(new OperatorPart(Operator.ADD));
                }
            } else if (lexer.peekType() == MINUS) {
                lexer.pop();
                if (lexer.peekType() == MINUS) {
                    lexer.pop();
                    tokens.add(new OperatorPart(Operator.DECREMENT));
                } else {
                    tokens.add(new OperatorPart(Operator.SUB));
                }
            } else if (lexer.peekType() == STAR) {
                lexer.pop();
                tokens.add(new OperatorPart(Operator.MULT));
            } else if (lexer.peekType() == TokenType.FORWARD_SLASH) {
                lexer.pop();
                tokens.add(new OperatorPart(Operator.DIV));
            } else if (lexer.peekType() == MODULO) {
                lexer.pop();
                tokens.add(new OperatorPart(Operator.MOD));
            } else {
                throw new IllegalStateException();
            }
        }

        return null;
    }

    public static abstract class ExpressionPart {

        public TokenType type;

        public ExpressionPart(TokenType t) {
            this.type = t;
        }

        public TokenType getType() {
            return this.type;
        }

    }

    public static class TokenPart extends ExpressionPart {

        public ParseToken token;

        public TokenPart(ParseToken t) {
            super(t.getType());
            this.token = t;
        }
    }

    public static class NewPart extends ExpressionPart {

        public TypeSignature type;

        public NewPart(TypeSignature t) {
            super(IDENTIFIER);
            this.type = t;
        }
    }

    public static class OperatorPart extends ExpressionPart {

        public Operator op;

        public OperatorPart(Operator op) {
            super(TokenType.PLUS);
            this.op = op;
        }
    }

    public static class InstructionPart extends ExpressionPart {

        public Instruction insn;

        public InstructionPart(Instruction i) {
            super(TokenType.DOT);
            this.insn = i;
        }
    }

    public static enum Operator {
        DOT(1, true),
        NOT(2, false),
        BITWISE_NOT(2, false),
        NEGATIVE(2, false),
        INCREMENT(2, false),
        DECREMENT(2, false),
        CAST(2, false),
        MULT(3, true),
        DIV(3, true),
        MOD(3, true),
        ADD(4, true),
        SUB(4, true),
        SHL(5, true),
        SHR(5, true),
        GREATER(6, true),
        GREATER_EQUAL(6, true),
        LESS(6, true),
        LESS_EQUAL(6, true),
        EQUAL(7, true),
        NOT_EQUAL(7, true),
        AND(8, true),
        XOR(9, true),
        OR(10, true),
        LOGICAL_AND(11, true),
        LOGICAL_OR(12, true),
        TERNARY_Q(13, false),
        TERNARY_ELSE(13, false);

        public int precidence;
        public boolean left_assoc;

        Operator(int p, boolean a) {
            this.precidence = p;
            this.left_assoc = a;
        }
    }

}
