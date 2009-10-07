/*
 * ChannelPanel.java
 *
 * Created on February 22, 2007, 12:20 PM
 */

package com.webreach.mirth.client.ui;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.Channel;

/**
 * 
 * @author brendanh
 */
public class ChannelPanel extends javax.swing.JPanel implements DropTargetListener
{
    private final String STATUS_COLUMN_NAME = "Status";
    private final String PROTOCOL_COLUMN_NAME = "Protocol";
    private final String NAME_COLUMN_NAME = "Name";
    private final String DESCRIPTION_COLUMN_NAME = "Description";
    private final String ID_COLUMN_NAME = "Id";
    private final String ENABLED_STATUS = "Enabled";
    private int lastRow;
    private Frame parent;
    private DropTarget dropTarget;

    /** Creates new form ChannelPanel */
    public ChannelPanel()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        dropTarget = new DropTarget(this, this);
        lastRow = -1;
        makeChannelTable();

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
        
        channelTable.getColumnExt(PROTOCOL_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        channelTable.getColumnExt(PROTOCOL_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        
        channelTable.getColumnExt(NAME_COLUMN_NAME).setMaxWidth(350);
        channelTable.getColumnExt(NAME_COLUMN_NAME).setMinWidth(250);
        
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        
        channelTable.getColumnExt(ID_COLUMN_NAME).setMinWidth(240);
        channelTable.getColumnExt(ID_COLUMN_NAME).setMaxWidth(240);
        
        channelTable.packTable(UIConstants.COL_MARGIN);

        channelTable.setRowHeight(UIConstants.ROW_HEIGHT);
        channelTable.setOpaque(true);
        channelTable.setRowSelectionAllowed(true);

        channelPane.setViewportView(channelTable);
        
        channelTable.setDropTarget(dropTarget);
        channelPane.setDropTarget(dropTarget);
        
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
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() >= 2)
                    parent.doEditChannel();
            }
        });
        
        // Key Listener trigger for DEL
        channelTable.addKeyListener(new KeyListener()
        {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                {
                	parent.doDeleteChannel();
                }
            }
            
            public void keyReleased(KeyEvent e)
            {
            }
            
            public void keyTyped(KeyEvent e)
            {
            }
        });
    }

    public void updateChannelTable()
    {
        Object[][] tableData = null;

        if (parent.channels != null)
        {
            tableData = new Object[parent.channels.size()][5];

            int i = 0;
            for (Channel channel : parent.channels.values())
            {
                if (channel.isEnabled())
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
                else
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");
                tableData[i][1] = parent.protocols.get(channel.getSourceConnector().getTransformer().getInboundProtocol());
                tableData[i][2] = channel.getName();
                tableData[i][3] = channel.getId();
                tableData[i][4] = channel.getDescription();

                i++;
            }
        }

        if (channelTable != null)
        {
            lastRow = channelTable.getSelectedRow();
            RefreshTableModel model = (RefreshTableModel) channelTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            channelTable = new MirthTable();
            channelTable.setModel(new RefreshTableModel(tableData, new String[] { STATUS_COLUMN_NAME, PROTOCOL_COLUMN_NAME, NAME_COLUMN_NAME,ID_COLUMN_NAME, DESCRIPTION_COLUMN_NAME})
            {
                boolean[] canEdit = new boolean[] { false, false, false, false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }

        if (lastRow >= 0 && lastRow < channelTable.getRowCount())
            channelTable.setRowSelectionInterval(lastRow, lastRow);
        else
            lastRow = UIConstants.ERROR_CONSTANT;
        
        // Set highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
        	Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
        	channelTable.setHighlighters(highlighter);
        }

    }
    
    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.  Deselects the rows if no row was selected.
     */
    private void checkSelectionAndPopupMenu(java.awt.event.MouseEvent evt)
    {
        int row = channelTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (row == -1) {
            deselectRows();
        }
        
        if (evt.isPopupTrigger()) {
            if (row != -1) {
                channelTable.setRowSelectionInterval(row, row);
            }
            parent.channelPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /** The action called when a Channel is selected. Sets tasks as well. */
    private void ChannelListSelected(ListSelectionEvent evt)
    {
        int row = channelTable.getSelectedRow();

        if (row >= 0 && row < channelTable.getRowCount())
        {
            parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 7, -1, true);

            int columnNumber = channelTable.getColumnNumber(STATUS_COLUMN_NAME);
            if (((CellData) channelTable.getValueAt(row, columnNumber)).getText().equals(ENABLED_STATUS))
                parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 11, 11, false);
            else
                parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 12, 12, false);
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
            String channelId = (String) channelTable.getModel().getValueAt(channelTable.convertRowIndexToModel(selectedRow), channelTable.getColumnNumber(ID_COLUMN_NAME));
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
            if (channelId.equals(channelTable.getModel().getValueAt(i, channelTable.getColumnNumber(ID_COLUMN_NAME))))
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
        parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 7, -1, false);
    }
    
    public void dragEnter(DropTargetDragEvent dtde)
    {
        try
        {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
            {
                
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
                
                List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator iterator = fileList.iterator();
                while (iterator.hasNext())
                {
                    String fileName = ((File)iterator.next()).getName();
                    if(!fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".xml"))
                    {
                        dtde.rejectDrag();
                        return;
                    }
                }
            }
            else
                dtde.rejectDrag();
        }
        catch (Exception e)
        {
            dtde.rejectDrag();
        }
    }
    
    public void dragOver(DropTargetDragEvent dtde)
    {
    }
    
    public void dropActionChanged(DropTargetDragEvent dtde)
    {
    }
    
    public void dragExit(DropTargetEvent dte)
    {
    }
    
    public void drop(DropTargetDropEvent dtde)
    {
        try
        {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
            {
                
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                
                List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator iterator = fileList.iterator();
                
                if(fileList.size() == 1)
                {
                    File file = (File)iterator.next();
                    parent.importChannel(file, true);
                }
                else
                {
                    while (iterator.hasNext())
                    {
                        File file = (File)iterator.next();
                        parent.importChannel(file, false);
                    }
                }
            }
        }
        catch (Exception e)
        {
            dtde.rejectDrop();
        }
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
