/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.mmc;

import com.cleanroommc.launcherutils.LauncherGson;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * An instance's {@code mmc-pack.json}, the components MultiMC and Prism stack into a launch.
 */
public final class MmcPack {

    public static final String MINECRAFT_UID = "net.minecraft";
    public static final String LWJGL2_UID = "org.lwjgl";
    public static final String LWJGL3_UID = "org.lwjgl3";
    public static final String FORGE_UID = "net.minecraftforge";

    private int formatVersion = 1;
    private List<Component> components;

    public static MmcPack read(Reader reader) {
        return LauncherGson.read(reader, MmcPack.class);
    }

    public int formatVersion() {
        return this.formatVersion;
    }

    public List<Component> components() {
        return this.components == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(this.components);
    }

    public Optional<Component> component(String uid) {
        for (Component component : this.components()) {
            if (uid.equals(component.uid)) {
                return Optional.of(component);
            }
        }
        return Optional.empty();
    }

    public MmcPack component(Component component) {
        if (this.components == null) {
            this.components = new ArrayList<>();
        }
        this.components.add(component);
        return this;
    }

    public static final class Component {

        private String uid;
        private String version;
        private Boolean important;
        private Boolean dependencyOnly;
        private String cachedName;
        private String cachedVersion;
        private List<MmcRequirement> cachedRequires;

        public static Component of(String uid, String version) {
            Component component = new Component();
            component.uid = uid;
            component.version = version;
            return component;
        }

        public String uid() {
            return this.uid;
        }

        public String version() {
            return this.version;
        }

        /**
         * Whether the launcher keeps users from removing this component.
         */
        public boolean isImportant() {
            return Boolean.TRUE.equals(this.important);
        }

        /**
         * Whether the component is only here because another requires it.
         */
        public boolean isDependencyOnly() {
            return Boolean.TRUE.equals(this.dependencyOnly);
        }

        public String cachedName() {
            return this.cachedName;
        }

        public Component cachedName(String cachedName) {
            this.cachedName = cachedName;
            return this;
        }

        public String cachedVersion() {
            return this.cachedVersion;
        }

        public Component cachedVersion(String cachedVersion) {
            this.cachedVersion = cachedVersion;
            return this;
        }

        public List<MmcRequirement> cachedRequires() {
            return this.cachedRequires == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(this.cachedRequires);
        }

        public Component important(boolean important) {
            this.important = important;
            return this;
        }

        public Component dependencyOnly(boolean dependencyOnly) {
            this.dependencyOnly = dependencyOnly;
            return this;
        }

        public Component cachedRequire(MmcRequirement requirement) {
            if (this.cachedRequires == null) {
                this.cachedRequires = new ArrayList<>();
            }
            this.cachedRequires.add(requirement);
            return this;
        }

    }

}
