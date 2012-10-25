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
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class DICOMListener extends ConnectorSettingsPanel {

    private Frame parent;
    
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
        
        properties.setNoClientAuth(noclientauthYes.isSelected());
        properties.setNossl2(nossl2Yes.isSelected());

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
        truststoreField.setText(props.getTrustStore());
        truststorepwField.setText(props.getTrustStorePW());
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
    public void resetInvalidProperties() {
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
        jLabel32 = new javax.swing.JLabel();
        tls3des = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tlsaes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tlswithout = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tlsno = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel12 = new javax.swing.JLabel();
        noclientauthYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        noclientauthNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel13 = new javax.swing.JLabel();
        nossl2Yes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        nossl2No = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel10 = new javax.swing.JLabel();
        keyStoreField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel33 = new javax.swing.JLabel();
        truststoreField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel9 = new javax.swing.JLabel();
        keyStorePasswordField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel34 = new javax.swing.JLabel();
        truststorepwField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel11 = new javax.swing.JLabel();
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

        jLabel32.setText("TLS:");

        tls3des.setBackground(new java.awt.Color(255, 255, 255));
        tls3des.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tls3des);
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
        buttonGroup6.add(tlsaes);
        tlsaes.setText("AES");
        tlsaes.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tlsaes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsaes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsaesActionPerformed(evt);
            }
        });

        tlswithout.setBackground(new java.awt.Color(255, 255, 255));
        tlswithout.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tlswithout);
        tlswithout.setText("Without");
        tlswithout.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tlswithout.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlswithout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlswithoutActionPerformed(evt);
            }
        });

        tlsno.setBackground(new java.awt.Color(255, 255, 255));
        tlsno.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(tlsno);
        tlsno.setSelected(true);
        tlsno.setText("No TLS");
        tlsno.setToolTipText("Enable TLS connection without, 3DES or AES encryption.");
        tlsno.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tlsno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsnoActionPerformed(evt);
            }
        });

        jLabel12.setText("Client Authentication TLS:");

        noclientauthYes.setBackground(new java.awt.Color(255, 255, 255));
        noclientauthYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(noclientauthYes);
        noclientauthYes.setSelected(true);
        noclientauthYes.setText("Yes");
        noclientauthYes.setToolTipText("Enable client authentification for TLS.");
        noclientauthYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        noclientauthNo.setBackground(new java.awt.Color(255, 255, 255));
        noclientauthNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(noclientauthNo);
        noclientauthNo.setText("No");
        noclientauthNo.setToolTipText("Enable client authentification for TLS.");
        noclientauthNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel13.setText("Accept ssl v2 TLS handshake:");

        nossl2Yes.setBackground(new java.awt.Color(255, 255, 255));
        nossl2Yes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup8.add(nossl2Yes);
        nossl2Yes.setSelected(true);
        nossl2Yes.setText("Yes");
        nossl2Yes.setToolTipText("Enable acceptance of SSLv2Hello TLS handshake.");
        nossl2Yes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        nossl2No.setBackground(new java.awt.Color(255, 255, 255));
        nossl2No.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup8.add(nossl2No);
        nossl2No.setText("No");
        nossl2No.setToolTipText("Enable acceptance of SSLv2Hello TLS handshake.");
        nossl2No.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel10.setText("Keystore:");

        keyStoreField.setToolTipText("File path or URL of P12 or JKS keystore, resource:tls/test_sys_2.p12 by default.");

        jLabel33.setText("Trust Store:");

        truststoreField.setToolTipText("File path or URL of JKS truststore, resource:tls/mesa_certs.jks by default.");

        jLabel9.setText("Keystore Password:");

        keyStorePasswordField.setToolTipText("Password for keystore file.");

        jLabel34.setText("Trust Store Password:");

        truststorepwField.setToolTipText("Password for truststore file.");

        jLabel11.setText("Key Password:");

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
                    .addComponent(jLabel11)
                    .addComponent(jLabel13)
                    .addComponent(jLabel28)
                    .addComponent(jLabel29)
                    .addComponent(jLabel31)
                    .addComponent(jLabel27)
                    .addComponent(jLabel30)
                    .addComponent(jLabel5)
                    .addComponent(jLabel17)
                    .addComponent(jLabel32)
                    .addComponent(jLabel12)
                    .addComponent(jLabel10)
                    .addComponent(jLabel33)
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
                        .addComponent(noclientauthYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(noclientauthNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nossl2Yes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nossl2No, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addComponent(tls3des, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tlsaes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tlswithout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tlsno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                            .addComponent(truststoreField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(keyStoreField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel34))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(truststorepwField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(jLabel32)
                    .addComponent(tls3des, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tlsaes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tlswithout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tlsno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(noclientauthYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(noclientauthNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(nossl2Yes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nossl2No, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(keyStoreField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel33)
                            .addComponent(truststoreField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(keyPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(keyStorePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel34)
                            .addComponent(truststorepwField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 106, Short.MAX_VALUE))
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
        jLabel33.setEnabled(false);
        jLabel34.setEnabled(false);
    }//GEN-LAST:event_tlsnoActionPerformed

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
        jLabel33.setEnabled(true);
        jLabel34.setEnabled(true);
    }//GEN-LAST:event_tlswithoutActionPerformed

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
        jLabel33.setEnabled(true);
        jLabel34.setEnabled(true);
    }//GEN-LAST:event_tlsaesActionPerformed

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
        jLabel33.setEnabled(true);
        jLabel34.setEnabled(true);
    }//GEN-LAST:event_tls3desActionPerformed

    private void nativeNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nativeNoActionPerformed
// TODO add your handling code here:
        if (bigendianNo.isSelected()) {
            jLabel29.setEnabled(true);
            deftsYes.setEnabled(true);
            deftsNo.setEnabled(true);
        }
    }//GEN-LAST:event_nativeNoActionPerformed

    private void deftsNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deftsNoActionPerformed
// TODO add your handling code here:
        jLabel31.setEnabled(true);
        nativeYes.setEnabled(true);
        nativeNo.setEnabled(true);
        jLabel28.setEnabled(true);
        bigendianYes.setEnabled(true);
        bigendianNo.setEnabled(true);

    }//GEN-LAST:event_deftsNoActionPerformed

    private void bigendianNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bigendianNoActionPerformed
// TODO add your handling code here:
        if (nativeNo.isSelected()) {
            jLabel29.setEnabled(true);
            deftsYes.setEnabled(true);
            deftsNo.setEnabled(true);
        }
    }//GEN-LAST:event_bigendianNoActionPerformed

    private void bigendianYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bigendianYesActionPerformed
// TODO add your handling code here:
        jLabel29.setEnabled(false);
        deftsYes.setEnabled(false);
        deftsNo.setEnabled(false);
        deftsNo.setSelected(true);
    }//GEN-LAST:event_bigendianYesActionPerformed

    private void deftsYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deftsYesActionPerformed
// TODO add your handling code here:
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
// TODO add your handling code here:
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
    private com.mirth.connect.client.ui.components.MirthRadioButton deftsNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton deftsYes;
    private com.mirth.connect.client.ui.components.MirthTextField destField;
    private com.mirth.connect.client.ui.components.MirthTextField idletoField;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private com.mirth.connect.client.ui.components.MirthTextField keyPasswordField;
    private com.mirth.connect.client.ui.components.MirthTextField keyStoreField;
    private com.mirth.connect.client.ui.components.MirthTextField keyStorePasswordField;
    private com.mirth.connect.client.ui.components.MirthRadioButton nativeNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton nativeYes;
    private com.mirth.connect.client.ui.components.MirthRadioButton noclientauthNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton noclientauthYes;
    private com.mirth.connect.client.ui.components.MirthRadioButton nossl2No;
    private com.mirth.connect.client.ui.components.MirthRadioButton nossl2Yes;
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
    private com.mirth.connect.client.ui.components.MirthRadioButton tls3des;
    private com.mirth.connect.client.ui.components.MirthRadioButton tlsaes;
    private com.mirth.connect.client.ui.components.MirthRadioButton tlsno;
    private com.mirth.connect.client.ui.components.MirthRadioButton tlswithout;
    private com.mirth.connect.client.ui.components.MirthTextField truststoreField;
    private com.mirth.connect.client.ui.components.MirthTextField truststorepwField;
    // End of variables declaration//GEN-END:variables
}
