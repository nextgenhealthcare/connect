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

package com.webreach.mirth.connectors.jdbc;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;
import org.syntax.jedit.tokenmarker.TSQLTokenMarker;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.DriverInfo;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class DatabaseWriter extends ConnectorClass
{
    /** Creates new form DatabaseWriter */

    private static SyntaxDocument sqlMappingDoc;

    private static SyntaxDocument jsMappingDoc;

    private List<DriverInfo> drivers;

    public DatabaseWriter()
    {
        name = DatabaseWriterProperties.name;

        try
        {
            drivers = this.parent.mirthClient.getDatabaseDrivers();
        }
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }

        initComponents();
        
        drivers.add(0, new DriverInfo("Please Select One", "Please Select One"));
        String[] driverNames = new String[drivers.size()];
        
        for (int i = 0; i < drivers.size(); i++)
        {
            driverNames[i] = drivers.get(i).getName();
        }
        
        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(driverNames));

        sqlMappingDoc = new SyntaxDocument();
        sqlMappingDoc.setTokenMarker(new TSQLTokenMarker());
        jsMappingDoc = new SyntaxDocument();
        jsMappingDoc.setTokenMarker(new JavaScriptTokenMarker());
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DatabaseWriterProperties.DATATYPE, name);
        properties.put(DatabaseWriterProperties.DATABASE_HOST, DatabaseWriterProperties.DATABASE_HOST_VALUE);

        for (int i = 0; i < drivers.size(); i++)
        {
            DriverInfo driver = drivers.get(i);
            if (driver.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem())))
                properties.put(DatabaseWriterProperties.DATABASE_DRIVER, driver.getClassName());
        }

        properties.put(DatabaseWriterProperties.DATABASE_URL, databaseURLField.getText());
        properties.put(DatabaseWriterProperties.DATABASE_USERNAME, databaseUsernameField.getText());
        properties.put(DatabaseWriterProperties.DATABASE_PASSWORD, new String(databasePasswordField.getPassword()));

        if (useJavaScriptYes.isSelected())
        {
            properties.put(DatabaseWriterProperties.DATABASE_USE_JS, UIConstants.YES_OPTION);
            properties.put(DatabaseWriterProperties.DATABASE_JS_SQL_STATEMENT, databaseSQLTextPane.getText());
            properties.put(DatabaseWriterProperties.DATABASE_SQL_STATEMENT, "");
        }
        else
        {
            properties.put(DatabaseWriterProperties.DATABASE_USE_JS, UIConstants.NO_OPTION);
            properties.put(DatabaseWriterProperties.DATABASE_SQL_STATEMENT, databaseSQLTextPane.getText());
            properties.put(DatabaseWriterProperties.DATABASE_JS_SQL_STATEMENT, "");
        }

        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();
                
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        for (int i = 0; i < drivers.size(); i++)
        {
            DriverInfo driver = drivers.get(i);
            if (driver.getClassName().equalsIgnoreCase(((String) props.get(DatabaseWriterProperties.DATABASE_DRIVER))))
                databaseDriverCombobox.setSelectedItem(driver.getName());
        }

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
        databaseURLField.setText((String) props.get(DatabaseWriterProperties.DATABASE_URL));
        databaseUsernameField.setText((String) props.get(DatabaseWriterProperties.DATABASE_USERNAME));
        databasePasswordField.setText((String) props.get(DatabaseWriterProperties.DATABASE_PASSWORD));
        if (((String) props.get(DatabaseWriterProperties.DATABASE_USE_JS)).equals(UIConstants.YES_OPTION))
        {
            useJavaScriptYes.setSelected(true);
            useJavaScriptYesActionPerformed(null);
            databaseSQLTextPane.setText((String) props.get(DatabaseWriterProperties.DATABASE_JS_SQL_STATEMENT));
        }
        else
        {
            useJavaScriptNo.setSelected(true);
            useJavaScriptNoActionPerformed(null);
            databaseSQLTextPane.setText((String) props.get(DatabaseWriterProperties.DATABASE_SQL_STATEMENT));
        }

    }

    public Properties getDefaults()
    {
        return new DatabaseWriterProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.get(DatabaseWriterProperties.DATABASE_URL)).length() == 0)
        {
            valid = false;
            if (highlight)
            	databaseURLField.setBackground(UIConstants.INVALID_COLOR);
        }
        if ((((String) props.get(DatabaseWriterProperties.DATABASE_SQL_STATEMENT)).length() == 0) && (((String) props.get(DatabaseWriterProperties.DATABASE_JS_SQL_STATEMENT)).length() == 0))
        {
            valid = false;
            if (highlight)
            	databaseSQLTextPane.setBackground(UIConstants.INVALID_COLOR);
        }
        if ((((String) props.get(DatabaseWriterProperties.DATABASE_DRIVER)).equals("Please Select One")))
        {
            valid = false;
            if (highlight)
            	databaseDriverCombobox.setBackground(UIConstants.INVALID_COLOR);
        }
        
        return valid;
    }
    
    public String[] getDragAndDropCharacters(Properties props)
    {
        if(((String)props.get(DatabaseWriterProperties.DATABASE_USE_JS)).equals(UIConstants.YES_OPTION))
            return new String[]{"$('", "')"};
        else
            return new String[]{"${", "}"};
    }
    
    private void resetInvalidProperties()
    {
        databaseURLField.setBackground(null);
        databaseSQLTextPane.setBackground(null);
        databaseDriverCombobox.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
    }
    
    public String doValidate(Properties props, boolean highlight)
    {
    	String error = null;
    	
    	if (!checkProperties(props, highlight))
    		error = "Error in the form for connector \"" + getName() + "\".\n\n";
    	
    	String script = ((String) props.get(DatabaseWriterProperties.DATABASE_JS_SQL_STATEMENT));
    	
    	if (script.length() != 0)
    	{
	    	Context context = Context.enter();
	        try
	        {
	            context.compileString("function rhinoWrapper() {" + script + "\n}", UUID.randomUUID().toString(), 1, null);
	        }
	        catch (EvaluatorException e)
	        {
	        	if (error == null)
	        		error = "";
	            error += "Error in connector \"" + getName() + "\" at Javascript:\nError on line " + e.lineNumber() + ": " + e.getMessage() + ".\n\n";
	        }
	        catch (Exception e)
	        {
	        	if (error == null)
	        		error = "";
	        	error += "Error in connector \"" + getName() + "\" at Javascript:\nUnknown error occurred during validation.";
	        }
	        
	        Context.exit();
    	}
	        
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        sqlLabel = new javax.swing.JLabel();
        databaseDriverCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        databaseURLField = new com.webreach.mirth.client.ui.components.MirthTextField();
        databaseUsernameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        databasePasswordField = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        databaseSQLTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(true,false);
        useJavaScriptYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        useJavaScriptNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel6 = new javax.swing.JLabel();
        generateConnection = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel1.setText("Driver:");

        jLabel2.setText("URL:");

        jLabel3.setText("Username:");

        jLabel4.setText("Password:");

        sqlLabel.setText("SQL:");

        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sun JDBC-ODBC Bridge", "ODBC - MySQL", "ODBC - PostgreSQL", "ODBC - SQL Server/Sybase", "ODBC - Oracle 10g Release 2" }));

        databasePasswordField.setFont(new java.awt.Font("Tahoma", 0, 11));

        databaseSQLTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        useJavaScriptYes.setBackground(new java.awt.Color(255, 255, 255));
        useJavaScriptYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(useJavaScriptYes);
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
        buttonGroup1.add(useJavaScriptNo);
        useJavaScriptNo.setText("No");
        useJavaScriptNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJavaScriptNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                useJavaScriptNoActionPerformed(evt);
            }
        });

        jLabel6.setText("Use JavaScript:");

        generateConnection.setText("Generate Connection");
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
                    .add(jLabel2)
                    .add(jLabel1)
                    .add(jLabel3)
                    .add(jLabel4)
                    .add(jLabel6)
                    .add(sqlLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(databaseURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(databaseDriverCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(databaseUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(useJavaScriptYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(useJavaScriptNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(databasePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 46, Short.MAX_VALUE)
                        .add(generateConnection)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(databaseSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel1)
                            .add(databaseDriverCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel2)
                            .add(databaseURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel3)
                            .add(databaseUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel4)
                            .add(databasePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(useJavaScriptYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(useJavaScriptNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel6)))
                    .add(generateConnection))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sqlLabel)
                    .add(databaseSQLTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private String generateConnectionString()
    {
        String driver = "";

        for (int i = 0; i < drivers.size(); i++)
        {
            DriverInfo driverInfo = drivers.get(i);
            if (driverInfo.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem())))
                driver = driverInfo.getClassName();
        }
        
        StringBuilder connectionString = new StringBuilder();
        connectionString.append("var dbConn = DatabaseConnectionFactory.createDatabaseConnection('");
        connectionString.append(driver + "','" + databaseURLField.getText() + "','");
        connectionString.append(databaseUsernameField.getText() + "','" +  new String(databasePasswordField.getPassword()) + "\');\n");
        
        connectionString.append("var result = dbConn.executeUpdate(\"");
        connectionString.append("expression");
        connectionString.append("\");\n\n// YOUR CODE GOES HERE \n\ndbConn.close();");
        return connectionString.toString();
    }
    
    private void generateConnectionActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_generateConnectionActionPerformed
    {// GEN-HEADEREND:event_generateConnectionActionPerformed
        databaseSQLTextPane.setText(generateConnectionString() +"\n\n" + databaseSQLTextPane.getText());

        parent.enableSave();
    }// GEN-LAST:event_generateConnectionActionPerformed
    
    private void useJavaScriptYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_useJavaScriptYesActionPerformed
    {// GEN-HEADEREND:event_useJavaScriptYesActionPerformed
        sqlLabel.setText("JavaScript:");
        databaseSQLTextPane.setDocument(jsMappingDoc);
        databaseSQLTextPane.setText(generateConnectionString());
        generateConnection.setEnabled(true);
        parent.channelEditPanel.destinationVariableList.setPrefixAndSuffix("$('", "')");
    }// GEN-LAST:event_useJavaScriptYesActionPerformed

    private void useJavaScriptNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_useJavaScriptNoActionPerformed
    {// GEN-HEADEREND:event_useJavaScriptNoActionPerformed
        sqlLabel.setText("SQL:");
        databaseSQLTextPane.setDocument(sqlMappingDoc);
        databaseSQLTextPane.setText("INSERT INTO");
        generateConnection.setEnabled(false);
        parent.channelEditPanel.destinationVariableList.setPrefixAndSuffix("${", "}");
    }// GEN-LAST:event_useJavaScriptNoActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private com.webreach.mirth.client.ui.components.MirthComboBox databaseDriverCombobox;
    private com.webreach.mirth.client.ui.components.MirthPasswordField databasePasswordField;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea databaseSQLTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField databaseURLField;
    private com.webreach.mirth.client.ui.components.MirthTextField databaseUsernameField;
    private javax.swing.JButton generateConnection;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel sqlLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton useJavaScriptNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton useJavaScriptYes;
    // End of variables declaration//GEN-END:variables

}
