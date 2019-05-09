/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ListenerSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class WebServiceListener extends ConnectorSettingsPanel {

    private boolean usingHttps = false;

    public WebServiceListener() {
        initComponents();
        initLayout();
        wsdlField.setEditable(false);
        methodField.setEditable(false);
    }

    @Override
    public String getConnectorName() {
        return new WebServiceReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        WebServiceReceiverProperties properties = new WebServiceReceiverProperties();
        properties.setClassName(classNameField.getText());
        properties.setServiceName(serviceNameField.getText());
        properties.setSoapBinding(Binding.fromDisplayName((String) versionComboBox.getSelectedItem()));

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        WebServiceReceiverProperties props = (WebServiceReceiverProperties) properties;

        versionComboBox.setSelectedIndex(0);
        versionComboBox.setSelectedItem(props.getSoapBinding().getName());

        classNameField.setText(props.getClassName());
        updateClassNameRadio();

        serviceNameField.setText(props.getServiceName());

        updateWSDL();
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new WebServiceReceiverProperties();
    }

    private void updateClassNameRadio() {
        if (classNameField.getText().equals(new WebServiceReceiverProperties().getClassName())) {
            classNameDefaultRadio.setSelected(true);
            classNameDefaultRadioActionPerformed();
        } else {
            classNameCustomRadio.setSelected(true);
            classNameCustomRadioActionPerformed();
        }
    }

    public void updateWSDL() {
        String server = "<server ip>";
        try {
            server = new URI(PlatformUI.SERVER_URL).getHost();
        } catch (Exception e) {
            // ignore exceptions getting the server ip
        }

        wsdlField.setText("http" + (usingHttps ? "s" : "") + "://" + server + ":" + ((WebServiceReceiverProperties) getFilledProperties()).getListenerConnectorProperties().getPort() + "/services/" + serviceNameField.getText() + "?wsdl");
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        WebServiceReceiverProperties props = (WebServiceReceiverProperties) properties;

        boolean valid = true;

        if (props.getClassName().length() == 0) {
            valid = false;
            if (highlight) {
                classNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getServiceName().length() == 0) {
            valid = false;
            if (highlight) {
                serviceNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        classNameField.setBackground(null);
        serviceNameField.setBackground(null);
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        usingHttps = connectorTypeDecoration != null;
        updateWSDL();
    }

    @Override
    public void updatedField(String field) {
        if (ListenerSettingsPanel.FIELD_PORT.equals(field)) {
            updateWSDL();
        }
    }

    private void initComponents() {
        versionLabel = new JLabel("Binding:");
        serviceNameLabel = new JLabel("Service Name:");
        methodLabel = new JLabel("Method:");
        webServiceLabel = new JLabel("Web Service:");
        classNameLabel = new JLabel("Service Class Name:");

        serviceNameField = new MirthTextField();
        serviceNameField.setToolTipText("The name to give to the web service.");
        serviceNameField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                serviceNameFieldKeyReleased(evt);
            }
        });

        methodField = new JTextField();
        methodField.setText("String acceptMessage(String message)");
        methodField.setToolTipText("Displays the generated web service operation signature the client will call.");

        versionComboBox = new MirthComboBox();
        versionComboBox.addItem(Binding.DEFAULT.getName());
        versionComboBox.addItem(Binding.SOAP11HTTP.getName());
        versionComboBox.addItem(Binding.SOAP12HTTP.getName());
        versionComboBox.setSelectedIndex(0);
        versionComboBox.setToolTipText("<html>The selected binding version defines the structure of the generated envelope.<br/>Selecting default will publish this endpoint with the specified binding annotation.<br/>If no annotation is provided, a SOAP 1.1 binding will be used.</html>");

        wsdlLabel = new JLabel("WSDL URL:");

        wsdlField = new JTextField();
        wsdlField.setToolTipText("<html>Displays the generated WSDL URL for the web service.<br>The client that sends messages to the service can download this file to determine how to call the web service.</html>");

        classNameDefaultRadio = new MirthRadioButton("Default service");
        classNameDefaultRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        classNameDefaultRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classNameDefaultRadio.setToolTipText("<html>If checked, the connector will use the DefaultAcceptMessage web service.</html>");
        classNameDefaultRadio.setMargin(new Insets(0, 0, 0, 0));
        classNameDefaultRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                classNameDefaultRadioActionPerformed();
            }
        });

        classNameCustomRadio = new MirthRadioButton("Custom service");
        classNameCustomRadio.setSelected(true);
        classNameCustomRadio.setBackground(new Color(255, 255, 255));
        classNameCustomRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classNameCustomRadio.setToolTipText("<html>If checked, the connector will use a custom web service defined below.</html>");
        classNameCustomRadio.setMargin(new Insets(0, 0, 0, 0));
        classNameCustomRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                classNameCustomRadioActionPerformed();
            }
        });

        classNameButtonGroup = new ButtonGroup();
        classNameButtonGroup.add(classNameDefaultRadio);
        classNameButtonGroup.add(classNameCustomRadio);

        classNameField = new MirthTextField();
        classNameField.setToolTipText("<html>The fully qualified class name of the web service that should be hosted.<br>If this is a custom class, it should be added in a custom jar so it is loaded with Mirth Connect.</html>");
    }

    private void initLayout() {
        setLayout(new MigLayout("novisualpadding, hidemode 3, insets 0", "[right]12[left]"));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        add(webServiceLabel);
        add(classNameDefaultRadio, "split");
        add(classNameCustomRadio, "wrap");

        add(classNameLabel);
        add(classNameField, "w 300!, wrap");

        add(serviceNameLabel);
        add(serviceNameField, "w 100!, wrap");

        add(versionLabel);
        add(versionComboBox, "wrap");

        add(wsdlLabel);
        add(wsdlField, "w 250!, wrap");

        add(methodLabel);
        add(methodField, "w 250!, wrap");
    }

    private void serviceNameFieldKeyReleased(KeyEvent evt) {
        updateWSDL();
    }

    private void classNameDefaultRadioActionPerformed() {
        classNameField.setText(new WebServiceReceiverProperties().getClassName());
        methodField.setText("String acceptMessage(String message)");
        classNameLabel.setEnabled(false);
        classNameField.setEnabled(false);
    }

    private void classNameCustomRadioActionPerformed() {
        methodField.setText("<Custom Web Service Methods>");
        classNameLabel.setEnabled(true);
        classNameField.setEnabled(true);
    }

    private JLabel versionLabel;
    private MirthComboBox versionComboBox;

    private JLabel webServiceLabel;
    private MirthRadioButton classNameDefaultRadio;
    private MirthRadioButton classNameCustomRadio;

    private JLabel classNameLabel;
    private MirthTextField classNameField;

    private JLabel serviceNameLabel;
    private MirthTextField serviceNameField;

    private JLabel wsdlLabel;
    private JTextField wsdlField;

    private ButtonGroup classNameButtonGroup;

    private JLabel methodLabel;
    private JTextField methodField;
}
