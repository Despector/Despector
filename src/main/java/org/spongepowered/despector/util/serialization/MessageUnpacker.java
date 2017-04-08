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
package org.spongepowered.despector.util.serialization;

import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.despector.util.serialization.MessageType.*;

import com.google.common.base.Charsets;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A deserializer for data using the messagepack format.
 */
public class MessageUnpacker implements AutoCloseable {

    private static final int FIXINT_TYPE_MASK = 0x80;
    private static final int FIXINT_MASK = 0x7F;
    private static final int NEGATIVE_FIXINT_MASK = 0xFFFFFF00;
    private static final int SHORTSTRING_TYPE_MASK = 0xE0;
    private static final int SHORTSTRING_MASK = 0x1F;
    private static final int SHORTARRAY_MASK = 0xF0;
    private static final int NIBBLE_MASK = 0xF;

    private final DataInputStream stream;

    public MessageUnpacker(InputStream str) {
        if (str instanceof DataInputStream) {
            this.stream = (DataInputStream) str;
        } else {
            this.stream = new DataInputStream(str);
        }
        checkState(this.stream.markSupported());
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    /**
     * Peeks at the next type in the input.
     */
    public MessageType peekType() throws IOException {
        this.stream.mark(1);
        int next = this.stream.readUnsignedByte();
        this.stream.reset();
        return MessageType.of(next);
    }

    private void expectType(MessageType type) throws IOException {
        MessageType actual = peekType();
        if (actual != type && !(type == MessageType.UINT && actual == MessageType.INT)) {
            throw new IllegalStateException("Unexpected type " + actual.name() + " but expected " + type.name());
        }
    }

    /**
     * Reads a nil value from the input.
     */
    public void readNil() throws IOException {
        expectType(MessageType.NIL);
        this.stream.skipBytes(1);
    }

    /**
     * Reads a boolean value from the input.
     */
    public boolean readBool() throws IOException {
        int next = this.stream.readUnsignedByte();
        if (next == TYPE_BOOL_TRUE) {
            return true;
        } else if (next == TYPE_BOOL_FALSE) {
            return false;
        }
        throw new IllegalStateException("Unexpected type " + MessageType.of(next).name() + " but expected BOOL");
    }

    /**
     * Reads a byte value from the input.
     */
    public byte readByte() throws IOException {
        expectType(MessageType.INT);
        int next = this.stream.readUnsignedByte();
        if ((next & FIXINT_TYPE_MASK) == 0) {
            return (byte) (next & FIXINT_MASK);
        } else if ((next & TYPE_NEGINT_MASK) == TYPE_NEGINT_MASK) {
            return (byte) (NEGATIVE_FIXINT_MASK | next);
        } else if (next == TYPE_INT8) {
            return this.stream.readByte();
        }
        throw new IllegalStateException("Unexpected type " + MessageType.of(next).name() + " but expected INT");
    }

    /**
     * Reads a short value from the input.
     */
    public short readShort() throws IOException {
        expectType(MessageType.INT);
        int next = this.stream.readUnsignedByte();
        if ((next & FIXINT_TYPE_MASK) == 0) {
            return (byte) (next & FIXINT_MASK);
        } else if ((next & TYPE_NEGINT_MASK) == TYPE_NEGINT_MASK) {
            return (byte) (NEGATIVE_FIXINT_MASK | next);
        } else if (next == TYPE_INT8) {
            return this.stream.readByte();
        } else if (next == TYPE_INT16) {
            return this.stream.readShort();
        }
        throw new IllegalStateException("Unexpected type " + MessageType.of(next).name() + " but expected INT");
    }

    /**
     * Reads an integer value from the input.
     */
    public int readInt() throws IOException {
        expectType(MessageType.INT);
        int next = this.stream.readUnsignedByte();
        if ((next & FIXINT_TYPE_MASK) == 0) {
            return (byte) (next & FIXINT_MASK);
        } else if ((next & TYPE_NEGINT_MASK) == TYPE_NEGINT_MASK) {
            return (byte) (NEGATIVE_FIXINT_MASK | next);
        } else if (next == TYPE_INT8) {
            return this.stream.readByte();
        } else if (next == TYPE_INT16) {
            return this.stream.readShort();
        } else if (next == TYPE_INT32) {
            return this.stream.readInt();
        }
        throw new IllegalStateException("Unexpected type " + MessageType.of(next).name() + " but expected INT");
    }

    /**
     * Reads a long value from the input.
     */
    public long readLong() throws IOException {
        expectType(MessageType.INT);
        int next = this.stream.readUnsignedByte();
        if ((next & FIXINT_TYPE_MASK) == 0) {
            return (byte) (next & FIXINT_MASK);
        } else if ((next & TYPE_NEGINT_MASK) == TYPE_NEGINT_MASK) {
            return (byte) (NEGATIVE_FIXINT_MASK | next);
        } else if (next == TYPE_INT8) {
            return this.stream.readByte();
        } else if (next == TYPE_INT16) {
            return this.stream.readShort();
        } else if (next == TYPE_INT32) {
            return this.stream.readInt();
        } else if (next == TYPE_INT64) {
            return this.stream.readLong();
        }
        throw new IllegalStateException("Unexpected type " + MessageType.of(next).name() + " but expected INT");
    }

    /**
     * Reads an unsigned byte value from the input.
     */
    public int readUnsignedByte() throws IOException {
        expectType(MessageType.UINT);
        int next = this.stream.readUnsignedByte();
        if ((next & FIXINT_TYPE_MASK) == 0) {
            return (next & FIXINT_MASK);
        } else if (next == TYPE_UINT8) {
            return this.stream.readByte();
        }
        throw new IllegalStateException("Unexpected type " + MessageType.of(next).name() + " but expected UINT");
    }

    /**
     * Reads an unsigned short value from the input.
     */
    public int readUnsignedShort() throws IOException {
        expectType(MessageType.UINT);
        int next = this.stream.readUnsignedByte();
        if ((next & FIXINT_TYPE_MASK) == 0) {
            return (byte) (next & FIXINT_MASK);
        } else if (next == TYPE_UINT8) {
            return this.stream.readByte();
        } else if (next == TYPE_UINT16) {
            return this.stream.readShort();
        }
        throw new IllegalStateException("Unexpected type " + MessageType.of(next).name() + " but expected UINT");
    }

    /**
     * Reads an unsigned integer value from the input.
     */
    public long readUnsignedInt() throws IOException {
        expectType(MessageType.UINT);
        int next = this.stream.readUnsignedByte();
        if ((next & FIXINT_TYPE_MASK) == 0) {
            return (byte) (next & FIXINT_MASK);
        } else if (next == TYPE_UINT8) {
            return this.stream.readByte();
        } else if (next == TYPE_UINT16) {
            return this.stream.readShort();
        } else if (next == TYPE_UINT32) {
            return this.stream.readInt();
        }
        throw new IllegalStateException("Unexpected type " + MessageType.of(next).name() + " but expected UINT");
    }

    /**
     * Reads an unsigned long value from the input.
     */
    public long readUnsignedLong() throws IOException {
        expectType(MessageType.UINT);
        int next = this.stream.readUnsignedByte();
        if ((next & FIXINT_TYPE_MASK) == 0) {
            return (byte) (next & FIXINT_MASK);
        } else if (next == TYPE_UINT8) {
            return this.stream.readByte();
        } else if (next == TYPE_UINT16) {
            return this.stream.readShort();
        } else if (next == TYPE_UINT32) {
            return this.stream.readInt();
        } else if (next == TYPE_UINT64) {
            return this.stream.readLong();
        }
        throw new IllegalStateException("Unexpected type " + MessageType.of(next).name() + " but expected UINT");
    }

    /**
     * Reads a 32-bit floating point value from the input.
     */
    public float readFloat() throws IOException {
        expectType(MessageType.FLOAT);
        this.stream.skipBytes(1);
        return this.stream.readFloat();
    }

    /**
     * Reads a 64-bit floating point value from the input.
     */
    public double readDouble() throws IOException {
        expectType(MessageType.DOUBLE);
        this.stream.skipBytes(1);
        return this.stream.readDouble();
    }

    /**
     * Reads a string value from the input.
     */
    public String readString() throws IOException {
        expectType(MessageType.STRING);
        int next = this.stream.readUnsignedByte();
        int len = -1;
        if ((next & SHORTSTRING_TYPE_MASK) == TYPE_STR5_MASK) {
            len = next & SHORTSTRING_MASK;
        } else if (next == TYPE_STR8) {
            len = this.stream.readUnsignedByte();
        } else if (next == TYPE_STR16) {
            len = this.stream.readUnsignedShort();
        } else if (next == TYPE_STR32) {
            len = this.stream.readInt();
        }
        byte[] data = new byte[len];
        this.stream.read(data);
        return new String(data, Charsets.UTF_8);
    }

    /**
     * Reads a binary value from the input.
     */
    public byte[] readBinary() throws IOException {
        expectType(MessageType.BIN);
        int next = this.stream.readUnsignedByte();
        int len = -1;
        if (next == TYPE_BIN8) {
            len = this.stream.readUnsignedByte();
        } else if (next == TYPE_BIN16) {
            len = this.stream.readUnsignedShort();
        } else if (next == TYPE_BIN32) {
            len = this.stream.readInt();
        }
        byte[] data = new byte[len];
        this.stream.read(data);
        return data;
    }

    /**
     * Reads an array value from the input and returns the number of items in
     * the array.
     */
    public int readArray() throws IOException {
        expectType(MessageType.ARRAY);
        int next = this.stream.readUnsignedByte();
        int len = -1;
        if ((next & SHORTARRAY_MASK) == TYPE_ARRAY8_MASK) {
            len = next & NIBBLE_MASK;
        } else if (next == TYPE_ARRAY16) {
            len = this.stream.readUnsignedShort();
        } else if (next == TYPE_ARRAY32) {
            len = this.stream.readInt();
        }
        return len;
    }

    /**
     * Reads a map value from the input and returns the number of key value
     * pairs in the map.
     */
    public int readMap() throws IOException {
        expectType(MessageType.MAP);
        int next = this.stream.readUnsignedByte();
        int len = -1;
        if ((next & SHORTARRAY_MASK) == TYPE_MAP8_MASK) {
            len = next & NIBBLE_MASK;
        } else if (next == TYPE_MAP16) {
            len = this.stream.readUnsignedShort();
        } else if (next == TYPE_MAP32) {
            len = this.stream.readInt();
        }
        return len;
    }

}
