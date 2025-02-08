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

import org.shredzone.feinrip.lsdvd.DvdAudioAttributes.Mode;
import org.shredzone.feinrip.lsdvd.DvdAudioAttributes.Type;

/**
 * Information about a title's individual audio stream.
 *
 * @author Richard "Shred" Körber
 */
public class DvdAudio {

    private final DvdAudioAttributes attrs;
    private Integer streamId;

    /**
     * Create a new {@link DvdAudio}.
     *
     * @param attrs
     *            Generic {@link DvdAudioAttributes} of this audio stream.
     */
    public DvdAudio(DvdAudioAttributes attrs) {
        this.attrs = attrs;
    }

   /**
    * Audio stream mode.
    */
   public Mode getMode()                       { return attrs.getMode(); }

   /**
    * Number of channels.
    */
   public int getChannels()                    { return attrs.getChannels(); }

   /**
    * Audio stream type, or {@code null} if unspecified.
    */
   public Type getType()                       { return attrs.getType(); }

   /**
    * Audio stream language, or {@code null} if unspecified.
    */
   public String getLang()                     { return attrs.getLang(); }

    /**
     * Stream ID for this audio stream, or {@code null} if this stream is disabled.
     */
    public Integer getStreamId()               { return streamId; }
    public void setStreamId(Integer streamId)  { this.streamId = streamId; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[audio ");
        sb.append(attrs.toString());
        sb.append(", streamId=").append(streamId);
        sb.append("]");
        return sb.toString();
    }

}
