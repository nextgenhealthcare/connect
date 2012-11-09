/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;
import org.syntax.jedit.tokenmarker.TSQLTokenMarker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.util.SQLParserUtil;
import com.mirth.connect.connectors.jdbc.DatabaseMetadataDialog.STATEMENT_TYPE;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.converters.DataTypeFactory;
import com.mirth.connect.model.converters.DocumentSerializer;

public class DatabaseReader extends ConnectorSettingsPanel {

    private static SyntaxDocument sqlMappingDoc;
    private static SyntaxDocument sqlUpdateMappingDoc;
    private static SyntaxDocument jsMappingDoc;
    private static SyntaxDocument jsUpdateMappingDoc;
    private List<DriverInfo> drivers;
    private Timer timer;

    private Frame parent;

    public DatabaseReader() {
        this.parent = PlatformUI.MIRTH_FRAME;
        
        try {
            drivers = this.parent.mirthClient.getDatabaseDrivers();
        } catch (ClientException e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }

        initComponents();

        drivers.add(0, new DriverInfo(DatabaseReceiverProperties.DRIVER_DEFAULT, DatabaseReceiverProperties.DRIVER_DEFAULT, "", ""));
        String[] driverNames = new String[drivers.size()];

        for (int i = 0; i < drivers.size(); i++) {
            driverNames[i] = drivers.get(i).getName();
        }

        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(driverNames));

        sqlMappingDoc = new SyntaxDocument();
        sqlMappingDoc.setTokenMarker(new TSQLTokenMarker());
        sqlUpdateMappingDoc = new SyntaxDocument();
        sqlUpdateMappingDoc.setTokenMarker(new TSQLTokenMarker());
        jsMappingDoc = new SyntaxDocument();
        jsMappingDoc.setTokenMarker(new JavaScriptTokenMarker());
        jsUpdateMappingDoc = new SyntaxDocument();
        jsUpdateMappingDoc.setTokenMarker(new JavaScriptTokenMarker());

        databaseSQLTextPane.setDocument(sqlMappingDoc);
        databaseUpdateSQLTextPane.setDocument(sqlUpdateMappingDoc);

        sqlMappingDoc.addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void insertUpdate(DocumentEvent e) {
                update();
            }
        });

        jsMappingDoc.addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void insertUpdate(DocumentEvent e) {
                update();
            }
        });
    }
    
    @Override
    public String getConnectorName() {
        return new DatabaseReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        DatabaseReceiverProperties properties = new DatabaseReceiverProperties();
        
        for (int i = 0; i < drivers.size(); i++) {
            DriverInfo driver = drivers.get(i);
            if (driver.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem()))) {
                properties.setDriver(driver.getClassName());
            }
        }

        properties.setUrl(databaseURLField.getText());
        properties.setUsername(databaseUsernameField.getText());
        properties.setPassword(new String(databasePasswordField.getPassword()));

        properties.setCacheResults(cacheResultsYesButton.isSelected());

        properties.setUseScript(useJavaScriptYes.isSelected());
        properties.setQuery(databaseSQLTextPane.getText());
        properties.setAck(databaseUpdateSQLTextPane.getText());

        properties.setUseAck(readOnUpdateYes.isSelected());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        DatabaseReceiverProperties props = (DatabaseReceiverProperties) properties;

        boolean enabled = parent.isSaveEnabled();

        for (int i = 0; i < drivers.size(); i++) {
            DriverInfo driver = drivers.get(i);
            if (driver.getClassName().equalsIgnoreCase(props.getDriver())) {
                databaseDriverCombobox.setSelectedItem(driver.getName());
            }
        }

        parent.setSaveEnabled(enabled);
        
        databaseURLField.setText(props.getUrl());
        databaseUsernameField.setText(props.getUsername());
        databasePasswordField.setText(props.getPassword());

        if (props.isCacheResults()) {
            cacheResultsYesButton.setSelected(true);
        } else {
            cacheResultsNoButton.setSelected(true);
        }

        if (props.isUseScript()) {
            useJavaScriptYes.setSelected(true);
            useJavaScriptYesActionPerformed(null);
        } else {
            useJavaScriptNo.setSelected(true);
            useJavaScriptNoActionPerformed(null);
        }
        
        databaseSQLTextPane.setText(props.getQuery());
        databaseUpdateSQLTextPane.setText(props.getAck());

        if (props.isUseAck()) {
            readOnUpdateYes.setSelected(true);
            readOnUpdateYesActionPerformed(null);
        } else {
            readOnUpdateNo.setSelected(true);
            readOnUpdateNoActionPerformed(null);
        }
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new DatabaseReceiverProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        DatabaseReceiverProperties props = (DatabaseReceiverProperties) properties;
        
        boolean valid = true;

        if (props.getUrl().length() == 0) {
            valid = false;
            if (highlight) {
                databaseURLField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getQuery().length() == 0) {
            valid = false;
            if (highlight) {
                databaseSQLTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.isUseAck() && (props.getAck().length() == 0)) {
            valid = false;
            if (highlight) {
                databaseUpdateSQLTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        if (props.getDriver().equals(DatabaseReceiverProperties.DRIVER_DEFAULT)) {
            valid = false;
            if (highlight) {
                databaseDriverCombobox.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        databaseURLField.setBackground(null);
        databaseSQLTextPane.setBackground(null);
        databaseUpdateSQLTextPane.setBackground(null);
        databaseDriverCombobox.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
    }

    @Override
    public String doValidate(ConnectorProperties properties, boolean highlight) {
        DatabaseReceiverProperties props = (DatabaseReceiverProperties) properties;
        
        String error = null;

        if (props.isUseScript()) {
            String script = props.getQuery();
            if (script.length() != 0) {
                Context context = Context.enter();
                try {
                    context.compileString("function rhinoWrapper() {" + script + "\n}", UUID.randomUUID().toString(), 1, null);
                } catch (EvaluatorException e) {
                    if (error == null) {
                        error = "";
                    }
                    error += "Error in connector \"" + getConnectorName() + "\" at Javascript:\nError on line " + e.lineNumber() + ": " + e.getMessage() + ".\n\n";
                } catch (Exception e) {
                    if (error == null) {
                        error = "";
                    }
                    error += "Error in connector \"" + getConnectorName() + "\" at Javascript:\nUnknown error occurred during validation.";
                }

                Context.exit();
            }
            
            if (props.isUseAck()) {
                String onUpdateScript = props.getAck();
                
                if (onUpdateScript.length() != 0) {
                    Context context = Context.enter();
                    try {
                        context.compileString("function rhinoWrapper() {" + onUpdateScript + "\n}", UUID.randomUUID().toString(), 1, null);
                    } catch (EvaluatorException e) {
                        if (error == null) {
                            error = "";
                        }
                        error += "Error in connector \"" + getConnectorName() + "\" at On-Update Javascript:\nError on line " + e.lineNumber() + ": " + e.getMessage() + ".\n\n";
                    } catch (Exception e) {
                        if (error == null) {
                            error = "";
                        }
                        error += "Error in connector \"" + getConnectorName() + "\" at Javascript:\nUnknown error occurred during validation.";
                    }

                    Context.exit();
                }
                
            }
        }
        
        return error;
    }
    
    @Override
    public boolean requiresXmlDataType() {
        return true;
    }

    private void update() {
        class UpdateTimer extends TimerTask {

            public void run() {

                final String workingId = PlatformUI.MIRTH_FRAME.startWorking("Parsing...");
                updateSQL();
                PlatformUI.MIRTH_FRAME.stopWorking(workingId);
            }
        }
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new UpdateTimer(), 1000);
        } else {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new UpdateTimer(), 1000);
        }
    }

    private void updateSQL() {
        Object sqlStatement = databaseSQLTextPane.getText();
        String[] data;

        if ((sqlStatement != null) && (!sqlStatement.equals(""))) {
            SQLParserUtil spu = new SQLParserUtil((String) sqlStatement);
            data = spu.Parse();

        } else {
            data = new String[]{};
        }
        dbVarList.setListData(data);
        updateIncomingData(data);
    }

    private void updateIncomingData(String[] data) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element resultElement = document.createElement("result");
            for (int i = 0; i < data.length; i++) {
                Element columnElement = document.createElement(data[i]);
                columnElement.setTextContent("value");
                resultElement.appendChild(columnElement);
            }
            document.appendChild(resultElement);

            DocumentSerializer docSerializer = new DocumentSerializer();

            String xml = docSerializer.toXML(document);

//            parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().setInboundTemplate(xml.replaceAll("\\r\\n", "\n"));  // Not required with current text area
            parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().setInboundTemplate(xml);

            if (parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundDataType().equals(DataTypeFactory.XML)
                    && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate() != null
                    && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate().length() == 0) {
                List<Connector> list = parent.channelEditPanel.currentChannel.getDestinationConnectors();
                for (Connector c : list) {
//                    c.getTransformer().setInboundTemplate(xml.replaceAll("\\r\\n", "\n"));  // Not required with current text area
                    c.getTransformer().setInboundTemplate(xml);
                }
            }
        } catch (Exception ex) {
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        sqlLabel = new javax.swing.JLabel();
        databaseDriverCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        databaseURLField = new com.mirth.connect.client.ui.components.MirthTextField();
        databaseUsernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        databasePasswordField = new com.mirth.connect.client.ui.components.MirthPasswordField();
        onUpdateLabel = new javax.swing.JLabel();
        readOnUpdateYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        readOnUpdateNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        dbVarList = new com.mirth.connect.client.ui.components.MirthVariableList();
        databaseSQLTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea(true,false);
        databaseUpdateSQLTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea(true,false);
        jLabel6 = new javax.swing.JLabel();
        useJavaScriptYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useJavaScriptNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        generateConnection = new javax.swing.JButton();
        generateUpdateConnection = new javax.swing.JButton();
        generateSelect = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        generateUpdateSelect = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        cacheResultsNoButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        cacheResultsYesButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel10 = new javax.swing.JLabel();
        insertURLTemplateButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel1.setText("Driver:");

        jLabel2.setText("URL:");

        jLabel3.setText("Username:");

        jLabel4.setText("Password:");

        sqlLabel.setText("SQL:");

        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sun JDBC-ODBC Bridge", "ODBC - MySQL", "ODBC - PostgresSQL", "ODBC - SQL Server/Sybase", "ODBC - Oracle 10g Release 2" }));
        databaseDriverCombobox.setToolTipText("Specifies the type of database driver to use to connect to the database.");

        databaseURLField.setToolTipText("<html>The JDBC URL to connect to the database. This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");

        databaseUsernameField.setToolTipText("<html>The user name to connect to the database. This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");

        databasePasswordField.setToolTipText("<html>The password to connect to the database. This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");

        onUpdateLabel.setText("On-Update SQL:");

        readOnUpdateYes.setBackground(new java.awt.Color(255, 255, 255));
        readOnUpdateYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(readOnUpdateYes);
        readOnUpdateYes.setText("Yes");
        readOnUpdateYes.setToolTipText("<html>When using a database reader, it is usually necessary to execute a separate SQL statement<br>to mark the message that was just fetched as processed, so it will not be fetched again the next time a poll occurs.<br>Selecting \"Yes\" for Run On-Update Statement turns this behavior on.<br>Selecting \"No\" turns this behavior off.</html>");
        readOnUpdateYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        readOnUpdateYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readOnUpdateYesActionPerformed(evt);
            }
        });

        readOnUpdateNo.setBackground(new java.awt.Color(255, 255, 255));
        readOnUpdateNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(readOnUpdateNo);
        readOnUpdateNo.setSelected(true);
        readOnUpdateNo.setText("No");
        readOnUpdateNo.setToolTipText("<html>When using a database reader, it is usually necessary to execute a separate SQL statement<br>to mark the message that was just fetched as processed, so it will not be fetched again the next time a poll occurs.<br>Selecting \"Yes\" for Run On-Update Statement turns this behavior on.<br>Selecting \"No\" turns this behavior off.</html>");
        readOnUpdateNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        readOnUpdateNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readOnUpdateNoActionPerformed(evt);
            }
        });

        jLabel8.setText("Run On-Update Statment:");

        dbVarList.setToolTipText("<html>This list is populated with mappings based on the select statement in the JavaScript or SQL editor.<br>These mappings can dragged into the On-Update JavaScript or SQL editors.</html>");
        jScrollPane1.setViewportView(dbVarList);

        databaseSQLTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        databaseUpdateSQLTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setText("Use JavaScript");

        useJavaScriptYes.setBackground(new java.awt.Color(255, 255, 255));
        useJavaScriptYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(useJavaScriptYes);
        useJavaScriptYes.setText("Yes");
        useJavaScriptYes.setToolTipText("Implement JavaScript code using JDBC to get the messages to be processed and mark messages in the database as processed.");
        useJavaScriptYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJavaScriptYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJavaScriptYesActionPerformed(evt);
            }
        });

        useJavaScriptNo.setBackground(new java.awt.Color(255, 255, 255));
        useJavaScriptNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(useJavaScriptNo);
        useJavaScriptNo.setText("No");
        useJavaScriptNo.setToolTipText("Specify the SQL statements to get messages to be processed and mark messages in the database as processed.");
        useJavaScriptNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJavaScriptNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJavaScriptNoActionPerformed(evt);
            }
        });

        generateConnection.setText("Connection");
        generateConnection.setToolTipText("<html>If \"Yes\" is selected for Use JavaScript, this button is enabled.<br>When clicked, it inserts boilerplate Connection construction code into the JavaScript control at the current caret location.</html>");
        generateConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateConnectionActionPerformed(evt);
            }
        });

        generateUpdateConnection.setText("Connection");
        generateUpdateConnection.setToolTipText("<html>If \"Yes\" is selected for Use JavaScript and \"Yes\" is selected for Run On-Update Statement, this button is enabled.<br>When clicked, it inserts boilerplate Connection construction code into the On-Update JavaScript control at the current caret location.</html>");
        generateUpdateConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateUpdateConnectionActionPerformed(evt);
            }
        });

        generateSelect.setText("Select");
        generateSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateSelectActionPerformed(evt);
            }
        });

        jLabel7.setText("Generate:");

        generateUpdateSelect.setText("Update");
        generateUpdateSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateUpdateSelectActionPerformed(evt);
            }
        });

        jLabel9.setText("Generate:");

        cacheResultsNoButton.setBackground(new java.awt.Color(255, 255, 255));
        cacheResultsNoButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(cacheResultsNoButton);
        cacheResultsNoButton.setText("No");
        cacheResultsNoButton.setToolTipText("Specify the SQL statements to get messages to be processed and mark messages in the database as processed.");
        cacheResultsNoButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cacheResultsYesButton.setBackground(new java.awt.Color(255, 255, 255));
        cacheResultsYesButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(cacheResultsYesButton);
        cacheResultsYesButton.setText("Yes");
        cacheResultsYesButton.setToolTipText("Implement JavaScript code using JDBC to get the messages to be processed and mark messages in the database as processed.");
        cacheResultsYesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel10.setText("Cache Results:");

        insertURLTemplateButton.setText("Insert URL Template");
        insertURLTemplateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertURLTemplateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sqlLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(onUpdateLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(databasePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(databaseUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(databaseURLField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(databaseDriverCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(insertURLTemplateButton)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(readOnUpdateYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(readOnUpdateNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generateUpdateConnection)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generateUpdateSelect))
                    .addComponent(databaseSQLTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(databaseUpdateSQLTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(cacheResultsYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cacheResultsNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(useJavaScriptYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(useJavaScriptNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generateConnection)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generateSelect)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {generateConnection, generateSelect});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {generateUpdateConnection, generateUpdateSelect});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseDriverCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(insertURLTemplateButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseURLField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databasePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(cacheResultsYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cacheResultsNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(useJavaScriptYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(useJavaScriptNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(generateSelect)
                        .addComponent(generateConnection)
                        .addComponent(jLabel7)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sqlLabel)
                    .addComponent(databaseSQLTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(readOnUpdateYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(readOnUpdateNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(generateUpdateConnection)
                    .addComponent(generateUpdateSelect)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                    .addComponent(databaseUpdateSQLTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                    .addComponent(onUpdateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void generateUpdateConnectionActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_generateUpdateConnectionActionPerformed
    {//GEN-HEADEREND:event_generateUpdateConnectionActionPerformed
        String connString = "// This update script will be executed once for every result returned from the above query.\n";
        connString += generateConnectionString();
        databaseUpdateSQLTextPane.setText(connString + "\n\n" + databaseUpdateSQLTextPane.getText());
        databaseUpdateSQLTextPane.requestFocus();
        databaseUpdateSQLTextPane.setCaretPosition(databaseUpdateSQLTextPane.getText().indexOf("\n\n") + 1);
        parent.setSaveEnabled(true);
}//GEN-LAST:event_generateUpdateConnectionActionPerformed

private void generateSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateSelectActionPerformed
    showDatabaseMetaData(STATEMENT_TYPE.SELECT_TYPE);
}//GEN-LAST:event_generateSelectActionPerformed

private void generateUpdateSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateUpdateSelectActionPerformed
    showDatabaseMetaData(STATEMENT_TYPE.UPDATE_TYPE);
}//GEN-LAST:event_generateUpdateSelectActionPerformed

private void insertURLTemplateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertURLTemplateButtonActionPerformed

    if (!databaseURLField.getText().equals("")) {
        if (!parent.alertOption(parent, "Are you sure you would like to replace your current connection URL with the template URL?")) {
            return;
        }
    }

    String template = "";

    for (int i = 0; i < drivers.size(); i++) {
        DriverInfo driverInfo = drivers.get(i);
        if (driverInfo.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem()))) {
            template = driverInfo.getTemplate();
        }
    }

    databaseURLField.setText(template);
    databaseURLField.grabFocus();
    parent.setSaveEnabled(true);

}//GEN-LAST:event_insertURLTemplateButtonActionPerformed

    public void showDatabaseMetaData(STATEMENT_TYPE type) {
        DatabaseReceiverProperties properties = (DatabaseReceiverProperties) getProperties();
        
        if (properties.getUrl().length() == 0 || properties.getDriver().equals(DatabaseReceiverProperties.DRIVER_DEFAULT)) {
            parent.alertError(parent, "A valid Driver and URL are required to perform this operation.");
        } else {
            String selectLimit = null;
                
            for (int i = 0; i < drivers.size(); i++) {
                DriverInfo driver = drivers.get(i);
                if (driver.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem()))) {
                    selectLimit = driver.getSelectLimit();
                }
            }
            
            new DatabaseMetadataDialog(this, type, new DatabaseConnectionInfo(properties.getDriver(), properties.getUrl(), properties.getUsername(), properties.getPassword(), "", selectLimit));
        }
    }

    public void setSelectText(String statement) {
        if (!useJavaScriptYes.isSelected()) {
            databaseSQLTextPane.setText(statement + "\n\n" + databaseSQLTextPane.getText());
        } else {
            StringBuilder connectionString = new StringBuilder();
            connectionString.append("var result = dbConn.executeCachedQuery(\"");
            connectionString.append(statement.replaceAll("\\n", " "));
            connectionString.append("\");\n");
            databaseSQLTextPane.setSelectedText("\n" + connectionString.toString());
        }
        parent.setSaveEnabled(true);
    }

    public void setUpdateText(List<String> statements) {
        if (!useJavaScriptYes.isSelected()) {
            for (String statement : statements) {
                databaseUpdateSQLTextPane.setText(statement.replaceAll("\\?", "") + "\n\n" + databaseUpdateSQLTextPane.getText());
            }
        } else {
            StringBuilder connectionString = new StringBuilder();
            for (String statement : statements) {
                connectionString.append("var result = dbConn.executeUpdate(\"");
                connectionString.append(statement.replaceAll("\\n", " "));
                connectionString.append("\");\n");
            }
            databaseUpdateSQLTextPane.setSelectedText("\n" + connectionString.toString());
        }

        parent.setSaveEnabled(true);
    }

    private void generateConnectionActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_generateConnectionActionPerformed
    {// GEN-HEADEREND:event_generateConnectionActionPerformed
        String connString = generateConnectionString();
        connString += "\n// You may access this result below with $('column_name')\nreturn result;";
        databaseSQLTextPane.setText(connString + "\n\n" + databaseSQLTextPane.getText());
        databaseSQLTextPane.requestFocus();
        databaseSQLTextPane.setCaretPosition(databaseSQLTextPane.getText().indexOf("\n\n") + 1);
        parent.setSaveEnabled(true);
    }// GEN-LAST:event_generateConnectionActionPerformed

    private String generateConnectionString() {
        String driver = "";

        for (int i = 0; i < drivers.size(); i++) {
            DriverInfo driverInfo = drivers.get(i);
            if (driverInfo.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem()))) {
                driver = driverInfo.getClassName();
            }
        }

        StringBuilder connectionString = new StringBuilder();
        connectionString.append("var dbConn = DatabaseConnectionFactory.createDatabaseConnection('");
        connectionString.append(driver + "','" + databaseURLField.getText() + "','");
        connectionString.append(databaseUsernameField.getText() + "','" + new String(databasePasswordField.getPassword()) + "\');\n");
        connectionString.append("\ndbConn.close();");

        return connectionString.toString();
    }

    private void useJavaScriptNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_useJavaScriptNoActionPerformed
    {// GEN-HEADEREND:event_useJavaScriptNoActionPerformed
        sqlLabel.setText("SQL:");
        onUpdateLabel.setText("On-Update SQL:");
        databaseSQLTextPane.setDocument(sqlMappingDoc);
        databaseUpdateSQLTextPane.setDocument(sqlUpdateMappingDoc);
        databaseSQLTextPane.setText("");
        databaseUpdateSQLTextPane.setText("");
        generateConnection.setEnabled(false);
        updateSQL();

        generateUpdateConnection.setEnabled(false);
        dbVarList.setPrefixAndSuffix("${", "}");
    }// GEN-LAST:event_useJavaScriptNoActionPerformed

    private void useJavaScriptYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_useJavaScriptYesActionPerformed
    {// GEN-HEADEREND:event_useJavaScriptYesActionPerformed
        sqlLabel.setText("JavaScript:");
        onUpdateLabel.setText("On-Update JavaScript:");
        databaseSQLTextPane.setDocument(jsMappingDoc);
        databaseUpdateSQLTextPane.setDocument(jsUpdateMappingDoc);
        String connString = generateConnectionString();
        String query = connString + "\n// You may access this result below with $('column_name')\nreturn result;";
        databaseSQLTextPane.setText(query);
        String update = "// This update script will be executed once for every result returned from the above query.\n" + connString;
        databaseUpdateSQLTextPane.setText(update);
        generateConnection.setEnabled(true);
        updateSQL();

        if (readOnUpdateYes.isSelected()) {
            generateUpdateConnection.setEnabled(true);
        }
        dbVarList.setPrefixAndSuffix("$('", "')");
    }// GEN-LAST:event_useJavaScriptYesActionPerformed

    private void readOnUpdateNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_readOnUpdateNoActionPerformed
    {// GEN-HEADEREND:event_readOnUpdateNoActionPerformed
        onUpdateLabel.setEnabled(false);
        databaseUpdateSQLTextPane.setEnabled(false);
        generateUpdateConnection.setEnabled(false);
    }// GEN-LAST:event_readOnUpdateNoActionPerformed

    private void readOnUpdateYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_readOnUpdateYesActionPerformed
    {// GEN-HEADEREND:event_readOnUpdateYesActionPerformed
        onUpdateLabel.setEnabled(true);
        databaseUpdateSQLTextPane.setEnabled(true);

        if (useJavaScriptYes.isSelected()) {
            generateUpdateConnection.setEnabled(true);
        }
    }// GEN-LAST:event_readOnUpdateYesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup4;
    private com.mirth.connect.client.ui.components.MirthRadioButton cacheResultsNoButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton cacheResultsYesButton;
    private com.mirth.connect.client.ui.components.MirthComboBox databaseDriverCombobox;
    private com.mirth.connect.client.ui.components.MirthPasswordField databasePasswordField;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea databaseSQLTextPane;
    private com.mirth.connect.client.ui.components.MirthTextField databaseURLField;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea databaseUpdateSQLTextPane;
    private com.mirth.connect.client.ui.components.MirthTextField databaseUsernameField;
    private com.mirth.connect.client.ui.components.MirthVariableList dbVarList;
    private javax.swing.JButton generateConnection;
    private javax.swing.JButton generateSelect;
    private javax.swing.JButton generateUpdateConnection;
    private javax.swing.JButton generateUpdateSelect;
    private javax.swing.JButton insertURLTemplateButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel onUpdateLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton readOnUpdateNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton readOnUpdateYes;
    private javax.swing.JLabel sqlLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJavaScriptNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJavaScriptYes;
    // End of variables declaration//GEN-END:variables
}
