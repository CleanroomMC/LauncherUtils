/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils;

import com.cleanroommc.launcherutils.version.Argument;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;

/**
 * Reads and writes launcher metadata with Gson, which consumers supply. Gson 2.8.0, as Minecraft 1.12.2 ships, is
 * enough.
 */
public final class LauncherGson {

    private static volatile Gson gson;

    private LauncherGson() { }

    /**
     * Registers the adapters launcher metadata needs on a consumer's builder.
     *
     * @param builder the builder to register on
     */
    public static GsonBuilder register(GsonBuilder builder) {
        return builder.registerTypeAdapter(Argument.class, new Argument.Adapter());
    }

    public static Gson gson() {
        Gson current = gson;
        if (current == null) {
            current = register(new GsonBuilder()).disableHtmlEscaping().setPrettyPrinting().create();
            gson = current;
        }
        return current;
    }

    public static <T> T read(Reader reader, Class<T> type) {
        return gson().fromJson(reader, type);
    }

    public static String write(Object value) {
        return gson().toJson(value);
    }

}
