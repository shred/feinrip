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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.shredzone.feinrip.database.TvdbService;
import org.shredzone.feinrip.database.TvdbService.TvdbEpisode;
import org.shredzone.feinrip.database.TvdbService.TvdbSeries;
import org.shredzone.feinrip.gui.ErrorDialog;
import org.shredzone.feinrip.gui.model.SimpleArrayListModel;
import org.shredzone.feinrip.model.Project;

/**
 * Action that queries the TVDB by the title name and offers a selection of matching TV
 * series to the user. If the user picks a series, the episodes are downloaded and set up
 * in the Project model.
 *
 * @author Richard "Shred" Körber
 */
public class TvdbQueryAction extends AbstractAsyncAction implements PropertyChangeListener, ListSelectionListener {
    private static final long serialVersionUID = -5601680508716350160L;

    private static final int POPUP_WIDTH = 600;
    private static final int POPUP_HEIGHT = 300;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final Icon tvdbIcon = new ImageIcon(TvdbQueryAction.class.getResource("/org/shredzone/feinrip/icon/find.png"));

    private final Project project;

    private List<TvdbSeries> series;
    private JPopupMenu popup;
    private JList<TvdbSeries> pickList;

    public TvdbQueryAction(Project project) {
        super(B.getString("action.tvdb.title"), tvdbIcon);
        setToolTipText(B.getString("action.tvdb.tt"));

        this.project = project;
        project.addPropertyChangeListener(this);

        setupPopup();

        updateEnabled();
    }

    private void updateEnabled() {
        setEnabled(project.getTitle() != null && !project.getTitle().trim().isEmpty());
    }

    @Override
    public void onAction(ActionEvent e) {
        try {
            series = TvdbService.findSeries(project.getTitle().trim());
        } catch (IOException ex) {
            // Silently ignore the exception and return an empty list
            series = Collections.emptyList();
        }
    }

    @Override
    public void postAction(ActionEvent e) {
        final Component src = (Component) e.getSource();

        if (series.isEmpty()) {
            ErrorDialog.showError("action.tvdb.msgtitle", "action.tvdb.nothing");
            project.setEpisodes(null);
            project.setEpisode(null);
            return;
        }

        pickList.setModel(new SimpleArrayListModel<>(series));
        popup.show(src, src.getWidth() - POPUP_WIDTH, src.getHeight());

        series = null;
    }

    protected void setupPopup() {
        pickList = new JList<>();
        pickList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pickList.addListSelectionListener(this);

        JScrollPane scrollPane = new JScrollPane(pickList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        popup.add(scrollPane);
        popup.setPopupSize(POPUP_WIDTH, POPUP_HEIGHT);
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        final TvdbSeries selection = pickList.getSelectedValue();
        if (selection != null) {
            // Change the title
            StringBuilder sb = new StringBuilder();
            sb.append(selection.title).append(" (");
            sb.append(selection.aired.substring(0, 4));
            sb.append(")");
            project.setTitle(sb.toString());

            // Fetch list of episodes
            WorkerThread thread = new WorkerThread((JFrame) getFrame(evt), true) {
                private List<TvdbEpisode> episodes;

                @Override
                protected void preAction() {
                    popup.setVisible(false);
                }

                @Override
                protected void onAction() {
                    try {
                        episodes = TvdbService.findEpisodes(selection);
                    } catch (IOException ex) {
                        episodes = Collections.emptyList();
                    }
                }

                @Override
                protected void postAction() {
                    if (!episodes.isEmpty()) {
                        project.setEpisodes(episodes);
                        project.setEpisode(episodes.get(0));
                    } else {
                        project.setEpisodes(null);
                        project.setEpisode(null);
                    }
                }
            };
            thread.start();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "title":
                updateEnabled();
        }
    }

}
