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
package org.shredzone.feinrip.gui.action;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.shredzone.feinrip.FeinripProcessor;
import org.shredzone.feinrip.gui.ErrorDialog;
import org.shredzone.feinrip.gui.FeinripPane;
import org.shredzone.feinrip.gui.pane.ProgressPane;
import org.shredzone.feinrip.model.Configuration;
import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.source.Source;

/**
 * Action for processing the sources based on the Project, and generating the mkv file.
 *
 * @author Richard "Shred" Körber
 */
public class StartAction extends AbstractAsyncAction implements PropertyChangeListener, Runnable {
    private static final long serialVersionUID = 7368475121002513675L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final Icon playIcon = new ImageIcon(StartAction.class.getResource("/org/shredzone/feinrip/icon/play.png"));

    private final Configuration config = Configuration.global();
    private final Project project;
    private final FeinripPane master;
    private final ProgressPane progress;

    public StartAction(Project project, FeinripPane master, ProgressPane progress) {
        super(B.getString("action.start"), playIcon);
        setModal(false);
        this.project = project;
        this.master = master;
        this.progress = progress;
        project.addPropertyChangeListener(this);
        updateEnabled();
    }

    private void updateEnabled() {
        if (project.getSource() == null || !project.getSource().isValid()) {
            setEnabled(false);
            return;
        }

        if (project.getOutput() == null || project.getOutput().isEmpty()) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "output":
            case "source":
                updateEnabled();
        }

        if (evt.getSource() instanceof Source) {
            updateEnabled();
        }
    }

    @Override
    public void preAction(ActionEvent e) {
        project.setProcessing(true);
        progress.setFrame(getFrame(e));
        setEnabled(false);
        master.showProgressDialog();
    }

    @Override
    public void onAction(ActionEvent e) {
        try {
            FeinripProcessor processor = new FeinripProcessor(project);
            processor.setPreMuxHook(this);
            processor.setProgressMeter(progress);
            processor.start();
        } catch (Exception ex) {
            ErrorDialog.showException(ex);
        }
    }

    @Override
    public void postAction(ActionEvent e) {
        master.hideProgressDialog();
        progress.cleanup();
        progress.setFrame(null);
        updateEnabled();
        project.setProcessing(false);
    }

    /**
     * Invoked just before muxing the mkv file. Note that this is not executed in the
     * Swing GUI thread!
     */
    @Override
    public void run() {
        if (config.isHoldBeforeMuxing()) {
            try {
                EventQueue.invokeAndWait(() -> {
                    int result = JOptionPane.showConfirmDialog(
                            master,
                            B.getString("action.start.hold.dialog"),
                            B.getString("action.start.hold.title"),
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    if (result != JOptionPane.OK_OPTION) {
                        throw new RuntimeException("Cancelled on user request");
                    }
                });
            } catch (InvocationTargetException|InterruptedException ex) {
                throw new RuntimeException("Muxing was aborted!", ex);
            }
        }
    }

}
