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
package org.shredzone.feinrip.gui.pane;

import java.awt.BorderLayout;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.shredzone.feinrip.gui.FeinripLogo;
import org.shredzone.feinrip.model.Project;

/**
 * PowerPane for about info.
 *
 * @author Richard "Shred" Körber
 */
@Pane(name = "about", title = "pane.about", icon = "about.png")
public class AboutPane extends PowerPane {
    private static final long serialVersionUID = 1589160560468756941L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    public AboutPane(Project project) {
        super(project);
        setLayout(new BorderLayout());

        FeinripLogo logo = new FeinripLogo(50.0f);
        add(logo, BorderLayout.NORTH);

        String info = MessageFormat.format(B.getString("pane.about.info"), new Date());
        JLabel jlInfo = new JLabel(info);
        jlInfo.setHorizontalAlignment(SwingConstants.CENTER);
        jlInfo.setVerticalAlignment(SwingConstants.TOP);
        jlInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(jlInfo, BorderLayout.CENTER);
    }

}
