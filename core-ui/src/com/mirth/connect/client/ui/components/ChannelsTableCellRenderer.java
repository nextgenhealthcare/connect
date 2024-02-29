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

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.mirth.connect.client.ui.codetemplate.CodeTemplatePanel;

public class ChannelsTableCellRenderer extends JCheckBox implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        ChannelInfo channelInfo = (ChannelInfo) value;
        setEnabled(table.isEnabled());
        setSelected(channelInfo.isEnabled());
        setText(channelInfo.getName());
        if (channelInfo.getName().equals(CodeTemplatePanel.NEW_CHANNELS)) {
            setToolTipText(table.getToolTipText());
        } else {
            setToolTipText(channelInfo.getName());
        }
        setBackground(table.getBackground());
        setMargin(new Insets(0, 5, 0, 0));
        return this;
    }
}