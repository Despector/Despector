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
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.GenericClassTypeSignature;
import org.spongepowered.despector.ast.generic.MethodSignature;
import org.spongepowered.despector.ast.generic.TypeArgument;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.VoidTypeSignature;
import org.spongepowered.despector.ast.generic.WildcardType;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.var.InstanceFieldAccess;
import org.spongepowered.despector.ast.insn.var.StaticFieldAccess;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.ast.stmt.invoke.InvokeStatement;
import org.spongepowered.despector.ast.stmt.invoke.StaticMethodInvoke;
import org.spongepowered.despector.ast.stmt.misc.Return;
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
import org.spongepowered.despector.source.parse.TokenType;

import java.util.ArrayList;
import java.util.List;

public class JavaParser implements SourceParser {

    private final ExpressionParser expr_parser = new ExpressionParser(this);

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
        state.setCurrentType(entry);
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
            while (lexer.peekType() == IDENTIFIER || lexer.peekType() == LESS || lexer.peekType() == GREATER || lexer.peekType() == COMMA
                    || lexer.peekType() == DOT) {
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

        // TODO add field initializers to init/clinit

        return entry;
    }

    private FieldEntry parseField(ParseState state, Lexer lexer) {
        FieldEntry fld = new FieldEntry(state.getSourceSet());
        fld.setAccessModifier(AccessModifier.PACKAGE_PRIVATE);
        while (true) {
            int mark = lexer.mark();
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
                lexer.reset(mark);
                break;
            }
        }
        TypeSignature type = parseType(state, lexer);
        fld.setType(type);
        String name = lexer.expect(IDENTIFIER).getString();
        fld.setName(name);
        if (lexer.peekType() == EQUALS) {
            lexer.pop();
            Instruction val = parseInstruction(state, lexer);
            state.setFieldInitializer(fld, val);
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
        Locals locals = new Locals(true);
        state.setCurrentLocals(locals);
        String desc = "(";
        lexer.expect(TokenType.PAREN_LEFT);
        int i = 0;
        MethodSignature sig = new MethodSignature();
        while (lexer.peekType() != PAREN_RIGHT) {
            TypeSignature param_type = parseType(state, lexer);
            String param_name = lexer.expect(IDENTIFIER).getString();
            Local l = locals.getLocal(i);
            l.addInstance(new LocalInstance(l, param_name, param_type, -1, -1));
            desc += param_type.getDescriptor();
            sig.getParameters().add(param_type);
        }
        lexer.expect(PAREN_RIGHT);
        desc += ")";
        desc += "V";
        sig.setReturnType(VoidTypeSignature.VOID);
        mth.setMethodSignature(sig);
        mth.setLocals(locals);
        mth.setDescription(desc);
        mth.setOwner(state.getCurrentType().getName());
        lexer.expect(BRACE_LEFT);
        StatementBlock block = new StatementBlock(StatementBlock.Type.METHOD);
        while (lexer.peekType() != BRACE_RIGHT) {
            Statement stmt = parseStatement(state, lexer);
            block.append(stmt);
        }
        if (block.size() == 0 || !(block.get(block.size() - 1) instanceof Return)) {
            block.append(new Return());
        }
        mth.setInstructions(block);
        lexer.expect(BRACE_RIGHT);
        return mth;
    }

    private TypeArgument parseTypeArgument(ParseState state, Lexer lexer) {
        WildcardType wild = WildcardType.NONE;
        if (lexer.peekType() == QUESTION) {
            lexer.pop();
            if (lexer.peekType() == TokenType.IDENTIFIER) {
                int mark = lexer.mark();
                String next = lexer.pop().getString();
                if ("extends".equals(next)) {
                    wild = WildcardType.EXTENDS;
                } else if ("super".equals(next)) {
                    wild = WildcardType.SUPER;
                } else {
                    lexer.reset(mark);
                }
            } else if (lexer.peekType() == TokenType.GREATER || lexer.peekType() == COMMA) {
                return new TypeArgument(WildcardType.STAR, null);
            }
        }

        TypeSignature sig = parseType(state, lexer);
        return new TypeArgument(wild, sig);
    }

    TypeSignature parseType(ParseState state, Lexer lexer) {
        if (lexer.peekType() == TokenType.IDENTIFIER) {
            String next = lexer.expect(IDENTIFIER).getString();
            // TODO check if token is a type parameter in scope
            String sig = null;
            List<TypeArgument> params = null;
            if ("byte".equals(next)) {
                sig = "B";
            } else if ("short".equals(next)) {
                sig = "S";
            } else if ("int".equals(next)) {
                sig = "I";
            } else if ("long".equals(next)) {
                sig = "J";
            } else if ("float".equals(next)) {
                sig = "F";
            } else if ("double".equals(next)) {
                sig = "D";
            } else if ("char".equals(next)) {
                sig = "C";
            } else if ("boolean".equals(next)) {
                sig = "Z";
            } else {
                sig = next;
                while (lexer.peekType() == DOT) {
                    lexer.expect(DOT);
                    sig += ".";
                    sig += lexer.expect(IDENTIFIER).getString();
                }
                sig = state.getSource().resolveType(sig);
                if (sig == null) {
                    // assume package local
                    sig = state.getSource().getPackage().replace('/', '.') + "/" + sig;
                }
                if (lexer.peekType() == LESS) {
                    params = new ArrayList<>();
                    lexer.pop();
                    boolean first = true;
                    while (lexer.peekType() != GREATER) {
                        if (!first) {
                            lexer.expect(COMMA);
                        } else {
                            first = false;
                        }
                        TypeArgument param = parseTypeArgument(state, lexer);
                        params.add(param);
                    }
                    lexer.expect(GREATER);
                }
                // TODO generics
                sig = "L" + sig + ";";
            }
            while (lexer.peekType() == BRACKET_LEFT) {
                lexer.pop();
                lexer.expect(BRACKET_RIGHT);
                sig = "[" + sig;
            }
            if (params != null) {
                GenericClassTypeSignature generic = new GenericClassTypeSignature(sig);
                generic.getArguments().addAll(params);
                return generic;
            }
            return ClassTypeSignature.of(sig);
        }
        throw new IllegalStateException(lexer.peek().toString());
    }

    private Statement parseStatement(ParseState state, Lexer lexer) {
        Instruction callee = null;
        String static_type = null;
        String accu = "";
        while (true) {
            String next = lexer.expect(IDENTIFIER).getString();
            if (callee == null) {
                if (static_type == null) {
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
                            Instruction arg = parseInstruction(state, lexer);
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
                            break;
                        }
                    } else if (lexer.peekType() == EQUALS) {
                        Instruction val = parseInstruction(state, lexer);
                        return new StaticFieldAssignment(next, null, "L" + static_type + ";", val);
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
                        Instruction arg = parseInstruction(state, lexer);
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
                        break;
                    }
                } else if (lexer.peekType() == EQUALS) {
                    Instruction val = parseInstruction(state, lexer);
                    return new InstanceFieldAssignment(next, null, "incomplete", callee, val);
                } else {
                    callee = new InstanceFieldAccess(next, null, "incomplete", callee);
                }
            }
            lexer.expect(DOT);
        }
        if (callee != null) {
            return new InvokeStatement(callee);
        }
        throw new IllegalStateException();
    }

    private Instruction parseInstruction(ParseState state, Lexer lexer) {
        return this.expr_parser.parseExpression(state, lexer);
    }

}
