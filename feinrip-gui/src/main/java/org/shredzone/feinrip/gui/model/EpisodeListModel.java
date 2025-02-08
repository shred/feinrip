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
package org.shredzone.feinrip.gui.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import org.shredzone.feinrip.database.TvdbService.TvdbEpisode;

/**
 * A List model of {@link TvdbEpisode}.
 *
 * @author Richard "Shred" Körber
 */
public class EpisodeListModel implements ListModel<TvdbEpisode>, Serializable {
    private static final long serialVersionUID = 2740885538500636474L;

    private List<TvdbEpisode> episodes;

    public EpisodeListModel() {
        episodes = Collections.emptyList();
    }

    public EpisodeListModel(List<TvdbEpisode> episodes) {
        this.episodes = episodes;
    }

    @Override
    public int getSize() {
        return episodes.size();
    }

    @Override
    public TvdbEpisode getElementAt(int index) {
        return episodes.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        // List and contents won't change after creation, so just ignore listeners
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        // List and contents won't change after creation, so just ignore listeners
    }

}
