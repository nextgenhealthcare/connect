/*
 * ChannelPanel.java
 *
 * Created on February 22, 2007, 12:20 PM
 */

package com.webreach.mirth.client.ui;

import java.awt.Point;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.Channel;

/**
 * 
 * @author brendanh
 */
public class ChannelPanel extends javax.swing.JPanel
{
    private final String STATUS_COLUMN_NAME = "Status";
    private final String NAME_COLUMN_NAME = "Name";
    private final String ID_COLUMN_NAME = "Id";
    private final int ID_COLUMN_NUMBER = 2;
    private final String ENABLED_STATUS = "Enabled";
    private int lastRow;
    private Frame parent;

    /** Creates new form ChannelPanel */
    public ChannelPanel()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        lastRow = -1;
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
    }

    /** Creates the channel table */
    public void makeChannelTable()
    {
        updateChannelTable();
        channelTable.setSelectionMode(0);

        // Must set the maximum width on columns that should be packed.
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        channelTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        channelTable.getColumnExt(ID_COLUMN_NAME).setVisible(false);
        channelTable.packTable(UIConstants.COL_MARGIN);

        channelTable.setRowHeight(UIConstants.ROW_HEIGHT);
        channelTable.setOpaque(true);
        channelTable.setRowSelectionAllowed(true);

        // Set highlighter.
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
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

    public void updateChannelTable()
    {
        Object[][] tableData = null;

        if (parent.channels != null)
        {
            tableData = new Object[parent.channels.size()][3];

            int i = 0;
            for (Channel channel : parent.channels.values())
            {
                if (channel.isEnabled())
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
                else
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");

                tableData[i][1] = channel.getName();
                tableData[i][2] = channel.getId();
                i++;
            }
        }

        int row = UIConstants.ERROR_CONSTANT;

        if (channelTable != null)
        {
            row = channelTable.getSelectedRow();
            lastRow = row;
            RefreshTableModel model = (RefreshTableModel) channelTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            channelTable = new MirthTable();
            channelTable.setModel(new RefreshTableModel(tableData, new String[] { STATUS_COLUMN_NAME, NAME_COLUMN_NAME, ID_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { false, false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }

        if (lastRow >= 0 && lastRow < channelTable.getRowCount())
        {
            channelTable.setRowSelectionInterval(lastRow, lastRow);
        }
    }

    /**
     * Show the popup menu on trigger button press (right-click). If it's on the
     * table then the row should be selected, if not any selected rows should be
     * deselected first.
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

    /** The action called when a Channel is selected. Sets tasks as well. */
    private void ChannelListSelected(ListSelectionEvent evt)
    {
        int row = channelTable.getSelectedRow();

        if (row >= 0 && row < channelTable.getRowCount())
        {
            parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 5, -1, true);

            int columnNumber = channelTable.getColumnNumber(STATUS_COLUMN_NAME);
            if (((CellData) channelTable.getValueAt(row, columnNumber)).getText().equals(ENABLED_STATUS))
                parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 9, 9, false);
            else
                parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 10, 10, false);
        }
    }

    /**
     * Gets the selected channel index that corresponds to the saved channels
     * list
     */
    public Channel getSelectedChannel()
    {
        int selectedRow = channelTable.getSelectedRow();
        if (selectedRow != -1)
        {
            String channelId = (String) channelTable.getModel().getValueAt(channelTable.convertRowIndexToModel(selectedRow), ID_COLUMN_NUMBER);
            return parent.channels.get(channelId);
        }

        return null;
    }

    /** Sets a channel to be selected by taking it's id */
    public boolean setSelectedChannel(String channelId)
    {
        int i = 0;
        for (Channel channel : parent.channels.values())
        {
            if (channelId.equals(channelTable.getModel().getValueAt(i, ID_COLUMN_NUMBER)))
            {
                int row = channelTable.convertRowIndexToView(i);
                channelTable.setRowSelectionInterval(row, row);
                return true;
            }
            i++;
        }
        return false;
    }

    public void deselectRows()
    {
        channelTable.deselectRows();
        parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 5, -1, false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        channelPane = new javax.swing.JScrollPane();
        channelTable = null;

        channelPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        channelPane.setViewportView(channelTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(channelPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(channelPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane channelPane;

    private com.webreach.mirth.client.ui.components.MirthTable channelTable;
    // End of variables declaration//GEN-END:variables

}
