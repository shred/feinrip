/*
 * feinrip
 *
 * Copyright (C) 2014 Richard "Shred" Körber
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
package org.shredzone.feinrip.model;


/**
 * Represents a language.
 *
 * @author Richard "Shred" Körber
 */
public class Language implements Comparable<Language> {

    private final String name;
    private final String iso1;  // ISO639-1-Code, two letters
    private final String iso2;  // ISO639-2-Code, three letters

    /**
     * Creates a new {@link Language}.
     *
     * @param name
     *            Human readable name, English
     * @param iso1
     *            ISO639-1-Code (two letters), may be {@code null}
     * @param iso2
     *            ISO639-2-Code (three letters), may be {@code null}
     */
    public Language(String name, String iso1, String iso2) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be empty or null");
        }
        if ((iso1 == null || iso1.trim().isEmpty()) && (iso2 == null || iso2.trim().isEmpty())) {
            throw new IllegalArgumentException("both iso1 and iso2 must not be empty or null");
        }

        this.name = name.trim();
        this.iso1 = (iso1 != null && !iso1.trim().isEmpty() ? iso1.trim() : null);
        this.iso2 = (iso2 != null && !iso2.trim().isEmpty() ? iso2.trim() : null);
    }

    public String getName()                     { return name; }
    public String getIso1()                     { return iso1; }
    public String getIso2()                     { return iso2; }

    /**
     * Returns a short representation of the language.
     */
    public String toShortString() {
        return (iso2 != null ? iso2 : iso1);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Two {@link Language} are considered equal if they have the same name.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Language)) {
            return false;
        }
        return ((Language) obj).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(Language o) {
        return name.compareTo(o.name);
    }

}
