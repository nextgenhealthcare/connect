/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
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

public class BasicModeClientProvider extends FrameTransmissionModeClientProvider {

    public static final String CHANGE_START_BYTES_COMMAND = "changeStartBytes";
    public static final String CHANGE_END_BYTES_COMMAND = "changeEndBytes";

    protected BasicModeSettingsPanel settingsPanel;
    private FrameModeProperties frameModeProperties;

    @Override
    public void initialize(ActionListener actionListener) {
        super.initialize(actionListener);
        settingsPanel = new BasicModeSettingsPanel(this);
        super.settingsPanel.switchComponent(settingsPanel);
        setProperties(new FrameModeProperties());
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
        return new FrameModeProperties();
    }

    @Override
    public void setProperties(TransmissionModeProperties properties) {
        super.setProperties(properties);
        frameModeProperties = (FrameModeProperties) properties;
        changeSampleValue();
    }

    @Override
    public JComponent getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(CHANGE_START_BYTES_COMMAND)) {
            super.settingsPanel.startOfMessageBytesField.setText(((JTextField) e.getSource()).getText());
        } else if (e.getActionCommand().equals(CHANGE_END_BYTES_COMMAND)) {
            super.settingsPanel.endOfMessageBytesField.setText(((JTextField) e.getSource()).getText());
        } else {
            BasicModeSettingsDialog settingsDialog = new BasicModeSettingsDialog(this);
            settingsDialog.setProperties(frameModeProperties);
            settingsDialog.setVisible(true);

            if (settingsDialog.isSaved()) {
                setProperties(settingsDialog.getProperties());
            } else {
                setProperties(frameModeProperties);
            }
        }
    }
}
