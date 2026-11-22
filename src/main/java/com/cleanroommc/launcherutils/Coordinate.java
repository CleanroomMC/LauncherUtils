/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils;

import java.util.Objects;

/**
 * A Maven coordinate as library names write it, {@code group:artifact:version[:classifier][@extension]}.
 */
public final class Coordinate {

    private final String group;
    private final String artifact;
    private final String version;
    private final String classifier;
    private final String extension;

    private Coordinate(String group, String artifact, String version, String classifier, String extension) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.classifier = classifier == null || classifier.isEmpty() ? null : classifier;
        this.extension = extension == null || extension.isEmpty() ? "jar" : extension;
    }

    public static Coordinate of(String group, String artifact, String version) {
        return new Coordinate(group, artifact, version, null, null);
    }

    /**
     * Parses {@code group:artifact:version[:classifier][@extension]}. An empty classifier means none.
     *
     * @param notation the coordinate notation
     * @throws IllegalArgumentException when a required part is blank or the notation has too many parts
     */
    public static Coordinate parse(String notation) {
        String remainder = notation;
        String extension = null;
        int at = remainder.indexOf('@');
        if (at >= 0) {
            extension = remainder.substring(at + 1);
            remainder = remainder.substring(0, at);
            if (isBlank(extension) || extension.indexOf('@') >= 0) {
                throw new IllegalArgumentException("Not a Maven coordinate: " + notation);
            }
        }
        String[] parts = remainder.split(":", -1);
        if (parts.length < 3 || parts.length > 4 || isBlank(parts[0]) || isBlank(parts[1]) || isBlank(parts[2])) {
            throw new IllegalArgumentException("Not a Maven coordinate: " + notation);
        }
        return new Coordinate(parts[0], parts[1], parts[2], parts.length == 4 ? parts[3] : null, extension);
    }

    private static boolean isBlank(String value) {
        return value.trim().isEmpty();
    }

    public String group() {
        return this.group;
    }

    public String artifact() {
        return this.artifact;
    }

    public String version() {
        return this.version;
    }

    public String classifier() {
        return this.classifier;
    }

    public String extension() {
        return this.extension;
    }

    /**
     * The {@code group:artifact} this coordinate is a version of.
     */
    public String module() {
        return this.group + ":" + this.artifact;
    }

    public Coordinate withClassifier(String classifier) {
        return new Coordinate(this.group, this.artifact, this.version, classifier, this.extension);
    }

    public Coordinate withoutClassifier() {
        return this.withClassifier(null);
    }

    public Coordinate withExtension(String extension) {
        return new Coordinate(this.group, this.artifact, this.version, this.classifier, extension);
    }

    /**
     * The path in a Maven repository, such as {@code org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-linux.jar}.
     */
    public String path() {
        return this.group.replace('.', '/') + '/' + this.artifact + '/' + this.version + '/' + this.fileName();
    }

    public String fileName() {
        return this.artifact + '-' + this.version + (this.classifier == null ? "" : '-' + this.classifier) + '.'
                + this.extension;
    }

    @Override
    public String toString() {
        return this.module() + ':' + this.version + (this.classifier == null ? "" : ':' + this.classifier)
                + ("jar".equals(this.extension) ? "" : '@' + this.extension);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Coordinate)) {
            return false;
        }
        Coordinate that = (Coordinate) other;
        return this.group.equals(that.group)
                && this.artifact.equals(that.artifact)
                && this.version.equals(that.version)
                && Objects.equals(this.classifier, that.classifier)
                && this.extension.equals(that.extension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.group, this.artifact, this.version, this.classifier, this.extension);
    }

}
