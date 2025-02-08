/*
 * feinrip
 *
 * Copyright (C) 2014 Richard "Shred" Körber
 *   https://codeberg.org/shred/feinrip
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
package org.shredzone.feinrip.progress;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link LogConsumer} that is able to handle <code>ffmpeg</code> output.
 *
 * @author Richard "Shred" Körber
 */
public class FFmpegConsumer extends LogConsumer {

    private static final Pattern PERCENT = Pattern.compile(".*?size=.*?time=(\\d+)\\.(\\d\\d).*");
    private static final Pattern PERCENT2 = Pattern.compile(".*?size=.*?time=(\\d\\d):(\\d\\d):(\\d\\d)\\.(\\d\\d).*");

    private long duration = 0;

    /**
     * Creates a new {@link FFmpegConsumer}.
     *
     * @param meter
     *            {@link ProgressMeter} to log into
     * @param error
     *            {@code true}: this {@link Consumer} logs errors, {@code false}:
     *            information and warnings
     */
    public FFmpegConsumer(ProgressMeter meter, boolean error) {
        this(meter, error, null);
    }

    /**
     * Creates a new {@link FFmpegConsumer}.
     *
     * @param meter
     *            {@link ProgressMeter} to log into
     * @param error
     *            {@code true}: this {@link Consumer} logs errors, {@code false}:
     *            information and warnings
     * @param duration
     *            Estimated total duration, or {@code null} if unknown
     */
    public FFmpegConsumer(ProgressMeter meter, boolean error, Long duration) {
        super(meter, error);
        if (duration != null) {
            this.duration = duration;
        }
    }

    @Override
    public void accept(String line) {
        if (duration > 0) {
            Matcher m = PERCENT.matcher(line);
            if (m.matches()) {
                long sec = Long.parseLong(m.group(1));
                long frc = Long.parseLong(m.group(2));

                long current = (sec * 100) + frc;

                float percent = current * 100.0f / duration;
                if (percent > 100) percent = 100;
                meter.percent(percent);
                return; // Swallow the progress line
            }

            m = PERCENT2.matcher(line);
            if (m.matches()) {
                long hr = Long.parseLong(m.group(1));
                long min = Long.parseLong(m.group(2));
                long sec = Long.parseLong(m.group(3));
                long frc = Long.parseLong(m.group(4));

                long current = (hr * 60 * 60 * 100) + (min * 60 * 100) + (sec * 100) + frc;

                float percent = current * 100.0f / duration;
                if (percent > 100) percent = 100;
                meter.percent(percent);
                return; // Swallow the progress line
            }
        }

        super.accept(line);
    }

}
