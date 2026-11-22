/*
 * Copyright (c) 2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.launcherutils.version;

import java.util.Objects;

/**
 * The logging configuration a version ships, keyed by side.
 */
public final class Logging {

    private Config client;
    private Config server;

    public Config client() {
        return this.client;
    }

    public Logging client(Config client) {
        this.client = client;
        return this;
    }

    public Config server() {
        return this.server;
    }

    public Logging server(Config server) {
        this.server = server;
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
        Logging that = (Logging) other;
        return Objects.equals(this.client, that.client) && Objects.equals(this.server, that.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.client, this.server);
    }

    public static final class Config {

        private String argument;
        private LogFile file;
        private String type;

        /**
         * The JVM argument that points at the file, with a {@code ${path}} placeholder.
         */
        public String argument() {
            return this.argument;
        }

        public Config argument(String argument) {
            this.argument = argument;
            return this;
        }

        public LogFile file() {
            return this.file;
        }

        public Config file(LogFile file) {
            this.file = file;
            return this;
        }

        public String type() {
            return this.type;
        }

        public Config type(String type) {
            this.type = type;
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
            Config that = (Config) other;
            return Objects.equals(this.argument, that.argument)
                    && Objects.equals(this.file, that.file)
                    && Objects.equals(this.type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.argument, this.file, this.type);
        }

    }

    public static final class LogFile extends Download {

        private String id;

        public String id() {
            return this.id;
        }

        public LogFile id(String id) {
            this.id = id;
            return this;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            LogFile that = (LogFile) other;
            return Objects.equals(this.id, that.id);
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + Objects.hash(this.id);
        }

    }

}
