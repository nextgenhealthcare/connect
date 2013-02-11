package com.mirth.connect.model.datatype;

public class DataTypePropertyDescriptor {

    private Object value;
    private String displayName;
    private String description;
    private PropertyEditorType editorType;
    
    public DataTypePropertyDescriptor(Object value, String displayName, String description, PropertyEditorType editorType) {
        this.value = value;
        this.displayName = displayName;
        this.description = description;
        this.editorType = editorType;
    }
    
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public PropertyEditorType getEditorType() {
        return editorType;
    }

    public void setEditorType(PropertyEditorType editorType) {
        this.editorType = editorType;
    }
    
    @Override
    public boolean equals(Object object) {
    	if (object instanceof DataTypePropertyDescriptor) {
    		DataTypePropertyDescriptor descriptor = (DataTypePropertyDescriptor) object;
    		
    		if (value.equals(descriptor.getValue()) && displayName.equals(descriptor.getDisplayName()) && description.equals(descriptor.getDescription()) && editorType.equals(descriptor.getEditorType())) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
}
