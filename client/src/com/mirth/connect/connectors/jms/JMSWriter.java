/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;

import java.util.EventObject;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class JMSWriter extends ConnectorClass {

    private final int PROPERTY_COLUMN = 0;
    private final int VALUE_COLUMN = 1;
    private final String PROPERTY_COLUMN_NAME = "Property";
    private final String VALUE_COLUMN_NAME = "Value";
    private int lastIndex = -1;
    private final String TRANSACTED = "Transacted";
    private final String AUTO_ACKNOWLEDGE = "Auto Acknowledge";
    private final String CLIENT_ACKNOWLEDGE = "Client Acknowledge";
    private final String DUPLICATES_OK = "Duplicates OK";

    /** Creates new form JMSWriter */
    public JMSWriter() {
        name = JMSWriterProperties.name;
        initComponents();
        specDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"1.1", "1.0.2b"}));
        propertiesPane.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deselectRows();
            }
        });
        ackMode.setModel(new javax.swing.DefaultComboBoxModel(new String[]{TRANSACTED, AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPLICATES_OK}));
        deleteButton.setEnabled(false);
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(JMSWriterProperties.DATATYPE, name);
        properties.put(JMSWriterProperties.JMS_SPECIFICATION, (String) specDropDown.getSelectedItem());

        if (useJNDIYes.isSelected()) {
            properties.put(JMSWriterProperties.JMS_USE_JNDI, UIConstants.YES_OPTION);
            properties.put(JMSWriterProperties.JMS_URL, jmsURL.getText());
            properties.put(JMSWriterProperties.JMS_INITIAL_FACTORY, jndiInitialFactory.getText());
            properties.put(JMSWriterProperties.JMS_CONNECTION_FACTORY_JNDI, connectionFactoryJndi.getText());
        } else {
            properties.put(JMSWriterProperties.JMS_USE_JNDI, "0");
            properties.put(JMSWriterProperties.JMS_CONNECTION_FACTORY_CLASS, connectionFactoryClass.getText());
        }

        if (((String) ackMode.getSelectedItem()).equals(TRANSACTED)) {
            properties.put(JMSWriterProperties.JMS_ACK_MODE, "0");
        } else if (((String) ackMode.getSelectedItem()).equals(AUTO_ACKNOWLEDGE)) {
            properties.put(JMSWriterProperties.JMS_ACK_MODE, "1");
        } else if (((String) ackMode.getSelectedItem()).equals(CLIENT_ACKNOWLEDGE)) {
            properties.put(JMSWriterProperties.JMS_ACK_MODE, "2");
        } else if (((String) ackMode.getSelectedItem()).equals(DUPLICATES_OK)) {
            properties.put(JMSWriterProperties.JMS_ACK_MODE, "3");
        }

        properties.put(JMSWriterProperties.JMS_USERNAME, username.getText());
        properties.put(JMSWriterProperties.JMS_PASSWORD, String.valueOf(password.getPassword()));
        properties.put(JMSWriterProperties.JMS_QUEUE, queue.getText());
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(JMSWriterProperties.JMS_ADDITIONAL_PROPERTIES, serializer.toXML(getAdditionalProperties()));
        properties.put(JMSWriterProperties.JMS_TEMPLATE, templateTextArea.getText());

        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        boolean enabled = parent.isSaveEnabled();

        specDropDown.setSelectedItem(props.get(JMSWriterProperties.JMS_SPECIFICATION));

        username.setText((String) props.get(JMSWriterProperties.JMS_USERNAME));
        password.setText((String) props.get(JMSWriterProperties.JMS_PASSWORD));
        queue.setText((String) props.get(JMSWriterProperties.JMS_QUEUE));

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        if (((String) props.get(JMSWriterProperties.JMS_ADDITIONAL_PROPERTIES)).length() > 0) {
            setAdditionalProperties((Properties) serializer.fromXML((String) props.get(JMSWriterProperties.JMS_ADDITIONAL_PROPERTIES)));
        } else {
            setAdditionalProperties(new Properties());
        }

        if (((String) props.get(JMSWriterProperties.JMS_USE_JNDI)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            useJNDIYes.setSelected(true);
            useJNDIYesActionPerformed(null);
        } else {
            useJNDINo.setSelected(true);
            useJNDINoActionPerformed(null);
        }

        jmsURL.setText((String) props.get(JMSWriterProperties.JMS_URL));
        jndiInitialFactory.setText((String) props.get(JMSWriterProperties.JMS_INITIAL_FACTORY));
        connectionFactoryJndi.setText((String) props.get(JMSWriterProperties.JMS_CONNECTION_FACTORY_JNDI));
        connectionFactoryClass.setText((String) props.get(JMSWriterProperties.JMS_CONNECTION_FACTORY_CLASS));

        if (props.get(JMSWriterProperties.JMS_ACK_MODE).equals("0")) {
            ackMode.setSelectedItem(TRANSACTED);
        } else if (props.get(JMSWriterProperties.JMS_ACK_MODE).equals("1")) {
            ackMode.setSelectedItem(AUTO_ACKNOWLEDGE);
        } else if (props.get(JMSWriterProperties.JMS_ACK_MODE).equals("2")) {
            ackMode.setSelectedItem(CLIENT_ACKNOWLEDGE);
        } else if (props.get(JMSWriterProperties.JMS_ACK_MODE).equals("3")) {
            ackMode.setSelectedItem(DUPLICATES_OK);
        }

        templateTextArea.setText((String) props.get(JMSWriterProperties.JMS_TEMPLATE));

        parent.setSaveEnabled(enabled);
    }

    public Properties getDefaults() {
        return new JMSWriterProperties().getDefaults();
    }

    public void setAdditionalProperties(Properties properties) {
        Object[][] tableData = new Object[properties.size()][2];

        propertiesTable = new MirthTable();

        int j = 0;
        Iterator i = properties.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            tableData[j][PROPERTY_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }

        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{PROPERTY_COLUMN_NAME, VALUE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        propertiesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow() != -1) {
                    lastIndex = getSelectedRow();
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
                }
            }
        });

        class JMSTableCellEditor extends TextFieldCellEditor {
            boolean checkProperties;

            public JMSTableCellEditor(boolean checkProperties) {
                super();
                this.checkProperties = checkProperties;
            }

            public boolean checkUniqueProperty(String property) {
                boolean exists = false;

                for (int i = 0; i < propertiesTable.getRowCount(); i++) {
                    if (propertiesTable.getValueAt(i, PROPERTY_COLUMN) != null && ((String) propertiesTable.getValueAt(i, PROPERTY_COLUMN)).equalsIgnoreCase(property)) {
                        exists = true;
                    }
                }

                return exists;
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                boolean editable = super.isCellEditable(evt);
                
                if (editable) {
                    deleteButton.setEnabled(false);
                }

                return editable; 
            }

            @Override
            protected boolean valueChanged(String value) {
                deleteButton.setEnabled(true);
                
                if (checkProperties && (value.length() == 0 || checkUniqueProperty(value))) {
                    return false;
                }

                parent.setSaveEnabled(true);
                return true;
            }
        }

        propertiesTable.getColumnModel().getColumn(propertiesTable.getColumnModel().getColumnIndex(PROPERTY_COLUMN_NAME)).setCellEditor(new JMSTableCellEditor(true));
        propertiesTable.getColumnModel().getColumn(propertiesTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new JMSTableCellEditor(false));
        propertiesTable.setCustomEditorControls(true);

        propertiesTable.setSelectionMode(0);
        propertiesTable.setRowSelectionAllowed(true);
        propertiesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        propertiesTable.setDragEnabled(false);
        propertiesTable.setOpaque(true);
        propertiesTable.setSortable(false);
        propertiesTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            propertiesTable.setHighlighters(highlighter);
        }

        propertiesPane.setViewportView(propertiesTable);
    }

    public Map getAdditionalProperties() {
        Properties properties = new Properties();

        for (int i = 0; i < propertiesTable.getRowCount(); i++) {
            if (((String) propertiesTable.getValueAt(i, PROPERTY_COLUMN)).length() > 0) {
                properties.put(((String) propertiesTable.getValueAt(i, PROPERTY_COLUMN)), ((String) propertiesTable.getValueAt(i, VALUE_COLUMN)));
            }
        }

        return properties;
    }

    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows() {
        propertiesTable.clearSelection();
        deleteButton.setEnabled(false);
    }

    /** Get the currently selected destination index */
    public int getSelectedRow() {
        if (propertiesTable.isEditing()) {
            return propertiesTable.getEditingRow();
        } else {
            return propertiesTable.getSelectedRow();
        }
    }

    /**
     * Get the name that should be used for a new property so that it is unique.
     */
    private String getNewPropertyName() {
        String temp = "Property ";

        for (int i = 1; i <= propertiesTable.getRowCount() + 1; i++) {
            boolean exists = false;
            for (int j = 0; j < propertiesTable.getRowCount(); j++) {
                if (((String) propertiesTable.getValueAt(j, PROPERTY_COLUMN)).equalsIgnoreCase(temp + i)) {
                    exists = true;
                }
            }
            if (!exists) {
                return temp + i;
            }
        }
        return "";
    }

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.get(JMSWriterProperties.JMS_USE_JNDI)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            if (((String) props.getProperty(JMSWriterProperties.JMS_URL)).length() == 0) {
                valid = false;
                if (highlight) {
                    jmsURL.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (((String) props.getProperty(JMSWriterProperties.JMS_CONNECTION_FACTORY_JNDI)).length() == 0) {
                valid = false;
                if (highlight) {
                    connectionFactoryJndi.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (((String) props.getProperty(JMSWriterProperties.JMS_INITIAL_FACTORY)).length() == 0) {
                valid = false;
                if (highlight) {
                    jndiInitialFactory.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        } else {
            if (((String) props.getProperty(JMSWriterProperties.JMS_CONNECTION_FACTORY_CLASS)).length() == 0) {
                valid = false;
                if (highlight) {
                    connectionFactoryClass.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        if (((String) props.getProperty(JMSWriterProperties.JMS_QUEUE)).length() == 0) {
            valid = false;
            if (highlight) {
                queue.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.getProperty(JMSWriterProperties.JMS_TEMPLATE)).length() == 0) {
            valid = false;
            if (highlight) {
                templateTextArea.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    private void resetInvalidProperties() {
        jmsURL.setBackground(null);
        connectionFactoryJndi.setBackground(null);
        queue.setBackground(null);
        jndiInitialFactory.setBackground(null);
        templateTextArea.setBackground(null);
        connectionFactoryClass.setBackground(null);
    }

    public String doValidate(Properties props, boolean highlight) {
        String error = null;

        if (!checkProperties(props, highlight)) {
            error = "Error in the form for connector \"" + getName() + "\".\n\n";
        }

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

        durableButtonGroup = new javax.swing.ButtonGroup();
        useJndiButtonGroup = new javax.swing.ButtonGroup();
        jLabel3 = new javax.swing.JLabel();
        specDropDown = new com.mirth.connect.client.ui.components.MirthComboBox();
        jLabel9 = new javax.swing.JLabel();
        jmsURL = new com.mirth.connect.client.ui.components.MirthTextField();
        queue = new com.mirth.connect.client.ui.components.MirthTextField();
        jndiInitialFactory = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel11 = new javax.swing.JLabel();
        username = new com.mirth.connect.client.ui.components.MirthTextField();
        password = new com.mirth.connect.client.ui.components.MirthPasswordField();
        jLabel12 = new javax.swing.JLabel();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        propertiesPane = new javax.swing.JScrollPane();
        propertiesTable = new com.mirth.connect.client.ui.components.MirthTable();
        jLabel2 = new javax.swing.JLabel();
        templateLabel = new javax.swing.JLabel();
        templateTextArea = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        jmsUrlLabel = new javax.swing.JLabel();
        jndiInitialFactoryLabel = new javax.swing.JLabel();
        connectionFactoryJndiLabel = new javax.swing.JLabel();
        connectionFactoryClassLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        useJNDIYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useJNDINo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        connectionFactoryClass = new com.mirth.connect.client.ui.components.MirthTextField();
        connectionFactoryJndi = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel13 = new javax.swing.JLabel();
        ackMode = new com.mirth.connect.client.ui.components.MirthComboBox();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel3.setText("Specification:");

        specDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        specDropDown.setToolTipText("<html>Select 1.1 if the JMS provider supports JMS 1.1.<br>Select 1.0.2b if the JMS provider supports JMS 1.0.2b.</html>");

        jLabel9.setText("Destination:");

        jmsURL.setToolTipText("If Use JNDI Yes is selected, enter the URL of the JNDI provider here.");

        queue.setToolTipText("Enter the JMS provider's name for the queue or topic here.");
        queue.setAutoscrolls(false);

        jndiInitialFactory.setToolTipText("If Use JNDI Yes is selected, enter the full Java classname of the JNDI Initial Context Factory class.");

        jLabel11.setText("Username:");

        username.setToolTipText("Enter the login username for access to the queue or topic here.");

        password.setToolTipText("Enter the login password for access to the queue or topic here.");

        jLabel12.setText("Password:");

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
        propertiesTable.setToolTipText("Enter any necessary JMS connection factory properties and values here. These vary between JMS providers.");
        propertiesPane.setViewportView(propertiesTable);

        jLabel2.setText("Additional Properties:");

        templateLabel.setText("Template:");

        templateTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jmsUrlLabel.setText("Provider URL:");

        jndiInitialFactoryLabel.setText("Initial Context Factory:");

        connectionFactoryJndiLabel.setText("Connection Factory Name:");

        connectionFactoryClassLabel.setText("Connection Factory Class:");

        jLabel5.setText("Use JNDI:");

        useJNDIYes.setBackground(new java.awt.Color(255, 255, 255));
        useJNDIYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useJndiButtonGroup.add(useJNDIYes);
        useJNDIYes.setText("Yes");
        useJNDIYes.setToolTipText("<html>Select Yes to use JNDI to look up a connection factory to connect to the topic or queue.<br>Select No to specify a connection factory class directly.</html>");
        useJNDIYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJNDIYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJNDIYesActionPerformed(evt);
            }
        });

        useJNDINo.setBackground(new java.awt.Color(255, 255, 255));
        useJNDINo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useJndiButtonGroup.add(useJNDINo);
        useJNDINo.setSelected(true);
        useJNDINo.setText("No");
        useJNDINo.setToolTipText("<html>Select Yes to use JNDI to look up a connection factory to connect to the topic or queue.<br>Select No to specify a connection factory class directly.</html>");
        useJNDINo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJNDINo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJNDINoActionPerformed(evt);
            }
        });

        connectionFactoryClass.setToolTipText("If Use JNDI No is selected, enter the full Java classname of the JMS connection factory class here.");
        connectionFactoryClass.setAutoscrolls(false);

        connectionFactoryJndi.setToolTipText("If Use JNDI Yes is selected, enter the JNDI name for the connection factory.");

        jLabel13.setText("ACK Mode:");

        ackMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        ackMode.setToolTipText("Determine the type of acknowledgement made when a message is received.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(templateLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(connectionFactoryClassLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(connectionFactoryJndiLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jndiInitialFactoryLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jmsUrlLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(templateTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(propertiesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(deleteButton)
                            .addComponent(newButton)))
                    .addComponent(ackMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(queue, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectionFactoryClass, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectionFactoryJndi, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jmsURL, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(useJNDIYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(useJNDINo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(specDropDown, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jndiInitialFactory, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {deleteButton, newButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(specDropDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useJNDIYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useJNDINo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jmsURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jmsUrlLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jndiInitialFactory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jndiInitialFactoryLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connectionFactoryJndiLabel)
                    .addComponent(connectionFactoryJndi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connectionFactoryClassLabel)
                    .addComponent(connectionFactoryClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(queue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(ackMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton))
                    .addComponent(jLabel2)
                    .addComponent(propertiesPane, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(templateLabel)
                    .addComponent(templateTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void useJNDINoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_useJNDINoActionPerformed
    {//GEN-HEADEREND:event_useJNDINoActionPerformed
        jmsURL.setEnabled(false);
        jmsUrlLabel.setEnabled(false);
        jndiInitialFactory.setEnabled(false);
        jndiInitialFactoryLabel.setEnabled(false);
        connectionFactoryJndi.setEnabled(false);
        connectionFactoryJndiLabel.setEnabled(false);
        connectionFactoryClass.setEnabled(true);
        connectionFactoryClassLabel.setEnabled(true);
    }//GEN-LAST:event_useJNDINoActionPerformed

    private void useJNDIYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_useJNDIYesActionPerformed
    {//GEN-HEADEREND:event_useJNDIYesActionPerformed
        jmsURL.setEnabled(true);
        jmsUrlLabel.setEnabled(true);
        jndiInitialFactory.setEnabled(true);
        jndiInitialFactoryLabel.setEnabled(true);
        connectionFactoryJndi.setEnabled(true);
        connectionFactoryJndiLabel.setEnabled(true);
        connectionFactoryClass.setEnabled(false);
        connectionFactoryClassLabel.setEnabled(false);
    }//GEN-LAST:event_useJNDIYesActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_deleteButtonActionPerformed
    {// GEN-HEADEREND:event_deleteButtonActionPerformed
        if (getSelectedRow() != -1 && !propertiesTable.isEditing()) {
            ((DefaultTableModel) propertiesTable.getModel()).removeRow(getSelectedRow());

            if (propertiesTable.getRowCount() != 0) {
                if (lastIndex == 0) {
                    propertiesTable.setRowSelectionInterval(0, 0);
                } else if (lastIndex == propertiesTable.getRowCount()) {
                    propertiesTable.setRowSelectionInterval(lastIndex - 1, lastIndex - 1);
                } else {
                    propertiesTable.setRowSelectionInterval(lastIndex, lastIndex);
                }
            }

            parent.setSaveEnabled(true);
        }
    }// GEN-LAST:event_deleteButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_newButtonActionPerformed
    {// GEN-HEADEREND:event_newButtonActionPerformed
        ((DefaultTableModel) propertiesTable.getModel()).addRow(new Object[]{getNewPropertyName(), ""});
        propertiesTable.setRowSelectionInterval(propertiesTable.getRowCount() - 1, propertiesTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }// GEN-LAST:event_newButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthComboBox ackMode;
    private com.mirth.connect.client.ui.components.MirthTextField connectionFactoryClass;
    private javax.swing.JLabel connectionFactoryClassLabel;
    private com.mirth.connect.client.ui.components.MirthTextField connectionFactoryJndi;
    private javax.swing.JLabel connectionFactoryJndiLabel;
    private javax.swing.JButton deleteButton;
    private javax.swing.ButtonGroup durableButtonGroup;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
    private com.mirth.connect.client.ui.components.MirthTextField jmsURL;
    private javax.swing.JLabel jmsUrlLabel;
    private com.mirth.connect.client.ui.components.MirthTextField jndiInitialFactory;
    private javax.swing.JLabel jndiInitialFactoryLabel;
    private javax.swing.JButton newButton;
    private com.mirth.connect.client.ui.components.MirthPasswordField password;
    private javax.swing.JScrollPane propertiesPane;
    private com.mirth.connect.client.ui.components.MirthTable propertiesTable;
    private com.mirth.connect.client.ui.components.MirthTextField queue;
    private com.mirth.connect.client.ui.components.MirthComboBox specDropDown;
    private javax.swing.JLabel templateLabel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea templateTextArea;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJNDINo;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJNDIYes;
    private javax.swing.ButtonGroup useJndiButtonGroup;
    private com.mirth.connect.client.ui.components.MirthTextField username;
    // End of variables declaration//GEN-END:variables
}
