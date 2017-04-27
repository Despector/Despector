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

import java.io.EOFException;
import java.io.IOException;

public class MessagePrinter {

    public static String print(MessageUnpacker unpack) {
        StringBuilder str = new StringBuilder();
        try {
            printNext(unpack, str, 0);
        } catch (EOFException e) {
            str.append("EOF!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    private static void printNext(MessageUnpacker unpack, StringBuilder out, int indent) throws IOException {
        for (int i = 0; i < indent; i++) {
            out.append("  ");
        }
        MessageType type = unpack.peekType();
        out.append(type.name()).append(" ");
        switch (type) {
        case ARRAY:
            int sz = unpack.readArray();
            out.append("(entryies = ").append(sz).append(")\n");
            for (int i = 0; i < sz; i++) {
                printNext(unpack, out, indent + 1);
            }
            break;
        case BIN:
            unpack.readBinary();
            out.append("\n");
            break;
        case BOOL:
            boolean zval = unpack.readBool();
            out.append(zval).append("\n");
            break;
        case DOUBLE:
            double dval = unpack.readDouble();
            out.append(dval).append("\n");
            break;
        case FLOAT:
            float fval = unpack.readFloat();
            out.append(fval).append("\n");
            break;
        case INT:
            int ival = unpack.readInt();
            out.append(ival).append("\n");
            break;
        case LONG:
            long lval = unpack.readLong();
            out.append(lval).append("\n");
            break;
        case MAP:
            int map_sz = unpack.readMap();
            out.append("(entryies = ").append(map_sz).append(")\n");
            for (int i = 0; i < map_sz * 2; i++) {
                printNext(unpack, out, indent + 1);
            }
            break;
        case NIL:
            unpack.readNil();
            out.append("\n");
            break;
        case STRING:
            String str = unpack.readString();
            out.append("\"").append(str).append("\"").append("\n");
            break;
        case UINT:
            long uival = unpack.readUnsignedInt();
            out.append(uival).append("\n");
            break;
        case ULONG:
            long ulval = unpack.readUnsignedLong();
            out.append(ulval).append("\n");
            break;
        default:
            throw new IllegalStateException("Unsupported message type: " + type.name());
        }
    }

}
