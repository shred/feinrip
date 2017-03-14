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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.shredzone.feinrip.database.ImdbService;
import org.shredzone.feinrip.database.OfdbService;
import org.shredzone.feinrip.database.OmdbService;
import org.shredzone.feinrip.gui.ErrorDialog;
import org.shredzone.feinrip.gui.model.SimpleArrayListModel;
import org.shredzone.feinrip.model.Configuration;
import org.shredzone.feinrip.model.Project;

/**
 * Action that queries movie databases with the current title. A choice of the resulting
 * movie titles (along with the production year) is shown to the user.
 *
 * @author Richard "Shred" Körber
 */
public class TitleQueryAction extends AbstractAsyncAction implements PropertyChangeListener, ListSelectionListener {
    private static final long serialVersionUID = 2039193013819372281L;

    private static final int POPUP_WIDTH = 600;
    private static final int POPUP_HEIGHT = 300;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final Icon titleIcon = new ImageIcon(OpenChaptersAction.class.getResource("/org/shredzone/feinrip/icon/find.png"));

    private final Configuration config = Configuration.global();

    private final Project project;
    private List<String> options;
    private JPopupMenu popup;
    private JList<String> pickList;

    public TitleQueryAction(Project project) {
        super(B.getString("action.title.title"), titleIcon);
        setToolTipText(B.getString("action.title.tt"));

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
        String title = project.getTitle().trim();
        title = title.replaceAll("\\(\\d{4}\\)$", "");
        try {
            options = searchTitles(title);
        } catch (IOException ex) {
            ErrorDialog.showException(ex);
            options = Collections.emptyList();
        }
    }

    /**
     * Helper that only invokes the service if enabled is {@code true}.
     *
     * @param enabled
     *            Enabled flag
     * @param service
     *            Service to be invoked
     * @return Either service if enabled, or a callable returning an empty list if
     *         disabled.
     */
    private static Callable<List<String>> ifEnabled(boolean enabled, Callable<List<String>> service) {
        if (enabled) {
            return service;
        } else {
            return Collections::emptyList;
        }
    }

    /**
     * Queries multiple databases for titles and mixes the result into one large list.
     *
     * @param title
     *            Title to search for
     * @return List of potential movie titles
     */
    private List<String> searchTitles(String title) throws IOException {
        ExecutorService exs = Executors.newFixedThreadPool(3);

        Future<List<String>> imdbFuture = exs.submit(ifEnabled(
                        config.isImdbEnabled(), () -> ImdbService.searchTitles(title)));
        Future<List<String>> omdbFuture = exs.submit(ifEnabled(
                        config.isOmdbEnabled(), () -> OmdbService.searchTitles(title)));
        Future<List<String>> ofdbFuture = exs.submit(ifEnabled(
                        config.isOfdbEnabled(), () -> OfdbService.searchTitles(title)));

        Set<String> seen = new TreeSet<>();
        List<String> result = new ArrayList<>();

        try {
            Iterator<String> imdbIt = imdbFuture.get().iterator();
            Iterator<String> omdbIt = omdbFuture.get().iterator();
            Iterator<String> ofdbIt = ofdbFuture.get().iterator();

            while (imdbIt.hasNext() || omdbIt.hasNext() || ofdbIt.hasNext()) {
                if (imdbIt.hasNext()) {
                    String t = imdbIt.next();
                    if (seen.add(t)) {
                        result.add(t);
                    }
                }

                if (omdbIt.hasNext()) {
                    String t = omdbIt.next();
                    if (seen.add(t)) {
                        result.add(t);
                    }
                }

                if (ofdbIt.hasNext()) {
                    String t = ofdbIt.next();
                    if (seen.add(t)) {
                        result.add(t);
                    }
                }
            }
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            }
            // otherwise ignore
        } catch (InterruptedException ex) {
            // ignore
        } finally {
            exs.shutdown();
        }

        return result;
    }

    @Override
    public void postAction(ActionEvent e) {
        final Component src = (Component) e.getSource();

        if (options.isEmpty()) {
            ErrorDialog.showError("action.title.msgtitle", "action.title.nothing");
            return;
        }

        pickList.setModel(new SimpleArrayListModel<>(options));
        popup.show(src, src.getWidth() - POPUP_WIDTH, src.getHeight());

        options = null;
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
        String selection = pickList.getSelectedValue();
        if (selection != null) {
            project.setTitle(selection);
            popup.setVisible(false);
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
