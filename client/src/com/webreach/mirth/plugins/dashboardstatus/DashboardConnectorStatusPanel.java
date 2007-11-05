/*
 * DashboardConnectorStatusPanel.java
 *
 * Created on October 10, 2007, 3:40 PM
 */

package com.webreach.mirth.plugins.dashboardstatus;

import com.webreach.mirth.client.ui.*;
import com.webreach.mirth.client.ui.components.MirthTable;

import java.util.LinkedList;
import java.util.prefs.Preferences;

import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;

import javax.swing.*;

/**
 *
 * @author  chrisr
 */

public class DashboardConnectorStatusPanel extends javax.swing.JPanel {

    private final String CHANNEL_COLUMN_HEADER = "Channel Name";    
    private final String TIME_COLUMN_HEADER = "Timestamp";
    private final String CONNECTOR_INFO_COLUMN_HEADER = "Connector Info";
    private final String EVENT_COLUMN_HEADER = "Event";
    private final String INFORMATION_COLUMN_HEADER = "Info";

    private ImageIcon greenBullet;      //  CONNECTED
    private ImageIcon yellowBullet;     //  BUSY
    private ImageIcon redBullet;        //  DISCONNECTED
    private ImageIcon blueBullet;       //  INITIALIZED
    private ImageIcon blackBullet;      //  DONE


    /** Creates new form DashboardConnectorStatusPanel */
    public DashboardConnectorStatusPanel()
    {
        greenBullet = new ImageIcon(Frame.class.getResource("images/bullet_green.png"));
        yellowBullet = new ImageIcon(Frame.class.getResource("images/bullet_yellow.png"));
        redBullet = new ImageIcon(Frame.class.getResource("images/bullet_red.png"));
        blueBullet = new ImageIcon(Frame.class.getResource("images/bullet_blue.png"));
        blackBullet = new ImageIcon(Frame.class.getResource("images/bullet_black.png"));

        initComponents();
        makeLogTable();
    }



    /**
     * Makes the status table with all current server information.
     */
    public void makeLogTable()
    {
        updateTable(null);
        mirthTable1.setDoubleBuffered(true);
        mirthTable1.setSelectionMode(0);
        mirthTable1.getColumnExt(CHANNEL_COLUMN_HEADER).setVisible(false);
        mirthTable1.getColumnExt(EVENT_COLUMN_HEADER).setCellRenderer(new ImageCellRenderer());
        mirthTable1.packTable(UIConstants.COL_MARGIN);
        mirthTable1.setRowHeight(UIConstants.ROW_HEIGHT);
        mirthTable1.setOpaque(true);
        mirthTable1.setRowSelectionAllowed(false);
        mirthTable1.setSortable(true);
        mirthTable1.setFocusable(false);
        jScrollPane1.setViewportView(mirthTable1);
    }



    public void updateTable(LinkedList<String[]> channelLogs)
    {
        Object[][] tableData;
        if (channelLogs != null)
        {
            tableData = new Object[channelLogs.size()][5];
            for (int i = 0; i < channelLogs.size(); i++) {

                String channelName = channelLogs.get(i)[0];        // (not used, but never know when it _may_ come in handy later).
                tableData[i][0] = channelLogs.get(i)[0];       // Channel Name (hidden)
                tableData[i][1] = channelLogs.get(i)[1];       // Timestamp
                tableData[i][2] = channelLogs.get(i)[2];       // Connector Info

                // Event State - INITIALIZED (blue), CONNECTED (green), BUSY (yellow), DONE (black), DISCONNECTED (red)
                if (channelLogs.get(i)[3].equalsIgnoreCase("INITIALIZED"))
                    tableData[i][3] = new CellData(blueBullet, "Initialized");
                else if (channelLogs.get(i)[3].equalsIgnoreCase("CONNECTED"))
                    tableData[i][3] = new CellData(greenBullet, "Connected");
                else if (channelLogs.get(i)[3].equalsIgnoreCase("BUSY"))
                    tableData[i][3] = new CellData(yellowBullet, "Busy");
                else if (channelLogs.get(i)[3].equalsIgnoreCase("DONE"))
                    tableData[i][3] = new CellData(blackBullet, "Done");
                else if (channelLogs.get(i)[3].equalsIgnoreCase("DISCONNECTED"))
                    tableData[i][3] = new CellData(redBullet, "Disconnected");

                tableData[i][4] = channelLogs.get(i)[4];       // Infomation
            }
        } else {
            tableData = new Object[0][5];
        }
        
        if (mirthTable1 != null)
        {
            RefreshTableModel model = (RefreshTableModel) mirthTable1.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            mirthTable1 = new MirthTable();
            mirthTable1.setModel(new RefreshTableModel(tableData,
                                                       new String[] {
                                                               CHANNEL_COLUMN_HEADER, TIME_COLUMN_HEADER, CONNECTOR_INFO_COLUMN_HEADER,
                                                               EVENT_COLUMN_HEADER, INFORMATION_COLUMN_HEADER })
            {
                boolean[] canEdit = new boolean[] { false, false, false, false, false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }

        // Add the highlighters.  Always add the error highlighter.
        HighlighterPipeline highlighter = new HighlighterPipeline();

        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
        }

        mirthTable1.setHighlighters(highlighter);
    }




    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        mirthTable1 = null;

        jScrollPane1.setViewportView(mirthTable1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private com.webreach.mirth.client.ui.components.MirthTable mirthTable1;
    // End of variables declaration//GEN-END:variables

}
