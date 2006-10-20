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

import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.WSDefinition;
import com.webreach.mirth.model.WSParameter;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;

/**
 * A form that extends from ConnectorClass.  All methods implemented
 * are described in ConnectorClass.
 */
public class SOAPSender extends ConnectorClass
{
    public final int PARAMETER_COLUMN = 0;
    public final int TYPE_COLUMN = 1;
    public final int VALUE_COLUMN = 2;
    
    public final String PARAMETER_COLUMN_NAME = "Parameter";
    public final String TYPE_COLUMN_NAME = "Type";
    public final String VALUE_COLUMN_NAME = "Value";
    Frame parent;
    WSDefinition methodList;
    
    /**
     * Creates new form SOAPListener
     */
    public final String DATATYPE = "DataType";
    public final String SOAP_HOST = "host";
    public final String SOAP_SERVICE_ENDPOINT = "serviceEndpoint";
    public final String SOAP_URL = "wsdlUrl";
    public final String SOAP_METHOD = "method";
    public final String SOAP_PARAMETERS = "parameters";

    public SOAPSender()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "SOAP Sender";
        initComponents();
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(SOAP_URL, soapUrl.getText());
        properties.put(SOAP_METHOD, (String)method.getSelectedItem());
        properties.put(SOAP_PARAMETERS, getParameters());
        properties.put(SOAP_HOST, buildHost());
        return properties;
    }

    public void setProperties(Properties props)
    {
        methodList = null;
        
        soapUrl.setText((String)props.get(SOAP_URL));
        
        if(props.getProperty(SOAP_METHOD) != null)
        {    
            method.setModel(new javax.swing.DefaultComboBoxModel(new String[] { (String)props.getProperty(SOAP_METHOD) }));
        }
        
        ArrayList<WSParameter> parameters = (ArrayList<WSParameter>) props.get(SOAP_PARAMETERS);
        
        Object[][] tableData = new Object[parameters.size()][3];
        paramTable = new MirthTable();
      
        for (int i = 0; i < parameters.size(); i++)
        {
            tableData[i][PARAMETER_COLUMN] = parameters.get(i).getName();
            tableData[i][TYPE_COLUMN] = parameters.get(i).getType();
            tableData[i][VALUE_COLUMN] = parameters.get(i).getValue();
        }        
        
        paramTable.setModel(new javax.swing.table.DefaultTableModel(
        tableData, new String[] { PARAMETER_COLUMN_NAME, TYPE_COLUMN_NAME,
        VALUE_COLUMN_NAME })
        {
            boolean[] canEdit = new boolean[] { false, false, true };
            
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });
    }
    
    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(SOAP_URL, "");
        properties.put(SOAP_METHOD, "Press Get Methods");
        return properties;
    }
    
    public boolean checkProperties(Properties props)
    {
        return false;
    }
    
    public void setDropDownItems()
    {
        //methodList
    }
    
    public String buildHost()
    {
        return "axis:" + serviceEndpoint.getText() + "/" + (String)method.getSelectedItem();
    }
    
    public List getParameters()
    {
        ArrayList <WSParameter> parameters = new ArrayList<WSParameter>();
        
        for(int i = 0; i < paramTable.getRowCount(); i++)
        {
            WSParameter param = new WSParameter();
            param.setName((String)paramTable.getValueAt(i,PARAMETER_COLUMN));
            param.setName((String)paramTable.getValueAt(i,TYPE_COLUMN));
            param.setName((String)paramTable.getValueAt(i,VALUE_COLUMN));
            parameters.add(param);
        }
        
        return parameters;
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        URL = new javax.swing.JLabel();
        soapUrl = new com.webreach.mirth.client.ui.components.MirthTextField();
        getMethodsButton = new javax.swing.JButton();
        method = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel1 = new javax.swing.JLabel();
        paramPane = new javax.swing.JScrollPane();
        paramTable = new com.webreach.mirth.client.ui.components.MirthTable();
        jLabel2 = new javax.swing.JLabel();
        serviceEndpoint = new com.webreach.mirth.client.ui.components.MirthTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "SOAP Listener", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        URL.setText("WSDL URL:");

        getMethodsButton.setText("Get Methods");
        getMethodsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                getMethodsButtonActionPerformed(evt);
            }
        });

        method.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Press Get Methods" }));

        jLabel1.setText("Method:");

        paramTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String []
            {
                "Parameter", "Type", "Value"
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        paramPane.setViewportView(paramTable);

        jLabel2.setText("Service Endpoint:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(URL)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, paramPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, serviceEndpoint, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, soapUrl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, method, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(getMethodsButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(URL)
                    .add(getMethodsButton)
                    .add(soapUrl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(serviceEndpoint, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(method, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(paramPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void getMethodsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_getMethodsButtonActionPerformed
    {//GEN-HEADEREND:event_getMethodsButtonActionPerformed
        //methodList = parent.mirthClient.getMethods(soapUrl.getText().trim());
        
    }//GEN-LAST:event_getMethodsButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.JButton getMethodsButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private com.webreach.mirth.client.ui.components.MirthComboBox method;
    private javax.swing.JScrollPane paramPane;
    private com.webreach.mirth.client.ui.components.MirthTable paramTable;
    private com.webreach.mirth.client.ui.components.MirthTextField serviceEndpoint;
    private com.webreach.mirth.client.ui.components.MirthTextField soapUrl;
    // End of variables declaration//GEN-END:variables

}
