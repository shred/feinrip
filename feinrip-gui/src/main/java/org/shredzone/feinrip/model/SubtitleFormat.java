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

import java.util.ResourceBundle;

/**
 * An enumeration of different subtitle formats.
 *
 * @author Richard "Shred" Körber
 */
public enum SubtitleFormat {
    STANDARD,
    WIDE,
    LETTERBOX,
    PANSCAN;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    @Override
    public String toString() {
        return B.getString("subformat." + name().toLowerCase());
    };

}
