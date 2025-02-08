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
package org.shredzone.feinrip.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.TableModel;

import org.shredzone.feinrip.model.Language;
import org.shredzone.feinrip.system.LanguageUtils;

/**
 * A {@link JTable} that is enabled to edit {@link Language} cells.
 *
 * @author Richard "Shred" Körber
 */
public class JLanguageEditableTable extends JTable {
    private static final long serialVersionUID = 3391769364717652460L;

    private static final int LANGUAGE_MIN_WIDTH = 180;

    /**
     * Creates a new {@link JLanguageEditableTable}.
     */
    public JLanguageEditableTable() {
        ListSelectionModel selModel = new NoListSelectionModel();
        selModel.setSelectionMode(NoListSelectionModel.NO_SELECTION);
        setSelectionModel(selModel);

        Collection<Language> languages = LanguageUtils.getLanguages();
        JComboBox<Language> editor = new JComboBox<Language>(languages.toArray(new Language[languages.size()]));
        editor.setRenderer(new LanguageListCellRenderer(editor.getRenderer()));
        setDefaultEditor(Language.class, new DefaultCellEditor(editor));
    }

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);

        // Set the minimum width of all Language columns
        for (int ix = 0; ix < dataModel.getColumnCount(); ix++) {
            if (dataModel.getColumnClass(ix).isAssignableFrom(Language.class)) {
                getColumnModel().getColumn(ix).setMinWidth(LANGUAGE_MIN_WIDTH);
            }
        }
    }

    /**
     * {@link ListCellRenderer} that shows a separator below the "undefined" language.
     */
    private static class LanguageListCellRenderer implements ListCellRenderer<Language> {
        private final Border separator = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);
        private final ListCellRenderer<? super Language> delegate;

        public LanguageListCellRenderer(ListCellRenderer<? super Language> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Language> list, Language value, int index, boolean isSelected, boolean cellHasFocus) {
            JComponent comp = (JComponent) delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            comp.setBorder("und".equals(value.getIso2()) ? separator : null);
            return comp;
        }
    }

}
