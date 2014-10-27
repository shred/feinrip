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
package org.shredzone.feinrip.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.shredzone.feinrip.gui.ErrorDialog;
import org.shredzone.feinrip.model.Audio;
import org.shredzone.feinrip.model.Chapter;
import org.shredzone.feinrip.model.Configuration;
import org.shredzone.feinrip.model.MountPoint;
import org.shredzone.feinrip.model.Palette;
import org.shredzone.feinrip.model.StreamType;
import org.shredzone.feinrip.model.Subtitle;
import org.shredzone.feinrip.model.Track;
import org.shredzone.feinrip.progress.ProgressMeter;
import org.shredzone.feinrip.system.EitAnalyzer;
import org.shredzone.feinrip.system.StreamUtils;
import org.shredzone.feinrip.system.TrackAnalyzer;
import org.shredzone.feinrip.util.DvdAnalyzer;

/**
 * A {@link Source} for physical DVDs.
 *
 * @author Richard "Shred" Körber
 */
public class DvdSource extends AbstractSource implements TrackableSource {

    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private final Configuration config = Configuration.global();

    private File device;
    private File mountPoint;
    private File eitFile;
    private DvdAnalyzer dvd;
    private List<Track> tracks;
    private Track track;

    /**
     * Sets the {@link MountPoint} to be used.
     */
    public void setMountPoint(MountPoint mp) {
        File oldDevice = device;
        File oldMountPoint = mountPoint;

        if (mp != null) {
            device = mp.getDevice();
            mountPoint = mp.getMount();
            try {
                dvd = new DvdAnalyzer(getMountPoint());
                tracks = Arrays.asList(dvd.getTracks());
                eitFile = findEitFile(mountPoint);
            } catch (IOException ex) {
                throw new RuntimeException("Could not analyze DVD", ex);
            }
        } else {
            device = null;
            mountPoint = null;
            dvd = null;
            tracks = null;
            eitFile = null;
        }

        firePropertyChange("source.device", oldDevice, device);
        firePropertyChange("source.mountPoint", oldMountPoint, mountPoint);
    }

    /**
     * Returns the device of the physical DVD drive.
     */
    public File getDevice()                     { return device; }

    /**
     * Returns the path to the mounted DVD.
     */
    public File getMountPoint()                 { return mountPoint; }

    /**
     * Additional EIT file name.
     */
    public File getEitFile()                    { return eitFile; }
    public void setEitFile(File eitFile) {
        File old = this.eitFile;
        this.eitFile = eitFile;
        firePropertyChange("source.eitFile", old, this.eitFile);
    }

    /**
     * Selected streaming type.
     */
    public StreamType getStreamType()           {
        return config.getDvdStreamType();
    }

    public void setStreamType(StreamType streamType) {
        config.setDvdStreamType(streamType);
    }

    @Override
    public List<Track> getTracks() {
        return tracks;
    }

    @Override
    public Track getSelectedTrack() {
        return track;
    }

    @Override
    public void setSelectedTrack(Track track) {
        Track oldTrack = this.track;
        this.track = track;
        firePropertyChange("source.track", oldTrack, this.track);
    }

    /**
     * Returns the longest {@link Track}, or {@code null} if there is no such track.
     */
    public Track getLongestTrack() {
        if (tracks != null) {
            int tnr = dvd.getLongestTrack();
            return tracks.stream()
                    .filter(t -> t.getTrack() == tnr)
                    .findAny()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Returns the selected track number, or 0 if no track is selected.
     */
    protected int getSelectedTrackNr() {
        return (track != null ? track.getTrack() : 0);
    }

    /**
     * Returns {@code true} if the bound project is being processed.
     */
    public boolean isProcessing() {
        return (project != null && project.isProcessing());
    }

    @Override
    public void nextTrack() {
        if (tracks != null && track != null) {
            int next = getSelectedTrackNr() + 1;
            int max = tracks.size();

            if (next < max) {
                setSelectedTrack(tracks.get(next));
            }
        }
    }

    @Override
    public boolean isValid() {
        return (device != null && mountPoint != null && track != null);
    }

    @Override
    public void setupProject() {
        project.setDefAudio(null);
        project.setDefSub(null);

        List<Audio> audios = dvd.getAudios(getSelectedTrackNr());
        project.setAudios(audios);
        if (audios.size() == 1) {
            // If there is only one audio stream, make it the default
            project.setDefAudio(audios.get(0));

        } else {
            // If there is a stream of the user's language, make it the default
            String selectLanguage = B.getString("language.selected");
            audios.stream()
                    .filter(audio -> selectLanguage.equals(audio.getLanguage().toShortString()))
                    .findFirst()
                    .ifPresent(audio -> project.setDefAudio(audio));
        }
        try {
            setAudioAvailableState(audios);
        } catch (IOException ex) {
            ErrorDialog.showException(ex);
        }
        project.touchAudios();

        List<Subtitle> subtitles = dvd.getSubtitles(getSelectedTrackNr());
        project.setSubs(subtitles);
        project.touchSubs();

        if (track != null) {
            project.setChapters(dvd.getChapters(getSelectedTrackNr()));
        } else {
            List<Chapter> emptyChapters = Collections.emptyList();
            project.setChapters(emptyChapters);
        }
        project.touchChapters();

        project.setAspect(track != null ? track.getAspect() : null);
        project.setSize(track != null ? track.getDimension() : null);
    }

    /**
     * Sets the available state of each {@link Audio} entry according to the actually
     * available audio streams.
     */
    private void setAudioAvailableState(List<Audio> audios) throws IOException {
        int trackNr = getSelectedTrackNr();

        if (trackNr > 0) {
            TrackAnalyzer analyzer = new TrackAnalyzer(device, trackNr);
            Set<Integer> audioIds = analyzer.getAudioIds();
            audios.forEach(a -> {
                a.setAvailable(audioIds.contains(a.getStreamId()));
                a.setEnabled(a.isEnabled() && a.isAvailable());
            });
        }
    }

    @Override
    public String getTitleProposal() {
        if (getMountPoint() == null) {
            return "";
        }

        if (eitFile != null) {
            try {
                String eitTitle = EitAnalyzer.getTitle(eitFile);
                if (eitTitle != null) {
                    return eitTitle.trim();
                }
            } catch (IOException ex) {
                // Silently ignore this exception and try something else...
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String part : dvd.getTitle().split("[ _-]+")) {
            part = part.trim();
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1).toLowerCase());
                }
            }
            sb.append(' ');
        }

        return sb.toString().trim();
    }

    @Override
    public String resolveTargetFileName() {
        String result = super.resolveTargetFileName();

        result = result.replace("%nn", String.format("%02d", getSelectedTrackNr()));
        result = result.replace("%n", String.valueOf(getSelectedTrackNr()));

        return result;
    }

    @Override
    public File createVobFile(ProgressMeter meter) throws IOException {
        File vobFile = File.createTempFile("feinrip-", "-video.vob");
        StreamUtils.readStream(getDevice(), getSelectedTrackNr(), vobFile, getStreamType(), meter);
        return vobFile;
    }

    @Override
    public void deleteVobFile(File vob) throws IOException {
        if (vob != null && vob.exists()) {
            vob.delete();
        }
    }

    @Override
    public File createSubFile(Subtitle sub, File sourceVob, ProgressMeter meter) throws IOException {
        File srtFile = EitAnalyzer.findSrtFile(getMountPoint(), sub);
        if (srtFile != null) {
            return srtFile;
        }

        File vobsubFile = File.createTempFile("feinrip-", "-sub-" + sub.getIndex());
        try {
            StreamUtils.readSubtitle(sub, getMountPoint(), sourceVob, vobsubFile, meter);

            File idxFile = new File(vobsubFile.getAbsolutePath() + ".idx");
            File subFile = new File(vobsubFile.getAbsolutePath() + ".sub");

            if (!subFile.exists() || subFile.length() == 0) { // ignore empty sub files
                return null;
            }

            // Exchange palette, mencoder seems to have some difficulties...
            replacePalette(idxFile, dvd.getPalette(track.getTrack()));

            return idxFile;
        } finally {
            vobsubFile.delete();
        }
    }

    /**
     * Replaces the palette of a vobsub idx file.
     *
     * @param idx
     *            idx file to replace the palette of
     * @param palette
     *            {@link Palette} to be used in the file instead
     */
    private void replacePalette(File idx, Palette palette) throws IOException {
        File tmpFile = File.createTempFile("feinrip-", "-sub-tmp");
        if (!idx.renameTo(tmpFile)) {
            throw new IOException("Could not rename " + idx.getName());
        }

        try (BufferedReader in = new BufferedReader(new FileReader(tmpFile));
             PrintWriter out = new PrintWriter(idx)) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("palette: ")) {
                    out.print("palette: ");
                    out.println(palette.toRgbString());
                } else {
                    out.println(line);
                }
            }
        }

        tmpFile.delete();
    }

    @Override
    public void deleteSubFile(File file) throws IOException {
        if (file != null) {
            String name = file.getAbsolutePath();
            // Do not delete srt files, because they are not ours...
            if (name.endsWith(".idx")) {
                File subFile = new File(name.substring(0, name.length() - 3).concat("sub"));
                file.delete();
                subFile.delete();
            }
        }
    }

    @Override
    public File createEitFile() throws IOException {
        return eitFile;
    }

    @Override
    public void deleteEitFile(File file) throws IOException {
        // Do not delete eit file because it is not ours...
    }

    /**
     * Finds a .eit file at the root of the mount point. If there are multiple .eit
     * files, a random one is chosen.
     *
     * @param base
     *            Base directory to find the first eit file in
     * @return eit {@link File} or {@code null} if there is no such file
     */
    private File findEitFile(File base) {
        return Arrays.stream(base.listFiles()).parallel()
                        .filter(file ->    file.isFile()
                                        && !file.isHidden()
                                        && file.getName().toLowerCase().endsWith(".eit"))
                        .findAny()
                        .orElse(null);
    }

    @Override
    public String getHtmlDescription() {
        if (mountPoint == null || device == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder("<html>");
        sb.append(mountPoint.getName()).append("<br>");
        sb.append("Device: ").append(device.getAbsolutePath());
        if (track != null) {
            sb.append("<br>Track: ").append(track.getTrack());
        }
        return sb.toString();
    }

}