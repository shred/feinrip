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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BoxLayout;

import org.shredzone.feinrip.gui.ConfigurablePane;
import org.shredzone.feinrip.gui.JLabelGroup;
import org.shredzone.feinrip.model.Project;

/**
 * PowerPane for configurating generic settings.
 *
 * @author Richard "Shred" Körber
 */
@Pane(name = "settings", title = "pane.settings", icon = "settings.png")
public class SettingsPane extends PowerPane {
    private static final long serialVersionUID = -8921287708996834749L;

    public SettingsPane(Project project) {
        super(project);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /**
     * Adds a {@link ConfigurablePane} to the set of configurable panes.
     */
    public void addConfigurablePane(ConfigurablePane pane, AtomicReference<JLabelGroup> lgRef) {
        add(Objects.requireNonNull(pane).getConfigurationPane(lgRef));
    }

}
