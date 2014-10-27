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
package org.shredzone.feinrip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.shredzone.feinrip.audio.PlaySoundFx;
import org.shredzone.feinrip.model.Audio;
import org.shredzone.feinrip.model.Configuration;
import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.model.Subtitle;
import org.shredzone.feinrip.progress.ProgressMeter;
import org.shredzone.feinrip.source.Source;
import org.shredzone.feinrip.system.ChapterUtils;
import org.shredzone.feinrip.system.MkvEncoder;
import org.shredzone.feinrip.system.StreamUtils;

/**
 * The main processor for generating mkv files.
 *
 * @author Richard "Shred" Körber
 */
public class FeinripProcessor {
    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private final Configuration config = Configuration.global();
    private final Project project;
    private ProgressMeter progressMeter;
    private File chapFile;
    private File vobFile;
    private File eitFile;
    private Map<Integer, File> vobsubFiles = new HashMap<>();
    private List<File> audioFiles = new ArrayList<>();
    private Runnable preMuxHook;

    /**
     * Creates a new {@link FeinripProcessor}.
     */
    public FeinripProcessor(Project project) {
        this.project = project;
    }

    public void setPreMuxHook(Runnable preMuxHook) {
        this.preMuxHook = preMuxHook;
    }

    public void setProgressMeter(ProgressMeter progressMeter) {
        this.progressMeter = progressMeter;
    }

    /**
     * Starts conversion process.
     */
    public void start() throws IOException {
        // Globally change default directory for createTempFile
        System.setProperty("java.io.tmpdir", config.getTempDir());

        final Source source = project.getSource();

        try {
            createChapterFile();
            createVobFile();
            createEitFile();

            for (Subtitle sub : project.getSubs()) {
                if (sub.isEnabled()) {
                    createSubtitleFiles(sub);
                }
            }

            MkvEncoder encoder = new MkvEncoder();
            encoder.setProject(project);
            encoder.setVobFile(vobFile);
            encoder.setChapFile(chapFile);
            encoder.setEitFile(eitFile);
            encoder.setVobsubFiles(vobsubFiles);
            encoder.setForceAudioDemux(config.isForceAudioDemux());

            encoder.prepareAudioStreams();

            for (Audio audio : encoder.getMissingAudioStreams()) {
                if (progressMeter != null) {
                    progressMeter.message(B.getString("progress.audio"), audio.getIx(), audio.getLanguage()).percent(null);
                }

                File audioFile = File.createTempFile("feinrip-", "-audio-" + audio.getIx());
                try {
                    File resultFile = StreamUtils.extractAudio(vobFile, audio, audioFile, progressMeter);
                    audioFiles.add(resultFile);
                    encoder.mapAudioFile(audio, resultFile);
                } finally {
                    audioFile.delete();
                }
            }

            if (preMuxHook != null) {
                preMuxHook.run();
            }

            if (progressMeter != null) {
                progressMeter.message(B.getString("progress.mkv")).percent(null);
            }
            encoder.writeMkv(progressMeter, new File(source.resolveTargetFileName()));

        } finally {
            if (progressMeter != null) {
                progressMeter.message(B.getString("progress.cleanup")).percent(null);
            }

            source.deleteVobFile(vobFile);

            if (chapFile != null) {
                chapFile.delete();
            }

            for (File file : vobsubFiles.values()) {
                source.deleteSubFile(file);
            }

            for (File file : audioFiles) {
                file.delete();
            }

            source.deleteEitFile(eitFile);

            playSound();
        }

        if (progressMeter != null) {
            progressMeter.message(B.getString("progress.done")).percent(100.0f);
        }
    }

    private void createChapterFile() throws IOException {
        if (progressMeter != null) {
            progressMeter.message(B.getString("progress.chapter")).percent(null);
        }
        chapFile = File.createTempFile("feinrip-", ".chap");
        ChapterUtils.writeChapters(project.getChapters(), chapFile);
    }

    private void createVobFile() throws IOException {
        if (progressMeter != null) {
            progressMeter.message(B.getString("progress.vob"))
                    .percent(null);
        }
        // ATTENTION: don't create file names of the pattern /(.+[_\-])(\d+)$/!
        // It will trigger mkvmerge's open_multi, which merges ALL FILES of the
        // same pattern in the same directory AND CANNOT BE TURNED OFF.
        vobFile = project.getSource().createVobFile(progressMeter);
    }

    private void createSubtitleFiles(Subtitle sub) throws IOException {
        if (progressMeter != null) {
            progressMeter.message(B.getString("progress.sub"), sub.getIndex(), sub.getLanguage())
                    .percent(null);
        }
        File subFile = project.getSource().createSubFile(sub, vobFile, progressMeter);
        vobsubFiles.put(sub.getIndex(), subFile);
    }

    private void createEitFile() throws IOException {
        eitFile = project.getSource().createEitFile();
    }

    private void playSound() {
        String soundFile = config.getSoundFile();
        if (soundFile != null) {
            File resource = new File(soundFile);
            if (resource.exists() && resource.isFile()) {
                Runnable r = () -> PlaySoundFx.mp3(resource);
                new Thread(r).start();
            }
        }
    }

}
