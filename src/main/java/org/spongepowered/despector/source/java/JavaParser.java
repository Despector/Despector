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

import com.google.common.collect.Lists;
import org.spongepowered.despector.source.SourceFile;
import org.spongepowered.despector.source.SourceParser;
import org.spongepowered.despector.source.parse.Lexer;

import java.util.List;

public class JavaParser implements SourceParser {

    @Override
    public SourceFile parse(String name, String input) {
        Lexer lexer = new Lexer(input);

        SourceFile src = new SourceFile(name);
        ParseState state = new ParseState(src);

        parseJavaSource(state, lexer);

        return src;
    }

    public List<String> parseComment(ParseState state, Lexer lexer) {
        if (lexer.peekType() != FORWARD_SLASH) {
            return null;
        }
        int mark = lexer.mark();
        lexer.pop();
        if (lexer.peekType() != STAR) {
            lexer.reset(mark);
            return null;
        }
        String text = lexer.until("*/");
        List<String> lines = Lists.newArrayList(text.split("\r?\n"));
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = line.trim();
            if (line.startsWith("*")) {
                line = line.substring(1).trim();
            }
            lines.set(i, line);
        }
        return lines;
    }

    public void parseJavaSource(ParseState state, Lexer lexer) {
        List<String> header = parseComment(state, lexer);
        if (header != null) {
            state.getSource().setHeader(header);
        }
        List<String> comment = null;
        while (true) {
            if (lexer.peekType() == FORWARD_SLASH) {
                comment = parseComment(state, lexer);
            }
            String next = lexer.expect(IDENTIFIER).getString();
            if ("package".equals(next)) {
                state.getSource().setPackage(parsePackage(state, lexer));
            } else if ("import".equals(next)) {
                state.getSource().addImport(parseImport(state, lexer));
            } else {
                throw new IllegalStateException("Unexpected identifier: '" + next + "'");
            }
        }
    }

    private String parsePackage(ParseState state, Lexer lexer) {
        String pkg = lexer.expect(IDENTIFIER).getString();
        while (lexer.peekType() != SEMICOLON) {
            lexer.expect(DOT);
            pkg += ".";
            pkg += lexer.expect(IDENTIFIER).getString();
        }
        lexer.expect(SEMICOLON);
        return pkg;
    }

    private String parseImport(ParseState state, Lexer lexer) {
        String pkg = lexer.expect(IDENTIFIER).getString();
        while (lexer.peekType() != SEMICOLON) {
            lexer.expect(DOT);
            pkg += ".";
            if (lexer.peekType() == STAR) {
                lexer.pop();
                pkg += "*";
                break;
            }
            pkg += lexer.expect(IDENTIFIER).getString();
        }
        lexer.expect(SEMICOLON);
        return pkg;
    }

}
