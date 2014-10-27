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
package org.shredzone.feinrip.database;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

import org.shredzone.commons.xml.XQuery;
import org.xml.sax.InputSource;

/**
 * Service for accessing OFDb.
 *
 * @see <a href="http://www.ofdb.de">Online Filmdatenbank</a>
 * @author Richard "Shred" Körber
 */
public class OfdbService {

    private static final String SITE = "http://ofdbgw.org";

    /**
     * Searches for titles matching a query.
     *
     * @param query
     *                  Query string
     * @return List of potential matches, may be empty but never {@code null}
     */
    public static List<String> searchTitles(String query) throws IOException {
        String url = SITE + "/search/" + URLEncoder.encode(query, "utf-8");

        URLConnection conn = new URL(url).openConnection();

        try (InputStream in = conn.getInputStream()) {
            XQuery doc = XQuery.parse(new InputSource(in));
            return doc.select("//ofdbgw/resultat/eintrag")
                    .map(entry -> {
                        String title = entry.text("titel");
                        String year = entry.text("jahr");

                        if (year != null) {
                            title = title + " (" + year + ")";
                        }

                        return title;
                    })
                    .collect(toList());
        }
    }

}
