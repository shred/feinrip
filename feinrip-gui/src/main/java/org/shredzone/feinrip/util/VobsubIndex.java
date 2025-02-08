/*
 * feinrip
 *
 * Copyright (C) 2015 Richard "Shred" Körber
 *   https://codeberg.org/shred/feinrip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.feinrip.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.EnumMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads and writes VOBsub index files.
 *
 * @see <a href="http://wiki.multimedia.cx/index.php?title=VOBsub">VOBsub index format
 *      reference</a>
 * @author Richard "Shred" Körber
 */
public class VobsubIndex {

    private static final String TYPE_LINE = "# VobSub index file, v7 (do not modify this line!)";
    private static final Pattern TS_PATTERN = Pattern.compile("([0-9:]+),\\s*filepos:\\s*([0-9a-fA-F]+)");

    private final EnumMap<Setting, String> settings = new EnumMap<>(Setting.class);
    private final SortedMap<Timestamp, String> timestamps = new TreeMap<>();

    /**
     * Resets this subtitle set.
     */
    public void clear() {
        settings.clear();
        timestamps.clear();
    }

    /**
     * Returns the setting with the given key.
     *
     * @param key
     *            {@link Setting} key
     * @return Value, or {@code null} if the setting was not set
     */
    public String get(Setting key) {
        return settings.get(key);
    }

    /**
     * Sets a setting. If the setting is already set, it is changed to the new value.
     *
     * @param key
     *            {@link Setting} to change
     * @param value
     *            Value to set
     */
    public VobsubIndex set(Setting key, String value) {
        settings.put(key, value);
        return this;
    }

    /**
     * Sets a timestamp and file position. Timestamps may appear in any order, they are
     * sorted automatically. If a timestamp was already set, it will be replaced by the
     * new filepos value.
     *
     * @param ts
     *            {@link Timestamp} to set
     * @param filepos
     *            File position
     */
    public VobsubIndex timestamp(Timestamp ts, String filepos) {
        timestamps.put(ts, filepos);
        return this;
    }

    /**
     * Returns all timestamps, in chronological order.
     */
    public SortedMap<Timestamp, String> getTimestamps() {
        return Collections.unmodifiableSortedMap(timestamps);
    }

    /**
     * Reads a VOBsub index file. Does some sanitizing, but throws an exception if the
     * file is too broken.
     *
     * @param idxFile
     *            File to read
     */
    public void read(File idxFile) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(idxFile))) {
            String line = in.readLine();
            if (!TYPE_LINE.equals(line)) {
                throw new IOException("not a VOBsub index file");
            }

            Timestamp lastTs = null;

            while ((line = in.readLine()) != null) {
                // ignore comments and empty lines
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int pos = line.indexOf(':');
                if (pos < 0 || pos == line.length() - 1) {
                    throw new IOException("invalid VOBsub line: '" + line + "'");
                }

                String key = line.substring(0, pos).trim().toLowerCase();
                String value = line.substring(pos + 1).trim();

                if ("timestamp".equals(key)) {
                    Matcher m = TS_PATTERN.matcher(value);
                    if (!m.matches()) {
                        throw new IOException("Illegal timestamp: " + value);
                    }

                    Timestamp ts = new Timestamp(m.group(1));
                    String fp = m.group(2);

                    if (lastTs == null || ts.compareTo(lastTs) > 0) {
                        timestamp(ts, fp);
                        lastTs = ts;
                    }

                } else {
                    Setting setting = Setting.parse(key);
                    if (setting == null) {
                        throw new IOException("Illegal setting: " + key);
                    }
                    set(setting, value);
                }
            }
        }
    }

    /**
     * Writes a VOBsub index files with the current settings and timestamps.
     *
     * @param idxFile
     *            File to write
     */
    public void write(File idxFile) throws IOException {
        try (PrintWriter out = new PrintWriter(idxFile)) {
            out.println(TYPE_LINE);
            out.println("#");
            out.println("# Generated by Feinrip");
            out.println();

            settings.forEach((key, value) -> {
                if (key == Setting.ID) {
                    out.println();
                }
                out.append(key.toString()).append(": ").append(value).println();
            });

            timestamps.forEach((ts, location) -> {
                out.append("timestamp: ").append(ts.toString()).append(", filepos: ").append(location).println();
            });
        }
    }

    /**
     * A VOBsub settings key.
     *
     * @author Richard "Shred" Körber
     */
    public enum Setting {
        DELAY("delay"), SIZE("size"), ORG("org"), SCALE("scale"), ALPHA("alpha"),
        SMOOTH("smooth"), FADEINOUT("fadein/out"), ALIGN("align"),
        TIME_OFFSET("time offset"), FORCED_SUBS("forced subs"), PALETTE("palette"),
        CUSTOM_COLORS("custom colors"), LANGIDX("langidx"), ID("id");

        /**
         * Parses a key and returns a matching {@link Setting}.
         *
         * @param key
         *            Key string to parse
         * @return {@link Setting} matching the key, or {@code null} for an unknown key
         */
        public static Setting parse(String key) {
            for (Setting s : values()) {
                if (s.toString().equalsIgnoreCase(key)) {
                    return s;
                }
            }
            return null;
        }

        private final String key;

        private Setting(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Represents a timestamp. It is comparable and results a chronological order.
     *
     * @author Richard "Shred" Körber
     */
    public static class Timestamp implements Comparable<Timestamp> {
        private final static Pattern TS_PATTERN = Pattern.compile("\\d\\d:\\d\\d:\\d\\d:\\d\\d\\d");

        private final String ts;

        public Timestamp(String ts) {
            if (!TS_PATTERN.matcher(ts).matches()) {
                throw new IllegalArgumentException("Illegal timestamp: " + ts);
            }
            this.ts = ts;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Timestamp)) {
                return false;
            }
            Timestamp cmp = (Timestamp) obj;
            return ts.equals(cmp.ts);
        }

        @Override
        public int hashCode() {
            return ts.hashCode();
        }

        @Override
        public String toString() {
            return ts;
        }

        @Override
        public int compareTo(Timestamp o) {
            return ts.compareTo(o.ts);
        }
    }

    public static void main(String... args) throws IOException {
        for (String fn : args) {
            File file = new File(fn);
            VobsubIndex vsi = new VobsubIndex();
            vsi.read(file);
            vsi.write(file);
        }
    }

}
