/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.mllpmode;

import com.mirth.connect.plugins.TransmissionModeClientProvider;
import com.mirth.connect.plugins.TransmissionModePlugin;

public class MLLPModePlugin extends TransmissionModePlugin {

    public MLLPModePlugin(String pluginName) {
        super(pluginName);
    }

    @Override
    public String getPluginPointName() {
        return MLLPModeProperties.PLUGIN_POINT;
    }

    @Override
    public TransmissionModeClientProvider createProvider() {
        return new MLLPModeClientProvider();
    }
}