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
package org.shredzone.feinrip.util;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.shredzone.feinrip.lsdvd.DvdAudio;
import org.shredzone.feinrip.lsdvd.DvdSubtitle;
import org.shredzone.feinrip.lsdvd.DvdTitle;
import org.shredzone.feinrip.lsdvd.DvdTitleSet;
import org.shredzone.feinrip.lsdvd.IfoReader;
import org.shredzone.feinrip.model.AspectRatio;
import org.shredzone.feinrip.model.Audio;
import org.shredzone.feinrip.model.AudioType;
import org.shredzone.feinrip.model.Chapter;
import org.shredzone.feinrip.model.Language;
import org.shredzone.feinrip.model.Palette;
import org.shredzone.feinrip.model.Subtitle;
import org.shredzone.feinrip.model.SubtitleFormat;
import org.shredzone.feinrip.model.SubtitleType;
import org.shredzone.feinrip.model.Track;
import org.shredzone.feinrip.system.LanguageUtils;

/**
 * Analyzes a DVD structure.
 *
 * @author Richard "Shred" Körber
 */
public class DvdAnalyzer {

    private IfoReader info;
    private String title;

    /**
     * Creates a new {@link DvdAnalyzer} for the given DVD mount point.
     *
     * @param mount
     *            Moint point
     */
    public DvdAnalyzer(File mount) throws IOException {
        info = new IfoReader(mount);
        title = mount.getName();
    }

    /**
     * Returns the title of the DVD.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the number of the longest track.
     */
    public int getLongestTrack() {
        int maxTitle = 0;
        long maxLength = 0;

        for (int ix = 0; ix < info.getTitles().size(); ix++) {
            DvdTitle title = info.getTitle(ix);
            if (title.getTotalTimeMs() > maxLength) {
                maxTitle = ix;
                maxLength = title.getTotalTimeMs();
            }
        }

        return maxTitle + 1;
    }

    /**
     * Gets a description of all {@link Track} of the DVD.
     */
    public Track[] getTracks() {
        Track[] result = new Track[info.getTitles().size()];

        for (int ix = 0; ix < result.length; ix++) {
            DvdTitle title = info.getTitle(ix);
            DvdTitleSet vts = title.getTitleSet();

            Track track = new Track();
            track.setTrack(title.getTitle());
            track.setAngles(title.getAngles());
            track.setChapters(title.getChapters());
            track.setDimension(new Dimension(vts.getWidth(), vts.getHeight()));

            long lengthSec = title.getTotalTimeMs() / 1000L;
            track.setLength(String.format("%d:%02d", (int) (lengthSec / 60), (int) (lengthSec % 60)));
            track.setAspect(AspectRatio.valueOf(vts.getAspect().name()));

            result[ix] = track;
        }

        return result;
    }

    /**
     * Analyzes a DVD track and returns all chapters.
     *
     * @param trackNr
     *            Track number to be analyzed, starting from 1
     * @return List of {@link Chapter} entities
     */
    public List<Chapter> getChapters(int trackNr) {
        if (trackNr <= 0) {
            return Collections.emptyList();
        }

        DvdTitle title = info.getTitle(trackNr - 1);
        int chapters = title.getChapters();

        List<Chapter> result = new ArrayList<>();

        long ms = 0;
        int chapter = 1;
        for (long time : title.getChapterTimeMs()) {
            result.add(createChapter(chapter++, ms, chapters));
            ms += time;
        }
        result.add(createChapter(chapter, ms, chapters));

        return result;
    }

    /**
     * Creates a {@link Chapter} entry.
     *
     * @param chapter
     *            chapter number, starting from 1
     * @param ms
     *            position of that chapter
     * @param chapters
     *            number of chapters, used for detecting annexes
     * @return {@link Chapter} that was created
     */
    private Chapter createChapter(int chapter, long ms, int chapters) {
        Chapter chap = new Chapter();
        chap.setNumber(chapter);
        if (chapter > chapters) {
            chap.setTitle(String.format("Annex %02d", chapter - chapters));
        } else {
            chap.setTitle(String.format("Chapter %02d", chapter));
        }
        chap.setPosition(
                String.format("%2d:%02d:%02d.%03d",
                (ms / (60 * 60 * 1000L)),
                (ms / (60 * 1000L) % 60),
                ((ms / 1000L) % 60),
                ms % 1000L
        ));
        return chap;
    }

    /**
     * Gets all {@link Audio} streams of a track.
     *
     * @param trackNr
     *            Track number, starting from 1
     * @return List of {@link Audio} streams found
     */
    public List<Audio> getAudios(int trackNr) {
        if (trackNr <= 0) {
            return Collections.emptyList();
        }

        DvdTitle title = info.getTitle(trackNr - 1);

        List<Audio> result = new ArrayList<>();

        boolean anyAudio = false;

        for (int ix = 0; ix < title.getAudios().size(); ix++) {
            DvdAudio da = title.getAudios().get(ix);

            Audio audio = new Audio();
            audio.setIx(ix + 1);
            audio.setFormat(da.getMode().toString().toLowerCase());
            audio.setChannels(da.getChannels());
            audio.setStreamId(da.getStreamId());
            audio.setType(AudioType.NOT_SPECIFIED);

            if (da.getType() != null) {
                try {
                    audio.setType(AudioType.valueOf(da.getType().name()));
                } catch (IllegalArgumentException ex) {
                    // ignore
                }
            }

            Language language = LanguageUtils.findLanguage(da.getLang());
            if (language != null) {
                audio.setLanguage(language);
                audio.setEnabled(true);
                anyAudio = true;
            } else {
                audio.setLanguage(LanguageUtils.getMissing());
                audio.setEnabled(false);
            }

            result.add(audio);
        }

        // If there was no audio stream selected, just select the first one.
        if (!anyAudio && !result.isEmpty()) {
            result.get(0).setEnabled(true);
        }

        return result;
    }

    /**
     * Gets all {@link Subtitle} of a track.
     *
     * @param trackNr
     *            Track number, starting from 1
     * @return List of {@link Subtitle} found
     */
    public List<Subtitle> getSubtitles(int trackNr) {
        if (trackNr <= 0) {
            return Collections.emptyList();
        }

        DvdTitle title = info.getTitle(trackNr - 1);

        List<Subtitle> result = new ArrayList<>();
        Set<Integer> seen = new HashSet<>(); // make sure streams appear only once

        int index = 1;
        for (DvdSubtitle ds : title.getSubs()) {
            if (ds.getStream43Id() != null && !seen.contains(ds.getStream43Id())) {
                result.add(createSubtitle(index++, ds.getStream43Id(), title.getVtsn(), ds, SubtitleFormat.STANDARD));
                seen.add(ds.getStream43Id());
            }

            if (ds.getStreamWideId() != null && !seen.contains(ds.getStreamWideId())) {
                result.add(createSubtitle(index++, ds.getStreamWideId(), title.getVtsn(), ds, SubtitleFormat.WIDE));
                seen.add(ds.getStreamWideId());
            }

            if (ds.getStreamLetterboxId() != null && !seen.contains(ds.getStreamLetterboxId())) {
                result.add(createSubtitle(index++, ds.getStreamLetterboxId(), title.getVtsn(), ds, SubtitleFormat.LETTERBOX));
                seen.add(ds.getStreamLetterboxId());
            }

            if (ds.getStreamPanScanId() != null && !seen.contains(ds.getStreamPanScanId())) {
                result.add(createSubtitle(index++, ds.getStreamPanScanId(), title.getVtsn(), ds, SubtitleFormat.PANSCAN));
                seen.add(ds.getStreamPanScanId());
            }
        }

        return result;
    }

    /**
     * Creates a Subtitle with the given parameters.
     *
     * @param index
     *            Subtitle index
     * @param streamId
     *            Stream ID of this subtitle
     * @param vts
     *            Stream VTS
     * @param ds
     *            {@link DvdSubtitle} with further data
     * @param format
     *            {@link SubtitleFormat} of this subtitle
     * @return Created {@link Subtitle}
     */
    private Subtitle createSubtitle(int index, int streamId, int vts, DvdSubtitle ds, SubtitleFormat format) {
        Subtitle sub = new Subtitle();
        sub.setIndex(index);
        sub.setStreamId(String.format("0x%02X", streamId));
        sub.setFormat(format);
        sub.setVts(vts);

        Language language = LanguageUtils.findLanguage(ds.getLanguage());
        if (language != null) {
            sub.setLanguage(language);
            sub.setEnabled(format == SubtitleFormat.STANDARD || format == SubtitleFormat.WIDE);
        } else {
            sub.setLanguage(LanguageUtils.getMissing());
            sub.setEnabled(false);
        }

        sub.setType(SubtitleType.NOT_SPECIFIED);
        if (ds.getType() != null) {
            try {
                sub.setType(SubtitleType.valueOf(ds.getType().name()));
            } catch (IllegalArgumentException ex) {
                // ignore
            }
        }

        return sub;
    }

    /**
     * Gets the subtitle palette of a track.
     *
     * @param trackNr
     *            Track number, starting from 1
     * @return {@link Palette}
     */
    public Palette getPalette(int trackNr) {
        DvdTitle title = info.getTitle(trackNr - 1);
        return new Palette(title.getColors());
    }

}
