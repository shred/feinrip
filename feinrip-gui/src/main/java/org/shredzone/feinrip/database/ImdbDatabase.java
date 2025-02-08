/*
 * feinrip
 *
 * Copyright (C) 2015 Richard "Shred" Körber
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
package org.shredzone.feinrip.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.shredzone.feinrip.gui.ErrorDialog;

/**
 * Manages a movie title database.
 * <p>
 * Internally, the database is stored via HSQLDB in a file in the user's home directory.
 *
 * @author Richard "Shred" Körber
 */
public class ImdbDatabase {

    private static final int QUERY_TIMEOUT = 30;
    private static final Pattern TITLE_PATTERN = Pattern.compile("(\\s+\\(aka\\s+)?(.*?)\\s+\\((\\d{4})\\).*");

    private static ImdbDatabase instance;

    private Connection connection;
    private boolean ro;

    /**
     * Returns the singleton instance of the database.
     *
     * @return {@link ImdbDatabase} singleton
     */
    public static synchronized ImdbDatabase global() {
        if (instance == null) {
            instance = new ImdbDatabase();
            new Thread(() -> {
                IOException exception = null;

                // First try to open in read/write mode...
                try {
                    instance.connect(false);
                } catch (IOException ex) {
                    exception = ex;
                }

                // If could not be locked, try in read only mode...
                if (exception != null
                        && exception.getCause() != null
                        && exception.getCause() instanceof SQLException
                        && exception.getCause().getMessage().startsWith("Database lock acquisition failure")) {
                    try {
                        exception = null;
                        instance.connect(true);
                    } catch (IOException ex) {
                        exception = ex;
                    }
                }

                // It failed for other reasons...
                if (exception != null) {
                    ErrorDialog.showException(exception);
                }
            }).start();
        }
        return instance;
    }

    /**
     * Connects to the database.
     *
     * @param readonly
     *            {@code true} if the database is to be opened as read-only. Useful for
     *            shared access to the database.
     */
    protected void connect(boolean readonly) throws IOException {
        try {
            System.setProperty("hsqldb.log_data", "false");

            Path database = FileSystems.getDefault().getPath(System.getProperty("user.home"), ".local", "share", "feinrip", "movie.db");
            Files.createDirectories(database.getParent());

            String url = "jdbc:hsqldb:file:" + database.toString();
            if (readonly) {
                url += ";readonly=true";
            }

            connection = DriverManager.getConnection(url);
            connection.setAutoCommit(false);

            ro = readonly;

            createDatabase();
        } catch (SQLException ex) {
            close();
            throw new IOException("Could not open database", ex);
        }
    }

    /**
     * Closes the database connection.
     */
    public void close() throws IOException {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException ex) {
            throw new IOException("Could not close database", ex);
        }
    }

    /**
     * Creates a standard statement.
     */
    protected Statement createStatement() throws SQLException {
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(QUERY_TIMEOUT);
        return statement;
    }

    /**
     * Creates a prepared statement.
     */
    protected PreparedStatement createPreparedStatement(String sql) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setQueryTimeout(QUERY_TIMEOUT);
        return statement;
    }

    /**
     * Normalizes a string. It is converted to lowercase, all non-alphanumerical
     * characters are stripped and all white spaces characters are squeezed into one
     * space.
     *
     * @param title
     *            Title to normalize
     * @return Normalized title
     */
    protected String normalize(String title) {
        char[] t = title.trim().toLowerCase().toCharArray();
        for (int ix = 0; ix < t.length; ix++) {
            if (!Character.isLetterOrDigit(t[ix])) {
                t[ix] = ' ';
            }
        }
        return new String(t).replaceAll("\\s+", " ");
    }

    /**
     * Drops the database if it exists.
     */
    protected void dropDatabase() throws SQLException {
        if (ro) return;

        try (Statement statement = createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS movie");
        }
    }

    /**
     * Creates a new database with all tables, unless it exists already.
     */
    protected void createDatabase() throws SQLException {
        if (ro) return;

        try (Statement statement = createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS movie ("
                + "id INTEGER PRIMARY KEY,"
                + "movie INTEGER NOT NULL,"
                + "title VARCHAR(1024) NOT NULL,"
                + "year INTEGER NOT NULL,"
                + "normalized VARCHAR(1024) NOT NULL)");
        }
    }

    /**
     * Recreates the entire movie database from the given <tt>aka-titles.list</tt> input
     * stream. If a database was already existing, it will be dropped before.
     *
     * @param in
     *            {@link InputStream} providing the <em>uncompressed</em>
     *            <tt>aka-titles.list</tt> file to be imported.
     */
    public void recreate(InputStream in) throws IOException {
        if (connection == null || ro) return;

        try (ImdbReader titles = new ImdbReader(in)) {
            dropDatabase();
            createDatabase();

            int id = 0;
            int movie = 0;

            String line;

            // Skip header
            while ((line = titles.readLine()) != null) {
                if (line.startsWith("========")) {
                    break;
                }
            }

            // Import into database
            try (PreparedStatement ps = createPreparedStatement(
                            "INSERT INTO movie (id, movie, title, year, normalized)"
                            + " VALUES (?, ?, ?, ?, ?)")) {

                while ((line = titles.readLine()) != null) {
                    Matcher m = TITLE_PATTERN.matcher(line);
                    if (m.matches()) {
                        id++;
                        if (m.group(1) == null) {
                            movie++; // not an aka of the current movie, but a new title
                        }

                        ps.setInt(1, id);
                        ps.setInt(2, movie);
                        ps.setString(3, m.group(2));
                        ps.setInt(4, Integer.parseInt(m.group(3)));
                        ps.setString(5, normalize(m.group(2)));
                        ps.addBatch();
                    }
                }

                ps.executeBatch();
                connection.commit();
            }
        } catch (SQLException ex) {
            throw new IOException("Failed to recreate database", ex);
        }
    }

    /**
     * Returns all movie titles in the database. For debugging purposes only, as it
     * consumes a considerable amount of heap space.
     *
     * @return List of all movie titles, format: "title (year)"
     */
    public List<String> findAll() throws IOException {
        List<String> result = new ArrayList<>();

        if (connection != null) {
            try (Statement statement = createStatement();
                    ResultSet rs = statement.executeQuery(
                            "SELECT CONCAT(title, ' (', year, ')')"
                            + " FROM movie"
                            + " ORDER BY id")) {
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
            } catch (SQLException ex) {
                throw new IOException("Failed to read database", ex);
            }
        }

        return result;
    }

    /**
     * Returns all movies matching the given term. It also returns the movie title in
     * other languages, so the user can also search for e.g. the German title for the
     * given English title.
     *
     * @param term
     *            Title to search for, or a part of the title
     * @param limit
     *            Maximum number of records to return
     * @return List of all movie titles found, format: "title (year)"
     */
    public List<String> find(String term, int limit) throws IOException {
        List<String> result = new ArrayList<>();

        if (connection != null) {
            try (PreparedStatement ps = createPreparedStatement(
                            "SELECT CONCAT(m2.title, ' (', m2.year, ')')"
                            + " FROM movie m1, movie m2"
                            + " WHERE m1.normalized LIKE ?"
                            + " AND m2.movie = m1.movie"
                            + " ORDER BY m2.id"
                            + " LIMIT ?")) {
                ps.setString(1, '%' + normalize(term) + '%');
                ps.setInt(2, limit);

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
            } catch (SQLException ex) {
                throw new IOException("Failed to find in database, term: '" + term + "', limit " + limit, ex);
            }
        }

        return result;
    }

    /**
     * Checks if the database runs in read-only mode. This is {@code true} if the
     * database is shared with another running instance of feinrip.
     */
    public boolean isReadOnly() {
        return ro;
    }

}
