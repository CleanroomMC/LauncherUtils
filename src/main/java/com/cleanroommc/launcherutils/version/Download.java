/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.version;

import com.cleanroommc.launcherutils.Coordinate;

/**
 * A file to download, with its hash and size.
 */
public class Download {

    private String path;
    private String sha1;
    private Long size;
    private String url;

    public String path() {
        return this.path;
    }

    /**
     * The path under the libraries directory. Without a {@code path}, it is taken from the URL when the URL holds the
     * coordinate's Maven path, since classifier downloads share their library's name.
     * Otherwise, it is the coordinate's own path.
     *
     * @param coordinate the library this file belongs to
     */
    public String path(Coordinate coordinate) {
        if (this.path != null && !this.path.isEmpty()) {
            return this.path;
        }
        if (this.url != null) {
            String prefix = coordinate.group().replace('.', '/') + '/' + coordinate.artifact() + '/';
            int index = this.url.indexOf(prefix);
            if (index >= 0) {
                return this.url.substring(index);
            }
        }
        return coordinate.path();
    }

    public Download path(String path) {
        this.path = path;
        return this;
    }

    public String sha1() {
        return this.sha1;
    }

    public Download sha1(String sha1) {
        this.sha1 = sha1;
        return this;
    }

    public long size() {
        return this.size == null ? 0 : this.size;
    }

    public Download size(long size) {
        this.size = size;
        return this;
    }

    public String url() {
        return this.url;
    }

    public Download url(String url) {
        this.url = url;
        return this;
    }

    /**
     * Whether the file ships inside the installer, which installer profiles mark with an empty URL.
     */
    public boolean isEmbedded() {
        return this.url == null || this.url.trim().isEmpty();
    }

}
