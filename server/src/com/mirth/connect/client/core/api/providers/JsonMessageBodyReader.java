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

import com.mirth.connect.model.converters.ObjectJSONSerializer;

@Provider
@Singleton
@Consumes(MediaType.APPLICATION_JSON)
public class JsonMessageBodyReader implements MessageBodyReader<Object> {

    protected ObjectJSONSerializer getObjectJsonSerializer() {
        return ObjectJSONSerializer.getInstance();
    }
    
    @Override
    public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
        return true;
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        // If the type is List, call deserializeList instead
        if (type.equals(List.class) && genericType instanceof ParameterizedType) {
            Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
            if (ArrayUtils.isNotEmpty(actualTypes) && actualTypes[0] instanceof Class) {
                return getObjectJsonSerializer().deserializeList(IOUtils.toString(entityStream, "UTF-8"), (Class<?>) actualTypes[0]);
            }
        }
        return getObjectJsonSerializer().deserialize(IOUtils.toString(entityStream, "UTF-8"), type);
    }
}
