/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.mllpmode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
import com.mirth.connect.plugins.FrameTransmissionModePlugin;

public class MLLPModePlugin extends FrameTransmissionModePlugin implements ActionListener {
    
    static final String CHANGE_START_BYTES_COMMAND = "changeStartBytes";
    static final String CHANGE_END_BYTES_COMMAND = "changeEndBytes";
    
    protected MLLPModeSettingsPanel settingsPanel;
    private MLLPModeSettingsDialog settingsDialog;
    private MLLPModeProperties mllpModeProperties;

    public MLLPModePlugin(String name) {
        this();
    }

    public MLLPModePlugin() {
        super(MLLPModeProperties.PLUGIN_POINT);
    }

    @Override
    public void initialize(ActionListener actionListener) {
        super.initialize(actionListener);
        settingsPanel = new MLLPModeSettingsPanel(this);
        super.settingsPanel.switchComponent(settingsPanel);
        settingsDialog = new MLLPModeSettingsDialog(this);
        setProperties(new MLLPModeProperties());
    }
    
    @Override
    public TransmissionModeProperties getProperties() {
        FrameModeProperties frameModeProperties = (FrameModeProperties) super.getProperties();
        mllpModeProperties.setStartOfMessageBytes(frameModeProperties.getStartOfMessageBytes());
        mllpModeProperties.setEndOfMessageBytes(frameModeProperties.getEndOfMessageBytes());
        return mllpModeProperties;
    }

    @Override
    public TransmissionModeProperties getDefaultProperties() {
        return new MLLPModeProperties();
    }
    
    @Override
    public void setProperties(TransmissionModeProperties properties) {
        super.setProperties(properties);
        
        if (properties instanceof MLLPModeProperties) {
            mllpModeProperties = (MLLPModeProperties) properties;
        } else {
            mllpModeProperties = new MLLPModeProperties();
            FrameModeProperties frameModeProperties = (FrameModeProperties) properties;
            mllpModeProperties.setStartOfMessageBytes(frameModeProperties.getStartOfMessageBytes());
            mllpModeProperties.setEndOfMessageBytes(frameModeProperties.getEndOfMessageBytes());
        }
        
        settingsDialog.setProperties(mllpModeProperties);
        changeSampleValue();
    }
    
    @Override
    public JComponent getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public String getSampleLabel() {
        return "MLLP Sample Frame:";
    }

    @Override
    public String getPluginPointName() {
        return MLLPModeProperties.PLUGIN_POINT;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(CHANGE_START_BYTES_COMMAND)) {
            super.settingsPanel.startOfMessageBytesField.setText(((JTextField) e.getSource()).getText());
        } else if (e.getActionCommand().equals(CHANGE_END_BYTES_COMMAND)) {
            super.settingsPanel.endOfMessageBytesField.setText(((JTextField) e.getSource()).getText());
        } else {
            settingsDialog.setProperties(getProperties());
            String oldStartBytes = mllpModeProperties.getStartOfMessageBytes();
            String oldEndBytes = mllpModeProperties.getEndOfMessageBytes();
            
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
