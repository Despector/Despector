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
package org.spongepowered.despector.ast.transform.cleanup;

import org.spongepowered.despector.ast.AccessModifier;
import org.spongepowered.despector.ast.members.MethodEntry;
import org.spongepowered.despector.ast.transform.TypeTransformer;
import org.spongepowered.despector.ast.type.ClassEntry;
import org.spongepowered.despector.ast.type.TypeEntry;

/**
 * Ensures utility classes (no instance fields or methods) are declared as final
 * with a private constructor.
 */
public class UtilityClassNoInstance implements TypeTransformer {

    @Override
    public void transform(TypeEntry type) {
        if (!(type instanceof ClassEntry)) {
            return;
        }
        if (type.getFieldCount() != 0) {
            return;
        }
        if (type.getMethodCount() != 1) {
            return;
        }
        MethodEntry ctor = type.getMethod("<init>");
        if (!ctor.getParamTypes().isEmpty() || ctor.getInstructions().getStatements().size() != 2) {
            return;
        }
        ctor.setAccessModifier(AccessModifier.PRIVATE);
        type.setFinal(true);
    }

}
