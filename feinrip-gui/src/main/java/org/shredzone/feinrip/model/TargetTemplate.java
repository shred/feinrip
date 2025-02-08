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

import java.util.ResourceBundle;

/**
 * Often used templates for target file names.
 *
 * @author Richard "Shred" Körber
 */
public enum TargetTemplate {
    MOVIE("%t.mkv"),
    SERIES("%s-%ee - %et.mkv"),
    NO_SEASON("%ee - %et.mkv"),
    TRACK("%n - %t.mkv");

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private final String pattern;

    private TargetTemplate(String pattern) {
        this.pattern = pattern;
    }

    /**
     * File name pattern.
     */
    public String getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return B.getString("template." + name().toLowerCase());
    }

}
