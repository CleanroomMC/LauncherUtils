/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.version;

import com.cleanroommc.launcherutils.Coordinate;
import com.cleanroommc.launcherutils.LauncherNames;
import com.cleanroommc.launcherutils.rule.Rule;
import com.cleanroommc.launcherutils.rule.RuleContext;
import com.cleanroommc.platformutils.Architecture;
import com.cleanroommc.platformutils.Platform;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One entry of a {@code libraries} list.
 */
public final class Library {

    private String name;
    private String url;
    private Downloads downloads;
    private Map<String, String> natives;
    private List<Rule> rules;
    private Extract extract;
    private String side;
    @SerializedName("MMC-hint") private String mmcHint;

    public static Library of(String name) {
        return new Library().name(name);
    }

    public static Library of(Coordinate coordinate) {
        return of(coordinate.toString());
    }

    public String name() {
        return this.name;
    }

    public Library name(String name) {
        this.name = name;
        return this;
    }

    public Coordinate coordinate() {
        return Coordinate.parse(this.name);
    }

    /**
     * The repository to fetch from when there are no {@code downloads}, as Forge profiles write it.
     */
    public String url() {
        return this.url;
    }

    public Library url(String url) {
        this.url = url;
        return this;
    }

    public Download artifact() {
        return this.downloads == null ? null : this.downloads.artifact;
    }

    public Library artifact(Download artifact) {
        this.downloads().artifact = artifact;
        return this;
    }

    public Download classifier(String classifier) {
        return this.downloads == null || this.downloads.classifiers == null
                ? null
                : this.downloads.classifiers.get(classifier);
    }

    public Library classifier(String classifier, Download download) {
        Downloads downloads = this.downloads();
        if (downloads.classifiers == null) {
            downloads.classifiers = new LinkedHashMap<>();
        }
        downloads.classifiers.put(classifier, download);
        return this;
    }

    public Map<String, Download> classifiers() {
        return this.downloads == null || this.downloads.classifiers == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(this.downloads.classifiers);
    }

    public boolean hasDownloads() {
        return this.downloads != null;
    }

    public Map<String, String> natives() {
        return this.natives == null ? Collections.emptyMap() : Collections.unmodifiableMap(this.natives);
    }

    /**
     * Maps a system name, such as {@code linux} or {@code linux-arm64}, to the classifier holding its natives.
     *
     * @param system the system name
     * @param classifier the classifier holding its natives
     */
    public Library natives(String system, String classifier) {
        if (this.natives == null) {
            this.natives = new LinkedHashMap<>();
        }
        this.natives.put(system, classifier);
        return this;
    }

    public boolean hasNatives() {
        return this.natives != null;
    }

    public List<Rule> rules() {
        return this.rules == null ? null : Collections.unmodifiableList(this.rules);
    }

    public Library rules(List<Rule> rules) {
        this.rules = rules == null ? null : new ArrayList<>(rules);
        return this;
    }

    public List<String> extractExcludes() {
        return this.extract == null || this.extract.exclude == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(this.extract.exclude);
    }

    public Library extractExcludes(String... excludes) {
        this.extract = new Extract();
        this.extract.exclude = new ArrayList<>(Arrays.asList(excludes));
        return this;
    }

    /**
     * The side an installer profile limits this library to, such as {@code client}.
     */
    public String side() {
        return this.side;
    }

    public Library side(String side) {
        this.side = side;
        return this;
    }

    public String mmcHint() {
        return this.mmcHint;
    }

    public Library mmcHint(String mmcHint) {
        this.mmcHint = mmcHint;
        return this;
    }

    public boolean allowed(RuleContext context) {
        return Rule.allows(this.rules, context);
    }

    /**
     * The natives classifier for a context, or null when {@code natives} names none for it.
     * <p>
     * The key is the MultiMC system name such as {@code linux-arm64} first, then the bare system name where the
     * dialect lets it match. {@code ${arch}} becomes {@code 32} or {@code 64}. Off x64, a classifier with the
     * architecture appended, such as {@code natives-linux-arm64}, is preferred when it is downloadable.
     *
     * @param context what the rules and natives match
     */
    public String nativeClassifier(RuleContext context) {
        if (this.natives == null) {
            return null;
        }
        Platform platform = context.platform();
        String classifier = this.natives.get(LauncherNames.mmcSystem(platform));
        if (classifier == null && context.bareSystemMatches()) {
            classifier = this.natives.get(LauncherNames.os(platform));
        }
        if (classifier == null) {
            return null;
        }
        classifier = classifier.replace("${arch}", platform.is64Bit() ? "64" : "32");
        if (platform.architecture() != Architecture.X64) {
            String variant = classifier + "-" + LauncherNames.arch(platform.architecture());
            if (this.classifier(variant) != null) {
                return variant;
            }
        }
        return classifier;
    }

    public Download nativeDownload(RuleContext context) {
        String classifier = this.nativeClassifier(context);
        return classifier == null ? null : this.classifier(classifier);
    }

    public Library rule(Rule rule) {
        if (this.rules == null) {
            this.rules = new ArrayList<>();
        }
        this.rules.add(rule);
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        Library that = (Library) other;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.url, that.url)
                && Objects.equals(this.downloads, that.downloads)
                && Objects.equals(this.natives, that.natives)
                && Objects.equals(this.rules, that.rules)
                && Objects.equals(this.extract, that.extract)
                && Objects.equals(this.side, that.side)
                && Objects.equals(this.mmcHint, that.mmcHint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.url, this.downloads, this.natives, this.rules, this.extract, this.side,
                this.mmcHint);
    }

    private Downloads downloads() {
        if (this.downloads == null) {
            this.downloads = new Downloads();
        }
        return this.downloads;
    }

    private static final class Downloads {

        private Download artifact;
        private Map<String, Download> classifiers;

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || this.getClass() != other.getClass()) {
                return false;
            }
            Downloads that = (Downloads) other;
            return Objects.equals(this.artifact, that.artifact) && Objects.equals(this.classifiers, that.classifiers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.artifact, this.classifiers);
        }

    }

    private static final class Extract {

        private List<String> exclude;

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || this.getClass() != other.getClass()) {
                return false;
            }
            Extract that = (Extract) other;
            return Objects.equals(this.exclude, that.exclude);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.exclude);
        }

    }

}
