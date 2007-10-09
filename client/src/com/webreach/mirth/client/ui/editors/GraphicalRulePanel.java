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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
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
import org.syntax.jedit.SyntaxDocument;

import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthTable;

/**
 * 
 * @author brendanh
 */
public class GraphicalRulePanel extends BasePanel
{
    public boolean updating = false;

    protected String label;

    protected MirthEditorPane parent;

    public final int VALUE_COLUMN = 0;

    public final String VALUE_COLUMN_NAME = "Value";

    private int lastIndex = -1;
    
    /** Creates new form MapperPanel */
    public GraphicalRulePanel(MirthEditorPane p)
    {
        parent = p;
        initComponents();


        fieldTextField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent arg0)
            {
            }

            public void insertUpdate(DocumentEvent arg0)
            {
                parent.modified = true;
            }

            public void removeUpdate(DocumentEvent arg0)
            {
                parent.modified = true;
            }
        });
        
        equals.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                parent.modified = true;
            }
        });
        
        doesNotEqual.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                parent.modified = true;
            }
        });
        
        accept.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                parent.modified = true;
            }
        });
        
        reject.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                parent.modified = true;
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
                    parent.getTableModel().setValueAt(fieldTextField.getText(), parent.getSelectedRow(), parent.STEP_NAME_COL);
                    parent.updateTaskPane(parent.getTableModel().getValueAt(parent.getSelectedRow(), parent.STEP_TYPE_COL).toString());
                }
            });
        }
    }

    public Map<Object, Object> getData()
    {
        Map<Object, Object> m = new HashMap<Object, Object>();
        m.put("Field", fieldTextField.getText().trim());
        
        if(equals.isSelected())
            m.put("Equals", UIConstants.YES_OPTION);
        else
            m.put("Equals", UIConstants.NO_OPTION);
        
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

        if (data != null)
        {
            fieldTextField.setText((String) data.get("Field"));
            
            if(((String)data.get("Equals")).equals(UIConstants.YES_OPTION))
                equals.setSelected(true);
            else
                doesNotEqual.setSelected(true);
                
            ArrayList<String> values = (ArrayList<String>) data.get("Values");
            if (values != null)
                setValues(values);
            else
                setValues(new ArrayList<String>());
            
            if(((String)data.get("Accept")).equals(UIConstants.YES_OPTION))
                accept.setSelected(true);
            else
                reject.setSelected(true);
        }
        else
        {
            fieldTextField.setText("");
            equals.setSelected(true);
            ArrayList<String> values = new ArrayList<String>();
            values.add("\"Example Value\"");
            setValues(values);
            accept.setSelected(true);
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
        jLabel4 = new javax.swing.JLabel();
        fieldTextField = new javax.swing.JTextField();
        equals = new javax.swing.JRadioButton();
        doesNotEqual = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        accept = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        reject = new javax.swing.JRadioButton();
        exists = new javax.swing.JRadioButton();

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

        jLabel4.setText("Values:");

        equals.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(equals);
        equals.setText("Equals");
        equals.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        equals.setMargin(new java.awt.Insets(0, 0, 0, 0));

        doesNotEqual.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(doesNotEqual);
        doesNotEqual.setText("Does Not Equal");
        doesNotEqual.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        doesNotEqual.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel2.setText("Condition:");

        accept.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup2.add(accept);
        accept.setText("Pass");
        accept.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accept.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel3.setText("Behavior:");

        reject.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup2.add(reject);
        reject.setText("Fail");
        reject.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reject.setMargin(new java.awt.Insets(0, 0, 0, 0));

        exists.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(exists);
        exists.setText("Exists");
        exists.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exists.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(jLabel4)
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
                    .add(exists))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(newButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteButton))
                    .add(jLabel4)
                    .add(valuesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

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

            parent.modified = true;
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newButtonActionPerformed
    {//GEN-HEADEREND:event_newButtonActionPerformed
        ((DefaultTableModel) valuesTable.getModel()).addRow(new Object[] { "", "" });
        valuesTable.setRowSelectionInterval(valuesTable.getRowCount() - 1, valuesTable.getRowCount() - 1);
        parent.modified = true;
    }//GEN-LAST:event_newButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton accept;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton deleteButton;
    private javax.swing.JRadioButton doesNotEqual;
    private javax.swing.JRadioButton equals;
    private javax.swing.JRadioButton exists;
    private javax.swing.JTextField fieldTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JButton newButton;
    private javax.swing.JRadioButton reject;
    private javax.swing.JScrollPane valuesScrollPane;
    private com.webreach.mirth.client.ui.components.MirthTable valuesTable;
    // End of variables declaration//GEN-END:variables

}
