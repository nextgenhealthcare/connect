/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.client.ui;

import java.awt.Point;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.model.User;

/**
 * Creates the main Users scroll pane.
 */
public class Users extends javax.swing.JScrollPane 
{
    private final String USER_ID_COLUMN_NAME = "User ID";
    private final String USERNAME_COLUMN_NAME = "Username";
    
    public JXTable usersTable;
    
    private Frame parent;
    
    /** Creates new form Users */
    public Users() 
    {  
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        setVisible(true);
    }
    
    /**
     * makes the table and adds mouse listeners
     */
    private void initComponents()
    {
        makeUsersTable();
        this.addMouseListener(new java.awt.event.MouseAdapter() 
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showUserPopupMenu(evt, false);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showUserPopupMenu(evt, false);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
    }
    
    /**
     * Makes the users table with the information from the server
     */
    public void makeUsersTable()
    {
        usersTable = new JXTable();
        Object[][] tableData = null;
        
        if(parent.users != null)
        {
            tableData = new Object[parent.users.size()][3];

            for (int i=0; i < parent.users.size(); i++)
            {
                User temp = parent.users.get(i);

                tableData[i][0] = "" + temp.getId();
                tableData[i][1] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/user.png")), temp.getUsername());
            } 
        }
        
        usersTable.setModel(new javax.swing.table.DefaultTableModel(
            tableData,
            new String []
            {
                USER_ID_COLUMN_NAME, USERNAME_COLUMN_NAME
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        
        usersTable.setSelectionMode(0);
        
        usersTable.getColumnExt(USERNAME_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());        
        usersTable.getColumnExt(USER_ID_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        
        usersTable.getColumnExt(USER_ID_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        usersTable.getColumnExt(USER_ID_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER); 
        
        usersTable.packTable(UIConstants.COL_MARGIN);
        
        usersTable.setRowHeight(UIConstants.ROW_HEIGHT);
        usersTable.setOpaque(true);
        
        usersTable.setCellSelectionEnabled(false);
        usersTable.setRowSelectionAllowed(true);
        
        if(Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            usersTable.setHighlighters(highlighter);
        }
        
        this.setViewportView(usersTable);
        
        usersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                UsersListSelected(evt);
            }
        });
        usersTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showUserPopupMenu(evt, true);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showUserPopupMenu(evt, true);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() >= 2)
                    parent.doEditUser();
            }
        });
        
    }    

    /**
     * Shows the popup menu on trigger buton (right-click).
     */
    private void showUserPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = usersTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                usersTable.setRowSelectionInterval(row, row);
            }
            else
                deselectRows();
            parent.userPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    /**
     * An action to set the correct tasks to visible when a user is selected in the table.
     */
    private void UsersListSelected(ListSelectionEvent evt) 
    {
        int row = usersTable.getSelectedRow();
        if(row >= 0)
        {
            parent.setVisibleTasks(parent.userTasks, parent.userPopupMenu, 2, -1, true);
        }
    }
    
    /**
     * Deselects all rows and sets the visible tasks appropriately.
     */
    public void deselectRows()
    {
        usersTable.clearSelection();
        parent.setVisibleTasks(parent.userTasks, parent.userPopupMenu, 2, -1, false);
    }
    
    /**
     *  Returns the selected row in the user table.
     */
    public int getSelectedRow()
    {
        return usersTable.getSelectedRow();
    }
    
    /**
     *  Sets the selected user to user with the given 'userName'.
     */
    public boolean setSelectedUser(String userName)
    {
        int columnNumber = getColumnNumber(USERNAME_COLUMN_NAME);
        for (int i = 0; i < parent.users.size(); i++)
        {
            if (userName.equals((String)(((CellData)usersTable.getValueAt(i, columnNumber)).getText())))
            {
                usersTable.setRowSelectionInterval(i,i);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the index according to the stored server's user list
     * of the currently selected user.
     */
    public int getUserIndex()
    {
        int columnNumber = getColumnNumber(USERNAME_COLUMN_NAME);
         
        if (usersTable.getSelectedRow() != -1)
        {
            String userName = ((CellData)usersTable.getValueAt(getSelectedRow(), columnNumber)).getText();

            for (int i=0; i < parent.users.size(); i++)
            {
                if(parent.users.get(i).getUsername().equals(userName))
                {
                    return i;
                }
            }
        }
        return UIConstants.ERROR_CONSTANT;
    }
    
    /**
     * Gets the column index number of the column titled 'name'.
     */
    public int getColumnNumber(String name)
    {
        for (int i = 0; i < usersTable.getColumnCount(); i++)
        {
            if (usersTable.getColumnName(i).equalsIgnoreCase(name))
                return i;
        }
        return UIConstants.ERROR_CONSTANT;
    }
}
