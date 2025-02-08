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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * A JLabelGroup is a {@link JLabel} set left to a {@link Component}.
 *
 * @author Richard "Shred" Körber
 */
public class JLabelGroup extends JPanel {
    private static final long serialVersionUID = 3381504646372204060L;

    private Component comp;
    private JComponent label;
    private JLabelGroup pred;

    /**
     * Utility method to set the height of a {@link JComponent} to its minimum.
     */
    public static void setMinimumHeight(JComponent comp) {
        int height = comp.getMinimumSize().height;
        comp.setMaximumSize(new Dimension(comp.getMaximumSize().width, height));
        comp.setPreferredSize(new Dimension(comp.getPreferredSize().width, height));
    }

    /**
     * Creates the first JLabelGroup of a chain.
     *
     * @param c
     *            {@link Component} to be labelled
     * @param text
     *            Label text
     */
    public JLabelGroup(Component c, String text) {
        this(c, text, null);
    }

    /**
     * Creates a new JLabelGroup element.
     *
     * @param c
     *            {@link Component} to be labelled
     * @param text
     *            Label text
     * @param pred
     *            Predecessor {@link JLabelGroup} instance, or {@code null}
     */
    public JLabelGroup(Component c, String text, JLabelGroup pred) {
        this(c, new JLabel(text), pred);
    }

    /**
     * Creates a new JLabelGroup label with a given label {@link JComponent}. Use this if
     * you want to use a different label.
     *
     * @param c
     *            {@link Component} to be labelled
     * @param label
     *            Label {@link JComponent}
     * @param pred
     *            Predecessor {@link JLabelGroup} instance, or {@code null}
     */
    public JLabelGroup(Component c, JComponent label, JLabelGroup pred) {
        this.comp = c;
        this.label = label;
        this.pred = pred;

        setVerticalAlignment(SwingConstants.CENTER);
        if (label instanceof JLabel) {
            ((JLabel) label).setLabelFor(comp);
        }

        setLayout(new BorderLayout());
        add(label, BorderLayout.LINE_START);
        add(comp, BorderLayout.CENTER);
    }

    /**
     * Calculates the maximum label with of a {@link JLabelGroup} chain.
     *
     * @return Maximum width
     */
    protected int getMaximumWidth() {
        if (pred != null) {
            return Math.max(label.getMinimumSize().width, pred.getMaximumWidth());
        } else {
            return label.getMinimumSize().width;
        }
    }

    /**
     * Sets the vertical alignment of the label, using {@link SwingConstants}. Default is
     * {@link SwingConstants#CENTER}.
     */
    public void setVerticalAlignment(int pos) {
        Border border;

        switch (pos) {
            case SwingConstants.TOP:
                border = new EmptyBorder(1, 0, 0, 5);
                break;

            case SwingConstants.BOTTOM:
                border = new EmptyBorder(0, 0, 1, 5);
                break;

            default:
                border = new EmptyBorder(0, 0, 0, 5);
        }

        label.setBorder(border);

        if (label instanceof JLabel) {
            ((JLabel) label).setVerticalAlignment(pos);
        }
    }

    /**
     * Recursively set the minimum width of this {@link JLabelGroup} chain. This method
     * must be invoked on the <em>last</em> {@link JLabelGroup} of the chain.
     *
     * @param width
     *            New minimum width
     */
    protected void setMinimumWidth(int width) {
        Dimension dim = new Dimension(width, label.getMinimumSize().height);
        label.setMinimumSize(dim);
        label.setPreferredSize(dim);
        if (pred != null) pred.setMinimumWidth(width);
        invalidate();
    }

    /**
     * Rearrange the {@link JLabelGroup} chain. All labels in this chain are set to the
     * width of the broadest label. This method must be invoked on the <em>last</em>
     * {@link JLabelGroup} of a chain!
     */
    public void rearrange() {
        setMinimumWidth(getMaximumWidth());
    }

}
