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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.shredzone.feinrip.dvb.DvbInputStream;
import org.shredzone.feinrip.dvb.si.EventInformation;
import org.shredzone.feinrip.dvb.si.descriptor.Descriptor;
import org.shredzone.feinrip.dvb.si.descriptor.ShortEventDescriptor;
import org.shredzone.feinrip.model.Subtitle;

/**
 * Analyzes eit files and srt files of recorded TV.
 * <p>
 * This class heavily depends on some private scripts I wrote for recording TV shows
 * by my Dreambox and burning them on DVD. Basically the <code>movie.eit</code> file is
 * the eit file written by the Dreambox along with the recorded TV show. It contains the
 * movie title, a plot summary, and the recording date and time. The <code>.srt</code>
 * files are subtitle texts extracted from the TV teletext stream.
 *
 * @author Richard "Shred" Körber
 */
public class EitAnalyzer {

    private static final Pattern YEAR_PATTERN = Pattern.compile(".*?(\\d{4}).*");

    /**
     * Gets the title stored in an eit file.
     *
     * @param file
     *            eit file
     * @return title that was found, or {@code null} if there was no such file or if it
     *         contained no title
     */
    public static String getTitle(File file) throws IOException {
        if (!(file.exists() && file.isFile())) {
            return null;
        }

        try (DvbInputStream dvbin = new DvbInputStream(new FileInputStream(file))) {
            EventInformation ei = new EventInformation();
            ei.read(dvbin);

            for (Descriptor d : ei.getDescriptors()) {
                if (d instanceof ShortEventDescriptor) {
                    StringBuilder title = new StringBuilder(((ShortEventDescriptor) d).getEventName());
                    String more = ((ShortEventDescriptor) d).getText();

                    Matcher m = YEAR_PATTERN.matcher(more);
                    if (m.matches()) {
                        title.append(" (").append(m.group(1)).append(")");
                    }

                    return title.toString();
                }
            }
        }

        return null;
    }

    /**
     * Tries to find a srt file for the given subtitle. If it's there, it should be
     * preferred over the subtitle that is contained in the stream, because this subtitle
     * contains the raw text, which can be rendered very nicely on high resolution
     * devices.
     *
     * @param mountPoint
     *            DVD's mount point
     * @param sub
     *            {@link Subtitle} to be checked
     * @return srt file found, or {@code null} if there is no such file
     */
    public static File findSrtFile(File mountPoint, Subtitle sub) {
        String fn = String.format("movie-%s-%02d.srt", sub.getLanguage().getIso1(), (sub.getIndex() - 1));
        File file = new File(mountPoint, fn);
        if (file.exists() && file.isFile() && file.length() > 0) {
            return file;
        } else {
            return null;
        }
    }

}
