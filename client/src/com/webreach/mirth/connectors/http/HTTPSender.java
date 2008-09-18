/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.connectors.http;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.connectors.vm.ChannelWriterProperties;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.QueuedSenderProperties;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class HTTPSender extends ConnectorClass
{
    private final int VARIABLE_COLUMN = 0;
    
    private final int VALUE_COLUMN = 1;
    
    private final String VARIABLE_COLUMN_NAME = "Variable";
    
    private final String VALUE_COLUMN_NAME = "Value";
    
    private int propertiesLastIndex = -1;
    
    private int headerLastIndex = -1;
    
    /** Creates new form HTTPWriter */
    private HashMap channelList;
    
    public HTTPSender()
    {
        name = HTTPSenderProperties.name;
        initComponents();
        propertiesPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows(propertiesTable, deleteButton);
            }
        });
        headerVariablesPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows(headerVariablesTable, headerDeleteButton);
            }
        });
        deleteButton.setEnabled(false);
        headerDeleteButton.setEnabled(false);
    }
    
    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(HTTPSenderProperties.DATATYPE, name);
        properties.put(HTTPSenderProperties.HTTP_URL, httpURL.getText());
        
        if (post.isSelected())
            properties.put(HTTPSenderProperties.HTTP_METHOD, "post");
        else
            properties.put(HTTPSenderProperties.HTTP_METHOD, "get");
        
        if (responseHeadersIncludeButton.isSelected())
            properties.put(HTTPSenderProperties.HTTP_EXCLUDE_HEADERS, UIConstants.NO_OPTION);
        else
            properties.put(HTTPSenderProperties.HTTP_EXCLUDE_HEADERS, UIConstants.YES_OPTION);
        
        properties.put(HTTPSenderProperties.CHANNEL_ID, channelList.get((String) channelNames.getSelectedItem()));
        
        properties.put(QueuedSenderProperties.RECONNECT_INTERVAL, reconnectInterval.getText());
        
        if (usePersistentQueuesYesRadio.isSelected())
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.YES_OPTION);
        else
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.NO_OPTION);

        if (rotateMessages.isSelected())
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.YES_OPTION);
        else
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.NO_OPTION);
        
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(HTTPSenderProperties.HTTP_ADDITIONAL_PROPERTIES, serializer.toXML(getAdditionalProperties()));
        properties.put(HTTPSenderProperties.HTTP_HEADER_PROPERTIES, serializer.toXML(getHeaderProperties()));
        return properties;
    }
    
    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
        
        httpURL.setText((String) props.get(HTTPSenderProperties.HTTP_URL));
        
        if (((String) props.get(HTTPSenderProperties.HTTP_METHOD)).equalsIgnoreCase("post"))
            post.setSelected(true);
        else
            get.setSelected(true);
        
        if (((String) props.get(HTTPSenderProperties.HTTP_EXCLUDE_HEADERS)).equals(UIConstants.YES_OPTION))
            responseHeadersExcludeButton.setSelected(true);
        else
            responseHeadersIncludeButton.setSelected(true);
        
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        
        if (((String) props.get(HTTPSenderProperties.HTTP_ADDITIONAL_PROPERTIES)).length() > 0)
            setAdditionalProperties((Properties) serializer.fromXML((String) props.get(HTTPSenderProperties.HTTP_ADDITIONAL_PROPERTIES)));
        else
            setAdditionalProperties(new Properties());
        
        if (((String) props.get(HTTPSenderProperties.HTTP_HEADER_PROPERTIES)).length() > 0)
            setHeaderProperties((Properties) serializer.fromXML((String) props.get(HTTPSenderProperties.HTTP_HEADER_PROPERTIES)));
        else
            setHeaderProperties(new Properties());
        
        reconnectInterval.setText((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL));
        
        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION))
        {
            usePersistentQueuesYesRadio.setSelected(true);
            usePersistentQueuesYesRadioActionPerformed(null);
        }
        else
        {
            usePersistentQueuesNoRadio.setSelected(true);
            usePersistentQueuesNoRadioActionPerformed(null);
        }
        
        if (((String) props.get(QueuedSenderProperties.ROTATE_QUEUE)).equals(UIConstants.YES_OPTION))
            rotateMessages.setSelected(true);
        else
            rotateMessages.setSelected(false);
        
        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap();
        channelList.put("None", "sink");
        channelNameArray.add("None");
        
        String selectedChannelName = "None";
        
        for (Channel channel : parent.channels.values())
        {
        	if (((String) props.get(HTTPSenderProperties.CHANNEL_ID)).equalsIgnoreCase(channel.getId()))
        		selectedChannelName = channel.getName();
        	
            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }
        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));
        
        channelNames.setSelectedItem(selectedChannelName);
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }
    
    public Properties getDefaults()
    {
        return new HTTPSenderProperties().getDefaults();
    }
    
    public void setAdditionalProperties(Properties properties)
    {
        Object[][] tableData = new Object[properties.size()][2];
        
        propertiesTable = new MirthTable();
        
        int j = 0;
        Iterator i = properties.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry entry = (Map.Entry) i.next();
            tableData[j][VARIABLE_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }
        
        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] { VARIABLE_COLUMN_NAME, VALUE_COLUMN_NAME })
        {
            boolean[] canEdit = new boolean[] { true, true };
            
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });
        
        propertiesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (getSelectedRow(propertiesTable) != -1)
                {
                    propertiesLastIndex = getSelectedRow(propertiesTable);
                    deleteButton.setEnabled(true);
                }
                else
                    deleteButton.setEnabled(false);
            }
        });
        
        class HTTPTableCellEditor extends AbstractCellEditor implements TableCellEditor
        {
            JComponent component = new JTextField();
            
            Object originalValue;
            
            boolean checkProperties;
            
            public HTTPTableCellEditor(boolean checkProperties)
            {
                super();
                this.checkProperties = checkProperties;
            }
            
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
            {
                // 'value' is value contained in the cell located at (rowIndex,
                // vColIndex)
                originalValue = value;
                
                if (isSelected)
                {
                    // cell (and perhaps other cells) are selected
                }
                
                // Configure the component with the specified value
                ((JTextField) component).setText((String) value);
                
                // Return the configured component
                return component;
            }
            
            public Object getCellEditorValue()
            {
                return ((JTextField) component).getText();
            }
            
            public boolean stopCellEditing()
            {
                String s = (String) getCellEditorValue();
                
                if (checkProperties && (s.length() == 0 || checkUniqueProperty(s)))
                    super.cancelCellEditing();
                else
                    parent.enableSave();
                
                deleteButton.setEnabled(true);
                
                return super.stopCellEditing();
            }
            
            public boolean checkUniqueProperty(String property)
            {
                boolean exists = false;
                
                for (int i = 0; i < propertiesTable.getRowCount(); i++)
                {
                    if (propertiesTable.getValueAt(i, VARIABLE_COLUMN) != null && ((String) propertiesTable.getValueAt(i, VARIABLE_COLUMN)).equalsIgnoreCase(property))
                        exists = true;
                }
                
                return exists;
            }
            
            /**
             * Enables the editor only for double-clicks.
             */
            public boolean isCellEditable(EventObject evt)
            {
                if (evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2)
                {
                    deleteButton.setEnabled(false);
                    return true;
                }
                return false;
            }
        };
        
        // Set the custom cell editor for the Destination Name column.
        propertiesTable.getColumnModel().getColumn(propertiesTable.getColumnModel().getColumnIndex(VARIABLE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(true));
        
        // Set the custom cell editor for the Destination Name column.
        propertiesTable.getColumnModel().getColumn(propertiesTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(false));
        
        propertiesTable.setSelectionMode(0);
        propertiesTable.setRowSelectionAllowed(true);
        propertiesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        propertiesTable.setDragEnabled(false);
        propertiesTable.setOpaque(true);
        propertiesTable.setSortable(false);
        propertiesTable.getTableHeader().setReorderingAllowed(false);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            propertiesTable.setHighlighters(highlighter);
        }
        
        propertiesPane.setViewportView(propertiesTable);
    }
    
    public void setHeaderProperties(Properties properties)
    {
        Object[][] tableData = new Object[properties.size()][2];
        
        headerVariablesTable = new MirthTable();
        
        int j = 0;
        Iterator i = properties.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry entry = (Map.Entry) i.next();
            tableData[j][VARIABLE_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }
        
        headerVariablesTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] { VARIABLE_COLUMN_NAME, VALUE_COLUMN_NAME })
        {
            boolean[] canEdit = new boolean[] { true, true };
            
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });
        
        headerVariablesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (getSelectedRow(headerVariablesTable) != -1)
                {
                    headerLastIndex = getSelectedRow(headerVariablesTable);
                    headerDeleteButton.setEnabled(true);
                }
                else
                    headerDeleteButton.setEnabled(false);
            }
        });
        
        class HTTPTableCellEditor extends AbstractCellEditor implements TableCellEditor
        {
            JComponent component = new JTextField();
            
            Object originalValue;
            
            boolean checkProperties;
            
            public HTTPTableCellEditor(boolean checkProperties)
            {
                super();
                this.checkProperties = checkProperties;
            }
            
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
            {
                // 'value' is value contained in the cell located at (rowIndex,
                // vColIndex)
                originalValue = value;
                
                if (isSelected)
                {
                    // cell (and perhaps other cells) are selected
                }
                
                // Configure the component with the specified value
                ((JTextField) component).setText((String) value);
                
                // Return the configured component
                return component;
            }
            
            public Object getCellEditorValue()
            {
                return ((JTextField) component).getText();
            }
            
            public boolean stopCellEditing()
            {
                String s = (String) getCellEditorValue();
                
                if (checkProperties && (s.length() == 0 || checkUniqueProperty(s)))
                    super.cancelCellEditing();
                else
                    parent.enableSave();
                
                headerDeleteButton.setEnabled(true);
                
                return super.stopCellEditing();
            }
            
            public boolean checkUniqueProperty(String property)
            {
                boolean exists = false;
                
                for (int i = 0; i < headerVariablesTable.getRowCount(); i++)
                {
                    if (headerVariablesTable.getValueAt(i, VARIABLE_COLUMN) != null && ((String) headerVariablesTable.getValueAt(i, VARIABLE_COLUMN)).equalsIgnoreCase(property))
                        exists = true;
                }
                
                return exists;
            }
            
            /**
             * Enables the editor only for double-clicks.
             */
            public boolean isCellEditable(EventObject evt)
            {
                if (evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2)
                {
                    headerDeleteButton.setEnabled(false);
                    return true;
                }
                return false;
            }
        };
        
        // Set the custom cell editor for the Destination Name column.
        headerVariablesTable.getColumnModel().getColumn(headerVariablesTable.getColumnModel().getColumnIndex(VARIABLE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(true));
        
        // Set the custom cell editor for the Destination Name column.
        headerVariablesTable.getColumnModel().getColumn(headerVariablesTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(false));
        
        headerVariablesTable.setSelectionMode(0);
        headerVariablesTable.setRowSelectionAllowed(true);
        headerVariablesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        headerVariablesTable.setDragEnabled(false);
        headerVariablesTable.setOpaque(true);
        headerVariablesTable.setSortable(false);
        headerVariablesTable.getTableHeader().setReorderingAllowed(false);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            headerVariablesTable.setHighlighters(highlighter);
        }
        
        headerVariablesPane.setViewportView(headerVariablesTable);
    }
    
    public Map getAdditionalProperties()
    {
        Properties properties = new Properties();
        
        for (int i = 0; i < propertiesTable.getRowCount(); i++)
            if (((String) propertiesTable.getValueAt(i, VARIABLE_COLUMN)).length() > 0)
                properties.put(((String) propertiesTable.getValueAt(i, VARIABLE_COLUMN)), ((String) propertiesTable.getValueAt(i, VALUE_COLUMN)));
        
        return properties;
    }
    
    public Map getHeaderProperties()
    {
        Properties properties = new Properties();
        
        for (int i = 0; i < headerVariablesTable.getRowCount(); i++)
            if (((String) headerVariablesTable.getValueAt(i, VARIABLE_COLUMN)).length() > 0)
                properties.put(((String) headerVariablesTable.getValueAt(i, VARIABLE_COLUMN)), ((String) headerVariablesTable.getValueAt(i, VALUE_COLUMN)));
        
        return properties;
    }
    
    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows(MirthTable table, JButton button)
    {
        table.clearSelection();
        button.setEnabled(false);
    }
    
    /** Get the currently selected table index */
    public int getSelectedRow(MirthTable table)
    {
        if (table.isEditing())
            return table.getEditingRow();
        else
            return table.getSelectedRow();
    }
    
    /**
     * Get the name that should be used for a new property so that it is unique.
     */
    private String getNewPropertyName(MirthTable table)
    {
        String temp = "Property ";
        
        for (int i = 1; i <= table.getRowCount() + 1; i++)
        {
            boolean exists = false;
            for (int j = 0; j < table.getRowCount(); j++)
            {
                if (((String) table.getValueAt(j, VARIABLE_COLUMN)).equalsIgnoreCase(temp + i))
                {
                    exists = true;
                }
            }
            if (!exists)
                return temp + i;
        }
        return "";
    }
    
    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.getProperty(HTTPSenderProperties.HTTP_URL)).length() == 0)
        {
            valid = false;
            if (highlight)
            	httpURL.setBackground(UIConstants.INVALID_COLOR);
        }
        
        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION) && ((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL)).length() == 0)
        {
            valid = false;
            if (highlight)
            	reconnectInterval.setBackground(UIConstants.INVALID_COLOR);
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        httpURL.setBackground(null);
    }
    
    public String doValidate(Properties props, boolean highlight)
    {
    	String error = null;
    	
    	if (!checkProperties(props, highlight))
    		error = "Error in the form for connector \"" + getName() + "\".\n\n";
    	
    	return error;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        methodButtonGroup = new javax.swing.ButtonGroup();
        responseHeadersButtonGroup = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel7 = new javax.swing.JLabel();
        httpURL = new com.webreach.mirth.client.ui.components.MirthTextField();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        propertiesPane = new javax.swing.JScrollPane();
        propertiesTable = new com.webreach.mirth.client.ui.components.MirthTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        post = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        get = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        channelNames = new com.webreach.mirth.client.ui.components.MirthComboBox();
        URL1 = new javax.swing.JLabel();
        headerVariablesPane = new javax.swing.JScrollPane();
        headerVariablesTable = new com.webreach.mirth.client.ui.components.MirthTable();
        jLabel3 = new javax.swing.JLabel();
        headerNewButton = new javax.swing.JButton();
        headerDeleteButton = new javax.swing.JButton();
        responseHeadersLabel = new javax.swing.JLabel();
        responseHeadersIncludeButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        responseHeadersExcludeButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        rotateMessages = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        usePersistentQueuesNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        usePersistentQueuesYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel36 = new javax.swing.JLabel();
        reconnectIntervalLabel = new javax.swing.JLabel();
        reconnectInterval = new com.webreach.mirth.client.ui.components.MirthTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel7.setText("URL:");

        newButton.setText("New");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Property", "Value"
            }
        ));
        propertiesPane.setViewportView(propertiesTable);

        jLabel2.setText("Request Variables:");

        jLabel1.setText("HTTP Method:");

        post.setBackground(new java.awt.Color(255, 255, 255));
        post.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(post);
        post.setText("Post");
        post.setMargin(new java.awt.Insets(0, 0, 0, 0));

        get.setBackground(new java.awt.Color(255, 255, 255));
        get.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(get);
        get.setText("Get");
        get.setMargin(new java.awt.Insets(0, 0, 0, 0));

        channelNames.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        URL1.setText("Send Response to:");

        headerVariablesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Property", "Value"
            }
        ));
        headerVariablesPane.setViewportView(headerVariablesTable);

        jLabel3.setText("Header Variables:");

        headerNewButton.setText("New");
        headerNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headerNewButtonActionPerformed(evt);
            }
        });

        headerDeleteButton.setText("Delete");
        headerDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headerDeleteButtonActionPerformed(evt);
            }
        });

        responseHeadersLabel.setText("Response Headers:");

        responseHeadersIncludeButton.setBackground(new java.awt.Color(255, 255, 255));
        responseHeadersIncludeButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        responseHeadersButtonGroup.add(responseHeadersIncludeButton);
        responseHeadersIncludeButton.setText("Include");
        responseHeadersIncludeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        responseHeadersExcludeButton.setBackground(new java.awt.Color(255, 255, 255));
        responseHeadersExcludeButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        responseHeadersButtonGroup.add(responseHeadersExcludeButton);
        responseHeadersExcludeButton.setText("Exclude");
        responseHeadersExcludeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        rotateMessages.setBackground(new java.awt.Color(255, 255, 255));
        rotateMessages.setText("Rotate Messages in Queue");

        usePersistentQueuesNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(usePersistentQueuesNoRadio);
        usePersistentQueuesNoRadio.setSelected(true);
        usePersistentQueuesNoRadio.setText("No");
        usePersistentQueuesNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesNoRadioActionPerformed(evt);
            }
        });

        usePersistentQueuesYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(usePersistentQueuesYesRadio);
        usePersistentQueuesYesRadio.setText("Yes");
        usePersistentQueuesYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesYesRadioActionPerformed(evt);
            }
        });

        jLabel36.setText("Use Persistent Queues:");

        reconnectIntervalLabel.setText("Reconnect Interval (ms):");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, reconnectIntervalLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, responseHeadersLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel36)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, URL1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(headerVariablesPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(headerNewButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(headerDeleteButton)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(propertiesPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(deleteButton)
                            .add(newButton)))
                    .add(httpURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(post, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(get, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(channelNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(responseHeadersIncludeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(responseHeadersExcludeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(usePersistentQueuesYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(usePersistentQueuesNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rotateMessages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(reconnectInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {deleteButton, newButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(httpURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(post, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(get, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(URL1)
                    .add(channelNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel36)
                    .add(usePersistentQueuesYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(usePersistentQueuesNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rotateMessages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(reconnectInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(reconnectIntervalLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(responseHeadersLabel)
                    .add(responseHeadersIncludeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(responseHeadersExcludeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(newButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteButton))
                    .add(propertiesPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(headerVariablesPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                    .add(jLabel3)
                    .add(layout.createSequentialGroup()
                        .add(headerNewButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(headerDeleteButton)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void headerDeleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_headerDeleteButtonActionPerformed
    {//GEN-HEADEREND:event_headerDeleteButtonActionPerformed
        if (getSelectedRow(headerVariablesTable) != -1 && !headerVariablesTable.isEditing())
        {
            ((DefaultTableModel) headerVariablesTable.getModel()).removeRow(getSelectedRow(headerVariablesTable));
            
            if (headerVariablesTable.getRowCount() != 0)
            {
                if (headerLastIndex == 0)
                    headerVariablesTable.setRowSelectionInterval(0, 0);
                else if (headerLastIndex == headerVariablesTable.getRowCount())
                    headerVariablesTable.setRowSelectionInterval(headerLastIndex - 1, headerLastIndex - 1);
                else
                    headerVariablesTable.setRowSelectionInterval(headerLastIndex, headerLastIndex);
            }
            
            parent.enableSave();
        }
    }//GEN-LAST:event_headerDeleteButtonActionPerformed
    
    private void headerNewButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_headerNewButtonActionPerformed
    {//GEN-HEADEREND:event_headerNewButtonActionPerformed
        ((DefaultTableModel) headerVariablesTable.getModel()).addRow(new Object[] { getNewPropertyName(headerVariablesTable), "" });
        headerVariablesTable.setRowSelectionInterval(headerVariablesTable.getRowCount() - 1, headerVariablesTable.getRowCount() - 1);
        parent.enableSave();
    }//GEN-LAST:event_headerNewButtonActionPerformed

private void usePersistentQueuesNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesNoRadioActionPerformed
rotateMessages.setEnabled(false);
reconnectInterval.setEnabled(false);
reconnectIntervalLabel.setEnabled(false);
}//GEN-LAST:event_usePersistentQueuesNoRadioActionPerformed

private void usePersistentQueuesYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesYesRadioActionPerformed
rotateMessages.setEnabled(true);
reconnectInterval.setEnabled(true);
reconnectIntervalLabel.setEnabled(true);
}//GEN-LAST:event_usePersistentQueuesYesRadioActionPerformed
    
    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_deleteButtonActionPerformed
    {// GEN-HEADEREND:event_deleteButtonActionPerformed
        if (getSelectedRow(propertiesTable) != -1 && !propertiesTable.isEditing())
        {
            ((DefaultTableModel) propertiesTable.getModel()).removeRow(getSelectedRow(propertiesTable));
            
            if (propertiesTable.getRowCount() != 0)
            {
                if (propertiesLastIndex == 0)
                    propertiesTable.setRowSelectionInterval(0, 0);
                else if (propertiesLastIndex == propertiesTable.getRowCount())
                    propertiesTable.setRowSelectionInterval(propertiesLastIndex - 1, propertiesLastIndex - 1);
                else
                    propertiesTable.setRowSelectionInterval(propertiesLastIndex, propertiesLastIndex);
            }
            
            parent.enableSave();
        }
    }// GEN-LAST:event_deleteButtonActionPerformed
    
    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_newButtonActionPerformed
    {// GEN-HEADEREND:event_newButtonActionPerformed
        ((DefaultTableModel) propertiesTable.getModel()).addRow(new Object[] { getNewPropertyName(propertiesTable), "" });
        propertiesTable.setRowSelectionInterval(propertiesTable.getRowCount() - 1, propertiesTable.getRowCount() - 1);
        parent.enableSave();
    }// GEN-LAST:event_newButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL1;
    private javax.swing.ButtonGroup buttonGroup1;
    private com.webreach.mirth.client.ui.components.MirthComboBox channelNames;
    private javax.swing.JButton deleteButton;
    private com.webreach.mirth.client.ui.components.MirthRadioButton get;
    private javax.swing.JButton headerDeleteButton;
    private javax.swing.JButton headerNewButton;
    private javax.swing.JScrollPane headerVariablesPane;
    private com.webreach.mirth.client.ui.components.MirthTable headerVariablesTable;
    private com.webreach.mirth.client.ui.components.MirthTextField httpURL;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel7;
    private javax.swing.ButtonGroup methodButtonGroup;
    private javax.swing.JButton newButton;
    private com.webreach.mirth.client.ui.components.MirthRadioButton post;
    private javax.swing.JScrollPane propertiesPane;
    private com.webreach.mirth.client.ui.components.MirthTable propertiesTable;
    private com.webreach.mirth.client.ui.components.MirthTextField reconnectInterval;
    private javax.swing.JLabel reconnectIntervalLabel;
    private javax.swing.ButtonGroup responseHeadersButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton responseHeadersExcludeButton;
    private com.webreach.mirth.client.ui.components.MirthRadioButton responseHeadersIncludeButton;
    private javax.swing.JLabel responseHeadersLabel;
    private com.webreach.mirth.client.ui.components.MirthCheckBox rotateMessages;
    private com.webreach.mirth.client.ui.components.MirthRadioButton usePersistentQueuesNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton usePersistentQueuesYesRadio;
    // End of variables declaration//GEN-END:variables
    
}
