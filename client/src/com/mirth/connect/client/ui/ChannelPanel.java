/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Component;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatus;

public class ChannelPanel extends javax.swing.JPanel implements DropTargetListener {

    private final String STATUS_COLUMN_NAME = "Status";
    private final String PROTOCOL_COLUMN_NAME = "Protocol";
    private final String NAME_COLUMN_NAME = "Name";
    private final String DESCRIPTION_COLUMN_NAME = "Description";
    private final String ID_COLUMN_NAME = "Id";
    private final String LAST_DEPLOYED_COLUMN_NAME = "Last Deployed";
    private final String DEPLOYED_REVISION_DELTA_COLUMN_NAME = "Rev \u0394";
    private final String ENABLED_STATUS = "Enabled";
    
    private final int NAME_COLUMN_NUMBER = 2;
    
    private Frame parent;
    private DropTarget dropTarget;

    /** Creates new form ChannelPanel */
    public ChannelPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        dropTarget = new DropTarget(this, this);
        makeChannelTable();

        channelPane.setComponentPopupMenu(parent.channelPopupMenu);
    }

    /** Creates the channel table */
    public void makeChannelTable() {
        updateChannelTable();
        channelTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Must set the maximum width on columns that should be packed.
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());

        channelTable.getColumnExt(PROTOCOL_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        channelTable.getColumnExt(PROTOCOL_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        channelTable.getColumnExt(DESCRIPTION_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        channelTable.getColumnExt(NAME_COLUMN_NAME).setMaxWidth(325);
        channelTable.getColumnExt(NAME_COLUMN_NAME).setMinWidth(150);

        channelTable.getColumnExt(DEPLOYED_REVISION_DELTA_COLUMN_NAME).setMaxWidth(50);
        channelTable.getColumnExt(DEPLOYED_REVISION_DELTA_COLUMN_NAME).setMinWidth(50);
        channelTable.getColumnExt(DEPLOYED_REVISION_DELTA_COLUMN_NAME).setCellRenderer(new NumberCellRenderer());
        channelTable.getColumnExt(DEPLOYED_REVISION_DELTA_COLUMN_NAME).setToolTipText("<html><body>The number of times the channel was saved since this channel was deployed.<br>Rev \u0394 = Channel Revision - Deployed Revision</body></html>");
        channelTable.getColumnExt(DEPLOYED_REVISION_DELTA_COLUMN_NAME).setResizable(false);
        
        channelTable.getColumnExt(ID_COLUMN_NAME).setMinWidth(150);
        channelTable.getColumnExt(ID_COLUMN_NAME).setMaxWidth(215);
        
        channelTable.getColumnExt(LAST_DEPLOYED_COLUMN_NAME).setMinWidth(95);
        channelTable.getColumnExt(LAST_DEPLOYED_COLUMN_NAME).setMaxWidth(95);
        channelTable.getColumnExt(LAST_DEPLOYED_COLUMN_NAME).setCellRenderer(new DateCellRenderer());
        channelTable.getColumnExt(LAST_DEPLOYED_COLUMN_NAME).setResizable(false);

        channelTable.setRowHeight(UIConstants.ROW_HEIGHT);
        channelTable.setOpaque(true);
        channelTable.setRowSelectionAllowed(true);
        
        channelTable.setSortable(true);
        
        // Sort by Channel Name column
        channelTable.getRowSorter().toggleSortOrder(NAME_COLUMN_NUMBER);

        channelPane.setViewportView(channelTable);

        channelTable.setDropTarget(dropTarget);
        channelPane.setDropTarget(dropTarget);

        channelTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                ChannelListSelected(evt);
            }
        });
        
        // listen for trigger button and double click to edit channel.
        channelTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (channelTable.rowAtPoint(new Point(evt.getX(), evt.getY())) == -1) {
                    return;
                }

                if (evt.getClickCount() >= 2) {
                    parent.doEditChannel();
                }
            }
        });

        // Key Listener trigger for DEL
        channelTable.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    parent.doDeleteChannel();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
    }

    public void updateChannelTable() {
        Object[][] tableData = null;
        
        if (parent.channels != null) {
            tableData = new Object[parent.channels.size()][7];

            int i = 0;
            for (Channel channel : parent.channels.values()) {
                if (channel.isEnabled()) {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");
                }
                tableData[i][1] = parent.protocols.get(channel.getSourceConnector().getTransformer().getInboundProtocol());
                tableData[i][2] = channel.getName();
                tableData[i][3] = channel.getId();
                tableData[i][4] = channel.getDescription();

                tableData[i][5] = null;
                tableData[i][6] = null;
                
                for (ChannelStatus status : parent.status.toArray(new ChannelStatus[]{})) {
                    if (status.getChannelId().equals(channel.getId())) {
                        tableData[i][5] = status.getDeployedRevisionDelta();
                        tableData[i][6] = status.getDeployedDate();
                    }
                }
                i++;
            }
        }

        if (channelTable != null) {
            RefreshTableModel model = (RefreshTableModel) channelTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            channelTable = new MirthTable();
            channelTable.setModel(new RefreshTableModel(tableData, new String[]{STATUS_COLUMN_NAME, PROTOCOL_COLUMN_NAME, NAME_COLUMN_NAME, ID_COLUMN_NAME, DESCRIPTION_COLUMN_NAME, DEPLOYED_REVISION_DELTA_COLUMN_NAME, LAST_DEPLOYED_COLUMN_NAME}) {

                boolean[] canEdit = new boolean[]{false, false, false, false, false, false, false};

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }

        // Set highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            channelTable.setHighlighters(highlighter);
        }
        
        HighlightPredicate revisionDeltaHighlighterPredicate = new HighlightPredicate() {
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == channelTable.getColumnViewIndex(DEPLOYED_REVISION_DELTA_COLUMN_NAME)) {
                    if (channelTable.getValueAt(adapter.row, adapter.column) != null && ((Integer) channelTable.getValueAt(adapter.row, adapter.column)).intValue() > 0) {
                        return true;
                    }
                }
                return false;
            }
        };
        channelTable.addHighlighter(new ColorHighlighter(revisionDeltaHighlighterPredicate, new Color(255, 204, 0), Color.BLACK, new Color(255, 204, 0), Color.BLACK));
        
        
        HighlightPredicate lastDeployedHighlighterPredicate = new HighlightPredicate() {
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == channelTable.getColumnViewIndex(LAST_DEPLOYED_COLUMN_NAME)) {
                    Calendar checkAfter = Calendar.getInstance();
                    checkAfter.add(Calendar.MINUTE, -2);

                    if (channelTable.getValueAt(adapter.row, adapter.column) != null && ((Calendar) channelTable.getValueAt(adapter.row, adapter.column)).after(checkAfter)) {
                        return true;
                    }
                }
                return false;
            }
        };
        channelTable.addHighlighter(new ColorHighlighter(lastDeployedHighlighterPredicate, new Color(240, 230, 140), Color.BLACK, new Color(240, 230, 140), Color.BLACK));
        
        // Packs the name column
        channelTable.packColumn(2, UIConstants.COL_MARGIN);
        
        // packs the ID column
        channelTable.packColumn(3, UIConstants.COL_MARGIN);
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.  Deselects the rows if no row was selected.
     */
    private void checkSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = channelTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (row == -1) {
            deselectRows();
        }

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                if (!channelTable.isRowSelected(row)) {
                    channelTable.setRowSelectionInterval(row, row);
                }
            }
            parent.channelPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /** The action called when a Channel is selected. Sets tasks as well. */
    private void ChannelListSelected(ListSelectionEvent evt) {
        int[] rows = channelTable.getSelectedModelRows();
        int column = channelTable.getColumnModelIndex(STATUS_COLUMN_NAME);

        if (rows.length > 0) {
            parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 2, 2, true);
            parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 8, -1, true);

            if (rows.length == 1) {
                if (((CellData) channelTable.getModel().getValueAt(rows[0], column)).getText().equals(ENABLED_STATUS)) {
                    parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 2, 2, true);
                } else {
                    parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 2, 2, false);
                }
            }

            if (rows.length > 1) {
                parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 10, 13, false); // hide edit, clone, enable, and disable
            } else {
                parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 12, 13, false); // hide enable and disable
            }

            for (int i = 0; i < rows.length; i++) {
                if (((CellData) channelTable.getModel().getValueAt(rows[i], column)).getText().equals(ENABLED_STATUS)) {
                    parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 13, 13, true);
                } else {
                    parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 12, 12, true);
                }
            }
        }
    }

    /**
     * Gets the selected channel index that corresponds to the saved channels
     * list
     */
    public List<Channel> getSelectedChannels() {
        int[] selectedRows = channelTable.getSelectedModelRows();
        List<Channel> selectedChannels = new ArrayList<Channel>();
        for (int i = 0; i < selectedRows.length; i++) {
            String channelId = (String) channelTable.getModel().getValueAt(selectedRows[i], channelTable.getColumnModelIndex(ID_COLUMN_NAME));
            Channel selectedChannel = parent.channels.get(channelId);
            if (selectedChannel != null) {
                selectedChannels.add(selectedChannel);
            }
        }

        return selectedChannels;
    }

    /** Sets a channel to be selected by taking it's id */
    public void setSelectedChannels(List<String> channelIds) {
        for (String channelId : channelIds) {
            for (int i = 0; i < parent.channels.values().size(); i++) {
                if (channelId.equals(channelTable.getModel().getValueAt(i, channelTable.getColumnModelIndex(ID_COLUMN_NAME)))) {
                    int row = channelTable.convertRowIndexToView(i);
                    channelTable.addRowSelectionInterval(row, row);
                }
            }
        }
    }

    public void deselectRows() {
        channelTable.clearSelection();
        parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 2, 2, false);
        parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 8, -1, false);
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);

                List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator<File> iterator = fileList.iterator();
                while (iterator.hasNext()) {
                    String fileName = (iterator.next()).getName();
                    if (!fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".xml")) {
                        dtde.rejectDrag();
                        return;
                    }
                }
            } else {
                dtde.rejectDrag();
            }
        } catch (Exception e) {
            dtde.rejectDrag();
        }
    }

    public void dragOver(DropTargetDragEvent dtde) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void drop(DropTargetDropEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator<File> iterator = fileList.iterator();

                if (fileList.size() == 1) {
                    File file = iterator.next();
                    parent.importChannel(file, true);
                } else {
                    while (iterator.hasNext()) {
                        File file = iterator.next();
                        parent.importChannel(file, false);
                    }
                }
            }
        } catch (Exception e) {
            dtde.rejectDrop();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        channelPane = new javax.swing.JScrollPane();
        channelTable = null;

        channelPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        channelPane.setViewportView(channelTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(channelPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(channelPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane channelPane;
    private com.mirth.connect.client.ui.components.MirthTable channelTable;
    // End of variables declaration//GEN-END:variables
}
