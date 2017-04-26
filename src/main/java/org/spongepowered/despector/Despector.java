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
package org.spongepowered.despector;

import org.spongepowered.despector.ast.SourceSet;
import org.spongepowered.despector.ast.type.TypeEntry;
import org.spongepowered.despector.config.ConfigBase.CleanupConfigSection;
import org.spongepowered.despector.config.ConfigManager;
import org.spongepowered.despector.decompiler.Decompiler;
import org.spongepowered.despector.decompiler.Decompilers;
import org.spongepowered.despector.decompiler.DirectoryWalker;
import org.spongepowered.despector.decompiler.JarWalker;
import org.spongepowered.despector.emitter.Emitter;
import org.spongepowered.despector.emitter.Emitters;
import org.spongepowered.despector.emitter.format.EmitterFormat;
import org.spongepowered.despector.emitter.format.FormatLoader;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.transform.TypeTransformer;
import org.spongepowered.despector.transform.cleanup.CleanupOperations;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Despector.
 */
public final class Despector {

    private static final Map<String, Consumer<String>> flags = new HashMap<>();

    public static Language LANGUAGE = Language.ANY;

    static {
        flags.put("--config=", (arg) -> {
            String config = arg.substring(9);
            Path config_path = Paths.get(".").resolve(config);
            ConfigManager.load(config_path);
        });
        flags.put("--lang=", (arg) -> {
            String lang = arg.substring(7);
            if ("kotlin".equalsIgnoreCase(lang)) {
                LANGUAGE = Language.KOTLIN;
            } else if ("java".equalsIgnoreCase(lang)) {
                LANGUAGE = Language.JAVA;
            } else if ("any".equalsIgnoreCase(lang)) {
                LANGUAGE = Language.ANY;
            } else {
                System.err.println("Unknown language: " + lang);
                System.err.println("Options are: java, kotlin, any");
                System.exit(0);
            }
        });
    }

    /**
     * Decompiles the given {@link InputStream}.
     */
    public static TypeEntry decompile(InputStream input) throws IOException {
        SourceSet source = new SourceSet();
        return decompile(input, source, Language.ANY);
    }

    /**
     * Decompiles the given {@link InputStream} as the given language.
     */
    public static TypeEntry decompile(InputStream input, Language lang) throws IOException {
        return decompile(input, new SourceSet(), lang);
    }

    /**
     * Decompiles the given {@link InputStream} with the given loader for
     * additional types.
     */
    public static TypeEntry decompile(InputStream input, SourceSet.Loader loader) throws IOException {
        SourceSet source = new SourceSet();
        source.setLoader(loader);
        return decompile(input, source);
    }

    /**
     * Decompiles the given {@link InputStream} with the given language and
     * loader for additional types.
     */
    public static TypeEntry decompile(InputStream input, Language lang, SourceSet.Loader loader) throws IOException {
        SourceSet source = new SourceSet();
        source.setLoader(loader);
        return decompile(input, source, lang);
    }

    /**
     * Decompiles the given {@link InputStream} into the given source set.
     */
    public static TypeEntry decompile(InputStream input, SourceSet source) throws IOException {
        return decompile(input, source, Language.ANY);
    }

    /**
     * Decompiles the given {@link InputStream} with the given language into the
     * given source set.
     */
    public static TypeEntry decompile(InputStream input, SourceSet source, Language lang) throws IOException {
        TypeEntry type = Decompilers.get(lang).decompile(input, source);
        return type;
    }

    /**
     * Emits the given type entry to a string.
     */
    public static String emitToString(TypeEntry type) {
        StringWriter writer = new StringWriter();
        JavaEmitterContext ctx = new JavaEmitterContext(writer, EmitterFormat.defaults());
        Emitters.get(type.getLanguage()).emit(ctx, type);
        return writer.toString();
    }

    /**
     * The main entrance point.
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java -jar Despector.jar [sources...] [destination]");
            return;
        }

        List<String> sources = new ArrayList<>();
        outer: for (int i = 0; i < args.length - 1; i++) {
            if (args[i].startsWith("-")) {
                for (String flag : flags.keySet()) {
                    if (args[i].startsWith(flag)) {
                        flags.get(flag).accept(args[i]);
                        continue outer;
                    }
                }
                System.err.println("Unknown flag: " + args[i]);
            } else {
                sources.add(args[i]);
            }
        }
        String destination = args[args.length - 1];
        Path output = Paths.get(destination).toAbsolutePath();
        if (!Files.exists(output)) {
            Files.createDirectories(output);
        }
        EmitterFormat formatter = EmitterFormat.defaults();
        formatter.loadFrom(ConfigManager.getConfig().formatter);
        Path formatter_path = Paths.get(".").resolve(ConfigManager.getConfig().emitter.formatting_path);
        Path importorder_path = Paths.get(".").resolve(ConfigManager.getConfig().emitter.imports_path);
        if (Files.exists(formatter_path) && Files.exists(importorder_path)) {
            FormatLoader formatter_loader = FormatLoader.getLoader(ConfigManager.getConfig().emitter.formatting_type);
            formatter_loader.load(formatter, formatter_path, importorder_path);
        }

        Decompiler decompiler = Decompilers.get(LANGUAGE);

        SourceSet source = new SourceSet();
        for (String s : sources) {
            Path path = Paths.get(s);
            if (!Files.exists(path)) {
                System.err.println("Unknown source: " + path.toAbsolutePath().toString());
            } else if (s.endsWith(".jar")) {
                JarWalker walker = new JarWalker(path);
                walker.walk(source, decompiler);
            } else if (Files.isDirectory(path)) {
                DirectoryWalker walker = new DirectoryWalker(path);
                try {
                    walker.walk(source, decompiler);
                } catch (IOException e) {
                    System.err.println("Error while walking directory: " + path.toAbsolutePath().toString());
                    e.printStackTrace();
                }
            } else if (s.endsWith(".class")) {
                decompiler.decompile(path, source);
            } else {
                System.err.println("Unknown source type: " + path.toAbsolutePath().toString() + " must be jar or directory");
            }
        }

        if (source.getAllClasses().isEmpty()) {
            System.err.println("No sources found.");
            return;
        }

        List<TypeTransformer> transformers = new ArrayList<>();
        for (String operation : ConfigManager.getConfig().cleanup.operations) {
            TypeTransformer transformer = CleanupOperations.getOperation(operation);
            if (transformer == null) {
                System.err.println("Unknown cleanup operation: " + operation);
            } else {
                transformers.add(transformer);
            }
        }
        Map<String, Set<TypeTransformer>> targeted_transformers = new HashMap<>();
        for (CleanupConfigSection section : ConfigManager.getConfig().cleanup_sections) {
            List<TypeTransformer> trans = new ArrayList<>();
            for (String operation : section.operations) {
                TypeTransformer transformer = CleanupOperations.getOperation(operation);
                if (transformer == null) {
                    System.err.println("Unknown cleanup operation: " + operation);
                } else {
                    trans.add(transformer);
                }
            }
            for (String target : section.targets) {
                Set<TypeTransformer> target_trans = targeted_transformers.get(target);
                if (target_trans == null) {
                    target_trans = new HashSet<>();
                    targeted_transformers.put(target, target_trans);
                }
                target_trans.addAll(trans);
            }
        }
        if (!transformers.isEmpty() || !targeted_transformers.isEmpty()) {
            for (TypeEntry type : source.getAllClasses()) {
                for (TypeTransformer transformer : transformers) {
                    transformer.transform(type);
                }
                Set<TypeTransformer> targetted = targeted_transformers.get(type.getName());
                if (targetted != null) {
                    for (TypeTransformer transformer : targetted) {
                        transformer.transform(type);
                    }
                }
            }
        }

        Emitter<JavaEmitterContext> emitter = Emitters.get(LANGUAGE);

        for (TypeEntry type : source.getAllClasses()) {
            if (type.isInnerClass() || type.isAnonType()) {
                continue;
            }
            Path out = output.resolve(type.getName() + LANGUAGE.getExtension(type));
            if (!Files.exists(out.getParent())) {
                Files.createDirectories(out.getParent());
            }
            try (FileWriter writer = new FileWriter(out.toFile())) {
                JavaEmitterContext ctx = new JavaEmitterContext(writer, formatter);
                emitter.emit(ctx, type);
            }
        }

    }

    private Despector() {
    }

}
