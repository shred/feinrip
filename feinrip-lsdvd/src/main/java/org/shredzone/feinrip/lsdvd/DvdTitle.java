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
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Information about a single DVD title.
 *
 * @author Richard "Shred" Körber
 */
public class DvdTitle {

    private final int[] colors = new int[16];
    private final List<Long> chapterTimeMs = new ArrayList<>();
    private final List<DvdAudio> audios = new ArrayList<>();
    private final List<DvdSubtitle> subs = new ArrayList<>();
    private int title;
    private int chapters;
    private int angles;
    private long totalTimeMs;
    private DvdTitleSet titleSet;
    private int vtsn;
    private int vts;

    /**
     * Subtitle CLUT. Contains YCrCb colors.
     */
    public int[] getColors()                    { return colors; }

    /**
     * Length of each chapter, in milliseconds.
     */
    public List<Long> getChapterTimeMs()        { return chapterTimeMs; }

    /**
     * {@link DvdAudio} streams. First VTS is at index 0.
     */
    public List<DvdAudio> getAudios()           { return audios; }

    /**
     * {@link DvdSubtitle} streams. First VTS is at index 0.
     */
    public List<DvdSubtitle> getSubs()          { return subs; }

    /**
     * Title number, counted from 1.
     */
    public int getTitle()                       { return title; }
    public void setTitle(int title)             { this.title = title; }

    /**
     * Total length of title, in milliseconds.
     */
    public long getTotalTimeMs()                { return totalTimeMs; }
    public void setTotalTimeMs(long totalTimeMs) { this.totalTimeMs = totalTimeMs; }

    /**
     * Number of chapters.
     */
    public int getChapters()                    { return chapters; }
    public void setChapters(int chapters)       { this.chapters = chapters; }

    /**
     * Number of viewing angels.
     */
    public int getAngles()                      { return angles; }
    public void setAngles(int angles)           { this.angles = angles; }

    /**
     * Number of VTS file. This is the number in the <code>VIDEO_TS/VTS_*_0.IFO</code>
     * file name.
     */
    public int getVtsn()                        { return vtsn; }
    public void setVtsn(int vtsn)               { this.vtsn = vtsn; }

    /**
     * Number of VTS within the VTS file.
     */
    public int getVts()                         { return vts; }
    public void setVts(int vts)                 { this.vts = vts; }

    /**
     * {@link DvdTitleSet} containing title set data.
     */
    public DvdTitleSet getTitleSet()                 { return titleSet; }
    public void setTitleSet(DvdTitleSet titleSet)    { this.titleSet = titleSet; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[title ").append(title);
        sb.append(", chapters=").append(chapters);
        sb.append(", audios=");
        sb.append(audios.stream()
                        .map(DvdAudio::toString)
                        .collect(Collectors.joining(" ", "[", "]")));
        sb.append(", subs=");
        sb.append(subs.stream()
                        .map(DvdSubtitle::toString)
                        .collect(Collectors.joining(" ", "[", "]")));
        sb.append(", angles=").append(angles);
        sb.append(", vtsn=").append(vtsn);
        sb.append(", vts=").append(vts);
        sb.append(", total=").append(totalTimeMs).append(" ms");
        sb.append(", chapter=");
        sb.append(chapterTimeMs.stream()
                        .map(String::valueOf)
                        .map(str -> str + " ms")
                        .collect(Collectors.joining(",")));
        sb.append(", colors=");
        StringJoiner sj = new StringJoiner(",");
        for (int color : colors) {
            sj.add(String.format("%06X", color));
        }
        sb.append(sj);
        sb.append(", titleSet=").append(titleSet);
        sb.append("]");
        return sb.toString();
    }

}
