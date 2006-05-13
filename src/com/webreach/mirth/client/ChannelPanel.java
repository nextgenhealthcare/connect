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
    private Frame parent;
    
    /** Creates new form ChannelPanel */
    public ChannelPanel(JFrame parent) {
        this.parent = (Frame)parent;
        initComponents();
    }
    
    public void initComponents() {
        channelPane = new JScrollPane();
        
        makeChannelTable();
        
        channelPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(channelPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, channelTable.getWidth(), Short.MAX_VALUE)
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(channelPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, channelTable.getHeight(), Short.MAX_VALUE)
                );
    }
    
    public void makeChannelTable() {
        channelTable = new JXTable();
        
        Object[][] tableData = new Object[parent.channels.size()][3];
        
        for (int i=0; i < parent.channels.size(); i++)
        {
                tableData[i][0] = parent.channels.get(i).status;
                tableData[i][1] = parent.channels.get(i).direction;
                tableData[i][2] = parent.channels.get(i).channelName;
        }
                
        
        channelTable.setModel(new javax.swing.table.DefaultTableModel(
                tableData,
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
        
        channelTable.setFocusable(false);
        channelTable.setSelectionMode(0);        
        
        channelTable.getColumnExt("Status").setMaxWidth(90);
        channelTable.getColumnExt("Status").setMinWidth(90);

        channelTable.getColumnExt("Direction").setMaxWidth(90);
        channelTable.getColumnExt("Direction").setMinWidth(90);
        
        channelTable.setRowHeight(20);
        channelTable.setColumnMargin(2);
        channelTable.setOpaque(true);
        channelTable.setRowSelectionAllowed(true);
        HighlighterPipeline highlighter = new HighlighterPipeline();
        highlighter.addHighlighter(AlternateRowHighlighter.beige);
        channelTable.setHighlighters(highlighter);
        
        channelPane.setViewportView(channelTable);
        
        channelTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                ChannelListSelected(evt);
            }
        });
        channelTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() >= 2)
                    parent.doEditChannel();
            }
        });
    }
    
    private void ChannelListSelected(ListSelectionEvent evt)
    {
        int row = channelTable.getSelectedRow();
        if(row >= 0 && channelTable.getSelectedColumn()>= 0)
        {
            parent.setVisibleTasks(parent.channelTasks, 2, true);

            int columnNumber = getColumnNumber("Status");
            if (((String)channelTable.getValueAt(row, columnNumber)).equals("Enabled"))
                parent.channelTasks.getContentPane().getComponent(4).setVisible(false);
            else
                parent.channelTasks.getContentPane().getComponent(5).setVisible(false);
        }
    }
    
    public void deselectRows()
    {
        channelTable.clearSelection();
        parent.setVisibleTasks(parent.channelTasks, 2, false);
    }
    
    public int getSelectedChannel()
    {
        int columnNumber = getColumnNumber("Name");
        
        String channelName = (String) channelTable.getValueAt(channelTable.getSelectedRow(), columnNumber);
        for (int i=0; i < parent.channels.size(); i++)
        {
            if (parent.channels.get(i).channelName.equals(channelName))
                return i;
        }
        return -1;
    }
    
    private int getColumnNumber(String name)
    {
        for (int i = 0; i < channelTable.getColumnCount(); i++)
        {
            if (channelTable.getColumnName(i).equalsIgnoreCase(name))
                return i;
        }
        return -1;
    }
}