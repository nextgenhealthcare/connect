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

package com.webreach.mirth.connectors.file;

import java.util.Properties;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.connectors.ConnectorClass;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class FileWriter extends ConnectorClass
{
    /** Creates new form FileWriter */

    public FileWriter()
    {
        name = FileWriterProperties.name;
        initComponents();
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(FileWriterProperties.DATATYPE, name);
        properties.put(FileWriterProperties.FILE_DIRECTORY, directoryField.getText().replace('\\', '/'));
        properties.put(FileWriterProperties.FILE_NAME, fileNameField.getText());

        if (appendToFileYes.isSelected())
            properties.put(FileWriterProperties.FILE_APPEND, UIConstants.YES_OPTION);
        else
            properties.put(FileWriterProperties.FILE_APPEND, UIConstants.NO_OPTION);

        properties.put(FileWriterProperties.FILE_CONTENTS, fileContentsTextPane.getText());

        properties.put(FileWriterProperties.CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        if (fileTypeBinary.isSelected())
            properties.put(FileWriterProperties.FILE_TYPE, UIConstants.YES_OPTION);
        else
            properties.put(FileWriterProperties.FILE_TYPE, UIConstants.NO_OPTION);

        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();

        directoryField.setText((String) props.get(FileWriterProperties.FILE_DIRECTORY));
        fileNameField.setText((String) props.get(FileWriterProperties.FILE_NAME));

        if (((String) props.get(FileWriterProperties.FILE_APPEND)).equalsIgnoreCase(UIConstants.YES_OPTION))
            appendToFileYes.setSelected(true);
        else
            appendToFileNo.setSelected(true);

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(FileWriterProperties.CONNECTOR_CHARSET_ENCODING));

        fileContentsTextPane.setText((String) props.get(FileWriterProperties.FILE_CONTENTS));

        if (((String) props.get(FileWriterProperties.FILE_TYPE)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            fileTypeBinary.setSelected(true);
            fileTypeBinaryActionPerformed(null);
        }
        else
        {
            fileTypeASCII.setSelected(true);
            fileTypeASCIIActionPerformed(null);
        }
    }

    public Properties getDefaults()
    {
        return new FileWriterProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.get(FileWriterProperties.FILE_DIRECTORY)).length() == 0)
        {
            valid = false;
            if (highlight)
            	directoryField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FileWriterProperties.FILE_NAME)).length() == 0)
        {
            valid = false;
            if (highlight)
            	fileNameField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FileWriterProperties.FILE_CONTENTS)).length() == 0)
        {
            valid = false;
            if (highlight)
            	fileContentsTextPane.setBackground(UIConstants.INVALID_COLOR);
        }

        return valid;
    }

    private void resetInvalidProperties()
    {
        directoryField.setBackground(null);
        fileNameField.setBackground(null);
        fileContentsTextPane.setBackground(null);
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        directoryField = new com.webreach.mirth.client.ui.components.MirthTextField();
        fileNameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel4 = new javax.swing.JLabel();
        appendToFileYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        appendToFileNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileContentsTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(false, false);
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        fileTypeBinary = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeASCII = new com.webreach.mirth.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel1.setText("Directory:");

        jLabel2.setText("File Name:");

        jLabel3.setText("Template:");

        jLabel4.setText("Append to file:");

        appendToFileYes.setBackground(new java.awt.Color(255, 255, 255));
        appendToFileYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(appendToFileYes);
        appendToFileYes.setText("Yes");
        appendToFileYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        appendToFileNo.setBackground(new java.awt.Color(255, 255, 255));
        appendToFileNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(appendToFileNo);
        appendToFileNo.setSelected(true);
        appendToFileNo.setText("No");
        appendToFileNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        fileContentsTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));

        encodingLabel.setText("Encoding:");

        jLabel5.setText("File Type:");

        fileTypeBinary.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeBinary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(fileTypeBinary);
        fileTypeBinary.setText("Binary");
        fileTypeBinary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileTypeBinary.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                fileTypeBinaryActionPerformed(evt);
            }
        });

        fileTypeASCII.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeASCII.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(fileTypeASCII);
        fileTypeASCII.setSelected(true);
        fileTypeASCII.setText("ASCII");
        fileTypeASCII.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileTypeASCII.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                fileTypeASCIIActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(jLabel3).add(jLabel4).add(jLabel2).add(jLabel1).add(jLabel5).add(encodingLabel)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(org.jdesktop.layout.GroupLayout.LEADING, charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup().add(appendToFileYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(appendToFileNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(org.jdesktop.layout.GroupLayout.LEADING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(org.jdesktop.layout.GroupLayout.LEADING, fileNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(org.jdesktop.layout.GroupLayout.LEADING, directoryField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)).add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup().add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(fileContentsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel1).add(directoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel2).add(fileNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(jLabel4).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel5).add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(appendToFileYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(appendToFileNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(encodingLabel)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(org.jdesktop.layout.GroupLayout.LEADING, jLabel3).add(fileContentsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)).addContainerGap()));
    }// </editor-fold>//GEN-END:initComponents

    private void fileTypeASCIIActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_fileTypeASCIIActionPerformed
    {// GEN-HEADEREND:event_fileTypeASCIIActionPerformed
        encodingLabel.setEnabled(true);
        charsetEncodingCombobox.setEnabled(true);
    }// GEN-LAST:event_fileTypeASCIIActionPerformed

    private void fileTypeBinaryActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_fileTypeBinaryActionPerformed
    {// GEN-HEADEREND:event_fileTypeBinaryActionPerformed
        encodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
    }// GEN-LAST:event_fileTypeBinaryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthRadioButton appendToFileNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton appendToFileYes;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthTextField directoryField;
    private javax.swing.JLabel encodingLabel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea fileContentsTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField fileNameField;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeASCII;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeBinary;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    // End of variables declaration//GEN-END:variables

}
