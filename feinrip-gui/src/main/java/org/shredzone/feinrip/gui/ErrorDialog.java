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
     * Show an error.
     *
     * @param title
     *            Resource name of the error title
     * @param message
     *            Resource name of the error content
     * @param args
     *            {@link MessageFormat} arguments for the error content
     */
    public static void showError(String title, String message, Object... args) {
        JOptionPane.showMessageDialog(null,
                        MessageFormat.format(B.getString(message), args),
                        B.getString(title),
                        JOptionPane.ERROR_MESSAGE);
    }

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
