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

import static java.util.stream.Collectors.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.shredzone.feinrip.model.Audio;
import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.progress.LogConsumer;
import org.shredzone.feinrip.progress.PercentConsumer;
import org.shredzone.feinrip.progress.ProgressMeter;
import org.shredzone.feinrip.util.Command;

/**
 * Service class for generating mkv files.
 * <p>
 * Main purpose is to assemble command line arguments and pass them to
 * <code>mkvmerge</code>.
 * <p>
 * However, <code>mkvmerge</code> does not always detect all audio streams in a source
 * vob file. For this reason, <code>mkvmerge</code> first analyzes the vob stream, putting
 * out all audio streams it found. This set of audio streams is aligned with the audio
 * streams found in the DVD structure. If there are streams missing, they will be
 * extracted in an intermediate step, and fed to <code>mkvmerge</code> separately.
 * <p>
 * Requires: <code>mkvtoolnix</code> package
 *
 * @author Richard "Shred" Körber
 */
public class MkvEncoder {
    private static final File MKVMERGE  = new File("/usr/bin/mkvmerge");
    private static final Pattern TRACK_PATTERN = Pattern.compile("Track ID (\\d+):.*stream_id:([0-9a-fA-F]+).sub_stream_id:([0-9a-fA-F]+).*");

    private final Map<Audio, ExtAudio> audioMap = new HashMap<>();

    private Project project;
    private File vobFile;
    private File chapFile;
    private File eitFile;
    private Map<Integer, File> vobsubFiles;
    private boolean forceAudioDemux;

    public void setProject(Project project) {
        this.project = project;
    }

    public void setVobFile(File vobFile) {
        this.vobFile = vobFile;
    }

    public void setChapFile(File chapFile) {
        this.chapFile = chapFile;
    }

    public void setEitFile(File eitFile) {
        this.eitFile = eitFile;
    }

    public void setVobsubFiles(Map<Integer, File> vobsubFiles) {
        this.vobsubFiles = vobsubFiles;
    }

    public void setForceAudioDemux(boolean forceAudioDemux) {
        this.forceAudioDemux = forceAudioDemux;
    }

    /**
     * Maps all vob audio streams to the internal stream indices used by
     * <code>mkvmerge</code>. Notes if <code>mkvmerge</code> misses audio streams.
     * <p>
     * Use {@link #getMissingAudioStreams()} to find out which audio streams were
     * undetected by <code>mkvmerge</code>.
     */
    public void prepareAudioStreams() throws IOException {
        Map<Integer, Integer> streamMap = getStreamMap();

        audioMap.clear();
        project.getAudios().forEach(audio -> {
            ExtAudio ea = new ExtAudio();
            ea.mkvStreamId = streamMap.get(audio.getStreamId());
            audioMap.put(audio, ea);
        });
    }

    /**
     * Analyzes the vob stream and returns a map of audio stream IDs found, along with
     * their index.
     *
     * @return Map of audio stream IDs found (as key) and the corresponding index (as
     *          value)
     */
    private Map<Integer, Integer> getStreamMap() throws IOException {
        Command mergeCmd = new Command(MKVMERGE);
        mergeCmd.param("--ui-language", "en_US");
        mergeCmd.param("--identify");
        mergeCmd.param(vobFile);
        Map<Integer, Integer> result = new HashMap<>();

        mergeCmd.redirectOutput(line -> {
            Matcher m = TRACK_PATTERN.matcher(line);
            if (m.matches()) {
                int streamId = Integer.parseInt(m.group(2), 16);
                int substreamId = Integer.parseInt(m.group(3), 16);
                int index = Integer.parseInt(m.group(1));

                if (substreamId > 0) {
                    result.put(substreamId, index);
                } else {
                    result.put(streamId, index);
                }
            }
        });

        mergeCmd.execute();

        return result;
    }

    /**
     * Finds all {@link Audio} which are undetected by <code>mkvmerge</code> and need to
     * be merged separately.
     *
     * @return Collection of {@link Audio}.
     */
    public Collection<Audio> getMissingAudioStreams() {
        return project.getAudios().stream()
                    .filter(Audio::isEnabled)
                    .filter(it -> forceAudioDemux || audioMap.get(it).mkvStreamId == null)
                    .collect(toSet());
    }

    /**
     * Maps an {@link Audio} entity to a file containing the unmuxed audio stream.
     *
     * @param audio
     *            {@link Audio} entity
     * @param file
     *            {@link File} containing the audio stream
     */
    public void mapAudioFile(Audio audio, File file) {
        audioMap.get(audio).file = file;
    }

    /**
     * Merges all streams and writes a mkv file, using the {@link Project} settings.
     *
     * @param meter
     *            {@link ProgressMeter} to update while writing
     * @param fn
     *            {@link File} to write the mkv to
     */
    public void writeMkv(ProgressMeter meter, File fn) throws IOException {
        fn.getParentFile().mkdirs();

        Command mergeCmd = new Command(MKVMERGE);
        mergeCmd.param("--title", project.getTitle());

        if (!project.isIgnoreChapters() && project.getChapters().size() > 1) {
            mergeCmd.param("--chapters", chapFile);
        }

        mergeCmd.param("-o", fn);

        if (project.getAspect() != null) {
            mergeCmd.param("--aspect-ratio", "0:" + project.getAspect().toString('/'));
        }

        if (!forceAudioDemux) {
            project.getAudios().stream()
                    .filter(Audio::isEnabled)
                    .forEach(audio -> {
                        Integer mkvStreamId = audioMap.get(audio).mkvStreamId;

                        if (project.getAudioSyncOffset() != 0) {
                            mergeCmd.param("--sync", mkvStreamId + ":" + project.getAudioSyncOffset());
                        }

                        if (mkvStreamId != null) {
                            mergeCmd.param("--language", mkvStreamId + ":" + audio.getLanguage());
                        }
                    });

            if (project.getDefAudio() != null && audioMap.get(project.getDefAudio()).mkvStreamId != null) {
                mergeCmd.param("--default-track", audioMap.get(project.getDefAudio()).mkvStreamId);
            }
        }

        String useTracks = project.getAudios().stream()
                .filter(Audio::isEnabled)
                .filter(it -> !forceAudioDemux)
                .filter(it -> audioMap.get(it).mkvStreamId != null)
                .filter(it -> audioMap.get(it).file == null)
                .map(it -> audioMap.get(it).mkvStreamId.toString())
                .collect(joining(","));

        if (!useTracks.isEmpty()) {
            mergeCmd.param("--audio-tracks", useTracks);
        } else {
            mergeCmd.param("--no-audio");
        }

        mergeCmd.param("--no-subtitles"); // strip all subtitles from the vob file!
        mergeCmd.param(vobFile);

        project.getAudios().forEach(audio -> {
            File audioFile = audioMap.get(audio).file;
            if (audioFile != null) {
                mergeCmd.param("--language", "0:" + audio.getLanguage());

                if (project.getAudioSyncOffset() != 0) {
                    mergeCmd.param("--sync", "0:" + project.getAudioSyncOffset());
                }

                if (project.getDefAudio() == audio) {
                    mergeCmd.param("--default-track", 0);
                }

                mergeCmd.param(audioFile);
            }
        });

        project.getSubs().forEach(sub -> {
            File subFile = vobsubFiles.get(sub.getIndex());
            if (sub.isEnabled() && subFile != null) {
                mergeCmd.param("--language", "0:" + sub.getLanguage());
                mergeCmd.param("--default-track", project.getDefSub() == sub ? "0:1" : "0:0");
                mergeCmd.param(subFile);
            }
        });

        if (eitFile != null && eitFile.exists() && eitFile.isFile()) {
            mergeCmd.param("--attachment-name", "movie.eit");
            mergeCmd.param("--attachment-mime-type", "application/vnd.dvb.eit");
            mergeCmd.param("--attach-file", eitFile);
        }

        mergeCmd.redirectOutput(new PercentConsumer(meter, false));
        mergeCmd.redirectError(new LogConsumer(meter, true));

        mergeCmd.failedIf(rc -> rc > 1);
        mergeCmd.execute();
    }

    /**
     * Returns a JSON string of the current configuration of files and streams.
     */
    public String getJsonProcessData() {
        JSONObject json = new JSONObject();
        json.put("title", project.getTitle());
        json.put("source", vobFile);
        json.put("chapterFile", chapFile);
        json.put("eitFile", eitFile);
        json.put("vobsubFiles", vobsubFiles);

        Map<Integer, File> audi = new TreeMap<>();
        audioMap.forEach((a, ea) -> audi.put(a.getStreamId(), ea.file));
        json.put("audioFiles", audi);

        return json.toString();
    }

    private static class ExtAudio {
        Integer mkvStreamId;
        File file;
    }

}
