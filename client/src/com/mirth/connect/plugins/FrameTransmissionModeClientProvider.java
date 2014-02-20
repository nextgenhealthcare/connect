/*
 * Copyright (c) Mirth Corporation. All rights reserved. http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.awt.event.ActionListener;

import javax.swing.JComponent;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.editors.FrameModeSettingsPanel;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
import com.mirth.connect.util.TcpUtil;

public abstract class FrameTransmissionModeClientProvider extends TransmissionModeClientProvider {

    private static final String DEFAULT_SETTINGS_TOOLTIP = "<html>Enter the bytes to send before the beginning and after the end of the actual message.<br/>An even number of characters must be entered, and only the characters 0-9 and A-F are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>";

    private String settingsToolTipText;

    protected FrameModeSettingsPanel settingsPanel;

    @Override
    public void initialize(ActionListener actionListener) {
        super.initialize(actionListener);
        settingsPanel = new FrameModeSettingsPanel(this);
        settingsToolTipText = DEFAULT_SETTINGS_TOOLTIP;
    }

    @Override
    public TransmissionModeProperties getProperties() {
        FrameModeProperties properties = new FrameModeProperties();

        properties.setStartOfMessageBytes(settingsPanel.startOfMessageBytesField.getText());
        properties.setEndOfMessageBytes(settingsPanel.endOfMessageBytesField.getText());

        return properties;
    }

    @Override
    public TransmissionModeProperties getDefaultProperties() {
        return new FrameModeProperties();
    }

    @Override
    public void setProperties(TransmissionModeProperties properties) {
        FrameModeProperties props = (FrameModeProperties) properties;
        settingsPanel.startOfMessageBytesField.getDocument().removeDocumentListener(this);
        settingsPanel.endOfMessageBytesField.getDocument().removeDocumentListener(this);

        settingsPanel.startOfMessageBytesField.setText(props.getStartOfMessageBytes());
        settingsPanel.endOfMessageBytesField.setText(props.getEndOfMessageBytes());
        changeSampleValue();

        settingsPanel.startOfMessageBytesField.getDocument().addDocumentListener(this);
        settingsPanel.endOfMessageBytesField.getDocument().addDocumentListener(this);
    }

    @Override
    public boolean checkProperties(TransmissionModeProperties properties, boolean highlight) {
        boolean valid = true;
        FrameModeProperties props = (FrameModeProperties) properties;

        if (!TcpUtil.isValidHexString(props.getStartOfMessageBytes())) {
            valid = false;
            if (highlight) {
                settingsPanel.startOfMessageBytesField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (!TcpUtil.isValidHexString(props.getEndOfMessageBytes())) {
            valid = false;
            if (highlight) {
                settingsPanel.endOfMessageBytesField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        settingsPanel.startOfMessageBytesField.setBackground(null);
        settingsPanel.endOfMessageBytesField.setBackground(null);
    }

    @Override
    public JComponent getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public String getSampleLabel() {
        return "Sample Frame:";
    }

    @Override
    public String getSampleValue() {
        String startOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(settingsPanel.startOfMessageBytesField.getText());
        String endOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(settingsPanel.endOfMessageBytesField.getText());

        String startReplaced = startOfMessageAbbreviation.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
        String endReplaced = endOfMessageAbbreviation.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
        String newSettingsToolTipText = settingsToolTipText.replace("SOM", startReplaced).replace("EOM", endReplaced);

        settingsPanel.startOfMessageBytes0XLabel.setToolTipText(newSettingsToolTipText);
        settingsPanel.startOfMessageBytesField.setToolTipText(newSettingsToolTipText);
        settingsPanel.messageDataLabel.setToolTipText(newSettingsToolTipText);
        settingsPanel.endOfMessageBytes0XLabel.setToolTipText(newSettingsToolTipText);
        settingsPanel.endOfMessageBytesField.setToolTipText(newSettingsToolTipText);

        return ("<html><b>" + startReplaced + "</b> <i>&lt;Message Data&gt;</i> <b>" + endReplaced + "</b></html>").trim();
    }
}
