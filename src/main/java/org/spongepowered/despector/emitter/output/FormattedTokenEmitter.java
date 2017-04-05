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
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeArgument;
import org.spongepowered.despector.ast.generic.TypeParameter;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.TypeVariableSignature;
import org.spongepowered.despector.ast.generic.VoidTypeSignature;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.format.EmitterFormat;
import org.spongepowered.despector.util.TypeHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FormattedTokenEmitter {

    private EmitterFormat format = EmitterFormat.defaults();

    private Writer writer;
    private TypeEntry type;
    private TokenEmitterType state = TokenEmitterType.CLASS;
    private IndentationState indentation_state;
    private Deque<TokenEmitterType> state_stack = new ArrayDeque<>();
    private List<String> implicit_imports = new ArrayList<>();
    private Set<String> imports = new HashSet<>();

    private int indentation;

    public FormattedTokenEmitter() {
        this.implicit_imports.add("java/lang/");
    }

    public EmitterFormat getFormat() {
        return this.format;
    }

    public void setFormat(EmitterFormat format) {
        this.format = format;
    }

    public void emit(Writer output, List<EmitterToken> tokens, TypeEntry type) {
        this.writer = output;
        this.state = TokenEmitterType.CLASS;
        this.state_stack.clear();
        this.type = type;
        this.indentation_state = IndentationState.NEW_LINE;

        for (int i = 0; i < tokens.size(); i++) {
            EmitterToken token = tokens.get(i);
            EmitterToken next = i < tokens.size() - 1 ? tokens.get(i + 1) : null;
            switch (token.getType()) {
            case PACKAGE:
                printString("package ");
                String pkg = (String) token.getToken();
                pkg = pkg.substring(0, pkg.lastIndexOf('/')).replace('/', '.');
                printString(pkg);
                printString(";");
                newLine();
                newLine();
                break;
            case ACCESS:
                AccessModifier acc = (AccessModifier) token.getToken();
                printString(acc.asString());
                if (!acc.asString().isEmpty()) {
                    printString(" ");
                }
                break;
            case SPECIAL:
            case MODIFIER:
                printString((String) token.getToken());
                printString(" ");
                break;
            case SUPERCLASS:
                printType((String) token.getToken());
                printString(" ");
                break;
            case INTERFACE:
                EmitterToken prev = i > 0 ? tokens.get(i - 1) : null;
                if (checkType(prev, TokenType.INTERFACE)) {
                    printString(", ");
                }
                printType((String) token.getToken());
                if (checkType(next, TokenType.BLOCK_START)) {
                    printString(" ");
                }
                break;
            case TYPE:
                if (token.getToken() instanceof String) {
                    printType((String) token.getToken());
                } else if (token.getToken() instanceof TypeSignature) {
                    emitTypeSignature((TypeSignature) token.getToken());
                } else {
                    throw new IllegalStateException(token.getToken().toString());
                }
                if (!checkType(next, TokenType.LEFT_PAREN)) {
                    printString(" ");
                }
                break;
            case NAME:
                printString((String) token.getToken());
                if (!checkType(next, TokenType.LEFT_PAREN) && !checkType(next, TokenType.DOT) && !checkType(next, TokenType.LEFT_BRACKET)) {
                    printString(" ");
                }
                break;
            case RETURN:
                printString((String) token.getToken());
                if (!checkType(next, TokenType.STATEMENT_END)) {
                    printString(" ");
                }
                break;
            case BLOCK_START:
                printString("{");
                newLine();
                if (this.state == TokenEmitterType.CLASS || this.state == TokenEmitterType.ENUM || this.state == TokenEmitterType.INTERFACE) {
                    newLine();
                }
                indent();
                if (next.getType() != TokenType.BLOCK_END) {
                    printIndentation();
                }
                break;
            case BLOCK_END:
                dedent();
                printIndentation();
                printString("}");
                newLine();
                newLine();
                break;
            case LEFT_PAREN:
            case LEFT_BRACKET:
            case DOT:
            case RAW:
            case RIGHT_BRACKET:
                printString((String) token.getToken());
                break;
            case RIGHT_PAREN:
                printString((String) token.getToken());
                if (checkType(next, TokenType.BLOCK_START)) {
                    printString(" ");
                }
                break;
            case ARG_SEPARATOR:
                printString(",");
                printString(' ');
                break;
            case STATEMENT_END:
                printString((String) token.getToken());
                newLine();
                EmitterToken far = i < tokens.size() - 2 ? tokens.get(i + 2) : null;
                if (checkType(far, TokenType.PUSH_EMITTER_TYPE) && ((TokenEmitterType) far.getToken()) == TokenEmitterType.METHOD) {
                    newLine();
                }
                if (!checkType(next, TokenType.BLOCK_END)) {
                    printIndentation();
                }
                break;
            case EQUALS:
                if (this.indentation_state != IndentationState.AFTER_SPACE) {
                    printString(" ");
                }
                printString((String) token.getToken());
                printString(" ");
                break;
            case DOUBLE:
            case BOOLEAN:
            case INT:
                printString(token.getToken().toString());
                break;
            case LONG:
                printString(token.getToken().toString());
                printString('L');
                break;
            case FLOAT:
                printString(token.getToken().toString());
                printString('F');
                break;
            case STRING:
                printString('"');
                printString((String) token.getToken());
                printString('"');
                break;
            case CHAR:
                printString(' ');
                printString(((Character) token.getToken()).charValue());
                printString(' ');
                break;
            case PUSH_EMITTER_TYPE:
                pushState((TokenEmitterType) token.getToken());
                if (this.indentation_state == IndentationState.END_OF_LINE) {
                    newLine();
                    printIndentation();
                } else if (this.indentation_state == IndentationState.NEW_LINE) {
                    printIndentation();
                }
                break;
            case POP_EMITTER_TYPE:
                popState();
                break;
            case INSERT_IMPORTS:
                generateImports(tokens);
                // TODO sort, separate into groups
                for (String s : this.imports) {
                    printString("import ");
                    printString(s.replace('/', '.'));
                    printString(";");
                    newLine();
                }
                newLine();
                break;
            case KEYWORD:
            case GENERIC_PARAMS:
            case ENUM_CONSTANT:
            case COMMENT:
            case BLOCK_COMMENT:
            case ARRAY_INITIALIZER_START:
            case ARRAY_INITIALIZER_END:
            case OPERATOR_EQUALS:
            case OPERATOR:
            case FOR_EACH:
            case FOR_SEPARATOR:
            case TERNARY_IF:
            case TERNARY_ELSE:
            case LAMBDA:
            case WHEN_CASE:
            default:
                printString(token.getType().name() + " " + token.getToken() + "\n");
                printIndentation();
                break;
            }
        }
    }

    private boolean checkType(EmitterToken token, TokenType type) {
        return token != null && token.getType() == type;
    }

    private void indent() {
        this.indentation++;
    }

    private void dedent() {
        this.indentation--;
    }

    private void printIndentation() {
        for (int i = 0; i < this.indentation; i++) {
            printString("    ");
        }
        this.indentation_state = IndentationState.NEW_LINE_INDENTED;
    }

    private void newLine() {
        printString("\n");
        this.indentation_state = IndentationState.NEW_LINE;
    }

    private void printString(char str) {
        if (str == ' ') {
            this.indentation_state = IndentationState.AFTER_SPACE;
        } else {
            this.indentation_state = IndentationState.END_OF_LINE;
        }
        try {
            this.writer.write(str);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void printString(String str) {
        if (str.endsWith(" ")) {
            this.indentation_state = IndentationState.AFTER_SPACE;
        } else {
            this.indentation_state = IndentationState.END_OF_LINE;
        }
        try {
            this.writer.write(str);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void pushState(TokenEmitterType type) {
        this.state_stack.push(this.state);
        this.state = type;
    }

    private void popState() {
        this.state = this.state_stack.pop();
    }

    private void printType(String desc) {
        String type = TypeHelper.descToType(desc);
        if (this.imports.contains(type) || type.equals(this.type.getName())) {
            printString(type.substring(type.lastIndexOf('/') + 1).replace('$', '.'));
            return;
        }
        if (type.startsWith(this.type.getName() + "$")) {
            printString(type.substring(type.lastIndexOf('$') + 1));
            return;
        }
        for (String implicit : this.implicit_imports) {
            if (type.startsWith(implicit)) {
                printString(type.substring(type.lastIndexOf('/') + 1).replace('$', '.'));
                return;
            }
        }
        printString(type.replace('/', '.').replace('$', '.'));
    }

    private void addImport(Object obj) {
        if (obj instanceof String) {
            String type = (String) obj;
            if (type.startsWith("[")) {
                addImport(type.substring(1));
                return;
            }
            if (!type.startsWith("L")) {
                return;
            }
            String name = TypeHelper.descToType(type);
            if (name.equals(this.type.getName())) {
                return;
            }
            if (name.startsWith(this.type.getName() + "$")) {
                return;
            }
            for (String implicit : this.implicit_imports) {
                if (name.startsWith(implicit)) {
                    return;
                }
            }
            if (name.indexOf('$') != -1) {
                String pkg = name.substring(0, name.indexOf('$'));
                this.imports.add(pkg);
            } else {
                this.imports.add(name);
            }
        } else if (obj instanceof ClassTypeSignature) {
            ClassTypeSignature type = (ClassTypeSignature) obj;
            addImport(type.getDescriptor());
            for (TypeArgument arg : type.getArguments()) {
                addImport(arg.getSignature());
            }
        } else if (obj instanceof Iterable) {
            for (Object o : (Iterable<?>) obj) {
                addImport(o);
            }
        } else if (obj instanceof VoidTypeSignature || obj instanceof TypeVariableSignature) {
            return;
        } else {
            throw new IllegalStateException(obj.getClass().getSimpleName());
        }
    }

    private void generateImports(List<EmitterToken> tokens) {
        for (EmitterToken token : tokens) {
            if (token.getType() == TokenType.TYPE || token.getType() == TokenType.GENERIC_PARAMS || token.getType() == TokenType.INTERFACE
                    || token.getType() == TokenType.SUPERCLASS) {
                addImport(token.getToken());
            }
        }
    }

    public void emitTypeParameter(TypeParameter param) {
        printString(param.getIdentifier());
        boolean had_superclass = false;
        superclass: if (param.getClassBound() != null) {
            if (param.getClassBound() instanceof ClassTypeSignature) {
                ClassTypeSignature cls = (ClassTypeSignature) param.getClassBound();
                if (cls.getType().equals("Ljava/lang/Object;")) {
                    break superclass;
                }
            }
            printString(" extends ");
            emitTypeSignature(param.getClassBound());
            had_superclass = true;
        }
        for (TypeSignature ibound : param.getInterfaceBounds()) {
            if (!had_superclass) {
                printString(" extends ");
            } else {
                printString(" & ");
            }
            emitTypeSignature(ibound);
        }
    }

    public void emitTypeSignature(TypeSignature sig) {
        if (sig instanceof TypeVariableSignature) {
            String desc = ((TypeVariableSignature) sig).getIdentifier();
            printString(desc.substring(1, desc.length() - 1));
        } else if (sig instanceof ClassTypeSignature) {
            ClassTypeSignature cls = (ClassTypeSignature) sig;
            int array_depth = 0;
            String type = cls.getType();
            while (type.startsWith("[")) {
                array_depth++;
                type = type.substring(1);
            }
            printType(type);
            emitTypeArguments(cls.getArguments());
            for (int i = 0; i < array_depth; i++) {
                printString("[]");
            }
        } else if (sig instanceof VoidTypeSignature) {
            printString("void");
        }
    }

    public void emitTypeParameters(List<TypeParameter> parameters) {
        if (parameters.isEmpty()) {
            return;
        }
        printString("<");
        boolean first = true;
        for (TypeParameter param : parameters) {
            if (!first) {
                printString(", ");
            }
            first = false;
            emitTypeParameter(param);
        }
        printString(">");

    }

    public void emitTypeArgument(TypeArgument arg) {
        switch (arg.getWildcard()) {
        case NONE:
            emitTypeSignature(arg.getSignature());
            break;
        case EXTENDS:
            printString("? extends ");
            emitTypeSignature(arg.getSignature());
            break;
        case SUPER:
            printString("? super ");
            emitTypeSignature(arg.getSignature());
            break;
        case STAR:
            printString("?");
            break;
        default:
            break;
        }
    }

    public void emitTypeArguments(List<TypeArgument> arguments) {
        if (arguments.isEmpty()) {
            return;
        }
        printString("<");
        boolean first = true;
        for (TypeArgument param : arguments) {
            if (!first) {
                printString(", ");
            }
            first = false;
            emitTypeArgument(param);
        }
        printString(">");
    }

    private static enum IndentationState {
        END_OF_LINE,
        AFTER_SPACE,
        NEW_LINE,
        NEW_LINE_INDENTED,
    }

}
