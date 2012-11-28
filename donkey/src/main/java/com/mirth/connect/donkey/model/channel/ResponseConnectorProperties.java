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

import org.apache.commons.lang3.ArrayUtils;

import com.mirth.connect.donkey.server.Constants;

public class ResponseConnectorProperties implements Serializable {

    public static final String[] DEFAULT_QUEUE_ON_RESPONSES = new String[] { Constants.RESPONSE_NONE, Constants.RESPONSE_AUTO_BEFORE };
    public static final String[] DEFAULT_QUEUE_OFF_RESPONSES = ArrayUtils.addAll(DEFAULT_QUEUE_ON_RESPONSES, Constants.RESPONSE_SOURCE_TRANSFORMED, Constants.RESPONSE_DESTINATIONS_COMPLETED, Constants.RESPONSE_POST_PROCESSOR);

    private String responseVariable;
    private String[] defaultQueueOnResponses;
    private String[] defaultQueueOffResponses;
    private boolean respondAfterProcessing;

    public ResponseConnectorProperties() {
        this(true);
    }

    public ResponseConnectorProperties(boolean autoResponseEnabled) {
        this(autoResponseEnabled, Constants.RESPONSE_NONE, DEFAULT_QUEUE_ON_RESPONSES, DEFAULT_QUEUE_OFF_RESPONSES);
    }

    private ResponseConnectorProperties(boolean autoResponseEnabled, String defaultResponse, String[] defaultQueueOnResponses, String[] defaultQueueOffResponses) {
        if (autoResponseEnabled) {
            this.responseVariable = defaultResponse;
            this.defaultQueueOnResponses = defaultQueueOnResponses;
            this.defaultQueueOffResponses = defaultQueueOffResponses;
        } else {
            this.responseVariable = Constants.RESPONSE_NONE;
            this.defaultQueueOnResponses = new String[] { Constants.RESPONSE_NONE };
            this.defaultQueueOffResponses = new String[] { Constants.RESPONSE_NONE };
        }
        this.setRespondAfterProcessing(true);
    }

    public String getResponseVariable() {
        return responseVariable;
    }

    public void setResponseVariable(String responseVariable) {
        this.responseVariable = responseVariable;
    }

    public String[] getDefaultQueueOnResponses() {
        return defaultQueueOnResponses;
    }

    public String[] getDefaultQueueOffResponses() {
        return defaultQueueOffResponses;
    }

    public boolean isRespondAfterProcessing() {
        return respondAfterProcessing;
    }

    public void setRespondAfterProcessing(boolean respondAfterProcessing) {
        this.respondAfterProcessing = respondAfterProcessing;
    }

}
