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
package org.shredzone.feinrip.model;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.shredzone.feinrip.database.TvdbService.TvdbEpisode;
import org.shredzone.feinrip.source.Source;

/**
 * The main model of a conversion. It contains all details required to read the source
 * and convert it to the desired mkv file.
 *
 * @author Richard "Shred" Körber
 */
public class Project {

    private Source source;
    private String output;
    private boolean processing;

    private String title;
    private List<TvdbEpisode> episodes = null;
    private TvdbEpisode episode;
    private boolean ignoreChapters;

    private List<Chapter> chapters = new ArrayList<>();
    private List<Audio> audios = new ArrayList<>();
    private List<Subtitle> subs = new ArrayList<>();

    private Audio defAudio = null;
    private Subtitle defSub = null;

    private Dimension size = null;
    private AspectRatio aspect = AspectRatio.ASPECT_4_3;

    private int audioSyncOffset = 0;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final PropertyChangeListener listener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            support.firePropertyChange(evt);
        }
    };

    /**
     * The stream source.
     */
    public Source getSource()                   { return source; }
    public void setSource(Source source) {
        Source old = this.source;
        this.source = source;

        if (old != null) {
            old.removePropertyChangeListener(listener);
        }

        if (source != null) {
            source.addPropertyChangeListener(listener);
        }

        support.firePropertyChange("source", old, source);
    }

    /**
     * Output path and file name. Contains placeholders for title, track, episodes etc.
     */
    public String getOutput()                   { return output; }
    public void setOutput(String output) {
        String old = this.output;
        this.output = output;
        support.firePropertyChange("output", old, output);
    }

    /**
     * {@code true} if this project is currently being processed. Properties should not
     * be changed while processing!
     */
    public boolean isProcessing()               { return processing; }
    public void setProcessing(boolean processing) {
        boolean old = this.processing;
        this.processing = processing;
        support.firePropertyChange("processing", old, processing);
    }

    /**
     * Movie title.
     */
    public String getTitle()                    { return title; }
    public void setTitle(String title) {
        String old = this.title;
        this.title = title;
        support.firePropertyChange("title", old, title);
    }

    /**
     * List of available TV episodes, or {@code null}.
     */
    public List<TvdbEpisode> getEpisodes()      { return episodes; }
    public void setEpisodes(List<TvdbEpisode> episodes) {
        List<TvdbEpisode> old = this.episodes;
        this.episodes = episodes;
        support.firePropertyChange("episodes", old, episodes);
    }

    /**
     * Selected TV episode. May be {@code null}.
     */
    public TvdbEpisode getEpisode()             { return episode; }
    public void setEpisode(TvdbEpisode episode) {
        TvdbEpisode old = this.episode;
        this.episode = episode;
        support.firePropertyChange("episode", old, episode);
    }

    /**
     * If {@code true}, chapters will not be written to the target file, even if chapters
     * are defined.
     */
    public boolean isIgnoreChapters()           { return ignoreChapters; }
    public void setIgnoreChapters(boolean ignoreChapters) {
        boolean old = this.ignoreChapters;
        this.ignoreChapters = ignoreChapters;
        support.firePropertyChange("ignoreChapters", old, ignoreChapters);
    }

    /**
     * List of {@link Chapter}. May be {@code null} or empty.
     */
    public List<Chapter> getChapters()          { return chapters; }
    public void setChapters(List<Chapter> chapters) {
        List<Chapter> old = this.chapters;
        this.chapters = chapters;
        support.firePropertyChange("chapters", old, chapters);
    }

    /**
     * Touches the chapters when one of the {@link Chapter} entries was changed.
     */
    public void touchChapters() {
        support.firePropertyChange("chapters", null, chapters);
    }

    /**
     * List of {@link Audio}. May be {@code null} or empty.
     */
    public List<Audio> getAudios()              { return audios; }
    public void setAudios(List<Audio> audios) {
        List<Audio> old = this.audios;
        this.audios = audios;
        support.firePropertyChange("audios", old, audios);
    }

    /**
     * Touches the audios when one of the {@link Audio} entries was changed.
     */
    public void touchAudios() {
        support.firePropertyChange("audios", null, audios);
    }

    /**
     * List of {@link Subtitle}. May be {@code null} or empty.
     */
    public List<Subtitle> getSubs()             { return subs; }
    public void setSubs(List<Subtitle> subs) {
        List<Subtitle> old = this.subs;
        this.subs = subs;
        support.firePropertyChange("subs", old, subs);
    }

    /**
     * Touches the subs when one of the {@link Subtitle} entries was changed.
     */
    public void touchSubs() {
        support.firePropertyChange("subs", null, subs);
    }

    /**
     * The default {@link Audio} track. {@code null} if there is no default track.
     * The instance must be one of {@link #getAudios()}.
     */
    public Audio getDefAudio()                  { return defAudio; }
    public void setDefAudio(Audio defAudio) {
        Audio old = this.defAudio;
        this.defAudio = defAudio;
        support.firePropertyChange("defAudio", old, defAudio);
    }

    /**
     * The default {@link Subtitle}. {@code null} if there is no default subtitle.
     * The instance must be one of {@link #getSubs()}.
     */
    public Subtitle getDefSub()                 { return defSub; }
    public void setDefSub(Subtitle defSub) {
        Subtitle old = this.defSub;
        this.defSub = defSub;
        support.firePropertyChange("defSub", old, defSub);
    }

    /**
     * Size of the video stream images.
     */
    public Dimension getSize()                  { return size; }
    public void setSize(Dimension size) {
        Dimension old = this.size;
        this.size = size;
        support.firePropertyChange("size", old, size);
    }

    /**
     * Aspect ratio of the video stream.
     */
    public AspectRatio getAspect()              { return aspect; }
    public void setAspect(AspectRatio aspect) {
        AspectRatio old = this.aspect;
        this.aspect = aspect;
        support.firePropertyChange("aspect", old, aspect);
    }

    /**
     * Audio tracks sync offset, in ms.
     */
    public int getAudioSyncOffset()             { return audioSyncOffset; }
    public void setAudioSyncOffset(int audioSyncOffset) {
        int old = this.audioSyncOffset;
        this.audioSyncOffset = audioSyncOffset;
        support.firePropertyChange("audioSyncOffset", old, audioSyncOffset);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

}
