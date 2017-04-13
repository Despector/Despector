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
package com.voxelgenesis.despector.jvm.loader;

import com.voxelgenesis.despector.core.ast.signature.ArrayTypeSignature;
import com.voxelgenesis.despector.core.ast.signature.ClassTypeSignature;
import com.voxelgenesis.despector.core.ast.signature.PrimativeTypeSignature;
import com.voxelgenesis.despector.core.ast.signature.TypeSignature;

import java.util.HashMap;
import java.util.Map;

public class JvmHelper {

    public static final PrimativeTypeSignature BOOLEAN = new PrimativeTypeSignature("Z");
    public static final PrimativeTypeSignature BYTE = new PrimativeTypeSignature("B");
    public static final PrimativeTypeSignature SHORT = new PrimativeTypeSignature("S");
    public static final PrimativeTypeSignature INT = new PrimativeTypeSignature("I");
    public static final PrimativeTypeSignature LONG = new PrimativeTypeSignature("J");
    public static final PrimativeTypeSignature FLOAT = new PrimativeTypeSignature("F");
    public static final PrimativeTypeSignature DOUBLE = new PrimativeTypeSignature("D");
    public static final PrimativeTypeSignature CHAR = new PrimativeTypeSignature("C");

    private static final Map<String, TypeSignature> SPECIAL = new HashMap<>();

    static {
        SPECIAL.put("B", BYTE);
        SPECIAL.put("S", SHORT);
        SPECIAL.put("I", INT);
        SPECIAL.put("J", LONG);
        SPECIAL.put("F", FLOAT);
        SPECIAL.put("D", DOUBLE);
        SPECIAL.put("C", CHAR);
        SPECIAL.put("Z", BOOLEAN);
    }

    public static TypeSignature of(String type) {
        TypeSignature sig = SPECIAL.get(type);
        if (sig != null) {
            return sig;
        }
        if (type.startsWith("[")) {
            return new ArrayTypeSignature(of(type.substring(1)));
        }
        sig = new ClassTypeSignature(type);
        return sig;
    }

}
