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
package org.shredzone.feinrip.gui.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.source.Source;

/**
 * Action that proposes a title based on the selected source.
 *
 * @author Richard "Shred" Körber
 */
public class ProposeTitleAction extends AbstractSyncAction implements PropertyChangeListener {
    private static final long serialVersionUID = -3157027774024103605L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final Icon proposeIcon = new ImageIcon(ProposeTitleAction.class.getResource("/org/shredzone/feinrip/icon/propose.png"));

    private final Project project;

    public ProposeTitleAction(Project project) {
        super(B.getString("action.propose.title"), proposeIcon);
        setToolTipText(B.getString("action.propose.tt"));

        this.project = project;
        project.addPropertyChangeListener(this);
        updateEnabled();
    }

    private void updateEnabled() {
        boolean before = isEnabled();
        boolean after = (project.getSource() != null && project.getSource().isValid());
        setEnabled(after);

        if (before != after && (project.getTitle() == null || project.getTitle().trim().isEmpty())) {
            project.setTitle(project.getSource().getTitleProposal());
        }
    }

    @Override
    public void onAction(ActionEvent e) {
        project.setTitle(project.getSource().getTitleProposal());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "source":
                updateEnabled();
        }

        if (evt.getSource() instanceof Source) {
            updateEnabled();
        }
    }

}
