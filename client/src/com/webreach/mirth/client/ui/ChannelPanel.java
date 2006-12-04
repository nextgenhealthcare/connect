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

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.model.Channel;

/** The main channel list panel view. */
public class ChannelPanel extends javax.swing.JPanel
{
    private final String STATUS_COLUMN_NAME = "Status";
    private final String DIRECTION_COLUMN_NAME = "Direction";
    private final String NAME_COLUMN_NAME = "Name";
    private final String MODE_COLUMN_NAME = "Mode";
    private final String INBOUND_DIRECTION = "Inbound";
    private final String OUTBOUND_DIRECTION = "Outbound";
    private final String ENABLED_STATUS = "Enabled";
    private final String ROUTER = "Router";
    private final String BROADCAST = "Broadcast";
    private final String APPLICATION = "Application Integration";
    
    private JScrollPane channelPane;
    private JXTable channelTable;
    private Frame parent;
    
    /** Creates new form ChannelPanel */
    public ChannelPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
    }
    
    /** Initializes the pane, makes the table, adds the mouse listeners, and sets the layout */
    public void initComponents()
    {
        channelPane = new JScrollPane();
        
        makeChannelTable();
        
        channelPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showChannelPopupMenu(evt, false);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showChannelPopupMenu(evt, false);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
        
        channelPane.setComponentPopupMenu(parent.channelPopupMenu);
        
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
    
    /** Creates the channel table */
    public void makeChannelTable()
    {
        channelTable = new JXTable();
        Object[][] tableData = null;
        
        if(parent.channels != null)
        {
            tableData = new Object[parent.channels.size()][4];

            for (int i=0; i < parent.channels.size(); i++)
            {
                Channel temp = parent.channels.get(i);

                if (temp.isEnabled())
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_blue.png")),"Enabled");
                else
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_black.png")),"Disabled");

                if (temp.getDirection().equals(Channel.Direction.INBOUND))
                    tableData[i][1] = INBOUND_DIRECTION;
                else
                    tableData[i][1] = OUTBOUND_DIRECTION;

                if(temp.getMode() == Channel.Mode.APPLICATION)
                    tableData[i][2] = APPLICATION;
                else if(temp.getMode() == Channel.Mode.BROADCAST)
                    tableData[i][2] = BROADCAST;
                else if(temp.getMode() == Channel.Mode.ROUTER)
                    tableData[i][2] = ROUTER;

                tableData[i][3] = temp.getName();
            }
        }
            
        channelTable.setModel(new javax.swing.table.DefaultTableModel(
            tableData,
            new String []
        {
            STATUS_COLUMN_NAME, DIRECTION_COLUMN_NAME, MODE_COLUMN_NAME, NAME_COLUMN_NAME
        }
        ) {
            boolean[] canEdit = new boolean []
            {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
                
        channelTable.setSelectionMode(0);        
        
        // Must set the maximum width on columns that should be packed.
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        channelTable.getColumnExt(DIRECTION_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        channelTable.getColumnExt(MODE_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        channelTable.packTable(UIConstants.COL_MARGIN);

        channelTable.setRowHeight(UIConstants.ROW_HEIGHT);
        channelTable.setOpaque(true);
        channelTable.setRowSelectionAllowed(true);
        
        // Set highlighter.
        if(Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            channelTable.setHighlighters(highlighter);
        }
        
        channelPane.setViewportView(channelTable);
        
        channelTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                ChannelListSelected(evt);
            }
        });
        
        // listen for trigger button and double click to edit channel.
        channelTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showChannelPopupMenu(evt, true);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showChannelPopupMenu(evt, true);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() >= 2)
                    parent.doEditChannel();
            }
        });
    }
    
    /** Show the popup menu on trigger button press (right-click).
     *  If it's on the table then the row should be selected, if not
     *  any selected rows should be deselected first.
     */
    private void showChannelPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = channelTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                channelTable.setRowSelectionInterval(row, row);
            }
            else
                deselectRows();
            parent.channelPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    /** The action called when a Channel is selected.  Sets tasks as well. */
    private void ChannelListSelected(ListSelectionEvent evt)
    {
        int row = channelTable.getSelectedRow();
        
        if(row >= 0)
        {
            parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 5, -1, true);

            int columnNumber = getColumnNumber(STATUS_COLUMN_NAME);
            if (((CellData)channelTable.getValueAt(row, columnNumber)).getText().equals(ENABLED_STATUS))
                parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 8, 8, false);
            else
                parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 9, 9, false);
        }
    }
    
    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows()
    {
        channelTable.clearSelection();
        parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 5, -1, false);
    }
    
    /** Gets the selected channel index that corresponds to the saved channels list */
    public int getSelectedChannel()
    {
        int columnNumber = getColumnNumber(NAME_COLUMN_NAME);
        
        if (channelTable.getSelectedRow() != -1)
        {
            String channelName = (String) channelTable.getValueAt(channelTable.getSelectedRow(), columnNumber);
            for (int i=0; i < parent.channels.size(); i++)
            {
                if (parent.channels.get(i).getName().equals(channelName))
                    return i;
            }
        }
        return -1;
    }
    
    /** Sets a channel to be selected by taking it's name */
    public boolean setSelectedChannel(String channelName)
    {
        int columnNumber = getColumnNumber(NAME_COLUMN_NAME);
        for (int i = 0; i < parent.channels.size(); i++)
        {
            if (channelName.equals(channelTable.getValueAt(i, columnNumber)))
            {
                channelTable.setRowSelectionInterval(i,i);
                return true;
            }
        }
        return false;
    }
    
    /** Gets a column index by taking it's name */
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
