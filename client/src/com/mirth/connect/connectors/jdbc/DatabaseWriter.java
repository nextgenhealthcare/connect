/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.VariableListHandler.TransferMode;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthPasswordField;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.connectors.jdbc.DatabaseMetadataDialog.STATEMENT_TYPE;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.util.JavaScriptSharedUtil;

public class DatabaseWriter extends ConnectorSettingsPanel {

    private List<DriverInfo> drivers;
    private Frame parent;
    private AtomicBoolean driverAdjusting = new AtomicBoolean(false);

    public DatabaseWriter() {
        this.parent = PlatformUI.MIRTH_FRAME;

        initComponents();
        initToolTips();
        initLayout();
    }

    @Override
    public String getConnectorName() {
        return new DatabaseDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        DatabaseDispatcherProperties properties = new DatabaseDispatcherProperties();

        properties.setDriver(driverField.getText());
        properties.setUrl(urlField.getText());
        properties.setUsername(usernameField.getText());
        properties.setPassword(new String(passwordField.getPassword()));

        properties.setUseScript(useJavaScriptYesRadio.isSelected());
        properties.setQuery(sqlTextPane.getText());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        DatabaseDispatcherProperties props = (DatabaseDispatcherProperties) properties;

        boolean enabled = parent.isSaveEnabled();

        if (StringUtils.equals(props.getDriver(), DatabaseReceiverProperties.DRIVER_DEFAULT)) {
            driverField.setText("");
        } else {
            driverField.setText(props.getDriver());
        }
        updateDriverComboBoxFromField();
        retrieveDatabaseDrivers(props.getDriver());

        parent.setSaveEnabled(enabled);

        urlField.setText(props.getUrl());
        usernameField.setText(props.getUsername());
        passwordField.setText(props.getPassword());

        if (props.isUseScript()) {
            useJavaScriptYesRadio.setSelected(true);
            useJavaScriptYesActionPerformed();

        } else {
            useJavaScriptNoRadio.setSelected(true);
            useJavaScriptNoActionPerformed();
        }

        sqlTextPane.setText(props.getQuery());
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new DatabaseDispatcherProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        DatabaseDispatcherProperties props = (DatabaseDispatcherProperties) properties;

        boolean valid = true;

        if (!props.isUseScript() && props.getUrl().length() == 0) {
            valid = false;
            if (highlight) {
                urlField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getQuery().length() == 0) {
            valid = false;
            if (highlight) {
                sqlTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (StringUtils.isBlank(props.getDriver()) || props.getDriver().equals(DatabaseDispatcherProperties.DRIVER_DEFAULT)) {
            valid = false;
            if (highlight) {
                driverComboBox.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public TransferMode getTransferMode() {
        return ((DatabaseDispatcherProperties) getProperties()).isUseScript() ? TransferMode.JAVASCRIPT : TransferMode.VELOCITY;
    }

    @Override
    public void resetInvalidProperties() {
        urlField.setBackground(null);
        sqlTextPane.setBackground(null);
        driverComboBox.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        sqlTextPane.updateDisplayOptions();
    }

    @Override
    public String doValidate(ConnectorProperties properties, boolean highlight) {
        DatabaseDispatcherProperties props = (DatabaseDispatcherProperties) properties;

        String error = null;

        if (props.isUseScript()) {
            String script = props.getQuery();

            if (script.length() != 0) {
                Context context = JavaScriptSharedUtil.getGlobalContextForValidation();
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
        }

        return error;
    }

    @Override
    public List<String> getScripts(ConnectorProperties properties) {
        DatabaseDispatcherProperties props = (DatabaseDispatcherProperties) properties;

        List<String> scripts = new ArrayList<String>();

        if (props.isUseScript()) {
            scripts.add(props.getQuery());
        }

        return scripts;
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

        useJavaScriptRadioPanel = new JPanel();
        useJavaScriptRadioPanel.setBackground(getBackground());

        ButtonGroup useJavaScriptButtonGroup = new ButtonGroup();

        useJavaScriptYesRadio = new MirthRadioButton("Yes");
        useJavaScriptYesRadio.setBackground(getBackground());
        useJavaScriptYesRadio.addActionListener(evt -> useJavaScriptYesActionPerformed());
        useJavaScriptButtonGroup.add(useJavaScriptYesRadio);

        useJavaScriptNoRadio = new MirthRadioButton("No");
        useJavaScriptNoRadio.setBackground(getBackground());
        useJavaScriptNoRadio.addActionListener(evt -> useJavaScriptNoActionPerformed());
        useJavaScriptButtonGroup.add(useJavaScriptNoRadio);

        generateLabel = new JLabel("Generate:");

        generatePanel = new JPanel();
        generatePanel.setBackground(getBackground());

        generateConnectionButton = new JButton("Connection");
        generateConnectionButton.addActionListener(evt -> generateConnectionActionPerformed());

        generateInsertButton = new JButton("Insert");
        generateInsertButton.addActionListener(evt -> generateInsertActionPerformed());

        sqlLabel = new JLabel("SQL:");

        sqlTextPane = new MirthRTextScrollPane(ContextType.DESTINATION_DISPATCHER, true);
        sqlTextPane.setBorder(BorderFactory.createEtchedBorder());
    }

    private void initToolTips() {
        driverComboBox.setToolTipText("Specifies the type of database driver to use to connect to the database.");
        driverField.setToolTipText("The fully-qualified class name of the JDBC driver to use to connect to the database.");
        manageDriversButton.setToolTipText("<html>Click here to view and manage the list of database JDBC drivers.<br/>Any changes will require re-saving and redeploying the channel.</html>");
        urlField.setToolTipText("<html>The JDBC URL to connect to the database.<br>This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");
        usernameField.setToolTipText("<html>The username to connect to the database.<br>This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");
        passwordField.setToolTipText("<html>The password to connect to the database.<br>This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");
        useJavaScriptYesRadio.setToolTipText("Implement JavaScript code using JDBC to insert a message into the database.");
        useJavaScriptNoRadio.setToolTipText("Specify the SQL statements to insert a message into the database.");
        generateConnectionButton.setToolTipText("<html>If \"Yes\" is selected for Use JavaScript, this button is enabled.<br>When clicked, it inserts boilerplate Connection construction code into the JavaScript control at the current caret location.</html>");
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 6", "[]12[grow]", "[][][][][][grow]"));

        add(driverLabel, "right");
        add(driverComboBox, "split 3");
        add(driverField, "w 200!");
        add(manageDriversButton, "h 22!, w 22!");
        add(urlLabel, "newline, right");
        add(urlField, "w 318!, split 2");
        add(insertURLTemplateButton);
        add(usernameLabel, "newline, right");
        add(usernameField, "w 125!");
        add(passwordLabel, "newline, right");
        add(passwordField, "w 125!");
        add(useJavaScriptLabel, "newline, right");

        generatePanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 6", "[][grow][][][]"));
        generatePanel.add(useJavaScriptYesRadio, "left");
        generatePanel.add(useJavaScriptNoRadio, "left");

        generatePanel.add(generateLabel, "right, gapafter 12");
        generatePanel.add(generateConnectionButton, "right, sg");
        generatePanel.add(generateInsertButton, "right, sg");
        add(generatePanel, "growx, pushx, right");

        add(sqlLabel, "newline, top, right");
        add(sqlTextPane, "grow, push");
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

    private void generateInsertActionPerformed() {
        showDatabaseMetaData(STATEMENT_TYPE.INSERT_TYPE);
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

    public void showDatabaseMetaData(STATEMENT_TYPE type) {
        DatabaseDispatcherProperties properties = (DatabaseDispatcherProperties) getProperties();

        if (properties.getUrl().length() == 0 || properties.getDriver().equals(DatabaseReceiverProperties.DRIVER_DEFAULT)) {
            parent.alertError(parent, "A valid Driver and URL are required to perform this operation.");
        } else {
            Connector destinationConnector = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel.getDestinationConnectors().get(PlatformUI.MIRTH_FRAME.channelEditPanel.lastModelIndex);
            Set<String> resourceIds = PlatformUI.MIRTH_FRAME.channelEditPanel.resourceIds.get(destinationConnector.getMetaDataId()).keySet();
            new DatabaseMetadataDialog(this, type, new DatabaseConnectionInfo(properties.getDriver(), properties.getUrl(), properties.getUsername(), properties.getPassword(), "", getSelectedDriver().getSelectLimit(), resourceIds));
        }
    }

    public void setInsertText(List<String> statements) {
        if (!useJavaScriptYesRadio.isSelected()) {
            for (String statement : statements) {
                sqlTextPane.setText(statement.replaceAll("\\?", "") + "\n\n" + sqlTextPane.getText());
            }
        } else {
            StringBuilder connectionString = new StringBuilder();
            for (String statement : statements) {
                connectionString.append("\tvar result = dbConn.executeUpdate(\"");
                connectionString.append(statement.replaceAll("\\n", " "));
                connectionString.append("\");\n");
            }
            sqlTextPane.setSelectedText("\n" + connectionString.toString());
        }

        parent.setSaveEnabled(true);
    }

    private String generateConnectionString() {
        StringBuilder connectionString = new StringBuilder();
        connectionString.append("var dbConn;\n");
        connectionString.append("\ntry {\n\tdbConn = DatabaseConnectionFactory.createDatabaseConnection('");
        connectionString.append(driverField.getText() + "','" + urlField.getText() + "','");
        connectionString.append(usernameField.getText() + "','" + new String(passwordField.getPassword()) + "\');\n\n} finally {");
        connectionString.append("\n\tif (dbConn) { \n\t\tdbConn.close();\n\t}\n}");

        return connectionString.toString();
    }

    private void generateConnectionActionPerformed() {
        sqlTextPane.setText(generateConnectionString() + "\n\n" + sqlTextPane.getText());
        sqlTextPane.getTextArea().requestFocus();
        sqlTextPane.setCaretPosition(sqlTextPane.getText().lastIndexOf("\n\n", sqlTextPane.getText().length() - 3) + 1);
        parent.setSaveEnabled(true);
    }

    private void useJavaScriptYesActionPerformed() {
        sqlLabel.setText("JavaScript:");
        sqlTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        sqlTextPane.setText(generateConnectionString());
        generateConnectionButton.setEnabled(true);
        parent.channelEditPanel.destinationVariableList.setTransferMode(TransferMode.JAVASCRIPT);
    }

    private void useJavaScriptNoActionPerformed() {
        sqlLabel.setText("SQL:");
        sqlTextPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        sqlTextPane.setText("");
        generateConnectionButton.setEnabled(false);
        parent.channelEditPanel.destinationVariableList.setTransferMode(TransferMode.VELOCITY);
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
    private JPanel useJavaScriptRadioPanel;
    private MirthRadioButton useJavaScriptYesRadio;
    private MirthRadioButton useJavaScriptNoRadio;
    private JPanel generatePanel;
    private JLabel generateLabel;
    private JButton generateConnectionButton;
    private JButton generateInsertButton;
    private JLabel sqlLabel;
    private MirthRTextScrollPane sqlTextPane;
}
