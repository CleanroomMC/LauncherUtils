/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.version;

import java.util.Objects;

/**
 * The Java runtime a version asks for, such as {@code java-runtime-delta} at major version 21.
 */
public final class JavaVersion {

    private String component;
    private int majorVersion;

    public static JavaVersion of(String component, int majorVersion) {
        JavaVersion version = new JavaVersion();
        version.component = component;
        version.majorVersion = majorVersion;
        return version;
    }

    /**
     * The runtime Mojang's launcher downloads for a major version. Mojang has no runtime for majors past those it
     * names, so those versions leave the component unset and launchers pick a runtime by major version alone.
     *
     * @param majorVersion a Java major version such as {@code 21}
     */
    public static JavaVersion forMajor(int majorVersion) {
        return of(component(majorVersion), majorVersion);
    }

    // Java 17 also ships as java-runtime-gamma, but java-runtime-beta covers every version that asks for 17
    private static String component(int majorVersion) {
        if (majorVersion >= 29) {
            return null;
        }
        if (majorVersion >= 25) {
            return "java-runtime-epsilon";
        }
        if (majorVersion >= 21) {
            return "java-runtime-delta";
        }
        if (majorVersion >= 17) {
            return "java-runtime-beta";
        }
        if (majorVersion >= 16) {
            return "java-runtime-alpha";
        }
        return "jre-legacy";
    }

    public String component() {
        return this.component;
    }

    public int majorVersion() {
        return this.majorVersion;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        JavaVersion that = (JavaVersion) other;
        return Objects.equals(this.component, that.component) && this.majorVersion == that.majorVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.component, this.majorVersion);
    }

}
