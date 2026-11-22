/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils;

import com.cleanroommc.launcherutils.asset.AssetIndex;
import com.cleanroommc.launcherutils.manifest.VersionManifest;
import com.cleanroommc.launcherutils.version.Download;
import com.cleanroommc.platformutils.Architecture;
import com.cleanroommc.platformutils.OperatingSystem;
import com.cleanroommc.platformutils.Platform;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LayoutTest {

    @Test
    void coordinatesRoundTrip() {
        Coordinate coordinate = Coordinate.parse("org.lwjgl:lwjgl:3.3.3:natives-linux");
        assertThat(coordinate.path()).isEqualTo("org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-linux.jar");
        assertThat(coordinate.module()).isEqualTo("org.lwjgl:lwjgl");
        assertThat(coordinate.withoutClassifier()).hasToString("org.lwjgl:lwjgl:3.3.3");
        assertThat(Coordinate.parse("de.oceanlabs.mcp:mcp:1.12.2@zip").fileName()).isEqualTo("mcp-1.12.2.zip");
        assertThat(Coordinate.parse("a:b:1@zip")).hasToString("a:b:1@zip");
        assertThat(Coordinate.parse("g:a:1:")).isEqualTo(Coordinate.parse("g:a:1"));
        for (String malformed : new String[] {
                "", "g", "a:b", "g:a:1:x:y", ":a:1", "g::1", "g:a: ", "g:a:1@", "g:a:1@zip@jar"
        }) {
            assertThatThrownBy(() -> Coordinate.parse(malformed))
                    .as(malformed)
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void downloadPathsFallBackToTheUrlThenTheCoordinate() {
        Coordinate coordinate = Coordinate.parse("org.lwjgl.lwjgl:lwjgl-platform:2.9.4");
        Download classifier = new Download().url("https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl-platform/2.9.4/lwjgl-platform-2.9.4-natives-linux.jar");
        assertThat(
                classifier.path(coordinate)).isEqualTo("org/lwjgl/lwjgl/lwjgl-platform/2.9.4/lwjgl-platform-2.9.4-natives-linux.jar");
        assertThat(new Download().url("https://example.com/file.jar").path(coordinate)).isEqualTo(coordinate.path());
        assertThat(new Download().path("custom/path.jar").path(coordinate)).isEqualTo("custom/path.jar");
        assertThat(new Download().url("").isEmbedded()).isTrue();
    }

    @Test
    void findsVersionsInTheManifest() {
        VersionManifest manifest = VersionManifest.read(
                new StringReader("{\"latest\":{\"release\":\"1.21.4\",\"snapshot\":\"25w02a\"},\"versions\":["
                                + "{\"id\":\"25w02a\",\"type\":\"snapshot\",\"url\":\"https://a\",\"sha1\":\"1\"},"
                                + "{\"id\":\"1.21.4\",\"type\":\"release\",\"url\":\"https://b\",\"sha1\":\"2\",\"complianceLevel\":1}]}"));
        assertThat(manifest.latestRelease().get().url()).isEqualTo("https://b");
        assertThat(manifest.latestSnapshot().get().type()).isEqualTo("snapshot");
        assertThat(manifest.find("1.12.2")).isEmpty();
    }

    @Test
    void assetObjectsLiveUnderTheirHashPrefix() {
        AssetIndex index = AssetIndex.read(
                new StringReader("{\"virtual\":true,\"objects\":{\"sounds/a.ogg\":{\"hash\":\"bdf48ef6b5d0d23bbb02e17d04865216179f510a\",\"size\":5}}}"));
        AssetIndex.AssetObject object = index.objects().get("sounds/a.ogg");
        assertThat(index.isVirtual()).isTrue();
        assertThat(index.mapsToResources()).isFalse();
        assertThat(object.path()).isEqualTo("bd/bdf48ef6b5d0d23bbb02e17d04865216179f510a");
        assertThat(object.url()).isEqualTo(MojangEndpoints.RESOURCES + object.path());
    }

    @Test
    void findsTheMinecraftDirectory() {
        Path home = Paths.get("/home/dev");
        assertThat(MinecraftDirectory.defaultFor(Platform.of(OperatingSystem.WINDOWS, Architecture.X64),
                Collections.singletonMap("APPDATA", "/appdata")::get, home)).isEqualTo(
                Paths.get("/appdata/.minecraft"));
        assertThat(MinecraftDirectory.defaultFor(Platform.of(OperatingSystem.WINDOWS, Architecture.X64), name -> null,
                home)).isEqualTo(home.resolve("AppData/Roaming/.minecraft"));
        assertThat(MinecraftDirectory.defaultFor(Platform.of(OperatingSystem.MAC_OS, Architecture.ARM64), name -> null,
                home)).isEqualTo(home.resolve("Library/Application Support/minecraft"));
        assertThat(MinecraftDirectory.defaultFor(Platform.of(OperatingSystem.FREE_BSD, Architecture.X64), name -> null,
                home)).isEqualTo(home.resolve(".minecraft"));
    }

}
