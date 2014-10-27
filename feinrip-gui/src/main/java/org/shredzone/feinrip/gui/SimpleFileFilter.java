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
package org.shredzone.feinrip.gui;

import java.io.File;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;

import javax.swing.filechooser.FileFilter;

/**
 * A simple {@link FileFilter} that accepts all directories and all files ending with
 * one of the given suffixes.
 *
 * @author Richard "Shred" Körber
 */
public class SimpleFileFilter extends FileFilter {

    private final SortedSet<String> suffix = new TreeSet<>();
    private final String description;

    /**
     * Creates a new {@link SimpleFileFilter}.
     *
     * @param sx
     *            Suffixes, with no leading period
     */
    public SimpleFileFilter(String... sx) {
        StringJoiner sj = new StringJoiner(", ");
        for (String s : sx) {
            sj.add(s);
            suffix.add(s.toLowerCase());
        }
        description = sj.toString();
    }

    @Override
    public boolean accept(File f) {
        if (!f.isFile()){
            return true;
        }

        String fn = f.getName();
        int ix = fn.lastIndexOf('.');
        if (ix < 0 || ix > fn.length() - 1) {
            return false;
        }

        String su = fn.substring(ix + 1).toLowerCase();
        return suffix.contains(su);
    }

    @Override
    public String getDescription() {
        return description;
    }

}
