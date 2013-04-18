/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.client.ui.components.MirthTriStateCheckBox;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.datatype.DataTypeProperties;

public class DataTypesDialog extends javax.swing.JDialog {
	
	public static int CONNECTOR_COLUMN = 1;
	public static int INBOUND_COLUMN = 2;
	public static int OUTBOUND_COLUMN = 3;
	public static int SELECTION_COLUMN = 0;
	private static int SELECTION_COLUMN_WIDTH = 20;
    private Frame parent;
    private final String[] columnNames = {"", "Connector", "Inbound", "Outbound"};
    private enum EditMode {SINGLE, BULK};
    private EditMode editMode;
    private Map<Integer, TransformerContainer> transformerContainer;

    public DataTypesDialog() {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        
        inboundPropertiesPanel.setInbound(true);
        outboundPropertiesPanel.setInbound(false);
        
        inboundPropertiesPanel.setUseTitleBorder(true);
        outboundPropertiesPanel.setUseTitleBorder(true);
        
        String[] dataTypes = new String[PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.values().size()];
	    PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.values().toArray(dataTypes);
	    inboundPropertiesPanel.getDataTypeComboBox().setModel(new DefaultComboBoxModel(dataTypes));
	    outboundPropertiesPanel.getDataTypeComboBox().setModel(new DefaultComboBoxModel(dataTypes));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }
        
        // Initialize the map that stores all of the transformers
        transformerContainer = new HashMap<Integer, TransformerContainer>();
        
        makeTables();
        
        // Begin with single edit mode
        toggleEditMode(EditMode.SINGLE);
        
        // Add the listener to revert properties if the window is closed with the close (X) button
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                revertProperties();
            }
            
        });
        
        // Add listeners to the data types properties panel
        addListeners();
        
        // Show the dialog
        setVisible(true);
    }
    
    /**
     * Toggle between single and bulk edit modes
     */
    public void toggleEditMode(EditMode mode) {
    	// Do nothing if the current and selected modes are the same
    	if (editMode == mode) {
    		return;
    	}
    	
    	// Store the mode
    	editMode = mode;
    	// Helper variable for updating attributes
    	boolean singleEdit;
    	
    	// Get the selection column
    	TableColumnExt column = connectorTreeTable.getColumnExt(SELECTION_COLUMN);
    	if (editMode == EditMode.SINGLE) {
    		singleEdit = true;
    		
    		// Hide the selection column if single edit mode
    		column.setMaxWidth(0);
    		column.setMinWidth(0);
    		column.setPreferredWidth(0);
    		
        	inboundPropertiesPanel.getDataTypeComboBox().setEnabled(true);
        	outboundPropertiesPanel.getDataTypeComboBox().setEnabled(true);
    		
    		connectorTreeTable.clearSelection();
    		
    		// Automatically select the source connector by default
            connectorTreeTable.getTreeSelectionModel().setSelectionPath(connectorTreeTable.getPathForRow(0));
    	} else {
    		singleEdit = false;
    		
    		// Show the selection column if bulk edit mode
    		column.setMaxWidth(SELECTION_COLUMN_WIDTH);
    		column.setMinWidth(SELECTION_COLUMN_WIDTH);
    		column.setPreferredWidth(SELECTION_COLUMN_WIDTH);
    		
    		// Clear all selections
    		TreeTableNode root = (TreeTableNode) connectorTreeTable.getTreeTableModel().getRoot();
    		for (int i = 0; i < root.getChildCount(); i++) {
    			TreeTableNode connector = root.getChildAt(i);
    			connector.setValueAt(false, SELECTION_COLUMN);
    			
    			for (int j = 0; j < connector.getChildCount(); j++) {
    				connector.getChildAt(j).setValueAt(false, SELECTION_COLUMN);
    			}
    		}
    		
    		// Updates the dialog based on the current selections
    		updateBulkSelection();
    	}
    	
    	// Show or hide the bulk selection checkboxes
    	allCheckBox.setVisible(!singleEdit);
    	destinationsCheckBox.setVisible(!singleEdit);
    	responsesCheckBox.setVisible(!singleEdit);
    	
    	// Enable or disable single row selection
    	connectorTreeTable.setRowSelectionAllowed(singleEdit);
    	// Enable or disable focus on the tree table so the arrow keys can be used to select different rows
    	connectorTreeTable.setFocusable(singleEdit);
    	// Enable or disable editing the data type from the table
    	connectorTreeTable.getColumnExt(INBOUND_COLUMN).setEditable(singleEdit);
    	connectorTreeTable.getColumnExt(OUTBOUND_COLUMN).setEditable(singleEdit);
    }
    
    private void addListeners() {
    	inboundPropertiesPanel.getDataTypeComboBox().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox comboBox = (JComboBox) e.getSource();
				if (comboBox.isPopupVisible()) {
					String dataTypeDisplayName = (String)((JComboBox) e.getSource()).getSelectedItem();
					
					if (editMode == EditMode.SINGLE) {
						DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) connectorTreeTable.getPathForRow(connectorTreeTable.getSelectedRow()).getLastPathComponent();
						updateSingleDataType(tableNode, dataTypeDisplayName, true);
					} else {
						String dataType = parent.displayNameToDataType.get(dataTypeDisplayName);
						if (dataType != null) {
			                TreeTableNode root = (TreeTableNode) connectorTreeTable.getTreeTableModel().getRoot();
			                // Update the selected inbound data types in the connector tree table
			            	if (updateInboundDataType(root, dataType, false)) {
			            		updateInboundDataType(root, dataType, true);
			            		
			            		for (int i = 0; i < root.getChildCount(); i++) {
			            			DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) root.getChildAt(i);
			            			
			        				Transformer transformer = transformerContainer.get(tableNode.getContainerIndex()).getTransformer();
			        				TransformerType type = transformerContainer.get(tableNode.getContainerIndex()).getType();
			            			
			            			if (type == TransformerType.SOURCE) {
			            				DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
			            				
			            				tableNode.setValueAt(parent.dataTypeToDisplayName.get(dataType), OUTBOUND_COLUMN);
				        				transformer.setOutboundDataType(dataType);
				                        transformer.setOutboundProperties(defaultProperties);
			            			}
			            		}
			            	}
			            	updateBulkSelection();
						}
					}
					
					connectorTreeTable.repaint();
				}
			}
			
    	});
    	inboundPropertiesPanel.getDefaultButton().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) connectorTreeTable.getTreeSelectionModel().getSelectionPath().getLastPathComponent();
				String dataTypeDisplayName = (String) tableNode.getValueAt(INBOUND_COLUMN);
				String dataType = PlatformUI.MIRTH_FRAME.displayNameToDataType.get(dataTypeDisplayName);
				
				if (editMode == EditMode.SINGLE) {
					int containerIndex = tableNode.getContainerIndex();
					TransformerContainer container = transformerContainer.get(containerIndex);
					
					Transformer transformer = container.getTransformer();
					
					DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
					
					transformer.setInboundProperties(defaultProperties);
					inboundPropertiesPanel.setDataTypeProperties(dataTypeDisplayName, new DataTypePropertiesContainer(defaultProperties, container.getType()));
				} else if (editMode == EditMode.BULK) {
					TreeTableNode root = (TreeTableNode) connectorTreeTable.getTreeTableModel().getRoot();
					resetToDefault(root, dataType, true);
					
					updateBulkSelection();
				}
				
				inboundPropertiesPanel.updateDefaultButton();
			}
    		
    	});
    	outboundPropertiesPanel.getDataTypeComboBox().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox comboBox = (JComboBox) e.getSource();
				if (comboBox.isPopupVisible()) {
					String dataTypeDisplayName = (String)((JComboBox) e.getSource()).getSelectedItem();
					
					if (editMode == EditMode.SINGLE) {
						DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) connectorTreeTable.getPathForRow(connectorTreeTable.getSelectedRow()).getLastPathComponent();
						updateSingleDataType(tableNode, dataTypeDisplayName, false);
					} else {
						String dataType = parent.displayNameToDataType.get(dataTypeDisplayName);
						if (dataType != null) {
							
			                TreeTableNode root = (TreeTableNode) connectorTreeTable.getTreeTableModel().getRoot();
			                // Update the selected outbound data types in the connector tree table
			            	if (updateOutboundDataType(root, dataType)) {
			            		// Update the destination inbound data types if the source outbound was changed
			            		updateInboundDataType(root, dataType, true);
			            	}
			            	// Update the properties tables
			            	updateBulkSelection();
						}
					}
					
					connectorTreeTable.repaint();
				}
			}
			
    	});
    	outboundPropertiesPanel.getDefaultButton().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) connectorTreeTable.getTreeSelectionModel().getSelectionPath().getLastPathComponent();
				String dataTypeDisplayName = (String) tableNode.getValueAt(OUTBOUND_COLUMN);
				String dataType = PlatformUI.MIRTH_FRAME.displayNameToDataType.get(dataTypeDisplayName);
				
				if (editMode == EditMode.SINGLE) {
					int containerIndex = tableNode.getContainerIndex();
					TransformerContainer container = transformerContainer.get(containerIndex);
					
					Transformer transformer = container.getTransformer();
					
					DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
					
					transformer.setOutboundProperties(defaultProperties);
					outboundPropertiesPanel.setDataTypeProperties(dataTypeDisplayName, new DataTypePropertiesContainer(defaultProperties, container.getType()));
				} else if (editMode == EditMode.BULK) {
					TreeTableNode root = (TreeTableNode) connectorTreeTable.getTreeTableModel().getRoot();
					resetToDefault(root, dataType, false);
					
					updateBulkSelection();
				}
				
				outboundPropertiesPanel.updateDefaultButton();
			}
    		
    	});
    }
    
    /**
     * Reset all selected data type properties
     */
    private void resetToDefault(TreeTableNode node, String dataType, boolean inbound) {
    	if (node instanceof DataTypeConnectorTableNode) {
			DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) node;
			
			if ((Boolean) tableNode.getValueAt(SELECTION_COLUMN)) {
				DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
				Transformer transformer = transformerContainer.get(tableNode.getContainerIndex()).getTransformer();
				
				tableNode.setValueAt(parent.dataTypeToDisplayName.get(dataType), INBOUND_COLUMN);
				
				if (inbound) {
					transformer.setInboundProperties(defaultProperties);
				} else {
					transformer.setOutboundProperties(defaultProperties);
				}
			}
		}
		
		for (int i = 0; i < node.getChildCount(); i++) {
			resetToDefault(node.getChildAt(i), dataType, inbound);
		}
    }
    
    /**
     * Updates all selected inbound data types and returns whether or not a destination inbound datatype was changed
     */
    private boolean updateInboundDataType(TreeTableNode node, String dataType, boolean updateDestinationInbound) {
    	boolean updateSourceOutbound = false;
    	
		if (node instanceof DataTypeConnectorTableNode) {
			DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) node;
			TransformerType type = transformerContainer.get(tableNode.getContainerIndex()).getType();
			
			if (!(PlatformUI.MIRTH_FRAME.channelEditPanel.requiresXmlDataType() && type == TransformerType.SOURCE) && ((!updateDestinationInbound && (Boolean) tableNode.getValueAt(SELECTION_COLUMN) && type != TransformerType.DESTINATION) || (updateDestinationInbound && type == TransformerType.DESTINATION))) {
				DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
				Transformer transformer = transformerContainer.get(tableNode.getContainerIndex()).getTransformer();
				
				tableNode.setValueAt(parent.dataTypeToDisplayName.get(dataType), INBOUND_COLUMN);
				transformer.setInboundDataType(dataType);
                transformer.setInboundProperties(defaultProperties);
			} else if (!updateDestinationInbound && (Boolean) tableNode.getValueAt(SELECTION_COLUMN) && type == TransformerType.DESTINATION) {
				updateSourceOutbound = true;
			}
		}
		
		for (int i = 0; i < node.getChildCount(); i++) {
			if (updateInboundDataType(node.getChildAt(i), dataType, updateDestinationInbound)) {
				updateSourceOutbound = true;
			}
		}
		
		return updateSourceOutbound;
	}
    
    /**
     * Updates all selected outbound data types and returns whether or not the source outbound datatype was changed
     */
    private boolean updateOutboundDataType(TreeTableNode node, String dataType) {
    	boolean updateDestinationInbound = false;
    	
		if (node instanceof DataTypeConnectorTableNode) {
			DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) node;
			
			if ((Boolean) tableNode.getValueAt(SELECTION_COLUMN)) {
				DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
				Transformer transformer = transformerContainer.get(tableNode.getContainerIndex()).getTransformer();
				TransformerType type = transformerContainer.get(tableNode.getContainerIndex()).getType();
				
				tableNode.setValueAt(parent.dataTypeToDisplayName.get(dataType), OUTBOUND_COLUMN);
				transformer.setOutboundDataType(dataType);
                transformer.setOutboundProperties(defaultProperties);
                
                // The source row should always be the first edited row
                if (type == TransformerType.SOURCE) {
                	updateDestinationInbound = true;
                }
			}
		}
		
		for (int i = 0; i < node.getChildCount(); i++) {
			if (updateOutboundDataType(node.getChildAt(i), dataType)) {
				updateDestinationInbound = true;
			}
		}
		
		return updateDestinationInbound;
	}

    public void makeTables() {
    	// Updates the model for the connector tree table
        updateConnectorTreeTable();
        // Set the table attributes
        makeTreeTable(connectorTreeTable, connectorTreeTablePane);
    }
    
    public void makeTreeTable(MirthTreeTable table, JScrollPane scrollPane) {
    	int dataTypeColumnWidth = 100;
        
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        String[] dataTypes = new String[parent.dataTypeToDisplayName.values().size()];
        parent.dataTypeToDisplayName.values().toArray(dataTypes);
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (editMode == EditMode.SINGLE && !e.getValueIsAdjusting()) {
                	if (connectorTreeTable.getSelectedRow() != -1) {
                	DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) connectorTreeTable.getPathForRow(connectorTreeTable.getSelectedRow()).getLastPathComponent();
                    Transformer transformer = transformerContainer.get(tableNode.getContainerIndex()).getTransformer();
                    TransformerType type = transformerContainer.get(tableNode.getContainerIndex()).getType();
                    
                    String inboundDataType = (String) tableNode.getValueAt(INBOUND_COLUMN);
                    String outboundDataType = (String) tableNode.getValueAt(OUTBOUND_COLUMN);
                    
                    inboundPropertiesPanel.setDataTypeProperties(inboundDataType, new DataTypePropertiesContainer(transformer.getInboundProperties(), type));
                    inboundPropertiesPanel.getDataTypeComboBox().getModel().setSelectedItem(inboundDataType);
                    inboundPropertiesPanel.getDataTypeComboBox().setEnabled(!(type == TransformerType.DESTINATION || (type == TransformerType.SOURCE && PlatformUI.MIRTH_FRAME.channelEditPanel.requiresXmlDataType())));
                    
                    outboundPropertiesPanel.setDataTypeProperties(outboundDataType, new DataTypePropertiesContainer(transformer.getOutboundProperties(), type));
                    outboundPropertiesPanel.getDataTypeComboBox().getModel().setSelectedItem(outboundDataType);
                	} else {
                	    // Need to set a type for the null value because of overloaded method
                		DataTypePropertiesContainer propertiesContainer = null;
                		inboundPropertiesPanel.setDataTypeProperties(null, propertiesContainer);
                		outboundPropertiesPanel.setDataTypeProperties(null, propertiesContainer);
                	}
                }
            }
            
        });
        
        table.getColumnExt(INBOUND_COLUMN).setCellEditor(new MirthComboBoxTableCellEditor(table, dataTypes, 1, false, new DataTypeComboBoxActionListener(true)));
        table.getColumnExt(INBOUND_COLUMN).setCellRenderer(new DataTypeCellRenderer(dataTypes));
        
        table.getColumnExt(OUTBOUND_COLUMN).setCellEditor(new MirthComboBoxTableCellEditor(table, dataTypes, 1, false, new DataTypeComboBoxActionListener(false)));
        table.getColumnExt(OUTBOUND_COLUMN).setCellRenderer(new DataTypeCellRenderer(dataTypes));
        
        table.getColumnExt(SELECTION_COLUMN).setCellRenderer(new TableCellRenderer() {
        	
        	private JCheckBox checkBox = new JCheckBox();

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				if (value instanceof Boolean) {
					checkBox.setSelected((Boolean) value);
				}
				
				if (isSelected) {
					checkBox.setForeground(table.getSelectionForeground());
					checkBox.setBackground(table.getSelectionBackground());
    	        } else {
    	        	checkBox.setForeground(table.getForeground());
    	        	checkBox.setBackground(table.getBackground());
    	        }
				checkBox.setVerticalAlignment(SwingConstants.CENTER);
				checkBox.setHorizontalAlignment(SwingConstants.CENTER);
				
				return checkBox;
			}
        	
        });
        table.getColumnExt(SELECTION_COLUMN).setCellEditor(new CheckBoxCellEditor());
        
        table.setRowHeight(UIConstants.ROW_HEIGHT);
        table.setSortable(false);
        table.setOpaque(true);
        table.setDragEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setShowGrid(true, true);
        table.setLeafIcon(null);
        table.setOpenIcon(null);
        table.setClosedIcon(null);
        table.setAutoCreateColumnsFromModel(false);

        table.getColumnExt(SELECTION_COLUMN).setMaxWidth(SELECTION_COLUMN_WIDTH);
        table.getColumnExt(SELECTION_COLUMN).setMinWidth(SELECTION_COLUMN_WIDTH);
        table.getColumnExt(SELECTION_COLUMN).setResizable(false);
        
        table.getColumnExt(CONNECTOR_COLUMN).setMinWidth(UIConstants.MIN_WIDTH);
        table.getColumnExt(CONNECTOR_COLUMN).setResizable(false);

        table.getColumnExt(INBOUND_COLUMN).setMaxWidth(dataTypeColumnWidth);
        table.getColumnExt(INBOUND_COLUMN).setMinWidth(dataTypeColumnWidth);
        table.getColumnExt(INBOUND_COLUMN).setResizable(false);
        
        table.getColumnExt(OUTBOUND_COLUMN).setMaxWidth(dataTypeColumnWidth);
        table.getColumnExt(OUTBOUND_COLUMN).setMinWidth(dataTypeColumnWidth);
        table.getColumnExt(OUTBOUND_COLUMN).setResizable(false);
        
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            table.setHighlighters(highlighter);
        }

        scrollPane.setViewportView(table);
    }
    
    private class CheckBoxCellEditor extends AbstractCellEditor implements TableCellEditor {

    	private JCheckBox checkBox;
    	
    	public CheckBoxCellEditor() {
    		checkBox = new JCheckBox();
    		checkBox.setVerticalAlignment(SwingConstants.CENTER);
			checkBox.setHorizontalAlignment(SwingConstants.CENTER);
			checkBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
					
					updateBulkSelection();
				}
				
			});
    	}
    	
		@Override
		public Object getCellEditorValue() {
			return checkBox.isSelected();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if (value != null && value instanceof Boolean) {
    			checkBox.setSelected((Boolean) value);
    		}
			
			checkBox.setForeground(table.getSelectionForeground());
			checkBox.setBackground(table.getSelectionBackground());
			
			return checkBox;
		}
    	
    }

    
    private class DataTypeCellRenderer extends MirthComboBoxTableCellRenderer {
    	private JLabel label = new JLabel();
    	
		public DataTypeCellRenderer(Object[] items) {
			super(items);
			
			label.setOpaque(true);
		}
		
    	@Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    		if (value != null && !table.isCellEditable(row, column)) {
    			if (isSelected) {
    				label.setForeground(table.getSelectionForeground());
    				label.setBackground(table.getSelectionBackground());
    	        } else {
    	        	label.setForeground(table.getForeground());
    	        	label.setBackground(table.getBackground());
    	        }
    			
    			label.setText(" " + value.toString());
    			
    			return label;
    		}
    		
    		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    	}
    	
    }
    
    private class DataTypeComboBoxActionListener implements ActionListener {
        
        private boolean inbound;
        
        public DataTypeComboBoxActionListener(boolean inbound) {
            this.inbound = inbound;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        	JComboBox comboBox = (JComboBox) e.getSource();
        	if (comboBox.isPopupVisible()) {
        		DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) connectorTreeTable.getPathForRow(connectorTreeTable.getEditingRow()).getLastPathComponent();
                String dataTypeDisplayName = (String)comboBox.getSelectedItem();
                
                updateSingleDataType(tableNode, dataTypeDisplayName, inbound);
            }
        }
        
    }
    
    public void updateSingleDataType(DataTypeConnectorTableNode tableNode, String dataTypeDisplayName, boolean inbound) {
    	String dataType = PlatformUI.MIRTH_FRAME.displayNameToDataType.get(dataTypeDisplayName);
        DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
        TransformerContainer container = transformerContainer.get(tableNode.getContainerIndex());
        Transformer transformer = container.getTransformer();

        if (inbound) {
            if (!transformer.getInboundDataType().equals(dataType)) {
                transformer.setInboundDataType(dataType);
                transformer.setInboundProperties(defaultProperties);
                tableNode.setValueAt(dataTypeDisplayName, INBOUND_COLUMN);
                inboundPropertiesPanel.getDataTypeComboBox().setSelectedItem(dataTypeDisplayName);
                inboundPropertiesPanel.setDataTypeProperties(dataTypeDisplayName, new DataTypePropertiesContainer(transformer.getInboundProperties(), container.getType()));
            }
        } else {
            if (!transformer.getOutboundDataType().equals(dataType)) {
                transformer.setOutboundDataType(dataType);
                transformer.setOutboundProperties(defaultProperties);
                tableNode.setValueAt(dataTypeDisplayName, OUTBOUND_COLUMN);
                outboundPropertiesPanel.getDataTypeComboBox().setSelectedItem(dataTypeDisplayName);
                outboundPropertiesPanel.setDataTypeProperties(dataTypeDisplayName, new DataTypePropertiesContainer(transformer.getOutboundProperties(), container.getType()));
            }
            
            if (container.getType() == TransformerType.SOURCE) {
            	// If the source outbound data type is changed, also update all destination inbound data types.
                TreeTableNode root = (TreeTableNode) connectorTreeTable.getTreeTableModel().getRoot();
                for (int i = 0; i < root.getChildCount(); i++) {
            		if (root.getChildAt(i) instanceof DataTypeConnectorTableNode) {
            			DataTypeConnectorTableNode node = (DataTypeConnectorTableNode) root.getChildAt(i);
            			
            			TransformerContainer childContainer = transformerContainer.get(node.getContainerIndex());
            			if (childContainer.getType() == TransformerType.DESTINATION) {
            				// Get a new properties object so they aren't all using the same one
                            defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
                            
                            node.setValueAt(dataTypeDisplayName, INBOUND_COLUMN);
                            transformer = childContainer.getTransformer();
                            transformer.setInboundDataType(dataType);
                            transformer.setInboundProperties(defaultProperties);
            			}
            		}
            	}
            } else if (container.getType() == TransformerType.DESTINATION) {
            	// If the destination outbound data type is changed, also update the destination's inbound and outbound data types.
            	for (int i = 0; i < tableNode.getChildCount(); i++) {
            		if (tableNode.getChildAt(i) instanceof DataTypeConnectorTableNode) {
            			DataTypeConnectorTableNode node = (DataTypeConnectorTableNode) tableNode.getChildAt(i);
            			
            			TransformerContainer childContainer = transformerContainer.get(node.getContainerIndex());
            			if (childContainer.getType() == TransformerType.RESPONSE) {
            				transformer = childContainer.getTransformer();
            				// Get a new properties object so they aren't all using the same one
                            defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
                            
                            node.setValueAt(dataTypeDisplayName, INBOUND_COLUMN);
                            
                            transformer.setInboundDataType(dataType);
                            transformer.setInboundProperties(defaultProperties);
                            
                            // Get a new properties object so they aren't all using the same one
                            defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
                            
                            node.setValueAt(dataTypeDisplayName, OUTBOUND_COLUMN);
                            transformer.setOutboundDataType(dataType);
                            transformer.setOutboundProperties(defaultProperties);
            			}
            		}
            	}
            }
        }
    }
    
    /**
     * Populates the tree table model for the connector table
     */
    public void updateConnectorTreeTable() {
    	Object[][] tableData = null;
    	Set<Integer> destinations = new HashSet<Integer>();
    	int containerIndex = 0;
    	
    	Channel currentChannel = parent.channelEditPanel.currentChannel;
    	
    	DataTypeConnectorTableModel tableModel = new DataTypeConnectorTableModel();
    	tableModel.setColumnIdentifiers(Arrays.asList(columnNames));
    	
    	Connector sourceConnector = currentChannel.getSourceConnector();
        Transformer transformer = sourceConnector.getTransformer();
        transformerContainer.put(containerIndex, new TransformerContainer(transformer, TransformerType.SOURCE, transformer.getInboundDataType(), transformer.getOutboundDataType(), transformer.getInboundProperties().clone(), transformer.getOutboundProperties().clone()));
        
    	tableData = new Object[1][columnNames.length];
    	tableData[0][SELECTION_COLUMN] = false;
    	tableData[0][CONNECTOR_COLUMN] = "Source Connector";
        tableData[0][INBOUND_COLUMN] = parent.dataTypeToDisplayName.get(sourceConnector.getTransformer().getInboundDataType());
        tableData[0][OUTBOUND_COLUMN] = parent.dataTypeToDisplayName.get(sourceConnector.getTransformer().getOutboundDataType());
    	
    	tableModel.addConnector(containerIndex++, tableData);
    	
    	for (Connector destinationConnector : currentChannel.getDestinationConnectors()) {
    		tableData = new Object[2][columnNames.length];
    		
            transformer = destinationConnector.getTransformer();
            destinations.add(containerIndex);
            transformerContainer.put(containerIndex, new TransformerContainer(transformer, TransformerType.DESTINATION, transformer.getInboundDataType(), transformer.getOutboundDataType(), transformer.getInboundProperties().clone(), transformer.getOutboundProperties().clone()));
            tableData[0][SELECTION_COLUMN] = false;
            tableData[0][CONNECTOR_COLUMN] = destinationConnector.getName();
            tableData[0][INBOUND_COLUMN] = parent.dataTypeToDisplayName.get(currentChannel.getSourceConnector().getTransformer().getOutboundDataType());
            tableData[0][OUTBOUND_COLUMN] = parent.dataTypeToDisplayName.get(destinationConnector.getTransformer().getOutboundDataType());
            
            transformer = destinationConnector.getResponseTransformer();
            transformerContainer.put(containerIndex + 1, new TransformerContainer(transformer, TransformerType.RESPONSE, transformer.getInboundDataType(), transformer.getOutboundDataType(), transformer.getInboundProperties().clone(), transformer.getOutboundProperties().clone()));
            tableData[1][SELECTION_COLUMN] = false;
            tableData[1][CONNECTOR_COLUMN] = "Response";
            tableData[1][INBOUND_COLUMN] = parent.dataTypeToDisplayName.get(destinationConnector.getResponseTransformer().getInboundDataType());
            tableData[1][OUTBOUND_COLUMN] = parent.dataTypeToDisplayName.get(destinationConnector.getResponseTransformer().getOutboundDataType());
            
            tableModel.addConnector(containerIndex, tableData);
           
            containerIndex += 2;
        }
    	
    	tableModel.setDestinations(destinations);
    	connectorTreeTable.setTreeTableModel(tableModel);
    }
    
    /**
     * Discard all changes made by this dialog instance
     */
    private void revertProperties() {
    	for (TransformerContainer container : transformerContainer.values()) {
        	Transformer transformer = container.getTransformer();
        	
        	transformer.setInboundDataType(container.getLastInboundDataType());
        	transformer.setInboundProperties(container.getLastInboundDataTypeProperties());
        	transformer.setOutboundDataType(container.getLastOutboundDataType());
        	transformer.setOutboundProperties(container.getLastOutboundDataTypeProperties());
    	}
    }
    
    private void revertProperties(TreeTableNode node) {
    	if (node == null) {
    		node = (TreeTableNode) connectorTreeTable.getTreeTableModel().getRoot();
    	}
    	
    	if (node instanceof DataTypeConnectorTableNode) {
    		DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) node;
    		
    		TransformerContainer container = transformerContainer.get(tableNode.getContainerIndex());
        	Transformer transformer = container.getTransformer();
        	
        	transformer.setInboundDataType(container.getLastInboundDataType());
        	transformer.setInboundProperties(container.getLastInboundDataTypeProperties());
        	transformer.setOutboundDataType(container.getLastOutboundDataType());
        	transformer.setOutboundProperties(container.getLastOutboundDataTypeProperties());
    	}
    	
    	for (int i = 0; i < node.getChildCount(); i++) {
    		revertProperties(node.getChildAt(i));
        }
    }
    
    /**
     * Updates the selected rows based on the Transformer types that are provided.
     * For bulk editing only
     */
    private void toggleSelection(Set<TransformerType> types, boolean select) {
    	TreeTableNode root = (TreeTableNode) connectorTreeTable.getTreeTableModel().getRoot();
    	
    	toggleSelection(types, select, root);
    	
    	if (types.contains(TransformerType.RESPONSE) && select) {
    		connectorTreeTable.expandAll();
    	}
    	
    	updateBulkSelection();
    	connectorTreeTable.repaint();
    }
    
    /**
     * Helper function to traverse the tree table and update selected rows
     * For bulk editing only
     */
    private void toggleSelection(Set<TransformerType> types, boolean select, TreeTableNode node) {
    	if (node instanceof DataTypeConnectorTableNode) {
    		DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) node;
    		
    		TransformerContainer container = transformerContainer.get(tableNode.getContainerIndex());
    		if (types.contains(container.getType())) {
				tableNode.setValueAt(select, SELECTION_COLUMN);
    		}
    	}
    	
    	for (int i = 0; i < node.getChildCount(); i++) {
    		toggleSelection(types, select, node.getChildAt(i));
    	}
    }
    
    /**
     * Updates the All, Destination, Response checkboxes based on which rows are selected in the connector tree table
     * Updates the data type properties panel with the list of DataTypeProperties to be updated when a change is made
     */
    private void updateBulkSelection() {
    	TreeTableNode root = (TreeTableNode) connectorTreeTable.getTreeTableModel().getRoot();
    	Map<MirthTriStateCheckBox, Integer> status = new HashMap<MirthTriStateCheckBox, Integer>();
    	List<Integer> containerIds = new ArrayList<Integer>();

    	updateSelectionStatus(root, status, containerIds);
    	
    	// Set the bulk selection checkbox states
    	for (Entry<MirthTriStateCheckBox, Integer> entry : status.entrySet()) {
    		entry.getKey().setState(entry.getValue());
    	}
    
    	List<DataTypePropertiesContainer> inboundPropertiesList = null;
    	List<DataTypePropertiesContainer> outboundPropertiesList = null;
    	
    	String inboundDataType = null;
    	String inboundDataTypeDisplayName = null;
    	String outboundDataType = null;
    	String outboundDataTypeDisplayName = null;
    	
    	if (containerIds.isEmpty()) {
    		inboundPropertiesPanel.getDataTypeComboBox().setSelectedIndex(-1);
    		outboundPropertiesPanel.getDataTypeComboBox().setSelectedIndex(-1);
    		
    		inboundPropertiesPanel.getDataTypeComboBox().setEnabled(false);
    		outboundPropertiesPanel.getDataTypeComboBox().setEnabled(false);
    	} else {
    		inboundPropertiesPanel.getDataTypeComboBox().setEnabled(true);
    		outboundPropertiesPanel.getDataTypeComboBox().setEnabled(true);
    		
    		inboundPropertiesList = new ArrayList<DataTypePropertiesContainer>();
    		outboundPropertiesList = new ArrayList<DataTypePropertiesContainer>();
    		
	    	// For each selected row
	    	for (int containerId : containerIds) {
	    		TransformerContainer container = transformerContainer.get(containerId);
	    		
	    		if (inboundPropertiesList != null) {
	    			inboundPropertiesList.add(new DataTypePropertiesContainer(container.getTransformer().getInboundProperties(), container.getType()));
	    			
		    		if (inboundDataType == null) {
		    			// If the combined inbound data type has not been set yet, set it to the currently selected inbound data type.
		    			inboundDataType = container.getTransformer().getInboundDataType();
		    			// Set the data type's display name
		    			inboundDataTypeDisplayName = parent.dataTypeToDisplayName.get(inboundDataType);
		    		} else if (!inboundDataType.equals(container.getTransformer().getInboundDataType())) {
		    			inboundPropertiesPanel.getDataTypeComboBox().getModel().setSelectedItem("<Different Data Types>");
		    			inboundDataTypeDisplayName = null;
		    			inboundPropertiesList = null;
		    		}
	    		}
	    		
	    		if (outboundPropertiesList != null) {
	    			outboundPropertiesList.add(new DataTypePropertiesContainer(container.getTransformer().getOutboundProperties(), container.getType()));
	    			
		    		if (outboundDataType == null) {
		    			// If the combined inbound data type has not been set yet, set it to the currently selected inbound data type.
		    			outboundDataType = container.getTransformer().getOutboundDataType();
		    			// Set the data type's display name
		    			outboundDataTypeDisplayName = parent.dataTypeToDisplayName.get(outboundDataType);
		    		} else if (!outboundDataType.equals(container.getTransformer().getOutboundDataType())) {
		    			outboundPropertiesPanel.getDataTypeComboBox().getModel().setSelectedItem("<Different Data Types>");
		    			outboundDataTypeDisplayName = null;
		    			outboundPropertiesList = null;
		    		}
	    		}
	    	}
	    	
	    	if (inboundDataTypeDisplayName != null) {
	    		inboundPropertiesPanel.getDataTypeComboBox().setSelectedItem(inboundDataTypeDisplayName);
	    	}
	    	
	    	if (outboundDataTypeDisplayName != null) {
	    		outboundPropertiesPanel.getDataTypeComboBox().setSelectedItem(outboundDataTypeDisplayName);
	    	}
    	}
        
        inboundPropertiesPanel.setDataTypeProperties(inboundDataTypeDisplayName, inboundPropertiesList);
        
        outboundPropertiesPanel.setDataTypeProperties(outboundDataTypeDisplayName, outboundPropertiesList);
    }
    
    /**
     * Retrieves the bulk selection checkbox (ALL, DESTINATIONS, RESPONSES) states based on the selections in the connector tree table and gets a list of connectors that are selected.
     */
    private void updateSelectionStatus(TreeTableNode node, Map<MirthTriStateCheckBox, Integer> status, List<Integer> containerIds) {
    	if (node instanceof DataTypeConnectorTableNode) {
    		DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) node;
    		TransformerContainer container = transformerContainer.get(tableNode.getContainerIndex());
    		boolean selected = (Boolean) tableNode.getValueAt(SELECTION_COLUMN);
    		int state = selected ? MirthTriStateCheckBox.CHECKED : MirthTriStateCheckBox.UNCHECKED;
    		if (selected) {
    			containerIds.add(tableNode.getContainerIndex());
    		}
    		
    		if (!status.containsKey(allCheckBox)) {
    			status.put(allCheckBox, state);
			} else if (status.get(allCheckBox) != MirthTriStateCheckBox.PARTIAL && status.get(allCheckBox) != state) {
				status.put(allCheckBox, MirthTriStateCheckBox.PARTIAL);
			}
    		
    		if (container.getType() == TransformerType.DESTINATION) {
	    		if (!status.containsKey(destinationsCheckBox)) {
	    			status.put(destinationsCheckBox, state);
				} else if (status.get(destinationsCheckBox) != MirthTriStateCheckBox.PARTIAL && status.get(destinationsCheckBox) != state) {
					status.put(destinationsCheckBox, MirthTriStateCheckBox.PARTIAL);
				}
    		}
    		
    		if (container.getType() == TransformerType.RESPONSE) {
	    		if (!status.containsKey(responsesCheckBox)) {
	    			status.put(responsesCheckBox, state);
				} else if (status.get(responsesCheckBox) != MirthTriStateCheckBox.PARTIAL && status.get(responsesCheckBox) != state) {
					status.put(responsesCheckBox, MirthTriStateCheckBox.PARTIAL);
				}
    		}
    	}
    	
    	for (int i = 0; i < node.getChildCount(); i++) {
    		updateSelectionStatus(node.getChildAt(i), status, containerIds);
    	}
    }
    
    /**
     * Container for transformer and type, as well as previous data types and properties for reverting.
     */
    private class TransformerContainer {
    	private Transformer transformer;
    	private TransformerType type;
    	private String lastInboundDataType;
    	private String lastOutboundDataType;
    	private DataTypeProperties lastInboundDataTypeProperties;
    	private DataTypeProperties lastOutboundDataTypeProperties;
    	
    	public TransformerContainer(Transformer transformer, TransformerType type, String lastInboundDataType, String lastOutboundDataType, DataTypeProperties lastInboundDataTypeProperties, DataTypeProperties lastOutboundDataTypeProperties) {
    		this.transformer = transformer;
    		this.type = type;
    		this.lastInboundDataType = lastInboundDataType;
    		this.lastOutboundDataType = lastOutboundDataType;
    		this.lastInboundDataTypeProperties = lastInboundDataTypeProperties;
    		this.lastOutboundDataTypeProperties = lastOutboundDataTypeProperties;
    	}

		public Transformer getTransformer() {
			return transformer;
		}

		public TransformerType getType() {
			return type;
		}

		public String getLastInboundDataType() {
			return lastInboundDataType;
		}

		public String getLastOutboundDataType() {
			return lastOutboundDataType;
		}

		public DataTypeProperties getLastInboundDataTypeProperties() {
			return lastInboundDataTypeProperties;
		}

		public DataTypeProperties getLastOutboundDataTypeProperties() {
			return lastOutboundDataTypeProperties;
		}
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        editingGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel5 = new javax.swing.JPanel();
        inboundPropertiesPanel = new com.mirth.connect.client.ui.DataTypePropertiesPanel();
        outboundPropertiesPanel = new com.mirth.connect.client.ui.DataTypePropertiesPanel();
        singleEditButton = new javax.swing.JRadioButton();
        bulkEditButton = new javax.swing.JRadioButton();
        connectorTreeTablePane = new javax.swing.JScrollPane();
        connectorTreeTable = new com.mirth.connect.client.ui.components.MirthTreeTable();
        allCheckBox = new com.mirth.connect.client.ui.components.MirthTriStateCheckBox();
        destinationsCheckBox = new com.mirth.connect.client.ui.components.MirthTriStateCheckBox();
        responsesCheckBox = new com.mirth.connect.client.ui.components.MirthTriStateCheckBox();
        expandLabel = new javax.swing.JLabel();
        collapseLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Set Data Types");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        okButton.setText("OK");
        okButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        okButton.setMaximumSize(new java.awt.Dimension(48, 21));
        okButton.setMinimumSize(new java.awt.Dimension(48, 21));
        okButton.setPreferredSize(new java.awt.Dimension(48, 21));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Cancel");
        closeButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setLayout(new java.awt.GridLayout(0, 2));
        jPanel5.add(inboundPropertiesPanel);
        jPanel5.add(outboundPropertiesPanel);

        singleEditButton.setBackground(new java.awt.Color(255, 255, 255));
        editingGroup.add(singleEditButton);
        singleEditButton.setSelected(true);
        singleEditButton.setText("Single Edit");
        singleEditButton.setFocusable(false);
        singleEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singleEditButtonActionPerformed(evt);
            }
        });

        bulkEditButton.setBackground(new java.awt.Color(255, 255, 255));
        editingGroup.add(bulkEditButton);
        bulkEditButton.setText("Bulk Edit");
        bulkEditButton.setFocusable(false);
        bulkEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bulkEditButtonActionPerformed(evt);
            }
        });

        connectorTreeTablePane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        connectorTreeTablePane.setViewportView(connectorTreeTable);

        allCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        allCheckBox.setText("All");
        allCheckBox.setFocusable(false);
        allCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allCheckBoxActionPerformed(evt);
            }
        });

        destinationsCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        destinationsCheckBox.setText("Destinations");
        destinationsCheckBox.setFocusable(false);
        destinationsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                destinationsCheckBoxActionPerformed(evt);
            }
        });

        responsesCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        responsesCheckBox.setText("Responses");
        responsesCheckBox.setFocusable(false);
        responsesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                responsesCheckBoxActionPerformed(evt);
            }
        });

        expandLabel.setForeground(java.awt.Color.blue);
        expandLabel.setText("<html><u>Expand All</u></html>");
        expandLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        expandLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                expandLabelMouseReleased(evt);
            }
        });

        collapseLabel.setForeground(java.awt.Color.blue);
        collapseLabel.setText("<html><u>Collapse All</u></html>");
        collapseLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        collapseLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                collapseLabelMouseReleased(evt);
            }
        });

        jLabel1.setText("|");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton))
                    .addComponent(connectorTreeTablePane)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(singleEditButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bulkEditButton)
                        .addGap(18, 18, 18)
                        .addComponent(allCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(destinationsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(responsesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 324, Short.MAX_VALUE)
                        .addComponent(expandLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(collapseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {closeButton, okButton});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(expandLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(collapseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(destinationsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(allCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responsesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(singleEditButton)
                    .addComponent(bulkEditButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectorTreeTablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(closeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(okButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {closeButton, okButton});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
	revertProperties();
    this.dispose();
}//GEN-LAST:event_closeButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    	for (TransformerContainer container : transformerContainer.values()) {
    		Transformer transformer = container.getTransformer();
    		
    		// Enable the save task if a anything was changed
    		if (!transformer.getInboundDataType().equals(container.getLastInboundDataType()) || !transformer.getInboundProperties().equals(container.getLastInboundDataTypeProperties()) || !transformer.getOutboundDataType().equals(container.getLastOutboundDataType()) || !transformer.getOutboundProperties().equals(container.getLastOutboundDataTypeProperties())) {
    			parent.setSaveEnabled(true);
    		}
    	}
    	
        this.dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void expandLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_expandLabelMouseReleased
    	connectorTreeTable.expandAll();
    }//GEN-LAST:event_expandLabelMouseReleased

    private void collapseLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_collapseLabelMouseReleased
        connectorTreeTable.collapseAll();
    }//GEN-LAST:event_collapseLabelMouseReleased

    private void allCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allCheckBoxActionPerformed
        MirthTriStateCheckBox checkBox = (MirthTriStateCheckBox) evt.getSource();
        
        Set<TransformerType> types = new HashSet<TransformerType>();
        types.add(TransformerType.SOURCE);
        types.add(TransformerType.DESTINATION);
        types.add(TransformerType.RESPONSE);
        
        toggleSelection(types, checkBox.getState() == MirthTriStateCheckBox.CHECKED);
    }//GEN-LAST:event_allCheckBoxActionPerformed

    private void destinationsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_destinationsCheckBoxActionPerformed
    	MirthTriStateCheckBox checkBox = (MirthTriStateCheckBox) evt.getSource();
        
        Set<TransformerType> types = new HashSet<TransformerType>();
        types.add(TransformerType.DESTINATION);
        
        toggleSelection(types, checkBox.getState() == MirthTriStateCheckBox.CHECKED);
    }//GEN-LAST:event_destinationsCheckBoxActionPerformed

    private void responsesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_responsesCheckBoxActionPerformed
    	MirthTriStateCheckBox checkBox = (MirthTriStateCheckBox) evt.getSource();
        
        Set<TransformerType> types = new HashSet<TransformerType>();
        types.add(TransformerType.RESPONSE);
        
        toggleSelection(types, checkBox.getState() == MirthTriStateCheckBox.CHECKED);
    }//GEN-LAST:event_responsesCheckBoxActionPerformed

    private void singleEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleEditButtonActionPerformed
        toggleEditMode(EditMode.SINGLE);
    }//GEN-LAST:event_singleEditButtonActionPerformed

    private void bulkEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bulkEditButtonActionPerformed
        toggleEditMode(EditMode.BULK);
    }//GEN-LAST:event_bulkEditButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthTriStateCheckBox allCheckBox;
    private javax.swing.JRadioButton bulkEditButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel collapseLabel;
    private com.mirth.connect.client.ui.components.MirthTreeTable connectorTreeTable;
    private javax.swing.JScrollPane connectorTreeTablePane;
    private com.mirth.connect.client.ui.components.MirthTriStateCheckBox destinationsCheckBox;
    private javax.swing.ButtonGroup editingGroup;
    private javax.swing.JLabel expandLabel;
    private com.mirth.connect.client.ui.DataTypePropertiesPanel inboundPropertiesPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton okButton;
    private com.mirth.connect.client.ui.DataTypePropertiesPanel outboundPropertiesPanel;
    private com.mirth.connect.client.ui.components.MirthTriStateCheckBox responsesCheckBox;
    private javax.swing.JRadioButton singleEditButton;
    // End of variables declaration//GEN-END:variables
}
