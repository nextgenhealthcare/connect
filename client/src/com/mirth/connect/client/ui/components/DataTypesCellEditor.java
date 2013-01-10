/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Properties;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.apache.commons.collections.MapUtils;

import com.mirth.connect.client.ui.DataTypesDialog;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.editors.BoundPropertiesSheetDialog;
import com.mirth.connect.model.Channel;
import com.mirth.connect.plugins.DataTypeClientPlugin;

public class DataTypesCellEditor extends AbstractCellEditor implements TableCellEditor {

    private boolean source;
    private boolean inbound;
    private JPanel panel;
    private JTable table;
    private JComboBox comboBox;
    private JButton button;
    private int clickCount;
    private DataTypesDialog dataTypesDialog;
    
    public DataTypesCellEditor(JTable table, String[] items, int clickCount, boolean focusable, boolean source, boolean inbound, DataTypesDialog dataTypesDialog) {
        this.table = table;
        this.source = source;
        this.inbound = inbound;
        this.dataTypesDialog = dataTypesDialog;
        this.clickCount = clickCount;
        
        comboBox = new JComboBox(items);
        comboBox.setFocusable(focusable);
        comboBox.setMaximumRowCount(20);
        comboBox.setForeground(table.getForeground());
        comboBox.setBackground(table.getBackground());
        comboBox.addActionListener(new ComboBoxActionListener());
        boolean enabled = true;
        if (inbound) {
            if ((source && PlatformUI.MIRTH_FRAME.channelEditPanel.requiresXmlDataType()) || !source) {
                enabled = false;
            }
        }
        comboBox.setEnabled(enabled);
        
        button = new JButton();
        button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mirth/connect/client/ui/images/wrench.png")));
        button.setForeground(table.getForeground());
        button.setBackground(table.getBackground());
        button.setFocusable(false);
        button.addActionListener(new ButtonActionListener());
        
        
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder());
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        
        panel.add(comboBox, constraints);
        panel.add(button);
        
    }
    
    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= clickCount;
        }
        
        return false;
    }

    @Override
    public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        comboBox.setSelectedItem(value);
        return panel;
    }
    
    private class ComboBoxActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (table.getEditingRow() != -1) {
                int selectedRow = table.convertRowIndexToModel(table.getEditingRow());
                String dataTypeDisplayName = (String)(comboBox).getSelectedItem();
                Channel currentChannel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
                String dataType = PlatformUI.MIRTH_FRAME.displayNameToDataType.get(dataTypeDisplayName);
                Properties defaultProperties = MapUtils.toProperties(LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties());
                
                // Set the new data type and default properties for that data type
                if (source) {
                    if (inbound) {
                        if (!currentChannel.getSourceConnector().getTransformer().getInboundDataType().equals(dataType)) {
                            currentChannel.getSourceConnector().getTransformer().setInboundDataType(dataType);
                            currentChannel.getSourceConnector().getTransformer().setInboundProperties(defaultProperties);
                        }
                    } else {
                        if (!currentChannel.getSourceConnector().getTransformer().getOutboundDataType().equals(dataType)) {
                            currentChannel.getSourceConnector().getTransformer().setOutboundDataType(dataType);
                            currentChannel.getSourceConnector().getTransformer().setOutboundProperties(defaultProperties);
                            
                            // Also set the inbound data type and properties for all destinations
                            dataTypesDialog.updateDestinationInboundDataType(dataTypeDisplayName);
                        }
                    }
                } else {
                    if (!currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().getOutboundDataType().equals(dataType)) {
                        currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().setOutboundDataType(dataType);
                        currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().setOutboundProperties(defaultProperties);
                    }
                }
            }
            
            stopCellEditing();
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        }
        
    }
    
    private class ButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = table.convertRowIndexToModel(table.getEditingRow());
            // There's probably a better way to get this column number
            int dataTypeColumnNumber = inbound ? 1 : 2;
            String dataType = (String) table.getModel().getValueAt(selectedRow, dataTypeColumnNumber);
            Channel currentChannel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
            
            // Load the properties editor for the selected data type
            if (source) {
                if (inbound) {
                    loadPropertiesEditor(dataType, currentChannel.getSourceConnector().getTransformer().getInboundProperties());
                } else {
                    loadPropertiesEditor(dataType, currentChannel.getSourceConnector().getTransformer().getOutboundProperties());
                }
            } else {
                if (inbound) {
                    loadPropertiesEditor(dataType, currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().getInboundProperties());
                } else {
                    loadPropertiesEditor(dataType, currentChannel.getDestinationConnectors().get(selectedRow).getTransformer().getOutboundProperties());
                }
            }
            
            stopCellEditing();
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

}
