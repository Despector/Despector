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
package org.spongepowered.test.serialization;

import org.junit.Assert;
import org.junit.Test;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.GenericClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeArgument;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.TypeVariableSignature;
import org.spongepowered.despector.ast.generic.VoidTypeSignature;
import org.spongepowered.despector.ast.generic.WildcardType;
import org.spongepowered.despector.util.serialization.AstLoader;
import org.spongepowered.despector.util.serialization.MessagePacker;
import org.spongepowered.despector.util.serialization.MessageUnpacker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SignatureSerializationTest {

    @Test
    public void testVoid() throws IOException {
        TypeSignature sig = VoidTypeSignature.VOID;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MessagePacker pack = new MessagePacker(out);
        sig.writeTo(pack);
        MessageUnpacker unpack = new MessageUnpacker(new ByteArrayInputStream(out.toByteArray()));
        TypeSignature loaded = AstLoader.loadTypeSignature(unpack);

        Assert.assertEquals(sig, loaded);
    }

    @Test
    public void testClass() throws IOException {
        TypeSignature sig = ClassTypeSignature.of("Ljava/lang/String;");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MessagePacker pack = new MessagePacker(out);
        sig.writeTo(pack);
        MessageUnpacker unpack = new MessageUnpacker(new ByteArrayInputStream(out.toByteArray()));
        TypeSignature loaded = AstLoader.loadTypeSignature(unpack);

        Assert.assertEquals(sig, loaded);
    }

    @Test
    public void testPrimative() throws IOException {
        TypeSignature sig = ClassTypeSignature.INT;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MessagePacker pack = new MessagePacker(out);
        sig.writeTo(pack);
        MessageUnpacker unpack = new MessageUnpacker(new ByteArrayInputStream(out.toByteArray()));
        TypeSignature loaded = AstLoader.loadTypeSignature(unpack);

        Assert.assertEquals(sig, loaded);
    }

    @Test
    public void testGeneric() throws IOException {
        GenericClassTypeSignature sig = new GenericClassTypeSignature("Ljava/util/List;");
        sig.getArguments().add(new TypeArgument(WildcardType.NONE, ClassTypeSignature.INTEGER_OBJECT));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MessagePacker pack = new MessagePacker(out);
        sig.writeTo(pack);
        MessageUnpacker unpack = new MessageUnpacker(new ByteArrayInputStream(out.toByteArray()));
        TypeSignature loaded = AstLoader.loadTypeSignature(unpack);

        Assert.assertEquals(sig, loaded);
    }

    @Test
    public void testVar() throws IOException {
        TypeSignature sig = new TypeVariableSignature("T");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MessagePacker pack = new MessagePacker(out);
        sig.writeTo(pack);
        MessageUnpacker unpack = new MessageUnpacker(new ByteArrayInputStream(out.toByteArray()));
        TypeSignature loaded = AstLoader.loadTypeSignature(unpack);

        Assert.assertEquals(sig, loaded);
    }

}
