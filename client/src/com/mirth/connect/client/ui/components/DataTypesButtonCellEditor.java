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
import java.util.Properties;

import javax.swing.JTable;

import com.mirth.connect.client.ui.DataTypesDialog;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.editors.BoundPropertiesSheetDialog;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.plugins.DataTypeClientPlugin;

public class DataTypesButtonCellEditor extends MirthButtonCellEditor {

    private boolean source;
    
    public DataTypesButtonCellEditor(JTable table, boolean source) {
        super(table);
        this.source = source;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedRow = super.table.convertRowIndexToModel(super.table.getEditingRow());
        String dataType = (String) super.table.getModel().getValueAt(selectedRow, DataTypesDialog.DATA_TYPE_COLUMN_NUMBER);
        Channel currentChannel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
        
        // Load the properties editor for the selected data type
        if (source) {
            if (selectedRow == 0) {
                loadPropertiesEditor(dataType, currentChannel.getSourceConnector().getTransformer().getInboundProperties());
            } else {
                loadPropertiesEditor(dataType, currentChannel.getSourceConnector().getTransformer().getOutboundProperties());
                
                // Also set the inbound properties for all destinations
                for (Connector connector : currentChannel.getDestinationConnectors()) {
                    connector.getTransformer().setInboundProperties(currentChannel.getSourceConnector().getTransformer().getOutboundProperties());
                }
            }
        } else {
            loadPropertiesEditor(dataType, currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().getOutboundProperties());
        }
        
        PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
    }
    
    private void loadPropertiesEditor(String dataType, Properties dataProperties) {
        DataTypeClientPlugin dataTypeClientPlugin = LoadedExtensions.getInstance().getDataTypePlugins().get(PlatformUI.MIRTH_FRAME.displayNameToDataType.get(dataType));
        Object beanProperties = dataTypeClientPlugin.getBeanProperties();
        
        if (beanProperties != null) {
            new BoundPropertiesSheetDialog(dataProperties, beanProperties, dataTypeClientPlugin.getBeanDimensions());
        }
    }

}
