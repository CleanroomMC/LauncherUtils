/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.mmc;

import com.cleanroommc.launcherutils.LauncherGson;
import com.cleanroommc.launcherutils.version.Library;
import com.cleanroommc.launcherutils.version.VersionJson;

import com.google.gson.annotations.SerializedName;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A component's metadata, as in an instance's {@code patches/<uid>.json}. It extends version metadata with the
 * component's identity and the {@code +} lists MultiMC and Prism append across components.
 *
 * <p>Libraries in a patch follow {@link com.cleanroommc.launcherutils.rule.Dialect#MMC}.
 */
public final class MmcPatch extends VersionJson {

    private int formatVersion = 1;
    private String uid;
    private String name;
    private String version;
    private List<MmcRequirement> requires;
    private List<MmcRequirement> conflicts;
    private Library mainJar;
    @SerializedName("+jvmArgs") private List<String> jvmArgs;
    @SerializedName("+traits") private List<String> traits;
    @SerializedName("+tweakers") private List<String> tweakers;
    private List<Integer> compatibleJavaMajors;
    private String compatibleJavaName;

    public static MmcPatch read(Reader reader) {
        return LauncherGson.read(reader, MmcPatch.class);
    }

    public static MmcPatch of(String uid, String name, String version) {
        MmcPatch patch = new MmcPatch();
        patch.uid = uid;
        patch.name = name;
        patch.version = version;
        return patch;
    }

    private static <T> List<T> append(List<T> list, T value) {
        List<T> appended = list == null ? new ArrayList<T>() : list;
        appended.add(value);
        return appended;
    }

    private static <T> List<T> view(List<T> list) {
        return list == null ? Collections.<T>emptyList() : Collections.unmodifiableList(list);
    }

    public int formatVersion() {
        return this.formatVersion;
    }

    public String uid() {
        return this.uid;
    }

    public String name() {
        return this.name;
    }

    public String version() {
        return this.version;
    }

    public List<MmcRequirement> requires() {
        return view(this.requires);
    }

    public List<MmcRequirement> conflicts() {
        return view(this.conflicts);
    }

    /**
     * The jar that replaces Minecraft's own on the classpath.
     */
    public Library mainJar() {
        return this.mainJar;
    }

    public MmcPatch mainJar(Library mainJar) {
        this.mainJar = mainJar;
        return this;
    }

    public List<String> jvmArgs() {
        return view(this.jvmArgs);
    }

    /**
     * Launcher behaviours the component asks for, such as {@code FirstThreadOnMacOS}.
     */
    public List<String> traits() {
        return view(this.traits);
    }

    public List<String> tweakers() {
        return view(this.tweakers);
    }

    public List<Integer> compatibleJavaMajors() {
        return view(this.compatibleJavaMajors);
    }

    public String compatibleJavaName() {
        return this.compatibleJavaName;
    }

    public MmcPatch compatibleJavaName(String compatibleJavaName) {
        this.compatibleJavaName = compatibleJavaName;
        return this;
    }

    public MmcPatch require(MmcRequirement requirement) {
        this.requires = append(this.requires, requirement);
        return this;
    }

    public MmcPatch conflict(MmcRequirement conflict) {
        this.conflicts = append(this.conflicts, conflict);
        return this;
    }

    public MmcPatch jvmArg(String argument) {
        this.jvmArgs = append(this.jvmArgs, argument);
        return this;
    }

    public MmcPatch trait(String trait) {
        this.traits = append(this.traits, trait);
        return this;
    }

    public MmcPatch tweaker(String tweaker) {
        this.tweakers = append(this.tweakers, tweaker);
        return this;
    }

    public MmcPatch compatibleJavaMajor(int major) {
        this.compatibleJavaMajors = append(this.compatibleJavaMajors, major);
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
        MmcPatch that = (MmcPatch) other;
        return this.formatVersion == that.formatVersion
                && Objects.equals(this.uid, that.uid)
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.version, that.version)
                && Objects.equals(this.requires, that.requires)
                && Objects.equals(this.conflicts, that.conflicts)
                && Objects.equals(this.mainJar, that.mainJar)
                && Objects.equals(this.jvmArgs, that.jvmArgs)
                && Objects.equals(this.traits, that.traits)
                && Objects.equals(this.tweakers, that.tweakers)
                && Objects.equals(this.compatibleJavaMajors, that.compatibleJavaMajors)
                && Objects.equals(this.compatibleJavaName, that.compatibleJavaName);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode()
                + Objects.hash(this.formatVersion, this.uid, this.name, this.version, this.requires, this.conflicts,
                        this.mainJar, this.jvmArgs, this.traits, this.tweakers, this.compatibleJavaMajors,
                        this.compatibleJavaName);
    }

}
