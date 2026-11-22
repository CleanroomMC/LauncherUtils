/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils;

import com.cleanroommc.platformutils.Architecture;
import com.cleanroommc.platformutils.OperatingSystem;
import com.cleanroommc.platformutils.Platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The operating system and architecture names launchers write into version metadata, and the platforms named by native
 * classifiers.
 *
 * <p>Mojang's launcher names systems {@code windows}, {@code osx} and {@code linux}. Every other system keeps its
 * {@link OperatingSystem#id()}, so FreeBSD is {@code freebsd} and never matches a Linux rule. MultiMC and Prism also
 * write system names such as {@code linux-arm64}, joining the system to an architecture they spell as
 * {@link #mmcArch(Architecture)}.
 */
public final class LauncherNames {

    /**
     * The prefix of classifiers launchers extract natives from, as in {@code natives-linux}.
     */
    public static final String NATIVES_PREFIX = "natives-";

    private static final List<String> OS_TOKENS;

    static {
        List<String> tokens = new ArrayList<>(Arrays.asList("osx", "macosx", "mac", "darwin", "win", "win32"));
        for (OperatingSystem os : OperatingSystem.values()) {
            if (os != OperatingSystem.UNKNOWN) {
                tokens.add(os.id());
            }
        }
        OS_TOKENS = tokens;
    }

    private LauncherNames() { }

    public static String os(OperatingSystem os) {
        switch (os) {
            case MAC_OS:
                return "osx";
            // Android launchers such as PojavLauncher run Linux builds
            case ANDROID:
                return "linux";
            default:
                return os.id();
        }
    }

    public static String os(Platform platform) {
        return os(platform.operatingSystem());
    }

    public static String arch(Architecture architecture) {
        return architecture.id();
    }

    public static String mmcArch(Architecture architecture) {
        return architecture == Architecture.X64 ? "x86_64" : architecture.id();
    }

    /**
     * A MultiMC and Prism system name such as {@code linux-arm64} or {@code windows-x86_64}.
     *
     * @param platform the platform to name
     */
    public static String mmcSystem(Platform platform) {
        return os(platform) + "-" + mmcArch(platform.architecture());
    }

    /**
     * Parses a system name from version metadata or a classifier, such as {@code osx}, {@code macos} or {@code win}.
     *
     * @param name a system name such as {@code osx}
     * @return the system, or {@link OperatingSystem#UNKNOWN}
     */
    public static OperatingSystem parseOs(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        switch (lower) {
            case "osx":
            case "macosx":
            case "mac":
            case "darwin":
                return OperatingSystem.MAC_OS;
            case "win":
            case "win32":
                return OperatingSystem.WINDOWS;
            default:
                return OperatingSystem.fromId(lower);
        }
    }

    /**
     * Parses an architecture from version metadata or a classifier, such as {@code x86_64}, {@code amd64},
     * {@code arm64} or {@code aarch_64}.
     *
     * @param name an architecture name such as {@code aarch_64}
     * @return the architecture, or {@link Architecture#UNKNOWN}
     */
    public static Architecture parseArch(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        // Netty and protobuf classifiers spell arm64 as aarch_64
        return Architecture.parse(lower.equals("aarch_64") ? "aarch64" : lower);
    }

    /**
     * The platform a native classifier names, such as {@code natives-macos-arm64}, Netty's {@code osx-aarch_64} or
     * zstd-jni's {@code linux_amd64}. A classifier that names only a system, as LWJGL's {@code natives-linux} does,
     * names its x64 build.
     *
     * @param classifier a Maven classifier
     * @return the platform, or empty when the classifier names none
     */
    public static Optional<Platform> classifierPlatform(String classifier) {
        String token = classifier.toLowerCase(Locale.ROOT);
        if (token.startsWith(NATIVES_PREFIX)) {
            token = token.substring(NATIVES_PREFIX.length());
        }
        for (String os : OS_TOKENS) {
            if (!token.startsWith(os)) {
                continue;
            }
            String rest = token.substring(os.length());
            if (!rest.isEmpty() && rest.charAt(0) != '-' && rest.charAt(0) != '_') {
                continue;
            }
            Architecture architecture = rest.isEmpty() ? Architecture.X64 : parseArch(rest.substring(1));
            if (architecture == Architecture.UNKNOWN) {
                return Optional.empty();
            }
            return Optional.of(Platform.of(parseOs(os), architecture));
        }
        return Optional.empty();
    }

}
