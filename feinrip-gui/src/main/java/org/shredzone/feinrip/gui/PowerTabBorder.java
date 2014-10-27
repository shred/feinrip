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

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import javax.swing.border.EmptyBorder;

/**
 * Renders the border of a powertab.
 *
 * @author Richard "Shred" Körber
 */
public class PowerTabBorder extends EmptyBorder {
    private static final long serialVersionUID = -5029790123112279094L;

    private static final int BORDER_WIDTH = 20;
    private static final int SHADOW_WIDTH = 6;

    private static final Color BACKGROUND = new Color(0xC0_FF_80);
    private static final Color BACKGROUND_DARK = new Color(0x9C_BF_78);
    private static final Color LINE = new Color(0x68_A6_2A);
    private static final Color SHADOW = new Color(0x60_00_00_00, true);
    private static final Color TRANSPARENT = new Color(0x00_FF_FF_FF, true);

    private boolean selected = false;

    public PowerTabBorder() {
        super(3, BORDER_WIDTH, 3, 3);
    }

    public void setSelected(boolean selected)   { this.selected = selected; }
    public boolean isSelected()                 { return selected; }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x, y, width, height);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

        int left = x + BORDER_WIDTH - SHADOW_WIDTH;

        if (selected) {
            int right = x + width;
            int bottom = y + height - 1;
            int half = y + (height / 2);

            Polygon p = new Polygon();
            p.addPoint(left - 1, y);
            p.addPoint(right, y);
            p.addPoint(right, bottom);
            p.addPoint(left - 1, bottom);
            p.addPoint(3, half);
            g2.setColor(BACKGROUND);
            g2.fillPolygon(p);
            g2.setColor(LINE);
            g2.drawPolygon(p);

        } else {
            g2.setColor(BACKGROUND_DARK);
            g2.fillRect(left, y, width - left, y + height);

            g2.setColor(LINE);
            g2.drawLine(left - 1, y, left - 1, y + height);

            GradientPaint gp = new GradientPaint(
                            left, 0f, SHADOW,
                            BORDER_WIDTH, 0f, TRANSPARENT);
            g2.setPaint(gp);
            g2.fillRect(left, y, SHADOW_WIDTH, height);
        }

        g2.dispose();
    }

}
