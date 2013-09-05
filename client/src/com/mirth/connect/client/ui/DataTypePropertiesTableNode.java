/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import com.mirth.connect.model.datatype.DataTypePropertiesGroup;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;

public class DataTypePropertiesTableNode extends AbstractMutableTreeTableNode {
    private String groupName;
    private String groupDescription;
    private String propertyName;
    private List<DataTypePropertiesGroup> propertiesGroups;
    private DataTypePropertiesGroup defaultPropertiesGroup;

    public DataTypePropertiesTableNode(String groupName, String groupDescription) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
    }

    public DataTypePropertiesTableNode(String propertyName, List<DataTypePropertiesGroup> propertiesGroups, DataTypePropertiesGroup defaultPropertiesGroup) {
        this.propertyName = propertyName;
        this.propertiesGroups = propertiesGroups;
        this.defaultPropertiesGroup = defaultPropertiesGroup;
    }

    @Override
    public boolean isEditable(int column) {
        if (column == 1 && propertiesGroups != null) {
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
                if (groupName != null) {
                    // Return the group name for the name column if it is a group node
                    return groupName;
                } else {
                    // Return the property's display name if it is a property node
                    String displayName = defaultPropertiesGroup.getPropertyDescriptors().get(propertyName).getDisplayName();
                    if (!isDefaultProperty()) {
                        // Bold the display name if the value is not the default value
                        displayName = "<html><b>" + displayName + "</b></html>";
                    }

                    return displayName;
                }

            case 1:
                if (groupName != null) {
                    // Return null for the description column if it is a group node
                    return null;
                } else {
                    DataTypeNodeDescriptor descriptor = null;

                    for (DataTypePropertiesGroup propertiesGroup : propertiesGroups) {
                        DataTypePropertyDescriptor currentDescriptor = propertiesGroup.getPropertyDescriptors().get(propertyName);

                        if (descriptor == null) {
                            // Populate the node descriptor with the data from the first node
                            descriptor = new DataTypeNodeDescriptor(currentDescriptor.getValue(), currentDescriptor.getEditorType(), false);
                        } else {
                            // If the values in the list of properties are not all the same, indicate that there are multiple values
                            if (!descriptor.getValue().equals(currentDescriptor.getValue())) {
                                descriptor.setMultipleValues(true);
                                break;
                            }
                        }
                    }

                    return descriptor;
                }
        }

        return null;
    }

    @Override
    public void setValueAt(Object value, int column) {
        if (column == 1 && propertiesGroups != null && value != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(propertyName, value);
            for (DataTypePropertiesGroup propertiesGroup : propertiesGroups) {
                propertiesGroup.setProperties(map);
            }
        }
    }

    public DataTypePropertyDescriptor getPropertyDescriptor() {
        return defaultPropertiesGroup.getPropertyDescriptors().get(propertyName);
    }

    public String getName() {
        if (groupName != null) {
            return groupName;
        } else {
            return defaultPropertiesGroup.getPropertyDescriptors().get(propertyName).getDisplayName();
        }
    }

    public String getDescription() {
        if (groupName != null) {
            return groupDescription;
        } else {
            return defaultPropertiesGroup.getPropertyDescriptors().get(propertyName).getDescription();
        }
    }

    /**
     * Reset the current node to its default value
     */
    public void resetToDefault() {
        if (propertyName != null) {
            Object defaultValue = defaultPropertiesGroup.getPropertyDescriptors().get(propertyName).getValue();
            setValueAt(defaultValue, 1);
        }
    }

    /**
     * Returns whether or not the node holds the default value
     */
    public boolean isDefaultProperty() {
        if (propertiesGroups != null) {
            Object defaultValue = defaultPropertiesGroup.getPropertyDescriptors().get(propertyName).getValue();

            for (DataTypePropertiesGroup propertiesGroup : propertiesGroups) {
                Object value = propertiesGroup.getPropertyDescriptors().get(propertyName).getValue();

                if (!defaultValue.equals(value)) {
                    return false;
                }
            }
        }

        return true;
    }

}
