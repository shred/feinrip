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

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A logger for the lsdvd package.
 * <p>
 * It usually only logs warnings to the console. However with the system property
 * "feinrip.debug" set to anything, debug log level is activated.
 *
 * @author Richard "Shred" Körber
 */
public class LsdvdLogger extends Logger {

    public static final String DEBUG_PROPERTY_NAME = "feinrip.debug";

    /**
     * Creates a new logger.
     *
     * @param clazz
     *            Class this logger is bound to
     */
    public LsdvdLogger(Class<?> clazz) {
        super(clazz.getName(), null);

        ConsoleHandler ch = new ConsoleHandler();

        if (System.getProperty(DEBUG_PROPERTY_NAME) != null) {
            ch.setLevel(Level.FINE);
        } else {
            ch.setLevel(Level.WARNING);
        }

        ch.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                StringBuilder sb = new StringBuilder();
                sb.append(record.getLevel().getName().substring(0, 1));
                sb.append(": ");
                sb.append(String.format(record.getMessage(), record.getParameters()));
                sb.append('\n');
                return sb.toString();
            }
        });

        setLevel(Level.ALL);
        setUseParentHandlers(false);
        addHandler(ch);
    }

    /**
     * Checks if DEBUG log level is activated.
     */
    public boolean isDebug() {
        return isLoggable(Level.FINE);
    }

    /**
     * Logs on debug level.
     */
    public void debug(String msg, Object... args) {
        if (isLoggable(Level.FINE)) {
            log(Level.FINE, msg, args);
        }
    }

    /**
     * Logs on info level.
     */
    public void info(String msg, Object... args) {
        if (isLoggable(Level.INFO)) {
            log(Level.INFO, msg, args);
        }
    }

    /**
     * Logs on warn level.
     */
    public void warn(String msg, Object... args) {
        if (isLoggable(Level.WARNING)) {
            log(Level.WARNING, msg, args);
        }
    }

}
