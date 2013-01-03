/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.mllpmode;

import java.awt.event.ActionListener;

import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.plugins.FrameTransmissionModePlugin;

public class MLLPModePlugin extends FrameTransmissionModePlugin {

    public MLLPModePlugin(String name) {
        this();
    }

    public MLLPModePlugin() {
        super(MLLPModeProperties.PLUGIN_POINT);
    }

    @Override
    public void initialize(ActionListener actionListener) {
        super.initialize(actionListener);
        setProperties(new MLLPModeProperties());
    }

    @Override
    public TransmissionModeProperties getDefaultProperties() {
        return new MLLPModeProperties();
    }

    @Override
    public boolean checkProperties(TransmissionModeProperties properties, boolean highlight) {
        return true;
    }

    @Override
    public void resetInvalidProperties() {}

    @Override
    public String getSampleLabel() {
        return "MLLP Sample Frame:";
    }

    @Override
    public String getPluginPointName() {
        return MLLPModeProperties.PLUGIN_POINT;
    }
}
