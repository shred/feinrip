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
package org.shredzone.feinrip.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFrame;

/**
 * An abstract {@link Action} that is executed asynchronously.
 *
 * @author Richard "Shred" Körber
 */
public abstract class AbstractAsyncAction extends AbstractSyncAction {
    private static final long serialVersionUID = 4895362124840932162L;

    private boolean modal = true;

    /**
     * Creates a new {@link AbstractAsyncAction}.
     *
     * @param name
     *            Action display name
     * @param icon
     *            Action icon, may be {@code null}
     */
    public AbstractAsyncAction(String name, Icon icon) {
        super(name, icon);
    }

    /**
     * If modal is enabled, the action will disable input to the main frame.
     * <p>
     * By default, modal is set to {@code true}.
     */
    public void setModal(boolean modal) {
        this.modal = modal;
    }

    /**
     * Invoked before the worker thread is invoked. This method is invoked in the Swing
     * thread.
     *
     * @param e
     *            {@link ActionEvent}
     */
    public void preAction(ActionEvent e) {
        // Does nothing by default
    }

    /**
     * Invoked after the worker thread is completed. This method is invoked in the Swing
     * thread.
     *
     * @param e
     *            {@link ActionEvent}
     */
    public void postAction(ActionEvent e) {
        // Does nothing by default
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        WorkerThread thread = new WorkerThread((JFrame) getFrame(e), modal) {
            @Override
            protected void preAction() {
                AbstractAsyncAction.this.preAction(e);
            }

            @Override
            protected void onAction() {
                AbstractAsyncAction.this.onAction(e);
            }

            @Override
            protected void postAction() {
                AbstractAsyncAction.this.postAction(e);
            }
        };

        thread.start();
    }

}
