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

import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import com.mirth.connect.model.filters.elements.MetaDataSearchOperator;

@Provider
@Singleton
public class MetaDataSearchParamConverterProvider implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.getName().equals(MetaDataSearch.class.getName())) {
            return new ParamConverter<T>() {
                @Override
                public T fromString(String value) {
                    return (T) MetaDataSearch.fromString(value);
                }

                @Override
                public String toString(T value) {
                    return value != null ? value.toString() : null;
                }
            };
        }

        return null;
    }

    public static class MetaDataSearch {

        private String columnName;
        private MetaDataSearchOperator operator;
        private String value;

        public MetaDataSearch() {}

        public MetaDataSearch(String columnName, MetaDataSearchOperator operator, String value) {
            this.columnName = columnName;
            this.operator = operator;
            this.value = value;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public MetaDataSearchOperator getOperator() {
            return operator;
        }

        public void setOperator(MetaDataSearchOperator operator) {
            this.operator = operator;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public static MetaDataSearch fromString(String value) {
            return valueOf(value);
        }

        public static MetaDataSearch valueOf(String value) {
            if (value != null) {
                for (MetaDataSearchOperator operator : MetaDataSearchOperator.values()) {
                    String operatorValue = operator.toString();
                    int index = value.indexOf(operatorValue);
                    if (index > 0) {
                        return new MetaDataSearch(value.substring(0, index).trim(), operator, value.substring(index + operatorValue.length()).trim());
                    }
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return new StringBuilder(columnName).append(' ').append(operator).append(' ').append(value).toString();
        }
    }
}