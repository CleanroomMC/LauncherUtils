/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.mmc;

import com.cleanroommc.launcherutils.rule.Dialect;
import com.cleanroommc.launcherutils.rule.RuleContext;
import com.cleanroommc.launcherutils.version.Library;
import com.cleanroommc.platformutils.Architecture;
import com.cleanroommc.platformutils.OperatingSystem;
import com.cleanroommc.platformutils.Platform;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MmcPatchTest {

    private static MmcPatch fixture(String name) throws IOException {
        try (Reader reader = new InputStreamReader(MmcPatchTest.class.getResourceAsStream("/" + name),
                StandardCharsets.UTF_8)) {
            return MmcPatch.read(reader);
        }
    }

    private static RuleContext mmc(OperatingSystem os, Architecture architecture) {
        return RuleContext.of(Platform.of(os, architecture)).dialect(Dialect.MMC);
    }

    private static List<String> glfwNatives(MmcPatch patch, RuleContext context) {
        List<String> names = new ArrayList<>();
        for (Library library : patch.libraries(context)) {
            if (library.name().startsWith("org.lwjgl:lwjgl-glfw-natives-")) {
                names.add(library.name());
            }
        }
        return names;
    }

    @Test
    void readsTheComponentIdentity() throws IOException {
        MmcPatch patch = fixture("prism-lwjgl3-3.3.3.json");
        assertThat(patch.uid()).isEqualTo(MmcPack.LWJGL3_UID);
        assertThat(patch.version()).isEqualTo("3.3.3");
        assertThat(patch.formatVersion()).isEqualTo(1);
        assertThat(patch.conflicts()).extracting(MmcRequirement::uid).contains(MmcPack.LWJGL2_UID);
    }

    @Test
    void eachPlatformTakesOnlyItsOwnLwjglNatives() throws IOException {
        MmcPatch patch = fixture("prism-lwjgl3-3.3.3.json");
        assertThat(glfwNatives(patch,
                mmc(OperatingSystem.LINUX,
                        Architecture.X64))).containsExactly("org.lwjgl:lwjgl-glfw-natives-linux:3.3.3");
        assertThat(glfwNatives(patch,
                mmc(OperatingSystem.LINUX,
                        Architecture.ARM64))).containsExactly("org.lwjgl:lwjgl-glfw-natives-linux-arm64:3.3.3-lwjgl.1");
        assertThat(glfwNatives(patch,
                mmc(OperatingSystem.MAC_OS,
                        Architecture.ARM64))).containsExactly("org.lwjgl:lwjgl-glfw-natives-macos-arm64:3.3.3");
        assertThat(glfwNatives(patch, mmc(OperatingSystem.FREE_BSD, Architecture.X64))).isEmpty();
    }

    @Test
    void nativesKeysUseTheMmcSystemName() throws IOException {
        Library platform = null;
        for (Library library : fixture("prism-lwjgl-2.9.4.json").libraries()) {
            if (library.hasNatives() && library.name().startsWith("org.lwjgl.lwjgl:lwjgl-platform:")) {
                platform = library;
            }
        }
        assertThat(platform).isNotNull();
        assertThat(platform.nativeClassifier(mmc(OperatingSystem.LINUX,
                Architecture.ARM64))).isEqualTo("natives-linux-arm64");
        assertThat(platform.nativeClassifier(mmc(OperatingSystem.LINUX, Architecture.X64))).isEqualTo("natives-linux");
        assertThat(platform.nativeClassifier(mmc(OperatingSystem.MAC_OS,
                Architecture.ARM64))).isEqualTo("natives-osx-arm64");
        assertThat(platform.nativeClassifier(mmc(OperatingSystem.LINUX, Architecture.RISCV64))).isNull();
    }

}
