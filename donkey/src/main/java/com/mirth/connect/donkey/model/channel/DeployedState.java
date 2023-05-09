/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

public enum DeployedState {
    UNDEPLOYED, DEPLOYING, UNDEPLOYING, STARTING, STARTED, PAUSING, PAUSED, STOPPING, STOPPED, SYNCING, UNKNOWN;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString());
    }

    public static DeployedState fromString(String value) {
        for (DeployedState state : values()) {
            if (StringUtils.equalsIgnoreCase(state.toString(), value)) {
                return state;
            }
        }
        return null;
    }
}
