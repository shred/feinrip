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
 * A Component Descriptor.
 *
 * @author Richard "Shred" Körber
 * @see <a href="http://www.etsi.org/deliver/etsi_en/300400_300499/300468/01.12.01_40/en_300468v011201o.pdf">ETSI EN 300 468, Chapter 6.2.8</a>
 */
public class ComponentDescriptor implements Descriptor {

    public static final int TAG = 0x50;

    private int length;
    private int streamContent;
    private int componentType;
    private int componentTag;
    private String language;
    private String text;

    @Override
    public void read(DvbInputStream in) throws IOException {
        length = in.readUnsignedByte();

        int sc = in.readUnsignedByte();
        streamContent = sc & 0x0F;
        componentType = in.readUnsignedByte();
        componentTag = in.readUnsignedByte();

        language = in.readLanguageCode();
        text = in.readEncodedString(length - 6);
    }

    @Override
    public int getLength() {
        return length;
    }

    public int getComponentTag()                { return componentTag; }
    public void setComponentTag(int componentTag) { this.componentTag = componentTag; }

    public int getComponentType()               { return componentType; }
    public void setComponentType(int componentType) { this.componentType = componentType; }

    public String getLanguage()                 { return language; }
    public void setLanguage(String language)    { this.language = language; }

    public int getStreamContent()               { return streamContent; }
    public void setStreamContent(int streamContent) { this.streamContent = streamContent; }

    public String getText()                     { return text; }
    public void setText(String text)            { this.text = text; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ComponentDescriptor:[");
        sb.append("stream=").append(streamContent).append(':').append(componentType).append(':').append(componentTag).append(',');
        sb.append("text(").append(language).append(")='").append(text).append("'");
        sb.append(']');
        return sb.toString();
    }

}
