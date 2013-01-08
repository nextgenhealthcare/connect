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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DashboardStatus.StatusType;
import com.mirth.connect.plugins.ChannelColumnPlugin;
import com.mirth.connect.plugins.ChannelPanelPlugin;

public class ChannelPanel extends javax.swing.JPanel {

    private final String STATUS_COLUMN_NAME = "Status";
    private final String DATA_TYPE_COLUMN_NAME = "Data Type";
    private final String NAME_COLUMN_NAME = "Name";
    private final int NAME_COLUMN_NUMBER = 2;
    private final String DESCRIPTION_COLUMN_NAME = "Description";
    private final String ID_COLUMN_NAME = "Id";
    private final int ID_COLUMN_NUMBER = 3;
    private final String LAST_DEPLOYED_COLUMN_NAME = "Last Deployed";
    private final String DEPLOYED_REVISION_DELTA_COLUMN_NAME = "Rev \u0394";
    private final String ENABLED_STATUS = "Enabled";
    
    private final String[] DEFAULT_COLUMNS = new String[] { STATUS_COLUMN_NAME, DATA_TYPE_COLUMN_NAME, NAME_COLUMN_NAME, ID_COLUMN_NAME, DESCRIPTION_COLUMN_NAME, DEPLOYED_REVISION_DELTA_COLUMN_NAME, LAST_DEPLOYED_COLUMN_NAME };
    
    private Frame parent;

    /** Creates new form ChannelPanel */
    public ChannelPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();

        split.setBottomComponent(null);
        split.setDividerSize(0);
        split.setOneTouchExpandable(true);
        loadPanelPlugins();
        ChangeListener changeListener = new ChangeListener() {

            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                loadPanelPlugin(sourceTabbedPane.getTitleAt(index));
            }
        };
        tabs.addChangeListener(changeListener);

        makeChannelTable();

        channelPane.setComponentPopupMenu(parent.channelPopupMenu);
    }
    
    public void loadPanelPlugins() {
        if (LoadedExtensions.getInstance().getChannelPanelPlugins().size() > 0) {
            for (ChannelPanelPlugin plugin : LoadedExtensions.getInstance().getChannelPanelPlugins().values()) {
                if (plugin.getComponent() != null) {
                    tabs.addTab(plugin.getPluginPointName(), plugin.getComponent());
                }
            }

            split.setBottomComponent(tabs);
            split.setDividerSize(6);
            split.setDividerLocation(3 * Preferences.userNodeForPackage(Mirth.class).getInt("height", UIConstants.MIRTH_HEIGHT) / 5);
            split.setResizeWeight(0.5);
        }
    }
    
    public void loadPanelPlugin(String pluginName) {
        ChannelPanelPlugin plugin = LoadedExtensions.getInstance().getChannelPanelPlugins().get(pluginName);
        if (plugin != null && getSelectedChannels().size() != 0) {
            plugin.update(getSelectedChannels());
        } else {
            plugin.update();
        }
    }

    public synchronized void updateCurrentPluginPanel() {
        if (LoadedExtensions.getInstance().getChannelPanelPlugins().size() > 0) {
            loadPanelPlugin(tabs.getTitleAt(tabs.getSelectedIndex()));
        }
    }

    /** Creates the channel table */
    public void makeChannelTable() {
        updateChannelTable(null);
        channelTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                String columnName = plugin.getColumnHeader();
                channelTable.getColumnExt(columnName).setMaxWidth(plugin.getMaxWidth());
                channelTable.getColumnExt(columnName).setMinWidth(plugin.getMinWidth());
                channelTable.getColumnExt(columnName).setCellRenderer(plugin.getCellRenderer());
            }
        }
        
        channelTable.setHorizontalScrollEnabled(true);
        
        // Must set the maximum width on columns that should be packed.
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MIN_WIDTH);
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        channelTable.getColumnExt(STATUS_COLUMN_NAME).setToolTipText("<html><body>The status of this channel. Possible values are enabled and disabled.<br>Only enabled channels can be deployed.</body></html>");

        channelTable.getColumnExt(DATA_TYPE_COLUMN_NAME).setMaxWidth(UIConstants.MIN_WIDTH);
        channelTable.getColumnExt(DATA_TYPE_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        channelTable.getColumnExt(DATA_TYPE_COLUMN_NAME).setToolTipText("<html><body>The inbound data type of this channel's source connector.</body></html>");

        channelTable.getColumnExt(NAME_COLUMN_NAME).setMaxWidth(325);
        channelTable.getColumnExt(NAME_COLUMN_NAME).setMinWidth(150);
        channelTable.getColumnExt(NAME_COLUMN_NAME).setToolTipText("<html><body>The name of this channel.</body></html>");

        channelTable.getColumnExt(ID_COLUMN_NAME).setMinWidth(215);
        channelTable.getColumnExt(ID_COLUMN_NAME).setMaxWidth(215);
        channelTable.getColumnExt(ID_COLUMN_NAME).setToolTipText("<html><body>The unique id of this channel.</body></html>");

        channelTable.getColumnExt(DESCRIPTION_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        channelTable.getColumnExt(DESCRIPTION_COLUMN_NAME).setToolTipText("<html><body>The description of this channel.</body></html>");

        channelTable.getColumnExt(DEPLOYED_REVISION_DELTA_COLUMN_NAME).setMaxWidth(50);
        channelTable.getColumnExt(DEPLOYED_REVISION_DELTA_COLUMN_NAME).setMinWidth(50);
        channelTable.getColumnExt(DEPLOYED_REVISION_DELTA_COLUMN_NAME).setCellRenderer(new NumberCellRenderer(SwingConstants.CENTER, false));
        channelTable.getColumnExt(DEPLOYED_REVISION_DELTA_COLUMN_NAME).setToolTipText("<html><body>The number of times the channel was saved since this channel was deployed.<br>Rev \u0394 = Channel Revision - Deployed Revision</body></html>");
        channelTable.getColumnExt(DEPLOYED_REVISION_DELTA_COLUMN_NAME).setResizable(false);
        channelTable.getColumnExt(DEPLOYED_REVISION_DELTA_COLUMN_NAME).setToolTipText("<html><body>The number of times this channel was saved since it was deployed.<br>Rev \u0394 = Channel Revision - Deployed Revision<br>This value will be highlighted if it is greater than 0.</body></html>");

        channelTable.getColumnExt(LAST_DEPLOYED_COLUMN_NAME).setMinWidth(95);
        channelTable.getColumnExt(LAST_DEPLOYED_COLUMN_NAME).setMaxWidth(95);
        channelTable.getColumnExt(LAST_DEPLOYED_COLUMN_NAME).setCellRenderer(new DateCellRenderer());
        channelTable.getColumnExt(LAST_DEPLOYED_COLUMN_NAME).setResizable(false);
        channelTable.getColumnExt(LAST_DEPLOYED_COLUMN_NAME).setToolTipText("<html><body>The time this channel was last deployed.<br>This value will be highlighted if it is within the last two minutes.</body></html>");

        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            if (!plugin.isDisplayFirst()) {
                String columnName = plugin.getColumnHeader();
                channelTable.getColumnExt(columnName).setMaxWidth(plugin.getMaxWidth());
                channelTable.getColumnExt(columnName).setMinWidth(plugin.getMinWidth());
                channelTable.getColumnExt(columnName).setCellRenderer(plugin.getCellRenderer());
            }
        }
        
        channelTable.packTable(UIConstants.COL_MARGIN);
        
        channelTable.setRowHeight(UIConstants.ROW_HEIGHT);
        channelTable.setOpaque(true);
        channelTable.setRowSelectionAllowed(true);
        
        channelTable.setSortable(true);
        
        // Sort by Channel Name column
        channelTable.getRowSorter().toggleSortOrder(channelTable.getColumnModelIndex(NAME_COLUMN_NAME));

        channelPane.setViewportView(channelTable);

        class CustomTransferHandler extends TransferHandler {

            @Override
            protected Transferable createTransferable(JComponent c) {
                MirthTable table = (MirthTable) c;
                int[] rows = table.getSelectedModelRows();

                // Don't put anything on the clipboard if no rows are selected
                if (rows.length == 0) {
                    return null;
                }

                StringBuilder builder = new StringBuilder();

                for (int i = 0; i < rows.length; i++) {
                    builder.append(table.getModel().getValueAt(rows[i], NAME_COLUMN_NUMBER));
                    builder.append(" (");
                    builder.append(table.getModel().getValueAt(rows[i], ID_COLUMN_NUMBER));
                    builder.append(")");

                    if (i != rows.length - 1) {
                        builder.append("\n");
                    }
                }

                return new StringSelection(builder.toString());
            }

            @Override
            public int getSourceActions(JComponent c) {
                return COPY_OR_MOVE;
            }
            
            @Override
            public boolean importData(TransferSupport support) {
                if (canImport(support)) {
                    try {
                        List<File> fileList = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        boolean showAlerts = (fileList.size() == 1);

                        for (File file : fileList) {
                            if (FilenameUtils.isExtension(file.getName(), "xml")) {
                                parent.importChannel(parent.readFileToString(file), showAlerts);
                            }
                        }

                        return true;
                    } catch (Exception e) {
                        // Let it return false
                    }
                }

                return false;
            }
            
            @Override
            public boolean canImport(TransferSupport support) {
                if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        List<File> fileList = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                        for (File file : fileList) {
                            if (!FilenameUtils.isExtension(file.getName(), "xml")) {
                                return false;
                            }
                        }

                        return true;
                    } catch (Exception e) {
                        // Return true anyway until this bug is fixed:
                        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6759788
                        return true;
                    }
                }
                
                return false;
            }
        }

        channelTable.setDragEnabled(true);
        channelTable.setDropMode(DropMode.ON);
        channelTable.setTransferHandler(new CustomTransferHandler());

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

    public void updateChannelTable(List<Channel> channels) {
        Object[][] tableData = null;
        
        if (channels != null) {
            boolean tagFilterEnabled = parent.channelFilter.isTagFilterEnabled();
            Set<String> visibleTags = parent.channelFilter.getVisibleTags();
            List<Channel> filteredChannels = new ArrayList<Channel>();
            int enabled = 0;

            for (Channel channel : channels) {
                if (!tagFilterEnabled || CollectionUtils.containsAny(visibleTags, channel.getTags())) {
                    filteredChannels.add(channel);

                    if (channel.isEnabled()) {
                        enabled++;
                    }
                }
            }

            int totalChannelCount = channels.size();
            int visibleChannelCount = filteredChannels.size();

            if (tagFilterEnabled) {
                tagsLabel.setText(visibleChannelCount + " of " + totalChannelCount + " Channels, " + enabled + " Enabled (" + StringUtils.join(visibleTags, ", ") + ")");
            } else {
                tagsLabel.setText(totalChannelCount + " Channels, " + enabled + " Enabled");
            }
            
            for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
                plugin.tableUpdate(filteredChannels);
            }
            
            tableData = new Object[filteredChannels.size()][LoadedExtensions.getInstance().getChannelColumnPlugins().size() + DEFAULT_COLUMNS.length];
            int i = 0;
            
            for (Channel channel : filteredChannels) {
                int j = 0;
                
                for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
                    if (plugin.isDisplayFirst()){
                        tableData[i][j++] = plugin.getTableData(channel);
                    }
                }
                
                if (channel.isEnabled()) {
                    tableData[i][j++] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
                } else {
                    tableData[i][j++] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");
                }
                tableData[i][j++] = parent.dataTypeToDisplayName.get(channel.getSourceConnector().getTransformer().getInboundDataType());
                tableData[i][j++] = channel.getName();
                tableData[i][j++] = channel.getId();
                tableData[i][j++] = channel.getDescription();

                tableData[i][j] = null;
                tableData[i][j+1] = null;
                
                for (DashboardStatus status : parent.status.toArray(new DashboardStatus[]{})) {
                    if (status.getStatusType() == StatusType.CHANNEL && status.getChannelId().equals(channel.getId())) {
                        tableData[i][j] = status.getDeployedRevisionDelta();
                        tableData[i][j+1] = status.getDeployedDate();
                    }
                }
                
                j += 2;
                
                for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
                    if (!plugin.isDisplayFirst()){
                        tableData[i][j++] = plugin.getTableData(channel);
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
            
            ArrayList<String> columns = new ArrayList<String>();
            for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
                if (plugin.isDisplayFirst()) {
                    columns.add(plugin.getColumnHeader());
                }
            }
            for (int i = 0; i < DEFAULT_COLUMNS.length; i++) {
                columns.add(DEFAULT_COLUMNS[i]);
            }
            for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
                if (!plugin.isDisplayFirst()) {
                    columns.add(plugin.getColumnHeader());
                }
            }
            
            channelTable.setModel(new RefreshTableModel(tableData, columns.toArray(new String[0])) {

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return false;
                }
            });
        }
        
        // MIRTH-2301
        // Since we are using addHighlighter here instead of using setHighlighters, we need to remove the old ones first.
        channelTable.setHighlighters();

        // Set highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            channelTable.addHighlighter(highlighter);
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
            
            updateCurrentPluginPanel();
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
        TableModel model = channelTable.getModel();
        int rowCount = model.getRowCount();
        int idColumn = channelTable.getColumnModelIndex(ID_COLUMN_NAME);
        
        for (String channelId : channelIds) {
            for (int i = 0; i < rowCount; i++) {
                if (channelId.equals(model.getValueAt(i, idColumn))) {
                    int row = channelTable.convertRowIndexToView(i);
                    channelTable.addRowSelectionInterval(row, row);
                }
            }
        }
        
        // The plugin panel will be updated if any rows were selected above. If not, update it now.
        if (channelIds.size() == 0) {
            updateCurrentPluginPanel();
        }
    }

    public void deselectRows() {
        channelTable.clearSelection();
        parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 2, 2, false);
        parent.setVisibleTasks(parent.channelTasks, parent.channelPopupMenu, 8, -1, false);
        updateCurrentPluginPanel();
    }
    
    public List<String> getVisibleChannelIds() {
        TableModel model = channelTable.getModel();
        int rowCount = model.getRowCount();
        int idColumn = channelTable.getColumnModelIndex(ID_COLUMN_NAME);
        
        List<String> channelIds = new ArrayList<String>();
        
        for (int i = 0; i < rowCount; i++) {
            channelIds.add(model.getValueAt(i, idColumn).toString());
        }
        
        return channelIds;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        split = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        channelPane = new javax.swing.JScrollPane();
        channelTable = null;
        infoPanel = new javax.swing.JPanel();
        tagsLabel = new javax.swing.JLabel();
        tagFilterButton = new com.mirth.connect.client.ui.components.IconButton();
        tabs = new javax.swing.JTabbedPane();

        split.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        split.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        channelPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        channelPane.setViewportView(channelTable);

        infoPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(164, 164, 164)));

        tagFilterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mirth/connect/client/ui/images/wrench.png"))); // NOI18N
        tagFilterButton.setToolTipText("Show Channel Filter");
        tagFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagFilterButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout infoPanelLayout = new javax.swing.GroupLayout(infoPanel);
        infoPanel.setLayout(infoPanelLayout);
        infoPanelLayout.setHorizontalGroup(
            infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tagFilterButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tagsLabel)
                .addContainerGap(386, Short.MAX_VALUE))
        );
        infoPanelLayout.setVerticalGroup(
            infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tagsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(infoPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(tagFilterButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(infoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(channelPane)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(channelPane, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        split.setTopComponent(jPanel1);
        split.setRightComponent(tabs);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(split)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(split, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tagFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tagFilterButtonActionPerformed
        parent.channelFilter.setVisible(true);
    }//GEN-LAST:event_tagFilterButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane channelPane;
    private com.mirth.connect.client.ui.components.MirthTable channelTable;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSplitPane split;
    private javax.swing.JTabbedPane tabs;
    private com.mirth.connect.client.ui.components.IconButton tagFilterButton;
    private javax.swing.JLabel tagsLabel;
    // End of variables declaration//GEN-END:variables
}
