/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.rule;

/**
 * How a bare system name such as {@code linux} matches.
 */
public enum Dialect {

    /**
     * Mojang's launcher, where a bare system name matches every architecture.
     */
    MOJANG,
    /**
     * MultiMC and Prism, where a bare system name matches x86 and x64 only.
     * Other architectures match a joined name such as {@code linux-arm64}.
     */
    MMC

}
