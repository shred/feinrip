/*
 * feinrip
 *
 * Copyright (C) 2014 Richard "Shred" Körber
 *   https://codeberg.org/shred/feinrip
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
package org.shredzone.feinrip.model;

import java.io.File;

/**
 * A mount point contains the device and mount point of a DVD.
 * <p>
 * Instances are immutable.
 *
 * @author Richard "Shred" Körber
 */
public class MountPoint {
    private final File device;
    private final File mount;

    /**
     * Creates a new {@link MountPoint}.
     *
     * @param device
     *            Device of the DVD drive
     * @param mount
     *            Mount point where the DVD has been mounted
     */
    public MountPoint(File device, File mount) {
        this.device = device;
        this.mount = mount;
    }

    public File getDevice()                 { return device; }
    public File getMount()                  { return mount; }

}
