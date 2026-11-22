/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.asset;

import com.cleanroommc.launcherutils.LauncherGson;
import com.cleanroommc.launcherutils.MojangEndpoints;

import com.google.gson.annotations.SerializedName;

import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An asset index, as in {@code assets/indexes/<id>.json}.
 */
public final class AssetIndex {

    private Map<String, AssetObject> objects;
    private Boolean virtual;
    @SerializedName("map_to_resources") private Boolean mapToResources;

    public static AssetIndex read(Reader reader) {
        return LauncherGson.read(reader, AssetIndex.class);
    }

    /**
     * Every asset by its resource path, such as {@code minecraft/sounds/ambient/cave/cave1.ogg}.
     */
    public Map<String, AssetObject> objects() {
        return this.objects == null ? Collections.emptyMap() : Collections.unmodifiableMap(this.objects);
    }

    /**
     * Whether launchers also copy each asset to {@code assets/virtual/<id>/<path>}, as indexes before 1.7.3 ask.
     */
    public boolean isVirtual() {
        return Boolean.TRUE.equals(this.virtual);
    }

    /**
     * Whether launchers also copy each asset to {@code <game directory>/resources/<path>}, as pre-1.6 indexes ask.
     */
    public boolean mapsToResources() {
        return Boolean.TRUE.equals(this.mapToResources);
    }

    public AssetIndex object(String path, AssetObject object) {
        if (this.objects == null) {
            this.objects = new LinkedHashMap<>();
        }
        this.objects.put(path, object);
        return this;
    }

    public AssetIndex virtual(boolean virtual) {
        this.virtual = virtual;
        return this;
    }

    public AssetIndex mapToResources(boolean mapToResources) {
        this.mapToResources = mapToResources;
        return this;
    }

    public static final class AssetObject {

        private String hash;
        private long size;

        public static AssetObject of(String hash, long size) {
            AssetObject object = new AssetObject();
            object.hash = hash;
            object.size = size;
            return object;
        }

        public String hash() {
            return this.hash;
        }

        public long size() {
            return this.size;
        }

        /**
         * The path under {@code assets/objects}, the hash's first two characters then the hash.
         */
        public String path() {
            return this.hash.substring(0, 2) + '/' + this.hash;
        }

        public String url() {
            return MojangEndpoints.RESOURCES + this.path();
        }

    }

}
