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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
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
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class JMSWriter extends ConnectorClass
{
    private final int PROPERTY_COLUMN = 0;

    private final int VALUE_COLUMN = 1;

    private final String PROPERTY_COLUMN_NAME = "Property";

    private final String VALUE_COLUMN_NAME = "Value";

    private int lastIndex = -1;

    /** Creates new form JMSWriter */
    private final String DATATYPE = "DataType";

    private final String JMS_SPECIFICATION = "specification";

    private final String JMS_DURABLE = "durable";

    private final String JMS_CLIENT_ID = "clientId";

    private final String JMS_USERNAME = "username";

    private final String JMS_PASSWORD = "password";

    private final String JMS_QUEUE = "host";

    private final String JMS_URL = "jndiProviderUrl";

    private final String JMS_INITIAL_FACTORY = "jndiInitialFactory";

    private final String JMS_CONNECTION_FACTORY = "connectionFactoryJndiName";

    private final String JMS_ADDITIONAL_PROPERTIES = "connectionFactoryProperties";
    
    private final String JMS_TEMPLATE = "template";

    public JMSWriter()
    {
        name = "JMS Writer";
        initComponents();
        specDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1.1", "1.0.2b" }));
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
        properties.put(JMS_SPECIFICATION, (String) specDropDown.getSelectedItem());

        if (durableNo.isSelected())
            properties.put(JMS_DURABLE, UIConstants.NO_OPTION);
        else
            properties.put(JMS_DURABLE, UIConstants.YES_OPTION);

        properties.put(JMS_CLIENT_ID, cliendId.getText());
        properties.put(JMS_USERNAME, username.getText());
        properties.put(JMS_PASSWORD, String.valueOf(password.getPassword()));
        properties.put(JMS_QUEUE, queue.getText());
        properties.put(JMS_URL, jmsURL.getText());
        properties.put(JMS_INITIAL_FACTORY, jndiInitialFactory.getText());
        properties.put(JMS_CONNECTION_FACTORY, connectionFactory.getText());
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(JMS_ADDITIONAL_PROPERTIES, serializer.toXML(getAdditionalProperties()));
        properties.put(JMS_TEMPLATE, templateTextArea.getText());

        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        specDropDown.setSelectedItem(props.get(JMS_SPECIFICATION));

        if (((String) props.get(JMS_DURABLE)).equalsIgnoreCase(UIConstants.NO_OPTION))
        {
            durableNo.setSelected(true);
            durableNoActionPerformed(null);
        }
        else
        {
            durableYes.setSelected(true);
            durableYesActionPerformed(null);
        }

        cliendId.setText((String) props.get(JMS_CLIENT_ID));
        username.setText((String) props.get(JMS_USERNAME));
        password.setText((String) props.get(JMS_PASSWORD));
        jmsURL.setText((String) props.get(JMS_URL));
        queue.setText((String) props.get(JMS_QUEUE));
        jndiInitialFactory.setText((String) props.get(JMS_INITIAL_FACTORY));
        connectionFactory.setText((String) props.get(JMS_CONNECTION_FACTORY));

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        if (((String) props.get(JMS_ADDITIONAL_PROPERTIES)).length() > 0)
            setAdditionalProperties((Properties) serializer.fromXML((String) props.get(JMS_ADDITIONAL_PROPERTIES)));
        else
            setAdditionalProperties(new Properties());
        
        templateTextArea.setText((String) props.get(JMS_TEMPLATE));

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(JMS_SPECIFICATION, (String) specDropDown.getItemAt(0));
        properties.put(JMS_DURABLE, UIConstants.NO_OPTION);
        properties.put(JMS_CLIENT_ID, "");
        properties.put(JMS_USERNAME, "");
        properties.put(JMS_PASSWORD, "");
        properties.put(JMS_URL, "");
        properties.put(JMS_QUEUE, "");
        properties.put(JMS_INITIAL_FACTORY, "");
        properties.put(JMS_CONNECTION_FACTORY, "");
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(JMS_ADDITIONAL_PROPERTIES, serializer.toXML(new Properties()));
        properties.put(JMS_TEMPLATE, "${message.encodedData}");
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
            tableData[j][PROPERTY_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }

        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] { PROPERTY_COLUMN_NAME, VALUE_COLUMN_NAME })
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
                if (getSelectedRow() != -1)
                {
                    lastIndex = getSelectedRow();
                    deleteButton.setEnabled(true);
                }
                else
                    deleteButton.setEnabled(false);
            }
        });

        class JMSTableCellEditor extends AbstractCellEditor implements TableCellEditor
        {
            JComponent component = new JTextField();

            Object originalValue;

            boolean checkProperties;

            public JMSTableCellEditor(boolean checkProperties)
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
                    if (propertiesTable.getValueAt(i, PROPERTY_COLUMN) != null && ((String) propertiesTable.getValueAt(i, PROPERTY_COLUMN)).equalsIgnoreCase(property))
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
        propertiesTable.getColumnModel().getColumn(propertiesTable.getColumnModel().getColumnIndex(PROPERTY_COLUMN_NAME)).setCellEditor(new JMSTableCellEditor(true));

        // Set the custom cell editor for the Destination Name column.
        propertiesTable.getColumnModel().getColumn(propertiesTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new JMSTableCellEditor(false));

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

    public Map getAdditionalProperties()
    {
        Properties properties = new Properties();

        for (int i = 0; i < propertiesTable.getRowCount(); i++)
            if (((String) propertiesTable.getValueAt(i, PROPERTY_COLUMN)).length() > 0)
                properties.put(((String) propertiesTable.getValueAt(i, PROPERTY_COLUMN)), ((String) propertiesTable.getValueAt(i, VALUE_COLUMN)));

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
     * Get the name that should be used for a new property so that it is unique.
     */
    private String getNewPropertyName()
    {
        String temp = "Property ";

        for (int i = 1; i <= propertiesTable.getRowCount() + 1; i++)
        {
            boolean exists = false;
            for (int j = 0; j < propertiesTable.getRowCount(); j++)
            {
                if (((String) propertiesTable.getValueAt(j, PROPERTY_COLUMN)).equalsIgnoreCase(temp + i))
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
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.getProperty(JMS_URL)).length() == 0)
        {
            valid = false;
            jmsURL.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.getProperty(JMS_CONNECTION_FACTORY)).length() == 0)
        {
            valid = false;
            connectionFactory.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.getProperty(JMS_QUEUE)).length() == 0)
        {
            valid = false;
            queue.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.getProperty(JMS_INITIAL_FACTORY)).length() == 0)
        {
            valid = false;
            jndiInitialFactory.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.getProperty(JMS_TEMPLATE)).length() == 0)
        {
            valid = false;
            templateTextArea.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.getProperty(JMS_DURABLE)).equals(UIConstants.YES_OPTION))
        {
            if (((String) props.getProperty(JMS_CLIENT_ID)).length() == 0)
            {
                valid = false;
                cliendId.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        jmsURL.setBackground(null);
        connectionFactory.setBackground(null);
        queue.setBackground(null);
        jndiInitialFactory.setBackground(null);
        templateTextArea.setBackground(null);
        cliendId.setBackground(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        deliveryButtonGroup = new javax.swing.ButtonGroup();
        durableButtonGroup = new javax.swing.ButtonGroup();
        recoverButtonGroup = new javax.swing.ButtonGroup();
        jLabel3 = new javax.swing.JLabel();
        specDropDown = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel4 = new javax.swing.JLabel();
        clientIdLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        durableNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        durableYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        cliendId = new com.webreach.mirth.client.ui.components.MirthTextField();
        jmsURL = new com.webreach.mirth.client.ui.components.MirthTextField();
        connectionFactory = new com.webreach.mirth.client.ui.components.MirthTextField();
        queue = new com.webreach.mirth.client.ui.components.MirthTextField();
        jndiInitialFactory = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        username = new com.webreach.mirth.client.ui.components.MirthTextField();
        password = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        jLabel12 = new javax.swing.JLabel();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        propertiesPane = new javax.swing.JScrollPane();
        propertiesTable = new com.webreach.mirth.client.ui.components.MirthTable();
        jLabel2 = new javax.swing.JLabel();
        templateLabel = new javax.swing.JLabel();
        templateTextArea = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel3.setText("Specification:");

        specDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setText("Durable:");

        clientIdLabel.setText("Client ID:");

        jLabel7.setText("JNDI Provider URL:");

        jLabel8.setText("Connection Factory JNDI Name:");

        jLabel9.setText("Destination:");

        durableNo.setBackground(new java.awt.Color(255, 255, 255));
        durableNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        durableButtonGroup.add(durableNo);
        durableNo.setSelected(true);
        durableNo.setText("No");
        durableNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        durableNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                durableNoActionPerformed(evt);
            }
        });

        durableYes.setBackground(new java.awt.Color(255, 255, 255));
        durableYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        durableButtonGroup.add(durableYes);
        durableYes.setText("Yes");
        durableYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        durableYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                durableYesActionPerformed(evt);
            }
        });

        queue.setAutoscrolls(false);

        jLabel10.setText("JNDI Initial Context Factory:");

        jLabel11.setText("Username:");

        password.setFont(new java.awt.Font("Tahoma", 0, 11));

        jLabel12.setText("Password:");

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

        jLabel2.setText("Additional Properties:");

        templateLabel.setText("Template:");

        templateTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(clientIdLabel)
                    .add(jLabel4)
                    .add(jLabel12)
                    .add(jLabel11)
                    .add(jLabel9)
                    .add(jLabel8)
                    .add(jLabel10)
                    .add(jLabel7)
                    .add(jLabel3)
                    .add(templateLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cliendId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(username, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(queue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(durableYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(durableNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(connectionFactory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(specDropDown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jmsURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jndiInitialFactory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(propertiesPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(newButton)
                            .add(deleteButton)))
                    .add(templateTextArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {deleteButton, newButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(specDropDown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jmsURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(jndiInitialFactory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(connectionFactory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(queue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(username, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(durableYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(durableNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(clientIdLabel)
                    .add(cliendId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(newButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteButton))
                    .add(jLabel2)
                    .add(propertiesPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 119, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(templateLabel)
                    .add(templateTextArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_deleteButtonActionPerformed
    {// GEN-HEADEREND:event_deleteButtonActionPerformed
        if (getSelectedRow() != -1 && !propertiesTable.isEditing())
        {
            ((DefaultTableModel) propertiesTable.getModel()).removeRow(getSelectedRow());

            if (propertiesTable.getRowCount() != 0)
            {
                if (lastIndex == 0)
                    propertiesTable.setRowSelectionInterval(0, 0);
                else if (lastIndex == propertiesTable.getRowCount())
                    propertiesTable.setRowSelectionInterval(lastIndex - 1, lastIndex - 1);
                else
                    propertiesTable.setRowSelectionInterval(lastIndex, lastIndex);
            }

            parent.enableSave();
        }
    }// GEN-LAST:event_deleteButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_newButtonActionPerformed
    {// GEN-HEADEREND:event_newButtonActionPerformed
        ((DefaultTableModel) propertiesTable.getModel()).addRow(new Object[] { getNewPropertyName(), "" });
        propertiesTable.setRowSelectionInterval(propertiesTable.getRowCount() - 1, propertiesTable.getRowCount() - 1);
        parent.enableSave();
    }// GEN-LAST:event_newButtonActionPerformed

    private void durableYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_durableYesActionPerformed
    {// GEN-HEADEREND:event_durableYesActionPerformed
        cliendId.setEnabled(true);
        clientIdLabel.setEnabled(true);
    }// GEN-LAST:event_durableYesActionPerformed

    private void durableNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_durableNoActionPerformed
    {// GEN-HEADEREND:event_durableNoActionPerformed
        cliendId.setEnabled(false);
        clientIdLabel.setEnabled(false);
        cliendId.setText("");
    }// GEN-LAST:event_durableNoActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthTextField cliendId;
    private javax.swing.JLabel clientIdLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField connectionFactory;
    private javax.swing.JButton deleteButton;
    private javax.swing.ButtonGroup deliveryButtonGroup;
    private javax.swing.ButtonGroup durableButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton durableNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton durableYes;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private com.webreach.mirth.client.ui.components.MirthTextField jmsURL;
    private com.webreach.mirth.client.ui.components.MirthTextField jndiInitialFactory;
    private javax.swing.JButton newButton;
    private com.webreach.mirth.client.ui.components.MirthPasswordField password;
    private javax.swing.JScrollPane propertiesPane;
    private com.webreach.mirth.client.ui.components.MirthTable propertiesTable;
    private com.webreach.mirth.client.ui.components.MirthTextField queue;
    private javax.swing.ButtonGroup recoverButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthComboBox specDropDown;
    private javax.swing.JLabel templateLabel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea templateTextArea;
    private com.webreach.mirth.client.ui.components.MirthTextField username;
    // End of variables declaration//GEN-END:variables

}
