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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.shredzone.feinrip.database.TvdbService.TvdbEpisode;
import org.shredzone.feinrip.gui.BorderAndFlowPanel;
import org.shredzone.feinrip.gui.ConfigurablePane;
import org.shredzone.feinrip.gui.DocumentListenerAdapter;
import org.shredzone.feinrip.gui.JEpisodeList;
import org.shredzone.feinrip.gui.JLabelGroup;
import org.shredzone.feinrip.gui.action.ImdbSyncAction;
import org.shredzone.feinrip.gui.action.NextEpisodeAction;
import org.shredzone.feinrip.gui.action.ProposeTitleAction;
import org.shredzone.feinrip.gui.action.TitleQueryAction;
import org.shredzone.feinrip.gui.action.TvdbQueryAction;
import org.shredzone.feinrip.gui.model.EpisodeListModel;
import org.shredzone.feinrip.model.Configuration;
import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.source.Source;

/**
 * PowerPane for configurating title settings.
 *
 * @author Richard "Shred" Körber
 */
@Pane(name = "title", title = "pane.title", icon = "title.png")
public class TitlePane extends PowerPane implements ConfigurablePane, ListSelectionListener {
    private static final long serialVersionUID = 5465622345023874309L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");

    private Configuration config = Configuration.global();

    private JTextField jtfTitle;
    private JEpisodeList jlEpisodes;
    private JTextField jtfImdbUrl;
    private JCheckBox jcbImdbEnabled;
    private JCheckBox jcbOfdbEnabled;
    private JCheckBox jcbOmdbEnabled;

    public TitlePane(Project project) {
        super(project);
        setup();
        updateBody();
    }

    private void setup() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabelGroup lg = null;

        JPanel jpTitle = new BorderAndFlowPanel();
        {
            jtfTitle = new JTextField();
            jtfTitle.addActionListener(this::onTitleAction);
            jtfTitle.getDocument().addDocumentListener(
                            new DocumentListenerAdapter(e -> project.setTitle(jtfTitle.getText())));
            jpTitle.add(jtfTitle, BorderLayout.CENTER);

            JButton jbPropose = new JButton(new ProposeTitleAction(project));
            jbPropose.setText("");
            jpTitle.add(jbPropose, BorderLayout.LINE_END);

            JButton jbTitle = new JButton(new TitleQueryAction(project));
            jbTitle.setText("");
            jpTitle.add(jbTitle, BorderLayout.LINE_END);
        }
        add(lg = new JLabelGroup(jpTitle, B.getString("pane.title.title"), lg));
        JLabelGroup.setMinimumHeight(lg);

        add(Box.createVerticalStrut(5));

        JPanel jpSeries = new BorderAndFlowPanel();
        {
            jlEpisodes = new JEpisodeList();
            jlEpisodes.addListSelectionListener(this);
            jlEpisodes.setEnabled(false);
            jpSeries.add(new JScrollPane(jlEpisodes), BorderLayout.CENTER);

            JButton jbNext = new JButton(new NextEpisodeAction(project));
            jbNext.setText("");
            jpSeries.add(jbNext, BorderLayout.LINE_END);

            JButton jbPick = new JButton(new TvdbQueryAction(project));
            jbPick.setText("");
            jpSeries.add(jbPick, BorderLayout.LINE_END);
        }
        add(lg = new JLabelGroup(jpSeries, B.getString("pane.title.series"), lg));
        lg.setVerticalAlignment(SwingConstants.TOP);

        add(Box.createVerticalStrut(3));

        lg.rearrange();
    }

    @Override
    public Component getConfigurationPane() {
        JPanel jpConfig = new JPanel();
        jpConfig.setLayout(new BoxLayout(jpConfig,  BoxLayout.Y_AXIS));
        jpConfig.setBorder(BorderFactory.createTitledBorder(B.getString("pane.title.settings")));
        {
            JLabelGroup lg = null;

            JPanel jpImdbUrl = new BorderAndFlowPanel();
            {
                jtfImdbUrl = new JTextField(config.getImdbUrl());
                jtfImdbUrl.setToolTipText(B.getString("pane.title.imdburl.tt"));
                jtfImdbUrl.getDocument().addDocumentListener(
                                new DocumentListenerAdapter(e -> config.setImdbUrl(jtfImdbUrl.getText())));
                jpImdbUrl.add(jtfImdbUrl, BorderLayout.CENTER);

                JButton jbImdbSync = new JButton(new ImdbSyncAction());
                jpImdbUrl.add(jbImdbSync, BorderLayout.LINE_END);
            }
            jpConfig.add(lg = new JLabelGroup(jpImdbUrl, B.getString("pane.title.imdb"), lg));
            jpConfig.add(lg = new JLabelGroup(new JLabel(B.getString("pane.title.imdb.license")), "", lg));

            jpConfig.add(new JSeparator());

            jcbImdbEnabled = new JCheckBox(B.getString("pane.title.enableimdb"));
            jcbImdbEnabled.setSelected(config.isImdbEnabled());
            jcbImdbEnabled.addChangeListener(e -> config.setImdbEnabled(jcbImdbEnabled.isSelected()));
            jpConfig.add(lg = new JLabelGroup(jcbImdbEnabled, "", lg));

            jcbOfdbEnabled = new JCheckBox(B.getString("pane.title.enableofdb"));
            jcbOfdbEnabled.setSelected(config.isOfdbEnabled());
            jcbOfdbEnabled.addChangeListener(e -> config.setOfdbEnabled(jcbOfdbEnabled.isSelected()));
            jpConfig.add(lg = new JLabelGroup(jcbOfdbEnabled, "", lg));

            jcbOmdbEnabled = new JCheckBox(B.getString("pane.title.enableomdb"));
            jcbOmdbEnabled.setSelected(config.isOmdbEnabled());
            jcbOmdbEnabled.addChangeListener(e -> config.setOmdbEnabled(jcbOmdbEnabled.isSelected()));
            jpConfig.add(lg = new JLabelGroup(jcbOmdbEnabled, "", lg));


            lg.rearrange();
        }

        JLabelGroup.setMinimumHeight(jpConfig);
        return jpConfig;
    }

    private void proposeTitleIfEmpty() {
        if (project.getTitle() != null && project.getTitle().trim().isEmpty()) {
            project.setTitle(project.getSource().getTitleProposal());
        }
    }

    private void updateSelectedEpisode() {
        project.setEpisode(jlEpisodes.getSelectedValue());
    }

    private void updateBody() {
        StringBuilder sb = new StringBuilder("<html>");
        if (project.getTitle() != null && !project.getTitle().isEmpty()) {
            sb.append(escape(project.getTitle()));
        } else {
            sb.append("<i>").append(B.getString("pane.title.none")).append("</i>");
        }

        TvdbEpisode episode = project.getEpisode();
        if (episode != null) {
            sb.append("<br>");
            sb.append(String.format("%d-%02d", episode.season, episode.episode));
            sb.append(": ").append(escape(episode.title));
        }

        getPowerTabModel().setBody(sb.toString());
    }

    private String escape(String str) {
        return str.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
    }

    private void onTitleAction(ActionEvent e) {
        project.setTitle(jtfTitle.getText());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "source":
                proposeTitleIfEmpty();
                break;

            case "episodes":
                @SuppressWarnings("unchecked")
                List<TvdbEpisode> newEpisodes = (List<TvdbEpisode>) evt.getNewValue();
                if (newEpisodes != null && !newEpisodes.isEmpty()) {
                    jlEpisodes.setModel(new EpisodeListModel(newEpisodes));
                    jlEpisodes.setEnabled(true);
                } else {
                    jlEpisodes.setModel(new EpisodeListModel());
                    jlEpisodes.setEnabled(false);
                }
                updateBody();
                break;

            case "episode":
                TvdbEpisode newEpisode = (TvdbEpisode) evt.getNewValue();
                if (jlEpisodes.getSelectedValue() != newEpisode) {
                    jlEpisodes.setSelectedValue(newEpisode, true);
                }
                updateBody();
                break;

            case "title":
                String newValue = evt.getNewValue().toString();
                if (!jtfTitle.getText().equals(newValue)) {
                    jtfTitle.setText(newValue);
                }
                updateBody();
                break;
        }

        if (evt.getSource() instanceof Source) {
            proposeTitleIfEmpty();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        updateSelectedEpisode();
    }

}
