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
import java.beans.PropertyChangeEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.shredzone.feinrip.gui.JLanguageEditableTable;
import org.shredzone.feinrip.gui.JToolbarButton;
import org.shredzone.feinrip.gui.action.TableSelectAllAction;
import org.shredzone.feinrip.gui.model.AudioTableModel;
import org.shredzone.feinrip.model.Audio;
import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.source.Source;

/**
 * PowerPane for configurating audio settings.
 *
 * @author Richard "Shred" Körber
 */
@Pane(name = "audio", title = "pane.audio", icon = "audio.png")
public class AudioPane extends PowerPane {
    private static final long serialVersionUID = -390298616306980792L;

    private static final int MAX_AUDIO_BODY = 9;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private JLanguageEditableTable jtAudio;
    private TableSelectAllAction actionSelectAll;
    private TableSelectAllAction actionUnselectAll;

    public AudioPane(Project project) {
        super(project);
        setup();
        updateBody();
    }

    private void setup() {
        JToolBar jtbTools = new JToolBar();
        jtbTools.setFloatable(false);
        {
            actionSelectAll = new TableSelectAllAction(true);
            jtbTools.add(new JToolbarButton(actionSelectAll));

            actionUnselectAll = new TableSelectAllAction(false);
            jtbTools.add(new JToolbarButton(actionUnselectAll));
        }
        add(jtbTools, BorderLayout.NORTH);

        jtAudio = new JLanguageEditableTable();
        add(new JScrollPane(jtAudio), BorderLayout.CENTER);
    }

    private void updateSource() {
        AudioTableModel model = new AudioTableModel(project);
        jtAudio.setModel(model);
        actionSelectAll.setModel(model);
        actionUnselectAll.setModel(model);
    }

    private void updateBody() {
        StringBuilder sb = new StringBuilder("<html>");
        int count = 0;

        Audio defAudio = project.getDefAudio();
        for (Audio audio : project.getAudios()) {
            if (audio.isEnabled()) {
                if (count++ < MAX_AUDIO_BODY) {
                    sb.append(audio.getIx()).append(": ");
                    if (audio == defAudio) {
                        sb.append("<b>").append(audio.getLanguage().toShortString()).append("</b>");
                    } else {
                        sb.append(audio.getLanguage().toShortString());
                    }
                    sb.append(" - ").append(audio.getFormat());
                    sb.append(' ').append(audio.getChannels()).append("ch");
                    sb.append("<br>");
                }
            }
        }

        if (count == 0) {
            sb.append("<i><b>").append(B.getString("pane.audio.empty")).append("</b></i>");
        } else if (count > MAX_AUDIO_BODY) {
            sb.append("<i>");
            sb.append(MessageFormat.format(
                            B.getString("pane.audio.more"),
                            count - MAX_AUDIO_BODY));
            sb.append("</i>");
        }

        getPowerTabModel().setBody(sb.toString());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "audios":
            case "source":
                updateSource();
                // falls through...

            case "defAudio":
                updateBody();
                break;
        }

        if (evt.getSource() instanceof Source) {
            updateSource();
        }
    }

}
