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
 * Model for a single audio stream.
 *
 * @author Richard "Shred" Körber
 */
public class Audio {

    private int ix;
    private Language language;
    private String format;
    private AudioType type;
    private int channels;
    private int streamId;
    private boolean enabled;
    private boolean available = true;

    /**
     * Audio stream index. Counted starting by 1.
     */
    public int getIx()                          { return ix; }
    public void setIx(int ix)                   { this.ix = ix; }

    /**
     * Audio language.
     */
    public Language getLanguage()               { return language; }
    public void setLanguage(Language language)  { this.language = language; }

    /**
     * Stream format.
     */
    public String getFormat()                   { return format; }
    public void setFormat(String format)        { this.format = format; }

    /**
     * Audio stream type.
     */
    public AudioType getType()                  { return type; }
    public void setType(AudioType type)         { this.type = type; }

    /**
     * Number of channels.
     */
    public int getChannels()                    { return channels; }
    public void setChannels(int channels)       { this.channels = channels; }

    /**
     * Stream id.
     */
    public int getStreamId()                    { return streamId; }
    public void setStreamId(int streamId)       { this.streamId = streamId; }

    /**
     * {@code true} if this stream is to be encoded, {@code false} to ignore this stream
     * in the output file.
     */
    public boolean isEnabled()                  { return enabled; }
    public void setEnabled(boolean enabled)     { this.enabled = enabled; }

    /**
     * {@code true} if this stream is available in the stream. {@code false} if it is only
     * announced in the DVD ifo, but not actually available.
     */
    public boolean isAvailable()                { return available; }
    public void setAvailable(boolean available) { this.available = available; }

}
