/*
 * feinrip
 *
 * Copyright (C) 2016 Richard "Shred" Körber
 *   https://codeberg.org/shred/feinrip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.feinrip.lsdvd;

/**
 * Generic information about all subtitle streams in a title set.
 *
 * @author Richard "Shred" Körber
 */
public class DvdSubtitleAttributes {

    public enum SubType { NORMAL, LARGE, CHILDREN, NORMAL_CAPTIONS, LARGE_CAPTIONS,
        CHILDREN_CAPTIONS, FORCED, DIRECTOR_COMMENTS, LARGE_DIRECTOR_COMMENTS,
        CHILDREN_DIRECTOR_COMMENTS }

    private SubType type;
    private String language;

    /**
     * Subtitle type, or {@code null} if unspecified.
     */
    public SubType getType()                    { return type; }
    public void setType(SubType type)           { this.type = type; }

    /**
     * Language code, or {@code null} if unspecified.
     */
    public String getLanguage()                 { return language; }
    public void setLanguage(String language)    { this.language = language; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[subattr ");
        sb.append("type=").append(type);
        sb.append(", language=").append(language);
        sb.append("]");
        return sb.toString();
    }

}
