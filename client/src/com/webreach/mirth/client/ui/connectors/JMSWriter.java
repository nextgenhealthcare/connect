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
    public final String JMS_ACK_MODE = "AckMode";
    public final String JMS_SPECIFICATION = "Specification";
    public final String JMS_DELIVERY = "Delivery";
    public final String JMS_DURABLE = "Durable";
    public final String JMS_CLIENT_ID = "ClientID";
    public final String JMS_JNDI_INITIAL_FACTORY = "JNDIInitialFactory";
    public final String JMS_JNDI_PROVIDER_URL = "JNDIProviderURL";
    public final String JMS_JNDI_CONNECTION_NAME = "JNDIConnectionName";    
    public final String JMS_REDELIVERY_HANDLER = "redeliveryHandler";    
    
    public JMSWriter()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "JMS Writer";
        initComponents();
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(JMS_ACK_MODE, ackMode.getText());
        properties.put(JMS_SPECIFICATION, (String)specDropDown.getSelectedItem());
        
        if(deliveryNo.isSelected())
            properties.put(JMS_DELIVERY, "false");
        else
            properties.put(JMS_DELIVERY, "true");
        
        if(durableNo.isSelected())
            properties.put(JMS_DURABLE, "false");
        else
            properties.put(JMS_DURABLE, "true");
        
        properties.put(JMS_CLIENT_ID, cliendId.getText());
        properties.put(JMS_JNDI_INITIAL_FACTORY, JNDIInitialFactory.getText());
        properties.put(JMS_JNDI_PROVIDER_URL, JNDIProviderURL.getText());
        properties.put(JMS_JNDI_CONNECTION_NAME, JNDIConnectionName.getText());
        properties.put(JMS_REDELIVERY_HANDLER, redeliveryHandler.getText());
        return properties;
    }

    public void setProperties(Properties props)
    {
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
        
        ackMode.setText((String)props.get(JMS_ACK_MODE));
        specDropDown.setSelectedItem(props.get(JMS_SPECIFICATION));
        
        if(((String)props.get(JMS_DELIVERY)).equalsIgnoreCase("false"))
            deliveryNo.setSelected(true);
        else
            deliveryYes.setSelected(true);
        
        if(((String)props.get(JMS_DELIVERY)).equalsIgnoreCase("false"))
            durableNo.setSelected(true);
        else
            durableYes.setSelected(true);
        
        cliendId.setText((String)props.get(JMS_CLIENT_ID));
        JNDIInitialFactory.setText((String)props.get(JMS_JNDI_INITIAL_FACTORY));
        JNDIProviderURL.setText((String)props.get(JMS_JNDI_PROVIDER_URL));
        JNDIConnectionName.setText((String)props.get(JMS_JNDI_CONNECTION_NAME));
        redeliveryHandler.setText((String)props.get(JMS_REDELIVERY_HANDLER));
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(JMS_ACK_MODE, "1");
        properties.put(JMS_SPECIFICATION, (String)specDropDown.getItemAt(0));
        properties.put(JMS_DELIVERY, "false");
        properties.put(JMS_DURABLE, "false");
        properties.put(JMS_CLIENT_ID, "");
        properties.put(JMS_JNDI_INITIAL_FACTORY, "");
        properties.put(JMS_JNDI_PROVIDER_URL, "");
        properties.put(JMS_JNDI_CONNECTION_NAME, "");
        properties.put(JMS_REDELIVERY_HANDLER, "org.mule.proivders.jms.DefaultRedeliveryHandler");
        return properties;
    }
    
    public boolean checkProperties(Properties props)
    {
        /*if()
        {
            return true;
        }*/
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        ackMode = new com.webreach.mirth.client.ui.components.MirthTextField();
        specDropDown = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        deliveryYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        deliveryNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        durableNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        durableYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        cliendId = new com.webreach.mirth.client.ui.components.MirthTextField();
        JNDIInitialFactory = new com.webreach.mirth.client.ui.components.MirthTextField();
        JNDIProviderURL = new com.webreach.mirth.client.ui.components.MirthTextField();
        JNDIConnectionName = new com.webreach.mirth.client.ui.components.MirthTextField();
        redeliveryHandler = new com.webreach.mirth.client.ui.components.MirthTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "JMS Writer", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jLabel1.setText("Acknowledgement Mode:");

        jLabel2.setText("Persistent Delivery:");

        jLabel3.setText("Specification:");

        specDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setText("Durable:");

        jLabel5.setText("Client ID:");

        jLabel6.setText("JNDI Initial Factory:");

        jLabel7.setText("JNDI Provider URL:");

        jLabel8.setText("Connection Factory JNDI Name:");

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

        durableYes.setBackground(new java.awt.Color(255, 255, 255));
        durableYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        durableButtonGroup.add(durableYes);
        durableYes.setText("Yes");
        durableYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        redeliveryHandler.setAutoscrolls(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel9)
                    .add(jLabel8)
                    .add(jLabel7)
                    .add(jLabel6)
                    .add(jLabel5)
                    .add(jLabel3)
                    .add(jLabel1)
                    .add(jLabel4)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(durableYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(durableNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(deliveryYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deliveryNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(JNDIProviderURL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(JNDIInitialFactory, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(cliendId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(JNDIConnectionName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(specDropDown, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(ackMode, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .add(redeliveryHandler, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(ackMode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(specDropDown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(deliveryYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(deliveryNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(durableYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(durableNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(cliendId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(JNDIInitialFactory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(JNDIProviderURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(JNDIConnectionName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(redeliveryHandler, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthTextField JNDIConnectionName;
    private com.webreach.mirth.client.ui.components.MirthTextField JNDIInitialFactory;
    private com.webreach.mirth.client.ui.components.MirthTextField JNDIProviderURL;
    private com.webreach.mirth.client.ui.components.MirthTextField ackMode;
    private com.webreach.mirth.client.ui.components.MirthTextField cliendId;
    private javax.swing.ButtonGroup deliveryButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deliveryNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deliveryYes;
    private javax.swing.ButtonGroup durableButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton durableNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton durableYes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private com.webreach.mirth.client.ui.components.MirthTextField redeliveryHandler;
    private com.webreach.mirth.client.ui.components.MirthComboBox specDropDown;
    // End of variables declaration//GEN-END:variables

}
