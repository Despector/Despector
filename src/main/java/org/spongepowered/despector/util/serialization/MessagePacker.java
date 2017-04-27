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

import static org.spongepowered.despector.util.serialization.MessageType.*;

import com.google.common.base.Charsets;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A serializer for writing files using the messagepack format.
 */
public class MessagePacker implements AutoCloseable {

    private static final int MAX_POSITIVE_FIXINT = 0x7F;
    private static final int MIN_NEGATUVE_FIXINT = -32;
    private static final int NIBBLE_MASK = 0xF;
    private static final int BYTE_MASK = 0xFF;
    private static final int SHORT_MASK = 0xFFFF;
    private static final int MAX_FIXSTRING_LENGTH = 31;

    private final DataOutputStream stream;
    private final Deque<Frame> frames = new ArrayDeque<>();

    public MessagePacker(OutputStream str) {
        if (str instanceof DataOutputStream) {
            this.stream = (DataOutputStream) str;
        } else {
            this.stream = new DataOutputStream(str);
        }
        this.frames.push(new Frame(FrameType.MAP, 1));
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    /**
     * Writes a nil value.
     */
    public MessagePacker writeNil() throws IOException {
        decreaseFrame();
        this.stream.writeByte(TYPE_NIL);
        return this;
    }

    /**
     * Writes a boolean value.
     */
    public MessagePacker writeBool(boolean val) throws IOException {
        decreaseFrame();
        this.stream.writeByte(val ? TYPE_BOOL_TRUE : TYPE_BOOL_FALSE);
        return this;
    }

    /**
     * Writes an integer value.
     */
    public MessagePacker writeInt(long val) throws IOException {
        decreaseFrame();
        if (val >= 0 && val <= MAX_POSITIVE_FIXINT) {
            this.stream.writeByte((int) (val & MAX_POSITIVE_FIXINT));
        } else if (val < 0 && val >= MIN_NEGATUVE_FIXINT) {
            this.stream.writeByte((int) val);
        } else if (val <= Byte.MAX_VALUE && val >= Byte.MIN_VALUE) {
            this.stream.writeByte(TYPE_INT8);
            this.stream.writeByte((int) (val & BYTE_MASK));
        } else if (val <= Short.MAX_VALUE && val >= Short.MIN_VALUE) {
            this.stream.writeByte(TYPE_INT16);
            this.stream.writeShort((int) (val & SHORT_MASK));
        } else if (val <= Integer.MAX_VALUE && val >= Integer.MIN_VALUE) {
            this.stream.writeByte(TYPE_INT32);
            this.stream.writeInt((int) val);
        } else {
            this.stream.writeByte(TYPE_INT64);
            this.stream.writeLong(val);
        }
        return this;
    }

    /**
     * Writes an unsigned integer value.
     */
    public MessagePacker writeUnsignedInt(long val) throws IOException {
        decreaseFrame();
        if (val <= MAX_POSITIVE_FIXINT) {
            this.stream.writeByte((int) (val & MAX_POSITIVE_FIXINT));
        } else if (val <= BYTE_MASK) {
            this.stream.writeByte(TYPE_UINT8);
            this.stream.writeByte((int) (val & BYTE_MASK));
        } else if (val <= SHORT_MASK) {
            this.stream.writeByte(TYPE_UINT16);
            this.stream.writeShort((int) (val & SHORT_MASK));
        } else if (val <= 0xFFFFFFFF) {
            this.stream.writeByte(TYPE_UINT32);
            this.stream.writeInt((int) (val & 0xFFFFFFFF));
        } else {
            this.stream.writeByte(TYPE_UINT64);
            this.stream.writeLong(val);
        }
        return this;
    }

    /**
     * Writes a 32-bit floating point value.
     */
    public MessagePacker writeFloat(float val) throws IOException {
        decreaseFrame();
        this.stream.writeByte(TYPE_FLOAT);
        this.stream.writeFloat(val);
        return this;
    }

    /**
     * Writes a 64-bit floating point value.
     */
    public MessagePacker writeDouble(double val) throws IOException {
        decreaseFrame();
        this.stream.writeByte(TYPE_DOUBLE);
        this.stream.writeDouble(val);
        return this;
    }

    /**
     * Writes a string value.
     */
    public MessagePacker writeString(String val) throws IOException {
        decreaseStringFrame();
        byte[] chars = val.getBytes(Charsets.UTF_8);
        int len = chars.length;
        if (len <= MAX_FIXSTRING_LENGTH) {
            this.stream.writeByte(TYPE_STR5_MASK | len);
        } else if (len < BYTE_MASK) {
            this.stream.writeByte(TYPE_STR8);
            this.stream.writeByte(len);
        } else if (len < SHORT_MASK) {
            this.stream.writeByte(TYPE_STR16);
            this.stream.writeShort(len);
        } else {
            this.stream.writeByte(TYPE_STR32);
            this.stream.writeInt(len);
        }
        this.stream.write(chars);
        return this;
    }

    /**
     * Writes a binary value.
     */
    public MessagePacker writeBin(byte[] data) throws IOException {
        decreaseFrame();
        if (data.length < BYTE_MASK) {
            this.stream.writeByte(TYPE_BIN8);
            this.stream.writeByte(data.length);
        } else if (data.length < SHORT_MASK) {
            this.stream.writeByte(TYPE_BIN16);
            this.stream.writeShort(data.length);
        } else {
            this.stream.writeByte(TYPE_BIN32);
            this.stream.writeInt(data.length);
        }
        this.stream.write(data);
        return this;
    }

    /**
     * Starts an array of values.
     */
    public MessagePacker startArray(int len) throws IOException {
        decreaseFrame();
        if (len < NIBBLE_MASK) {
            this.stream.writeByte(TYPE_ARRAY8_MASK | len);
        } else if (len < SHORT_MASK) {
            this.stream.writeByte(TYPE_ARRAY16);
            this.stream.writeShort(len);
        } else {
            this.stream.writeByte(TYPE_ARRAY32);
            this.stream.writeInt(len);
        }
        this.frames.push(new Frame(FrameType.ARRAY, len));
        return this;
    }

    public MessagePacker endArray() {
        Frame f = this.frames.pop();
        if (f.type != FrameType.ARRAY) {
            throw new IllegalStateException("Attempted to end map frame as array");
        }
        if (f.remaining != 0) {
            throw new IllegalStateException("Frame underflow");
        }
        return this;
    }

    /**
     * Starts a map of key value pairs.
     */
    public MessagePacker startMap(int len) throws IOException {
        decreaseFrame();
        if (len < NIBBLE_MASK) {
            this.stream.writeByte(TYPE_MAP8_MASK | len);
        } else if (len < SHORT_MASK) {
            this.stream.writeByte(TYPE_MAP16);
            this.stream.writeShort(len);
        } else {
            this.stream.writeByte(TYPE_MAP32);
            this.stream.writeInt(len);
        }
        this.frames.push(new Frame(FrameType.MAP, len * 2));
        return this;
    }

    public MessagePacker endMap() {
        Frame f = this.frames.pop();
        if (f.type != FrameType.MAP) {
            throw new IllegalStateException("Attempted to end array frame as map");
        }
        if (f.remaining != 0) {
            throw new IllegalStateException("Frame underflow");
        }
        return this;
    }

    private void decreaseFrame() {
        Frame f = this.frames.peek();
        if (f.type == FrameType.MAP && f.remaining % 2 == 0) {
            throw new IllegalStateException("Expected map key");
        }
        f.remaining--;
        if (f.remaining < 0) {
            throw new IllegalStateException("Frame " + f.type.name() + " overflowed");
        }
    }

    private void decreaseStringFrame() {
        Frame f = this.frames.peek();
        f.remaining--;
        if (f.remaining < 0) {
            throw new IllegalStateException("Frame " + f.type.name() + " overflowed");
        }
    }

    // TODO support EXT

    private static class Frame {

        public FrameType type;
        public int remaining;

        public Frame(FrameType type, int len) {
            this.type = type;
            this.remaining = len;
        }
    }

    private static enum FrameType {
        ARRAY,
        MAP,
    }

}
