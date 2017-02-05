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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.shredzone.feinrip.gui.model.PowerTabModel;

/**
 * PowerTabs are similar to standard tabbed panes. However, besides an icon and a title,
 * each tab may contain a body with multiple lines of information.
 * <p>
 * PowerTabs are always rendered at the right and with a fixed width.
 * <p>
 * There is a list area at the top of the PowerTabs, and an action area at the bottom.
 *
 * @author Richard "Shred" Körber
 */
public class PowerTabPane extends JPanel {
    private static final long serialVersionUID = -6086300562254539308L;

    private static final int MIN_WIDTH = 150;

    private CardLayout cardLayout;
    private JPanel centerPane;
    private JPanel listPane;
    private JPanel actionPane;
    private HashMap<String, PowerTabModel> modelMap = new HashMap<>();
    private HashMap<PowerTabRenderer, String> rendererMap = new HashMap<>();

    /**
     * Creates a new, empty {@link PowerTabPane}.
     */
    public PowerTabPane() {
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        centerPane = new JPanel(cardLayout);

        JPanel outerListPane = new JPanel(new BorderLayout());
        {
            listPane = new JPanel();
            listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
            outerListPane.add(listPane, BorderLayout.NORTH);

            JPanel dummyPane = new JPanel();
            dummyPane.setBorder(new PowerTabBorder());
            outerListPane.add(dummyPane, BorderLayout.CENTER);

            actionPane = new JPanel();
            actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.Y_AXIS));
            outerListPane.add(actionPane, BorderLayout.SOUTH);
        }

        Dimension min = outerListPane.getMinimumSize();
        min.width = MIN_WIDTH;
        outerListPane.setMinimumSize(min);
        outerListPane.setPreferredSize(min);

        add(centerPane, BorderLayout.CENTER);

// TODO: doesn't work nicely yet...
//        JScrollPane jsp = new JScrollPane(outerListPane);
//        jsp.setBorder(null);
//        add(jsp, BorderLayout.EAST);

        add(outerListPane, BorderLayout.EAST);
    }

    /**
     * Adds a plain component to the {@link PowerTabPane}.
     *
     * @param component
     *            {@link Component} to add
     */
    public void addPlain(Component component) {
        addPlain(component, false);
    }

    /**
     * Adds a plain component to the {@link PowerTabPane}.
     *
     * @param component
     *            {@link Component} to add
     * @param action
     *            {@code true}: add to action pane, {@code false}: add to list pane
     */
    public void addPlain(Component component, boolean action) {
        JPanel jpWrap = new JPanel(new BorderLayout());
        jpWrap.add(component, BorderLayout.CENTER);
        jpWrap.setBorder(new PowerTabBorder());

        if (action) {
            actionPane.add(jpWrap);
        } else {
            listPane.add(jpWrap);
        }
    }


    /**
     * Adds a tab to the {@link PowerTabPane}.
     *
     * @param name
     *            Tab name, used for selecting a tab. Must be unique.
     * @param model
     *            {@link PowerTabModel}, containing all information about a tab
     * @param component
     *            {@link Component} to be added as tab content
     */
    public void addTab(String name, PowerTabModel model, Component component) {
        addTab(name, model, component, false);
    }

    /**
     * Adds a tab to the {@link PowerTabPane}.
     *
     * @param name
     *            Tab name, used for selecting a tab. Must be unique.
     * @param model
     *            {@link PowerTabModel}, containing all information about a tab
     * @param component
     *            {@link Component} to be added as tab content
     * @param action
     *            {@code true}: add to action pane, {@code false}: add to list pane
     */
    public void addTab(String name, PowerTabModel model, Component component, boolean action) {
        if (modelMap.containsKey(name)) {
            throw new IllegalArgumentException("Tab '" + name + "' already added");
        }

        modelMap.put(name, model);

        centerPane.add(component, name);

        if (model != null) {
            PowerTabRenderer renderer = new PowerTabRenderer(model);
            renderer.addActionListener(this::onPowerTabAction);
            if (action) {
                actionPane.add(renderer);
            } else {
                listPane.add(renderer);
            }

            rendererMap.put(renderer, name);
        }

        if (modelMap.size() == 1) {
            setSelectedTab(name);
        }
    }

    /**
     * Adds a stealth tab without visible tab component. This tab can only be selected
     * via {@link #setSelectedTab(String)}. All visible tabs will be deselected.
     *
     * @param name
     *            Tab name, used for selecting a tab. Must be unique.
     * @param component
     *            {@link Component} to be added as tab content
     */
    public void addStealthTab(String name, Component component) {
        addTab(name, null, component);
    }

    /**
     * Selects a tab to be shown.
     *
     * @param name
     *            Tab name, must have been added previously
     */
    public void setSelectedTab(String name) {
        if (!modelMap.containsKey(name)) {
            throw new IllegalArgumentException("Tab '" + name + "' was not added");
        }

        rendererMap.forEach((renderer, rn) -> renderer.setSelected(rn.equals(name)));

        cardLayout.show(centerPane, name);
    }

    /**
     * Gets the name of the currently selected tab.
     *
     * @return Tab name, or {@code null} if (for some strange reason) no tab is currenly
     *         being selected
     */
    public String getSelectedTab() {
        return rendererMap.keySet().stream()
            .filter(renderer -> renderer.isSelected())
            .findFirst()
            .map(renderer -> rendererMap.get(renderer))
            .orElse(null);
    }

    private void onPowerTabAction(ActionEvent e) {
        if (isEnabled()) {
            String name = rendererMap.get(e.getSource());
            setSelectedTab(name);
        }
    }

}
