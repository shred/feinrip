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
 * Subtitle information.
 *
 * @author Richard "Shred" Körber
 */
public class DvdSubtitle {

    public enum SubType { NORMAL, LARGE, CHILDREN, NORMAL_CAPTIONS, LARGE_CAPTIONS,
        CHILDREN_CAPTIONS, FORCED, DIRECTOR_COMMENTS, LARGE_DIRECTOR_COMMENTS,
        CHILDREN_DIRECTOR_COMMENTS }

    private SubType type;
    private String language;
    private Integer stream43Id;
    private Integer streamWideId;
    private Integer streamLetterboxId;
    private Integer streamPanScanId;


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

    /**
     * Stream ID for 4:3 subtitles, or {@code null} if not enabled.
     */
    public Integer getStream43Id()              { return stream43Id; }
    public void setStream43Id(Integer stream43Id) { this.stream43Id = stream43Id; }

    /**
     * Stream ID for Wide Screen subtitles, or {@code null} if not enabled.
     */
    public Integer getStreamWideId()            { return streamWideId; }
    public void setStreamWideId(Integer streamWideId) { this.streamWideId = streamWideId; }

    /**
     * Stream ID for Letterbox subtitles, or {@code null} if not enabled.
     */
    public Integer getStreamLetterboxId()       { return streamLetterboxId; }
    public void setStreamLetterboxId(Integer streamLetterboxId) { this.streamLetterboxId = streamLetterboxId; }

    /**
     * Stream ID for Pan & Scan subtitles, or {@code null} if not enabled.
     */
    public Integer getStreamPanScanId()         { return streamPanScanId; }
    public void setStreamPanScanId(Integer streamPanScanId) { this.streamPanScanId = streamPanScanId; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[sub ");
        sb.append("type=").append(type);
        sb.append(", language=").append(language);
        sb.append(", stream43Id=").append(stream43Id);
        sb.append(", streamWideId=").append(streamWideId);
        sb.append(", streamLetterboxId=").append(streamLetterboxId);
        sb.append(", streamPanScanId=").append(streamPanScanId);
        sb.append("]");
        return sb.toString();
    }

}
