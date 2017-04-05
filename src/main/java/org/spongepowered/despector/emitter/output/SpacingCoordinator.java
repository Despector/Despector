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
package org.spongepowered.despector.emitter.output;

import org.spongepowered.despector.ast.AccessModifier;

import java.util.EnumSet;
import java.util.List;

public class SpacingCoordinator {

    private final EnumSet<TokenType> no_space = EnumSet.of(TokenType.LEFT_PAREN, TokenType.DOT, TokenType.LEFT_BRACKET, TokenType.GENERIC_PARAMS,
            TokenType.RIGHT_PAREN, TokenType.RIGHT_BRACKET, TokenType.ARG_SEPARATOR, TokenType.STATEMENT_END);
    private final EnumSet<TokenType> ends = EnumSet.of(TokenType.CLASS_END, TokenType.METHOD_END, TokenType.BLOCK_END);

    private final FormattedTokenEmitter emitter;

    public SpacingCoordinator(FormattedTokenEmitter emitter) {
        this.emitter = emitter;
    }

    public void reset() {
    }

    public void pre(EmitterToken token, List<EmitterToken> tokens, int index, EmitterToken next, EmitterToken last) {
        switch (token.getType()) {
        case CLASS_END:
        case METHOD_END:
        case BLOCK_END:
            this.emitter.dedent();
            this.emitter.printIndentation();
            break;
        case PACKAGE:
        case ACCESS:
        case SPECIAL:
        case MODIFIER:
        case SUPERCLASS:
        case INTERFACE:
        case TYPE:
        case NAME:
        case GENERIC_PARAMS:
        case RETURN:
        case ENUM_CONSTANT:
        case CLASS_START:
        case METHOD_START:
        case BLOCK_START:
        case LEFT_PAREN:
        case LEFT_BRACKET:
        case DOT:
        case RAW:
        case RIGHT_BRACKET:
        case RIGHT_PAREN:
        case ARG_SEPARATOR:
        case STATEMENT_END:
        case EQUALS:
        case DOUBLE:
        case BOOLEAN:
        case INT:
        case LONG:
        case FLOAT:
        case STRING:
        case CHAR:
        case OPERATOR:
        case IF:
        case ELSE_IF:
        case ELSE:
        case INSERT_IMPORTS:
        case COMMENT:
        case BLOCK_COMMENT:
        case ARRAY_INITIALIZER_START:
        case ARRAY_INITIALIZER_END:
        case OPERATOR_EQUALS:
        case FOR_EACH:
        case FOR_SEPARATOR:
        case TERNARY_IF:
        case TERNARY_ELSE:
        case LAMBDA:
        case WHEN_CASE:
        default:
            break;
        }
    }

    public void post(EmitterToken token, List<EmitterToken> tokens, int index, EmitterToken next, EmitterToken last) {
        switch (token.getType()) {
        case PACKAGE:
            this.emitter.newLine();
            this.emitter.newLine();
            break;
        case ACCESS:
            AccessModifier acc = (AccessModifier) token.getToken();
            if (!acc.asString().isEmpty()) {
                this.emitter.printString(' ');
            }
            break;
        case SPECIAL:
        case MODIFIER:
        case SUPERCLASS:
            this.emitter.printString(' ');
            break;
        case INTERFACE:
            if (checkType(next, TokenType.BLOCK_START)) {
                this.emitter.printString(' ');
            }
            break;
        case TYPE:
        case NAME:
            if(!this.no_space.contains(next.getType())) {
                this.emitter.printString(' ');
            }
            break;
        case GENERIC_PARAMS:
        case RETURN:
            if (!checkType(next, TokenType.STATEMENT_END)) {
                this.emitter.printString(' ');
            }
            break;
        case CLASS_START:
            this.emitter.newLine();
            this.emitter.indent();
            if (!this.ends.contains(next.getType())) {
                this.emitter.newLine();
                this.emitter.printIndentation();
            }
            break;
        case CLASS_END:
            this.emitter.newLine();
            break;
        case METHOD_START:
            this.emitter.newLine();
            this.emitter.indent();
            if (!this.ends.contains(next.getType())) {
                this.emitter.printIndentation();
            }
            break;
        case METHOD_END:
            this.emitter.newLine();
            this.emitter.newLine();
            if (!this.ends.contains(next.getType())) {
                this.emitter.printIndentation();
            }
            break;
        case BLOCK_START:
            this.emitter.newLine();
            this.emitter.indent();
            if (!this.ends.contains(next.getType())) {
                this.emitter.printIndentation();
            }
            break;
        case BLOCK_END:
            if(checkType(next, TokenType.DOT)) {
                break;
            }
            if (!checkType(next, TokenType.ELSE) && !checkType(next, TokenType.ELSE_IF)) {
                this.emitter.newLine();
                if (!this.ends.contains(next.getType())) {
                    this.emitter.printIndentation();
                }
            } else {
                this.emitter.printString(' ');
            }
            break;
        case RIGHT_PAREN:
            if (checkType(next, TokenType.BLOCK_START) || checkType(next, TokenType.METHOD_START)) {
                this.emitter.printString(' ');
            }
            break;
        case ARG_SEPARATOR:
            if (checkType(next, TokenType.ENUM_CONSTANT)) {
                this.emitter.newLine();
                this.emitter.printIndentation();
            } else {
                this.emitter.printString(' ');
            }
            break;
        case EQUALS:
        case OPERATOR:
            this.emitter.printString(' ');
            break;
        case STATEMENT_END:
            this.emitter.newLine();
            if (!checkType(next, TokenType.BLOCK_END) && !checkType(next, TokenType.METHOD_END) && !checkType(next, TokenType.CLASS_END)) {
                this.emitter.printIndentation();
            }
            break;
        case ENUM_CONSTANT:
        case LEFT_PAREN:
        case LEFT_BRACKET:
        case DOT:
        case RAW:
        case RIGHT_BRACKET:
        case DOUBLE:
        case BOOLEAN:
        case INT:
        case LONG:
        case FLOAT:
        case STRING:
        case CHAR:
        case IF:
        case ELSE_IF:
        case ELSE:
        case INSERT_IMPORTS:
        case COMMENT:
        case BLOCK_COMMENT:
        case ARRAY_INITIALIZER_START:
        case ARRAY_INITIALIZER_END:
        case OPERATOR_EQUALS:
        case FOR_EACH:
        case FOR_SEPARATOR:
        case TERNARY_IF:
        case TERNARY_ELSE:
        case LAMBDA:
        case WHEN_CASE:
        default:
            break;
        }
    }

    private boolean checkType(EmitterToken token, TokenType type) {
        return token != null && token.getType() == type;
    }

}
