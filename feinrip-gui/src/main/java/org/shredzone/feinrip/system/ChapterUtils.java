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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.shredzone.feinrip.model.Chapter;

/**
 * Utility class for analyzing DVD chapters.
 *
 * @author Richard "Shred" Körber
 */
public class ChapterUtils {

    private static final Pattern CHAPTER_PATTERN = Pattern.compile("CHAPTER(\\d+)(NAME)?=(.*)");

    /**
     * Reads all chapters of a ".chap" file.
     * <p>
     * The file must contain a single line with all chapter positions, separated by
     * colons. Alternatively, the dvdxchap file format is accepted as well.
     *
     * @param source
     *            ".chap" file
     * @return List of {@link Chapter} entities parsed from this file
     */
    public static List<Chapter> readChapters(File source) throws IOException {
        List<Chapter> chapters = new ArrayList<>();
        Chapter lastChapter = null;

        try (BufferedReader r = new BufferedReader(new FileReader(source))) {
            String line;
            while ((line = r.readLine()) != null) {
                Matcher m = CHAPTER_PATTERN.matcher(line);
                if (m.matches()) {
                    // dvdxchap format
                    int index = Integer.parseInt(m.group(1));
                    boolean named = "NAME".equals(m.group(2));
                    String value = m.group(3);

                    if (lastChapter != null && lastChapter.getNumber() != index) {
                        chapters.add(lastChapter);
                        lastChapter = null;
                    }

                    if (lastChapter == null) {
                        lastChapter = new Chapter();
                        lastChapter.setNumber(index);
                    }

                    if (named) {
                        lastChapter.setTitle(value);
                    } else {
                        lastChapter.setPosition(value);
                    }

                } else {
                    // single-line format

                    if (chapters.isEmpty()) {
                        Chapter ch = new Chapter();
                        ch.setNumber(1);
                        ch.setPosition("0:00:00.000");
                        ch.setTitle("Chapter 01");
                        chapters.add(ch);
                    }

                    Stream.of(line.split(",")).forEach(pos -> {
                        int chap = chapters.size() + 1;
                        String title = String.format("Chapter %02d", chap);
                        Chapter ch = new Chapter();
                        ch.setNumber(chap);
                        ch.setPosition(pos);
                        ch.setTitle(title);
                        chapters.add(ch);
                    });
                }
            }
        }

        if (lastChapter != null) {
            chapters.add(lastChapter);
        }

        return chapters;
    }

    /**
     * Writes chapters to a chap file suited for mkvmerge.
     *
     * @param chapters
     *            Collection of {@link Chapter} to be written
     * @param out
     *            Target file
     */
    public static void writeChapters(Collection<Chapter> chapters, File out) throws IOException {
        try (PrintWriter pw = new PrintWriter(out)) {
            chapters.stream()
                .filter(chap -> !chap.getTitle().trim().isEmpty())
                .forEach(chap -> {
                    pw.println(String.format("CHAPTER%02d=%s", chap.getNumber(), chap.getPosition()));
                    pw.println(String.format("CHAPTER%02dNAME=%s", chap.getNumber(), chap.getTitle()));
                });
        }
    }

}
