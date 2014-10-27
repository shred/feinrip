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
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.shredzone.feinrip.gui.model.PowerTabModel;
import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.source.Source;

/**
 * Abstract superclass for all PowerPanes.
 *
 * @author Richard "Shred" Körber
 */
public abstract class PowerPane extends JPanel implements PropertyChangeListener {
    private static final long serialVersionUID = 9124999414210494139L;
    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private final PowerTabModel powerTabModel = new PowerTabModel();
    protected final Project project;

    /**
     * Gets the unique name of the PowerPane.
     */
    public String getPaneName() {
        Pane anno = getClass().getAnnotation(Pane.class);
        if (anno == null) {
            throw new IllegalStateException("No @PaneName at " + getClass().getName());
        }

        return anno.name();
    }

    /**
     * Gets the {@link PowerTabModel} of this PowertTab.
     */
    public PowerTabModel getPowerTabModel() {
        return powerTabModel;
    }

    /**
     * Creates a new {@link PowerPane}.
     *
     * @param project
     *            {@link Project} this PowerPane is bound to
     */
    public PowerPane(Project project) {
        this.project = project;
        project.addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        Pane anno = getClass().getAnnotation(Pane.class);
        if (anno != null) {
            if (!anno.icon().isEmpty()) {
                powerTabModel.setIcon(new ImageIcon(PowerPane.class.getResource("/org/shredzone/feinrip/icon/" + anno.icon())));
            }

            if (!anno.title().isEmpty()) {
                powerTabModel.setTitle(B.getString(anno.title()));
            }
        }
    }

    /**
     * Invoked on all changes of {@link Project} and {@link Source} properties.
     * <p>
     * The default implementation does nothing.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Does nothing by default
    }

}
