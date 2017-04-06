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

public enum MessageType {

    NIL,
    BOOL,
    INT,
    UINT,
    LONG,
    ULONG,
    FLOAT,
    DOUBLE,
    STRING,
    BIN,
    ARRAY,
    MAP;

    public static final int TYPE_NIL = 0xC0;
    public static final int TYPE_BOOL_FALSE = 0xC2;
    public static final int TYPE_BOOL_TRUE = 0xC3;
    public static final int TYPE_NEGINT_MASK = 0xE0;
    public static final int TYPE_UINT8 = 0xCC;
    public static final int TYPE_UINT16 = 0xCD;
    public static final int TYPE_UINT32 = 0xCE;
    public static final int TYPE_UINT64 = 0xCF;
    public static final int TYPE_INT8 = 0xD0;
    public static final int TYPE_INT16 = 0xD1;
    public static final int TYPE_INT32 = 0xD2;
    public static final int TYPE_INT64 = 0xD3;
    public static final int TYPE_FLOAT = 0xCA;
    public static final int TYPE_DOUBLE = 0xCB;
    public static final int TYPE_STR5_MASK = 0xA0;
    public static final int TYPE_STR8 = 0xD9;
    public static final int TYPE_STR16 = 0xDA;
    public static final int TYPE_STR32 = 0xDB;
    public static final int TYPE_BIN8 = 0xC4;
    public static final int TYPE_BIN16 = 0xC5;
    public static final int TYPE_BIN32 = 0xC6;
    public static final int TYPE_ARRAY8_MASK = 0x90;
    public static final int TYPE_ARRAY16 = 0xDC;
    public static final int TYPE_ARRAY32 = 0xDD;
    public static final int TYPE_MAP8_MASK = 0x80;
    public static final int TYPE_MAP16 = 0xDE;
    public static final int TYPE_MAP32 = 0xDF;
    public static final int TYPE_FIXEXT1 = 0xD4;
    public static final int TYPE_FIXEXT2 = 0xD5;
    public static final int TYPE_FIXEXT4 = 0xD6;
    public static final int TYPE_FIXEXT8 = 0xD7;
    public static final int TYPE_FIXEXT16 = 0xD8;
    public static final int TYPE_EXT8 = 0xC7;
    public static final int TYPE_EXT16 = 0xC8;
    public static final int TYPE_EXT32 = 0xC9;

    public static MessageType of(int next) {
        if (next == TYPE_BOOL_FALSE || next == TYPE_BOOL_TRUE) {
            return BOOL;
        } else if ((next & 0x80) == 0 || (next & TYPE_NEGINT_MASK) == TYPE_NEGINT_MASK || next == TYPE_INT8 || next == TYPE_INT16
                || next == TYPE_INT32 || next == TYPE_INT64) {
            return INT;
        } else if (next == TYPE_UINT8 || next == TYPE_UINT16 || next == TYPE_UINT32 || next == TYPE_UINT64) {
            return UINT;
        } else if (next == TYPE_FLOAT) {
            return FLOAT;
        } else if (next == TYPE_DOUBLE) {
            return DOUBLE;
        } else if ((next & 0xE0) == TYPE_STR5_MASK || next == TYPE_STR8 || next == TYPE_STR16 || next == TYPE_STR32) {
            return STRING;
        } else if (next == TYPE_BIN8 || next == TYPE_BIN16 || next == TYPE_BIN32) {
            return BIN;
        } else if ((next & 0xF0) == TYPE_ARRAY8_MASK || next == TYPE_ARRAY16 || next == TYPE_ARRAY32) {
            return ARRAY;
        } else if ((next & 0xF0) == TYPE_MAP8_MASK || next == TYPE_MAP16 || next == TYPE_MAP32) {
            return MAP;
        } else if (next == TYPE_NIL) {
            return NIL;
        }
        throw new IllegalArgumentException("Unsupported messagepack type: 0x" + Integer.toHexString(next));
    }
}
