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

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.syntax.jedit.SyntaxDocument;

import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthTable;

/**
 * 
 * @author brendanh
 */
public class MessageBuilder extends BasePanel
{
    public boolean updating = false;

    protected String label;

    protected static SyntaxDocument mappingDoc;

    protected MirthEditorPane parent;

    public final int REGEX_COLUMN = 0;

    public final int REPLACEWITH_COLUMN = 1;

    public final String REGEX_COLUMN_NAME = "Regular Expression";

    public final String REPLACEWITH_COLUMN_NAME = "Replace With";

    private int lastIndex = -1;

    /** Creates new form MapperPanel */
    public MessageBuilder(MirthEditorPane p)
    {
        parent = p;
        initComponents();
        variableTextField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent arg0)
            {
            }

            public void insertUpdate(DocumentEvent arg0)
            {
                updateTable();
                parent.modified = true;
            }

            public void removeUpdate(DocumentEvent arg0)
            {
                updateTable();
                parent.modified = true;
            }
        });

        mappingTextField.getDocument().addDocumentListener(new DocumentListener()
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
        
        defaultValueTextField.getDocument().addDocumentListener(new DocumentListener()
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

        regularExpressionsScrollPane.addMouseListener(new java.awt.event.MouseAdapter()
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
                    //parent.getTableModel().setValueAt(variableTextField.getText(), parent.getSelectedRow(), parent.STEP_NAME_COL);
                    parent.updateTaskPane(parent.getTableModel().getValueAt(parent.getSelectedRow(), parent.STEP_TYPE_COL).toString());
                }
            });
        }
    }

    public Map<Object, Object> getData()
    {
        Map<Object, Object> m = new HashMap<Object, Object>();
        m.put("Variable", variableTextField.getText().trim());
        m.put("Mapping", mappingTextField.getText().trim());
        m.put("DefaultValue", defaultValueTextField.getText().trim());
        m.put("RegularExpressions", getRegexProperties());
        return m;
    }

    public void setData(Map<Object, Object> data)
    {
        boolean modified = parent.modified;

        if (data != null)
        {
            variableTextField.setText((String) data.get("Variable"));
            mappingTextField.setText((String) data.get("Mapping"));
            defaultValueTextField.setText((String) data.get("DefaultValue"));

            ArrayList<String[]> p = (ArrayList<String[]>) data.get("RegularExpressions");
            if (p != null)
                setRegexProperties(p);
            else
                setRegexProperties(new ArrayList<String[]>());
        }
        else
        {
            variableTextField.setText("");
            mappingTextField.setText("");
            defaultValueTextField.setText("");
            setRegexProperties(new ArrayList<String[]>());
        }

        parent.modified = modified;
    }

    public void setRegexProperties(ArrayList<String[]> properties)
    {
        Object[][] tableData = new Object[properties.size()][2];

        regularExpressionsTable = new MirthTable();

        for(int i = 0; i < properties.size(); i++)
        {
            tableData[i][REGEX_COLUMN] = properties.get(i)[0];
            tableData[i][REPLACEWITH_COLUMN] = properties.get(i)[1];
        }

        regularExpressionsTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] { REGEX_COLUMN_NAME, REPLACEWITH_COLUMN_NAME })
        {
            boolean[] canEdit = new boolean[] { true, true };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });

        regularExpressionsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
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
        regularExpressionsTable.getColumnModel().getColumn(regularExpressionsTable.getColumnModel().getColumnIndex(REGEX_COLUMN_NAME)).setCellEditor(new RegExTableCellEditor());

        // Set the custom cell editor for the Destination Name column.
        regularExpressionsTable.getColumnModel().getColumn(regularExpressionsTable.getColumnModel().getColumnIndex(REPLACEWITH_COLUMN_NAME)).setCellEditor(new RegExTableCellEditor());

        regularExpressionsTable.setSelectionMode(0);
        regularExpressionsTable.setRowSelectionAllowed(true);
        regularExpressionsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        regularExpressionsTable.setDragEnabled(false);
        regularExpressionsTable.setOpaque(true);
        regularExpressionsTable.setSortable(false);
        regularExpressionsTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
        	Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
        	regularExpressionsTable.setHighlighters(highlighter);
        }

        regularExpressionsScrollPane.setViewportView(regularExpressionsTable);
    }

    public ArrayList<String[]> getRegexProperties()
    {
        ArrayList<String[]> properties = new ArrayList<String[]>();

        for (int i = 0; i < regularExpressionsTable.getRowCount(); i++)
            if (((String) regularExpressionsTable.getValueAt(i, REGEX_COLUMN)).length() > 0)
                properties.add(new String[]{((String) regularExpressionsTable.getValueAt(i, REGEX_COLUMN)), ((String) regularExpressionsTable.getValueAt(i, REPLACEWITH_COLUMN))});
        
        return properties;
    }

    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows()
    {
        regularExpressionsTable.clearSelection();
        deleteButton.setEnabled(false);
    }

    /** Get the currently selected destination index */
    public int getSelectedRow()
    {
        if (regularExpressionsTable.isEditing())
            return regularExpressionsTable.getEditingRow();
        else
            return regularExpressionsTable.getSelectedRow();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        regularExpressionsScrollPane = new javax.swing.JScrollPane();
        regularExpressionsTable = new com.webreach.mirth.client.ui.components.MirthTable();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        variableTextField = new javax.swing.JTextField();
        mappingTextField = new javax.swing.JTextField();
        defaultValueTextField = new javax.swing.JTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Message Segment:");

        jLabel2.setText("Mapping:");

        jLabel3.setText("Default Value:");

        regularExpressionsTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] { "Property", "Value" }));
        regularExpressionsScrollPane.setViewportView(regularExpressionsTable);

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

        jLabel4.setText("String Replacement:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(16, 16, 16).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(jLabel3).add(jLabel2).add(jLabel1)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup().add(regularExpressionsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(deleteButton).add(newButton))).add(variableTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE).add(mappingTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE).add(defaultValueTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE))).add(layout.createSequentialGroup().addContainerGap().add(jLabel4))).addContainerGap()));

        layout.linkSize(new java.awt.Component[] { deleteButton, newButton }, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel1).add(variableTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel2).add(mappingTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel3).add(defaultValueTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel4).add(newButton)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(deleteButton)).add(regularExpressionsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)).addContainerGap()));
    }// </editor-fold>//GEN-END:initComponents

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_deleteButtonActionPerformed
    {// GEN-HEADEREND:event_deleteButtonActionPerformed
        if (getSelectedRow() != -1 && !regularExpressionsTable.isEditing())
        {
            ((DefaultTableModel) regularExpressionsTable.getModel()).removeRow(getSelectedRow());

            if (regularExpressionsTable.getRowCount() != 0)
            {
                if (lastIndex == 0)
                    regularExpressionsTable.setRowSelectionInterval(0, 0);
                else if (lastIndex == regularExpressionsTable.getRowCount())
                    regularExpressionsTable.setRowSelectionInterval(lastIndex - 1, lastIndex - 1);
                else
                    regularExpressionsTable.setRowSelectionInterval(lastIndex, lastIndex);
            }

            parent.modified = true;
        }
    }// GEN-LAST:event_deleteButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_newButtonActionPerformed
    {// GEN-HEADEREND:event_newButtonActionPerformed
        ((DefaultTableModel) regularExpressionsTable.getModel()).addRow(new Object[] { "", "" });
        regularExpressionsTable.setRowSelectionInterval(regularExpressionsTable.getRowCount() - 1, regularExpressionsTable.getRowCount() - 1);
        parent.modified = true;
    }// GEN-LAST:event_newButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField defaultValueTextField;

    private javax.swing.JButton deleteButton;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel4;

    private javax.swing.JTextField mappingTextField;

    private javax.swing.JButton newButton;

    private javax.swing.JScrollPane regularExpressionsScrollPane;

    private com.webreach.mirth.client.ui.components.MirthTable regularExpressionsTable;

    private javax.swing.JTextField variableTextField;
    // End of variables declaration//GEN-END:variables

}
