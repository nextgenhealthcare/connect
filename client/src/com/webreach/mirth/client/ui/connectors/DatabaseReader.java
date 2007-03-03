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

import com.webreach.mirth.client.ui.components.MirthVariableList;
import java.util.List;
import java.util.Properties;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;
import org.syntax.jedit.tokenmarker.TSQLTokenMarker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.client.ui.util.SQLParserUtil;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.DriverInfo;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DocumentSerializer;

/**
 * A form that extends from ConnectorClass.  All methods implemented
 * are described in ConnectorClass.
 */
public class DatabaseReader extends ConnectorClass
{
    /** Creates new form DatabaseWriter */
    private final String DATATYPE = "DataType";
    private final String DATABASE_HOST = "host";
    private final String DATABASE_HOST_VALUE = "query";
    private final String DATABASE_DRIVER = "driver";
    private final String DATABASE_URL = "URL";
    private final String DATABASE_USERNAME = "username";
    private final String DATABASE_PASSWORD = "password";
    private final String DATABASE_POLLING_FREQUENCY = "pollingFrequency";
    private final String DATABASE_SQL_STATEMENT = "query";
    private final String DATABASE_JS_SQL_STATEMENT = "script";
    private final String DATABASE_USE_JS = "useScript";
    private final String DATABASE_USE_ACK = "useAck";
    private final String DATABASE_ACK = "ack";
    private final String DATABASE_JS_ACK = "ackScript";

    private static SyntaxDocument sqlMappingDoc;
    private static SyntaxDocument sqlUpdateMappingDoc;
    private static SyntaxDocument jsMappingDoc;
    private static SyntaxDocument jsUpdateMappingDoc;
    private List <DriverInfo> drivers;
    
    public DatabaseReader()
    {
        name = "Database Reader";
        
        try
        {
            drivers = this.parent.mirthClient.getDatabaseDrivers();
        } 
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }
        
        initComponents();
        String[] driverNames = new String[drivers.size()];
        for (int i = 0; i < drivers.size(); i++)
        {
            driverNames[i] = drivers.get(i).getName();
        }
        
        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(driverNames));        
        sqlMappingDoc = new SyntaxDocument();
        sqlMappingDoc.setTokenMarker(new TSQLTokenMarker());
        sqlUpdateMappingDoc = new SyntaxDocument();
        sqlUpdateMappingDoc.setTokenMarker(new TSQLTokenMarker());
        jsMappingDoc = new SyntaxDocument();
        jsMappingDoc.setTokenMarker(new JavaScriptTokenMarker());
        jsUpdateMappingDoc = new SyntaxDocument();
        jsUpdateMappingDoc.setTokenMarker(new JavaScriptTokenMarker());
        
        databaseSQLTextPane.setDocument(sqlMappingDoc);
        databaseUpdateSQLTextPane.setDocument(sqlUpdateMappingDoc);
        
        databaseSQLTextPane.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
            }
            public void removeUpdate(DocumentEvent e) {
                updateSQL();
            }
            public void insertUpdate(DocumentEvent e) {
                updateSQL();
            }
        });
        
        pollingFreq.setDocument(new MirthFieldConstraints(0, false, true));
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(DATABASE_HOST, DATABASE_HOST_VALUE);
        
        for(int i = 0; i < drivers.size(); i++)
        {
            DriverInfo driver = drivers.get(i);
            if(driver.getName().equalsIgnoreCase(((String)databaseDriverCombobox.getSelectedItem())))
                properties.put(DATABASE_DRIVER, driver.getClassName());
        }

        properties.put(DATABASE_URL, databaseURLField.getText());
        properties.put(DATABASE_USERNAME, databaseUsernameField.getText());
        properties.put(DATABASE_PASSWORD, new String(databasePasswordField.getPassword()));
        properties.put(DATABASE_POLLING_FREQUENCY, pollingFreq.getText());
        
        if(useJavaScriptYes.isSelected())
        {
            properties.put(DATABASE_USE_JS, UIConstants.YES_OPTION);
            properties.put(DATABASE_JS_SQL_STATEMENT, databaseSQLTextPane.getText());
            properties.put(DATABASE_JS_ACK, databaseUpdateSQLTextPane.getText());
            properties.put(DATABASE_SQL_STATEMENT, "");
            properties.put(DATABASE_ACK, "");
        }
        else
        {
            properties.put(DATABASE_USE_JS, UIConstants.NO_OPTION);
            properties.put(DATABASE_SQL_STATEMENT, databaseSQLTextPane.getText());
            properties.put(DATABASE_ACK, databaseUpdateSQLTextPane.getText());
            properties.put(DATABASE_JS_SQL_STATEMENT, "");
            properties.put(DATABASE_JS_ACK, "");
        }
        
        if (readOnUpdateYes.isSelected())
            properties.put(DATABASE_USE_ACK, UIConstants.YES_OPTION);
        else
            properties.put(DATABASE_USE_ACK, UIConstants.NO_OPTION);

        return properties;
    }

    public void setProperties(Properties props)
    {
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
        
        for(int i = 0; i < drivers.size(); i++)
        {
            DriverInfo driver = drivers.get(i);
            if(driver.getClassName().equalsIgnoreCase(((String)props.get(DATABASE_DRIVER))))
                databaseDriverCombobox.setSelectedItem(driver.getName());
        }

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
        databaseURLField.setText((String)props.get(DATABASE_URL));
        databaseUsernameField.setText((String)props.get(DATABASE_USERNAME));
        databasePasswordField.setText((String)props.get(DATABASE_PASSWORD));
        pollingFreq.setText((String)props.get(DATABASE_POLLING_FREQUENCY));
        databaseSQLTextPane.setText((String)props.get(DATABASE_SQL_STATEMENT));
        
        if(((String)props.get(DATABASE_USE_JS)).equals(UIConstants.YES_OPTION))
        {
            useJavaScriptYes.setSelected(true);
            useJavaScriptYesActionPerformed(null);
            databaseSQLTextPane.setText((String)props.get(DATABASE_JS_SQL_STATEMENT));
            databaseUpdateSQLTextPane.setText((String)props.get(DATABASE_JS_ACK));
        }
        else
        {
            useJavaScriptNo.setSelected(true);
            useJavaScriptNoActionPerformed(null);
            databaseSQLTextPane.setText((String)props.get(DATABASE_SQL_STATEMENT));
            databaseUpdateSQLTextPane.setText((String)props.get(DATABASE_ACK));
        }
        
        if(((String)props.get(DATABASE_USE_ACK)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            readOnUpdateYes.setSelected(true);
            readOnUpdateYesActionPerformed(null);
        }
        else
        {
            readOnUpdateNo.setSelected(true);
            readOnUpdateNoActionPerformed(null);
        }
    }

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(DATABASE_HOST, DATABASE_HOST_VALUE);
        properties.put(DATABASE_DRIVER, drivers.get(0).getClassName());
        properties.put(DATABASE_URL, "");
        properties.put(DATABASE_USERNAME, "");
        properties.put(DATABASE_PASSWORD, "");
        properties.put(DATABASE_POLLING_FREQUENCY, "5000");
        properties.put(DATABASE_SQL_STATEMENT, "SELECT FROM");
        properties.put(DATABASE_USE_JS, UIConstants.NO_OPTION);
        properties.put(DATABASE_JS_SQL_STATEMENT, "");
        properties.put(DATABASE_USE_ACK, UIConstants.NO_OPTION);
        properties.put(DATABASE_ACK, "UPDATE");
        properties.put(DATABASE_JS_ACK, "");
        return properties;
    }
    
    public boolean checkProperties(Properties props)
    {
        if(((String)props.get(DATABASE_URL)).length() > 0 && ((String)props.get(DATABASE_POLLING_FREQUENCY)).length() > 0 && ((String)props.get(DATABASE_SQL_STATEMENT)).length() > 0)
        {
            if(((String)props.get(DATABASE_USE_ACK)).equalsIgnoreCase(UIConstants.YES_OPTION) && ((String)props.get(DATABASE_ACK)).length() > 0)
                return true;
            else if(((String)props.get(DATABASE_USE_ACK)).equalsIgnoreCase(UIConstants.NO_OPTION))
                return true;
        }
        return false;
    }  
    
    private void updateSQL()
    {
        Object sqlStatement = databaseSQLTextPane.getText();
        String [] data;
        
        if ((sqlStatement != null) && (!sqlStatement.equals("")))
        {
            SQLParserUtil spu = new SQLParserUtil((String) sqlStatement);
            data = spu.Parse();

        }
        else
        {
            data = new String[]{};
        }
        dbVarList.setListData(data);
        updateIncomingData(data);
    }
    
    private void updateIncomingData(String[] data)
    {
        try
        {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element resultElement = document.createElement("result");
            for(int i = 0; i < data.length; i++)
            {
                Element columnElement = document.createElement(data[i]);
                columnElement.setTextContent("value");
                resultElement.appendChild(columnElement);
            }
            document.appendChild(resultElement);

            DocumentSerializer docSerializer = new DocumentSerializer();
            
            String xml = docSerializer.toXML(document);
            
            parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().setInboundTemplate(xml);
            
            if(parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundProtocol() == MessageObject.Protocol.XML && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate().length() == 0)
            {
                List<Connector> list = parent.channelEditPanel.currentChannel.getDestinationConnectors();
                for(Connector c : list)
                {
                    c.getTransformer().setInboundTemplate(xml);
                }
            }
        } 
        catch (ParserConfigurationException ex)
        {
        }
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        sqlLabel = new javax.swing.JLabel();
        databaseDriverCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        databaseURLField = new com.webreach.mirth.client.ui.components.MirthTextField();
        databaseUsernameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        databasePasswordField = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        onUpdateLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        pollingFreq = new com.webreach.mirth.client.ui.components.MirthTextField();
        readOnUpdateYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        readOnUpdateNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        dbVarList = new com.webreach.mirth.client.ui.components.MirthVariableList();
        databaseSQLTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(true,false);
        databaseUpdateSQLTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(true,false);
        jLabel6 = new javax.swing.JLabel();
        useJavaScriptYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        useJavaScriptNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        generateConnection = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel1.setText("Driver:");

        jLabel2.setText("URL:");

        jLabel3.setText("Username:");

        jLabel4.setText("Password:");

        sqlLabel.setText("SQL:");

        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sun JDBC-ODBC Bridge", "ODBC - MySQL", "ODBC - PostgresSQL", "ODBC - SQL Server/Sybase", "ODBC - Oracle 10g Release 2" }));

        databasePasswordField.setFont(new java.awt.Font("Tahoma", 0, 11));

        onUpdateLabel.setText("On-Update SQL:");

        jLabel7.setText("Polling Frequency (ms):");

        readOnUpdateYes.setBackground(new java.awt.Color(255, 255, 255));
        readOnUpdateYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(readOnUpdateYes);
        readOnUpdateYes.setText("Yes");
        readOnUpdateYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        readOnUpdateYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                readOnUpdateYesActionPerformed(evt);
            }
        });

        readOnUpdateNo.setBackground(new java.awt.Color(255, 255, 255));
        readOnUpdateNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(readOnUpdateNo);
        readOnUpdateNo.setSelected(true);
        readOnUpdateNo.setText("No");
        readOnUpdateNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        readOnUpdateNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                readOnUpdateNoActionPerformed(evt);
            }
        });

        jLabel8.setText("Run On-Update Statment:");

        jScrollPane1.setViewportView(dbVarList);

        databaseSQLTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        databaseUpdateSQLTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setText("Use JavaScript");

        useJavaScriptYes.setBackground(new java.awt.Color(255, 255, 255));
        useJavaScriptYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(useJavaScriptYes);
        useJavaScriptYes.setText("Yes");
        useJavaScriptYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJavaScriptYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                useJavaScriptYesActionPerformed(evt);
            }
        });

        useJavaScriptNo.setBackground(new java.awt.Color(255, 255, 255));
        useJavaScriptNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(useJavaScriptNo);
        useJavaScriptNo.setText("No");
        useJavaScriptNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJavaScriptNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                useJavaScriptNoActionPerformed(evt);
            }
        });

        generateConnection.setText("Insert Connections");
        generateConnection.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                generateConnectionActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(sqlLabel)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(jLabel3)
                    .add(jLabel4)
                    .add(jLabel7)
                    .add(jLabel8)
                    .add(onUpdateLabel)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(readOnUpdateYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(readOnUpdateNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(databaseUpdateSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 95, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(databaseUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(databaseURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(databaseDriverCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(databaseSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .addContainerGap())
                    .add(databasePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(useJavaScriptYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(useJavaScriptNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(pollingFreq, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 176, Short.MAX_VALUE)
                        .add(generateConnection)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(databaseDriverCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(databaseURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(databaseUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(databasePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(pollingFreq, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel7))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(useJavaScriptYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel6)
                            .add(useJavaScriptNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(generateConnection))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sqlLabel)
                    .add(databaseSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(readOnUpdateYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(readOnUpdateNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(onUpdateLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(databaseUpdateSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void generateConnectionActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_generateConnectionActionPerformed
    {//GEN-HEADEREND:event_generateConnectionActionPerformed
       
        String driver = "";
        
        for(int i = 0; i < drivers.size(); i++)
        {
            DriverInfo driverInfo = drivers.get(i);
            if(driverInfo.getName().equalsIgnoreCase(((String)databaseDriverCombobox.getSelectedItem())))
                driver = driverInfo.getClassName();
        }
        
        String connectionText = "Class.forName(\"" + driver + "\");\n" +
                "Properties info = new Properties();\n" +
                "info.setProperty(\"user\", \"" + databaseUsernameField.getText() + "\");\n" +
                "info.setProperty(\"password\", \"" + new String(databasePasswordField.getPassword()) +  "\");\n" +
                "Connection dbConn = DriverManager.getConnection(\"" + databaseURLField.getText() + "\", info);\n\n" +
                "// YOUR CODE GOES HERE\n\n" +
                "dbConn.close();";
        
        String updateText = "Class.forName(\"" + driver + "\");\n" +
                "Properties info = new Properties();\n" +
                "info.setProperty(\"user\", \"" + databaseUsernameField.getText() + "\");\n" +
                "info.setProperty(\"password\", \"" + new String(databasePasswordField.getPassword()) +  "\");\n" +
                "Connection dbConn = DriverManager.getConnection(\"" + databaseURLField.getText() + "\", info);\n\n" +
                "// YOUR CODE GOES HERE\n\n" +
                "dbConn.close();";
        
        databaseSQLTextPane.setText(connectionText + "\n\n" + databaseSQLTextPane.getText());
        databaseUpdateSQLTextPane.setText(updateText + "\n\n" + databaseUpdateSQLTextPane.getText());
        
        parent.enableSave();
    }//GEN-LAST:event_generateConnectionActionPerformed

    private void useJavaScriptNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_useJavaScriptNoActionPerformed
    {//GEN-HEADEREND:event_useJavaScriptNoActionPerformed
        sqlLabel.setText("SQL:");
        onUpdateLabel.setText("On-Update SQL:");
        databaseSQLTextPane.setDocument(sqlMappingDoc);
        databaseUpdateSQLTextPane.setDocument(sqlUpdateMappingDoc);
        databaseSQLTextPane.setText("SELECT FROM");
        generateConnection.setEnabled(false);
        dbVarList.setEnabled(true);
        updateSQL();
    }//GEN-LAST:event_useJavaScriptNoActionPerformed

    private void useJavaScriptYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_useJavaScriptYesActionPerformed
    {//GEN-HEADEREND:event_useJavaScriptYesActionPerformed
        sqlLabel.setText("JavaScript:");
        onUpdateLabel.setText("On-Update JavaScript:");
        databaseSQLTextPane.setDocument(jsMappingDoc);
        databaseUpdateSQLTextPane.setDocument(jsUpdateMappingDoc);
        generateConnection.setEnabled(true);
        dbVarList.setEnabled(false);
        updateSQL();
    }//GEN-LAST:event_useJavaScriptYesActionPerformed

    private void readOnUpdateNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_readOnUpdateNoActionPerformed
    {//GEN-HEADEREND:event_readOnUpdateNoActionPerformed
        onUpdateLabel.setEnabled(false);
        databaseUpdateSQLTextPane.setEnabled(false);
    }//GEN-LAST:event_readOnUpdateNoActionPerformed

    private void readOnUpdateYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_readOnUpdateYesActionPerformed
    {//GEN-HEADEREND:event_readOnUpdateYesActionPerformed
        onUpdateLabel.setEnabled(true);
        databaseUpdateSQLTextPane.setEnabled(true);
    }//GEN-LAST:event_readOnUpdateYesActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private com.webreach.mirth.client.ui.components.MirthComboBox databaseDriverCombobox;
    private com.webreach.mirth.client.ui.components.MirthPasswordField databasePasswordField;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea databaseSQLTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField databaseURLField;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea databaseUpdateSQLTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField databaseUsernameField;
    private com.webreach.mirth.client.ui.components.MirthVariableList dbVarList;
    private javax.swing.JButton generateConnection;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel onUpdateLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField pollingFreq;
    private com.webreach.mirth.client.ui.components.MirthRadioButton readOnUpdateNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton readOnUpdateYes;
    private javax.swing.JLabel sqlLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton useJavaScriptNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton useJavaScriptYes;
    // End of variables declaration//GEN-END:variables

}
