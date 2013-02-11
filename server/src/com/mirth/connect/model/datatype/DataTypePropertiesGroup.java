package com.mirth.connect.model.datatype;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

public abstract class DataTypePropertiesGroup implements Serializable {

    public abstract Map<String, DataTypePropertyDescriptor> getProperties();
    
    public abstract void setProperties(Map<String, Object> properties);
    
    @Override
    public boolean equals(Object object) {
    	boolean isEqual = true;
    	
    	if (object instanceof DataTypePropertiesGroup) {
    		DataTypePropertiesGroup propertiesGroup = (DataTypePropertiesGroup) object;
    		Map<String, DataTypePropertyDescriptor> descriptorMap = propertiesGroup.getProperties();
    		
    		for (Entry<String, DataTypePropertyDescriptor> entry : getProperties().entrySet()) {
    			if (!descriptorMap.containsKey(entry.getKey()) || !entry.getValue().equals(descriptorMap.get(entry.getKey()))) {
					isEqual = false;
    			}
    		}
    	} else {
    		isEqual = false;
    	}
    	
		return isEqual;
    }
}
