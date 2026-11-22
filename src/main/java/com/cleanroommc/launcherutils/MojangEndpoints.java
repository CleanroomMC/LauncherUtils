/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils;

public final class MojangEndpoints {

    public static final String VERSION_MANIFEST_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    /**
     * Where asset objects are, by {@link com.cleanroommc.launcherutils.asset.AssetIndex.AssetObject#path()}.
     */
    public static final String RESOURCES = "https://resources.download.minecraft.net/";
    public static final String LIBRARIES = "https://libraries.minecraft.net/";

    private MojangEndpoints() { }

}
