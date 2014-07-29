/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import com.mirth.connect.model.datatype.PropertyEditorType;

/**
 * This descriptor is used by the properties table renderer and editor
 */
public class DataTypeNodeDescriptor {

    private Object value;
    private PropertyEditorType editorType;
    private boolean multipleValues;
    private Object[] options;

    public DataTypeNodeDescriptor(Object value, PropertyEditorType editorType, boolean multipleValues, Object[] options) {
        this.value = value;
        this.editorType = editorType;
        this.multipleValues = multipleValues;
        this.options = options;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public PropertyEditorType getEditorType() {
        return editorType;
    }

    public void setEditorType(PropertyEditorType editorType) {
        this.editorType = editorType;
    }

    public boolean isMultipleValues() {
        return multipleValues;
    }

    public void setMultipleValues(boolean multipleValues) {
        this.multipleValues = multipleValues;
    }

    public Object[] getOptions() {
        return options;
    }

    public void setOptions(Object[] options) {
        this.options = options;
    }
}
