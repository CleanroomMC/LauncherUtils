/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils;

import com.cleanroommc.launcherutils.mmc.MmcPack;
import com.cleanroommc.launcherutils.mmc.MmcPatch;
import com.cleanroommc.launcherutils.mmc.MmcRequirement;
import com.cleanroommc.launcherutils.rule.Rule;
import com.cleanroommc.launcherutils.rule.RuleContext;
import com.cleanroommc.launcherutils.version.Argument;
import com.cleanroommc.launcherutils.version.Download;
import com.cleanroommc.launcherutils.version.Library;
import com.cleanroommc.launcherutils.version.VersionJson;
import com.cleanroommc.platformutils.Architecture;
import com.cleanroommc.platformutils.OperatingSystem;
import com.cleanroommc.platformutils.Platform;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

class WritingTest {

    private static JsonObject json(String text) {
        return new JsonParser().parse(text).getAsJsonObject();
    }

    @Test
    void writesLibrariesAsLaunchersReadThem() {
        Library library = Library.of(Coordinate.parse("org.lwjgl:lwjgl:3.3.3:natives-freebsd"))
                .artifact(new Download()
                                .path("org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-freebsd.jar")
                                .sha1("abc")
                                .size(3)
                                .url("https://repo/lwjgl-3.3.3-natives-freebsd.jar"))
                .rule(Rule.allow().os(OperatingSystem.FREE_BSD).arch(Architecture.X64));

        JsonObject written = json(LauncherGson.write(library));
        assertThat(written.get("name").getAsString()).isEqualTo("org.lwjgl:lwjgl:3.3.3:natives-freebsd");
        JsonObject os = written.getAsJsonArray("rules").get(0).getAsJsonObject().getAsJsonObject("os");
        assertThat(os.get("name").getAsString()).isEqualTo("freebsd");
        assertThat(os.get("arch").getAsString()).isEqualTo("x64");
        assertThat(written.getAsJsonArray("rules")
                        .get(0)
                        .getAsJsonObject()
                        .get("action")
                        .getAsString()).isEqualTo("allow");
        assertThat(written.has("natives")).isFalse();

        Library read = LauncherGson.read(new StringReader(written.toString()), Library.class);
        assertThat(read.allowed(RuleContext.of(Platform.of(OperatingSystem.FREE_BSD, Architecture.X64)))).isTrue();
        assertThat(read.allowed(RuleContext.of(Platform.of(OperatingSystem.LINUX, Architecture.X64)))).isFalse();
        assertThat(read.artifact().size()).isEqualTo(3);
    }

    @Test
    void writesArgumentsInTheirShortestForm() {
        VersionJson version = new VersionJson()
                .id("example")
                .gameArgument(Argument.of("--demo").rule(Rule.allow().feature("is_demo_user", true)))
                .jvmArgument(Argument.of("-Xmx1G"))
                .jvmArgument(Argument.of("-Xss1M", "-XX:+UnlockDiagnosticVMOptions").rule(Rule.allow().arch("x86")));

        JsonObject arguments = json(LauncherGson.write(version)).getAsJsonObject("arguments");
        assertThat(arguments.getAsJsonArray("jvm").get(0).getAsString()).isEqualTo("-Xmx1G");
        assertThat(arguments.getAsJsonArray("jvm").get(1).getAsJsonObject().getAsJsonArray("value")).hasSize(2);
        assertThat(arguments.getAsJsonArray("game")
                        .get(0)
                        .getAsJsonObject()
                        .get("value")
                        .getAsString()).isEqualTo("--demo");

        VersionJson read = VersionJson.read(new StringReader(LauncherGson.write(version)));
        assertThat(read.jvmArgumentList()).hasSize(2);
        assertThat(read.gameArgumentList().get(0).rules()).hasSize(1);
    }

    @Test
    void writesMmcPatchesAndPacks() {
        MmcPatch patch = MmcPatch.of(MmcPack.FORGE_UID, "Cleanroom", "0.3.0")
                .require(MmcRequirement.exactly(MmcPack.MINECRAFT_UID, "1.12.2"))
                .jvmArg("-Dexample=true")
                .trait("FirstThreadOnMacOS")
                .compatibleJavaMajor(21);
        patch.mainClass("top.outlands.foundation.boot.Foundation")
                .library(Library.of("com.cleanroommc:cleanroom:0.3.0").mmcHint("local"));

        JsonObject written = json(LauncherGson.write(patch));
        assertThat(written.get("formatVersion").getAsInt()).isEqualTo(1);
        assertThat(written.getAsJsonArray("+jvmArgs").get(0).getAsString()).isEqualTo("-Dexample=true");
        assertThat(written.getAsJsonArray("requires")
                        .get(0)
                        .getAsJsonObject()
                        .get("equals")
                        .getAsString()).isEqualTo("1.12.2");
        assertThat(written.getAsJsonArray("libraries")
                        .get(0)
                        .getAsJsonObject()
                        .get("MMC-hint")
                        .getAsString()).isEqualTo("local");

        MmcPack pack = new MmcPack()
                .component(MmcPack.Component.of(MmcPack.MINECRAFT_UID, "1.12.2").important(true))
                .component(MmcPack.Component.of(MmcPack.FORGE_UID, "0.3.0"));
        MmcPack read = MmcPack.read(new StringReader(LauncherGson.write(pack)));
        assertThat(read.component(MmcPack.MINECRAFT_UID).get().isImportant()).isTrue();
        assertThat(read.component(MmcPack.FORGE_UID).get().isImportant()).isFalse();
        assertThat(json(LauncherGson.write(pack))
                        .getAsJsonArray("components")
                        .get(1)
                        .getAsJsonObject()
                        .has("important")).isFalse();
    }

}
