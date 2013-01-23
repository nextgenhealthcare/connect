package com.mirth.connect.client.ui;

import java.util.Map.Entry;

import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.DataTypePropertiesGroup;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.SerializerProperties;

public class DataTypeTreeTableModel extends SortableTreeTableModel {

    private AbstractSortableTreeTableNode root;
    
    public DataTypeTreeTableModel() {
        root = new AbstractSortableTreeTableNode() {
            @Override
            public Object getValueAt(int column) {
                return null;
            }

            @Override
            public int getColumnCount() {
                return 0;
            }
        };
        
        setRoot(root);
    }
    
    @Override
    public int getHierarchicalColumn() {
        return 0;
    }
    
    @Override
    public Class<?> getColumnClass(int column) {
        return null;
    }
    
    @Override
    public boolean isCellEditable(Object node, int column) {
        if (node instanceof DataTypeTreeTableNode) {
            return ((DataTypeTreeTableNode) node).isEditable(column);
        }
        
        return false;
    }
    
    public void addProperties(boolean inbound, DataTypeProperties properties) {
        if (properties != null) {
            SerializerProperties serializerProperties = properties.getSerializerProperties();
            if (serializerProperties != null) {
                DataTypePropertiesGroup serializationProperties = serializerProperties.getSerializationProperties();
                
                if (inbound) {
                    if (serializationProperties != null) {
                        createAndInsertNode("Serialization", "These properties are used to convert the inbound message to XML.", serializationProperties);
                    }
                } else {
                    DataTypePropertiesGroup deserializationProperties = serializerProperties.getDeserializationProperties();
                    if (deserializationProperties != null && !inbound) {
                        createAndInsertNode("Deserialization", "These properties are used to convert the transformed XML message into the specified data type", deserializationProperties);
                    }
                    
                    if (serializationProperties != null) {
                        createAndInsertNode("Template Serialization", "These properties are used to convert the outbound message template to XML.", serializationProperties);
                    }
                }
                
                DataTypePropertiesGroup batchProperties = serializerProperties.getBatchProperties();
                if (batchProperties != null && inbound) {
                    createAndInsertNode("Batch", "These properties are used when reading in batch files.", batchProperties);
                }
            }
            
            DataTypePropertiesGroup responseGenerationProperties = properties.getResponseGenerationProperties();
            if (responseGenerationProperties != null && inbound) {
                createAndInsertNode("Response Generation", "These properties are used when responses are automatically generated.", responseGenerationProperties);
            }
            
            DataTypePropertiesGroup responseValidationProperties = properties.getResponseValidationProperties();
            if (responseValidationProperties != null && !inbound) {
                createAndInsertNode("Response Validation", "These properties are used to validate the response received by a destination connector. They are not used by the source connector.", responseValidationProperties);
            }
        } 
        
        if (root.getChildCount() == 0){
            insertNodeInto(new DataTypeTreeTableNode(" This data type has no properties.", null), root);
        }
    }
    
    private void createAndInsertNode(String groupName, String groupDescription, DataTypePropertiesGroup propertiesGroup) {
        DataTypeTreeTableNode groupNode = new DataTypeTreeTableNode(groupName, groupDescription);
        insertNodeInto(groupNode, root);
        
        for (Entry<String, DataTypePropertyDescriptor> entry : propertiesGroup.getProperties().entrySet()) {
            insertNodeInto(new DataTypeTreeTableNode(entry.getKey(), entry.getValue(), propertiesGroup), groupNode);
        }
    }
    
    public void clear() {
        int childCount = root.getChildCount();
        
        for (int i = 0; i < childCount; i++) {
            removeNodeFromParent((MutableTreeTableNode) root.getChildAt(0));
        }
    }

}
