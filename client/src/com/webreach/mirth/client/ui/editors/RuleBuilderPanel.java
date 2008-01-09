/*
 * MapperPanel.java
 *
 * Created on February 6, 2007, 12:30 PM
 */

package com.webreach.mirth.client.ui.editors;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.plugins.filter.rule.rulebuilder.RuleBuilderPlugin;

/**
 *
 * @author brendanh
 */
public class RuleBuilderPanel extends BasePanel
{
    protected String label;
    protected MirthEditorPane parent;
    public final int VALUE_COLUMN = 0;
    public final String VALUE_COLUMN_NAME = "Value";
    private final RuleBuilderPlugin rulePlugin;
    private int lastIndex = -1;
    private String name = "";
    private String originalField = "";
    
    /** Creates new form MapperPanel */
    public RuleBuilderPanel(MirthEditorPane p,final RuleBuilderPlugin rulePlugin)
    {
        parent = p;
        this.rulePlugin = rulePlugin;
        initComponents();
        
        
        fieldTextField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent arg0)
            {
            	parent.modified = true;
                rulePlugin.updateName();
            }
            
            public void insertUpdate(DocumentEvent arg0)
            {
                parent.modified = true;
                rulePlugin.updateName();
            }
            
            public void removeUpdate(DocumentEvent arg0)
            {
                parent.modified = true;
                rulePlugin.updateName();
            }
        });

        accept.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                parent.modified = true;
                rulePlugin.updateName();
            }
        });
        
        reject.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                parent.modified = true;
                rulePlugin.updateName();
            }
        });
        doesNotEqual.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                parent.modified = true;
                rulePlugin.updateName();
            }
        });
        doesNotExist.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                parent.modified = true;

                rulePlugin.updateName();
            }
        });
        equals.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                parent.modified = true;
                rulePlugin.updateName();
            }
        });
        exists.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                parent.modified = true;
                rulePlugin.updateName();
            }
        });
        
        
        valuesScrollPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
        deleteButton.setEnabled(false);
   
    }
    
    public void updateTable()
    {
        if (parent.getSelectedRow() != -1 && !parent.getTableModel().getValueAt(parent.getSelectedRow(), parent.STEP_TYPE_COL).toString().equals("JavaScript"))
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    parent.getTableModel().setValueAt(  rulePlugin.getName(), parent.getSelectedRow(), parent.STEP_NAME_COL);
                    parent.updateTaskPane(parent.getTableModel().getValueAt(parent.getSelectedRow(), parent.STEP_TYPE_COL).toString());
                }
            });
        }
    }
    
    public Map<Object, Object> getData()
    {
        Map<Object, Object> m = new HashMap<Object, Object>();
        m.put("Field", fieldTextField.getText().trim());
        m.put("Name", name);
        m.put("OriginalField", originalField);
        
        if(equals.isSelected())
            m.put("Equals", UIConstants.YES_OPTION);
        else if(doesNotEqual.isSelected())
            m.put("Equals", UIConstants.NO_OPTION);
        else if(exists.isSelected())
            m.put("Equals", UIConstants.EXISTS_OPTION);
        else if(doesNotExist.isSelected())
            m.put("Equals", UIConstants.DOES_NOT_EXISTS_OPTION);
        
        m.put("Values", getValues());
        
        if(accept.isSelected())
            m.put("Accept", UIConstants.YES_OPTION);
        else
            m.put("Accept", UIConstants.NO_OPTION);
        
        return m;
    }
    
    public void setData(Map<Object, Object> data)
    {
        boolean modified = parent.modified;
        
        // Must set the text last so that the text field change action is
        // not called before the new button values are set.
        if (data != null)
        {            
            if(((String)data.get("Equals")).equals(UIConstants.YES_OPTION))
            {
                equalsActionPerformed(null);
                equals.setSelected(true);
            }
            else if(((String)data.get("Equals")).equals(UIConstants.NO_OPTION))
            {
                doesNotEqualActionPerformed(null);
                doesNotEqual.setSelected(true);
            }
            else if(((String)data.get("Equals")).equals(UIConstants.EXISTS_OPTION))
            {
                existsActionPerformed(null);
                exists.setSelected(true);
            }
            else if(((String)data.get("Equals")).equals(UIConstants.DOES_NOT_EXISTS_OPTION))
            {
                doesNotExistActionPerformed(null);
                doesNotExist.setSelected(true);
            }
            
            ArrayList<String> values = (ArrayList<String>) data.get("Values");
            if (values != null)
                setValues(values);
            else
                setValues(new ArrayList<String>());
            
            if(((String)data.get("Accept")).equals(UIConstants.YES_OPTION))
                accept.setSelected(true);
            else
                reject.setSelected(true);
            
            originalField = (String) data.get("OriginalField");
            name = (String) data.get("Name");
            fieldTextField.setText((String) data.get("Field"));
        }
        else
        {
            equals.setSelected(true);
            ArrayList<String> values = new ArrayList<String>();
            values.add("\"Example Value\"");
            setValues(values);
            accept.setSelected(true);
            fieldTextField.setText("");
        }
        
        parent.modified = modified;
    }
    
    public void setValues(ArrayList<String> values)
    {
        Object[][] tableData = new Object[values.size()][2];
        
        valuesTable = new MirthTable();
        
        for(int i = 0; i < values.size(); i++)
        {
            tableData[i][VALUE_COLUMN] = values.get(i);
        }
        
        valuesTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] { VALUE_COLUMN_NAME })
        {
            boolean[] canEdit = new boolean[] { true, true };
            
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });
        
        valuesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (getSelectedRow() != -1)
                {
                    lastIndex = getSelectedRow();
                    deleteButton.setEnabled(true);
                }
                else
                    deleteButton.setEnabled(false);
                
                rulePlugin.updateName();
            }
        });
        
        class RegExTableCellEditor extends AbstractCellEditor implements TableCellEditor
        {
            JComponent component = new JTextField();
            
            Object originalValue;
            
            public RegExTableCellEditor()
            {
                super();
            }
            
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
            {
                // 'value' is value contained in the cell located at (rowIndex,
                // vColIndex)
                originalValue = value;
                
                if (isSelected)
                {
                    // cell (and perhaps other cells) are selected
                }
                
                // Configure the component with the specified value
                ((JTextField) component).setText((String) value);
                
                // Return the configured component
                return component;
            }
            
            public Object getCellEditorValue()
            {
                return ((JTextField) component).getText();
            }
            
            public boolean stopCellEditing()
            {
                String s = (String) getCellEditorValue();
                
                parent.modified = true;
                
                deleteButton.setEnabled(true);
                
                return super.stopCellEditing();
            }
            
            /**
             * Enables the editor only for double-clicks.
             */
            public boolean isCellEditable(EventObject evt)
            {
                if (evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2)
                {
                    deleteButton.setEnabled(false);
                    return true;
                }
                return false;
            }
        };
        
        // Set the custom cell editor for the Destination Name column.
        valuesTable.getColumnModel().getColumn(valuesTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new RegExTableCellEditor());
        
        valuesTable.setSelectionMode(0);
        valuesTable.setRowSelectionAllowed(true);
        valuesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        valuesTable.setDragEnabled(false);
        valuesTable.setOpaque(true);
        valuesTable.setSortable(false);
        valuesTable.getTableHeader().setReorderingAllowed(false);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            valuesTable.setHighlighters(highlighter);
        }
        
        valuesScrollPane.setViewportView(valuesTable);
    }
    
    public ArrayList<String> getValues()
    {
        ArrayList<String> values = new ArrayList<String>();
        
        for (int i = 0; i < valuesTable.getRowCount(); i++)
            if (((String) valuesTable.getValueAt(i, VALUE_COLUMN)).length() > 0)
                values.add((String) valuesTable.getValueAt(i, VALUE_COLUMN));
        
        return values;
    }
    
    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows()
    {
        valuesTable.clearSelection();
        deleteButton.setEnabled(false);
    }
    
    /** Get the currently selected destination index */
    public int getSelectedRow()
    {
        if (valuesTable.isEditing())
            return valuesTable.getEditingRow();
        else
            return valuesTable.getSelectedRow();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        valuesScrollPane = new javax.swing.JScrollPane();
        valuesTable = new com.webreach.mirth.client.ui.components.MirthTable();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        valuesLabel = new javax.swing.JLabel();
        fieldTextField = new javax.swing.JTextField();
        equals = new javax.swing.JRadioButton();
        doesNotEqual = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        accept = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        reject = new javax.swing.JRadioButton();
        exists = new javax.swing.JRadioButton();
        doesNotExist = new javax.swing.JRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Field:");

        valuesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Value"
            }
        ));
        valuesScrollPane.setViewportView(valuesTable);

        newButton.setText("New");
        newButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                newButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteButtonActionPerformed(evt);
            }
        });

        valuesLabel.setText("Values:");

        equals.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(equals);
        equals.setText("Equals");
        equals.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        equals.setMargin(new java.awt.Insets(0, 0, 0, 0));
        equals.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                equalsActionPerformed(evt);
            }
        });

        doesNotEqual.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(doesNotEqual);
        doesNotEqual.setText("Does Not Equal");
        doesNotEqual.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        doesNotEqual.setMargin(new java.awt.Insets(0, 0, 0, 0));
        doesNotEqual.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                doesNotEqualActionPerformed(evt);
            }
        });

        jLabel2.setText("Condition:");

        accept.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup2.add(accept);
        accept.setText("Accept");
        accept.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accept.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel3.setText("Behavior:");

        reject.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup2.add(reject);
        reject.setText("Reject");
        reject.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reject.setMargin(new java.awt.Insets(0, 0, 0, 0));

        exists.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(exists);
        exists.setText("Exists");
        exists.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exists.setMargin(new java.awt.Insets(0, 0, 0, 0));
        exists.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                existsActionPerformed(evt);
            }
        });

        doesNotExist.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(doesNotExist);
        doesNotExist.setText("Does Not Exist");
        doesNotExist.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        doesNotExist.setMargin(new java.awt.Insets(0, 0, 0, 0));
        doesNotExist.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                doesNotExistActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(valuesLabel)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(accept)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(reject))
                    .add(layout.createSequentialGroup()
                        .add(exists)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(doesNotExist)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(equals)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(doesNotEqual))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(valuesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(newButton)
                            .add(deleteButton)))
                    .add(fieldTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {deleteButton, newButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(accept)
                    .add(reject)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(fieldTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(equals)
                    .add(doesNotEqual)
                    .add(jLabel2)
                    .add(exists)
                    .add(doesNotExist))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(newButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteButton))
                    .add(valuesLabel)
                    .add(valuesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void doesNotExistActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_doesNotExistActionPerformed
    {//GEN-HEADEREND:event_doesNotExistActionPerformed
        valuesScrollPane.setEnabled(false);
        valuesTable.setEnabled(false);
        valuesLabel.setEnabled(false);
        newButton.setEnabled(false);
        deleteButton.setEnabled(false);
        parent.modified = true;
    }//GEN-LAST:event_doesNotExistActionPerformed
    
    private void doesNotEqualActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_doesNotEqualActionPerformed
    {//GEN-HEADEREND:event_doesNotEqualActionPerformed
        valuesScrollPane.setEnabled(true);
        valuesTable.setEnabled(true);
        valuesLabel.setEnabled(true);
        newButton.setEnabled(true);
        deleteButton.setEnabled(true);
        parent.modified = true;
    }//GEN-LAST:event_doesNotEqualActionPerformed
    
    private void equalsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_equalsActionPerformed
    {//GEN-HEADEREND:event_equalsActionPerformed
        valuesScrollPane.setEnabled(true);
        valuesTable.setEnabled(true);
        valuesLabel.setEnabled(true);
        newButton.setEnabled(true);
        deleteButton.setEnabled(true);
        parent.modified = true;
    }//GEN-LAST:event_equalsActionPerformed
    
    private void existsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_existsActionPerformed
    {//GEN-HEADEREND:event_existsActionPerformed
        valuesScrollPane.setEnabled(false);
        valuesTable.setEnabled(false);
        valuesLabel.setEnabled(false);
        newButton.setEnabled(false);
        deleteButton.setEnabled(false);
        parent.modified = true;
    }//GEN-LAST:event_existsActionPerformed
    
    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteButtonActionPerformed
    {//GEN-HEADEREND:event_deleteButtonActionPerformed
        if (getSelectedRow() != -1 && !valuesTable.isEditing())
        {
            ((DefaultTableModel) valuesTable.getModel()).removeRow(getSelectedRow());
            
            if (valuesTable.getRowCount() != 0)
            {
                if (lastIndex == 0)
                    valuesTable.setRowSelectionInterval(0, 0);
                else if (lastIndex == valuesTable.getRowCount())
                    valuesTable.setRowSelectionInterval(lastIndex - 1, lastIndex - 1);
                else
                    valuesTable.setRowSelectionInterval(lastIndex, lastIndex);
            }
            rulePlugin.updateName();
            parent.modified = true;
        }
    }//GEN-LAST:event_deleteButtonActionPerformed
    
    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newButtonActionPerformed
    {//GEN-HEADEREND:event_newButtonActionPerformed
        ((DefaultTableModel) valuesTable.getModel()).addRow(new Object[] { "", "" });
        valuesTable.setRowSelectionInterval(valuesTable.getRowCount() - 1, valuesTable.getRowCount() - 1);
        rulePlugin.updateName();
        parent.modified = true;
    }//GEN-LAST:event_newButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton accept;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton deleteButton;
    private javax.swing.JRadioButton doesNotEqual;
    private javax.swing.JRadioButton doesNotExist;
    private javax.swing.JRadioButton equals;
    private javax.swing.JRadioButton exists;
    private javax.swing.JTextField fieldTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton newButton;
    private javax.swing.JRadioButton reject;
    private javax.swing.JLabel valuesLabel;
    private javax.swing.JScrollPane valuesScrollPane;
    private com.webreach.mirth.client.ui.components.MirthTable valuesTable;
    // End of variables declaration//GEN-END:variables
    
}
