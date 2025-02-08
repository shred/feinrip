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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A model containing data for a single PowerTab tab.
 *
 * @author Richard "Shred" Körber
 */
public class PowerTabModel implements Serializable {
    private static final long serialVersionUID = 6090146237287651462L;

    private ImageIcon icon;
    private String title;
    private String body;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        ImageIcon old = this.icon;
        this.icon = icon;
        support.firePropertyChange("icon", old, icon);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        String old = this.title;
        this.title = title;
        support.firePropertyChange("title", old, title);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        String old = this.body;
        this.body = body;
        support.firePropertyChange("body", old, body);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

}
