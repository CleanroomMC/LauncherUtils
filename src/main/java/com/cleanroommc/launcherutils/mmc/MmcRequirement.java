/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.mmc;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * A component another component requires, pinned to one version or suggesting one.
 */
public final class MmcRequirement {

    private String uid;
    @SerializedName("equals") private String exactVersion;
    private String suggests;

    public static MmcRequirement exactly(String uid, String version) {
        MmcRequirement requirement = new MmcRequirement();
        requirement.uid = uid;
        requirement.exactVersion = version;
        return requirement;
    }

    public static MmcRequirement suggesting(String uid, String version) {
        MmcRequirement requirement = new MmcRequirement();
        requirement.uid = uid;
        requirement.suggests = version;
        return requirement;
    }

    public String uid() {
        return this.uid;
    }

    public String exactVersion() {
        return this.exactVersion;
    }

    public String suggests() {
        return this.suggests;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        MmcRequirement that = (MmcRequirement) other;
        return Objects.equals(this.uid, that.uid)
                && Objects.equals(this.exactVersion, that.exactVersion)
                && Objects.equals(this.suggests, that.suggests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uid, this.exactVersion, this.suggests);
    }

}
