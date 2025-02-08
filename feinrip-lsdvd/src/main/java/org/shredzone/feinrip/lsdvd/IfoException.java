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
package org.shredzone.feinrip.lsdvd;

import java.io.IOException;

/**
 * This exception is raised when the IFO file itself could be read, but did not contain
 * usable data. This may be a hint to use the BUP file instead.
 *
 * @author Richard "Shred" Körber
 */
public class IfoException extends IOException {
    private static final long serialVersionUID = 6542488780817607613L;

    public IfoException(String msg) {
        super(msg);
    }

}
