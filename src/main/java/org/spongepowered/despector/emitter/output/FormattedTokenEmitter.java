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

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeArgument;
import org.spongepowered.despector.ast.generic.TypeParameter;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.TypeVariableSignature;
import org.spongepowered.despector.ast.generic.VoidTypeSignature;
import org.spongepowered.despector.emitter.EmitterSet;
import org.spongepowered.despector.emitter.format.EmitterFormat;
import org.spongepowered.despector.util.TypeHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FormattedTokenEmitter {

    private final EmitterSet set;
    private EmitterFormat format = EmitterFormat.defaults();

    private Writer writer;
    private TokenEmitterType state = TokenEmitterType.CLASS;
    private Deque<TokenEmitterType> state_stack = new ArrayDeque<>();
    private Set<String> imports = new HashSet<>();

    private int indentation;

    public FormattedTokenEmitter(EmitterSet set) {
        this.set = set;
    }

    public EmitterFormat getFormat() {
        return this.format;
    }

    public void setFormat(EmitterFormat format) {
        this.format = format;
    }

    public void emit(Writer output, List<EmitterToken> tokens) throws IOException {
        this.writer = output;
        this.state = TokenEmitterType.CLASS;
        this.state_stack.clear();

        for (EmitterToken token : tokens) {
            switch (token.getType()) {
            case PACKAGE:
                printString("package ");
                String pkg = (String) token.getToken();
                pkg = pkg.substring(0, pkg.lastIndexOf('/')).replace('/', '.');
                printString(pkg);
                printString(";");
                newLine();
                break;
            case PUSH_EMITTER_TYPE:
                pushState((TokenEmitterType) token.getToken());
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
                break;
            case RAW:
            case NAME:
            case ACCESS:
            case SPECIAL:
            case MODIFIER:
            case TYPE:
            case SUPERCLASS:
            case INTERFACE:
            case KEYWORD:
            case GENERIC_PARAMS:
            case BLOCK_START:
            case BLOCK_END:
            case LEFT_PAREN:
            case RIGHT_PAREN:
            case ARG_START:
            case LEFT_BRACKET:
            case RIGHT_BRACKET:
            case ENUM_CONSTANT:
            case COMMENT:
            case BLOCK_COMMENT:
            case FIELD_INITIALIZER:
            case ARRAY_INITIALIZER_START:
            case ARRAY_INITIALIZER_END:
            case STATEMENT_END:
            case DOT:
            case EQUALS:
            case OPERATOR_EQUALS:
            case OPERATOR:
            case FOR_EACH:
            case FOR_SEPARATOR:
            case TERNARY_IF:
            case TERNARY_ELSE:
            case LAMBDA:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case STRING:
            case BOOLEAN:
            case CHAR:
            case WHEN_CASE:
                printString(token.getType().name() + " " + token.getToken() + "\n");
                printIndentation();
                break;
            }
        }
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
    }

    private void newLine() {
        printString("\n");
    }

    private void printString(String str) {
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
        if (type.indexOf('/') != -1) {
            String pkg = type.substring(0, type.lastIndexOf('/'));
            if (this.imports.contains(pkg)) {
                printString(type.substring(pkg.length() + 1));
                return;
            }
        }
        printString(type.replace('/', '.'));
    }

    private void addImport(Object obj) {
        if (obj instanceof String) {
            String type = (String) obj;
            if (type.startsWith("[")) {
                addImport(type.substring(1));
                return;
            }
            if (type.indexOf('/') != -1) {
                String pkg = type.substring(0, type.lastIndexOf('/'));
                this.imports.add(pkg);
            }
        } else if (obj instanceof VoidTypeSignature) {
            return;
        } else if (obj instanceof Iterable) {
            for (Object o : (Iterable<?>) obj) {
                addImport(o);
            }
        } else {
            throw new IllegalStateException(obj.getClass().getSimpleName());
        }
    }

    private void generateImports(List<EmitterToken> tokens) {
        // TODO skip implicit imports
        for (EmitterToken token : tokens) {
            if (token.getType() == TokenType.TYPE) {
                addImport(token.getToken());
            } else if (token.getType() == TokenType.GENERIC_PARAMS) {
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

}
