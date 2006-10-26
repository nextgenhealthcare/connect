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
    
    /** Creates new form JMSWriter */
    public final String DATATYPE = "DataType";
    public final String JMS_SPECIFICATION = "specification";
    public final String JMS_DURABLE = "durable";
    public final String JMS_CLIENT_ID = "clientId";
    public final String JMS_USERNAME = "username";
    public final String JMS_PASSWORD = "password";
    public final String JMS_QUEUE = "host";
    public final String JMS_URL = "jndiProviderUrl";
    public final String JMS_INITIAL_FACTORY = "jndiInitialFactory";
    public final String JMS_CONNECTION_FACTORY = "connectionFactoryJndiName";
    
    public JMSWriter()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "JMS Writer";
        initComponents();
        specDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1.1", "1.0.2b"}));
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(JMS_SPECIFICATION, (String)specDropDown.getSelectedItem());
        
        if(durableNo.isSelected())
            properties.put(JMS_DURABLE, UIConstants.NO_OPTION);
        else
            properties.put(JMS_DURABLE, UIConstants.YES_OPTION);
        
        properties.put(JMS_CLIENT_ID, cliendId.getText());
        properties.put(JMS_USERNAME, username.getText());
        properties.put(JMS_PASSWORD,String.valueOf(password.getPassword()));
        properties.put(JMS_QUEUE, queue.getText());
        properties.put(JMS_URL, jmsURL.getText());
        properties.put(JMS_INITIAL_FACTORY,jndiInitialFactory.getText());
        properties.put(JMS_CONNECTION_FACTORY, connectionFactory.getText());
        
        return properties;
    }

    public void setProperties(Properties props)
    {
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
        
        specDropDown.setSelectedItem(props.get(JMS_SPECIFICATION));
        
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
        username.setText((String)props.get(JMS_USERNAME));
        password.setText((String)props.get(JMS_PASSWORD));
        jmsURL.setText((String)props.get(JMS_URL));
        queue.setText((String)props.get(JMS_QUEUE));
        jndiInitialFactory.setText((String)props.get(JMS_INITIAL_FACTORY));
        connectionFactory.setText((String)props.get(JMS_CONNECTION_FACTORY));
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(JMS_SPECIFICATION, (String)specDropDown.getItemAt(0));
        properties.put(JMS_DURABLE, UIConstants.NO_OPTION);
        properties.put(JMS_CLIENT_ID, "");
        properties.put(JMS_USERNAME, "");
        properties.put(JMS_PASSWORD, "");
        properties.put(JMS_URL, "");
        properties.put(JMS_QUEUE, "");
        properties.put(JMS_INITIAL_FACTORY, "");
        properties.put(JMS_CONNECTION_FACTORY, "");
        return properties;
    }
    
    public boolean checkProperties(Properties props)
    {
        if(((String)props.getProperty(JMS_DURABLE)).equals(UIConstants.YES_OPTION) && ((String)props.getProperty(JMS_CLIENT_ID)).length() == 0)
            return false;
        else if(((String)props.getProperty(JMS_URL)).length() > 0 &&
                ((String)props.getProperty(JMS_CONNECTION_FACTORY)).length() > 0 &&
                ((String)props.getProperty(JMS_QUEUE)).length() > 0 &&
                ((String)props.getProperty(JMS_INITIAL_FACTORY)).length() > 0 &&
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
    private void initComponents() {
        deliveryButtonGroup = new javax.swing.ButtonGroup();
        durableButtonGroup = new javax.swing.ButtonGroup();
        recoverButtonGroup = new javax.swing.ButtonGroup();
        jLabel3 = new javax.swing.JLabel();
        specDropDown = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel4 = new javax.swing.JLabel();
        clientIdLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        durableNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        durableYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        cliendId = new com.webreach.mirth.client.ui.components.MirthTextField();
        jmsURL = new com.webreach.mirth.client.ui.components.MirthTextField();
        connectionFactory = new com.webreach.mirth.client.ui.components.MirthTextField();
        queue = new com.webreach.mirth.client.ui.components.MirthTextField();
        jndiInitialFactory = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        username = new com.webreach.mirth.client.ui.components.MirthTextField();
        password = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        jLabel12 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "JMS Writer", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jLabel3.setText("Specification:");

        specDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setText("Durable:");

        clientIdLabel.setText("Client ID:");

        jLabel7.setText("JNDI Provider URL:");

        jLabel8.setText("Connection Factory JNDI Name:");

        jLabel9.setText("Queue:");

        durableNo.setBackground(new java.awt.Color(255, 255, 255));
        durableNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        durableButtonGroup.add(durableNo);
        durableNo.setSelected(true);
        durableNo.setText("No");
        durableNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        durableNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                durableNoActionPerformed(evt);
            }
        });

        durableYes.setBackground(new java.awt.Color(255, 255, 255));
        durableYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        durableButtonGroup.add(durableYes);
        durableYes.setText("Yes");
        durableYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        durableYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                durableYesActionPerformed(evt);
            }
        });

        queue.setAutoscrolls(false);

        jLabel10.setText("JNDI Initial Context Factory:");

        jLabel11.setText("Username:");

        password.setFont(new java.awt.Font("Tahoma", 0, 11));

        jLabel12.setText("Password:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel7)
                    .add(jLabel3)
                    .add(jLabel10)
                    .add(jLabel8)
                    .add(jLabel9)
                    .add(jLabel11)
                    .add(jLabel12)
                    .add(jLabel4)
                    .add(clientIdLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cliendId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(username, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(queue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(durableYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(durableNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(connectionFactory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(specDropDown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jmsURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jndiInitialFactory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(specDropDown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jmsURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(jndiInitialFactory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(connectionFactory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(queue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(username, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(durableYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(durableNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(clientIdLabel)
                    .add(cliendId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
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
        cliendId.setText("");
    }//GEN-LAST:event_durableNoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthTextField cliendId;
    private javax.swing.JLabel clientIdLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField connectionFactory;
    private javax.swing.ButtonGroup deliveryButtonGroup;
    private javax.swing.ButtonGroup durableButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton durableNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton durableYes;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private com.webreach.mirth.client.ui.components.MirthTextField jmsURL;
    private com.webreach.mirth.client.ui.components.MirthTextField jndiInitialFactory;
    private com.webreach.mirth.client.ui.components.MirthPasswordField password;
    private com.webreach.mirth.client.ui.components.MirthTextField queue;
    private javax.swing.ButtonGroup recoverButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthComboBox specDropDown;
    private com.webreach.mirth.client.ui.components.MirthTextField username;
    // End of variables declaration//GEN-END:variables

}
