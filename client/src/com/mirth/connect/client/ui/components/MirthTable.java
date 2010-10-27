/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.jdesktop.swingx.JXTable;

import com.mirth.connect.client.ui.PlatformUI;

public class MirthTable extends JXTable {

    /** Creates a new instance of MirthTable */
    public MirthTable() {
        super();
        this.setDragEnabled(true);
        this.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                boolean isAccelerated = (e.getModifiers() & java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0;
                if ((e.getKeyCode() == KeyEvent.VK_S) && isAccelerated) {
                    PlatformUI.MIRTH_FRAME.doContextSensitiveSave();
                }
            }

            public void keyReleased(KeyEvent e) {
                // TODO Auto-generated method stub
            }

            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub
            }
        });

        // Swingx 1.0 has this set to true by default, which doesn't allow
        // dragging
        // and dropping into tables. Swingx 0.8 had this set to false.
        // Tables that want it set to true can override it.
        this.putClientProperty("terminateEditOnFocusLost", Boolean.FALSE);

    }

    @Override
    public Class getColumnClass(int column) {
        try {
            if (getRowCount() >= 0 && column >= 0 && column < getColumnCount() && getValueAt(0, column) != null) {
                return getValueAt(0, column).getClass();
            } else {
                return Object.class;
            }
        } catch (Exception e) {
            return Object.class;
        }
    }

    public int getColumnViewIndex(String columnName) {
        return this.getColumnModel().getColumnIndex(columnName);
    }

    public int getColumnModelIndex(String columnName) {
        return this.convertColumnIndexToModel(this.getColumnModel().getColumnIndex(columnName));
    }

    public int getSelectedModelIndex() {
        int index = -1;

        if (this.isEditing()) {
            index = this.getEditingRow();
        } else {
            index = this.getSelectedRow();
        }

        if (index == -1) {
            return index;
        }

        return this.convertRowIndexToModel(index);
    }

    public int[] getSelectedModelRows() {
        int[] views = this.getSelectedRows();

        for (int i = 0; i < views.length; i++) {
            views[i] = this.convertRowIndexToModel(views[i]);
        }

        return views;
    }
}
