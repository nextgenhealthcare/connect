package com.mirth.connect.client.ui;

import com.mirth.connect.model.datatype.PropertyEditorType;

/**
 * This descriptor is used by the properties table renderer and editor
 */
public class DataTypeNodeDescriptor {

	private Object value;
    private PropertyEditorType editorType;
    private boolean multipleValues;
    
    public DataTypeNodeDescriptor(Object value, PropertyEditorType editorType, boolean multipleValues) {
        this.value = value;
        this.editorType = editorType;
        this.multipleValues = multipleValues;
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
}
