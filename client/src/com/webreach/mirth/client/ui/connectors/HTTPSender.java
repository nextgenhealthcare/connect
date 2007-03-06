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


package com.webreach.mirth.client.ui.connectors;

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
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;


/** 
 * A form that extends from ConnectorClass.  All methods implemented
 * are described in ConnectorClass.
 */
public class HTTPSender extends ConnectorClass
{
    private final int VARIABLE_COLUMN = 0;
    private final int VALUE_COLUMN = 1;
    
    private final String VARIABLE_COLUMN_NAME = "Variable";
    private final String VALUE_COLUMN_NAME = "Value";
    
    private int lastIndex = -1;
    
    /** Creates new form HTTPWriter */
    private final String DATATYPE = "DataType";
    private final String HTTP_URL = "host";
    private final String HTTP_METHOD = "method";
    private final String HTTP_ADDITIONAL_PROPERTIES = "requestVariables";
    private final String CHANNEL_ID = "replyChannelId";
    private final String CHANNEL_NAME = "channelName";
    
    private HashMap channelList;
    
    public HTTPSender()
    {
        name = "HTTP Sender";
        initComponents();
        propertiesPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
        deleteButton.setEnabled(false);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(HTTP_URL, httpURL.getText());
        
        if(post.isSelected())
            properties.put(HTTP_METHOD, "post");
        else
            properties.put(HTTP_METHOD, "get");
               
        properties.put(CHANNEL_ID, channelList.get((String)channelNames.getSelectedItem()));
        properties.put(CHANNEL_NAME, (String)channelNames.getSelectedItem());        
        
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(HTTP_ADDITIONAL_PROPERTIES, serializer.toXML(getAdditionalProperties()));
        return properties;
    }

    public void setProperties(Properties props)
    {
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        httpURL.setText((String)props.get(HTTP_URL));

        if(((String)props.get(HTTP_METHOD)).equalsIgnoreCase("post"))
            post.setSelected(true);
        else
            get.setSelected(true);
        
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        
        if(((String)props.get(HTTP_ADDITIONAL_PROPERTIES)).length() > 0)
            setAdditionalProperties((Properties) serializer.fromXML((String)props.get(HTTP_ADDITIONAL_PROPERTIES)));
        else
            setAdditionalProperties(new Properties());                       
        
        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap();
        channelList.put("None", "sink");
        channelNameArray.add("None");
        for (Channel channel : parent.channels.values())
        {
            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }
        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));
        
        if(props.get(CHANNEL_NAME) != null)
            channelNames.setSelectedItem((String)props.get(CHANNEL_NAME));
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(HTTP_URL, "");
        properties.put(HTTP_METHOD, "post");
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(HTTP_ADDITIONAL_PROPERTIES, serializer.toXML(new Properties()));
        properties.put(CHANNEL_ID, "sink");
        properties.put(CHANNEL_NAME, "None");
        return properties;
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

        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(
        tableData, new String[] { VARIABLE_COLUMN_NAME, VALUE_COLUMN_NAME })
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
                if(getSelectedRow() != -1)
                {
                    lastIndex = getSelectedRow();
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
                // 'value' is value contained in the cell located at (rowIndex, vColIndex)
                originalValue = value;

                if (isSelected)
                {
                    // cell (and perhaps other cells) are selected
                }

                // Configure the component with the specified value
                ((JTextField)component).setText((String)value);

                // Return the configured component
                return component;
            }

            public Object getCellEditorValue()
            {
                return ((JTextField)component).getText();
            }
            
            public boolean stopCellEditing()
            {
                String s = (String)getCellEditorValue();
                
                if(checkProperties && (s.length() == 0 || checkUniqueProperty(s)))
                    super.cancelCellEditing();
                else
                    parent.enableSave();
                
                deleteButton.setEnabled(true);
                
                return super.stopCellEditing();
            }
            
            public boolean checkUniqueProperty(String property)
            {
                boolean exists = false;
                                
                for(int i = 0; i < propertiesTable.getRowCount(); i++)
                {
                    if(propertiesTable.getValueAt(i,VARIABLE_COLUMN) != null && ((String)propertiesTable.getValueAt(i,VARIABLE_COLUMN)).equalsIgnoreCase(property))
                        exists = true;                        
                }
                
                return exists;
            }
            
            /**
             * Enables the editor only for double-clicks.
             */
            public boolean isCellEditable(EventObject evt) 
            {
                if (evt instanceof MouseEvent && ((MouseEvent)evt).getClickCount() >= 2) 
                {
                    deleteButton.setEnabled(false);
                    return true;
                }
                return false;
            }
        };
        
        // Set the custom cell editor for the Destination Name column.
        propertiesTable.getColumnModel().getColumn(
                propertiesTable.getColumnModel().getColumnIndex(
                VARIABLE_COLUMN_NAME)).setCellEditor(
                new HTTPTableCellEditor(true));
        
        // Set the custom cell editor for the Destination Name column.
        propertiesTable.getColumnModel().getColumn(
                propertiesTable.getColumnModel().getColumnIndex(
                VALUE_COLUMN_NAME)).setCellEditor(
                new HTTPTableCellEditor(false));
                
        propertiesTable.setSelectionMode(0);
        propertiesTable.setRowSelectionAllowed(true);
        propertiesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        propertiesTable.setDragEnabled(false);
        propertiesTable.setOpaque(true);
        propertiesTable.setSortable(false);
        propertiesTable.getTableHeader().setReorderingAllowed(false);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean(
        "highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter
                    .addHighlighter(new AlternateRowHighlighter(
                    UIConstants.HIGHLIGHTER_COLOR,
                    UIConstants.BACKGROUND_COLOR,
                    UIConstants.TITLE_TEXT_COLOR));
            propertiesTable.setHighlighters(highlighter);
        }
        
        propertiesPane.setViewportView(propertiesTable);
    }
    
    public Map getAdditionalProperties()
    {
        Properties properties = new Properties();
        
        for(int i = 0; i < propertiesTable.getRowCount(); i++)
            if(((String)propertiesTable.getValueAt(i,VARIABLE_COLUMN)).length() > 0)
                properties.put(((String)propertiesTable.getValueAt(i,VARIABLE_COLUMN)),((String)propertiesTable.getValueAt(i,VALUE_COLUMN)));
        
        return properties;
    }
    
    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows()
    {
        propertiesTable.clearSelection();
        deleteButton.setEnabled(false);
    }
    
        /** Get the currently selected destination index */
    public int getSelectedRow()
    {
        if (propertiesTable.isEditing())
            return propertiesTable.getEditingRow();
        else
            return propertiesTable.getSelectedRow();
    }
    
    /**
     * Get the name that should be used for a new property so that it is
     * unique.
     */
    private String getNewPropertyName()
    {
        String temp = "Property ";
        
        for (int i = 1; i <= propertiesTable.getRowCount() + 1; i++)
        {
            boolean exists = false;
            for (int j = 0; j < propertiesTable.getRowCount(); j++)
            {
                if (((String) propertiesTable.getValueAt(j,VARIABLE_COLUMN)).equalsIgnoreCase(temp + i))
                {
                    exists = true;
                }
            }
            if (!exists)
                return temp + i;
        }
        return "";
    }
    
    public boolean checkProperties(Properties props)
    {
        if(((String)props.getProperty(HTTP_URL)).length() > 0)
            return true;
        else
            return false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        methodButtonGroup = new javax.swing.ButtonGroup();
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

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel7.setText("URL:");

        newButton.setText("New");
        newButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                newButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteButtonActionPerformed(evt);
            }
        });

        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, URL1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(propertiesPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(deleteButton)
                            .add(newButton)))
                    .add(httpURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(post, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(get, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(channelNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {deleteButton, newButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(newButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteButton))
                    .add(propertiesPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteButtonActionPerformed
    {//GEN-HEADEREND:event_deleteButtonActionPerformed
        if(getSelectedRow() != -1 && !propertiesTable.isEditing())
        {
            ((DefaultTableModel)propertiesTable.getModel()).removeRow(getSelectedRow());
            
            if(propertiesTable.getRowCount() != 0)
            {
                if(lastIndex == 0)
                    propertiesTable.setRowSelectionInterval(0,0);
                else if(lastIndex == propertiesTable.getRowCount())
                    propertiesTable.setRowSelectionInterval(lastIndex-1,lastIndex-1);
                else
                    propertiesTable.setRowSelectionInterval(lastIndex,lastIndex);
            }
            
            parent.enableSave();
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newButtonActionPerformed
    {//GEN-HEADEREND:event_newButtonActionPerformed
        ((DefaultTableModel)propertiesTable.getModel()).addRow(new Object[]{getNewPropertyName(),""});
        propertiesTable.setRowSelectionInterval(propertiesTable.getRowCount()-1,propertiesTable.getRowCount()-1);
        parent.enableSave();
    }//GEN-LAST:event_newButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL1;
    private com.webreach.mirth.client.ui.components.MirthComboBox channelNames;
    private javax.swing.JButton deleteButton;
    private com.webreach.mirth.client.ui.components.MirthRadioButton get;
    private com.webreach.mirth.client.ui.components.MirthTextField httpURL;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel7;
    private javax.swing.ButtonGroup methodButtonGroup;
    private javax.swing.JButton newButton;
    private com.webreach.mirth.client.ui.components.MirthRadioButton post;
    private javax.swing.JScrollPane propertiesPane;
    private com.webreach.mirth.client.ui.components.MirthTable propertiesTable;
    // End of variables declaration//GEN-END:variables

}
