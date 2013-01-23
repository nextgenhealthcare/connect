package com.mirth.connect.client.ui;

import java.awt.Component;
import java.awt.Font;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.TreeCellRenderer;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;

public class DataTypePropertiesPanel extends javax.swing.JPanel {
    private DataTypeTreeTableModel tableModel;
    private boolean inbound;
    private String dataType;
    private String transformerName;
    private Style style;
    
    /**
     * Creates new form DataTypePropertiesPanel
     */
    public DataTypePropertiesPanel() {
        initComponents();
        
        // Add the bold style to the description pane
        style = descriptionPane.addStyle("BOLD", null);
        StyleConstants.setBold(style, true);
        
        tableModel = new DataTypeTreeTableModel();
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
                if (value instanceof DataTypeTreeTableNode) {
                    
                    if (!leaf) {
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                        label.setText(" " + ((DataTypeTreeTableNode)value).getValueAt(0));
                    } else {
                        label.setFont(label.getFont().deriveFont(Font.PLAIN));
                        label.setText((String) ((DataTypeTreeTableNode)value).getValueAt(0));
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
                    DataTypeTreeTableNode tableNode = (DataTypeTreeTableNode)e.getNewLeadSelectionPath().getLastPathComponent();
                    DataTypePropertyDescriptor descriptor = tableNode.getPropertyDescriptor();
                    
                    String descriptionTitle;
                    String descriptionBody;
                    
                    if (descriptor == null) {
                        descriptionTitle = tableNode.getGroupName() + " Properties";
                        descriptionBody = tableNode.getGroupDescription();
                    } else {
                        descriptionTitle = descriptor.getDisplayName();
                        descriptionBody = descriptor.getDescription();
                    } 
                    
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
        
        // Sets the alternating highlighter for the table
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            propertiesTreeTable.setHighlighters(highlighter);
        }
    }
    
    // Sets whether this pane should show inbound or outbound properties
    public void setInbound(boolean inbound) {
        this.inbound = inbound;
    }
    
    // Sets the title for this pane
    public void setTitle(String transformerName) {
        this.transformerName = transformerName;
    }
    
    public void updateTitle() {
        setBorder(BorderFactory.createTitledBorder(dataType + " - " + (transformerName == null ? "" : transformerName + " ") + (inbound ? "Inbound" : "Outbound") + " Properties"));
    }
    
    // Load a new property set
    public void setDataTypeProperties(String dataType, DataTypeProperties properties) {
        this.dataType = dataType;
        updateTitle();
        
        tableModel.clear();
        
        tableModel.addProperties(inbound, properties);
        propertiesTreeTable.expandAll();
    }
    
    public void save() {
        if (propertiesTreeTable.isEditing()) {
            propertiesTreeTable.getCellEditor().stopCellEditing();
        }
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

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));

        propertiesTreeTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane3.setViewportView(propertiesTreeTable);

        descriptionPane.setEditable(false);
        jScrollPane1.setViewportView(descriptionPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane descriptionPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private com.mirth.connect.client.ui.components.MirthTreeTable propertiesTreeTable;
    // End of variables declaration//GEN-END:variables
}
