/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.connectors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.donkey.model.channel.ListenerConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorPropertiesInterface;

import net.miginfocom.swing.MigLayout;

public class ListenerSettingsPanel extends JPanel {

    public static final String FIELD_PORT = ListenerSettingsPanel.class.getSimpleName() + ".PORT";

    public ListenerSettingsPanel() {
        initComponents();
        initLayout();
    }

    public void setProperties(ListenerConnectorPropertiesInterface propertiesInterface) {
        ListenerConnectorProperties properties = propertiesInterface.getListenerConnectorProperties();

        addressField.setText(properties.getHost());
        updateAddressRadio();

        portField.setText(properties.getPort());
    }

    public void fillProperties(ListenerConnectorPropertiesInterface propertiesInterface) {
        ListenerConnectorProperties properties = propertiesInterface.getListenerConnectorProperties();

        properties.setHost(addressField.getText());
        properties.setPort(portField.getText());
    }

    public boolean checkProperties(ListenerConnectorPropertiesInterface propertiesInterface, boolean highlight) {
        ListenerConnectorProperties properties = propertiesInterface.getListenerConnectorProperties();

        boolean valid = true;

        if (properties.getHost().length() == 0) {
            valid = false;
            if (highlight) {
                addressField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (properties.getPort().length() == 0) {
            valid = false;
            if (highlight) {
                portField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    public void resetInvalidProperties() {
        addressField.setBackground(null);
        portField.setBackground(null);
    }

    private void updateAddressRadio() {
        if (addressField.getText().equals(new ListenerConnectorProperties(null).getHost())) {
            allRadio.setSelected(true);
            allRadioActionPerformed(null);
        } else {
            specificRadio.setSelected(true);
            specificRadioActionPerformed(null);
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        listenerButtonGroup = new ButtonGroup();
        addressLabel = new JLabel();
        allRadio = new MirthRadioButton();
        specificRadio = new MirthRadioButton();
        addressField = new MirthTextField();
        portLabel = new JLabel();
        portField = new MirthTextField();
        portsInUse = new JButton();

        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Listener Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        addressLabel.setText("Local Address:");

        allRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        allRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listenerButtonGroup.add(allRadio);
        allRadio.setText("All interfaces");
        allRadio.setToolTipText("<html>If checked, the connector will listen on all interfaces, using address 0.0.0.0.</html>");
        allRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        allRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allRadioActionPerformed(evt);
            }
        });

        specificRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        specificRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listenerButtonGroup.add(specificRadio);
        specificRadio.setText("Specific interface:");
        specificRadio.setToolTipText("<html>If checked, the connector will listen on the specific interface address defined.</html>");
        specificRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        specificRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                specificRadioActionPerformed(evt);
            }
        });

        addressField.setToolTipText("The DNS domain name or IP address on which the web service should listen for connections.");

        portLabel.setText("Local Port:");

        portField.setToolTipText("The port on which the web service should listen for connections.");
        portField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                portFieldKeyReleased(evt);
            }
        });
        
        portsInUse.setText("Ports in Use");
        portsInUse.setToolTipText("View all ports currently used by Mirth Connect.");
        portsInUse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                portsInUseActionPerformed(evt);
            }

        });

    }
    
    private void initLayout() {        
    	JPanel listenerSettingsLayout = new JPanel(new MigLayout("novisualpadding, insets 0 0 0 320", "[right]12[fill]12[left, fill]"));
    	listenerSettingsLayout.setBackground(UIConstants.BACKGROUND_COLOR);
    	listenerSettingsLayout.add(addressLabel);
    	listenerSettingsLayout.add(allRadio);
    	listenerSettingsLayout.add(specificRadio);
    	listenerSettingsLayout.add(addressField, "width 150");
    	listenerSettingsLayout.add(portLabel, "newline");
    	listenerSettingsLayout.add(portField, "grow");
    	listenerSettingsLayout.add(portsInUse);
    	
    	add(listenerSettingsLayout);
    	
//        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
//        this.setLayout(layout);
//        layout.setHorizontalGroup(
//            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
//                    .addComponent(portLabel)
//                    .addComponent(addressLabel))
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                    .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
//                    .addGroup(layout.createSequentialGroup()
//                        .addComponent(allRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(specificRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))
//                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        layout.setVerticalGroup(
//            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
//                    .addComponent(addressLabel)
//                    .addComponent(allRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//                    .addComponent(specificRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//                    .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
//                    .addComponent(portLabel)
//                    .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
//        );
//
//        getAccessibleContext().setAccessibleName("Listener Settings");
    }
    
    private void allRadioActionPerformed(java.awt.event.ActionEvent evt) {
        addressField.setText(new ListenerConnectorProperties(null).getHost());
        addressField.setEnabled(false);
    }

    private void specificRadioActionPerformed(java.awt.event.ActionEvent evt) {
        addressField.setEnabled(true);
    }

    private void portFieldKeyReleased(java.awt.event.KeyEvent evt) {
        ((ConnectorPanel) getParent()).updatedField(FIELD_PORT);
    }

	private void portsInUseActionPerformed(ActionEvent evt) {
		// TODO Auto-generated method stub
		
	}

    private MirthTextField addressField;
    private JLabel addressLabel;
    private MirthRadioButton allRadio;
    private ButtonGroup listenerButtonGroup;
    private MirthTextField portField;
    private JLabel portLabel;
    private MirthRadioButton specificRadio;
    private JButton portsInUse;
}
