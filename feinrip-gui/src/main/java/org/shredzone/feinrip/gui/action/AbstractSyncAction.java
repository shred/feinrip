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
package org.shredzone.feinrip.gui.action;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPopupMenu;

/**
 * An abstract {@link Action} that is executed synchronously.
 *
 * @author Richard "Shred" Körber
 */
public abstract class AbstractSyncAction extends AbstractAction {
    private static final long serialVersionUID = -3232787365501484576L;

    /**
     * Creates a new synchronous action.
     *
     * @param name
     *            Action display name
     * @param icon
     *            Action icon, may be {@code null}
     */
    public AbstractSyncAction(String name, Icon icon) {
        putValue(Action.NAME, name);
        if (icon != null)
            putValue(Action.SMALL_ICON, icon);
    }

    /**
     * Sets a tool tip text.
     *
     * @param tip
     *            Tool tip text
     */
    public void setToolTipText(String tip) {
        putValue(Action.SHORT_DESCRIPTION, tip);
    }

    /**
     * Invoked when the action was triggered.
     * <p>
     * This method contains the main implementation of the action.
     *
     * @param e
     *            {@link ActionEvent} containing more details.
     */
    public abstract void onAction(ActionEvent e);

    /**
     * Returns the {@link Frame} this action was invoked in.
     *
     * @param event
     *            {@link EventObject} containing the event source
     * @return {@link Frame} this action was invoked in
     */
    protected Frame getFrame(EventObject event) {
        Object src = event.getSource();
        while (src != null) {
            if (src instanceof Frame) {
                return (Frame) src;
            }

            if (src instanceof JPopupMenu) {
                src = ((JPopupMenu) src).getInvoker();
            } else if (src instanceof Component) {
                src = ((Component) src).getParent();
            } else {
                break;
            }
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        onAction(e);
    }

}
