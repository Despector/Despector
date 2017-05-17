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
import org.spongepowered.despector.Language;
import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.type.AnnotationEntry;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.EnumEntry;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.source.SourceParser;
import org.spongepowered.despector.source.ast.SourceFile;
import org.spongepowered.despector.source.ast.SourceFileSet;
import org.spongepowered.despector.source.parse.Lexer;

import java.util.List;

public class JavaParser implements SourceParser {

    @Override
    public SourceFile parse(SourceFileSet set, String name, String input) {
        Lexer lexer = new Lexer(input);

        SourceFile src = new SourceFile(name);
        ParseState state = new ParseState(set, src);

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
            int mark = lexer.mark();
            if (lexer.peekType() == AT) {
                // TODO parse type annotations
                lexer.pop();
                String next = lexer.expect(IDENTIFIER).getString();
                if (!next.equals("interface")) {
                    throw new IllegalStateException();
                }
                lexer.reset(mark);
                TypeEntry type = parseTopLevelType(state, lexer);
                if (comment != null) {
                    type.setClassJavadoc(comment);
                    comment = null;
                }
                state.getSource().addTopLevelType(type);
                continue;
            }
            String next = lexer.expect(IDENTIFIER).getString();
            if ("package".equals(next)) {
                state.getSource().setPackage(parsePackage(state, lexer));
            } else if ("import".equals(next)) {
                state.getSource().addImport(parseImport(state, lexer));
            } else if ("public".equals(next) || "final".equals(next) || "class".equals(next) || "enum".equals(next) || "interface".equals(next)) {
                lexer.reset(mark);
                TypeEntry type = parseTopLevelType(state, lexer);
                if (comment != null) {
                    type.setClassJavadoc(comment);
                    comment = null;
                }
                state.getSource().addTopLevelType(type);
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

    private AccessModifier parseAccessModifier(ParseState state, Lexer lexer) {
        if (lexer.peekType() == IDENTIFIER) {
            String next = lexer.peek().getString();
            if ("public".equals(next)) {
                lexer.pop();
                return AccessModifier.PUBLIC;
            } else if ("private".equals(next)) {
                lexer.pop();
                return AccessModifier.PRIVATE;
            } else if ("protected".equals(next)) {
                lexer.pop();
                return AccessModifier.PROTECTED;
            }
        }
        return AccessModifier.PACKAGE_PRIVATE;
    }

    private TypeEntry parseTopLevelType(ParseState state, Lexer lexer) {
        AccessModifier acc = parseAccessModifier(state, lexer);
        String type = null;
        boolean is_final = false;
        boolean is_abstract = false;
        while (true) {
            if (lexer.peekType() == AT) {
                lexer.pop();
                String next = lexer.expect(IDENTIFIER).getString();
                if (!next.equals("interface")) {
                    throw new IllegalStateException();
                }
                type = "@interface";
                break;
            }
            String next = lexer.expect(IDENTIFIER).getString();
            if ("final".equals(next)) {
                is_final = true;
            } else if ("abstract".equals(next)) {
                is_abstract = true;
            } else if ("class".equals(next) || "interface".equals(next) || "enum".equals(next)) {
                type = next;
                break;
            } else {
                throw new IllegalStateException("Unexpected identifier: " + next);
            }
        }
        String name = lexer.expect(IDENTIFIER).getString();
        TypeEntry entry = null;
        if ("class".equals(type)) {
            entry = new ClassEntry(state.getSourceSet(), Language.JAVA, name);
        } else if ("enum".equals(type)) {
            entry = new EnumEntry(state.getSourceSet(), Language.JAVA, name);
        } else if ("interface".equals(type)) {
            entry = new InterfaceEntry(state.getSourceSet(), Language.JAVA, name);
        } else if ("@interface".equals(type)) {
            entry = new AnnotationEntry(state.getSourceSet(), Language.JAVA, name);
        }
        entry.setAccessModifier(acc);
        entry.setFinal(is_final);
        entry.setAbstract(is_abstract);

        // TODO generic signature

        if (lexer.peekType() != BRACE_LEFT) {
            String next = lexer.expect(IDENTIFIER).getString();
            if (entry instanceof ClassEntry && "extends".equals(next)) {
                // TODO handle fully qualified types
                ((ClassEntry) entry).setSuperclass("L" + state.getSource().resolveType(lexer.expect(IDENTIFIER).getString()) + ";");
                // TODO superclass generics
                if (lexer.peekType() == BRACE_LEFT) {
                    next = null;
                } else {
                    next = lexer.expect(IDENTIFIER).getString();
                }
            }
            if ("implements".equals(next)) {
                // TODO interfaces
            }
        }

        lexer.expect(BRACE_LEFT);

        while (lexer.peekType() != BRACE_RIGHT) {
            // TODO check for annotation

            int mark = lexer.mark();
            while (lexer.peekType() == IDENTIFIER) {
                lexer.pop();
            }
            if (lexer.peekType() == PAREN_LEFT) {
                lexer.reset(mark);
                MethodEntry mth = parseMethod(state, lexer);
                entry.addMethod(mth);
            } else if (lexer.peekType() == EQUALS || lexer.peekType() == SEMICOLON) {
                lexer.reset(mark);
                FieldEntry fld = parseField(state, lexer);
                entry.addField(fld);
            } else {
                throw new IllegalStateException("Unexpected token: " + lexer.peek());
            }
        }

        return entry;
    }

    private FieldEntry parseField(ParseState state, Lexer lexer) {
        FieldEntry fld = new FieldEntry(state.getSourceSet());
        fld.setAccessModifier(AccessModifier.PACKAGE_PRIVATE);
        while (true) {
            String next = lexer.expect(IDENTIFIER).getString();
            if ("final".equals(next)) {
                fld.setFinal(true);
            } else if ("static".equals(next)) {
                fld.setStatic(true);
            } else if ("volatile".equals(next)) {
                fld.setVolatile(true);
            } else if ("transient".equals(next)) {
                fld.setTransient(true);
            } else if ("public".equals(next)) {
                fld.setAccessModifier(AccessModifier.PUBLIC);
            } else if ("private".equals(next)) {
                fld.setAccessModifier(AccessModifier.PRIVATE);
            } else if ("protected".equals(next)) {
                fld.setAccessModifier(AccessModifier.PROTECTED);
            } else {
                break;
            }
        }
        // TODO handle fully qualified types
        String type = lexer.expect(IDENTIFIER).getString();
        // TODO parse generic
        String name = lexer.expect(IDENTIFIER).getString();
        fld.setName(name);
        if (lexer.peekType() == EQUALS) {
            // TODO parse value
        }
        lexer.expect(SEMICOLON);
        return fld;
    }

    private MethodEntry parseMethod(ParseState state, Lexer lexer) {
        MethodEntry mth = new MethodEntry(state.getSourceSet());
        mth.setAccessModifier(AccessModifier.PACKAGE_PRIVATE);
        while (true) {
            String next = lexer.expect(IDENTIFIER).getString();
            if ("final".equals(next)) {
                mth.setFinal(true);
            } else if ("static".equals(next)) {
                mth.setStatic(true);
            } else if ("strictfp".equals(next)) {
                mth.setStrictFp(true);
            } else if ("abstract".equals(next)) {
                mth.setAbstract(true);
            } else if ("public".equals(next)) {
                mth.setAccessModifier(AccessModifier.PUBLIC);
            } else if ("private".equals(next)) {
                mth.setAccessModifier(AccessModifier.PRIVATE);
            } else if ("protected".equals(next)) {
                mth.setAccessModifier(AccessModifier.PROTECTED);
            } else {
                break;
            }
        }
        String name = lexer.expect(IDENTIFIER).getString();
        mth.setName(name);
        return mth;
    }

}
