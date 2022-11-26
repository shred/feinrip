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
package org.shredzone.feinrip.system;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.shredzone.feinrip.model.Language;
import org.shredzone.feinrip.util.Command;


/**
 * Utility class for available languages.
 * <p>
 * Requires: <code>mkvtoolnix</code> package
 *
 * @author Richard "Shred" Körber
 */
public class LanguageUtils {
    private static final File MKVMERGE = new File("/usr/bin/mkvmerge");

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("(.*?)(?:\\;[^|]+)?\\s*\\|\\s+([a-z]+)\\s+\\|(?:\\s+([a-z]+))?.*");

    private static Map<String, Language> langMap;
    private static List<Language> langList;

    /**
     * Creates a map of all languages known to mkvmerge. If the map has already been
     * created, it will not be created again.
     */
    private static void loadLanguages() {
        if (langMap == null) {
            try {
                langMap = new HashMap<>();

                Command mergeCmd = new Command(MKVMERGE);
                mergeCmd.param("--list-languages");
                mergeCmd.redirectOutputAsStream(stream -> stream
                        .skip(2)
                        .forEach(line -> {
                            Matcher m = LANGUAGE_PATTERN.matcher(line);
                            if (m.matches()) {
                                String name = m.group(1).trim();
                                String iso2 = m.group(2);
                                String iso1 = m.group(3);
                                Language lng = new Language(name, iso1, iso2);
                                if (lng.getIso1() != null) {
                                    langMap.put(lng.getIso1(), lng);
                                }
                                if (lng.getIso2() != null) {
                                    langMap.put(lng.getIso2(), lng);
                                }
                            }
                        }));
                mergeCmd.execute();

                // Add exceptions that can be found on some DVDs
                Optional.ofNullable(langMap.get("und"))
                        .ifPresent(v -> langMap.put("xx", v));
                Optional.ofNullable(langMap.get("he"))
                        .ifPresent(v -> langMap.put("iw", v));
                Optional.ofNullable(langMap.get("heb"))
                        .ifPresent(v -> langMap.put("iw", v));

                sortLanguages();
            } catch (IOException ex) {
                langMap = null;
                throw new RuntimeException("Could not load mkvmerge languages, help!", ex);
            }
        }
    }

    /**
     * Creates a sorted list of all languages. Languages are sorted in this sequence:
     * <ol>
     * <li>Preferred languages, in order of appearance</li>
     * <li>"Undefined" language</li>
     * <li>All languages with iso1 (two letters) set</li>
     * <li>All remaining languages</li>
     * </ol>
     */
    private static void sortLanguages() {
        langList = new ArrayList<>();

        Set<String> seen = new TreeSet<>();

        // First add all preferred languages
        Stream.of(B.getString("language.preferred").split("[ ,;]+")).forEach(it -> {
            seen.add(it);
            langList.add(langMap.get(it.trim()));
        });

        // Then add "undefined" language
        seen.add("und");
        langList.add(langMap.get("und"));

        // Then add "mis" language
        seen.add("mis");
        langList.add(langMap.get("mis"));

        // Then add all remaining languages
        langList.addAll(langMap.values().stream()
                .distinct()
                .filter(it -> !seen.contains(it.getIso2()))
                .sorted((a, b) -> {
                    if (a.getIso1() != null && b.getIso1() == null) {
                        return -1;
                    }

                    if (a.getIso1() == null && b.getIso1() != null) {
                        return 1;
                    }

                    return a.compareTo(b);
                })
                .collect(Collectors.toList()));
    }

    /**
     * Returns the "Undefined" language, used if no language code was given.
     */
    public static Language getUndefined() {
        return findLanguage("und");
    }

    /**
     * Returns the "Missing" language, used if an unknown language was given.
     */
    public static Language getMissing() {
        return findLanguage("mis");
    }

    /**
     * Returns a collection of all {@link Language} known to the mkv encoder.
     */
    public static Collection<Language> getLanguages() {
        loadLanguages();
        return langList;
    }

    /**
     * Finds a {@link Language} by its two or three lettered iso code.
     *
     * @param code
     *            iso code
     * @return {@link Language}, or {@code null} if the language code is not known
     */
    public static Language findLanguage(String code) {
        loadLanguages();
        return langMap.get(code);
    }

}
