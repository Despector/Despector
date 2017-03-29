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
package org.spongepowered.despector.decompiler;

import org.spongepowered.despector.Language;
import org.spongepowered.despector.decompiler.kotlin.method.graph.create.ElvisGraphProducerStep;
import org.spongepowered.despector.decompiler.kotlin.method.graph.operate.KotlinTernaryPrePassOperation;
import org.spongepowered.despector.decompiler.kotlin.method.special.KotlinLocalsProcessor;
import org.spongepowered.despector.decompiler.kotlin.step.KotlinMethodInfoStep;
import org.spongepowered.despector.decompiler.method.MethodDecompiler;
import org.spongepowered.despector.decompiler.method.graph.create.JumpGraphProducerStep;
import org.spongepowered.despector.decompiler.method.graph.create.SwitchGraphProducerStep;
import org.spongepowered.despector.decompiler.method.graph.create.TryCatchGraphProducerStep;
import org.spongepowered.despector.decompiler.method.graph.operate.BlockTargetOperation;
import org.spongepowered.despector.decompiler.method.graph.operate.BreakPrePassOperation;
import org.spongepowered.despector.decompiler.method.graph.operate.EmptyBlockClearOperation;
import org.spongepowered.despector.decompiler.method.graph.operate.JumpSeparateOperation;
import org.spongepowered.despector.decompiler.method.graph.operate.TernaryPrePassOperation;
import org.spongepowered.despector.decompiler.method.graph.process.InternalBlockProcessor;
import org.spongepowered.despector.decompiler.method.graph.process.SubRegionBlockProcessor;
import org.spongepowered.despector.decompiler.method.graph.process.SwitchBlockProcessor;
import org.spongepowered.despector.decompiler.method.graph.process.TryCatchBlockProcessor;
import org.spongepowered.despector.decompiler.method.graph.region.ChildRegionProcessor;
import org.spongepowered.despector.decompiler.method.graph.region.DoWhileRegionProcessor;
import org.spongepowered.despector.decompiler.method.graph.region.IfBlockRegionProcessor;
import org.spongepowered.despector.decompiler.method.graph.region.WhileRegionProcessor;
import org.spongepowered.despector.decompiler.method.postprocess.ForEachPostProcessor;
import org.spongepowered.despector.decompiler.method.postprocess.ForFromWhilePostProcessor;
import org.spongepowered.despector.decompiler.method.postprocess.IfCombiningPostProcessor;
import org.spongepowered.despector.decompiler.method.special.LocalsProcessor;
import org.spongepowered.despector.decompiler.step.ClassInfoStep;
import org.spongepowered.despector.decompiler.step.EnumConstantsStep;
import org.spongepowered.despector.decompiler.step.FieldInfoStep;
import org.spongepowered.despector.decompiler.step.MethodInfoStep;

import java.util.EnumMap;

public class Decompilers {

    public static final BaseDecompiler JAVA = new BaseDecompiler(Language.JAVA);
    public static final BaseDecompiler KOTLIN = new BaseDecompiler(Language.KOTLIN);
    public static final WildDecompiler WILD = new WildDecompiler();

    public static final MethodDecompiler JAVA_METHOD = new MethodDecompiler();
    public static final MethodDecompiler KOTLIN_METHOD = new MethodDecompiler();

    private static final EnumMap<Language, Decompiler> DECOMPILERS = new EnumMap<>(Language.class);

    static {
        JAVA.addStep(new ClassInfoStep());
        JAVA.addStep(new FieldInfoStep());
        JAVA.addStep(new MethodInfoStep(JAVA_METHOD));
        JAVA.addStep(new EnumConstantsStep());

        KOTLIN.addStep(new ClassInfoStep());
        KOTLIN.addStep(new FieldInfoStep());
        KOTLIN.addStep(new KotlinMethodInfoStep(KOTLIN_METHOD));
        KOTLIN.addStep(new EnumConstantsStep());

        JAVA_METHOD.addGraphProducer(new JumpGraphProducerStep());
        JAVA_METHOD.addGraphProducer(new SwitchGraphProducerStep());
        JAVA_METHOD.addGraphProducer(new TryCatchGraphProducerStep());
        JAVA_METHOD.addCleanupOperation(new EmptyBlockClearOperation());
        JAVA_METHOD.addCleanupOperation(new JumpSeparateOperation());
        JAVA_METHOD.addCleanupOperation(new BlockTargetOperation());
        JAVA_METHOD.addCleanupOperation(new TernaryPrePassOperation());
        JAVA_METHOD.addCleanupOperation(new BreakPrePassOperation());
        JAVA_METHOD.addProcessor(new TryCatchBlockProcessor());
        JAVA_METHOD.addProcessor(new InternalBlockProcessor());
        JAVA_METHOD.addProcessor(new SwitchBlockProcessor());
        JAVA_METHOD.addProcessor(new SubRegionBlockProcessor());
        JAVA_METHOD.addRegionProcessor(new ChildRegionProcessor());
        JAVA_METHOD.addRegionProcessor(new DoWhileRegionProcessor());
        JAVA_METHOD.addRegionProcessor(new WhileRegionProcessor());
        JAVA_METHOD.addRegionProcessor(new IfBlockRegionProcessor());
        JAVA_METHOD.addPostProcessor(new IfCombiningPostProcessor());
        JAVA_METHOD.addPostProcessor(new ForFromWhilePostProcessor());
        JAVA_METHOD.addPostProcessor(new ForEachPostProcessor());

        KOTLIN_METHOD.addGraphProducer(new JumpGraphProducerStep());
        KOTLIN_METHOD.addGraphProducer(new SwitchGraphProducerStep());
        KOTLIN_METHOD.addGraphProducer(new TryCatchGraphProducerStep());
        KOTLIN_METHOD.addGraphProducer(new ElvisGraphProducerStep());
        KOTLIN_METHOD.addCleanupOperation(new EmptyBlockClearOperation());
        KOTLIN_METHOD.addCleanupOperation(new JumpSeparateOperation());
        KOTLIN_METHOD.addCleanupOperation(new BlockTargetOperation());
        KOTLIN_METHOD.addCleanupOperation(new BreakPrePassOperation());
        KOTLIN_METHOD.addCleanupOperation(new KotlinTernaryPrePassOperation());
        KOTLIN_METHOD.addProcessor(new TryCatchBlockProcessor());
        KOTLIN_METHOD.addProcessor(new InternalBlockProcessor());
        KOTLIN_METHOD.addProcessor(new SwitchBlockProcessor());
        KOTLIN_METHOD.addProcessor(new SubRegionBlockProcessor());
        KOTLIN_METHOD.addRegionProcessor(new ChildRegionProcessor());
        KOTLIN_METHOD.addRegionProcessor(new DoWhileRegionProcessor());
        KOTLIN_METHOD.addRegionProcessor(new WhileRegionProcessor());
        KOTLIN_METHOD.addRegionProcessor(new IfBlockRegionProcessor());
        KOTLIN_METHOD.addPostProcessor(new IfCombiningPostProcessor());
        KOTLIN_METHOD.addPostProcessor(new ForEachPostProcessor());
        KOTLIN_METHOD.setSpecialProcessor(LocalsProcessor.class, new KotlinLocalsProcessor());

        DECOMPILERS.put(Language.JAVA, JAVA);
        DECOMPILERS.put(Language.KOTLIN, KOTLIN);
        DECOMPILERS.put(Language.ANY, WILD);
    }

    public static Decompiler get(Language lang) {
        return DECOMPILERS.get(lang);
    }

}
