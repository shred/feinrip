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
package org.shredzone.feinrip.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.HashMap;

import javax.swing.JPanel;

/**
 * A convenience {@link JPanel} that sets up a {@link BorderLayout} in the {@code CENTER},
 * but {@link FlowLayout} on the other positions of the {@link BorderLayout}.
 * <p>
 * Use this to easily create text fields or comboboxes in the center, and buttons (like
 * file browsers) on the sides.
 *
 * @author Richard "Shred" Körber
 */
public class BorderAndFlowPanel extends JPanel {
    private static final long serialVersionUID = 8292863708014614152L;

    private final HashMap<Object, JPanel> panelMap = new HashMap<>();

    public BorderAndFlowPanel() {
        super(new BorderLayout());
    }

    @Override
    public void add(Component comp, Object constraints) {
        if (BorderLayout.CENTER.equals(constraints)) {
            super.add(comp, constraints);
            return;
        }

        JPanel flowPanel = panelMap.get(constraints);
        if (flowPanel == null) {
            flowPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
            super.add(flowPanel, constraints);
            panelMap.put(constraints, flowPanel);
        }
        flowPanel.add(comp);
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        if (BorderLayout.CENTER.equals(constraints)) {
            super.add(comp, constraints, index);
            return;
        }

        JPanel flowPanel = panelMap.get(constraints);
        if (flowPanel == null) {
            flowPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
            super.add(flowPanel, constraints);
            panelMap.put(constraints, flowPanel);
        }
        flowPanel.add(comp, index);
    }

}
