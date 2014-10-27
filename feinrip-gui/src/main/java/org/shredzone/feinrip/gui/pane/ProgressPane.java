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
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.shredzone.feinrip.gui.BorderAndFlowPanel;
import org.shredzone.feinrip.gui.ConfigurablePane;
import org.shredzone.feinrip.gui.JLabelGroup;
import org.shredzone.feinrip.gui.SimpleFileFilter;
import org.shredzone.feinrip.gui.action.AbstractSyncAction;
import org.shredzone.feinrip.gui.source.SourceVobPane;
import org.shredzone.feinrip.model.Configuration;
import org.shredzone.feinrip.model.Project;
import org.shredzone.feinrip.progress.ProgressMeter;

/**
 * PowerPane that shows the progress of a running conversion.
 *
 * @author Richard "Shred" Körber
 */
@Pane(name = "progress")
public class ProgressPane extends PowerPane implements ConfigurablePane, ProgressMeter {
    private static final long serialVersionUID = -4337805119426983223L;

    // Minimum delay between two percent meter updates, in milliseconds
    private static final long NEXT_PERCENT_LIMITER = 500;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final Icon selectFileIcon = new ImageIcon(SourceVobPane.class.getResource("/org/shredzone/feinrip/icon/file.png"));

    private Configuration config = Configuration.global();
    private JProgressBar jpbProgress;
    private JTextField jtfInfo;
    private JTextArea jtaLog;
    private JTextField jtfMp3File;
    private JTextField jtfTempDir;
    private JCheckBox jcAudioDemux;
    private JCheckBox jcHold;
    private Long startTime = null;
    private StringBuilder logBuilder = new StringBuilder();
    private Frame frame;
    private String frameTitle;
    private long nextPercentOutput = 0;

    public ProgressPane(Project project) {
        super(project);
        setup();
    }

    /**
     * Sets the frame that contains this pane. The frame title is modified depending on
     * the current progress.
     *
     * @param frame
     *            {@link Frame} or {@code null} if on frame title shall be updated
     */
    public void setFrame(Frame frame) {
        this.frame = frame;
        if (frame != null) {
            this.frameTitle = frame.getTitle();
        }
    }

    /**
     * Cleans up the pane after conversion. Resources (like logs) are freed.
     */
    public void cleanup() {
        jtfInfo.setText("");
        jtaLog.setText("");
        logBuilder = new StringBuilder();
        if (frame != null) {
            frame.setTitle(frameTitle);
        }
        nextPercentOutput = 0;
    }

    private void setup() {
        jtaLog = new JTextArea();
        jtaLog.setLineWrap(false);
        jtaLog.setEditable(false);
        add(new JScrollPane(jtaLog), BorderLayout.CENTER);

        JPanel jpOut = new JPanel();
        jpOut.setBorder(BorderFactory.createEmptyBorder(4, 0, 2, 0));
        jpOut.setLayout(new BoxLayout(jpOut, BoxLayout.Y_AXIS));
        {
            jtfInfo = new JTextField(" ");
            jtfInfo.setEditable(false);
            jpOut.add(jtfInfo);

            jpOut.add(Box.createVerticalStrut(1));

            jpbProgress = new JProgressBar(0, 100);
            jpbProgress.setString("");
            jpbProgress.setStringPainted(true);
            jpOut.add(jpbProgress);
        }
        add(jpOut, BorderLayout.SOUTH);
    }

    @Override
    public Component getConfigurationPane() {
        JPanel jpConfig = new JPanel();
        jpConfig.setLayout(new BoxLayout(jpConfig,  BoxLayout.Y_AXIS));
        jpConfig.setBorder(BorderFactory.createTitledBorder(B.getString("pane.progress.settings")));
        {
            JLabelGroup lg = null;

            JPanel jpTempDir = new BorderAndFlowPanel();
            {
                jtfTempDir = new JTextField(config.getTempDir());
                jtfTempDir.setToolTipText(B.getString("pane.progress.temp.tt"));
                jpTempDir.add(jtfTempDir, BorderLayout.CENTER);

                JButton jbTempSelect = new JButton(new TempSelectAction());
                jbTempSelect.setText("");
                jpTempDir.add(jbTempSelect, BorderLayout.LINE_END);
            }
            jpConfig.add(lg = new JLabelGroup(jpTempDir, B.getString("pane.progress.temp"), lg));

            JPanel jpSoundFile = new BorderAndFlowPanel();
            {
                jtfMp3File = new JTextField(config.getSoundFile());
                jtfMp3File.setToolTipText(B.getString("pane.progress.sound.tt"));
                jpSoundFile.add(jtfMp3File, BorderLayout.CENTER);

                JButton jbSoundSelect = new JButton(new Mp3SelectAction());
                jbSoundSelect.setText("");
                jpSoundFile.add(jbSoundSelect, BorderLayout.LINE_END);
            }
            jpConfig.add(lg = new JLabelGroup(jpSoundFile, B.getString("pane.progress.sound"), lg));

            jpConfig.add(new JSeparator());

            jcAudioDemux = new JCheckBox(B.getString("pane.progress.demux"));
            jcAudioDemux.setSelected(config.isForceAudioDemux());
            jcAudioDemux.addActionListener(this::onAudioDemuxAction);
            jpConfig.add(lg = new JLabelGroup(jcAudioDemux, "", lg));

            jcHold = new JCheckBox(B.getString("pane.progress.hold"));
            jcHold.setSelected(config.isHoldBeforeMuxing());
            jcHold.addActionListener(this::onHoldAction);
            jpConfig.add(lg = new JLabelGroup(jcHold, "", lg));

            lg.rearrange();
        }

        JLabelGroup.setMinimumHeight(jpConfig);
        return jpConfig;
    }

    private void onAudioDemuxAction(ActionEvent e) {
        config.setForceAudioDemux(jcAudioDemux.isSelected());
    }

    private void onHoldAction(ActionEvent e) {
        config.setHoldBeforeMuxing(jcHold.isSelected());
    }

    private void selectTempDir(File dir) {
        String dirname = (dir != null ? dir.getAbsolutePath() : null);
        jtfTempDir.setText(dirname);
        config.setTempDir(dirname);
    }

    private void selectMp3File(File file) {
        String filename = (file != null ? file.getAbsolutePath() : null);
        jtfMp3File.setText(filename);
        config.setSoundFile(filename);
    }

    /**
     * Estimates the time until 100% will be reached.
     * <p>
     * A first estimation is available after 3 seconds.
     *
     * @param percent
     *            current percent
     * @return Estimated number of seconds until 100% will be reached. Returns -1 if an
     *         estimation is not currently available.
     */
    private int estimateTime(float percent) {
        if (startTime == null || percent <= 0 || percent > 100) return -1;

        long current = System.currentTimeMillis();
        long elapsed = current - startTime;

        // Wait at least 3 seconds before estimating.
        if (elapsed < 3000) return -1;

        // --- Compute ETA and required time ---
        // The ETA is extrapolated from the elapsed time and the index
        // in relation to the given maximum.
        double eta = ((elapsed * 100.0d) / percent) + startTime;
        double required = Math.floor((eta - current) / 1000.0d); // floored, in seconds

        // --- Out of range? ---
        if (required < 0 || required > Integer.MAX_VALUE) return -1;

        // --- Return the time ---
        return (int) required;
    }

    @Override
    public ProgressMeter message(final String message, final Object... values) {
        EventQueue.invokeLater(() -> jtfInfo.setText(MessageFormat.format(message, values)));
        return this;
    }

    @Override
    public ProgressMeter percent(final Float percent) {
        long now = System.currentTimeMillis();
        if (now < nextPercentOutput) {
            // Too early, avoid output to keep GUI load low
            return this;
        }
        nextPercentOutput = now + NEXT_PERCENT_LIMITER;

        EventQueue.invokeLater(() -> {
            if (percent != null) {
                if (startTime == null) {
                    startTime = System.currentTimeMillis();
                }
                jpbProgress.setValue(percent.intValue());
                StringBuilder sb = new StringBuilder();
                sb.append(percent.intValue()).append('%');

                if (frame != null) {
                    StringBuilder sb2 = new StringBuilder(sb);
                    sb2.append(" - ").append(frameTitle);
                    frame.setTitle(sb2.toString());
                }

                int eta = estimateTime(percent);
                if (eta >= 0) {
                    sb.append(String.format(" - %02d:%02d", (eta / 60), (eta % 60)));
                }

                jpbProgress.setString(sb.toString());
                jpbProgress.setIndeterminate(false);
            } else {
                jpbProgress.setString("");
                jpbProgress.setIndeterminate(true);
                if (frame != null) {
                    frame.setTitle(frameTitle);
                }
                startTime = null;
            }
        });
        return this;
    }

    @Override
    public ProgressMeter log(final String line) {
        final int cursor = logBuilder.length();
        if (cursor > 0) {
            logBuilder.append("\n");
        }
        logBuilder.append(line);

        EventQueue.invokeLater(() -> {
            jtaLog.setText(logBuilder.toString());
            jtaLog.setCaretPosition(cursor + 1);
        });
        return this;
    }

    /**
     * Action for selecting a temp dir.
     */
    private class TempSelectAction extends AbstractSyncAction {
        private static final long serialVersionUID = -5613199421054499694L;

        public TempSelectAction() {
            super(B.getString("pane.progress.temp.title"), selectFileIcon);
        }

        @Override
        public void onAction(ActionEvent e) {
            File currentDir = null;
            String tempDir = config.getTempDir();
            if (tempDir != null) {
                currentDir = new File(tempDir).getParentFile();
            }

            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle(B.getString("pane.progress.temp.dialog"));
            jfc.setDialogType(JFileChooser.OPEN_DIALOG);
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jfc.setCurrentDirectory(currentDir);
            int result = jfc.showOpenDialog(ProgressPane.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectTempDir(jfc.getSelectedFile());
            }
        }
    }

    /**
     * Action for selecting an mp3 file.
     */
    private class Mp3SelectAction extends AbstractSyncAction {
        private static final long serialVersionUID = -4533364218037118291L;

        public Mp3SelectAction() {
            super(B.getString("pane.progress.mp3.title"), selectFileIcon);
        }

        @Override
        public void onAction(ActionEvent e) {
            File currentDir = null;
            String filePath = config.getSoundFile();
            if (filePath != null) {
                currentDir = new File(filePath).getParentFile();
            }

            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle(B.getString("pane.progress.mp3.dialog"));
            jfc.setDialogType(JFileChooser.OPEN_DIALOG);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setCurrentDirectory(currentDir);
            jfc.setFileFilter(new SimpleFileFilter("mp3"));
            int result = jfc.showOpenDialog(ProgressPane.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectMp3File(jfc.getSelectedFile());
            }
        }
    }

}
