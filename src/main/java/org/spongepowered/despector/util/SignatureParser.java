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

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.despector.ast.generic.ClassSignature;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.GenericClassTypeSignature;
import org.spongepowered.despector.ast.generic.MethodSignature;
import org.spongepowered.despector.ast.generic.TypeArgument;
import org.spongepowered.despector.ast.generic.TypeParameter;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.TypeVariableSignature;
import org.spongepowered.despector.ast.generic.VoidTypeSignature;
import org.spongepowered.despector.ast.generic.WildcardType;

import java.util.List;

/**
 * A parser for various generic signatures.
 */
public final class SignatureParser {

    private static final String VALID_PRIM = "BSIJFDCZ";

    /**
     * Parses the given class signature.
     */
    public static ClassSignature parse(String signature) {
        Parser parser = new Parser(signature);
        ClassSignature struct = new ClassSignature();
        if (signature.startsWith("<")) {
            parser.skip(1);
            parseFormalTypeParameters(parser, struct.getParameters());
        }
        GenericClassTypeSignature superclass = parseClassTypeSignature(parser, "");
        struct.setSuperclassSignature(superclass);
        while (parser.hasNext()) {
            GenericClassTypeSignature sig = parseClassTypeSignature(parser, "");
            struct.getInterfaceSignatures().add(sig);
        }
        return struct;
    }

    /**
     * Parses the given method signature.
     */
    public static MethodSignature parseMethod(String signature) {
        Parser parser = new Parser(signature);
        MethodSignature sig = new MethodSignature();
        if (parser.check('<')) {
            parseFormalTypeParameters(parser, sig.getTypeParameters());
        }
        parser.expect('(');
        while (!parser.check(')')) {
            sig.getParameters().add(parseTypeSignature(parser));
        }
        if (parser.check('V')) {
            sig.setReturnType(VoidTypeSignature.VOID);
        } else {
            sig.setReturnType(parseTypeSignature(parser));
        }
        // TODO throws signature
        return sig;
    }

    private static void parseFormalTypeParameters(Parser parser, List<TypeParameter> type_params) {
        while (parser.peek() != '>') {
            String identifier = parser.nextIdentifier();
            parser.expect(':');
            TypeSignature class_bound = null;
            if (parser.peek() != ':') {
                class_bound = parseFieldTypeSignature(parser);
            }
            TypeParameter param = new TypeParameter(identifier, class_bound);
            type_params.add(param);
            while (parser.peek() == ':') {
                parser.skip(1);
                param.getInterfaceBounds().add(parseFieldTypeSignature(parser));
            }
        }
        parser.skip(1);
    }

    public static TypeSignature parseFieldTypeSignature(String sig) {
        Parser parser = new Parser(sig);
        return parseFieldTypeSignature(parser);
    }

    private static TypeSignature parseTypeSignature(Parser parser) {
        char next = parser.peek();
        if (VALID_PRIM.indexOf(next) != -1) {
            parser.skip(1);
            return ClassTypeSignature.of(String.valueOf(next));
        }
        return parseFieldTypeSignature(parser);
    }

    private static TypeSignature parseFieldTypeSignature(Parser parser) {
        StringBuilder ident = new StringBuilder();
        while (parser.check('[')) {
            ident.append('[');
        }
        char next = parser.peek();
        if (ident.length() > 0) {
            if (VALID_PRIM.indexOf(next) != -1) {
                ident.append(next);
                parser.skip(1);
                return ClassTypeSignature.of(ident.toString());
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
        return parseClassTypeSignature(parser, ident.toString());
    }

    public static GenericClassTypeSignature parseClassTypeSignature(String sig) {
        Parser parser = new Parser(sig);
        return parseClassTypeSignature(parser, "");
    }

    private static GenericClassTypeSignature parseClassTypeSignature(Parser parser, String prefix) {
        StringBuilder ident = new StringBuilder(prefix);
        parser.expect('L');
        ident.append("L");
        ident.append(parser.nextIdentifier());
        while (parser.check('/')) {
            ident.append('/');
            ident.append(parser.nextIdentifier());
        }
        ident.append(";");
        GenericClassTypeSignature sig = new GenericClassTypeSignature(ident.toString());
        if (parser.check('<')) {
            while (!parser.check('>')) {
                char wild = parser.peek();
                WildcardType wildcard = null;
                if (wild == '*') {
                    sig.getArguments().add(new TypeArgument(WildcardType.STAR, null));
                    parser.skip(1);
                    continue;
                } else if (wild == '+') {
                    parser.skip(1);
                    wildcard = WildcardType.EXTENDS;
                } else if (wild == '-') {
                    parser.skip(1);
                    wildcard = WildcardType.SUPER;
                } else {
                    wildcard = WildcardType.NONE;
                }
                sig.getArguments().add(new TypeArgument(wildcard, parseFieldTypeSignature(parser)));
            }
        }
        while (parser.peek() == '.') {
            parser.pop();
            StringBuilder child = new StringBuilder();
            child.append("L");
            child.append(parser.nextIdentifier());
            while (parser.check('/')) {
                child.append('/');
                child.append(parser.nextIdentifier());
            }
            child.append(";");
            sig = new GenericClassTypeSignature(sig, child.toString());
            if (parser.check('<')) {
                while (!parser.check('>')) {
                    char wild = parser.peek();
                    WildcardType wildcard = null;
                    if (wild == '*') {
                        sig.getArguments().add(new TypeArgument(WildcardType.STAR, null));
                        parser.skip(1);
                        continue;
                    } else if (wild == '+') {
                        parser.skip(1);
                        wildcard = WildcardType.EXTENDS;
                    } else if (wild == '-') {
                        parser.skip(1);
                        wildcard = WildcardType.SUPER;
                    } else {
                        wildcard = WildcardType.NONE;
                    }
                    sig.getArguments().add(new TypeArgument(wildcard, parseFieldTypeSignature(parser)));
                }
            }

        }
        parser.expect(';');
        return sig;
    }

    /**
     * A helper for parsing.
     */
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

        public char pop() {
            return this.buffer.charAt(this.index++);
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
