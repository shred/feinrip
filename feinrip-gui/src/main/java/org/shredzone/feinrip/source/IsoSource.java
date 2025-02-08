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
package org.shredzone.feinrip.source;

import java.io.File;

import org.shredzone.feinrip.model.MountPoint;
import org.shredzone.feinrip.model.StreamType;

/**
 * A {@link Source} for a DVD structured directory.
 *
 * @author Richard "Shred" Körber
 */
public class IsoSource extends DvdSource {

    public void setMountPoint(File base) {
        setMountPoint(new MountPoint(base, base));
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.DVD;
    }

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        return getMountPoint().exists() && getMountPoint().isDirectory();
    }

    @Override
    public String getHtmlDescription() {
        if (getMountPoint() == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder("<html>");
        sb.append(getMountPoint().getName()).append("<br>");
        sb.append("Track: ").append(getSelectedTrackNr());
        return sb.toString();
    }

}
