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
package org.spongepowered.test.ast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.spongepowered.despector.ast.generic.ClassSignature;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.GenericClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeArgument;
import org.spongepowered.despector.ast.generic.TypeParameter;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.TypeVariableSignature;
import org.spongepowered.despector.ast.generic.WildcardType;
import org.spongepowered.despector.util.SignatureParser;

public class SignatureParserTest {

    @Test
    public void testBasic() {
        String sig = "Ljava/lang/Object;";
        ClassSignature cls = SignatureParser.parse(sig);
        assertEquals(0, cls.getParameters().size());
        assertEquals("Ljava/lang/Object;", cls.getSuperclassSignature().getType());
        assertEquals(0, cls.getInterfaceSignatures().size());
    }

    @Test
    public void testBasic2() {
        String sig = "Ljava/lang/Object<TT;>;";
        ClassSignature cls = SignatureParser.parse(sig);
        assertEquals(0, cls.getParameters().size());
        assertEquals("Ljava/lang/Object;", cls.getSuperclassSignature().getType());
        assertEquals(1, cls.getSuperclassSignature().getArguments().size());
        assertEquals(TypeVariableSignature.class, cls.getSuperclassSignature().getArguments().get(0).getSignature().getClass());
        TypeVariableSignature param = (TypeVariableSignature) cls.getSuperclassSignature().getArguments().get(0).getSignature();
        assertEquals("TT;", param.getIdentifier());
        assertEquals(0, cls.getInterfaceSignatures().size());
    }

    @Test
    public void testBasic3() {
        String sig = "<T:Ljava/lang/Object;E:Ljava/lang/Number;>Ljava/lang/Object;Ljava/util/Comparator<TE;>;";
        ClassSignature cls = SignatureParser.parse(sig);
        assertEquals(2, cls.getParameters().size());
        TypeParameter param1 = cls.getParameters().get(0);
        assertEquals("T", param1.getIdentifier());
        assertTrue(param1.getClassBound() instanceof GenericClassTypeSignature);
        GenericClassTypeSignature param1_classbound = (GenericClassTypeSignature) param1.getClassBound();
        assertEquals("Ljava/lang/Object;", param1_classbound.getType());
    }

    @Test
    public void testBasic4() {
        String sig = "Ljava/util/List<[I>;";
        TypeSignature cls = SignatureParser.parseFieldTypeSignature(sig);
        assertEquals(GenericClassTypeSignature.class, cls.getClass());
        GenericClassTypeSignature g = (GenericClassTypeSignature) cls;
        assertEquals("Ljava/util/List;", g.getDescriptor());
        assertEquals(1, g.getArguments().size());
        TypeArgument p = g.getArguments().get(0);
        assertEquals(WildcardType.NONE, p.getWildcard());
        TypeSignature ps = p.getSignature();
        assertEquals(ClassTypeSignature.class, ps.getClass());
        assertEquals("[I", ps.getDescriptor());
    }

}
