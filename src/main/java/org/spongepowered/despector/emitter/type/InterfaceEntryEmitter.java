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
package org.spongepowered.despector.emitter.type;

import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.members.FieldEntry;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.type.InterfaceEntry;
import org.spongepowered.despector.emitter.AstEmitter;
import org.spongepowered.despector.emitter.EmitterContext;

public class InterfaceEntryEmitter implements AstEmitter<InterfaceEntry> {

    @Override
    public boolean emit(EmitterContext ctx, InterfaceEntry type) {
        String name = type.getName().replace('/', '.');
        if (name.indexOf('.') != -1) {
            name = name.substring(name.lastIndexOf('.') + 1, name.length());
        }
        name = name.replace('$', '.');
        if (!(name.contains(".") && type.getAccessModifier() == AccessModifier.PUBLIC)) {
            ctx.printString(type.getAccessModifier().asString());
            if (type.getAccessModifier() != AccessModifier.PACKAGE_PRIVATE) {
                ctx.printString(" ");
            }
        }
        ctx.printString("interface ");
        ctx.printString(name);
        if (!type.getInterfaces().isEmpty()) {
            ctx.printString(" extends ");
            for (int i = 0; i < type.getInterfaces().size(); i++) {
                ctx.emitType(type.getInterfaces().get(i));
                if (i < type.getInterfaces().size() - 1) {
                    if (ctx.getFormat().insert_space_before_comma_in_superinterfaces) {
                        ctx.printString(" ");
                    }
                    ctx.printString(",");
                    if (ctx.getFormat().insert_space_after_comma_in_superinterfaces) {
                        ctx.printString(" ");
                    }
                }
            }
        }
        if (ctx.getFormat().insert_space_before_opening_brace_in_type_declaration) {
            ctx.printString(" ");
        }
        ctx.printString("{\n\n");
        ctx.indent();
        if (!type.getStaticFields().isEmpty()) {
            boolean at_least_one = false;
            for (FieldEntry field : type.getStaticFields()) {
                if (field.isSynthetic()) {
                    continue;
                }
                at_least_one = true;
                ctx.printIndentation();
                ctx.emit(field);
                ctx.printString(";\n");
            }
            if (at_least_one) {
                ctx.printString("\n");
            }
        }
        if (!type.getStaticMethods().isEmpty()) {
            for (MethodEntry mth : type.getStaticMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                ctx.emit(mth);
                ctx.printString("\n\n");
            }
        }
        if (!type.getMethods().isEmpty()) {
            for (MethodEntry mth : type.getMethods()) {
                if (mth.isSynthetic()) {
                    continue;
                }
                // TODO need something for emitting 'default' for default
                // methods
                ctx.emit(mth);
                ctx.printString("\n\n");
            }
        }
        ctx.dedent();
        ctx.printString("}\n");
        return true;
    }

}
