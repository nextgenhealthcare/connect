/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.transmission;

import java.io.Serializable;

public class TransmissionModeProperties implements Serializable {

    private String pluginPointName;

    public TransmissionModeProperties(String pluginPointName) {
        this.pluginPointName = pluginPointName;
    }

    public String getPluginPointName() {
        return pluginPointName;
    }

    public void setPluginPointName(String pluginPointName) {
        this.pluginPointName = pluginPointName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TransmissionModeProperties) {
            if (((TransmissionModeProperties) obj).getPluginPointName().equals(pluginPointName)) {
                return true;
            }
        }
        return false;
    }
}
