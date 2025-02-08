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

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;

/**
 * This {@link DefaultListSelectionModel} also allows that no item can be selected at all.
 * Use this selection model if you want to disable a {@link JList}, but keep the content
 * readable. Besides allowing a fourth selection mode {@code NO_SELECTION}, it completely
 * behaves like the {@link DefaultListSelectionModel}.
 *
 * @author Richard "Shred" Körber
 */
public class NoListSelectionModel extends DefaultListSelectionModel {
    private static final long serialVersionUID = 393116339432230821L;

    public static final int NO_SELECTION = 9181;

    private boolean noselect = false;

    @Override
    public void setSelectionInterval(int index0, int index1) {
        if (!noselect) {
            super.setSelectionInterval(index0, index1);
        }
    }

    @Override
    public void addSelectionInterval(int index0, int index1) {
        if (!noselect) {
            super.addSelectionInterval(index0, index1);
        }
    }

    @Override
    public void insertIndexInterval(int index, int length, boolean before) {
        if (!noselect) {
            super.insertIndexInterval(index, length, before);
        }
    }

    @Override
    public void setSelectionMode(int selectionMode) {
        if (selectionMode == NO_SELECTION) {
            super.setSelectionMode(SINGLE_SELECTION);
            clearSelection();
            noselect = true;
        } else {
            noselect = false;
            super.setSelectionMode(selectionMode);
        }
    }

    @Override
    public int getSelectionMode() {
        if (noselect) {
            return NO_SELECTION;
        } else {
            return super.getSelectionMode();
        }
    }

}
