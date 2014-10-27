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
package org.shredzone.feinrip.dvb.si;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.shredzone.feinrip.dvb.DvbInputStream;
import org.shredzone.feinrip.dvb.si.descriptor.Descriptor;

/**
 * A single Event Information from the Event Information Table (EIT).
 *
 * @author Richard "Shred" Körber
 * @see <a href="http://www.etsi.org/deliver/etsi_en/300400_300499/300468/01.12.01_40/en_300468v011201o.pdf">ETSI EN 300 468, Chapter 5.2.4</a>
 */
public class EventInformation {

    private int eventId;
    private Date startTime;
    private int duration;
    private RunningStatus runningStatus;
    private boolean freeCAmode;
    private List<Descriptor> descriptors = new ArrayList<Descriptor>();

    /**
     * Unmarshalls from the given {@link DvbInputStream}.
     *
     * @param in
     *              {@link DvbInputStream} to read from
     */
    public void read(DvbInputStream in) throws IOException {
        eventId = in.readUnsignedShort();
        startTime = in.readDateTime();
        duration = in.readDuration();

        int status = in.readUnsignedShort();

        runningStatus = RunningStatus.values()[(status >> 13) & 0x07];
        freeCAmode = (status & 0x1000) != 0;

        int length = status & 0x0FFF;

        while (length > 0) {
            Descriptor desc = in.readDescriptor();
            descriptors.add(desc);
            length -= desc.getLength() + 2;
        }
    }

    public List<Descriptor> getDescriptors()    { return descriptors;  }
    public void setDescriptors(List<Descriptor> descriptors) { this.descriptors = descriptors;  }

    public int getDuration()                    { return duration; }
    public void setDuration(int duration)       { this.duration = duration; }

    public int getEventId()                     { return eventId; }
    public void setEventId(int eventId)         { this.eventId = eventId; }

    public boolean isFreeCAmode()               { return freeCAmode; }
    public void setFreeCAmode(boolean freeCAmode) { this.freeCAmode = freeCAmode; }

    public RunningStatus getRunningStatus()     { return runningStatus; }
    public void setRunningStatus(RunningStatus runningStatus) { this.runningStatus = runningStatus; }

    public Date getStartTime()                  { return startTime; }
    public void setStartTime(Date startTime)    { this.startTime = startTime; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EventInformation:[");
        sb.append("id=").append(String.format("%04X", eventId)).append(',');
        sb.append("start=").append(startTime.toString()).append(',');
        sb.append("duration=").append(duration).append("s,");
        sb.append("runningStatus=").append(runningStatus.toString()).append(',');
        sb.append(freeCAmode ? "encrypted" : "free");
        for (Descriptor d : descriptors) {
            sb.append(',');
            sb.append(d.toString());
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Enumeration of Running Status.
     */
    public static enum RunningStatus {
        UNDEFINED, NOT_RUNNING, STARTS_IN_A_FEW_SECONDS, PAUSING, RUNNING,
        OFF_AIR, RESERVED_6, RESERVED_7;
    }

}
