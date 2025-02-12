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
package org.shredzone.feinrip.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Helps executing shell commands.
 *
 * @author Richard "Shred" Körber
 */
public class Command {

    @FunctionalInterface
    public interface IOStream {
        void accept(Stream<String> t) throws IOException;
    }

    private File cmdName;
    private List<String> command = new ArrayList<>();
    private ProcessBuilder builder;
    private int rc;
    private IOStream outConsumer = stream -> stream.count();
    private IOStream errConsumer = stream -> stream.count();
    private Predicate<Integer> hasFailed = rc -> rc != 0;
    private byte[] inputData = null;

    /**
     * Create a new command.
     */
    public Command(File cmd) {
        this.cmdName = cmd;
        command.add(cmd.getAbsolutePath());
        builder = new ProcessBuilder();
    }

    /**
     * Add a set of parameters.
     */
    public Command param(Object... param) {
        for (Object p : param) {
            if (p == null) {
                continue;
            }

            if (p instanceof File) {
                command.add(((File) p).getAbsolutePath());
            } else {
                command.add(p.toString());
            }
        }
        return this;
    }

    /**
     * Sets input data.
     *
     * @param data
     *            Input data to be sent to the process
     */
    public Command input(String data) {
        return input(data.getBytes());
    }

    /**
     * Sets input data.
     *
     * @param data
     *            Input data to be sent to the process
     */
    public Command input(byte[] data) {
        this.inputData = data;
        return this;
    }

    /**
     * Changes the "failed if" predicate. By default, a command failed if it returned a
     * different return code than 0.
     *
     * @param predicate
     *            Failure predicate
     */
    public Command failedIf(Predicate<Integer> predicate) {
        this.hasFailed = predicate;
        return this;
    }

    /**
     * Redirect stdout to a {@link File}.
     */
    public void redirectOutput(File file) {
        builder.redirectOutput(file);
        outConsumer = null;
    }

    /**
     * Redirect stdout to a {@link IOStream}. The stream contains a String for each line.
     */
    public void redirectOutputAsStream(IOStream consumer) {
        builder.redirectOutput(Redirect.PIPE);
        outConsumer = consumer;
    }

    /**
     * Redirect stdout to a consumer. The consumer will receive each line separately.
     */
    public void redirectOutput(Consumer<String> consumer) {
        redirectOutputAsStream(stream -> stream.forEach(consumer::accept));
    }

    /**
     * Redirect stderr to a {@link File}.
     */
    public void redirectError(File file) {
        builder.redirectError(file);
        errConsumer = null;
    }

    /**
     * Redirect stderr to a {@link IOStream}. The stream contains a String for each line.
     */
    public void redirectErrorAsStream(IOStream consumer) {
        builder.redirectError(Redirect.PIPE);
        errConsumer = consumer;
    }

    /**
     * Redirect stderr to a consumer. The consumer will receive each line separately.
     */
    public void redirectError(Consumer<String> consumer) {
        redirectErrorAsStream(stream -> stream.forEach(consumer::accept));
    }

    /**
     * Redirect stderr to stdout.
     */
    public void redirectErrorToOutput() {
        builder.redirectErrorStream(true);
        errConsumer = null;
    }

    /**
     * Executes the command synchronously. The stream consumers are executed in a separate
     * thread.
     *
     * @throws IOException
     *             when the command failed to execute successfully
     */
    public void execute() throws IOException {
        StreamSpiller inSpiller = null;
        StreamGobbler outGobbler = null;
        StreamGobbler errGobbler = null;

        System.out.print(command.get(0));
        command.stream().skip(1).map(it -> " '" + it + "'").forEach(System.out::print);
        System.out.println();

        builder.command(command);
        Process p = builder.start();

        if (inputData != null) {
            inSpiller = new StreamSpiller(p.getOutputStream(), inputData);
            inSpiller.start();
        }

        if (outConsumer != null) {
            outGobbler = new StreamGobbler(p.getInputStream(), outConsumer);
            outGobbler.start();
        }

        if (errConsumer != null) {
            errGobbler = new StreamGobbler(p.getErrorStream(), errConsumer);
            errGobbler.start();
        }

        try {
            rc = p.waitFor();
            if (inSpiller != null) {
                inSpiller.join(1000L);
            }
            if (outGobbler != null) {
                outGobbler.join();
            }
            if (errGobbler != null) {
                errGobbler.join();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted", ex);
        }

        if (hasFailed.test(rc)) {
            throw new IOException("command " + cmdName.getName()
                + " failed, returning error code " + rc);
        }
    }

    /**
     * A {@link Thread} that writes to the {@link OutputStream}.
     */
    private static class StreamSpiller extends Thread {
        private final OutputStream out;
        private final byte[] data;

        public StreamSpiller(OutputStream out, byte[] data) {
            this.out = out;
            this.data = data;
        }

        @Override
        public void run() {
            try (OutputStream o = out) { // auto-close after write
                o.write(data);
            } catch (IOException ex) {
                throw new RuntimeException("Failed writing stream", ex);
            }
        }
    }

    /**
     * A {@link Thread} that reads the {@link InputStream} and feeds an {@link IOStream}.
     */
    private static class StreamGobbler extends Thread {
        private final InputStream in;
        private final IOStream consumer;

        public StreamGobbler(InputStream in, IOStream consumer) {
            this.in = in;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
                consumer.accept(r.lines());
            } catch (IOException ex) {
                throw new RuntimeException("Failed reading stream", ex);
            }
        }
    }

}
