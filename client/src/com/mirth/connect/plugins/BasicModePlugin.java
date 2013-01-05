/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.mirth.connect.client.ui.editors.BasicModeSettingsDialog;
import com.mirth.connect.client.ui.editors.BasicModeSettingsPanel;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.framemode.FrameModeProperties;

public class BasicModePlugin extends FrameTransmissionModePlugin implements ActionListener {
    
    public static final String CHANGE_START_BYTES_COMMAND = "changeStartBytes";
    public static final String CHANGE_END_BYTES_COMMAND = "changeEndBytes";
    
    protected BasicModeSettingsPanel settingsPanel;
    private BasicModeSettingsDialog settingsDialog;
    private FrameModeProperties frameModeProperties;

    public BasicModePlugin() {
        super("Basic");
    }

    @Override
    public void initialize(ActionListener actionListener) {
        super.initialize(actionListener);
        settingsPanel = new BasicModeSettingsPanel(this);
        super.settingsPanel.switchComponent(settingsPanel);
        settingsDialog = new BasicModeSettingsDialog(this);
        setProperties(new FrameModeProperties(getPluginPointName()));
    }
    
    @Override
    public TransmissionModeProperties getProperties() {
        FrameModeProperties frameModeProperties = (FrameModeProperties) super.getProperties();
        frameModeProperties.setStartOfMessageBytes(frameModeProperties.getStartOfMessageBytes());
        frameModeProperties.setEndOfMessageBytes(frameModeProperties.getEndOfMessageBytes());
        return frameModeProperties;
    }
    
    @Override
    public TransmissionModeProperties getDefaultProperties() {
        return new FrameModeProperties(getPluginPointName());
    }
    
    @Override
    public void setProperties(TransmissionModeProperties properties) {
        super.setProperties(properties);
        settingsDialog.setProperties(frameModeProperties = (FrameModeProperties) properties);
        changeSampleValue();
    }
    
    @Override
    public JComponent getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public String getPluginPointName() {
        return "Basic";
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(CHANGE_START_BYTES_COMMAND)) {
            super.settingsPanel.startOfMessageBytesField.setText(((JTextField) e.getSource()).getText());
        } else if (e.getActionCommand().equals(CHANGE_END_BYTES_COMMAND)) {
            super.settingsPanel.endOfMessageBytesField.setText(((JTextField) e.getSource()).getText());
        } else {
            settingsDialog.setProperties(getProperties());
            String oldStartBytes = frameModeProperties.getStartOfMessageBytes();
            String oldEndBytes = frameModeProperties.getEndOfMessageBytes();
            
            settingsDialog.setVisible(true);
            if (settingsDialog.isSaved()) {
                setProperties(settingsDialog.getProperties());
            } else {
                super.settingsPanel.startOfMessageBytesField.setText(oldStartBytes);
                super.settingsPanel.endOfMessageBytesField.setText(oldEndBytes);
            }
        }
    }
}
