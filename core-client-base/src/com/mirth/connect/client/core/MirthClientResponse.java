/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.internal.PropertiesDelegate;

public class MirthClientResponse extends ClientResponse {

    public MirthClientResponse(ClientRequest requestContext, Response response) {
        super(requestContext, response);
    }

    public MirthClientResponse(Response.StatusType status, ClientRequest requestContext) {
        super(status, requestContext);
    }

    public MirthClientResponse(Response.StatusType status, ClientRequest requestContext, URI resolvedRequestUri) {
        super(status, requestContext, resolvedRequestUri);
    }

    @Override
    public <T> T readEntity(Class<T> rawType, Type type, Annotation[] annotations, PropertiesDelegate propertiesDelegate) {
        // Always call the super method to allow the entity to be read/closed if needed
        T t = super.readEntity(rawType, type, annotations, propertiesDelegate);

        if (getStatus() == Status.NO_CONTENT.getStatusCode()) {
            return null;
        }

        return t;
    }
}