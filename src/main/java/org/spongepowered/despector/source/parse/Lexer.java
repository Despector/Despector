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
package org.spongepowered.despector.source.parse;

import java.util.BitSet;

public class Lexer {

    public static final BitSet LOWER_ALPHA;
    public static final BitSet UPPER_ALPHA;
    public static final BitSet ALPHA;
    public static final BitSet NUMERIC;
    public static final BitSet HEXNUMBER;
    public static final BitSet ALPHA_NUMERIC;
    public static final BitSet IDENTIFIER;

    private final String str;
    private final int length;
    private int index = 0;

    private boolean extract_string_constants = true;

    private ParseToken next = null;

    public Lexer(String s) {
        this.str = s;
        this.length = s.length();
    }

    public int mark() {
        return this.index;
    }

    public void reset(int mark) {
        this.index = mark;
        this.next = null;
    }

    private void check() {
        if (this.next != null) {
            return;
        }
        if (this.index >= this.length) {
            return;
        }
        char n = this.str.charAt(this.index++);
        while (Character.isWhitespace(n) && this.index < this.length) {
            n = this.str.charAt(this.index++);
        }
        switch (n) {
        case '$':
            this.next = new ParseToken(TokenType.DOLLAR);
            break;
        case ';':
            this.next = new ParseToken(TokenType.SEMICOLON);
            break;
        case '.':
            this.next = new ParseToken(TokenType.DOT);
            break;
        case ',':
            this.next = new ParseToken(TokenType.COMMA);
            break;
        case ':':
            this.next = new ParseToken(TokenType.COLON);
            break;
        case '(':
            this.next = new ParseToken(TokenType.PAREN_LEFT);
            break;
        case ')':
            this.next = new ParseToken(TokenType.PAREN_RIGHT);
            break;
        case '/':
            this.next = new ParseToken(TokenType.FORWARD_SLASH);
            break;
        case '=':
            this.next = new ParseToken(TokenType.EQUALS);
            break;
        case '!':
            this.next = new ParseToken(TokenType.NOT);
            break;
        case '*':
            this.next = new ParseToken(TokenType.STAR);
            break;
        case '{':
            this.next = new ParseToken(TokenType.BRACE_LEFT);
            break;
        case '}':
            this.next = new ParseToken(TokenType.BRACE_RIGHT);
            break;
        case '[':
            this.next = new ParseToken(TokenType.BRACKET_LEFT);
            break;
        case ']':
            this.next = new ParseToken(TokenType.BRACKET_RIGHT);
            break;
        default:
        }
        if (this.next != null) {
            return;
        }
        if (ALPHA.get(n)) {
            StringBuilder token = new StringBuilder();
            token.append(n);
            n = this.str.charAt(this.index++);
            while (IDENTIFIER.get(n)) {
                token.append(n);
                n = this.str.charAt(this.index++);
            }
            this.index--;
            this.next = new ParseToken(TokenType.IDENTIFIER, token.toString());
        } else if (NUMERIC.get(n)) {
            StringBuilder token = new StringBuilder();
            token.append(n);
            n = this.str.charAt(this.index++);
            if (n == 'x') {
                token.append(n);
                n = this.str.charAt(this.index++);
                while (HEXNUMBER.get(n)) {
                    token.append(n);
                    n = this.str.charAt(this.index++);
                }
                this.index--;
                this.next = new ParseToken(TokenType.HEXADECIMAL, token.toString());
            }
            while (NUMERIC.get(n)) {
                token.append(n);
                n = this.str.charAt(this.index++);
            }
            if (n == 'L') {
                this.next = new ParseToken(TokenType.LONG, token.toString());
            } else if (n == '.') {
                token.append(n);
                n = this.str.charAt(this.index++);
                while (NUMERIC.get(n)) {
                    token.append(n);
                    n = this.str.charAt(this.index++);
                }
                if (n == 'F' || n == 'f') {
                    this.next = new ParseToken(TokenType.FLOAT, token.toString());
                } else if (n == 'D' || n == 'd') {
                    this.next = new ParseToken(TokenType.DOUBLE, token.toString());
                } else {
                    this.index--;
                    this.next = new ParseToken(TokenType.DOUBLE, token.toString());
                }
            } else {
                this.index--;
                this.next = new ParseToken(TokenType.INTEGER, token.toString());
            }
        } else if (this.extract_string_constants && n == '"') {
            StringBuilder str = new StringBuilder();
            n = this.str.charAt(this.index++);
            while (n != '"') {
                if (n == '\\') {
                    n = this.str.charAt(this.index++);
                    switch (n) {
                    case '"':
                        str.append('"');
                        break;
                    case 'n':
                        str.append('\n');
                        break;
                    case 't':
                        str.append('\t');
                        break;
                    case 'r':
                        str.append('\r');
                        break;
                    case 'b':
                        str.append('\b');
                        break;
                    case 'f':
                        str.append('\f');
                        break;
                    case '\\':
                        str.append('\\');
                        break;
                    case '\'':
                        str.append('\'');
                        break;
                    default:
                        throw new IllegalStateException("Invalid escape char in stringF '" + n + "'");
                    }
                    continue;
                }
                str.append(n);
                n = this.str.charAt(this.index++);
            }
            this.next = new ParseToken(TokenType.STRING_CONSTANT, str.toString());
        } else {
            throw new IllegalStateException("Unexpected symbol '" + n + "'");
        }
    }

    public boolean hasNext() {
        check();
        return this.next != null;
    }

    public TokenType peekType() {
        check();
        return this.next == null ? null : this.next.getType();
    }

    public ParseToken peek() {
        check();
        return this.next;
    }

    public ParseToken pop() {
        check();
        ParseToken n = this.next;
        this.next = null;
        return n;
    }

    public ParseToken expect(TokenType type) {
        ParseToken n = pop();
        if (n.getType() != type) {
            throw new IllegalStateException("Expected " + type + " but was " + n);
        }
        return n;
    }

    public ParseToken expect(TokenType type, String str) {
        ParseToken n = pop();
        if (n.getType() != type) {
            throw new IllegalStateException("Expected " + type + " but was " + n);
        }
        if (!n.getString().equals(str)) {
            throw new IllegalStateException("Expected '" + str + "' but was " + n);
        }
        return n;
    }

    public String until(String match) {
        int next = this.str.indexOf(match, this.index);
        String text = this.str.substring(this.index, next);
        this.index = next + match.length();
        this.next = null;
        return text;
    }

    static {
        LOWER_ALPHA = new BitSet();
        for (int i = 'a'; i <= 'z'; i++) {
            LOWER_ALPHA.set(i);
        }
        UPPER_ALPHA = new BitSet();
        for (int i = 'A'; i <= 'Z'; i++) {
            UPPER_ALPHA.set(i);
        }
        ALPHA = new BitSet();
        ALPHA.or(LOWER_ALPHA);
        ALPHA.or(UPPER_ALPHA);
        NUMERIC = new BitSet();
        for (int i = '0'; i <= '9'; i++) {
            NUMERIC.set(i);
        }
        HEXNUMBER = new BitSet();
        HEXNUMBER.or(NUMERIC);
        for (int i = 'a'; i <= 'f'; i++) {
            HEXNUMBER.set(i);
        }
        for (int i = 'A'; i <= 'F'; i++) {
            HEXNUMBER.set(i);
        }
        ALPHA_NUMERIC = new BitSet();
        ALPHA_NUMERIC.or(ALPHA);
        ALPHA_NUMERIC.or(NUMERIC);
        IDENTIFIER = new BitSet();
        IDENTIFIER.or(ALPHA_NUMERIC);
        IDENTIFIER.set('_');
        IDENTIFIER.set('$');
    }

}
