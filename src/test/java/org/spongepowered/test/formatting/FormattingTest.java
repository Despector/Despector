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
package org.spongepowered.test.formatting;

import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.Emitters;
import org.spongepowered.despector.emitter.format.EmitterFormat;
import org.spongepowered.test.util.TestHelper;

import java.io.StringWriter;

/**
 * Writing unit tests for all cases of formatting would be exhausting so this is
 * a simple test rig for playing with formatting easily and checking the
 * results.
 */
public class FormattingTest {

    public static void main(String[] args) {
        TypeEntry type = TestHelper.get(FormattingTestClass.class);

        EmitterFormat format = new EmitterFormat();
        // Configure format for testing here
        
        

        StringWriter writer = new StringWriter();
        EmitterContext emitter = new EmitterContext(writer, format);
        emitter.setEmitterSet(Emitters.JAVA_SET);
        emitter.emitOuterType(type);
        emitter.flush();

        System.out.println(writer.toString());

    }

}
