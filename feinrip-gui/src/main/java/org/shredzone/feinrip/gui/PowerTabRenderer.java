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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.shredzone.feinrip.gui.model.PowerTabModel;

/**
 * A renderer class for a single power tab.
 *
 * @author Richard "Shred" Körber
 */
public class PowerTabRenderer extends JPanel implements PropertyChangeListener {
    private static final long serialVersionUID = -53372996432237750L;

    private PowerTabModel model;
    private PowerTabBorder border;
    private JLabel jlTitle;
    private JLabel jlBody;
    private HashSet<ActionListener> listener = new HashSet<>();

    /**
     * Creates a new {@link PowerTabRenderer}.
     *
     * @param model
     *            {@link PowerTabModel} to be rendered
     */
    public PowerTabRenderer(PowerTabModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        border = new PowerTabBorder();
        setBorder(border);

        jlTitle = new JLabel(model.getTitle());
        jlTitle.setIcon(model.getIcon());
        jlTitle.setFont(jlTitle.getFont().deriveFont(Font.BOLD));
        add(jlTitle, BorderLayout.NORTH);

        jlBody = new JLabel(model.getBody());
        jlBody.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        jlBody.setFont(jlBody.getFont().deriveFont(Font.PLAIN, 8.5f));
        add(jlBody, BorderLayout.CENTER);

        addMouseListener(new PowerMouseAdapter());
    }

    /**
     * Sets whether the power tab is to be rendered in selection state, or not.
     *
     * @param selected
     *            {@code true}: power tab is rendered in selection state
     */
    public void setSelected(boolean selected) {
        border.setSelected(selected);
        repaint();
    }

    /**
     * Returns if the power tab is currently selected.
     */
    public boolean isSelected() {
        return border.isSelected();
    }

    /**
     * Adds an {@link ActionListener} that is notified when the power tab was selected.
     *
     * @param l
     *            {@link ActionListener} to be added
     */
    public void addActionListener(ActionListener l) {
        listener.add(l);
    }

    /**
     * Removes an {@link ActionListener}.
     *
     * @param l
     *            {@link ActionListener} to be removed
     */
    public void removeActionListener(ActionListener l) {
        listener.remove(l);
    }

    /**
     * Notifies all registered {@link ActionListener} that the power tab was selected.
     */
    protected void fireActionListener() {
        ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "click");
        for (ActionListener l : listener) {
            l.actionPerformed(e);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "title":
                jlTitle.setText(model.getTitle());
                break;

            case "icon":
                jlTitle.setIcon(model.getIcon());
                break;

            case "body":
                jlBody.setText(model.getBody());
                break;
        }
    }

    /**
     * Notifies that the power tab was selected when the mouse pointer was clicked on the
     * tab, or the mouse pointer entered the area of the power tab while being pressed.
     */
    private class PowerMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                fireActionListener();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
                fireActionListener();
            }
        }
    }

}
