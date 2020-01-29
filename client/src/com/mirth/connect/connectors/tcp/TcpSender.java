/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthIconTextField;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ResponseHandler;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.plugins.BasicModeClientProvider;
import com.mirth.connect.plugins.TransmissionModeClientProvider;
import com.mirth.connect.plugins.TransmissionModePlugin;
import com.mirth.connect.util.ConnectionTestResponse;

import net.miginfocom.swing.MigLayout;

public class TcpSender extends ConnectorSettingsPanel implements ActionListener {

    private Logger logger = Logger.getLogger(this.getClass());
    private Frame parent;
    private TransmissionModeClientProvider defaultProvider;
    private TransmissionModeClientProvider transmissionModeProvider;
    private JComponent settingsPlaceHolder;
    private String selectedMode;
    private boolean modeLock = false;

    public TcpSender() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initLayout();
        
        sendTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        responseTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("Basic TCP");
        selectedMode = "Basic TCP";

        for (String pluginPointName : LoadedExtensions.getInstance().getTransmissionModePlugins().keySet()) {
            model.addElement(pluginPointName);
            if (pluginPointName.equals("MLLP")) {
                defaultProvider = LoadedExtensions.getInstance().getTransmissionModePlugins().get(pluginPointName).createProvider();
            }
        }

        transmissionModeComboBox.setModel(model);

        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    @Override
    public String getConnectorName() {
        return new TcpDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        TcpDispatcherProperties properties = new TcpDispatcherProperties();

        if (transmissionModeProvider != null) {
            properties.setTransmissionModeProperties((TransmissionModeProperties) transmissionModeProvider.getProperties());
        }
        properties.setServerMode(modeServerRadio.isSelected());
        properties.setRemoteAddress(remoteAddressField.getText());
        properties.setRemotePort(remotePortField.getText());
        properties.setOverrideLocalBinding(overrideLocalBindingYesRadio.isSelected());
        properties.setLocalAddress(localAddressField.getText());
        properties.setLocalPort(localPortField.getText());
        properties.setMaxConnections(maxConnectionsField.getText());
        properties.setSendTimeout(sendTimeoutField.getText());
        properties.setBufferSize(bufferSizeField.getText());
        properties.setKeepConnectionOpen(keepConnectionOpenYesRadio.isSelected());
        properties.setCheckRemoteHost(checkRemoteHostYesRadio.isSelected());
        properties.setResponseTimeout(responseTimeoutField.getText());
        properties.setIgnoreResponse(ignoreResponseCheckBox.isSelected());
        properties.setQueueOnResponseTimeout(queueOnResponseTimeoutYesRadio.isSelected());
        properties.setDataTypeBinary(dataTypeBinaryRadio.isSelected());
        properties.setCharsetEncoding(parent.getSelectedEncodingForConnector(charsetEncodingCombobox));
        properties.setTemplate(templateTextArea.getText());

        logger.debug("getProperties: properties=" + properties);

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        logger.debug("setProperties: properties=" + properties);
        TcpDispatcherProperties props = (TcpDispatcherProperties) properties;

        TransmissionModeProperties modeProps = props.getTransmissionModeProperties();
        String name = "Basic TCP";
        if (modeProps != null && LoadedExtensions.getInstance().getTransmissionModePlugins().containsKey(modeProps.getPluginPointName())) {
            name = modeProps.getPluginPointName();
        }

        modeLock = true;
        transmissionModeComboBox.setSelectedItem(name);
        transmissionModeComboBoxActionPerformed(null);
        modeLock = false;
        selectedMode = name;
        if (transmissionModeProvider != null) {
            transmissionModeProvider.setProperties(modeProps);
        }

        remoteAddressField.setText(props.getRemoteAddress());
        remotePortField.setText(props.getRemotePort());

        if (props.isOverrideLocalBinding()) {
            overrideLocalBindingYesRadio.setSelected(true);
            overrideLocalBindingYesRadioActionPerformed(null);
        } else {
            overrideLocalBindingNoRadio.setSelected(true);
            overrideLocalBindingNoRadioActionPerformed(null);
        }

        localAddressField.setText(props.getLocalAddress());
        localPortField.setText(props.getLocalPort());
        maxConnectionsField.setText(props.getMaxConnections());
        sendTimeoutField.setText(props.getSendTimeout());
        bufferSizeField.setText(props.getBufferSize());

        if (props.isKeepConnectionOpen()) {
            keepConnectionOpenYesRadio.setSelected(true);
            keepConnectionOpenYesRadioActionPerformed(null);
        } else {
            keepConnectionOpenNoRadio.setSelected(true);
            keepConnectionOpenNoRadioActionPerformed(null);
        }
        
        // This should be done after updating the UI for isOverrideLocalBinding and isKeepConnectionOpen
        // because it will override some of the enabling/disabling of fields that are done for those properties
        if (props.isServerMode()) {
            modeServerRadio.setSelected(true);
            modeServerRadioActionPerformed();
        } else {
            modeClientRadio.setSelected(true);
            modeClientRadioActionPerformed();
        }

        if (props.isCheckRemoteHost()) {
            checkRemoteHostYesRadio.setSelected(true);
        } else {
            checkRemoteHostNoRadio.setSelected(true);
        }

        responseTimeoutField.setText(String.valueOf(props.getResponseTimeout()));

        ignoreResponseCheckBox.setSelected(props.isIgnoreResponse());
        ignoreResponseCheckBoxActionPerformed(null);

        if (props.isQueueOnResponseTimeout()) {
            queueOnResponseTimeoutYesRadio.setSelected(true);
        } else {
            queueOnResponseTimeoutNoRadio.setSelected(true);
        }

        if (props.isDataTypeBinary()) {
            dataTypeBinaryRadio.setSelected(true);
            dataTypeBinaryRadioActionPerformed(null);
        } else {
            dataTypeASCIIRadio.setSelected(true);
            dataTypeASCIIRadioActionPerformed(null);
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharsetEncoding());

        templateTextArea.setText(props.getTemplate());
    }

    @Override
    public ConnectorProperties getDefaults() {
        TcpDispatcherProperties props = new TcpDispatcherProperties();
        if (defaultProvider != null) {
            props.setTransmissionModeProperties(defaultProvider.getDefaultProperties());
        }
        return props;
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        logger.debug("checkProperties: properties=" + properties);
        TcpDispatcherProperties props = (TcpDispatcherProperties) properties;

        boolean valid = true;

        if (transmissionModeProvider != null) {
            if (!transmissionModeProvider.checkProperties(transmissionModeProvider.getProperties(), highlight)) {
                valid = false;
            }
        }
        
        if (!props.isServerMode()) {
	        if (props.getRemoteAddress().length() <= 3) {
	            valid = false;
	            if (highlight) {
	                remoteAddressField.setBackground(UIConstants.INVALID_COLOR);
	            }
	        }
	        if (props.getRemotePort().length() == 0) {
	            valid = false;
	            if (highlight) {
	                remotePortField.setBackground(UIConstants.INVALID_COLOR);
	            }
	        }
        }
        
        if (props.isServerMode() || props.isOverrideLocalBinding()) {
            if (props.getLocalAddress().length() <= 3) {
                valid = false;
                if (highlight) {
                    localAddressField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (props.getLocalPort().length() == 0) {
                valid = false;
                if (highlight) {
                    localPortField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }
        
        if (props.isServerMode() &&
        		(props.getMaxConnections().length() == 0 || NumberUtils.toInt(props.getMaxConnections()) <= 0)) {
        	valid = false;
            if (highlight) {
                maxConnectionsField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        if (!props.isServerMode() && props.isKeepConnectionOpen()) {
            if (props.getSendTimeout().length() == 0) {
                valid = false;
                if (highlight) {
                    sendTimeoutField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }
        if (props.getBufferSize().length() == 0) {
            valid = false;
            if (highlight) {
                bufferSizeField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getResponseTimeout().length() == 0) {
            valid = false;
            if (highlight) {
                responseTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getTemplate().length() == 0) {
            valid = false;
            if (highlight) {
                templateTextArea.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        if (transmissionModeProvider != null) {
            transmissionModeProvider.resetInvalidProperties();
        }
        remoteAddressField.setBackground(null);
        remotePortField.setBackground(null);
        localAddressField.setBackground(null);
        localPortField.setBackground(null);
        decorateConnectorType();
        maxConnectionsField.setBackground(null);
        sendTimeoutField.setBackground(null);
        bufferSizeField.setBackground(null);
        responseTimeoutField.setBackground(null);
        templateTextArea.setBackground(null);
    }

    @Override
    public ConnectorTypeDecoration getConnectorTypeDecoration() {
        return new ConnectorTypeDecoration(Mode.DESTINATION);
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        if (connectorTypeDecoration != null) {
            if (modeServerRadio.isSelected()) {
                localAddressField.setIcon(connectorTypeDecoration.getIcon());
                localAddressField.setAlternateToolTipText(connectorTypeDecoration.getIconToolTipText());
                localAddressField.setIconPopupMenuComponent(connectorTypeDecoration.getIconPopupComponent());
                localAddressField.setBackground(connectorTypeDecoration.getHighlightColor());
                remoteAddressField.setIcon(null);
                remoteAddressField.setAlternateToolTipText(null);
                remoteAddressField.setIconPopupMenuComponent(null);
                remoteAddressField.setBackground(null);
            } else {
                localAddressField.setIcon(null);
                localAddressField.setAlternateToolTipText(null);
                localAddressField.setIconPopupMenuComponent(null);
                localAddressField.setBackground(null);
                remoteAddressField.setIcon(connectorTypeDecoration.getIcon());
                remoteAddressField.setAlternateToolTipText(connectorTypeDecoration.getIconToolTipText());
                remoteAddressField.setIconPopupMenuComponent(connectorTypeDecoration.getIconPopupComponent());
                remoteAddressField.setBackground(connectorTypeDecoration.getHighlightColor());
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource().equals(transmissionModeProvider)) {
            if (evt.getActionCommand().equals(TransmissionModeClientProvider.CHANGE_SAMPLE_LABEL_COMMAND)) {
                sampleLabel.setText(transmissionModeProvider.getSampleLabel());
            } else if (evt.getActionCommand().equals(TransmissionModeClientProvider.CHANGE_SAMPLE_VALUE_COMMAND)) {
                sampleValue.setText(transmissionModeProvider.getSampleValue());
            }
        }
    }

    // @formatter:off
    private void initComponents() {

        keepConnectionOpenGroup = new ButtonGroup();
        dataTypeButtonGroup = new ButtonGroup();
        overrideLocalBindingButtonGroup = new ButtonGroup();
        queueOnResponseTimeoutButtonGroup = new ButtonGroup();
        checkRemoteHostButtonGroup = new ButtonGroup();
        keepConnectionOpenLabel = new JLabel();
        bufferSizeLabel = new JLabel();
        sendTimeoutLabel = new JLabel();
        remotePortLabel = new JLabel();
        remoteAddressLabel = new JLabel();
        remotePortField = new MirthTextField();
        sendTimeoutField = new MirthTextField();
        bufferSizeField = new MirthTextField();
        keepConnectionOpenYesRadio = new MirthRadioButton();
        keepConnectionOpenNoRadio = new MirthRadioButton();
        remoteAddressField = new MirthIconTextField();
        responseTimeoutField = new MirthTextField();
        responseTimeoutLabel = new JLabel();
        charsetEncodingCombobox = new MirthComboBox();
        charsetEncodingLabel = new JLabel();
        templateLabel = new JLabel();
        templateTextArea = new MirthSyntaxTextArea();
        ignoreResponseCheckBox = new MirthCheckBox();
        dataTypeASCIIRadio = new MirthRadioButton();
        dataTypeBinaryRadio = new MirthRadioButton();
        dataTypeLabel = new JLabel();
        testConnection = new JButton();
        transmissionModeComboBox = new MirthComboBox();
        transmissionModeLabel = new JLabel();
        sampleLabel = new JLabel();
        sampleValue = new JLabel();
        localAddressLabel = new JLabel();
        localAddressField = new MirthIconTextField();
        localPortLabel = new JLabel();
        localPortField = new MirthTextField();
        overrideLocalBindingLabel = new JLabel();
        overrideLocalBindingYesRadio = new MirthRadioButton();
        overrideLocalBindingNoRadio = new MirthRadioButton();
        settingsPlaceHolder = new JPanel();
        queueOnResponseTimeoutLabel = new JLabel();
        queueOnResponseTimeoutYesRadio = new MirthRadioButton();
        queueOnResponseTimeoutNoRadio = new MirthRadioButton();
        checkRemoteHostLabel = new JLabel();
        checkRemoteHostYesRadio = new MirthRadioButton();
        checkRemoteHostNoRadio = new MirthRadioButton();

        setBackground(new Color(255, 255, 255));
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        keepConnectionOpenLabel.setText("Keep Connection Open:");

        bufferSizeLabel.setText("Buffer Size (bytes):");

        sendTimeoutLabel.setText("Send Timeout (ms):");

        remotePortLabel.setText("Remote Port:");

        remoteAddressLabel.setText("Remote Address:");

        remotePortField.setToolTipText("<html>The port on which to connect.</html>");

        sendTimeoutField.setToolTipText("<html>The number of milliseconds to keep the connection<br/>to the host open, if Keep Connection Open is enabled.<br/>If zero, the connection will be kept open indefinitely.</html>");

        bufferSizeField.setToolTipText("<html>The size, in bytes, of the buffer to be used to hold messages waiting to be sent. Generally, the default value is fine.<html>");

        keepConnectionOpenYesRadio.setBackground(new Color(255, 255, 255));
        keepConnectionOpenYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenYesRadio);
        keepConnectionOpenYesRadio.setText("Yes");
        keepConnectionOpenYesRadio.setToolTipText("<html>Select Yes to keep the connection to the host open across multiple messages.<br>Select No to immediately the close the connection to the host after sending each message.</html>");
        keepConnectionOpenYesRadio.setMargin(new Insets(0, 0, 0, 0));
        keepConnectionOpenYesRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                keepConnectionOpenYesRadioActionPerformed(evt);
            }
        });

        keepConnectionOpenNoRadio.setBackground(new Color(255, 255, 255));
        keepConnectionOpenNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenNoRadio);
        keepConnectionOpenNoRadio.setText("No");
        keepConnectionOpenNoRadio.setToolTipText("<html>Select Yes to keep the connection to the host open across multiple messages.<br>Select No to immediately the close the connection to the host after sending each message.</html>");
        keepConnectionOpenNoRadio.setMargin(new Insets(0, 0, 0, 0));
        keepConnectionOpenNoRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                keepConnectionOpenNoRadioActionPerformed(evt);
            }
        });

        remoteAddressField.setToolTipText("<html>The DNS domain name or IP address on which to connect.</html>");

        responseTimeoutField.setToolTipText("<html>The number of milliseconds the connector should wait whenever attempting to read from the remote socket.</html>");

        responseTimeoutLabel.setText("Response Timeout (ms):");

        charsetEncodingCombobox.setModel(new DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingCombobox.setToolTipText("<html>The character set encoding to use when converting the outbound message to a byte stream if Data Type Text is selected.<br>Select Default to use the default character set encoding for the JVM running the Mirth Connect server.</html>");
        charsetEncodingCombobox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        charsetEncodingLabel.setText("Encoding:");

        templateLabel.setText("Template:");

        templateTextArea.setBorder(BorderFactory.createEtchedBorder());

        ignoreResponseCheckBox.setBackground(new Color(255, 255, 255));
        ignoreResponseCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ignoreResponseCheckBox.setText("Ignore Response");
        ignoreResponseCheckBox.setToolTipText("<html>If checked, the connector will not wait for a response after sending a message.<br>If unchecked, the connector will wait for a response from the host after each message is sent.</html>");
        ignoreResponseCheckBox.setMargin(new Insets(0, 0, 0, 0));
        ignoreResponseCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ignoreResponseCheckBoxActionPerformed(evt);
            }
        });

        dataTypeASCIIRadio.setBackground(new Color(255, 255, 255));
        dataTypeASCIIRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dataTypeButtonGroup.add(dataTypeASCIIRadio);
        dataTypeASCIIRadio.setSelected(true);
        dataTypeASCIIRadio.setText("Text");
        dataTypeASCIIRadio.setToolTipText("<html>Select Binary if the outbound message is a Base64 string (will be decoded before it is sent out).<br/>Select Text if the outbound message is text (will be encoded with the specified character set encoding).</html>");
        dataTypeASCIIRadio.setMargin(new Insets(0, 0, 0, 0));
        dataTypeASCIIRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dataTypeASCIIRadioActionPerformed(evt);
            }
        });

        dataTypeBinaryRadio.setBackground(new Color(255, 255, 255));
        dataTypeBinaryRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dataTypeButtonGroup.add(dataTypeBinaryRadio);
        dataTypeBinaryRadio.setText("Binary");
        dataTypeBinaryRadio.setToolTipText("<html>Select Binary if the outbound message is a Base64 string (will be decoded before it is sent out).<br/>Select Text if the outbound message is text (will be encoded with the specified character set encoding).</html>");
        dataTypeBinaryRadio.setMargin(new Insets(0, 0, 0, 0));
        dataTypeBinaryRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dataTypeBinaryRadioActionPerformed(evt);
            }
        });

        dataTypeLabel.setText("Data Type:");

        testConnection.setText("Test Connection");
        testConnection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        transmissionModeComboBox.setModel(new DefaultComboBoxModel(new String[] { "MLLP" }));
        transmissionModeComboBox.setToolTipText("<html>Select the transmission mode to use for sending and receiving data.<br/></html>");
        transmissionModeComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                transmissionModeComboBoxActionPerformed(evt);
            }
        });

        transmissionModeLabel.setText("Transmission Mode:");
        transmissionModeLabel.setToolTipText("<html>Select the transmission mode to use for sending and receiving data.<br/></html>");

        sampleLabel.setText("Sample Frame:");

        sampleValue.setForeground(new Color(153, 153, 153));
        sampleValue.setText("<html><b>&lt;VT&gt;</b> <i>&lt;Message Data&gt;</i> <b>&lt;FS&gt;&lt;CR&gt;</b></html>");
        sampleValue.setEnabled(false);

        localAddressLabel.setText("Local Address:");

        localAddressField.setToolTipText("<html>The local address that the client socket will be bound to, if Override Local Binding is set to Yes.<br/></html>");

        localPortLabel.setText("Local Port:");

        localPortField.setToolTipText("<html>The local port that the client socket will be bound to, if Override Local Binding is set to Yes.<br/><br/>Note that if a specific (non-zero) local port is chosen, then after a socket is closed it's up to the<br/>underlying OS to release the port before the next socket creation, otherwise the bind attempt will fail.<br/></html>");

        overrideLocalBindingLabel.setText("Override Local Binding:");

        overrideLocalBindingYesRadio.setBackground(new Color(255, 255, 255));
        overrideLocalBindingYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        overrideLocalBindingButtonGroup.add(overrideLocalBindingYesRadio);
        overrideLocalBindingYesRadio.setText("Yes");
        overrideLocalBindingYesRadio.setToolTipText("<html>Select Yes to override the local address and port that the client socket will be bound to.<br/>Select No to use the default values of 0.0.0.0:0.<br/>A local port of zero (0) indicates that the OS should assign an ephemeral port automatically.<br/><br/>Note that if a specific (non-zero) local port is chosen, then after a socket is closed it's up to the<br/>underlying OS to release the port before the next socket creation, otherwise the bind attempt will fail.<br/></html>");
        overrideLocalBindingYesRadio.setMargin(new Insets(0, 0, 0, 0));
        overrideLocalBindingYesRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                overrideLocalBindingYesRadioActionPerformed(evt);
            }
        });

        overrideLocalBindingNoRadio.setBackground(new Color(255, 255, 255));
        overrideLocalBindingNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        overrideLocalBindingButtonGroup.add(overrideLocalBindingNoRadio);
        overrideLocalBindingNoRadio.setText("No");
        overrideLocalBindingNoRadio.setToolTipText("<html>Select Yes to override the local address and port that the client socket will be bound to.<br/>Select No to use the default values of 0.0.0.0:0.<br/>A local port of zero (0) indicates that the OS should assign an ephemeral port automatically.<br/><br/>Note that if a specific (non-zero) local port is chosen, then after a socket is closed it's up to the<br/>underlying OS to release the port before the next socket creation, otherwise the bind attempt will fail.<br/></html>");
        overrideLocalBindingNoRadio.setMargin(new Insets(0, 0, 0, 0));
        overrideLocalBindingNoRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                overrideLocalBindingNoRadioActionPerformed(evt);
            }
        });

        queueOnResponseTimeoutLabel.setText("Queue on Response Timeout:");

        queueOnResponseTimeoutYesRadio.setBackground(new Color(255, 255, 255));
        queueOnResponseTimeoutYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        queueOnResponseTimeoutButtonGroup.add(queueOnResponseTimeoutYesRadio);
        queueOnResponseTimeoutYesRadio.setText("Yes");
        queueOnResponseTimeoutYesRadio.setToolTipText("<html>If enabled, the message is queued when a timeout occurs while waiting for a response.<br/>Otherwise, the message is set to errored when a timeout occurs while waiting for a response.<br/>This setting has no effect unless queuing is enabled for the connector.</html>");
        queueOnResponseTimeoutYesRadio.setMargin(new Insets(0, 0, 0, 0));

        queueOnResponseTimeoutNoRadio.setBackground(new Color(255, 255, 255));
        queueOnResponseTimeoutNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        queueOnResponseTimeoutButtonGroup.add(queueOnResponseTimeoutNoRadio);
        queueOnResponseTimeoutNoRadio.setText("No");
        queueOnResponseTimeoutNoRadio.setToolTipText("<html>If enabled, the message is queued when a timeout occurs while waiting for a response.<br/>Otherwise, the message is set to errored when a timeout occurs while waiting for a response.<br/>This setting has no effect unless queuing is enabled for the connector.</html>");
        queueOnResponseTimeoutNoRadio.setMargin(new Insets(0, 0, 0, 0));

        checkRemoteHostLabel.setText("Check Remote Host:");

        checkRemoteHostYesRadio.setBackground(new Color(255, 255, 255));
        checkRemoteHostYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkRemoteHostButtonGroup.add(checkRemoteHostYesRadio);
        checkRemoteHostYesRadio.setText("Yes");
        checkRemoteHostYesRadio.setToolTipText("<html>Select Yes to check if the remote host has closed the connection before each message.<br>Select No to assume the remote host has not closed the connection.<br>Checking the remote host will decrease throughput but will prevent the message from<br>erroring if the remote side closed the connection and queueing is disabled.</html>");
        checkRemoteHostYesRadio.setMargin(new Insets(0, 0, 0, 0));

        checkRemoteHostNoRadio.setBackground(new Color(255, 255, 255));
        checkRemoteHostNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkRemoteHostButtonGroup.add(checkRemoteHostNoRadio);
        checkRemoteHostNoRadio.setText("No");
        checkRemoteHostNoRadio.setToolTipText("<html>Select Yes to check if the remote host has closed the connection before each message.<br>Select No to assume the remote host has not closed the connection.<br>Checking the remote host will decrease throughput but will prevent the message from<br>erroring if the remote side closed the connection and queueing is disabled.</html>");
        checkRemoteHostNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        modeLabel = new JLabel("Mode:");
        ButtonGroup modeButtonGroup = new ButtonGroup();

        String toolTipText = "<html>Select Server to listen for connections from clients, or Client to connect to a TCP Server.</html>";
        modeServerRadio = new MirthRadioButton("Server");
        modeServerRadio.setToolTipText(toolTipText);
        modeServerRadio.setBackground(getBackground());
        modeServerRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                modeServerRadioActionPerformed();
            }
        });
        modeButtonGroup.add(modeServerRadio);

        modeClientRadio = new MirthRadioButton("Client");
        modeClientRadio.setToolTipText(toolTipText);
        modeClientRadio.setBackground(getBackground());
        modeClientRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                modeClientRadioActionPerformed();
            }
        });
        modeButtonGroup.add(modeClientRadio);
        
        maxConnectionsLabel = new JLabel("Max Connections:");
        maxConnectionsField = new MirthTextField();
        maxConnectionsField.setToolTipText("<html>The maximum number of client connections to accept.<br/>After this number has been reached, subsequent socket requests will result in a rejection.</html>");
    }
    // @formatter:on
    
    private void initLayout() {
    	setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, gap 12 6", "", "[][]4[]4[][]4[]4[][][][]4[]4[]4[]4[]4[][]"));
    	
    	add(transmissionModeLabel, "right");
        add(transmissionModeComboBox, "h 22!, split 2");
        
        settingsPlaceHolder.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        add(settingsPlaceHolder, "gapbefore 6, h 22!");
        
        add(sampleLabel, "newline, right");
        add(sampleValue, "growx, sx");
        add(modeLabel, "newline, right");
        add(modeClientRadio, "split 2");
        add(modeServerRadio);
        add(remoteAddressLabel, "newline, right");
        add(remoteAddressField, "w 200!, split 2, spanx");
        add(testConnection, "gapleft 6");
        add(remotePortLabel, "newline, right");
        add(remotePortField, "w 50!, sx");
        add(overrideLocalBindingLabel, "newline, right");
        add(overrideLocalBindingYesRadio, "split 2");
        add(overrideLocalBindingNoRadio);
        add(localAddressLabel, "newline, right");
        add(localAddressField, "w 200!, sx");
        add(localPortLabel, "newline, right");
        add(localPortField, "w 50!, sx");
        add(maxConnectionsLabel, "newline, right");
        add(maxConnectionsField, "w 75!, sx");
        add(keepConnectionOpenLabel, "newline, right");
        add(keepConnectionOpenYesRadio, "split 2");
        add(keepConnectionOpenNoRadio);
        add(checkRemoteHostLabel, "newline, right");
        add(checkRemoteHostYesRadio, "split 2");
        add(checkRemoteHostNoRadio);
        add(sendTimeoutLabel, "newline, right");
        add(sendTimeoutField, "w 75!, sx");
        add(bufferSizeLabel, "newline, right");
        add(bufferSizeField, "w 75!, sx");
        add(responseTimeoutLabel, "newline, right");
        add(responseTimeoutField, "w 75!, split 2, spanx");
        add(ignoreResponseCheckBox, "gapleft 8");
        add(queueOnResponseTimeoutLabel, "newline, right");
        add(queueOnResponseTimeoutYesRadio, "split 2");
        add(queueOnResponseTimeoutNoRadio);
        add(dataTypeLabel, "newline, right");
        add(dataTypeBinaryRadio, "split 2");
        add(dataTypeASCIIRadio);
        add(charsetEncodingLabel, "newline, right");
        add(charsetEncodingCombobox);
        add(templateLabel, "newline, aligny top, right");
        add(templateTextArea, "w 425, h 105, grow, span, push");
    }

    private void dataTypeBinaryRadioActionPerformed(ActionEvent evt) {
        charsetEncodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
    }

    private void dataTypeASCIIRadioActionPerformed(ActionEvent evt) {
        charsetEncodingLabel.setEnabled(true);
        charsetEncodingCombobox.setEnabled(true);
    }

    private void ignoreResponseCheckBoxActionPerformed(ActionEvent evt) {
        boolean selected = ignoreResponseCheckBox.isSelected();
        queueOnResponseTimeoutLabel.setEnabled(!selected);
        queueOnResponseTimeoutYesRadio.setEnabled(!selected);
        queueOnResponseTimeoutNoRadio.setEnabled(!selected);
    }

    private void testConnectionActionPerformed(ActionEvent evt) {
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void handle(Object response) {
                ConnectionTestResponse connectionTestResponse = (ConnectionTestResponse) response;
                if (connectionTestResponse == null) {
                    parent.alertError(parent, "Failed to invoke service.");
                } else if (connectionTestResponse.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
                    parent.alertInformation(parent, connectionTestResponse.getMessage());
                } else {
                    parent.alertWarning(parent, connectionTestResponse.getMessage());
                }
            }
        };

        try {
            getServlet(TcpConnectorServletInterface.class, "Testing connection...", "Failed to test connection: ", handler).testConnection(getChannelId(), getChannelName(), (TcpDispatcherProperties) getFilledProperties());
        } catch (ClientException e) {
            // Should not happen
        }
    }
    
    private void updateKeepConnectionOpenUI() {
    	if (keepConnectionOpenYesRadio.isSelected()) {
    		keepConnectionOpenYesRadioActionPerformed(null);
    	} else {
    		keepConnectionOpenNoRadioActionPerformed(null);
    	}
    }

    private void keepConnectionOpenYesRadioActionPerformed(ActionEvent evt) {
    	sendTimeoutLabel.setEnabled(true);
        sendTimeoutField.setEnabled(true);
        checkRemoteHostLabel.setEnabled(true);
        checkRemoteHostYesRadio.setEnabled(true);
        checkRemoteHostNoRadio.setEnabled(true);
    }

    private void keepConnectionOpenNoRadioActionPerformed(ActionEvent evt) {
        sendTimeoutLabel.setEnabled(false);
        sendTimeoutField.setEnabled(false);
        checkRemoteHostLabel.setEnabled(false);
        checkRemoteHostYesRadio.setEnabled(false);
        checkRemoteHostNoRadio.setEnabled(false);
    }

    private void transmissionModeComboBoxActionPerformed(ActionEvent evt) {
        String name = (String) transmissionModeComboBox.getSelectedItem();

        if (!modeLock && transmissionModeProvider != null) {
            if (!transmissionModeProvider.getDefaultProperties().equals(transmissionModeProvider.getProperties())) {
                if (JOptionPane.showConfirmDialog(parent, "Are you sure you would like to change the transmission mode and lose all of the current transmission properties?", "Select an Option", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    modeLock = true;
                    transmissionModeComboBox.setSelectedItem(selectedMode);
                    modeLock = false;
                    return;
                }
            }
        }

        selectedMode = name;
        if (name.equals("Basic TCP")) {
            transmissionModeProvider = new BasicModeClientProvider();
        } else {
            for (TransmissionModePlugin plugin : LoadedExtensions.getInstance().getTransmissionModePlugins().values()) {
                if (plugin.getPluginPointName().equals(name)) {
                    transmissionModeProvider = plugin.createProvider();
                }
            }
        }

        if (transmissionModeProvider != null) {
            transmissionModeProvider.initialize(this);
            settingsPlaceHolder.removeAll();
            settingsPlaceHolder.add(transmissionModeProvider.getSettingsComponent());
        }
    }
    
    private void modeClientRadioActionPerformed() {
        remoteAddressLabel.setEnabled(true);
        remoteAddressField.setEnabled(true);
        remotePortLabel.setEnabled(true);
        remotePortField.setEnabled(true);
        testConnection.setEnabled(true);
        overrideLocalBindingLabel.setEnabled(true);
        overrideLocalBindingYesRadio.setEnabled(true);
        overrideLocalBindingNoRadio.setEnabled(true);
        keepConnectionOpenLabel.setEnabled(true);
        keepConnectionOpenYesRadio.setEnabled(true);
        keepConnectionOpenNoRadio.setEnabled(true);
        
        updateOverrideLocalBindingUI();
        updateKeepConnectionOpenUI();
        
        maxConnectionsLabel.setEnabled(false);
        maxConnectionsField.setEnabled(false);
    }

    private void modeServerRadioActionPerformed() {    	
        remoteAddressLabel.setEnabled(false);
        remoteAddressField.setEnabled(false);
        remotePortLabel.setEnabled(false);
        remotePortField.setEnabled(false);
        testConnection.setEnabled(false);
        overrideLocalBindingLabel.setEnabled(false);
        overrideLocalBindingYesRadio.setEnabled(false);
        overrideLocalBindingNoRadio.setEnabled(false);
        keepConnectionOpenLabel.setEnabled(false);
        keepConnectionOpenYesRadio.setEnabled(false);
        keepConnectionOpenNoRadio.setEnabled(false);
        checkRemoteHostLabel.setEnabled(false);
        checkRemoteHostYesRadio.setEnabled(false);
        checkRemoteHostNoRadio.setEnabled(false);
        sendTimeoutLabel.setEnabled(false);
        sendTimeoutField.setEnabled(false);
        
        localAddressLabel.setEnabled(true);
        localAddressField.setEnabled(true);
        localPortLabel.setEnabled(true);
        localPortField.setEnabled(true);
        maxConnectionsLabel.setEnabled(true);
        maxConnectionsField.setEnabled(true);
    }
    
    private void updateOverrideLocalBindingUI() {
    	if (overrideLocalBindingYesRadio.isSelected()) {
    		overrideLocalBindingYesRadioActionPerformed(null);
    	} else {
    		overrideLocalBindingNoRadioActionPerformed(null);
    	}
    }

    private void overrideLocalBindingYesRadioActionPerformed(ActionEvent evt) {
        localAddressField.setEnabled(true);
        localAddressLabel.setEnabled(true);
        localPortField.setEnabled(true);
        localPortLabel.setEnabled(true);
    }

    private void overrideLocalBindingNoRadioActionPerformed(ActionEvent evt) {
        localAddressField.setEnabled(false);
        localAddressLabel.setEnabled(false);
        localPortField.setEnabled(false);
        localPortLabel.setEnabled(false);
    }

    private void charsetEncodingComboboxActionPerformed(ActionEvent evt) {
    }

    private JLabel modeLabel;
    public MirthRadioButton modeServerRadio;
    public MirthRadioButton modeClientRadio;
    private MirthTextField bufferSizeField;
    private MirthComboBox charsetEncodingCombobox;
    private ButtonGroup checkRemoteHostButtonGroup;
    private JLabel checkRemoteHostLabel;
    private MirthRadioButton checkRemoteHostNoRadio;
    private MirthRadioButton checkRemoteHostYesRadio;
    private MirthRadioButton dataTypeASCIIRadio;
    private MirthRadioButton dataTypeBinaryRadio;
    private ButtonGroup dataTypeButtonGroup;
    private JLabel dataTypeLabel;
    private JLabel charsetEncodingLabel;
    private MirthCheckBox ignoreResponseCheckBox;
    private JLabel bufferSizeLabel;
    private JLabel remotePortLabel;
    private JLabel remoteAddressLabel;
    private JLabel templateLabel;
    private ButtonGroup keepConnectionOpenGroup;
    private JLabel keepConnectionOpenLabel;
    private JLabel overrideLocalBindingLabel;
    private MirthRadioButton keepConnectionOpenNoRadio;
    private MirthRadioButton keepConnectionOpenYesRadio;
    private MirthIconTextField localAddressField;
    private JLabel localAddressLabel;
    private MirthTextField localPortField;
    private JLabel localPortLabel;
    private JLabel maxConnectionsLabel;
    private MirthTextField maxConnectionsField;
    private ButtonGroup overrideLocalBindingButtonGroup;
    private MirthRadioButton overrideLocalBindingNoRadio;
    private MirthRadioButton overrideLocalBindingYesRadio;
    private ButtonGroup queueOnResponseTimeoutButtonGroup;
    private JLabel queueOnResponseTimeoutLabel;
    private MirthRadioButton queueOnResponseTimeoutNoRadio;
    private MirthRadioButton queueOnResponseTimeoutYesRadio;
    private MirthIconTextField remoteAddressField;
    private MirthTextField remotePortField;
    private MirthTextField responseTimeoutField;
    private JLabel responseTimeoutLabel;
    private JLabel sampleLabel;
    private JLabel sampleValue;
    private MirthTextField sendTimeoutField;
    private JLabel sendTimeoutLabel;
    private MirthSyntaxTextArea templateTextArea;
    private JButton testConnection;
    private MirthComboBox transmissionModeComboBox;
    private JLabel transmissionModeLabel;
}
