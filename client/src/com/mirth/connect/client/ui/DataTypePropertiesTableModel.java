package com.mirth.connect.client.ui;

import java.util.ArrayList;
import java.util.List;

import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.DataTypePropertiesGroup;

public class DataTypePropertiesTableModel extends SortableTreeTableModel {

    private AbstractSortableTreeTableNode root;
    
    public DataTypePropertiesTableModel() {
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
        if (node instanceof DataTypePropertiesTableNode) {
            return ((DataTypePropertiesTableNode) node).isEditable(column);
        }
        
        return false;
    }
    
    /**
     * Adds the property set to the tree table. All DataTypeProperties in the properties list and the default properties must be for the same data type
     */
    public void addProperties(boolean inbound, List<DataTypePropertiesContainer> propertiesContainers, DataTypeProperties defaultProperties) {
        if (propertiesContainers != null) {
        	
        	// Create a list for each DataTypePropertiesGroup
        	List<DataTypePropertiesGroup> serializationProperties = new ArrayList<DataTypePropertiesGroup>();
        	List<DataTypePropertiesGroup> deserializationProperties = new ArrayList<DataTypePropertiesGroup>();
        	List<DataTypePropertiesGroup> batchProperties = new ArrayList<DataTypePropertiesGroup>();
        	List<DataTypePropertiesGroup> responseGenerationProperties = new ArrayList<DataTypePropertiesGroup>();
        	List<DataTypePropertiesGroup> responseValidationProperties = new ArrayList<DataTypePropertiesGroup>();
        	
        	// Load the lists with the properties from each DataTypeProperties objects
        	for (DataTypePropertiesContainer dataTypePropertiesContainer : propertiesContainers) {
        	    DataTypeProperties dataTypeProperties = dataTypePropertiesContainer.getProperties();
        	    
        		if (dataTypeProperties.getSerializationProperties() != null) {
        			serializationProperties.add(dataTypeProperties.getSerializationProperties());
        		}
        		
        		if (dataTypeProperties.getDeserializationProperties() != null) {
        			deserializationProperties.add(dataTypeProperties.getDeserializationProperties());
        		}
        		
        		if (dataTypeProperties.getBatchProperties() != null && dataTypePropertiesContainer.getType() == TransformerType.SOURCE) {
        			batchProperties.add(dataTypeProperties.getBatchProperties());
        		}
        		
        		if (dataTypeProperties.getResponseGenerationProperties() != null && dataTypePropertiesContainer.getType() == TransformerType.SOURCE) {
        			responseGenerationProperties.add(dataTypeProperties.getResponseGenerationProperties());
        		}
        		
        		if (dataTypeProperties.getResponseValidationProperties() != null && dataTypePropertiesContainer.getType() == TransformerType.RESPONSE) {
        			responseValidationProperties.add(dataTypeProperties.getResponseValidationProperties());
        		}
        	}
        	
        	
            if (inbound) {
            	// Show serialization if inbound
                if (!serializationProperties.isEmpty()) {
                    createAndInsertNode("Serialization", "These properties are used to convert the inbound message to XML.", serializationProperties, defaultProperties.getSerializationProperties());
                }
            } else {
            	// Show deserialization if outbound
                if (!deserializationProperties.isEmpty()) {
                    createAndInsertNode("Deserialization", "These properties are used to convert the transformed XML message into the specified data type", deserializationProperties, defaultProperties.getDeserializationProperties());
                }
                
                // Show serialization as Template Serialization if outbound
                if (!serializationProperties.isEmpty()) {
                    createAndInsertNode("Template Serialization", "These properties are used to convert the outbound message template to XML.", serializationProperties, defaultProperties.getSerializationProperties());
                }
            }
            
            // Show batch if inbound
            if (!batchProperties.isEmpty() && inbound) {
                createAndInsertNode("Batch", "These properties are used when reading in batch files.", batchProperties, defaultProperties.getBatchProperties());
            }
            
            // Show response generation if inbound
            if (!responseGenerationProperties.isEmpty() && inbound) {
                createAndInsertNode("Response Generation", "These properties are used when responses are automatically generated.", responseGenerationProperties, defaultProperties.getResponseGenerationProperties());
            }
            
            // Show response validation if outbound
            if (!responseValidationProperties.isEmpty() && inbound) {
                createAndInsertNode("Response Validation", "These properties are used to validate the response received by a destination connector. They are not used by the source connector.", responseValidationProperties, defaultProperties.getResponseValidationProperties());
            }
            
            // If no properties have been added, indicate that the data type has no properties
            if (root.getChildCount() == 0){
                insertNodeInto(new DataTypePropertiesTableNode(" This data type has no properties.", ""), root);
            }
        }
    }
    
    /** 
     * Inserts the group node for each properties group and the children (properties)
     */
    private void createAndInsertNode(String groupName, String groupDescription, List<DataTypePropertiesGroup> propertiesGroups, DataTypePropertiesGroup defaultPropertiesGroup) {
        DataTypePropertiesTableNode groupNode = new DataTypePropertiesTableNode(groupName, groupDescription);
        insertNodeInto(groupNode, root);
        
        for (String key : propertiesGroups.get(0).getProperties().keySet()) {
            insertNodeInto(new DataTypePropertiesTableNode(key, propertiesGroups, defaultPropertiesGroup), groupNode);
        }
    }
    
    /**
     * Remove all nodes from the tree table
     */
    public void clear() {
        int childCount = root.getChildCount();
        
        for (int i = 0; i < childCount; i++) {
            removeNodeFromParent((MutableTreeTableNode) root.getChildAt(0));
        }
    }
    
    /**
     * Resets the node and all of its children to the data type's default values
     */
    public void resetToDefault(TreeTableNode node) {
    	if (node == null) {
    		node = root;
    	}
    	
    	if (node instanceof DataTypePropertiesTableNode) {
    		DataTypePropertiesTableNode tableNode = (DataTypePropertiesTableNode) node;
    	
    		tableNode.resetToDefault();
    	}
    	
    	for (int i = 0; i < node.getChildCount(); i++) {
    		resetToDefault(node.getChildAt(i));
    	}
    }
    
    /**
     * Returns if the node and all of its children have are set to their default values
     */
    public boolean isDefaultProperties(TreeTableNode node) {
    	if (node == null) {
    		node = root;
    	}
    	
    	if (node instanceof DataTypePropertiesTableNode) {
    		DataTypePropertiesTableNode tableNode = (DataTypePropertiesTableNode) node;
    	
    		if (!tableNode.isDefaultProperty()) {
    			return false;
    		}
    	}
    	
    	for (int i = 0; i < node.getChildCount(); i++) {
    		if (!isDefaultProperties(node.getChildAt(i))) {
    			return false;
    		}
    	}
    	
    	return true;
    }

}
