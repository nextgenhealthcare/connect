/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ListenerSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class WebServiceListener extends ConnectorSettingsPanel {

    private final int USERNAME_COLUMN_NUMBER = 0;
    private final int PASSWORD_COLUMN_NUMBER = 1;
    private final String USERNAME_COLUMN_NAME = "Username";
    private final String PASSWORD_COLUMN_NAME = "Password";
    private Frame parent;
    private boolean usingHttps = false;

    public WebServiceListener() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initLayout();
        wsdlField.setEditable(false);
        methodField.setEditable(false);
    }

    @Override
    public String getConnectorName() {
        return new WebServiceReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        WebServiceReceiverProperties properties = new WebServiceReceiverProperties();
        properties.setClassName(classNameField.getText());
        properties.setServiceName(serviceNameField.getText());
        ArrayList<ArrayList<String>> credentials = getCredentials();
        properties.setUsernames(credentials.get(0));
        properties.setPasswords(credentials.get(1));
        properties.setSoapBinding(Binding.fromDisplayName((String) versionComboBox.getSelectedItem()));

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        WebServiceReceiverProperties props = (WebServiceReceiverProperties) properties;

        versionComboBox.setSelectedIndex(0);
        versionComboBox.setSelectedItem(props.getSoapBinding());

        classNameField.setText(props.getClassName());
        updateClassNameRadio();

        serviceNameField.setText(props.getServiceName());

        updateWSDL();

        List<List<String>> credentials = new ArrayList<List<String>>();

        credentials.add(props.getUsernames());
        credentials.add(props.getPasswords());
        setCredentials(credentials);
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new WebServiceReceiverProperties();
    }

    private void updateClassNameRadio() {
        if (classNameField.getText().equals(new WebServiceReceiverProperties().getClassName())) {
            classNameDefaultRadio.setSelected(true);
            classNameDefaultRadioActionPerformed();
        } else {
            classNameCustomRadio.setSelected(true);
            classNameCustomRadioActionPerformed();
        }
    }

    public void updateWSDL() {
        String server = "<server ip>";
        try {
            server = new URI(PlatformUI.SERVER_URL).getHost();
        } catch (Exception e) {
            // ignore exceptions getting the server ip
        }

        wsdlField.setText("http" + (usingHttps ? "s" : "") + "://" + server + ":" + ((WebServiceReceiverProperties) getFilledProperties()).getListenerConnectorProperties().getPort() + "/services/" + serviceNameField.getText() + "?wsdl");
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        WebServiceReceiverProperties props = (WebServiceReceiverProperties) properties;

        boolean valid = true;

        if (props.getClassName().length() == 0) {
            valid = false;
            if (highlight) {
                classNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getServiceName().length() == 0) {
            valid = false;
            if (highlight) {
                serviceNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        classNameField.setBackground(null);
        serviceNameField.setBackground(null);
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        usingHttps = connectorTypeDecoration != null;
        updateWSDL();
    }

    private void setCredentials(List<List<String>> credentials) {

        List<String> usernames = credentials.get(0);
        List<String> passwords = credentials.get(1);

        Object[][] tableData = new Object[usernames.size()][2];

        credentialsTable = new MirthTable();

        for (int i = 0; i < usernames.size(); i++) {
            tableData[i][USERNAME_COLUMN_NUMBER] = usernames.get(i);
            tableData[i][PASSWORD_COLUMN_NUMBER] = passwords.get(i);
        }

        credentialsTable.setModel(new DefaultTableModel(tableData, new String[] {
                USERNAME_COLUMN_NAME, PASSWORD_COLUMN_NAME }) {

            boolean[] canEdit = new boolean[] { true, true };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        credentialsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (credentialsTable.getSelectedModelIndex() != -1) {
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
                }
            }
        });

        class AttachmentsTableCellEditor extends TextFieldCellEditor {

            boolean checkUnique;

            public AttachmentsTableCellEditor(boolean checkUnique) {
                super();
                this.checkUnique = checkUnique;
            }

            public boolean checkUnique(String value) {
                boolean exists = false;

                for (int i = 0; i < credentialsTable.getModel().getRowCount(); i++) {
                    if (((String) credentialsTable.getModel().getValueAt(i, USERNAME_COLUMN_NUMBER)).equalsIgnoreCase(value)) {
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

                if (checkUnique && (value.length() == 0 || checkUnique(value))) {
                    return false;
                }

                parent.setSaveEnabled(true);
                return true;
            }
        }

        credentialsTable.getColumnModel().getColumn(credentialsTable.getColumnModelIndex(USERNAME_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(true));
        credentialsTable.getColumnModel().getColumn(credentialsTable.getColumnModelIndex(PASSWORD_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(false));
        credentialsTable.setCustomEditorControls(true);

        credentialsTable.setSelectionMode(0);
        credentialsTable.setRowSelectionAllowed(true);
        credentialsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        credentialsTable.setDragEnabled(false);
        credentialsTable.setOpaque(true);
        credentialsTable.setSortable(true);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            credentialsTable.setHighlighters(highlighter);
        }

        credentialsPane.setViewportView(credentialsTable);
        deleteButton.setEnabled(false);
    }

    private ArrayList<ArrayList<String>> getCredentials() {
        ArrayList<ArrayList<String>> credentials = new ArrayList<ArrayList<String>>();

        ArrayList<String> usernames = new ArrayList<String>();
        ArrayList<String> passwords = new ArrayList<String>();

        for (int i = 0; i < credentialsTable.getModel().getRowCount(); i++) {
            if (((String) credentialsTable.getModel().getValueAt(i, USERNAME_COLUMN_NUMBER)).length() > 0) {
                usernames.add((String) credentialsTable.getModel().getValueAt(i, USERNAME_COLUMN_NUMBER));
                passwords.add((String) credentialsTable.getModel().getValueAt(i, PASSWORD_COLUMN_NUMBER));
            }
        }

        credentials.add(usernames);
        credentials.add(passwords);

        return credentials;
    }

    private void stopCellEditing() {
        if (credentialsTable.isEditing()) {
            credentialsTable.getColumnModel().getColumn(credentialsTable.convertColumnIndexToModel(credentialsTable.getEditingColumn())).getCellEditor().stopCellEditing();
        }
    }

    /**
     * Get the name that should be used for a new user so that it is unique.
     */
    private String getNewUsername(int size) {
        String temp = "user";

        for (int i = 1; i <= size; i++) {
            boolean exists = false;

            for (int j = 0; j < size - 1; j++) {
                if (((String) credentialsTable.getModel().getValueAt(j, credentialsTable.getColumnModelIndex(USERNAME_COLUMN_NAME))).equalsIgnoreCase(temp + i)) {
                    exists = true;
                }
            }

            if (!exists) {
                return temp + i;
            }
        }
        return "";
    }

    @Override
    public void updatedField(String field) {
        if (ListenerSettingsPanel.FIELD_PORT.equals(field)) {
            updateWSDL();
        }
    }

    private void initComponents() {
        versionLabel = new JLabel("Binding:");
        serviceNameLabel = new JLabel("Service Name:");
        methodLabel = new JLabel("Method:");
        webServiceLabel = new JLabel("Web Service:");
        credentialsLabel = new JLabel("Basic Authentication:");
        classNameLabel = new JLabel("Service Class Name:");

        serviceNameField = new MirthTextField();
        serviceNameField.setToolTipText("The name to give to the web service.");
        serviceNameField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                serviceNameFieldKeyReleased(evt);
            }
        });

        methodField = new JTextField();
        methodField.setText("String acceptMessage(String message)");
        methodField.setToolTipText("Displays the generated web service operation signature the client will call.");

        versionComboBox = new MirthComboBox();
        versionComboBox.addItem(Binding.DEFAULT.getName());
        versionComboBox.addItem(Binding.SOAP11HTTP.getName());
        versionComboBox.addItem(Binding.SOAP12HTTP.getName());
        versionComboBox.setSelectedIndex(0);
        versionComboBox.setToolTipText("<html>The selected binding version defines the structure of the generated envelope.<br/>Selecting default will publish this endpoint with the specified binding annotation.<br/>If no annotation is provided, a SOAP 1.1 binding will be used.</html>");

        wsdlLabel = new JLabel("WSDL URL:");

        wsdlField = new JTextField();
        wsdlField.setToolTipText("<html>Displays the generated WSDL URL for the web service.<br>The client that sends messages to the service can download this file to determine how to call the web service.</html>");

        classNameDefaultRadio = new MirthRadioButton("Default service");
        classNameDefaultRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        classNameDefaultRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classNameDefaultRadio.setToolTipText("<html>If checked, the connector will use the DefaultAcceptMessage web service.</html>");
        classNameDefaultRadio.setMargin(new Insets(0, 0, 0, 0));
        classNameDefaultRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                classNameDefaultRadioActionPerformed();
            }
        });

        classNameCustomRadio = new MirthRadioButton("Custom service");
        classNameCustomRadio.setSelected(true);
        classNameCustomRadio.setBackground(new Color(255, 255, 255));
        classNameCustomRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classNameCustomRadio.setToolTipText("<html>If checked, the connector will use a custom web service defined below.</html>");
        classNameCustomRadio.setMargin(new Insets(0, 0, 0, 0));
        classNameCustomRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                classNameCustomRadioActionPerformed();
            }
        });

        classNameButtonGroup = new ButtonGroup();
        classNameButtonGroup.add(classNameDefaultRadio);
        classNameButtonGroup.add(classNameCustomRadio);

        classNameField = new MirthTextField();
        classNameField.setToolTipText("<html>The fully qualified class name of the web service that should be hosted.<br>If this is a custom class, it should be added in a custom jar so it is loaded with Mirth Connect.</html>");

        credentialsTable = new MirthTable();
        credentialsTable.setModel(new DefaultTableModel(new Object[][] {

        }, new String[] { "Username", "Password" }) {
            Class[] types = new Class[] { java.lang.String.class, java.lang.String.class };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        });

        credentialsPane = new JScrollPane();
        credentialsPane.setViewportView(credentialsTable);

        newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newButtonActionPerformed();
            }
        });

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deleteButtonActionPerformed();
            }
        });
    }

    private void initLayout() {
        setLayout(new MigLayout("novisualpadding, hidemode 3, insets 0", "[right]12[left]"));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        add(webServiceLabel);
        add(classNameDefaultRadio, "split");
        add(classNameCustomRadio, "wrap");

        add(classNameLabel);
        add(classNameField, "w 300!, wrap");

        add(serviceNameLabel);
        add(serviceNameField, "w 100!, wrap");

        add(versionLabel);
        add(versionComboBox, "wrap");

        add(wsdlLabel);
        add(wsdlField, "w 250!, wrap");

        add(methodLabel);
        add(methodField, "w 250!, wrap");

        JPanel buttonPanel = new JPanel(new MigLayout("novisualpadding, hidemode 3, insets 0"));
        buttonPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        buttonPanel.add(newButton, "w 50!, wrap");
        buttonPanel.add(deleteButton, "w 50!, wrap");

        add(credentialsLabel, "aligny top, gaptop 2");
        add(credentialsPane, "sx, grow, push, split");
        add(buttonPanel, "aligny top, gaptop 0");
    }

    private void serviceNameFieldKeyReleased(KeyEvent evt) {
        updateWSDL();
    }

    private void classNameDefaultRadioActionPerformed() {
        classNameField.setText(new WebServiceReceiverProperties().getClassName());
        methodField.setText("String acceptMessage(String message)");
        classNameLabel.setEnabled(false);
        classNameField.setEnabled(false);
    }

    private void classNameCustomRadioActionPerformed() {
        methodField.setText("<Custom Web Service Methods>");
        classNameLabel.setEnabled(true);
        classNameField.setEnabled(true);
    }

    private void newButtonActionPerformed() {
        stopCellEditing();
        ((DefaultTableModel) credentialsTable.getModel()).addRow(new Object[] {
                getNewUsername(credentialsTable.getModel().getRowCount() + 1), "" });
        int newViewIndex = credentialsTable.convertRowIndexToView(credentialsTable.getModel().getRowCount() - 1);
        credentialsTable.setRowSelectionInterval(newViewIndex, newViewIndex);

        credentialsPane.getViewport().setViewPosition(new Point(0, credentialsTable.getRowHeight() * credentialsTable.getModel().getRowCount()));
        parent.setSaveEnabled(true);
    }

    private void deleteButtonActionPerformed() {
        stopCellEditing();

        int selectedModelIndex = credentialsTable.getSelectedModelIndex();
        int newViewIndex = credentialsTable.convertRowIndexToView(selectedModelIndex);
        if (newViewIndex == (credentialsTable.getModel().getRowCount() - 1)) {
            newViewIndex--;
        }

        ((DefaultTableModel) credentialsTable.getModel()).removeRow(selectedModelIndex);

        parent.setSaveEnabled(true);

        if (credentialsTable.getModel().getRowCount() == 0) {
            credentialsTable.clearSelection();
            deleteButton.setEnabled(false);
        } else {
            credentialsTable.setRowSelectionInterval(newViewIndex, newViewIndex);
        }

        parent.setSaveEnabled(true);
    }

    private JLabel versionLabel;
    private MirthComboBox versionComboBox;

    private JLabel webServiceLabel;
    private MirthRadioButton classNameDefaultRadio;
    private MirthRadioButton classNameCustomRadio;

    private JLabel classNameLabel;
    private MirthTextField classNameField;

    private JLabel serviceNameLabel;
    private MirthTextField serviceNameField;

    private JLabel wsdlLabel;
    private JTextField wsdlField;

    private ButtonGroup classNameButtonGroup;
    private JLabel credentialsLabel;

    private JLabel methodLabel;
    private JTextField methodField;

    private MirthTable credentialsTable;
    private JScrollPane credentialsPane;
    private JButton deleteButton;
    private JButton newButton;
}
