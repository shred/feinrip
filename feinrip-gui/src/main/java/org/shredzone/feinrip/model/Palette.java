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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A Palette contains a set of 16 colors used for subtitles.
 * <p>
 * Objects are immutable.
 *
 * @author Richard "Shred" Körber
 */
public class Palette implements Serializable {
    private static final long serialVersionUID = 276940182433052136L;

    private final int[] palette;

    /**
     * Parses a string of YUV color values in hex notation, separated by spaces, commas or
     * semicolons.
     *
     * @param str
     *            String to be parsed
     * @return {@link Palette} containing the palette
     * @throws IllegalArgumentException
     *             if the string could not be parsed
     */
    public static Palette parse(String str) {
        String[] parts = str.split("[ ,;]+");

        int[] result = new int[parts.length];
        Arrays.fill(result, 0x008080); // Pre-fill it with YUV black

        for(int ix = 0; ix < result.length; ix++) {
            try {
                result[ix] = Integer.parseInt(parts[ix].trim(), 16) & 0xFFFFFF;
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Cannot parse yuv color value: '" + parts[ix] + "'");
            }
        }

        return new Palette(result);
    }

    /**
     * Creates a new palette with the given set of YUV color values.
     * <p>
     * If less than 16 colors, the palette is filled with the given yuv colors again. If
     * more than 16 colors, the 17th and subsequent color values are being ignored.
     *
     * @param yuv
     *            Collection of YUV colors
     */
    public Palette(int... yuv) {
        if (yuv == null || yuv.length == 0) {
            throw new IllegalArgumentException("Palette needs at least 1 color");
        }

        palette = new int[16];
        for (int ix = 0; ix < 16; ix++) {
            palette[ix] = yuv[ix % yuv.length];
        }
    }

    /**
     * Returns the number of colors in this palette. This is usually always 16.
     *
     * @return number of colors
     */
    public int size() {
        return palette.length;
    }

    /**
     * Returns the YUV color value.
     *
     * @param ix
     *            color index, must be 0 &lt;= ix &lt; 16.
     * @return YUV color
     */
    public int getYuv(int ix) {
        return palette[ix];
    }

    /**
     * Returns the RGB representation of the YUV color.
     *
     * @param ix
     *            color index, must be 0 &lt;= ix &lt; 16.
     * @return RGB representation
     */
    public int getRgb(int ix) {
        int color = palette[ix];

        float y = (color >> 16) & 0xFF;
        float u = ((color >> 8) & 0xFF) - 128f;
        float v = (color & 0xFF) - 128f;

        float r = y + 1.4022f * u;
        float g = y - 0.3456f * u - 0.7145f * v;
        float b = y + 1.7710f * v;

        int ri = Math.max(Math.min((int) r, 255), 0);
        int gi = Math.max(Math.min((int) g, 255), 0);
        int bi = Math.max(Math.min((int) b, 255), 0);

        return (ri << 16) | (gi << 8) | bi;
    }

    /**
     * Returns the palette as list of YUV values.
     *
     * @return List of YUV values
     */
    public List<Integer> getYuvAsList() {
        return Collections.unmodifiableList(IntStream
                        .range(0, size())
                        .mapToObj(this::getYuv)
                        .collect(Collectors.toList()));
    }

    /**
     * Returns the palette as list of RGB values.
     *
     * @return List of RGB values
     */
    public List<Integer> getRgbAsList() {
        return Collections.unmodifiableList(IntStream
                        .range(0, size())
                        .mapToObj(this::getRgb)
                        .collect(Collectors.toList()));
    }

    /**
     * Returns the brightness of the YUV color.
     *
     * @param ix
     *            color index, must be 0 &lt;= ix &lt; 16.
     * @return brightness, between 0 (dark) and 255 (bright)
     */
    public int getBrightness(int ix) {
        return (palette[ix] >> 16) & 0xFF;
    }


    /**
     * Returns the palette as a comma separated string of RGB color values.
     */
    public String toRgbString() {
        return IntStream.range(0, size())
                        .mapToObj(ix -> String.format("%06x", getRgb(ix)))
                        .collect(Collectors.joining(", "));
    }

    /**
     * Returns the palette as a comma separated string of YUV color values. The result
     * can be used to create a new Palette via {@link Palette#parse(String)}.
     */
    @Override
    public String toString() {
        return IntStream.range(0, size())
                        .mapToObj(ix -> String.format("%06x", getYuv(ix)))
                        .collect(Collectors.joining(", "));
    }

}
