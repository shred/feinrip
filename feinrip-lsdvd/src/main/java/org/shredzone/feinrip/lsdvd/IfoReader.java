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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.shredzone.feinrip.lsdvd.DvdTitleSet.Aspect;

/**
 * Reads IFO and BUP files of a DVD and returns a basic overview of the DVD structure.
 * <p>
 * It is a simpler, but less buggy replacement for the <code>lsdvd</code> tool.
 *
 * @author Richard "Shred" Körber
 * @see <a href="http://stnsoft.com/DVD/index.html">DVD-Video Information</a>
 */
public class IfoReader {

    private static LsdvdLogger LOG = new LsdvdLogger(IfoReader.class);

    private final List<DvdTitle> titles = new ArrayList<>();

    /**
     * Creates a new {@link IfoReader}.
     *
     * @param dvdDir
     *            Mount directory of the DVD (must not be the device!)
     */
    public IfoReader(File dvdDir) throws IOException {
        read(dvdDir);

        if (LOG.isDebug()) {
            titles.forEach(title -> LOG.debug("%s", title));
        }
    }

    /**
     * List of all DVD titles. The first title is at index 0.
     */
    public List<DvdTitle> getTitles() {
        return titles;
    }

    /**
     * Gets {@link DvdTitle} about the given title number.
     *
     * @param title
     *            Title number, starting from 1
     */
    public DvdTitle getTitle(int title) {
        return getTitles().get(title);
    }

    /**
     * Reads the DVD structure.
     *
     * @param dvdDir
     *            Mount directory of the DVD
     */
    private void read(File dvdDir) throws IOException {
        titles.clear();
        try {
            LOG.info("Reading IFO from mount %s", dvdDir);
            readVmgFile(dvdDir, "VIDEO_TS/VIDEO_TS.IFO");
        } catch (IfoException | EOFException ex) {
            LOG.warn("Failed to read VIDEO_TS.IFO file: %s", ex.getMessage());
            LOG.info("Reading BUP from mount %s", dvdDir);
            readVmgFile(dvdDir, "VIDEO_TS/VIDEO_TS.BUP");
        }
        LOG.debug("Reading VIDEO_TS completed successfully");
    }

    /**
     * Reads the given VMG file.
     *
     * @param dvdDir
     *            Mount directory of the DVD
     * @param vmgName
     *            Path and name of the VMG file
     */
    private void readVmgFile(File dvdDir, String vmgName) throws IOException {
        LOG.info("Reading VMG file %s", vmgName);
        try (IfoRandomAccessFile vmg = new IfoRandomAccessFile(dvdDir, vmgName)) {
            if (!"DVDVIDEO-VMG".equals(vmg.readFixedString(12))) {
                throw new IfoException("No VMG file");
            }

            long tt_srpt = vmg.at(0xC4).readOffset();
            LOG.debug("tt_srpt: 0x%08X", tt_srpt);
            vmg.at(tt_srpt);

            int titlesCount = vmg.readu16();
            LOG.debug("titlesCount: %d", titlesCount);

            long endAddress = vmg.skip(2).readu32();
            long computedTitles = endAddress / 12;
            LOG.debug("computedTitles: %d", computedTitles);
            if (computedTitles != titlesCount) {
                LOG.warn("Different number of titles: %d != %d, using the latter one",
                                titlesCount, computedTitles);
                titlesCount = (int) computedTitles;
            }

            int lastVtsn = -1;
            List<DvdTitle> vtsnTitles = new ArrayList<>();

            for (int ix = 0; ix < titlesCount; ix++) {
                DvdTitle title = new DvdTitle();

                title.setTitle(ix + 1);
                title.setAngles(vmg.skip(1).readu8());
                title.setChapters(vmg.readu16());
                title.setVtsn(vmg.skip(2).readu8());
                title.setVts(vmg.readu8());
                vmg.skip(4);

                titles.add(title);
                LOG.debug("Title %2d: vtsn=%d, vts=%d", title.getTitle(),
                                title.getVtsn(), title.getVts());

                if (title.getVtsn() != lastVtsn) {
                    if (!vtsnTitles.isEmpty()) {
                        completeTitles(dvdDir, lastVtsn, vtsnTitles);
                    }
                    vtsnTitles.clear();
                    lastVtsn = title.getVtsn();
                }

                vtsnTitles.add(title);
            }

            if (!vtsnTitles.isEmpty()) {
                completeTitles(dvdDir, lastVtsn, vtsnTitles);
            }
        }
    }

    /**
     * Completes all {@link DvdTitle} by reading the appropriate VTS file.
     *
     * @param dvdDir
     *            Mount directory of the DVD
     * @param vtsn
     *            VTS number
     * @param vtsnTitles
     *            {@link DvdTitle} belonging to this VTS file. When this method returns,
     *            the {@link DvdTitle} will contain detailed data unless the respective
     *            IFO/BUP files were invalid.
     */
    private void completeTitles(File dvdDir, int vtsn, List<DvdTitle> vtsnTitles) throws IOException {
        try {
            try {
                LOG.info("Reading VTS_%02d_0.IFO", vtsn);
                readVtsFile(vtsnTitles, dvdDir, String.format("VIDEO_TS/VTS_%02d_0.IFO", vtsn));
            } catch (IfoException | EOFException ex) {
                try {
                    LOG.warn("Failed to read VTS IFO file: %s", ex.getMessage());
                    LOG.info("Reading VTS_%02d_0.BUP", vtsn);
                    readVtsFile(vtsnTitles, dvdDir, String.format("VIDEO_TS/VTS_%02d_0.BUP", vtsn));
                } catch (IfoException | EOFException ex2) {
                    LOG.warn("VTS file for vtsn %d is invalid: %s", vtsn, ex.getMessage());
                }
            }
            LOG.debug("Reading VTS_%02d_0 completed successfully...", vtsn);
        } catch (FileNotFoundException ex) {
            LOG.warn("Found no VTS file for vtsn %d, ignoring!", vtsn);
        }
    }

    /**
     * Reads a single VTS file.
     *
     * @param vtsnTitles
     *            {@link DvdTitle} belonging to this VTS file. When this method returns,
     *            the {@link DvdTitle} will contain detailed data.
     * @param dvdDir
     *            Mount directory of the DVD
     * @param vtsFile
     *            actual VTS file to be read
     */
    private void readVtsFile(List<DvdTitle> vtsnTitles, File dvdDir, String vtsFile) throws IOException {
        try (IfoRandomAccessFile vts = new IfoRandomAccessFile(dvdDir, vtsFile)) {
            if (!"DVDVIDEO-VTS".equals(vts.readFixedString(12))) {
                throw new IfoException("No VTS file");
            }

            Map<Integer, Long> pgcOffsets = vts.readPgcOffsets();

            DvdTitleSet titleSet = vts.readTitleSet();

            for (int ix = 0; ix < vtsnTitles.size(); ix++) {
                DvdTitle title = vtsnTitles.get(ix);
                title.setTitleSet(titleSet);
                readVtsPgc(vts, title, titleSet, pgcOffsets.get(title.getVts()));
            }
        }
    }

    /**
     * Reads a PGC structure and completes the {@link DvdTitle} with the data found there.
     *
     * @param vts
     *            random access to the VTS file
     * @param title
     *            {@link DvdTitle} to complete
     * @param offset
     *            Offset of the PGC structure to be read
     */
    private void readVtsPgc(IfoRandomAccessFile vts, DvdTitle title, DvdTitleSet titleSet, long offset) throws IOException {
        vts.at(offset).skip(2);

        int chapters = vts.readu8();
        int cellCount = vts.readu8();
        LOG.debug("  chapters: %d, cells: %d", chapters, cellCount);

        title.setTotalTimeMs(vts.readBcdTimeMs());

        vts.skip(4);

        for (int ix = 0; ix < 8; ix++) {
            int snr = vts.readu8();
            if ((snr & 0x80) != 0) {
                DvdAudio audio = titleSet.getAudios().get(ix);
                int streamId = snr & 0x07;
                if (streamId == 0) {
                    streamId = ix;
                }
                streamId += audio.getMode().getBaseStreamId();
                audio.setStreamId(streamId);
            }
            vts.skip(1);
        }

        for (int ix = 0; ix < 32; ix++) {
            int snr = vts.readu8();
            int sWide = vts.readu8();
            int sLetterbox = vts.readu8();
            int sPanScan = vts.readu8();
            if ((snr & 0x80) != 0) {
                snr &= 0x1F;
                sWide &= 0x1F;
                sLetterbox &= 0x1F;
                sPanScan &= 0x1F;

                DvdSubtitle sub = titleSet.getSubs().get(ix);
                if (titleSet.getAspect() == Aspect.ASPECT_16_9) {
                    sub.setStreamWideId((sWide > 0 ? sWide : ix) + 0x20);
                } else {
                    sub.setStream43Id((snr > 0 ? snr : ix) + 0x20);
                }

                if (titleSet.isLetterboxEnabled()) {
                    sub.setStreamLetterboxId((sLetterbox > 0 ? sLetterbox : ix) + 0x20);
                }

                if (titleSet.isPanScanEnabled()) {
                    sub.setStreamPanScanId((sPanScan > 0 ? sPanScan : ix) + 0x20);
                }
            }
        }

        vts.skip(8);
        for (int ix = 0; ix < 16; ix++) {
            title.getColors()[ix] = (int) vts.readu32();
        }

        vts.skip(2);
        int programMapOffset = vts.readu16();
        int cellPlaybackOffset = vts.readu16();

        int[] cells = new int[chapters];
        vts.at(offset + programMapOffset);
        for (int ix = 0; ix < chapters; ix++) {
            cells[ix] = vts.readu8();
        }

        for (int ix = 0; ix < chapters; ix++) {
            int current = cells[ix];
            int next = (ix + 1 < chapters ? cells[ix + 1] : cellCount);
            long ms = 0;
            while (current < next) {
                vts.at(offset + cellPlaybackOffset + ((current - 1) *  0x18) + 4);
                ms += vts.readBcdTimeMs();
                current++;
            }
            if (ms > 0 || next != cellCount) {
                title.getChapterTimeMs().add(ms);
            }
        }
    }

    /**
     * Example tool that can be used for testing and debugging purposes.
     * <p>
     * Debug log level is activated automatically.
     */
    public static void main(String[] args) {
        System.setProperty(LsdvdLogger.DEBUG_PROPERTY_NAME, "true");

        if (args.length < 1) {
            System.err.println("Usage: IfoReader [<dvd-mountpoint> ...]");
            System.exit(1);
        }

        for (String file : args) {
            try {
                IfoReader reader = new IfoReader(new File(file));
                reader.getTitles().forEach(System.out::println);
                System.out.println();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
