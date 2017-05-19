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
package org.spongepowered.despector.source.ast;

import org.spongepowered.despector.ast.type.TypeEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceFile {

    private static final Map<String, String> DEFAULT_IMPORTS = new HashMap<>();

    static {
        DEFAULT_IMPORTS.put("AbstractMethodError", "java/lang/AbstractMethodError");
        DEFAULT_IMPORTS.put("Appendable", "java/lang/Appendable");
        DEFAULT_IMPORTS.put("ArithmeticException", "java/lang/ArithmeticException");
        DEFAULT_IMPORTS.put("ArrayIndexOutOfBoundsException", "java/lang/ArrayIndexOutOfBoundsException");
        DEFAULT_IMPORTS.put("ArrayStoreException", "java/lang/ArrayStoreException");
        DEFAULT_IMPORTS.put("AssertionError", "java/lang/AssertionError");
        DEFAULT_IMPORTS.put("AutoCloseable", "java/lang/AutoCloseable");
        DEFAULT_IMPORTS.put("Boolean", "java/lang/Boolean");
        DEFAULT_IMPORTS.put("BootstrapMethodError", "java/lang/BootstrapMethodError");
        DEFAULT_IMPORTS.put("Byte", "java/lang/Byte");
        DEFAULT_IMPORTS.put("Character", "java/lang/Character");
        DEFAULT_IMPORTS.put("CharSequence", "java/lang/CharSequence");
        DEFAULT_IMPORTS.put("Class", "java/lang/Class");
        DEFAULT_IMPORTS.put("ClassCastException", "java/lang/ClassCastException");
        DEFAULT_IMPORTS.put("ClassCircularityError", "java/lang/ClassCircularityError");
        DEFAULT_IMPORTS.put("ClassFormatError", "java/lang/ClassFormatError");
        DEFAULT_IMPORTS.put("ClassLoader", "java/lang/ClassLoader");
        DEFAULT_IMPORTS.put("ClassNotFoundException", "java/lang/ClassNotFoundException");
        DEFAULT_IMPORTS.put("ClassValue", "java/lang/ClassValue");
        DEFAULT_IMPORTS.put("Cloneable", "java/lang/Cloneable");
        DEFAULT_IMPORTS.put("CloneNotSupportedException", "java/lang/CloneNotSupportedException");
        DEFAULT_IMPORTS.put("Comparable", "java/lang/Comparable");
        DEFAULT_IMPORTS.put("Compiler", "java/lang/Compiler");
        DEFAULT_IMPORTS.put("Deprecated", "java/lang/Deprecated");
        DEFAULT_IMPORTS.put("Double", "java/lang/Double");
        DEFAULT_IMPORTS.put("Enum", "java/lang/Enum");
        DEFAULT_IMPORTS.put("EnumConstantNotPresentException", "java/lang/EnumConstantNotPresentException");
        DEFAULT_IMPORTS.put("Error", "java/lang/Error");
        DEFAULT_IMPORTS.put("Exception", "java/lang/Exception");
        DEFAULT_IMPORTS.put("ExceptionInInitializerError", "java/lang/ExceptionInInitializerError");
        DEFAULT_IMPORTS.put("Float", "java/lang/Float");
        DEFAULT_IMPORTS.put("FunctionalInterface", "java/lang/FunctionalInterface");
        DEFAULT_IMPORTS.put("IllegalStateException", "java/lang/IllegalStateException");
        DEFAULT_IMPORTS.put("IllegalArgumentException", "java/lang/IllegalArgumentException");
        DEFAULT_IMPORTS.put("IllegalAccessError", "java/lang/IllegalAccessError");
        DEFAULT_IMPORTS.put("IllegalAccessException", "java/lang/IllegalAccessException");
        DEFAULT_IMPORTS.put("IllegalMonitorStateException", "java/lang/IllegalMonitorStateException");
        DEFAULT_IMPORTS.put("IllegalThreadStateException", "java/lang/IllegalThreadStateException");
        DEFAULT_IMPORTS.put("IncompatibleClassChangeError", "java/lang/IncompatibleClassChangeError");
        DEFAULT_IMPORTS.put("IndexOutOfBoundsException", "java/lang/IndexOutOfBoundsException");
        DEFAULT_IMPORTS.put("InheritableThreadLocal", "java/lang/InheritableThreadLocal");
        DEFAULT_IMPORTS.put("InstantiationError", "java/lang/InstantiationError");
        DEFAULT_IMPORTS.put("InstantiationException", "java/lang/InstantiationException");
        DEFAULT_IMPORTS.put("Integer", "java/lang/Integer");
        DEFAULT_IMPORTS.put("InternalError", "java/lang/InternalError");
        DEFAULT_IMPORTS.put("InterruptedException", "java/lang/InterruptedException");
        DEFAULT_IMPORTS.put("Iterable", "java/lang/Iterable");
        DEFAULT_IMPORTS.put("LinkageError", "java/lang/LinkageError");
        DEFAULT_IMPORTS.put("Long", "java/lang/Long");
        DEFAULT_IMPORTS.put("Math", "java/lang/Math");
        DEFAULT_IMPORTS.put("NegativeArraySizeException", "java/lang/NegativeArraySizeException");
        DEFAULT_IMPORTS.put("NoClassDefFoundError", "java/lang/NoClassDefFoundError");
        DEFAULT_IMPORTS.put("NoSuchFieldError", "java/lang/NoSuchFieldError");
        DEFAULT_IMPORTS.put("NoSuchFieldException", "java/lang/NoSuchFieldException");
        DEFAULT_IMPORTS.put("NoSuchMethodError", "java/lang/NoSuchMethodError");
        DEFAULT_IMPORTS.put("NoSuchMethodException", "java/lang/NoSuchMethodException");
        DEFAULT_IMPORTS.put("NullPointerException", "java/lang/NullPointerException");
        DEFAULT_IMPORTS.put("Number", "java/lang/Number");
        DEFAULT_IMPORTS.put("NumberFormatException", "java/lang/NumberFormatException");
        DEFAULT_IMPORTS.put("Object", "java/lang/Object");
        DEFAULT_IMPORTS.put("Override", "java/lang/Override");
        DEFAULT_IMPORTS.put("OutOfMemoryError", "java/lang/OutOfMemoryError");
        DEFAULT_IMPORTS.put("Package", "java/lang/Package");
        DEFAULT_IMPORTS.put("Process", "java/lang/Process");
        DEFAULT_IMPORTS.put("ProcessBuilder", "java/lang/ProcessBuilder");
        DEFAULT_IMPORTS.put("Readable", "java/lang/Readable");
        DEFAULT_IMPORTS.put("ReflectiveOperationException", "java/lang/ReflectiveOperationException");
        DEFAULT_IMPORTS.put("Runnable", "java/lang/Runnable");
        DEFAULT_IMPORTS.put("Runtime", "java/lang/Runtime");
        DEFAULT_IMPORTS.put("RuntimeException", "java/lang/RuntimeException");
        DEFAULT_IMPORTS.put("RuntimePermission", "java/lang/RuntimePermission");
        DEFAULT_IMPORTS.put("Short", "java/lang/Short");
        DEFAULT_IMPORTS.put("String", "java/lang/String");
        DEFAULT_IMPORTS.put("System", "java/lang/System");
        DEFAULT_IMPORTS.put("SafeVarargs", "java/lang/SafeVarargs");
        DEFAULT_IMPORTS.put("SecurityException", "java/lang/SecurityException");
        DEFAULT_IMPORTS.put("SecurityManager", "java/lang/SecurityManager");
        DEFAULT_IMPORTS.put("StackOverflowError", "java/lang/StackOverflowError");
        DEFAULT_IMPORTS.put("StackTraceElement", "java/lang/StackTraceElement");
        DEFAULT_IMPORTS.put("StrictMath", "java/lang/StrictMath");
        DEFAULT_IMPORTS.put("StringBuffer", "java/lang/StringBuffer");
        DEFAULT_IMPORTS.put("StringBuilder", "java/lang/StringBuilder");
        DEFAULT_IMPORTS.put("StringIndexOutOfBoundsException", "java/lang/StringIndexOutOfBoundsException");
        DEFAULT_IMPORTS.put("SuppressWarnings", "java/lang/SuppressWarnings");
        DEFAULT_IMPORTS.put("Thread", "java/lang/Thread");
        DEFAULT_IMPORTS.put("ThreadDeath", "java/lang/ThreadDeath");
        DEFAULT_IMPORTS.put("ThreadGroup", "java/lang/ThreadGroup");
        DEFAULT_IMPORTS.put("ThreadLocal", "java/lang/ThreadLocal");
        DEFAULT_IMPORTS.put("Throwable", "java/lang/Throwable");
        DEFAULT_IMPORTS.put("TypeNotPresentException", "java/lang/TypeNotPresentException");
        DEFAULT_IMPORTS.put("UnknownError", "java/lang/UnknownError");
        DEFAULT_IMPORTS.put("UnsatisfiedLinkError", "java/lang/UnsatisfiedLinkError");
        DEFAULT_IMPORTS.put("UnsupportedOperationException", "java/lang/UnsupportedOperationException");
        DEFAULT_IMPORTS.put("UnsupportedClassVersionError", "java/lang/UnsupportedClassVersionError");
        DEFAULT_IMPORTS.put("VerifyError", "java/lang/VerifyError");
        DEFAULT_IMPORTS.put("VirtualMachineError", "java/lang/VirtualMachineError");
        DEFAULT_IMPORTS.put("Void", "java/lang/Void");
    }

    protected final String name;

    protected final List<TypeEntry> top_types = new ArrayList<>();
    protected final List<TypeEntry> all_types = new ArrayList<>();

    protected Map<String, String> default_imports;

    protected List<String> header;
    protected String pkg = "";
    protected final List<String> imports = new ArrayList<>();

    public SourceFile(String name) {
        this.name = name;
        this.default_imports = DEFAULT_IMPORTS;
    }

    public String getName() {
        return this.name;
    }

    public List<TypeEntry> getTopLevelTypes() {
        return this.top_types;
    }

    public void addTopLevelType(TypeEntry type) {
        this.top_types.add(type);
        this.all_types.add(type);
    }

    public List<TypeEntry> getAllTypes() {
        return this.all_types;
    }

    public void addInnerType(TypeEntry type) {
        this.all_types.add(type);
    }

    public List<String> getHeader() {
        return this.header;
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }

    public String getPackage() {
        return this.pkg;
    }

    public void setPackage(String pkg) {
        this.pkg = pkg;
    }

    public List<String> getImports() {
        return this.imports;
    }

    public void addImport(String im) {
        this.imports.add(im);
    }

    public String resolveType(String type) {
        String def = this.default_imports.get(type);
        if (def != null) {
            return def;
        }
        for (String im : this.imports) {
            if (im.endsWith(type) && im.charAt(im.length() - type.length()) == '.') {
                return im.replace('.', '/');
            }
        }
        return null;
    }

}
