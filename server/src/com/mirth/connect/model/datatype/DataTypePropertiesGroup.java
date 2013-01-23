package com.mirth.connect.model.datatype;

import java.io.Serializable;
import java.util.Map;

public interface DataTypePropertiesGroup extends Serializable {

    public Map<String, DataTypePropertyDescriptor> getProperties();
    
    public void setProperties(Map<String, Object> properties);
}
