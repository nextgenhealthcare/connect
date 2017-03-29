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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

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
                value = value.replaceAll("^\\s+", "");
                int spaceIndex = StringUtils.indexOf(value, ' ');

                if (spaceIndex > 0) {
                    String columnName = StringUtils.trim(StringUtils.substring(value, 0, spaceIndex));
                    value = StringUtils.substring(value, spaceIndex).replaceAll("^\\s+", "");

                    // Sort operators by longest first so "<" doesn't match incorrectly on "<=", etc.
                    List<MetaDataSearchOperator> operators = new ArrayList<MetaDataSearchOperator>(Arrays.asList(MetaDataSearchOperator.values()));
                    Collections.sort(operators, new Comparator<MetaDataSearchOperator>() {
                        @Override
                        public int compare(MetaDataSearchOperator o1, MetaDataSearchOperator o2) {
                            int diff = o2.toString().length() - o1.toString().length();
                            return diff == 0 ? o2.toString().compareTo(o1.toString()) : diff;
                        }
                    });

                    MetaDataSearchOperator operator = null;
                    for (MetaDataSearchOperator op : operators) {
                        if (StringUtils.startsWithIgnoreCase(value, op.toString() + " ")) {
                            operator = op;
                            value = StringUtils.removeStartIgnoreCase(value, op.toString() + " ");
                            break;
                        }
                    }

                    if (StringUtils.isNotBlank(columnName) && operator != null) {
                        return new MetaDataSearch(columnName, operator, value);
                    }
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return new StringBuilder(StringUtils.trim(columnName)).append(' ').append(operator).append(' ').append(value).toString();
        }
    }
}