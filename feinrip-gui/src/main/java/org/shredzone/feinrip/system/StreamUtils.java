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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.shredzone.feinrip.model.Audio;
import org.shredzone.feinrip.model.StreamType;
import org.shredzone.feinrip.model.Subtitle;
import org.shredzone.feinrip.progress.FFmpegConsumer;
import org.shredzone.feinrip.progress.FilteringConsumer;
import org.shredzone.feinrip.progress.LogConsumer;
import org.shredzone.feinrip.progress.PercentConsumer;
import org.shredzone.feinrip.progress.PredicateLogConsumer;
import org.shredzone.feinrip.progress.ProgressMeter;
import org.shredzone.feinrip.util.Command;

/**
 * Utility class for generic streaming operations.
 * <p>
 * Requires: <code>ffmpeg</code>, <code>mplayer</code>, <code>transcode</code>,
 * <code>mencoder</code> packages
 *
 * @author Richard "Shred" Körber
 */
public class StreamUtils {
    private static final File FFMPEG    = new File("/usr/bin/ffmpeg");
    private static final File MENCODER  = new File("/usr/bin/mencoder");
    private static final File MPLAYER   = new File("/usr/bin/mplayer");
    private static final File TCCAT     = new File("/usr/bin/tccat");

    private static final String PROBESIZE = "50M";

    /**
     * Reads a vob stream from the given device.
     *
     * @param device
     *            Device to read the stream frmo
     * @param track
     *            Track number of the stream to read
     * @param out
     *            File to write the stream to
     * @param type
     *            {@link StreamType} to be used for streaming
     * @param meter
     *            {@link ProgressMeter} to update while streaming
     * @return {@code true} if there was an I/O error reported while reading
     */
    public static boolean readStream(File device, int track, File out, StreamType type, ProgressMeter meter)
                    throws IOException {
        if (type == StreamType.TCCAT) {
            return readStreamTccat(device, track, out, meter);
        }

        // Ignore all lines starting with "dump:" and without percent character
        Predicate<String> noPercentPredicate = Pattern.compile("^dump\\:[^%]*$").asPredicate().negate();

        PredicateLogConsumer logConsumer = new PredicateLogConsumer(meter, true,
            line -> line.contains("stream read error!")
        );

        Command mplayerCmd = new Command(MPLAYER);
        mplayerCmd.param("-dvd-device", device);
        mplayerCmd.param(type == StreamType.DVDNAV ? "dvdnav://" + track : "dvd://" + track);
        mplayerCmd.param("-nocache");
        mplayerCmd.param("-dumpstream");
        mplayerCmd.param("-dumpfile", out);

        mplayerCmd.redirectOutput(new FilteringConsumer<String>(noPercentPredicate, new PercentConsumer(meter, false)));
        mplayerCmd.redirectError(logConsumer);

        mplayerCmd.execute();

        return logConsumer.hasMatched();
    }

    /**
     * Reads a vob stream from the given device, by using <code>tccat</code>.
     *
     * @param device
     *            Device to read the stream frmo
     * @param track
     *            Track number of the stream to read
     * @param out
     *            File to write the stream to
     * @param meter
     *            {@link ProgressMeter} to update while streaming
     * @return {@code true} if there was an I/O error reported while reading
     */
    private static boolean readStreamTccat(File device, int track, File out, ProgressMeter meter)
                    throws IOException {
        PredicateLogConsumer logConsumer = new PredicateLogConsumer(meter, true,
            Pattern.compile(".*?critical.*?Read.failed.for.\\d+.blocks.*").asPredicate()
        );

        Command tccatCmd = new Command(TCCAT);
        tccatCmd.param("-i", device);
        tccatCmd.param("-T", track + ",-1");

        tccatCmd.redirectOutput(out);
        tccatCmd.redirectError(logConsumer);

        tccatCmd.execute();

        return logConsumer.hasMatched();
    }

    /**
     * Extracts a subtitle from a vob file.
     * <p>
     * Source DVD must be present. Subtitle dimensions and palette are extracted from the
     * corresponding IFO file.
     *
     * @param sub
     *            {@link Subtitle} to be read
     * @param baseDir
     *            Directory where the DVD is mounted
     * @param vobFile
     *            Vob file that was read
     * @param vobsubFile
     *            Sub file to write to
     * @param meter
     *            {@link ProgressMeter} to update while streaming
     */
    public static void readSubtitle(Subtitle sub, File baseDir, File vobFile, File vobsubFile, ProgressMeter meter)
                    throws IOException {
        int streamNr = Integer.parseInt(sub.getStreamId().substring(2), 16) - 32;
        File ifoFile = new File(baseDir, String.format("VIDEO_TS/VTS_%02d_0.IFO", sub.getVts()));

        Command mencoderCmd = new Command(MENCODER);
        mencoderCmd.param(vobFile);
        mencoderCmd.param("-ifo", ifoFile);
        mencoderCmd.param("-vobsubout", vobsubFile);
        mencoderCmd.param("-sid", streamNr);
        mencoderCmd.param("-nosound");
        mencoderCmd.param("-ovc", "copy");
        mencoderCmd.param("-o", new File("/dev/null"));

        mencoderCmd.redirectOutput(new PercentConsumer(meter, false));
        mencoderCmd.redirectError(new LogConsumer(meter, true));

        mencoderCmd.execute();
    }

    /**
     * Extracts a subtitle from a vob file.
     * <p>
     * Source DVD does not need to be present. The given subtitle dimensions and palette
     * are used instead.
     *
     * @param sub
     *            {@link Subtitle} to be read
     * @param vobFile
     *            Vob file that was read
     * @param vobsubFile
     *            Sub file to write to
     * @param dim
     *            Dimension of the video, to be used for subtitles. Required.
     * @param meter
     *            {@link ProgressMeter} to update while streaming
     */
    public static void readSubtitleNoIfo(Subtitle sub, File vobFile, File vobsubFile, Dimension dim, ProgressMeter meter)
                    throws IOException {
        int streamNr = Integer.parseInt(sub.getStreamId().substring(2), 16) - 32;

        Command mencoderCmd = new Command(MENCODER);
        mencoderCmd.param(vobFile);
        mencoderCmd.param("-vobsubout", vobsubFile);
        mencoderCmd.param("-vobsuboutindex", 0);
        mencoderCmd.param("-vobsuboutid", (sub.getLanguage() != null ? sub.getLanguage().toShortString() : "und"));
        mencoderCmd.param("-sid", streamNr);
        mencoderCmd.param("-nosound");
        mencoderCmd.param("-ovc", "copy");
        mencoderCmd.param("-o", new File("/dev/null"));

        mencoderCmd.redirectOutput(new PercentConsumer(meter, false));
        mencoderCmd.redirectError(new LogConsumer(meter, true));

        mencoderCmd.execute();
    }

    /**
     * Extracts an audio stream from a vob file.
     *
     * @param vob
     *            vob file to read from
     * @param audio
     *            {@code Audio} to be extracted
     * @param out
     *            audio stream file to write to
     * @param meter
     *            {@link ProgressMeter} to update while streaming
     * @return Audio file that has been extracted
     */
    public static File extractAudio(File vob, Audio audio, File out, ProgressMeter meter)
                    throws IOException {
        Command ffAnalyzeCmd = new Command(FFMPEG);
        ffAnalyzeCmd.param("-analyzeduration", Integer.MAX_VALUE);
        ffAnalyzeCmd.param("-probesize", PROBESIZE);
        ffAnalyzeCmd.param("-i", vob);

        String streamId = String.format("%x", audio.getStreamId());

        AnalyzerCollector ac = new AnalyzerCollector(streamId);
        ffAnalyzeCmd.redirectOutput(ac);
        ffAnalyzeCmd.redirectErrorToOutput();

        try {
            ffAnalyzeCmd.execute();
        } catch (IOException ex) {
            // ffmpeg always complains for missing output file, so ignore exception
        }

        if (ac.ffStreamId == null) {
            throw new IOException("No ffmpeg stream for mpeg audio stream ID 0x" + streamId);
        }

        File outName = new File(out.getAbsolutePath() + '.' + ac.suffix);
        Command ffCmd = new Command(FFMPEG);
        ffCmd.param("-analyzeduration", Integer.MAX_VALUE);
        ffCmd.param("-probesize", PROBESIZE);
        ffCmd.param("-loglevel", "quiet");
        ffCmd.param("-stats");
        ffCmd.param("-i", vob);
        ffCmd.param("-map", ac.ffStreamId);
        ffCmd.param("-acodec", ac.acodec);
        ffCmd.param("-y", outName);

        ffCmd.redirectOutput(new FFmpegConsumer(meter, false, ac.duration));
        ffCmd.redirectErrorToOutput();

        ffCmd.execute();

        return outName;
    }

    /**
     * A helper class that collects the analyzed data.
     */
    private static class AnalyzerCollector implements Consumer<String> {
        private String ffStreamId;
        private String suffix;
        private String acodec = "copy";
        private Long duration = null;

        private static final Pattern DURATION = Pattern.compile(".*?Duration: (\\d\\d):(\\d\\d):(\\d\\d)\\.(\\d\\d).*");
        private final Pattern streamPattern;

        public AnalyzerCollector(String streamId) {
            streamPattern = Pattern.compile(".*?Stream #([0-9.:]+)\\[0x1?" + streamId + "\\].*?Audio:.([a-zA-Z0-9_]+).*");
        }

        @Override
        public void accept(String line) {
            if (duration == null) {
                Matcher d = DURATION.matcher(line);
                if (d.matches()) {
                    long hr = Long.parseLong(d.group(1));
                    long min = Long.parseLong(d.group(2));
                    long sec = Long.parseLong(d.group(3));
                    long frc = Long.parseLong(d.group(4));

                    duration = (hr * 60 * 60 * 100) + (min * 60 * 100) + (sec * 100) + frc;
                }
            }

            Matcher m = streamPattern.matcher(line);
            if (m.matches()) {
                ffStreamId = m.group(1);
                suffix = m.group(2);

                if ("pcm_dvd".equals(suffix)) {
                    suffix = "wav";
                    acodec = "pcm_s24le";
                } else if ("pcm_s16be".equals(suffix)) {
                    suffix = "wav";
                    acodec = "pcm_s16le";
                } else if (suffix.startsWith("pcm_")) {
                    suffix = "wav";
                }
            }
        }
    }

}
