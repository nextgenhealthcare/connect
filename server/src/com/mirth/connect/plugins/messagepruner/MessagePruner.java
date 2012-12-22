/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagepruner;

import java.util.Calendar;

import com.mirth.connect.server.controllers.MessagePrunerException;

public interface MessagePruner {
    public int[] executePruner(String channelId, Calendar messageDateThreshold, Calendar contentDateThreshold) throws MessagePrunerException;
}
