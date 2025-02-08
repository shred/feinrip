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
package org.shredzone.feinrip.source;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.model.Subtitle;
import org.shredzone.feinrip.progress.ProgressMeter;

/**
 * This interface defines a stream source.
 *
 * @author Richard "Shred" Körber
 */
public interface Source {

    /**
     * Binds the stream source to a {@link Project}.
     *
     * @param project
     *            {@link Project} to bind this {@link Source} to
     */
    void bind(Project project);

    /**
     * Initializes all parameters of the bound {@link Project}. Audios, subtitles,
     * chapters etc will be set.
     */
    void setupProject();

    /**
     * Gets a simple HTML formatted description of the current settings of the source,
     * to be used in the PowerPaneTab body.
     */
    String getHtmlDescription();

    /**
     * Returns a title proposal based on the source parameters. This could be the DVD
     * name or the file name of a vob file.
     */
    String getTitleProposal();

    /**
     * {@code true} if the source is valid for reading. If {@code false}, some internal
     * parameters may be missing or invalid, like e.g. a missing track number.
     */
    boolean isValid();

    /**
     * {@code true} if the vob file returned by {@link #createVobFile(ProgressMeter)}
     * is likely corrupted (for example because of read errors while reading a DVD).
     * <p>
     * Only valid after {@link #createVobFile(ProgressMeter)} has been invoked.
     */
    boolean isVobFileCorrupted();

    /**
     * Returns the file name of the target file. Resolves all placeholders.
     */
    String resolveTargetFileName();

    /**
     * Returns a vob file of the source stream. The source file could not be our propery,
     * so we shouldn't do anything more than reading it.
     *
     * @param meter
     *            {@link ProgressMeter} to be used for showing progress
     * @return {@link File} of the vob source
     */
    File createVobFile(ProgressMeter meter) throws IOException;

    /**
     * Deletes the vob file returned by {@link #createVobFile(ProgressMeter)}, if
     * applicable. Does nothing if the vob file was not created by us.
     *
     * @param vob
     *            Vob file
     */
    void deleteVobFile(File vob) throws IOException;

    /**
     * Returns a subtitle file unmuxed from the source stream. The file could not be our
     * property, so we shouldn't do anything more than reading it.
     *
     * @param sub
     *            {@link Subtitle} to extract
     * @param sourceVob
     *            Source vob file to extract the subtitle from
     * @param meter
     *            {@link ProgressMeter} to be used for showing progress
     * @return Subtitle file
     */
    File createSubFile(Subtitle sub, File sourceVob, ProgressMeter meter) throws IOException;

    /**
     * Deletes the subtitle file returned by
     * {@link #createSubFile(Subtitle, File, ProgressMeter)}, if applicable. Does nothing
     * if the vob file was not created by us.
     *
     * @param file
     *            Subtitle file
     */
    void deleteSubFile(File file) throws IOException;

    /**
     * Creates an EIT (DVB Event Information Table) file.
     *
     * @return EIT {@link File}, or {@code null} if there is no such file
     */
    File createEitFile() throws IOException;

    /**
     * Deletes the EIT file returned by {@link #createEitFile()}, if applicable.
     *
     * @param file
     *            EIT {@link File}, may be {@code null}
     */
    void deleteEitFile(File file) throws IOException;

    void addPropertyChangeListener(PropertyChangeListener l);
    void removePropertyChangeListener(PropertyChangeListener l);

}
