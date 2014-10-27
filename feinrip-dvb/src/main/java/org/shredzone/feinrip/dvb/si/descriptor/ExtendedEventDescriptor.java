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
package org.shredzone.feinrip.dvb.si.descriptor;

import java.io.IOException;

import org.shredzone.feinrip.dvb.DvbInputStream;

/**
 * An Extended Event Descriptor.
 *
 * @author Richard "Shred" Körber
 * @see <a href="http://www.etsi.org/deliver/etsi_en/300400_300499/300468/01.12.01_40/en_300468v011201o.pdf">ETSI EN 300 468, Chapter 6.2.15</a>
 */
public class ExtendedEventDescriptor implements Descriptor {

    public static final int TAG = 0x4E;

    private int length;
    private int descriptorNumber;
    private int lastDescriptorNumber;
    private String language;
    private String[] itemDescription;
    private String[] item;
    private String text;

    @Override
    public void read(DvbInputStream in) throws IOException {
        length = in.readUnsignedByte();

        int dn = in.readUnsignedByte();
        descriptorNumber = (dn >> 4) & 0x0F;
        lastDescriptorNumber = dn & 0x0F;

        language = in.readLanguageCode();

        int loi = in.readUnsignedByte();
        itemDescription = new String[loi];
        item = new String[loi];

        for (int ix = 0; ix < loi; ix++) {
            itemDescription[ix] = in.readEncodedString();
            item[ix] = in.readEncodedString();
        }

        text = in.readEncodedString();
    }

    @Override
    public int getLength() {
        return length;
    }

    public int getDescriptorNumber()            { return descriptorNumber; }
    public void setDescriptorNumber(int descriptorNumber) { this.descriptorNumber = descriptorNumber; }

    public String[] getItem()                   { return item; }
    public void setItem(String[] item)          { this.item = item; }

    public String[] getItemDescription()        { return itemDescription; }
    public void setItemDescription(String[] itemDescription) { this.itemDescription = itemDescription; }

    public String getLanguage()                 { return language; }
    public void setLanguage(String language)    { this.language = language; }

    public int getLastDescriptorNumber()        { return lastDescriptorNumber; }
    public void setLastDescriptorNumber(int lastDescriptorNumber) { this.lastDescriptorNumber = lastDescriptorNumber; }

    public String getText()                     { return text; }
    public void setText(String text)            { this.text = text; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExtendedEventDescriptor:[");
        sb.append("descriptorNumber=").append(descriptorNumber).append('/').append(lastDescriptorNumber).append(',');
        for (int ix = 0; ix < item.length; ix++) {
            sb.append(itemDescription[ix]).append('=').append(item[ix]).append(',');
        }
        sb.append("text(").append(language).append(")='").append(text).append("'");
        sb.append(']');
        return sb.toString();
    }

}
