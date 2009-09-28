/*
 * MirthTable.java
 *
 * Created on October 18, 2006, 10:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.components;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.jdesktop.swingx.JXTable;

import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;

public class MirthTable extends JXTable
{

    /** Creates a new instance of MirthTable */
    public MirthTable()
    {
        super();
        this.setDragEnabled(true);
        this.addKeyListener(new KeyListener()
        {

            public void keyPressed(KeyEvent e)
            {
                // TODO Auto-generated method stub
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown())
                {
                    PlatformUI.MIRTH_FRAME.doContextSensitiveSave();
                }
            }

            public void keyReleased(KeyEvent e)
            {
                // TODO Auto-generated method stub

            }

            public void keyTyped(KeyEvent e)
            {
                // TODO Auto-generated method stub

            }

        });
    
    }


    public Class getColumnClass(int column)
    {
        try{
            if (getRowCount() >= 0 && column >= 0 && column < getColumnCount() && getValueAt(0, column) != null)
                return getValueAt(0, column).getClass();
            else
                return Object.class;
        }catch (Exception e){
            return Object.class;
        }
    }

    /**
     * Deselects all rows and sets the correct tasks visible.
     */
    public void deselectRows()
    {
        this.clearSelection();
    }

    /**
     * Gets the index of column with title 'name'.
     */
    public int getColumnNumber(String name)
    {
        for (int i = 0; i < this.getColumnCount(); i++)
        {
            if (this.getColumnName(i).equalsIgnoreCase(name))
                return i;
        }
        return UIConstants.ERROR_CONSTANT;
    }
    
    public int getColumnModelIndex(String columnName) {
        return this.convertColumnIndexToModel(this.getColumnModel().getColumnIndex(columnName));
    }
    
    public int getSelectedModelIndex() {
        int index = -1;
        
        if (this.isEditing())
            index = this.getEditingRow();
        else
            index = this.getSelectedRow();
        
        if (index == -1)
            return index;
        
        return this.convertRowIndexToModel(index);
    }
}
