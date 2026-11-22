/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.version;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces the {@code ${name}} placeholders in launch arguments.
 */
public final class Substitutions {

    public static final String AUTH_PLAYER_NAME = "auth_player_name";
    public static final String AUTH_UUID = "auth_uuid";
    public static final String AUTH_ACCESS_TOKEN = "auth_access_token";
    public static final String AUTH_XUID = "auth_xuid";
    public static final String AUTH_SESSION = "auth_session";
    public static final String CLIENT_ID = "clientid";
    public static final String USER_TYPE = "user_type";
    public static final String USER_PROPERTIES = "user_properties";
    public static final String VERSION_NAME = "version_name";
    public static final String VERSION_TYPE = "version_type";
    public static final String GAME_DIRECTORY = "game_directory";
    public static final String ASSETS_ROOT = "assets_root";
    public static final String GAME_ASSETS = "game_assets";
    public static final String ASSETS_INDEX_NAME = "assets_index_name";
    public static final String NATIVES_DIRECTORY = "natives_directory";
    public static final String LIBRARY_DIRECTORY = "library_directory";
    public static final String CLASSPATH = "classpath";
    public static final String CLASSPATH_SEPARATOR = "classpath_separator";
    public static final String LAUNCHER_NAME = "launcher_name";
    public static final String LAUNCHER_VERSION = "launcher_version";
    public static final String RESOLUTION_WIDTH = "resolution_width";
    public static final String RESOLUTION_HEIGHT = "resolution_height";
    public static final String QUICK_PLAY_PATH = "quickPlayPath";
    public static final String QUICK_PLAY_SINGLEPLAYER = "quickPlaySingleplayer";
    public static final String QUICK_PLAY_MULTIPLAYER = "quickPlayMultiplayer";
    public static final String QUICK_PLAY_REALMS = "quickPlayRealms";

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    private final Map<String, String> values = new LinkedHashMap<>();

    private Function<String, String> missing = name -> "${" + name + "}";

    public static Substitutions create() {
        return new Substitutions();
    }

    public Substitutions put(String name, String value) {
        this.values.put(name, value);
        return this;
    }

    public Substitutions putAll(Map<String, String> values) {
        this.values.putAll(values);
        return this;
    }

    /**
     * What an unknown placeholder becomes, given its name. By default, it stays as written.
     *
     * @param missing maps a placeholder name to its replacement
     */
    public Substitutions onMissing(Function<String, String> missing) {
        this.missing = missing;
        return this;
    }

    public String apply(String value) {
        Matcher matcher = PLACEHOLDER.matcher(value);
        StringBuffer builder = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1);
            String replacement = this.values.get(name);
            if (replacement == null) {
                replacement = this.missing.apply(name);
            }
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

}
