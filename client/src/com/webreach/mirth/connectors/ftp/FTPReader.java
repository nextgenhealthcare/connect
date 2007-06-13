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

package com.webreach.mirth.connectors.ftp;

import com.webreach.mirth.connectors.ConnectorClass;
import java.awt.Color;
import java.util.Properties;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class FTPReader extends ConnectorClass
{
    /** Creates new form FTPReader */
    private final String DATATYPE = "DataType";

    private final String FTP_HOST = "host";

    private final String FTP_ANONYMOUS = "FTPAnonymous";

    private final String FTP_USERNAME = "username";

    private final String FTP_PASSWORD = "password";

    private final String FTP_POLLING_FREQUENCY = "pollingFrequency";

    private final String FTP_PASSIVE_MODE = "passive";

    private final String FTP_VALIDATE_CONNECTION = "validateConnections";
    
    private final String FILE_MOVE_TO_PATTERN = "moveToPattern";

    private final String FILE_MOVE_TO_DIRECTORY = "moveToDirectory";

    private final String FILE_DELETE_AFTER_READ = "autoDelete";

    private final String FILE_CHECK_FILE_AGE = "checkFileAge";

    private final String FILE_FILE_AGE = "fileAge";

    private final String FILE_SORT_BY = "sortAttribute";

    private final String FILE_PROCESS_BATCH_FILES = "processBatchFiles";

    private final String SORT_BY_NAME = "name";

    private final String SORT_BY_SIZE = "size";

    private final String SORT_BY_DATE = "date";

    private final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";

    private final String FILE_FILTER = "fileFilter";

    private final String FILE_TYPE = "binary";

    public FTPReader()
    {
        name = "FTP Reader";
        initComponents();
        pollingFrequencyField.setDocument(new MirthFieldConstraints(0, false, false, true));
        fileAge.setDocument(new MirthFieldConstraints(0, false, false, true));
        // ast:encoding activation
        parent.setupCharsetEncodingForChannel(charsetEncodingCombobox);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FTP_HOST, FTPURLField.getText());
        if (anonymousYes.isSelected())
            properties.put(FTP_ANONYMOUS, UIConstants.YES_OPTION);
        else
            properties.put(FTP_ANONYMOUS, UIConstants.NO_OPTION);

        properties.put(FTP_USERNAME, FTPUsernameField.getText());
        properties.put(FTP_PASSWORD, new String(FTPPasswordField.getPassword()));
        properties.put(FTP_POLLING_FREQUENCY, pollingFrequencyField.getText());

        if (passiveModeYes.isSelected())
            properties.put(FTP_PASSIVE_MODE, UIConstants.YES_OPTION);
        else
            properties.put(FTP_PASSIVE_MODE, UIConstants.NO_OPTION);
        
        if (validateConnectionYes.isSelected())
            properties.put(FTP_VALIDATE_CONNECTION, UIConstants.YES_OPTION);
        else
            properties.put(FTP_VALIDATE_CONNECTION, UIConstants.NO_OPTION);
        
        //common file properties
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

        if (((String) sortBy.getSelectedItem()).equals("Name"))
            properties.put(FILE_SORT_BY, SORT_BY_NAME);
        else if (((String) sortBy.getSelectedItem()).equals("Size"))
            properties.put(FILE_SORT_BY, SORT_BY_SIZE);
        else if (((String) sortBy.getSelectedItem()).equals("Date"))
            properties.put(FILE_SORT_BY, SORT_BY_DATE);
        // ast:encoding
        properties.put(CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForChannel(charsetEncodingCombobox));
        properties.put(FILE_FILTER, fileNameFilter.getText());

        if (processBatchFilesYes.isSelected())
            properties.put(FILE_PROCESS_BATCH_FILES, UIConstants.YES_OPTION);
        else
            properties.put(FILE_PROCESS_BATCH_FILES, UIConstants.NO_OPTION);

        if (fileTypeBinary.isSelected())
            properties.put(FILE_TYPE, UIConstants.YES_OPTION);
        else
            properties.put(FILE_TYPE, UIConstants.NO_OPTION);
       
        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        
        FTPURLField.setText((String) props.get(FTP_HOST));

        if (((String) props.get(FTP_ANONYMOUS)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            anonymousYes.setSelected(true);
            anonymousYesActionPerformed(null);
        }
        else
        {
            anonymousNo.setSelected(true);
            anonymousNoActionPerformed(null);
            FTPUsernameField.setText((String) props.get(FTP_USERNAME));
            FTPPasswordField.setText((String) props.get(FTP_PASSWORD));
        }

        pollingFrequencyField.setText((String) props.get(FTP_POLLING_FREQUENCY));

        if (((String) props.get(FTP_PASSIVE_MODE)).equalsIgnoreCase(UIConstants.YES_OPTION))
            passiveModeYes.setSelected(true);
        else
            passiveModeNo.setSelected(true);

        if (((String) props.get(FTP_VALIDATE_CONNECTION)).equalsIgnoreCase(UIConstants.YES_OPTION))
            validateConnectionYes.setSelected(true);
        else
            validateConnectionNo.setSelected(true);
        
        //common file properties
        moveToPattern.setText((String) props.get(FILE_MOVE_TO_PATTERN));
        moveToDirectory.setText((String) props.get(FILE_MOVE_TO_DIRECTORY));
        if (((String) props.get(FILE_DELETE_AFTER_READ)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            deleteAfterReadYes.setSelected(true);
            deleteAfterReadYesActionPerformed(null);
        }
        else
        {
            deleteAfterReadNo.setSelected(true);
            deleteAfterReadNoActionPerformed(null);
        }
        if (((String) props.get(FILE_CHECK_FILE_AGE)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            checkFileAgeYes.setSelected(true);
            checkFileAgeYesActionPerformed(null);
        }
        else
        {
            checkFileAgeNo.setSelected(true);
            checkFileAgeNoActionPerformed(null);
        }

        fileAge.setText((String) props.get(FILE_FILE_AGE));

        if (props.get(FILE_SORT_BY).equals(SORT_BY_NAME))
            sortBy.setSelectedItem("Name");
        else if (props.get(FILE_SORT_BY).equals(SORT_BY_SIZE))
            sortBy.setSelectedItem("Size");
        else if (props.get(FILE_SORT_BY).equals(SORT_BY_DATE))
            sortBy.setSelectedItem("Date");
        // ast:encoding
        parent.sePreviousSelectedEncodingForChannel(charsetEncodingCombobox, (String) props.get(CONNECTOR_CHARSET_ENCODING));
        fileNameFilter.setText((String) props.get(FILE_FILTER));

        if (((String) props.get(FILE_PROCESS_BATCH_FILES)).equalsIgnoreCase(UIConstants.YES_OPTION))
            processBatchFilesYes.setSelected(true);
        else
            processBatchFilesNo.setSelected(true);

        if (((String) props.get(FILE_TYPE)).equalsIgnoreCase(UIConstants.YES_OPTION))
            fileTypeBinary.setSelected(true);
        else
            fileTypeASCII.setSelected(true);
    }

    public Properties getDefaults()
    {
        return new FTPReaderProperties().getDefaults();
    }

    public boolean checkProperties(Properties props)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.get(FTP_HOST)).length() == 0)
        {
            valid = false;
            FTPURLField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FILE_FILTER)).length() == 0)
        {
            valid = false;
            fileNameFilter.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FTP_POLLING_FREQUENCY)).length() == 0)
        {
            valid = false;
            pollingFrequencyField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FTP_ANONYMOUS)).equals(UIConstants.NO_OPTION))
        {
            if (((String) props.get(FTP_USERNAME)).length() == 0)
            {
                valid = false;
                FTPUsernameField.setBackground(UIConstants.INVALID_COLOR);
            }
            if (((String) props.get(FTP_PASSWORD)).length() == 0)
            {
                valid = false;
                FTPPasswordField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(FILE_CHECK_FILE_AGE)).equals(UIConstants.YES_OPTION))
        {
            if (((String) props.get(FILE_FILE_AGE)).length() == 0)
            {
                valid = false;
                fileAge.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        FTPURLField.setBackground(null);
        fileNameFilter.setBackground(null);
        pollingFrequencyField.setBackground(null);
        FTPUsernameField.setBackground(null);
        FTPPasswordField.setBackground(null);
        fileAge.setBackground(null);
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
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        URL = new javax.swing.JLabel();
        FTPURLField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        fileAgeLabel = new javax.swing.JLabel();
        passiveModeYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        passiveModeNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeBinary = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeASCII = new com.webreach.mirth.client.ui.components.MirthRadioButton();
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
        jLabel8 = new javax.swing.JLabel();
        fileNameFilter = new com.webreach.mirth.client.ui.components.MirthTextField();
        moveToDirectoryLabel = new javax.swing.JLabel();
        moveToFileLabel = new javax.swing.JLabel();
        moveToPattern = new com.webreach.mirth.client.ui.components.MirthTextField();
        moveToDirectory = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel11 = new javax.swing.JLabel();
        deleteAfterReadYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        deleteAfterReadNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        checkFileAgeNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        checkFileAgeYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel12 = new javax.swing.JLabel();
        fileAgeLabel1 = new javax.swing.JLabel();
        fileAge = new com.webreach.mirth.client.ui.components.MirthTextField();
        processBatchFilesNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        processBatchFilesYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        sortBy = new com.webreach.mirth.client.ui.components.MirthComboBox();
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel41 = new javax.swing.JLabel();
        mirthVariableList1 = new com.webreach.mirth.client.ui.components.MirthVariableList();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setMinimumSize(new java.awt.Dimension(200, 200));
        URL.setText("Host:");

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
        anonymousYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
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
        anonymousNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                anonymousNoActionPerformed(evt);
            }
        });

        jLabel8.setText("Filename Filter Pattern:");

        moveToDirectoryLabel.setText("Move-to Directory:");

        moveToFileLabel.setText("Move-to File Name:");

        jLabel11.setText("Delete File After Read:");

        deleteAfterReadYes.setBackground(new java.awt.Color(255, 255, 255));
        deleteAfterReadYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(deleteAfterReadYes);
        deleteAfterReadYes.setText("Yes");
        deleteAfterReadYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deleteAfterReadYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteAfterReadYesActionPerformed(evt);
            }
        });

        deleteAfterReadNo.setBackground(new java.awt.Color(255, 255, 255));
        deleteAfterReadNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(deleteAfterReadNo);
        deleteAfterReadNo.setSelected(true);
        deleteAfterReadNo.setText("No");
        deleteAfterReadNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deleteAfterReadNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteAfterReadNoActionPerformed(evt);
            }
        });

        checkFileAgeNo.setBackground(new java.awt.Color(255, 255, 255));
        checkFileAgeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(checkFileAgeNo);
        checkFileAgeNo.setSelected(true);
        checkFileAgeNo.setText("No");
        checkFileAgeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkFileAgeNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                checkFileAgeNoActionPerformed(evt);
            }
        });

        checkFileAgeYes.setBackground(new java.awt.Color(255, 255, 255));
        checkFileAgeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(checkFileAgeYes);
        checkFileAgeYes.setText("Yes");
        checkFileAgeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkFileAgeYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                checkFileAgeYesActionPerformed(evt);
            }
        });

        jLabel12.setText("Check File Age:");

        fileAgeLabel1.setText("File Age (ms):");

        processBatchFilesNo.setBackground(new java.awt.Color(255, 255, 255));
        processBatchFilesNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(processBatchFilesNo);
        processBatchFilesNo.setSelected(true);
        processBatchFilesNo.setText("No");
        processBatchFilesNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        processBatchFilesNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                processBatchFilesNoActionPerformed(evt);
            }
        });

        processBatchFilesYes.setBackground(new java.awt.Color(255, 255, 255));
        processBatchFilesYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(processBatchFilesYes);
        processBatchFilesYes.setText("Yes");
        processBatchFilesYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel14.setText("Process Batch Files:");

        jLabel3.setText("Sort Files By:");

        sortBy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Date", "Name", "Size" }));

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingCombobox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        jLabel41.setText("Encoding:");

        mirthVariableList1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        mirthVariableList1.setModel(new javax.swing.AbstractListModel()
        {
            String[] strings = { "DATE", "COUNT", "UUID", "SYSTIME", "ORIGINALNAME" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(23, 23, 23)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel6)
                            .add(FTPPasswordLabel)
                            .add(FTPUsernameLabel)
                            .add(jLabel10)
                            .add(URL)
                            .add(fileAgeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(passiveModeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(passiveModeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(validateConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(validateConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(FTPPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(FTPUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(anonymousYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(anonymousNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(FTPURLField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fileNameFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel12)
                            .add(jLabel11)
                            .add(moveToDirectoryLabel)
                            .add(jLabel9)
                            .add(moveToFileLabel)
                            .add(jLabel7)
                            .add(jLabel41)
                            .add(jLabel3)
                            .add(fileAgeLabel1)
                            .add(jLabel14))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(deleteAfterReadYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(deleteAfterReadNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(moveToPattern, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(moveToDirectory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(checkFileAgeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(checkFileAgeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(5, 5, 5)
                                .add(mirthVariableList1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(pollingFrequencyField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fileAge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(sortBy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(processBatchFilesYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(processBatchFilesNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
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
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fileAgeLabel)
                            .add(validateConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(validateConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel6)
                            .add(passiveModeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(passiveModeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel8)
                            .add(fileNameFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel9)
                            .add(pollingFrequencyField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(moveToDirectoryLabel)
                            .add(moveToDirectory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(moveToFileLabel)
                            .add(moveToPattern, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel11)
                            .add(deleteAfterReadYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(deleteAfterReadNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel12)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(checkFileAgeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(checkFileAgeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fileAge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fileAgeLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel7))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel14)
                            .add(processBatchFilesYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(processBatchFilesNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(sortBy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel41)))
                    .add(layout.createSequentialGroup()
                        .add(187, 187, 187)
                        .add(mirthVariableList1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void deleteAfterReadYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteAfterReadYesActionPerformed
    {//GEN-HEADEREND:event_deleteAfterReadYesActionPerformed
        moveToDirectory.setEnabled(false);
        moveToPattern.setEnabled(false);
        
        moveToDirectoryLabel.setEnabled(false);
        moveToFileLabel.setEnabled(false);
        
        moveToDirectory.setText("");
        moveToPattern.setText("");
    }//GEN-LAST:event_deleteAfterReadYesActionPerformed

    private void deleteAfterReadNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteAfterReadNoActionPerformed
    {//GEN-HEADEREND:event_deleteAfterReadNoActionPerformed
        moveToDirectory.setEnabled(true);
        moveToPattern.setEnabled(true);
        
        moveToDirectoryLabel.setEnabled(true);
        moveToFileLabel.setEnabled(true);
    }//GEN-LAST:event_deleteAfterReadNoActionPerformed

    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charsetEncodingComboboxActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_charsetEncodingComboboxActionPerformed

    private void processBatchFilesNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processBatchFilesNoActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_processBatchFilesNoActionPerformed

    private void checkFileAgeYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFileAgeYesActionPerformed
        fileAgeLabel.setEnabled(true);
        fileAge.setEnabled(true);
    }//GEN-LAST:event_checkFileAgeYesActionPerformed

    private void checkFileAgeNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFileAgeNoActionPerformed
        fileAgeLabel.setEnabled(false);
        fileAge.setEnabled(false);
    }//GEN-LAST:event_checkFileAgeNoActionPerformed

    private void anonymousNoActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_anonymousNoActionPerformed
        FTPUsernameLabel.setEnabled(true);
        FTPUsernameField.setEnabled(true);
        FTPPasswordLabel.setEnabled(true);
        FTPPasswordField.setEnabled(true);
    }// GEN-LAST:event_anonymousNoActionPerformed

    private void anonymousYesActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_anonymousYesActionPerformed
        FTPUsernameLabel.setEnabled(false);
        FTPUsernameField.setEnabled(false);
        FTPPasswordLabel.setEnabled(false);
        FTPPasswordField.setEnabled(false);
        FTPUsernameField.setText("anonymous");
        FTPPasswordField.setText("anonymous");
    }// GEN-LAST:event_anonymousYesActionPerformed

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
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthRadioButton checkFileAgeNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton checkFileAgeYes;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deleteAfterReadNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deleteAfterReadYes;
    private com.webreach.mirth.client.ui.components.MirthTextField fileAge;
    private javax.swing.JLabel fileAgeLabel;
    private javax.swing.JLabel fileAgeLabel1;
    private com.webreach.mirth.client.ui.components.MirthTextField fileNameFilter;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeASCII;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeBinary;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private com.webreach.mirth.client.ui.components.MirthVariableList mirthVariableList1;
    private com.webreach.mirth.client.ui.components.MirthTextField moveToDirectory;
    private javax.swing.JLabel moveToDirectoryLabel;
    private javax.swing.JLabel moveToFileLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField moveToPattern;
    private com.webreach.mirth.client.ui.components.MirthRadioButton passiveModeNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton passiveModeYes;
    private com.webreach.mirth.client.ui.components.MirthTextField pollingFrequencyField;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processBatchFilesNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processBatchFilesYes;
    private com.webreach.mirth.client.ui.components.MirthComboBox sortBy;
    private com.webreach.mirth.client.ui.components.MirthRadioButton validateConnectionNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton validateConnectionYes;
    // End of variables declaration//GEN-END:variables

}
