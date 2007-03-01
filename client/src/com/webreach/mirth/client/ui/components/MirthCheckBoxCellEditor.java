/*
 * MirthCheckBoxCellEditor.java
 *
 * Created on February 28, 2007, 3:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.components;

import java.awt.Component;
import java.util.EventObject;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author brendanh
 */
public class MirthCheckBoxCellEditor implements TableCellEditor
{
    private JCheckBox box;

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        box = new JCheckBox();
        box.setFocusable(false);
        box.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        box.setSelected(((Boolean)value).booleanValue()); 
        box.setForeground(table.getForeground());
        box.setBackground(table.getBackground());
        return box;
    }
    
    public void addCellEditorListener(CellEditorListener l)
    {}
    
    public void cancelCellEditing()
    {}
    
    public Object getCellEditorValue()
    {
        return box.isSelected();
    }
    
    public boolean isCellEditable(EventObject anEvent)
    {
        return true;
    }
    
    public void removeCellEditorListener(CellEditorListener l)
    {}
    
    public boolean shouldSelectCell(EventObject anEvent)
    {
        return true;
    }
    
    public boolean stopCellEditing()
    {
        return true;
    }   
}
