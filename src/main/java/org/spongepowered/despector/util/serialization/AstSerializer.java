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

/**
 * Constants for serializing ast elements.
 */
public final class AstSerializer {

    public static final int VERSION = 1;

    public static final int ENTRY_ID_CLASS = 0x00;
    public static final int ENTRY_ID_ENUM = 0x01;
    public static final int ENTRY_ID_INTERFACE = 0x02;
    public static final int ENTRY_ID_METHOD = 0x03;
    public static final int ENTRY_ID_FIELD = 0x04;
    public static final int ENTRY_ID_ARRAY = 0x05;
    public static final int ENTRY_ID_STATEMENT_BODY = 0x06;
    public static final int ENTRY_ID_ANNOTATION = 0x07;
    public static final int ENTRY_ID_ANNOTATIONTYPE = 0x07;

    public static final int STATEMENT_ID_COMMENT = 0x10;
    public static final int STATEMENT_ID_INCREMENT = 0x11;
    public static final int STATEMENT_ID_RETURN = 0x12;
    public static final int STATEMENT_ID_THROW = 0x13;
    public static final int STATEMENT_ID_STATIC_INVOKE = 0x14;
    public static final int STATEMENT_ID_INSTANCE_INVOKE = 0x15;
    public static final int STATEMENT_ID_DYNAMIC_INVOKE = 0x16;
    public static final int STATEMENT_ID_NEW = 0x17;
    public static final int STATEMENT_ID_IF = 0x18;
    public static final int STATEMENT_ID_DO_WHILE = 0x19;
    public static final int STATEMENT_ID_WHILE = 0x1A;
    public static final int STATEMENT_ID_FOR = 0x1B;
    public static final int STATEMENT_ID_FOREACH = 0x1C;
    public static final int STATEMENT_ID_SWITCH = 0x1D;
    public static final int STATEMENT_ID_TRY_CATCH = 0x1E;
    public static final int STATEMENT_ID_TERNARY = 0x1F;
    public static final int STATEMENT_ID_ARRAY_ASSIGN = 0x20;
    public static final int STATEMENT_ID_INSTANCE_FIELD_ASSIGN = 0x21;
    public static final int STATEMENT_ID_STATIC_FIELD_ASSIGN = 0x22;
    public static final int STATEMENT_ID_LOCAL_ASSIGN = 0x23;
    public static final int STATEMENT_ID_NUMBER_COMPARE = 0x24;
    public static final int STATEMENT_ID_NEW_ARRAY = 0x25;
    public static final int STATEMENT_ID_INSTANCE_OF = 0x26;
    public static final int STATEMENT_ID_CAST = 0x27;
    public static final int STATEMENT_ID_OPERATOR = 0x28;
    public static final int STATEMENT_ID_NEGATIVE_OPERATOR = 0x29;
    public static final int STATEMENT_ID_ARRAY_ACCESS = 0x2A;
    public static final int STATEMENT_ID_INSTANCE_FIELD_ACCESS = 0x2B;
    public static final int STATEMENT_ID_STATIC_FIELD_ACCESS = 0x2C;
    public static final int STATEMENT_ID_LOCAL_ACCESS = 0x2D;
    public static final int STATEMENT_ID_DOUBLE_CONSTANT = 0x2E;
    public static final int STATEMENT_ID_FLOAT_CONSTANT = 0x2F;
    public static final int STATEMENT_ID_INT_CONSTANT = 0x30;
    public static final int STATEMENT_ID_LONG_CONSTANT = 0x31;
    public static final int STATEMENT_ID_NULL_CONSTANT = 0x32;
    public static final int STATEMENT_ID_STRING_CONSTANT = 0x33;
    public static final int STATEMENT_ID_TYPE_CONSTANT = 0x34;
    public static final int STATEMENT_ID_INVOKE = 0x35;
    public static final int STATEMENT_ID_BREAK = 0x36;
    public static final int STATEMENT_ID_MULTI_NEW_ARRAY = 0x37;

    public static final int SIGNATURE_ID_TYPEVOID = 0x80;
    public static final int SIGNATURE_ID_TYPECLASS = 0x81;
    public static final int SIGNATURE_ID_TYPEVAR = 0x82;
    public static final int SIGNATURE_ID_ARG = 0x83;
    public static final int SIGNATURE_ID_PARAM = 0x84;
    public static final int SIGNATURE_ID_CLASS = 0x85;
    public static final int SIGNATURE_ID_METHOD = 0x86;
    public static final int SIGNATURE_ID_TYPEGENERIC = 0x87;

    public static final int CONDITION_ID_AND = 0x90;
    public static final int CONDITION_ID_BOOL = 0x91;
    public static final int CONDITION_ID_OR = 0x92;
    public static final int CONDITION_ID_COMPARE = 0x93;
    public static final int CONDITION_ID_INVERSE = 0x94;

    private AstSerializer() {
    }

}
