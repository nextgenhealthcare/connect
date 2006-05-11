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
    private JFrame parent;
    
    /** Creates new form statusPanel */
    public StatusPanel(JFrame parent) 
    {  
        this.parent = parent;
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
/*        statusTable.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                deselectRows();
            }
        });
 */
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
            for (int i=1; i<((Frame)parent).statusTasks.getContentPane().getComponentCount(); i++)
                ((Frame)parent).statusTasks.getContentPane().getComponent(i).setVisible(true);
        }
    }
    
    private void deselectRows()
    {
        statusTable.clearSelection();
        for (int i=1; i<((Frame)parent).statusTasks.getContentPane().getComponentCount(); i++)
            ((Frame)parent).statusTasks.getContentPane().getComponent(i).setVisible(false);
    }
    
    public int getSelectedRow()
    {
        return statusTable.getSelectedRow();
    }
}