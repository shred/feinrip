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
package org.shredzone.feinrip.dvb.si.descriptor;

import java.io.IOException;

import org.shredzone.feinrip.dvb.DvbInputStream;
import org.shredzone.feinrip.dvb.si.ContentDescription;

/**
 * A Content Descriptor.
 *
 * @author Richard "Shred" Körber
 * @see <a href="http://www.etsi.org/deliver/etsi_en/300400_300499/300468/01.12.01_40/en_300468v011201o.pdf">ETSI EN 300 468, Chapter 6.2.9</a>
 */
public class ContentDescriptor implements Descriptor {

    public static final int TAG = 0x54;

    private int length;
    private ContentDescription[] items;

    @Override
    public void read(DvbInputStream in) throws IOException {
        length = in.readUnsignedByte();

        int number = length / 2;
        items = new ContentDescription[number];
        for (int ix = 0; ix < number; ix++) {
            items[ix] = new ContentDescription(in.readUnsignedShort());
        }
    }

    @Override
    public int getLength() {
        return length;
    }

    public ContentDescription[] getItems()      { return items; }
    public void setItems(ContentDescription[] items) { this.items = items; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ContentDescriptor:[");
        boolean needsComma = false;
        for (ContentDescription cd : items) {
            if (needsComma) {
                sb.append(',');
            }
            sb.append(cd.toString());
            needsComma = true;
        }
        sb.append(']');
        return sb.toString();
    }

}
