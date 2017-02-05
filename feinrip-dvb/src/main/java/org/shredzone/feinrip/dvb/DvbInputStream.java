/*
 * feinrip
 *
 * Copyright (C) 2014 Richard "Shred" Körber
 *   https://github.com/shred/feinrip
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
package org.shredzone.feinrip.dvb;

import static java.lang.Math.*;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.shredzone.feinrip.dvb.si.CharsetMapper;
import org.shredzone.feinrip.dvb.si.descriptor.Descriptor;
import org.shredzone.feinrip.dvb.si.descriptor.DescriptorFactory;

/**
 * InputStream for reading DVB streams.
 *
 * @author Richard "Shred" Körber
 * @see <a href="http://www.etsi.org/deliver/etsi_en/300400_300499/300468/01.12.01_40/en_300468v011201o.pdf">ETSI EN 300 468</a>
 */
public class DvbInputStream extends DataInputStream {

    private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    public DvbInputStream(InputStream in) {
        super(in);
    }

    /**
     * Reads a byte as Binary Encoded Decimals.
     *
     * @return BCD number read
     */
    public int readBCD() throws IOException {
        int val = readUnsignedByte();
        return (((val & 0xF0) >> 4) * 10) + (val & 0x0F);
    }

    /**
     * Reads a duration.
     *
     * @return Duration (seconds)
     */
    public int readDuration() throws IOException {
        int hours = readBCD();
        int minutes = readBCD();
        int seconds = readBCD();

        return ((((hours * 60) + minutes) * 60) + seconds);
    }

    /**
     * Reads an encoded string. The string is prepended by its size. The string
     * is properly charset decoded.
     *
     * @return String that was read
     */
    public String readEncodedString() throws IOException {
        int length = readUnsignedByte();
        return readEncodedString(length);
    }

    /**
     * Reads the exact number of bytes into a byte array.
     *
     * @param length
     *            Number of bytes to read
     * @return Byte array containing the bytes
     */
    public byte[] readFixed(int length) throws IOException {
        byte[] data = new byte[length];
        int bytesRead = read(data);
        if (bytesRead != length) {
            throw new EOFException();
        }
        return data;
    }

    /**
     * Reads an encoded string of the given length. The string is properly
     * charset decoded.
     *
     * @param length
     *          String length (in byte)
     * @return String that was read
     */
    public String readEncodedString(int length) throws IOException {
        if (length == 0) {
            return "";
        }

        byte[] data = readFixed(length);

        if (data[0] >= 0x20) {
            // Default latin encoding
            return new String(data, ISO_8859_1);

        } else if (data[0] == 0x10) {
            // ISO Mapping
            if (data[1] != 0x00) {
                throw new IOException("Unsupported encoding type: " + data[1]);
            }
            Charset cs = CharsetMapper.mapIsoCode(data[2]);
            if (cs == null) {
                throw new IOException("Unsupported iso encoding: " + data[2]);
            }
            return new String(data, 3, length - 3, cs);
        } else if (data[0] == 0x1F) {
            // TODO: Support
            throw new IOException("encoding_type_id is not currently supported");
        } else {
            Charset cs = CharsetMapper.mapCharacterCode(data[0]);
            if (cs == null) {
                throw new IOException("Unsupported character encoding: " + data[0]);
            }
            return new String(data, 1, length - 1, cs);
        }
    }

    /**
     * Reads a language code.
     *
     * @return Language code (ISO 639-3)
     */
    public String readLanguageCode() throws IOException {
        byte[] lc = readFixed(3);
        return new String(lc, ISO_8859_1);
    }

    /**
     * Reads a date and time.
     * <p>
     * The date is expected as Modified Julian Date (see Annex C).
     *
     * @return Date and time that was read
     */
    public Date readDateTime() throws IOException {
        int mjd = readUnsignedShort();
        int hour = readBCD();
        int minute = readBCD();
        int second = readBCD();

        // This is really insane! What have you smoked, guys?
        long y1 = round(floor((mjd - 15078.2) / 365.25));
        long m1 = round(floor((mjd - 14956.1 - floor(y1 * 365.25)) / 30.6001));
        long d = round(mjd - 14956 - floor(y1 * 365.25) - floor(m1 * 30.6001));
        long k = (m1 == 14 || m1 == 15) ? 1 : 0;
        long y = y1 + k;
        long m = m1 - 1 - k * 12;

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.clear();
        cal.set(((int) y) + 1900, ((int) m) - 1, (int) d, hour, minute, second);

        return cal.getTime();
    }

    /**
     * Reads a {@link Descriptor}.
     *
     * @return {@link Descriptor} that was read.
     */
    public Descriptor readDescriptor() throws IOException {
        int tag = readUnsignedByte();

        Descriptor descriptor = DescriptorFactory.createDescriptor(tag);
        if (descriptor == null) {
            throw new IOException("Unknown Descriptor Tag " + tag);
        }

        descriptor.read(this);
        return descriptor;
    }

}
