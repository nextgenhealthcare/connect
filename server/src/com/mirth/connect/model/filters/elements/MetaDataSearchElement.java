/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.filters.elements;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("metaDataSearchCriteria")
public class MetaDataSearchElement implements Serializable {

    private String columnName;
    private String operator;
    private Object value;
    private Boolean ignoreCase;

    public MetaDataSearchElement(String columnName, String operator, Object value, Boolean ignoreCase) {
        this.setColumnName(columnName);
        this.setOperator(operator);
        this.setValue(value);
        this.setIgnoreCase(ignoreCase);
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Boolean getIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(Boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

}
