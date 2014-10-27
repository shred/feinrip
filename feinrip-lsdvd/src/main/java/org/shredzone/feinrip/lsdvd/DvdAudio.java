/*
 * feinrip
 *
 * Copyright (C) 2014 Richard "Shred" Körber
 *   https://github.com/shred/feinrip
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
 * Information about audio streams.
 *
 * @author Richard "Shred" Körber
 */
public class DvdAudio {

    public enum Mode {
        MPEG1(0xC0), MPEG2(0xC0), AC3(0x80), LPCM(0xA0), DTS(0x88), SDDS(0x80);

        private final int baseStreamId;

        private Mode(int baseStreamId) {
            this.baseStreamId = baseStreamId;
        }

        public int getBaseStreamId() {
            return baseStreamId;
        }
    }

    public enum Type { NORMAL, VISUALLY_IMPAIRED, DIRECTORS_COMMENT, ALTERNATE };

    private Mode mode;
    private int channels;
    private Type type;
    private String lang;
    private Integer streamId;

    /**
     * Audio stream mode.
     */
    public Mode getMode()                       { return mode; }
    public void setMode(Mode mode)              { this.mode = mode; }

    /**
     * Number of channels.
     */
    public int getChannels()                    { return channels; }
    public void setChannels(int channels)       { this.channels = channels; }

    /**
     * Audio stream type, or {@code null} if unspecified.
     */
    public Type getType()                       { return type; }
    public void setType(Type type)              { this.type = type; }

    /**
     * Audio stream language, or {@code null} if unspecified.
     */
    public String getLang()                     { return lang; }
    public void setLang(String lang)            { this.lang = lang; }

    /**
     * Stream ID for this audio stream, or {@code null} if this stream is disabled.
     */
    public Integer getStreamId()                { return streamId; }
    public void setStreamId(Integer streamId)   { this.streamId = streamId; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[audio ");
        sb.append("mode=").append(mode);
        sb.append(", channels=").append(channels);
        sb.append(", type=").append(type);
        sb.append(", language=").append(lang);
        sb.append(", streamId=").append(streamId);
        sb.append("]");
        return sb.toString();
    }

}
