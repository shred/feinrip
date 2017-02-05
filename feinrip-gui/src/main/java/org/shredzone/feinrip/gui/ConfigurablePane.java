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

import java.awt.Component;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Marks a pane that provides a configuration pane to be shown in the settings pane.
 *
 * @author Richard "Shred" Körber
 */
public interface ConfigurablePane {

    /**
     * Returns a {@link Component} to be used for configuration.
     *
     * @param lgRef Reference to a {@link JLabelGroup} for global aligning
     */
    Component getConfigurationPane(AtomicReference<JLabelGroup> lgRef);

}
