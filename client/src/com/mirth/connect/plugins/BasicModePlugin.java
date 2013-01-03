/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.awt.event.ActionListener;

import com.mirth.connect.model.transmission.framemode.FrameModeProperties;

public class BasicModePlugin extends FrameTransmissionModePlugin {

    public BasicModePlugin() {
        super("Basic");
    }

    @Override
    public void initialize(ActionListener actionListener) {
        super.initialize(actionListener);
        setProperties(new FrameModeProperties(getPluginPointName()));
    }

    @Override
    public String getPluginPointName() {
        return "Basic";
    }
}
