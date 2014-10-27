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
 * A Content Descriptor.
 *
 * @author Richard "Shred" Körber
 * @see <a href="http://www.etsi.org/deliver/etsi_en/300400_300499/300468/01.12.01_40/en_300468v011201o.pdf">ETSI EN 300 468, Chapter 6.2.37</a>
 */
public class ShortEventDescriptor implements Descriptor {

    public static final int TAG = 0x4D;

    private int length;
    private String language;
    private String eventName;
    private String text;

    @Override
    public void read(DvbInputStream in) throws IOException {
        length = in.readUnsignedByte();
        language = in.readLanguageCode();
        eventName = in.readEncodedString();
        text = in.readEncodedString();
    }

    @Override
    public int getLength() {
        return length;
    }

    public String getEventName()                { return eventName; }
    public void setEventName(String eventName)  { this.eventName = eventName; }

    public String getLanguage()                 { return language; }
    public void setLanguage(String language)    { this.language = language; }

    public String getText()                     { return text; }
    public void setText(String text)            { this.text = text; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ShortEventDescriptor:[");
        sb.append("eventName='").append(eventName).append("',");
        sb.append("text(").append(language).append(")='").append(text).append("'");
        sb.append(']');
        return sb.toString();
    }

}
