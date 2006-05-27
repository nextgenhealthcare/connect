package com.webreach.mirth.client;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

public class Logs extends JFrame {

    /** Creates new form About */
    Frame parent;
    private JScrollPane mainPanel;
    private JLabel jLabel1;
    private JXTable logs;
    private int row;
    private JPanel panel1;
    
    public Logs(JFrame parent, int row) 
    {
        this.row = row;
        this.parent = (Frame)parent;
        this.setSize(new Dimension(300,400));
        initComponents();
        panel1 = new JPanel();
        this.add(panel1);
        setTitle("Logs: " + (String)this.parent.statusListPage.statusTable.getValueAt(row,this.parent.statusListPage.getColumnNumber("Name")));
        logs = makeLogsTable();
        mainPanel.setViewportView(logs);
       
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(panel1);
        //this.add(mainPanel);
        panel1.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, logs.getWidth(), Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, logs.getHeight(), Short.MAX_VALUE)
        );
        
        
        Rectangle parentBounds = parent.getBounds();
        Dimension size = getSize();

        // Center in the parent
        int x = Math.max(0, parentBounds.x + (parentBounds.width - size.width) / 2);
        int y = Math.max(0, parentBounds.y + (parentBounds.height - size.height) / 2);
        setLocation(new Point(x, y));
        this.setResizable(false);
        setVisible(true);
    }
    
    private JXTable makeLogsTable()
    {
        JXTable jxTable = new JXTable();
        /*
        Object[][] tableData = new Object[parent.channels.get(row).][3];
        
        for (int i=0; i < parent.channels.size(); i++)
        {
            Channel temp = parent.channels.get(i);
            
            if (temp.isEnabled())
                tableData[i][0] = "Enabled";
            else
                tableData[i][0] = "Disabled";
            
            if (temp.getDirection().equals(Channel.Direction.INBOUND))
                tableData[i][1] = "Inbound";
            else
                tableData[i][1] = "Outbound";

            tableData[i][2] = temp.getName();
        }
        */
        jxTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {"01/12/1221", "ADT Processing"},
                {"11/12/1221", "ADT Outflow feed"},
                {"01/14/1221", "Another status"}
            },
            new String []
            {
                "Time", "Message"
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
        
        jxTable.setFocusable(false);
        jxTable.setSelectionMode(0);
                
        jxTable.getColumnExt("Time").setMaxWidth(90);
        jxTable.getColumnExt("Time").setMinWidth(90);
        
        jxTable.setRowHeight(20);
        jxTable.setColumnMargin(2);
        jxTable.setOpaque(true);
        jxTable.setRowSelectionAllowed(false);
        HighlighterPipeline highlighter = new HighlighterPipeline();
        highlighter.addHighlighter(AlternateRowHighlighter.beige);
        jxTable.setHighlighters(highlighter);

        return jxTable;
    }    
  
    private void initComponents() 
    {
        mainPanel = new JScrollPane();
    }          
}

