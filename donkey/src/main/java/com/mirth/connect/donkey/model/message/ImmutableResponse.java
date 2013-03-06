/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

public class ImmutableResponse {
	private Response response;
	
    public ImmutableResponse(Response response) {
        this.response = response;
    }
    
	public String getMessage() {
		return response.getMessage();
	}
	    
	public Status getNewMessageStatus() {
		return response.getNewMessageStatus();
	}

	public String getError() {
		return response.getError();
	}
}
