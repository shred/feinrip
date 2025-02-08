/*
 * feinrip
 *
 * Copyright (C) 2014 Richard "Shred" Körber
 *   https://codeberg.org/shred/feinrip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.feinrip.dvb.si;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps charset codes to a Java {@link Charset} object. The result is cached.
 *
 * @author Richard "Shred" Körber
 * @see <a href="http://www.etsi.org/deliver/etsi_en/300400_300499/300468/01.12.01_40/en_300468v011201o.pdf">ETSI EN 300 468, Annex A</a>
 */
public class CharsetMapper {

    private static final Map<String, Charset> CHARSET_CACHE = new HashMap<String, Charset>();

    /**
     * Maps a character type to a {@link Charset}, according to table A.3.
     *
     * @param type
     *          Character type
     * @return {@link Charset}, or {@code null} if not supported
     */
    public static Charset mapCharacterCode(int type) {
        String name = null;
        switch (type) {
            case 0x01: name = "ISO-8859-5"; break;
            case 0x02: name = "ISO-8859-6"; break;
            case 0x03: name = "ISO-8859-7"; break;
            case 0x04: name = "ISO-8859-8"; break;
            case 0x05: name = "ISO-8859-9"; break;
            case 0x06: name = "ISO-8859-10"; break;
            case 0x07: name = "ISO-8859-11"; break;
            case 0x09: name = "ISO-8859-13"; break;
            case 0x0A: name = "ISO-8859-14"; break;
            case 0x0B: name = "ISO-8859-15"; break;
            case 0x11: name = "ISO-8859-1"; break;
            case 0x12: name = "ISO-2022-KR"; break;
            case 0x13: name = "GB2312"; break;
            case 0x14: name = "Big5"; break;
            case 0x15: name = "UTF-8"; break;
        }
        return mapCharset(name);
    }

    /**
     * Maps an ISO code number to a {@link Charset}, according to table A.4.
     *
     * @param code
     *          ISO code number
     * @return {@link Charset}, or {@code null} if not supported
     */
    public static Charset mapIsoCode(int code) {
        if (code >= 0x01 && code <= 0x0F && code != 0x0C) {
            return mapCharset("ISO-8859-" + code);
        } else {
            return null;
        }
    }

    /**
     * Maps a character set name to a {@link Charset}. The result is cached.
     *
     * @param name
     *          Character set name
     * @return {@link Charset}, or {@code null} if not supported
     */
    public static Charset mapCharset(String name) {
        if (name == null) {
            return null;
        }

        if (CHARSET_CACHE.containsKey(name)) {
            return CHARSET_CACHE.get(name);
        } else {
            Charset result = null;
            try {
                result = Charset.forName(name);
            } catch (UnsupportedCharsetException ex) {
                // Ignore exception and return null
            }
            CHARSET_CACHE.put(name, result);
            return result;
        }
    }

}
