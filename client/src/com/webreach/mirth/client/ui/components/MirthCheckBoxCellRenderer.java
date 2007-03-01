/*
 * MirthCheckBoxCellRenderer.java
 *
 * Created on February 28, 2007, 3:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.components;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author brendanh
 */

public class MirthCheckBoxCellRenderer implements TableCellRenderer
{
    // the method gives the component  like whome the cell must be rendered
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean isFocused, int row, int col)
    {
        JCheckBox box = new JCheckBox();
        box.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        
        boolean marked = Boolean.valueOf( value.toString() ).booleanValue();
        if (marked)
        {
            box.setSelected(true);
        }
        else
        {
            box.setSelected(false);
        }
                
        return box;
    }
}
