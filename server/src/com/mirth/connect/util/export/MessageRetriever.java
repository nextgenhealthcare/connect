/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util.export;

import java.util.List;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.model.filters.MessageFilter;

public interface MessageRetriever {
    public List<Message> getMessages(String channelId, MessageFilter filter, boolean includeContent, Integer offset, Integer limit) throws Exception;
}
