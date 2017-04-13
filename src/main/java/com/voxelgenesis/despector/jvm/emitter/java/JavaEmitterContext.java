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
package com.voxelgenesis.despector.jvm.emitter.java;

import static com.google.common.base.Preconditions.checkArgument;

import com.voxelgenesis.despector.core.ast.signature.TypeSignature;
import com.voxelgenesis.despector.core.emitter.AbstractEmitterContext;
import com.voxelgenesis.despector.core.emitter.EmitterContext;
import com.voxelgenesis.despector.core.emitter.EmitterSet;
import com.voxelgenesis.despector.core.emitter.format.BracePosition;
import com.voxelgenesis.despector.core.emitter.format.EmitterFormat;
import com.voxelgenesis.despector.core.emitter.format.WrappingStyle;
import org.spongepowered.despector.util.TypeHelper;

import java.io.IOException;
import java.io.Writer;

public class JavaEmitterContext extends AbstractEmitterContext {

    private final ImportManager import_manager = new ImportManager();

    private EmitterFormat format;
    private final Writer output;

    private int indentation = 0;
    private int offs = 0;

    private int line_length = 0;
    private int wrap_point = -1;
    private StringBuilder line_buffer = new StringBuilder();
    private boolean is_wrapped = false;

    public JavaEmitterContext(EmitterSet set, Writer out, EmitterFormat format) {
        super(set);
        this.output = out;
        this.format = format;
    }

    @Override
    public EmitterFormat getFormat() {
        return this.format;
    }

    /**
     * Increases the indentation level by one.
     */
    @Override
    public EmitterContext indent() {
        this.indentation++;
        return this;
    }

    /**
     * Decreases the indentation level by one.
     */
    @Override
    public EmitterContext dedent() {
        this.indentation--;
        return this;
    }

    /**
     * Prints the required indentation for the current indentation level.
     */
    @Override
    public EmitterContext printIndentation() {
        if (this.format.indent_with_spaces) {
            for (int i = 0; i < this.indentation * this.format.indentation_size; i++) {
                printString(" ");
            }
        } else {
            for (int i = 0; i < this.indentation; i++) {
                printString("\t");
            }
        }
        return this;
    }

    /**
     * Gets the string for the given type descriptor taking imports into
     * account.
     */
    @Override
    public String getType(String name) {
        return getTypeName(TypeHelper.descToType(name));
    }

    /**
     * Gets the string for the given type internal name taking imports into
     * account.
     */
    @Override
    public String getTypeName(String name) {
        if (name.endsWith("[]")) {
            String n = getTypeName(name.substring(0, name.length() - 2));
            if (this.format.insert_space_before_opening_bracket_in_array_type_reference) {
                n += " ";
            }
            if (this.format.insert_space_between_brackets_in_array_type_reference) {
                n += "[ ]";
            } else {
                n += "[]";
            }
            return n;
        }
        if (name.indexOf('/') != -1) {
            if (this.import_manager.checkImport(name)) {
                name = name.substring(name.lastIndexOf('/') + 1);
            } else if (getCurrentType() != null) {
                String this_package = "";
                String target_package = name;
                String this_name = getCurrentType().getName();
                String outer_name = null;
                if (this_name.indexOf('/') != -1) {
                    this_package = this_name.substring(0, this_name.lastIndexOf('/'));
                    outer_name = this_name.substring(this_package.length() + 1);
                    if (outer_name.indexOf('$') != -1) {
                        outer_name = outer_name.substring(0, outer_name.indexOf('$'));
                    }
                    target_package = name.substring(0, name.lastIndexOf('/'));
                }
                if (this_package.equals(target_package)) {
                    name = name.substring(name.lastIndexOf('/') + 1);
                    if (name.startsWith(outer_name + "$")) {
                        name = name.substring(outer_name.length() + 1);
                    }
                }
            }
        }
        return name.replace('/', '.').replace('$', '.');
    }

    /**
     * Emits the given type signature taking imports into account.
     */
    @Override
    public EmitterContext emitType(TypeSignature sig) {
//        GenericsEmitter generics = this.set.getSpecialEmitter(GenericsEmitter.class);
//        generics.emitTypeSignature(this, sig);
        return this;
    }

    /**
     * Emits the given type descriptor taking imports into account.
     */
    @Override
    public EmitterContext emitType(String name) {
        emitTypeName(TypeHelper.descToType(name));
        return this;
    }

    /**
     * Emits the given type internal name taking imports into account.
     */
    @Override
    public EmitterContext emitTypeName(String name) {
        printString(getTypeName(name));
        return this;
    }

    /**
     * Inserts `count` new lines.
     */
    @Override
    public EmitterContext newLine(int count) {
        for (int i = 0; i < count; i++) {
            newLine();
        }
        return this;
    }

    /**
     * Inserts a new line.
     */
    @Override
    public EmitterContext newLine() {
        __newLine();
        if (this.is_wrapped) {
            for (int i = 0; i < this.format.continuation_indentation; i++) {
                dedent();
            }
            this.is_wrapped = false;
        }
        return this;
    }

    /**
     * Flushes the line buffer to the output.
     */
    public void flush() {
        try {
            this.output.write(this.line_buffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * inserts a new line without resetting wrapping.
     */
    private void __newLine() {
        flush();
        this.offs += 1;
        try {
            this.output.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.line_length = 0;
        this.wrap_point = -1;
        this.line_buffer.setLength(0);
    }

    /**
     * Inserts a new line and indents.
     */
    @Override
    public void newIndentedLine() {
        newLine();
        printIndentation();
    }

    /**
     * Gets the length of the current line.
     */
    @Override
    public int getCurrentLength() {
        return this.line_length;
    }

    /**
     * Prints the given string to the output.
     */
    @Override
    public EmitterContext printString(String line) {
        checkArgument(line.indexOf('\n') == -1);
        this.offs += this.line_buffer.length();
        this.line_length += line.length();
        this.line_buffer.append(line);
        if (this.line_length > this.format.line_split) {
            if (this.wrap_point != -1) {
                String existing = this.line_buffer.toString();
                String next = existing.substring(this.wrap_point);
                this.line_buffer.setLength(this.wrap_point);
                this.wrap_point = -1;
                newLine();
                if (!this.is_wrapped) {
                    this.is_wrapped = true;
                    for (int i = 0; i < this.format.continuation_indentation; i++) {
                        indent();
                    }
                }
                printIndentation();
                printString(next);
            }
        }
        return this;
    }

    /**
     * Prints the given string if the condition is met.
     */
    @Override
    public EmitterContext printString(String line, boolean condition) {
        if (condition) {
            printString(line);
        }
        return this;
    }

    /**
     * Marks the current line posittion as a possible line break point.
     */
    @Override
    public EmitterContext markWrapPoint() {
        markWrapPoint(WrappingStyle.WRAP_WHEN_NEEDED, 0);
        return this;
    }

    /**
     * Wraps the current line at the current position.
     */
    private void wrap(boolean indent) {
        __newLine();
        if (!this.is_wrapped && indent) {
            this.is_wrapped = true;
            for (int i = 0; i < this.format.continuation_indentation; i++) {
                indent();
            }
        }
        printIndentation();
    }

    /**
     * Marks the current line posittion as a possible line break point depending
     * on the given wrapping style and index.
     */
    @Override
    public JavaEmitterContext markWrapPoint(WrappingStyle style, int index) {
        switch (style) {
        case DO_NOT_WRAP:
            break;
        case WRAP_ALL:
            wrap(false);
            break;
        case WRAP_ALL_AND_INDENT:
            wrap(true);
            break;
        case WRAP_ALL_EXCEPT_FIRST:
            if (index != 0) {
                wrap(true);
            }
            break;
        case WRAP_FIRST_OR_NEEDED:
            if (index == 0) {
                wrap(true);
            } else {
                this.wrap_point = this.line_length;
            }
            break;
        case WRAP_WHEN_NEEDED:
            this.wrap_point = this.line_length;
            break;
        default:
            break;
        }
        return this;
    }

    /**
     * Emits an opening brace with a position depending on the given
     * {@link BracePosition}.
     */
    @Override
    public void emitBrace(BracePosition pos, boolean wrapped, boolean with_space) {
        switch (pos) {
        case NEXT_LINE:
            newLine();
            printIndentation();
            printString("{");
            indent();
            break;
        case NEXT_LINE_ON_WRAP:
            if (wrapped) {
                newLine();
                printIndentation();
            } else if (with_space) {
                printString(" ");
            }
            printString("{");
            indent();
            break;
        case NEXT_LINE_SHIFTED:
            newLine();
            indent();
            printIndentation();
            printString("{");
            break;
        case SAME_LINE:
        default:
            if (with_space) {
                printString(" ");
            }
            printString("{");
            indent();
            break;
        }
    }

}
