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

/**
 * A factory class for creating {@link Descriptor} objects.
 *
 * @author Richard "Shred" Körber
 */
public final class DescriptorFactory {

    /**
     * Creates an {@link Descriptor}.
     *
     * @param tag
     *              Descriptor tag number
     * @return {@link Descriptor}, or {@code null} if the tag is not supported
     */
    public static Descriptor createDescriptor(int tag) {
        switch (tag) {
            case ShortEventDescriptor.TAG:      return new ShortEventDescriptor();          // 0x4D
            case ExtendedEventDescriptor.TAG:   return new ExtendedEventDescriptor();       // 0x4E
            case ComponentDescriptor.TAG:       return new ComponentDescriptor();           // 0x50
            case ContentDescriptor.TAG:         return new ContentDescriptor();             // 0x54

            default:                            return null;
        }
    }

}
