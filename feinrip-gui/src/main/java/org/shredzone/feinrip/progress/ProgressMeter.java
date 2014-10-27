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

/**
 * Interface of a progress meter.
 *
 * @author Richard "Shred" Körber
 */
public interface ProgressMeter {

    /**
     * Shows a message string.
     *
     * @param message
     *            Message to be shown, {@code null} for no message
     * @param values
     *            Values for the formatter
     * @return {@code this}
     */
    ProgressMeter message(String message, Object... values);

    /**
     * Shows a percent value.
     *
     * @param percent
     *            Progress percent, {@code null} for no percentage
     * @return {@code this}
     */
    ProgressMeter percent(Float percent);

    /**
     * Logs a line to a log pane.
     *
     * @param line
     *            Line to be logged
     * @return {@code this}
     */
    ProgressMeter log(String line);

}
