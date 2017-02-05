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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.shredzone.feinrip.gui.JToolbarButton;
import org.shredzone.feinrip.gui.NoListSelectionModel;
import org.shredzone.feinrip.gui.action.OpenChaptersAction;
import org.shredzone.feinrip.model.Chapter;
import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.model.Track;
import org.shredzone.feinrip.source.DvdSource;
import org.shredzone.feinrip.source.Source;

/**
 * PowerPane for configurating chapter settings.
 *
 * @author Richard "Shred" Körber
 */
@Pane(name = "chapter", title = "pane.chapter", icon = "chapter.png")
public class ChapterPane extends PowerPane {
    private static final long serialVersionUID = -9076194361885838577L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private JScrollPane jScroll;
    private JPanel jpChapters;
    private JCheckBox jcbIgnore;
    private HashMap<JTextField, ChapterEditListener> listenerMap = new HashMap<>();
    private boolean noChapterUpdates;

    public ChapterPane(Project project) {
        super(project);
        setup();
        updateBody();
    }

    private void setup() {
        setLayout(new BorderLayout());

        JToolBar jtbTools = new JToolBar();
        jtbTools.setFloatable(false);
        {
            jcbIgnore = new JCheckBox(B.getString("pane.chapter.enabled"));
            jcbIgnore.setSelected(true);
            jcbIgnore.addActionListener(this::onIgnoreAction);
            jtbTools.add(jcbIgnore);

            jtbTools.addSeparator();

            jtbTools.add(new JToolbarButton(new OpenChaptersAction(project), true));
        }
        add(jtbTools, BorderLayout.NORTH);

        ListSelectionModel selModel = new NoListSelectionModel();
        selModel.setSelectionMode(NoListSelectionModel.NO_SELECTION);

        jpChapters = new JPanel(new BorderLayout());
        jScroll = new JScrollPane(jpChapters);
        jScroll.getVerticalScrollBar().setUnitIncrement(15);
        add(jScroll, BorderLayout.CENTER);
    }

    private void updateSource() {
        updateChapters();
        jcbIgnore.setSelected(!project.isIgnoreChapters());
    }

    private void updateChapters() {
        listenerMap.forEach((textfield, l) -> {
            textfield.removeFocusListener(l);
            textfield.getDocument().removeDocumentListener(l);
        });
        listenerMap.clear();

        jpChapters.removeAll();
        jpChapters.add(buildChapterPane(project.getChapters()), BorderLayout.CENTER);
        jScroll.validate();
    }

    private JPanel buildChapterPane(List<Chapter> chapters) {
        final JPanel jpResult = new JPanel(new GridBagLayout());
        jpResult.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
        GridBagConstraints c1 = new GridBagConstraints(0, 0, 1, 1, 0.0d, 0.0d, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 5), 0, 0);
        GridBagConstraints c2 = new GridBagConstraints(1, 0, 1, 1, 1.0d, 0.0d, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
        GridBagConstraints c3 = new GridBagConstraints(2, 0, 1, 1, 0.0d, 0.0d, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 2), 0, 0);

        chapters.forEach(ch -> {
            JLabel jlCount = new JLabel(String.valueOf(ch.getNumber()) + ":", JLabel.RIGHT);
            jpResult.add(jlCount, c1);

            JTextField jtfName = new JTextField(ch.getTitle());
            ChapterEditListener listener = new ChapterEditListener(project, jpResult, ch);
            jtfName.addFocusListener(listener);
            jtfName.getDocument().addDocumentListener(listener);
            listenerMap.put(jtfName, listener);
            jpResult.add(jtfName, c2);

            JLabel jlTime = new JLabel(ch.getPosition());
            jpResult.add(jlTime, c3);

            c1.gridy++;
            c2.gridy++;
            c3.gridy++;
        });

        return jpResult;
    }

    private void updateBody() {
        StringBuilder sb = new StringBuilder("<html>");
        if (project.isIgnoreChapters()) {
            sb.append("<i>").append(B.getString("pane.chapter.disabled")).append("</i>");
        } else {
            int chapters = project.getChapters().size();
            int annex = 0;

            if (project.getSource() instanceof DvdSource) {
                // Evaluate number of chapters and annexes on DVD source
                DvdSource src = (DvdSource) project.getSource();
                Track track = src.getSelectedTrack();
                if (track != null) {
                    annex = Math.max(0, chapters - track.getChapters());
                    chapters = track.getChapters();
                }
            }

            sb.append(MessageFormat.format(B.getString("pane.chapter.chapters"), chapters, annex));
        }
        getPowerTabModel().setBody(sb.toString());
    }

    private void onIgnoreAction(ActionEvent e) {
        project.setIgnoreChapters(!jcbIgnore.isSelected());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "chapters": //NOSONAR: falls through
                if (!noChapterUpdates) {
                    updateChapters();
                }
                // falls through...

            case "ignoreChapters":
                updateBody();
                break;

            case "source":
                updateSource();
                break;
        }

        if (evt.getSource() instanceof Source) {
            updateSource();
        }
    }

    private class ChapterEditListener implements DocumentListener, FocusListener {
        private final Project project;
        private final JComponent parent;
        private final Chapter chapter;

        public ChapterEditListener(Project project, JComponent parent, Chapter chapter) {
            this.project = project;
            this.parent = parent;
            this.chapter = chapter;
        }

        private void update(Document doc) {
            try {
                chapter.setTitle(doc.getText(0, doc.getLength()));
            } catch (BadLocationException ex) {
                // Ignore, we cannot do anything about it...
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update(e.getDocument());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update(e.getDocument());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update(e.getDocument());
        }

        @Override
        public void focusLost(FocusEvent e) {
            Object src = e.getSource();
            if (src instanceof JTextField) {
                chapter.setTitle(((JTextField) src).getText());
                noChapterUpdates = true;
                project.touchChapters();
                noChapterUpdates = false;
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            JComponent focused = (JComponent) e.getSource();
            parent.scrollRectToVisible(focused.getBounds());
            if (focused instanceof JTextField) {
                ((JTextField) focused).selectAll();
            }
        }
    }

}
