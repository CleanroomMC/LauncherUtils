/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils;

import com.cleanroommc.platformutils.Platform;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * Where Mojang's launcher keeps its {@code .minecraft} directory.
 */
public final class MinecraftDirectory {

    private MinecraftDirectory() { }

    public static Path current() {
        return defaultFor(Platform.current(), System::getenv, Paths.get(System.getProperty("user.home")));
    }

    /**
     * {@code %APPDATA%\.minecraft} on Windows, {@code ~/Library/Application Support/minecraft} on macOS, and
     * {@code ~/.minecraft} elsewhere.
     *
     * @param platform the platform the launcher runs on
     * @param environment looks up environment variables
     * @param home the user's home directory
     */
    public static Path defaultFor(Platform platform, Function<String, String> environment, Path home) {
        if (platform.isWindows()) {
            String appData = environment.apply("APPDATA");
            Path roaming = appData == null || appData.isEmpty()
                    ? home.resolve("AppData").resolve("Roaming")
                    : Paths.get(appData);
            return roaming.resolve(".minecraft");
        }
        if (platform.isMacOS()) {
            return home.resolve("Library").resolve("Application Support").resolve("minecraft");
        }
        return home.resolve(".minecraft");
    }

}
