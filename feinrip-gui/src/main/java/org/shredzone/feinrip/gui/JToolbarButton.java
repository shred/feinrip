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

import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JButton;

/**
 * This {@link JButton} is to be used in toolbars. It takes care that the button never has
 * a text, never gets the focus and has no borders.
 *
 * @author Richard "Shred" Körber
 */
public class JToolbarButton extends JButton {
    private static final long serialVersionUID = -4420919451785717946L;

    /**
     * Creates a new {@link JToolbarButton} for a certain {@link Action}. The
     * {@link Action}'s text is not displayed.
     *
     * @param a
     *            {@link Action}
     */
    public JToolbarButton(Action a) {
        this(a, false);
    }

    /**
     * Creates a new {@link JToolbarButton} for a certain {@link Action}.
     *
     * @param a
     *            {@link Action}
     * @param show
     *            {@code true}: Show the action text.
     */
    public JToolbarButton(Action a, boolean show) {
        super(a);
        setRequestFocusEnabled(false);
        setFocusable(false);
        setMargin(new Insets(0, 0, 0, 0));
        setContentAreaFilled(false);
        if (!show) {
            setText("");
        }
    }

}

