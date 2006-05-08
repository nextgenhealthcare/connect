package com.webreach.mirth.client;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

public class ChannelPanel extends javax.swing.JPanel {
    
    private JScrollPane channelPane;
    private JXTable channelTable;
    private JFrame parent;
    
    /** Creates new form ChannelPanel */
    public ChannelPanel(JFrame parent) {
        this.parent = parent;
        initComponents();
    }
    
    private void initComponents() {
        channelPane = new JScrollPane();
        channelTable = makeChannelTable();
        
        channelPane.setViewportView(channelTable);
        
        channelTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                ChannelListSelected(evt);
            }
        });
/*        channelTable.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                deselectRows();
            }
        });
 */
        channelPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deselectRows();
            }
        });
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(channelPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(channelPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                );
    }
    
    private JXTable makeChannelTable() {
        JXTable jxTable = new JXTable();
        
        jxTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][]
        {
            {"Enabled","Inbound", "ADT Processing"},
            {"Enabled","Inbound", "ADT Outflow feed"},
            {"Disabled","Inbound", "Another Channel"}
        },
                new String []
        {
            "Status", "Direction", "Name"
        }
        ) {
            boolean[] canEdit = new boolean []
            {
                false, false, false
            };
            
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        
        jxTable.setFocusable(false);
        jxTable.setSelectionMode(0);        
        
        jxTable.getColumnExt(0).setMaxWidth(90);
        jxTable.getColumnExt(0).setMinWidth(90);

        jxTable.getColumnExt(1).setMaxWidth(90);
        jxTable.getColumnExt(1).setMinWidth(90);
        
        
        jxTable.setRowHeight(20);
        jxTable.setColumnMargin(2);
        jxTable.setOpaque(true);
        jxTable.setRowSelectionAllowed(true);
        HighlighterPipeline highlighter = new HighlighterPipeline();
        highlighter.addHighlighter(AlternateRowHighlighter.beige);
        jxTable.setHighlighters(highlighter);
        
        return jxTable;
    }
    
    private void ChannelListSelected(ListSelectionEvent evt)
    {
        int row = channelTable.getSelectedRow();
        if(row >= 0 && channelTable.getSelectedColumn()>= 0) {
            for (int i=2; i<((Frame)parent).channelTasks.getContentPane().getComponentCount(); i++)
                ((Frame)parent).channelTasks.getContentPane().getComponent(i).setVisible(true);
        }
    }
    
    private void deselectRows() {
        channelTable.clearSelection();
        for (int i=2; i<((Frame)parent).channelTasks.getContentPane().getComponentCount(); i++)
            ((Frame)parent).channelTasks.getContentPane().getComponent(i).setVisible(false);
    }
    
    public int getSelectedRow() {
        return channelTable.getSelectedRow();
    }
}