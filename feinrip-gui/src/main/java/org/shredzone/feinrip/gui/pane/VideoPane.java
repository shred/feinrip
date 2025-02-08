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
package org.shredzone.feinrip.gui.pane;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

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

    private static final int AUDIO_RESYNC_MAX = 30000;
    private static final int AUDIO_RESYNC_STEP = 100;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private final EnumMap<AspectRatio, JRadioButton> jrAspects = new EnumMap<>(AspectRatio.class);

    private JTextField jtfDimension;
    private JSpinner jspAudioSync;

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

        JPanel jpSync = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        jpSync.setBorder(null);
        {
            jspAudioSync = new JSpinner(new SpinnerNumberModel(
                            0, -AUDIO_RESYNC_MAX, AUDIO_RESYNC_MAX, AUDIO_RESYNC_STEP));
            jspAudioSync.addChangeListener(this::onAudioSyncChange);
            jspAudioSync.setToolTipText(B.getString("pane.video.audioSync.tt"));
            jpSync.add(jspAudioSync);
            jpSync.add(new JLabel(" ms"));
        }
        add(lg = new JLabelGroup(jpSync, B.getString("pane.video.audioSync"), lg));
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

        if (project.getAudioSyncOffset() != 0) {
            String sync = NumberFormat.getIntegerInstance().format(project.getAudioSyncOffset());
            sb.append("<br>").append(B.getString("pane.video.async")).append(' ').append(sync).append(" ms");
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

    private void onAudioSyncChange(ChangeEvent e) {
        project.setAudioSyncOffset(((Number) jspAudioSync.getValue()).intValue());
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

            case "audioSyncOffset":
                jspAudioSync.setValue(project.getAudioSyncOffset());
                updateBody();
                break;
        }
    }

}
