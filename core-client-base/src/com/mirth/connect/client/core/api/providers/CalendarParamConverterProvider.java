/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Singleton;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

@Provider
@Singleton
public class CalendarParamConverterProvider implements ParamConverterProvider {

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.getName().equals(Calendar.class.getName())) {
            return new ParamConverter<T>() {
                @Override
                public T fromString(String value) {
                    if (value == null) {
                        return null;
                    }

                    try {
                        Calendar date = Calendar.getInstance();
                        date.setTime(format.parse(value));
                        return (T) date;
                    } catch (ParseException e) {
                        throw new ProcessingException(e);
                    }
                }

                @Override
                public String toString(T value) {
                    if (value == null) {
                        return null;
                    }
                    return format.format(((Calendar) value).getTime());
                }
            };
        }

        return null;
    }
}