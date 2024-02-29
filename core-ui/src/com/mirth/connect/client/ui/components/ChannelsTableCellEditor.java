/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Component;
import java.awt.Insets;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.codetemplate.CodeTemplatePanel;

public class ChannelsTableCellEditor extends DefaultCellEditor {

    private JCheckBox checkBox;

    public ChannelsTableCellEditor() {
        super(new MirthCheckBox());
        checkBox = (JCheckBox) editorComponent;
        checkBox.setMargin(new Insets(0, 5, 0, 0));
    }

    @Override
    public Object getCellEditorValue() {
        return new ChannelInfo(checkBox.getText(), checkBox.isSelected());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        ChannelInfo channelInfo = (ChannelInfo) value;
        checkBox.setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
        checkBox.setSelected(channelInfo.isEnabled());
        checkBox.setText(channelInfo.getName());
        if (channelInfo.getName().equals(CodeTemplatePanel.NEW_CHANNELS)) {
            checkBox.setToolTipText(table.getToolTipText());
        } else {
            checkBox.setToolTipText(channelInfo.getName());
        }
        return checkBox;
    }
}