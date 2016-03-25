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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.mirth.connect.client.ui.AbstractConnectorPropertiesPanel;
import com.mirth.connect.client.ui.CharsetEncodingInformation;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.InvalidConnectorPluginProperties;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.plugins.BasicModeClientProvider;
import com.mirth.connect.plugins.ConnectorPropertiesPlugin;
import com.mirth.connect.plugins.TransmissionModeClientProvider;
import com.mirth.connect.plugins.TransmissionModePlugin;

public class TcpListener extends ConnectorSettingsPanel implements ActionListener {

    public static final String NEW_CONNECTION_PLUGIN_PROPS = "tcpListenerNewConnectionPluginProperties";

    private Frame parent;
    private TransmissionModeClientProvider defaultProvider;
    private TransmissionModeClientProvider transmissionModeProvider;
    private String selectedMode;
    private boolean modeLock = false;

    public TcpListener() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initToolTips();
        initLayout();

        reconnectIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));
        receiveTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        maxConnectionsField.setDocument(new MirthFieldConstraints(0, false, false, true));

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
        model.addElement("Basic TCP");
        selectedMode = "Basic TCP";

        for (String pluginPointName : LoadedExtensions.getInstance().getTransmissionModePlugins().keySet()) {
            model.addElement(pluginPointName);
            if (pluginPointName.equals("MLLP")) {
                defaultProvider = LoadedExtensions.getInstance().getTransmissionModePlugins().get(pluginPointName).createProvider();
            }
        }

        transmissionModeComboBox.setModel(model);

        parent.setupCharsetEncodingForConnector(charsetEncodingComboBox);
    }

    @Override
    public String getConnectorName() {
        return new TcpReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        TcpReceiverProperties properties = new TcpReceiverProperties();

        if (transmissionModeProvider != null) {
            properties.setTransmissionModeProperties((TransmissionModeProperties) transmissionModeProvider.getProperties());
        }
        properties.setServerMode(modeServerRadio.isSelected());
        properties.setRemoteAddress(remoteAddressField.getText());
        properties.setRemotePort(remotePortField.getText());
        properties.setOverrideLocalBinding(overrideLocalBindingYesRadio.isSelected());
        properties.setReconnectInterval(reconnectIntervalField.getText());
        properties.setReceiveTimeout(receiveTimeoutField.getText());
        properties.setBufferSize(bufferSizeField.getText());
        properties.setMaxConnections(maxConnectionsField.getText());
        properties.setKeepConnectionOpen(keepConnectionOpenYesRadio.isSelected());
        properties.setCharsetEncoding(parent.getSelectedEncodingForConnector(charsetEncodingComboBox));
        properties.setDataTypeBinary(dataTypeBinaryRadio.isSelected());

        if (respondOnNewConnectionYesRadio.isSelected()) {
            properties.setRespondOnNewConnection(TcpReceiverProperties.NEW_CONNECTION);
        } else if (respondOnNewConnectionNoRadio.isSelected()) {
            properties.setRespondOnNewConnection(TcpReceiverProperties.SAME_CONNECTION);
        } else if (respondOnNewConnectionRecoveryRadio.isSelected()) {
            properties.setRespondOnNewConnection(TcpReceiverProperties.NEW_CONNECTION_ON_RECOVERY);
        }
        properties.setResponseAddress(responseAddressField.getText());
        properties.setResponsePort(responsePortField.getText());
        
        if (responseConnectorPropertiesPanel != null) {
            Set<ConnectorPluginProperties> connectorPluginProperties = new HashSet<ConnectorPluginProperties>();
            connectorPluginProperties.add(responseConnectorPropertiesPanel.getProperties());
            properties.setResponseConnectorPluginProperties(connectorPluginProperties);
        }

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        TcpReceiverProperties props = (TcpReceiverProperties) properties;

        TransmissionModeProperties modeProps = props.getTransmissionModeProperties();
        String name = "Basic TCP";
        if (modeProps != null && LoadedExtensions.getInstance().getTransmissionModePlugins().containsKey(modeProps.getPluginPointName())) {
            name = modeProps.getPluginPointName();
        }

        modeLock = true;
        transmissionModeComboBox.setSelectedItem(name);
        transmissionModeComboBoxActionPerformed();
        modeLock = false;
        selectedMode = name;
        if (transmissionModeProvider != null) {
            transmissionModeProvider.setProperties(modeProps);
        }

        if (props.isServerMode()) {
            modeServerRadio.setSelected(true);
            modeServerRadioActionPerformed();
        } else {
            modeClientRadio.setSelected(true);
            modeClientRadioActionPerformed();
        }

        remoteAddressField.setText(props.getRemoteAddress());
        remotePortField.setText(props.getRemotePort());

        if (props.isOverrideLocalBinding()) {
            overrideLocalBindingYesRadio.setSelected(true);
        } else {
            overrideLocalBindingNoRadio.setSelected(true);
        }

        reconnectIntervalField.setText(props.getReconnectInterval());
        receiveTimeoutField.setText(props.getReceiveTimeout());
        bufferSizeField.setText(props.getBufferSize());
        maxConnectionsField.setText(props.getMaxConnections());

        if (props.isKeepConnectionOpen()) {
            keepConnectionOpenYesRadio.setSelected(true);
        } else {
            keepConnectionOpenNoRadio.setSelected(true);
        }

        if (props.isDataTypeBinary()) {
            dataTypeBinaryRadio.setSelected(true);
            dataTypeBinaryRadioActionPerformed();
        } else {
            dataTypeTextRadio.setSelected(true);
            dataTypeASCIIRadioActionPerformed();
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingComboBox, props.getCharsetEncoding());
        
        if (responseConnectorPropertiesPanel != null) {
            Set<ConnectorPluginProperties> connectorPluginProperties = props.getResponseConnectorPluginProperties();
            if (CollectionUtils.isEmpty(connectorPluginProperties)) {
                connectorPluginProperties = new HashSet<ConnectorPluginProperties>();
                connectorPluginProperties.add(responseConnectorPropertiesPanel.getDefaults());
            }
            ConnectorPluginProperties pluginProperties = connectorPluginProperties.iterator().next();
            if (!(pluginProperties instanceof InvalidConnectorPluginProperties)) {
                responseConnectorPropertiesPanel.setProperties(properties, pluginProperties, Mode.DESTINATION, props.getName());
            }
        }

        switch (props.getRespondOnNewConnection()) {
            case TcpReceiverProperties.NEW_CONNECTION:
                respondOnNewConnectionYesRadio.setSelected(true);
                respondOnNewConnectionYesRadioActionPerformed();
                break;
            case TcpReceiverProperties.SAME_CONNECTION:
                respondOnNewConnectionNoRadio.setSelected(true);
                respondOnNewConnectionNoRadioActionPerformed();
                break;
            case TcpReceiverProperties.NEW_CONNECTION_ON_RECOVERY:
                respondOnNewConnectionRecoveryRadio.setSelected(true);
                respondOnNewConnectionRecoveryRadioActionPerformed();
                break;
        }

        responseAddressField.setText(props.getResponseAddress());
        responsePortField.setText(props.getResponsePort());
    }

    @Override
    public ConnectorProperties getDefaults() {
        TcpReceiverProperties props = new TcpReceiverProperties();
        if (defaultProvider != null) {
            props.setTransmissionModeProperties(defaultProvider.getDefaultProperties());
        }
        if (responseConnectorPropertiesPanel != null) {
            Set<ConnectorPluginProperties> connectorPluginProperties = new HashSet<ConnectorPluginProperties>();
            connectorPluginProperties.add(responseConnectorPropertiesPanel.getDefaults());
            props.setResponseConnectorPluginProperties(connectorPluginProperties);
        }
        return props;
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        TcpReceiverProperties props = (TcpReceiverProperties) properties;

        boolean valid = true;

        if (transmissionModeProvider != null) {
            if (!transmissionModeProvider.checkProperties(transmissionModeProvider.getProperties(), highlight)) {
                valid = false;
            }
        }
        if (!props.isServerMode()) {
            if (props.getRemoteAddress().length() == 0) {
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

            if (props.getReconnectInterval().length() == 0) {
                valid = false;
                if (highlight) {
                    reconnectIntervalField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }
        if (props.getReceiveTimeout().length() == 0) {
            valid = false;
            if (highlight) {
                receiveTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getBufferSize().length() == 0) {
            valid = false;
            if (highlight) {
                bufferSizeField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getMaxConnections().length() == 0 || NumberUtils.toInt(props.getMaxConnections()) <= 0) {
            valid = false;
            if (highlight) {
                maxConnectionsField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION || props.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION_ON_RECOVERY) {
            if (props.getResponseAddress().length() <= 3) {
                valid = false;
                if (highlight) {
                    responseAddressField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (props.getResponsePort().length() == 0) {
                valid = false;
                if (highlight) {
                    responsePortField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }
        if (responseConnectorPropertiesPanel != null && props.getResponseConnectorPluginProperties() != null) {
            for (ConnectorPluginProperties pluginProperties : props.getResponseConnectorPluginProperties()) {
                if (!(pluginProperties instanceof InvalidConnectorPluginProperties)) {
                    responseConnectorPropertiesPanel.checkProperties(pluginProperties, Mode.DESTINATION, props.getName(), highlight);
                }
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
        reconnectIntervalField.setBackground(null);
        receiveTimeoutField.setBackground(null);
        bufferSizeField.setBackground(null);
        maxConnectionsField.setBackground(null);
        responseAddressField.setBackground(null);
        responsePortField.setBackground(null);
        if (responseConnectorPropertiesPanel != null) {
            responseConnectorPropertiesPanel.resetInvalidProperties();
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

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        
        transmissionModeLabel = new JLabel("Transmission Mode:");
        transmissionModeComboBox = new MirthComboBox<String>();
        transmissionModeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                transmissionModeComboBoxActionPerformed();
            }
        });

        settingsPlaceHolder = new JPanel();
        
        sampleLabel = new JLabel("Sample Frame:");
        sampleValue = new JLabel("<html><b>&lt;VT&gt;</b> <i>&lt;Message Data&gt;</i> <b>&lt;FS&gt;&lt;CR&gt;</b></html>");
        sampleValue.setForeground(new Color(153, 153, 153));
        sampleValue.setEnabled(false);
        
        modeLabel = new JLabel("Mode:");
        ButtonGroup modeButtonGroup = new ButtonGroup();

        modeServerRadio = new MirthRadioButton("Server");
        modeServerRadio.setBackground(getBackground());
        modeServerRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                modeServerRadioActionPerformed();
            }
        });
        modeButtonGroup.add(modeServerRadio);

        modeClientRadio = new MirthRadioButton("Client");
        modeClientRadio.setBackground(getBackground());
        modeClientRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                modeClientRadioActionPerformed();
            }
        });
        modeButtonGroup.add(modeClientRadio);
        
        remoteAddressLabel = new JLabel("Remote Address:");
        remoteAddressField = new MirthTextField();
        
        remotePortLabel = new JLabel("Remote Port:");
        remotePortField = new MirthTextField();
        
        overrideLocalBindingLabel = new JLabel("Override Local Binding:");
        ButtonGroup overrideLocalBindingButtonGroup = new ButtonGroup();
        
        overrideLocalBindingYesRadio = new MirthRadioButton("Yes");
        overrideLocalBindingYesRadio.setBackground(getBackground());
        overrideLocalBindingButtonGroup.add(overrideLocalBindingYesRadio);

        overrideLocalBindingNoRadio = new MirthRadioButton("No");
        overrideLocalBindingNoRadio.setBackground(getBackground());
        overrideLocalBindingButtonGroup.add(overrideLocalBindingNoRadio);
        
        reconnectIntervalLabel = new JLabel("Reconnect Interval (ms):");
        reconnectIntervalField = new MirthTextField();
        
        maxConnectionsLabel = new JLabel("Max Connections:");
        maxConnectionsField = new MirthTextField();
        
        receiveTimeoutLabel = new JLabel("Receive Timeout (ms):");
        receiveTimeoutField = new MirthTextField();
        
        bufferSizeLabel = new JLabel("Buffer Size (bytes):");
        bufferSizeField = new MirthTextField();

        keepConnectionOpenLabel = new JLabel("Keep Connection Open:");
        ButtonGroup keepConnectionOpenGroup = new ButtonGroup();

        keepConnectionOpenYesRadio = new MirthRadioButton("Yes");
        keepConnectionOpenYesRadio.setBackground(getBackground());
        keepConnectionOpenGroup.add(keepConnectionOpenYesRadio);

        keepConnectionOpenNoRadio = new MirthRadioButton("No");
        keepConnectionOpenNoRadio.setBackground(getBackground());
        keepConnectionOpenGroup.add(keepConnectionOpenNoRadio);
        
        dataTypeLabel = new JLabel("Data Type:");
        ButtonGroup dataTypeButtonGroup = new ButtonGroup();
        
        dataTypeBinaryRadio = new MirthRadioButton("Binary");
        dataTypeBinaryRadio.setBackground(getBackground());
        dataTypeBinaryRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dataTypeBinaryRadioActionPerformed();
            }
        });
        dataTypeButtonGroup.add(dataTypeBinaryRadio);
        
        dataTypeTextRadio = new MirthRadioButton("Text");
        dataTypeTextRadio.setBackground(getBackground());
        dataTypeTextRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dataTypeASCIIRadioActionPerformed();
            }
        });
        dataTypeButtonGroup.add(dataTypeTextRadio);

        charsetEncodingLabel = new JLabel("Encoding:");
        charsetEncodingComboBox = new MirthComboBox<CharsetEncodingInformation>();
        
        respondOnNewConnectionLabel = new JLabel("Respond on New Connection:");
        ButtonGroup respondOnNewConnectionButtonGroup = new ButtonGroup();
        
        respondOnNewConnectionYesRadio = new MirthRadioButton("Yes");
        respondOnNewConnectionYesRadio.setBackground(getBackground());
        respondOnNewConnectionYesRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                respondOnNewConnectionYesRadioActionPerformed();
            }
        });
        respondOnNewConnectionButtonGroup.add(respondOnNewConnectionYesRadio);

        respondOnNewConnectionNoRadio = new MirthRadioButton("No");
        respondOnNewConnectionNoRadio.setBackground(getBackground());
        respondOnNewConnectionNoRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                respondOnNewConnectionNoRadioActionPerformed();
            }
        });
        respondOnNewConnectionButtonGroup.add(respondOnNewConnectionNoRadio);

        respondOnNewConnectionRecoveryRadio = new MirthRadioButton("Message Recovery");
        respondOnNewConnectionRecoveryRadio.setBackground(getBackground());
        respondOnNewConnectionRecoveryRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                respondOnNewConnectionRecoveryRadioActionPerformed();
            }
        });
        respondOnNewConnectionButtonGroup.add(respondOnNewConnectionRecoveryRadio);
        
        responseAddressLabel = new JLabel("Response Address:");
        responseAddressField = new MirthTextField();
        
        responsePortLabel = new JLabel("Response Port:");
        responsePortField = new MirthTextField();
    }

    private void initToolTips() {
        String toolTipText = "<html>Select the transmission mode to use for sending and receiving data.<br/></html>";
        transmissionModeLabel.setToolTipText(toolTipText);
        transmissionModeComboBox.setToolTipText(toolTipText);
        
        toolTipText = "<html>Select Server to listen for connections from clients, or Client to connect to a TCP Server.<br/>In Client mode, the listener settings will only be used if Override Local Binding is enabled.</html>";
        modeServerRadio.setToolTipText(toolTipText);
        modeClientRadio.setToolTipText(toolTipText);
        
        remoteAddressField.setToolTipText("<html>The DNS domain name or IP address on which to connect.</html>");
        remotePortField.setToolTipText("<html>The port on which to connect.</html>");
        
        toolTipText = "<html>Select Yes to override the local address and port that the client socket will be bound to.<br/>Select No to use the default values of 0.0.0.0:0.<br/>A local port of zero (0) indicates that the OS should assign an ephemeral port automatically.<br/><br/>Note that if a specific (non-zero) local port is chosen, then after a socket is closed it's up to the<br/>underlying OS to release the port before the next socket creation, otherwise the bind attempt will fail.<br/></html>";
        overrideLocalBindingYesRadio.setToolTipText(toolTipText);
        overrideLocalBindingNoRadio.setToolTipText(toolTipText);
        
        reconnectIntervalField.setToolTipText("<html>If Client mode is selected, enter the time (in milliseconds) to wait<br/>between disconnecting from the TCP server and connecting to it again.</html>");
        maxConnectionsField.setToolTipText("<html>The maximum number of client connections to accept.<br/>After this number has been reached, subsequent socket requests will result in a rejection.</html>");
        receiveTimeoutField.setToolTipText("The amount of time, in milliseconds, to wait without receiving a message before closing a connection.");
        bufferSizeField.setToolTipText("<html>Use larger values for larger messages, and smaller values <br>for smaller messages. Generally, the default value is fine.</html>");
        
        toolTipText = "<html>Select No to close the listening socket after a received message has finished processing.<br/>Otherwise the socket will remain open until the sending system closes it. In that case,<br/>messages will only be processed if data is received and either the receive timeout is reached,<br/>the client closes the socket, or an end of message byte sequence has been detected.</html>";
        keepConnectionOpenYesRadio.setToolTipText(toolTipText);
        keepConnectionOpenNoRadio.setToolTipText(toolTipText);
        
        toolTipText = "<html>Select Binary if the inbound messages are raw byte streams; the payload will be Base64 encoded.<br>Select Text if the inbound messages are text streams; the payload will be encoded with the specified character set encoding.</html>";
        dataTypeBinaryRadio.setToolTipText(toolTipText);
        dataTypeTextRadio.setToolTipText(toolTipText);
        
        charsetEncodingComboBox.setToolTipText("<html>Select the character set encoding used by the message sender,<br/>or Select Default to use the default character set encoding for the JVM running Mirth.</html>");
        
        toolTipText = "<html>Select No to send responses only via the same connection the inbound message was received on.<br/>Select Yes to always send responses on a new connection (during normal processing as well as recovery).<br/>Select Message Recovery to only send responses on a new connection during message recovery.<br/>Connections will be bound locally on the same interface chosen in the Listener Settings with an ephemeral port.</html>";
        respondOnNewConnectionYesRadio.setToolTipText(toolTipText);
        respondOnNewConnectionNoRadio.setToolTipText(toolTipText);
        respondOnNewConnectionRecoveryRadio.setToolTipText(toolTipText);
        
        responseAddressField.setToolTipText("<html>Enter the DNS domain name or IP address to send message responses to.</html>");
        responsePortField.setToolTipText("<html>Enter the port to send message responses to.</html>");

        for (ConnectorPropertiesPlugin connectorPropertiesPlugin : LoadedExtensions.getInstance().getConnectorPropertiesPlugins().values()) {
            if (connectorPropertiesPlugin.isConnectorPropertiesPluginSupported(NEW_CONNECTION_PLUGIN_PROPS)) {
                responseConnectorPropertiesPanel = connectorPropertiesPlugin.getConnectorPropertiesPanel();
            }
        }
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, gap 12 6", "", "[][]4[]4[][]4[]4[][][][]4[]4[]4[]4[]4[][]"));
        
        add(transmissionModeLabel, "right");
        add(transmissionModeComboBox, "h 22!, split 2");
        
        settingsPlaceHolder.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        add(settingsPlaceHolder, "gapbefore 6, h 22!");
        
        add(sampleLabel, "newline, right");
        add(sampleValue, "growx, sx");
        add(modeLabel, "newline, right");
        add(modeServerRadio, "split 2");
        add(modeClientRadio);
        add(remoteAddressLabel, "newline, right");
        add(remoteAddressField, "w 200!, sx");
        add(remotePortLabel, "newline, right");
        add(remotePortField, "w 50!, sx");
        add(overrideLocalBindingLabel, "newline, right");
        add(overrideLocalBindingYesRadio, "split 2");
        add(overrideLocalBindingNoRadio);
        add(reconnectIntervalLabel, "newline, right");
        add(reconnectIntervalField, "w 75!, sx");
        add(maxConnectionsLabel, "newline, right");
        add(maxConnectionsField, "w 75!, sx");
        add(receiveTimeoutLabel, "newline, right");
        add(receiveTimeoutField, "w 75!, sx");
        add(bufferSizeLabel, "newline, right");
        add(bufferSizeField, "w 75!, sx");
        add(keepConnectionOpenLabel, "newline, right");
        add(keepConnectionOpenYesRadio, "split 2");
        add(keepConnectionOpenNoRadio);
        add(dataTypeLabel, "newline, right");
        add(dataTypeBinaryRadio, "split 2");
        add(dataTypeTextRadio);
        add(charsetEncodingLabel, "newline, right");
        add(charsetEncodingComboBox);
        add(respondOnNewConnectionLabel, "newline, right");
        add(respondOnNewConnectionYesRadio, "split 3");
        add(respondOnNewConnectionNoRadio);
        add(respondOnNewConnectionRecoveryRadio);
        add(responseAddressLabel, "newline, right");
        add(responseAddressField, "w 200!, sx");
        add(responsePortLabel, "newline, right");
        add(responsePortField, "w 50!, sx");
        
        if (responseConnectorPropertiesPanel != null && responseConnectorPropertiesPanel.getLayoutComponents() != null) {
            Component[][] components = responseConnectorPropertiesPanel.getLayoutComponents();
            for (int row = 0; row < components.length; row++) {
                Component[] componentRow = components[row];
                for (int column = 0; column < componentRow.length; column++) {
                    if (column == 0) {
                        if (row == 0) {
                            add(componentRow[column], "newline, right, gaptop 4");
                        } else {
                            add(componentRow[column], "newline, right");
                        }
                    } else {
                        add(componentRow[column]);
                    }
                }
            }
        }
    }

    private void dataTypeBinaryRadioActionPerformed() {
        charsetEncodingLabel.setEnabled(false);
        charsetEncodingComboBox.setEnabled(false);
        charsetEncodingComboBox.setSelectedIndex(0);
    }

    private void dataTypeASCIIRadioActionPerformed() {
        charsetEncodingLabel.setEnabled(true);
        charsetEncodingComboBox.setEnabled(true);
    }

    private void modeClientRadioActionPerformed() {
        remoteAddressLabel.setEnabled(true);
        remoteAddressField.setEnabled(true);
        remotePortLabel.setEnabled(true);
        remotePortField.setEnabled(true);
        overrideLocalBindingLabel.setEnabled(true);
        overrideLocalBindingYesRadio.setEnabled(true);
        overrideLocalBindingNoRadio.setEnabled(true);
        reconnectIntervalLabel.setEnabled(true);
        reconnectIntervalField.setEnabled(true);
        maxConnectionsLabel.setEnabled(false);
        maxConnectionsField.setEnabled(false);
    }

    private void modeServerRadioActionPerformed() {
        remoteAddressLabel.setEnabled(false);
        remoteAddressField.setEnabled(false);
        remotePortLabel.setEnabled(false);
        remotePortField.setEnabled(false);
        overrideLocalBindingLabel.setEnabled(false);
        overrideLocalBindingYesRadio.setEnabled(false);
        overrideLocalBindingNoRadio.setEnabled(false);
        reconnectIntervalLabel.setEnabled(false);
        reconnectIntervalField.setEnabled(false);
        maxConnectionsLabel.setEnabled(true);
        maxConnectionsField.setEnabled(true);
    }

    private void transmissionModeComboBoxActionPerformed() {
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

    private void respondOnNewConnectionRecoveryRadioActionPerformed() {
        responseAddressField.setEnabled(true);
        responsePortField.setEnabled(true);
        responseAddressLabel.setEnabled(true);
        responsePortLabel.setEnabled(true);
        
        if (responseConnectorPropertiesPanel != null) {
            responseConnectorPropertiesPanel.setLayoutComponentsEnabled(true);
        }
    }

    private void respondOnNewConnectionNoRadioActionPerformed() {
        responseAddressField.setEnabled(false);
        responsePortField.setEnabled(false);
        responseAddressLabel.setEnabled(false);
        responsePortLabel.setEnabled(false);
        
        if (responseConnectorPropertiesPanel != null) {
            responseConnectorPropertiesPanel.setLayoutComponentsEnabled(false);
        }
    }

    private void respondOnNewConnectionYesRadioActionPerformed() {
        responseAddressField.setEnabled(true);
        responsePortField.setEnabled(true);
        responseAddressLabel.setEnabled(true);
        responsePortLabel.setEnabled(true);
        
        if (responseConnectorPropertiesPanel != null) {
            responseConnectorPropertiesPanel.setLayoutComponentsEnabled(true);
        }
    }

    private JLabel transmissionModeLabel;
    private MirthComboBox<String> transmissionModeComboBox;
    private JPanel settingsPlaceHolder;
    private JLabel sampleLabel;
    private JLabel sampleValue;
    private JLabel modeLabel;
    public MirthRadioButton modeServerRadio;
    public MirthRadioButton modeClientRadio;
    private JLabel remoteAddressLabel;
    private MirthTextField remoteAddressField;
    private JLabel remotePortLabel;
    private MirthTextField remotePortField;
    private JLabel overrideLocalBindingLabel;
    private MirthRadioButton overrideLocalBindingYesRadio;
    private MirthRadioButton overrideLocalBindingNoRadio;
    private JLabel reconnectIntervalLabel;
    private MirthTextField reconnectIntervalField;
    private JLabel maxConnectionsLabel;
    private MirthTextField maxConnectionsField;
    private JLabel receiveTimeoutLabel;
    private MirthTextField receiveTimeoutField;
    private JLabel bufferSizeLabel;
    private MirthTextField bufferSizeField;
    private JLabel keepConnectionOpenLabel;
    private MirthRadioButton keepConnectionOpenYesRadio;
    private MirthRadioButton keepConnectionOpenNoRadio;
    private JLabel dataTypeLabel;
    private MirthRadioButton dataTypeBinaryRadio;
    private MirthRadioButton dataTypeTextRadio;
    private JLabel charsetEncodingLabel;
    private MirthComboBox<CharsetEncodingInformation> charsetEncodingComboBox;
    private JLabel respondOnNewConnectionLabel;
    private MirthRadioButton respondOnNewConnectionYesRadio;
    private MirthRadioButton respondOnNewConnectionNoRadio;
    private MirthRadioButton respondOnNewConnectionRecoveryRadio;
    private JLabel responseAddressLabel;
    private MirthTextField responseAddressField;
    private JLabel responsePortLabel;
    private MirthTextField responsePortField;
    private AbstractConnectorPropertiesPanel responseConnectorPropertiesPanel;
}
