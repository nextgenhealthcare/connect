/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class MirthApiException extends WebApplicationException {

    public MirthApiException() {}

    public MirthApiException(Status status) {
        super(status);
    }

    public MirthApiException(String message) {
        super(message);
    }

    public MirthApiException(Throwable cause) {
        super(Response.serverError().type(MediaType.APPLICATION_XML_TYPE).entity(cause).build());
    }

    public MirthApiException(Response response) {
        super(response);
    }
}