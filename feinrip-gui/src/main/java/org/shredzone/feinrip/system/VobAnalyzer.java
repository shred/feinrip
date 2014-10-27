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
package org.shredzone.feinrip.system;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.shredzone.feinrip.model.AspectRatio;
import org.shredzone.feinrip.model.Audio;
import org.shredzone.feinrip.model.Subtitle;
import org.shredzone.feinrip.util.Command;

/**
 * Analyzes a vob file.
 * <p>
 * Requires: <code>ffmpeg</code> package
 *
 * @author Richard "Shred" Körber
 */
public class VobAnalyzer {
    private static final File FFMPEG = new File("/usr/bin/ffmpeg");

    private static final String PROBESIZE = "400M";
    private static final Pattern VIDEO_PATTERN = Pattern.compile(".*?Stream.*?Video.*?(\\d+)x(\\d+).*?DAR.(\\d+.\\d+).*");
    private static final Pattern AUDIO_PATTERN = Pattern.compile(".*?Stream.*?\\[0x([0-9a-fA-F]{2,4})\\].*?Audio.*?(mp2|mp3|ac3|dca|dts).*?(mono|stereo|\\d.channels|\\d\\.\\d).*");
    private static final Pattern SUB_PATTERN = Pattern.compile(".*?Stream.*?\\[(0x..)\\].*?Subtitle.*");

    private AspectRatio aspect;
    private Dimension dimension;
    private List<Audio> audios = new ArrayList<>();
    private List<Subtitle> subs = new ArrayList<>();

    /**
     * Creates a new VobAnalyzer.
     * <p>
     * The vob file is analyzed immediately. This may take some seconds and temporarily
     * consume some hundreds megabytes of memory.
     * <p>
     * The results can be read from class properties.
     *
     * @param vobFile
     *            Vob file to analyze
     */
    public VobAnalyzer(File vobFile) throws IOException {
        Command ffmpegCmd = new Command(FFMPEG);
        ffmpegCmd.param("-analyzeduration", Integer.MAX_VALUE);
        ffmpegCmd.param("-probesize", PROBESIZE);
        ffmpegCmd.param("-i", vobFile);

        ffmpegCmd.redirectErrorToOutput();
        ffmpegCmd.redirectOutput(line -> {
            Matcher m1 = VIDEO_PATTERN.matcher(line);
            if (m1.matches()) {
                int width = Integer.parseInt(m1.group(1));
                int height = Integer.parseInt(m1.group(2));
                dimension = new Dimension(width, height);
                aspect = AspectRatio.parse(m1.group(3));
            }

            Matcher m2 = AUDIO_PATTERN.matcher(line);
            if (m2.matches()) {
                String id = m2.group(1);
                String format = m2.group(2);
                String channels = m2.group(3);

                int streamId = Integer.parseInt(id, 16);
                int chanNr = 0;

                if ("mono".equals(channels)) {
                    chanNr = 1;
                } else if ("stereo".equals(channels)) {
                    chanNr = 2;
                } else if (channels.endsWith("channels") || channels.endsWith(".0")) {
                    chanNr = Integer.parseInt(channels.substring(0, 1));
                } else if (channels.endsWith(".1")) {
                    chanNr = Integer.parseInt(channels.substring(0, 1)) + 1;
                }

                if ("dca".equals(format)) {
                    format = "dts";
                }

                Audio a = new Audio();
                a.setIx(0);
                a.setLanguage(LanguageUtils.getUndefined());
                a.setFormat(format);
                a.setChannels(chanNr);
                a.setStreamId(streamId);
                a.setEnabled(true);
                audios.add(a);
            }

            Matcher m3 = SUB_PATTERN.matcher(line);
            if (m3.matches()) {
                Subtitle s = new Subtitle();
                s.setIndex(0);
                s.setStreamId(m3.group(1));
                s.setLanguage(LanguageUtils.getUndefined());
                s.setEnabled(true);
                subs.add(s);
            }
        });

        try {
            ffmpegCmd.execute();
        } catch (IOException ex) {
            // ffmpeg complains about missing output file, ignore it...
        }

        audios.sort((a, b) -> Integer.compare(a.getStreamId(), b.getStreamId()));
        subs.sort((a, b) -> a.getStreamId().compareTo(b.getStreamId()));

        int ix = 1;
        for (Audio a : audios) {
            a.setIx(ix++);
        }

        ix = 1;
        for (Subtitle s : subs) {
            s.setIndex(ix++);
        }
    }

    public AspectRatio getAspect() {
        return aspect;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public List<Audio> getAudios() {
        return audios;
    }

    public List<Subtitle> getSubs() {
        return subs;
    }

}
