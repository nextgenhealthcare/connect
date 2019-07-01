/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class ChannelDoesNotExistException extends DonkeyDaoException {

    private Set<String> channelIds;

    public ChannelDoesNotExistException(String channelId) {
        this(Collections.singleton(channelId));
    }

    public ChannelDoesNotExistException(Set<String> channelIds) {
        super("The following channel IDs do not exist: " + StringUtils.join(channelIds, ", "));
        this.channelIds = channelIds;
    }

    public Set<String> getChannelIds() {
        return channelIds;
    }
}