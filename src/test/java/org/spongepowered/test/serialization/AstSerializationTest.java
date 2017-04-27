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
import org.spongepowered.despector.ast.Locals;
import org.spongepowered.despector.ast.Locals.Local;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.util.serialization.AstLoader;
import org.spongepowered.despector.util.serialization.MessagePacker;
import org.spongepowered.despector.util.serialization.MessageUnpacker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AstSerializationTest {

    @Test
    public void testLocals() throws IOException {
        Locals locals = new Locals(false);
        Local l = locals.getLocal(0);
        LocalInstance a = new LocalInstance(l, "this", null, -1, -1);
        l.addInstance(a);
        l = locals.getLocal(1);
        LocalInstance b = new LocalInstance(l, "i", ClassTypeSignature.INT, -1, -1);
        l.addInstance(b);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MessagePacker pack = new MessagePacker(out);
        locals.writeTo(pack);
        MessageUnpacker unpack = new MessageUnpacker(new ByteArrayInputStream(out.toByteArray()));
        Locals loaded = AstLoader.loadLocals(unpack, false, new SourceSet());

        l = loaded.getLocal(0);
        Assert.assertEquals(a, l.getParameterInstance());
        l = loaded.getLocal(1);
        Assert.assertEquals(b, l.getParameterInstance());
    }

}
