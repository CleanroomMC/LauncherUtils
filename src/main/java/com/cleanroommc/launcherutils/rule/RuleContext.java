/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.rule;

import com.cleanroommc.platformutils.Architecture;
import com.cleanroommc.platformutils.Platform;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * What rules are evaluated against. Every method returns a new context.
 */
public final class RuleContext {

    private final Platform platform;
    private final String osVersion;
    private final Map<String, Boolean> features;
    private final Dialect dialect;

    private RuleContext(Platform platform, String osVersion, Map<String, Boolean> features, Dialect dialect) {
        this.platform = platform;
        this.osVersion = osVersion;
        this.features = features;
        this.dialect = dialect;
    }

    /**
     * A context with no OS version, no features and the {@link Dialect#MOJANG} dialect.
     *
     * @param platform the platform rules match
     */
    public static RuleContext of(Platform platform) {
        return new RuleContext(platform, null, Collections.emptyMap(), Dialect.MOJANG);
    }

    /**
     * The running platform and its {@code os.version}.
     */
    public static RuleContext current() {
        return of(Platform.current()).osVersion(System.getProperty("os.version"));
    }

    public RuleContext osVersion(String osVersion) {
        return new RuleContext(this.platform, osVersion, this.features, this.dialect);
    }

    public String osVersion() {
        return this.osVersion;
    }

    public RuleContext feature(String name, boolean enabled) {
        Map<String, Boolean> features = new HashMap<>(this.features);
        features.put(name, enabled);
        return new RuleContext(this.platform, this.osVersion, Collections.unmodifiableMap(features), this.dialect);
    }

    /**
     * Whether a feature is enabled. Features the context doesn't set are disabled.
     *
     * @param name the feature, such as {@code is_demo_user}
     */
    public boolean feature(String name) {
        return Boolean.TRUE.equals(this.features.get(name));
    }

    public RuleContext dialect(Dialect dialect) {
        return new RuleContext(this.platform, this.osVersion, this.features, dialect);
    }

    public Dialect dialect() {
        return this.dialect;
    }

    public Platform platform() {
        return this.platform;
    }

    /**
     * Whether a bare system name such as {@code linux} matches this context's architecture.
     */
    public boolean bareSystemMatches() {
        Architecture architecture = this.platform.architecture();
        return this.dialect == Dialect.MOJANG || architecture == Architecture.X86 || architecture == Architecture.X64;
    }

}
