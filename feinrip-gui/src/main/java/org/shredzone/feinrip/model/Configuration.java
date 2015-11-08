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
package org.shredzone.feinrip.model;

import java.util.prefs.Preferences;

/**
 * Configuration model. Changes are automatically persisted. This is a singleton.
 *
 * @author Richard "Shred" Körber
 */
public class Configuration {
    private static final Configuration INSTANCE = new Configuration();
    private static final String SOUNDFILE_KEY = "soundFile";
    private static final String FORCEAUDIODEMUX_KEY = "forceAudioDemux";
    private static final String MUXERHOLD_KEY = "muxerHold";
    private static final String DVD_STREAMTYPE_KEY = "dvdStreamType";
    private static final String TEMP_DIR = "tempDir";
    private static final String IMDB_URL = "imdbUrl";
    private static final String IMDB_ENABLE = "imdbEnable";
    private static final String OMDB_ENABLE = "omdbEnable";
    private static final String OFDB_ENABLE = "ofdbEnable";

    /**
     * Returns the global {@link Configuration} instance.
     */
    public static Configuration global() {
        return INSTANCE;
    }

    private final Preferences prefs = Preferences.userNodeForPackage(Configuration.class);

    private Configuration() {
        // No public constructor
    }

    /**
     * Sets the path to the sound file to be played on completion.
     */
    public void setSoundFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            prefs.remove(SOUNDFILE_KEY);
        } else {
            prefs.put(SOUNDFILE_KEY, filename);
        }
    }

    /**
     * Gets the path to the sound file to be played on completion.
     */
    public String getSoundFile() {
        return prefs.get(SOUNDFILE_KEY, null);
    }

    /**
     * Sets if audio streams are forcibly demuxed.
     */
    public void setForceAudioDemux(boolean mode) {
        prefs.putBoolean(FORCEAUDIODEMUX_KEY, mode);
    }

    /**
     * Gets if audio streams are forcibly demuxed.
     */
    public boolean isForceAudioDemux() {
        return prefs.getBoolean(FORCEAUDIODEMUX_KEY, false);
    }

    /**
     * Sets if waiting for user before muxing.
     */
    public void setHoldBeforeMuxing(boolean mode) {
        prefs.putBoolean(MUXERHOLD_KEY, mode);
    }

    /**
     * Gets if waiting for user before muxing.
     */
    public boolean isHoldBeforeMuxing() {
        return prefs.getBoolean(MUXERHOLD_KEY, false);
    }

    /**
     * Sets {@link StreamType} for streaming DVD sources.
     */
    public void setDvdStreamType(StreamType type) {
        if (type != null) {
            prefs.put(DVD_STREAMTYPE_KEY, type.name());
        } else {
            prefs.remove(DVD_STREAMTYPE_KEY);
        }
    }

    /**
     * Gets {@link StreamType} for streaming DVD sources.
     */
    public StreamType getDvdStreamType() {
        StreamType def = StreamType.DVDNAV;
        try {
            return StreamType.valueOf(prefs.get(DVD_STREAMTYPE_KEY, def.name()));
        } catch (IllegalArgumentException ex) {
            // An unknown value was found in the preferences...
            return def;
        }
    }

    /**
     * Sets the directory to be used for temporary files.
     */
    public void setTempDir(String dir) {
        if (dir != null) {
            prefs.put(TEMP_DIR, dir);
        } else {
            prefs.remove(TEMP_DIR);
        }
    }

    /**
     * Gets the directory to be used for temporary files.
     */
    public String getTempDir() {
        // Use /var/tmp as default because /tmp is a ram FS on some Linuxes, which may
        // be too small to hold entire movies.
        return prefs.get(TEMP_DIR, "/var/tmp");
    }

    /**
     * Sets the URL of the IMDb database file server to be used.
     */
    public void setImdbUrl(String url) {
        if (url != null) {
            prefs.put(IMDB_URL, url);
        } else {
            prefs.remove(IMDB_URL);
        }
    }

    /**
     * Gets the URL of the IMDb database file server to be used.
     */
    public String getImdbUrl() {
        return prefs.get(IMDB_URL, "ftp://ftp.fu-berlin.de/pub/misc/movies/database/");
    }

    /**
     * Sets if the IMDb is to be searched for titles.
     */
    public void setImdbEnabled(boolean enabled) {
        prefs.putBoolean(IMDB_ENABLE, enabled);
    }

    /**
     * Checks if the IMDb is to be searched for titles.
     */
    public boolean isImdbEnabled() {
        return prefs.getBoolean(IMDB_ENABLE, true);
    }

    /**
     * Sets if the OMDB is to be searched for titles.
     */
    public void setOmdbEnabled(boolean enabled) {
        prefs.putBoolean(OMDB_ENABLE, enabled);
    }

    /**
     * Checks if the OMDB is to be searched for titles.
     */
    public boolean isOmdbEnabled() {
        return prefs.getBoolean(OMDB_ENABLE, true);
    }

    /**
     * Sets if the OFDb is to be searched for titles.
     */
    public void setOfdbEnabled(boolean enabled) {
        prefs.putBoolean(OFDB_ENABLE, enabled);
    }

    /**
     * Checks if the OFDb is to be searched for titles.
     */
    public boolean isOfdbEnabled() {
        return prefs.getBoolean(OFDB_ENABLE, true);
    }

}
