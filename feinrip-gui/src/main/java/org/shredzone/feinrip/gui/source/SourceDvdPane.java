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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.shredzone.feinrip.gui.BorderAndFlowPanel;
import org.shredzone.feinrip.gui.ConfigurablePane;
import org.shredzone.feinrip.gui.ErrorDialog;
import org.shredzone.feinrip.gui.JLabelGroup;
import org.shredzone.feinrip.gui.JToolbarButton;
import org.shredzone.feinrip.gui.JTrackList;
import org.shredzone.feinrip.gui.SimpleFileFilter;
import org.shredzone.feinrip.gui.action.AbstractAsyncAction;
import org.shredzone.feinrip.gui.action.AbstractSyncAction;
import org.shredzone.feinrip.model.MountPoint;
import org.shredzone.feinrip.model.StreamType;
import org.shredzone.feinrip.source.DvdSource;
import org.shredzone.feinrip.source.Source;
import org.shredzone.feinrip.system.DeviceUtils;
import org.shredzone.feinrip.system.MediaChangeListener;

/**
 * A source configuration panel for DVD sources.
 *
 * @author Richard "Shred" Körber
 */
public class SourceDvdPane extends SourceSelectorPane implements ConfigurablePane,
            ListSelectionListener, MediaChangeListener, PropertyChangeListener {
    private static final long serialVersionUID = 8155941897642114923L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final Icon ejectIcon = new ImageIcon(SourceDvdPane.class.getResource("/org/shredzone/feinrip/icon/eject.png"));
    private static final Icon selectIcon = new ImageIcon(SourceDvdPane.class.getResource("/org/shredzone/feinrip/icon/large-dvd.png"));
    private static final Icon reloadIcon = new ImageIcon(SourceDvdPane.class.getResource("/org/shredzone/feinrip/icon/reload.png"));
    private static final Icon selectFileIcon = new ImageIcon(SourceVobPane.class.getResource("/org/shredzone/feinrip/icon/file.png"));
    private static final String KEY = "lastEitPath";

    private final Preferences prefs = Preferences.userNodeForPackage(SourceDvdPane.class);
    private final DvdSource source = new DvdSource();

    private JList<MountPoint> jlDevice;
    private JComboBox<StreamType> jcbRipMode;
    private JTrackList jlTrack;
    private JTextField jtfEitFile;

    public SourceDvdPane() {
        setup();
        reloadDeviceList();
        source.addPropertyChangeListener(this);
        DeviceUtils.addMediaChangeListener(this);
    }

    private void setup() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel jpSource = new JPanel(new BorderLayout());
        jpSource.setBorder(BorderFactory.createTitledBorder(B.getString("source.dvd.device")));
        {
            JToolBar jtbTools = new JToolBar();
            jtbTools.setFloatable(false);
            jtbTools.add(new JToolbarButton(new DvdReloadAction(), true));
            jtbTools.add(new JToolbarButton(new DvdEjectAction(), true));
            jpSource.add(jtbTools, BorderLayout.NORTH);

            jlDevice = new JList<>();
            jlDevice.setCellRenderer(new MointPointListCellRenderer());
            jlDevice.setVisibleRowCount(3);
            jlDevice.getSelectionModel().addListSelectionListener(this);
            jpSource.add(new JScrollPane(jlDevice));
        }
        add(jpSource);
        add(Box.createVerticalStrut(3));

        JPanel jpTrack = new JPanel(new BorderLayout());
        jpTrack.setBorder(BorderFactory.createTitledBorder(B.getString("source.dvd.track")));
        {
            jlTrack = new JTrackList();
            jpTrack.add(new JScrollPane(jlTrack), BorderLayout.CENTER);
        }
        add(jpTrack);
        add(Box.createVerticalStrut(3));

        JPanel jpStream = new JPanel();
        jpStream.setLayout(new BoxLayout(jpStream,  BoxLayout.Y_AXIS));
        jpStream.setBorder(BorderFactory.createTitledBorder(B.getString("source.dvd.attachments")));
        {
            JLabelGroup lg = null;

            JPanel jpEitFile = new BorderAndFlowPanel();
            {
                jtfEitFile = new JTextField();
                jpEitFile.add(jtfEitFile, BorderLayout.CENTER);

                JButton jbEitSelect = new JButton(new EitSelectAction());
                jbEitSelect.setText("");
                jpEitFile.add(jbEitSelect, BorderLayout.LINE_END);
            }
            jpStream.add(lg = new JLabelGroup(jpEitFile, B.getString("source.dvd.eit"), lg));

            lg.rearrange();
        }
        add(jpStream);
        JLabelGroup.setMinimumHeight(jpStream);
    }

    @Override
    public Component getConfigurationPane(AtomicReference<JLabelGroup> lgRef) {
        JPanel jpConfig = new JPanel();
        jpConfig.setLayout(new BoxLayout(jpConfig,  BoxLayout.Y_AXIS));
        jpConfig.setBorder(BorderFactory.createTitledBorder(B.getString("source.dvd.settings")));
        {
            JLabelGroup lg = lgRef.get();

            jcbRipMode = new JComboBox<>(StreamType.values());
            jcbRipMode.setSelectedItem(source.getStreamType());
            jcbRipMode.addActionListener(this::onRipModeAction);
            jpConfig.add(lg = new JLabelGroup(jcbRipMode, B.getString("source.dvd.stream"), lg));

            lgRef.set(lg);
        }
        JLabelGroup.setMinimumHeight(jpConfig);
        return jpConfig;
    }

    private void reloadDeviceList() {
        // Remember currently selected mount point
        MountPoint oldMountPoint = jlDevice.getSelectedValue();

        // Recreate list of mount points
        DefaultComboBoxModel<MountPoint> model = new DefaultComboBoxModel<>();

        try {
            for (File device : DeviceUtils.findDvdDevices()) {
                File mount = DeviceUtils.findMountPoint(device);
                if (mount != null) {
                    model.addElement(new MountPoint(device, mount));
                }
            }
        } catch (IOException ex) {
            ErrorDialog.showException(ex);
        }
        jlDevice.setModel(model);

        // Select the old mount point if still available, otherwise pick the first one
        if (oldMountPoint != null) {
            for (int ix = 0; ix < model.getSize(); ix++) {
                MountPoint testMountPoint = model.getElementAt(ix);
                if (testMountPoint.getDevice().equals(oldMountPoint.getDevice())) {
                    jlDevice.setSelectedIndex(ix);
                    break;
                }
            }
        } else {
            jlDevice.setSelectedValue(null, false);
        }
    }

    @Override
    public Source getSource() {
        return source;
    }

    @Override
    public void onMediaChange() {
        if (!source.isProcessing()) {
            reloadDeviceList();
        }
    }

    private void selectMountPoint(MountPoint mp) {
        // Changed mount point: do not continue to use previous eit file
        selectEitFile(null);

        if (mp != null) {
            source.setMountPoint(mp);
            jlTrack.setSource(source);
            jlTrack.setSelectedValue(source.getLongestTrack(), true);
        } else {
            jlTrack.setSelectedValue(null, false);
            jlTrack.setSource(null);
            source.setMountPoint(null);
        }
    }

    private void selectEitFile(File file) {
        jtfEitFile.setText(file != null ? file.getAbsolutePath() : "");
        source.setEitFile(file);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        Object src = e.getSource();

        if (src == jlDevice.getSelectionModel()) {
            selectMountPoint(jlDevice.getSelectedValue());
        }
    }

    private void onRipModeAction(ActionEvent e) {
        StreamType selection = jcbRipMode.getItemAt(jcbRipMode.getSelectedIndex());
        source.setStreamType(selection);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "source.streamType":
                jcbRipMode.setSelectedItem(source.getStreamType());
                break;
        }
    }

    /**
     * Action for reloading the list of available DVDs.
     */
    private class DvdReloadAction extends AbstractAsyncAction {
        private static final long serialVersionUID = -6392058002300620916L;

        public DvdReloadAction() {
            super(B.getString("action.dvdreload.title"), reloadIcon);
            setToolTipText(B.getString("action.dvdreload.tt"));
        }

        @Override
        public void onAction(ActionEvent e) {
            reloadDeviceList();
        }
    }

    /**
     * Action for ejecting the currently selected DVD.
     */
    private class DvdEjectAction extends AbstractAsyncAction {
        private static final long serialVersionUID = 7474719755768224845L;

        public DvdEjectAction() {
            super(B.getString("action.eject.title"), ejectIcon);
        }

        @Override
        public void onAction(ActionEvent e) {
            // Remove selected eit file, as we might eject its file system
            selectEitFile(null);

            MountPoint mp = jlDevice.getSelectedValue();
            if (mp != null) {
                try {
                    DeviceUtils.eject(mp);
                } catch (IOException ex) {
                    ErrorDialog.showException(ex);
                }
                reloadDeviceList();
            }
        }
    }

    /**
     * Renderer for mount point lists.
     */
    private static class MointPointListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = -6891744883662396982L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object renderValue = value;
            if (renderValue != null && renderValue instanceof MountPoint) {
                MountPoint mp = (MountPoint) renderValue;

                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append(mp.getMount().getName());
                sb.append("<br><small>").append(mp.getDevice().getAbsolutePath()).append("</small>");

                renderValue = sb;
            }

            JLabel jl = (JLabel) super.getListCellRendererComponent(list, renderValue, index, isSelected, cellHasFocus);
            jl.setIcon(selectIcon);
            return jl;
        }
    }

    /**
     * Action for selecting an eit file.
     */
    private class EitSelectAction extends AbstractSyncAction {
        private static final long serialVersionUID = 581152181550511413L;

        public EitSelectAction() {
            super(B.getString("action.eit.title"), selectFileIcon);
        }

        @Override
        public void onAction(ActionEvent e) {
            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle(B.getString("action.eit.dialog"));
            jfc.setDialogType(JFileChooser.OPEN_DIALOG);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setCurrentDirectory(new File(prefs.get(KEY, "")));
            jfc.setFileFilter(new SimpleFileFilter("eit"));
            int result = jfc.showOpenDialog(SourceDvdPane.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectEitFile(jfc.getSelectedFile());
            } else {
                selectEitFile(null);
            }
        }
    }

}
