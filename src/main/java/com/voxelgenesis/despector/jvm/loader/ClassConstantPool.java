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

import com.google.common.base.Charsets;
import com.voxelgenesis.despector.core.loader.SourceFormatException;

import java.io.DataInputStream;
import java.io.IOException;

public class ClassConstantPool {

    private static final boolean DUMP_CONSTANT_POOL = Boolean.getBoolean("despect.debug.jvm.dump_constant_pool");

    private Entry[] values;

    public ClassConstantPool() {
    }

    public void load(DataInputStream data) throws IOException {
        int entry_count = data.readUnsignedShort();

        this.values = new Entry[entry_count - 1];

        for (int i = 0; i < entry_count - 1; i++) {
            int tag = data.readUnsignedByte();
            EntryType type = EntryType.values()[tag];
            switch (type) {
            case UTF8: {
                Utf8 u = new Utf8();
                int len = data.readUnsignedShort();
                byte[] bytes = new byte[len];
                data.read(bytes, 0, len);
                u.value = new String(bytes, Charsets.UTF_8);
                this.values[i] = u;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": Utf8 " + u.value);
                }
                break;
            }
            case INTEGER: {
                IntConstant c = new IntConstant();
                c.value = data.readInt();
                this.values[i] = c;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": Int " + c.value);
                }
                break;
            }
            case FLOAT: {
                FloatConstant c = new FloatConstant();
                c.value = data.readFloat();
                this.values[i] = c;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": Float " + c.value);
                }
                break;
            }
            case LONG: {
                LongConstant c = new LongConstant();
                long l = ((long) data.readInt() << 32);
                l |= data.readInt();
                c.value = l;
                i++;
                this.values[i] = c;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": Long " + c.value);
                }
                break;
            }
            case DOUBLE: {
                DoubleConstant c = new DoubleConstant();
                long l = ((long) data.readInt() << 32);
                l |= data.readInt();
                c.value = Double.longBitsToDouble(l);
                i++;
                this.values[i] = c;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": Double " + c.value);
                }
                break;
            }
            case CLASS: {
                ClassEntry e = new ClassEntry();
                e.name_index = data.readUnsignedShort();
                this.values[i] = e;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": Class " + e.name_index);
                }
                break;
            }
            case STRING: {
                StringEntry e = new StringEntry();
                e.value_index = data.readUnsignedShort();
                this.values[i] = e;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": String " + e.value_index);
                }
                break;
            }
            case FIELD_REF: {
                FieldRef f = new FieldRef();
                f.class_index = data.readUnsignedShort();
                f.name_and_type_index = data.readUnsignedShort();
                this.values[i] = f;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": FieldRef " + f.class_index + " " + f.name_and_type_index);
                }
                break;
            }
            case METHOD_REF: {
                MethodRef f = new MethodRef();
                f.class_index = data.readUnsignedShort();
                f.name_and_type_index = data.readUnsignedShort();
                this.values[i] = f;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": MethodRef " + f.class_index + " " + f.name_and_type_index);
                }
                break;
            }
            case INTERFACE_METHOD_REF: {
                InterfaceMethodRef f = new InterfaceMethodRef();
                f.class_index = data.readUnsignedShort();
                f.name_and_type_index = data.readUnsignedShort();
                this.values[i] = f;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": InterfaceMethodRef " + f.class_index + " " + f.name_and_type_index);
                }
                break;
            }
            case NAME_AND_TYPE: {
                NameAndType n = new NameAndType();
                n.name_index = data.readUnsignedShort();
                n.type_index = data.readUnsignedShort();
                this.values[i] = n;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": NameAndType " + n.name_index + " " + n.type_index);
                }
                break;
            }
            case METHOD_HANDLE: {
                MethodHandle h = new MethodHandle();
                h.kind = data.readByte();
                h.reference_index = data.readUnsignedShort();
                this.values[i] = h;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": MethodHandle " + h.kind + " " + h.reference_index);
                }
                break;
            }
            case METHOD_TYPE: {
                MethodType t = new MethodType();
                t.desc_index = data.readUnsignedShort();
                this.values[i] = t;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": MethodType " + t.desc_index);
                }
                break;
            }
            case INVOKE_DYNAMIC: {
                InvokeDynamic d = new InvokeDynamic();
                d.bootstrap_index = data.readUnsignedShort();
                d.name_and_type_index = data.readUnsignedShort();
                this.values[i] = d;
                if (DUMP_CONSTANT_POOL) {
                    System.out.println(i + ": InvokeDynamic " + d.bootstrap_index + " " + d.name_and_type_index);
                }
                break;
            }
            default:
                throw new SourceFormatException("Illegal tag in constant pool");
            }
            this.values[i].type = type;
        }

        // bake references
        for (int i = 0; i < entry_count - 1; i++) {
            Entry e = this.values[i];
            switch (e.type) {
            case UTF8:
            case INTEGER:
            case FLOAT:
                break;
            case LONG:
            case DOUBLE:
                i++;
                break;
            case CLASS: {
                ClassEntry c = (ClassEntry) e;
                c.name = getUtf8(c.name_index);
                break;
            }
            case STRING: {
                StringEntry c = (StringEntry) e;
                c.value = getUtf8(c.value_index);
                break;
            }
            case FIELD_REF: {
                FieldRef f = (FieldRef) e;
                f.cls = getClass(f.class_index).name;
                f.name = getNameAndType(f.name_and_type_index).name;
                f.type = getNameAndType(f.name_and_type_index).type;
                break;
            }
            case METHOD_REF: {
                MethodRef f = (MethodRef) e;
                f.cls = getClass(f.class_index).name;
                f.name = getNameAndType(f.name_and_type_index).name;
                f.type = getNameAndType(f.name_and_type_index).type;
                break;
            }
            case INTERFACE_METHOD_REF: {
                InterfaceMethodRef f = (InterfaceMethodRef) e;
                f.cls = getClass(f.class_index).name;
                f.name = getUtf8(getNameAndType(f.name_and_type_index).name_index);
                f.type = getUtf8(getNameAndType(f.name_and_type_index).type_index);
                break;
            }
            case NAME_AND_TYPE: {
                NameAndType n = (NameAndType) e;
                n.name = getUtf8(n.name_index);
                n.type = getUtf8(n.type_index);
                break;
            }
            case METHOD_HANDLE:
                break;
            case METHOD_TYPE: {
                MethodType t = (MethodType) e;
                t.desc = getUtf8(t.desc_index);
                break;
            }
            case INVOKE_DYNAMIC:
                break;
            default:
                throw new SourceFormatException("Illegal tag in constant pool");
            }
        }
    }

    public String getUtf8(int index) {
        return ((Utf8) this.values[index - 1]).value;
    }

    public int getInt(int index) {
        return ((IntConstant) this.values[index - 1]).value;
    }

    public float getFloat(int index) {
        return ((FloatConstant) this.values[index - 1]).value;
    }

    public long getLong(int index) {
        return ((LongConstant) this.values[index - 1]).value;
    }

    public double getDouble(int index) {
        return ((DoubleConstant) this.values[index - 1]).value;
    }

    public ClassEntry getClass(int index) {
        return (ClassEntry) this.values[index - 1];
    }

    public NameAndType getNameAndType(int index) {
        return (NameAndType) this.values[index - 1];
    }

    public FieldRef getFieldRef(int index) {
        return (FieldRef) this.values[index - 1];
    }

    public MethodRef getMethodRef(int index) {
        return (MethodRef) this.values[index - 1];
    }

    public InterfaceMethodRef getInterfaceMethodRef(int index) {
        return (InterfaceMethodRef) this.values[index - 1];
    }

    public static abstract class Entry {

        public EntryType type;
    }

    public static class Utf8 extends Entry {

        public String value;
    }

    public static class IntConstant extends Entry {

        public int value;
    }

    public static class FloatConstant extends Entry {

        public float value;
    }

    public static class LongConstant extends Entry {

        public long value;
    }

    public static class DoubleConstant extends Entry {

        public double value;
    }

    public static class StringEntry extends Entry {

        public int value_index;
        public String value;
    }

    public static class ClassEntry extends Entry {

        public int name_index;
        public String name;
    }

    public static class NameAndType extends Entry {

        public int name_index;
        public int type_index;

        public String name;
        public String type;
    }

    public static class FieldRef extends Entry {

        public int class_index;
        public int name_and_type_index;

        public String cls;
        public String name;
        public String type;
    }

    public static class MethodRef extends Entry {

        public int class_index;
        public int name_and_type_index;

        public String cls;
        public String name;
        public String type;
    }

    public static class InterfaceMethodRef extends Entry {

        public int class_index;
        public int name_and_type_index;

        public String cls;
        public String name;
        public String type;
    }

    public static class MethodHandle extends Entry {

        public byte kind;
        public int reference_index;
    }

    public static class MethodType extends Entry {

        public int desc_index;

        public String desc;
    }

    public static class InvokeDynamic extends Entry {

        public int bootstrap_index;
        public int name_and_type_index;
    }

    private static enum EntryType {
        _0,
        UTF8,
        _2,
        INTEGER,
        FLOAT,
        LONG,
        DOUBLE,
        CLASS,
        STRING,
        FIELD_REF,
        METHOD_REF,
        INTERFACE_METHOD_REF,
        NAME_AND_TYPE,
        _13,
        _14,
        METHOD_HANDLE,
        METHOD_TYPE,
        INVOKE_DYNAMIC,
    }

}
