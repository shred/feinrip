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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.shredzone.feinrip.gui.ErrorDialog;
import org.shredzone.feinrip.model.Audio;
import org.shredzone.feinrip.model.Palette;
import org.shredzone.feinrip.model.PaletteType;
import org.shredzone.feinrip.model.Subtitle;
import org.shredzone.feinrip.progress.ProgressMeter;
import org.shredzone.feinrip.system.EitAnalyzer;
import org.shredzone.feinrip.system.StreamUtils;
import org.shredzone.feinrip.system.VobAnalyzer;
import org.shredzone.feinrip.util.VobsubIndex;
import org.shredzone.feinrip.util.VobsubIndex.Setting;

/**
 * A {@link Source} for a single vob file.
 *
 * @author Richard "Shred" Körber
 */
public class VobSource extends AbstractSource {

    private File vobFile;
    private File eitFile;
    private PaletteType palette = PaletteType.DEFAULT;
    private Palette customPalette;

    /**
     * The source VOB file name.
     */
    public File getVobFile()                    { return vobFile; }
    public void setVobFile(File vobFile) {
        File old = this.vobFile;
        this.vobFile = vobFile;
        firePropertyChange("source.vobFile", old, this.vobFile);
    }

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
     * The {@link PaletteType} for enclosed subtitles.
     */
    public PaletteType getPalette()             { return palette; }
    public void setPalette(PaletteType palette) {
        PaletteType old = this.palette;
        this.palette = palette;
        firePropertyChange("source.palette", old, this.palette);
    }

    /**
     * The custom palette, if {@link PaletteType#CUSTOM} is used.
     */
    public Palette getCustomPalette()           { return customPalette; }
    public void setCustomPalette(Palette customPalette) {
        Palette old = this.customPalette;
        this.customPalette = customPalette;
        firePropertyChange("source.customPalette", old, this.customPalette);
    }

    @Override
    public boolean isValid() {
        return (vobFile != null && vobFile.exists() && vobFile.isFile());
    }

    @Override
    public void setupProject() {
        project.setDefAudio(null);
        project.setDefSub(null);

        try {
            VobAnalyzer analyzer = new VobAnalyzer(vobFile);

            List<Audio> audios = analyzer.getAudios();
            project.setAudios(audios);
            if (audios.size() == 1) {
                project.setDefAudio(audios.get(0));
            }
            project.touchAudios();

            project.setSubs(analyzer.getSubs());
            project.touchSubs();

            project.getChapters().clear();
            project.touchChapters();

            project.setSize(analyzer.getDimension());
            project.setAspect(analyzer.getAspect());
            project.setAudioSyncOffset(0);
        } catch (IOException ex) {
            ErrorDialog.showException(ex);
        }
    }

    @Override
    public String getTitleProposal() {
        if (eitFile != null) {
            try {
                String title = EitAnalyzer.getTitle(eitFile);
                if (title != null && !title.trim().isEmpty()) {
                    return title;
                }
            } catch (IOException ex) {
                // Silently ignore this exception and try something else...
            }
        }

        if (vobFile == null) {
            return "";
        }

        String fn = vobFile.getName();
        int pos = fn.lastIndexOf('.');
        if (pos > 0) {
            fn = fn.substring(0, pos);
        }
        return fn;
    }

    @Override
    public File createVobFile(ProgressMeter meter) throws IOException {
        return vobFile;
    }

    @Override
    public void deleteVobFile(File vob) throws IOException {
        // Do not delete the vob file, it's not ours...
    }

    @Override
    public File createSubFile(Subtitle sub, File sourceVob, ProgressMeter meter) throws IOException {
        File vobsubFile = File.createTempFile("feinrip-", "-sub-" + sub.getIndex());
        try {
            Palette paletteColors;
            if (palette == PaletteType.CUSTOM) {
                paletteColors = customPalette;
            } else {
                paletteColors = palette.getPalette();
            }

            StreamUtils.readSubtitleNoIfo(sub, sourceVob, vobsubFile, project.getSize(), meter);

            File idxFile = new File(vobsubFile.getAbsolutePath() + ".idx");
            File subFile = new File(vobsubFile.getAbsolutePath() + ".sub");

            if (subFile.exists() && subFile.length() > 0) { // ignore empty sub files
                return idxFile;
            }

            VobsubIndex vsi = new VobsubIndex();
            vsi.read(idxFile);
            vsi.set(Setting.PALETTE, paletteColors.toRgbString());
            vsi.write(idxFile);

            return null;
        } finally {
            if (!vobsubFile.delete()) {
                throw new IOException("Could not delete " + vobsubFile.getAbsolutePath());
            }
        }
    }

    @Override
    public void deleteSubFile(File file) throws IOException {
        if (file != null) {
            String name = file.getAbsolutePath();
            File subFile = new File(name.substring(0, name.length() - 3).concat("sub"));
            if (!file.delete()) {
                throw new IOException("Could not delete " + file.getAbsolutePath());
            }
            if (!subFile.delete()) {
                throw new IOException("Could not delete " + subFile.getAbsolutePath());
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

    @Override
    public String getHtmlDescription() {
        if (vobFile == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder("<html>");
        sb.append(vobFile.getName());

        if (palette != null && palette != PaletteType.DEFAULT) {
            sb.append("<br>Palette: ").append(palette);
        }

        return sb.toString();
    }

}
