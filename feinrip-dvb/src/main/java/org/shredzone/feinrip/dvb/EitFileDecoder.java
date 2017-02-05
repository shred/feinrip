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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.shredzone.feinrip.dvb.si.EventInformation;
import org.shredzone.feinrip.dvb.si.descriptor.ContentDescriptor;
import org.shredzone.feinrip.dvb.si.descriptor.Descriptor;
import org.shredzone.feinrip.dvb.si.descriptor.ExtendedEventDescriptor;
import org.shredzone.feinrip.dvb.si.descriptor.ShortEventDescriptor;

/**
 * Example for reading an .eit file and printing out some of its contents.
 *
 * @author Richard "Shred" Körber
 */
public class EitFileDecoder {

    private static final DateFormat formatter = SimpleDateFormat.getDateTimeInstance();

    /**
     * Decode a single eit file.
     *
     * @param file
     *          File to decode
     */
    public static void decodeFile(File file) throws IOException {
        DvbInputStream in = new DvbInputStream(new FileInputStream(file));
        try {
            EventInformation ei = new EventInformation();
            ei.read(in);

            StringBuilder details = new StringBuilder();

            for (Descriptor d : ei.getDescriptors()) {
                if (d instanceof ShortEventDescriptor) {
                    System.out.println("Movie:   " + ((ShortEventDescriptor) d).getEventName());
                    System.out.println("         " + ((ShortEventDescriptor) d).getText());
                }
                if (d instanceof ExtendedEventDescriptor) {
                    details.append(((ExtendedEventDescriptor) d).getText());
                }
                if (d instanceof ContentDescriptor) {
                    // TODO
                }
            }

            System.out.println("Date:    " + formatter.format(ei.getStartTime()));
            System.out.println("Runtime: " + ei.getDuration() / 60 + " min");

            if (details.length() > 0) {
                System.out.println();
                System.out.println(details);
            }

        } finally {
            in.close();
        }
    }

    public static void main(String[] args) {
        for (String name : args) {
            System.out.println("File:    " + name);
            System.out.println();
            try {
                decodeFile(new File(name));
            } catch (IOException ex) {
                System.err.println("IO-Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            }
            System.out.println("\n\n");
        }
    }

}
