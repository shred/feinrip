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
package org.shredzone.feinrip.gui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.shredzone.feinrip.model.Track;
import org.shredzone.feinrip.source.TrackableSource;

/**
 * A {@link JList} that renders a {@link TrackableSource} and allows the user to select a
 * {@link Track}.
 *
 * @author Richard "Shred" Körber
 */
public class JTrackList extends JList<Track> implements ListSelectionListener, PropertyChangeListener {
    private static final long serialVersionUID = 4608933969471490267L;

    private TrackableSource tracks;

    /**
     * Creates an empty {@link JTrackList}.
     */
    public JTrackList() {
        this(null);
        addListSelectionListener(this);
    }

    /**
     * Creates a {@link JTrackList} showing the given {@link TrackableSource}.
     *
     * @param tracks
     *            {@link TrackableSource} to be shown
     */
    public JTrackList(TrackableSource tracks) {
        super(new TrackListModel(tracks));
        this.tracks = tracks;
        if (tracks != null) {
            tracks.addPropertyChangeListener(this);
        }
        setCellRenderer(new TrackListRenderer());
    }

    /**
     * Sets a new model of {@link TrackableSource}.
     *
     * @param tracks
     *            {@link TrackableSource} to be shown
     */
    public void setSource(TrackableSource tracks) {
        if (this.tracks != null) {
            this.tracks.removePropertyChangeListener(this);
        }
        if (tracks != null) {
            tracks.addPropertyChangeListener(this);
        }

        this.tracks = tracks;
        setModel(new TrackListModel(tracks));

        if (tracks != null) {
            setSelectedValue(tracks.getSelectedTrack(), true);
        } else {
            setSelectedValue(null, false);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (tracks != null) {
            Track selection = getSelectedValue();
            Track previous = tracks.getSelectedTrack();
            if (selection != previous) {
                tracks.setSelectedTrack(selection);
                tracks.setupProject();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "source.device": //NOSONAR: falls through
                setModel(new TrackListModel(tracks));
                // falls through...

            case "source.track":
                Object current = getSelectedValue();
                Object selection = evt.getNewValue();
                if (selection != current) {
                    setSelectedValue(evt.getNewValue(), true);
                }
                break;
        }
    }

    /**
     * A simple {@link ListModel} that keeps an unmodifiable array of {@link Track}.
     */
    public static class TrackListModel implements ListModel<Track> {
        private final TrackableSource tracks;

        public TrackListModel(TrackableSource tracks) {
            this.tracks = tracks;
        }

        @Override
        public int getSize() {
            return (tracks != null ? tracks.getTracks().size() : 0);
        }

        @Override
        public Track getElementAt(int index) {
            return tracks.getTracks().get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            // Track entities won't change, so there is no need to listen...
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            // Track entities won't change, so there is no need to listen...
        }
    }

    /**
     * A renderer for tracks.
     */
    public static class TrackListRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = -2703114154053607262L;
        private static final ResourceBundle B = ResourceBundle.getBundle("message");

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object renderValue = value;
            if (renderValue != null && renderValue instanceof Track) {
                Track track = (Track) renderValue;

                renderValue = MessageFormat.format(B.getString("track.list"),
                                track.getTrack(),
                                track.getLength(),
                                String.valueOf(track.getDimension().width) + 'x' + String.valueOf(track.getDimension().height),
                                track.getAspect(),
                                track.getAngles(),
                                track.getChapters()
                );
            }

            return super.getListCellRendererComponent(list, renderValue, index, isSelected, cellHasFocus);
        }
    }

}
