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

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.WSDefinition;
import com.webreach.mirth.model.WSParameter;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.webreach.mirth.client.ui.BeanBinder;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;

import org.apache.wsif.schema.ComplexType;
import org.apache.wsif.schema.ElementType;
import org.apache.wsif.schema.SchemaType;
import org.apache.wsif.schema.SequenceElement;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

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
    WSDefinition definition;
    private BeanBinder beanBinder;
    private DefaultMutableTreeNode currentNode;
    
    /**
     * Creates new form SOAPListener
     */
    public final String DATATYPE = "DataType";
    public final String SOAP_HOST = "host";
    public final String SOAP_SERVICE_ENDPOINT = "serviceEndpoint";
    public final String SOAP_URL = "wsdlUrl";
    public final String SOAP_METHOD = "method";
    public final String SOAP_PARAMETERS = "parameters";
    public final String SOAP_DEFAULT_DROPDOWN = "Press Get Methods";
    public final String SOAP_ENVELOPE = "soapEnvelope";
    public final String SOAP_ACTION_URI = "soapActionURI";
    
    public SOAPSender()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "SOAP Sender";
        initComponents();
        propertySheetPanel1.setRestoreToggleStates(true);
        SyntaxDocument document = new SyntaxDocument();
        document.setTokenMarker(new XMLTokenMarker());
        soapEnvelope.setDocument(document);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(SOAP_URL, wsdlUrl.getText());
        properties.put(SOAP_SERVICE_ENDPOINT, serviceEndpoint.getText());
        properties.put(SOAP_METHOD, (String)method.getSelectedItem());
        properties.put(SOAP_PARAMETERS, new ArrayList());//getParameters());
        properties.put(SOAP_HOST, buildHost());
        properties.put(SOAP_ENVELOPE, soapEnvelope.getText());
        properties.put(SOAP_ACTION_URI, soapActionURI.getText());
        return properties;
    }

    public void setProperties(Properties props)
    {
        definition = null;
        
        wsdlUrl.setText((String)props.get(SOAP_URL));
        serviceEndpoint.setText((String)props.get(SOAP_SERVICE_ENDPOINT));
        soapEnvelope.setText((String)props.getProperty(SOAP_ENVELOPE));
        soapActionURI.setText((String)props.getProperty(SOAP_ACTION_URI));
        if(props.getProperty(SOAP_METHOD) != null)
        {    
            method.setModel(new javax.swing.DefaultComboBoxModel(new String[] { (String)props.getProperty(SOAP_METHOD) }));
        }
        
        if(props.get(SOAP_PARAMETERS) != null)
            setupTable((ArrayList<WSParameter>) props.get(SOAP_PARAMETERS));
        else
            setupTable(new ArrayList<WSParameter>());
    }
    
    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(SOAP_URL, "");
        properties.put(SOAP_SERVICE_ENDPOINT, "");
        properties.put(SOAP_METHOD, SOAP_DEFAULT_DROPDOWN);
        properties.put(SOAP_PARAMETERS, new ArrayList<WSParameter>());
        properties.put(SOAP_HOST, buildHost());
        properties.put(SOAP_ENVELOPE, "");
        properties.put(SOAP_ACTION_URI, "");
        
        return properties;
    }
    
    public boolean checkProperties(Properties props)
    {
        if(!((String)props.getProperty(SOAP_METHOD)).equals(SOAP_DEFAULT_DROPDOWN) && 
                ((String)props.getProperty(SOAP_URL)).length() > 0 &&
                ((String)props.getProperty(SOAP_SERVICE_ENDPOINT)).length() > 0 &&
                ((String)props.getProperty(SOAP_ENVELOPE)).length() > 0)
            return true;
        return false;
    }
    
    public String buildHost()
    {
        return "axis:" + serviceEndpoint.getText() + "?method=" + (String)method.getSelectedItem();
    }
    
    public List getParameters()
    {
    	return null;
       /* ArrayList parameters = new ArrayList();
        
        for(int i = 0; i < paramTable.getRowCount(); i++)
        {
            WSParameter param = new WSParameter();
            param.setName((String)paramTable.getValueAt(i,PARAMETER_COLUMN));
            param.setType((String)paramTable.getValueAt(i,TYPE_COLUMN));
            param.setValue((String)paramTable.getValueAt(i,VALUE_COLUMN));
            parameters.add(param);
        }
        
        return parameters;
        */
    }
    
    public void setupTable(List<WSParameter> parameters)
    {
    	
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode(method.getSelectedItem());
    	DefaultMutableTreeNode headers = new DefaultMutableTreeNode("Headers");
    	DefaultMutableTreeNode body = new DefaultMutableTreeNode("Body");
    	root.add(headers);
    	root.add(body);
    	Iterator<WSParameter> paramIterator = parameters.iterator();
    	DefaultMutableTreeNode currentNode = body;
    	while (paramIterator.hasNext()){
    		WSParameter parameter = paramIterator.next();
    		buildParams(body,parameter);
    	}
    	jTree1 = new JTree(root);
    	jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });

    	jScrollPane1.setViewportView(jTree1);
    	/*
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
        paramPane.setViewportView(paramTable);
        */
    }
    private void buildParams(DefaultMutableTreeNode parentNode, WSParameter parameter) {

		//If this is a complex type, we need to add the sub nodes
		if (parameter.isComplex()){
			//loop through each param of the complex type
					DefaultMutableTreeNode pNode;
					if (!parameter.getName().equals("parameters"))
						pNode = new DefaultMutableTreeNode(parameter);
					else
						pNode = parentNode;
					Iterator<WSParameter> paramIter = parameter.getChildParameters().iterator();
					while(paramIter.hasNext()){
						WSParameter child = paramIter.next();
						System.out.println(child);
						DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(child);
						if (child.isComplex()){
							buildParams(subNode, child);
							if (subNode.getChildCount() > 0)
								pNode.add((DefaultMutableTreeNode)subNode.getFirstChild());
							else
								pNode.add(subNode);
						}else{
							pNode.add(subNode);
						}
					}
					if (parentNode != pNode)
						parentNode.add(pNode);

		}else{
			
			DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(parameter);
			parentNode.add(pNode);
			return;
		}
	}
	
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        URL = new javax.swing.JLabel();
        wsdlUrl = new com.webreach.mirth.client.ui.components.MirthTextField();
        getMethodsButton = new javax.swing.JButton();
        method = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        soapActionURI = new com.webreach.mirth.client.ui.components.MirthTextField();
        soapEnvelope = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(true,false);
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        propertySheetPanel1 = new com.l2fprod.common.propertysheet.PropertySheetPanel();
        jLabel3 = new javax.swing.JLabel();
        serviceEndpoint = new com.webreach.mirth.client.ui.components.MirthTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "SOAP Listener", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        URL.setText("WSDL URL:");

        getMethodsButton.setText("Get Methods");
        getMethodsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getMethodsButtonActionPerformed(evt);
            }
        });

        method.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                methodItemStateChanged(evt);
            }
        });

        jLabel1.setText("Method:");

        jLabel2.setText("Service Endpoint URI:");

        soapEnvelope.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });

        jScrollPane1.setViewportView(jTree1);

        propertySheetPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        propertySheetPanel1.setAutoscrolls(true);

        jLabel3.setText("Soap Action URI:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(URL)
                    .add(jLabel3)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, soapEnvelope, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(propertySheetPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 186, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, serviceEndpoint, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, wsdlUrl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, soapActionURI, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, method, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
                    .add(wsdlUrl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(serviceEndpoint, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(soapActionURI, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(method, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, 0, 0, Short.MAX_VALUE)
                    .add(propertySheetPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(soapEnvelope, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
    	if (currentNode != null)
    		((DefaultTreeModel)jTree1.getModel()).nodeChanged(currentNode);
    	if (beanBinder != null){
    		beanBinder.setWriteEnabled(false);
			beanBinder.unbind();
    	}

    	DefaultMutableTreeNode nodeSelected = (DefaultMutableTreeNode)jTree1.getLastSelectedPathComponent();
    	
    	if (nodeSelected != null && nodeSelected.getUserObject() != null && nodeSelected.getUserObject() instanceof WSParameter){
    		currentNode = nodeSelected;
    		beanBinder = new BeanBinder((WSParameter)nodeSelected.getUserObject(), propertySheetPanel1);
    		beanBinder.setWriteEnabled(true);
    		if (((WSParameter)nodeSelected.getUserObject()).getSchemaType() != null)
    			propertySheetPanel1.removeProperty(propertySheetPanel1.getProperties()[2]);
    		
    		System.out.println(((WSParameter)nodeSelected.getUserObject()));
    	}
    }//GEN-LAST:event_jTree1ValueChanged

    private void methodItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_methodItemStateChanged
    {//GEN-HEADEREND:event_methodItemStateChanged
        if(definition != null)
        {
            if(evt.getStateChange() == evt.SELECTED)
            {
                String item = evt.getItem().toString();
                if(item.equals(SOAP_DEFAULT_DROPDOWN))
                    return;
                else{
                	soapActionURI.setText(definition.getOperations().get(method.getSelectedIndex()).getSoapActionURI());
                    setupTable(definition.getOperations().get(method.getSelectedIndex()).getParameters());
                }
            }
        }
    }//GEN-LAST:event_methodItemStateChanged

    private void getMethodsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_getMethodsButtonActionPerformed
    {//GEN-HEADEREND:event_getMethodsButtonActionPerformed
        try
        {
            definition = parent.mirthClient.getWebServiceDefinition(wsdlUrl.getText().trim());
            String[] methodNames = new String[definition.getOperations().size()];
            for (int i = 0; i < definition.getOperations().size(); i++)
            {
                methodNames[i] = definition.getOperations().get(i).getName();
            }

            method.setModel(new javax.swing.DefaultComboBoxModel(methodNames));

            method.setSelectedIndex(0);
            serviceEndpoint.setText(definition.getServiceEndpointURI());
            soapActionURI.setText(definition.getOperations().get(method.getSelectedIndex()).getSoapActionURI());
            setupTable( definition.getOperations().get(0).getParameters() ); 
        }
        catch(ClientException e)
        {
            parent.alertError("No methods found.  Check the WSDL URL and try again.");
        }
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    private com.webreach.mirth.client.ui.components.MirthComboBox method;
    private com.l2fprod.common.propertysheet.PropertySheetPanel propertySheetPanel1;
    private com.webreach.mirth.client.ui.components.MirthTextField serviceEndpoint;
    private com.webreach.mirth.client.ui.components.MirthTextField soapActionURI;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea soapEnvelope;
    private com.webreach.mirth.client.ui.components.MirthTextField wsdlUrl;
    // End of variables declaration//GEN-END:variables

}
