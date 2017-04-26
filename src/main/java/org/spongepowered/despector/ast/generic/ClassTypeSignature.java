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
package org.spongepowered.despector.ast.generic;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.util.TypeHelper;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A type signature of a class or primative type (but not void).
 */
public class ClassTypeSignature extends TypeSignature {

    public static final ClassTypeSignature BOOLEAN = new ClassTypeSignature("Z");
    public static final ClassTypeSignature BYTE = new ClassTypeSignature("B");
    public static final ClassTypeSignature SHORT = new ClassTypeSignature("S");
    public static final ClassTypeSignature INT = new ClassTypeSignature("I");
    public static final ClassTypeSignature LONG = new ClassTypeSignature("J");
    public static final ClassTypeSignature FLOAT = new ClassTypeSignature("F");
    public static final ClassTypeSignature DOUBLE = new ClassTypeSignature("D");
    public static final ClassTypeSignature CHAR = new ClassTypeSignature("C");
    public static final ClassTypeSignature OBJECT = new ClassTypeSignature("Ljava/lang/Object;");
    public static final ClassTypeSignature STRING = new ClassTypeSignature("Ljava/lang/String;");

    public static final ClassTypeSignature BOOLEAN_OBJECT = new ClassTypeSignature("Ljava/lang/Boolean;");
    public static final ClassTypeSignature BYTE_OBJECT = new ClassTypeSignature("Ljava/lang/Byte;");
    public static final ClassTypeSignature SHORT_OBJECT = new ClassTypeSignature("Ljava/lang/Short;");
    public static final ClassTypeSignature INTEGER_OBJECT = new ClassTypeSignature("Ljava/lang/Integer;");
    public static final ClassTypeSignature LONG_OBJECT = new ClassTypeSignature("Ljava/lang/Long;");
    public static final ClassTypeSignature FLOAT_OBJECT = new ClassTypeSignature("Ljava/lang/Float;");
    public static final ClassTypeSignature DOUBLE_OBJECT = new ClassTypeSignature("Ljava/lang/Double;");
    public static final ClassTypeSignature CHARACTER_OBJECT = new ClassTypeSignature("Ljava/lang/Character;");

    private static final Map<String, ClassTypeSignature> SPECIAL = new HashMap<>();

    static {
        SPECIAL.put(BOOLEAN.getType(), BOOLEAN);
        SPECIAL.put(BYTE.getType(), BYTE);
        SPECIAL.put(SHORT.getType(), SHORT);
        SPECIAL.put(INT.getType(), INT);
        SPECIAL.put(LONG.getType(), LONG);
        SPECIAL.put(FLOAT.getType(), FLOAT);
        SPECIAL.put(DOUBLE.getType(), DOUBLE);
        SPECIAL.put(CHAR.getType(), CHAR);

        SPECIAL.put(STRING.getType(), STRING);
        SPECIAL.put(OBJECT.getType(), OBJECT);

        SPECIAL.put(BOOLEAN_OBJECT.getType(), BOOLEAN_OBJECT);
        SPECIAL.put(BYTE_OBJECT.getType(), BYTE_OBJECT);
        SPECIAL.put(SHORT_OBJECT.getType(), SHORT_OBJECT);
        SPECIAL.put(INTEGER_OBJECT.getType(), INTEGER_OBJECT);
        SPECIAL.put(LONG_OBJECT.getType(), LONG_OBJECT);
        SPECIAL.put(FLOAT_OBJECT.getType(), FLOAT_OBJECT);
        SPECIAL.put(DOUBLE_OBJECT.getType(), DOUBLE_OBJECT);
        SPECIAL.put(CHARACTER_OBJECT.getType(), CHARACTER_OBJECT);
    }

    /**
     * Gets the {@link ClassTypeSignature} for the given type descriptor. A new
     * instance is created unless the type is a special type (primatives,
     * primative wrappers, object, and string).
     */
    public static ClassTypeSignature of(String type) {
        return of(type, false);
    }

    /**
     * Gets the {@link ClassTypeSignature} for the given type descriptor. A new
     * instance is created unless the type is a special type (primatives,
     * primative wrappers, object, and string) and the no_special flag is unset.
     */
    public static ClassTypeSignature of(String type, boolean no_special) {
        if (!no_special) {
            ClassTypeSignature sig = SPECIAL.get(type);
            if (sig != null) {
                return sig;
            }
        }
        return new ClassTypeSignature(type);
    }

    protected String type_name;

    ClassTypeSignature(String type) {
        this.type_name = checkNotNull(type, "type");
    }

    /**
     * Gets the type descriptor.
     */
    public String getType() {
        return this.type_name;
    }

    /**
     * Sets the type descriptor.
     */
    public void setType(String type) {
        this.type_name = checkNotNull(type, "type");
    }

    @Override
    public boolean hasArguments() {
        return false;
    }

    @Override
    public String getName() {
        return TypeHelper.descToType(this.type_name);
    }

    public String getClassName() {
        return getName().replace('/', '.');
    }

    @Override
    public String getDescriptor() {
        return this.type_name;
    }

    @Override
    public boolean isArray() {
        return this.type_name.startsWith("[");
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(2);
        pack.writeString("id").writeInt(AstSerializer.SIGNATURE_ID_TYPECLASS);
        pack.writeString("type").writeString(this.type_name);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.type_name);
        return str.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ClassTypeSignature)) {
            if (o instanceof GenericClassTypeSignature) {
                GenericClassTypeSignature g = (GenericClassTypeSignature) o;
                return !g.hasArguments() && this.type_name.equals(g.getType());
            }
            return false;
        }
        ClassTypeSignature c = (ClassTypeSignature) o;
        return c.type_name.equals(this.type_name);
    }

}
