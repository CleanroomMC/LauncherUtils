/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.rule;

import com.cleanroommc.launcherutils.LauncherNames;
import com.cleanroommc.platformutils.Architecture;
import com.cleanroommc.platformutils.OperatingSystem;
import com.cleanroommc.platformutils.Platform;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * One entry of a {@code rules} list, which gates a library or an argument.
 *
 * <p>A rule applies when every condition it sets matches.
 * {@code os.name} is a system name, or a system joined to an architecture such as {@code linux-arm64}.
 * {@code os.arch} is an architecture.
 * {@code os.version} is a regular expression found in the OS version.
 * Each of {@code features} has to equal the context's value.
 */
public final class Rule {

    private Action action;
    private Os os;
    private Map<String, Boolean> features;

    public static Rule allow() {
        Rule rule = new Rule();
        rule.action = Action.ALLOW;
        return rule;
    }

    public static Rule disallow() {
        Rule rule = new Rule();
        rule.action = Action.DISALLOW;
        return rule;
    }

    /**
     * Evaluates a {@code rules} list. A missing list allows, an empty list disallows, and otherwise the last rule that
     * applies decides.
     *
     * @param rules the list, or null when the entry has none
     * @param context what the rules match
     */
    public static boolean allows(List<Rule> rules, RuleContext context) {
        if (rules == null) {
            return true;
        }
        boolean allowed = false;
        for (Rule rule : rules) {
            if (rule.appliesTo(context)) {
                allowed = rule.isAllow();
            }
        }
        return allowed;
    }

    public Rule os(String name) {
        this.os().name = name;
        return this;
    }

    public Rule os(OperatingSystem os) {
        return this.os(LauncherNames.os(os));
    }

    private Os os() {
        if (this.os == null) {
            this.os = new Os();
        }
        return this.os;
    }

    /**
     * Matches a platform the way MultiMC and Prism name it, such as {@code linux-arm64}.
     *
     * @param platform the platform to match
     */
    public Rule mmcSystem(Platform platform) {
        return this.os(LauncherNames.mmcSystem(platform));
    }

    public Rule arch(String arch) {
        this.os().arch = arch;
        return this;
    }

    public Rule arch(Architecture architecture) {
        return this.arch(LauncherNames.arch(architecture));
    }

    public Rule osVersion(String regex) {
        this.os().version = regex;
        return this;
    }

    public String osVersion() {
        return this.os == null ? null : this.os.version;
    }

    public Rule feature(String name, boolean enabled) {
        if (this.features == null) {
            this.features = new LinkedHashMap<>();
        }
        this.features.put(name, enabled);
        return this;
    }

    public Action action() {
        return this.action;
    }

    public boolean isAllow() {
        return this.action == Action.ALLOW;
    }

    public String osName() {
        return this.os == null ? null : this.os.name;
    }

    public String osArch() {
        return this.os == null ? null : this.os.arch;
    }

    public Map<String, Boolean> features() {
        return this.features == null ? Collections.emptyMap() : Collections.unmodifiableMap(this.features);
    }

    public boolean appliesTo(RuleContext context) {
        if (this.features != null) {
            for (Map.Entry<String, Boolean> feature : this.features.entrySet()) {
                if (context.feature(feature.getKey()) != Boolean.TRUE.equals(feature.getValue())) {
                    return false;
                }
            }
        }
        return this.os == null || this.os.matches(context);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        Rule that = (Rule) other;
        return Objects.equals(this.action, that.action)
                && Objects.equals(this.os, that.os)
                && Objects.equals(this.features, that.features);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.action, this.os, this.features);
    }

    public enum Action {

        @SerializedName("allow") ALLOW,
        @SerializedName("disallow") DISALLOW

    }

    private static final class Os {

        private String name;
        private String arch;
        private String version;

        private static boolean sameSystem(String name, Platform platform) {
            OperatingSystem os = LauncherNames.parseOs(name);
            return os != OperatingSystem.UNKNOWN && LauncherNames.os(os).equals(LauncherNames.os(platform));
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || this.getClass() != other.getClass()) {
                return false;
            }
            Os that = (Os) other;
            return Objects.equals(this.name, that.name)
                    && Objects.equals(this.arch, that.arch)
                    && Objects.equals(this.version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.arch, this.version);
        }

        private boolean matches(RuleContext context) {
            Platform platform = context.platform();
            if (this.name != null && !this.nameMatches(context)) {
                return false;
            }
            if (this.arch != null && LauncherNames.parseArch(this.arch) != platform.architecture()) {
                return false;
            }
            if (this.version != null) {
                String osVersion = context.osVersion();
                return osVersion != null && Pattern.compile(this.version).matcher(osVersion).find();
            }
            return true;
        }

        private boolean nameMatches(RuleContext context) {
            Platform platform = context.platform();
            if (sameSystem(this.name, platform)) {
                return context.bareSystemMatches();
            }
            int dash = this.name.indexOf('-');
            return dash > 0
                    && sameSystem(this.name.substring(0, dash), platform)
                    && LauncherNames.parseArch(this.name.substring(dash + 1)) == platform.architecture();
        }

    }

}
