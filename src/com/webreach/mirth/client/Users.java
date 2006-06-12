package com.webreach.mirth.client;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.User;
import java.awt.Point;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

public class Users extends javax.swing.JScrollPane {

    public JXTable usersTable;
    private Frame parent;
    
    /** Creates new form thisl */
    public Users(JFrame parent) 
    {  
        this.parent = (Frame)parent;
        initComponents();
        setVisible(true);
    }
    
    private void initComponents()
    {
        makeUsersTable();
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
    }
    
    public void makeUsersTable()
    {
        usersTable = new JXTable();
        Object[][] tableData = null;
        tableData = new Object[parent.users.size()][3];
        for (int i=0; i < parent.users.size(); i++)
        {
            User temp = parent.users.get(i);

            tableData[i][0] = "" + temp.getId();
            tableData[i][1] = temp.getUsername();
        } 
        usersTable.setModel(new javax.swing.table.DefaultTableModel(
            tableData,
            new String []
            {
                "ID", "Username"
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
        
        usersTable.setFocusable(false);
        usersTable.setSelectionMode(0);
                
        usersTable.getColumnExt("ID").setMaxWidth(90);
        usersTable.getColumnExt("ID").setMinWidth(90);
        
        usersTable.setRowHeight(20);
        usersTable.setColumnMargin(2);
        usersTable.setOpaque(true);
        
        usersTable.setCellSelectionEnabled(false);
        usersTable.setRowSelectionAllowed(true);
        HighlighterPipeline highlighter = new HighlighterPipeline();
        highlighter.addHighlighter(AlternateRowHighlighter.beige);
        usersTable.setHighlighters(highlighter);
        
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
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() >= 2)
                    parent.doEditUser();
            }
        });
        
    }    
    
    private void UsersListSelected(ListSelectionEvent evt) 
    {
        int row = usersTable.getSelectedRow();
        if(row >= 0 && usersTable.getSelectedColumn()>= 0)
        {
            parent.setVisibleTasks(parent.userTasks, 2, true);
        }
    }
    
    private void deselectRows()
    {
        usersTable.clearSelection();
        parent.setVisibleTasks(parent.userTasks, 2, false);
    }
    
    public int getSelectedRow()
    {
        return usersTable.getSelectedRow();
    }
    
    public boolean setSelectedUser(String userName)
    {
        int columnNumber = getColumnNumber("Username");
        for (int i = 0; i < parent.users.size(); i++)
        {
            if (userName.equals(usersTable.getValueAt(i, columnNumber)))
            {
                usersTable.setRowSelectionInterval(i,i);
                return true;
            }
        }
        return false;
    }
    
    public int getUserIndex()
    {
        int columnNumber = getColumnNumber("Username");
         
        if (usersTable.getSelectedRow() != -1)
        {
            String userName = (String) usersTable.getValueAt(getSelectedRow(), columnNumber);
             if(userName.equalsIgnoreCase("admin")) 
             {
                JOptionPane.showMessageDialog(this, "You must have at least one destination.");
                return -1;
             }
            for (int i=0; i < parent.users.size(); i++)
            {
                if(parent.users.get(i).getUsername().equals(userName))
                {
                    return i;
                }
            }
        }
        return -1;
    }
    
    public int getColumnNumber(String name)
    {
        for (int i = 0; i < usersTable.getColumnCount(); i++)
        {
            if (usersTable.getColumnName(i).equalsIgnoreCase(name))
                return i;
        }
        return -1;
    }
}