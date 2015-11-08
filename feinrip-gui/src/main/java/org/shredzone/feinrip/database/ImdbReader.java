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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads an <code>aka-titles.list</code> file.
 * <p>
 * This class is able to decode the different charsets used for some titles.
 *
 * @author Richard "Shred" Körber
 */
public class ImdbReader implements AutoCloseable {
    private static final char CR = '\n';
    private static final Charset DEFAULT_CHARSET = Charset.forName("iso-8859-1");

    private static final Pattern AKA_PATTERN = Pattern.compile(
                    "\\s+\\(aka.*?\\).*\\([^)]+(ISO-LATIN-\\d+|ISO-8859-\\d+|KOI8-R)[^)]+\\)",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern LATIN_PATTERN = Pattern.compile(
                    "ISO-LATIN-(\\d+)",
                    Pattern.CASE_INSENSITIVE);

    private final InputStream in;
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

    /**
     * Creates a new {@link ImdbReader} for the given {@link InputStream}.
     */
    public ImdbReader(InputStream in) {
        this.in = in;
    }

    /**
     * Reads a line from input. The line ends either a the next CR character or at EOF.
     *
     * @return Line that was read, or {@code null} if EOF is reached.
     */
    private byte[] read() throws IOException {
        int ch = in.read();
        if (ch < 0) {
            return null;    // EOF reached
        }

        lineBuffer.reset();
        while (ch >= 0 && ch != CR) {
            lineBuffer.write(ch);
            ch = in.read();
        }

        return lineBuffer.toByteArray();
    }

    /**
     * Decodes a line. If this is an "aka" line with a known charset at the end of line,
     * the title part is decoded with the given charset. The returned line should always
     * be unicode and ready for further processing without charset hassles.
     *
     * @param line
     *            Line to be decoded
     * @return Decoded line
     */
    private String decodeLine(byte[] line) {
        String decoded = new String(line, DEFAULT_CHARSET);

        Matcher m = AKA_PATTERN.matcher(decoded);
        if (!m.matches()) {
            // It's not an 'aka' line and/or default encoded. Just return it.
            return decoded;
        }

        Charset charset = Charset.forName(latinToIso(m.group(1)));

        int start = "   (aka ".length();
        int end = start;
        int cnt = 0;
        for (int ix = start; ix < line.length; ix++) {
            if (line[ix] == '(') {
                cnt++;
            } else if (line[ix] == ')') {
                cnt--;
                if (cnt < 0) {
                    end = ix - 1;
                    break;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(new String(line, 0, start, DEFAULT_CHARSET));
        sb.append(new String(line, start, end - start, charset));
        sb.append(new String(line, end, line.length - end, DEFAULT_CHARSET));
        return sb.toString();
    }

    /**
     * Reads and decodes a line from the stream.
     *
     * @return Line, or {@code null} if EOF is reached
     */
    public String readLine() throws IOException {
        byte[] line = read();
        if (line == null) {
            return null;
        }

        return decodeLine(line);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    /**
     * Checks if the input charset is an "ISO-LATIN" charset. If so, converts it to the
     * respective "ISO-8859" charset.
     *
     * @param charset
     *            Charset name
     * @return Converted charset name (if "ISO-LATIN") or the given charset name
     */
    private static String latinToIso(String charset) {
        Matcher m = LATIN_PATTERN.matcher(charset);
        if (!m.matches()) {
            return charset;
        }

        int table = Integer.parseInt(m.group(1));
        switch (table) {
            case 1: return "ISO-8859-1";
            case 2: return "ISO-8859-2";
            case 3: return "ISO-8859-3";
            case 4: return "ISO-8859-4";
            case 5: return "ISO-8859-9";
            case 6: return "ISO-8859-10";
            case 7: return "ISO-8859-13";
            case 8: return "ISO-8859-14";
            case 9: return "ISO-8859-15";
            case 10: return "ISO-8859-16";
            default: throw new IllegalArgumentException("Unknown charset " + charset);
        }
    }

}
