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
package org.shredzone.feinrip.database;

import java.io.IOException;
import java.util.List;

/**
 * Service for accessing a local copy of the IMDb aka-titles database.
 *
 * @see <a href="http://www.imdb.com/interfaces">IMDb: Alternative Interfaces</a>
 * @author Richard "Shred" Körber
 */
public class ImdbService {

    private static final int MAX_RESULTS = 50;

    /**
     * Searches for titles matching a query.
     *
     * @param query
     *                  Query string
     * @return List of potential matches, may be empty but never {@code null}
     */
    public static List<String> searchTitles(String query) throws IOException {
        ImdbDatabase db = ImdbDatabase.global();
        return db.find(query, MAX_RESULTS);
    }

}
