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

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.despector.ast.generic.ClassSignature;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeArgument;
import org.spongepowered.despector.ast.generic.TypeParameter;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.TypeVariableSignature;
import org.spongepowered.despector.ast.generic.WildcardType;

public class SignatureParser {

    private static final String VALID_PRIM = "BSIJFDCZ";

    public static ClassSignature parse(String signature) {
        Parser parser = new Parser(signature);
        ClassSignature struct = new ClassSignature();
        if (signature.startsWith("<")) {
            parser.skip(1);
            parseFormalTypeParameters(parser, struct);
        }
        ClassTypeSignature superclass = parseClassTypeSignature(parser);
        struct.setSuperclassSignature(superclass);
        while (parser.hasNext()) {
            ClassTypeSignature sig = parseClassTypeSignature(parser);
            struct.getInterfaceSignatures().add(sig);
        }
        return struct;
    }

    private static void parseFormalTypeParameters(Parser parser, ClassSignature struct) {
        while (parser.peek() != '>') {
            String identifier = parser.nextIdentifier();
            parser.expect(':');
            TypeSignature class_bound = null;
            if (parser.peek() != ':') {
                class_bound = parseTypeSignature(parser);
            }
            TypeParameter param = new TypeParameter(identifier, class_bound);
            struct.getParameters().add(param);
            while (parser.peek() == ':') {
                parser.skip(1);
                param.getInterfaceBounds().add(parseTypeSignature(parser));
            }
        }
        parser.skip(1);
    }

    public static TypeSignature parseTypeSignature(String sig) {
        Parser parser = new Parser(sig);
        return parseTypeSignature(parser);
    }

    private static TypeSignature parseTypeSignature(Parser parser) {
        StringBuilder ident = new StringBuilder();
        while (parser.check('[')) {
            ident.append('[');
        }
        char next = parser.peek();
        if (ident.length() > 0) {
            if (VALID_PRIM.indexOf(next) != -1) {
                ident.append(next);
                return new ClassTypeSignature(ident.toString());
            }
        }
        if (next == 'T') {
            parser.skip(1);
            ident.append('T');
            ident.append(parser.nextIdentifier());
            ident.append(';');
            TypeVariableSignature sig = new TypeVariableSignature(ident.toString());
            parser.expect(';');
            return sig;
        }
        checkState(next == 'L');
        return parseClassTypeSignature(parser);
    }

    public static ClassTypeSignature parseClassTypeSignature(String sig) {
        Parser parser = new Parser(sig);
        return parseClassTypeSignature(parser);
    }

    private static ClassTypeSignature parseClassTypeSignature(Parser parser) {
        StringBuilder ident = new StringBuilder();
        parser.expect('L');
        ident.append(parser.nextIdentifier());
        while (parser.check('/')) {
            ident.append('/');
            ident.append(parser.nextIdentifier());
        }
        ClassTypeSignature sig = new ClassTypeSignature(ident.toString());
        if (parser.check('<')) {
            while (!parser.check('>')) {
                char wild = parser.peek();
                WildcardType wildcard = null;
                if (wild == '*') {
                    sig.getArguments().add(new TypeArgument(WildcardType.STAR, null));
                    parser.skip(1);
                    continue;
                } else if (wild == '+') {
                    wildcard = WildcardType.EXTENDS;
                } else if (wild == '-') {
                    wildcard = WildcardType.SUPER;
                } else {
                    wildcard = WildcardType.NONE;
                }
                sig.getArguments().add(new TypeArgument(wildcard, parseTypeSignature(parser)));
            }
        }
        if (parser.peek() == '.') {

        }
        parser.expect(';');
        return sig;
    }

    private static class Parser {

        private int index;
        private String buffer;

        public Parser(String data) {
            this.buffer = data;
            this.index = 0;
        }

        public boolean hasNext() {
            return this.index < this.buffer.length();
        }

        public void skip(int n) {
            this.index += n;
        }

        public char peek() {
            return this.buffer.charAt(this.index);
        }

        public boolean check(char n) {
            if (peek() == n) {
                this.index++;
                return true;
            }
            return false;
        }

        public void expect(char n) {
            if (peek() != n) {
                throw new IllegalStateException("Expected '" + n + "' at char " + this.index + " in \"" + this.buffer + "\"");
            }
            this.index++;
        }

        public String nextIdentifier() {
            StringBuilder ident = new StringBuilder();
            int start = this.index;
            for (; this.index < this.buffer.length(); this.index++) {
                char next = this.buffer.charAt(this.index);
                if ((next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z') || next == '_' || next == '$'
                        || (this.index > start && next >= '0' && next <= '9')) {
                    ident.append(next);
                } else {
                    break;
                }
            }
            if (ident.length() == 0) {
                throw new IllegalStateException("Expected identifier at char " + this.index + " in \"" + this.buffer + "\"");
            }
            return ident.toString();
        }

    }

    private SignatureParser() {
    }

}
