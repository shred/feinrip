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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 * A simple {@link ListModel} that contains an array. The array contents must not change,
 * because the connected lists are not notified about changes.
 *
 * @author Richard "Shred" Körber
 *
 * @param <T> Array type
 */
public class SimpleArrayListModel<T> implements ListModel<T>, Serializable {
    private static final long serialVersionUID = -3502533710581734541L;

    private final List<T> data;

    /**
     * Creates a new {@link SimpleArrayListModel}.
     *
     * @param data
     *            Array to be used as data. Array contents must not change.
     */
    public SimpleArrayListModel(T[] data) {
        this.data = Arrays.asList(data);
    }

    public SimpleArrayListModel(List<T> list) {
        this.data = list;
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public T getElementAt(int index) {
        return data.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        // Array and its contents won't change by policy, so no need to track listeners
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        // Array and its contents won't change by policy, so no need to track listeners
    }

}
