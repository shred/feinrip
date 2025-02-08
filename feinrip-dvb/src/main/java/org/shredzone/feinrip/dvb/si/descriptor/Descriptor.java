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

/**
 * Common interface that is implemented by all descriptors.
 *
 * @author Richard "Shred" Körber
 * @see <a href="http://www.etsi.org/deliver/etsi_en/300400_300499/300468/01.12.01_40/en_300468v011201o.pdf">ETSI EN 300 468, Chapter 6</a>
 */
public interface Descriptor {

    /**
     * Unmarshalls a Descriptor from the {@link DvbInputStream}.
     *
     * @param in
     *              {@link DvbInputStream} to read from
     */
    void read(DvbInputStream in) throws IOException;

    /**
     * The length of the current descriptor content.
     *
     * @return Length of the descriptor's data portion. The descriptor tag and
     *         length byte are not included.
     */
    int getLength();

}
