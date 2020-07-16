/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.mongo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthPasswordField;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ResponseHandler;
import com.mirth.connect.connectors.doc.DocumentConnectorServletInterface;
import com.mirth.connect.connectors.mongo.MongoDispatcherProperties;
import com.mirth.connect.connectors.doc.PageSize;
import com.mirth.connect.connectors.doc.Unit;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.util.ConnectionTestResponse;
import javax.swing.SwingConstants;

public class MongoWriter extends ConnectorSettingsPanel {

    private Frame parent;
    private boolean pageSizeUpdating;

    public MongoWriter() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initLayout();
    }

    @Override
    public String getConnectorName() {
        return new MongoDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
    	
        MongoDispatcherProperties properties = new MongoDispatcherProperties();

        properties.setDbName(dbNameField.getText().trim());
        properties.setDbUserName(userNameField.getText().trim());
        properties.setCollectionName(collectionField.getText().trim());       
        properties.setHostAddress(hostField.getText().trim());
        properties.setDbPort(Integer.parseInt(portField.getText()));
        properties.setDbPassword(new String(passwordField.getPassword()));

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        MongoDispatcherProperties props = (MongoDispatcherProperties) properties;

        dbNameField.setText(props.getDbName());
        userNameField.setText(props.getDbUserName());
        collectionField.setText(props.getCollectionName());
        passwordField.setText(props.getDbPassword());
        hostField.setText(props.getHostAddress());
        portField.setText(props.getDbPort()+"");

    }

    @Override
    public ConnectorProperties getDefaults() {
        return new MongoDispatcherProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        MongoDispatcherProperties props = (MongoDispatcherProperties) properties;        
        
        boolean valid = true;

        try {
        	String pText=portField.getText().trim();
        	Integer.parseInt(pText);
		} catch (Exception e) {
			portField.setBackground(UIConstants.INVALID_COLOR);
		}
      

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        hostField.setBackground(null);
        portField.setBackground(null);
        passwordField.setBackground(null);

    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        outputLabel = new JLabel("DB:");
        ButtonGroup outputButtonGroup = new ButtonGroup();

        directoryLabel = new JLabel("Host:");
        directoryLabel.setToolTipText("Host address");

        hostField = new MirthTextField();
        hostField.setText("localhost");
        hostField.setToolTipText("The directory (folder) where the generated file should be written.");

        fileNameLabel = new JLabel("port:");
        portField = new MirthTextField();
        portField.setText("27017");
        portField.setToolTipText("DB port number");

        userNameLabel = new JLabel("User Name:");
        ButtonGroup documentTypeButtonGroup = new ButtonGroup();
        ButtonGroup encryptedButtonGroup = new ButtonGroup();

        DocumentListener pageSizeDocumentListener = new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent evt) {
                updatePageSizeComboBox();
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                updatePageSizeComboBox();
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                updatePageSizeComboBox();
            }
        };
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, gap 6", "[]12[grow]", "[]4[]6[]4[]4[]4[]"));

        add(outputLabel, "cell 0 0,alignx trailing");
        
        dbNameField = new MirthTextField();
        dbNameField.setToolTipText("DB Name");
        add(dbNameField, "cell 1 0,growx");
        add(directoryLabel, "cell 0 1,alignx right");
        add(hostField, "cell 1 1,width 200!");
        add(fileNameLabel, "cell 0 2,alignx right");
        add(portField, "cell 1 2,width 200!");
        add(userNameLabel, "cell 0 3,alignx trailing");
        
        userNameField = new MirthTextField();
        userNameField.setToolTipText("DB User Name");
        add(userNameField, "cell 1 3,growx");
        
                passwordLabel = new JLabel("Password:");
                add(passwordLabel, "cell 0 4,alignx right");
        passwordField = new MirthPasswordField();
        passwordField.setToolTipText("password");
        add(passwordField, "cell 1 4,growx,width 124!");
        
        lblCollection = new JLabel("collection:");
        add(lblCollection, "cell 0 5,alignx trailing");
        
        collectionField = new MirthTextField();
        collectionField.setToolTipText("collection name");
        add(collectionField, "cell 1 5,growx");
    }

    private void updateFileEnabled(boolean enable) {
        fileNameLabel.setEnabled(enable);
        portField.setEnabled(enable);
        directoryLabel.setEnabled(enable);
        hostField.setEnabled(enable);

    }





    private void encryptedYesActionPerformed() {
        passwordLabel.setEnabled(true);
        passwordField.setEnabled(true);
    }

    private void encryptedNoActionPerformed() {
        passwordLabel.setEnabled(false);
        passwordField.setEnabled(false);
    }

    private void updatePageSizeComboBox() {
        if (pageSizeUpdating) {
            return;
        }
        pageSizeUpdating = true;

        try {
         
        } catch (Exception e) {
        }

        pageSizeUpdating = false;
    }

    private JLabel outputLabel;
    private JLabel directoryLabel;
    private JTextField hostField;
    private JLabel fileNameLabel;
    private JTextField portField;
    private JLabel userNameLabel;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private MirthTextField dbNameField;
    private MirthTextField userNameField;
    private JLabel lblCollection;
    private MirthTextField collectionField;
}
