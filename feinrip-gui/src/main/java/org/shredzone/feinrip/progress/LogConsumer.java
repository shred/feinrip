/*
 * feinrip
 *
 * Copyright (C) 2014 Richard "Shred" Körber
 *   https://github.com/shred/feinrip
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
package org.shredzone.feinrip.progress;

import java.util.function.Consumer;

/**
 * A {@link Consumer} that logs into a {@link ProgressMeter}.
 *
 * @author Richard "Shred" Körber
 */
public class LogConsumer implements Consumer<String> {

    protected final ProgressMeter meter;
    protected final boolean error;

    /**
     * Creates a new {@link LogConsumer}.
     *
     * @param meter
     *            {@link ProgressMeter} to log into
     * @param error
     *            {@code true}: this {@link Consumer} logs errors, {@code false}:
     *            information and warnings
     */
    public LogConsumer(ProgressMeter meter, boolean error) {
        this.meter = meter;
        this.error = error;
    }

    @Override
    public void accept(String line) {
        if (!line.trim().isEmpty()) {
            meter.log(line);
        }
    }

}
