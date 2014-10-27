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
package org.shredzone.feinrip.gui.pane;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.shredzone.feinrip.gui.BorderAndFlowPanel;
import org.shredzone.feinrip.gui.JLabelGroup;
import org.shredzone.feinrip.gui.SimpleFileFilter;
import org.shredzone.feinrip.gui.action.AbstractSyncAction;
import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.model.TargetTemplate;

/**
 * PowerPane for configurating target file name settings.
 *
 * @author Richard "Shred" Körber
 */
@Pane(name = "target", title = "pane.target", icon = "target.png")
public class TargetPane extends PowerPane implements DocumentListener {
    private static final long serialVersionUID = -7660584324877119554L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final Icon selectIcon = new ImageIcon(TargetPane.class.getResource("/org/shredzone/feinrip/icon/file.png"));
    private static final Icon pickIcon = new ImageIcon(TargetPane.class.getResource("/org/shredzone/feinrip/icon/pick.png"));

    private static final String KEY = "lastPath";

    private final Preferences prefs = Preferences.userNodeForPackage(TargetPane.class);

    private JTextField jtfFile;
    private JButton jbSelect;

    public TargetPane(Project project) {
        super(project);
        setup();
        updateBody();

        String file = prefs.get(KEY, "");
        updateTargetFile(file);
        jtfFile.setText(file);
    }

    private void setup() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabelGroup lg = null;

        JPanel jpFile = new BorderAndFlowPanel();
        {
            jtfFile = new JTextField();
            jtfFile.setToolTipText(B.getString("pane.target.keys"));
            jtfFile.addActionListener(this::onFileAction);
            jtfFile.getDocument().addDocumentListener(this);
            jpFile.add(jtfFile, BorderLayout.CENTER);

            jbSelect = new JButton(new FileSelectAction());
            jbSelect.setText("");
            jpFile.add(jbSelect, BorderLayout.EAST);
        }
        add(lg = new JLabelGroup(jpFile, B.getString("pane.target.target"), lg));
        JLabelGroup.setMinimumHeight(lg);

        JPanel jpTemplates = new JPanel(new GridLayout(0, 1));
        jpTemplates.setBorder(BorderFactory.createTitledBorder("Templates"));
        {
            for (TargetTemplate tt : TargetTemplate.values()) {
                JPanel panel = new JPanel(new BorderLayout(3, 0));
                JButton button = new JButton(new TemplateAction(tt));
                button.setText("");
                panel.add(button, BorderLayout.WEST);
                panel.add(new JLabel(tt.toString()), BorderLayout.CENTER);
                panel.add(new JLabel(tt.getPattern()), BorderLayout.EAST);
                jpTemplates.add(panel);
            }
        }
        add(jpTemplates);
        JLabelGroup.setMinimumHeight(jpTemplates);

        lg.rearrange();
    }

    private void updateTargetFile(String path) {
        prefs.put(KEY, path);
        project.setOutput(path);
    }

    private void updateBody() {
        StringBuilder sb = new StringBuilder("<html>");
        if (project.getOutput() != null) {
            String file = project.getOutput();
            int ix = file.lastIndexOf('/');
            if (ix >= 0 && ix < file.length() - 1) {
                file = file.substring(ix + 1);
            }
            file = file.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
            sb.append(file);
        } else {
            sb.append("<i><b>").append(B.getString("pane.target.none")).append("</b></i>");
        }
        getPowerTabModel().setBody(sb.toString());
    }

    private void onFileAction(ActionEvent e) {
        updateTargetFile(jtfFile.getText());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateTargetFile(jtfFile.getText());
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateTargetFile(jtfFile.getText());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateTargetFile(jtfFile.getText());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "output":
                String current = jtfFile.getText();
                String value = evt.getNewValue().toString();
                if (!current.equals(value)) {
                    jtfFile.setText(value);
                }
                updateBody();
                break;
        }
    }

    /**
     * Action for setting a template path.
     */
    private class TemplateAction extends AbstractSyncAction {
        private static final long serialVersionUID = -4843148193926477594L;

        private final TargetTemplate template;

        public TemplateAction(TargetTemplate template) {
            super(B.getString("action.target.pick"), pickIcon);
            this.template = template;
        }

        @Override
        public void onAction(ActionEvent e) {
            String out = project.getOutput();
            int slash = out.lastIndexOf('/');
            if (slash >= 0) {
                out = out.substring(0, slash + 1);
            } else {
                out = "";
            }
            out += template.getPattern();
            project.setOutput(out);
        }
    }

    /**
     * Action for selecting a target file.
     */
    private class FileSelectAction extends AbstractSyncAction {
        private static final long serialVersionUID = -6001332296352193981L;

        public FileSelectAction() {
            super(B.getString("action.target.title"), selectIcon);
            setToolTipText(B.getString("action.target.title.tt"));
        }

        @Override
        public void onAction(ActionEvent e) {
            JFileChooser jfc = new JFileChooser();

            String text = jtfFile.getText();
            if (text != null && !text.isEmpty()) {
                jfc.setSelectedFile(new File(text));
            }

            jfc.setDialogTitle(B.getString("action.target.select"));
            jfc.setDialogType(JFileChooser.SAVE_DIALOG);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setFileFilter(new SimpleFileFilter("mkv"));
            int result = jfc.showSaveDialog(TargetPane.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String path = jfc.getSelectedFile().getAbsolutePath();
                jtfFile.setText(path);
                updateTargetFile(path);
            }
        }
    }

}
