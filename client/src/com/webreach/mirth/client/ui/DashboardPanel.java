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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.ChannelStatus;

/**
 * The main dashboard panel.
 */
public class DashboardPanel extends javax.swing.JPanel
{
    private final String STATUS_COLUMN_NAME = "Status";
    private final String NAME_COLUMN_NAME = "Name";
    private final String RECEIVED_COLUMN_NAME = "Received";
    private final String SENT_COLUMN_NAME = "Sent";
    private final String ERROR_COLUMN_NAME = "Errors";
    private final String REJECTED_COLUMN_NAME = "Rejected";
    public JXTable statusTable;
    
    private JScrollPane statusPane;
    private Frame parent;
    
    /** Creates new form DashboardPanel */
    public DashboardPanel()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        this.setDoubleBuffered(true);
        setBorder(BorderFactory.createEmptyBorder());
    }
    
    /**
     * Initializes the dashboard panel layout, table, and mouse listeners.
     */
    private void initComponents()
    {
        statusPane = new JScrollPane();
        statusPane.setDoubleBuffered(true);
        statusPane.setBorder(BorderFactory.createEmptyBorder());
        statusTable = null; 
        makeStatusTable();
        statusPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showStatusPopupMenu(evt, false);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showStatusPopupMenu(evt, false);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                .add(org.jdesktop.layout.GroupLayout.LEADING, statusPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(statusPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                );
    }
    
    /**
     * Makes the status table with all current server information.
     */
    public void makeStatusTable()
    {        
        updateTable();
        
        statusTable.setDoubleBuffered(true);
        statusTable.setBorder(BorderFactory.createEmptyBorder());
        
        statusTable.setSelectionMode(0);
        
        statusTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(RECEIVED_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(SENT_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(ERROR_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        statusTable.getColumnExt(REJECTED_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        
        statusTable.getColumnExt(STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(RECEIVED_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(SENT_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(ERROR_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(REJECTED_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        statusTable.getColumnExt(NAME_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        
        statusTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        statusTable.getColumnExt(RECEIVED_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        statusTable.getColumnExt(SENT_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        statusTable.getColumnExt(ERROR_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        statusTable.getColumnExt(REJECTED_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        
        statusTable.getColumnExt(RECEIVED_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        statusTable.getColumnExt(SENT_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        statusTable.getColumnExt(ERROR_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        statusTable.getColumnExt(REJECTED_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        
        statusTable.packTable(UIConstants.COL_MARGIN);
        
        statusTable.setRowHeight(UIConstants.ROW_HEIGHT);
        statusTable.setOpaque(true);
        statusTable.setRowSelectionAllowed(true);
        
        statusTable.setSortable(false);
        
        if(Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            statusTable.setHighlighters(highlighter);
        }
        
        statusPane.setViewportView(statusTable);
        
        statusTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                StatusListSelected(evt);
            }
        });
        statusTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showStatusPopupMenu(evt, true);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showStatusPopupMenu(evt, true);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() >= 2)
                    parent.doShowMessages();
            }
        });
    }

    public void updateTable()
    {
        Object[][] tableData = null;
        
        if(parent.status != null)
        {
            tableData = new Object[parent.status.size()][6];
            for (int i=0; i < parent.status.size(); i++)
            {
                ChannelStatus tempStatus = parent.status.get(i);
                try
                {
                    ChannelStatistics tempStats = parent.mirthClient.getStatistics(tempStatus.getChannelId());
                    tableData[i][2] = tempStats.getReceivedCount();
                    tableData[i][3] = tempStats.getRejectedCount();
                    tableData[i][4] = tempStats.getSentCount();
                    tableData[i][5] = tempStats.getErrorCount();
                }
                catch (ClientException ex)
                {
                    ex.printStackTrace();
                }
                
                if (tempStatus.getState() == ChannelStatus.State.STARTED)
                    tableData[i][0] = new CellData(new ImageIcon(Frame.class.getResource("images/bullet_green.png")), "Started");
                else if (tempStatus.getState() == ChannelStatus.State.STOPPED)
                    tableData[i][0] = new CellData(new ImageIcon(Frame.class.getResource("images/bullet_red.png")), "Stopped");
                else if (tempStatus.getState() == ChannelStatus.State.PAUSED)
                    tableData[i][0] = new CellData(new ImageIcon(Frame.class.getResource("images/bullet_yellow.png")), "Paused");
                
                tableData[i][1] = tempStatus.getName();
                
            }
            
        }
        
        int row = UIConstants.ERROR_CONSTANT;

        if(statusTable != null)
        {
            row = statusTable.getSelectedRow();
            RefreshTableModel model = (RefreshTableModel)statusTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            statusTable = new JXTable();
            statusTable.setModel(new RefreshTableModel(tableData,new String []{STATUS_COLUMN_NAME, NAME_COLUMN_NAME, RECEIVED_COLUMN_NAME, REJECTED_COLUMN_NAME, SENT_COLUMN_NAME, ERROR_COLUMN_NAME})
            {
                boolean[] canEdit = new boolean []
                {
                    false, false, false, false, false, false, false
                };

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit [columnIndex];
                }
            });
        }
        
        if(row >= 0 && row < statusTable.getRowCount())
        {
            System.out.println(row);
            statusTable.setRowSelectionInterval(row,row);
        }
    }
    
    /**
     * Shows the popup menu when the trigger button (right-click) has been pushed.
     */
    private void showStatusPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = statusTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                statusTable.setRowSelectionInterval(row, row);
            }
            else
                deselectRows();
            parent.statusPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    /*
     * Action when something on the status list has been selected.
     * Sets all appropriate tasks visible.
     */
    private void StatusListSelected(ListSelectionEvent evt)
    {
        int row = statusTable.getSelectedRow();
        if(row >= 0)
        {
            parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 3, -1, true);
            
            int columnNumber = getColumnNumber(STATUS_COLUMN_NAME);
            if (((CellData)statusTable.getValueAt(row, columnNumber)).getText().equals("Started"))
            {
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 4, 4, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 5, 5, false);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 6, 6, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 7, 7, true);
            }
            else if (((CellData)statusTable.getValueAt(row, columnNumber)).getText().equals("Paused"))
            {
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 4, 4, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 5, 5, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 6, 6, false);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 7, 7, true);
            }
            else
            {
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 4, 4, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 5, 5, true);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 6, 6, false);
                parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 7, 7, false);
            }
        }
    }
    
    /**
     * Deselects all rows and sets the correct tasks visible.
     */
    public void deselectRows()
    {
        statusTable.clearSelection();
        parent.setVisibleTasks(parent.statusTasks, parent.statusPopupMenu, 3, -1, false);
    }
    
    /**
     * Gets the index of the selected status row.
     */
    public synchronized int getSelectedStatus()
    {
        for(int i=0; i<parent.status.size(); i++)
        {
            if(statusTable.getSelectedRow() > -1 && ((String)statusTable.getValueAt(statusTable.getSelectedRow(), getColumnNumber(NAME_COLUMN_NAME))).equalsIgnoreCase(parent.status.get(i).getName()))
                return i;
        }
        return UIConstants.ERROR_CONSTANT;
    }
    
    /**
     * Gets the index of column with title 'name'.
     */
    public int getColumnNumber(String name)
    {
        for (int i = 0; i < statusTable.getColumnCount(); i++)
        {
            if (statusTable.getColumnName(i).equalsIgnoreCase(name))
                return i;
        }
        return UIConstants.ERROR_CONSTANT;
    }
}
