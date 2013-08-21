/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.event;

import org.apache.commons.lang3.text.WordUtils;

import com.mirth.connect.donkey.model.channel.ChannelState;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("channelEventType")
public enum ChannelEventType {
    DEPLOY, UNDEPLOY, STARTING, STARTED, PAUSING, PAUSED, STOPPING, STOPPED;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString().replace("_", " "));
    }

    public static ChannelEventType getTypeFromChannelState(ChannelState state) {
        switch (state) {
            case STARTING:
                return ChannelEventType.STARTING;
            case STARTED:
                return ChannelEventType.STARTED;
            case PAUSING:
                return ChannelEventType.PAUSING;
            case PAUSED:
                return ChannelEventType.PAUSED;
            case STOPPING:
                return ChannelEventType.STOPPING;
            case STOPPED:
                return ChannelEventType.STOPPED;
        }

        return null;
    }
}
