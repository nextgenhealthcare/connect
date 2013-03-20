package com.mirth.connect.client.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.model.datatype.DataTypeProperties;

public class DataTypePropertiesPanel extends javax.swing.JPanel {
    private DataTypePropertiesTableModel tableModel;
    private boolean inbound;
    private Style style;
    
    /**
     * Creates new form DataTypePropertiesPanel
     */
    public DataTypePropertiesPanel() {
        initComponents();
        
        // Add the bold style to the description pane
        style = descriptionPane.addStyle("BOLD", null);
        StyleConstants.setBold(style, true);
        
        tableModel = new DataTypePropertiesTableModel() {
        	
        	@Override
        	public void setValueAt(Object value, Object node, int column) {
        		super.setValueAt(value, node, column);
        		
        		updateDefaultButton();
        	}
        };
        tableModel.setColumnIdentifiers(Arrays.asList(new String[] { "Name", "Value" }));
        propertiesTreeTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        propertiesTreeTable.setColumnFactory(new DataTypeColumnFactory());
        propertiesTreeTable.setTreeTableModel(tableModel);
        propertiesTreeTable.setDragEnabled(false);
        propertiesTreeTable.setSortable(false);
        propertiesTreeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propertiesTreeTable.setDoubleBuffered(true);
        propertiesTreeTable.setLeafIcon(null);
        propertiesTreeTable.setOpenIcon(null);
        propertiesTreeTable.setClosedIcon(null);
        propertiesTreeTable.setAutoCreateColumnsFromModel(false);
        propertiesTreeTable.setShowGrid(true, true);
        propertiesTreeTable.setTableHeader(null);
        propertiesTreeTable.setEditable(true);
        propertiesTreeTable.setShowsRootHandles(false);
        
        // This renderer bolds adds a space and bolds the property group names
        propertiesTreeTable.setTreeCellRenderer(new TreeCellRenderer() {
            
            private JLabel label = new JLabel();
            
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (value instanceof DataTypePropertiesTableNode) {
                    
                    if (!leaf) {
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                        label.setText(" " + ((DataTypePropertiesTableNode)value).getValueAt(0));
                    } else {
                        label.setFont(label.getFont().deriveFont(Font.PLAIN));
                        label.setText((String) ((DataTypePropertiesTableNode)value).getValueAt(0));
                    }
                }
                return label;
            }
            
        });
        
        // This listener updates the property description pane.
        propertiesTreeTable.getTreeSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                descriptionPane.setText("");
                
                if (e.getNewLeadSelectionPath() != null) {
                    StyledDocument document = descriptionPane.getStyledDocument();
                    DataTypePropertiesTableNode tableNode = (DataTypePropertiesTableNode)e.getNewLeadSelectionPath().getLastPathComponent();
                    
                    String descriptionTitle = tableNode.getName();
                    String descriptionBody = tableNode.getDescription();
                    
                    try {
                        if (descriptionTitle != null && descriptionBody != null) {
                            document.insertString(0, descriptionTitle, style);
                            document.insertString(document.getLength(), "\n" + descriptionBody, null);
                        }
                    } catch (BadLocationException e1) {
                        
                    }
                    
                    descriptionPane.setCaretPosition(0);
                }
            }
            
        });
        
        // This listener forces the focus onto the value column so the user can start editing no matter which column they selected
        propertiesTreeTable.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getFirstIndex() != 1) {
                    propertiesTreeTable.getColumnModel().getSelectionModel().setLeadSelectionIndex(1);
                }
            }
            
        });
        
        // This listener shows the popup menu to restore a single value or group to its default value when the mouse is right clicked.
        propertiesTreeTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					final int x = e.getX();
					final int y = e.getY();
					
					final TreePath path = propertiesTreeTable.getPathForLocation(x, y);
					propertiesTreeTable.getTreeSelectionModel().setSelectionPath(path);
					
					JPopupMenu popupMenu = new JPopupMenu();
					 
					JMenuItem menuItem = new JMenuItem("Restore Defaults");
					menuItem.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							DataTypePropertiesTableNode tableNode = (DataTypePropertiesTableNode) path.getLastPathComponent();
							
							tableModel.resetToDefault(tableNode);
							
							updateDefaultButton();
						}
					 
					});
					 
					popupMenu.add(menuItem);
					popupMenu.show(e.getComponent(), x, y);

				}
			}
        	
        });
        
        // Sets the alternating highlighter for the table
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            propertiesTreeTable.setHighlighters(highlighter);
        }
    }
    
    public JComboBox getDataTypeComboBox() {
    	return dataTypeComboBox;
    }
    
    public JButton getDefaultButton() {
    	return defaultButton;
    }
    
    /**
     * Enable or disable the reset defaults button based on whether the properties are already the defaults
     */
    public void updateDefaultButton() {
    	defaultButton.setEnabled(!tableModel.isDefaultProperties(null));
    }
    
    /**
     *  Sets whether this pane should show inbound or outbound properties
     */
    public void setInbound(boolean inbound) {
        this.inbound = inbound;
    }
    
    /**
     * Shows or hides the title border
     */
    public void setUseTitleBorder(boolean useTitleBorder) {
    	if (useTitleBorder) {
			// If no data type was provided, just show inbound or outbound
			setBorder(BorderFactory.createTitledBorder((inbound ? "Inbound" : "Outbound") + " Properties"));
    	} else {
    		setBorder(BorderFactory.createEmptyBorder());
    	}
    }
    
    /**
     * Wraps a single DataTypeProperties in a list and forwards the method call
     */
    public void setDataTypeProperties(String dataType, DataTypeProperties properties) {
    	// If a single DataTypeProperties object is provided, wrap it in an list
    	List<DataTypeProperties> propertiesList = null;
    	if (properties != null) {
    		propertiesList = new ArrayList<DataTypeProperties>();
			propertiesList.add(properties);
    	}
    	setDataTypeProperties(dataType, propertiesList);
    }
    
    /**
     *  Load a new property set. Multiple DataTypeProperties objects can be loaded and they will all be updated when the user makes a change
     */
    public void setDataTypeProperties(String displayName, List<DataTypeProperties> properties) {
        // Gets the default properties for a data type 
        DataTypeProperties defaultProperties = null;
        if (displayName != null) {
        	defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(PlatformUI.MIRTH_FRAME.displayNameToDataType.get(displayName)).getDefaultProperties();
        }
        
        // Remove all nodes from the tree table
        tableModel.clear();
        
        // Adds the properties to the tree table
        tableModel.addProperties(inbound, properties, defaultProperties);
        
        // Enable or disable the default button depending on whether the properties already equal the defaults
        updateDefaultButton();
        
        // Show all nodes
        propertiesTreeTable.expandAll();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        propertiesTreeTable = new com.mirth.connect.client.ui.components.MirthTreeTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionPane = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        defaultButton = new javax.swing.JButton();
        dataTypeComboBox = new javax.swing.JComboBox();

        setBackground(new java.awt.Color(255, 255, 255));

        propertiesTreeTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane3.setViewportView(propertiesTreeTable);

        descriptionPane.setEditable(false);
        jScrollPane1.setViewportView(descriptionPane);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        defaultButton.setText("Restore Defaults");

        dataTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(dataTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(defaultButton))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(defaultButton)
                .addComponent(dataTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {dataTypeComboBox, defaultButton});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
            .addComponent(jScrollPane3)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox dataTypeComboBox;
    private javax.swing.JButton defaultButton;
    private javax.swing.JTextPane descriptionPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private com.mirth.connect.client.ui.components.MirthTreeTable propertiesTreeTable;
    // End of variables declaration//GEN-END:variables
}
