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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.shredzone.feinrip.gui.ErrorDialog;
import org.shredzone.feinrip.gui.SimpleFileFilter;
import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.system.ChapterUtils;

/**
 * Action that asks the user for a chapter file. If the user chooses a file, it is
 * parsed, and the current chapters are replaced.
 *
 * @author Richard "Shred" Körber
 */
public class OpenChaptersAction extends AbstractAsyncAction {
    private static final long serialVersionUID = -496049816622013044L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final Icon fileIcon = new ImageIcon(OpenChaptersAction.class.getResource("/org/shredzone/feinrip/icon/file.png"));

    private static final String KEY = "lastPath";

    private final Preferences prefs = Preferences.userNodeForPackage(OpenChaptersAction.class);
    private final Project project;

    private File file;

    public OpenChaptersAction(Project project) {
        super(B.getString("action.chap.title"), fileIcon);
        this.project = project;
    }

    @Override
    public void preAction(ActionEvent e) {
        Component src = (Component) e.getSource();

        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle(B.getString("action.chap.dialogtitle"));
        jfc.setDialogType(JFileChooser.OPEN_DIALOG);
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setFileFilter(new SimpleFileFilter("chap"));
        jfc.setCurrentDirectory(new File(prefs.get(KEY, "")));
        int result = jfc.showOpenDialog(src);
        if (result == JFileChooser.APPROVE_OPTION) {
            file = jfc.getSelectedFile();
            prefs.put(KEY, file.getParentFile().getAbsolutePath());
        }
    }

    @Override
    public void onAction(ActionEvent e) {
        if (file != null) {
            try {
                project.setChapters(ChapterUtils.readChapters(file));
            } catch (IOException ex) {
                ErrorDialog.showException(ex);
            }
        }
    }

    @Override
    public void postAction(ActionEvent e) {
        file = null;
    }

}
