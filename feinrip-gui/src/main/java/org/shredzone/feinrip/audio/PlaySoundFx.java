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
package org.shredzone.feinrip.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.shredzone.feinrip.gui.ErrorDialog;

/**
 * Utility class for playing sounds.
 *
 * @author Richard "Shred" Körber
 */
public class PlaySoundFx {

    /**
     * Plays an MP3.
     *
     * @param file
     *            Location of the mp3 file to be played
     */
    public static void mp3(File file) {
        try (AudioInputStream ain = AudioSystem.getAudioInputStream(new FileInputStream(file))) {
            AudioFormat baseFormat = ain.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(), 16,
                            baseFormat.getChannels(), baseFormat.getChannels() * 2,
                            baseFormat.getSampleRate(), false);

            try (AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, ain)) {
                byte[] data = new byte[4096];
                SourceDataLine line = getLine(decodedFormat);
                if (line != null) {
                    line.start();
                    int nBytesRead;
                    while ((nBytesRead = din.read(data, 0, data.length)) != -1) {
                        line.write(data, 0, nBytesRead);
                    }
                    line.drain();
                    line.stop();
                    line.close();
                }
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
            ErrorDialog.showException(ex);
        }
    }

    private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }

}
