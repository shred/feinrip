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
package org.shredzone.feinrip.gui.model;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.shredzone.feinrip.model.Language;
import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.model.Subtitle;
import org.shredzone.feinrip.model.SubtitleFormat;
import org.shredzone.feinrip.model.SubtitleType;

/**
 * An editable {@link TableModel} for {@link Subtitle} entities.
 *
 * @author Richard "Shred" Körber
 */
public class SubTableModel implements SelectionTableModel {

    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private final Project project;
    private final Set<TableModelListener> listener = new HashSet<>();

    /**
     * Creates a new {@link SubTableModel}.
     *
     * @param project
     *            {@link Project} this model is bound to
     */
    public SubTableModel(Project project) {
        this.project = project;
    }

    /**
     * Selects all subtitles.
     */
    @Override
    public void selectAll() {
        project.getSubs().forEach(sub -> sub.setEnabled(true));
        project.touchSubs();
        fireRefresh();
    }

    /**
     * Unselects all subtitles.
     */
    @Override
    public void unselectAll() {
        project.getSubs().forEach(sub -> sub.setEnabled(false));
        project.touchSubs();
        project.setDefSub(null);
        fireRefresh();
    }

    @Override
    public int getRowCount() {
        return project.getSubs().size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return B.getString("model.sub.default");
            case 1: return B.getString("model.sub.enabled");
            case 2: return B.getString("model.sub.language");
            case 3: return B.getString("model.sub.type");
            case 4: return B.getString("model.sub.format");
            case 5: return B.getString("model.sub.streamid");
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Boolean.class;
            case 1: return Boolean.class;
            case 2: return Language.class;
            case 3: return SubtitleType.class;
            case 4: return SubtitleFormat.class;
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
        Subtitle sub = project.getSubs().get(rowIndex);
        Subtitle defSub = project.getDefSub();

        switch (columnIndex) {
            case 0: return defSub == sub;
            case 1: return sub.isEnabled();
            case 2: return sub.getLanguage();
            case 3: return sub.getType();
            case 4: return sub.getFormat();
            case 5: return sub.getStreamId();
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            if ((Boolean) aValue) {
                Subtitle sub = project.getSubs().get(rowIndex);
                project.setDefSub(sub);
                sub.setEnabled(true);
            } else {
                project.setDefSub(null);
            }

        } else if (columnIndex == 1) {
            Subtitle sub = project.getSubs().get(rowIndex);
            if ((Boolean) aValue) {
                sub.setEnabled(true);
            } else {
                sub.setEnabled(false);
                Subtitle defSub = project.getDefSub();
                if (sub == defSub) {
                    project.setDefSub(null);
                }
            }

        } else if (columnIndex == 2) {
            Subtitle sub = project.getSubs().get(rowIndex);
            sub.setLanguage((Language) aValue);
        }

        project.touchSubs();
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