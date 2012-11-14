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

public class ResponseConnectorProperties implements Serializable {
    private String responseVariable;
    private String[] defaultResponses;
    private boolean respondAfterProcessing;

    public ResponseConnectorProperties(String defaultResponse, String[] defaultResponses) {
        this.responseVariable = defaultResponse;
        this.defaultResponses = defaultResponses;
        this.setRespondAfterProcessing(true);
    }

    public String getResponseVariable() {
        return responseVariable;
    }

    public void setResponseVariable(String responseVariable) {
        this.responseVariable = responseVariable;
    }

    public String[] getDefaultResponses() {
        return defaultResponses;
    }

	public boolean isRespondAfterProcessing() {
		return respondAfterProcessing;
	}

	public void setRespondAfterProcessing(boolean respondAfterProcessing) {
		this.respondAfterProcessing = respondAfterProcessing;
	}

}
