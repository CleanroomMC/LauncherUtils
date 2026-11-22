/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.version;

/**
 * The asset index a version uses.
 */
public final class AssetIndexInfo extends Download {

    private String id;
    private Long totalSize;

    public String id() {
        return this.id;
    }

    public AssetIndexInfo id(String id) {
        this.id = id;
        return this;
    }

    public long totalSize() {
        return this.totalSize == null ? 0 : this.totalSize;
    }

    public AssetIndexInfo totalSize(long totalSize) {
        this.totalSize = totalSize;
        return this;
    }

}
