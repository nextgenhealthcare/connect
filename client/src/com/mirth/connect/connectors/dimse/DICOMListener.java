/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.dimse;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class DICOMListener extends ConnectorSettingsPanel {

    private Frame parent;
    private boolean tlsComponentsEnabled = true;

    public DICOMListener() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
    }

    @Override
    public String getConnectorName() {
        return new DICOMReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        DICOMReceiverProperties properties = new DICOMReceiverProperties();
        properties.setAsync(asyncField.getText());
        properties.setBufSize(bufsizeField.getText());

        properties.setPdv1(pdv1Yes.isSelected());
        properties.setBigEndian(bigendianYes.isSelected());
        properties.setDefts(deftsYes.isSelected());
        properties.setNativeData(nativeYes.isSelected());

        properties.setDest(destField.getText());
        properties.setRcvpdulen(rcvpdulenField.getText());
        properties.setSndpdulen(sndpdulenField.getText());
        properties.setReaper(reaperField.getText());
        properties.setReleaseTo(releasetoField.getText());
        properties.setRequestTo(requesttoField.getText());
        properties.setRspDelay(rspdelayField.getText());

        properties.setIdleTo(idletoField.getText());
        properties.setSoCloseDelay(soclosedelayField.getText());
        properties.setSorcvbuf(sorcvbufField.getText());
        properties.setSosndbuf(sosndbufField.getText());
        properties.setTcpDelay(tcpdelayYes.isSelected());

        properties.setKeyPW(keyPasswordField.getText());
        properties.setKeyStore(keyStoreField.getText());
        properties.setKeyStorePW(keyStorePasswordField.getText());

        properties.setNoClientAuth(clientAuthenticationYesRadio.isSelected());
        properties.setNossl2(acceptSSLv2YesRadio.isSelected());

        if (tlsAESRadio.isSelected()) {
            properties.setTls("aes");
        } else if (tls3DESRadio.isSelected()) {
            properties.setTls("3des");
        } else if (tlsWithoutRadio.isSelected()) {
            properties.setTls("without");
        } else if (tlsBCPRadio.isSelected()) {
            properties.setTls("bcp");
        } else if (tlsNonBCPRadio.isSelected()) {
            properties.setTls("non_bcp");
        } else {
            properties.setTls("notls");
        }

        properties.setTrustStore(trustStoreField.getText());
        properties.setTrustStorePW(trustStorePasswordField.getText());
        properties.setApplicationEntity(applicationEntityField.getText());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        DICOMReceiverProperties props = (DICOMReceiverProperties) properties;

        asyncField.setText(props.getAsync());
        bufsizeField.setText(props.getBufSize());
        rcvpdulenField.setText(props.getRcvpdulen());
        reaperField.setText(props.getReaper());
        releasetoField.setText(props.getReleaseTo());
        requesttoField.setText(props.getRequestTo());
        idletoField.setText(props.getIdleTo());
        rspdelayField.setText(props.getRspDelay());
        sndpdulenField.setText(props.getSndpdulen());
        soclosedelayField.setText(props.getSoCloseDelay());
        sorcvbufField.setText(props.getSorcvbuf());
        sosndbufField.setText(props.getSosndbuf());
        destField.setText(props.getDest());

        if (props.isPdv1()) {
            pdv1Yes.setSelected(true);
        } else {
            pdv1No.setSelected(true);
        }
        if (props.isBigEndian()) {
            bigendianYes.setSelected(true);
            bigendianYesActionPerformed(null);
        } else {
            bigendianNo.setSelected(true);
            bigendianNoActionPerformed(null);
        }
        if (props.isDefts()) {
            deftsYes.setSelected(true);
            deftsYesActionPerformed(null);
        } else {
            deftsNo.setSelected(true);
            deftsNoActionPerformed(null);
        }
        if (props.isNativeData()) {
            nativeYes.setSelected(true);
            nativeYesActionPerformed(null);
        } else {
            nativeNo.setSelected(true);
            nativeNoActionPerformed(null);
        }
        if (props.isTcpDelay()) {
            tcpdelayYes.setSelected(true);
        } else {
            tcpdelayNo.setSelected(true);
        }

        keyPasswordField.setText(props.getKeyPW());
        keyStoreField.setText(props.getKeyStore());
        keyStorePasswordField.setText(props.getKeyStorePW());
        trustStoreField.setText(props.getTrustStore());
        trustStorePasswordField.setText(props.getTrustStorePW());
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
        if (props.getTls().equals("aes")) {
            tlsAESRadio.setSelected(true);
            tlsAESRadioActionPerformed(null);
        } else if (props.getTls().equals("3des")) {
            tls3DESRadio.setSelected(true);
            tls3DESRadioActionPerformed(null);
        } else if (props.getTls().equals("without")) {
            tlsWithoutRadio.setSelected(true);
            tlsWithoutRadioActionPerformed(null);
        } else if (props.getTls().equals("bcp")) {
            tlsBCPRadio.setSelected(true);
            tlsBcpRadioActionPerformed(null);
        } else if (props.getTls().equals("non_bcp")) {
            tlsNonBCPRadio.setSelected(true);
            tlsNonBcpRadioActionPerformed(null);
        } else {
            tlsNoRadio.setSelected(true);
            tlsNoRadioActionPerformed(null);
        }
        applicationEntityField.setText(props.getApplicationEntity());
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new DICOMReceiverProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        DICOMReceiverProperties props = (DICOMReceiverProperties) properties;

        boolean valid = true;

        return valid;
    }

    @Override
    public void resetInvalidProperties() {}

    public void enableTLSComponents() {
        tlsComponentsEnabled = true;
        tlsLabel.setEnabled(true);
        tls3DESRadio.setEnabled(true);
        tlsAESRadio.setEnabled(true);
        tlsWithoutRadio.setEnabled(true);
        tlsBCPRadio.setEnabled(true);
        tlsNonBCPRadio.setEnabled(true);
        tlsNoRadio.setEnabled(true);
        if (tls3DESRadio.isSelected()) {
            tls3DESRadioActionPerformed(null);
        } else if (tlsAESRadio.isSelected()) {
            tlsAESRadioActionPerformed(null);
        } else if (tlsWithoutRadio.isSelected()) {
            tlsWithoutRadioActionPerformed(null);
        } else if (tlsBCPRadio.isSelected()) {
            tlsBcpRadioActionPerformed(null);
        } else if (tlsNonBCPRadio.isSelected()) {
            tlsNonBcpRadioActionPerformed(null);
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
        tlsBCPRadio.setEnabled(false);
        tlsNonBCPRadio.setEnabled(false);
        tlsNoRadio.setEnabled(false);
        tlsNoRadioActionPerformed(null);
    }

    // @formatter:off
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
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        buttonGroup8 = new javax.swing.ButtonGroup();
        jLabel5 = new javax.swing.JLabel();
        asyncField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel17 = new javax.swing.JLabel();
        pdv1Yes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        pdv1No = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel18 = new javax.swing.JLabel();
        reaperField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel19 = new javax.swing.JLabel();
        releasetoField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel23 = new javax.swing.JLabel();
        soclosedelayField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        sndpdulenField = new com.mirth.connect.client.ui.components.MirthTextField();
        rcvpdulenField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        sorcvbufField = new com.mirth.connect.client.ui.components.MirthTextField();
        sosndbufField = new com.mirth.connect.client.ui.components.MirthTextField();
        bufsizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel27 = new javax.swing.JLabel();
        tcpdelayYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tcpdelayNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel20 = new javax.swing.JLabel();
        requesttoField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel21 = new javax.swing.JLabel();
        idletoField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel26 = new javax.swing.JLabel();
        rspdelayField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel28 = new javax.swing.JLabel();
        bigendianYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        bigendianNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel29 = new javax.swing.JLabel();
        deftsYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        deftsNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel30 = new javax.swing.JLabel();
        destField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel31 = new javax.swing.JLabel();
        nativeYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        nativeNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tlsLabel = new javax.swing.JLabel();
        tls3DESRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tlsAESRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tlsWithoutRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tlsBCPRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tlsNonBCPRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tlsNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        clientAuthenticationLabel = new javax.swing.JLabel();
        clientAuthenticationYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        clientAuthenticationNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        acceptSSLv2Label = new javax.swing.JLabel();
        acceptSSLv2YesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        acceptSSLv2NoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        keyStoreLabel = new javax.swing.JLabel();
        keyStoreField = new com.mirth.connect.client.ui.components.MirthTextField();
        trustStoreLabel = new javax.swing.JLabel();
        trustStoreField = new com.mirth.connect.client.ui.components.MirthTextField();
        keyStorePasswordLabel = new javax.swing.JLabel();
        keyStorePasswordField = new com.mirth.connect.client.ui.components.MirthTextField();
        trustStorePasswordLabel = new javax.swing.JLabel();
        trustStorePasswordField = new com.mirth.connect.client.ui.components.MirthTextField();
        keyPasswordLabel = new javax.swing.JLabel();
        keyPasswordField = new com.mirth.connect.client.ui.components.MirthTextField();
        applicationEntityField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel3 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setToolTipText("");

        jLabel5.setText("Max Async operations:");

        asyncField.setToolTipText("Maximum number of outstanding operations performed asynchronously, unlimited by default.");

        jLabel17.setText("Pack PDV:");

        pdv1Yes.setBackground(new java.awt.Color(255, 255, 255));
        pdv1Yes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(pdv1Yes);
        pdv1Yes.setText("Yes");
        pdv1Yes.setToolTipText("Send only one PDV in one P-Data-TF PDU, pack command and data PDV in one P-DATA-TF PDU by default.");
        pdv1Yes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        pdv1No.setBackground(new java.awt.Color(255, 255, 255));
        pdv1No.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(pdv1No);
        pdv1No.setSelected(true);
        pdv1No.setText("No");
        pdv1No.setToolTipText("Send only one PDV in one P-Data-TF PDU, pack command and data PDV in one P-DATA-TF PDU by default.");
        pdv1No.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel18.setText("DIMSE-RSP interval period (s):");

        reaperField.setToolTipText("Period in ms to check for outstanding DIMSE-RSP, 10s by default");

        jLabel19.setText("A-RELEASE-RP timeout (s):");

        releasetoField.setToolTipText("Timeout in ms for receiving A-RELEASE-RP, 5s by default.");

        jLabel23.setText("Socket Close Delay After A-ABORT (ms):");

        soclosedelayField.setToolTipText("Delay in ms for Socket close after sending A-ABORT, 50ms by default.");

        jLabel22.setText("Receive Socket Buffer Size (KB):");

        jLabel16.setText("P-DATA-TF PDUs max length sent (KB):");

        sndpdulenField.setToolTipText("Maximal length in KB of sent P-DATA-TF PDUs, 16KB by default.");

        rcvpdulenField.setToolTipText("Maximal length in KB of received P-DATA-TF PDUs, 16KB by default.");

        jLabel6.setText("Transcoder Buffer Size (KB):");

        jLabel25.setText("Send Socket Buffer Size (KB):");

        jLabel24.setText("P-DATA-TF PDUs max length received (KB):");

        sorcvbufField.setToolTipText("Set receive socket buffer to specified value in KB");

        sosndbufField.setToolTipText("Set send socket buffer to specified value in KB");

        bufsizeField.setToolTipText("Minimal buffer size to write received object to file, 1KB by default.");

        jLabel27.setText("TCP Delay:");

        tcpdelayYes.setBackground(new java.awt.Color(255, 255, 255));
        tcpdelayYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(tcpdelayYes);
        tcpdelayYes.setSelected(true);
        tcpdelayYes.setText("Yes");
        tcpdelayYes.setToolTipText("Set TCP_NODELAY socket option to false, true by default.");
        tcpdelayYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        tcpdelayNo.setBackground(new java.awt.Color(255, 255, 255));
        tcpdelayNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(tcpdelayNo);
        tcpdelayNo.setText("No");
        tcpdelayNo.setToolTipText("Set TCP_NODELAY socket option to false, true by default.");
        tcpdelayNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel20.setText("ASSOCIATE-RQ timeout (ms):");

        requesttoField.setToolTipText("Timeout in ms for receiving -ASSOCIATE-RQ, 5s by default.");

        jLabel21.setText("DIMSE-RQ timeout (ms):");

        idletoField.setToolTipText("Timeout in ms for receiving DIMSE-RQ, 60s by default.");

        jLabel26.setText("DIMSE-RSP delay (ms):");

        rspdelayField.setToolTipText("Delay in ms for DIMSE-RSP; useful for testing asynchronous mode.");

        jLabel28.setText("Accept Explict VR Big Endian:");

        bigendianYes.setBackground(new java.awt.Color(255, 255, 255));
        bigendianYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(bigendianYes);
        bigendianYes.setText("Yes");
        bigendianYes.setToolTipText("Accept also Explict VR Big Endian transfer syntax.");
        bigendianYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bigendianYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bigendianYesActionPerformed(evt);
            }
        });

        bigendianNo.setBackground(new java.awt.Color(255, 255, 255));
        bigendianNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(bigendianNo);
        bigendianNo.setSelected(true);
        bigendianNo.setText("No");
        bigendianNo.setToolTipText("Accept also Explict VR Big Endian transfer syntax.");
        bigendianNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bigendianNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bigendianNoActionPerformed(evt);
            }
        });

        jLabel29.setText("Only Accept Default Transfer Syntax:");

        deftsYes.setBackground(new java.awt.Color(255, 255, 255));
        deftsYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(deftsYes);
        deftsYes.setText("Yes");
        deftsYes.setToolTipText("Accept only default transfer syntax.");
        deftsYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deftsYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deftsYesActionPerformed(evt);
            }
        });

        deftsNo.setBackground(new java.awt.Color(255, 255, 255));
        deftsNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(deftsNo);
        deftsNo.setSelected(true);
        deftsNo.setText("No");
        deftsNo.setToolTipText("Accept only default transfer syntax.");
        deftsNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deftsNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deftsNoActionPerformed(evt);
            }
        });

        jLabel30.setText("Store Recieved Objects in Directory:");

        destField.setToolTipText("Store received objects into files in specified directory <dir>. Do not store received objects by default.");

        jLabel31.setText("Only Uncompressed Pixel Data:");

        nativeYes.setBackground(new java.awt.Color(255, 255, 255));
        nativeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(nativeYes);
        nativeYes.setText("Yes");
        nativeYes.setToolTipText("Accept only transfer syntax with uncompressed pixel data.");
        nativeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nativeYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nativeYesActionPerformed(evt);
            }
        });

        nativeNo.setBackground(new java.awt.Color(255, 255, 255));
        nativeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(nativeNo);
        nativeNo.setSelected(true);
        nativeNo.setText("No");
        nativeNo.setToolTipText("Accept only transfer syntax with uncompressed pixel data.");
        nativeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nativeNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nativeNoActionPerformed(evt);
            }
        });

        tlsLabel.setText("TLS:");

        tls3DESRadio.setBackground(new java.awt.Color(255, 255, 255));
        tls3DESRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tls3DESRadio);
        tls3DESRadio.setText("3DES");
        tls3DESRadio.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tls3DESRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tls3DESRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tls3DESRadioActionPerformed(evt);
            }
        });

        tlsAESRadio.setBackground(new java.awt.Color(255, 255, 255));
        tlsAESRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tlsAESRadio);
        tlsAESRadio.setText("AES");
        tlsAESRadio.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tlsAESRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsAESRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsAESRadioActionPerformed(evt);
            }
        });

        tlsBCPRadio.setBackground(new java.awt.Color(255, 255, 255));
        tlsBCPRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tlsBCPRadio);
        tlsBCPRadio.setText("BCP 195");
        tlsBCPRadio.setToolTipText("BCP 195 profile.");
        tlsBCPRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsBCPRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsBcpRadioActionPerformed(evt);
            }
        });

        tlsNonBCPRadio.setBackground(new java.awt.Color(255, 255, 255));
        tlsNonBCPRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tlsNonBCPRadio);
        tlsNonBCPRadio.setText("BCP 195 Non Downgrading");
        tlsNonBCPRadio.setToolTipText("BCP 195 Non Downgrading profile.");
        tlsNonBCPRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsNonBCPRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsNonBcpRadioActionPerformed(evt);
            }
        });

        tlsWithoutRadio.setBackground(new java.awt.Color(255, 255, 255));
        tlsWithoutRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tlsWithoutRadio);
        tlsWithoutRadio.setText("Without");
        tlsWithoutRadio.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tlsWithoutRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsWithoutRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsWithoutRadioActionPerformed(evt);
            }
        });

        tlsNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        tlsNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tlsNoRadio);
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
        clientAuthenticationYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(clientAuthenticationYesRadio);
        clientAuthenticationYesRadio.setSelected(true);
        clientAuthenticationYesRadio.setText("Yes");
        clientAuthenticationYesRadio.setToolTipText("Enable client authentification for TLS.");
        clientAuthenticationYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        clientAuthenticationNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        clientAuthenticationNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(clientAuthenticationNoRadio);
        clientAuthenticationNoRadio.setText("No");
        clientAuthenticationNoRadio.setToolTipText("Enable client authentification for TLS.");
        clientAuthenticationNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        acceptSSLv2Label.setText("Accept ssl v2 TLS handshake:");

        acceptSSLv2YesRadio.setBackground(new java.awt.Color(255, 255, 255));
        acceptSSLv2YesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup8.add(acceptSSLv2YesRadio);
        acceptSSLv2YesRadio.setSelected(true);
        acceptSSLv2YesRadio.setText("Yes");
        acceptSSLv2YesRadio.setToolTipText("Enable acceptance of SSLv2Hello TLS handshake.");
        acceptSSLv2YesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        acceptSSLv2NoRadio.setBackground(new java.awt.Color(255, 255, 255));
        acceptSSLv2NoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup8.add(acceptSSLv2NoRadio);
        acceptSSLv2NoRadio.setText("No");
        acceptSSLv2NoRadio.setToolTipText("Enable acceptance of SSLv2Hello TLS handshake.");
        acceptSSLv2NoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        keyStoreLabel.setText("Keystore:");

        keyStoreField.setToolTipText("File path or URL of P12 or JKS keystore, resource:tls/test_sys_2.p12 by default.");

        trustStoreLabel.setText("Trust Store:");

        trustStoreField.setToolTipText("File path or URL of JKS truststore, resource:tls/mesa_certs.jks by default.");

        keyStorePasswordLabel.setText("Keystore Password:");

        keyStorePasswordField.setToolTipText("Password for keystore file.");

        trustStorePasswordLabel.setText("Trust Store Password:");

        trustStorePasswordField.setToolTipText("Password for truststore file.");

        keyPasswordLabel.setText("Key Password:");

        keyPasswordField.setToolTipText("Password for accessing the key in the keystore.");

        applicationEntityField.setToolTipText("If specified, only requests with a matching called AE title will be accepted");

        jLabel3.setText("Application Entity:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(keyPasswordLabel)
                    .addComponent(acceptSSLv2Label)
                    .addComponent(jLabel28)
                    .addComponent(jLabel29)
                    .addComponent(jLabel31)
                    .addComponent(jLabel27)
                    .addComponent(jLabel30)
                    .addComponent(jLabel5)
                    .addComponent(jLabel17)
                    .addComponent(tlsLabel)
                    .addComponent(clientAuthenticationLabel)
                    .addComponent(keyStoreLabel)
                    .addComponent(trustStoreLabel)
                    .addComponent(jLabel18)
                    .addComponent(jLabel19)
                    .addComponent(jLabel23)
                    .addComponent(jLabel20)
                    .addComponent(jLabel21)
                    .addComponent(jLabel26))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nativeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nativeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(rspdelayField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(asyncField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pdv1Yes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pdv1No, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(clientAuthenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clientAuthenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(acceptSSLv2YesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(acceptSSLv2NoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tcpdelayYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tcpdelayNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bigendianYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bigendianNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(deftsYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deftsNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(destField, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(applicationEntityField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keyPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tls3DESRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tlsAESRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(tlsNonBCPRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(tlsBCPRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tlsWithoutRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tlsNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(reaperField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(soclosedelayField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(requesttoField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(idletoField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(releasetoField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel22)
                            .addComponent(jLabel6)
                            .addComponent(jLabel25)
                            .addComponent(jLabel24)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bufsizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sorcvbufField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sosndbufField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rcvpdulenField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sndpdulenField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(trustStoreField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(keyStoreField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(keyStorePasswordLabel)
                            .addComponent(trustStorePasswordLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(trustStorePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(keyStorePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(applicationEntityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(asyncField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pdv1Yes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(pdv1No, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(reaperField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(releasetoField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(soclosedelayField, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel23))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(requesttoField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel20))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(idletoField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel21))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rspdelayField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sndpdulenField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rcvpdulenField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel24))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sosndbufField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sorcvbufField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(bufsizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(bigendianYes, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bigendianNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deftsYes, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29)
                    .addComponent(deftsNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nativeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nativeYes, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel31))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tcpdelayYes, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27)
                    .addComponent(tcpdelayNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(destField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tlsLabel)
                    .addComponent(tls3DESRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tlsAESRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tlsNonBCPRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tlsBCPRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tlsWithoutRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tlsNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(clientAuthenticationLabel)
                            .addComponent(clientAuthenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(clientAuthenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(acceptSSLv2Label)
                            .addComponent(acceptSSLv2YesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(acceptSSLv2NoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(keyStoreLabel)
                            .addComponent(keyStoreField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(trustStoreLabel)
                            .addComponent(trustStoreField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(keyPasswordLabel)
                            .addComponent(keyPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(keyStorePasswordLabel)
                            .addComponent(keyStorePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(trustStorePasswordLabel)
                            .addComponent(trustStorePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 106, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // @formatter:on

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
    }//GEN-LAST:event_tlsNoRadioActionPerformed

    private void tlsWithoutRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tlsWithoutRadioActionPerformed
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
    }//GEN-LAST:event_tlsWithoutRadioActionPerformed

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
    }//GEN-LAST:event_tlsAESRadioActionPerformed

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
    }//GEN-LAST:event_tls3DESRadioActionPerformed

    private void tlsBcpRadioActionPerformed(java.awt.event.ActionEvent evt) {
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

    private void tlsNonBcpRadioActionPerformed(java.awt.event.ActionEvent evt) {
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

    private void nativeNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nativeNoActionPerformed
        if (bigendianNo.isSelected()) {
            jLabel29.setEnabled(true);
            deftsYes.setEnabled(true);
            deftsNo.setEnabled(true);
        }
    }//GEN-LAST:event_nativeNoActionPerformed

    private void deftsNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deftsNoActionPerformed
        jLabel31.setEnabled(true);
        nativeYes.setEnabled(true);
        nativeNo.setEnabled(true);
        jLabel28.setEnabled(true);
        bigendianYes.setEnabled(true);
        bigendianNo.setEnabled(true);

    }//GEN-LAST:event_deftsNoActionPerformed

    private void bigendianNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bigendianNoActionPerformed
        if (nativeNo.isSelected()) {
            jLabel29.setEnabled(true);
            deftsYes.setEnabled(true);
            deftsNo.setEnabled(true);
        }
    }//GEN-LAST:event_bigendianNoActionPerformed

    private void bigendianYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bigendianYesActionPerformed
        jLabel29.setEnabled(false);
        deftsYes.setEnabled(false);
        deftsNo.setEnabled(false);
        deftsNo.setSelected(true);
    }//GEN-LAST:event_bigendianYesActionPerformed

    private void deftsYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deftsYesActionPerformed
        jLabel31.setEnabled(false);
        nativeYes.setEnabled(false);
        nativeNo.setEnabled(false);
        nativeNo.setSelected(true);
        jLabel28.setEnabled(false);
        bigendianYes.setEnabled(false);
        bigendianNo.setEnabled(false);
        bigendianNo.setSelected(true);

    }//GEN-LAST:event_deftsYesActionPerformed

    private void nativeYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nativeYesActionPerformed
        jLabel29.setEnabled(false);
        deftsYes.setEnabled(false);
        deftsNo.setEnabled(false);
        deftsNo.setSelected(true);

    }//GEN-LAST:event_nativeYesActionPerformed

    private void ackOnNewConnectionNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ackOnNewConnectionNoActionPerformed
    {// GEN-HEADEREND:event_ackOnNewConnectionNoActionPerformed
    }// GEN-LAST:event_ackOnNewConnectionNoActionPerformed

    private void ackOnNewConnectionYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ackOnNewConnectionYesActionPerformed
    {// GEN-HEADEREND:event_ackOnNewConnectionYesActionPerformed
    }// GEN-LAST:event_ackOnNewConnection   YesActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables

    private javax.swing.JLabel acceptSSLv2Label;
    private com.mirth.connect.client.ui.components.MirthRadioButton acceptSSLv2NoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton acceptSSLv2YesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField applicationEntityField;
    private com.mirth.connect.client.ui.components.MirthTextField asyncField;
    private com.mirth.connect.client.ui.components.MirthRadioButton bigendianNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton bigendianYes;
    private com.mirth.connect.client.ui.components.MirthTextField bufsizeField;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.ButtonGroup buttonGroup8;
    private javax.swing.JLabel clientAuthenticationLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton clientAuthenticationNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton clientAuthenticationYesRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton deftsNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton deftsYes;
    private com.mirth.connect.client.ui.components.MirthTextField destField;
    private com.mirth.connect.client.ui.components.MirthTextField idletoField;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
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
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private com.mirth.connect.client.ui.components.MirthTextField keyPasswordField;
    private javax.swing.JLabel keyPasswordLabel;
    private com.mirth.connect.client.ui.components.MirthTextField keyStoreField;
    private javax.swing.JLabel keyStoreLabel;
    private com.mirth.connect.client.ui.components.MirthTextField keyStorePasswordField;
    private javax.swing.JLabel keyStorePasswordLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton nativeNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton nativeYes;
    private com.mirth.connect.client.ui.components.MirthRadioButton pdv1No;
    private com.mirth.connect.client.ui.components.MirthRadioButton pdv1Yes;
    private com.mirth.connect.client.ui.components.MirthTextField rcvpdulenField;
    private com.mirth.connect.client.ui.components.MirthTextField reaperField;
    private com.mirth.connect.client.ui.components.MirthTextField releasetoField;
    private com.mirth.connect.client.ui.components.MirthTextField requesttoField;
    private com.mirth.connect.client.ui.components.MirthTextField rspdelayField;
    private com.mirth.connect.client.ui.components.MirthTextField sndpdulenField;
    private com.mirth.connect.client.ui.components.MirthTextField soclosedelayField;
    private com.mirth.connect.client.ui.components.MirthTextField sorcvbufField;
    private com.mirth.connect.client.ui.components.MirthTextField sosndbufField;
    private com.mirth.connect.client.ui.components.MirthRadioButton tcpdelayNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton tcpdelayYes;
    private com.mirth.connect.client.ui.components.MirthRadioButton tls3DESRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton tlsAESRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton tlsBCPRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton tlsNonBCPRadio;
    private javax.swing.JLabel tlsLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton tlsNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton tlsWithoutRadio;
    private com.mirth.connect.client.ui.components.MirthTextField trustStoreField;
    private javax.swing.JLabel trustStoreLabel;
    private com.mirth.connect.client.ui.components.MirthTextField trustStorePasswordField;
    private javax.swing.JLabel trustStorePasswordLabel;
    // End of variables declaration//GEN-END:variables
}
