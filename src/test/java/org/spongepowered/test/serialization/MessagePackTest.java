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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.spongepowered.despector.util.serialization.MessagePacker;
import org.spongepowered.despector.util.serialization.MessageUnpacker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MessagePackTest {

    @Test
    public void testNil() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (MessagePacker msg = new MessagePacker(out)) {
            msg.startMap(2);
            msg.writeString("val1").writeNil();
        }

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        try (MessageUnpacker unpack = new MessageUnpacker(in)) {
            int len = unpack.readMap();
            assertEquals(2, len);
            assertEquals("val1", unpack.readString());
            unpack.readNil();
        }
    }

    @Test
    public void testBool() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (MessagePacker msg = new MessagePacker(out)) {
            msg.startMap(2);
            msg.writeString("val1").writeBool(true);
            msg.writeString("val2").writeBool(false);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        try (MessageUnpacker unpack = new MessageUnpacker(in)) {
            int len = unpack.readMap();
            assertEquals(2, len);
            assertEquals("val1", unpack.readString());
            assertEquals(true, unpack.readBool());
            assertEquals("val2", unpack.readString());
            assertEquals(false, unpack.readBool());
        }
    }

    @Test
    public void testInt() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (MessagePacker msg = new MessagePacker(out)) {
            msg.startMap(6);
            msg.writeString("val1").writeInt(5);
            msg.writeString("val2").writeInt(-5);
            msg.writeString("val3").writeInt(86);
            msg.writeString("val4").writeInt(500);
            msg.writeString("val5").writeInt(150000);
            msg.writeString("val6").writeInt(5000000000L);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        try (MessageUnpacker unpack = new MessageUnpacker(in)) {
            int len = unpack.readMap();
            assertEquals(6, len);
            assertEquals("val1", unpack.readString());
            assertEquals(5, unpack.readInt());
            assertEquals("val2", unpack.readString());
            assertEquals(-5, unpack.readInt());
            assertEquals("val3", unpack.readString());
            assertEquals(86, unpack.readInt());
            assertEquals("val4", unpack.readString());
            assertEquals(500, unpack.readInt());
            assertEquals("val5", unpack.readString());
            assertEquals(150000, unpack.readInt());
            assertEquals("val6", unpack.readString());
            assertEquals(5000000000L, unpack.readLong());
        }
    }

    @Test
    public void testFloat() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (MessagePacker msg = new MessagePacker(out)) {
            msg.startMap(2);
            msg.writeString("val1").writeFloat(0.5f);
            msg.writeString("val2").writeDouble(0.5);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        try (MessageUnpacker unpack = new MessageUnpacker(in)) {
            int len = unpack.readMap();
            assertEquals(2, len);
            assertEquals("val1", unpack.readString());
            assertEquals(0.5f, unpack.readFloat(), 0.001f);
            assertEquals("val2", unpack.readString());
            assertEquals(0.5, unpack.readDouble(), 0.001);
        }
    }

    @Test
    public void testString() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (MessagePacker msg = new MessagePacker(out)) {
            msg.startMap(2);
            msg.writeString("val1").writeString("Hello");
            msg.writeString("val2").writeString("This string is longer than 31 bytes and therefore will be encoded as a TYTE_STR8");
        }

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        try (MessageUnpacker unpack = new MessageUnpacker(in)) {
            int len = unpack.readMap();
            assertEquals(2, len);
            assertEquals("val1", unpack.readString());
            assertEquals("Hello", unpack.readString());
            assertEquals("val2", unpack.readString());
            assertEquals("This string is longer than 31 bytes and therefore will be encoded as a TYTE_STR8", unpack.readString());
        }
    }

}
