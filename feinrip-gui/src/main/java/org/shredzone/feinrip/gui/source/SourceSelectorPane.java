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
package org.shredzone.feinrip.gui.source;

import javax.swing.JPanel;

import org.shredzone.feinrip.source.Source;

/**
 * Abstract class for all source selector panes.
 *
 * @author Richard "Shred" Körber
 */
public abstract class SourceSelectorPane extends JPanel {
    private static final long serialVersionUID = -3857604053220548271L;

    /**
     * Gets the {@link Source} that is configured by this pane.
     */
    public abstract Source getSource();

}
