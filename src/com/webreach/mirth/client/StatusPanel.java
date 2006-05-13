package com.webreach.mirth.client;

import java.awt.Point;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

public class StatusPanel extends javax.swing.JPanel {

    private JScrollPane statusPane;
    private JXTable statusTable;
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
        statusTable = makeStatusTable();
        
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
    
    private JXTable makeStatusTable()
    {
        JXTable jxTable = new JXTable();
        
        jxTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {"Deployed", "ADT Processing", "0", "0", "0"},
                {"Deployed", "ADT Outflow feed", "0", "0", "0"},
                {"Deployed", "Another status", "0", "0", "0"}
            },
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
        
        jxTable.setFocusable(false);
        jxTable.setSelectionMode(0);
                
        jxTable.getColumnExt("Status").setMaxWidth(90);
        jxTable.getColumnExt("Status").setMinWidth(90);
        
        jxTable.getColumnExt("Transformed").setMaxWidth(90);
        jxTable.getColumnExt("Transformed").setMinWidth(90);
        
        jxTable.getColumnExt("Received").setMaxWidth(90);
        jxTable.getColumnExt("Received").setMinWidth(90);
        
        jxTable.getColumnExt("Errors").setMaxWidth(90);
        jxTable.getColumnExt("Errors").setMinWidth(90);    
        
        jxTable.setRowHeight(20);
        jxTable.setColumnMargin(2);
        jxTable.setOpaque(true);
        jxTable.setRowSelectionAllowed(true);
        HighlighterPipeline highlighter = new HighlighterPipeline();
        highlighter.addHighlighter(AlternateRowHighlighter.beige);
        jxTable.setHighlighters(highlighter);

        return jxTable;
    }    
    
    private void StatusListSelected(ListSelectionEvent evt) 
    {           
        int row = statusTable.getSelectedRow();
        if(row >= 0 && statusTable.getSelectedColumn()>= 0)
        {
            parent.setVisibleTasks(parent.statusTasks, 1, true);
            
            int columnNumber = getColumnNumber("Status");
            if (((String)statusTable.getValueAt(row, columnNumber)).equals("Deployed"))
                parent.statusTasks.getContentPane().getComponent(1).setVisible(false);
            else
                parent.statusTasks.getContentPane().getComponent(2).setVisible(false);
        }
    }
    
    private void deselectRows()
    {
        statusTable.clearSelection();
        parent.setVisibleTasks(parent.statusTasks, 1, false);
    }
    
    public int getSelectedRow()
    {
        return statusTable.getSelectedRow();
    }
    
    private int getColumnNumber(String name)
    {
        for (int i = 0; i < statusTable.getColumnCount(); i++)
        {
            if (statusTable.getColumnName(i).equalsIgnoreCase(name))
                return i;
        }
        return -1;
    }
}