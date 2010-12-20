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

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.util.PropertiesUtil;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.converters.DefaultSerializerPropertiesFactory;

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
            String dataType = (String)((JComboBox)super.getComponent()).getSelectedItem();
            Channel currentChannel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
            MessageObject.Protocol protocol = getProtocol(dataType);
            Properties defaultProperties = PropertiesUtil.convertMapToProperties(DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(protocol));
            
            // Set the new data type and default properties for that data type
            if (source) {
                if (selectedRow == 0) {
                    if (!currentChannel.getSourceConnector().getTransformer().getInboundProtocol().equals(protocol)) {
                        currentChannel.getSourceConnector().getTransformer().setInboundProtocol(protocol);
                        currentChannel.getSourceConnector().getTransformer().setInboundProperties(defaultProperties);
                    }
                } else {
                    if (!currentChannel.getSourceConnector().getTransformer().getOutboundProtocol().equals(protocol)) {
                        currentChannel.getSourceConnector().getTransformer().setOutboundProtocol(protocol);
                        currentChannel.getSourceConnector().getTransformer().setOutboundProperties(defaultProperties);
                        
                        // Also set the inbound data type and properties for all destinations
                        for (Connector connector : currentChannel.getDestinationConnectors()) {
                            connector.getTransformer().setInboundProtocol(protocol);
                            connector.getTransformer().setInboundProperties(defaultProperties);
                        }
                    }
                }
            } else {
                if (!currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().getOutboundProtocol().equals(protocol)) {
                    currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().setOutboundProtocol(protocol);
                    currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().setOutboundProperties(defaultProperties);
                }
            }
        }
        
        PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
    }
    
    private MessageObject.Protocol getProtocol(String dataType) {
        for (MessageObject.Protocol protocol : MessageObject.Protocol.values()) {
            if (PlatformUI.MIRTH_FRAME.protocols.get(protocol).equals(dataType)) {
                return protocol;
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
