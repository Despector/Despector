/*
 * The MIT License (MIT)
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
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
package org.spongepowered.despector.ast;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.objectweb.asm.Type;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Annotation {

    private static final Set<Class<?>> VALID_TYPES = new HashSet<>();

    static {
        VALID_TYPES.add(String.class);
        VALID_TYPES.add(Type.class);
        VALID_TYPES.add(ArrayList.class);

        VALID_TYPES.add(Boolean.class);
        VALID_TYPES.add(Byte.class);
        VALID_TYPES.add(Short.class);
        VALID_TYPES.add(Integer.class);
        VALID_TYPES.add(Long.class);
        VALID_TYPES.add(Float.class);
        VALID_TYPES.add(Double.class);
        VALID_TYPES.add(Character.class);
    }

    public static boolean isValidValue(Class<?> type) {
        if (type.isPrimitive() || VALID_TYPES.contains(type)) {
            return true;
        }
        if (type.isArray()) {
            return isValidValue(type.getComponentType());
        }
        return false;
    }

    private final AnnotationType type;

    private final Map<String, Object> values = new LinkedHashMap<>();

    public Annotation(AnnotationType type) {
        this.type = checkNotNull(type, "type");
    }

    public AnnotationType getType() {
        return this.type;
    }

    public void setValue(String key, Object value) {
        Class<?> expected = this.type.getType(key);
        if (this.type.isComplete()) {
            checkNotNull(expected, "No matching type for key " + key);
            checkArgument(expected.isInstance(value), "Annotation value was not of proper type, expected " + expected.getName());
        } else if (expected != null) {
            checkArgument(expected.isInstance(value), "Annotation value was not of proper type, expected " + expected.getName());
        } else {
            this.type.setType(key, value.getClass());
        }
        this.values.put(key, value);
    }

    public Object getValue(String key) {
        return this.values.get(key);
    }

    public Set<String> getKeys() {
        return this.values.keySet();
    }

    private void writeObj(Object o, MessagePacker pack) throws IOException {
        if (o instanceof Integer) {
            pack.writeInt(((Integer) o).intValue());
        } else if (o instanceof Byte) {
            pack.writeInt(((Byte) o).byteValue());
        } else if (o instanceof Short) {
            pack.writeInt(((Short) o).shortValue());
        } else if (o instanceof Long) {
            pack.writeInt(((Long) o).longValue());
        } else if (o instanceof Float) {
            pack.writeFloat(((Float) o).floatValue());
        } else if (o instanceof Double) {
            pack.writeDouble(((Double) o).doubleValue());
        } else if (o instanceof String) {
            pack.writeString(((String) o));
        } else if (o instanceof Class) {
            pack.startMap(2);
            pack.writeString("id").writeInt(AstSerializer.ENTRY_ID_CLASS);
            pack.writeString("class").writeString(((Class<?>) o).getName());
        }
        throw new IllegalStateException("Cannot pack " + o.getClass().getSimpleName());
    }

    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(4);
        pack.writeString("id").writeInt(AstSerializer.ENTRY_ID_ANNOTATION);
        pack.writeString("typename").writeString(this.type.getName());
        pack.writeString("values").startArray(this.values.size());
        for (String key : this.values.keySet()) {
            pack.startMap(4);
            pack.writeString("name").writeString(key);
            pack.writeString("type").writeString(this.type.getType(key).getName());
            pack.writeString("default");
            writeObj(this.type.getDefaultValue(key), pack);
            pack.writeString("value");
            writeObj(this.values.get(key), pack);
        }
        pack.writeString("runtime").writeBool(this.type.isRuntimeVisible());
    }

}
