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

import java.util.Properties;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;

/**
 * A form that extends from ConnectorClass.  All methods implemented
 * are described in ConnectorClass.
 */
public class FileWriter extends ConnectorClass
{
    Frame parent;

    /** Creates new form FileWriter */
    public final String DATATYPE = "DataType";
    public final String FILE_DIRECTORY = "host";
    public final String FILE_NAME = "outputPattern";
    public final String FILE_APPEND = "outputAppend";
    public final String FILE_CONTENTS = "template";
    //ast: encodign
    public final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";

    public FileWriter()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "File Writer";
        initComponents();
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FILE_DIRECTORY, directoryField.getText().replace('\\', '/'));
        properties.put(FILE_NAME, fileNameField.getText());

        if (appendToFileYes.isSelected())
            properties.put(FILE_APPEND, UIConstants.YES_OPTION);
        else
            properties.put(FILE_APPEND, UIConstants.NO_OPTION);

        properties.put(FILE_CONTENTS, fileContentsTextPane.getText());
        //ast:encoding
        if( charsetEncodingCombobox.getSelectedIndex()==1)
            properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.UTF8_OPTION);
        else if (charsetEncodingCombobox.getSelectedIndex()==2)
            properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.LATIN1_OPTION);
        else if (charsetEncodingCombobox.getSelectedIndex()==3)
            properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.UTF16LE_OPTION);        
        else if (charsetEncodingCombobox.getSelectedIndex()==4)
            properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.UTF16BE_OPTION);        
        else if (charsetEncodingCombobox.getSelectedIndex()==5)
            properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.UTF16BOM_OPTION);
        else if (charsetEncodingCombobox.getSelectedIndex()==5)
            properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.USASCII_OPTION);
        else
            properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.DEFAULT_ENCODING_OPTION);
        return properties;
    }

    public void setProperties(Properties props)
    {
        directoryField.setText((String)props.get(FILE_DIRECTORY));
        fileNameField.setText((String)props.get(FILE_NAME));

        if(((String)props.get(FILE_APPEND)).equalsIgnoreCase(UIConstants.YES_OPTION))
            appendToFileYes.setSelected(true);
        else
            appendToFileNo.setSelected(true);
        
        //ast:encoding        
        String encoding=(String)props.get(CONNECTOR_CHARSET_ENCODING);        
         if(encoding.equalsIgnoreCase(UIConstants.UTF8_OPTION)){
             charsetEncodingCombobox.setSelectedIndex(1);
         }else if (encoding.equalsIgnoreCase(UIConstants.LATIN1_OPTION)){
            charsetEncodingCombobox.setSelectedIndex(2);
         }else if (encoding.equalsIgnoreCase(UIConstants.UTF16LE_OPTION)){
             charsetEncodingCombobox.setSelectedIndex(3);
        }else if (encoding.equalsIgnoreCase(UIConstants.UTF16BE_OPTION)){
             charsetEncodingCombobox.setSelectedIndex(4);
        }else if (encoding.equalsIgnoreCase(UIConstants.UTF16BOM_OPTION)){
             charsetEncodingCombobox.setSelectedIndex(5);
        }else if (encoding.equalsIgnoreCase(UIConstants.USASCII_OPTION)){              
             charsetEncodingCombobox.setSelectedIndex(6);
         }else{
             charsetEncodingCombobox.setSelectedIndex(0);
         }
        fileContentsTextPane.setText((String)props.get(FILE_CONTENTS));
    }

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FILE_DIRECTORY, "");
        properties.put(FILE_NAME, "");
        properties.put(FILE_APPEND, UIConstants.YES_OPTION);
        properties.put(FILE_CONTENTS, "");
        //ast:encoding
        properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.DEFAULT_ENCODING_OPTION);
        return properties;
    }
    
    public boolean checkProperties(Properties props)
    {
        if(((String)props.get(FILE_DIRECTORY)).length() > 0 && ((String)props.get(FILE_NAME)).length() > 0 && ((String)props.get(FILE_CONTENTS)).length() > 0)
            return true;
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        directoryField = new com.webreach.mirth.client.ui.components.MirthTextField();
        fileNameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel4 = new javax.swing.JLabel();
        appendToFileYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        appendToFileNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileContentsTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(false,false);
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel41 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "File Writer", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
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

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));

        jLabel41.setText("Encoding to use:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel4)
                    .add(jLabel2)
                    .add(jLabel1)
                    .add(jLabel41)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(appendToFileYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(appendToFileNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(fileContentsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, fileNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, directoryField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(directoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(fileNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(appendToFileYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(appendToFileNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel41))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(fileContentsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthRadioButton appendToFileNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton appendToFileYes;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthTextField directoryField;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea fileContentsTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField fileNameField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel41;
    // End of variables declaration//GEN-END:variables

}
