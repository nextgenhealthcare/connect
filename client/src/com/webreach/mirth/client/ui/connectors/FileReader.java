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
public class FileReader extends ConnectorClass
{
    Frame parent;

    /** Creates new form FileWriter */
    public final String DATATYPE = "DataType";
    public final String FILE_DIRECTORY = "host";
    public final String FILE_POLLING_FREQUENCY = "pollingFrequency";
    public final String FILE_MOVE_TO_PATTERN = "moveToPattern";
    public final String FILE_MOVE_TO_DIRECTORY = "moveToDirectory";
    public final String FILE_DELETE_AFTER_READ = "autoDelete";
    public final String FILE_CHECK_FILE_AGE = "checkFileAge";
    public final String FILE_FILE_AGE = "fileAge";
    public final String FILE_SORT_BY = "sortAttribute";
    public final String SORT_BY_NAME = "name";
    public final String SORT_BY_SIZE = "size";
    public final String SORT_BY_DATE = "date";
    //ast: encodign
    public final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    public FileReader()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "File Reader";
        initComponents();
        pollingFreq.setDocument(new MirthFieldConstraints(0, false, true));
        fileAge.setDocument(new MirthFieldConstraints(0, false, true));
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FILE_DIRECTORY, directoryField.getText().replace('\\', '/'));
        properties.put(FILE_POLLING_FREQUENCY, pollingFreq.getText());
        properties.put(FILE_MOVE_TO_PATTERN, moveToPattern.getText());
        properties.put(FILE_MOVE_TO_DIRECTORY, moveToDirectory.getText().replace('\\', '/'));

        if (deleteAfterReadYes.isSelected())
            properties.put(FILE_DELETE_AFTER_READ, UIConstants.YES_OPTION);
        else
            properties.put(FILE_DELETE_AFTER_READ, UIConstants.NO_OPTION);

        if (checkFileAgeYes.isSelected())
            properties.put(FILE_CHECK_FILE_AGE, UIConstants.YES_OPTION);
        else
            properties.put(FILE_CHECK_FILE_AGE, UIConstants.NO_OPTION);

        properties.put(FILE_FILE_AGE, fileAge.getText());
        
        if(((String)sortBy.getSelectedItem()).equals("Name"))
            properties.put(FILE_SORT_BY,SORT_BY_NAME);
        else if(((String)sortBy.getSelectedItem()).equals("Size"))
            properties.put(FILE_SORT_BY,SORT_BY_SIZE);
        else if(((String)sortBy.getSelectedItem()).equals("Date"))
            properties.put(FILE_SORT_BY,SORT_BY_DATE);
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
        pollingFreq.setText((String)props.get(FILE_POLLING_FREQUENCY));
        moveToPattern.setText((String)props.get(FILE_MOVE_TO_PATTERN));
        moveToDirectory.setText((String)props.get(FILE_MOVE_TO_DIRECTORY));
        if(((String)props.get(FILE_DELETE_AFTER_READ)).equalsIgnoreCase(UIConstants.YES_OPTION))
            deleteAfterReadYes.setSelected(true);
        else
            deleteAfterReadNo.setSelected(true);
        if(((String)props.get(FILE_CHECK_FILE_AGE)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            checkFileAgeYes.setSelected(true);
            checkFileAgeYesActionPerformed(null);
        }
        else
        {
            checkFileAgeNo.setSelected(true);
            checkFileAgeNoActionPerformed(null);
        }

        fileAge.setText((String)props.get(FILE_FILE_AGE));
        
        if(props.get(FILE_SORT_BY).equals(SORT_BY_NAME))
            sortBy.setSelectedItem("Name");
        else if(props.get(FILE_SORT_BY).equals(SORT_BY_SIZE))
            sortBy.setSelectedItem("Size");
        else if(props.get(FILE_SORT_BY).equals(SORT_BY_DATE))
            sortBy.setSelectedItem("Date");
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
    }
    
    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FILE_DIRECTORY, "");
        properties.put(FILE_POLLING_FREQUENCY, "1000");
        properties.put(FILE_MOVE_TO_PATTERN, "");
        properties.put(FILE_MOVE_TO_DIRECTORY, "");
        properties.put(FILE_DELETE_AFTER_READ, UIConstants.NO_OPTION);
        properties.put(FILE_CHECK_FILE_AGE, UIConstants.NO_OPTION);
        properties.put(FILE_FILE_AGE, "0");
        properties.put(FILE_SORT_BY, SORT_BY_DATE);
        //ast: encoding
        properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.DEFAULT_ENCODING_OPTION);
        return properties;
    }
    
    public boolean checkProperties(Properties props)
    {
        if(((String)props.get(FILE_DIRECTORY)).length() > 0 && ((String)props.get(FILE_POLLING_FREQUENCY)).length() > 0 && 
        ((String)props.get(FILE_FILE_AGE)).length() > 0)
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
        directoryField = new com.webreach.mirth.client.ui.components.MirthTextField();
        pollingFreq = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel4 = new javax.swing.JLabel();
        moveToPattern = new com.webreach.mirth.client.ui.components.MirthTextField();
        moveToDirectory = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        fileAgeLabel = new javax.swing.JLabel();
        deleteAfterReadYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        deleteAfterReadNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        checkFileAgeYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        checkFileAgeNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileAge = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel3 = new javax.swing.JLabel();
        sortBy = new com.webreach.mirth.client.ui.components.MirthComboBox();
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel41 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "File Reader", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jLabel1.setText("Directory to read:");

        jLabel2.setText("Polling Frequency (ms):");

        jLabel4.setText("Move-to File Name:");

        jLabel5.setText("Move-to Directory:");

        jLabel6.setText("Delete File After Read:");

        jLabel7.setText("Check File Age:");

        fileAgeLabel.setText("File Age (ms):");

        deleteAfterReadYes.setBackground(new java.awt.Color(255, 255, 255));
        deleteAfterReadYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(deleteAfterReadYes);
        deleteAfterReadYes.setText("Yes");
        deleteAfterReadYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        deleteAfterReadNo.setBackground(new java.awt.Color(255, 255, 255));
        deleteAfterReadNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(deleteAfterReadNo);
        deleteAfterReadNo.setSelected(true);
        deleteAfterReadNo.setText("No");
        deleteAfterReadNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        checkFileAgeYes.setBackground(new java.awt.Color(255, 255, 255));
        checkFileAgeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(checkFileAgeYes);
        checkFileAgeYes.setText("Yes");
        checkFileAgeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkFileAgeYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFileAgeYesActionPerformed(evt);
            }
        });

        checkFileAgeNo.setBackground(new java.awt.Color(255, 255, 255));
        checkFileAgeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(checkFileAgeNo);
        checkFileAgeNo.setSelected(true);
        checkFileAgeNo.setText("No");
        checkFileAgeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkFileAgeNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFileAgeNoActionPerformed(evt);
            }
        });

        jLabel3.setText("Sort Files By:");

        sortBy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Date", "Name", "Size" }));

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        jLabel41.setText("Encoding to use:");

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "DATE", "COUNT", "UUID", "ORIGINALFILE", "SYSTIME" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setDragEnabled(true);
        jScrollPane1.setViewportView(jList1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel41)
                            .add(jLabel6)
                            .add(jLabel7)
                            .add(fileAgeLabel)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(sortBy, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(fileAge, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                        .add(deleteAfterReadYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 16, Short.MAX_VALUE)
                                        .add(deleteAfterReadNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(checkFileAgeYes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(checkFileAgeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(8, 8, 8))))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel2)
                            .add(jLabel1)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jLabel5)
                                .add(jLabel4)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, directoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, moveToPattern, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, pollingFreq, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(moveToDirectory, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(directoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel2)
                            .add(pollingFreq, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(moveToPattern, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel5)
                            .add(moveToDirectory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(25, 25, 25)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel6)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(deleteAfterReadYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(deleteAfterReadNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(checkFileAgeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(checkFileAgeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileAgeLabel)
                    .add(fileAge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(sortBy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel41))
                .addContainerGap(15, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charsetEncodingComboboxActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_charsetEncodingComboboxActionPerformed

    private void checkFileAgeNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkFileAgeNoActionPerformed
    {//GEN-HEADEREND:event_checkFileAgeNoActionPerformed
        fileAgeLabel.setEnabled(false);
        fileAge.setEnabled(false);
    }//GEN-LAST:event_checkFileAgeNoActionPerformed

    private void checkFileAgeYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkFileAgeYesActionPerformed
    {//GEN-HEADEREND:event_checkFileAgeYesActionPerformed
        fileAgeLabel.setEnabled(true);
        fileAge.setEnabled(true);
    }//GEN-LAST:event_checkFileAgeYesActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthRadioButton checkFileAgeNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton checkFileAgeYes;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deleteAfterReadNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deleteAfterReadYes;
    private com.webreach.mirth.client.ui.components.MirthTextField directoryField;
    private com.webreach.mirth.client.ui.components.MirthTextField fileAge;
    private javax.swing.JLabel fileAgeLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private com.webreach.mirth.client.ui.components.MirthTextField moveToDirectory;
    private com.webreach.mirth.client.ui.components.MirthTextField moveToPattern;
    private com.webreach.mirth.client.ui.components.MirthTextField pollingFreq;
    private com.webreach.mirth.client.ui.components.MirthComboBox sortBy;
    // End of variables declaration//GEN-END:variables

}
