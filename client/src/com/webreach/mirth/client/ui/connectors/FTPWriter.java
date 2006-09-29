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
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import java.util.Properties;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;

/**
 * A form that extends from ConnectorClass.  All methods implemented
 * are described in ConnectorClass.
 */
public class FTPWriter extends ConnectorClass
{
    Frame parent;

    /** Creates new form FTPReader */
    public final String DATATYPE = "DataType";
    public final String FTP_URL = "url";
    public final String FTP_ANONYMOUS = "FTPAnonymous";
    public final String FTP_USERNAME = "username";
    public final String FTP_PASSWORD = "password";
    public final String FTP_POLLING_FREQUENCY = "pollingFrequency";
    public final String FTP_FILENAME_PARSER = "filenameParser ";
    public final String FTP_OUTPUT_PATTERN = "outputPattern";
    public final String FTP_PASSIVE_MODE = "passive";
    public final String FTP_FILE_TYPE = "binary";
    public final String FTP_VALIDATE_CONNECTION = "validateConnections";

    public FTPWriter()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "FTP Writer";
        initComponents();
        pollingFrequencyField.setDocument(new MirthFieldConstraints(0, false, true));
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FTP_URL, FTPURLField.getText());

        if (anonymousYes.isSelected())
            properties.put(FTP_ANONYMOUS, UIConstants.YES_OPTION);
        else
            properties.put(FTP_ANONYMOUS, UIConstants.NO_OPTION);
        
        properties.put(FTP_USERNAME, FTPUsernameField.getText());
        properties.put(FTP_PASSWORD, new String(FTPPasswordField.getPassword()));
        properties.put(FTP_POLLING_FREQUENCY, pollingFrequencyField.getText());
        properties.put(FTP_FILENAME_PARSER, filenameParserField.getText());
        properties.put(FTP_OUTPUT_PATTERN, outputPatternField.getText());

        if (passiveModeYes.isSelected())
            properties.put(FTP_PASSIVE_MODE, UIConstants.YES_OPTION);
        else
            properties.put(FTP_PASSIVE_MODE, UIConstants.NO_OPTION);

        if (fileTypeBinary.isSelected())
            properties.put(FTP_FILE_TYPE, UIConstants.YES_OPTION);
        else
            properties.put(FTP_FILE_TYPE, UIConstants.NO_OPTION);
        
        if (validateConnectionYes.isSelected())
            properties.put(FTP_VALIDATE_CONNECTION, UIConstants.YES_OPTION);
        else
            properties.put(FTP_VALIDATE_CONNECTION, UIConstants.NO_OPTION);

        return properties;
    }

    public void setProperties(Properties props)
    {
        FTPURLField.setText((String)props.get(FTP_URL));
        
        if(((String)props.get(FTP_ANONYMOUS)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            anonymousYes.setSelected(true);
            anonymousYesActionPerformed(null);
        }
        else
        {
            anonymousNo.setSelected(true);
            anonymousNoActionPerformed(null);
        }
        
        FTPUsernameField.setText((String)props.get(FTP_USERNAME));
        FTPPasswordField.setText((String)props.get(FTP_PASSWORD));
        pollingFrequencyField.setText((String)props.get(FTP_POLLING_FREQUENCY));
        filenameParserField.setText((String)props.get(FTP_FILENAME_PARSER));
        outputPatternField.setText((String)props.get(FTP_OUTPUT_PATTERN));
        
        if(((String)props.get(FTP_PASSIVE_MODE)).equalsIgnoreCase(UIConstants.YES_OPTION))
            passiveModeYes.setSelected(true);
        else
            passiveModeNo.setSelected(true);
        
        if(((String)props.get(FTP_FILE_TYPE)).equalsIgnoreCase(UIConstants.YES_OPTION))
            fileTypeBinary.setSelected(true);
        else
            fileTypeASCII.setSelected(true);
        
        if(((String)props.get(FTP_VALIDATE_CONNECTION)).equalsIgnoreCase(UIConstants.YES_OPTION))
            validateConnectionYes.setSelected(true);
        else
            validateConnectionNo.setSelected(true);
    }
    
    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FTP_URL, "");
        properties.put(FTP_ANONYMOUS, UIConstants.YES_OPTION);
        properties.put(FTP_USERNAME, "");
        properties.put(FTP_PASSWORD, "");
        properties.put(FTP_POLLING_FREQUENCY, "1000");
        properties.put(FTP_FILENAME_PARSER, "");
        properties.put(FTP_OUTPUT_PATTERN, "");
        properties.put(FTP_PASSIVE_MODE, UIConstants.YES_OPTION);
        properties.put(FTP_FILE_TYPE, UIConstants.YES_OPTION);
        properties.put(FTP_VALIDATE_CONNECTION, UIConstants.YES_OPTION);
        return properties;
    }
    
    public boolean checkProperties(Properties props)
    {
        if (((String)props.get(FTP_ANONYMOUS)).equals(UIConstants.YES_OPTION))
        {
            if(((String)props.get(FTP_URL)).length() > 0 && ((String)props.get(FTP_POLLING_FREQUENCY)).length() > 0 && 
               ((String)props.get(FTP_FILENAME_PARSER)).length() > 0 && ((String)props.get(FTP_OUTPUT_PATTERN)).length() > 0)
                return true;
        }
        else
        {
            if(((String)props.get(FTP_URL)).length() > 0 && ((String)props.get(FTP_USERNAME)).length() > 0 && 
               ((String)props.get(FTP_PASSWORD)).length() > 0 && ((String)props.get(FTP_POLLING_FREQUENCY)).length() > 0 && 
               ((String)props.get(FTP_FILENAME_PARSER)).length() > 0 && ((String)props.get(FTP_OUTPUT_PATTERN)).length() > 0)
                return true;
        }
        return false;
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
        FTPURLField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel4 = new javax.swing.JLabel();
        filenameParserField = new com.webreach.mirth.client.ui.components.MirthTextField();
        outputPatternField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        fileAgeLabel = new javax.swing.JLabel();
        passiveModeYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        passiveModeNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeBinary = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeASCII = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        mirthVariableList1 = new com.webreach.mirth.client.ui.components.MirthVariableList();
        FTPUsernameLabel = new javax.swing.JLabel();
        FTPUsernameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        FTPPasswordField = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        FTPPasswordLabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        pollingFrequencyField = new com.webreach.mirth.client.ui.components.MirthTextField();
        validateConnectionYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        validateConnectionNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        anonymousYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel10 = new javax.swing.JLabel();
        anonymousNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "FTP Writer", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        URL.setText("FTP URL:");

        jLabel4.setText("Filename Parser:");

        jLabel5.setText("Output Pattern:");

        jLabel6.setText("Passive Mode:");

        jLabel7.setText("File Type:");

        fileAgeLabel.setText("Validate Connection:");

        passiveModeYes.setBackground(new java.awt.Color(255, 255, 255));
        passiveModeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(passiveModeYes);
        passiveModeYes.setText("Yes");
        passiveModeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        passiveModeNo.setBackground(new java.awt.Color(255, 255, 255));
        passiveModeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(passiveModeNo);
        passiveModeNo.setSelected(true);
        passiveModeNo.setText("No");
        passiveModeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        fileTypeBinary.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeBinary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(fileTypeBinary);
        fileTypeBinary.setText("Binary");
        fileTypeBinary.setMargin(new java.awt.Insets(0, 0, 0, 0));

        fileTypeASCII.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeASCII.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(fileTypeASCII);
        fileTypeASCII.setSelected(true);
        fileTypeASCII.setText("ASCII");
        fileTypeASCII.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mirthVariableList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "DATE", "DATE:yy-MM-dd", "SYSTIME", "UUID", "ORIGINALNAME", "COUNT" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(mirthVariableList1);

        FTPUsernameLabel.setText("Username:");

        FTPPasswordField.setFont(new java.awt.Font("Tahoma", 0, 11));

        FTPPasswordLabel.setText("Password:");

        jLabel9.setText("Polling Frequency (ms):");

        validateConnectionYes.setBackground(new java.awt.Color(255, 255, 255));
        validateConnectionYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(validateConnectionYes);
        validateConnectionYes.setText("Yes");
        validateConnectionYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        validateConnectionNo.setBackground(new java.awt.Color(255, 255, 255));
        validateConnectionNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(validateConnectionNo);
        validateConnectionNo.setSelected(true);
        validateConnectionNo.setText("No");
        validateConnectionNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        anonymousYes.setBackground(new java.awt.Color(255, 255, 255));
        anonymousYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(anonymousYes);
        anonymousYes.setText("Yes");
        anonymousYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        anonymousYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anonymousYesActionPerformed(evt);
            }
        });

        jLabel10.setText("Anonymous:");

        anonymousNo.setBackground(new java.awt.Color(255, 255, 255));
        anonymousNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(anonymousNo);
        anonymousNo.setSelected(true);
        anonymousNo.setText("No");
        anonymousNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        anonymousNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anonymousNoActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel10)
                    .add(URL)
                    .add(FTPPasswordLabel)
                    .add(jLabel9)
                    .add(jLabel4)
                    .add(jLabel5)
                    .add(jLabel6)
                    .add(jLabel7)
                    .add(fileAgeLabel)
                    .add(FTPUsernameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(anonymousYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(anonymousNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(FTPPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(filenameParserField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(pollingFrequencyField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(outputPatternField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(passiveModeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(passiveModeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(validateConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(validateConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(FTPURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(FTPUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(58, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(URL)
                    .add(FTPURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(anonymousNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10)
                    .add(anonymousYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(FTPUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(FTPUsernameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(FTPPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(FTPPasswordLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pollingFrequencyField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 98, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(filenameParserField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(outputPatternField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel5))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel6)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(passiveModeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(passiveModeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel7)
                            .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fileAgeLabel)
                            .add(validateConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(validateConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(89, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void anonymousNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anonymousNoActionPerformed
        FTPUsernameLabel.setEnabled(true);
        FTPUsernameField.setEnabled(true);
        FTPPasswordLabel.setEnabled(true);
        FTPPasswordField.setEnabled(true);
    }//GEN-LAST:event_anonymousNoActionPerformed

    private void anonymousYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anonymousYesActionPerformed
        FTPUsernameLabel.setEnabled(false);
        FTPUsernameField.setEnabled(false);
        FTPPasswordLabel.setEnabled(false);
        FTPPasswordField.setEnabled(false);
    }//GEN-LAST:event_anonymousYesActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthPasswordField FTPPasswordField;
    private javax.swing.JLabel FTPPasswordLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField FTPURLField;
    private com.webreach.mirth.client.ui.components.MirthTextField FTPUsernameField;
    private javax.swing.JLabel FTPUsernameLabel;
    private javax.swing.JLabel URL;
    private com.webreach.mirth.client.ui.components.MirthRadioButton anonymousNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton anonymousYes;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.JLabel fileAgeLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeASCII;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeBinary;
    private com.webreach.mirth.client.ui.components.MirthTextField filenameParserField;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private com.webreach.mirth.client.ui.components.MirthVariableList mirthVariableList1;
    private com.webreach.mirth.client.ui.components.MirthTextField outputPatternField;
    private com.webreach.mirth.client.ui.components.MirthRadioButton passiveModeNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton passiveModeYes;
    private com.webreach.mirth.client.ui.components.MirthTextField pollingFrequencyField;
    private com.webreach.mirth.client.ui.components.MirthRadioButton validateConnectionNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton validateConnectionYes;
    // End of variables declaration//GEN-END:variables

}
