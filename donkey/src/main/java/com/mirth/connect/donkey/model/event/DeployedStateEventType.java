/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.event;

import org.apache.commons.lang3.text.WordUtils;

import com.mirth.connect.donkey.model.channel.DeployedState;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("deployedStateEventType")
public enum DeployedStateEventType {
    DEPLOYED, UNDEPLOYED, STARTING, STARTED, PAUSING, PAUSED, STOPPING, STOPPED;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString().replace("_", " "));
    }

    public static DeployedStateEventType getTypeFromDeployedState(DeployedState state) {
        switch (state) {
            case STARTING:
                return DeployedStateEventType.STARTING;
            case STARTED:
                return DeployedStateEventType.STARTED;
            case PAUSING:
                return DeployedStateEventType.PAUSING;
            case PAUSED:
                return DeployedStateEventType.PAUSED;
            case STOPPING:
                return DeployedStateEventType.STOPPING;
            case STOPPED:
                return DeployedStateEventType.STOPPED;
        }

        return null;
    }
}
