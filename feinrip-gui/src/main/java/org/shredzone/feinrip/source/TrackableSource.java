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
package org.shredzone.feinrip.source;

import java.util.List;

import org.shredzone.feinrip.model.Track;

/**
 * A {@link Source} that supports multiple vob streams on different tracks.
 *
 * @author Richard "Shred" Körber
 */
public interface TrackableSource extends Source {

    /**
     * Information about all {@link Track} of the source.
     */
    List<Track> getTracks();

    /**
     * Sets the selected {@link Track}.
     */
    void setSelectedTrack(Track track);

    /**
     * Gets the currently selected {@link Track}.
     */
    Track getSelectedTrack();

    /**
     * Select the next track, if applicable.
     *
     * @throws IndexOutOfBoundsException
     *             if the last track has been reached, and there is no next track.
     */
    void nextTrack();

}
