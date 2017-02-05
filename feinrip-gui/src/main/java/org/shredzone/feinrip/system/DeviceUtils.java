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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.shredzone.feinrip.model.MountPoint;
import org.shredzone.feinrip.util.Command;


/**
 * Utility class for finding DVD devices and their mount points.
 * <p>
 * Only works on Linuxoids.
 *
 * @author Richard "Shred" Körber
 */
public class DeviceUtils {
    private static final File MOUNT = new File("/usr/bin/mount");
    private static final File EJECT = new File("/usr/bin/eject");

    private static final long POLL_FREQUENCY = 1500L;

    private static Set<MediaChangeListener> listener = new HashSet<>();
    private static Thread changeThread;

    /**
     * Finds all available DVD devices. Also finds CD-ROM and BD-ROM.
     *
     * @return {@link File} refering to DVD devices
     */
    public static List<File> findDvdDevices() {
        return Stream.of(new File("/sys/block").listFiles())
                .filter(File::isDirectory)
                .filter(DeviceUtils::isCdRomType)
                .map(dir -> new File("/dev/" + dir.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a block device dir is of CDROM type.
     *
     * @param dir
     *            Directory to check
     * @return {@code true} if CDROM type
     */
    private static boolean isCdRomType(File dir) {
        // CDROM type block devices are type 5
        File typeFile = new File(dir, "device/type");
        if (typeFile.isFile()) {
            try (BufferedReader r = new BufferedReader(new FileReader(typeFile))) {
                String line = r.readLine();
                if (line != null && "5".equals(line.trim())) {
                    return true;
                }
            } catch (IOException ex) {
                // Ignore this exception and return false
            }
        }

        return false;
    }

    /**
     * Finds the mount point where a DVD is mounted.
     *
     * @param device
     *            DVD Device
     * @return Mount point, or {@code null} if this device is not currently mounted
     */
    public static File findMountPoint(File device) throws IOException {
        Pattern devicePattern = Pattern.compile(Pattern.quote(device.getPath()) + "\\son\\s(\\/.*?)\\stype.*");
        List<File> result = new LinkedList<>();

        Command cmd = new Command(MOUNT);
        cmd.redirectOutputAsStream(stream -> stream
                        .map(devicePattern::matcher)
                        .filter(Matcher::matches)
                        .map(m -> new File(m.group(1)))
                        .forEach(result::add));
        cmd.execute();

        return result.stream().findFirst().orElse(null);
    }

    /**
     * Ejects a DVD.
     *
     * @param mountPoint
     *            DVD mount point to be ejected
     */
    public static void eject(MountPoint mountPoint) throws IOException {
        try {
            new Command(EJECT).param("-i", 0).param(mountPoint.getDevice()).execute();
        } catch (IOException ex) {
            // Ignore it...
        }
        new Command(EJECT).param(mountPoint.getDevice()).execute();
    }

    /**
     * Adds a {@link MediaChangeListener} that is invoked when mounted DVDs are added or
     * removed.
     *
     * @param l
     *            {@link MediaChangeListener} to be added
     */
    public static void addMediaChangeListener(MediaChangeListener l) {
        listener.add(l);

        if (changeThread == null) {
            try {
                FileSystem fs = FileSystems.getDefault();
                Path path = fs.getPath("/run/media/" + System.getProperty("user.name"));

                final WatchKey key = path.register(fs.newWatchService(),
                        StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

                Runnable r = () -> {
                    while (true) {
                        if (key.pollEvents().stream().findAny().isPresent()) {
                            listener.forEach(it -> it.onMediaChange());
                        }
                        try {
                            Thread.sleep(POLL_FREQUENCY);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                };

                changeThread = new Thread(r);
                changeThread.setName("Media change watch daemon");
                changeThread.setDaemon(true);
                changeThread.start();
            } catch (IOException ex) {
                // Swallow this exception. Media changes won't be detected.
            }
        }
    }

}
