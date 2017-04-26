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
package org.spongepowered.test.util;

import org.junit.Assert;
import org.spongepowered.despector.ast.Annotation;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.type.FieldEntry;
import org.spongepowered.despector.ast.type.MethodEntry;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.ast.type.TypeEntry.InnerClassInfo;

public class AstAsserts {

    public static void assertEquals(TypeEntry a, TypeEntry b) {
        Assert.assertEquals(a.getLanguage(), b.getLanguage());
        Assert.assertEquals(a.getAccessModifier(), b.getAccessModifier());
        Assert.assertEquals(a.isSynthetic(), b.isSynthetic());
        Assert.assertEquals(a.isFinal(), b.isFinal());
        Assert.assertEquals(a.isAbstract(), b.isAbstract());
        Assert.assertEquals(a.isDeprecated(), b.isDeprecated());
        Assert.assertEquals(a.getName(), b.getName());
        Assert.assertEquals(a.getInterfaces().size(), b.getInterfaces().size());
        for (String i : a.getInterfaces()) {
            if (!b.getInterfaces().contains(i)) {
                Assert.fail();
            }
        }
        for (FieldEntry fld : a.getFields()) {
            FieldEntry other = b.getField(fld.getName());
            if (other == null) {
                Assert.fail();
            }
            assertEquals(fld, other);
        }
        for (FieldEntry fld : a.getStaticFields()) {
            FieldEntry other = b.getStaticField(fld.getName());
            if (other == null) {
                Assert.fail();
            }
            assertEquals(fld, other);
        }
        for (MethodEntry fld : a.getStaticMethods()) {
            MethodEntry other = b.getMethod(fld.getName());
            if (other == null) {
                Assert.fail();
            }
            assertEquals(fld, other);
        }
        for (MethodEntry fld : a.getMethods()) {
            MethodEntry other = b.getMethod(fld.getName());
            if (other == null) {
                Assert.fail();
            }
            assertEquals(fld, other);
        }
        Assert.assertEquals(a.getSignature(), b.getSignature());
        for (Annotation anno : a.getAnnotations()) {
            Annotation other = b.getAnnotation(anno.getType());
            if (other == null) {
                Assert.fail();
            }
            assertEquals(anno, other);
        }
        for (InnerClassInfo inner : a.getInnerClasses()) {
            InnerClassInfo other = b.getInnerClassInfo(inner.getName());
            if (other == null) {
                Assert.fail();
            }
            Assert.assertEquals(inner, other);
        }
    }

    public static void assertEquals(FieldEntry a, FieldEntry b) {
        Assert.assertEquals(a.getAccessModifier(), b.getAccessModifier());
        Assert.assertEquals(a.getOwner(), b.getOwner());
        Assert.assertEquals(a.getType(), b.getType());
        Assert.assertEquals(a.getName(), b.getName());
        Assert.assertEquals(a.isFinal(), b.isFinal());
        Assert.assertEquals(a.isStatic(), b.isStatic());
        Assert.assertEquals(a.isSynthetic(), b.isSynthetic());
        Assert.assertEquals(a.isVolatile(), b.isVolatile());
        Assert.assertEquals(a.isTransient(), b.isTransient());
        Assert.assertEquals(a.isDeprecated(), b.isDeprecated());
        for (Annotation anno : a.getAnnotations()) {
            Annotation other = b.getAnnotation(anno.getType());
            if (other == null) {
                Assert.fail();
            }
            assertEquals(anno, other);
        }
    }

    public static void assertEquals(MethodEntry a, MethodEntry b) {
        Assert.assertEquals(a.getAccessModifier(), b.getAccessModifier());
        Assert.assertEquals(a.getOwner(), b.getOwner());
        Assert.assertEquals(a.getName(), b.getName());
        Assert.assertEquals(a.getDescription(), b.getDescription());
        Assert.assertEquals(a.isAbstract(), b.isAbstract());
        Assert.assertEquals(a.isFinal(), b.isFinal());
        Assert.assertEquals(a.isStatic(), b.isStatic());
        Assert.assertEquals(a.isSynthetic(), b.isSynthetic());
        Assert.assertEquals(a.isBridge(), b.isBridge());
        Assert.assertEquals(a.isSynchronized(), b.isSynchronized());
        Assert.assertEquals(a.isNative(), b.isNative());
        Assert.assertEquals(a.isVarargs(), b.isVarargs());
        Assert.assertEquals(a.isStrictFp(), b.isStrictFp());
        Assert.assertEquals(a.isDeprecated(), b.isDeprecated());
        Assert.assertEquals(a.getMethodSignature(), b.getMethodSignature());
        Assert.assertEquals(a.getAnnotationValue(), b.getAnnotationValue());

        assertEquals(a.getInstructions(), b.getInstructions());

        for (Annotation anno : a.getAnnotations()) {
            Annotation other = b.getAnnotation(anno.getType());
            if (other == null) {
                Assert.fail();
            }
            assertEquals(anno, other);
        }
    }

    public static void assertEquals(Annotation a, Annotation b) {
        Assert.assertEquals(a.getType(), b.getType());
        Assert.assertEquals(a.getValues(), b.getValues());
    }

    public static void assertEquals(StatementBlock a, StatementBlock b) {
        if (a.getStatementCount() != b.getStatementCount()) {
            Assert.fail();
        }
        for (int i = 0; i < a.getStatementCount(); i++) {
            Assert.assertEquals(a.getStatement(i), b.getStatement(i));
        }
    }

}
