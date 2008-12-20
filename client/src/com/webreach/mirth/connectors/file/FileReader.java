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

import com.webreach.mirth.client.core.ClientException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.util.ConnectionTestResponse;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.swingworker.SwingWorker;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class FileReader extends ConnectorClass
{
    private Logger logger = Logger.getLogger(this.getClass());

    /** Creates new form FileReader */
    public FileReader()
    {
        name = FileReaderProperties.name;
        initComponents();
        pollingFrequency.setDocument(new MirthFieldConstraints(0, false, false, true));
        fileAge.setDocument(new MirthFieldConstraints(0, false, false, true));
        // ast:encoding activation
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }
    
    /** Converts the values of the form fields to a Properties */
    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(FileReaderProperties.DATATYPE, name);
        
        if (((String) schemeComboBox.getSelectedItem()).equals("file"))
            properties.put(FileReaderProperties.FILE_SCHEME, FileReaderProperties.SCHEME_FILE);
        else if (((String) schemeComboBox.getSelectedItem()).equals("ftp"))
            properties.put(FileReaderProperties.FILE_SCHEME, FileReaderProperties.SCHEME_FTP);
        else if (((String) schemeComboBox.getSelectedItem()).equals("sftp"))
            properties.put(FileReaderProperties.FILE_SCHEME, FileReaderProperties.SCHEME_SFTP);
        else if (((String) schemeComboBox.getSelectedItem()).equals("smb"))
            properties.put(FileReaderProperties.FILE_SCHEME, FileReaderProperties.SCHEME_SMB);
        else {
           	// This "can't happen"
            logger.error("Unrecognized this.schemeComboBox value '" + schemeComboBox.getSelectedItem() + "', using 'file' instead");
            properties.put(FileReaderProperties.FILE_SCHEME, FileReaderProperties.SCHEME_FILE);
        }
        
        if (schemeComboBox.getSelectedItem().equals("file")) {
            properties.put(FileReaderProperties.FILE_HOST, directoryField.getText().replace('\\', '/'));
        }
        else {
            properties.put(FileReaderProperties.FILE_HOST, hostField.getText() + "/" + pathField.getText());
        }
        
        if (anonymousYes.isSelected())
            properties.put(FileReaderProperties.FILE_ANONYMOUS, UIConstants.YES_OPTION);
        else
            properties.put(FileReaderProperties.FILE_ANONYMOUS, UIConstants.NO_OPTION);

        properties.put(FileReaderProperties.FILE_USERNAME, usernameField.getText());
        properties.put(FileReaderProperties.FILE_PASSWORD, new String(passwordField.getPassword()));
        
        if (passiveModeYes.isSelected())
            properties.put(FileReaderProperties.FILE_PASSIVE_MODE, UIConstants.YES_OPTION);
        else
            properties.put(FileReaderProperties.FILE_PASSIVE_MODE, UIConstants.NO_OPTION);
        
        if (validateConnectionYes.isSelected())
            properties.put(FileReaderProperties.FILE_VALIDATE_CONNECTION, UIConstants.YES_OPTION);
        else
            properties.put(FileReaderProperties.FILE_VALIDATE_CONNECTION, UIConstants.NO_OPTION);
        
        properties.put(FileReaderProperties.FILE_MOVE_TO_PATTERN, moveToPattern.getText());
        properties.put(FileReaderProperties.FILE_MOVE_TO_DIRECTORY, moveToDirectory.getText().replace('\\', '/'));
        properties.put(FileReaderProperties.FILE_MOVE_TO_ERROR_DIRECTORY, errorMoveToDirectory.getText().replace('\\', '/'));
        
        if (deleteAfterReadYes.isSelected())
            properties.put(FileReaderProperties.FILE_DELETE_AFTER_READ, UIConstants.YES_OPTION);
        else
            properties.put(FileReaderProperties.FILE_DELETE_AFTER_READ, UIConstants.NO_OPTION);
        
        if (checkFileAgeYes.isSelected())
            properties.put(FileReaderProperties.FILE_CHECK_FILE_AGE, UIConstants.YES_OPTION);
        else
            properties.put(FileReaderProperties.FILE_CHECK_FILE_AGE, UIConstants.NO_OPTION);
        
        properties.put(FileReaderProperties.FILE_FILE_AGE, fileAge.getText());
        
        if (((String) sortBy.getSelectedItem()).equals("Name"))
            properties.put(FileReaderProperties.FILE_SORT_BY, FileReaderProperties.SORT_BY_NAME);
        else if (((String) sortBy.getSelectedItem()).equals("Size"))
            properties.put(FileReaderProperties.FILE_SORT_BY, FileReaderProperties.SORT_BY_SIZE);
        else if (((String) sortBy.getSelectedItem()).equals("Date"))
            properties.put(FileReaderProperties.FILE_SORT_BY, FileReaderProperties.SORT_BY_DATE);
        
        properties.put(FileReaderProperties.CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForConnector(charsetEncodingCombobox));
        
        properties.put(FileReaderProperties.FILE_FILTER, fileNameFilter.getText());
        
        if (processBatchFilesYes.isSelected())
            properties.put(FileReaderProperties.FILE_PROCESS_BATCH_FILES, UIConstants.YES_OPTION);
        else
            properties.put(FileReaderProperties.FILE_PROCESS_BATCH_FILES, UIConstants.NO_OPTION);
        
        if (fileTypeBinary.isSelected())
            properties.put(FileReaderProperties.FILE_TYPE, UIConstants.YES_OPTION);
        else
            properties.put(FileReaderProperties.FILE_TYPE, UIConstants.NO_OPTION);
        
        if (pollingIntervalButton.isSelected())
        {
            properties.put(FileReaderProperties.FILE_POLLING_TYPE, "interval");
            properties.put(FileReaderProperties.FILE_POLLING_FREQUENCY, pollingFrequency.getText());
        }
        else
        {
            properties.put(FileReaderProperties.FILE_POLLING_TYPE, "time");
            properties.put(FileReaderProperties.FILE_POLLING_TIME, pollingTime.getDate());
        }
        
    	logger.debug("getProperties: properties=" + properties);

        return properties;
    }
    
    /** Parses the scheme and URL to determine the values for the
     * directory, host and path fields, optionally storing them to
     * the fields, highlighting field errors, or just testing for
     * valid values.
     * 
     * @param props The connector properties from which to take the
     * values.
     * @param store If true, the parsed values are stored to the
     * corresponding form controls.
     * @param highlight If true, fields for which the parsed values
     * are invalid are highlighted.
     */ 
    public boolean setDirHostPath(Properties props, boolean store, boolean highlight) {
    	
    	boolean valid = true;
        Object schemeValue = props.get(FileReaderProperties.FILE_SCHEME);
    	String hostPropValue = (String) props.get(FileReaderProperties.FILE_HOST);
    	String directoryValue = "";
    	String hostValue = "";
    	String pathValue = "";
        if (schemeValue.equals(FileReaderProperties.SCHEME_FILE)) {
        	
        	directoryValue = hostPropValue;
        	if (directoryValue.length() <= 0) {
        		if (highlight) {
        			directoryField.setBackground(UIConstants.INVALID_COLOR);
        		}
        		valid = false;
        	}
        }
        else {
        	
            int splitIndex = hostPropValue.indexOf('/');
            if (splitIndex != -1)
            {
            	hostValue = hostPropValue.substring(0, splitIndex);
            	pathValue = hostPropValue.substring(splitIndex + 1);
            }
            else
            {
            	hostValue = hostPropValue;
            }
            
        	if (hostValue.length() <= 0) {
        		if (highlight) {
        			hostField.setBackground(UIConstants.INVALID_COLOR);
        		}
        		valid = false;
        	}
        }
        
        if (store) {
        	
        	directoryField.setText(directoryValue);
            hostField.setText(hostValue);
            pathField.setText(pathValue);
        }
        
        return valid;
    }

    /** Converts a Properties to values of the form fields */
    public void setProperties(Properties props)
    {
    	logger.debug("setProperties: props=" + props);

        resetInvalidProperties();

        Object schemeValue = props.get(FileReaderProperties.FILE_SCHEME);
        if (schemeValue.equals(FileReaderProperties.SCHEME_FILE))
            schemeComboBox.setSelectedItem("file");
        else if (schemeValue.equals(FileReaderProperties.SCHEME_FTP))
            schemeComboBox.setSelectedItem("ftp");
        else if (schemeValue.equals(FileReaderProperties.SCHEME_SFTP))
            schemeComboBox.setSelectedItem("sftp");
        else if (schemeValue.equals(FileReaderProperties.SCHEME_SMB))
            schemeComboBox.setSelectedItem("smb");
        else {
           	// This "can't happen"
            logger.error("Unrecognized props[\"scheme\"] value '" + schemeValue + "', using 'file' instead");
            schemeComboBox.setSelectedItem("file");
        }
        schemeComboBoxActionPerformed(null);

        setDirHostPath(props, true, false);
        
        if (((String) props.get(FileReaderProperties.FILE_ANONYMOUS)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            anonymousYes.setSelected(true);
            anonymousNo.setSelected(false);
            anonymousYesActionPerformed(null);
        }
        else
        {
            anonymousYes.setSelected(false);
            anonymousNo.setSelected(true);
            anonymousNoActionPerformed(null);
            usernameField.setText((String) props.get(FileReaderProperties.FILE_USERNAME));
            passwordField.setText((String) props.get(FileReaderProperties.FILE_PASSWORD));
        }
        
        if (((String) props.get(FileReaderProperties.FILE_PASSIVE_MODE)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            passiveModeYes.setSelected(true);
            passiveModeNo.setSelected(false);
        }
        else {
            passiveModeYes.setSelected(false);
            passiveModeNo.setSelected(true);
        }
        
        if (((String) props.get(FileReaderProperties.FILE_VALIDATE_CONNECTION)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            validateConnectionYes.setSelected(true);
            validateConnectionNo.setSelected(false);
        }
        else {
            validateConnectionYes.setSelected(false);
            validateConnectionNo.setSelected(true);
        }
        
        moveToPattern.setText((String) props.get(FileReaderProperties.FILE_MOVE_TO_PATTERN));
        moveToDirectory.setText((String) props.get(FileReaderProperties.FILE_MOVE_TO_DIRECTORY));
        errorMoveToDirectory.setText((String) props.get(FileReaderProperties.FILE_MOVE_TO_ERROR_DIRECTORY));
        
        if (((String) props.get(FileReaderProperties.FILE_DELETE_AFTER_READ)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            deleteAfterReadYes.setSelected(true);
            deleteAfterReadNo.setSelected(false);
            deleteAfterReadYesActionPerformed(null);
        }
        else
        {
            deleteAfterReadYes.setSelected(false);
            deleteAfterReadNo.setSelected(true);
            deleteAfterReadNoActionPerformed(null);
        }
        if (((String) props.get(FileReaderProperties.FILE_CHECK_FILE_AGE)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            checkFileAgeYes.setSelected(true);
            checkFileAgeNo.setSelected(false);
            checkFileAgeYesActionPerformed(null);
        }
        else
        {
            checkFileAgeYes.setSelected(false);
            checkFileAgeNo.setSelected(true);
            checkFileAgeNoActionPerformed(null);
        }
        
        fileAge.setText((String) props.get(FileReaderProperties.FILE_FILE_AGE));
        
        if (props.get(FileReaderProperties.FILE_SORT_BY).equals(FileReaderProperties.SORT_BY_NAME))
            sortBy.setSelectedItem("Name");
        else if (props.get(FileReaderProperties.FILE_SORT_BY).equals(FileReaderProperties.SORT_BY_SIZE))
            sortBy.setSelectedItem("Size");
        else if (props.get(FileReaderProperties.FILE_SORT_BY).equals(FileReaderProperties.SORT_BY_DATE))
            sortBy.setSelectedItem("Date");
        
        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(FileReaderProperties.CONNECTOR_CHARSET_ENCODING));
        
        fileNameFilter.setText((String) props.get(FileReaderProperties.FILE_FILTER));
        
        if (((String) props.get(FileReaderProperties.FILE_TYPE)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            fileTypeBinary.setSelected(true);
            fileTypeASCII.setSelected(false);
            fileTypeBinaryActionPerformed(null);
        }
        else
        {
            fileTypeBinary.setSelected(false);
            fileTypeASCII.setSelected(true);
            fileTypeASCIIActionPerformed(null);
        }
        
        if (((String) props.get(FileReaderProperties.FILE_PROCESS_BATCH_FILES)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            processBatchFilesYes.setSelected(true);
            processBatchFilesNo.setSelected(false);
        }
        else {
            processBatchFilesYes.setSelected(false);
            processBatchFilesNo.setSelected(true);
        }
        
        if (((String) props.get(FileReaderProperties.FILE_POLLING_TYPE)).equalsIgnoreCase("interval"))
        {
            pollingIntervalButton.setSelected(true);
            pollingTimeButton.setSelected(false);
            pollingIntervalButtonActionPerformed(null);
            pollingFrequency.setText((String) props.get(FileReaderProperties.FILE_POLLING_FREQUENCY));
        }
        else
        {
            pollingIntervalButton.setSelected(false);
            pollingTimeButton.setSelected(true);
            pollingTimeButtonActionPerformed(null);
            pollingTime.setDate((String) props.get(FileReaderProperties.FILE_POLLING_TIME));
        }
    }
    
    /** Returns the default Properties */
    public Properties getDefaults()
    {
        return new FileReaderProperties().getDefaults();
    }
    
    /** Tests if the specified Properties are valid, optionally highlighting fields
     * with invalid entries.
     */
    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;

        valid = setDirHostPath(props, false, highlight);

        if (((String) props.get(FileReaderProperties.FILE_FILTER)).length() == 0)
        {
            valid = false;
            if (highlight)
            	fileNameFilter.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FileReaderProperties.FILE_POLLING_TYPE)).equalsIgnoreCase("interval") && ((String) props.get(FileReaderProperties.FILE_POLLING_FREQUENCY)).length() == 0)
        {
            valid = false;
            if (highlight)
            	pollingFrequency.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FileReaderProperties.FILE_POLLING_TYPE)).equalsIgnoreCase("time") && ((String) props.get(FileReaderProperties.FILE_POLLING_TIME)).length() == 0)
        {
            valid = false;
            if (highlight)
            	pollingTime.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FileReaderProperties.FILE_ANONYMOUS)).equals(UIConstants.NO_OPTION))
        {
            if (((String) props.get(FileReaderProperties.FILE_USERNAME)).length() == 0)
            {
                valid = false;
                if (highlight)
                	usernameField.setBackground(UIConstants.INVALID_COLOR);
            }
            if (((String) props.get(FileReaderProperties.FILE_PASSWORD)).length() == 0)
            {
                valid = false;
                if (highlight)
                	passwordField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(FileReaderProperties.FILE_CHECK_FILE_AGE)).equals(UIConstants.YES_OPTION))
        {
            if (((String) props.get(FileReaderProperties.FILE_FILE_AGE)).length() == 0)
            {
                valid = false;
                if (highlight)
                	fileAge.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        return valid;
    }
    
    /** Turns off all invalid property value highlighting */
    private void resetInvalidProperties()
    {
        directoryField.setBackground(null);
        hostField.setBackground(null);
        pathField.setBackground(null);
        fileNameFilter.setBackground(null);
        pollingFrequency.setBackground(null);
        pollingTime.setBackground(null);
        fileAge.setBackground(null);
        usernameField.setBackground(null);
        passwordField.setBackground(null);
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        buttonGroup8 = new javax.swing.ButtonGroup();
        schemeLabel = new javax.swing.JLabel();
        schemeComboBox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        directoryLabel = new javax.swing.JLabel();
        directoryField = new com.webreach.mirth.client.ui.components.MirthTextField();
        hostLabel = new javax.swing.JLabel();
        hostField = new com.webreach.mirth.client.ui.components.MirthTextField();
        pathLabel = new javax.swing.JLabel();
        pathField = new com.webreach.mirth.client.ui.components.MirthTextField();
        filenameFilterLabel = new javax.swing.JLabel();
        fileNameFilter = new com.webreach.mirth.client.ui.components.MirthTextField();
        pollingTypeLabel = new javax.swing.JLabel();
        pollingIntervalButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        pollingTimeButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        pollingFrequencyLabel = new javax.swing.JLabel();
        pollingFrequency = new com.webreach.mirth.client.ui.components.MirthTextField();
        pollingTimeLabel = new javax.swing.JLabel();
        pollingTime = new com.webreach.mirth.client.ui.components.MirthTimePicker();
        moveToDirectoryLabel = new javax.swing.JLabel();
        moveToPattern = new com.webreach.mirth.client.ui.components.MirthTextField();
        moveToDirectory = new com.webreach.mirth.client.ui.components.MirthTextField();
        moveToFileLabel = new javax.swing.JLabel();
        deleteAfterReadLabel = new javax.swing.JLabel();
        checkFileAgeLabel = new javax.swing.JLabel();
        fileAgeLabel = new javax.swing.JLabel();
        deleteAfterReadYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        deleteAfterReadNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        checkFileAgeYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        checkFileAgeNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        mirthVariableList1 = new com.webreach.mirth.client.ui.components.MirthVariableList();
        fileAge = new com.webreach.mirth.client.ui.components.MirthTextField();
        sortFilesByLabel = new javax.swing.JLabel();
        sortBy = new com.webreach.mirth.client.ui.components.MirthComboBox();
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        processBatchFilesLabel = new javax.swing.JLabel();
        processBatchFilesYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        processBatchFilesNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeASCII = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeBinary = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeLabel = new javax.swing.JLabel();
        errorMoveToDirectoryLabel = new javax.swing.JLabel();
        errorMoveToDirectory = new com.webreach.mirth.client.ui.components.MirthTextField();
        anonymousLabel = new javax.swing.JLabel();
        anonymousYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        anonymousNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        usernameLabel = new javax.swing.JLabel();
        usernameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        validateConnectionLabel = new javax.swing.JLabel();
        validateConnectionYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        validateConnectionNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        passiveModeLabel = new javax.swing.JLabel();
        passiveModeYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        passiveModeNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        testConnection = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        schemeLabel.setText("Method:");

        schemeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "file", "ftp", "sftp" }));
        schemeComboBox.setToolTipText("The basic method used to access files to be read - file (local filesystem), FTP, or SFTP.");
        schemeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                schemeComboBoxActionPerformed(evt);
            }
        });

        directoryLabel.setText("Directory:");

        directoryField.setToolTipText("The directory (folder) in which the files to be read can be found.");

        hostLabel.setText("ftp://");

        hostField.setToolTipText("The name or IP address of the host (computer) on which the files to be read can be found.");

        pathLabel.setText("/");

        pathField.setToolTipText("The directory (folder) in which the files to be read can be found.");

        filenameFilterLabel.setText("Filename Filter Pattern:");

        fileNameFilter.setToolTipText("<html>The pattern which names of files must match in order to be read.<br>Files with names that do not match the pattern will be ignored.</html>");

        pollingTypeLabel.setText("Polling Type:");

        pollingIntervalButton.setBackground(new java.awt.Color(255, 255, 255));
        pollingIntervalButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(pollingIntervalButton);
        pollingIntervalButton.setText("Interval");
        pollingIntervalButton.setToolTipText("Records that the time at which polling for files to be read will be specified as the time between polling attempts.");
        pollingIntervalButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pollingIntervalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pollingIntervalButtonActionPerformed(evt);
            }
        });

        pollingTimeButton.setBackground(new java.awt.Color(255, 255, 255));
        pollingTimeButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(pollingTimeButton);
        pollingTimeButton.setText("Time");
        pollingTimeButton.setToolTipText("Records that the time at which polling for files to be read will be specified as the time of day at which a polling attempt will occur each day.");
        pollingTimeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pollingTimeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pollingTimeButtonActionPerformed(evt);
            }
        });

        pollingFrequencyLabel.setText("Polling Frequency (ms):");

        pollingFrequency.setToolTipText("If the Interval Polling Type is selected, enter the number of milliseconds between polling attempts here.");

        pollingTimeLabel.setText("Polling Time (daily):");

        pollingTime.setToolTipText("If the Time Polling Type is selected, enter the time of day for polling attempts here.");

        moveToDirectoryLabel.setText("Move-to Directory:");

        moveToPattern.setToolTipText("<html>If successfully processed files should be renamed, enter the new name here.<br>The filename specified may include template substitutions from the list to the right.<br>If this field is left empty, successfully processed files will not be renamed.</html>");

        moveToDirectory.setToolTipText("<html>If successfully processed files should be moved to a different directory (folder), enter that directory here.<br>The directory name specified may include template substitutions from the list to the right.<br>If this field is left empty, successfully processed files will not be moved to a different directory.</html>");

        moveToFileLabel.setText("Move-to File Name:");

        deleteAfterReadLabel.setText("Delete File After Read:");

        checkFileAgeLabel.setText("Check File Age:");

        fileAgeLabel.setText("File Age (ms):");

        deleteAfterReadYes.setBackground(new java.awt.Color(255, 255, 255));
        deleteAfterReadYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(deleteAfterReadYes);
        deleteAfterReadYes.setText("Yes");
        deleteAfterReadYes.setToolTipText("Select Yes to delete files after they are processed.");
        deleteAfterReadYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deleteAfterReadYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAfterReadYesActionPerformed(evt);
            }
        });

        deleteAfterReadNo.setBackground(new java.awt.Color(255, 255, 255));
        deleteAfterReadNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(deleteAfterReadNo);
        deleteAfterReadNo.setSelected(true);
        deleteAfterReadNo.setText("No");
        deleteAfterReadNo.setToolTipText("Select No to not delete files after they are processed.");
        deleteAfterReadNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deleteAfterReadNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAfterReadNoActionPerformed(evt);
            }
        });

        checkFileAgeYes.setBackground(new java.awt.Color(255, 255, 255));
        checkFileAgeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(checkFileAgeYes);
        checkFileAgeYes.setText("Yes");
        checkFileAgeYes.setToolTipText("Select Yes to skip processing files which are older than the specified age.");
        checkFileAgeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkFileAgeYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFileAgeYesActionPerformed(evt);
            }
        });

        checkFileAgeNo.setBackground(new java.awt.Color(255, 255, 255));
        checkFileAgeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(checkFileAgeNo);
        checkFileAgeNo.setSelected(true);
        checkFileAgeNo.setText("No");
        checkFileAgeNo.setToolTipText("Select No to process files regardless of age.");
        checkFileAgeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkFileAgeNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFileAgeNoActionPerformed(evt);
            }
        });

        mirthVariableList1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        mirthVariableList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "DATE", "COUNT", "UUID", "SYSTIME", "ORIGINALNAME" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });

        fileAge.setToolTipText("If Check File Age Yes is selected, the maximum age of a file, in milliseconds, that should be processed.");

        sortFilesByLabel.setText("Sort Files By:");

        sortBy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Date", "Name", "Size" }));
        sortBy.setToolTipText("<html>Selects the order in which files should be processed, if there are multiple files available to be processed.<br>Files can be processed by Date (oldest last modification date first), Size (smallest first) or name (a before z, etc.).</html>");
        sortBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByActionPerformed(evt);
            }
        });

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingCombobox.setToolTipText("If File Type ASCII is selected, select the character set encoding (ASCII, UTF-8, etc.) to be used in reading the contents of each file.");
        charsetEncodingCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        encodingLabel.setText("Encoding:");

        processBatchFilesLabel.setText("Process Batch Files:");

        processBatchFilesYes.setBackground(new java.awt.Color(255, 255, 255));
        processBatchFilesYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup8.add(processBatchFilesYes);
        processBatchFilesYes.setText("Yes");
        processBatchFilesYes.setToolTipText("Select Yes to process all messages in each file.");
        processBatchFilesYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        processBatchFilesNo.setBackground(new java.awt.Color(255, 255, 255));
        processBatchFilesNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup8.add(processBatchFilesNo);
        processBatchFilesNo.setSelected(true);
        processBatchFilesNo.setText("No");
        processBatchFilesNo.setToolTipText("Select No to process the entire contents of the file as a single message.");
        processBatchFilesNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        processBatchFilesNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processBatchFilesNoActionPerformed(evt);
            }
        });

        fileTypeASCII.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeASCII.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(fileTypeASCII);
        fileTypeASCII.setSelected(true);
        fileTypeASCII.setText("ASCII");
        fileTypeASCII.setToolTipText("Select No if files contain text (ASCII is a misnomer here).");
        fileTypeASCII.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileTypeASCII.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTypeASCIIActionPerformed(evt);
            }
        });

        fileTypeBinary.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeBinary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(fileTypeBinary);
        fileTypeBinary.setText("Binary");
        fileTypeBinary.setToolTipText("Select Yes if files contain binary data which should be Base64 encoded before processing.");
        fileTypeBinary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileTypeBinary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTypeBinaryActionPerformed(evt);
            }
        });

        fileTypeLabel.setText("File Type:");

        errorMoveToDirectoryLabel.setText("Error Move-to Directory:");

        errorMoveToDirectory.setToolTipText("<html>If files which cause processing errors should be moved to a different directory (folder), enter that directory here.<br>The directory name specified may include template substitutions from the list to the right.<br>If this field is left empty, files which cause processing errors will not be moved to a different directory.</html>");

        anonymousLabel.setText("Anonymous:");

        anonymousYes.setBackground(new java.awt.Color(255, 255, 255));
        anonymousYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(anonymousYes);
        anonymousYes.setText("Yes");
        anonymousYes.setToolTipText("Connects to the file anonymously instead of using a username and password.");
        anonymousYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        anonymousYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anonymousYesActionPerformed(evt);
            }
        });

        anonymousNo.setBackground(new java.awt.Color(255, 255, 255));
        anonymousNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(anonymousNo);
        anonymousNo.setSelected(true);
        anonymousNo.setText("No");
        anonymousNo.setToolTipText("Connects to the file using a username and password instead of anonymously.");
        anonymousNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        anonymousNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anonymousNoActionPerformed(evt);
            }
        });

        usernameLabel.setText("Username:");

        usernameField.setToolTipText("The user name used to gain access to the server.");

        passwordLabel.setText("Password:");

        passwordField.setToolTipText("The password used to gain access to the server.");

        validateConnectionLabel.setText("Validate Connection:");

        validateConnectionYes.setBackground(new java.awt.Color(255, 255, 255));
        validateConnectionYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(validateConnectionYes);
        validateConnectionYes.setText("Yes");
        validateConnectionYes.setToolTipText("Select Yes to test the connection to the server before each operation.");
        validateConnectionYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        validateConnectionNo.setBackground(new java.awt.Color(255, 255, 255));
        validateConnectionNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(validateConnectionNo);
        validateConnectionNo.setSelected(true);
        validateConnectionNo.setText("No");
        validateConnectionNo.setToolTipText("Select No to skip testing the connection to the server before each operation.");
        validateConnectionNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        passiveModeLabel.setText("Passive Mode:");

        passiveModeYes.setBackground(new java.awt.Color(255, 255, 255));
        passiveModeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(passiveModeYes);
        passiveModeYes.setText("Yes");
        passiveModeYes.setToolTipText("<html>Select Yes to connect to the server in \"passive mode\".<br>Passive mode sometimes allows a connection through a firewall that normal mode does not.</html>");
        passiveModeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        passiveModeNo.setBackground(new java.awt.Color(255, 255, 255));
        passiveModeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(passiveModeNo);
        passiveModeNo.setSelected(true);
        passiveModeNo.setText("No");
        passiveModeNo.setToolTipText("Select Yes to connect to the server in \"normal mode\" as opposed to passive mode.");
        passiveModeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        testConnection.setText("Test Read");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(processBatchFilesLabel)
                    .add(anonymousLabel)
                    .add(filenameFilterLabel)
                    .add(directoryLabel)
                    .add(hostLabel)
                    .add(schemeLabel)
                    .add(passiveModeLabel)
                    .add(passwordLabel)
                    .add(validateConnectionLabel)
                    .add(pollingTypeLabel)
                    .add(pollingFrequencyLabel)
                    .add(moveToFileLabel)
                    .add(moveToDirectoryLabel)
                    .add(errorMoveToDirectoryLabel)
                    .add(deleteAfterReadLabel)
                    .add(checkFileAgeLabel)
                    .add(fileAgeLabel)
                    .add(sortFilesByLabel)
                    .add(fileTypeLabel)
                    .add(encodingLabel)
                    .add(usernameLabel)
                    .add(pollingTimeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(processBatchFilesYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(processBatchFilesNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(checkFileAgeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(checkFileAgeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(pollingFrequency, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(pollingIntervalButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pollingTimeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(validateConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(validateConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(passiveModeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(passiveModeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(usernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(anonymousYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(anonymousNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(fileNameFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(directoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(schemeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(testConnection))
                    .add(layout.createSequentialGroup()
                        .add(hostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pathLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pathField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 185, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(moveToPattern, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(pollingTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(moveToDirectory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(deleteAfterReadYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(deleteAfterReadNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(sortBy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fileAge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(errorMoveToDirectory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mirthVariableList1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(schemeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(schemeLabel)
                    .add(testConnection))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(directoryLabel)
                    .add(directoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostLabel)
                    .add(pathLabel)
                    .add(hostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pathField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(filenameFilterLabel)
                            .add(fileNameFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(anonymousLabel)
                            .add(anonymousYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(anonymousNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(usernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(usernameLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(passwordLabel)
                            .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(passiveModeLabel)
                            .add(passiveModeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(passiveModeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(validateConnectionLabel)
                            .add(validateConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(validateConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(pollingTypeLabel)
                            .add(pollingIntervalButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(pollingTimeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(pollingFrequencyLabel)
                            .add(pollingFrequency, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(pollingTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(pollingTimeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(moveToDirectoryLabel)
                            .add(moveToDirectory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(moveToFileLabel)
                            .add(moveToPattern, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(errorMoveToDirectoryLabel)
                            .add(errorMoveToDirectory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(deleteAfterReadLabel)
                            .add(deleteAfterReadYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(deleteAfterReadNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(checkFileAgeLabel)
                            .add(checkFileAgeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(checkFileAgeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fileAgeLabel)
                            .add(fileAge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(sortFilesByLabel)
                            .add(sortBy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fileTypeLabel)
                            .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(encodingLabel)
                            .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(processBatchFilesLabel)
                            .add(processBatchFilesYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(processBatchFilesNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(206, 206, 206)
                        .add(mirthVariableList1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void anonymousNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anonymousNoActionPerformed

        usernameLabel.setEnabled(true);
        usernameField.setEnabled(true);

        passwordLabel.setEnabled(true);
        passwordField.setEnabled(true);
    }//GEN-LAST:event_anonymousNoActionPerformed

    private void anonymousYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anonymousYesActionPerformed

        usernameLabel.setEnabled(false);
        usernameField.setEnabled(false);
        usernameField.setText("anonymous");
        
        passwordLabel.setEnabled(false);
        passwordField.setEnabled(false);
        passwordField.setText("anonymous");
    }//GEN-LAST:event_anonymousYesActionPerformed

    private void onSchemeChange(boolean enableHost, boolean enableOthers, boolean anonymous) {
        
            // act like the appropriate Anonymous button was selected.
        if (anonymous) {
            
            anonymousNo.setSelected(false);
            anonymousYes.setSelected(true);
            anonymousYesActionPerformed(null);
        }
        else {
            
            anonymousNo.setSelected(true);
            anonymousYes.setSelected(false);
            anonymousNoActionPerformed(null);
        }
            
        hostLabel.setEnabled(enableHost);
        hostField.setEnabled(enableHost);
        pathLabel.setEnabled(enableHost);
        pathField.setEnabled(enableHost);
        directoryLabel.setEnabled(!enableHost);
        directoryField.setEnabled(!enableHost);

        anonymousLabel.setEnabled(enableOthers);
        anonymousYes.setEnabled(enableOthers);
        anonymousNo.setEnabled(enableOthers);
        passiveModeLabel.setEnabled(enableOthers);
        passiveModeYes.setEnabled(enableOthers);
        passiveModeNo.setEnabled(enableOthers);
        validateConnectionLabel.setEnabled(enableOthers);
        validateConnectionYes.setEnabled(enableOthers);
        validateConnectionNo.setEnabled(enableOthers);
    }

    private void schemeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schemeComboBoxActionPerformed

        String text = (String) schemeComboBox.getSelectedItem();
        
        // if File is selected
        if (text.equals("file")) {
            
            onSchemeChange(false, false, true);
            hostField.setText("");
        }
        // else if FTP is selected
        else if (text.equals("ftp")) {

            onSchemeChange(true, true, anonymousYes.isSelected());
            hostLabel.setText("ftp://");
        }
        // else if SFTP is selected
        else if (text.equals("sftp")) {
            
            onSchemeChange(true, false, false);
            hostLabel.setText("sftp://");
        }
        // else if SMB is selected
        else if (text.equals("smb")) {
            
            onSchemeChange(true, false, false);
            hostLabel.setText("smb://");
        }
    }//GEN-LAST:event_schemeComboBoxActionPerformed

    private void sortByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_sortByActionPerformed
    
    private void pollingTimeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pollingTimeButtonActionPerformed
    {//GEN-HEADEREND:event_pollingTimeButtonActionPerformed
        pollingFrequencyLabel.setEnabled(false);
        pollingTimeLabel.setEnabled(true);
        pollingFrequency.setEnabled(false);
        pollingTime.setEnabled(true);
        
    }//GEN-LAST:event_pollingTimeButtonActionPerformed
    
    private void pollingIntervalButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pollingIntervalButtonActionPerformed
    {//GEN-HEADEREND:event_pollingIntervalButtonActionPerformed
        pollingFrequencyLabel.setEnabled(true);
        pollingTimeLabel.setEnabled(false);
        pollingFrequency.setEnabled(true);
        pollingTime.setEnabled(false);
    }//GEN-LAST:event_pollingIntervalButtonActionPerformed
    
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
    
    private void fileTypeASCIIActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fileTypeASCIIActionPerformed
    {//GEN-HEADEREND:event_fileTypeASCIIActionPerformed
        encodingLabel.setEnabled(true);
        charsetEncodingCombobox.setEnabled(true);
        
        processBatchFilesLabel.setEnabled(true);
        processBatchFilesYes.setSelected(true);
        processBatchFilesNo.setEnabled(true);
        processBatchFilesYes.setEnabled(true);
    }//GEN-LAST:event_fileTypeASCIIActionPerformed
    
    private void fileTypeBinaryActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fileTypeBinaryActionPerformed
    {//GEN-HEADEREND:event_fileTypeBinaryActionPerformed
        encodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
        
        processBatchFilesLabel.setEnabled(false);
        processBatchFilesNo.setSelected(true);
        processBatchFilesNo.setEnabled(false);
        processBatchFilesYes.setEnabled(false);
    }//GEN-LAST:event_fileTypeBinaryActionPerformed

private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
parent.setWorking("Testing connection...", true);

    SwingWorker worker = new SwingWorker<Void, Void>() {

        public Void doInBackground() {
            
            try {
                ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(name, "testRead", getProperties());

                if (response == null) {
                    throw new ClientException("Failed to invoke service.");
                } else if(response.getType().equals(ConnectionTestResponse.Type.SUCCESS)) { 
                    parent.alertInformation(parent, response.getMessage());
                } else { 
                    parent.alertWarning(parent, response.getMessage());
                }

                return null;
            } catch (ClientException e) {
                parent.alertError(parent, e.getMessage());
                return null;
            }
        }

        public void done() {
            parent.setWorking("", false);
        }
    };

    worker.execute();
}//GEN-LAST:event_testConnectionActionPerformed
    
    private void processBatchFilesNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_processBatchFilesNoActionPerformed
    {// GEN-HEADEREND:event_processBatchFilesNoActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_processBatchFilesNoActionPerformed
    
    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_charsetEncodingComboboxActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_charsetEncodingComboboxActionPerformed
    
    private void checkFileAgeNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_checkFileAgeNoActionPerformed
    {// GEN-HEADEREND:event_checkFileAgeNoActionPerformed
        fileAgeLabel.setEnabled(false);
        fileAge.setEnabled(false);
    }// GEN-LAST:event_checkFileAgeNoActionPerformed
    
    private void checkFileAgeYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_checkFileAgeYesActionPerformed
    {// GEN-HEADEREND:event_checkFileAgeYesActionPerformed
        fileAgeLabel.setEnabled(true);
        fileAge.setEnabled(true);
    }// GEN-LAST:event_checkFileAgeYesActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel anonymousLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton anonymousNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton anonymousYes;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.ButtonGroup buttonGroup8;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private javax.swing.JLabel checkFileAgeLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton checkFileAgeNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton checkFileAgeYes;
    private javax.swing.JLabel deleteAfterReadLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deleteAfterReadNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton deleteAfterReadYes;
    private com.webreach.mirth.client.ui.components.MirthTextField directoryField;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.JLabel encodingLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField errorMoveToDirectory;
    private javax.swing.JLabel errorMoveToDirectoryLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField fileAge;
    private javax.swing.JLabel fileAgeLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField fileNameFilter;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeASCII;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeBinary;
    private javax.swing.JLabel fileTypeLabel;
    private javax.swing.JLabel filenameFilterLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField hostField;
    private javax.swing.JLabel hostLabel;
    private com.webreach.mirth.client.ui.components.MirthVariableList mirthVariableList1;
    private com.webreach.mirth.client.ui.components.MirthTextField moveToDirectory;
    private javax.swing.JLabel moveToDirectoryLabel;
    private javax.swing.JLabel moveToFileLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField moveToPattern;
    private javax.swing.JLabel passiveModeLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton passiveModeNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton passiveModeYes;
    private com.webreach.mirth.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField pathField;
    private javax.swing.JLabel pathLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField pollingFrequency;
    private javax.swing.JLabel pollingFrequencyLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton pollingIntervalButton;
    private com.webreach.mirth.client.ui.components.MirthTimePicker pollingTime;
    private com.webreach.mirth.client.ui.components.MirthRadioButton pollingTimeButton;
    private javax.swing.JLabel pollingTimeLabel;
    private javax.swing.JLabel pollingTypeLabel;
    private javax.swing.JLabel processBatchFilesLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processBatchFilesNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processBatchFilesYes;
    private com.webreach.mirth.client.ui.components.MirthComboBox schemeComboBox;
    private javax.swing.JLabel schemeLabel;
    private com.webreach.mirth.client.ui.components.MirthComboBox sortBy;
    private javax.swing.JLabel sortFilesByLabel;
    private javax.swing.JButton testConnection;
    private com.webreach.mirth.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JLabel validateConnectionLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton validateConnectionNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton validateConnectionYes;
    // End of variables declaration//GEN-END:variables
    
}
