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

import java.awt.Dimension;

/**
 * A single track of a DVD style source.
 *
 * @author Richard "Shred" Körber
 */
public class Track {

    private int track;
    private String length;
    private int chapters;
    private AspectRatio aspect;
    private int angles;
    private Dimension dimension;

    /**
     * Track number, counted from 1.
     */
    public int getTrack()                       { return track; }
    public void setTrack(int track)             { this.track = track; }

    /**
     * Length of the track. Format: "mm:ss"
     */
    public String getLength()                   { return length; }
    public void setLength(String length)        { this.length = length; }

    /**
     * Number of chapters
     */
    public int getChapters()                    { return chapters; }
    public void setChapters(int chapters)       { this.chapters = chapters; }

    /**
     * Aspect ratio of the track's video stream.
     */
    public AspectRatio getAspect()              { return aspect; }
    public void setAspect(AspectRatio aspect)   { this.aspect = aspect; }

    /**
     * Number of angles of the track's video stream.
     */
    public int getAngles()                      { return angles; }
    public void setAngles(int angles)           { this.angles = angles; }

    /**
     * Image dimension of the track's video stream.
     */
    public Dimension getDimension()             { return dimension; }
    public void setDimension(Dimension dimension) { this.dimension = dimension; }

}
