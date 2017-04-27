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
package org.spongepowered.despector.ast;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.type.TypeVisitor;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An instance of an annotation on a member.
 */
public class Annotation {

    private static final Set<Class<?>> VALID_TYPES = new HashSet<>();

    static {
        VALID_TYPES.add(String.class);
        VALID_TYPES.add(ClassTypeSignature.class);
        VALID_TYPES.add(ArrayList.class);
        VALID_TYPES.add(Annotation.class);
        VALID_TYPES.add(EnumConstant.class);

        VALID_TYPES.add(Boolean.class);
        VALID_TYPES.add(Byte.class);
        VALID_TYPES.add(Short.class);
        VALID_TYPES.add(Integer.class);
        VALID_TYPES.add(Long.class);
        VALID_TYPES.add(Float.class);
        VALID_TYPES.add(Double.class);
        VALID_TYPES.add(Character.class);
    }

    /**
     * Gets if the given class is a valid annotation value type.
     */
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

    /**
     * Gets the type of this annotation.
     */
    public AnnotationType getType() {
        return this.type;
    }

    /**
     * Sets the given key value pair in this annotation.
     */
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

    /**
     * Gets the value of the given field.
     */
    public Object getValue(String key) {
        return this.values.get(key);
    }

    public Map<String, Object> getValues() {
        return this.values;
    }

    /**
     * Gets all valid fields in this annotation.
     */
    public Set<String> getKeys() {
        return this.values.keySet();
    }

    private void writeObj(Object o, MessagePacker pack) throws IOException {
        pack.startMap(2);
        pack.writeString("typename").writeString(o.getClass().getName());
        pack.writeString("value");
        if (o instanceof Integer) {
            pack.writeInt(((Integer) o).intValue());
        } else if (o instanceof Byte) {
            pack.writeInt(((Byte) o).byteValue());
        } else if (o instanceof Short) {
            pack.writeInt(((Short) o).shortValue());
        } else if (o instanceof Character) {
            pack.writeUnsignedInt((char) o);
        } else if (o instanceof Long) {
            pack.writeInt(((Long) o).longValue());
        } else if (o instanceof Float) {
            pack.writeFloat(((Float) o).floatValue());
        } else if (o instanceof Double) {
            pack.writeDouble(((Double) o).doubleValue());
        } else if (o instanceof String) {
            pack.writeString(((String) o));
        } else if (o instanceof ArrayList) {
            List<?> lst = (List<?>) o;
            pack.startArray(lst.size());
            for (Object obj : lst) {
                writeObj(obj, pack);
            }
            pack.endArray();
        } else if (o instanceof ClassTypeSignature) {
            ((ClassTypeSignature) o).writeTo(pack);
        } else if (o instanceof Annotation) {
            ((Annotation) o).writeTo(pack);
        } else if (o instanceof EnumConstant) {
            pack.startMap(2);
            EnumConstant e = (EnumConstant) o;
            pack.writeString("enumtype").writeString(e.getEnumType());
            pack.writeString("constant").writeString(e.getConstantName());
            pack.endMap();
        } else {
            throw new IllegalStateException("Cannot pack " + o.getClass().getSimpleName());
        }
        pack.endMap();
    }

    /**
     * Writes this annotation to the given {@link MessagePacker}.
     */
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(4);
        pack.writeString("id").writeInt(AstSerializer.ENTRY_ID_ANNOTATION);
        pack.writeString("typename").writeString(this.type.getName());
        pack.writeString("runtime").writeBool(this.type.isRuntimeVisible());
        pack.writeString("values").startArray(this.values.size());
        for (String key : this.values.keySet()) {
            pack.startMap(4);
            pack.writeString("name").writeString(key);
            pack.writeString("type").writeString(this.type.getType(key).getName());
            pack.writeString("default");
            writeObj(this.type.getDefaultValue(key), pack);
            pack.writeString("value");
            writeObj(this.values.get(key), pack);
            pack.endMap();
        }
        pack.endArray();
        pack.endMap();
    }

    public void accept(AstVisitor visitor) {
        if (visitor instanceof TypeVisitor) {
            ((TypeVisitor) visitor).visitAnnotation(this);
        }
    }

    public static class EnumConstant {

        private final String type_name;
        private final String constant;

        public EnumConstant(String type, String cst) {
            this.type_name = type;
            this.constant = cst;
        }

        public String getEnumType() {
            return this.type_name;
        }

        public String getConstantName() {
            return this.constant;
        }
    }

}
