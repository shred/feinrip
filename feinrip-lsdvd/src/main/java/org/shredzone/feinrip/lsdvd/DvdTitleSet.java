/*
 * feinrip
 *
 * Copyright (C) 2014 Richard "Shred" Körber
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link DvdTitleSet} contains information that is shared between one or more
 * {@link DvdTitle}. The information is found in a single VTS file.
 *
 * @author Richard "Shred" Körber
 */
public class DvdTitleSet {

    public enum Format { NTSC, PAL }
    public enum Aspect { ASPECT_4_3, ASPECT_16_9 }

    private final List<DvdAudioAttributes> audioAttrs = new ArrayList<>();
    private final List<DvdSubtitleAttributes> subAttrs = new ArrayList<>();
    private Format format;
    private Aspect aspect;
    private boolean panScanEnabled;
    private boolean letterboxEnabled;
    private int width;
    private int height;

    /**
     * {@link DvdAudioAttributes} streams. First VTS is at index 0.
     */
    public List<DvdAudioAttributes> getAudioAttributes() { return audioAttrs; }

    /**
     * {@link DvdSubtitleAttributes} streams. First VTS is at index 0.
     */
    public List<DvdSubtitleAttributes> getSubAttributes() { return subAttrs; }


    /**
     * Video format.
     */
    public Format getFormat()                   { return format; }
    public void setFormat(Format format)        { this.format = format; }

    /**
     * Aspect ratio.
     */
    public Aspect getAspect()                   { return aspect; }
    public void setAspect(Aspect aspect)        { this.aspect = aspect; }

    /**
     * Letterbox format enabled?
     */
    public boolean isLetterboxEnabled()         { return letterboxEnabled; }
    public void setLetterboxEnabled(boolean letterboxEnabled) { this.letterboxEnabled = letterboxEnabled; }

    /**
     * Pan &amp; Scan format enabled?
     */
    public boolean isPanScanEnabled()           { return panScanEnabled; }
    public void setPanScanEnabled(boolean panScanEnabled) { this.panScanEnabled = panScanEnabled; }

    /**
     * Resolution width.
     */
    public int getWidth()                       { return width; }
    public void setWidth(int width)             { this.width = width; }

    /**
     * Resolution height.
     */
    public int getHeight()                      { return height; }
    public void setHeight(int height)           { this.height = height; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[vts ");
        sb.append("format=").append(format);
        sb.append(", aspect=").append(aspect);
        sb.append(", letterbox=").append(letterboxEnabled);
        sb.append(", panscan=").append(panScanEnabled);
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append(", audioattrs=");
        sb.append(audioAttrs.stream()
                        .map(DvdAudioAttributes::toString)
                        .collect(Collectors.joining(" ", "[", "]")));
        sb.append(", subattrs=");
        sb.append(subAttrs.stream()
                        .map(DvdSubtitleAttributes::toString)
                        .collect(Collectors.joining(" ", "[", "]")));
        sb.append("]");
        return sb.toString();
    }

}
