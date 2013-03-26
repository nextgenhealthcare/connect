/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.util.prefs.Preferences;

import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.NumberCellRenderer;
import com.mirth.connect.client.ui.UIConstants;

public class ItemSelectionTable extends MirthTable {
    private final static int CHECKBOX_COLUMN_WIDTH = 50;

    public ItemSelectionTable() {
        setDragEnabled(false);
        setRowSelectionAllowed(false);
        setRowHeight(UIConstants.ROW_HEIGHT);
        setFocusable(false);
        setOpaque(true);
        getTableHeader().setReorderingAllowed(false);
        setSortable(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {}
        });
    }

    public void setModel(TableModel model) {
        super.setModel(model);

        if (model instanceof ItemSelectionTableModel) {
            getColumnExt(ItemSelectionTableModel.CHECKBOX_COLUMN).setMaxWidth(CHECKBOX_COLUMN_WIDTH);
            getColumnExt(ItemSelectionTableModel.CHECKBOX_COLUMN).setMinWidth(CHECKBOX_COLUMN_WIDTH);

            if (getColumnExt(ItemSelectionTableModel.KEY_COLUMN).getTitle() != null) {
                getColumnExt(ItemSelectionTableModel.KEY_COLUMN).setMaxWidth(UIConstants.METADATA_ID_COLUMN_WIDTH);
                getColumnExt(ItemSelectionTableModel.KEY_COLUMN).setMinWidth(UIConstants.METADATA_ID_COLUMN_WIDTH);
                getColumnExt(ItemSelectionTableModel.KEY_COLUMN).setPreferredWidth(UIConstants.METADATA_ID_COLUMN_WIDTH);
                getColumnExt(ItemSelectionTableModel.KEY_COLUMN).setCellRenderer(new NumberCellRenderer(SwingConstants.CENTER, false));
                getColumnExt(ItemSelectionTableModel.KEY_COLUMN).setVisible(true);
            } else {
                getColumnExt(ItemSelectionTableModel.KEY_COLUMN).setVisible(false);
            }
        }
    }

    public class ItemSelectionTableException extends RuntimeException {
        public ItemSelectionTableException(String message) {
            super(message);
        }
    }
}
