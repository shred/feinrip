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

import org.shredzone.feinrip.lsdvd.DvdSubtitleAttributes.SubType;

/**
 * Information about a title's individual subtitle stream.
 *
 * @author Richard "Shred" Körber
 */
public class DvdSubtitle {

    private final DvdSubtitleAttributes attrs;
    private Integer stream43Id;
    private Integer streamWideId;
    private Integer streamLetterboxId;
    private Integer streamPanScanId;

    /**
     * Create a new {@link DvdSubtitle}.
     *
     * @param attrs
     *            Generic {@link DvdSubtitleAttributes} of this subtitle stream.
     */
    public DvdSubtitle(DvdSubtitleAttributes attrs) {
        this.attrs = attrs;
    }

    /**
     * Subtitle type, or {@code null} if unspecified.
     */
    public SubType getType()                    { return attrs.getType(); }

    /**
     * Language code, or {@code null} if unspecified.
     */
    public String getLanguage()                 { return attrs.getLanguage(); }

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
     * Stream ID for Pan &amp; Scan subtitles, or {@code null} if not enabled.
     */
    public Integer getStreamPanScanId()         { return streamPanScanId; }
    public void setStreamPanScanId(Integer streamPanScanId) { this.streamPanScanId = streamPanScanId; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[sub ");
        sb.append(attrs.toString());
        sb.append(", stream43Id=").append(stream43Id);
        sb.append(", streamWideId=").append(streamWideId);
        sb.append(", streamLetterboxId=").append(streamLetterboxId);
        sb.append(", streamPanScanId=").append(streamPanScanId);
        sb.append("]");
        return sb.toString();
    }

}
