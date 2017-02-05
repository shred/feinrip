/*
 * feinrip
 *
 * Copyright (C) 2017 Richard "Shred" Körber
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
package org.shredzone.feinrip.system;

import java.io.File;
import java.io.IOException;

import org.shredzone.feinrip.progress.LogConsumer;
import org.shredzone.feinrip.progress.PercentConsumer;
import org.shredzone.feinrip.progress.ProgressMeter;
import org.shredzone.feinrip.util.Command;

/**
 * Invoker of an external preprocessor script.
 *
 * @author Richard "Shred" Körber
 */
public class PreprocessorInvoker {

    private final File script;
    private File vobFile;
    private String jsonProcessData;

    /**
     * Creates a new {@link PreprocessorInvoker}.
     *
     * @param script
     *            Target script to be invoked
     */
    public PreprocessorInvoker(File script) {
        this.script = script;
    }

    public void setVobFile(File vobFile) {
        this.vobFile = vobFile;
    }

    public void setJsonProcessData(String jsonProcessData) {
        this.jsonProcessData = jsonProcessData;
    }

    /**
     * Invoke the preprocessor.
     *
     * @param meter
     *            {@link ProgressMeter} to write output to
     */
    public void invoke(ProgressMeter meter) throws IOException {
        Command scriptCmd = new Command(script);
        if (vobFile != null) {
            scriptCmd.param(vobFile);
        }

        if (jsonProcessData != null) {
            scriptCmd.input(jsonProcessData);
            System.out.println("JSON data: " + jsonProcessData);
        }

        scriptCmd.redirectOutput(new PercentConsumer(meter, false));
        scriptCmd.redirectError(new LogConsumer(meter, true));

        scriptCmd.execute();
    }

}
