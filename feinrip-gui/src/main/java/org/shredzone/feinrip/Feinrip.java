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
package org.shredzone.feinrip;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.shredzone.feinrip.database.ImdbDatabase;
import org.shredzone.feinrip.gui.FeinripPane;

/**
 * Main class that starts <i>feinrip</i>.
 *
 * @author Richard "Shred" Körber
 */
public class Feinrip extends JFrame {
    private static final long serialVersionUID = -6575536880238285877L;
    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private final Preferences prefs = Preferences.userNodeForPackage(Feinrip.class);

    public Feinrip() {
        super(B.getString("title"));

        ImdbDatabase.global(); // Initialize database connection

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Point pnt = getLocation();
                prefs.putInt("win.pos.x", pnt.x);
                prefs.putInt("win.pos.y", pnt.y);
                Dimension dim = getSize();
                prefs.putInt("win.size.w", dim.width);
                prefs.putInt("win.size.h", dim.height);
                setVisible(false);
                dispose();
            }
        });

        FeinripPane pane = new FeinripPane();
        getContentPane().add(pane, BorderLayout.CENTER);

        int fw = prefs.getInt("win.size.w", -1);
        int fh = prefs.getInt("win.size.h", -1);
        if (fw >= 0 && fh >= 0) {
            setSize(fw, fh);
        } else {
            pack();
        }

        int px = prefs.getInt("win.pos.x", Integer.MIN_VALUE);
        int py = prefs.getInt("win.pos.y", Integer.MIN_VALUE);
        if (px > Integer.MIN_VALUE && py > Integer.MIN_VALUE) {
            setLocation( px, py );
        } else {
            setLocationRelativeTo( null );
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // Ignore... A different L&F isn't that important.
        }

        final JFrame frame = new Feinrip();

        // Why using invokeLater here? See:
        // http://java.sun.com/developer/JDCTechTips/2003/tt1208.html#1
        EventQueue.invokeLater(() -> frame.setVisible(true));
    }

}
