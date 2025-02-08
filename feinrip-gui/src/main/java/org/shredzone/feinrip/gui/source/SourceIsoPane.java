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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.shredzone.feinrip.gui.BorderAndFlowPanel;
import org.shredzone.feinrip.gui.JLabelGroup;
import org.shredzone.feinrip.gui.JTrackList;
import org.shredzone.feinrip.gui.action.AbstractSyncAction;
import org.shredzone.feinrip.model.MountPoint;
import org.shredzone.feinrip.source.IsoSource;
import org.shredzone.feinrip.source.Source;

/**
 * A source configuration panel for DVD iso directories.
 *
 * @author Richard "Shred" Körber
 */
public class SourceIsoPane extends SourceSelectorPane {
    private static final long serialVersionUID = 6853299422072887626L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final Icon selectIcon = new ImageIcon(SourceIsoPane.class.getResource("/org/shredzone/feinrip/icon/file.png"));
    private static final String KEY = "lastIsoDir";

    private final Preferences prefs = Preferences.userNodeForPackage(SourceIsoPane.class);
    private final IsoSource source = new IsoSource();

    private JTextField jtfDir;
    private JTrackList jlTrack;

    public SourceIsoPane() {
        setup();
    }

    private void setup() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabelGroup lg = null;

        JPanel jpSource = new JPanel(new BorderLayout());
        jpSource.setBorder(BorderFactory.createTitledBorder(B.getString("source.iso.directory")));
        {
            JPanel jpFile = new BorderAndFlowPanel();
            {
                jtfDir = new JTextField();
                jtfDir.addActionListener(this::onDirAction);
                jpFile.add(jtfDir, BorderLayout.CENTER);

                JButton jbDirSelect = new JButton(new DirSelectAction());
                jbDirSelect.setText("");
                jpFile.add(jbDirSelect, BorderLayout.LINE_END);
            }
            jpSource.add(lg = new JLabelGroup(jpFile, B.getString("source.iso.dir"), lg));
        }
        add(jpSource);
        JLabelGroup.setMinimumHeight(jpSource);

        add(Box.createVerticalStrut(3));

        JPanel jpTrack = new JPanel(new BorderLayout());
        jpTrack.setBorder(BorderFactory.createTitledBorder(B.getString("source.iso.track")));
        {
            jlTrack = new JTrackList();
            jpTrack.add(new JScrollPane(jlTrack), BorderLayout.CENTER);
        }
        add(jpTrack);
        add(Box.createVerticalStrut(3));

        lg.rearrange();
    }

    @Override
    public Source getSource() {
        return source;
    }

    private void selectDirectory(File file) {
        if (file != null) {
            jtfDir.setText(file.getAbsolutePath());
            prefs.put(KEY, file.getParentFile().getAbsolutePath());

            source.setMountPoint(file);
            jlTrack.setSource(source);
        } else {
            jlTrack.setSource(null);
            source.setMountPoint((MountPoint) null);
        }
    }

    private void onDirAction(ActionEvent e) {
        File dir = new File(jtfDir.getText());
        if (dir.exists() && dir.isDirectory()) {
            selectDirectory(dir);
        }
    }

    /**
     * Action for selecting an iso directory.
     */
    private class DirSelectAction extends AbstractSyncAction {
        private static final long serialVersionUID = -7741031192975942044L;

        public DirSelectAction() {
            super(B.getString("action.isodir.title"), selectIcon);
        }

        @Override
        public void onAction(ActionEvent e) {
            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle(B.getString("action.isodir.dialog"));
            jfc.setDialogType(JFileChooser.OPEN_DIALOG);
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jfc.setCurrentDirectory(new File(prefs.get(KEY, "")));
            int result = jfc.showOpenDialog(SourceIsoPane.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selected = jfc.getSelectedFile();
                selectDirectory(selected);
            }
        }
    }

}
