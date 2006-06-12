/*
 * HTTPSListener.java
 *
 * Created on May 22, 2006, 12:03 PM
 */

package com.webreach.mirth.client;

import java.util.Properties;

/**
 *
 * @author  brendanh
 */
public class HTTPSListener extends ConnectorClass
{
    
    /** Creates new form HTTPSListener */
    public HTTPSListener()
    {
        name = "HTTPS Listener";
        initComponents();
    }
    
    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put("HTTPSListener_ListenerIPAddress", listenerIPAddressField.getText());
        properties.put("HTTPSListener_ListenerPort", listenerPortField.getText());
        properties.put("HTTPSListener_ReceiveTimeout", receiveTimeoutField.getText());
        properties.put("HTTPSListener_BufferSize", bufferSizeField.getText());
        
        if (keepConnectionOpenYesRadio.isSelected())
            properties.put("HTTPSListener_KeepConnectionOpen", "YES");
        else
            properties.put("HTTPSListener_KeepConnectionOpen", "NO");
        
        properties.put("HTTPSListener_KeyStore", keyStoreField.getText());
        properties.put("HTTPSListener_KeyStorePassword", keyStorePasswordField.getText());
        properties.put("HTTPSListener_KeyStoreType", keyStoreTypeField.getText());
        properties.put("HTTPSListener_KeyManagerAlgorithm", keyManagerAlgorithmField.getText());
        properties.put("HTTPSListener_ProtocolHandler", protocolHandlerField.getText());
        
        if (requireClientAuthenticationYesRadio.isSelected())
            properties.put("HTTPSListener_RequireClientAuthentication", "YES");
        else
            properties.put("HTTPSListener_RequireClientAuthentication", "NO");        
        
        properties.put("HTTPSListener_SecurityProvider", securityProviderField.getText());
        properties.put("HTTPSListener_ClientKeystore", clientKeystoreField.getText());
        properties.put("HTTPSListener_ClientKeystorePassword", clientKeystorePasswordField.getText());
        properties.put("HTTPSListener_TrustKeystore", trustKeystoreField.getText());
        properties.put("HTTPSListener_TrustKeystorePassword", trustKeystorePasswordField.getText());

        if (explicitTrustStoreOnlyYesRadio.isSelected())
            properties.put("HTTPSListener_ExplicitTrustStoreOnly", "YES");
        else
            properties.put("HTTPSListener_ExplicitTrustStoreOnly", "NO");        
        
        properties.put("HTTPSListener_StartOfMessageCharacter", startOfMessageCharacterField.getText());
        properties.put("HTTPSListener_EndOfMessageCharacter", endOfMessageCharacterField.getText());
        properties.put("HTTPSListener_FieldSeparator", fieldSeparatorField.getText());
        properties.put("HTTPSListener_RecordSeparator", recordSeparatorField.getText());
        properties.put("HTTPSListener_SendACK", sendACKCombobox.getSelectedItem());
        return properties;
    }

    public void setProperties(Properties props)
    {
        listenerIPAddressField.setText((String)props.get("HTTPSListener_ListenerIPAddress"));
        listenerPortField.setText((String)props.get("HTTPSListener_ListenerPort"));
        receiveTimeoutField.setText((String)props.get("HTTPSListener_ReceiveTimeout"));
        bufferSizeField.setText((String)props.get("HTTPSListener_BufferSize"));
        
        if(((String)props.get("HTTPSListener_KeepConnectionOpen")).equals("YES"))
            keepConnectionOpenYesRadio.setSelected(true);
        else
            keepConnectionOpenNoRadio.setSelected(true);
        
        keyStoreField.setText((String)props.get("HTTPSListener_KeyStore"));
        keyStorePasswordField.setText((String)props.get("HTTPSListener_KeyStorePassword"));
        keyStoreTypeField.setText((String)props.get("HTTPSListener_KeyStoreType"));
        keyManagerAlgorithmField.setText((String)props.get("HTTPSListener_KeyManagerAlgorithm"));
        protocolHandlerField.setText((String)props.get("HTTPSListener_ProtocolHandler"));
        
        if(((String)props.get("HTTPSListener_RequireClientAuthentication")).equals("YES"))
            requireClientAuthenticationYesRadio.setSelected(true);
        else
            requireClientAuthenticationNoRadio.setSelected(true);
        
        securityProviderField.setText((String)props.get("HTTPSListener_SecurityProvider"));
        clientKeystoreField.setText((String)props.get("HTTPSListener_ClientKeystore"));
        clientKeystorePasswordField.setText((String)props.get("HTTPSListener_ClientKeystorePassword"));
        trustKeystoreField.setText((String)props.get("HTTPSListener_TrustKeystore"));
        trustKeystorePasswordField.setText((String)props.get("HTTPSListener_TrustKeystorePassword"));
        
        if(((String)props.get("HTTPSListener_ExplicitTrustStoreOnly")).equals("YES"))
            explicitTrustStoreOnlyYesRadio.setSelected(true);
        else
            explicitTrustStoreOnlyNoRadio.setSelected(true);
        
        startOfMessageCharacterField.setText((String)props.get("HTTPSListener_StartOfMessageCharacter"));
        endOfMessageCharacterField.setText((String)props.get("HTTPSListener_EndOfMessageCharacter"));
        fieldSeparatorField.setText((String)props.get("HTTPSListener_FieldSeparator"));
        recordSeparatorField.setText((String)props.get("HTTPSListener_RecordSeparator"));
        sendACKCombobox.setSelectedItem(props.get("HTTPSListener_SendACK"));
    }
    
    public void setDefault()
    {
        listenerIPAddressField.setText("");
        listenerPortField.setText("");
        receiveTimeoutField.setText("");
        bufferSizeField.setText("");
        keepConnectionOpenYesRadio.setSelected(true);        
        keyStoreField.setText("");
        keyStorePasswordField.setText("");
        keyStoreTypeField.setText("");
        keyManagerAlgorithmField.setText("");
        protocolHandlerField.setText("");
        requireClientAuthenticationYesRadio.setSelected(true);        
        securityProviderField.setText("");
        clientKeystoreField.setText("");
        clientKeystorePasswordField.setText("");
        trustKeystoreField.setText("");
        trustKeystorePasswordField.setText("");
        explicitTrustStoreOnlyYesRadio.setSelected(true);
        startOfMessageCharacterField.setText("");
        endOfMessageCharacterField.setText("");
        fieldSeparatorField.setText("");
        recordSeparatorField.setText("");
        sendACKCombobox.setSelectedIndex(0);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        keepConnectionOpenGroup = new javax.swing.ButtonGroup();
        requireClientAuthenticationGroup = new javax.swing.ButtonGroup();
        explicitTrustStoreOnlyGroup = new javax.swing.ButtonGroup();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        listenerIPAddressField = new javax.swing.JTextField();
        listenerPortField = new javax.swing.JTextField();
        receiveTimeoutField = new javax.swing.JTextField();
        bufferSizeField = new javax.swing.JTextField();
        keyStoreField = new javax.swing.JTextField();
        keyStorePasswordField = new javax.swing.JTextField();
        keyStoreTypeField = new javax.swing.JTextField();
        keyManagerAlgorithmField = new javax.swing.JTextField();
        protocolHandlerField = new javax.swing.JTextField();
        securityProviderField = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        clientKeystoreField = new javax.swing.JTextField();
        clientKeystorePasswordField = new javax.swing.JTextField();
        trustKeystoreField = new javax.swing.JTextField();
        trustKeystorePasswordField = new javax.swing.JTextField();
        keyManagerFactoryField = new javax.swing.JTextField();
        startOfMessageCharacterField = new javax.swing.JTextField();
        endOfMessageCharacterField = new javax.swing.JTextField();
        recordSeparatorField = new javax.swing.JTextField();
        fieldSeparatorField = new javax.swing.JTextField();
        sendACKCombobox = new javax.swing.JComboBox();
        keepConnectionOpenYesRadio = new javax.swing.JRadioButton();
        keepConnectionOpenNoRadio = new javax.swing.JRadioButton();
        requireClientAuthenticationYesRadio = new javax.swing.JRadioButton();
        requireClientAuthenticationNoRadio = new javax.swing.JRadioButton();
        explicitTrustStoreOnlyYesRadio = new javax.swing.JRadioButton();
        explicitTrustStoreOnlyNoRadio = new javax.swing.JRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "HTTPS Listener", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jLabel13.setText("Keep Connection Open:");

        jLabel14.setText("Key Store Tye:");

        jLabel15.setText("Buffer Size:");

        jLabel16.setText("Receive Timeout (ms):");

        jLabel17.setText("Listener Port:");

        jLabel18.setText("Listener IP Address:");

        jLabel19.setText("Key Manager Algorithm:");

        jLabel20.setText("Protocol Handler:");

        jLabel1.setText("Require Client Authentication:");

        jLabel2.setText("Security Provider:");

        jLabel3.setText("Client Keystore:");

        jLabel4.setText("Client Keystore Password:");

        jLabel5.setText("Trust Keystore:");

        jLabel6.setText("Trust Keystore Password:");

        jLabel7.setText("Explicit Trust Store Only:");

        jLabel8.setText("Key Manager Factory:");

        jLabel10.setText("Start of Message Character:");

        jLabel11.setText("End of Message Character:");

        jLabel12.setText("Record Sparator:");

        jLabel21.setText("Field Separator:");

        jLabel22.setText("Send ACK:");

        jLabel23.setText("Key Store:");

        jLabel24.setText("Key Store Password:");

        sendACKCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Auto", "Yes", "No" }));

        keepConnectionOpenYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenGroup.add(keepConnectionOpenYesRadio);
        keepConnectionOpenYesRadio.setText("Yes");
        keepConnectionOpenYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        keepConnectionOpenNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenGroup.add(keepConnectionOpenNoRadio);
        keepConnectionOpenNoRadio.setText("No");
        keepConnectionOpenNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        requireClientAuthenticationYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        requireClientAuthenticationGroup.add(requireClientAuthenticationYesRadio);
        requireClientAuthenticationYesRadio.setText("Yes");
        requireClientAuthenticationYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        requireClientAuthenticationYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        requireClientAuthenticationNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        requireClientAuthenticationGroup.add(requireClientAuthenticationNoRadio);
        requireClientAuthenticationNoRadio.setText("No");
        requireClientAuthenticationNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        requireClientAuthenticationNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        explicitTrustStoreOnlyYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        explicitTrustStoreOnlyGroup.add(explicitTrustStoreOnlyYesRadio);
        explicitTrustStoreOnlyYesRadio.setText("Yes");
        explicitTrustStoreOnlyYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        explicitTrustStoreOnlyYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        explicitTrustStoreOnlyNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        explicitTrustStoreOnlyGroup.add(explicitTrustStoreOnlyNoRadio);
        explicitTrustStoreOnlyNoRadio.setText("No");
        explicitTrustStoreOnlyNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        explicitTrustStoreOnlyNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel23)
                    .add(jLabel21)
                    .add(jLabel12)
                    .add(jLabel10)
                    .add(jLabel8)
                    .add(jLabel7)
                    .add(jLabel6)
                    .add(jLabel5)
                    .add(jLabel4)
                    .add(jLabel3)
                    .add(jLabel2)
                    .add(jLabel20)
                    .add(jLabel1)
                    .add(jLabel19)
                    .add(jLabel13)
                    .add(jLabel15)
                    .add(jLabel16)
                    .add(jLabel17)
                    .add(jLabel18)
                    .add(jLabel14)
                    .add(jLabel24)
                    .add(jLabel22)
                    .add(jLabel11))
                .add(17, 17, 17)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(explicitTrustStoreOnlyYesRadio)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(explicitTrustStoreOnlyNoRadio))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(listenerIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(receiveTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(keyStoreField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(keyStorePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(keyStoreTypeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(keyManagerAlgorithmField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(protocolHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(securityProviderField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(clientKeystoreField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(clientKeystorePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(trustKeystoreField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(trustKeystorePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(keyManagerFactoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(startOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(endOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(recordSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(fieldSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(sendACKCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(keepConnectionOpenYesRadio)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(keepConnectionOpenNoRadio))
                    .add(layout.createSequentialGroup()
                        .add(requireClientAuthenticationYesRadio)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(requireClientAuthenticationNoRadio)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel18)
                    .add(listenerIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel17)
                    .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel16)
                    .add(receiveTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel15)
                    .add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(keepConnectionOpenYesRadio)
                    .add(keepConnectionOpenNoRadio))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel23)
                    .add(keyStoreField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel24)
                    .add(keyStorePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel14)
                    .add(keyStoreTypeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel19)
                    .add(keyManagerAlgorithmField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel20)
                    .add(protocolHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(requireClientAuthenticationYesRadio)
                    .add(requireClientAuthenticationNoRadio))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(securityProviderField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(clientKeystoreField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(clientKeystorePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(trustKeystoreField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(trustKeystorePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(explicitTrustStoreOnlyYesRadio)
                    .add(explicitTrustStoreOnlyNoRadio))
                .add(7, 7, 7)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(keyManagerFactoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(startOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(endOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel11))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(recordSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel21)
                    .add(layout.createSequentialGroup()
                        .add(fieldSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(sendACKCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel22))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bufferSizeField;
    private javax.swing.JTextField clientKeystoreField;
    private javax.swing.JTextField clientKeystorePasswordField;
    private javax.swing.JTextField endOfMessageCharacterField;
    private javax.swing.ButtonGroup explicitTrustStoreOnlyGroup;
    private javax.swing.JRadioButton explicitTrustStoreOnlyNoRadio;
    private javax.swing.JRadioButton explicitTrustStoreOnlyYesRadio;
    private javax.swing.JTextField fieldSeparatorField;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.ButtonGroup keepConnectionOpenGroup;
    private javax.swing.JRadioButton keepConnectionOpenNoRadio;
    private javax.swing.JRadioButton keepConnectionOpenYesRadio;
    private javax.swing.JTextField keyManagerAlgorithmField;
    private javax.swing.JTextField keyManagerFactoryField;
    private javax.swing.JTextField keyStoreField;
    private javax.swing.JTextField keyStorePasswordField;
    private javax.swing.JTextField keyStoreTypeField;
    private javax.swing.JTextField listenerIPAddressField;
    private javax.swing.JTextField listenerPortField;
    private javax.swing.JTextField protocolHandlerField;
    private javax.swing.JTextField receiveTimeoutField;
    private javax.swing.JTextField recordSeparatorField;
    private javax.swing.ButtonGroup requireClientAuthenticationGroup;
    private javax.swing.JRadioButton requireClientAuthenticationNoRadio;
    private javax.swing.JRadioButton requireClientAuthenticationYesRadio;
    private javax.swing.JTextField securityProviderField;
    private javax.swing.JComboBox sendACKCombobox;
    private javax.swing.JTextField startOfMessageCharacterField;
    private javax.swing.JTextField trustKeystoreField;
    private javax.swing.JTextField trustKeystorePasswordField;
    // End of variables declaration//GEN-END:variables
    
}
