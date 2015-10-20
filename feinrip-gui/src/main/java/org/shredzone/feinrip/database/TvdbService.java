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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.shredzone.commons.xml.XQuery;
import org.xml.sax.InputSource;

/**
 * Service for accessing The Tvdb.
 *
 * @see <a href="http://thetvdb.com/">The TVDb</a>
 * @author Richard "Shred" Körber
 */
public class TvdbService {

    private static final String SITE = "http://www.thetvdb.com/api";
    private static final Pattern NAME_AND_YEAR_PATTERN = Pattern.compile("(.*?)\\s+\\(\\d+\\)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+).*");
    private static final int TIMEOUT = 10000;

    // It is permitted to publish the API key in the source code, see here:
    //   http://forums.thetvdb.com/viewtopic.php?f=17&t=8227
    // Please do not use this key for your own project, but request your own key.
    // It's easy and it's free: http://thetvdb.com/?tab=apiregister
    private static final String API_KEY = "A0FD47D057D7D4E2";

    /**
     * Represents a Tvdb series.
     *
     * @author Richard "Shred" Körber
     */
    public static class TvdbSeries {
        public long id;
        public String title;
        public String language;
        public String aired;
        public String imdbId;

        @Override
        public String toString() {
            return title + " (" + language + ", " + aired + ")";
        }
    }

    /**
     * Represents a Tvdb episode.
     *
     * @author Richard "Shred" Körber
     */
    public static class TvdbEpisode {
        public long id;
        public int season;
        public int episode;
        public String title;
    }

    /**
     * Finds all series matching a query.
     *
     * @param query
     *                  Query string
     * @return Matching {@link TvdbSeries} entities
     */
    public static List<TvdbSeries> findSeries(String query) throws IOException {
        String q = query;

        Matcher m = NAME_AND_YEAR_PATTERN.matcher(query);
        if (m.matches()) {
            q = m.group(1);
        }

        String url = SITE + "/GetSeries.php?language=all&seriesname=" + URLEncoder.encode(q, "utf-8");
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);

        try (InputStream in = conn.getInputStream()) {
            XQuery doc = XQuery.parse(new InputSource(in));
            return doc.select("//Series")
                    .map(series -> {
                        TvdbSeries ts = new TvdbSeries();
                        ts.id = Long.parseLong(series.text("seriesid"));
                        ts.title = series.text("SeriesName");
                        ts.language = series.text("language");
                        ts.aired = series.text("FirstAired");
                        ts.imdbId = series.text("IMDB_ID");
                        return ts;
                    })
                    .collect(Collectors.toList());
        }
    }

    /**
     * Finds all episodes for a series. The episodes are returned in DVD order. If there
     * is no such order, they are returned in absolute order.
     *
     * @param series
     *                      {@link TvdbSeries} to get all episodes of
     * @return All {@link TvdbEpisode}
     */
    public static List<TvdbEpisode> findEpisodes(TvdbSeries series) throws IOException {
        String url = SITE + "/" + API_KEY + "/series/" + series.id + "/all/" + series.language + ".xml";

        URLConnection conn = new URL(url).openConnection();

        try (InputStream in = conn.getInputStream()) {
            XQuery doc = XQuery.parse(new InputSource(in));

            // First try the DVD episodes
            List<TvdbEpisode> result = findDvdEpisodes(doc);

            if (result.isEmpty()) {
                // If there are none, use the aired episodes
                result = findAiredEpisodes(doc);
            }

            result.sort((a, b) -> {
                int cmp = Integer.compare(a.season, b.season);
                if (cmp == 0) {
                    cmp = Integer.compare(a.episode, b.episode);
                }
                if (cmp == 0) {
                    cmp = Long.compare(a.id, b.id);
                }
                return cmp;
            });

            return result;
        }
    }

    private static List<TvdbEpisode> findDvdEpisodes(XQuery doc) {
        return doc.select("//Episode")
                .map(episode -> {
                    TvdbEpisode te = createEpisode(episode, "Combined_season", "Combined_episodenumber");
                    if (te == null) {
                        te = createEpisode(episode, "DVD_season", "DVD_episodenumber");
                    }
                    return te;
                })
                .filter(it -> it != null)
                .collect(toList());
    }

    private static List<TvdbEpisode> findAiredEpisodes(XQuery doc) {
        return doc.select("//Episode")
                .map(episode -> createEpisode(episode, "SeasonNumber", "EpisodeNumber"))
                .filter(it -> it != null)
                .collect(toList());
    }

    private static TvdbEpisode createEpisode(XQuery episode, String seasonTag, String episodeTag) {
        Matcher ms = NUMBER_PATTERN.matcher(episode.text(seasonTag));
        Matcher me = NUMBER_PATTERN.matcher(episode.text(episodeTag));

        if (ms.matches() && me.matches()) {
            TvdbEpisode te = new TvdbEpisode();
            te.id = Long.parseLong(episode.text("id"));
            te.title = episode.text("EpisodeName");
            te.season = Integer.parseInt(ms.group(1));
            te.episode = Integer.parseInt(me.group(1));
            return te;
        } else {
            return null;
        }
    }

}
