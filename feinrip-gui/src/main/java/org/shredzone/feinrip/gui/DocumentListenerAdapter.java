/*
 * feinrip
 *
 * Copyright (C) 2015 Richard "Shred" Körber
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

import java.util.function.Consumer;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A helper class for listening to {@link DocumentListener} for generic changes to the
 * document.
 *
 * @author Richard "Shred" Körber
 */
public class DocumentListenerAdapter implements DocumentListener {

    private final Consumer<DocumentEvent> consumer;

    /**
     * Creates a new {@link DocumentListener}.
     *
     * @param consumer
     *            Consumer of the {@link DocumentEvent}. The consumer is only notified
     *            that the document has changed, but does not know whether it was an
     *            insert, change or removal of text.
     */
    public DocumentListenerAdapter(Consumer<DocumentEvent> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        consumer.accept(e);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        consumer.accept(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        consumer.accept(e);
    }

}
