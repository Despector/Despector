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
import org.spongepowered.despector.ast.insn.cst.DoubleConstant;
import org.spongepowered.despector.ast.insn.cst.FloatConstant;
import org.spongepowered.despector.ast.insn.cst.IntConstant;
import org.spongepowered.despector.ast.insn.cst.LongConstant;
import org.spongepowered.despector.ast.insn.cst.NullConstant;
import org.spongepowered.despector.ast.insn.cst.StringConstant;
import org.spongepowered.despector.ast.insn.op.Operator;
import org.spongepowered.despector.ast.insn.op.OperatorType;
import org.spongepowered.despector.ast.insn.var.InstanceFieldAccess;
import org.spongepowered.despector.ast.insn.var.StaticFieldAccess;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.ast.stmt.invoke.New;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;
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
        outer: while (lexer.hasNext()) {
            if (lexer.peekType() == SEMICOLON) {
                break;
            } else if (lexer.peekType() == STRING_CONSTANT) {
                tokens.add(new InstructionPart(new StringConstant(lexer.pop().getString())));
            } else if (lexer.peekType() == INTEGER || lexer.peekType() == TokenType.HEXADECIMAL) {
                int val = Integer.parseInt(lexer.pop().getString());
                tokens.add(new InstructionPart(new IntConstant(val)));
            } else if (lexer.peekType() == DOUBLE) {
                double val = Double.parseDouble(lexer.pop().getString());
                tokens.add(new InstructionPart(new DoubleConstant(val)));
            } else if (lexer.peekType() == LONG) {
                long val = Long.parseLong(lexer.pop().getString());
                tokens.add(new InstructionPart(new LongConstant(val)));
            } else if (lexer.peekType() == FLOAT) {
                float val = Float.parseFloat(lexer.pop().getString());
                tokens.add(new InstructionPart(new FloatConstant(val)));
            } else if (lexer.peekType() == PLUS) {
                lexer.pop();
                if (lexer.peekType() == PLUS) {
                    lexer.pop();
                    tokens.add(new OperatorPart(Operators.INCREMENT));
                } else {
                    tokens.add(new OperatorPart(Operators.ADD));
                }
            } else if (lexer.peekType() == MINUS) {
                lexer.pop();
                if (lexer.peekType() == MINUS) {
                    lexer.pop();
                    tokens.add(new OperatorPart(Operators.DECREMENT));
                } else {
                    tokens.add(new OperatorPart(Operators.SUB));
                }
            } else if (lexer.peekType() == STAR) {
                lexer.pop();
                tokens.add(new OperatorPart(Operators.MULT));
            } else if (lexer.peekType() == TokenType.FORWARD_SLASH) {
                lexer.pop();
                tokens.add(new OperatorPart(Operators.DIV));
            } else if (lexer.peekType() == MODULO) {
                lexer.pop();
                tokens.add(new OperatorPart(Operators.MOD));
            } else if (lexer.peekType() == PAREN_LEFT || lexer.peekType() == PAREN_RIGHT) {
                tokens.add(new TokenPart(lexer.pop()));
            } else if (lexer.peekType() == TokenType.IDENTIFIER) {
                Instruction callee = null;
                String accu = "";
                String static_type = null;
                // TODO static type match should be greedy
                while (lexer.hasNext()) {
                    String next = lexer.expect(IDENTIFIER).getString();
                    if (callee == null) {
                        if ("null".equals(next)) {
                            callee = NullConstant.NULL;
                        } else if ("new".equals(next)) {
                            if (!accu.isEmpty()) {
                                throw new IllegalStateException();
                            }
                            TypeSignature type = this.parser.parseType(state, lexer);
                            lexer.expect(PAREN_LEFT);
                            List<Instruction> args = new ArrayList<>();
                            boolean first = true;
                            String tmp_desc = "(";
                            while (lexer.peekType() != PAREN_RIGHT) {
                                if (!first) {
                                    lexer.expect(COMMA);
                                } else {
                                    first = false;
                                }
                                Instruction arg = parseExpression(state, lexer);
                                tmp_desc += arg.inferType().getDescriptor();
                                args.add(arg);
                            }
                            tmp_desc += ")";
                            lexer.expect(PAREN_RIGHT);
                            if (lexer.peekType() != SEMICOLON) {
                                tmp_desc += "Ljava/lang/Object;";
                            } else {
                                tmp_desc += "V";
                            }
                            callee = new New(type, tmp_desc, args.toArray(new Instruction[args.size()]));
                        } else if (static_type == null) {
                            accu += next;
                            String type = state.getSource().resolveType(accu);
                            if (type != null) {
                                static_type = type;
                            } else {
                                accu += ".";
                            }
                        } else {
                            if (lexer.peekType() == PAREN_LEFT) {
                                lexer.expect(PAREN_LEFT);
                                List<Instruction> args = new ArrayList<>();
                                boolean first = true;
                                String tmp_desc = "(";
                                while (lexer.peekType() != PAREN_RIGHT) {
                                    if (!first) {
                                        lexer.expect(COMMA);
                                    } else {
                                        first = false;
                                    }
                                    Instruction arg = parseExpression(state, lexer);
                                    tmp_desc += arg.inferType().getDescriptor();
                                    args.add(arg);
                                }
                                tmp_desc += ")";
                                lexer.expect(PAREN_RIGHT);
                                if (lexer.peekType() != SEMICOLON) {
                                    tmp_desc += "Ljava/lang/Object;";
                                } else {
                                    tmp_desc += "V";
                                }
                                callee = new StaticMethodInvoke(next, tmp_desc, "L" + static_type + ";", args.toArray(new Instruction[args.size()]));
                                if (lexer.peekType() == SEMICOLON) {
                                    lexer.expect(SEMICOLON);
                                    break outer;
                                }
                            } else {
                                callee = new StaticFieldAccess(next, null, "L" + static_type + ";");
                            }
                        }
                    } else {
                        if (lexer.peekType() == PAREN_LEFT) {
                            lexer.expect(PAREN_LEFT);
                            List<Instruction> args = new ArrayList<>();
                            boolean first = true;
                            String tmp_desc = "(";
                            while (lexer.peekType() != PAREN_RIGHT) {
                                if (!first) {
                                    lexer.expect(COMMA);
                                } else {
                                    first = false;
                                }
                                Instruction arg = parseExpression(state, lexer);
                                tmp_desc += arg.inferType().getDescriptor();
                                args.add(arg);
                            }
                            tmp_desc += ")";
                            lexer.expect(PAREN_RIGHT);
                            if (lexer.peekType() != SEMICOLON) {
                                tmp_desc += "Ljava/lang/Object;";
                            } else {
                                tmp_desc += "V";
                            }
                            callee = new InstanceMethodInvoke(null, next, tmp_desc, "incomplete", args.toArray(new Instruction[args.size()]),
                                    callee);
                            if (lexer.peekType() == SEMICOLON) {
                                lexer.expect(SEMICOLON);
                                break outer;
                            }
                        } else {
                            callee = new InstanceFieldAccess(next, null, "incomplete", callee);
                        }
                    }
                    if (lexer.peekType() != DOT) {
                        break;
                    }
                    lexer.expect(DOT);
                }
                if (callee == null) {
                    throw new IllegalStateException();
                }
                tokens.add(new InstructionPart(callee));
            } else {
                throw new IllegalStateException();
            }
        }
        List<ExpressionPart> output = new ArrayList<>();
        Deque<ExpressionPart> stack = new ArrayDeque<>();

        for (ExpressionPart token : tokens) {
            if (token instanceof InstructionPart) {
                output.add(token);
            } else if (token instanceof OperatorPart) {
                Operators op = ((OperatorPart) token).op;
                while (stack.peek() instanceof OperatorPart) {
                    Operators head = ((OperatorPart) stack.peek()).op;
                    if (op.left_assoc) {
                        if (op.precidence <= head.precidence) {
                            output.add(stack.pop());
                            continue;
                        }
                    } else if (op.precidence < head.precidence) {
                        output.add(stack.pop());
                        continue;

                    }
                    break;
                }
                stack.push(token);
            } else if (token instanceof TokenPart) {
                TokenType type = ((TokenPart) token).type;
                if (type == PAREN_LEFT) {
                    stack.push(token);
                } else if (type == PAREN_RIGHT) {
                    while (stack.peek().type != PAREN_LEFT) {
                        output.add(stack.pop());
                    }
                    stack.pop();
                }
            } else {
                throw new IllegalStateException();
            }
        }

        while (!stack.isEmpty()) {
            if (stack.peek().type == PAREN_LEFT) {
                throw new IllegalStateException();
            }
            output.add(stack.pop());
        }

        Deque<Instruction> insn = new ArrayDeque<>();

        for (ExpressionPart token : output) {
            if (token instanceof InstructionPart) {
                insn.push(((InstructionPart) token).insn);
            } else if (token instanceof OperatorPart) {
                Operators op = ((OperatorPart) token).op;
                switch (op) {
                case ADD: {
                    Instruction left = insn.pop();
                    Instruction right = insn.pop();
                    insn.push(new Operator(OperatorType.AND, left, right));
                    break;
                }
                case SUB: {
                    Instruction left = insn.pop();
                    Instruction right = insn.pop();
                    insn.push(new Operator(OperatorType.SUBTRACT, left, right));
                    break;
                }
                case MULT: {
                    Instruction left = insn.pop();
                    Instruction right = insn.pop();
                    insn.push(new Operator(OperatorType.MULTIPLY, left, right));
                    break;
                }
                case DIV: {
                    Instruction left = insn.pop();
                    Instruction right = insn.pop();
                    insn.push(new Operator(OperatorType.DIVIDE, left, right));
                    break;
                }
                case MOD: {
                    Instruction left = insn.pop();
                    Instruction right = insn.pop();
                    insn.push(new Operator(OperatorType.REMAINDER, left, right));
                    break;
                }
                default:
                    throw new IllegalStateException();
                }
            }
        }

        if (insn.size() != 1) {
            throw new IllegalStateException();
        }
        return insn.pop();
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

    public static class OperatorPart extends ExpressionPart {

        public Operators op;

        public OperatorPart(Operators op) {
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

    public static enum Operators {
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

        Operators(int p, boolean a) {
            this.precidence = p;
            this.left_assoc = a;
        }
    }

}
