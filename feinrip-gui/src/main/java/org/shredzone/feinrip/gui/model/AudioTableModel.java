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
package org.shredzone.feinrip.gui.model;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.shredzone.feinrip.model.Audio;
import org.shredzone.feinrip.model.AudioType;
import org.shredzone.feinrip.model.Language;
import org.shredzone.feinrip.model.Project;

/**
 * An editable {@link TableModel} for {@link Audio} entities.
 *
 * @author Richard "Shred" Körber
 */
public class AudioTableModel implements SelectionTableModel {

    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private final Project project;
    private final Set<TableModelListener> listener = new HashSet<>();

    /**
     * Creates a new {@link AudioTableModel}.
     *
     * @param project
     *            {@link Project} this model is bound to
     */
    public AudioTableModel(Project project) {
        this.project = project;
    }

    /**
     * Selects all audio tracks.
     */
    @Override
    public void selectAll() {
        project.getAudios().forEach(audio -> audio.setEnabled(true));
        project.touchAudios();
        fireRefresh();
    }

    /**
     * Unselects all audio tracks.
     */
    @Override
    public void unselectAll() {
        project.getAudios().forEach(audio -> audio.setEnabled(false));
        project.touchAudios();
        project.setDefAudio(null);
        fireRefresh();
    }

    @Override
    public int getRowCount() {
        return project.getAudios().size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return B.getString("model.audio.default");
            case 1: return B.getString("model.audio.enabled");
            case 2: return B.getString("model.audio.language");
            case 3: return B.getString("model.audio.type");
            case 4: return B.getString("model.audio.format");
            case 5: return B.getString("model.audio.streamid");
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Boolean.class;
            case 1: return Boolean.class;
            case 2: return Language.class;
            case 3: return AudioType.class;
            case 4: return String.class;
            case 5: return String.class;
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0 || columnIndex == 1 || columnIndex == 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Audio audio = project.getAudios().get(rowIndex);
        Audio defAudio = project.getDefAudio();

        switch (columnIndex) {
            case 0: return defAudio == audio;
            case 1: return audio.isEnabled();
            case 2: return audio.getLanguage();
            case 3: return audio.getType();
            case 4: return audio.getFormat() + ' ' + audio.getChannels() + "ch";
            case 5: return String.format("<html>%s0x%02X%s",
                            (audio.isAvailable() ? "" : "<strike>"),
                            audio.getStreamId(),
                            (audio.isAvailable() ? "" : "</strike>"));
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            if ((Boolean) aValue) {
                Audio audio = project.getAudios().get(rowIndex);
                project.setDefAudio(audio);
                audio.setEnabled(true);
            } else {
                project.setDefAudio(null);
            }

        } else if (columnIndex == 1) {
            Audio audio = project.getAudios().get(rowIndex);
            if ((Boolean) aValue) {
                audio.setEnabled(true);
            } else {
                audio.setEnabled(false);
                Audio defAudio = project.getDefAudio();
                if (audio == defAudio) {
                    project.setDefAudio(null);
                }
            }

        } else if (columnIndex == 2) {
            Audio audio = project.getAudios().get(rowIndex);
            audio.setLanguage((Language) aValue);
        }

        project.touchAudios();
        fireRefresh();
    }

    protected void fireRefresh() {
        TableModelEvent e = new TableModelEvent(this);
        listener.forEach(l -> l.tableChanged(e));
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listener.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listener.remove(l);
    }

}