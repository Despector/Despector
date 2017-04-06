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
package org.spongepowered.despector.emitter.output;

public enum TokenType {

    BEGIN_CLASS,
    END_CLASS,
    BEGIN_FIELD,
    END_FIELD,
    BEGIN_METHOD,
    END_METHOD,
    BEGIN_STATEMENT,
    END_STATEMENT,

    RAW,
    NAME,
    ACCESS,
    SPECIAL,
    MODIFIER,
    TYPE,
    SUPERCLASS,
    INTERFACE,
    PACKAGE,

    GENERIC_PARAMS,
    BLOCK_START,
    BLOCK_END,
    LEFT_PAREN,
    RIGHT_PAREN,
    ARG_SEPARATOR,
    LEFT_BRACKET,
    RIGHT_BRACKET,

    ENUM_CONSTANT,

    COMMENT,
    BLOCK_COMMENT,

    ARRAY_INITIALIZER_START,
    ARRAY_INITIALIZER_END,

    DOT,
    EQUALS,
    OPERATOR_EQUALS,
    OPERATOR,
    FOR_EACH,
    FOR_SEPARATOR,
    TERNARY_IF,
    TERNARY_ELSE,
    LAMBDA,
    RETURN,
    IF,
    ELSE_IF,
    ELSE,

    INT,
    LONG,
    FLOAT,
    DOUBLE,
    STRING,
    BOOLEAN,
    CHAR,

    WHEN_CASE,

    INSERT_IMPORTS,

}
