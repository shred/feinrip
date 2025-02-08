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
package org.shredzone.feinrip.gui.source;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.shredzone.feinrip.gui.BorderAndFlowPanel;
import org.shredzone.feinrip.gui.ErrorDialog;
import org.shredzone.feinrip.gui.JLabelGroup;
import org.shredzone.feinrip.gui.SimpleFileFilter;
import org.shredzone.feinrip.gui.action.AbstractAsyncAction;
import org.shredzone.feinrip.gui.action.AbstractSyncAction;
import org.shredzone.feinrip.model.Palette;
import org.shredzone.feinrip.model.PaletteType;
import org.shredzone.feinrip.source.Source;
import org.shredzone.feinrip.source.VobSource;

/**
 * A source configuration pane for VOB files.
 *
 * @author Richard "Shred" Körber
 */
public class SourceVobPane extends SourceSelectorPane {
    private static final long serialVersionUID = 1542369139876207725L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final Icon selectIcon = new ImageIcon(SourceVobPane.class.getResource("/org/shredzone/feinrip/icon/file.png"));
    private static final String KEY = "lastPath";

    private final Preferences prefs = Preferences.userNodeForPackage(SourceVobPane.class);
    private final VobSource source = new VobSource();

    private JTextField jtfFile;
    private JTextField jtfEitFile;
    private JComboBox<PaletteType> jcbPalette;
    private JTextField jtfCustomPalette;
    private JLabel[] jlColors = new JLabel[16];

    public SourceVobPane() {
        setup();
    }

    private void setup() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabelGroup lg = null;

        JPanel jpSource = new JPanel();
        jpSource.setLayout(new BoxLayout(jpSource, BoxLayout.Y_AXIS));
        jpSource.setBorder(BorderFactory.createTitledBorder(B.getString("source.vob.file")));
        {
            JPanel jpFile = new BorderAndFlowPanel();
            {
                jtfFile = new JTextField();
                jtfFile.addActionListener(this::onFileAction);
                jpFile.add(jtfFile, BorderLayout.CENTER);

                JButton jbFileSelect = new JButton(new FileSelectAction());
                jbFileSelect.setText("");
                jpFile.add(jbFileSelect, BorderLayout.LINE_END);
            }
            jpSource.add(lg = new JLabelGroup(jpFile, B.getString("source.vob.fl"), lg));
        }
        add(jpSource);
        JLabelGroup.setMinimumHeight(jpSource);

        JPanel jpPalette = new JPanel();
        jpPalette.setLayout(new BoxLayout(jpPalette, BoxLayout.Y_AXIS));
        jpPalette.setBorder(BorderFactory.createTitledBorder(B.getString("source.vob.palettebox")));
        {
            jcbPalette = new JComboBox<>(PaletteType.values());
            jcbPalette.addActionListener(this::onPaletteAction);
            jpPalette.add(lg = new JLabelGroup(jcbPalette, B.getString("source.vob.palette"), lg));

            JPanel jpCustom = new BorderAndFlowPanel();
            {
                jtfCustomPalette = new JTextField();
                jtfCustomPalette.setToolTipText(B.getString("source.vob.custom.tt"));
                jtfCustomPalette.addActionListener(this::onCustomPaletteAction);
                jpCustom.add(jtfCustomPalette, BorderLayout.CENTER);

                JButton jbLoad = new JButton(new LoadPaletteAction());
                jbLoad.setText("");
                jpCustom.add(jbLoad, BorderLayout.LINE_END);
            }
            jpPalette.add(lg = new JLabelGroup(jpCustom, B.getString("source.vob.custom"), lg));

            jpPalette.add(Box.createVerticalStrut(2));

            JPanel jpColors = new JPanel(new GridLayout(0, 8, 1, 1));
            jpColors.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLoweredBevelBorder(),
                            BorderFactory.createEmptyBorder(3, 3, 3, 3)
                            ));
            for (int ix = 0; ix < jlColors.length; ix++) {
                JLabel label = new JLabel(String.valueOf(ix));
                label.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Color.BLACK),
                                BorderFactory.createEmptyBorder(1, 3, 1, 3)));
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                label.setOpaque(true);
                jlColors[ix] = label;
                jpColors.add(jlColors[ix]);
            }
            jpPalette.add(lg = new JLabelGroup(jpColors, B.getString("source.vob.colors"), lg));
        }
        add(jpPalette);
        JLabelGroup.setMinimumHeight(jpPalette);

        JPanel jpEitFile = new JPanel();
        jpEitFile.setLayout(new BoxLayout(jpEitFile, BoxLayout.Y_AXIS));
        jpEitFile.setBorder(BorderFactory.createTitledBorder(B.getString("source.vob.attachments")));
        {
            JPanel jpFile = new BorderAndFlowPanel();
            {
                jtfEitFile = new JTextField();
                jpFile.add(jtfEitFile, BorderLayout.CENTER);

                JButton jbEitSelect = new JButton(new EitSelectAction());
                jbEitSelect.setText("");
                jpFile.add(jbEitSelect, BorderLayout.LINE_END);
            }
            jpEitFile.add(lg = new JLabelGroup(jpFile, B.getString("source.vob.eit"), lg));
        }
        add(jpEitFile);
        JLabelGroup.setMinimumHeight(jpEitFile);

        lg.rearrange();
    }

    @Override
    public Source getSource() {
        return source;
    }

    private void updateColors() {
        Palette colors;
        if (source.getPalette() == PaletteType.CUSTOM) {
            colors = source.getCustomPalette();
        } else {
            colors = source.getPalette().getPalette();
        }

        if (colors != null) {
            for (int ix = 0; ix < colors.size(); ix++) {
                jlColors[ix].setBackground(new Color(colors.getRgb(ix)));
                jlColors[ix].setForeground(colors.getBrightness(ix) < 100 ? Color.WHITE : Color.BLACK);
            }
        }
    }

    private void parseCustomPalette(String palette) {
        source.setCustomPalette(Palette.parse(palette));
        updateColors();
    }

    private void selectFile(File file) {
        jtfFile.setText(file.getAbsolutePath());
        source.setVobFile(file);

        selectEitFile(null);

        jcbPalette.setSelectedIndex(0);
        source.setPalette(PaletteType.DEFAULT);
        updateColors();

        source.setupProject();
        prefs.put(KEY, file.getParentFile().getAbsolutePath());
    }

    private void selectEitFile(File file) {
        jtfEitFile.setText(file != null ? file.getAbsolutePath() : "");
        source.setEitFile(file);
    }

    private void onFileAction(ActionEvent e) {
        File file = new File(jtfFile.getText());
        if (file.exists() && file.isFile()) {
            selectFile(file);
        }
    }

    private void onPaletteAction(ActionEvent e) {
        PaletteType pt = jcbPalette.getItemAt(jcbPalette.getSelectedIndex());
        source.setPalette(pt);
        updateColors();
    }

    private void onCustomPaletteAction(ActionEvent e) {
        jcbPalette.setSelectedItem(PaletteType.CUSTOM);
        source.setPalette(PaletteType.CUSTOM);
        parseCustomPalette(jtfCustomPalette.getText());
        updateColors();
    }

    /**
     * Action to load a yuv palette file
     */
    private class LoadPaletteAction extends AbstractAsyncAction {
        private static final long serialVersionUID = 8386797533663223880L;

        private File selected;

        public LoadPaletteAction() {
            super(B.getString("action.loadpalette.title"), selectIcon);
        }

        @Override
        public void preAction(ActionEvent e) {
            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle(B.getString("action.loadpalette.dialog"));
            jfc.setDialogType(JFileChooser.OPEN_DIALOG);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setFileFilter(new SimpleFileFilter("yuv"));
            jfc.setCurrentDirectory(new File(prefs.get(KEY, "")));
            int result = jfc.showOpenDialog(SourceVobPane.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selected = jfc.getSelectedFile();
            } else {
                selected = null;
            }
        }

        @Override
        public void onAction(ActionEvent e) {
            if (selected != null) {
                try (BufferedReader in = new BufferedReader(new FileReader(selected))) {
                    String line = in.readLine().trim();
                    jtfCustomPalette.setText(line);
                    parseCustomPalette(line);
                } catch (IOException ex) {
                    ErrorDialog.showException(ex);
                }
            }
        }
    }

    /**
     * Action for selecting a vob file.
     */
    private class FileSelectAction extends AbstractAsyncAction {
        private static final long serialVersionUID = -2328099767483540149L;

        private File selected;

        public FileSelectAction() {
            super(B.getString("action.vob.title"), selectIcon);
        }

        @Override
        public void preAction(ActionEvent e) {
            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle(B.getString("action.vob.dialog"));
            jfc.setDialogType(JFileChooser.OPEN_DIALOG);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setCurrentDirectory(new File(prefs.get(KEY, "")));
            jfc.setFileFilter(new SimpleFileFilter("vob"));
            int result = jfc.showOpenDialog(SourceVobPane.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selected = jfc.getSelectedFile();
            } else {
                selected = null;
            }
        }

        @Override
        public void onAction(ActionEvent e) {
            if (selected != null) {
                selectFile(selected);
            }
        }
    }

    /**
     * Action for selecting an eit file.
     */
    private class EitSelectAction extends AbstractSyncAction {
        private static final long serialVersionUID = -809157912320872123L;

        public EitSelectAction() {
            super(B.getString("action.eit.title"), selectIcon);
        }

        @Override
        public void onAction(ActionEvent e) {
            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle(B.getString("action.eit.dialog"));
            jfc.setDialogType(JFileChooser.OPEN_DIALOG);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setCurrentDirectory(new File(prefs.get(KEY, "")));
            jfc.setFileFilter(new SimpleFileFilter("eit"));
            int result = jfc.showOpenDialog(SourceVobPane.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectEitFile(jfc.getSelectedFile());
            } else {
                selectEitFile(null);
            }
        }
    }

}
