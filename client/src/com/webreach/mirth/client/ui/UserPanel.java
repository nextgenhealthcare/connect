/*
 * UserPanel.java
 *
 * Created on February 21, 2007, 5:29 PM
 */

package com.webreach.mirth.client.ui;

import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.User;

/**
 * 
 * @author brendanh
 */
public class UserPanel extends javax.swing.JPanel
{
    private final String USER_EMAIL_COLUMN_NAME = "Email";
    private final String USERNAME_COLUMN_NAME = "Username";
    private final String USERFIRSTNAME_COLUMN_NAME = "First Name";
    private final String USERLASTNAME_COLUMN_NAME = "Last Name";
    private final String USERORGANIZATION_COLUMN_NAME = "Organization";
    private final String USERPHONENUMBER_COLUMN_NAME = "Phone Number";
    private final String USERDESCRIPTION_COLUMN_NAME = "Description";
    private final String USERLASTLOGIN_COLUMN_NAME = "Last Login";
    private Frame parent;
    private int lastRow;

    public UserPanel()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        lastRow = -1;
        initComponents();
        makeUsersTable();
        usersTable.setBorder(BorderFactory.createEmptyBorder());
        usersPane.addMouseListener(new java.awt.event.MouseAdapter()
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
        setBorder(BorderFactory.createEmptyBorder());
        setVisible(true);
    }

    /**
     * Makes the users table with the information from the server
     */
    public void makeUsersTable()
    {
        updateUserTable();

        usersTable.setSelectionMode(0);

        usersTable.getColumnExt(USERNAME_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        usersTable.getColumnExt(USER_EMAIL_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);

        usersTable.getColumnExt(USERNAME_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        usersTable.getColumnExt(USERFIRSTNAME_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        usersTable.getColumnExt(USERLASTNAME_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        usersTable.getColumnExt(USERORGANIZATION_COLUMN_NAME).setMinWidth(125);
        usersTable.getColumnExt(USERORGANIZATION_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        usersTable.getColumnExt(USER_EMAIL_COLUMN_NAME).setMinWidth(150);
        usersTable.getColumnExt(USER_EMAIL_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        usersTable.getColumnExt(USERPHONENUMBER_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        usersTable.getColumnExt(USERLASTLOGIN_COLUMN_NAME).setMinWidth(125);
        usersTable.getColumnExt(USERLASTLOGIN_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        
        usersTable.packTable(UIConstants.COL_MARGIN);

        usersTable.setRowHeight(UIConstants.ROW_HEIGHT);
        usersTable.setOpaque(true);

        usersTable.setCellSelectionEnabled(false);
        usersTable.setRowSelectionAllowed(true);

        usersPane.setViewportView(usersTable);

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

    public void updateUserTable()
    {
        Object[][] tableData = null;

        if (parent.users != null)
        {
            tableData = new Object[parent.users.size()][8];

            for (int i = 0; i < parent.users.size(); i++)
            {
                User temp = parent.users.get(i);

                tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/user.png")), temp.getUsername());
                tableData[i][1] = temp.getFirstName();
                tableData[i][2] = temp.getLastName();
                tableData[i][3] = temp.getOrganization();
                tableData[i][4] = temp.getEmail();
                tableData[i][5] = temp.getPhoneNumber();
                tableData[i][6] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(temp.getLastLogin().getTime());
                tableData[i][7] = temp.getDescription();
            }
        }

        if (usersTable != null)
        {
            lastRow = usersTable.getSelectedRow();
            RefreshTableModel model = (RefreshTableModel) usersTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            usersTable = new MirthTable();
            usersTable.setModel(new RefreshTableModel(tableData, new String[] { USERNAME_COLUMN_NAME, USERFIRSTNAME_COLUMN_NAME, USERLASTNAME_COLUMN_NAME, USERORGANIZATION_COLUMN_NAME, USER_EMAIL_COLUMN_NAME, USERPHONENUMBER_COLUMN_NAME, USERLASTLOGIN_COLUMN_NAME, USERDESCRIPTION_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { false, false, false, false, false, false, false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }

        if (lastRow >= 0 && lastRow < usersTable.getRowCount())
            usersTable.setRowSelectionInterval(lastRow, lastRow);
        else
            lastRow = UIConstants.ERROR_CONSTANT;
        
        // Set highlighter.
        HighlighterPipeline highlighter = new HighlighterPipeline();
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
        }
        usersTable.setHighlighters(highlighter);
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
     * An action to set the correct tasks to visible when a user is selected in
     * the table.
     */
    private void UsersListSelected(ListSelectionEvent evt)
    {
        int row = usersTable.getSelectedRow();
        if (row >= 0 && row < usersTable.getRowCount())
        {
            parent.setVisibleTasks(parent.userTasks, parent.userPopupMenu, 2, -1, true);
        }
    }

    /**
     * Returns the selected row in the user table.
     */
    public int getSelectedRow()
    {
        return usersTable.getSelectedRow();
    }

    /**
     * Sets the selected user to user with the given 'userName'.
     */
    public boolean setSelectedUser(String userName)
    {
        int columnNumber = usersTable.getColumnNumber(USERNAME_COLUMN_NAME);
        for (int i = 0; i < parent.users.size(); i++)
        {
            if (userName.equals((String) (((CellData) usersTable.getValueAt(i, columnNumber)).getText())))
            {
                usersTable.setRowSelectionInterval(i, i);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index according to the stored server's user list of the
     * currently selected user.
     */
    public int getUserIndex()
    {
        int columnNumber = usersTable.getColumnNumber(USERNAME_COLUMN_NAME);

        if (usersTable.getSelectedRow() != -1)
        {
            String userName = ((CellData) usersTable.getValueAt(getSelectedRow(), columnNumber)).getText();

            for (int i = 0; i < parent.users.size(); i++)
            {
                if (parent.users.get(i).getUsername().equals(userName))
                {
                    return i;
                }
            }
        }
        return UIConstants.ERROR_CONSTANT;
    }

    public void deselectRows()
    {
        usersTable.deselectRows();
        parent.setVisibleTasks(parent.userTasks, parent.userPopupMenu, 2, -1, false);
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
        usersPane = new javax.swing.JScrollPane();
        usersTable = null;

        usersPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        usersPane.setViewportView(usersTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(usersPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(usersPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane usersPane;

    public com.webreach.mirth.client.ui.components.MirthTable usersTable;
    // End of variables declaration//GEN-END:variables

}
