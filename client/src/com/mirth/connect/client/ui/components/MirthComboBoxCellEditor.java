/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

public class MirthComboBoxCellEditor extends DefaultCellEditor implements ActionListener {

    JTable table;
    int clickCount;

    public MirthComboBoxCellEditor(JTable table, String[] items, int clickCount, boolean focusable) {
        super(new JComboBox(items));
        this.table = table;
        this.clickCount = clickCount;
        super.getComponent().setFocusable(focusable);
        ((JComboBox)super.getComponent()).addActionListener(this);
    }

    /**
     * Enables the editor only for clickCount.
     */
    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= clickCount;
        }

        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {        
    }
    
    
}
