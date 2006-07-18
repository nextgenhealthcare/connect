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

import com.Ostermiller.Syntax.HighlightedDocument;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;

import java.util.Properties;

/** 
 * A form that extends from ConnectorClass.  All methods implemented
 * are described in ConnectorClass.
 */
public class DatabaseWriter extends ConnectorClass
{
    Frame parent;
    /** Creates new form DatabaseWriter */
    public final String DATATYPE = "DataType";
    public final String DATABASE_HOST = "host";
    public final String DATABASE_HOST_VALUE = "query";
    public final String DATABASE_DRIVER = "driver";
    public final String SUN_JDBC_ODBC_BRIDGE = "Sun JDBC-ODBC Bridge";
    public final String ODBC_MYSQL = "ODBC - MySQL";
    public final String ODBC_POSTGRESQL = "ODBC - PostgreSQL";
    public final String ODBC_SQL_SERVER_SYBASE = "ODBC - SQL Server/Sybase";
    public final String ODBC_ORACLE_10G_RELEASE_2 = "ODBC - Oracle 10g Release 2";
    public final String SUN_JDBC_ODBC_JDBCODBCDRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";
    public final String COM_MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public final String ORG_POSTGRESQL_DRIVER = "org.postgresql.Driver";
    public final String NET_SOURCEFORGE_JTDS_JDBC_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
    public final String ORACLE_JDBC_ORACLEDRIVER = "oracle.jdbc.OracleDriver";
    public final String DATABASE_URL = "URL";
    public final String DATABASE_USERNAME = "username";
    public final String DATABASE_PASSWORD = "password";
    public final String DATABASE_SQL_STATEMENT = "statement";
    
    private static HighlightedDocument mappingDoc;
    
    public DatabaseWriter()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "Database Writer";
        initComponents();
        mappingDoc = new HighlightedDocument();
        mappingDoc.setHighlightStyle(HighlightedDocument.SQL_STYLE);
        databaseSQLTextPane.setDocument(mappingDoc);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(DATABASE_HOST, DATABASE_HOST_VALUE);
        
        if(((String)databaseDriverCombobox.getSelectedItem()).equals(SUN_JDBC_ODBC_BRIDGE))
            properties.put(DATABASE_DRIVER, SUN_JDBC_ODBC_JDBCODBCDRIVER);
        else if(((String)databaseDriverCombobox.getSelectedItem()).equals(ODBC_MYSQL))
            properties.put(DATABASE_DRIVER, COM_MYSQL_JDBC_DRIVER);
        else if(((String)databaseDriverCombobox.getSelectedItem()).equals(ODBC_POSTGRESQL))
            properties.put(DATABASE_DRIVER, ORG_POSTGRESQL_DRIVER);
        else if(((String)databaseDriverCombobox.getSelectedItem()).equals(ODBC_SQL_SERVER_SYBASE))
            properties.put(DATABASE_DRIVER, NET_SOURCEFORGE_JTDS_JDBC_DRIVER);
        else if(((String)databaseDriverCombobox.getSelectedItem()).equals(ODBC_ORACLE_10G_RELEASE_2))
            properties.put(DATABASE_DRIVER, ORACLE_JDBC_ORACLEDRIVER);
        
        properties.put(DATABASE_URL, databaseURLField.getText());
        properties.put(DATABASE_USERNAME, databaseUsernameField.getText());
        properties.put(DATABASE_PASSWORD, new String(databasePasswordField.getPassword()));
        properties.put(DATABASE_SQL_STATEMENT, databaseSQLTextPane.getText());
        return properties;
    }

    public void setProperties(Properties props)
    {
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
        
        if(((String)props.get(DATABASE_DRIVER)).equals(SUN_JDBC_ODBC_JDBCODBCDRIVER))
            databaseDriverCombobox.setSelectedItem(SUN_JDBC_ODBC_BRIDGE);
        else if(((String)props.get(DATABASE_DRIVER)).equals(COM_MYSQL_JDBC_DRIVER))
            databaseDriverCombobox.setSelectedItem(ODBC_MYSQL);
        else if(((String)props.get(DATABASE_DRIVER)).equals(ORG_POSTGRESQL_DRIVER))
            databaseDriverCombobox.setSelectedItem(ODBC_POSTGRESQL);
        else if(((String)props.get(DATABASE_DRIVER)).equals(NET_SOURCEFORGE_JTDS_JDBC_DRIVER))
            databaseDriverCombobox.setSelectedItem(ODBC_SQL_SERVER_SYBASE);
        else if(((String)props.get(DATABASE_DRIVER)).equals(ORACLE_JDBC_ORACLEDRIVER))
            databaseDriverCombobox.setSelectedItem(ODBC_ORACLE_10G_RELEASE_2);
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
        databaseURLField.setText((String)props.get(DATABASE_URL));
        databaseUsernameField.setText((String)props.get(DATABASE_USERNAME));
        databasePasswordField.setText((String)props.get(DATABASE_PASSWORD));
        databaseSQLTextPane.setText((String)props.get(DATABASE_SQL_STATEMENT));
    }
    
    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(DATABASE_HOST, DATABASE_HOST_VALUE);
        properties.put(DATABASE_DRIVER, COM_MYSQL_JDBC_DRIVER);
        properties.put(DATABASE_URL, "");
        properties.put(DATABASE_USERNAME, "");
        properties.put(DATABASE_PASSWORD, "");
        properties.put(DATABASE_SQL_STATEMENT, "INSERT INTO");
        return properties;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        databaseDriverCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        databaseURLField = new com.webreach.mirth.client.ui.components.MirthTextField();
        databaseUsernameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        databasePasswordField = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        jScrollPane2 = new javax.swing.JScrollPane();
        databaseSQLTextPane = new com.webreach.mirth.client.ui.components.MirthTextPane();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Database Writer", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jLabel1.setText("Driver:");

        jLabel2.setText("Database URL:");

        jLabel3.setText("Username:");

        jLabel4.setText("Password:");

        jLabel5.setText("SQL Statement:");

        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sun JDBC-ODBC Bridge", "ODBC - MySQL", "ODBC - PostgreSQL", "ODBC - SQL Server/Sybase", "ODBC - Oracle 10g Release 2" }));

        databasePasswordField.setFont(new java.awt.Font("Tahoma", 0, 11));

        jScrollPane2.setViewportView(databaseSQLTextPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(jLabel1)
                    .add(jLabel3)
                    .add(jLabel4)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(databasePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(databaseUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(databaseURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
                    .add(databaseDriverCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(databaseDriverCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel2)
                            .add(databaseURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(databaseUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3)))
                    .add(layout.createSequentialGroup()
                        .add(50, 50, 50)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(databasePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel4))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel5)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthComboBox databaseDriverCombobox;
    private com.webreach.mirth.client.ui.components.MirthPasswordField databasePasswordField;
    private com.webreach.mirth.client.ui.components.MirthTextPane databaseSQLTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField databaseURLField;
    private com.webreach.mirth.client.ui.components.MirthTextField databaseUsernameField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

}
