/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.parsers.DocumentBuilderFactory;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.VariableListHandler.TransferMode;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.util.SQLParserUtil;
import com.mirth.connect.connectors.jdbc.DatabaseMetadataDialog.STATEMENT_TYPE;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.util.JavaScriptSharedUtil;

public class DatabaseReader extends ConnectorSettingsPanel {
    private List<DriverInfo> drivers;
    private Frame parent;
    private Timer timer;

    public DatabaseReader() {
        this.parent = PlatformUI.MIRTH_FRAME;

        try {
            drivers = this.parent.mirthClient.getDatabaseDrivers();
        } catch (ClientException e) {
            parent.alertThrowable(this, e);
        }

        initComponents();
        initLayout();

        retryCountField.setDocument(new MirthFieldConstraints(0, false, false, true));
        retryIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));

        drivers.add(0, new DriverInfo(DatabaseReceiverProperties.DRIVER_DEFAULT, DatabaseReceiverProperties.DRIVER_DEFAULT, "", ""));
        String[] driverNames = new String[drivers.size()];

        for (int i = 0; i < drivers.size(); i++) {
            driverNames[i] = drivers.get(i).getName();
        }

        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(driverNames));
        fetchSizeField.setDocument(new MirthFieldConstraints(9, false, false, true));

        selectTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        updateTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);

        DocumentListener documentListener = new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {}

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void insertUpdate(DocumentEvent e) {
                update();
            }
        };

        selectTextPane.getDocument().addDocumentListener(documentListener);
        updateTextPane.getDocument().addDocumentListener(documentListener);

        parent.setupCharsetEncodingForConnector(encodingCombobox);
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
        properties.setKeepConnectionOpen(keepConnOpenYes.isSelected());
        properties.setAggregateResults(aggregateResultsYesRadio.isSelected());
        properties.setCacheResults(cacheResultsYesButton.isSelected());
        properties.setFetchSize(fetchSizeField.getText());
        properties.setRetryCount(retryCountField.getText());
        properties.setRetryInterval(retryIntervalField.getText());
        properties.setUseScript(useScriptYes.isSelected());
        properties.setSelect(selectTextPane.getText());
        properties.setUpdate(updateTextPane.getText());

        if (updateOnce.isSelected()) {
            properties.setUpdateMode(DatabaseReceiverProperties.UPDATE_ONCE);
        } else if (updateEach.isSelected()) {
            properties.setUpdateMode(DatabaseReceiverProperties.UPDATE_EACH);
        } else {
            properties.setUpdateMode(DatabaseReceiverProperties.UPDATE_NEVER);
        }

        properties.setEncoding(parent.getSelectedEncodingForConnector(encodingCombobox));

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

        if (props.isKeepConnectionOpen()) {
            keepConnOpenYes.setSelected(true);
            keepConnOpenNo.setSelected(false);
        } else {
            keepConnOpenYes.setSelected(false);
            keepConnOpenNo.setSelected(true);
        }

        if (props.isCacheResults()) {
            cacheResultsYesButton.setSelected(true);
            cacheResultsYesButtonActionPerformed(null);
        } else {
            cacheResultsNoButton.setSelected(true);
            cacheResultsNoButtonActionPerformed(null);
        }

        if (props.isAggregateResults()) {
            aggregateResultsYesRadio.setSelected(true);
            aggregateResultsActionPerformed(true);
        } else {
            aggregateResultsNoRadio.setSelected(true);
            aggregateResultsActionPerformed(false);
        }

        fetchSizeField.setText(props.getFetchSize());
        retryCountField.setText(props.getRetryCount());
        retryIntervalField.setText(props.getRetryInterval());

        parent.setPreviousSelectedEncodingForConnector(encodingCombobox, props.getEncoding());

        if (props.isUseScript()) {
            useScriptYes.setSelected(true);
            useScriptYesActionPerformed(null);
        } else {
            useScriptNo.setSelected(true);
            useScriptNoActionPerformed(null);
        }

        selectTextPane.setText(props.getSelect());
        updateTextPane.setText(props.getUpdate());

        switch (props.getUpdateMode()) {
            case DatabaseReceiverProperties.UPDATE_EACH:
                updateEach.setSelected(true);
                updateEachActionPerformed(null);
                break;

            case DatabaseReceiverProperties.UPDATE_ONCE:
                updateOnce.setSelected(true);
                updateOnceActionPerformed(null);
                break;

            default:
                updateNever.setSelected(true);
                updateNeverActionPerformed(null);
                break;
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

        if (!props.isUseScript() && props.getUrl().length() == 0) {
            valid = false;
            if (highlight) {
                databaseURLField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getSelect().length() == 0) {
            valid = false;

            if (highlight) {
                selectTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getUpdateMode() != DatabaseReceiverProperties.UPDATE_NEVER && (props.getUpdate().length() == 0)) {
            valid = false;

            if (highlight) {
                updateTextPane.setBackground(UIConstants.INVALID_COLOR);
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
        fetchSizeField.setBackground(null);
        selectTextPane.setBackground(null);
        updateTextPane.setBackground(null);
        databaseDriverCombobox.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        selectTextPane.updateDisplayOptions();
        updateTextPane.updateDisplayOptions();
    }

    @Override
    public String doValidate(ConnectorProperties properties, boolean highlight) {
        DatabaseReceiverProperties props = (DatabaseReceiverProperties) properties;
        StringBuilder error = new StringBuilder();

        if (props.isUseScript()) {
            String script = props.getSelect();

            if (script.length() != 0) {
                Context context = JavaScriptSharedUtil.getGlobalContextForValidation();

                try {
                    context.compileString("function rhinoWrapper() {" + script + "\n}", UUID.randomUUID().toString(), 1, null);
                } catch (EvaluatorException e) {
                    error.append("Error in connector \"" + getConnectorName() + "\" at Javascript:\nError on line " + e.lineNumber() + ": " + e.getMessage() + ".\n\n");
                } catch (Exception e) {
                    error.append("Error in connector \"" + getConnectorName() + "\" at Javascript:\nUnknown error occurred during validation.");
                }

                Context.exit();
            }

            if (props.getUpdateMode() != DatabaseReceiverProperties.UPDATE_NEVER) {
                String onUpdateScript = props.getUpdate();

                if (onUpdateScript.length() != 0) {
                    Context context = JavaScriptSharedUtil.getGlobalContextForValidation();

                    try {
                        context.compileString("function rhinoWrapper() {" + onUpdateScript + "\n}", UUID.randomUUID().toString(), 1, null);
                    } catch (EvaluatorException e) {
                        error.append("Error in connector \"" + getConnectorName() + "\" at On-Update Javascript:\nError on line " + e.lineNumber() + ": " + e.getMessage() + ".\n\n");
                    } catch (Exception e) {
                        error.append("Error in connector \"" + getConnectorName() + "\" at Javascript:\nUnknown error occurred during validation.");
                    }

                    Context.exit();
                }

            }
        }

        return (error.length() == 0) ? null : error.toString();
    }

    @Override
    public boolean requiresXmlDataType() {
        return true;
    }

    private void update() {
        if (timer == null) {
            timer = new Timer(1000, new UpdateSQLActionListener());
            timer.setRepeats(false);
            timer.start();
        } else {
            timer.restart();
        }
    }

    private class UpdateSQLActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            final String workingId = PlatformUI.MIRTH_FRAME.startWorking("Parsing...");
            final String sqlStatement = selectTextPane.getText();

            SwingWorker<String[], Void> worker = new SwingWorker<String[], Void>() {
                @Override
                public String[] doInBackground() {
                    return parseData(sqlStatement);
                }

                @Override
                public void done() {
                    String[] data;
                    try {
                        data = get();
                    } catch (Exception e) {
                        data = new String[0];
                    }

                    dbVarList.setListData(data);
                    updateIncomingData(data);
                    PlatformUI.MIRTH_FRAME.stopWorking(workingId);
                }
            };

            worker.execute();
        }
    }

    private String[] parseData(String sqlStatement) {
        if (StringUtils.isNotEmpty(sqlStatement)) {
            SQLParserUtil spu = new SQLParserUtil((String) sqlStatement);
            return spu.Parse();

        } else {
            return new String[] {};
        }
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

            if (parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundDataType().equals(UIConstants.DATATYPE_XML) && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate() != null && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate().length() == 0) {
                List<Connector> list = parent.channelEditPanel.currentChannel.getDestinationConnectors();
                for (Connector c : list) {
//                    c.getTransformer().setInboundTemplate(xml.replaceAll("\\r\\n", "\n"));  // Not required with current text area
                    c.getTransformer().setInboundTemplate(xml);
                }
            }
        } catch (Exception ex) {
        }
    }

    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        driverLabel = new javax.swing.JLabel();
        urlLabel = new javax.swing.JLabel();
        usernameLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        selectLabel = new javax.swing.JLabel();
        databaseDriverCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        databaseURLField = new com.mirth.connect.client.ui.components.MirthTextField();
        databaseUsernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        databasePasswordField = new com.mirth.connect.client.ui.components.MirthPasswordField();
        updateLabel = new javax.swing.JLabel();
        updateEach = new com.mirth.connect.client.ui.components.MirthRadioButton();
        updateOnce = new com.mirth.connect.client.ui.components.MirthRadioButton();
        runUpdateLabel = new javax.swing.JLabel();
        dbVarScrollPane = new javax.swing.JScrollPane();
        dbVarList = new com.mirth.connect.client.ui.components.MirthVariableList();
        selectTextPane = new com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane(ContextType.SOURCE_RECEIVER, true);
        updateTextPane = new com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane(ContextType.SOURCE_RECEIVER, true);
        useScriptLabel = new javax.swing.JLabel();
        useScriptYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useScriptNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        generateConnection = new javax.swing.JButton();
        generateUpdateConnection = new javax.swing.JButton();
        generateSelect = new javax.swing.JButton();
        generateLabel = new javax.swing.JLabel();
        generateUpdateUpdate = new javax.swing.JButton();
        generateUpdateLabel = new javax.swing.JLabel();
        cacheResultsNoButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        cacheResultsYesButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        cacheResultsLabel = new javax.swing.JLabel();
        insertURLTemplateButton = new javax.swing.JButton();
        fetchSizeLabel = new javax.swing.JLabel();
        fetchSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        updateNever = new com.mirth.connect.client.ui.components.MirthRadioButton();
        retryCountLabel = new javax.swing.JLabel();
        keepConnOpenLabel = new javax.swing.JLabel();
        keepConnOpenYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        keepConnOpenNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        retryCountField = new com.mirth.connect.client.ui.components.MirthTextField();
        retryIntervalLabel = new javax.swing.JLabel();
        retryIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        contentEncodingLabel = new javax.swing.JLabel();
        encodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        driverLabel.setText("Driver:");

        urlLabel.setText("URL:");

        usernameLabel.setText("Username:");

        passwordLabel.setText("Password:");

        selectLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        selectLabel.setText("JavaScript:");

        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
                "Sun JDBC-ODBC Bridge", "ODBC - MySQL", "ODBC - PostgresSQL",
                "ODBC - SQL Server/Sybase", "ODBC - Oracle 10g Release 2" }));
        databaseDriverCombobox.setToolTipText("Specifies the type of database driver to use to connect to the database.");

        databaseURLField.setToolTipText("<html>The JDBC URL to connect to the database. This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");

        databaseUsernameField.setToolTipText("<html>The user name to connect to the database. This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");

        databasePasswordField.setToolTipText("<html>The password to connect to the database. This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");

        updateLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        updateLabel.setText("JavaScript:");

        updateEach.setBackground(new java.awt.Color(255, 255, 255));
        updateEach.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(updateEach);
        updateEach.setText("After each message");
        updateEach.setToolTipText("<html>Run the post-process statement/script after each message finishes processing.</html>");
        updateEach.setMargin(new java.awt.Insets(0, 0, 0, 0));
        updateEach.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateEachActionPerformed(evt);
            }
        });

        updateOnce.setBackground(new java.awt.Color(255, 255, 255));
        updateOnce.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(updateOnce);
        updateOnce.setText("Once after all messages");
        updateOnce.setToolTipText("<html>Run the post-process statement/script only after all messages have finished processing.</html>");
        updateOnce.setMargin(new java.awt.Insets(0, 0, 0, 0));
        updateOnce.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateOnceActionPerformed(evt);
            }
        });

        runUpdateLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        runUpdateLabel.setText("Run Post-Process Script:");
        runUpdateLabel.setToolTipText("<html>When using a database reader, it is usually necessary to execute a separate SQL statement<br>to mark the message that was just fetched as processed, so it will not be fetched again the next time a poll occurs.</html>");

        dbVarList.setToolTipText("<html>This list is populated with mappings based on the select statement in the JavaScript or SQL editor.<br>These mappings can dragged into the post-process JavaScript or SQL editors.</html>");
        dbVarScrollPane.setViewportView(dbVarList);

        selectTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        updateTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        useScriptLabel.setText("Use JavaScript:");

        useScriptYes.setBackground(new java.awt.Color(255, 255, 255));
        useScriptYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(useScriptYes);
        useScriptYes.setText("Yes");
        useScriptYes.setToolTipText("<html>Implement JavaScript code using JDBC to get the messages to be processed and mark messages in the database as processed.</html>");
        useScriptYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useScriptYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useScriptYesActionPerformed(evt);
            }
        });

        useScriptNo.setBackground(new java.awt.Color(255, 255, 255));
        useScriptNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(useScriptNo);
        useScriptNo.setText("No");
        useScriptNo.setToolTipText("<html>Specify the SQL statements to get messages to be processed and mark messages in the database as processed.</html>");
        useScriptNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useScriptNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useScriptNoActionPerformed(evt);
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
        generateUpdateConnection.setToolTipText("<html>This button is enabled when using JavaScript and a post-process script.<br>When clicked, it inserts boilerplate Connection construction code into the post-process JavaScript control at the current caret location.</html>");
        generateUpdateConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateUpdateConnectionActionPerformed(evt);
            }
        });

        generateSelect.setText("Select");
        generateSelect.setToolTipText("<html>Opens a window to assist in building a select query to select records from the database specified in the URL above.</html>");
        generateSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateSelectActionPerformed(evt);
            }
        });

        generateLabel.setText("Generate:");

        generateUpdateUpdate.setText("Update");
        generateUpdateUpdate.setToolTipText("<html>Opens a window to assist in building an update query to update records in the database specified in the URL above.<br/>(Only enabled if a post-process statement/script is enabled)</html>");
        generateUpdateUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateUpdateUpdateActionPerformed(evt);
            }
        });

        generateUpdateLabel.setText("Generate:");

        aggregateResultsLabel = new JLabel("Aggregate Results:");
        ButtonGroup aggregateResultsButtonGroup = new ButtonGroup();
        String toolTipText = "<html>If enabled, all rows returned in the query will be<br/>aggregated into a single XML message. Note that all rows<br/>will be read into memory at once, so use this with caution.</html>";

        aggregateResultsYesRadio = new MirthRadioButton("Yes");
        aggregateResultsYesRadio.setBackground(getBackground());
        aggregateResultsYesRadio.setToolTipText(toolTipText);
        aggregateResultsYesRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (!parent.alertOption(parent, "<html><b>Warning:</b> All rows returned by the query below will be aggregated<br/>into a single message. This could cause memory issues if you are<br/>reading in large amounts of data. Consider using LIMIT to limit<br/>the number of rows to return. Are you sure you wish to continue?</html>")) {
                    aggregateResultsNoRadio.setSelected(true);
                } else {
                    aggregateResultsActionPerformed(true);
                }
            }
        });
        aggregateResultsButtonGroup.add(aggregateResultsYesRadio);

        aggregateResultsNoRadio = new MirthRadioButton("No");
        aggregateResultsNoRadio.setBackground(getBackground());
        aggregateResultsNoRadio.setToolTipText(toolTipText);
        aggregateResultsNoRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                aggregateResultsActionPerformed(false);
            }
        });
        aggregateResultsButtonGroup.add(aggregateResultsNoRadio);

        cacheResultsNoButton.setBackground(new java.awt.Color(255, 255, 255));
        cacheResultsNoButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(cacheResultsNoButton);
        cacheResultsNoButton.setText("No");
        cacheResultsNoButton.setToolTipText("<html>Do not cache the entire result set in memory prior to processing messages.</html>");
        cacheResultsNoButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cacheResultsNoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cacheResultsNoButtonActionPerformed(evt);
            }
        });

        cacheResultsYesButton.setBackground(new java.awt.Color(255, 255, 255));
        cacheResultsYesButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(cacheResultsYesButton);
        cacheResultsYesButton.setText("Yes");
        cacheResultsYesButton.setToolTipText("<html>Cache the entire result set in memory prior to processing messages.</html>");
        cacheResultsYesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cacheResultsYesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cacheResultsYesButtonActionPerformed(evt);
            }
        });

        cacheResultsLabel.setText("Cache Results:");

        insertURLTemplateButton.setText("Insert URL Template");
        insertURLTemplateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertURLTemplateButtonActionPerformed(evt);
            }
        });

        fetchSizeLabel.setText("Fetch Size:");

        fetchSizeField.setToolTipText("<html>The JDBC ResultSet fetch size to be used when fetching results from the current cursor position.</html>");

        updateNever.setBackground(new java.awt.Color(255, 255, 255));
        updateNever.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(updateNever);
        updateNever.setSelected(true);
        updateNever.setText("Never");
        updateNever.setToolTipText("<html>Do not run the post-process statement/script.</html>");
        updateNever.setMargin(new java.awt.Insets(0, 0, 0, 0));
        updateNever.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateNeverActionPerformed(evt);
            }
        });

        retryCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        retryCountLabel.setText("# of Retries on Error:");

        keepConnOpenLabel.setText("Keep Connection Open:");

        keepConnOpenYes.setBackground(new java.awt.Color(255, 255, 255));
        keepConnOpenYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(keepConnOpenYes);
        keepConnOpenYes.setText("Yes");
        keepConnOpenYes.setToolTipText("<html>Re-use the same database connection each time the select query is executed.</html>");
        keepConnOpenYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        keepConnOpenNo.setBackground(new java.awt.Color(255, 255, 255));
        keepConnOpenNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(keepConnOpenNo);
        keepConnOpenNo.setText("No");
        keepConnOpenNo.setToolTipText("<html>Close the database connection after selected messages have finished processing.</html>");
        keepConnOpenNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        retryCountField.setToolTipText("<html>The number of times to retry executing the statement or script if an error occurs.</html>");

        retryIntervalLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        retryIntervalLabel.setText("Retry Interval (ms):");

        retryIntervalField.setToolTipText("<html>The amount of time that should elapse between retry attempts.</html>");

        contentEncodingLabel = new javax.swing.JLabel("Encoding:");
        encodingCombobox.setToolTipText("<html>Select the character set encoding used by the source database,<br/>or select Default to use the default character set encoding for the JVM running Mirth.</html>");
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 6 6", "6[]13[grow]", "[][][][][][][][][][][][][sgy][][sgy]"));

        add(driverLabel, "right");
        add(databaseDriverCombobox, "split");
        add(insertURLTemplateButton);
        add(urlLabel, "newline, right");
        add(databaseURLField, "w 331!");
        add(usernameLabel, "newline, right");
        add(databaseUsernameField, "w 121!");
        add(passwordLabel, "newline, right");
        add(databasePasswordField, "w 121!");
        add(useScriptLabel, "newline, right");
        add(useScriptYes, "split");
        add(useScriptNo);
        add(keepConnOpenLabel, "newline, right");
        add(keepConnOpenYes, "split");
        add(keepConnOpenNo);
        add(aggregateResultsLabel, "newline, right");
        add(aggregateResultsYesRadio, "split");
        add(aggregateResultsNoRadio);
        add(cacheResultsLabel, "newline, right");
        add(cacheResultsYesButton, "split");
        add(cacheResultsNoButton);
        add(fetchSizeLabel, "newline, right");
        add(fetchSizeField, "w 121!");
        add(retryCountLabel, "newline, right");
        add(retryCountField, "w 121!");
        add(retryIntervalLabel, "newline, right");
        add(retryIntervalField, "w 121!");
        add(contentEncodingLabel, "newline, right");
        add(encodingCombobox, "split 1, left");
        add(generateLabel, "split 3, sx, right");
        add(generateConnection, "w 67!");
        add(generateSelect, "w 67!");
        add(selectLabel, "newline, top, right");
        add(selectTextPane, "sx, grow, pushy, w :400, h :100");
        add(runUpdateLabel, "newline, right, w 120!");
        add(updateNever, "split 3");
        add(updateEach);
        add(updateOnce);
        add(generateUpdateLabel, "sx, split 3, right");
        add(generateUpdateConnection, "w 67!");
        add(generateUpdateUpdate, "w 67!");
        add(updateLabel, "newline, top, right");
        add(updateTextPane, "grow, split, sx, pushy, w :400, h :100");
        add(dbVarScrollPane, "growy, right, w 195!");
    }

    private void generateUpdateUpdateActionPerformed(java.awt.event.ActionEvent evt) {
        showDatabaseMetaData(STATEMENT_TYPE.UPDATE_TYPE);
    }

    private void generateUpdateConnectionActionPerformed(java.awt.event.ActionEvent evt) {
        updateTextPane.setText(generateUpdateConnectionString() + "\n\n" + updateTextPane.getText());
        updateTextPane.requestFocus();
        updateTextPane.setCaretPosition(updateTextPane.getText().lastIndexOf("\n\n", updateTextPane.getText().length() - 3) + 1);
        parent.setSaveEnabled(true);
    }

    private void generateSelectActionPerformed(java.awt.event.ActionEvent evt) {
        showDatabaseMetaData(STATEMENT_TYPE.SELECT_TYPE);
    }

    private void useScriptNoActionPerformed(java.awt.event.ActionEvent evt) {
        selectLabel.setText("SQL:");
        updateLabel.setText("SQL:");
        runUpdateLabel.setText("Run Post-Process SQL:");
        selectTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        updateTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        selectTextPane.setText("");
        updateTextPane.setText("");

        keepConnOpenLabel.setEnabled(true);
        keepConnOpenNo.setEnabled(true);
        keepConnOpenYes.setEnabled(true);

        aggregateResultsActionPerformed(aggregateResultsYesRadio.isSelected());

        update();

        generateConnection.setEnabled(false);
        generateUpdateConnection.setEnabled(false);
        dbVarList.setTransferMode(TransferMode.VELOCITY);
    }

    private void useScriptYesActionPerformed(java.awt.event.ActionEvent evt) {
        selectLabel.setText("JavaScript:");
        updateLabel.setText("JavaScript:");
        runUpdateLabel.setText("Run Post-Process Script:");
        selectTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        updateTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        selectTextPane.setText(generateConnectionString());
        updateTextPane.setText(generateUpdateConnectionString());

        generateConnection.setEnabled(true);

        keepConnOpenLabel.setEnabled(false);
        keepConnOpenNo.setEnabled(false);
        keepConnOpenYes.setEnabled(false);

        cacheResultsLabel.setEnabled(false);
        cacheResultsNoButton.setEnabled(false);
        cacheResultsYesButton.setEnabled(false);

        fetchSizeField.setEnabled(false);
        fetchSizeLabel.setEnabled(false);
        update();

        if (!updateNever.isSelected()) {
            generateUpdateConnection.setEnabled(true);
        }
        dbVarList.setTransferMode(TransferMode.JAVASCRIPT);
    }

    private void insertURLTemplateButtonActionPerformed(java.awt.event.ActionEvent evt) {
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
    }

    private void cacheResultsYesButtonActionPerformed(java.awt.event.ActionEvent evt) {
        fetchSizeField.setEnabled(false);
        fetchSizeLabel.setEnabled(false);
    }

    private void cacheResultsNoButtonActionPerformed(java.awt.event.ActionEvent evt) {
        fetchSizeField.setEnabled(useScriptNo.isSelected());
        fetchSizeLabel.setEnabled(useScriptNo.isSelected());
    }

    private void updateNeverActionPerformed(java.awt.event.ActionEvent evt) {
        updateLabel.setEnabled(false);
        updateTextPane.setEnabled(false);
        generateUpdateConnection.setEnabled(false);
        generateUpdateUpdate.setEnabled(false);
        generateUpdateLabel.setEnabled(false);
        dbVarList.setEnabled(false);
    }

    private void updateEachActionPerformed(java.awt.event.ActionEvent evt) {
        updateLabel.setEnabled(true);
        updateTextPane.setEnabled(true);

        if (useScriptYes.isSelected()) {
            generateUpdateConnection.setEnabled(true);
            dbVarList.setEnabled(true);
        }

        generateUpdateUpdate.setEnabled(true);
        generateUpdateLabel.setEnabled(true);
        dbVarList.setEnabled(true);
    }

    private void updateOnceActionPerformed(java.awt.event.ActionEvent evt) {
        updateLabel.setEnabled(true);
        updateTextPane.setEnabled(true);

        if (useScriptYes.isSelected()) {
            generateUpdateConnection.setEnabled(true);
            dbVarList.setEnabled(false);
        }

        generateUpdateUpdate.setEnabled(true);
        generateUpdateLabel.setEnabled(true);
        dbVarList.setEnabled(false);
    }

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

            Connector sourceConnector = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel.getSourceConnector();
            Set<String> resourceIds = PlatformUI.MIRTH_FRAME.channelEditPanel.resourceIds.get(sourceConnector.getMetaDataId()).keySet();
            new DatabaseMetadataDialog(this, type, new DatabaseConnectionInfo(properties.getDriver(), properties.getUrl(), properties.getUsername(), properties.getPassword(), "", selectLimit, resourceIds));
        }
    }

    public void setSelectText(String statement) {
        if (!useScriptYes.isSelected()) {
            selectTextPane.setText(statement + "\n\n" + selectTextPane.getText());
        } else {
            StringBuilder connectionString = new StringBuilder();
            connectionString.append("\tvar result = dbConn.executeCachedQuery(\"");
            connectionString.append(statement.replaceAll("\\n", " "));
            connectionString.append("\");\n");
            selectTextPane.setSelectedText("\n" + connectionString.toString());
        }
        parent.setSaveEnabled(true);
    }

    public void setUpdateText(List<String> statements) {
        if (!useScriptYes.isSelected()) {
            for (String statement : statements) {
                updateTextPane.setText(statement.replaceAll("\\?", "") + "\n\n" + updateTextPane.getText());
            }
        } else {
            StringBuilder connectionString = new StringBuilder();
            for (String statement : statements) {
                connectionString.append("\tvar result = dbConn.executeUpdate(\"");
                connectionString.append(statement.replaceAll("\\n", " "));
                connectionString.append("\");\n");
            }
            updateTextPane.setSelectedText("\n" + connectionString.toString());
        }

        parent.setSaveEnabled(true);
    }

    private void generateConnectionActionPerformed(java.awt.event.ActionEvent evt) {
        String connString = generateConnectionString();
        selectTextPane.setText(connString + "\n\n" + selectTextPane.getText());
        selectTextPane.getTextArea().requestFocus();
        selectTextPane.setCaretPosition(selectTextPane.getText().lastIndexOf("\n\n", selectTextPane.getText().length() - 3) + 1);
        parent.setSaveEnabled(true);
    }

    private String generateConnectionString() {
        String driver = "";

        for (int i = 0; i < drivers.size(); i++) {
            DriverInfo driverInfo = drivers.get(i);
            if (driverInfo.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem()))) {
                driver = driverInfo.getClassName();
            }
        }

        StringBuilder connectionString = new StringBuilder();
        connectionString.append("var dbConn;\n");
        connectionString.append("\ntry {\n\tdbConn = DatabaseConnectionFactory.createDatabaseConnection('");
        connectionString.append(driver + "','" + databaseURLField.getText() + "','");
        connectionString.append(databaseUsernameField.getText() + "','" + new String(databasePasswordField.getPassword()) + "\');\n\n\t// You may access this result below with $('column_name')\n\treturn result;\n} finally {");
        connectionString.append("\n\tif (dbConn) { \n\t\tdbConn.close();\n\t}\n}");

        return connectionString.toString();
    }

    private String generateUpdateConnectionString() {
        String driver = "";

        for (int i = 0; i < drivers.size(); i++) {
            DriverInfo driverInfo = drivers.get(i);
            if (driverInfo.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem()))) {
                driver = driverInfo.getClassName();
            }
        }

        StringBuilder connectionString = new StringBuilder();
        if (updateEach.isSelected()) {
            connectionString.append("// This update script will be executed once for every result returned from the above query.\n");
        } else {
            connectionString.append("// This update script will be executed once after all results have been processed.\n");
        }
        if (aggregateResultsYesRadio.isSelected()) {
            connectionString.append("// If \"Aggregate Results\" is enabled, you have access to \"results\",\n// a List of Map objects representing all rows returned from the above query.\n");
        }
        connectionString.append("var dbConn;\n");
        connectionString.append("\ntry {\n\tdbConn = DatabaseConnectionFactory.createDatabaseConnection('");
        connectionString.append(driver + "','" + databaseURLField.getText() + "','");
        connectionString.append(databaseUsernameField.getText() + "','" + new String(databasePasswordField.getPassword()) + "\');\n\n} finally {");
        connectionString.append("\n\tif (dbConn) { \n\t\tdbConn.close();\n\t}\n}");

        return connectionString.toString();
    }

    private void aggregateResultsActionPerformed(boolean aggregateResults) {
        if (aggregateResults) {
            cacheResultsYesButton.setSelected(true);
            cacheResultsYesButtonActionPerformed(null);
            cacheResultsLabel.setEnabled(false);
            cacheResultsYesButton.setEnabled(false);
            cacheResultsNoButton.setEnabled(false);

            updateEach.setText("For each row");
            updateEach.setToolTipText("<html>Run the post-process statement/script for each row in the result set.</html>");

            updateOnce.setText("Once for all rows");
            updateOnce.setToolTipText("<html>Run the post-process statement/script only once.<br/>If JavaScript mode is used, a List of Maps representing all rows<br/>in the result set will be available as the variable \"results\".</html>");
        } else {
            cacheResultsLabel.setEnabled(useScriptNo.isSelected());
            cacheResultsYesButton.setEnabled(useScriptNo.isSelected());
            cacheResultsNoButton.setEnabled(useScriptNo.isSelected());
            if (cacheResultsYesButton.isSelected()) {
                cacheResultsYesButtonActionPerformed(null);
            } else {
                cacheResultsNoButtonActionPerformed(null);
            }

            updateEach.setText("After each message");
            updateEach.setToolTipText("<html>Run the post-process statement/script after each message finishes processing.</html>");

            updateOnce.setText("Once after all messages");
            updateOnce.setToolTipText("<html>Run the post-process statement/script only after all messages have finished processing.</html>");
        }
    }

    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.JLabel cacheResultsLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton cacheResultsNoButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton cacheResultsYesButton;
    private com.mirth.connect.client.ui.components.MirthComboBox databaseDriverCombobox;
    private com.mirth.connect.client.ui.components.MirthPasswordField databasePasswordField;
    private com.mirth.connect.client.ui.components.MirthTextField databaseURLField;
    private com.mirth.connect.client.ui.components.MirthTextField databaseUsernameField;
    private com.mirth.connect.client.ui.components.MirthVariableList dbVarList;
    private javax.swing.JScrollPane dbVarScrollPane;
    private javax.swing.JLabel driverLabel;
    private com.mirth.connect.client.ui.components.MirthTextField fetchSizeField;
    private javax.swing.JLabel fetchSizeLabel;
    private javax.swing.JButton generateConnection;
    private javax.swing.JLabel generateLabel;
    private javax.swing.JButton generateSelect;
    private javax.swing.JButton generateUpdateConnection;
    private javax.swing.JLabel generateUpdateLabel;
    private javax.swing.JButton generateUpdateUpdate;
    private javax.swing.JButton insertURLTemplateButton;
    private javax.swing.JLabel keepConnOpenLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnOpenNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnOpenYes;
    private JLabel aggregateResultsLabel;
    private JRadioButton aggregateResultsYesRadio;
    private JRadioButton aggregateResultsNoRadio;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthTextField retryCountField;
    private javax.swing.JLabel retryCountLabel;
    private com.mirth.connect.client.ui.components.MirthTextField retryIntervalField;
    private javax.swing.JLabel retryIntervalLabel;
    private javax.swing.JLabel runUpdateLabel;
    private javax.swing.JLabel selectLabel;
    private com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane selectTextPane;
    private com.mirth.connect.client.ui.components.MirthRadioButton updateEach;
    private javax.swing.JLabel updateLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton updateNever;
    private com.mirth.connect.client.ui.components.MirthRadioButton updateOnce;
    private com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane updateTextPane;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JLabel useScriptLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton useScriptNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton useScriptYes;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JLabel contentEncodingLabel;
    private com.mirth.connect.client.ui.components.MirthComboBox encodingCombobox;
}