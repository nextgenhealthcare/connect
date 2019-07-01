/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.providers;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class ResponseCodeFilter implements ContainerResponseFilter {

    public static final String RESPONSE_CODE_PROPERTY = "responseCode";

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        Object responseCodeObj = containerRequestContext.getProperty(RESPONSE_CODE_PROPERTY);

        if (responseCodeObj != null && responseCodeObj instanceof Integer) {
            containerResponseContext.setStatus((Integer) responseCodeObj);
        }
    }
}
