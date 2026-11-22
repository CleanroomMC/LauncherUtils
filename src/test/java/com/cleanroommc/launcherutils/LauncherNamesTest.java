/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils;

import com.cleanroommc.platformutils.Architecture;
import com.cleanroommc.platformutils.OperatingSystem;
import com.cleanroommc.platformutils.Platform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class LauncherNamesTest {

    @Test
    void systemsKeepTheirOwnNamesExceptMacOs() {
        assertThat(LauncherNames.os(OperatingSystem.MAC_OS)).isEqualTo("osx");
        assertThat(LauncherNames.os(OperatingSystem.WINDOWS)).isEqualTo("windows");
        assertThat(LauncherNames.os(OperatingSystem.LINUX)).isEqualTo("linux");
        assertThat(LauncherNames.os(OperatingSystem.FREE_BSD)).isEqualTo("freebsd");
        assertThat(LauncherNames.os(OperatingSystem.ANDROID)).isEqualTo("linux");
    }

    @Test
    void mmcSystemsSpellX64AsX8664() {
        assertThat(LauncherNames.mmcSystem(Platform.of(OperatingSystem.LINUX,
                Architecture.X64))).isEqualTo("linux-x86_64");
        assertThat(LauncherNames.mmcSystem(Platform.of(OperatingSystem.MAC_OS,
                Architecture.ARM64))).isEqualTo("osx-arm64");
        assertThat(LauncherNames.mmcSystem(Platform.of(OperatingSystem.FREE_BSD,
                Architecture.X64))).isEqualTo("freebsd-x86_64");
    }

    @ParameterizedTest
    @CsvSource({
                    "natives-linux, linux-x64", "natives-linux-arm64, linux-arm64", "natives-linux-arm32, linux-arm32",
                    "natives-linux-ppc64le, linux-ppc64le", "natives-macos, macos-x64",
                    "natives-macos-arm64, macos-arm64", "natives-osx, macos-x64", "natives-windows-x86, windows-x86",
                    "natives-windows-arm64, windows-arm64", "natives-freebsd, freebsd-x64",
                    "linux-aarch_64, linux-arm64", "osx-x86_64, macos-x64", "linux_amd64, linux-x64",
                    "win_amd64, windows-x64", "darwin_aarch64, macos-arm64", "freebsd_amd64, freebsd-x64",
            })
    void classifiersNameTheirPlatform(String classifier, String platform) {
        assertThat(LauncherNames.classifierPlatform(classifier)).contains(Platform.parse(platform));
    }

    @ParameterizedTest
    @CsvSource({ "natives-macos-patch", "shaded", "sources", "windows10" })
    void otherClassifiersNameNoPlatform(String classifier) {
        assertThat(LauncherNames.classifierPlatform(classifier)).isEmpty();
    }

}
