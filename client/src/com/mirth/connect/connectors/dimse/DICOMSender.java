/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.dimse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthButton;
import com.mirth.connect.client.ui.components.MirthIconTextField;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.util.PortUsageDialog;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.Connector.Mode;

import net.miginfocom.swing.MigLayout;

public class DICOMSender extends ConnectorSettingsPanel {

    private Frame parent;
    private boolean tlsComponentsEnabled = true;

    public DICOMSender() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initLayout();
    }

    @Override
    public String getConnectorName() {
        return new DICOMDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        DICOMDispatcherProperties properties = new DICOMDispatcherProperties();

        properties.setHost(remoteAddressField.getText());
        properties.setPort(remotePortField.getText());
        properties.setLocalHost(localAddressField.getText());
        properties.setLocalPort(localPortField.getText());
        properties.setTemplate(fileContentsTextPane.getText());
        properties.setAcceptTo(associateTimeoutField.getText());
        properties.setAsync(maxAsyncOperationsField.getText());
        properties.setBufSize(transcoderBufferSizeField.getText());
        properties.setConnectTo(tcpConnectionTimeoutField.getText());
        properties.setKeyPW(keyPasswordField.getText());
        properties.setKeyStore(keyStoreField.getText());
        properties.setKeyStorePW(keyStorePasswordField.getText());

        properties.setNoClientAuth(clientAuthenticationYesRadio.isSelected());
        properties.setNossl2(acceptSSLv2YesRadio.isSelected());
        properties.setPasscode(passcodeField.getText());
        properties.setPdv1(packPdvYes.isSelected());
        if (lowPriority.isSelected()) {
            properties.setPriority("low");
        } else if (mediumPriority.isSelected()) {
            properties.setPriority("med");
        } else if (highPriority.isSelected()) {
            properties.setPriority("high");
        }
        properties.setRcvpdulen(pduMaxLengthReceivedField.getText());
        properties.setReaper(dimseRspIntervalPeriodField.getText());
        properties.setReleaseTo(releaseTimeoutField.getText());
        properties.setRspTo(dimseRspTimeoutField.getText());
        properties.setShutdownDelay(shutdowndelayField.getText());
        properties.setSndpdulen(pduMaxLengthSentField.getText());
        properties.setSoCloseDelay(socketCloseDelayField.getText());
        properties.setSorcvbuf(receiveSocketBufferField.getText());
        properties.setSosndbuf(sendSocketBufferField.getText());
        properties.setStgcmt(requestStorageCommYes.isSelected());
        properties.setTcpDelay(tcpDelayYes.isSelected());
        if (tlsAESRadio.isSelected()) {
            properties.setTls("aes");
        } else if (tls3DESRadio.isSelected()) {
            properties.setTls("3des");
        } else if (tlsWithoutRadio.isSelected()) {
            properties.setTls("without");
        } else {
            properties.setTls("notls");
        }
        properties.setTrustStore(trustStoreField.getText());
        properties.setTrustStorePW(trustStorePasswordField.getText());
        properties.setTs1(defaultPresentationYes.isSelected());
        properties.setUidnegrsp(requestPositiveUserYes.isSelected());

        properties.setUsername(usernameField.getText());
        properties.setApplicationEntity(remoteApplicationEntityField.getText());
        properties.setLocalApplicationEntity(localApplicationEntityField.getText());
        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        DICOMDispatcherProperties props = (DICOMDispatcherProperties) properties;

        remoteAddressField.setText(props.getHost());
        remotePortField.setText(props.getPort());
        localAddressField.setText(props.getLocalHost());
        localPortField.setText(props.getLocalPort());
        fileContentsTextPane.setText(props.getTemplate());
        associateTimeoutField.setText(props.getAcceptTo());
        maxAsyncOperationsField.setText(props.getAsync());
        transcoderBufferSizeField.setText(props.getBufSize());
        tcpConnectionTimeoutField.setText(props.getConnectTo());
        keyPasswordField.setText(props.getKeyPW());
        keyStoreField.setText(props.getKeyStore());
        keyStorePasswordField.setText(props.getKeyStorePW());
        passcodeField.setText(props.getPasscode());
        pduMaxLengthReceivedField.setText(props.getRcvpdulen());
        dimseRspIntervalPeriodField.setText(props.getReaper());
        releaseTimeoutField.setText(props.getReleaseTo());
        dimseRspTimeoutField.setText(props.getRspTo());
        shutdowndelayField.setText(props.getShutdownDelay());
        pduMaxLengthSentField.setText(props.getSndpdulen());
        socketCloseDelayField.setText(props.getSoCloseDelay());
        receiveSocketBufferField.setText(props.getSorcvbuf());
        sendSocketBufferField.setText(props.getSosndbuf());
        trustStoreField.setText(props.getTrustStore());
        trustStorePasswordField.setText(props.getTrustStorePW());
        usernameField.setText(props.getUsername());
        remoteApplicationEntityField.setText(props.getApplicationEntity());
        localApplicationEntityField.setText(props.getLocalApplicationEntity());

        if (props.isNoClientAuth()) {
            clientAuthenticationYesRadio.setSelected(true);
        } else {
            clientAuthenticationNoRadio.setSelected(true);
        }
        if (props.isNossl2()) {
            acceptSSLv2YesRadio.setSelected(true);
        } else {
            acceptSSLv2NoRadio.setSelected(true);
        }
        if (props.isPdv1()) {
            packPdvYes.setSelected(true);
        } else {
            packPdvNo.setSelected(true);
        }
        if (props.getPriority().equals("low")) {
            lowPriority.setSelected(true);
        } else if (props.getPriority().equals("med")) {
            mediumPriority.setSelected(true);
        } else {
            highPriority.setSelected(true);
        }
        if (props.isStgcmt()) {
            requestStorageCommYes.setSelected(true);
        } else {
            requestStorageCommNo.setSelected(true);
        }
        if (props.isTcpDelay()) {
            tcpDelayYes.setSelected(true);
        } else {
            tcpDelayNo.setSelected(true);
        }
        if (props.getTls().equals("aes")) {
            tlsAESRadio.setSelected(true);
            tlsAESRadioActionPerformed(null);
        } else if (props.getTls().equals("3des")) {
            tls3DESRadio.setSelected(true);
            tls3DESRadioActionPerformed(null);
        } else if (props.getTls().equals("without")) {
            tlsWithoutRadio.setSelected(true);
            tlsWithoutRadioActionPerformed(null);
        } else {
            tlsNoRadio.setSelected(true);
            tlsNoRadioActionPerformed(null);
        }
        if (props.isTs1()) {
            defaultPresentationYes.setSelected(true);
        } else {
            defaultPresentationNo.setSelected(true);
        }
        if (props.isUidnegrsp()) {
            requestPositiveUserYes.setSelected(true);
        } else {
            requestPositiveUserNo.setSelected(true);
        }
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new DICOMDispatcherProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        DICOMDispatcherProperties props = (DICOMDispatcherProperties) properties;

        boolean valid = true;

        if ((props.getHost()).length() <= 3) {
            valid = false;
            if (highlight) {
                remoteAddressField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if ((props.getPort()).length() == 0) {
            valid = false;
            if (highlight) {
                remotePortField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if ((props.getTemplate()).length() == 0) {
            valid = false;
            if (highlight) {
                fileContentsTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        remoteAddressField.setBackground(null);
        decorateConnectorType();
        remotePortField.setBackground(null);
        fileContentsTextPane.setBackground(null);
    }

    @Override
    public ConnectorTypeDecoration getConnectorTypeDecoration() {
        return new ConnectorTypeDecoration(Mode.DESTINATION);
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        if (connectorTypeDecoration != null) {
            remoteAddressField.setIcon(connectorTypeDecoration.getIcon());
            remoteAddressField.setAlternateToolTipText(connectorTypeDecoration.getIconToolTipText());
            remoteAddressField.setIconPopupMenuComponent(connectorTypeDecoration.getIconPopupComponent());
            remoteAddressField.setBackground(connectorTypeDecoration.getHighlightColor());
        }
    }

    public void enableTLSComponents() {
        tlsComponentsEnabled = true;
        tlsLabel.setEnabled(true);
        tls3DESRadio.setEnabled(true);
        tlsAESRadio.setEnabled(true);
        tlsWithoutRadio.setEnabled(true);
        tlsNoRadio.setEnabled(true);
        if (tls3DESRadio.isSelected()) {
            tls3DESRadioActionPerformed(null);
        } else if (tlsAESRadio.isSelected()) {
            tlsAESRadioActionPerformed(null);
        } else if (tlsWithoutRadio.isSelected()) {
            tlsWithoutRadioActionPerformed(null);
        } else {
            tlsNoRadioActionPerformed(null);
        }
    }

    public void disableTLSComponents() {
        tlsComponentsEnabled = false;
        tlsLabel.setEnabled(false);
        tls3DESRadio.setEnabled(false);
        tlsAESRadio.setEnabled(false);
        tlsWithoutRadio.setEnabled(false);
        tlsNoRadio.setEnabled(false);
        tlsNoRadioActionPerformed(null);
    }

    private void initComponents() {

        remoteAddressLabel = new JLabel();
        remoteAddressField = new MirthIconTextField();
        localAddressLabel = new JLabel();
        localAddressField = new MirthTextField();
        // newline
        remotePortLabel = new JLabel();
        remotePortField = new MirthTextField();
        localPortLabel = new JLabel();
        localPortField = new MirthTextField();
        portsInUse = new MirthButton();
        // newline
        remoteApplicationEntityLabel = new JLabel();
        remoteApplicationEntityField = new MirthTextField();
        localApplicationEntityLabel = new JLabel();
        localApplicationEntityField = new MirthTextField();
        // newline
        maxAsyncOperationsLabel = new JLabel();
        maxAsyncOperationsField = new MirthTextField();
        // newline
        priorityLabel = new JLabel(); 
        priorityButtonGroup = new ButtonGroup();
        highPriority = new MirthRadioButton();
        mediumPriority = new MirthRadioButton();
        lowPriority = new MirthRadioButton();
        // newline
        requestStorageCommLabel = new JLabel();
        requestStorageCommButtonGroup = new ButtonGroup();
        requestStorageCommYes = new MirthRadioButton();
        requestStorageCommNo = new MirthRadioButton();
        // newline
        usernameLabel = new JLabel();
        usernameField = new MirthTextField();
        // newline
        passCodeLabel = new JLabel();
        passcodeField = new MirthTextField();
        // newline
        requestPositiveUserLabel = new JLabel();
        requestPositiveUserButtonGroup = new ButtonGroup();
        requestPositiveUserYes = new MirthRadioButton();
        requestPositiveUserNo = new MirthRadioButton();
        // newline
        packPdvLabel = new JLabel();
        packPdvButtonGroup = new ButtonGroup();
        packPdvYes = new MirthRadioButton();
        packPdvNo = new MirthRadioButton();
        // newline
        dimseRspIntervalPeriodLabel = new JLabel();
        dimseRspIntervalPeriodField = new MirthTextField();
        pduMaxLengthSentLabel = new JLabel();
        pduMaxLengthSentField = new MirthTextField();
        // newline
        releaseTimeoutLabel = new JLabel();
        releaseTimeoutField = new MirthTextField();
        pduMaxLengthReceivedLabel = new JLabel();
        pduMaxLengthReceivedField = new MirthTextField();
        // newline
        dimseRspTimeoutLabel = new JLabel();
        dimseRspTimeoutField = new MirthTextField();
        sendSocketBufferLabel = new JLabel();
        sendSocketBufferField = new MirthTextField();
        // newline
        shutdownDelayLabel = new JLabel();
        shutdowndelayField = new MirthTextField();
        receiveSocketBufferLabel = new JLabel();
        receiveSocketBufferField = new MirthTextField();
        // newline
        socketCloseDelayLabel = new JLabel();
        socketCloseDelayField = new MirthTextField();
        transcoderBufferSizeLabel = new JLabel();
        transcoderBufferSizeField = new MirthTextField();
        // newline
        associateTimeoutLabel = new JLabel();
        associateTimeoutField = new MirthTextField();
        // newline
        tcpConnectionTimeoutLabel = new JLabel();
        tcpConnectionTimeoutField = new MirthTextField();
        // newline
        tcpDelayField = new JLabel();
        tcpDelayButtonGroup = new ButtonGroup();
        tcpDelayYes = new MirthRadioButton();
        tcpDelayNo = new MirthRadioButton();
        // newline
        defaultPresentationLabel = new JLabel();
        defaultPresentationButtonGroup = new ButtonGroup();
        defaultPresentationYes = new MirthRadioButton();
        defaultPresentationNo = new MirthRadioButton();
        // newline
        tlsLabel = new JLabel();
        tlsButtonGroup = new ButtonGroup();
        tls3DESRadio = new MirthRadioButton();
        tlsAESRadio = new MirthRadioButton();
        tlsWithoutRadio = new MirthRadioButton();
        tlsNoRadio = new MirthRadioButton();
        // newline
        clientAuthenticationLabel = new JLabel();
        clientAuthTLSButtonGroup = new ButtonGroup();
        clientAuthenticationYesRadio = new MirthRadioButton();
        clientAuthenticationNoRadio = new MirthRadioButton();
        // newline
        acceptSSLv2Label = new JLabel();
        acceptSSLv2ButtonGroup = new ButtonGroup();
        acceptSSLv2YesRadio = new MirthRadioButton();
        acceptSSLv2NoRadio = new MirthRadioButton();
        // newline
        keyStoreLabel = new JLabel();
        keyStoreField = new MirthTextField();
        keyStorePasswordLabel = new JLabel();
        keyStorePasswordField = new MirthTextField();
        // newline
        trustStoreLabel = new JLabel();
        trustStoreField = new MirthTextField();
        trustStorePasswordLabel = new JLabel();
        trustStorePasswordField = new MirthTextField();
        // newline
        keyPasswordLabel = new JLabel();
        keyPasswordField = new MirthTextField();
        // newline
        templateLabel = new JLabel();
        fileContentsTextPane = new MirthSyntaxTextArea();


        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        remoteAddressLabel.setText("Remote Host:");
        remoteAddressField.setToolTipText("Remote IP to send to.");

        localAddressLabel.setText("Local Host:");
        localAddressField.setToolTipText("Local address that the client socket will be bound to.");  

        remotePortLabel.setText("Remote Port:");
        remotePortField.setToolTipText("Remote PORT to send to.");

        localPortLabel.setText("Local Port:");
        localPortField.setToolTipText("Local port that the client socket will be bound to.");
        
        portsInUse.setText("Ports in Use");
        portsInUse.setToolTipText("View all ports currently used by Mirth Connect.");
        portsInUse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                portsInUseActionPerformed(evt);
            }
        });    
        
        remoteApplicationEntityField.setToolTipText("Remote Application Entity");
        remoteApplicationEntityLabel.setText("Remote Application Entity:");

        localApplicationEntityLabel.setText("Local Application Entity:");
        localApplicationEntityField.setToolTipText("Local Application Entity");
  
        maxAsyncOperationsLabel.setText("Max Async operations:");
        maxAsyncOperationsField.setToolTipText("Maximum number of outstanding operations it may invoke asynchronously, unlimited by default.");

        priorityLabel.setText("Priority:");
        
        highPriority.setBackground(new java.awt.Color(255, 255, 255));
        highPriority.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        priorityButtonGroup.add(highPriority);
        highPriority.setText("High");
        highPriority.setToolTipText("Priority of the C-STORE operation, MEDIUM by default.");
        highPriority.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        mediumPriority.setBackground(new java.awt.Color(255, 255, 255));
        mediumPriority.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        priorityButtonGroup.add(mediumPriority);
        mediumPriority.setSelected(true);
        mediumPriority.setText("Medium");
        mediumPriority.setToolTipText("Priority of the C-STORE operation, MEDIUM by default.");
        mediumPriority.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        lowPriority.setBackground(new java.awt.Color(255, 255, 255));
        lowPriority.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        priorityButtonGroup.add(lowPriority);
        lowPriority.setText("Low");
        lowPriority.setToolTipText("Priority of the C-STORE operation, MEDIUM by default.");
        lowPriority.setMargin(new java.awt.Insets(0, 0, 0, 0));

        requestStorageCommLabel.setText("Request Storage Commitment:");
        
        requestStorageCommYes.setBackground(new java.awt.Color(255, 255, 255));
        requestStorageCommYes.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        requestStorageCommButtonGroup.add(requestStorageCommYes);
        requestStorageCommYes.setSelected(true);
        requestStorageCommYes.setText("Yes");
        requestStorageCommYes.setToolTipText("Request storage commitment of (successfully) sent objects afterwards.");
        requestStorageCommYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        requestStorageCommNo.setBackground(new java.awt.Color(255, 255, 255));
        requestStorageCommNo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        requestStorageCommButtonGroup.add(requestStorageCommNo);
        requestStorageCommNo.setText("No");
        requestStorageCommNo.setToolTipText("Request storage commitment of (successfully) sent objects afterwards.");
        requestStorageCommNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        usernameLabel.setText("User Name:");
        usernameField.setToolTipText("Enable User Identity Negotiation with specified username and  optional passcode.");

        passCodeLabel.setText("Pass Code:");
        passcodeField.setToolTipText("Optional passcode for User Identity Negotiation, only effective with option -username.");

        requestPositiveUserLabel.setText("Request Positive User Identity Response:");
        requestPositiveUserYes.setBackground(new java.awt.Color(255, 255, 255));
        requestPositiveUserYes.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        requestPositiveUserButtonGroup.add(requestPositiveUserYes);
        requestPositiveUserYes.setText("Yes");
        requestPositiveUserYes.setToolTipText("Request positive User Identity Negotation response, only effective with option -username.");
        requestPositiveUserYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        requestPositiveUserNo.setBackground(new java.awt.Color(255, 255, 255));
        requestPositiveUserNo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        requestPositiveUserButtonGroup.add(requestPositiveUserNo);
        requestPositiveUserNo.setSelected(true);
        requestPositiveUserNo.setText("No");
        requestPositiveUserNo.setToolTipText("Request positive User Identity Negotation response, only effective with option -username.");
        requestPositiveUserNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        packPdvLabel.setText("Pack PDV:");
        packPdvYes.setBackground(new java.awt.Color(255, 255, 255));
        packPdvYes.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        packPdvButtonGroup.add(packPdvYes);
        packPdvYes.setText("Yes");
        packPdvYes.setToolTipText("Send only one PDV in one P-Data-TF PDU, pack command and data PDV in one P-DATA-TF PDU by default.");
        packPdvYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        packPdvNo.setBackground(new java.awt.Color(255, 255, 255));
        packPdvNo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        packPdvButtonGroup.add(packPdvNo);
        packPdvNo.setSelected(true);
        packPdvNo.setText("No");
        packPdvNo.setToolTipText("Send only one PDV in one P-Data-TF PDU, pack command and data PDV in one P-DATA-TF PDU by default.");
        packPdvNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        dimseRspIntervalPeriodLabel.setText("DIMSE-RSP interval period (s):");
        dimseRspIntervalPeriodField.setToolTipText("Period in ms to check for outstanding DIMSE-RSP, 10s by default.");

        pduMaxLengthSentLabel.setText("P-DATA-TF PDUs max length sent (KB):");        
        pduMaxLengthSentField.setToolTipText("Maximal length in KB of sent P-DATA-TF PDUs, 16KB by default.");

        releaseTimeoutLabel.setText("A-RELEASE-RP timeout (s):");
        releaseTimeoutField.setToolTipText("Timeout in ms for receiving A-RELEASE-RP, 5s by default.");

        pduMaxLengthReceivedLabel.setText("P-DATA-TF PDUs  max length received (KB):");        
        pduMaxLengthReceivedField.setToolTipText("Maximal length in KB of received P-DATA-TF PDUs, 16KB by default.");

        dimseRspTimeoutLabel.setText("DIMSE-RSP timeout (s):");
        dimseRspTimeoutField.setToolTipText("Timeout in ms for receiving DIMSE-RSP, 60s by default.");

        sendSocketBufferLabel.setText("Send Socket Buffer Size (KB):");
        sendSocketBufferField.setToolTipText("Set send socket buffer to specified value in KB.");
        
        shutdownDelayLabel.setText("Shutdown delay (ms):");
        shutdowndelayField.setToolTipText("Delay in ms for closing the listening socket, 1000ms by default.");

        receiveSocketBufferLabel.setText("Receive Socket Buffer Size (KB):");
        receiveSocketBufferField.setToolTipText("Set receive socket buffer to specified value in KB.");

        socketCloseDelayLabel.setText("Socket Close Delay After A-ABORT (ms):");     
        socketCloseDelayField.setToolTipText("Delay in ms for Socket close after sending A-ABORT, 50ms by default.");

        transcoderBufferSizeLabel.setText("Transcoder Buffer Size (KB):");
        transcoderBufferSizeField.setToolTipText("Transcoder buffer size in KB, 1KB by default.");

        associateTimeoutLabel.setText("Timeout A-ASSOCIATE-AC (ms):");       
        associateTimeoutField.setToolTipText("Timeout in ms for receiving A-ASSOCIATE-AC, 5000ms by default.");

        tcpConnectionTimeoutLabel.setText("TCP Connection Timeout (ms):");
        tcpConnectionTimeoutField.setToolTipText("Timeout in ms for TCP connect, no timeout by default.");

        tcpDelayField.setText("TCP Delay:");
        
        tcpDelayYes.setBackground(new java.awt.Color(255, 255, 255));
        tcpDelayYes.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tcpDelayButtonGroup.add(tcpDelayYes);
        tcpDelayYes.setSelected(true);
        tcpDelayYes.setText("Yes");
        tcpDelayYes.setToolTipText("Set TCP_NODELAY socket option to false, true by default.");
        tcpDelayYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        tcpDelayNo.setBackground(new java.awt.Color(255, 255, 255));
        tcpDelayNo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tcpDelayButtonGroup.add(tcpDelayNo);
        tcpDelayNo.setText("No");
        tcpDelayNo.setToolTipText("Set TCP_NODELAY socket option to false, true by default.");
        tcpDelayNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        defaultPresentationLabel.setText("Default Presentation Syntax:");
        
        defaultPresentationYes.setBackground(new java.awt.Color(255, 255, 255));
        defaultPresentationYes.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultPresentationButtonGroup.add(defaultPresentationYes);
        defaultPresentationYes.setText("Yes");
        defaultPresentationYes.setToolTipText("Offer Default Transfer Syntax in separate Presentation Context. By default offered with Explicit VR Little Endian TS in one PC.");
        defaultPresentationYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        defaultPresentationNo.setBackground(new java.awt.Color(255, 255, 255));
        defaultPresentationNo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultPresentationButtonGroup.add(defaultPresentationNo);
        defaultPresentationNo.setSelected(true);
        defaultPresentationNo.setText("No");
        defaultPresentationNo.setToolTipText("Offer Default Transfer Syntax in separate Presentation Context. By default offered with Explicit VR Little Endian TS in one PC.");
        defaultPresentationNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        tlsLabel.setText("TLS:");
        
        tls3DESRadio.setBackground(new java.awt.Color(255, 255, 255));
        tls3DESRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tlsButtonGroup.add(tls3DESRadio);
        tls3DESRadio.setText("3DES");
        tls3DESRadio.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tls3DESRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tls3DESRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tls3DESRadioActionPerformed(evt);
            }
        });
        
        tlsAESRadio.setBackground(new java.awt.Color(255, 255, 255));
        tlsAESRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tlsButtonGroup.add(tlsAESRadio);
        tlsAESRadio.setText("AES");
        tlsAESRadio.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tlsAESRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsAESRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsAESRadioActionPerformed(evt);
            }
        });
        
        tlsWithoutRadio.setBackground(new java.awt.Color(255, 255, 255));
        tlsWithoutRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tlsButtonGroup.add(tlsWithoutRadio);
        tlsWithoutRadio.setText("Without");
        tlsWithoutRadio.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tlsWithoutRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsWithoutRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsWithoutRadioActionPerformed(evt);
            }
        });
        
        tlsNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        tlsNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tlsButtonGroup.add(tlsNoRadio);
        tlsNoRadio.setSelected(true);
        tlsNoRadio.setText("No TLS");
        tlsNoRadio.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tlsNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsNoRadioActionPerformed(evt);
            }
        });

        clientAuthenticationLabel.setText("Client Authentication TLS:");
        
        clientAuthenticationYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        clientAuthenticationYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clientAuthTLSButtonGroup.add(clientAuthenticationYesRadio);
        clientAuthenticationYesRadio.setSelected(true);
        clientAuthenticationYesRadio.setText("Yes");
        clientAuthenticationYesRadio.setToolTipText("Enable client authentification for TLS.");
        clientAuthenticationYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        clientAuthenticationNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        clientAuthenticationNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clientAuthTLSButtonGroup.add(clientAuthenticationNoRadio);
        clientAuthenticationNoRadio.setText("No");
        clientAuthenticationNoRadio.setToolTipText("Enable client authentification for TLS.");
        clientAuthenticationNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        acceptSSLv2Label.setText("Accept ssl v2 TLS handshake:");
        
        acceptSSLv2YesRadio.setBackground(new java.awt.Color(255, 255, 255));
        acceptSSLv2YesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        acceptSSLv2ButtonGroup.add(acceptSSLv2YesRadio);
        acceptSSLv2YesRadio.setSelected(true);
        acceptSSLv2YesRadio.setText("Yes");
        acceptSSLv2YesRadio.setToolTipText("Enable acceptance of SSLv2Hello TLS handshake.");
        acceptSSLv2YesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        acceptSSLv2NoRadio.setBackground(new java.awt.Color(255, 255, 255));
        acceptSSLv2NoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        acceptSSLv2ButtonGroup.add(acceptSSLv2NoRadio);
        acceptSSLv2NoRadio.setText("No");
        acceptSSLv2NoRadio.setToolTipText("Enable acceptance of SSLv2Hello TLS handshake.");
        acceptSSLv2NoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        keyStoreLabel.setText("Keystore:");
        keyStoreField.setToolTipText("File path or URL of P12 or JKS keystore, resource:tls/test_sys_2.p12 by default.");

        keyStorePasswordLabel.setText("Keystore Password:");
        keyStorePasswordField.setToolTipText("Password for keystore file.");

        trustStoreLabel.setText("Trust Store:");
        trustStoreField.setToolTipText("File path or URL of JKS truststore, resource:tls/mesa_certs.jks by default.");

        trustStorePasswordLabel.setText("Trust Store Password:");
        trustStorePasswordField.setToolTipText("Password for truststore file.");

        keyPasswordLabel.setText("Key Password:");
        keyPasswordField.setToolTipText("Password for accessing the key in the keystore, keystore password by default.");

        fileContentsTextPane.setBorder(BorderFactory.createEtchedBorder());
        templateLabel.setText("Template:");
    }

    
    private void initLayout() {
    	JPanel remoteLocalPortsPanel = new JPanel();
    	remoteLocalPortsPanel.setLayout(new MigLayout("novisualpadding, gap 10 6, hidemode 3, insets 0, fill", "[200, right][][right][grow]"));	
    	remoteLocalPortsPanel.setBackground(UIConstants.BACKGROUND_COLOR);
    	
    	remoteLocalPortsPanel.add(remoteAddressLabel);
    	remoteLocalPortsPanel.add(remoteAddressField, "w 150!");
    	remoteLocalPortsPanel.add(localAddressLabel);
    	remoteLocalPortsPanel.add(localAddressField, "w 150!");
    	
    	remoteLocalPortsPanel.add(remotePortLabel, "newline");
    	remoteLocalPortsPanel.add(remotePortField, "w 50!");
    	remoteLocalPortsPanel.add(localPortLabel);
    	remoteLocalPortsPanel.add(localPortField, "w 50!, split 2, sx");
    	remoteLocalPortsPanel.add(portsInUse,  "gapleft 6");
    	
    	remoteLocalPortsPanel.add(remoteApplicationEntityLabel, "newline");
    	remoteLocalPortsPanel.add(remoteApplicationEntityField, "w 50!");
    	remoteLocalPortsPanel.add(localApplicationEntityLabel);
    	remoteLocalPortsPanel.add(localApplicationEntityField, "w 50!");
    	
    	remoteLocalPortsPanel.add(maxAsyncOperationsLabel, "newline");
    	remoteLocalPortsPanel.add(maxAsyncOperationsField, "w 50!, spanx");
    	
    	remoteLocalPortsPanel.add(priorityLabel, "newline, h 18!");
    	remoteLocalPortsPanel.add(highPriority, "split 3");
    	remoteLocalPortsPanel.add(mediumPriority);
    	remoteLocalPortsPanel.add(lowPriority, "spanx");
    	
    	remoteLocalPortsPanel.add(requestStorageCommLabel, "newline, h 18!");
    	remoteLocalPortsPanel.add(requestStorageCommYes, "split 4");
    	remoteLocalPortsPanel.add(requestStorageCommNo, "spanx");
    	
    	remoteLocalPortsPanel.add(usernameLabel, "newline");
    	remoteLocalPortsPanel.add(usernameField, "w 50!, spanx");
    	
    	remoteLocalPortsPanel.add(passCodeLabel, "newline");
    	remoteLocalPortsPanel.add(passcodeField, "w 50!, spanx");

    	JPanel settingsPanel = new JPanel();
    	settingsPanel.setLayout(new MigLayout("novisualpadding, gap 10 6, hidemode 3, insets 0, fill", "[200, right][][right][grow]"));
        settingsPanel.setBackground(UIConstants.BACKGROUND_COLOR);
    	
    	settingsPanel.add(requestPositiveUserLabel, "h 18!");
    	settingsPanel.add(requestPositiveUserYes, "split 2");
    	settingsPanel.add(requestPositiveUserNo, "spanx");
    	
    	settingsPanel.add(packPdvLabel, "newline, h 18!");
    	settingsPanel.add(packPdvYes, "split 2");
    	settingsPanel.add(packPdvNo, "spanx");
    	
    	settingsPanel.add(dimseRspIntervalPeriodLabel, "newline");
    	settingsPanel.add(dimseRspIntervalPeriodField, "w 50!");
    	settingsPanel.add(pduMaxLengthSentLabel);
    	settingsPanel.add(pduMaxLengthSentField, "w 50!, spanx");
    	
    	settingsPanel.add(releaseTimeoutLabel, "newline");
    	settingsPanel.add(releaseTimeoutField, "w 50!");
    	settingsPanel.add(pduMaxLengthReceivedLabel);
    	settingsPanel.add(pduMaxLengthReceivedField, "w 50!, spanx");
    	
    	settingsPanel.add(dimseRspTimeoutLabel, "newline");
    	settingsPanel.add(dimseRspTimeoutField, "w 50!, spanx");
    	settingsPanel.add(sendSocketBufferLabel);
    	settingsPanel.add(sendSocketBufferField, "w 50!");
    	
    	settingsPanel.add(shutdownDelayLabel, "newline");
    	settingsPanel.add(shutdowndelayField, "w 50!");
    	settingsPanel.add(receiveSocketBufferLabel);
    	settingsPanel.add(receiveSocketBufferField, "w 50!");
    	
    	settingsPanel.add(socketCloseDelayLabel, "newline");
    	settingsPanel.add(socketCloseDelayField, "w 50!");
    	settingsPanel.add(transcoderBufferSizeLabel);
    	settingsPanel.add(transcoderBufferSizeField, "w 50!");
    	
    	settingsPanel.add(associateTimeoutLabel, "newline");
    	settingsPanel.add(associateTimeoutField, "w 50!");
    	settingsPanel.add(tcpConnectionTimeoutLabel, "newline");
    	settingsPanel.add(tcpConnectionTimeoutField, "w 50!");

    	
    	JPanel tlsPanel = new JPanel();
    	tlsPanel.setLayout(new MigLayout("novisualpadding, gap 10 6, hidemode 3, insets 0, fill", "[200, right][grow]"));
        tlsPanel.setBackground(UIConstants.BACKGROUND_COLOR); 
    	
        tlsPanel.add(tcpDelayField, "h 18!");
        tlsPanel.add(tcpDelayYes, "split 2");
        tlsPanel.add(tcpDelayNo);
    	
        tlsPanel.add(defaultPresentationLabel, "newline, h 18!");
        tlsPanel.add(defaultPresentationYes, "split 2");
        tlsPanel.add(defaultPresentationNo);
    	
    	tlsPanel.add(tlsLabel, "newline, h 18!");
    	tlsPanel.add(tls3DESRadio, "split 4");
    	tlsPanel.add(tlsAESRadio);
    	tlsPanel.add(tlsWithoutRadio);
    	tlsPanel.add(tlsNoRadio);
    	
    	tlsPanel.add(clientAuthenticationLabel, "newline, h 18!");
    	tlsPanel.add(clientAuthenticationYesRadio, "split 2");
    	tlsPanel.add(clientAuthenticationNoRadio);
    	
    	tlsPanel.add(acceptSSLv2Label, "newline, h 18!");
    	tlsPanel.add(acceptSSLv2YesRadio, "split 2");
    	tlsPanel.add(acceptSSLv2NoRadio);

    	JPanel keystorePanel = new JPanel();
    	keystorePanel.setLayout(new MigLayout("novisualpadding, gap 10 6, hidemode 3, insets 0, fill", "[200, right][][right][]"));
        keystorePanel.setBackground(UIConstants.BACKGROUND_COLOR);

    	keystorePanel.add(keyStoreLabel);
    	keystorePanel.add(keyStoreField, "w 150!");
    	keystorePanel.add(keyStorePasswordLabel);
    	keystorePanel.add(keyStorePasswordField, "w 80!");
    	
    	keystorePanel.add(trustStoreLabel, "newline");
    	keystorePanel.add(trustStoreField, "w 150!");
    	keystorePanel.add(trustStorePasswordLabel);
    	keystorePanel.add(trustStorePasswordField, "w 80!");
    	
    	keystorePanel.add(keyPasswordLabel, "newline");
    	keystorePanel.add(keyPasswordField, "w 150!");

    	JPanel templatePanel = new JPanel();
    	templatePanel.setLayout(new MigLayout("novisualpadding, gap 10 6, hidemode 3, insets 0, fill", "[200, right][]"));	
        templatePanel.setBackground(UIConstants.BACKGROUND_COLOR);
    	
    	templatePanel.add(templateLabel, "aligny top");
    	templatePanel.add(fileContentsTextPane, "grow, push, w :400, h :150");
   	
    	setLayout(new MigLayout("novisualpadding, hidemode 3, insets 0, wrap, gapy 6, fill"));
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));  	
    	setBackground(UIConstants.BACKGROUND_COLOR);
    	add(remoteLocalPortsPanel);
    	add(settingsPanel);
    	add(tlsPanel);
    	add(keystorePanel);
    	add(templatePanel, "sx, grow");
    }

    private void tlsNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tlsNoRadioActionPerformed
        // disable
        clientAuthenticationLabel.setEnabled(false);
        clientAuthenticationYesRadio.setEnabled(false);
        clientAuthenticationNoRadio.setEnabled(false);
        acceptSSLv2Label.setEnabled(false);
        acceptSSLv2YesRadio.setEnabled(false);
        acceptSSLv2NoRadio.setEnabled(false);
        keyStoreLabel.setEnabled(false);
        keyStoreField.setEnabled(false);
        keyStorePasswordLabel.setEnabled(false);
        keyStorePasswordField.setEnabled(false);
        trustStoreLabel.setEnabled(false);
        trustStoreField.setEnabled(false);
        trustStorePasswordLabel.setEnabled(false);
        trustStorePasswordField.setEnabled(false);
        keyPasswordLabel.setEnabled(false);
        keyPasswordField.setEnabled(false);
    }

    private void tls3DESRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tls3DESRadioActionPerformed
        if (!tlsComponentsEnabled) {
            return;
        }
        clientAuthenticationLabel.setEnabled(true);
        clientAuthenticationYesRadio.setEnabled(true);
        clientAuthenticationNoRadio.setEnabled(true);
        acceptSSLv2Label.setEnabled(true);
        acceptSSLv2YesRadio.setEnabled(true);
        acceptSSLv2NoRadio.setEnabled(true);
        keyStoreLabel.setEnabled(true);
        keyStoreField.setEnabled(true);
        keyStorePasswordLabel.setEnabled(true);
        keyStorePasswordField.setEnabled(true);
        trustStoreLabel.setEnabled(true);
        trustStoreField.setEnabled(true);
        trustStorePasswordLabel.setEnabled(true);
        trustStorePasswordField.setEnabled(true);
        keyPasswordLabel.setEnabled(true);
        keyPasswordField.setEnabled(true);
    }

    private void tlsAESRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tlsAESRadioActionPerformed
        if (!tlsComponentsEnabled) {
            return;
        }
        clientAuthenticationLabel.setEnabled(true);
        clientAuthenticationYesRadio.setEnabled(true);
        clientAuthenticationNoRadio.setEnabled(true);
        acceptSSLv2Label.setEnabled(true);
        acceptSSLv2YesRadio.setEnabled(true);
        acceptSSLv2NoRadio.setEnabled(true);
        keyStoreLabel.setEnabled(true);
        keyStoreField.setEnabled(true);
        keyStorePasswordLabel.setEnabled(true);
        keyStorePasswordField.setEnabled(true);
        trustStoreLabel.setEnabled(true);
        trustStoreField.setEnabled(true);
        trustStorePasswordLabel.setEnabled(true);
        trustStorePasswordField.setEnabled(true);
        keyPasswordLabel.setEnabled(true);
        keyPasswordField.setEnabled(true);
    }

    private void tlsWithoutRadioActionPerformed(java.awt.event.ActionEvent evt) {
        if (!tlsComponentsEnabled) {
            return;
        }
        clientAuthenticationLabel.setEnabled(true);
        clientAuthenticationYesRadio.setEnabled(true);
        clientAuthenticationNoRadio.setEnabled(true);
        acceptSSLv2Label.setEnabled(true);
        acceptSSLv2YesRadio.setEnabled(true);
        acceptSSLv2NoRadio.setEnabled(true);
        keyStoreLabel.setEnabled(true);
        keyStoreField.setEnabled(true);
        keyStorePasswordLabel.setEnabled(true);
        keyStorePasswordField.setEnabled(true);
        trustStoreLabel.setEnabled(true);
        trustStoreField.setEnabled(true);
        trustStorePasswordLabel.setEnabled(true);
        trustStorePasswordField.setEnabled(true);
        keyPasswordLabel.setEnabled(true);
        keyPasswordField.setEnabled(true);
    }
    
	private void portsInUseActionPerformed(ActionEvent evt) {
		PortUsageDialog dialog = new PortUsageDialog(parent);	
	}

    // variables declaration
    private JLabel acceptSSLv2Label;
    private MirthRadioButton acceptSSLv2NoRadio;
    private MirthRadioButton acceptSSLv2YesRadio;
    private MirthTextField associateTimeoutField;
    private MirthTextField remoteApplicationEntityField;
    private MirthTextField maxAsyncOperationsField;
    private MirthTextField transcoderBufferSizeField;
    private ButtonGroup clientAuthTLSButtonGroup;
    private JLabel clientAuthenticationLabel;
    private MirthRadioButton clientAuthenticationNoRadio;
    private MirthRadioButton clientAuthenticationYesRadio;
    private MirthTextField tcpConnectionTimeoutField;
    private MirthSyntaxTextArea fileContentsTextPane;
    private MirthRadioButton highPriority;
    private JLabel remoteAddressLabel;
    private JLabel usernameLabel;
    private JLabel passCodeLabel;
    private JLabel pduMaxLengthReceivedLabel;
    private JLabel packPdvLabel;
    private JLabel dimseRspIntervalPeriodLabel;
    private JLabel releaseTimeoutLabel;
    private JLabel remotePortLabel;
    private JLabel dimseRspTimeoutLabel;
    private JLabel shutdownDelayLabel;
    private JLabel pduMaxLengthSentLabel;
    private JLabel socketCloseDelayLabel;
    private JLabel receiveSocketBufferLabel;
    private JLabel sendSocketBufferLabel;
    private JLabel requestStorageCommLabel;
    private JLabel tcpDelayField;
    private JLabel templateLabel;
    private JLabel defaultPresentationLabel;
    private JLabel requestPositiveUserLabel;
    private JLabel remoteApplicationEntityLabel;
    private JLabel localAddressLabel;
    private JLabel localPortLabel;
    private JLabel localApplicationEntityLabel;
    private JLabel associateTimeoutLabel;
    private JLabel maxAsyncOperationsLabel;
    private JLabel transcoderBufferSizeLabel;
    private JLabel tcpConnectionTimeoutLabel;
    private JLabel priorityLabel;
    private MirthTextField keyPasswordField;
    private JLabel keyPasswordLabel;
    private MirthTextField keyStoreField;
    private JLabel keyStoreLabel;
    private MirthTextField keyStorePasswordField;
    private JLabel keyStorePasswordLabel;
    private MirthIconTextField remoteAddressField;
    private MirthTextField remotePortField;
    private MirthTextField localAddressField;
    private MirthTextField localApplicationEntityField;
    private MirthTextField localPortField;
    private MirthRadioButton lowPriority;
    private MirthRadioButton mediumPriority;
    private ButtonGroup acceptSSLv2ButtonGroup;
    private MirthTextField passcodeField;
    private ButtonGroup packPdvButtonGroup;
    private MirthRadioButton packPdvNo;
    private MirthRadioButton packPdvYes;
    private MirthButton portsInUse;
    private ButtonGroup priorityButtonGroup;
    private MirthTextField pduMaxLengthReceivedField;
    private MirthTextField dimseRspIntervalPeriodField;
    private MirthTextField releaseTimeoutField;
    private MirthTextField dimseRspTimeoutField;
    private MirthTextField shutdowndelayField;
    private MirthTextField pduMaxLengthSentField;
    private MirthTextField socketCloseDelayField;
    private MirthTextField receiveSocketBufferField;
    private MirthTextField sendSocketBufferField;
    private ButtonGroup requestStorageCommButtonGroup;
    private MirthRadioButton requestStorageCommNo;
    private MirthRadioButton requestStorageCommYes;
    private ButtonGroup tcpDelayButtonGroup;
    private MirthRadioButton tcpDelayNo;
    private MirthRadioButton tcpDelayYes;
    private MirthRadioButton tls3DESRadio;
    private MirthRadioButton tlsAESRadio;
    private ButtonGroup tlsButtonGroup;
    private JLabel tlsLabel;
    private MirthRadioButton tlsNoRadio;
    private MirthRadioButton tlsWithoutRadio;
    private MirthTextField trustStoreField;
    private JLabel trustStoreLabel;
    private MirthTextField trustStorePasswordField;
    private JLabel trustStorePasswordLabel;
    private ButtonGroup defaultPresentationButtonGroup;
    private MirthRadioButton defaultPresentationNo;
    private MirthRadioButton defaultPresentationYes;
    private ButtonGroup requestPositiveUserButtonGroup;
    private MirthRadioButton requestPositiveUserNo;
    private MirthRadioButton requestPositiveUserYes;
    private MirthTextField usernameField;
}
