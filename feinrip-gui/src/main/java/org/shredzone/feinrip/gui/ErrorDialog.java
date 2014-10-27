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
package org.shredzone.feinrip.gui;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

/**
 * Shows an error dialog for exceptions.
 *
 * @author Richard "Shred" Körber
 */
public class ErrorDialog {

    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    /**
     * Show the exception.
     *
     * @param ex
     *            {@link Exception} that occured
     */
    public static void showException(Exception ex) {
        String message = MessageFormat.format(B.getString("error.message"), ex.getMessage());
        JOptionPane.showMessageDialog(null, message, B.getString("error.title"), JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace(); // DEBUG
    }

}
