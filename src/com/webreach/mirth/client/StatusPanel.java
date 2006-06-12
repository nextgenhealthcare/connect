package com.webreach.mirth.client;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatus;
import java.awt.Point;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

public class StatusPanel extends javax.swing.JPanel {

    private JScrollPane statusPane;
    public JXTable statusTable;
    private Frame parent;
    
    /** Creates new form statusPanel */
    public StatusPanel(JFrame parent) 
    {  
        this.parent = (Frame)parent;
        initComponents();
    }
    
    private void initComponents()
    {
        statusPane = new JScrollPane();
        makeStatusTable();
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }
    
    public void makeStatusTable()
    {
        statusTable = new JXTable();
        int numColumns = 5;
        Object[][] tableData = new Object[parent.status.size()][numColumns];
        for (int i=0; i < parent.status.size(); i++)
        {
            ChannelStatus temp = parent.status.get(i);      
            if (temp.getState() == ChannelStatus.State.STARTED)
                tableData[i][0] = "Started";
            else if (temp.getState() == ChannelStatus.State.STOPPED)
                tableData[i][0] = "Stopped";
            else if (temp.getState() == ChannelStatus.State.PAUSED)
                tableData[i][0] = "Paused";
            
            tableData[i][1] = temp.getName();
            
            tableData[i][2] = "0";
            tableData[i][3] = "0";
            tableData[i][4] = "0";
        }
        
        statusTable.setModel(new javax.swing.table.DefaultTableModel(
            tableData,
            new String []
            {
                "Status", "Name", "Transformed", "Received", "Errors"
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        
        statusTable.setFocusable(false);
        statusTable.setSelectionMode(0);
                
        statusTable.getColumnExt("Status").setMaxWidth(90);
        statusTable.getColumnExt("Status").setMinWidth(90);
        
        statusTable.getColumnExt("Transformed").setMaxWidth(90);
        statusTable.getColumnExt("Transformed").setMinWidth(90);
        
        statusTable.getColumnExt("Received").setMaxWidth(90);
        statusTable.getColumnExt("Received").setMinWidth(90);
        
        statusTable.getColumnExt("Errors").setMaxWidth(90);
        statusTable.getColumnExt("Errors").setMinWidth(90);    
        
        statusTable.setRowHeight(20);
        statusTable.setColumnMargin(2);
        statusTable.setOpaque(true);
        statusTable.setRowSelectionAllowed(true);
        HighlighterPipeline highlighter = new HighlighterPipeline();
        highlighter.addHighlighter(AlternateRowHighlighter.beige);
        statusTable.setHighlighters(highlighter);
        statusPane.setViewportView(statusTable);
        
        statusTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                StatusListSelected(evt);
            }
        });
        statusPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
    }    
    
    private void StatusListSelected(ListSelectionEvent evt) 
    {           
        int row = statusTable.getSelectedRow();
        if(row >= 0 && statusTable.getSelectedColumn()>= 0)
        {
            parent.setVisibleTasks(parent.statusTasks, 2, true);
            
            int columnNumber = getColumnNumber("Status");
            if (((String)statusTable.getValueAt(row, columnNumber)).equals("Started"))
            {
                parent.statusTasks.getContentPane().getComponent(2).setVisible(false);
                parent.statusTasks.getContentPane().getComponent(3).setVisible(true);
                parent.statusTasks.getContentPane().getComponent(4).setVisible(true);
            }
            else if (((String)statusTable.getValueAt(row, columnNumber)).equals("Paused"))
            {
                parent.statusTasks.getContentPane().getComponent(2).setVisible(true);
                parent.statusTasks.getContentPane().getComponent(3).setVisible(false);
                parent.statusTasks.getContentPane().getComponent(4).setVisible(true);
            }
            else
            {
                parent.statusTasks.getContentPane().getComponent(2).setVisible(true);
                parent.statusTasks.getContentPane().getComponent(3).setVisible(false);
                parent.statusTasks.getContentPane().getComponent(4).setVisible(false);
            }
        }
    }
    
    private void deselectRows()
    {
        statusTable.clearSelection();
        parent.setVisibleTasks(parent.statusTasks, 2, false);
    }
    
    public int getSelectedStatus()
    {
        for(int i=0; i<parent.status.size(); i++)
        {
            if(((String)statusTable.getValueAt(statusTable.getSelectedRow(), getColumnNumber("Name"))).equalsIgnoreCase(parent.status.get(i).getName()))
                return i;
        }
        return -1;
    }
    
    public int getColumnNumber(String name)
    {
        for (int i = 0; i < statusTable.getColumnCount(); i++)
        {
            if (statusTable.getColumnName(i).equalsIgnoreCase(name))
                return i;
        }
        return -1;
    }
}