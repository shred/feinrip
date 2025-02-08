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

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.shredzone.feinrip.gui.model.SelectionTableModel;

/**
 * An action that selects (or unselects) all entries of a {@link SelectionTableModel}.
 *
 * @author Richard "Shred" Körber
 */
public class TableSelectAllAction extends AbstractSyncAction implements TableModelListener {
    private static final long serialVersionUID = 7608210988679697836L;

    private static final ResourceBundle B = ResourceBundle.getBundle("message");
    private static final Icon selectAllIcon = new ImageIcon(TableSelectAllAction.class.getResource("/org/shredzone/feinrip/icon/select-all.png"));
    private static final Icon unselectAllIcon = new ImageIcon(TableSelectAllAction.class.getResource("/org/shredzone/feinrip/icon/unselect-all.png"));

    private final boolean select;

    private SelectionTableModel model;

    /**
     * Creates a new {@link TableSelectAllAction}.
     *
     * @param select
     *            {@code true}: select all, {@code false}: unselect all
     */
    public TableSelectAllAction(boolean select) {
        super(B.getString(select ? "action.all.select" : "action.all.unselect"),
                        (select ? selectAllIcon : unselectAllIcon));
        this.select = select;
        updateEnabled();
    }

    /**
     * Sets the model this action is bound to.
     *
     * @param model
     *            {@link SelectionTableModel} to be changed
     */
    public void setModel(SelectionTableModel model) {
        if (this.model != null) {
            this.model.removeTableModelListener(this);
        }

        if (model != null) {
            model.addTableModelListener(this);
        }

        this.model = model;
        updateEnabled();
    }

    private void updateEnabled() {
        setEnabled(model != null && model.getRowCount() > 0);
    }

    @Override
    public void onAction(ActionEvent e) {
        if (model != null) {
            if (select) {
                model.selectAll();
            } else {
                model.unselectAll();
            }
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        updateEnabled();
    }

}
