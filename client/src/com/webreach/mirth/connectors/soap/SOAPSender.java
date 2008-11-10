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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdesktop.swingworker.SwingWorker;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sun.misc.BASE64Decoder;

import com.l2fprod.common.propertysheet.Property;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.BeanBinder;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.MirthFileFilter;
import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.QueuedSenderProperties;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.ws.WSDefinition;
import com.webreach.mirth.model.ws.WSOperation;
import com.webreach.mirth.model.ws.WSParameter;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class SOAPSender extends ConnectorClass
{
    public final int PARAMETER_COLUMN = 0;

    public final int TYPE_COLUMN = 1;

    public final int VALUE_COLUMN = 2;

    public final String PARAMETER_COLUMN_NAME = "Parameter";

    public final String TYPE_COLUMN_NAME = "Type";

    public final String VALUE_COLUMN_NAME = "Value";

    WSDefinition definition = new WSDefinition();

    ObjectXMLSerializer serializer = new ObjectXMLSerializer();

    private BeanBinder beanBinder;

    private DefaultMutableTreeNode currentNode;

    private HashMap channelList;

    public SOAPSender()
    {
        name = SOAPSenderProperties.name;
        initComponents();
        propertySheetPanel1.setRestoreToggleStates(true);
        SyntaxDocument document = new SyntaxDocument();
        document.setTokenMarker(new XMLTokenMarker());
        soapEnvelope.setDocument(document);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        
        if (generateEnvelopeYesButton.isSelected())
            properties.put(SOAPSenderProperties.SOAP_GENERATE_ENVELOPE, UIConstants.YES_OPTION);
        else
            properties.put(SOAPSenderProperties.SOAP_GENERATE_ENVELOPE, UIConstants.NO_OPTION);
        
        properties.put(SOAPSenderProperties.SOAP_ENVELOPE, soapEnvelope.getText());
        properties.put(SOAPSenderProperties.DATATYPE, name);
        
        properties.put(QueuedSenderProperties.RECONNECT_INTERVAL, reconnectInterval.getText());
        
        if (usePersistentQueuesYesRadio.isSelected())
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.YES_OPTION);
        else
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.NO_OPTION);

        if (rotateMessages.isSelected())
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.YES_OPTION);
        else
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.NO_OPTION);
        
        //TODO: This won't work for HTTPS. WARNING.
        properties.put(SOAPSenderProperties.SOAP_URL, wsdlUrl.getText());
        properties.put(SOAPSenderProperties.SOAP_SERVICE_ENDPOINT, serviceEndpoint.getText());
        if (method.getSelectedIndex() != -1)
            properties.put(SOAPSenderProperties.SOAP_METHOD, (String) method.getSelectedItem());
        if (definition == null)
            definition = new WSDefinition();
        
        //the definition object can be large, so let's zip it and base64 encode it
        try{
	        String encodedDefintion = SOAPSenderProperties.zipAndEncodeDefinition(definition);
	        properties.put(SOAPSenderProperties.SOAP_DEFINITION, encodedDefintion);// getParameters());
        }catch (Exception e){
        	e.printStackTrace();
        }
      
        properties.put(SOAPSenderProperties.SOAP_HOST, buildHost());

        properties.put(SOAPSenderProperties.SOAP_ACTION_URI, soapActionURI.getText());
        properties.put(SOAPSenderProperties.CHANNEL_ID, channelList.get((String) channelNames.getSelectedItem()));
        
        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        //decode and decompress our definition
        String encodedDefinition = props.getProperty(SOAPSenderProperties.SOAP_DEFINITION);
       
        try{
        	if (encodedDefinition != null){
	        	byte[] byteDefinition = new BASE64Decoder().decodeBuffer(encodedDefinition);
		        ByteArrayInputStream bais = new ByteArrayInputStream(byteDefinition);
		        GZIPInputStream gs = new GZIPInputStream(bais);
		        ObjectInputStream ois = new ObjectInputStream(gs);
		        definition = (WSDefinition) ois.readObject();
		        ois.close();
		        bais.close();
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }
        wsdlUrl.setText((String) props.get(SOAPSenderProperties.SOAP_URL));
        serviceEndpoint.setText((String) props.get(SOAPSenderProperties.SOAP_SERVICE_ENDPOINT));
        
        // The model should be set before methods use it.
        if (props.getProperty(SOAPSenderProperties.SOAP_METHOD) != null)
        {
            method.setModel(new javax.swing.DefaultComboBoxModel(new String[] { (String) props.getProperty(SOAPSenderProperties.SOAP_METHOD) }));
        }
        
        if (((String) props.get(SOAPSenderProperties.SOAP_GENERATE_ENVELOPE)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            generateEnvelopeYesButton.setSelected(true);
            generateEnvelopeYesButtonActionPerformed(null);
        }
        else
        {
            generateEnvelopeNoButton.setSelected(true);
            generateEnvelopeNoButtonActionPerformed(null);
        }
        
        soapEnvelope.setText((String) props.getProperty(SOAPSenderProperties.SOAP_ENVELOPE));
        soapActionURI.setText((String) props.getProperty(SOAPSenderProperties.SOAP_ACTION_URI));

        reconnectInterval.setText((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL));
        
        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION))
        {
            usePersistentQueuesYesRadio.setSelected(true);
            usePersistentQueuesYesRadioActionPerformed(null);
        }
        else
        {
            usePersistentQueuesNoRadio.setSelected(true);
            usePersistentQueuesNoRadioActionPerformed(null);
        }
        
        if (((String) props.get(QueuedSenderProperties.ROTATE_QUEUE)).equals(UIConstants.YES_OPTION))
            rotateMessages.setSelected(true);
        else
            rotateMessages.setSelected(false);
        
        if (props.get(SOAPSenderProperties.SOAP_DEFINITION) != null)
        {
            WSOperation operation = definition.getOperation((String) props.getProperty(SOAPSenderProperties.SOAP_METHOD));
            if (operation != null)
                setupTable(operation);
            else {
                // dans: added for bug MIRTH-743
                jTree1 = new javax.swing.JTree();                
                jTree1.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("")));
                jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
                {
                    public void valueChanged(javax.swing.event.TreeSelectionEvent evt)
                    {
                        jTree1ValueChanged(evt);
                    }
                });
                jScrollPane1.setViewportView(jTree1);
            }

        }
        else{
            setupTable(null);
        }
        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap();
        channelList.put("None", "sink");
        channelNameArray.add("None");
        
        String selectedChannelName = "None";
        
        for (Channel channel : parent.channels.values())
        {
        	if (((String) props.get(SOAPSenderProperties.CHANNEL_ID)).equalsIgnoreCase(channel.getId()))
        		selectedChannelName = channel.getName();
        	
            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }
        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));

        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        channelNames.setSelectedItem(selectedChannelName);

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }
    
    public Properties getDefaults()
    {
        return new SOAPSenderProperties().getDefaults();
    }
    
    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.getProperty(SOAPSenderProperties.SOAP_METHOD)).equals(SOAPSenderProperties.SOAP_DEFAULT_DROPDOWN))
        {
            valid = false;
            if (highlight)
            	method.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.getProperty(SOAPSenderProperties.SOAP_URL)).length() == 0)
        {
            valid = false;
            if (highlight)
            	wsdlUrl.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.getProperty(SOAPSenderProperties.SOAP_ENVELOPE)).length() == 0)
        {
            valid = false;
            if (highlight)
            	soapEnvelope.setBackground(UIConstants.INVALID_COLOR);
        }
        
        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION) && ((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL)).length() == 0)
        {
            valid = false;
            if (highlight)
            	reconnectInterval.setBackground(UIConstants.INVALID_COLOR);
        }
        
        return valid;
    }

    private OutputStream convert(String aString)
    {
	    //Convert the string to a byte array
	    byte[] byteArray = aString.getBytes();
	    //Create a stream of that byte array
	    ByteArrayOutputStream out = new ByteArrayOutputStream(byteArray.length);
	    try
	    {
		    //Write the data to that stream
		    out.write(byteArray);
	    } catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    //Cast to OutputStream and return
	    return (OutputStream) out;
    }
    private void resetInvalidProperties()
    {
        method.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
        wsdlUrl.setBackground(null);
        soapEnvelope.setBackground(null);
    }
    
    public String doValidate(Properties props, boolean highlight)
    {
    	String error = null;
    	
    	if (!checkProperties(props, highlight))
    		error = "Error in the form for connector \"" + getName() + "\".\n\n";
    	
    	return error;
    }

    public String buildHost()
    {
        return "axis:" + serviceEndpoint.getText() + "?method=" + (String) method.getSelectedItem();
    }

    public List getParameters()
    {
        return null;
        /*
         * ArrayList parameters = new ArrayList();
         * 
         * for(int i = 0; i < paramTable.getRowCount(); i++) { WSParameter param =
         * new WSParameter();
         * param.setName((String)paramTable.getValueAt(i,PARAMETER_COLUMN));
         * param.setType((String)paramTable.getValueAt(i,TYPE_COLUMN));
         * param.setValue((String)paramTable.getValueAt(i,VALUE_COLUMN));
         * parameters.add(param); }
         * 
         * return parameters;
         */
    }

    public void setupTable(WSOperation operation)
    {
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode(method.getSelectedItem());
        DefaultMutableTreeNode body = new DefaultMutableTreeNode("Body");
        
        
    	if (operation != null){		
    		if (operation.getHeader() != null){
    			 DefaultMutableTreeNode headers = new DefaultMutableTreeNode("Headers");
    			buildParams(headers, operation.getHeader());
    			root.add(headers);
    		}
    		
	    	List<WSParameter> parameters = operation.getParameters();	       
	        Iterator<WSParameter> paramIterator = parameters.iterator();
	        DefaultMutableTreeNode currentNode = body;
	        while (paramIterator.hasNext())
	        {
	            WSParameter parameter = paramIterator.next();
	            buildParams(body, parameter);
	        }
    	}
    	root.add(body);
        propertySheetPanel1.setProperties(new Property[] {});
        jTree1 = new JTree(root);
        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
        {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt)
            {
                jTree1ValueChanged(evt);
            }
        });

        jScrollPane1.setViewportView(jTree1);
        int row = 0;
        while (row < jTree1.getRowCount()) {
        	jTree1.expandRow(row);
            row++;
        }
        if (!(soapEnvelope.getText().length() > 0)){
            buildSoapEnvelope();
        }
    }

    private void buildParams(DefaultMutableTreeNode parentNode, WSParameter parameter)
    {

        // If this is a complex type, we need to add the sub nodes
        if (parameter.isComplex())
        {
            // loop through each param of the complex type
            DefaultMutableTreeNode pNode;
            if (!parameter.getName().equals("parameters"))
                pNode = new DefaultMutableTreeNode(parameter);
            else
                pNode = parentNode;
            Iterator<WSParameter> paramIter = parameter.getChildParameters().iterator();
            while (paramIter.hasNext())
            {
                WSParameter child = paramIter.next();
                DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(child);
                if (child.isComplex())
                {
                    buildParams(subNode, child);
                    if (subNode.getChildCount() > 0)
                        pNode.add((DefaultMutableTreeNode) subNode.getFirstChild());
                    else
                        pNode.add(subNode);
                }
                else
                {
                    pNode.add(subNode);
                }
            }
            if (parentNode != pNode)
                parentNode.add(pNode);

        }
        else
        {

            DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(parameter);
            parentNode.add(pNode);
            return;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        generateEnvelopeButtonGroup = new javax.swing.ButtonGroup();
        userPersistentQueuesButtonGroup = new javax.swing.ButtonGroup();
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
        jLabel4 = new javax.swing.JLabel();
        rebuildEnvelope = new javax.swing.JButton();
        channelNames = new com.webreach.mirth.client.ui.components.MirthComboBox();
        URL1 = new javax.swing.JLabel();
        generateEnvelopeLabel = new javax.swing.JLabel();
        generateEnvelopeYesButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        generateEnvelopeNoButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        rotateMessages = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        usePersistentQueuesNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        usePersistentQueuesYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        reconnectInterval = new com.webreach.mirth.client.ui.components.MirthTextField();
        reconnectIntervalLabel = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        browseWSDLfileButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        URL.setText("WSDL Path:");

        wsdlUrl.setToolTipText("Enter the full URL to the WSDL file describing the web service method to be called, and then click the Get Methods button.");

        getMethodsButton.setText("Get Methods");
        getMethodsButton.setToolTipText("<html>Clicking this button fetches the WSDL file from the specified URL<br> and parses it to obtain a description of the data types and methods used by the web service to be called.<br>It replaces the values of all of the controls below by values taken from the WSDL file.</html>");
        getMethodsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getMethodsButtonActionPerformed(evt);
            }
        });

        method.setToolTipText("Select the web service method to be called from this list.");
        method.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                methodItemStateChanged(evt);
            }
        });

        jLabel1.setText("Method:");

        jLabel2.setText("Service Endpoint URI:");

        soapActionURI.setToolTipText("<html>Enter the SOAP Action URI for the method to be called here.<br>This field is normally filled in automatically when the Get Methods button is clicked and does not need to be changed.</html>");

        soapEnvelope.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTree1.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("")));
        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jTree1);

        propertySheetPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        propertySheetPanel1.setAutoscrolls(true);

        jLabel3.setText("Soap Action URI:");

        serviceEndpoint.setToolTipText("<html>Enter the Service Endpoint URI for the method to be called here.<br>This field is normally filled in automatically when the Get Methods button is clicked and does not need to be changed.</html>");

        jLabel4.setText("SOAP Envelope:");

        rebuildEnvelope.setText("Rebuild Envelope");
        rebuildEnvelope.setToolTipText("<html>Clicking this button regenerates the contents of the SOAP Envelope control based on the other controls,<br> discarding any changes that may have been made.</html>");
        rebuildEnvelope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rebuildEnvelopeActionPerformed(evt);
            }
        });

        channelNames.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        channelNames.setToolTipText("Select None to ignore the response from the web service method, or select a channel to send to as a new inbound message.");

        URL1.setText("Send Response to:");

        generateEnvelopeLabel.setText("Generate Envelope:");

        generateEnvelopeYesButton.setBackground(new java.awt.Color(255, 255, 255));
        generateEnvelopeYesButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        generateEnvelopeButtonGroup.add(generateEnvelopeYesButton);
        generateEnvelopeYesButton.setText("Yes");
        generateEnvelopeYesButton.setToolTipText("<html>When \"Yes\" is selected, any change to the controls above replaces the contents of the SOAP Envelope control below,<br> which is the actual SOAP envelope that will be sent to the web service to send the outbound message.<br>When \"No\" is selected, changes to the controls above are not reflected in the SOAP Envelope control.</html>");
        generateEnvelopeYesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        generateEnvelopeYesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateEnvelopeYesButtonActionPerformed(evt);
            }
        });

        generateEnvelopeNoButton.setBackground(new java.awt.Color(255, 255, 255));
        generateEnvelopeNoButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        generateEnvelopeButtonGroup.add(generateEnvelopeNoButton);
        generateEnvelopeNoButton.setText("No");
        generateEnvelopeNoButton.setToolTipText("<html>When \"Yes\" is selected, any change to the controls above replaces the contents of the SOAP Envelope control below,<br> which is the actual SOAP envelope that will be sent to the web service to send the outbound message.<br>When \"No\" is selected, changes to the controls above are not reflected in the SOAP Envelope control.</html>");
        generateEnvelopeNoButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        generateEnvelopeNoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateEnvelopeNoButtonActionPerformed(evt);
            }
        });

        rotateMessages.setBackground(new java.awt.Color(255, 255, 255));
        rotateMessages.setText("Rotate Messages in Queue");

        usePersistentQueuesNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        userPersistentQueuesButtonGroup.add(usePersistentQueuesNoRadio);
        usePersistentQueuesNoRadio.setSelected(true);
        usePersistentQueuesNoRadio.setText("No");
        usePersistentQueuesNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesNoRadioActionPerformed(evt);
            }
        });

        usePersistentQueuesYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        userPersistentQueuesButtonGroup.add(usePersistentQueuesYesRadio);
        usePersistentQueuesYesRadio.setText("Yes");
        usePersistentQueuesYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesYesRadioActionPerformed(evt);
            }
        });

        reconnectIntervalLabel.setText("Reconnect Interval (ms):");

        jLabel36.setText("Use Persistent Queues:");

        browseWSDLfileButton.setText("Browse...");
        browseWSDLfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseWSDLfileButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(reconnectIntervalLabel)
                    .add(jLabel36)
                    .add(generateEnvelopeLabel)
                    .add(URL1)
                    .add(jLabel2)
                    .add(URL)
                    .add(jLabel3)
                    .add(jLabel1)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(soapEnvelope, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(wsdlUrl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseWSDLfileButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(getMethodsButton))
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(propertySheetPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(layout.createSequentialGroup()
                        .add(method, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rebuildEnvelope, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(generateEnvelopeYesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(generateEnvelopeNoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(channelNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(usePersistentQueuesYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(usePersistentQueuesNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rotateMessages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(reconnectInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(serviceEndpoint, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                    .add(soapActionURI, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(URL)
                    .add(getMethodsButton)
                    .add(browseWSDLfileButton)
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
                    .add(URL1)
                    .add(channelNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel36)
                    .add(usePersistentQueuesYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(usePersistentQueuesNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rotateMessages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(reconnectIntervalLabel)
                    .add(reconnectInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(method, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rebuildEnvelope))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, 0, 0, Short.MAX_VALUE)
                    .add(propertySheetPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE))
                .add(7, 7, 7)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(generateEnvelopeLabel)
                    .add(generateEnvelopeYesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(generateEnvelopeNoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(soapEnvelope, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void generateEnvelopeNoButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_generateEnvelopeNoButtonActionPerformed
    {//GEN-HEADEREND:event_generateEnvelopeNoButtonActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_generateEnvelopeNoButtonActionPerformed

    private void generateEnvelopeYesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_generateEnvelopeYesButtonActionPerformed
    {//GEN-HEADEREND:event_generateEnvelopeYesButtonActionPerformed
    	//Build the envelope when the user selects "yes"
    	buildSoapEnvelope();
   
    }//GEN-LAST:event_generateEnvelopeYesButtonActionPerformed

private void usePersistentQueuesNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesNoRadioActionPerformed
rotateMessages.setEnabled(false);
reconnectInterval.setEnabled(false);
reconnectIntervalLabel.setEnabled(false);
}//GEN-LAST:event_usePersistentQueuesNoRadioActionPerformed

private void usePersistentQueuesYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesYesRadioActionPerformed
rotateMessages.setEnabled(true);
reconnectInterval.setEnabled(true);
reconnectIntervalLabel.setEnabled(true);
}//GEN-LAST:event_usePersistentQueuesYesRadioActionPerformed

private void browseWSDLfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseWSDLfileButtonActionPerformed
    JFileChooser wsdlXMLfile = new JFileChooser();
    wsdlXMLfile.setFileFilter(new MirthFileFilter("XML"));
    int returnVal = wsdlXMLfile.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        wsdlUrl.setText(wsdlXMLfile.getSelectedFile().getPath());
    }
}//GEN-LAST:event_browseWSDLfileButtonActionPerformed

    private void rebuildEnvelopeActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_rebuildEnvelopeActionPerformed
        buildSoapEnvelope();
    }// GEN-LAST:event_rebuildEnvelopeActionPerformed

    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt)
    {// GEN-FIRST:event_jTree1ValueChanged
        // if (currentNode != null)
        // ((DefaultTreeModel)jTree1.getModel()).nodeChanged(currentNode);
        if (beanBinder != null)
        {
            beanBinder.setWriteEnabled(false);
            beanBinder.unbind();
        }

        DefaultMutableTreeNode nodeSelected = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        if (nodeSelected != null && nodeSelected.getUserObject() != null && nodeSelected.getUserObject() instanceof WSParameter)
        {
            currentNode = nodeSelected;
            ActionListener updateListener = new ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    ((DefaultTreeModel) jTree1.getModel()).nodeChanged(currentNode);
                    buildSoapEnvelope();
                }

            };
            beanBinder = new BeanBinder((WSParameter) nodeSelected.getUserObject(), propertySheetPanel1, updateListener);
            beanBinder.setWriteEnabled(true);
            if (((WSParameter) nodeSelected.getUserObject()).isComplex())
                propertySheetPanel1.removeProperty(propertySheetPanel1.getProperties()[2]);
        }
    }// GEN-LAST:event_jTree1ValueChanged

    private void buildSoapEnvelope()
    {
    	if (generateEnvelopeNoButton.isSelected()){
    		//If the user has generateEnvelope turned off, then just ignore the request
    		return;
    	}
        StringBuilder soapEnvelopeString = new StringBuilder();
        soapEnvelopeString.append(SOAPSenderProperties.SOAP_ENVELOPE_HEADER);
        WSOperation operation = null;
        if (method.getSelectedItem() != null){
        	operation = definition.getOperation(method.getSelectedItem().toString());
        }
        if (operation != null)
        {
            Document document = null;
            try
            {
            	document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            	Element root = document.createElement("root");
            	Element soapHeaderEl =null;
            	if (operation.getHeader() != null)
            	{
            		WSParameter header = operation.getHeader();
            		soapHeaderEl = document.createElement("soap:Header");
            		Element headerEl = document.createElement(header.getType());
            		//ignore the name space for now, use the paramter nanmespce 
            		//if (operation.getHeaderNamespace() != null && operation.getHeaderNamespace().length() > 0){
            		//	headerEl.setAttribute("xmlns", operation.getHeaderNamespace());
            		//}
                    
                    if (header.isComplex())
                    {
                        if (header.getSchemaType() != null)
                        {
                        	headerEl.setAttribute("xmlns", header.getSchemaType().getTypeName().getNamespaceURI());
                        }
                        buildSoapEnvelope(document, headerEl, header);
                    }
                    else
                    {
                    	headerEl.appendChild(document.createTextNode(header.getValue()));
                        // paramEl.setNodeValue(param.getValue());
                    }
                    soapHeaderEl.appendChild(headerEl);    
                    root.appendChild(soapHeaderEl);
                    
            	}
            	Element bodyEl = document.createElement("soap:Body");
        
                Element operationEl = document.createElement(operation.getName());
                operationEl.setAttribute("xmlns", operation.getNamespace());
                // add each parameter and sub params
                Iterator<WSParameter> iterator = operation.getParameters().iterator();
                while (iterator.hasNext())
                {
                    WSParameter param = iterator.next();
                    if (param.getName().equals("parameters") && param.isComplex())
                    {
                        buildSoapEnvelope(document, operationEl, param);
                    }
                    else
                    {
                        Element paramEl = document.createElement(param.getName());
                        if (param.isComplex())
                        {
                            if (param.getSchemaType() != null)
                            {
                                paramEl.setAttribute("xmlns", param.getSchemaType().getTypeName().getNamespaceURI());
                            }
                            buildSoapEnvelope(document, paramEl, param);
                        }
                        else
                        {
                            paramEl.appendChild(document.createTextNode(param.getValue()));
                            // paramEl.setNodeValue(param.getValue());
                        }
                        operationEl.appendChild(paramEl);
                    }

                }
                bodyEl.appendChild(operationEl);
                root.appendChild(bodyEl);
                document.appendChild(root);
                document.getDocumentElement().normalize();
                StringWriter output = new StringWriter();
                try
                {
                    TransformerFactory tf = TransformerFactory.newInstance();
                    tf.setAttribute("indent-number", new Integer(2));
                    Transformer t = tf.newTransformer();

                    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    t.setOutputProperty(OutputKeys.INDENT, "yes");
                    t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                    
                    if (soapHeaderEl != null){
                    	t.transform(new DOMSource(soapHeaderEl), new StreamResult(output));
                    }
                    t.transform(new DOMSource(bodyEl), new StreamResult(output));
                }
                catch (TransformerConfigurationException e)
                {
                    e.printStackTrace();
                }
                catch (TransformerException e)
                {
                    e.printStackTrace();
                }
                catch (TransformerFactoryConfigurationError e)
                {
                    e.printStackTrace();
                }

                soapEnvelopeString.append(output.toString());
            }
            catch (ParserConfigurationException e)
            {
                e.printStackTrace();
            }
        }
        
        soapEnvelopeString.append(SOAPSenderProperties.SOAP_ENVELOPE_FOOTER);
        soapEnvelope.setText(soapEnvelopeString.toString().replaceAll("&gt;", ">").replaceAll("&lt;", "<").replaceAll("&apos;", "'").replaceAll("&quot;", "\"").replaceAll("&amp;", "&"));
        parent.enableSave();
    }

    private void buildSoapEnvelope(Document document, Element parent, WSParameter parameter)
    {
    	try{
	        Iterator<WSParameter> iterator = parameter.getChildParameters().iterator();
	        while (iterator.hasNext())
	        {
	            WSParameter param = iterator.next();
	            Element paramEl = document.createElement(param.getName());
	
	            if (param.isComplex())
	            {
	                if (param.getSchemaType() != null)
	                {
	                    // paramEl.setAttribute("xmlns",
	                    // param.getSchemaType().getTypeName().getNamespaceURI());
	                    Attr atr = document.createAttribute("xmlns");
	                    atr.setValue("");
	                    paramEl.setAttributeNodeNS(atr);
	                }
	                // only add co
	                buildSoapEnvelope(document, paramEl, param);
	            }
	            else
	            {
	                paramEl.appendChild(document.createTextNode(param.getValue()));
	            }
	            if (parent != paramEl){
	                parent.appendChild(paramEl);
	            }
	        }
        }catch (Exception e){
        	
        }
      }
    	

   

    private void methodItemStateChanged(java.awt.event.ItemEvent evt)// GEN-FIRST:event_methodItemStateChanged
    {// GEN-HEADEREND:event_methodItemStateChanged
        if (definition != null)
        {
            if (evt.getStateChange() == evt.SELECTED)
            {
                String item = evt.getItem().toString();
                if (item.equals(SOAPSenderProperties.SOAP_DEFAULT_DROPDOWN))
                    return;
                else
                {
                    soapActionURI.setText(definition.getOperations().get(method.getSelectedItem()).getSoapActionURI());
                    setupTable(definition.getOperations().get(method.getSelectedItem()));
                    buildSoapEnvelope();
                }
            }
        }
    }// GEN-LAST:event_methodItemStateChanged

    private void getMethodsButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_getMethodsButtonActionPerformed
    {// GEN-HEADEREND:event_getMethodsButtonActionPerformed

        parent.setWorking("Getting methods...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {

                try
                {
                    definition = (WSDefinition) parent.mirthClient.invokeConnectorService(name, "getWebServiceDefinition", wsdlUrl.getText().trim());
                    if (definition == null)
                        throw new ClientException("No WSDL Methods Found.");
                    return null;
                }
                catch (ClientException e)
                {

                    parent.alertError(parent, "No methods found.  Check the WSDL URL and try again.");
                    return null;
                }
            }

            public void done()
            {
            	if (definition != null){
	                String[] methodNames = new String[definition.getOperations().size()];
	                Iterator<WSOperation> opIterator = definition.getOperations().values().iterator();
	
	                for (int i = 0; i < definition.getOperations().size(); i++)
	                {
	                    methodNames[i] = opIterator.next().getName();
	                }
	
	                method.setModel(new javax.swing.DefaultComboBoxModel(methodNames));
	                if (methodNames.length > 0)
	                {
	                    method.setSelectedIndex(0);
	                    serviceEndpoint.setText(definition.getServiceEndpointURI());
	                    soapActionURI.setText(definition.getOperations().get(method.getSelectedItem()).getSoapActionURI());
	                    setupTable(definition.getOperations().get(method.getSelectedItem()));
	                    buildSoapEnvelope();
	                }
            	}
                parent.setWorking("", false);
            }

        };
        worker.execute();
    }// GEN-LAST:event_getMethodsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL;
    private javax.swing.JLabel URL1;
    private javax.swing.JButton browseWSDLfileButton;
    private com.webreach.mirth.client.ui.components.MirthComboBox channelNames;
    private javax.swing.ButtonGroup generateEnvelopeButtonGroup;
    private javax.swing.JLabel generateEnvelopeLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton generateEnvelopeNoButton;
    private com.webreach.mirth.client.ui.components.MirthRadioButton generateEnvelopeYesButton;
    private javax.swing.JButton getMethodsButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    private com.webreach.mirth.client.ui.components.MirthComboBox method;
    private com.l2fprod.common.propertysheet.PropertySheetPanel propertySheetPanel1;
    private javax.swing.JButton rebuildEnvelope;
    private com.webreach.mirth.client.ui.components.MirthTextField reconnectInterval;
    private javax.swing.JLabel reconnectIntervalLabel;
    private com.webreach.mirth.client.ui.components.MirthCheckBox rotateMessages;
    private com.webreach.mirth.client.ui.components.MirthTextField serviceEndpoint;
    private com.webreach.mirth.client.ui.components.MirthTextField soapActionURI;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea soapEnvelope;
    private com.webreach.mirth.client.ui.components.MirthRadioButton usePersistentQueuesNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton usePersistentQueuesYesRadio;
    private javax.swing.ButtonGroup userPersistentQueuesButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthTextField wsdlUrl;
    // End of variables declaration//GEN-END:variables

}
