/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.client.ui.connectors;

import com.webreach.mirth.client.ui.UIConstants;
import java.util.Properties;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;

/** 
 * A form that extends from ConnectorClass.  All methods implemented
 * are described in ConnectorClass.
 */
public class JMSWriter extends ConnectorClass
{
    Frame parent;
    
    /** Creates new form FileWriter */
    public final String DATATYPE = "DataType";
    public final String JMS_SPECIFICATION = "specification";
    public final String JMS_DELIVERY = "persistentDelivery";
    public final String JMS_DURABLE = "durable ";
    public final String JMS_CLIENT_ID = "clientId ";
    public final String JMS_URL = "jmsUrl";
    public final String JMS_USERNAME = "username";
    public final String JMS_PASSWORD = "password";
    public final String JMS_CONNECTION_FACTORY = "connectionFactory";
    public final String JMS_REDELIVERY_HANDLER = "redeliveryHandler";
    public final String JMS_RECOVER_CONNECTIONS = "recoverJmsConnections";   
    
    public JMSWriter()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "JMS Writer";
        initComponents();
        specDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1.0.2b", "1.1"}));
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(JMS_SPECIFICATION, (String)specDropDown.getSelectedItem());
        
        if(deliveryNo.isSelected())
            properties.put(JMS_DELIVERY, UIConstants.NO_OPTION);
        else
            properties.put(JMS_DELIVERY, UIConstants.YES_OPTION);
        
        if(durableNo.isSelected())
            properties.put(JMS_DURABLE, UIConstants.NO_OPTION);
        else
            properties.put(JMS_DURABLE, UIConstants.YES_OPTION);
        
        properties.put(JMS_CLIENT_ID, cliendId.getText());
        properties.put(JMS_URL, jmsURL.getText());
        properties.put(JMS_USERNAME, username.getText());
        properties.put(JMS_PASSWORD,String.valueOf(password.getPassword()));
        properties.put(JMS_CONNECTION_FACTORY, connectionFactory.getText());
        
        if(recoverJMSConnectionsNo.isSelected())
            properties.put(JMS_RECOVER_CONNECTIONS, UIConstants.NO_OPTION);
        else
            properties.put(JMS_RECOVER_CONNECTIONS, UIConstants.YES_OPTION);
        
        return properties;
    }

    public void setProperties(Properties props)
    {
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
        
        specDropDown.setSelectedItem(props.get(JMS_SPECIFICATION));
        
        if(((String)props.get(JMS_DELIVERY)).equalsIgnoreCase(UIConstants.NO_OPTION))
            deliveryNo.setSelected(true);
        else
            deliveryYes.setSelected(true);
        
        if(((String)props.get(JMS_DURABLE)).equalsIgnoreCase(UIConstants.NO_OPTION))
        {
            durableNo.setSelected(true);
            durableNoActionPerformed(null);
        }
        else
        {
            durableYes.setSelected(true);
            durableYesActionPerformed(null);
        }
        
        cliendId.setText((String)props.get(JMS_CLIENT_ID));
        jmsURL.setText((String)props.get(JMS_URL));
        username.setText((String)props.get(JMS_USERNAME));
        password.setText((String)props.get(JMS_PASSWORD));
        connectionFactory.setText((String)props.get(JMS_CONNECTION_FACTORY));
        
        if(((String)props.get(JMS_RECOVER_CONNECTIONS)).equalsIgnoreCase(UIConstants.NO_OPTION))
            recoverJMSConnectionsNo.setSelected(true);
        else
            recoverJMSConnectionsYes.setSelected(true);
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(JMS_SPECIFICATION, (String)specDropDown.getItemAt(0));
        properties.put(JMS_DELIVERY, UIConstants.NO_OPTION);
        properties.put(JMS_DURABLE, UIConstants.NO_OPTION);
        properties.put(JMS_CLIENT_ID, "");
        properties.put(JMS_URL, "");
        properties.put(JMS_USERNAME, "");
        properties.put(JMS_PASSWORD, "");
        properties.put(JMS_CONNECTION_FACTORY, "org.mule.proivders.jms.JmsMessageDispatcher");
        properties.put(JMS_RECOVER_CONNECTIONS, UIConstants.NO_OPTION);
        return properties;
    }
    
    public boolean checkProperties(Properties props)
    {
        if(((String)props.getProperty(JMS_DURABLE)).equals(UIConstants.YES_OPTION) && ((String)props.getProperty(JMS_CLIENT_ID)).length() == 0)
            return false;
        else if(((String)props.getProperty(JMS_URL)).length() > 0 &&
                ((String)props.getProperty(JMS_CONNECTION_FACTORY)).length() > 0 &&
                ((String)props.getProperty(JMS_USERNAME)).length() > 0 &&
                ((String)props.getProperty(JMS_PASSWORD)).length() > 0 &&
                ((String)props.getProperty(JMS_CONNECTION_FACTORY)).length() > 0)
            return true;
        else
            return false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        deliveryButtonGroup = new javax.swing.ButtonGroup();
        durableButtonGroup = new javax.swing.ButtonGroup();
        recoverButtonGroup = new javax.swing.ButtonGroup();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        specDropDown = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel4 = new javax.swing.JLabel();
        clientIdLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        deliveryYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        deliveryNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        durableNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        durableYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        cliendId = new com.webreach.mirth.client.ui.components.MirthTextField();
        jmsURL = new com.webreach.mirth.client.ui.components.MirthTextField();
        connectionFactory = new com.webreach.mirth.client.ui.components.MirthTextField();
        redeliveryHandler = new com.webreach.mirth.client.ui.components.MirthTextField();
        username = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        recoverJMSConnectionsYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        recoverJMSConnectionsNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        password = new com.webreach.mirth.client.ui.components.MirthPasswordField();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "JMS Writer", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jLabel2.setText("Persistent Delivery:");

        jLabel3.setText("Specification:");

        specDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setText("Durable:");

        clientIdLabel.setText("Client ID:");

        jLabel6.setText("Password:");

        jLabel7.setText("URL:");

        jLabel8.setText("Connection Factory:");

        jLabel9.setText("Redelivery Handler:");

        deliveryYes.setBackground(new java.awt.Color(255, 255, 255));
        deliveryYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        deliveryButtonGroup.add(deliveryYes);
        deliveryYes.setText("Yes");
        deliveryYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        deliveryNo.setBackground(new java.awt.Color(255, 255, 255));
        deliveryNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        deliveryButtonGroup.add(deliveryNo);
        deliveryNo.setSelected(true);
        deliveryNo.setText("No");
        deliveryNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        durableNo.setBackground(new java.awt.Color(255, 255, 255));
        durableNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        durableButtonGroup.add(durableNo);
        durableNo.setSelected(true);
        durableNo.setText("No");
        durableNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        durableNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                durableNoActionPerformed(evt);
            }
        });

        durableYes.setBackground(new java.awt.Color(255, 255, 255));
        durableYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        durableButtonGroup.add(durableYes);
        durableYes.setText("Yes");
        durableYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        durableYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                durableYesActionPerformed(evt);
            }
        });

        redeliveryHandler.setAutoscrolls(false);

        jLabel10.setText("Username:");

        jLabel11.setText("Recover JMS Connections:");

        recoverJMSConnectionsYes.setBackground(new java.awt.Color(255, 255, 255));
        recoverJMSConnectionsYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        recoverButtonGroup.add(recoverJMSConnectionsYes);
        recoverJMSConnectionsYes.setText("Yes");
        recoverJMSConnectionsYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        recoverJMSConnectionsNo.setBackground(new java.awt.Color(255, 255, 255));
        recoverJMSConnectionsNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        recoverButtonGroup.add(recoverJMSConnectionsNo);
        recoverJMSConnectionsNo.setText("No");
        recoverJMSConnectionsNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        password.setFont(new java.awt.Font("Tahoma", 0, 11));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(jLabel11)
                    .add(clientIdLabel)
                    .add(jLabel3)
                    .add(jLabel4)
                    .add(jLabel7)
                    .add(jLabel10)
                    .add(jLabel6)
                    .add(jLabel8)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(durableYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(durableNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(deliveryYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deliveryNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(recoverJMSConnectionsYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(recoverJMSConnectionsNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(specDropDown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jmsURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cliendId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, connectionFactory, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, username, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, redeliveryHandler, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(113, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(specDropDown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(deliveryYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(deliveryNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(durableYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(durableNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(clientIdLabel)
                    .add(cliendId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jmsURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel10)
                    .add(username, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(connectionFactory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(redeliveryHandler, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(recoverJMSConnectionsYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(recoverJMSConnectionsNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(205, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void durableYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_durableYesActionPerformed
    {//GEN-HEADEREND:event_durableYesActionPerformed
        cliendId.setEnabled(true);  
        clientIdLabel.setEnabled(true);
    }//GEN-LAST:event_durableYesActionPerformed

    private void durableNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_durableNoActionPerformed
    {//GEN-HEADEREND:event_durableNoActionPerformed
        cliendId.setEnabled(false);
        clientIdLabel.setEnabled(false);
    }//GEN-LAST:event_durableNoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthTextField cliendId;
    private javax.swing.JLabel clientIdLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField connectionFactory;
    private javax.swing.ButtonGroup deliveryButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deliveryNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deliveryYes;
    private javax.swing.ButtonGroup durableButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton durableNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton durableYes;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private com.webreach.mirth.client.ui.components.MirthTextField jmsURL;
    private com.webreach.mirth.client.ui.components.MirthPasswordField password;
    private javax.swing.ButtonGroup recoverButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton recoverJMSConnectionsNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton recoverJMSConnectionsYes;
    private com.webreach.mirth.client.ui.components.MirthTextField redeliveryHandler;
    private com.webreach.mirth.client.ui.components.MirthComboBox specDropDown;
    private com.webreach.mirth.client.ui.components.MirthTextField username;
    // End of variables declaration//GEN-END:variables

}
