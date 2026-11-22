/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.version;

import com.cleanroommc.launcherutils.rule.RuleContext;
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

class VersionJsonTest {

    private static VersionJson fixture(String name) throws IOException {
        try (Reader reader = new InputStreamReader(VersionJsonTest.class.getResourceAsStream("/" + name),
                StandardCharsets.UTF_8)) {
            return VersionJson.read(reader);
        }
    }

    private static RuleContext context(OperatingSystem os, Architecture architecture) {
        return RuleContext.of(Platform.of(os, architecture));
    }

    private static List<String> names(List<Library> libraries) {
        List<String> names = new ArrayList<>();
        for (Library library : libraries) {
            names.add(library.name());
        }
        return names;
    }

    @Test
    void readsEveryTopLevelField() throws IOException {
        VersionJson version = fixture("1.21.4.json");
        assertThat(version.id()).isEqualTo("1.21.4");
        assertThat(version.type()).isEqualTo("release");
        assertThat(version.mainClass()).isEqualTo("net.minecraft.client.main.Main");
        assertThat(version.assetIndex().id()).isEqualTo("19");
        assertThat(version.assetIndex().totalSize()).isPositive();
        assertThat(version.javaMajor(8)).isEqualTo(21);
        assertThat(version.download(VersionJson.CLIENT).sha1()).hasSize(40);
        assertThat(version.logging().client().file().id()).startsWith("client-");
        assertThat(version.complianceLevel()).isEqualTo(1);
        assertThat(fixture("1.12.2.json").javaMajor(8)).isEqualTo(8);
    }

    @Test
    void picksLwjglByPlatform() throws IOException {
        VersionJson version = fixture("1.12.2.json");
        assertThat(names(version.libraries(context(OperatingSystem.LINUX, Architecture.X64))))
                .contains("org.lwjgl.lwjgl:lwjgl:2.9.4-nightly-20150209")
                .doesNotContain("org.lwjgl.lwjgl:lwjgl:2.9.2-nightly-20140822", "ca.weblite:java-objc-bridge:1.0.0");
        assertThat(names(version.libraries(context(OperatingSystem.MAC_OS, Architecture.X64))))
                .contains("org.lwjgl.lwjgl:lwjgl:2.9.2-nightly-20140822", "ca.weblite:java-objc-bridge:1.0.0")
                .doesNotContain("org.lwjgl.lwjgl:lwjgl:2.9.4-nightly-20150209");
    }

    @Test
    void freeBsdTakesUngatedLibrariesButNoLinuxNatives() throws IOException {
        VersionJson version = fixture("1.12.2.json");
        RuleContext freeBsd = context(OperatingSystem.FREE_BSD, Architecture.X64);
        List<Library> libraries = version.libraries(freeBsd);
        assertThat(names(libraries)).contains("org.lwjgl.lwjgl:lwjgl-platform:2.9.4-nightly-20150209");
        for (Library library : libraries) {
            assertThat(library.nativeClassifier(freeBsd)).as(library.name()).isNull();
        }
    }

    @Test
    void substitutesArchIntoNativeClassifiers() throws IOException {
        Library twitch = null;
        for (Library library : fixture("1.7.10.json").libraries()) {
            if (library.name().startsWith("tv.twitch:twitch-external-platform:")) {
                twitch = library;
            }
        }
        assertThat(twitch).isNotNull();
        assertThat(twitch.nativeClassifier(context(OperatingSystem.WINDOWS,
                Architecture.X64))).isEqualTo("natives-windows-64");
        assertThat(twitch.nativeClassifier(context(OperatingSystem.WINDOWS,
                Architecture.X86))).isEqualTo("natives-windows-32");
        assertThat(twitch.nativeDownload(context(OperatingSystem.WINDOWS, Architecture.X86))
                        .url()).endsWith("natives-windows-32.jar");
        assertThat(twitch.allowed(context(OperatingSystem.LINUX, Architecture.X64))).isFalse();
    }

    @Test
    void prefersAnArchitectureVariantOffX64() {
        Library library = Library.of("org.lwjgl.lwjgl:lwjgl-platform:2.9.4")
                .natives("linux", "natives-linux")
                .classifier("natives-linux", new Download().url("https://example.com/natives-linux.jar"))
                .classifier("natives-linux-arm64", new Download().url("https://example.com/natives-linux-arm64.jar"));
        assertThat(library.nativeClassifier(context(OperatingSystem.LINUX,
                Architecture.ARM64))).isEqualTo("natives-linux-arm64");
        assertThat(library.nativeClassifier(context(OperatingSystem.LINUX,
                Architecture.X64))).isEqualTo("natives-linux");
        assertThat(library.nativeClassifier(context(OperatingSystem.LINUX,
                Architecture.ARM32))).isEqualTo("natives-linux");
    }

    @Test
    void rendersModernArguments() throws IOException {
        VersionJson version = fixture("1.21.4.json");
        Substitutions substitutions = Substitutions.create()
                .put(Substitutions.NATIVES_DIRECTORY, "/natives")
                .put(Substitutions.AUTH_PLAYER_NAME, "Dev");

        List<String> macJvm = version.jvmArguments(context(OperatingSystem.MAC_OS, Architecture.ARM64), substitutions);
        assertThat(macJvm).contains("-XstartOnFirstThread", "-Djava.library.path=/natives").doesNotContain("-Xss1M");
        List<String> x86Jvm = version.jvmArguments(context(OperatingSystem.WINDOWS, Architecture.X86), substitutions);
        assertThat(x86Jvm).contains("-Xss1M").doesNotContain("-XstartOnFirstThread");

        RuleContext linux = context(OperatingSystem.LINUX, Architecture.X64);
        List<String> game = version.gameArguments(linux, substitutions);
        assertThat(game).contains("--username", "Dev").doesNotContain("--demo");
        assertThat(version.gameArguments(linux.feature("is_demo_user", true), substitutions)).contains("--demo");
        assertThat(game).contains("${version_name}");
    }

    @Test
    void rendersLegacyArguments() throws IOException {
        VersionJson version = fixture("1.12.2.json");
        assertThat(version.hasGameArguments()).isTrue();
        List<String> game = version.gameArguments(context(OperatingSystem.LINUX, Architecture.X64),
                Substitutions.create().put(Substitutions.AUTH_PLAYER_NAME, "Dev").onMissing(name -> ""));
        assertThat(game).startsWith("--username", "Dev", "--version", "");
        assertThat(version.jvmArguments(context(OperatingSystem.LINUX, Architecture.X64),
                Substitutions.create())).isEmpty();
    }

    @Test
    void inheritsFromItsParent() throws IOException {
        VersionJson parent = fixture("1.21.4.json");
        VersionJson child = new VersionJson()
                .id("cleanroom")
                .inheritsFrom("1.21.4")
                .mainClass("com.cleanroommc.Main")
                .library(Library.of("com.cleanroommc:cleanroom:1.0"))
                .gameArgument(Argument.of("--tweakClass", "Example"));

        VersionJson merged = child.inherit(parent);
        assertThat(merged.id()).isEqualTo("cleanroom");
        assertThat(merged.inheritsFrom()).isNull();
        assertThat(merged.mainClass()).isEqualTo("com.cleanroommc.Main");
        assertThat(merged.assetIndex().id()).isEqualTo("19");
        assertThat(merged.libraries()).hasSize(parent.libraries().size() + 1);
        assertThat(merged.libraries().get(0).name()).isEqualTo("com.cleanroommc:cleanroom:1.0");
        List<String> game = merged.gameArguments(context(OperatingSystem.LINUX, Architecture.X64),
                Substitutions.create());
        assertThat(game).endsWith("--tweakClass", "Example");
        assertThat(merged.jvmArgumentList()).hasSameSizeAs(parent.jvmArgumentList());
    }

    @Test
    void equalMetadataIsEqual() throws IOException {
        VersionJson first = fixture("1.12.2.json");
        VersionJson second = fixture("1.12.2.json");
        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first).isNotEqualTo(fixture("1.21.4.json"));
        assertThat(second.id("changed")).isNotEqualTo(first);
    }

    @Test
    void namesTheRuntimeForEachMajor() {
        assertThat(JavaVersion.forMajor(8).component()).isEqualTo("jre-legacy");
        assertThat(JavaVersion.forMajor(17).component()).isEqualTo("java-runtime-beta");
        assertThat(JavaVersion.forMajor(21).component()).isEqualTo("java-runtime-delta");
        assertThat(JavaVersion.forMajor(25).component()).isEqualTo("java-runtime-epsilon");
        assertThat(JavaVersion.forMajor(29).component()).isNull();
        assertThat(JavaVersion.forMajor(29).majorVersion()).isEqualTo(29);
    }

}
