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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.shredzone.feinrip.gui.ConfigurablePane;
import org.shredzone.feinrip.gui.JLabelGroup;
import org.shredzone.feinrip.gui.source.SourceDvdPane;
import org.shredzone.feinrip.gui.source.SourceIsoPane;
import org.shredzone.feinrip.gui.source.SourceVobPane;
import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.source.Source;

/**
 * PowerPane for configurating source settings.
 *
 * @author Richard "Shred" Körber
 */
@Pane(name = "source", title = "pane.source", icon = "small-dvd.png")
public class SourcePane extends PowerPane implements ConfigurablePane {
    private static final long serialVersionUID = -3519322844850173205L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final ImageIcon dvdIcon = new ImageIcon(SourcePane.class.getResource("/org/shredzone/feinrip/icon/large-dvd.png"));
    private static final ImageIcon dvdIconSmall = new ImageIcon(SourcePane.class.getResource("/org/shredzone/feinrip/icon/small-dvd.png"));
    private static final ImageIcon isoIcon = new ImageIcon(SourcePane.class.getResource("/org/shredzone/feinrip/icon/large-iso.png"));
    private static final ImageIcon isoIconSmall = new ImageIcon(SourcePane.class.getResource("/org/shredzone/feinrip/icon/small-iso.png"));
    private static final ImageIcon vobIcon = new ImageIcon(SourcePane.class.getResource("/org/shredzone/feinrip/icon/large-vob.png"));
    private static final ImageIcon vobIconSmall = new ImageIcon(SourcePane.class.getResource("/org/shredzone/feinrip/icon/small-vob.png"));

    private ButtonGroup sourceGroup;
    private JToggleButton jtbDvdSource;
    private JToggleButton jtbIsoSource;
    private JToggleButton jtbVobSource;
    private CardLayout setupLayout;
    private JPanel jpSetup;

    private SourceDvdPane sourceDvdPane;
    private SourceIsoPane sourceIsoPane;
    private SourceVobPane sourceVobPane;

    public SourcePane(Project project) {
        super(project);
        setup();
        Source source = sourceDvdPane.getSource();
        project.setSource(source);
        source.bind(project);
        updateBody();
    }

    private void setup() {
        JPanel jpSelection = new JPanel();
        jpSelection.setLayout(new BoxLayout(jpSelection, BoxLayout.Y_AXIS));
        jpSelection.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, getBackground().darker()),
                        BorderFactory.createEmptyBorder(1, 3, 1, 3)
                        ));
        {
            sourceGroup = new ButtonGroup();

            jtbDvdSource = new JToggleButton(B.getString("pane.source.dvd"), dvdIcon);
            jtbDvdSource.setHorizontalTextPosition(SwingConstants.CENTER);
            jtbDvdSource.setVerticalTextPosition(SwingConstants.BOTTOM);
            jtbDvdSource.addActionListener(this::onDvdSourceAction);
            sourceGroup.add(jtbDvdSource);
            jpSelection.add(jtbDvdSource);

            jtbIsoSource = new JToggleButton(B.getString("pane.source.iso"), isoIcon);
            jtbIsoSource.setHorizontalTextPosition(SwingConstants.CENTER);
            jtbIsoSource.setVerticalTextPosition(SwingConstants.BOTTOM);
            jtbIsoSource.addActionListener(this::onIsoSourceAction);
            sourceGroup.add(jtbIsoSource);
            jpSelection.add(jtbIsoSource);

            jtbVobSource = new JToggleButton(B.getString("pane.source.vob"), vobIcon);
            jtbVobSource.setHorizontalTextPosition(SwingConstants.CENTER);
            jtbVobSource.setVerticalTextPosition(SwingConstants.BOTTOM);
            jtbVobSource.addActionListener(this::onVobSourceAction);
            sourceGroup.add(jtbVobSource);
            jpSelection.add(jtbVobSource);

            sourceGroup.setSelected(jtbDvdSource.getModel(), true);
        }
        add(jpSelection, BorderLayout.WEST);

        setupLayout = new CardLayout();
        jpSetup = new JPanel(setupLayout);
        jpSetup.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        {
            sourceDvdPane = new SourceDvdPane();
            jpSetup.add(sourceDvdPane, "dvd");

            sourceIsoPane = new SourceIsoPane();
            jpSetup.add(sourceIsoPane, "iso");

            sourceVobPane = new SourceVobPane();
            jpSetup.add(sourceVobPane, "vob");
        }
        add(jpSetup, BorderLayout.CENTER);
    }

    @Override
    public Component getConfigurationPane(AtomicReference<JLabelGroup> lgRef) {
        return sourceDvdPane.getConfigurationPane(lgRef);
    }

    private void updateBody() {
        if (project.getSource() != null) {
            getPowerTabModel().setBody(project.getSource().getHtmlDescription());
        }
    }

    private void onDvdSourceAction(ActionEvent e) {
        if (jtbDvdSource.isSelected()) {
            setupLayout.show(jpSetup, "dvd");
            getPowerTabModel().setIcon(dvdIconSmall);
            Source source = sourceDvdPane.getSource();
            project.setSource(source);
            source.bind(project);
        }
    }

    private void onIsoSourceAction(ActionEvent e) {
        if (jtbIsoSource.isSelected()) {
            setupLayout.show(jpSetup, "iso");
            getPowerTabModel().setIcon(isoIconSmall);
            Source source = sourceIsoPane.getSource();
            project.setSource(source);
            source.bind(project);
        }
    }

    private void onVobSourceAction(ActionEvent e) {
        if (jtbVobSource.isSelected()) {
            setupLayout.show(jpSetup, "vob");
            getPowerTabModel().setIcon(vobIconSmall);
            Source source = sourceVobPane.getSource();
            project.setSource(source);
            source.bind(project);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "source":
                updateBody();
                break;
        }

        if (evt.getSource() instanceof Source) {
            updateBody();
        }
    }

    public SourceDvdPane getSourceDvdPane() {
        return sourceDvdPane;
    }

}
