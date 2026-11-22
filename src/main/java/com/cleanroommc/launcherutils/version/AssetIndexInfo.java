/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.version;

import java.util.Objects;

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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        AssetIndexInfo that = (AssetIndexInfo) other;
        return Objects.equals(this.id, that.id) && Objects.equals(this.totalSize, that.totalSize);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(this.id, this.totalSize);
    }

}
