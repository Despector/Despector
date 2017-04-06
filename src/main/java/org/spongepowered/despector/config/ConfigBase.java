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
package org.spongepowered.despector.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * The global configuration.
 */
public class ConfigBase {

    @Setting(comment = "Emitter configuration")
    public EmitterConfig emitter = new EmitterConfig();
    @Setting(comment = "Cleanup configuration")
    public CleanupConfig cleanup = new CleanupConfig();
    @Setting(comment = "Kotlin specific configuration")
    public KotlinConfig kotlin = new KotlinConfig();

    @Setting(comment = "Targeted cleanup operations")
    public List<CleanupConfigSection> cleanup_sections = new ArrayList<>();

    @Setting(value = "print-opcodes-on-error", comment = "Prints out opcodes of a method when it fails to decompile.")
    public boolean print_opcodes_on_error = Boolean.valueOf(System.getProperty("despector.debug.printerrors", "false"));

    /**
     * Configuration for the emitter settings.
     */
    @ConfigSerializable
    public static class EmitterConfig {

        @Setting(value = "formatting-path", comment = "The path of the formatter configuration")
        public String formatting_path = "eclipse_formatter.xml";
        @Setting(value = "import-order-path", comment = "The path of the import order configuration")
        public String imports_path = "eclipse.importorder";
        @Setting(value = "formatting-type", comment = "One of: eclipse,intellij")
        public String formatting_type = "eclipse";
        @Setting(value = "emit-synthetics", comment = "Whether to emit synthetic members")
        public boolean emit_synthetics = false;

    }

    /**
     * Configuration for the cleanup operations.
     */
    @ConfigSerializable
    public static class CleanupConfig {

        @Setting(value = "operations", comment = "Cleanup operations to apply before emitting")
        public List<String> operations = new ArrayList<>();

    }

    @ConfigSerializable
    public static class CleanupConfigSection {

        @Setting(value = "operations", comment = "Cleanup operations to apply before emitting")
        public List<String> operations = new ArrayList<>();

        @Setting(value = "targets", comment = "Class targets to apply this cleanup to")
        public List<String> targets = new ArrayList<>();

    }

    @ConfigSerializable
    public static class KotlinConfig {

        @Setting(value = "replace-multiline-strings", comment = "Whether to replace strings containing new lines with raw strings")
        public boolean replace_mulit_line_strings = true;

    }

}
