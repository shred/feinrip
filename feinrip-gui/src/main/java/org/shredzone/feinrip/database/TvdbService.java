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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.shredzone.feinrip.model.Configuration;

/**
 * Service for accessing The Tvdb.
 *
 * @see <a href="http://thetvdb.com/">The TVDb</a>
 * @author Richard "Shred" Körber
 */
public class TvdbService {

    private static final String SITE = "https://api.thetvdb.com";
    private static final Pattern NAME_AND_YEAR_PATTERN = Pattern.compile("(.*?)\\s+\\(\\d+\\)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+).*");
    private static final int TIMEOUT = 10000;
    private static final String ENCODING = "UTF-8";
    private static final Locale FALLBACK_LOCALE = Locale.ENGLISH;

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
        private long id;
        private String title;
        private String language;
        private String aired;

        public String getTitle() {
            return title;
        }

        public String getLanguage() {
            return language;
        }

        public String getAired() {
            return aired;
        }

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
        private long id;
        private int season;
        private int episode;
        private String title;

        public int getSeason() {
            return season;
        }

        public int getEpisode() {
            return episode;
        }

        public String getTitle() {
            return title;
        }

        public String toKey() {
            return String.valueOf(season) + '-' + String.valueOf(episode);
        }

        @Override
        public String toString() {
            return String.format("%d-%02d - %s", season, episode, title);
        }
    }

    private final static TvdbService instance = new TvdbService();

    private Set<String> availableLanguages;
    private String token;

    /**
     * Returns the singleton instance of {@link TvdbService}.
     */
    public static TvdbService instance() {
        return instance;
    }

    private TvdbService() {
        // Singleton with private constructor
    }

    /**
     * Finds all series matching a query.
     *
     * @param query
     *                  Query string
     * @return Matching {@link TvdbSeries} entities
     */
    public List<TvdbSeries> findSeries(String query) throws IOException {
        Set<String> languages = getLanguages();

        List<TvdbSeries> result = new ArrayList<>();

        String sysLanguage = Locale.getDefault().getLanguage();
        if (languages.contains(sysLanguage)) {
            result.addAll(findSeries(query, sysLanguage));
        }

        String fallbackLanguage = FALLBACK_LOCALE.getLanguage();
        if (!fallbackLanguage.equals(sysLanguage) && languages.contains(fallbackLanguage)) {
            result.addAll(findSeries(query, fallbackLanguage));
        }

        return result;
    }

    /**
     * Finds all series matching a query, by given language
     *
     * @param query
     *            Query string
     * @param language
     *            Language
     * @return Matching {@link TvdbSeries} entities
     */
    private List<TvdbSeries> findSeries(String query, String language) throws IOException {
        String q = query;

        Matcher m = NAME_AND_YEAR_PATTERN.matcher(query);
        if (m.matches()) {
            q = m.group(1);
        }

        String token = getToken();

        String url = SITE + "/search/series?name=" + URLEncoder.encode(q, "utf-8");
        HttpURLConnection conn = request(url, token, language);

        if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return Collections.emptyList();
        }

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to find series: " + conn.getResponseCode()
                + " " + conn.getResponseMessage());
        }

        JSONObject json = readJsonResponse(conn);
        JSONArray data = json.getJSONArray("data");

        return IntStream.range(0, data.length())
            .mapToObj(ix -> data.getJSONObject(ix))
            .map(series -> {
                TvdbSeries ts = new TvdbSeries();
                ts.id = series.getLong("id");
                ts.title = series.getString("seriesName");
                ts.language = language;
                ts.aired = series.getString("firstAired");
                return ts;
            })
            .collect(toList());
    }

    /**
     * Finds all episodes for a series. The episodes are returned in DVD order. If there
     * is no such order, they are returned in aired order. If there is no episode title
     * in the desired language, the fallback language is used for that title.
     *
     * @param series
     *                      {@link TvdbSeries} to get all episodes of
     * @return All {@link TvdbEpisode}
     */
    public List<TvdbEpisode> findEpisodes(TvdbSeries series) throws IOException {
        Set<String> languages = getLanguages();

        List<TvdbEpisode> result = Collections.emptyList();

        String selectedLanguage = series.language;
        if (languages.contains(selectedLanguage)) {
            result = readEpisodes(series.id, selectedLanguage);
        }

        boolean missingTitle = result.stream()
                .filter(it -> it.title.trim().isEmpty())
                .findFirst()
                .isPresent();

        if (missingTitle) {
            String fallbackLanguage = FALLBACK_LOCALE.getLanguage();
            if (!fallbackLanguage.equals(selectedLanguage) && languages.contains(fallbackLanguage)) {
                Map<String, TvdbEpisode> fallbackMap =
                        readEpisodes(series.id, fallbackLanguage).stream()
                            .collect(toMap(TvdbEpisode::toKey, identity()));

                result.stream()
                    .filter(it -> it.title.trim().isEmpty())
                    .forEach(epi -> epi.title = fallbackMap.getOrDefault(epi.toKey(), epi).title);
            }
        }

        return result;
    }

    /**
     * Reads all episodes for a series.
     *
     * @param seriesId
     *            ID of the series to get the episodes of
     * @param language
     *            Language to read the episode titles for
     * @return All {@link TvdbEpisode}
     */
    private List<TvdbEpisode> readEpisodes(long seriesId, String language) throws IOException {
        List<TvdbEpisode> result = new ArrayList<>();

        Function<JSONObject, TvdbEpisode> mapper =
                Configuration.global().isTvdbAired()
                ? it -> createEpisode(it, "airedSeason", "airedEpisodeNumber")
                : it -> createEpisode(it, "dvdSeason", "dvdEpisodeNumber");

        int page = 1;
        while (true) {
            JSONObject json = readEpisodesAsJson(seriesId, language, page);
            JSONArray data = json.getJSONArray("data");

            IntStream.range(0, data.length())
                    .mapToObj(ix -> data.getJSONObject(ix))
                    .map(mapper)
                    .filter(it -> it != null)
                    .forEach(result::add);

            page = json.getJSONObject("links").optInt("next", -1);
            if (page == -1) {
                break;
            }
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

    /**
     * Returns a page of episodes for a series.
     *
     * @param seriesId
     *            ID of the series to get the episodes of
     * @param language
     *            Language of the episode titles
     * @param page
     *            Page number, starting from 1
     * @return {@link JSONObject} containing the result data
     */
    private JSONObject readEpisodesAsJson(long seriesId, String language, int page) throws IOException {
        String token = getToken();

        String url = SITE + "/series/" + seriesId + "/episodes?page=" + page;
        HttpURLConnection conn = request(url, token, language);

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to read episodes: " + conn.getResponseCode()
                + " " + conn.getResponseMessage());
        }

        return readJsonResponse(conn);
    }

    /**
     * Returns all available languages.
     */
    private Set<String> getLanguages() throws IOException {
        if (availableLanguages == null) {
            String token = getToken();

            String url = SITE + "/languages";
            HttpURLConnection conn = request(url, token, null);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to read languages: " + conn.getResponseCode()
                    + " " + conn.getResponseMessage());
            }

            JSONObject json = readJsonResponse(conn);

            JSONArray data = json.getJSONArray("data");
            availableLanguages = IntStream.range(0, data.length())
                    .mapToObj(ix -> data.getJSONObject(ix))
                    .map(language -> language.getString("abbreviation"))
                    .collect(toSet());
        }
        return availableLanguages;
    }

    /**
     * Logs in and returns a JWS token.
     */
    private String getToken() throws IOException {
        if (token == null) {
            JSONObject data = new JSONObject();
            data.put("apikey", API_KEY);
            HttpURLConnection conn = request(SITE + "/login", null, null, data);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to login into TVDB: " + conn.getResponseCode()
                    + " " + conn.getResponseMessage());
            }

            JSONObject result = readJsonResponse(conn);
            token = result.getString("token");
        }
        return token;
    }

    /**
     * Sends a GET request.
     *
     * @param url
     *            Target URL
     * @param token
     *            JWS token, or {@code null}
     * @param language
     *            Accepted langugae
     * @return {@link HttpURLConnection} to the site
     */
    private HttpURLConnection request(String url, String token, String language) throws IOException {
        return request(url, token, language, null);
    }

    /**
     * Sends a POST request.
     *
     * @param url
     *            Target URL
     * @param token
     *            JWS token, or {@code null}
     * @param language
     *            Accepted langugae
     * @param data
     *            JSON POST request body
     * @return {@link HttpURLConnection} to the site
     */
    private HttpURLConnection request(String url, String token, String language, JSONObject data) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setUseCaches(false);
        conn.setRequestMethod(data != null ? "POST" : "GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Accept-Charset", ENCODING);
        conn.setRequestProperty("Accept-Encoding", "gzip");
        if (language != null) {
            conn.setRequestProperty("Accept-Language", language);
        }

        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        byte[] outputData = null;
        if (data != null) {
            outputData = data.toString().getBytes(ENCODING);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setFixedLengthStreamingMode(outputData.length);
        }

        conn.connect();

        if (outputData != null) {
            try (OutputStream out = conn.getOutputStream()) {
                out.write(outputData);
            }
        }

        return conn;
    }

    /**
     * Reads a JSON response from the server.
     *
     * @param conn
     *            {@link HttpURLConnection} to read from
     * @return {@link JSONObject} of the returned body
     */
    private JSONObject readJsonResponse(HttpURLConnection conn) throws IOException {
        InputStream in = conn.getInputStream();
        if ("gzip".equals(conn.getContentEncoding())) {
            in = new GZIPInputStream(in);
        }
        try (Reader reader = new InputStreamReader(in, ENCODING)) {
            return (JSONObject) new JSONTokener(reader).nextValue();
        }
    }

    /**
     * Creates an {@link TvdbEpisode} object.
     *
     * @param episode
     *            {@link JSONObject} containing episode data
     * @param seasonTag
     *            Name of the season tag
     * @param episodeTag
     *            Name of the episode tag
     * @return {@link TvdbEpisode} that was generated
     */
    private TvdbEpisode createEpisode(JSONObject episode, String seasonTag, String episodeTag) {
        Matcher ms = NUMBER_PATTERN.matcher(episode.optString(seasonTag));
        Matcher me = NUMBER_PATTERN.matcher(episode.optString(episodeTag));

        if (ms.matches() && me.matches()) {
            TvdbEpisode te = new TvdbEpisode();
            te.id = episode.getLong("id");
            te.title = episode.optString("episodeName");
            te.season = Integer.parseInt(ms.group(1));
            te.episode = Integer.parseInt(me.group(1));
            return te;
        } else {
            return null;
        }
    }
}
