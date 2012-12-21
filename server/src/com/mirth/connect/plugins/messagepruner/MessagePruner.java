package com.mirth.connect.plugins.messagepruner;

import java.util.Calendar;

import com.mirth.connect.server.controllers.MessagePrunerException;

public interface MessagePruner {
    public int[] executePruner(String channelId, Calendar messageDateThreshold, Calendar contentDateThreshold) throws MessagePrunerException;
}
