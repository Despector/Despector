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
package org.spongepowered.despector.util;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various utility methods for working with types.
 */
public final class TypeHelper {

    private static final Pattern ANON_CLASS = Pattern.compile(".*\\$[0-9]+");

    public static boolean isAnonClass(String name) {
        return ANON_CLASS.matcher(name).matches();
    }

    public static String descToTypeName(String desc) {
        return descToType(desc).replace('/', '.');
    }

    /**
     * Converts a description to a type name.
     */
    public static String descToType(String desc) {
        if (desc.startsWith("[")) {
            return descToType(desc.substring(1)) + "[]";
        }
        if (desc.startsWith("L") && desc.endsWith(";")) {
            return desc.substring(1, desc.length() - 1);
        }
        if (desc.startsWith("T")) {
            // TODO parse the bounds of the generic
            return "java/lang/Object";
        }
        if (desc.equals("I")) {
            return "int";
        }
        if (desc.equals("S")) {
            return "short";
        }
        if (desc.equals("B")) {
            return "byte";
        }
        if (desc.equals("Z")) {
            return "boolean";
        }
        if (desc.equals("F")) {
            return "float";
        }
        if (desc.equals("D")) {
            return "double";
        }
        if (desc.equals("J")) {
            return "long";
        }
        if (desc.equals("C")) {
            return "char";
        }
        if (desc.equals("V")) {
            return "void";
        }
        return desc;
    }

    /**
     * Resolves the given type name to the actual class.
     */
    public static Class<?> classForTypeName(String obf) {
        Class<?> actual_class = null;
        if ("void".equals(obf)) {
            actual_class = void.class;
        } else if ("char".equals(obf)) {
            actual_class = char.class;
        } else if ("boolean".equals(obf)) {
            actual_class = boolean.class;
        } else if ("byte".equals(obf)) {
            actual_class = byte.class;
        } else if ("short".equals(obf)) {
            actual_class = short.class;
        } else if ("int".equals(obf)) {
            actual_class = int.class;
        } else if ("long".equals(obf)) {
            actual_class = long.class;
        } else if ("float".equals(obf)) {
            actual_class = float.class;
        } else if ("double".equals(obf)) {
            actual_class = double.class;
        } else {
            try {
                actual_class = Class.forName(obf.replace('/', '.'));
            } catch (ClassNotFoundException e) {
                System.err.println("Failed to find class " + obf + " on classpath");
                Throwables.propagate(e);
            }
        }
        return actual_class;
    }

    /**
     * Counts the number of parameters in the given method signature.
     */
    public static int paramCount(String sig) {
        if (sig == null) {
            return 0;
        }
        int depth = 0;
        int count = 0;
        for (int i = sig.indexOf('(') + 1; i < sig.length(); i++) {
            char next = sig.charAt(i);
            if (depth > 0) {
                if (next == '>') {
                    depth--;
                } else if (next == '<') {
                    depth++;
                }
                continue;
            }
            if (next == ')') {
                break;
            }
            if (next == '[') {
                continue;
            }
            if (next == '<') {
                depth++;
            }
            if (next == 'L' || next == 'T') {
                // Generics may be arbitrarily nested so we need to ensure we
                // parse until the ';' at the same level that we started.
                int generic_depth = 0;
                while (next != ';' || generic_depth > 0) {
                    if (next == '<') {
                        generic_depth++;
                        next = sig.charAt(++i);
                        continue;
                    }
                    if (generic_depth > 0) {
                        if (next == '>') {
                            generic_depth--;
                        } else if (next == '<') {
                            generic_depth++;
                        }
                    }
                    next = sig.charAt(++i);
                }
            }
            count++;
        }
        return count;
    }

    public static List<String> splitSig(String sig) {
        if (sig == null) {
            return null;
        }
        List<String> params = Lists.newArrayList();
        String accu = "";
        boolean is_array = false;
        int depth = 0;
        for (int i = sig.indexOf('(') + 1; i < sig.length(); i++) {
            char next = sig.charAt(i);
            if (depth > 0) {
                if (next == '>') {
                    depth--;
                } else if (next == '<') {
                    depth++;
                }
                continue;
            }
            if (next == ')') {
                break;
            }
            if (next == '[') {
                is_array = true;
                continue;
            }
            if (next == '<') {
                depth++;
            }
            if (next == 'L' || next == 'T') {
                int generics_depth = 0;
                while (next != ';' || generics_depth > 0) {
                    if (next == '<') {
                        generics_depth++;
                        next = sig.charAt(++i);
                        continue;
                    }
                    if (generics_depth > 0) {
                        if (next == '>') {
                            generics_depth--;
                        } else if (next == '<') {
                            generics_depth++;
                        }
                    } else {
                        accu += next;
                    }
                    next = sig.charAt(++i);
                }
                accu += next;
            } else {
                accu += next;
            }
            if (is_array) {
                accu = "[" + accu;
            }
            params.add(accu);
            accu = "";
            is_array = false;
        }
        return params;
    }

    private static final Pattern DESC = Pattern.compile("\\([^\\)]*\\)(.*)");

    /**
     * Gets the return value from the given method signature.
     */
    public static String getRet(String signature) {
        Matcher matcher = DESC.matcher(signature);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "V";
    }

    public static boolean isPrimative(String type) {
        if ("void".equals(type) || "boolean".equals(type) || "byte".equals(type) || "short".equals(type) || "int".equals(type) || "long".equals(type)
                || "float".equals(type) || "double".equals(type) || "char".equals(type)) {
            return true;
        }
        return false;
    }

    public static final Predicate<String> IS_NUMBER = (t) -> "BSIJFDC".indexOf(t.charAt(0)) != -1;
    public static final Predicate<String> IS_NUMBER_OR_BOOL = (t) -> "ZBSIJFDC".indexOf(t.charAt(0)) != -1;
    public static final Predicate<String> IS_OBJECT = (t) -> t.startsWith("L");
    public static final Predicate<String> IS_ARRAY = (t) -> t.startsWith("[");
    public static final Predicate<String> IS_OBJECT_OR_ARRAY = (t) -> {
        while (t.startsWith("[")) {
            t = t.substring(1);
        }
        return t.startsWith("L");
    };

    private TypeHelper() {
    }

}
