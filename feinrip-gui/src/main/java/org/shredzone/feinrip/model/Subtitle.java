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
 * A subtitle.
 *
 * @author Richard "Shred" Körber
 */
public class Subtitle {

    private int index;
    private String streamId;
    private SubtitleFormat format;
    private SubtitleType type;
    private Language language;
    private int vts;
    private boolean enabled;

    /**
     * Subtitle index, counted from 1.
     */
    public int getIndex()                       { return index; }
    public void setIndex(int index)             { this.index = index; }

    /**
     * Subtitle stream id, as hex number.
     */
    public String getStreamId()                 { return streamId; }
    public void setStreamId(String streamId)    { this.streamId = streamId; }

    /**
     * Subtitle format.
     */
    public SubtitleFormat getFormat()           { return format; }
    public void setFormat(SubtitleFormat format) { this.format = format; }

    /**
     * Subtitle stream type.
     */
    public SubtitleType getType()               { return type; }
    public void setType(SubtitleType type)      { this.type = type; }

    /**
     * Subtitle language.
     */
    public Language getLanguage()               { return language; }
    public void setLanguage(Language language)  { this.language = language; }

    /**
     * For DVD style sources: number of the corresponding VTS file.
     */
    public int getVts()                         { return vts; }
    public void setVts(int vts)                 { this.vts = vts; }

    /**
     * {@code true}: subtitle will be multiplexed to the target mkv.
     */
    public boolean isEnabled()                  { return enabled; }
    public void setEnabled(boolean enabled)     { this.enabled = enabled; }

}
