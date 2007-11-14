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

package com.webreach.mirth.connectors.soap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Step;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class SOAPListener extends ConnectorClass
{
    /**
     * Creates new form SOAPListener
     */

    public SOAPListener()
    {
        name = SOAPListenerProperties.name;
        initComponents();
        wsdlURL.setEditable(false);
        method.setEditable(false);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(SOAPListenerProperties.DATATYPE, name);
        properties.put(SOAPListenerProperties.SOAP_LISTENER_ADDRESS, listenerAddress.getText());
        properties.put(SOAPListenerProperties.SOAP_PORT, port.getText());
        properties.put(SOAPListenerProperties.SOAP_SERVICE_NAME, serviceName.getText());
        properties.put(SOAPListenerProperties.SOAP_HOST, buildHost());
        properties.put(SOAPListenerProperties.SOAP_CONTENT_TYPE, "text/xml");
        properties.put(SOAPListenerProperties.SOAP_RESPONSE_VALUE, (String)responseFromTransformer.getSelectedItem());
        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        
        listenerAddress.setText((String) props.get(SOAPListenerProperties.SOAP_LISTENER_ADDRESS));
        port.setText((String) props.getProperty(SOAPListenerProperties.SOAP_PORT));
        serviceName.setText((String) props.getProperty(SOAPListenerProperties.SOAP_SERVICE_NAME));
        updateResponseDropDown();
        
        if (parent.channelEditPanel.synchronousCheckBox.isSelected())
            responseFromTransformer.setSelectedItem((String) props.getProperty(SOAPListenerProperties.SOAP_RESPONSE_VALUE));
        
        updateWSDL();
    }

    public Properties getDefaults()
    {
        return new SOAPListenerProperties().getDefaults();
    }

    public String buildHost()
    {
        return "axis:soap://" + listenerAddress.getText() + ":" + port.getText() + "/services";
    }

    public void updateWSDL()
    {
        wsdlURL.setText("http://" + listenerAddress.getText() + ":" + port.getText() + "/services/" + serviceName.getText() + "?wsdl");

    }

    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.get(SOAPListenerProperties.SOAP_LISTENER_ADDRESS)).length() == 0)
        {
            valid = false;
            if (highlight)
            	listenerAddress.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(SOAPListenerProperties.SOAP_PORT)).length() == 0)
        {
            valid = false;
            if (highlight)
            	port.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(SOAPListenerProperties.SOAP_SERVICE_NAME)).length() == 0)
        {
            valid = false;
            if (highlight)
            	serviceName.setBackground(UIConstants.INVALID_COLOR);
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        listenerAddress.setBackground(null);
        port.setBackground(null);
        serviceName.setBackground(null);
    }

    public String doValidate(Properties props, boolean highlight)
    {
    	String error = null;
    	
    	if (!checkProperties(props, highlight))
    		error = "Error in the form for connector \"" + getName() + "\".\n\n";
    	
    	return error;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        buttonGroup1 = new javax.swing.ButtonGroup();
        URL = new javax.swing.JLabel();
        serviceName = new com.webreach.mirth.client.ui.components.MirthTextField();
        port = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        URL1 = new javax.swing.JLabel();
        listenerAddress = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel3 = new javax.swing.JLabel();
        method = new javax.swing.JTextField();
        wsdlURL = new javax.swing.JTextField();
        responseFromLabel = new javax.swing.JLabel();
        responseFromTransformer = new com.webreach.mirth.client.ui.components.MirthComboBox();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        URL.setText("Service Name:");

        serviceName.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(java.awt.event.KeyEvent evt)
            {
                serviceNameKeyReleased(evt);
            }
        });

        port.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(java.awt.event.KeyEvent evt)
            {
                portKeyReleased(evt);
            }
        });

        jLabel1.setText("Port:");

        jLabel2.setText("Method:");

        URL1.setText("WSDL URL:");

        listenerAddress.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(java.awt.event.KeyEvent evt)
            {
                listenerAddressKeyReleased(evt);
            }
        });

        jLabel3.setText("Listener Address:");

        method.setText("String acceptMessage(String message)");

        responseFromLabel.setText("Respond from:");

        responseFromTransformer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(responseFromLabel)
                    .add(jLabel2)
                    .add(URL)
                    .add(jLabel1)
                    .add(jLabel3)
                    .add(URL1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(serviceName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(port, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(listenerAddress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(wsdlURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 400, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(method, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(responseFromTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(listenerAddress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(port, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(URL)
                    .add(serviceName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(URL1)
                    .add(wsdlURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(method, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(responseFromLabel)
                    .add(responseFromTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    public void updateResponseDropDown()
    {
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
        
        String selectedItem = (String) responseFromTransformer.getSelectedItem();
        
        Channel channel = parent.channelEditPanel.currentChannel;
        
        Set<String> variables = new LinkedHashSet<String>();

        variables.add("None");

        List<Step> stepsToCheck = new ArrayList<Step>();
        stepsToCheck.addAll(channel.getSourceConnector().getTransformer().getSteps());

        List<String> scripts = new ArrayList<String>();

        for (Connector connector : channel.getDestinationConnectors())
        {
            if (connector.getTransportName().equals("Database Writer"))
            {
                if (connector.getProperties().getProperty("useScript").equals(UIConstants.YES_OPTION))
                {
                    scripts.add(connector.getProperties().getProperty("script"));
                }

            }
            else if (connector.getTransportName().equals("JavaScript Writer"))
            {
                scripts.add(connector.getProperties().getProperty("script"));
            }

            variables.add(connector.getName());
            stepsToCheck.addAll(connector.getTransformer().getSteps());
        }

        Pattern pattern = Pattern.compile(RESULT_PATTERN);

        int i = 0;
        for (Iterator it = stepsToCheck.iterator(); it.hasNext();)
        {
            Step step = (Step) it.next();
            Map data;
            data = (Map) step.getData();

            if (step.getType().equalsIgnoreCase(TransformerPane.JAVASCRIPT_TYPE))
            {
                Matcher matcher = pattern.matcher(step.getScript());
                while (matcher.find())
                {
                    String key = matcher.group(1);
                    variables.add(key);
                }
            }
            else if (step.getType().equalsIgnoreCase(TransformerPane.MAPPER_TYPE))
            {
                if (data.containsKey(UIConstants.IS_GLOBAL))
                {
                    if (((String) data.get(UIConstants.IS_GLOBAL)).equalsIgnoreCase(UIConstants.IS_GLOBAL_RESPONSE))
                        variables.add((String) data.get("Variable"));
                }
            }
        }

        scripts.add(channel.getPreprocessingScript());
        scripts.add(channel.getPostprocessingScript());

        for (String script : scripts)
        {
            if (script != null && script.length() > 0)
            {
                Matcher matcher = pattern.matcher(script);
                while (matcher.find())
                {
                    String key = matcher.group(1);
                    variables.add(key);
                }
            }
        }
        
        responseFromTransformer.setModel(new DefaultComboBoxModel(variables.toArray()));
        
        if(variables.contains(selectedItem))
            responseFromTransformer.setSelectedItem(selectedItem);
        else
            responseFromTransformer.setSelectedIndex(0);
        
        if (!parent.channelEditPanel.synchronousCheckBox.isSelected())
        {
            responseFromTransformer.setEnabled(false);
            responseFromLabel.setEnabled(false);
            responseFromTransformer.setSelectedIndex(0);
        }
        else
        {
            responseFromTransformer.setEnabled(true);
            responseFromLabel.setEnabled(true);
        }
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }
    
    private void serviceNameKeyReleased(java.awt.event.KeyEvent evt)// GEN-FIRST:event_serviceNameKeyReleased
    {// GEN-HEADEREND:event_serviceNameKeyReleased
        updateWSDL();
    }// GEN-LAST:event_serviceNameKeyReleased

    private void portKeyReleased(java.awt.event.KeyEvent evt)// GEN-FIRST:event_portKeyReleased
    {// GEN-HEADEREND:event_portKeyReleased
        updateWSDL();
    }// GEN-LAST:event_portKeyReleased

    private void listenerAddressKeyReleased(java.awt.event.KeyEvent evt)// GEN-FIRST:event_listenerAddressKeyReleased
    {// GEN-HEADEREND:event_listenerAddressKeyReleased
        updateWSDL();
    }// GEN-LAST:event_listenerAddressKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL;
    private javax.swing.JLabel URL1;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerAddress;
    private javax.swing.JTextField method;
    private com.webreach.mirth.client.ui.components.MirthTextField port;
    private javax.swing.JLabel responseFromLabel;
    private com.webreach.mirth.client.ui.components.MirthComboBox responseFromTransformer;
    private com.webreach.mirth.client.ui.components.MirthTextField serviceName;
    private javax.swing.JTextField wsdlURL;
    // End of variables declaration//GEN-END:variables

}
