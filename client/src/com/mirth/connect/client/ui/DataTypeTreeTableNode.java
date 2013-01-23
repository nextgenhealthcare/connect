package com.mirth.connect.client.ui;

import java.util.HashMap;
import java.util.Map;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import com.mirth.connect.model.datatype.DataTypePropertiesGroup;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;

public class DataTypeTreeTableNode extends AbstractMutableTreeTableNode {
    private String groupName;
    private String groupDescription;
    private String propertyName;
    private DataTypePropertyDescriptor propertyDescriptor;
    private DataTypePropertiesGroup propertiesGroup;
    
    public DataTypeTreeTableNode(String groupName, String groupDescription) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
    }
    
    public DataTypeTreeTableNode(String propertyName, DataTypePropertyDescriptor propertyDescriptor, DataTypePropertiesGroup propertiesGroup) {
        this.propertyName = propertyName;
        this.propertyDescriptor = propertyDescriptor;
        this.propertiesGroup = propertiesGroup;
    }
    
    @Override
    public boolean isEditable(int column) {
        if (column == 1 && propertiesGroup != null) {
            return true;
        }
        
        return false;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int column) {
        switch (column) {
            case 0: 
                if (propertyDescriptor == null) {
                    return groupName;
                } else {
                    return propertyDescriptor.getDisplayName();
                }
            
            case 1: 
                if (propertyDescriptor == null) {
                    return null;
                } else {
                    return propertyDescriptor;
                }
        }
        
        return null;
    }
    
    @Override
    public void setValueAt(Object value, int column) {
        if (column == 1 && propertyDescriptor != null && propertiesGroup != null) {
            propertyDescriptor.setValue(value);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(propertyName, value);
            propertiesGroup.setProperties(map);
        }
    }
    
    public DataTypePropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public String getGroupDescription() {
        return groupDescription;
    }

}
