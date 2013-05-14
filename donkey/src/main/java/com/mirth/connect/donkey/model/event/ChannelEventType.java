package com.mirth.connect.donkey.model.event;

import org.apache.commons.lang.WordUtils;

import com.mirth.connect.donkey.model.channel.ChannelState;

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
