/*
 * feinrip
 *
 * Copyright (C) 2014 Richard "Shred" Körber
 *   https://github.com/shred/feinrip
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enumeration for all available aspect ratios.
 *
 * @author Richard "Shred" Körber
 */
public enum AspectRatio {
    ASPECT_4_3(4, 3), ASPECT_16_9(16, 9);

    private static final Pattern RATIO = Pattern.compile("(\\d+).(\\d+)");

    /**
     * Parses a String and returns a matching {@link AspectRatio}.
     *
     * @param str
     *            String containing an aspect ratio. This are two numbers, separated by a
     *            single non-numerical character.
     * @return {@link AspectRatio} that was parsed
     * @throws IllegalArgumentException
     *             if the String did not contain a valid aspect ratio
     */
    public static AspectRatio parse(String str) {
        Matcher m = RATIO.matcher(str);
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot parse aspect ratio from '" + str + "'");
        }

        int w = Integer.parseInt(m.group(1));
        int h = Integer.parseInt(m.group(2));

        if ((3 * w) == (4 * h)) { // w/h == 4/3
            return ASPECT_4_3;
        } else if ((9 * w) == (16 * h)) { // w/h = 16/9
            return ASPECT_16_9;
        } else {
            throw new IllegalArgumentException("Unknown aspect ratio " + w + ":" + h);
        }
    }

    private final int w;
    private final int h;

    private AspectRatio(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public int getW()                           { return w; }
    public int getH()                           { return h; }

    /**
     * Returns a string representation of the aspect ratio, with the given separator
     * character.
     *
     * @param ch
     *            Separator character
     * @return Aspect ratio
     */
    public String toString(char ch) {
        StringBuilder sb = new StringBuilder();
        sb.append(w).append(ch).append(h);
        return sb.toString();
    }

    /**
     * Returns the aspect ratio with a colon as separator character.
     */
    @Override
    public String toString() {
        return toString(':');
    }

}
