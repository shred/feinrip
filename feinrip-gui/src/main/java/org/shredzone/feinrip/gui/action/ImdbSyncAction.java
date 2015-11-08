/*
 * feinrip
 *
 * Copyright (C) 2015 Richard "Shred" Körber
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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.zip.GZIPInputStream;

import org.shredzone.feinrip.database.ImdbDatabase;
import org.shredzone.feinrip.gui.ErrorDialog;
import org.shredzone.feinrip.model.Configuration;

/**
 * Synchronize the IMDb database. It downloads the necessary files from the IMDb
 * database file server, unpacks them and synchronizes them with the local movie
 * title database.
 *
 * @author Richard "Shred" Körber
 */
public class ImdbSyncAction extends AbstractAsyncAction {
    private static final long serialVersionUID = 9029110213144687640L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private final Configuration config = Configuration.global();
    private final ImdbDatabase database = ImdbDatabase.global();

    public ImdbSyncAction() {
        super(B.getString("action.imdbsync"), null);
        setToolTipText(B.getString("action.imdbsync.tt"));
    }

    @Override
    public void onAction(ActionEvent e) {
        if (database.isReadOnly()) {
            ErrorDialog.showError("action.imdbsync.blocked.title", "action.imdbsync.blocked.message");
            return;
        }

        try {
            URL url = new URL(config.getImdbUrl() + "/aka-titles.list.gz");

            try (GZIPInputStream in = new GZIPInputStream(url.openStream())) {
                database.recreate(in);
            }
        } catch (IOException ex) {
            ErrorDialog.showException(ex);
        }
    }

}
