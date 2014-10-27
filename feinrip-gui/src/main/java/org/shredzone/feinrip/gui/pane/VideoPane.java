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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.EnumMap;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.shredzone.feinrip.gui.JLabelGroup;
import org.shredzone.feinrip.model.AspectRatio;
import org.shredzone.feinrip.model.Project;

/**
 * PowerPane for configurating video settings.
 *
 * @author Richard "Shred" Körber
 */
@Pane(name = "video", title = "pane.video", icon = "video.png")
public class VideoPane extends PowerPane {
    private static final long serialVersionUID = -1015879881465391175L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private final EnumMap<AspectRatio, JRadioButton> jrAspects = new EnumMap<>(AspectRatio.class);

    private JTextField jtfDimension;

    public VideoPane(Project project) {
        super(project);
        setup();
        updateBody();
    }

    private void setup() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabelGroup lg = null;

        jtfDimension = new JTextField();
        jtfDimension.setEditable(false);
        add(lg = new JLabelGroup(jtfDimension, B.getString("pane.video.size"), lg));
        JLabelGroup.setMinimumHeight(lg);

        JPanel jpRadios = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup aspectGroup = new ButtonGroup();
        for (AspectRatio ar : AspectRatio.values()) {
            JRadioButton rb = new JRadioButton(ar.toString());
            rb.addActionListener(this::onAspectRatioAction);
            jrAspects.put(ar, rb);
            aspectGroup.add(rb);
            jpRadios.add(rb);
        }
        jrAspects.get(AspectRatio.ASPECT_4_3).setSelected(true);

        add(lg = new JLabelGroup(jpRadios, B.getString("pane.video.aspect"), lg));
        JLabelGroup.setMinimumHeight(lg);

        lg.rearrange();
    }

    private void updateBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(B.getString("pane.video.asp")).append(' ').append(project.getAspect());

        if (project.getSize() != null) {
            String size = String.format("%d\u00D7%d", project.getSize().width, project.getSize().height);
            jtfDimension.setText(size);
            sb.append("<br>").append(B.getString("pane.video.res")).append(' ').append(size);
        }

        getPowerTabModel().setBody(sb.toString());
    }

    private void onAspectRatioAction(ActionEvent e) {
        Object src = e.getSource();
        jrAspects.keySet().stream()
                .filter(ar -> jrAspects.get(ar) == src)
                .findAny()
                .ifPresent(ar -> project.setAspect(ar));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "size":
                updateBody();
                break;

            case "aspect":
                JRadioButton rb = jrAspects.get(project.getAspect());
                if (rb != null) {
                    rb.setSelected(true);
                }
                updateBody();
                break;
        }
    }

}
