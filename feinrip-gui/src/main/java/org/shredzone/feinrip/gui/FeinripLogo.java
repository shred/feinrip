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

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Renders the feinrip logo.
 *
 * @author Richard "Shred" Körber
 */
public class FeinripLogo extends JLabel {
    private static final long serialVersionUID = 7792266734117853560L;
    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private static final Font LOGO_FONT;

    static {
        Font font;
        try (InputStream fontStream = FeinripLogo.class.getResourceAsStream("/font/Calligraffiti.ttf")) {
            font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (IOException | FontFormatException ex) {
            font = Font.getFont(Font.SERIF);
        }
        LOGO_FONT = font;
    }

    public FeinripLogo() {
        this(24.0f);
    }

    public FeinripLogo(float size) {
        setFont(LOGO_FONT.deriveFont(Font.BOLD, size));
        setText(B.getString("title"));
        setHorizontalAlignment(SwingConstants.CENTER);
    }

}
