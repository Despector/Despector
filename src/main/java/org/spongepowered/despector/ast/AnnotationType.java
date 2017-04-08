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
import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;

/**
 * An anotation type.
 */
public class AnnotationType {

    private final String name;
    private final Map<String, Class<?>> types = new HashMap<>();
    private final Map<String, Object> defaults = new HashMap<>();

    private boolean runtime;
    private boolean complete;

    public AnnotationType(String name) {
        this.name = checkNotNull(name, "name");
    }

    /**
     * Gets the type descriptor.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the type of the given annotation field.
     */
    public Class<?> getType(String key) {
        return this.types.get(key);
    }

    /**
     * Sets the type of the given annotation field.
     */
    public void setType(String key, Class<?> type) {
        checkArgument(Annotation.isValidValue(type), "Type " + type.getName() + " is not a valid annotation value type");
        checkState(!this.complete);
        this.types.put(key, type);
    }

    /**
     * Gets the default value of the given field.
     */
    public Object getDefaultValue(String key) {
        return this.defaults.get(key);
    }

    /**
     * Sets the default value of the given field.
     */
    public void setDefault(String key, Object value) {
        checkNotNull(this.types.get(key), "Cannot set default value before type");
        checkArgument(this.types.get(key).isInstance(value), "Value does not match type for annotation key");
        this.defaults.put(key, value);
    }

    /**
     * Gets if this annotation type is complete.
     */
    public boolean isComplete() {
        return this.complete;
    }

    /**
     * Marks if this annotation type is complete.
     */
    public void markComplete() {
        this.complete = true;
    }

    /**
     * Gets if this annotation is visible at runtime.
     */
    public boolean isRuntimeVisible() {
        return this.runtime;
    }

    /**
     * Sets if this annotation is visible at runtime.
     */
    public void setRuntimeVisible(boolean state) {
        this.runtime = state;
    }

}
