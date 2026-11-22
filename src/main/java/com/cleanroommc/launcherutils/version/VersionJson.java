/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.version;

import com.cleanroommc.launcherutils.LauncherGson;
import com.cleanroommc.launcherutils.rule.RuleContext;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A version's metadata, as in {@code versions/<id>/<id>.json}.
 */
public class VersionJson {

    public static final String CLIENT = "client";
    public static final String SERVER = "server";
    public static final String CLIENT_MAPPINGS = "client_mappings";
    public static final String SERVER_MAPPINGS = "server_mappings";

    private String id;
    private String inheritsFrom;
    private String type;
    private String time;
    private String releaseTime;
    private String mainClass;
    private String minecraftArguments;
    private Arguments arguments;
    private AssetIndexInfo assetIndex;
    private String assets;
    private Map<String, Download> downloads;
    private JavaVersion javaVersion;
    private Logging logging;
    private List<Library> libraries;
    private Integer complianceLevel;
    private Integer minimumLauncherVersion;

    public static VersionJson read(Reader reader) {
        return LauncherGson.gson().fromJson(reader, VersionJson.class);
    }

    private static <T> List<T> concat(List<T> first, List<T> second) {
        if (first == null) {
            return second == null ? null : new ArrayList<>(second);
        }
        List<T> joined = new ArrayList<>(first);
        if (second != null) {
            joined.addAll(second);
        }
        return joined;
    }

    private static <T> T pick(T child, T parent) {
        return child != null ? child : parent;
    }

    public String id() {
        return this.id;
    }

    public VersionJson id(String id) {
        this.id = id;
        return this;
    }

    public String inheritsFrom() {
        return this.inheritsFrom;
    }

    public VersionJson inheritsFrom(String inheritsFrom) {
        this.inheritsFrom = inheritsFrom;
        return this;
    }

    public String type() {
        return this.type;
    }

    public VersionJson type(String type) {
        this.type = type;
        return this;
    }

    public String time() {
        return this.time;
    }

    public VersionJson time(String time) {
        this.time = time;
        return this;
    }

    public String releaseTime() {
        return this.releaseTime;
    }

    public VersionJson releaseTime(String releaseTime) {
        this.releaseTime = releaseTime;
        return this;
    }

    public String mainClass() {
        return this.mainClass;
    }

    public VersionJson mainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public String minecraftArguments() {
        return this.minecraftArguments;
    }

    public VersionJson minecraftArguments(String minecraftArguments) {
        this.minecraftArguments = minecraftArguments;
        return this;
    }

    public AssetIndexInfo assetIndex() {
        return this.assetIndex;
    }

    public VersionJson assetIndex(AssetIndexInfo assetIndex) {
        this.assetIndex = assetIndex;
        return this;
    }

    public String assets() {
        return this.assets;
    }

    public VersionJson assets(String assets) {
        this.assets = assets;
        return this;
    }

    public Map<String, Download> downloads() {
        return this.downloads == null ? Collections.emptyMap() : Collections.unmodifiableMap(this.downloads);
    }

    public Download download(String key) {
        return this.downloads == null ? null : this.downloads.get(key);
    }

    public VersionJson download(String key, Download download) {
        if (this.downloads == null) {
            this.downloads = new LinkedHashMap<>();
        }
        this.downloads.put(key, download);
        return this;
    }

    public JavaVersion javaVersion() {
        return this.javaVersion;
    }

    public VersionJson javaVersion(JavaVersion javaVersion) {
        this.javaVersion = javaVersion;
        return this;
    }

    /**
     * The Java major version this version asks for, or the fallback when it names none. Versions before 1.17 name
     * none.
     *
     * @param fallback the major version to assume
     */
    public int javaMajor(int fallback) {
        return this.javaVersion == null ? fallback : this.javaVersion.majorVersion();
    }

    public Logging logging() {
        return this.logging;
    }

    public VersionJson logging(Logging logging) {
        this.logging = logging;
        return this;
    }

    public List<Library> libraries() {
        return this.libraries == null ? Collections.emptyList() : Collections.unmodifiableList(this.libraries);
    }

    public List<Library> libraries(RuleContext context) {
        List<Library> allowed = new ArrayList<>();
        for (Library library : this.libraries()) {
            if (library.allowed(context)) {
                allowed.add(library);
            }
        }
        return allowed;
    }

    public VersionJson libraries(List<Library> libraries) {
        this.libraries = libraries == null ? null : new ArrayList<>(libraries);
        return this;
    }

    public int complianceLevel() {
        return this.complianceLevel == null ? 0 : this.complianceLevel;
    }

    public VersionJson complianceLevel(int complianceLevel) {
        this.complianceLevel = complianceLevel;
        return this;
    }

    public int minimumLauncherVersion() {
        return this.minimumLauncherVersion == null ? 0 : this.minimumLauncherVersion;
    }

    public VersionJson minimumLauncherVersion(int minimumLauncherVersion) {
        this.minimumLauncherVersion = minimumLauncherVersion;
        return this;
    }

    public List<Argument> gameArgumentList() {
        return this.arguments == null || this.arguments.game == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(this.arguments.game);
    }

    public List<Argument> jvmArgumentList() {
        return this.arguments == null || this.arguments.jvm == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(this.arguments.jvm);
    }

    /**
     * Whether game arguments are declared, either as {@code arguments.game} or as the older
     * {@code minecraftArguments}.
     */
    public boolean hasGameArguments() {
        return this.arguments != null && this.arguments.game != null || this.minecraftArguments != null;
    }

    /**
     * The game arguments allowed for a context with their placeholders replaced. {@code arguments.game} is used when
     * present, otherwise {@code minecraftArguments}.
     *
     * @param context what argument rules match
     * @param substitutions the placeholder values
     */
    public List<String> gameArguments(RuleContext context, Substitutions substitutions) {
        if (this.arguments != null && this.arguments.game != null) {
            return render(this.arguments.game, context, substitutions);
        }
        List<String> rendered = new ArrayList<>();
        if (this.minecraftArguments != null) {
            for (String token : this.minecraftArguments.trim().split("\\s+")) {
                if (!token.isEmpty()) {
                    rendered.add(substitutions.apply(token));
                }
            }
        }
        return rendered;
    }

    /**
     * The JVM arguments allowed for a context with their placeholders replaced. Versions before 1.13 declare none.
     *
     * @param context what argument rules match
     * @param substitutions the placeholder values
     */
    public List<String> jvmArguments(RuleContext context, Substitutions substitutions) {
        return this.arguments == null || this.arguments.jvm == null
                ? new ArrayList<>()
                : render(this.arguments.jvm, context, substitutions);
    }

    /**
     * Merges this version onto the parent it {@code inheritsFrom}, as launchers do. Fields this version sets win.
     * Libraries list this version's first, and {@code arguments} append this version's after the parent's.
     *
     * @param parent the version named by {@code inheritsFrom}
     */
    public VersionJson inherit(VersionJson parent) {
        VersionJson merged = new VersionJson();
        merged.id = pick(this.id, parent.id);
        merged.type = pick(this.type, parent.type);
        merged.time = pick(this.time, parent.time);
        merged.releaseTime = pick(this.releaseTime, parent.releaseTime);
        merged.mainClass = pick(this.mainClass, parent.mainClass);
        merged.minecraftArguments = pick(this.minecraftArguments, parent.minecraftArguments);
        merged.assetIndex = pick(this.assetIndex, parent.assetIndex);
        merged.assets = pick(this.assets, parent.assets);
        merged.javaVersion = pick(this.javaVersion, parent.javaVersion);
        merged.logging = pick(this.logging, parent.logging);
        merged.complianceLevel = pick(this.complianceLevel, parent.complianceLevel);
        merged.minimumLauncherVersion = pick(this.minimumLauncherVersion, parent.minimumLauncherVersion);
        merged.inheritsFrom = parent.inheritsFrom;
        if (parent.downloads != null || this.downloads != null) {
            merged.downloads = new LinkedHashMap<>();
            if (parent.downloads != null) {
                merged.downloads.putAll(parent.downloads);
            }
            if (this.downloads != null) {
                merged.downloads.putAll(this.downloads);
            }
        }
        merged.libraries = concat(this.libraries, parent.libraries);
        if (parent.arguments != null || this.arguments != null) {
            Arguments mine = this.arguments == null ? new Arguments() : this.arguments;
            Arguments theirs = parent.arguments == null ? new Arguments() : parent.arguments;
            merged.arguments = new Arguments();
            merged.arguments.game = concat(theirs.game, mine.game);
            merged.arguments.jvm = concat(theirs.jvm, mine.jvm);
        }
        return merged;
    }

    public VersionJson gameArgument(Argument argument) {
        this.arguments().game = concat(this.arguments().game, Collections.singletonList(argument));
        return this;
    }

    public VersionJson jvmArgument(Argument argument) {
        this.arguments().jvm = concat(this.arguments().jvm, Collections.singletonList(argument));
        return this;
    }

    public VersionJson library(Library library) {
        if (this.libraries == null) {
            this.libraries = new ArrayList<>();
        }
        this.libraries.add(library);
        return this;
    }

    private List<String> render(List<Argument> arguments, RuleContext context, Substitutions substitutions) {
        List<String> rendered = new ArrayList<>();
        for (Argument argument : arguments) {
            if (argument.allowed(context)) {
                for (String value : argument.values()) {
                    rendered.add(substitutions.apply(value));
                }
            }
        }
        return rendered;
    }

    private Arguments arguments() {
        if (this.arguments == null) {
            this.arguments = new Arguments();
        }
        return this.arguments;
    }

    private static final class Arguments {

        private List<Argument> game;
        private List<Argument> jvm;

    }

}
