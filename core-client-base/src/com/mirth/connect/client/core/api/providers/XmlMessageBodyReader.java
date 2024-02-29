/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.providers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.mirth.connect.model.converters.ObjectXMLSerializer;

@Provider
@Singleton
@Consumes(MediaType.APPLICATION_XML)
public class XmlMessageBodyReader implements MessageBodyReader<Object> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        // If the type is List, call deserializeList instead
        if (type.equals(List.class) && genericType instanceof ParameterizedType) {
            Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
            if (ArrayUtils.isNotEmpty(actualTypes) && actualTypes[0] instanceof Class) {
                return ObjectXMLSerializer.getInstance().deserializeList(IOUtils.toString(entityStream, "UTF-8"), (Class<?>) actualTypes[0]);
            }
        }
        return ObjectXMLSerializer.getInstance().deserialize(IOUtils.toString(entityStream, "UTF-8"), type);
    }
}