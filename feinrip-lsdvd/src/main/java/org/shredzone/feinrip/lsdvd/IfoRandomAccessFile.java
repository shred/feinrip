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
package org.shredzone.feinrip.lsdvd;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.shredzone.feinrip.lsdvd.DvdAudio.Mode;
import org.shredzone.feinrip.lsdvd.DvdAudio.Type;
import org.shredzone.feinrip.lsdvd.DvdSubtitle.SubType;
import org.shredzone.feinrip.lsdvd.DvdTitleSet.Aspect;
import org.shredzone.feinrip.lsdvd.DvdTitleSet.Format;

/**
 * Specialized reader methods for accessing IFO and BUP files.
 *
 * @author Richard "Shred" Körber
 */
public class IfoRandomAccessFile extends RandomAccessFile {

    private static final long DVD_BLOCK_LENGTH = 2048;

    /**
     * Creates a new {@link IfoRandomAccessFile}.
     *
     * @param basedir
     *            Mount directory of the DVD
     * @param file
     *            File name of the IFO or BUP file to be read. For this part, an
     *            all-lowercase version of the file name is tried when the given file name
     *            was not found.
     */
    public IfoRandomAccessFile(File basedir, String file) throws FileNotFoundException {
        super(findCaselessFilename(basedir, file), "r");
    }

    /**
     * Creates a new {@link File} instance with the give basedir and file name. If no such
     * file exists, a {@link File} instance with the file name all lowercased is returned
     * instead.
     *
     * @param basedir
     *            Base directory the file is expected in
     * @param file
     *            File name
     * @return {@link File} instance, not guaranteed to point to an existing file
     */
    private static File findCaselessFilename(File basedir, String file) {
        File given = new File(basedir, file);
        if (given.exists()) {
            return given;
        }

        return new File(basedir, file.toLowerCase());
    }

    /**
     * Moves the cursor to the given offset. If the offset lies beyond the end of file, an
     * {@link EOFException} is thrown.
     *
     * @param offset
     *            offset to jump to, in bytes
     * @return {@code this}
     */
    public IfoRandomAccessFile at(long offset) throws IOException {
        if (offset >= length()) {
            throw new EOFException();
        }
        seek(offset);
        return this;
    }

    /**
     * Skips the number of given bytes. If the exact number of bytes cannot be skipped
     * because end of file is reached, an {@link EOFException} will be thrown.
     *
     * @param num
     *            number of bytes to skip
     * @return {@code this}
     */
    public IfoRandomAccessFile skip(int num) throws IOException {
        int actual = skipBytes(num);
        if (actual != num) {
            throw new EOFException();
        }
        return this;
    }

    /**
     * Reads a fixed amount of bytes and returns them in a byte array. If the exact number
     * of bytes cannot be read because end of file is reached, an {@link EOFException}
     * will be thrown.
     *
     * @param length
     *            number of bytes to read
     * @return byte array containing the data
     */
    public byte[] readFixedBytes(int length) throws IOException {
        byte data[] = new byte[length];
        readFully(data);
        return data;
    }

    /**
     * Reads an ASCII string with fixed length. If the exact number of characters cannot
     * be read because end of file is reached, an {@link EOFException} will be thrown.
     *
     * @param length
     *            number of characters to read
     * @return String that was read, or {@code null} if the string was empty
     */
    public String readFixedString(int length) throws IOException {
        byte data[] = readFixedBytes(length);
        if (data[0] == 0) {
            return null;
        }
        return new String(data, "ASCII");
    }

    /**
     * Reads an unsigned byte.
     *
     * @return byte read
     * @throws EOFException
     *             if the end of file was reached
     */
    public int readu8() throws IOException {
        byte data[] = readFixedBytes(1);
        return (data[0] & 0xFF);
    }

    /**
     * Reads an unsigned word (2 byte).
     *
     * @return word read
     * @throws EOFException
     *             if the end of file was reached
     */
    public int readu16() throws IOException {
        byte data[] = readFixedBytes(2);
        return ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
    }

    /**
     * Reads an unsigned long (4 byte).
     *
     * @return long read
     * @throws EOFException
     *             if the end of file was reached
     */
    public long readu32() throws IOException {
        byte data[] = readFixedBytes(4);
        return ((data[0] & 0xFF) << 24)
                        | ((data[1] & 0xFF) << 16)
                        | ((data[2] & 0xFF) << 8)
                        | (data[3] & 0xFF);
    }

    /**
     * Reads an offset value. The offset is converted to a byte offset relative to the
     * file start.
     *
     * @return offset read
     * @throws EOFException
     *             if the end of file was reached
     */
    public long readOffset() throws IOException {
        return readu32() * DVD_BLOCK_LENGTH;
    }

    /**
     * Reads a BCD time. The encoded time contains hour, minute, second, frame and an
     * information about frames per second. This method returns the time in milliseconds.
     *
     * @return time in ms
     * @throws EOFException
     *             if the end of file was reached
     */
    public long readBcdTimeMs() throws IOException {
        byte data[] = readFixedBytes(4);

        double fps;
        switch (data[3] & 0xC0) {
            case 0x40: fps = 25.0d; break;
            case 0xC0: fps = 29.97d; break;
            default: throw new IfoException("Unknown fps rate: " + ((data[3] & 0xC0) >> 6));
        }

        int frame = (((data[3] & 0x30) >> 4) * 10 + (data[3] & 0x0F));
        int sec = ((data[2] & 0xF0) >> 4) * 10 + (data[2] & 0x0F);
        int min = ((data[1] & 0xF0) >> 4) * 10 + (data[1] & 0x0F);
        int hour = ((data[0] & 0xF0) >> 4) * 10 + (data[0] & 0x0F);

        double pos = (((((hour * 60) + min) * 60) + sec) * fps) + frame;

        return (long) (pos * 1000.0d / fps);
    }

    /**
     * Reads the PGC offset table of a VTS file.
     *
     * @return Map of VTS number and its PGC offsets, relative to the beginning of the VTS
     *         file.
     */
    public Map<Integer, Long> readPgcOffsets() throws IOException {
        long vts_ptt_srpt = at(0xC8).readOffset();
        long vts_pgci = readOffset();

        at(vts_ptt_srpt);
        int titles = readu16();
        if (titles > 256) {
            throw new IfoException("Too many titles in ptt_srpt: " + titles);
        }

        skip(6);
        long pttOffsets[] = new long[titles];
        for (int ix = 0; ix < titles; ix++) {
            pttOffsets[ix] = readu32() + vts_ptt_srpt;
        }

        int pgcn[] = new int[titles];
        for (int ix = 0; ix < titles; ix++) {
            pgcn[ix] = at(pttOffsets[ix]).readu16();
        }

        at(vts_pgci);
        int pgcns = readu16();
        skip(6);

        Map<Integer, Long> pgcOffset = new HashMap<>();
        for (int ix = 0; ix < titles; ix++) {
            int ipgcn = pgcn[ix] - 1;
            if (ipgcn >= pgcns) {
                throw new IfoException("PGCI index out of range: " + ipgcn + " > " + pgcns);
            }

            at(vts_pgci + 8 + (8 * ipgcn));
            int title = readu8() & 0x7F;
            skip(3);
            long offset = readu32() + vts_pgci;

            if (pgcOffset.containsKey(title)) {
                throw new IfoException("Title " + title + " defined twice in PGC offset table");
            }

            pgcOffset.put(title, offset);
        }

        return pgcOffset;
    }

    /**
     * Reads audio attributes and returns a {@link DvdAudio}. The cursor is expected at
     * the start of the audio attribute structure, and is placed at the start of the next
     * audio attribute structure.
     *
     * @return {@link DvdAudio} containing decoded audio attributes
     */
    public DvdAudio readAudioAttributes() throws IOException {
        DvdAudio audio = new DvdAudio();

        int flag1 = readu8();
        switch (flag1 & 0xE0) {
            case 0x00: audio.setMode(Mode.AC3);   break;
            case 0x40: audio.setMode(Mode.MPEG1); break;
            case 0x60: audio.setMode(Mode.MPEG2); break;
            case 0x80: audio.setMode(Mode.LPCM);  break;
            case 0xA0: audio.setMode(Mode.SDDS);  break;
            case 0xC0: audio.setMode(Mode.DTS);   break;
        }

        int flag2 = readu8();
        audio.setChannels((flag2 & 0x07) + 1);

        if ((flag1 & 0x0C) != 0x00) {
            audio.setLang(readFixedString(2));
        } else {
            skip(2);
        }

        int ext = skip(1).readu8();
        switch (ext) {
            case 1: audio.setType(Type.NORMAL);            break;
            case 2: audio.setType(Type.VISUALLY_IMPAIRED); break;
            case 3: audio.setType(Type.DIRECTORS_COMMENT); break;
            case 4: audio.setType(Type.ALTERNATE);         break;
        }

        skip(2);

        return audio;
    }

    /**
     * Reads subtitle attributes and returns a {@link DvdSubtitle}. The cursor is expected
     * at the start of the subtitle attribute structure, and is placed at the start of the
     * next subtitle attribute structure.
     *
     * @return {@link DvdSubtitle} containing decoded subtitle attributes
     */
    public DvdSubtitle readSubtitleAttributes() throws IOException {
        DvdSubtitle sub = new DvdSubtitle();

        int flags1 = readu8();
        skip(1);

        if ((flags1 & 0x03) != 0x00) {
            sub.setLanguage(readFixedString(2));
        } else {
            skip(2);
        }

        int ext = skip(1).readu8();
        switch (ext) {
            case  1: sub.setType(SubType.NORMAL);                     break;
            case  2: sub.setType(SubType.LARGE);                      break;
            case  3: sub.setType(SubType.CHILDREN);                   break;
            case  5: sub.setType(SubType.NORMAL_CAPTIONS);            break;
            case  6: sub.setType(SubType.LARGE_CAPTIONS);             break;
            case  7: sub.setType(SubType.CHILDREN_CAPTIONS);          break;
            case  9: sub.setType(SubType.FORCED);                     break;
            case 13: sub.setType(SubType.DIRECTOR_COMMENTS);          break;
            case 14: sub.setType(SubType.LARGE_DIRECTOR_COMMENTS);    break;
            case 15: sub.setType(SubType.CHILDREN_DIRECTOR_COMMENTS); break;
        }

        return sub;
    }

    /**
     * Reads a title set and returns a {@link DvdTitleSet}.
     *
     * @return {@link DvdTitleSet}
     */
    public DvdTitleSet readTitleSet() throws IOException {
        DvdTitleSet titleSet = new DvdTitleSet();

        int flag1 = at(0x200).readu8();
        boolean pal = (flag1 & 0x30) != 0x00;
        titleSet.setFormat(pal ? Format.PAL : Format.NTSC);
        titleSet.setAspect((flag1 & 0x0C) == 0x0C ? Aspect.ASPECT_16_9 : Aspect.ASPECT_4_3);
        titleSet.setLetterboxEnabled((flag1 & 0x01) == 0); // != 0 means disallowed!
        titleSet.setPanScanEnabled((flag1 & 0x02) == 0); // != 0 means disallowed!

        int flag2 = readu8();
        switch (flag2 & 0x38) {
            case 0x00:
                titleSet.setWidth(720);
                titleSet.setHeight(pal ? 576 : 480);
                break;

            case 0x08:
                titleSet.setWidth(704);
                titleSet.setHeight(pal ? 576 : 480);
                break;

            case 0x10:
                titleSet.setWidth(352);
                titleSet.setHeight(pal ? 576 : 480);
                break;

            case 0x18:
                titleSet.setWidth(352);
                titleSet.setHeight(pal ? 288 : 240);
                break;
        }

        int audios = readu16();
        for (int ix = 0; ix < 8; ix++) {
            if (ix < audios) {
                titleSet.getAudios().add(readAudioAttributes());
            } else {
                skip(8);
            }
        }

        skip(16);

        int subs = readu16();
        for (int ix = 0; ix < 32; ix++) {
            if (ix < subs) {
                titleSet.getSubs().add(readSubtitleAttributes());
            } else {
                skip(6);
            }
        }

        return titleSet;
    }

}
