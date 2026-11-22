/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.rule;

import com.cleanroommc.platformutils.Architecture;
import com.cleanroommc.platformutils.OperatingSystem;
import com.cleanroommc.platformutils.Platform;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class RuleTest {

    private static final RuleContext LINUX_X64 = RuleContext.of(Platform.of(OperatingSystem.LINUX, Architecture.X64));
    private static final RuleContext LINUX_ARM64 = RuleContext.of(Platform.of(OperatingSystem.LINUX,
            Architecture.ARM64));
    private static final RuleContext FREEBSD_X64 = RuleContext.of(Platform.of(OperatingSystem.FREE_BSD,
            Architecture.X64));
    private static final RuleContext MACOS_ARM64 = RuleContext.of(Platform.of(OperatingSystem.MAC_OS,
            Architecture.ARM64));

    @Test
    void missingRulesAllowAndEmptyRulesDisallow() {
        assertThat(Rule.allows(null, LINUX_X64)).isTrue();
        assertThat(Rule.allows(Collections.<Rule>emptyList(), LINUX_X64)).isFalse();
    }

    @Test
    void theLastRuleThatAppliesDecides() {
        assertThat(Rule.allows(Arrays.asList(Rule.allow(), Rule.disallow().os("osx")), MACOS_ARM64)).isFalse();
        assertThat(Rule.allows(Arrays.asList(Rule.allow(), Rule.disallow().os("osx")), LINUX_X64)).isTrue();
        assertThat(Rule.allows(Collections.singletonList(Rule.allow().os("osx")), LINUX_X64)).isFalse();
    }

    @Test
    void freeBsdIsItsOwnSystem() {
        assertThat(Rule.allow().os("linux").appliesTo(FREEBSD_X64)).isFalse();
        assertThat(Rule.allow().os("freebsd").appliesTo(FREEBSD_X64)).isTrue();
        assertThat(Rule.allow().os(OperatingSystem.FREE_BSD).arch(Architecture.X64).appliesTo(FREEBSD_X64)).isTrue();
        assertThat(Rule.allow().os("freebsd").appliesTo(LINUX_X64)).isFalse();
        assertThat(Rule.allows(Arrays.asList(Rule.allow(), Rule.disallow().os("linux")), FREEBSD_X64)).isTrue();
    }

    @Test
    void archAcceptsEverySpelling() {
        for (String arch : Arrays.asList("arm64", "aarch64", "aarch_64")) {
            assertThat(Rule.allow().arch(arch).appliesTo(LINUX_ARM64)).as(arch).isTrue();
            assertThat(Rule.allow().arch(arch).appliesTo(LINUX_X64)).as(arch).isFalse();
        }
        assertThat(Rule.allow().arch("x86").appliesTo(LINUX_X64)).isFalse();
        assertThat(Rule.allow().arch("x86_64").appliesTo(LINUX_X64)).isTrue();
    }

    @Test
    void osVersionIsARegularExpressionFoundInTheVersion() {
        Rule rule = Rule.allow().os("windows").osVersion("^10\\.");
        RuleContext windows = RuleContext.of(Platform.of(OperatingSystem.WINDOWS, Architecture.X64));
        assertThat(rule.appliesTo(windows.osVersion("10.0"))).isTrue();
        assertThat(rule.appliesTo(windows.osVersion("6.1"))).isFalse();
        assertThat(rule.appliesTo(windows)).isFalse();
    }

    @Test
    void featuresHaveToEqualTheContext() {
        Rule demo = Rule.allow().feature("is_demo_user", true);
        assertThat(demo.appliesTo(LINUX_X64)).isFalse();
        assertThat(demo.appliesTo(LINUX_X64.feature("is_demo_user", true))).isTrue();
        assertThat(Rule.allow().feature("is_demo_user", false).appliesTo(LINUX_X64)).isTrue();
    }

    @Test
    void joinedNamesMatchTheirArchitectureInEitherDialect() {
        Rule arm = Rule.allow().os("linux-arm64");
        assertThat(arm.appliesTo(LINUX_ARM64)).isTrue();
        assertThat(arm.appliesTo(LINUX_ARM64.dialect(Dialect.MMC))).isTrue();
        assertThat(arm.appliesTo(LINUX_X64)).isFalse();
        assertThat(Rule.allow().os("osx-arm64").appliesTo(MACOS_ARM64)).isTrue();
        assertThat(Rule.allow().os("linux-x86_64").appliesTo(LINUX_X64)).isTrue();
    }

    @Test
    void mmcBareNamesMatchOnlyX86AndX64() {
        Rule linux = Rule.allow().os("linux");
        assertThat(linux.appliesTo(LINUX_ARM64)).isTrue();
        assertThat(linux.appliesTo(LINUX_ARM64.dialect(Dialect.MMC))).isFalse();
        assertThat(linux.appliesTo(LINUX_X64.dialect(Dialect.MMC))).isTrue();
    }

    @Test
    void unknownSystemNamesNeverMatch() {
        RuleContext unknown = RuleContext.of(Platform.of(OperatingSystem.UNKNOWN, Architecture.X64));
        assertThat(Rule.allow().os("unknown").appliesTo(unknown)).isFalse();
        assertThat(Rule.allow().os("templeos").appliesTo(LINUX_X64)).isFalse();
    }

}
