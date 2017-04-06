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

public class MessagePacker implements AutoCloseable {

    private final DataOutputStream stream;

    public MessagePacker(OutputStream str) {
        if (str instanceof DataOutputStream) {
            this.stream = (DataOutputStream) str;
        } else {
            this.stream = new DataOutputStream(str);
        }
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    public MessagePacker writeNil() throws IOException {
        this.stream.writeByte(TYPE_NIL);
        return this;
    }

    public MessagePacker writeBool(boolean val) throws IOException {
        this.stream.writeByte(val ? TYPE_BOOL_TRUE : TYPE_BOOL_FALSE);
        return this;
    }

    public MessagePacker writeInt(long val) throws IOException {
        if (val >= 0 && val <= 0x7F) {
            this.stream.writeByte((int) (val & 0x7F));
        } else if (val < 0 && val >= -32) {
            this.stream.writeByte((int) val);
        } else if (val <= 127 && val >= -128) {
            this.stream.writeByte(TYPE_INT8);
            this.stream.writeByte((int) (val & 0xFF));
        } else if (val <= 0xFFFF) {
            this.stream.writeByte(TYPE_INT16);
            this.stream.writeShort((int) (val & 0xFFFF));
        } else if (val <= 0xFFFFFFFFL) {
            this.stream.writeByte(TYPE_INT32);
            this.stream.writeInt((int) val);
        } else {
            this.stream.writeByte(TYPE_INT64);
            this.stream.writeLong(val);
        }
        return this;
    }

    public MessagePacker writeUnsignedInt(long val) throws IOException {
        if (val <= 0x7F) {
            this.stream.writeByte((int) (val & 0x7F));
        } else if (val <= 0xFF) {
            this.stream.writeByte(TYPE_UINT8);
            this.stream.writeByte((int) (val & 0xFF));
        } else if (val <= 0xFFFF) {
            this.stream.writeByte(TYPE_UINT16);
            this.stream.writeShort((int) (val & 0xFFFF));
        } else if (val <= 0xFFFFFFFF) {
            this.stream.writeByte(TYPE_UINT32);
            this.stream.writeInt((int) (val & 0xFFFFFFFF));
        } else {
            this.stream.writeByte(TYPE_UINT64);
            this.stream.writeLong(val);
        }
        return this;
    }

    public MessagePacker writeFloat(float val) throws IOException {
        this.stream.writeByte(TYPE_FLOAT);
        this.stream.writeFloat(val);
        return this;
    }

    public MessagePacker writeDouble(double val) throws IOException {
        this.stream.writeByte(TYPE_DOUBLE);
        this.stream.writeDouble(val);
        return this;
    }

    public MessagePacker writeString(String val) throws IOException {
        if (val.length() <= 31) {
            this.stream.writeByte(TYPE_STR5_MASK | val.length());
        } else if (val.length() < 0xFF) {
            this.stream.writeByte(TYPE_STR8);
            this.stream.writeByte(val.length());
        } else if (val.length() < 0xFFFF) {
            this.stream.writeByte(TYPE_STR16);
            this.stream.writeShort(val.length());
        } else {
            this.stream.writeByte(TYPE_STR32);
            this.stream.writeInt(val.length());
        }
        this.stream.write(val.getBytes(Charsets.UTF_8));
        return this;
    }

    public MessagePacker writeBin(byte[] data) throws IOException {
        if (data.length < 0xFF) {
            this.stream.writeByte(TYPE_BIN8);
            this.stream.writeByte(data.length);
        } else if (data.length < 0xFFFF) {
            this.stream.writeByte(TYPE_BIN16);
            this.stream.writeShort(data.length);
        } else {
            this.stream.writeByte(TYPE_BIN32);
            this.stream.writeInt(data.length);
        }
        this.stream.write(data);
        return this;
    }

    public MessagePacker startArray(int len) throws IOException {
        if (len < 0xF) {
            this.stream.writeByte(TYPE_ARRAY8_MASK | len);
        } else if (len < 0xFFFF) {
            this.stream.writeByte(TYPE_ARRAY16);
            this.stream.writeShort(len);
        } else {
            this.stream.writeByte(TYPE_ARRAY32);
            this.stream.writeInt(len);
        }
        return this;
    }

    public MessagePacker startMap(int len) throws IOException {
        if (len < 0xF) {
            this.stream.writeByte(TYPE_MAP8_MASK | len);
        } else if (len < 0xFFFF) {
            this.stream.writeByte(TYPE_MAP16);
            this.stream.writeShort(len);
        } else {
            this.stream.writeByte(TYPE_MAP32);
            this.stream.writeInt(len);
        }
        return this;
    }
    
    // TODO support EXT

}
