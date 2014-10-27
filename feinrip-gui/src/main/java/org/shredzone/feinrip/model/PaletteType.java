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

/**
 * An enumeration of different default palettes. A palette consists of 16 YUV colors.
 * <p>
 * {@link #DEFAULT} means that the subtitle tool's default palette is to be used.
 * <p>
 * {@link #CUSTOM} means that a freely configurable palette is to be used.
 * <p>
 * {@link #YUV_TEST_1} and {@link #YUV_TEST_2} are two palettes that can be used to find
 * out the palette indices actually used in a subtitle.
 *
 * @author Richard "Shred" Körber
 */
public enum PaletteType {

    // No palette, do not change, use a reasonable default
    DEFAULT("default"),

    // Custom palette
    CUSTOM("Custom"),

    // YUV palettes with two colors checked
    YUV_WHITE_BLACK_CHECK("W&B check"
                    , 0xFF8080, 0x108080, 0xFF8080, 0x108080
                    , 0x108080, 0xFF8080, 0x108080, 0xFF8080
                    , 0xFF8080, 0x108080, 0xFF8080, 0x108080
                    , 0x108080, 0xFF8080, 0x108080, 0xFF8080
                    ),
    YUV_BLACK_WHITE_CHECK("B&W check"
                    , 0x108080, 0xFF8080, 0x108080, 0xFF8080
                    , 0xFF8080, 0x108080, 0xFF8080, 0x108080
                    , 0x108080, 0xFF8080, 0x108080, 0xFF8080
                    , 0xFF8080, 0x108080, 0xFF8080, 0x108080
                    ),

    // YUV palettes with two colors lined
    YUV_WHITE_BLACK_LINE("W&B lined"
                    , 0xFF8080, 0xFF8080, 0xFF8080, 0xFF8080
                    , 0x108080, 0x108080, 0x108080, 0x108080
                    , 0xFF8080, 0xFF8080, 0xFF8080, 0xFF8080
                    , 0x108080, 0x108080, 0x108080, 0x108080
                    ),
    YUV_BLACK_WHITE_LINE("B&W lined"
                    , 0x108080, 0x108080, 0x108080, 0x108080
                    , 0xFF8080, 0xFF8080, 0xFF8080, 0xFF8080
                    , 0x108080, 0x108080, 0x108080, 0x108080
                    , 0xFF8080, 0xFF8080, 0xFF8080, 0xFF8080
                    ),

    // YUV palettes with two colors columned
    YUV_WHITE_BLACK_COLUMN("W&B columned"
                    , 0xFF8080, 0x108080, 0xFF8080, 0x108080
                    , 0xFF8080, 0x108080, 0xFF8080, 0x108080
                    , 0xFF8080, 0x108080, 0xFF8080, 0x108080
                    , 0xFF8080, 0x108080, 0xFF8080, 0x108080
                    ),
    YUV_BLACK_WHITE_COLUMN("B&W columned"
                    , 0x108080, 0xFF8080, 0x108080, 0xFF8080
                    , 0x108080, 0xFF8080, 0x108080, 0xFF8080
                    , 0x108080, 0xFF8080, 0x108080, 0xFF8080
                    , 0x108080, 0xFF8080, 0x108080, 0xFF8080
                    ),

    // Greyscale palettes, descending and ascending
    YUV_GREYSCALE("Greyscale"
                    , 0xFF8080, 0xEB8080, 0x808080, 0x108080
                    , 0xFF8080, 0xEB8080, 0x808080, 0x108080
                    , 0xFF8080, 0xEB8080, 0x808080, 0x108080
                    , 0xFF8080, 0xEB8080, 0x808080, 0x108080
                    ),

    YUV_SCALEGREY("Scalegrey"
                    , 0x108080, 0x808080, 0xEB8080, 0xFF8080
                    , 0x108080, 0x808080, 0xEB8080, 0xFF8080
                    , 0x108080, 0x808080, 0xEB8080, 0xFF8080
                    , 0x108080, 0x808080, 0xEB8080, 0xFF8080
                    ),

    // YUV test palettes to find out the color arrangement
    YUV_TEST_1("YUV: Test 1"
                    , 0x408080, 0x808080, 0xC08080, 0xF08080 // shades of grey
                    , 0x40F080, 0x80F080, 0xC0F080, 0xF0F080 // shades of blue
                    , 0x4080F0, 0x8080F0, 0xC080F0, 0xF080F0 // shades of red
                    , 0x400080, 0x800080, 0xC00080, 0xF00080 // shades of green
                    ),

    YUV_TEST_2("YUV: Test 2"
                    , 0x408080, 0x40F080, 0x4080F0, 0x400080 // dark grey, blue, red, green
                    , 0x808080, 0x80F080, 0x8080F0, 0x800080 // med grey, blue, red, green
                    , 0xC08080, 0xC0F080, 0xC080F0, 0xC00080 // bright grey, blue, red, green
                    , 0xF08080, 0xF0F080, 0xF080F0, 0xF00080 // full grey, blue, red, green
                    ),
    ;

    private final String name;
    private final Palette palette;

    private PaletteType(String name, int... values) {
        this.name = name;
        if (values != null && values.length > 0) {
            palette = new Palette(values);
        } else {
            palette = null;
        }
    }

    /**
     * Returns the palette colors.
     */
    public Palette getPalette() {
        return palette;
    }

    /**
     * Returns a user-readable color name.
     */
    @Override
    public String toString() {
        return name;
    }

}
