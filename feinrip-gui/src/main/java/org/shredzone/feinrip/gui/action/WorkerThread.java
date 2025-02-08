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
package org.shredzone.feinrip.gui.action;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;

/**
 * Super class for Swing worker threads.
 * <p>
 * The operation is executed in a separate thread. If used in modal mode, the root pane is
 * blocked (via a glass pane) and a waiting cursor is being shown while processing.
 *
 * @author Richard "Shred" Körber
 */
public abstract class WorkerThread extends Thread {
    private final RootPaneContainer root;

    private boolean executing = false;

    public WorkerThread(JFrame frame, boolean modal) {
        RootPaneContainer r = null;
        if (frame != null && modal) {
            r = (RootPaneContainer) frame.getRootPane().getTopLevelAncestor();
            r.setGlassPane(createBlockerPanel());
        }
        root = r;
    }

    private JComponent createBlockerPanel() {
        JPanel blocker = new JPanel();
        blocker.setOpaque(false);

        MouseAdapter adapter = new MouseAdapter() {};
        blocker.addMouseListener(adapter);
        blocker.addMouseMotionListener(adapter);
        blocker.addMouseWheelListener(adapter);

        return blocker;
    }

    private void lockFrame() {
        if (root != null) {
            root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            root.getGlassPane().setVisible(true);
        }
    }

    private void unlockFrame() {
        if (root != null) {
            root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            root.getGlassPane().setVisible(false);
        }
    }

    @Override
    public synchronized void start() {
        if (executing) {
            throw new IllegalStateException("Action is already being executed!");
        }

        executing = true;
        lockFrame();
        try {
            preAction();
            super.start();
        } catch (RuntimeException ex) {
            // Make sure the frame is always unlocked!
            unlockFrame();
            executing = false;
            throw ex;
        }
    }

    @Override
    public void run() {
        try {
            onAction();
        } finally {
            EventQueue.invokeLater(() -> {
                try {
                    postAction();
                } finally {
                    unlockFrame();
                    executing = false;
                }
            });
        }
    }

    /**
     * Actions that are being executed in the Swing thread before the worker thread is
     * started. For modal threads, the window is already locked.
     * <p>
     * This method needs to be overridden on demand. By default nothing happens.
     */
    protected void preAction() {
        // Does nothing by default
    }

    /**
     * Actions that are being executed in the worker thread. No Swing methods must be
     * invoked here.
     * <p>
     * This method needs to be overridden on demand. By default nothing happens.
     */
    protected void onAction() {
        // Does nothing by default
    }

    /**
     * Actions that are being executed in the Swing thread after the worker thread is
     * completed.
     * <p>
     * This method needs to be overridden on demand. By default nothing happens.
     */
    protected void postAction() {
        // Does nothing by default
    }

}
