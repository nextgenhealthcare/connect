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
import java.util.List;

import java.util.Properties;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.TSQLTokenMarker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A form that extends from ConnectorClass.  All methods implemented
 * are described in ConnectorClass.
 */
public class DatabaseReader extends ConnectorClass
{
    Frame parent;
    /** Creates new form DatabaseWriter */
    public final String DATATYPE = "DataType";
    public final String DATABASE_HOST = "host";
    public final String DATABASE_HOST_VALUE = "query";
    public final String DATABASE_DRIVER = "driver";
    public final String DATABASE_URL = "URL";
    public final String DATABASE_USERNAME = "username";
    public final String DATABASE_PASSWORD = "password";
    public final String DATABASE_POLLING_FREQUENCY = "pollingFrequency";
    public final String DATABASE_SQL_STATEMENT = "query";
    public final String DATABASE_USE_ACK = "useAck";
    public final String DATABASE_ACK = "ack";

    private static SyntaxDocument mappingDoc;
    private static SyntaxDocument mappingDoc2;
    private List <DriverInfo> drivers;
    
    public DatabaseReader()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
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
        mappingDoc = new SyntaxDocument();
        mappingDoc.setTokenMarker(new TSQLTokenMarker());
        mappingDoc2 = new SyntaxDocument();
        mappingDoc2.setTokenMarker(new TSQLTokenMarker());
        databaseSQLTextPane.setDocument(mappingDoc);
        databaseUpdateSQLTextPane.setDocument(mappingDoc2);
        
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
        properties.put(DATABASE_SQL_STATEMENT, databaseSQLTextPane.getText());

        if (readOnUpdateYes.isSelected())
            properties.put(DATABASE_USE_ACK, UIConstants.YES_OPTION);
        else
            properties.put(DATABASE_USE_ACK, UIConstants.NO_OPTION);

        properties.put(DATABASE_ACK, databaseUpdateSQLTextPane.getText());
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

        databaseUpdateSQLTextPane.setText((String)props.get(DATABASE_ACK));

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
        properties.put(DATABASE_USE_ACK, UIConstants.NO_OPTION);
        properties.put(DATABASE_ACK, "UPDATE");
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
            parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().setInboundTemplate(docSerializer.toXML(document));
            
            if(parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundProtocol() == MessageObject.Protocol.XML && parent.channelEditPanel.currentChannel.getSourceConnector().getTransformer().getOutboundTemplate().length() == 0)
            {
                List<Connector> list = parent.channelEditPanel.currentChannel.getDestinationConnectors();
                for(Connector c : list)
                {
                    c.getTransformer().setInboundTemplate(docSerializer.toXML(document));
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
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

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Database Reader", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jLabel1.setText("Driver:");

        jLabel2.setText("URL:");

        jLabel3.setText("Username:");

        jLabel4.setText("Password:");

        jLabel5.setText("SQL Statement:");

        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sun JDBC-ODBC Bridge", "ODBC - MySQL", "ODBC - PostgresSQL", "ODBC - SQL Server/Sybase", "ODBC - Oracle 10g Release 2" }));

        databasePasswordField.setFont(new java.awt.Font("Tahoma", 0, 11));

        onUpdateLabel.setText("On-Update Statement:");

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

        jLabel8.setText("Run On-Update SQL:");

        jScrollPane1.setViewportView(dbVarList);

        databaseSQLTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        databaseUpdateSQLTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel5)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel7)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel8)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, onUpdateLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(databasePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pollingFreq, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(databaseSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(readOnUpdateYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(readOnUpdateNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(databaseUpdateSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 95, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(databaseUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(databaseURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(databaseDriverCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(databaseSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(readOnUpdateYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(readOnUpdateNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(onUpdateLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(databaseUpdateSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

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
    private com.webreach.mirth.client.ui.components.MirthComboBox databaseDriverCombobox;
    private com.webreach.mirth.client.ui.components.MirthPasswordField databasePasswordField;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea databaseSQLTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField databaseURLField;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea databaseUpdateSQLTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField databaseUsernameField;
    private com.webreach.mirth.client.ui.components.MirthVariableList dbVarList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel onUpdateLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField pollingFreq;
    private com.webreach.mirth.client.ui.components.MirthRadioButton readOnUpdateNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton readOnUpdateYes;
    // End of variables declaration//GEN-END:variables

}
