/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.dimse;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class DICOMSender extends ConnectorSettingsPanel {

    private Frame parent;

    public DICOMSender() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
    }

    @Override
    public String getConnectorName() {
        return new DICOMDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        DICOMDispatcherProperties properties = new DICOMDispatcherProperties();

        properties.setHost(listenerAddressField.getText());
        properties.setPort(listenerPortField.getText());
        properties.setLocalHost(localAddressField.getText());
        properties.setLocalPort(localPortField.getText());
        properties.setTemplate(fileContentsTextPane.getText());
        properties.setAcceptTo(accepttoField.getText());
        properties.setAsync(asyncField.getText());
        properties.setBufSize(bufsizeField.getText());
        properties.setConnectTo(connecttoField.getText());
        properties.setKeyPW(keyPasswordField.getText());
        properties.setKeyStore(keyStoreField.getText());
        properties.setKeyStorePW(keyStorePasswordField.getText());

        properties.setNoClientAuth(noclientauthYes.isSelected());
        properties.setNossl2(nossl2Yes.isSelected());
        properties.setPasscode(passcodeField.getText());
        properties.setPdv1(pdv1Yes.isSelected());
        if (lowPriority.isSelected()) {
            properties.setPriority("low");
        } else if (mediumPriority.isSelected()) {
            properties.setPriority("med");
        } else if (highPriority.isSelected()) {
            properties.setPriority("high");
        }
        properties.setRcvpdulen(rcvpdulenField.getText());
        properties.setReaper(reaperField.getText());
        properties.setReleaseTo(releasetoField.getText());
        properties.setRspTo(rsptoField.getText());
        properties.setShutdownDelay(shutdowndelayField.getText());
        properties.setSndpdulen(sndpdulenField.getText());
        properties.setSoCloseDelay(soclosedelayField.getText());
        properties.setSorcvbuf(sorcvbufField.getText());
        properties.setSosndbuf(sosndbufField.getText());
        properties.setStgcmt(stgcmtYes.isSelected());
        properties.setTcpDelay(tcpdelayYes.isSelected());
        if (tlsaes.isSelected()) {
            properties.setTls("aes");
        } else if (tls3des.isSelected()) {
            properties.setTls("3des");
        } else if (tlswithout.isSelected()) {
            properties.setTls("without");
        } else {
            properties.setTls("notls");
        }
        properties.setTrustStore(truststoreField.getText());
        properties.setTrustStorePW(truststorepwField.getText());
        properties.setTs1(ts1Yes.isSelected());
        properties.setUidnegrsp(uidnegrspYes.isSelected());

        properties.setUsername(usernameField.getText());
        properties.setApplicationEntity(applicationEntityField.getText());
        properties.setLocalApplicationEntity(localApplicationEntityField.getText());
        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        DICOMDispatcherProperties props = (DICOMDispatcherProperties) properties;

        listenerAddressField.setText(props.getHost());
        listenerPortField.setText(props.getPort());
        localAddressField.setText(props.getLocalHost());
        localPortField.setText(props.getLocalPort());
        fileContentsTextPane.setText(props.getTemplate());
        accepttoField.setText(props.getAcceptTo());
        asyncField.setText(props.getAsync());
        bufsizeField.setText(props.getBufSize());
        connecttoField.setText(props.getConnectTo());
        keyPasswordField.setText(props.getKeyPW());
        keyStoreField.setText(props.getKeyStore());
        keyStorePasswordField.setText(props.getKeyStorePW());
        passcodeField.setText(props.getPasscode());
        rcvpdulenField.setText(props.getRcvpdulen());
        reaperField.setText(props.getReaper());
        releasetoField.setText(props.getReleaseTo());
        rsptoField.setText(props.getRspTo());
        shutdowndelayField.setText(props.getShutdownDelay());
        sndpdulenField.setText(props.getSndpdulen());
        soclosedelayField.setText(props.getSoCloseDelay());
        sorcvbufField.setText(props.getSorcvbuf());
        sosndbufField.setText(props.getSosndbuf());
        truststoreField.setText(props.getTrustStore());
        truststorepwField.setText(props.getTrustStorePW());
        usernameField.setText(props.getUsername());
        applicationEntityField.setText(props.getApplicationEntity());
        localApplicationEntityField.setText(props.getLocalApplicationEntity());

        if (props.isNoClientAuth()) {
            noclientauthYes.setSelected(true);
        } else {
            noclientauthNo.setSelected(true);
        }
        if (props.isNossl2()) {
            nossl2Yes.setSelected(true);
        } else {
            nossl2No.setSelected(true);
        }
        if (props.isPdv1()) {
            pdv1Yes.setSelected(true);
        } else {
            pdv1No.setSelected(true);
        }
        if (props.getPriority().equals("low")) {
            lowPriority.setSelected(true);
        } else if (props.getPriority().equals("med")) {
            mediumPriority.setSelected(true);
        } else {
            highPriority.setSelected(true);
        }
        if (props.isStgcmt()) {
            stgcmtYes.setSelected(true);
        } else {
            stgcmtNo.setSelected(true);
        }
        if (props.isTcpDelay()) {
            tcpdelayYes.setSelected(true);
        } else {
            tcpdelayNo.setSelected(true);
        }
        if (props.getTls().equals("aes")) {
            tlsaes.setSelected(true);
            tlsaesActionPerformed(null);
        } else if (props.getTls().equals("3des")) {
            tls3des.setSelected(true);
            tls3desActionPerformed(null);
        } else if (props.getTls().equals("without")) {
            tlswithout.setSelected(true);
            tlswithoutActionPerformed(null);
        } else {
            tlsno.setSelected(true);
            tlsnoActionPerformed(null);
        }
        if (props.isTs1()) {
            ts1Yes.setSelected(true);
        } else {
            ts1No.setSelected(true);
        }
        if (props.isUidnegrsp()) {
            uidnegrspYes.setSelected(true);
        } else {
            uidnegrspNo.setSelected(true);
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
                listenerAddressField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if ((props.getPort()).length() == 0) {
            valid = false;
            if (highlight) {
                listenerPortField.setBackground(UIConstants.INVALID_COLOR);
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
        listenerAddressField.setBackground(null);
        listenerPortField.setBackground(null);
        fileContentsTextPane.setBackground(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        priorityButtonGroup = new javax.swing.ButtonGroup();
        clientAuthTLSButtonGroup = new javax.swing.ButtonGroup();
        filrefButtonGroup = new javax.swing.ButtonGroup();
        ts1ButtonGroup = new javax.swing.ButtonGroup();
        nossl2ButtonGroup = new javax.swing.ButtonGroup();
        noclientauthButtonGroup = new javax.swing.ButtonGroup();
        tcpdelayButtonGroup = new javax.swing.ButtonGroup();
        tlsButtonGroup = new javax.swing.ButtonGroup();
        pdv1ButtonGroup = new javax.swing.ButtonGroup();
        uidnegrspButtonGroup = new javax.swing.ButtonGroup();
        stgcmtButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        listenerPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        listenerAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        fileContentsTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea(false,false);
        jLabel3 = new javax.swing.JLabel();
        accepttoField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        asyncField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel6 = new javax.swing.JLabel();
        bufsizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel7 = new javax.swing.JLabel();
        connecttoField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel8 = new javax.swing.JLabel();
        highPriority = new com.mirth.connect.client.ui.components.MirthRadioButton();
        mediumPriority = new com.mirth.connect.client.ui.components.MirthRadioButton();
        lowPriority = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel9 = new javax.swing.JLabel();
        keyStorePasswordField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel10 = new javax.swing.JLabel();
        keyStoreField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel11 = new javax.swing.JLabel();
        keyPasswordField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel12 = new javax.swing.JLabel();
        noclientauthYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        noclientauthNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel13 = new javax.swing.JLabel();
        nossl2Yes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        nossl2No = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel14 = new javax.swing.JLabel();
        usernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel15 = new javax.swing.JLabel();
        passcodeField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel16 = new javax.swing.JLabel();
        rcvpdulenField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel17 = new javax.swing.JLabel();
        pdv1Yes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        pdv1No = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel18 = new javax.swing.JLabel();
        reaperField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel19 = new javax.swing.JLabel();
        releasetoField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel20 = new javax.swing.JLabel();
        rsptoField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel21 = new javax.swing.JLabel();
        shutdowndelayField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel22 = new javax.swing.JLabel();
        sndpdulenField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel23 = new javax.swing.JLabel();
        soclosedelayField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel24 = new javax.swing.JLabel();
        sorcvbufField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel25 = new javax.swing.JLabel();
        sosndbufField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel26 = new javax.swing.JLabel();
        stgcmtYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        stgcmtNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel27 = new javax.swing.JLabel();
        tcpdelayYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tcpdelayNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel28 = new javax.swing.JLabel();
        tlswithout = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tls3des = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tlsaes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel29 = new javax.swing.JLabel();
        truststoreField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel30 = new javax.swing.JLabel();
        truststorepwField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel31 = new javax.swing.JLabel();
        ts1Yes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        ts1No = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel32 = new javax.swing.JLabel();
        uidnegrspYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        uidnegrspNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tlsno = new com.mirth.connect.client.ui.components.MirthRadioButton();
        applicationEntityField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        localAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        localPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        localApplicationEntityField = new com.mirth.connect.client.ui.components.MirthTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel1.setText("Remote Host:");

        jLabel2.setText("Remote Port:");

        listenerPortField.setToolTipText("Remote PORT to send to.");

        listenerAddressField.setToolTipText("Remote IP to send to.");

        fileContentsTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setText("Template:");

        accepttoField.setToolTipText("Timeout in ms for receiving A-ASSOCIATE-AC, 5000ms by default.");

        jLabel4.setText("Timeout A-ASSOCIATE-AC (ms):");

        jLabel5.setText("Max Async operations:");

        asyncField.setToolTipText("Maximum number of outstanding operations it may invoke asynchronously, unlimited by default.");

        jLabel6.setText("Transcoder Buffer Size (KB):");

        bufsizeField.setToolTipText("Transcoder buffer size in KB, 1KB by default.");

        jLabel7.setText("TCP Connection Timeout (ms):");

        connecttoField.setToolTipText("Timeout in ms for TCP connect, no timeout by default.");

        jLabel8.setText("Priority:");

        highPriority.setBackground(new java.awt.Color(255, 255, 255));
        highPriority.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        priorityButtonGroup.add(highPriority);
        highPriority.setText("High");
        highPriority.setToolTipText("Priority of the C-STORE operation, MEDIUM by default.");
        highPriority.setMargin(new java.awt.Insets(0, 0, 0, 0));
        highPriority.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                highPriorityActionPerformed(evt);
            }
        });

        mediumPriority.setBackground(new java.awt.Color(255, 255, 255));
        mediumPriority.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        priorityButtonGroup.add(mediumPriority);
        mediumPriority.setSelected(true);
        mediumPriority.setText("Medium");
        mediumPriority.setToolTipText("Priority of the C-STORE operation, MEDIUM by default.");
        mediumPriority.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mediumPriority.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediumPriorityActionPerformed(evt);
            }
        });

        lowPriority.setBackground(new java.awt.Color(255, 255, 255));
        lowPriority.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        priorityButtonGroup.add(lowPriority);
        lowPriority.setText("Low");
        lowPriority.setToolTipText("Priority of the C-STORE operation, MEDIUM by default.");
        lowPriority.setMargin(new java.awt.Insets(0, 0, 0, 0));
        lowPriority.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lowPriorityActionPerformed(evt);
            }
        });

        jLabel9.setText("Keystore Password:");

        keyStorePasswordField.setToolTipText("Password for keystore file.");

        jLabel10.setText("Keystore:");

        keyStoreField.setToolTipText("File path or URL of P12 or JKS keystore, resource:tls/test_sys_2.p12 by default.");

        jLabel11.setText("Key Password:");

        keyPasswordField.setToolTipText("Password for accessing the key in the keystore, keystore password by default.");

        jLabel12.setText("Client Authentication TLS:");

        noclientauthYes.setBackground(new java.awt.Color(255, 255, 255));
        noclientauthYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clientAuthTLSButtonGroup.add(noclientauthYes);
        noclientauthYes.setSelected(true);
        noclientauthYes.setText("Yes");
        noclientauthYes.setToolTipText("Enable client authentification for TLS.");
        noclientauthYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        noclientauthNo.setBackground(new java.awt.Color(255, 255, 255));
        noclientauthNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clientAuthTLSButtonGroup.add(noclientauthNo);
        noclientauthNo.setText("No");
        noclientauthNo.setToolTipText("Enable client authentification for TLS.");
        noclientauthNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel13.setText("Accept ssl v2 TLS handshake:");

        nossl2Yes.setBackground(new java.awt.Color(255, 255, 255));
        nossl2Yes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        nossl2ButtonGroup.add(nossl2Yes);
        nossl2Yes.setSelected(true);
        nossl2Yes.setText("Yes");
        nossl2Yes.setToolTipText("Enable acceptance of SSLv2Hello TLS handshake.");
        nossl2Yes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        nossl2No.setBackground(new java.awt.Color(255, 255, 255));
        nossl2No.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        nossl2ButtonGroup.add(nossl2No);
        nossl2No.setText("No");
        nossl2No.setToolTipText("Enable acceptance of SSLv2Hello TLS handshake.");
        nossl2No.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel14.setText("User Name:");

        usernameField.setToolTipText("Enable User Identity Negotiation with specified username and  optional passcode.");

        jLabel15.setText("Pass Code:");

        passcodeField.setToolTipText("Optional passcode for User Identity Negotiation, only effective with option -username.");

        jLabel16.setText("P-DATA-TF PDUs  max length received (KB):");

        rcvpdulenField.setToolTipText("Maximal length in KB of received P-DATA-TF PDUs, 16KB by default.");

        jLabel17.setText("Pack PDV:");

        pdv1Yes.setBackground(new java.awt.Color(255, 255, 255));
        pdv1Yes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pdv1ButtonGroup.add(pdv1Yes);
        pdv1Yes.setText("Yes");
        pdv1Yes.setToolTipText("Send only one PDV in one P-Data-TF PDU, pack command and data PDV in one P-DATA-TF PDU by default.");
        pdv1Yes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        pdv1No.setBackground(new java.awt.Color(255, 255, 255));
        pdv1No.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pdv1ButtonGroup.add(pdv1No);
        pdv1No.setSelected(true);
        pdv1No.setText("No");
        pdv1No.setToolTipText("Send only one PDV in one P-Data-TF PDU, pack command and data PDV in one P-DATA-TF PDU by default.");
        pdv1No.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel18.setText("DIMSE-RSP interval period (s):");

        reaperField.setToolTipText("Period in ms to check for outstanding DIMSE-RSP, 10s by default.");

        jLabel19.setText("A-RELEASE-RP timeout (s):");

        releasetoField.setToolTipText("Timeout in ms for receiving A-RELEASE-RP, 5s by default.");

        jLabel20.setText("DIMSE-RSP timeout (s):");

        rsptoField.setToolTipText("Timeout in ms for receiving DIMSE-RSP, 60s by default.");

        jLabel21.setText("Shutdown delay (ms):");

        shutdowndelayField.setToolTipText("Delay in ms for closing the listening socket, 1000ms by default.");

        jLabel22.setText("P-DATA-TF PDUs max length sent (KB):");

        sndpdulenField.setToolTipText("Maximal length in KB of sent P-DATA-TF PDUs, 16KB by default.");

        jLabel23.setText("Socket Close Delay After A-ABORT (ms):");

        soclosedelayField.setToolTipText("Delay in ms for Socket close after sending A-ABORT, 50ms by default.");

        jLabel24.setText("Receive Socket Buffer Size (KB):");

        sorcvbufField.setToolTipText("Set receive socket buffer to specified value in KB.");

        jLabel25.setText("Send Socket Buffer Size (KB):");

        sosndbufField.setToolTipText("Set send socket buffer to specified value in KB.");

        jLabel26.setText("Request Storage Commitment:");

        stgcmtYes.setBackground(new java.awt.Color(255, 255, 255));
        stgcmtYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        stgcmtButtonGroup.add(stgcmtYes);
        stgcmtYes.setSelected(true);
        stgcmtYes.setText("Yes");
        stgcmtYes.setToolTipText("Request storage commitment of (successfully) sent objects afterwards.");
        stgcmtYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        stgcmtNo.setBackground(new java.awt.Color(255, 255, 255));
        stgcmtNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        stgcmtButtonGroup.add(stgcmtNo);
        stgcmtNo.setText("No");
        stgcmtNo.setToolTipText("Request storage commitment of (successfully) sent objects afterwards.");
        stgcmtNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel27.setText("TCP Delay:");

        tcpdelayYes.setBackground(new java.awt.Color(255, 255, 255));
        tcpdelayYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tcpdelayButtonGroup.add(tcpdelayYes);
        tcpdelayYes.setSelected(true);
        tcpdelayYes.setText("Yes");
        tcpdelayYes.setToolTipText("Set TCP_NODELAY socket option to false, true by default.");
        tcpdelayYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        tcpdelayNo.setBackground(new java.awt.Color(255, 255, 255));
        tcpdelayNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tcpdelayButtonGroup.add(tcpdelayNo);
        tcpdelayNo.setText("No");
        tcpdelayNo.setToolTipText("Set TCP_NODELAY socket option to false, true by default.");
        tcpdelayNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel28.setText("TLS:");

        tlswithout.setBackground(new java.awt.Color(255, 255, 255));
        tlswithout.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tlsButtonGroup.add(tlswithout);
        tlswithout.setText("Without");
        tlswithout.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tlswithout.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlswithout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlswithoutActionPerformed(evt);
            }
        });

        tls3des.setBackground(new java.awt.Color(255, 255, 255));
        tls3des.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tlsButtonGroup.add(tls3des);
        tls3des.setText("3DES");
        tls3des.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tls3des.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tls3des.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tls3desActionPerformed(evt);
            }
        });

        tlsaes.setBackground(new java.awt.Color(255, 255, 255));
        tlsaes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tlsButtonGroup.add(tlsaes);
        tlsaes.setText("AES");
        tlsaes.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tlsaes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsaes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsaesActionPerformed(evt);
            }
        });

        jLabel29.setText("Trust Store:");

        truststoreField.setToolTipText("File path or URL of JKS truststore, resource:tls/mesa_certs.jks by default.");

        jLabel30.setText("Trust Store Password:");

        truststorepwField.setToolTipText("Password for truststore file.");

        jLabel31.setText("Default Presentation Syntax:");

        ts1Yes.setBackground(new java.awt.Color(255, 255, 255));
        ts1Yes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ts1ButtonGroup.add(ts1Yes);
        ts1Yes.setText("Yes");
        ts1Yes.setToolTipText("Offer Default Transfer Syntax in separate Presentation Context. By default offered with Explicit VR Little Endian TS in one PC.");
        ts1Yes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        ts1No.setBackground(new java.awt.Color(255, 255, 255));
        ts1No.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ts1ButtonGroup.add(ts1No);
        ts1No.setSelected(true);
        ts1No.setText("No");
        ts1No.setToolTipText("Offer Default Transfer Syntax in separate Presentation Context. By default offered with Explicit VR Little Endian TS in one PC.");
        ts1No.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel32.setText("Request Positive User Identity Response:");

        uidnegrspYes.setBackground(new java.awt.Color(255, 255, 255));
        uidnegrspYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        uidnegrspButtonGroup.add(uidnegrspYes);
        uidnegrspYes.setText("Yes");
        uidnegrspYes.setToolTipText("Request positive User Identity Negotation response, only effective with option -username.");
        uidnegrspYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        uidnegrspNo.setBackground(new java.awt.Color(255, 255, 255));
        uidnegrspNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        uidnegrspButtonGroup.add(uidnegrspNo);
        uidnegrspNo.setSelected(true);
        uidnegrspNo.setText("No");
        uidnegrspNo.setToolTipText("Request positive User Identity Negotation response, only effective with option -username.");
        uidnegrspNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        tlsno.setBackground(new java.awt.Color(255, 255, 255));
        tlsno.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tlsButtonGroup.add(tlsno);
        tlsno.setSelected(true);
        tlsno.setText("No TLS");
        tlsno.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tlsno.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsnoActionPerformed(evt);
            }
        });

        applicationEntityField.setToolTipText("Remote Application Entity");

        jLabel33.setText("Remote Application Entity:");

        jLabel34.setText("Local Host:");

        jLabel35.setText("Local Port:");

        jLabel36.setText("Local Application Entity:");

        localAddressField.setToolTipText("Local IP to send to.");

        localPortField.setToolTipText("Local PORT to send to.");

        localApplicationEntityField.setToolTipText("Local Application Entity");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15)
                    .addComponent(jLabel17)
                    .addComponent(jLabel19)
                    .addComponent(jLabel20)
                    .addComponent(jLabel18)
                    .addComponent(jLabel26)
                    .addComponent(jLabel32)
                    .addComponent(jLabel8)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel33)
                    .addComponent(jLabel5)
                    .addComponent(jLabel21)
                    .addComponent(jLabel23)
                    .addComponent(jLabel4)
                    .addComponent(jLabel7)
                    .addComponent(jLabel27)
                    .addComponent(jLabel31)
                    .addComponent(jLabel28)
                    .addComponent(jLabel12)
                    .addComponent(jLabel13)
                    .addComponent(jLabel10)
                    .addComponent(jLabel29)
                    .addComponent(jLabel11)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(keyStoreField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(truststoreField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel30, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(keyStorePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(truststorepwField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(keyPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(nossl2Yes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nossl2No, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(noclientauthYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(noclientauthNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tls3des, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tlsaes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tlswithout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tlsno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(ts1Yes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ts1No, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tcpdelayYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tcpdelayNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(connecttoField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(accepttoField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(applicationEntityField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel36))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(listenerPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel35))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(listenerAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(27, 27, 27)
                                        .addComponent(jLabel34)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(localApplicationEntityField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(localPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(localAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(asyncField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(highPriority, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mediumPriority, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lowPriority, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(passcodeField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(pdv1Yes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pdv1No, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(uidnegrspYes, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(uidnegrspNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(stgcmtYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(stgcmtNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(rsptoField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(releasetoField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(reaperField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel22)
                                            .addComponent(jLabel16)
                                            .addComponent(jLabel25)
                                            .addComponent(jLabel24)
                                            .addComponent(jLabel6)))
                                    .addComponent(shutdowndelayField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(soclosedelayField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(sosndbufField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(rcvpdulenField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(sndpdulenField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(sorcvbufField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bufsizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 43, Short.MAX_VALUE))
                    .addComponent(fileContentsTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(listenerAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel34)
                    .addComponent(localAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(listenerPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35)
                    .addComponent(localPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(applicationEntityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36)
                    .addComponent(localApplicationEntityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(asyncField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(highPriority, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mediumPriority, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lowPriority, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stgcmtYes, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stgcmtNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(passcodeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel17)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(uidnegrspYes, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(uidnegrspNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel32))
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(pdv1Yes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pdv1No, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18)
                            .addComponent(reaperField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel19)
                            .addComponent(releasetoField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(rsptoField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sndpdulenField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rcvpdulenField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sosndbufField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel21)
                            .addComponent(shutdowndelayField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(soclosedelayField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(accepttoField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(connecttoField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel27)
                            .addComponent(tcpdelayYes, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tcpdelayNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel31)
                            .addComponent(ts1Yes, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ts1No, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel28)
                            .addComponent(tls3des, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tlsaes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tlswithout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tlsno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(noclientauthYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(noclientauthNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(nossl2Yes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nossl2No, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sorcvbufField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel24))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bufsizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(keyStoreField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(keyStorePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(truststoreField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30)
                    .addComponent(truststorepwField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(keyPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(fileContentsTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tlsnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tlsnoActionPerformed
// TODO add your handling code here:
        // disable
        keyStoreField.setEnabled(false);
        keyPasswordField.setEnabled(false);
        keyStorePasswordField.setEnabled(false);
        truststoreField.setEnabled(false);
        truststorepwField.setEnabled(false);
        nossl2No.setEnabled(false);
        nossl2Yes.setEnabled(false);
        noclientauthNo.setEnabled(false);
        noclientauthYes.setEnabled(false);
        jLabel12.setEnabled(false);
        jLabel13.setEnabled(false);
        jLabel9.setEnabled(false);
        jLabel30.setEnabled(false);
        jLabel10.setEnabled(false);
        jLabel29.setEnabled(false);
        jLabel11.setEnabled(false);

    }//GEN-LAST:event_tlsnoActionPerformed

    private void tls3desActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tls3desActionPerformed
// TODO add your handling code here:
        keyStoreField.setEnabled(true);
        keyPasswordField.setEnabled(true);
        keyStorePasswordField.setEnabled(true);
        truststoreField.setEnabled(true);
        truststorepwField.setEnabled(true);
        nossl2No.setEnabled(true);
        nossl2Yes.setEnabled(true);
        noclientauthNo.setEnabled(true);
        noclientauthYes.setEnabled(true);
        jLabel12.setEnabled(true);
        jLabel13.setEnabled(true);
        jLabel9.setEnabled(true);
        jLabel30.setEnabled(true);
        jLabel10.setEnabled(true);
        jLabel29.setEnabled(true);
        jLabel11.setEnabled(true);
    }//GEN-LAST:event_tls3desActionPerformed

    private void tlsaesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tlsaesActionPerformed
// TODO add your handling code here:
        keyStoreField.setEnabled(true);
        keyPasswordField.setEnabled(true);
        keyStorePasswordField.setEnabled(true);
        truststoreField.setEnabled(true);
        truststorepwField.setEnabled(true);
        nossl2No.setEnabled(true);
        nossl2Yes.setEnabled(true);
        noclientauthNo.setEnabled(true);
        noclientauthYes.setEnabled(true);
        jLabel12.setEnabled(true);
        jLabel13.setEnabled(true);
        jLabel9.setEnabled(true);
        jLabel30.setEnabled(true);
        jLabel10.setEnabled(true);
        jLabel29.setEnabled(true);
        jLabel11.setEnabled(true);
    }//GEN-LAST:event_tlsaesActionPerformed

    private void tlswithoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tlswithoutActionPerformed
// TODO add your handling code here:
        keyStoreField.setEnabled(true);
        keyPasswordField.setEnabled(true);
        keyStorePasswordField.setEnabled(true);
        truststoreField.setEnabled(true);
        truststorepwField.setEnabled(true);
        nossl2No.setEnabled(true);
        nossl2Yes.setEnabled(true);
        noclientauthNo.setEnabled(true);
        noclientauthYes.setEnabled(true);
        jLabel12.setEnabled(true);
        jLabel13.setEnabled(true);
        jLabel9.setEnabled(true);
        jLabel30.setEnabled(true);
        jLabel10.setEnabled(true);
        jLabel29.setEnabled(true);
        jLabel11.setEnabled(true);
    }//GEN-LAST:event_tlswithoutActionPerformed

    private void lowPriorityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lowPriorityActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_lowPriorityActionPerformed

    private void mediumPriorityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mediumPriorityActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_mediumPriorityActionPerformed

    private void highPriorityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highPriorityActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_highPriorityActionPerformed

    private void ackOnNewConnectionNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ackOnNewConnectionNoActionPerformed
    {// GEN-HEADEREND:event_ackOnNewConnectionNoActionPerformed
    }// GEN-LAST:event_ackOnNewConnectionNoActionPerformed

    private void ackOnNewConnectionYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ackOnNewConnectionYesActionPerformed
    {// GEN-HEADEREND:event_ackOnNewConnectionYesActionPerformed
    }// GEN-LAST:event_ackOnNewConnection   YesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthTextField accepttoField;
    private com.mirth.connect.client.ui.components.MirthTextField applicationEntityField;
    private com.mirth.connect.client.ui.components.MirthTextField asyncField;
    private com.mirth.connect.client.ui.components.MirthTextField bufsizeField;
    private javax.swing.ButtonGroup clientAuthTLSButtonGroup;
    private com.mirth.connect.client.ui.components.MirthTextField connecttoField;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea fileContentsTextPane;
    private javax.swing.ButtonGroup filrefButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton highPriority;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private com.mirth.connect.client.ui.components.MirthTextField keyPasswordField;
    private com.mirth.connect.client.ui.components.MirthTextField keyStoreField;
    private com.mirth.connect.client.ui.components.MirthTextField keyStorePasswordField;
    private com.mirth.connect.client.ui.components.MirthTextField listenerAddressField;
    private com.mirth.connect.client.ui.components.MirthTextField listenerPortField;
    private com.mirth.connect.client.ui.components.MirthTextField localAddressField;
    private com.mirth.connect.client.ui.components.MirthTextField localApplicationEntityField;
    private com.mirth.connect.client.ui.components.MirthTextField localPortField;
    private com.mirth.connect.client.ui.components.MirthRadioButton lowPriority;
    private com.mirth.connect.client.ui.components.MirthRadioButton mediumPriority;
    private javax.swing.ButtonGroup noclientauthButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton noclientauthNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton noclientauthYes;
    private javax.swing.ButtonGroup nossl2ButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton nossl2No;
    private com.mirth.connect.client.ui.components.MirthRadioButton nossl2Yes;
    private com.mirth.connect.client.ui.components.MirthTextField passcodeField;
    private javax.swing.ButtonGroup pdv1ButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton pdv1No;
    private com.mirth.connect.client.ui.components.MirthRadioButton pdv1Yes;
    private javax.swing.ButtonGroup priorityButtonGroup;
    private com.mirth.connect.client.ui.components.MirthTextField rcvpdulenField;
    private com.mirth.connect.client.ui.components.MirthTextField reaperField;
    private com.mirth.connect.client.ui.components.MirthTextField releasetoField;
    private com.mirth.connect.client.ui.components.MirthTextField rsptoField;
    private com.mirth.connect.client.ui.components.MirthTextField shutdowndelayField;
    private com.mirth.connect.client.ui.components.MirthTextField sndpdulenField;
    private com.mirth.connect.client.ui.components.MirthTextField soclosedelayField;
    private com.mirth.connect.client.ui.components.MirthTextField sorcvbufField;
    private com.mirth.connect.client.ui.components.MirthTextField sosndbufField;
    private javax.swing.ButtonGroup stgcmtButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton stgcmtNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton stgcmtYes;
    private javax.swing.ButtonGroup tcpdelayButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton tcpdelayNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton tcpdelayYes;
    private com.mirth.connect.client.ui.components.MirthRadioButton tls3des;
    private javax.swing.ButtonGroup tlsButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton tlsaes;
    private com.mirth.connect.client.ui.components.MirthRadioButton tlsno;
    private com.mirth.connect.client.ui.components.MirthRadioButton tlswithout;
    private com.mirth.connect.client.ui.components.MirthTextField truststoreField;
    private com.mirth.connect.client.ui.components.MirthTextField truststorepwField;
    private javax.swing.ButtonGroup ts1ButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton ts1No;
    private com.mirth.connect.client.ui.components.MirthRadioButton ts1Yes;
    private javax.swing.ButtonGroup uidnegrspButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton uidnegrspNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton uidnegrspYes;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    // End of variables declaration//GEN-END:variables
}
