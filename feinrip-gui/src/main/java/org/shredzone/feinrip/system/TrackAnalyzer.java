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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.shredzone.feinrip.util.Command;

/**
 * Analyzes a dvd track.
 * <p>
 * Requires: <code>mplayer</code> package
 *
 * @author Richard "Shred" Körber
 */
public class TrackAnalyzer {
    private static final File MPLAYER = new File("/usr/bin/mplayer");

    private static final Pattern DIMENSION_PATTERN = Pattern.compile("ID_VIDEO_(WIDTH|HEIGHT)=(\\d+).*");
    private static final Pattern AUDIOID_PATTERN = Pattern.compile("ID_AUDIO_ID=(\\d+).*");

    private Dimension dimension;
    private Set<Integer> audioIds = new HashSet<>();

    /**
     * Creates a new TrackAnalyzer.
     * <p>
     * The track is analyzed immediately. This may take some seconds.
     * <p>
     * The results can be read from class properties.
     *
     * @param device
     *            DVD device to analyze
     * @param track
     *            Track number
     */
    public TrackAnalyzer(File device, int track) throws IOException {
        Command mplayerCmd = new Command(MPLAYER);
        mplayerCmd.param("-dvd-device", device);
        mplayerCmd.param("dvd://" + track);
        mplayerCmd.param("-identify");
        mplayerCmd.param("-frames", 0);
        mplayerCmd.param("-quiet");

        mplayerCmd.redirectErrorToOutput();
        mplayerCmd.redirectOutput(line -> {
            Matcher m1 = DIMENSION_PATTERN.matcher(line);
            if (m1.matches()) {
                if (dimension == null) {
                    dimension = new Dimension();
                }
                switch (m1.group(1)) {
                    case "WIDTH": dimension.width = Integer.parseInt(m1.group(2)); break;
                    case "HEIGHT": dimension.height =  Integer.parseInt(m1.group(2)); break;
                }
            }

            Matcher m2 = AUDIOID_PATTERN.matcher(line);
            if (m2.matches()) {
                int id = Integer.parseInt(m2.group(1));
                if (id < 128) {
                    id |= 0xC0;
                }
                audioIds.add(id);
            }
        });

        mplayerCmd.execute();
    }

    /**
     * Video dimensions of the track. {@code null} if unknown.
     */
    public Dimension getDimension() {
        return dimension;
    }

    /**
     * Audio IDs found in the track. {@code null} if unknown.
     */
    public Set<Integer> getAudioIds() {
        return audioIds;
    }

}
