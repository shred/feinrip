/*
 * feinrip
 *
 * Copyright (C) 2016 Richard "Shred" Körber
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
package org.shredzone.feinrip.util;

/**
 * A builder for log outputs. It also compresses duplicate lines, to reduce output.
 * <p>
 * The builder is line oriented. Each append is a single line. There is no need to add
 * line feed characters.
 *
 * @author Richard "Shred" Körber
 */
public class LogBuilder implements Appendable {

    private final StringBuilder sb = new StringBuilder();
    private String lastLine;
    private int lastLineCounter;

    /**
     * Clears the builder, so it can be reused.
     */
    public void clear() {
        sb.setLength(0);
    }

    private void writeln(String line) {
        if (sb.length() > 0) {
            sb.append("\n");
        }
        sb.append(line);
        System.out.println(line); // DEBUG
    }

    @Override
    public Appendable append(CharSequence csq) {
        String line = csq.toString();

        // Count and ignore duplicated lines
        if (line.equals(lastLine)) {
            lastLineCounter++;
            return this;
        }

        // Show line multiplicator if applicable
        if (lastLineCounter > 0) {
            writeln(" × " + lastLineCounter);
            lastLineCounter = 0;
        }

        writeln(line);
        lastLine = line;

        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) {
        return append(csq.subSequence(start, end));
    }

    @Override
    public Appendable append(char c) {
        return append(String.valueOf(c));
    }

    @Override
    public String toString() {
        if (lastLineCounter > 0) {
            StringBuilder result = new StringBuilder(sb);
            result.append("\n × ").append(lastLineCounter);
            return result.toString();
        } else {
            return sb.toString();
        }
    }

}
