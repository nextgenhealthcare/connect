/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;

public class QueueConnectorProperties implements Serializable, Migratable {
    private boolean queueEnabled;
    private boolean sendFirst;
    private int retryIntervalMillis;
    private boolean regenerateTemplate;
    private int retryCount;
    private boolean rotate;
    private int threadCount;

    public QueueConnectorProperties() {
        queueEnabled = false;
        sendFirst = false;
        retryIntervalMillis = 10000;
        regenerateTemplate = false;
        retryCount = 0;
        rotate = false;
        threadCount = 1;
    }
    
    public QueueConnectorProperties(QueueConnectorProperties props) {
        queueEnabled = props.isQueueEnabled();
        sendFirst = props.isSendFirst();
        retryIntervalMillis = props.getRetryIntervalMillis();
        regenerateTemplate = props.isRegenerateTemplate();
        retryCount = props.getRetryCount();
        rotate = props.isRotate();
        threadCount = props.getThreadCount();
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

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}
}
