/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.manifest;

import com.cleanroommc.launcherutils.LauncherGson;

import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Mojang's list of every version, {@code version_manifest_v2.json}.
 */
public final class VersionManifest {

    private Latest latest;
    private List<Entry> versions;

    public static VersionManifest read(Reader reader) {
        return LauncherGson.read(reader, VersionManifest.class);
    }

    public List<Entry> versions() {
        return this.versions == null ? Collections.emptyList() : Collections.unmodifiableList(this.versions);
    }

    public Optional<Entry> find(String id) {
        for (Entry entry : this.versions()) {
            if (id.equals(entry.id)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public Optional<Entry> latestRelease() {
        return this.latest == null || this.latest.release == null ? Optional.empty() : this.find(this.latest.release);
    }

    public Optional<Entry> latestSnapshot() {
        return this.latest == null || this.latest.snapshot == null ? Optional.empty() : this.find(this.latest.snapshot);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        VersionManifest that = (VersionManifest) other;
        return Objects.equals(this.latest, that.latest) && Objects.equals(this.versions, that.versions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.latest, this.versions);
    }

    private static final class Latest {

        private String release;
        private String snapshot;

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || this.getClass() != other.getClass()) {
                return false;
            }
            Latest that = (Latest) other;
            return Objects.equals(this.release, that.release) && Objects.equals(this.snapshot, that.snapshot);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.release, this.snapshot);
        }

    }

    public static final class Entry {

        private String id;
        private String type;
        private String url;
        private String time;
        private String releaseTime;
        private String sha1;
        private int complianceLevel;

        public String id() {
            return this.id;
        }

        /**
         * One of {@code release}, {@code snapshot}, {@code old_beta} or {@code old_alpha}.
         */
        public String type() {
            return this.type;
        }

        /**
         * Where the version's {@link com.cleanroommc.launcherutils.version.VersionJson} is.
         */
        public String url() {
            return this.url;
        }

        public String time() {
            return this.time;
        }

        public String releaseTime() {
            return this.releaseTime;
        }

        /**
         * The SHA-1 of the version's metadata. Only the v2 manifest has it.
         */
        public String sha1() {
            return this.sha1;
        }

        public int complianceLevel() {
            return this.complianceLevel;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || this.getClass() != other.getClass()) {
                return false;
            }
            Entry that = (Entry) other;
            return Objects.equals(this.id, that.id)
                    && Objects.equals(this.type, that.type)
                    && Objects.equals(this.url, that.url)
                    && Objects.equals(this.time, that.time)
                    && Objects.equals(this.releaseTime, that.releaseTime)
                    && Objects.equals(this.sha1, that.sha1)
                    && this.complianceLevel == that.complianceLevel;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id, this.type, this.url, this.time, this.releaseTime, this.sha1,
                    this.complianceLevel);
        }

    }

}
