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
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JTable;

import org.apache.commons.collections.MapUtils;

import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;

public class DataTypesComboBoxCellEditor extends MirthComboBoxCellEditor {

    private boolean source;
    
    public DataTypesComboBoxCellEditor(JTable table, String[] items, int clickCount, boolean focusable, boolean source) {
        super(table, items, clickCount, focusable);
        this.source = source;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (super.table.getEditingRow() != -1) {
            int selectedRow = super.table.convertRowIndexToModel(super.table.getEditingRow());
            String dataTypeDisplayName = (String)((JComboBox)super.getComponent()).getSelectedItem();
            Channel currentChannel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
            String dataType = getDataTypeFromDisplayName(dataTypeDisplayName);
            Properties defaultProperties = MapUtils.toProperties(LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties());
            
            // Set the new data type and default properties for that data type
            if (source) {
                if (selectedRow == 0) {
                    if (!currentChannel.getSourceConnector().getTransformer().getInboundDataType().equals(dataType)) {
                        currentChannel.getSourceConnector().getTransformer().setInboundDataType(dataType);
                        currentChannel.getSourceConnector().getTransformer().setInboundProperties(defaultProperties);
                    }
                } else {
                    if (!currentChannel.getSourceConnector().getTransformer().getOutboundDataType().equals(dataType)) {
                        currentChannel.getSourceConnector().getTransformer().setOutboundDataType(dataType);
                        currentChannel.getSourceConnector().getTransformer().setOutboundProperties(defaultProperties);
                        
                        // Also set the inbound data type and properties for all destinations
                        for (Connector connector : currentChannel.getDestinationConnectors()) {
                            connector.getTransformer().setInboundDataType(dataType);
                            connector.getTransformer().setInboundProperties(defaultProperties);
                        }
                    }
                }
            } else {
                if (!currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().getOutboundDataType().equals(dataType)) {
                    currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().setOutboundDataType(dataType);
                    currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().setOutboundProperties(defaultProperties);
                }
            }
        }
        
        PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
    }
    
    private String getDataTypeFromDisplayName(String dataTypeDisplayName) {
        for (String dataType : LoadedExtensions.getInstance().getDataTypePlugins().keySet()) {
            if (PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.get(dataType).equals(dataTypeDisplayName)) {
                return dataType;
            }
        }
        
        return null;
    }
    
    @Override
    public boolean isCellEditable(EventObject anEvent) {
        boolean editable = super.isCellEditable(anEvent);
        
        // The cell (combobox) is not editable if it's the source 
        // inbound data type and it is required to be XML
        if (editable) {
            if (anEvent instanceof MouseEvent) {
                if (source && (super.table.rowAtPoint(((MouseEvent) anEvent).getPoint()) == 0) && PlatformUI.MIRTH_FRAME.channelEditPanel.requiresXmlDataType()) {
                    editable= false;
                }
            }
        }

        return editable;
    }

}
