/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.mozilla.javascript.Context;

import com.mirth.connect.client.ui.AbstractConnectorPropertiesPanel;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthButton;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.connectors.http.HttpDispatcherProperties;
import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.InvalidConnectorPluginProperties;
import com.mirth.connect.plugins.ConnectorPropertiesPlugin;
import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties.AuthType;
import com.mirth.connect.plugins.httpauth.basic.BasicHttpAuthProperties;
import com.mirth.connect.plugins.httpauth.custom.CustomHttpAuthProperties;
import com.mirth.connect.plugins.httpauth.digest.DigestHttpAuthProperties;
import com.mirth.connect.plugins.httpauth.digest.DigestHttpAuthProperties.Algorithm;
import com.mirth.connect.plugins.httpauth.digest.DigestHttpAuthProperties.QOPMode;
import com.mirth.connect.plugins.httpauth.javascript.JavaScriptHttpAuthDialog;
import com.mirth.connect.plugins.httpauth.javascript.JavaScriptHttpAuthProperties;
import com.mirth.connect.plugins.httpauth.oauth2.OAuth2HttpAuthProperties;
import com.mirth.connect.plugins.httpauth.oauth2.OAuth2HttpAuthProperties.TokenLocation;
import com.mirth.connect.util.JavaScriptSharedUtil;

public class HttpAuthConnectorPropertiesPanel extends AbstractConnectorPropertiesPanel {

    private static final String SCRIPT_DEFAULT = "<Default Script Set>";
    private static final String SCRIPT_SET = "<Custom Script Set>";

    private AuthType selectedAuthType = AuthType.NONE;
    private boolean forceAuthTypeChange = false;
    private String jsScript = "";

    public HttpAuthConnectorPropertiesPanel() {
        initComponents();
        initLayout();
    }

    @Override
    public ConnectorPluginProperties getProperties() {
        return getProperties((AuthType) typeComboBox.getSelectedItem());
    }

    private HttpAuthConnectorPluginProperties getProperties(AuthType authType) {
        if (authType == AuthType.BASIC) {
            BasicHttpAuthProperties props = new BasicHttpAuthProperties();

            props.setRealm(basicRealmField.getText());

            for (int row = 0; row < basicCredentialsTable.getModel().getRowCount(); row++) {
                props.getCredentials().put((String) basicCredentialsTable.getModel().getValueAt(row, 0), (String) basicCredentialsTable.getModel().getValueAt(row, 1));
            }

            return props;
        } else if (authType == AuthType.DIGEST) {
            DigestHttpAuthProperties props = new DigestHttpAuthProperties();

            props.setRealm(digestRealmField.getText());

            if (digestAlgorithmMD5Radio.isSelected()) {
                props.setAlgorithms(new HashSet<Algorithm>(Arrays.asList(new Algorithm[] {
                        Algorithm.MD5 })));
            } else if (digestAlgorithmMD5SessRadio.isSelected()) {
                props.setAlgorithms(new HashSet<Algorithm>(Arrays.asList(new Algorithm[] {
                        Algorithm.MD5_SESS })));
            } else {
                props.setAlgorithms(new HashSet<Algorithm>(Arrays.asList(Algorithm.values())));
            }

            Set<QOPMode> qopModes = new HashSet<QOPMode>();
            if (digestQOPAuthCheckBox.isSelected()) {
                qopModes.add(QOPMode.AUTH);
            }
            if (digestQOPAuthIntCheckBox.isSelected()) {
                qopModes.add(QOPMode.AUTH_INT);
            }
            props.setQopModes(qopModes);

            props.setOpaque(digestOpaqueField.getText());

            for (int row = 0; row < digestCredentialsTable.getModel().getRowCount(); row++) {
                props.getCredentials().put((String) digestCredentialsTable.getModel().getValueAt(row, 0), (String) digestCredentialsTable.getModel().getValueAt(row, 1));
            }

            return props;
        } else if (authType == AuthType.JAVASCRIPT) {
            JavaScriptHttpAuthProperties props = new JavaScriptHttpAuthProperties();

            props.setScript(jsScript);

            return props;
        } else if (authType == AuthType.CUSTOM) {
            CustomHttpAuthProperties props = new CustomHttpAuthProperties();

            props.setAuthenticatorClass(customClassNameField.getText());

            for (int row = 0; row < customPropertiesTable.getModel().getRowCount(); row++) {
                props.getProperties().put((String) customPropertiesTable.getModel().getValueAt(row, 0), (String) customPropertiesTable.getModel().getValueAt(row, 1));
            }

            return props;
        } else if (authType == AuthType.OAUTH2_VERIFICATION) {
            OAuth2HttpAuthProperties props = new OAuth2HttpAuthProperties();
            props.setTokenLocation((TokenLocation) oauth2TokenLocationComboBox.getSelectedItem());
            props.setLocationKey(oauth2TokenField.getText());
            props.setVerificationURL(oauth2VerificationURLField.getText());
            if (connectorPropertiesPanel != null) {
                Set<ConnectorPluginProperties> connectorPluginProperties = new HashSet<ConnectorPluginProperties>();
                connectorPluginProperties.add(connectorPropertiesPanel.getProperties());
                props.setConnectorPluginProperties(connectorPluginProperties);
            }

            return props;
        }

        return new NoneHttpAuthProperties();
    }

    @Override
    public void setProperties(ConnectorProperties connectorProperties, ConnectorPluginProperties properties, Mode mode, String transportName) {
        forceAuthTypeChange = true;
        typeComboBox.setSelectedItem(((HttpAuthConnectorPluginProperties) properties).getAuthType());
        authTypeChanged();
        forceAuthTypeChange = false;
        setProperties(connectorProperties, properties);
    }

    private void setProperties(ConnectorProperties connectorProperties, ConnectorPluginProperties properties) {
        if (properties instanceof BasicHttpAuthProperties) {
            BasicHttpAuthProperties props = (BasicHttpAuthProperties) properties;

            basicRealmField.setText(props.getRealm());

            Object[][] data = new Object[props.getCredentials().size()][2];
            int i = 0;
            for (Entry<String, String> entry : props.getCredentials().entrySet()) {
                data[i][0] = entry.getKey();
                data[i][1] = entry.getValue();
                i++;
            }
            ((RefreshTableModel) basicCredentialsTable.getModel()).refreshDataVector(data);
        } else if (properties instanceof DigestHttpAuthProperties) {
            DigestHttpAuthProperties props = (DigestHttpAuthProperties) properties;

            digestRealmField.setText(props.getRealm());

            if (props.getAlgorithms().contains(Algorithm.MD5) && props.getAlgorithms().contains(Algorithm.MD5_SESS)) {
                digestAlgorithmBothRadio.setSelected(true);
            } else if (props.getAlgorithms().contains(Algorithm.MD5)) {
                digestAlgorithmMD5Radio.setSelected(true);
            } else if (props.getAlgorithms().contains(Algorithm.MD5_SESS)) {
                digestAlgorithmMD5SessRadio.setSelected(true);
            }

            digestQOPAuthCheckBox.setSelected(props.getQopModes().contains(QOPMode.AUTH));
            digestQOPAuthIntCheckBox.setSelected(props.getQopModes().contains(QOPMode.AUTH_INT));
            digestOpaqueField.setText(props.getOpaque());

            Object[][] data = new Object[props.getCredentials().size()][2];
            int i = 0;
            for (Entry<String, String> entry : props.getCredentials().entrySet()) {
                data[i][0] = entry.getKey();
                data[i][1] = entry.getValue();
                i++;
            }
            ((RefreshTableModel) digestCredentialsTable.getModel()).refreshDataVector(data);
        } else if (properties instanceof JavaScriptHttpAuthProperties) {
            JavaScriptHttpAuthProperties props = (JavaScriptHttpAuthProperties) properties;
            jsScript = props.getScript();
            updateJSScriptField();
        } else if (properties instanceof CustomHttpAuthProperties) {
            CustomHttpAuthProperties props = (CustomHttpAuthProperties) properties;

            customClassNameField.setText(props.getAuthenticatorClass());

            Object[][] data = new Object[props.getProperties().size()][2];
            int i = 0;
            for (Entry<String, String> entry : props.getProperties().entrySet()) {
                data[i][0] = entry.getKey();
                data[i][1] = entry.getValue();
                i++;
            }
            ((RefreshTableModel) customPropertiesTable.getModel()).refreshDataVector(data);
        } else if (properties instanceof OAuth2HttpAuthProperties) {
            OAuth2HttpAuthProperties props = (OAuth2HttpAuthProperties) properties;
            oauth2TokenLocationComboBox.setSelectedItem(props.getTokenLocation());
            oauth2TokenField.setText(props.getLocationKey());
            oauth2VerificationURLField.setText(props.getVerificationURL());
            if (connectorPropertiesPanel != null) {
                Set<ConnectorPluginProperties> connectorPluginProperties = props.getConnectorPluginProperties();
                if (CollectionUtils.isEmpty(connectorPluginProperties)) {
                    connectorPluginProperties = new HashSet<ConnectorPluginProperties>();
                    connectorPluginProperties.add(connectorPropertiesPanel.getDefaults());
                }
                ConnectorPluginProperties pluginProperties = connectorPluginProperties.iterator().next();
                if (!(pluginProperties instanceof InvalidConnectorPluginProperties)) {
                    connectorPropertiesPanel.setProperties(connectorProperties, pluginProperties, Mode.DESTINATION, new HttpDispatcherProperties().getName());
                }
            }
        }
    }

    @Override
    public ConnectorPluginProperties getDefaults() {
        return getDefaultProperties(AuthType.NONE);
    }

    private HttpAuthConnectorPluginProperties getDefaultProperties(AuthType authType) {
        if (authType == AuthType.BASIC) {
            return new BasicHttpAuthProperties();
        } else if (authType == AuthType.DIGEST) {
            return new DigestHttpAuthProperties();
        } else if (authType == AuthType.JAVASCRIPT) {
            return new JavaScriptHttpAuthProperties();
        } else if (authType == AuthType.CUSTOM) {
            return new CustomHttpAuthProperties();
        } else if (authType == AuthType.OAUTH2_VERIFICATION) {
            OAuth2HttpAuthProperties props = new OAuth2HttpAuthProperties();
            if (connectorPropertiesPanel != null) {
                Set<ConnectorPluginProperties> connectorPluginProperties = new HashSet<ConnectorPluginProperties>();
                connectorPluginProperties.add(connectorPropertiesPanel.getDefaults());
                props.setConnectorPluginProperties(connectorPluginProperties);
            }
            return props;
        }
        return new NoneHttpAuthProperties();
    }

    @Override
    public boolean checkProperties(ConnectorPluginProperties properties, Mode mode, String transportName, boolean highlight) {
        boolean valid = true;

        if (properties instanceof BasicHttpAuthProperties) {
            BasicHttpAuthProperties props = (BasicHttpAuthProperties) properties;

            if (StringUtils.isBlank(props.getRealm())) {
                valid = false;
                if (highlight) {
                    basicRealmField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (props.getCredentials().size() == 0) {
                valid = false;
            }
        } else if (properties instanceof DigestHttpAuthProperties) {
            DigestHttpAuthProperties props = (DigestHttpAuthProperties) properties;

            if (StringUtils.isBlank(props.getRealm())) {
                valid = false;
                if (highlight) {
                    digestRealmField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (props.getCredentials().size() == 0) {
                valid = false;
            }
        } else if (properties instanceof CustomHttpAuthProperties) {
            CustomHttpAuthProperties props = (CustomHttpAuthProperties) properties;

            if (StringUtils.isBlank(props.getAuthenticatorClass())) {
                valid = false;
                if (highlight) {
                    customClassNameField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        } else if (properties instanceof OAuth2HttpAuthProperties) {
            OAuth2HttpAuthProperties props = (OAuth2HttpAuthProperties) properties;

            if (StringUtils.isBlank(props.getLocationKey())) {
                valid = false;
                if (highlight) {
                    oauth2TokenField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (StringUtils.isBlank(props.getVerificationURL())) {
                valid = false;
                if (highlight) {
                    oauth2VerificationURLField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (connectorPropertiesPanel != null && props.getConnectorPluginProperties() != null) {
                for (ConnectorPluginProperties pluginProperties : props.getConnectorPluginProperties()) {
                    if (!(pluginProperties instanceof InvalidConnectorPluginProperties)) {
                        if (!connectorPropertiesPanel.checkProperties(pluginProperties, Mode.DESTINATION, new HttpDispatcherProperties().getName(), highlight)) {
                            valid = false;
                        }
                    }
                }
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        basicRealmField.setBackground(null);
        digestRealmField.setBackground(null);
        customClassNameField.setBackground(null);
        oauth2TokenField.setBackground(null);
        oauth2VerificationURLField.setBackground(null);
    }

    @Override
    public Component[][] getLayoutComponents() {
        return null;
    }

    @Override
    public void setLayoutComponentsEnabled(boolean enabled) {}

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        typeLabel = new JLabel("Authentication Type:");
        typeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        typeComboBox = new MirthComboBox();
        typeComboBox.setModel(new DefaultComboBoxModel<AuthType>(AuthType.values()));
        typeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                authTypeChanged();
            }
        });
        typeComboBox.setToolTipText("Select the type of HTTP authentication to perform for incoming requests.");

        basicRealmLabel = new JLabel("Realm:");
        basicRealmField = new MirthTextField();
        basicRealmField.setToolTipText("The protection space for this server.");
        basicCredentialsLabel = new JLabel("Credentials:");

        basicCredentialsPanel = new JPanel();
        basicCredentialsPanel.setBackground(getBackground());

        basicCredentialsTable = new MirthTable();
        basicCredentialsTable.setModel(new RefreshTableModel(new String[] { "Username",
                "Password" }, 0));
        basicCredentialsTable.setCustomEditorControls(true);
        basicCredentialsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        basicCredentialsTable.setRowSelectionAllowed(true);
        basicCredentialsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        basicCredentialsTable.setDragEnabled(false);
        basicCredentialsTable.setOpaque(true);
        basicCredentialsTable.setSortable(false);
        basicCredentialsTable.getTableHeader().setReorderingAllowed(false);
        basicCredentialsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        basicCredentialsTable.setToolTipText("<html>Username and password pairs to authenticate<br/>users with. At least one pair is required.</html>");

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            basicCredentialsTable.setHighlighters(highlighter);
        }

        CredentialsTableCellEditor basicCredentialsTableCellEditor = new CredentialsTableCellEditor(basicCredentialsTable);
        basicCredentialsTable.getColumnExt(0).setCellEditor(basicCredentialsTableCellEditor);
        basicCredentialsTable.getColumnExt(0).setToolTipText("The username to authenticate with.");
        basicCredentialsTable.getColumnExt(1).setCellRenderer(new PasswordCellRenderer());
        basicCredentialsTable.getColumnExt(1).setCellEditor(new DefaultCellEditor(new JPasswordField()));
        basicCredentialsTable.getColumnExt(1).setToolTipText("The password to authenticate with.");

        basicCredentialsTableScrollPane = new JScrollPane(basicCredentialsTable);

        basicCredentialsNewButton = new MirthButton("New");
        basicCredentialsNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int num = 0;
                String username;
                boolean found;
                do {
                    num++;
                    username = "user" + num;

                    found = false;
                    for (int row = 0; row < basicCredentialsTable.getModel().getRowCount(); row++) {
                        if (StringUtils.equals(username, (String) basicCredentialsTable.getModel().getValueAt(row, 0))) {
                            found = true;
                        }
                    }
                } while (found);

                ((DefaultTableModel) basicCredentialsTable.getModel()).addRow(new String[] {
                        username, "" });
                basicCredentialsTable.setRowSelectionInterval(basicCredentialsTable.getRowCount() - 1, basicCredentialsTable.getRowCount() - 1);
                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            }
        });

        basicCredentialsDeleteButton = new MirthButton("Delete");
        basicCredentialsDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int selectedRow = getSelectedRow(basicCredentialsTable);
                if (selectedRow >= 0) {
                    if (basicCredentialsTable.isEditing()) {
                        basicCredentialsTable.getCellEditor().cancelCellEditing();
                    }

                    ((DefaultTableModel) basicCredentialsTable.getModel()).removeRow(selectedRow);

                    int rowCount = basicCredentialsTable.getRowCount();
                    if (selectedRow < rowCount) {
                        basicCredentialsTable.setRowSelectionInterval(selectedRow, selectedRow);
                    } else if (rowCount > 0) {
                        basicCredentialsTable.setRowSelectionInterval(rowCount - 1, rowCount - 1);
                    }

                    PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                }
            }
        });
        basicCredentialsTableCellEditor.setDeleteButton(basicCredentialsDeleteButton);

        digestRealmLabel = new JLabel("Realm:");
        digestRealmField = new MirthTextField();
        digestRealmField.setToolTipText("The protection space for this server.");

        digestAlgorithmLabel = new JLabel("Algorithms:");
        ButtonGroup digestAlgorithmButtonGroup = new ButtonGroup();
        String toolTipText = "<html>Specifies the digest algorithms supported by this server.<br/><b>&nbsp;- MD5:</b> The security data A1 will contain the username, realm, and password.<br/><b>&nbsp;- MD5-sess:</b> The security data A1 will also contain the server and client nonces.</html>";

        digestAlgorithmMD5Radio = new MirthRadioButton(Algorithm.MD5.toString());
        digestAlgorithmMD5Radio.setBackground(getBackground());
        digestAlgorithmMD5Radio.setToolTipText(toolTipText);
        digestAlgorithmButtonGroup.add(digestAlgorithmMD5Radio);

        digestAlgorithmMD5SessRadio = new MirthRadioButton(Algorithm.MD5_SESS.toString());
        digestAlgorithmMD5SessRadio.setBackground(getBackground());
        digestAlgorithmMD5SessRadio.setToolTipText(toolTipText);
        digestAlgorithmButtonGroup.add(digestAlgorithmMD5SessRadio);

        digestAlgorithmBothRadio = new MirthRadioButton("Both");
        digestAlgorithmBothRadio.setBackground(getBackground());
        digestAlgorithmBothRadio.setToolTipText(toolTipText);
        digestAlgorithmButtonGroup.add(digestAlgorithmBothRadio);

        digestQOPLabel = new JLabel("QOP Modes:");
        toolTipText = "<html>The quality of protection modes to support.<br/><b>&nbsp;- auth:</b> Regular auth with client nonce and count in the digest.<br/><b>&nbsp;- auth-int:</b> Same as auth, but also with message integrity protection enabled.</html>";

        digestQOPAuthCheckBox = new MirthCheckBox(QOPMode.AUTH.toString());
        digestQOPAuthCheckBox.setBackground(getBackground());
        digestQOPAuthCheckBox.setToolTipText(toolTipText);

        digestQOPAuthIntCheckBox = new MirthCheckBox(QOPMode.AUTH_INT.toString());
        digestQOPAuthIntCheckBox.setBackground(getBackground());
        digestQOPAuthIntCheckBox.setToolTipText(toolTipText);

        digestOpaqueLabel = new JLabel("Opaque:");
        digestOpaqueField = new MirthTextField();
        digestOpaqueField.setToolTipText("A string of data that should be returned by the client unchanged.");
        digestCredentialsLabel = new JLabel("Credentials:");

        digestCredentialsPanel = new JPanel();
        digestCredentialsPanel.setBackground(getBackground());

        digestCredentialsTable = new MirthTable();
        digestCredentialsTable.setModel(new RefreshTableModel(new String[] { "Username",
                "Password" }, 0));
        digestCredentialsTable.setCustomEditorControls(true);
        digestCredentialsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        digestCredentialsTable.setRowSelectionAllowed(true);
        digestCredentialsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        digestCredentialsTable.setDragEnabled(false);
        digestCredentialsTable.setOpaque(true);
        digestCredentialsTable.setSortable(false);
        digestCredentialsTable.getTableHeader().setReorderingAllowed(false);
        digestCredentialsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        digestCredentialsTable.setToolTipText("<html>Username and password pairs to authenticate<br/>users with. At least one pair is required.</html>");

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            digestCredentialsTable.setHighlighters(highlighter);
        }

        CredentialsTableCellEditor digestCredentialsTableCellEditor = new CredentialsTableCellEditor(digestCredentialsTable);
        digestCredentialsTable.getColumnExt(0).setCellEditor(digestCredentialsTableCellEditor);
        digestCredentialsTable.getColumnExt(0).setToolTipText("The username to authenticate with.");
        digestCredentialsTable.getColumnExt(1).setCellRenderer(new PasswordCellRenderer());
        digestCredentialsTable.getColumnExt(1).setCellEditor(new DefaultCellEditor(new JPasswordField()));
        digestCredentialsTable.getColumnExt(1).setToolTipText("The password to authenticate with.");

        digestCredentialsTableScrollPane = new JScrollPane(digestCredentialsTable);

        digestCredentialsNewButton = new MirthButton("New");
        digestCredentialsNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int num = 0;
                String username;
                boolean found;
                do {
                    num++;
                    username = "user" + num;

                    found = false;
                    for (int row = 0; row < digestCredentialsTable.getModel().getRowCount(); row++) {
                        if (StringUtils.equals(username, (String) digestCredentialsTable.getModel().getValueAt(row, 0))) {
                            found = true;
                        }
                    }
                } while (found);

                ((DefaultTableModel) digestCredentialsTable.getModel()).addRow(new String[] {
                        username, "" });
                digestCredentialsTable.setRowSelectionInterval(digestCredentialsTable.getRowCount() - 1, digestCredentialsTable.getRowCount() - 1);
                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            }
        });

        digestCredentialsDeleteButton = new MirthButton("Delete");
        digestCredentialsDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int selectedRow = getSelectedRow(digestCredentialsTable);
                if (selectedRow >= 0) {
                    if (digestCredentialsTable.isEditing()) {
                        digestCredentialsTable.getCellEditor().cancelCellEditing();
                    }

                    ((DefaultTableModel) digestCredentialsTable.getModel()).removeRow(selectedRow);

                    int rowCount = digestCredentialsTable.getRowCount();
                    if (selectedRow < rowCount) {
                        digestCredentialsTable.setRowSelectionInterval(selectedRow, selectedRow);
                    } else if (rowCount > 0) {
                        digestCredentialsTable.setRowSelectionInterval(rowCount - 1, rowCount - 1);
                    }

                    PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                }
            }
        });
        digestCredentialsTableCellEditor.setDeleteButton(digestCredentialsDeleteButton);

        jsScriptLabel = new JLabel("Script:");
        jsScriptField = new JTextField();
        jsScriptField.setEditable(false);
        jsScriptField.setBackground(getBackground());
        jsScriptField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jsScriptField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                JavaScriptHttpAuthDialog dialog = new JavaScriptHttpAuthDialog(PlatformUI.MIRTH_FRAME, jsScript);
                if (dialog.wasSaved()) {
                    PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                    jsScript = dialog.getScript();
                    updateJSScriptField();
                }
            }
        });
        jsScriptField.setToolTipText("<html>Click here to open the JavaScript editor dialog.<br/>The return value of this script is used to accept or reject requests.</html>");

        customClassNameLabel = new JLabel("Class Name:");
        customClassNameField = new MirthTextField();
        customClassNameField.setToolTipText("The fully-qualified Java class name of the Authenticator class to use.");
        customPropertiesLabel = new JLabel("Properties:");

        customPropertiesPanel = new JPanel();
        customPropertiesPanel.setBackground(getBackground());

        customPropertiesTable = new MirthTable();
        customPropertiesTable.setModel(new RefreshTableModel(new String[] { "Name", "Value" }, 0));
        customPropertiesTable.setCustomEditorControls(true);
        customPropertiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customPropertiesTable.setRowSelectionAllowed(true);
        customPropertiesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        customPropertiesTable.setDragEnabled(false);
        customPropertiesTable.setOpaque(true);
        customPropertiesTable.setSortable(false);
        customPropertiesTable.getTableHeader().setReorderingAllowed(false);
        customPropertiesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        customPropertiesTable.setToolTipText("Optional properties to pass into the Authenticator class when it is instantiated.");

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            customPropertiesTable.setHighlighters(highlighter);
        }

        CredentialsTableCellEditor customPropertiesTableCellEditor = new CredentialsTableCellEditor(customPropertiesTable);
        customPropertiesTable.getColumnExt(0).setCellEditor(customPropertiesTableCellEditor);
        customPropertiesTable.getColumnExt(0).setToolTipText("The name of the property to include.");
        customPropertiesTable.getColumnExt(1).setToolTipText("The value of the property to include.");

        customPropertiesTableScrollPane = new JScrollPane(customPropertiesTable);

        customPropertiesNewButton = new MirthButton("New");
        customPropertiesNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int num = 0;
                String name;
                boolean found;
                do {
                    num++;
                    name = "Property " + num;

                    found = false;
                    for (int row = 0; row < customPropertiesTable.getModel().getRowCount(); row++) {
                        if (StringUtils.equals(name, (String) customPropertiesTable.getModel().getValueAt(row, 0))) {
                            found = true;
                        }
                    }
                } while (found);

                ((DefaultTableModel) customPropertiesTable.getModel()).addRow(new String[] { name,
                        "" });
                customPropertiesTable.setRowSelectionInterval(customPropertiesTable.getRowCount() - 1, customPropertiesTable.getRowCount() - 1);
                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            }
        });

        customPropertiesDeleteButton = new MirthButton("Delete");
        customPropertiesDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int selectedRow = getSelectedRow(customPropertiesTable);
                if (selectedRow >= 0) {
                    if (customPropertiesTable.isEditing()) {
                        customPropertiesTable.getCellEditor().cancelCellEditing();
                    }

                    ((DefaultTableModel) customPropertiesTable.getModel()).removeRow(selectedRow);

                    int rowCount = customPropertiesTable.getRowCount();
                    if (selectedRow < rowCount) {
                        customPropertiesTable.setRowSelectionInterval(selectedRow, selectedRow);
                    } else if (rowCount > 0) {
                        customPropertiesTable.setRowSelectionInterval(rowCount - 1, rowCount - 1);
                    }

                    PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                }
            }
        });
        customPropertiesTableCellEditor.setDeleteButton(customPropertiesDeleteButton);

        oauth2TokenLabel = new JLabel("Access Token Location:");

        oauth2TokenLocationComboBox = new MirthComboBox();
        oauth2TokenLocationComboBox.setModel(new DefaultComboBoxModel<TokenLocation>(TokenLocation.values()));
        oauth2TokenLocationComboBox.setToolTipText("Determines where the access token is located in client requests.");

        oauth2TokenField = new MirthTextField();
        oauth2TokenField.setToolTipText("The header or query parameter to pass along with the verification request.");

        oauth2VerificationURLLabel = new JLabel("Verification URL:");
        oauth2VerificationURLField = new MirthTextField();
        oauth2VerificationURLField.setToolTipText("<html>The HTTP URL to perform a GET request to for access<br/>token verification. If the response code is >= 400,<br/>the authentication attempt is rejected by the server.</html>");

        for (ConnectorPropertiesPlugin connectorPropertiesPlugin : LoadedExtensions.getInstance().getConnectorPropertiesPlugins().values()) {
            if (connectorPropertiesPlugin.isConnectorPropertiesPluginSupported(HttpAuthConnectorPluginProperties.PLUGIN_POINT)) {
                connectorPropertiesPanel = connectorPropertiesPlugin.getConnectorPropertiesPanel();
            }
        }
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3", "[]12[]", ""));

        add(typeLabel, "right, w 115:");
        add(typeComboBox);

        add(basicRealmLabel, "newline, right");
        add(basicRealmField, "w 164!");

        add(basicCredentialsLabel, "newline, top, right");

        basicCredentialsPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3"));
        basicCredentialsPanel.add(basicCredentialsTableScrollPane, "grow, sy, h 80!, w 300!");
        basicCredentialsPanel.add(basicCredentialsNewButton, "top, w 44!");
        basicCredentialsPanel.add(basicCredentialsDeleteButton, "newline, top, w 44!");
        add(basicCredentialsPanel);

        add(digestRealmLabel, "newline, right");
        add(digestRealmField, "w 164!");

        add(digestAlgorithmLabel, "newline, right");
        add(digestAlgorithmMD5Radio, "split 3");
        add(digestAlgorithmMD5SessRadio);
        add(digestAlgorithmBothRadio);

        add(digestQOPLabel, "newline, right");
        add(digestQOPAuthCheckBox, "split 2");
        add(digestQOPAuthIntCheckBox);

        add(digestOpaqueLabel, "newline, right");
        add(digestOpaqueField, "w 200!");

        add(digestCredentialsLabel, "newline, top, right");

        digestCredentialsPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3"));
        digestCredentialsPanel.add(digestCredentialsTableScrollPane, "grow, sy, h 80!, w 300!");
        digestCredentialsPanel.add(digestCredentialsNewButton, "top, w 44!");
        digestCredentialsPanel.add(digestCredentialsDeleteButton, "newline, top, w 44!");
        add(digestCredentialsPanel);

        add(jsScriptLabel, "newline, right");
        add(jsScriptField, "w 164!");

        add(customClassNameLabel, "newline, right");
        add(customClassNameField, "w 300!");

        add(customPropertiesLabel, "newline, top, right");

        customPropertiesPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3"));
        customPropertiesPanel.add(customPropertiesTableScrollPane, "grow, sy, h 80!, w 300!");
        customPropertiesPanel.add(customPropertiesNewButton, "top, w 44!");
        customPropertiesPanel.add(customPropertiesDeleteButton, "newline, top, w 44!");
        add(customPropertiesPanel);

        add(oauth2TokenLabel, "newline, right");
        add(oauth2TokenLocationComboBox, "split 2");
        add(oauth2TokenField, "w 100!");

        add(oauth2VerificationURLLabel, "newline, right");
        add(oauth2VerificationURLField, "w 300!");

        if (connectorPropertiesPanel != null && connectorPropertiesPanel.getLayoutComponents() != null) {
            for (Component[] row : connectorPropertiesPanel.getLayoutComponents()) {
                for (int column = 0; column < row.length; column++) {
                    if (column == 0) {
                        add(row[column], "newline, right");
                    } else {
                        add(row[column]);
                    }
                }
            }
        }
    }

    private class CredentialsTableCellEditor extends TextFieldCellEditor {

        private JTable table;
        private JButton deleteButton;

        public CredentialsTableCellEditor(JTable table) {
            this.table = table;
        }

        public void setDeleteButton(JButton deleteButton) {
            this.deleteButton = deleteButton;
        }

        @Override
        protected boolean valueChanged(String value) {
            deleteButton.setEnabled(true);

            for (int row = 0; row < table.getModel().getRowCount(); row++) {
                if (StringUtils.equals(value, (String) table.getModel().getValueAt(row, 0))) {
                    return false;
                }
            }

            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            return true;
        }
    }

    private class PasswordCellRenderer extends JPasswordField implements TableCellRenderer {

        public PasswordCellRenderer() {
            setBorder(null);
            setBackground(null);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            setText((String) value);
            return this;
        }
    }

    private void authTypeChanged() {
        AuthType authType = (AuthType) typeComboBox.getSelectedItem();

        if (!forceAuthTypeChange) {
            if (!getProperties(selectedAuthType).equals(getDefaultProperties(selectedAuthType))) {
                if (!PlatformUI.MIRTH_FRAME.alertOkCancel(PlatformUI.MIRTH_FRAME, "The current HTTP authentication properties will be lost. Are you sure you want to continue?")) {
                    typeComboBox.setSelectedItem(selectedAuthType);
                    return;
                }
            }

            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        }

        basicRealmLabel.setVisible(false);
        basicRealmField.setVisible(false);
        basicCredentialsLabel.setVisible(false);
        basicCredentialsPanel.setVisible(false);
        jsScriptLabel.setVisible(false);
        jsScriptField.setVisible(false);
        customClassNameLabel.setVisible(false);
        customClassNameField.setVisible(false);
        customPropertiesLabel.setVisible(false);
        customPropertiesPanel.setVisible(false);
        digestRealmLabel.setVisible(false);
        digestRealmField.setVisible(false);
        digestAlgorithmLabel.setVisible(false);
        digestAlgorithmMD5Radio.setVisible(false);
        digestAlgorithmMD5SessRadio.setVisible(false);
        digestAlgorithmBothRadio.setVisible(false);
        digestQOPLabel.setVisible(false);
        digestQOPAuthCheckBox.setVisible(false);
        digestQOPAuthIntCheckBox.setVisible(false);
        digestOpaqueLabel.setVisible(false);
        digestOpaqueField.setVisible(false);
        digestCredentialsLabel.setVisible(false);
        digestCredentialsPanel.setVisible(false);
        oauth2TokenLabel.setVisible(false);
        oauth2TokenLocationComboBox.setVisible(false);
        oauth2TokenField.setVisible(false);
        oauth2VerificationURLLabel.setVisible(false);
        oauth2VerificationURLField.setVisible(false);
        if (connectorPropertiesPanel != null && connectorPropertiesPanel.getLayoutComponents() != null) {
            for (Component[] row : connectorPropertiesPanel.getLayoutComponents()) {
                for (Component column : row) {
                    column.setVisible(false);
                }
            }
        }

        if (authType == AuthType.BASIC) {
            basicRealmLabel.setVisible(true);
            basicRealmField.setVisible(true);
            basicCredentialsLabel.setVisible(true);
            basicCredentialsPanel.setVisible(true);
        } else if (authType == AuthType.DIGEST) {
            digestRealmLabel.setVisible(true);
            digestRealmField.setVisible(true);
            digestAlgorithmLabel.setVisible(true);
            digestAlgorithmMD5Radio.setVisible(true);
            digestAlgorithmMD5SessRadio.setVisible(true);
            digestAlgorithmBothRadio.setVisible(true);
            digestQOPLabel.setVisible(true);
            digestQOPAuthCheckBox.setVisible(true);
            digestQOPAuthIntCheckBox.setVisible(true);
            digestOpaqueLabel.setVisible(true);
            digestOpaqueField.setVisible(true);
            digestCredentialsLabel.setVisible(true);
            digestCredentialsPanel.setVisible(true);
        } else if (authType == AuthType.JAVASCRIPT) {
            jsScriptLabel.setVisible(true);
            jsScriptField.setVisible(true);
        } else if (authType == AuthType.CUSTOM) {
            customClassNameLabel.setVisible(true);
            customClassNameField.setVisible(true);
            customPropertiesLabel.setVisible(true);
            customPropertiesPanel.setVisible(true);
        } else if (authType == AuthType.OAUTH2_VERIFICATION) {
            oauth2TokenLabel.setVisible(true);
            oauth2TokenLocationComboBox.setVisible(true);
            oauth2TokenField.setVisible(true);
            oauth2VerificationURLLabel.setVisible(true);
            oauth2VerificationURLField.setVisible(true);
            if (connectorPropertiesPanel != null && connectorPropertiesPanel.getLayoutComponents() != null) {
                for (Component[] row : connectorPropertiesPanel.getLayoutComponents()) {
                    for (Component column : row) {
                        column.setVisible(true);
                    }
                }
            }
        }

        setProperties(connectorPanel.getDefaults(), getDefaultProperties(authType));
        selectedAuthType = authType;
    }

    private int getSelectedRow(MirthTable table) {
        if (table.isEditing()) {
            return table.getEditingRow();
        } else {
            return table.getSelectedRow();
        }
    }

    private void updateJSScriptField() {
        boolean equal = true;

        Context context = JavaScriptSharedUtil.getGlobalContextForValidation();
        try {
            try {
                String decompiledSavedScript = context.decompileScript(context.compileString("function doScript() {" + jsScript + "}", UUID.randomUUID().toString(), 1, null), 1);
                String decompiledDefaultScript = context.decompileScript(context.compileString("function doScript() {" + new JavaScriptHttpAuthProperties().getScript() + "}", UUID.randomUUID().toString(), 1, null), 1);
                equal = StringUtils.equals(decompiledSavedScript, decompiledDefaultScript);
            } catch (Exception e) {
                // If any script fails to compile for any reason, we can just assume they aren't equal.
                equal = false;
            }
        } finally {
            Context.exit();
        }

        if (equal) {
            jsScriptField.setText(SCRIPT_DEFAULT);
        } else {
            jsScriptField.setText(SCRIPT_SET);
        }
    }

    private static final ImageIcon ICON_WRENCH = new ImageIcon(Frame.class.getResource("images/wrench.png"));

    private JLabel typeLabel;
    private JComboBox typeComboBox;

    // Basic
    private JLabel basicRealmLabel;
    private JTextField basicRealmField;
    private JLabel basicCredentialsLabel;
    private JPanel basicCredentialsPanel;
    private MirthTable basicCredentialsTable;
    private JScrollPane basicCredentialsTableScrollPane;
    private JButton basicCredentialsNewButton;
    private JButton basicCredentialsDeleteButton;

    // Digest
    private JLabel digestRealmLabel;
    private JTextField digestRealmField;
    private JLabel digestAlgorithmLabel;
    private JRadioButton digestAlgorithmMD5Radio;
    private JRadioButton digestAlgorithmMD5SessRadio;
    private JRadioButton digestAlgorithmBothRadio;
    private JLabel digestQOPLabel;
    private JCheckBox digestQOPAuthCheckBox;
    private JCheckBox digestQOPAuthIntCheckBox;
    private JLabel digestOpaqueLabel;
    private JTextField digestOpaqueField;
    private JLabel digestCredentialsLabel;
    private JPanel digestCredentialsPanel;
    private MirthTable digestCredentialsTable;
    private JScrollPane digestCredentialsTableScrollPane;
    private JButton digestCredentialsNewButton;
    private JButton digestCredentialsDeleteButton;

    // JavaScript
    private JLabel jsScriptLabel;
    private JTextField jsScriptField;

    // Custom Java Class
    private JLabel customClassNameLabel;
    private JTextField customClassNameField;
    private JLabel customPropertiesLabel;
    private JPanel customPropertiesPanel;
    private MirthTable customPropertiesTable;
    private JScrollPane customPropertiesTableScrollPane;
    private JButton customPropertiesNewButton;
    private JButton customPropertiesDeleteButton;

    // OAuth 2.0 Verification
    private JLabel oauth2TokenLabel;
    private JComboBox oauth2TokenLocationComboBox;
    private JTextField oauth2TokenField;
    private JLabel oauth2VerificationURLLabel;
    private JTextField oauth2VerificationURLField;
    private AbstractConnectorPropertiesPanel connectorPropertiesPanel;
}