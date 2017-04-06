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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FormattedTokenEmitter {

    private EmitterFormat format = EmitterFormat.defaults();

    private Writer writer;
    private TypeEntry type;
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
        this.type = type;
        
        for(int i = 0; i < tokens.size(); i++) {
            
        }
        
    }

    void indent() {
        this.indentation++;
    }

    void dedent() {
        this.indentation--;
    }

    void printIndentation() {
        for (int i = 0; i < this.indentation; i++) {
            printString("    ");
        }
    }

    void newLine() {
        printString("\n");
    }

    void printString(char str) {
        try {
            this.writer.write(str);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void printString(String str) {
        try {
            this.writer.write(str);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void printType(String desc) {
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
        } else if (obj instanceof TypeArgument) {
            addImport(((TypeArgument) obj).getSignature());
        } else if (obj instanceof TypeParameter) {
            TypeParameter param = (TypeParameter) obj;
            if (param.getClassBound() != null) {
                addImport(param.getClassBound());
            }
            for (TypeSignature sig : param.getInterfaceBounds()) {
                addImport(sig);
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

}
