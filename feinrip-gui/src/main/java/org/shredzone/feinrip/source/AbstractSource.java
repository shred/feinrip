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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.shredzone.feinrip.database.TvdbService.TvdbEpisode;
import org.shredzone.feinrip.model.Project;

/**
 * Abstract implementation of {@link Source}.
 *
 * @author Richard "Shred" Körber
 */
public abstract class AbstractSource implements Source {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * {@link Project} the source is bound to.
     */
    protected Project project;

    @Override
    public void bind(Project project) {
        this.project = project;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    /**
     * Fires a property change event.
     *
     * @param propertyName
     *            Property name
     * @param oldValue
     *            old value
     * @param newValue
     *            new value
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public String resolveTargetFileName() {
        String result = project.getOutput();

        result = result.replace("%t", project.getTitle().replace('/', '_'));

        TvdbEpisode episode = project.getEpisode();
        if (episode != null) {
            result = result.replace("%ss", String.format("%02d", episode.season));
            result = result.replace("%s", String.valueOf(episode.season));
            result = result.replace("%ee", String.format("%02d", episode.episode));
            result = result.replace("%et", episode.title);
            result = result.replace("%e", String.valueOf(episode.episode));

        } else {
            result = result.replace("%ss", "");
            result = result.replace("%s", "");
            result = result.replace("%ee", "");
            result = result.replace("%et", "");
            result = result.replace("%e", "");
        }

        return result;
    }

}
