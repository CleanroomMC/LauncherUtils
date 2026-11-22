/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.version;

import com.cleanroommc.launcherutils.rule.Rule;
import com.cleanroommc.launcherutils.rule.RuleContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * One entry of an {@code arguments} list. It is written as a bare string, or as {@code rules} and a {@code value} that
 * is one string or a list.
 */
public final class Argument {

    private final List<String> values;

    private List<Rule> rules;

    private Argument(List<String> values) {
        this.values = values;
    }

    public static Argument of(String... values) {
        return new Argument(new ArrayList<>(Arrays.asList(values)));
    }

    public List<String> values() {
        return Collections.unmodifiableList(this.values);
    }

    public List<Rule> rules() {
        return this.rules == null ? null : Collections.unmodifiableList(this.rules);
    }

    public boolean allowed(RuleContext context) {
        return Rule.allows(this.rules, context);
    }

    public Argument rule(Rule rule) {
        if (this.rules == null) {
            this.rules = new ArrayList<>();
        }
        this.rules.add(rule);
        return this;
    }

    /**
     * Reads and writes either form. {@link com.cleanroommc.launcherutils.LauncherGson#register} installs it.
     */
    public static final class Adapter implements JsonSerializer<Argument>, JsonDeserializer<Argument> {

        private static final Type RULES = new TypeToken<List<Rule>>() { }.getType();

        @Override
        public Argument deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            if (json.isJsonPrimitive()) {
                return Argument.of(json.getAsString());
            }
            JsonObject object = json.getAsJsonObject();
            JsonElement value = object.get("value");
            List<String> values = new ArrayList<>();
            if (value.isJsonArray()) {
                for (JsonElement element : value.getAsJsonArray()) {
                    values.add(element.getAsString());
                }
            } else {
                values.add(value.getAsString());
            }
            Argument argument = new Argument(values);
            argument.rules = context.deserialize(object.get("rules"), RULES);
            return argument;
        }

        @Override
        public JsonElement serialize(Argument argument, Type type, JsonSerializationContext context) {
            if (argument.rules == null && argument.values.size() == 1) {
                return new JsonPrimitive(argument.values.get(0));
            }
            JsonObject object = new JsonObject();
            if (argument.rules != null) {
                object.add("rules", context.serialize(argument.rules, RULES));
            }
            if (argument.values.size() == 1) {
                object.addProperty("value", argument.values.get(0));
            } else {
                JsonArray values = new JsonArray();
                for (String value : argument.values) {
                    values.add(new JsonPrimitive(value));
                }
                object.add("value", values);
            }
            return object;
        }

    }

}
