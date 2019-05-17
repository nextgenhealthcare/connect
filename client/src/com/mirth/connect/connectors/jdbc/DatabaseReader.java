/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.parsers.DocumentBuilderFactory;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.VariableListHandler.TransferMode;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthPasswordField;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.MirthVariableList;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
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
    private AtomicBoolean driverAdjusting = new AtomicBoolean(false);

    public DatabaseReader() {
        this.parent = PlatformUI.MIRTH_FRAME;

        initComponents();
        initToolTips();
        initLayout();
    }

    @Override
    public String getConnectorName() {
        return new DatabaseReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        DatabaseReceiverProperties properties = new DatabaseReceiverProperties();

        properties.setDriver(driverField.getText());
        properties.setUrl(urlField.getText());
        properties.setUsername(usernameField.getText());
        properties.setPassword(new String(passwordField.getPassword()));
        properties.setKeepConnectionOpen(keepConnectionOpenYesRadio.isSelected());
        properties.setAggregateResults(aggregateResultsYesRadio.isSelected());
        properties.setCacheResults(cacheResultsYesRadio.isSelected());
        properties.setFetchSize(fetchSizeField.getText());
        properties.setRetryCount(retryCountField.getText());
        properties.setRetryInterval(retryIntervalField.getText());
        properties.setUseScript(useJavaScriptYesRadio.isSelected());
        properties.setSelect(selectSQLTextPane.getText());
        properties.setUpdate(postProcessSQLTextPane.getText());

        if (runPostProcessSQLOnceRadio.isSelected()) {
            properties.setUpdateMode(DatabaseReceiverProperties.UPDATE_ONCE);
        } else if (runPostProcessSQLEachRadio.isSelected()) {
            properties.setUpdateMode(DatabaseReceiverProperties.UPDATE_EACH);
        } else {
            properties.setUpdateMode(DatabaseReceiverProperties.UPDATE_NEVER);
        }

        properties.setEncoding(parent.getSelectedEncodingForConnector(encodingComboBox));

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        DatabaseReceiverProperties props = (DatabaseReceiverProperties) properties;

        boolean enabled = parent.isSaveEnabled();

        if (StringUtils.equals(props.getDriver(), DatabaseReceiverProperties.DRIVER_DEFAULT)) {
            driverField.setText("");
        } else {
            driverField.setText(props.getDriver());
        }

        driverAdjusting.set(true);
        try {
            updateDriverComboBoxFromField();
        } finally {
            driverAdjusting.set(false);
        }
        retrieveDatabaseDrivers(props.getDriver());

        parent.setSaveEnabled(enabled);

        urlField.setText(props.getUrl());
        usernameField.setText(props.getUsername());
        passwordField.setText(props.getPassword());

        if (props.isKeepConnectionOpen()) {
            keepConnectionOpenYesRadio.setSelected(true);
            keepConnectionOpenNoRadio.setSelected(false);
        } else {
            keepConnectionOpenYesRadio.setSelected(false);
            keepConnectionOpenNoRadio.setSelected(true);
        }

        if (props.isCacheResults()) {
            cacheResultsYesRadio.setSelected(true);
            cacheResultsYesButtonActionPerformed();
        } else {
            cacheResultsNoRadio.setSelected(true);
            cacheResultsNoButtonActionPerformed();
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

        parent.setPreviousSelectedEncodingForConnector(encodingComboBox, props.getEncoding());

        if (props.isUseScript()) {
            useJavaScriptYesRadio.setSelected(true);
            useScriptYesActionPerformed();
        } else {
            useJavaScriptNoRadio.setSelected(true);
            useScriptNoActionPerformed();
        }

        selectSQLTextPane.setText(props.getSelect());
        postProcessSQLTextPane.setText(props.getUpdate());

        switch (props.getUpdateMode()) {
            case DatabaseReceiverProperties.UPDATE_EACH:
                runPostProcessSQLEachRadio.setSelected(true);
                updateEachActionPerformed();
                break;

            case DatabaseReceiverProperties.UPDATE_ONCE:
                runPostProcessSQLOnceRadio.setSelected(true);
                updateOnceActionPerformed();
                break;

            default:
                runPostProcessSQLNeverRadio.setSelected(true);
                updateNeverActionPerformed();
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
                urlField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getSelect().length() == 0) {
            valid = false;

            if (highlight) {
                selectSQLTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getUpdateMode() != DatabaseReceiverProperties.UPDATE_NEVER && (props.getUpdate().length() == 0)) {
            valid = false;

            if (highlight) {
                postProcessSQLTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (StringUtils.isBlank(props.getDriver()) || props.getDriver().equals(DatabaseReceiverProperties.DRIVER_DEFAULT)) {
            valid = false;

            if (highlight) {
                driverComboBox.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        urlField.setBackground(null);
        fetchSizeField.setBackground(null);
        selectSQLTextPane.setBackground(null);
        postProcessSQLTextPane.setBackground(null);
        driverComboBox.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        selectSQLTextPane.updateDisplayOptions();
        postProcessSQLTextPane.updateDisplayOptions();
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
    public String getRequiredInboundDataType() {
        return UIConstants.DATATYPE_XML;
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
            final String sqlStatement = selectSQLTextPane.getText();

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
        setBackground(UIConstants.BACKGROUND_COLOR);

        driverLabel = new JLabel("Driver:");

        driverComboBox = new MirthComboBox<DriverInfo>();
        driverComboBox.addActionListener(evt -> {
            if (!driverAdjusting.getAndSet(true)) {
                try {
                    updateDriverFieldFromComboBox();
                } finally {
                    driverAdjusting.set(false);
                }
            }
        });
        driverComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DriverInfo) {
                    setText(((DriverInfo) value).getName());
                }
                return component;
            }
        });

        driverField = new MirthTextField();
        driverField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent evt) {
                update();
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                update();
            }

            private void update() {
                if (!driverAdjusting.getAndSet(true)) {
                    try {
                        updateDriverComboBoxFromField();
                    } finally {
                        driverAdjusting.set(false);
                    }
                }
            }
        });

        updateDriversComboBox();

        manageDriversButton = new JButton(new ImageIcon(Frame.class.getResource("images/wrench.png")));
        manageDriversButton.setToolTipText("<html>Click here to view and manage the list of database JDBC drivers.<br/>Any changes will require re-saving and redeploying the channel.</html>");
        manageDriversButton.addActionListener(evt -> {
            DatabaseDriversDialog dialog = new DatabaseDriversDialog(parent, drivers);
            if (dialog.wasSaved()) {
                drivers = dialog.getDrivers();
                updateDriversComboBox();
            }
        });

        urlLabel = new JLabel("URL:");
        urlField = new MirthTextField();

        insertURLTemplateButton = new JButton("Insert URL Template");
        insertURLTemplateButton.addActionListener(evt -> insertURLTemplateButtonActionPerformed());

        usernameLabel = new JLabel("Username:");
        usernameField = new MirthTextField();

        passwordLabel = new JLabel("Password:");
        passwordField = new MirthPasswordField();

        useJavaScriptLabel = new JLabel("Use JavaScript:");
        ButtonGroup useJavaScriptButtonGroup = new ButtonGroup();

        useJavaScriptYesRadio = new MirthRadioButton("Yes");
        useJavaScriptYesRadio.setBackground(getBackground());
        useJavaScriptYesRadio.addActionListener(evt -> useScriptYesActionPerformed());
        useJavaScriptButtonGroup.add(useJavaScriptYesRadio);

        useJavaScriptNoRadio = new MirthRadioButton("No");
        useJavaScriptNoRadio.setBackground(getBackground());
        useJavaScriptNoRadio.addActionListener(evt -> useScriptNoActionPerformed());
        useJavaScriptButtonGroup.add(useJavaScriptNoRadio);

        keepConnectionOpenLabel = new JLabel("Keep Connection Open:");
        ButtonGroup keepConnectionOpenButtonGroup = new ButtonGroup();

        keepConnectionOpenYesRadio = new MirthRadioButton("Yes");
        keepConnectionOpenYesRadio.setBackground(getBackground());
        keepConnectionOpenButtonGroup.add(keepConnectionOpenYesRadio);

        keepConnectionOpenNoRadio = new MirthRadioButton("No");
        keepConnectionOpenNoRadio.setBackground(getBackground());
        keepConnectionOpenButtonGroup.add(keepConnectionOpenNoRadio);

        aggregateResultsLabel = new JLabel("Aggregate Results:");
        ButtonGroup aggregateResultsButtonGroup = new ButtonGroup();

        aggregateResultsYesRadio = new MirthRadioButton("Yes");
        aggregateResultsYesRadio.setBackground(getBackground());
        aggregateResultsYesRadio.addActionListener(evt -> {
            if (!parent.alertOption(parent, "<html><b>Warning:</b> All rows returned by the query below will be aggregated<br/>into a single message. This could cause memory issues if you are<br/>reading in large amounts of data. Consider using LIMIT to limit<br/>the number of rows to return. Are you sure you wish to continue?</html>")) {
                aggregateResultsNoRadio.setSelected(true);
            } else {
                aggregateResultsActionPerformed(true);
            }
        });
        aggregateResultsButtonGroup.add(aggregateResultsYesRadio);

        aggregateResultsNoRadio = new MirthRadioButton("No");
        aggregateResultsNoRadio.setBackground(getBackground());
        aggregateResultsNoRadio.addActionListener(evt -> aggregateResultsActionPerformed(false));
        aggregateResultsButtonGroup.add(aggregateResultsNoRadio);

        cacheResultsLabel = new JLabel("Cache Results:");
        ButtonGroup cacheResultsButtonGroup = new ButtonGroup();

        cacheResultsYesRadio = new MirthRadioButton("Yes");
        cacheResultsYesRadio.setBackground(getBackground());
        cacheResultsYesRadio.addActionListener(evt -> cacheResultsYesButtonActionPerformed());
        cacheResultsButtonGroup.add(cacheResultsYesRadio);

        cacheResultsNoRadio = new MirthRadioButton("No");
        cacheResultsNoRadio.setBackground(getBackground());
        cacheResultsNoRadio.addActionListener(evt -> cacheResultsNoButtonActionPerformed());
        cacheResultsButtonGroup.add(cacheResultsNoRadio);

        fetchSizeLabel = new JLabel("Fetch Size:");
        fetchSizeField = new MirthTextField();
        fetchSizeField.setDocument(new MirthFieldConstraints(9, false, false, true));

        retryCountLabel = new JLabel("# of Retries on Error:");
        retryCountField = new MirthTextField();
        retryCountField.setDocument(new MirthFieldConstraints(0, false, false, true));

        retryIntervalLabel = new JLabel("Retry Interval (ms):");
        retryIntervalField = new MirthTextField();
        retryIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));

        encodingLabel = new JLabel("Encoding:");
        encodingComboBox = new MirthComboBox();
        parent.setupCharsetEncodingForConnector(encodingComboBox);

        generateLabel = new JLabel("Generate:");

        generateConnectionButton = new JButton("Connection");
        generateConnectionButton.addActionListener(evt -> generateConnectionActionPerformed());

        generateSelectButton = new JButton("Select");
        generateSelectButton.addActionListener(evt -> generateSelectActionPerformed());

        selectSQLLabel = new JLabel("JavaScript:");

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {}

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }
        };

        selectSQLTextPane = new MirthRTextScrollPane(ContextType.SOURCE_RECEIVER, true);
        selectSQLTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        selectSQLTextPane.setBorder(BorderFactory.createEtchedBorder());
        selectSQLTextPane.getDocument().addDocumentListener(documentListener);

        runPostProcessSQLLabel = new JLabel("Run Post-Process Script:");
        ButtonGroup postProcessSQLButtonGroup = new ButtonGroup();

        runPostProcessSQLNeverRadio = new MirthRadioButton("Never");
        runPostProcessSQLNeverRadio.setBackground(getBackground());
        runPostProcessSQLNeverRadio.addActionListener(evt -> updateNeverActionPerformed());
        postProcessSQLButtonGroup.add(runPostProcessSQLNeverRadio);

        runPostProcessSQLEachRadio = new MirthRadioButton("After each message");
        runPostProcessSQLEachRadio.setBackground(getBackground());
        runPostProcessSQLEachRadio.addActionListener(evt -> updateEachActionPerformed());
        postProcessSQLButtonGroup.add(runPostProcessSQLEachRadio);

        runPostProcessSQLOnceRadio = new MirthRadioButton("Once after all messages");
        runPostProcessSQLOnceRadio.setBackground(getBackground());
        runPostProcessSQLOnceRadio.addActionListener(evt -> updateOnceActionPerformed());
        postProcessSQLButtonGroup.add(runPostProcessSQLOnceRadio);

        generatePostProcessSQLLabel = new JLabel("Generate:");

        generatePostProcessSQLConnectionButton = new JButton("Connection");
        generatePostProcessSQLConnectionButton.addActionListener(evt -> generateUpdateConnectionActionPerformed());

        generatePostProcessSQLUpdateButton = new JButton("Update");
        generatePostProcessSQLUpdateButton.addActionListener(evt -> generateUpdateUpdateActionPerformed());

        postProcessSQLLabel = new JLabel("JavaScript:");
        postProcessSQLTextPane = new MirthRTextScrollPane(ContextType.SOURCE_RECEIVER, true);
        postProcessSQLTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        postProcessSQLTextPane.setBorder(BorderFactory.createEtchedBorder());
        postProcessSQLTextPane.getDocument().addDocumentListener(documentListener);

        dbVarList = new MirthVariableList();
        dbVarScrollPane = new JScrollPane(dbVarList);
    }

    private void initToolTips() {
        driverComboBox.setToolTipText("Specifies the type of database driver to use to connect to the database.");
        driverField.setToolTipText("The fully-qualified class name of the JDBC driver to use to connect to the database.");
        urlField.setToolTipText("<html>The JDBC URL to connect to the database. This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");
        usernameField.setToolTipText("<html>The user name to connect to the database. This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");
        passwordField.setToolTipText("<html>The password to connect to the database. This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");
        useJavaScriptYesRadio.setToolTipText("<html>Implement JavaScript code using JDBC to get the messages to be processed and mark messages in the database as processed.</html>");
        useJavaScriptNoRadio.setToolTipText("<html>Specify the SQL statements to get messages to be processed and mark messages in the database as processed.</html>");
        keepConnectionOpenYesRadio.setToolTipText("<html>Re-use the same database connection each time the select query is executed.</html>");
        keepConnectionOpenNoRadio.setToolTipText("<html>Close the database connection after selected messages have finished processing.</html>");

        String toolTipText = "<html>If enabled, all rows returned in the query will be<br/>aggregated into a single XML message. Note that all rows<br/>will be read into memory at once, so use this with caution.</html>";
        aggregateResultsYesRadio.setToolTipText(toolTipText);
        aggregateResultsNoRadio.setToolTipText(toolTipText);

        cacheResultsYesRadio.setToolTipText("<html>Cache the entire result set in memory prior to processing messages.</html>");
        cacheResultsNoRadio.setToolTipText("<html>Do not cache the entire result set in memory prior to processing messages.</html>");
        fetchSizeField.setToolTipText("<html>The JDBC ResultSet fetch size to be used when fetching results from the current cursor position.</html>");
        retryCountField.setToolTipText("<html>The number of times to retry executing the statement or script if an error occurs.</html>");
        retryIntervalField.setToolTipText("<html>The amount of time that should elapse between retry attempts.</html>");
        encodingComboBox.setToolTipText("<html>Select the character set encoding used by the source database,<br/>or select Default to use the default character set encoding for the JVM running Mirth Connect.</html>");
        generateConnectionButton.setToolTipText("<html>If \"Yes\" is selected for Use JavaScript, this button is enabled.<br>When clicked, it inserts boilerplate Connection construction code into the JavaScript control at the current caret location.</html>");
        generateSelectButton.setToolTipText("<html>Opens a window to assist in building a select query to select records from the database specified in the URL above.</html>");
        runPostProcessSQLLabel.setToolTipText("<html>When using a database reader, it is usually necessary to execute a separate SQL statement<br>to mark the message that was just fetched as processed, so it will not be fetched again the next time a poll occurs.</html>");
        runPostProcessSQLNeverRadio.setToolTipText("<html>Do not run the post-process statement/script.</html>");
        runPostProcessSQLEachRadio.setToolTipText("<html>Run the post-process statement/script after each message finishes processing.</html>");
        runPostProcessSQLOnceRadio.setToolTipText("<html>Run the post-process statement/script only after all messages have finished processing.</html>");
        generatePostProcessSQLConnectionButton.setToolTipText("<html>This button is enabled when using JavaScript and a post-process script.<br>When clicked, it inserts boilerplate Connection construction code into the post-process JavaScript control at the current caret location.</html>");
        generatePostProcessSQLUpdateButton.setToolTipText("<html>Opens a window to assist in building an update query to update records in the database specified in the URL above.<br/>(Only enabled if a post-process statement/script is enabled)</html>");
        dbVarList.setToolTipText("<html>This list is populated with mappings based on the select statement in the JavaScript or SQL editor.<br>These mappings can dragged into the post-process JavaScript or SQL editors.</html>");
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 6", "[]12[grow]", "[][][][][][][][][][][][][sgy][][sgy]"));

        add(driverLabel, "right");
        add(driverComboBox, "split 3");
        add(driverField, "w 200!");
        add(manageDriversButton, "h 22!, w 22!");
        add(urlLabel, "newline, right");
        add(urlField, "w 318!, split 2");
        add(insertURLTemplateButton);
        add(usernameLabel, "newline, right");
        add(usernameField, "w 121!");
        add(passwordLabel, "newline, right");
        add(passwordField, "w 121!");
        add(useJavaScriptLabel, "newline, right");
        add(useJavaScriptYesRadio, "split");
        add(useJavaScriptNoRadio);
        add(keepConnectionOpenLabel, "newline, right");
        add(keepConnectionOpenYesRadio, "split");
        add(keepConnectionOpenNoRadio);
        add(aggregateResultsLabel, "newline, right");
        add(aggregateResultsYesRadio, "split");
        add(aggregateResultsNoRadio);
        add(cacheResultsLabel, "newline, right");
        add(cacheResultsYesRadio, "split");
        add(cacheResultsNoRadio);
        add(fetchSizeLabel, "newline, right");
        add(fetchSizeField, "w 121!");
        add(retryCountLabel, "newline, right");
        add(retryCountField, "w 121!");
        add(retryIntervalLabel, "newline, right");
        add(retryIntervalField, "w 121!");
        add(encodingLabel, "newline, right");
        add(encodingComboBox, "split 1, left");
        add(generateLabel, "split 3, sx, right, gapafter 12");
        add(generateConnectionButton, "w 67!");
        add(generateSelectButton, "w 67!");
        add(selectSQLLabel, "newline, top, right");
        add(selectSQLTextPane, "sx, grow, pushy, w :400, h :100");
        add(runPostProcessSQLLabel, "newline, right");
        add(runPostProcessSQLNeverRadio, "split 3");
        add(runPostProcessSQLEachRadio);
        add(runPostProcessSQLOnceRadio);
        add(generatePostProcessSQLLabel, "sx, split 3, right, gapafter 12");
        add(generatePostProcessSQLConnectionButton, "w 67!");
        add(generatePostProcessSQLUpdateButton, "w 67!");
        add(postProcessSQLLabel, "newline, top, right");
        add(postProcessSQLTextPane, "grow, split, sx, pushy, w :400, h :100");
        add(dbVarScrollPane, "growy, right, w 195!");
    }

    private void retrieveDatabaseDrivers(final String selectedDriver) {
        final String workingId = parent.startWorking("Retrieving database drivers...");

        SwingWorker<List<DriverInfo>, Void> worker = new SwingWorker<List<DriverInfo>, Void>() {
            @Override
            protected List<DriverInfo> doInBackground() throws Exception {
                return parent.mirthClient.getDatabaseDrivers();
            }

            @Override
            protected void done() {
                try {
                    drivers = get();
                    boolean enabled = parent.isSaveEnabled();
                    updateDriversComboBox();
                    parent.setSaveEnabled(enabled);
                    parent.stopWorking(workingId);
                } catch (Exception e) {
                    parent.stopWorking(workingId);
                    parent.alertThrowable(parent, e, false);
                }
            }
        };

        worker.execute();
    }

    private DriverInfo getSelectedDriver() {
        DriverInfo selectedDriver = (DriverInfo) driverComboBox.getSelectedItem();
        if (selectedDriver == null) {
            selectedDriver = getSelectOneDriver();
        }
        return selectedDriver;
    }

    private DriverInfo getSelectOneDriver() {
        return new DriverInfo(DatabaseReceiverProperties.DRIVER_DEFAULT, "", "", "");
    }

    private DriverInfo getCustomDriver() {
        return new DriverInfo(DatabaseReceiverProperties.DRIVER_CUSTOM, "", "", "");
    }

    private void fixDriversList() {
        if (CollectionUtils.isEmpty(drivers)) {
            drivers = DriverInfo.getDefaultDrivers();
        }
        if (!StringUtils.equals(drivers.get(0).getName(), DatabaseReceiverProperties.DRIVER_DEFAULT)) {
            drivers.add(0, getSelectOneDriver());
        }
        if (!StringUtils.equals(drivers.get(drivers.size() - 1).getName(), DatabaseReceiverProperties.DRIVER_CUSTOM)) {
            drivers.add(getCustomDriver());
        }
    }

    private void updateDriversComboBox() {
        fixDriversList();
        driverAdjusting.set(true);
        try {
            driverComboBox.setModel(new DefaultComboBoxModel<DriverInfo>(drivers.toArray(new DriverInfo[drivers.size()])));
            updateDriverComboBoxFromField();
        } finally {
            driverAdjusting.set(false);
        }
    }

    private void updateDriverFieldFromComboBox() {
        DriverInfo driver = getSelectedDriver();
        if (!StringUtils.equals(driver.getName(), DatabaseReceiverProperties.DRIVER_CUSTOM)) {
            driverField.setText(driver.getClassName());
            driverField.setCaretPosition(0);
        }
    }

    private void updateDriverComboBoxFromField() {
        String driverClassName = driverField.getText();

        DriverInfo foundDriver = null;
        for (int i = 0; i < driverComboBox.getModel().getSize(); i++) {
            DriverInfo driver = driverComboBox.getModel().getElementAt(i);

            if (StringUtils.equals(driverClassName, driver.getClassName())) {
                foundDriver = driver;
                break;
            }

            if (CollectionUtils.isNotEmpty(driver.getAlternativeClassNames())) {
                for (String alternativeClassName : driver.getAlternativeClassNames()) {
                    if (StringUtils.equals(driverClassName, alternativeClassName)) {
                        foundDriver = driver;
                        break;
                    }
                }
                if (foundDriver != null) {
                    break;
                }
            }
        }

        if (foundDriver != null) {
            driverComboBox.setSelectedItem(foundDriver);
        } else {
            driverComboBox.setSelectedIndex(driverComboBox.getItemCount() - 1);
        }
    }

    private void generateUpdateUpdateActionPerformed() {
        showDatabaseMetaData(STATEMENT_TYPE.UPDATE_TYPE);
    }

    private void generateUpdateConnectionActionPerformed() {
        postProcessSQLTextPane.setText(generateUpdateConnectionString() + "\n\n" + postProcessSQLTextPane.getText());
        postProcessSQLTextPane.requestFocus();
        postProcessSQLTextPane.setCaretPosition(postProcessSQLTextPane.getText().lastIndexOf("\n\n", postProcessSQLTextPane.getText().length() - 3) + 1);
        parent.setSaveEnabled(true);
    }

    private void generateSelectActionPerformed() {
        showDatabaseMetaData(STATEMENT_TYPE.SELECT_TYPE);
    }

    private void useScriptNoActionPerformed() {
        selectSQLLabel.setText("SQL:");
        postProcessSQLLabel.setText("SQL:");
        runPostProcessSQLLabel.setText("Run Post-Process SQL:");
        selectSQLTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        postProcessSQLTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        selectSQLTextPane.setText("");
        postProcessSQLTextPane.setText("");

        keepConnectionOpenLabel.setEnabled(true);
        keepConnectionOpenNoRadio.setEnabled(true);
        keepConnectionOpenYesRadio.setEnabled(true);

        aggregateResultsActionPerformed(aggregateResultsYesRadio.isSelected());

        update();

        generateConnectionButton.setEnabled(false);
        generatePostProcessSQLConnectionButton.setEnabled(false);
        dbVarList.setTransferMode(TransferMode.VELOCITY);
    }

    private void useScriptYesActionPerformed() {
        selectSQLLabel.setText("JavaScript:");
        postProcessSQLLabel.setText("JavaScript:");
        runPostProcessSQLLabel.setText("Run Post-Process Script:");
        selectSQLTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        postProcessSQLTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        selectSQLTextPane.setText(generateConnectionString());
        postProcessSQLTextPane.setText(generateUpdateConnectionString());

        generateConnectionButton.setEnabled(true);

        keepConnectionOpenLabel.setEnabled(false);
        keepConnectionOpenNoRadio.setEnabled(false);
        keepConnectionOpenYesRadio.setEnabled(false);

        cacheResultsLabel.setEnabled(false);
        cacheResultsNoRadio.setEnabled(false);
        cacheResultsYesRadio.setEnabled(false);

        fetchSizeField.setEnabled(false);
        fetchSizeLabel.setEnabled(false);
        update();

        if (!runPostProcessSQLNeverRadio.isSelected()) {
            generatePostProcessSQLConnectionButton.setEnabled(true);
        }
        dbVarList.setTransferMode(TransferMode.JAVASCRIPT);
    }

    private void insertURLTemplateButtonActionPerformed() {
        if (!urlField.getText().equals("")) {
            if (!parent.alertOption(parent, "Are you sure you would like to replace your current connection URL with the template URL?")) {
                return;
            }
        }

        urlField.setText(getSelectedDriver().getTemplate());
        urlField.grabFocus();
        parent.setSaveEnabled(true);
    }

    private void cacheResultsYesButtonActionPerformed() {
        fetchSizeField.setEnabled(false);
        fetchSizeLabel.setEnabled(false);
    }

    private void cacheResultsNoButtonActionPerformed() {
        fetchSizeField.setEnabled(useJavaScriptNoRadio.isSelected());
        fetchSizeLabel.setEnabled(useJavaScriptNoRadio.isSelected());
    }

    private void updateNeverActionPerformed() {
        postProcessSQLLabel.setEnabled(false);
        postProcessSQLTextPane.setEnabled(false);
        generatePostProcessSQLConnectionButton.setEnabled(false);
        generatePostProcessSQLUpdateButton.setEnabled(false);
        generatePostProcessSQLLabel.setEnabled(false);
        dbVarList.setEnabled(false);
    }

    private void updateEachActionPerformed() {
        postProcessSQLLabel.setEnabled(true);
        postProcessSQLTextPane.setEnabled(true);

        if (useJavaScriptYesRadio.isSelected()) {
            generatePostProcessSQLConnectionButton.setEnabled(true);
            dbVarList.setEnabled(true);
        }

        generatePostProcessSQLUpdateButton.setEnabled(true);
        generatePostProcessSQLLabel.setEnabled(true);
        dbVarList.setEnabled(true);
    }

    private void updateOnceActionPerformed() {
        postProcessSQLLabel.setEnabled(true);
        postProcessSQLTextPane.setEnabled(true);

        if (useJavaScriptYesRadio.isSelected()) {
            generatePostProcessSQLConnectionButton.setEnabled(true);
            dbVarList.setEnabled(false);
        }

        generatePostProcessSQLUpdateButton.setEnabled(true);
        generatePostProcessSQLLabel.setEnabled(true);
        dbVarList.setEnabled(false);
    }

    public void showDatabaseMetaData(STATEMENT_TYPE type) {
        DatabaseReceiverProperties properties = (DatabaseReceiverProperties) getProperties();

        if (properties.getUrl().length() == 0 || properties.getDriver().equals(DatabaseReceiverProperties.DRIVER_DEFAULT)) {
            parent.alertError(parent, "A valid Driver and URL are required to perform this operation.");
        } else {
            Connector sourceConnector = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel.getSourceConnector();
            Set<String> resourceIds = PlatformUI.MIRTH_FRAME.channelEditPanel.resourceIds.get(sourceConnector.getMetaDataId()).keySet();
            new DatabaseMetadataDialog(this, type, new DatabaseConnectionInfo(properties.getDriver(), properties.getUrl(), properties.getUsername(), properties.getPassword(), "", getSelectedDriver().getSelectLimit(), resourceIds));
        }
    }

    public void setSelectText(String statement) {
        if (!useJavaScriptYesRadio.isSelected()) {
            selectSQLTextPane.setText(statement + "\n\n" + selectSQLTextPane.getText());
        } else {
            StringBuilder connectionString = new StringBuilder();
            connectionString.append("\tvar result = dbConn.executeCachedQuery(\"");
            connectionString.append(statement.replaceAll("\\n", " "));
            connectionString.append("\");\n");
            selectSQLTextPane.setSelectedText("\n" + connectionString.toString());
        }
        parent.setSaveEnabled(true);
    }

    public void setUpdateText(List<String> statements) {
        if (!useJavaScriptYesRadio.isSelected()) {
            for (String statement : statements) {
                postProcessSQLTextPane.setText(statement.replaceAll("\\?", "") + "\n\n" + postProcessSQLTextPane.getText());
            }
        } else {
            StringBuilder connectionString = new StringBuilder();
            for (String statement : statements) {
                connectionString.append("\tvar result = dbConn.executeUpdate(\"");
                connectionString.append(statement.replaceAll("\\n", " "));
                connectionString.append("\");\n");
            }
            postProcessSQLTextPane.setSelectedText("\n" + connectionString.toString());
        }

        parent.setSaveEnabled(true);
    }

    private void generateConnectionActionPerformed() {
        String connString = generateConnectionString();
        selectSQLTextPane.setText(connString + "\n\n" + selectSQLTextPane.getText());
        selectSQLTextPane.getTextArea().requestFocus();
        selectSQLTextPane.setCaretPosition(selectSQLTextPane.getText().lastIndexOf("\n\n", selectSQLTextPane.getText().length() - 3) + 1);
        parent.setSaveEnabled(true);
    }

    private String generateConnectionString() {
        StringBuilder connectionString = new StringBuilder();
        connectionString.append("var dbConn;\n");
        connectionString.append("\ntry {\n\tdbConn = DatabaseConnectionFactory.createDatabaseConnection('");
        connectionString.append(driverField.getText() + "','" + urlField.getText() + "','");
        connectionString.append(usernameField.getText() + "','" + new String(passwordField.getPassword()) + "\');\n\n\t// You may access this result below with $('column_name')\n\treturn result;\n} finally {");
        connectionString.append("\n\tif (dbConn) { \n\t\tdbConn.close();\n\t}\n}");

        return connectionString.toString();
    }

    private String generateUpdateConnectionString() {
        StringBuilder connectionString = new StringBuilder();
        if (runPostProcessSQLEachRadio.isSelected()) {
            connectionString.append("// This update script will be executed once for every result returned from the above query.\n");
        } else {
            connectionString.append("// This update script will be executed once after all results have been processed.\n");
        }
        if (aggregateResultsYesRadio.isSelected()) {
            connectionString.append("// If \"Aggregate Results\" is enabled, you have access to \"results\",\n// a List of Map objects representing all rows returned from the above query.\n");
        }
        connectionString.append("var dbConn;\n");
        connectionString.append("\ntry {\n\tdbConn = DatabaseConnectionFactory.createDatabaseConnection('");
        connectionString.append(driverField.getText() + "','" + urlField.getText() + "','");
        connectionString.append(usernameField.getText() + "','" + new String(passwordField.getPassword()) + "\');\n\n} finally {");
        connectionString.append("\n\tif (dbConn) { \n\t\tdbConn.close();\n\t}\n}");

        return connectionString.toString();
    }

    private void aggregateResultsActionPerformed(boolean aggregateResults) {
        if (aggregateResults) {
            cacheResultsYesRadio.setSelected(true);
            cacheResultsYesButtonActionPerformed();
            cacheResultsLabel.setEnabled(false);
            cacheResultsYesRadio.setEnabled(false);
            cacheResultsNoRadio.setEnabled(false);

            runPostProcessSQLEachRadio.setText("For each row");
            runPostProcessSQLEachRadio.setToolTipText("<html>Run the post-process statement/script for each row in the result set.</html>");

            runPostProcessSQLOnceRadio.setText("Once for all rows");
            runPostProcessSQLOnceRadio.setToolTipText("<html>Run the post-process statement/script only once.<br/>If JavaScript mode is used, a List of Maps representing all rows<br/>in the result set will be available as the variable \"results\".</html>");
        } else {
            cacheResultsLabel.setEnabled(useJavaScriptNoRadio.isSelected());
            cacheResultsYesRadio.setEnabled(useJavaScriptNoRadio.isSelected());
            cacheResultsNoRadio.setEnabled(useJavaScriptNoRadio.isSelected());
            if (cacheResultsYesRadio.isSelected()) {
                cacheResultsYesButtonActionPerformed();
            } else {
                cacheResultsNoButtonActionPerformed();
            }

            runPostProcessSQLEachRadio.setText("After each message");
            runPostProcessSQLEachRadio.setToolTipText("<html>Run the post-process statement/script after each message finishes processing.</html>");

            runPostProcessSQLOnceRadio.setText("Once after all messages");
            runPostProcessSQLOnceRadio.setToolTipText("<html>Run the post-process statement/script only after all messages have finished processing.</html>");
        }
    }

    private JLabel driverLabel;
    private MirthComboBox<DriverInfo> driverComboBox;
    private MirthTextField driverField;
    private JButton manageDriversButton;
    private JLabel urlLabel;
    private MirthTextField urlField;
    private JButton insertURLTemplateButton;
    private JLabel usernameLabel;
    private MirthTextField usernameField;
    private JLabel passwordLabel;
    private MirthPasswordField passwordField;
    private JLabel useJavaScriptLabel;
    private MirthRadioButton useJavaScriptYesRadio;
    private MirthRadioButton useJavaScriptNoRadio;
    private JLabel keepConnectionOpenLabel;
    private MirthRadioButton keepConnectionOpenYesRadio;
    private MirthRadioButton keepConnectionOpenNoRadio;
    private JLabel aggregateResultsLabel;
    private JRadioButton aggregateResultsYesRadio;
    private JRadioButton aggregateResultsNoRadio;
    private JLabel cacheResultsLabel;
    private MirthRadioButton cacheResultsYesRadio;
    private MirthRadioButton cacheResultsNoRadio;
    private JLabel fetchSizeLabel;
    private MirthTextField fetchSizeField;
    private JLabel retryCountLabel;
    private MirthTextField retryCountField;
    private JLabel retryIntervalLabel;
    private MirthTextField retryIntervalField;
    private JLabel encodingLabel;
    private MirthComboBox encodingComboBox;
    private JLabel generateLabel;
    private JButton generateConnectionButton;
    private JButton generateSelectButton;
    private JLabel selectSQLLabel;
    private MirthRTextScrollPane selectSQLTextPane;
    private JLabel runPostProcessSQLLabel;
    private MirthRadioButton runPostProcessSQLNeverRadio;
    private MirthRadioButton runPostProcessSQLEachRadio;
    private MirthRadioButton runPostProcessSQLOnceRadio;
    private JLabel generatePostProcessSQLLabel;
    private JButton generatePostProcessSQLConnectionButton;
    private JButton generatePostProcessSQLUpdateButton;
    private JLabel postProcessSQLLabel;
    private MirthRTextScrollPane postProcessSQLTextPane;
    private MirthVariableList dbVarList;
    private JScrollPane dbVarScrollPane;
}