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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.shredzone.feinrip.gui.action.StartAction;
import org.shredzone.feinrip.gui.pane.AboutPane;
import org.shredzone.feinrip.gui.pane.AudioPane;
import org.shredzone.feinrip.gui.pane.ChapterPane;
import org.shredzone.feinrip.gui.pane.PowerPane;
import org.shredzone.feinrip.gui.pane.ProgressPane;
import org.shredzone.feinrip.gui.pane.SettingsPane;
import org.shredzone.feinrip.gui.pane.SourcePane;
import org.shredzone.feinrip.gui.pane.SubPane;
import org.shredzone.feinrip.gui.pane.TargetPane;
import org.shredzone.feinrip.gui.pane.TitlePane;
import org.shredzone.feinrip.gui.pane.VideoPane;
import org.shredzone.feinrip.model.Project;

/**
 * Main pane of feinrip. Each {@link FeinripPane} is bound to one {@link Project}.
 *
 * @author Richard "Shred" Körber
 */
public class FeinripPane extends JPanel {
    private static final long serialVersionUID = 504145006959520172L;

    private final Project project = new Project();

    private PowerTabPane jTabs;
    private String oldTab;
    private SettingsPane settingsPane;

    public FeinripPane() {
        settingsPane = new SettingsPane(project);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 0));

        jTabs = new PowerTabPane();
        add(jTabs, BorderLayout.CENTER);

        addPlain(jTabs, new FeinripLogo());
        addTab(jTabs, new SourcePane(project));
        addTab(jTabs, new TitlePane(project));
        addTab(jTabs, new VideoPane(project));
        addTab(jTabs, new AudioPane(project));
        addTab(jTabs, new SubPane(project));
        addTab(jTabs, new ChapterPane(project));
        addTab(jTabs, new TargetPane(project));
        addActionTab(jTabs, new AboutPane(project));
        addActionTab(jTabs, settingsPane);

        ProgressPane progress = new ProgressPane(project);
        addStealthTab(jTabs, progress);

        JButton jbStart = new JButton(new StartAction(project, this, progress));
        jbStart.setHorizontalTextPosition(SwingConstants.LEFT);
        jbStart.setMaximumSize(new Dimension(Integer.MAX_VALUE, jbStart.getMaximumSize().height));
        addActionPlain(jTabs, jbStart);
    }

    /**
     * Locks the powerpane and shows the stealthy progress pane. The currently selected
     * powerpane is remembered.
     */
    public void showProgressDialog() {
        oldTab = jTabs.getSelectedTab();
        jTabs.setEnabled(false);
        jTabs.setSelectedTab("progress");
    }

    /**
     * Unlocks the powerpane and shows the pane that was shown when the powerpane was
     * locked.
     */
    public void hideProgressDialog() {
        jTabs.setSelectedTab(oldTab);
        jTabs.setEnabled(true);
        oldTab = null;
    }

    private void addTab(PowerTabPane tabbedPane, PowerPane pane) {
        tabbedPane.addTab(pane.getPaneName(), pane.getPowerTabModel(), pane);
        if (pane instanceof ConfigurablePane) {
            settingsPane.addConfigurablePane((ConfigurablePane) pane);
        }
    }

    private void addPlain(PowerTabPane tabbedPane, Component comp) {
        tabbedPane.addPlain(comp);
    }

    private void addActionTab(PowerTabPane tabbedPane, PowerPane pane) {
        tabbedPane.addTab(pane.getPaneName(), pane.getPowerTabModel(), pane, true);
        if (pane instanceof ConfigurablePane) {
            settingsPane.addConfigurablePane((ConfigurablePane) pane);
        }
    }

    private void addActionPlain(PowerTabPane tabbedPane, Component comp) {
        tabbedPane.addPlain(comp, true);
    }

    private void addStealthTab(PowerTabPane tabbedPane, PowerPane pane) {
        tabbedPane.addStealthTab(pane.getPaneName(), pane);
        if (pane instanceof ConfigurablePane) {
            settingsPane.addConfigurablePane((ConfigurablePane) pane);
        }
    }

}
