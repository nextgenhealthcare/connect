/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

import java.io.Serializable;

public class QueueConnectorProperties implements Serializable {
    private boolean queueEnabled;
    private boolean sendFirst;
    private int retryIntervalMillis;
    private boolean regenerateTemplate;
    private int retryCount;

    public QueueConnectorProperties() {
        queueEnabled = false;
        sendFirst = false;
        retryIntervalMillis = 1000;
        regenerateTemplate = false;
        retryCount = 0;
    }

    public boolean isQueueEnabled() {
        return queueEnabled;
    }

    public void setQueueEnabled(boolean enabled) {
        this.queueEnabled = enabled;
    }

    public boolean isSendFirst() {
        return sendFirst;
    }

    public void setSendFirst(boolean sendFirst) {
        this.sendFirst = sendFirst;
    }

    public int getRetryIntervalMillis() {
        return retryIntervalMillis;
    }

    public void setRetryIntervalMillis(int retryIntervalMillis) {
        this.retryIntervalMillis = retryIntervalMillis;
    }

    public boolean isRegenerateTemplate() {
        return regenerateTemplate;
    }

    public void setRegenerateTemplate(boolean regenerateTemplate) {
        this.regenerateTemplate = regenerateTemplate;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
