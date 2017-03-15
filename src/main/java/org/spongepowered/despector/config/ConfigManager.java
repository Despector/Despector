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
package org.spongepowered.despector.config;

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The manager for all configuration.
 */
public class ConfigManager {

    private static final String HEADER = "Despector decompiler configuration:";

    private static HoconConfigurationLoader loader;
    private static CommentedConfigurationNode node;
    private static ObjectMapper<ConfigBase>.BoundInstance configMapper;

    private static ConfigBase config = null;

    /**
     * Gets the global configuration object.
     */
    public static ConfigBase getConfig() {
        if (config == null) {
            config = new ConfigBase();
        }
        return config;
    }

    /**
     * Loads the given configuration file.
     */
    public static void load(Path path) {
        System.out.println("Loading config from " + path.toString());
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                Files.createFile(path);
            }

            loader = HoconConfigurationLoader.builder().setPath(path).build();
            configMapper = ObjectMapper.forClass(ConfigBase.class).bindToNew();
            node = loader.load(ConfigurationOptions.defaults().setHeader(HEADER));
            config = configMapper.populate(node);
            configMapper.serialize(node);
            loader.save(node);
        } catch (Exception e) {
            System.err.println("Error loading configuration:");
            e.printStackTrace();
        }
    }

    /**
     * Saves the config back to disk to persist and changes made.
     */
    public static void update() {
        try {
            configMapper.serialize(node);
            loader.save(node);
        } catch (Exception e) {
            System.err.println("Error saving configuration:");
            e.printStackTrace();
        }
    }

}
