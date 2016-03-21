/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.apache.commons.collections4.MapUtils;

public class DelegateErrorTaskHandler extends ChannelTaskHandler {

    private ChannelTaskHandler delegate;
    private Map<String, Exception> errorMap;

    public DelegateErrorTaskHandler(ChannelTaskHandler delegate) {
        if (delegate == null) {
            delegate = new LoggingTaskHandler();
        }
        this.delegate = delegate;
    }

    public Map<String, Exception> getErrorMap() {
        return errorMap;
    }

    public boolean isErrored() {
        return MapUtils.isNotEmpty(errorMap);
    }

    @Override
    public void taskStarted(String channelId, Integer metaDataId) {
        delegate.taskStarted(channelId, metaDataId);
    }

    @Override
    public void taskCompleted(String channelId, Integer metaDataId) {
        delegate.taskCompleted(channelId, metaDataId);
    }

    @Override
    public void taskErrored(String channelId, Integer metaDataId, Exception e) {
        delegate.taskErrored(channelId, metaDataId, e);
        if (errorMap == null) {
            errorMap = new HashMap<String, Exception>();
        }
        errorMap.put(channelId, e);
    }

    @Override
    public void taskCancelled(String channelId, Integer metaDataId, CancellationException e) {
        delegate.taskCancelled(channelId, metaDataId, e);
        if (errorMap == null) {
            errorMap = new HashMap<String, Exception>();
        }
        errorMap.put(channelId, e);
    }
}