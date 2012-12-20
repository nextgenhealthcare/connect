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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DashboardStatus.StatusType;
import com.mirth.connect.plugins.DashboardColumnPlugin;
import com.mirth.connect.plugins.DashboardPanelPlugin;

public class DashboardPanel extends javax.swing.JPanel {

    private static final String STATUS_COLUMN_NAME = "Status";
    private static final String NAME_COLUMN_NAME = "Name";
    private static final String RECEIVED_COLUMN_NAME = "Received";
    private static final String QUEUED_COLUMN_NAME = "Queued";
    private static final String SENT_COLUMN_NAME = "Sent";
    private static final String ERROR_COLUMN_NAME = "Errored";
    private static final String FILTERED_COLUMN_NAME = "Filtered";
    private static final String ALERTED_COLUMN_NAME = "Alerted";
    private static final String LAST_DEPLOYED_COLUMN_NAME = "Last Deployed";
    private static final String DEPLOYED_REVISION_DELTA_COLUMN_NAME = "Rev \u0394";
    private static final String[] defaultColumns = new String[] { STATUS_COLUMN_NAME, NAME_COLUMN_NAME, DEPLOYED_REVISION_DELTA_COLUMN_NAME, LAST_DEPLOYED_COLUMN_NAME, RECEIVED_COLUMN_NAME, FILTERED_COLUMN_NAME, QUEUED_COLUMN_NAME, SENT_COLUMN_NAME, ERROR_COLUMN_NAME, ALERTED_COLUMN_NAME };

    private Frame parent;
    private boolean showLifetimeStats = false;

    public DashboardPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;

        initComponents();
        statusPane.setDoubleBuffered(true);
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

        makeStatusTable();
        

        this.setDoubleBuffered(true);
    }

    public void loadPanelPlugins() {
        if (LoadedExtensions.getInstance().getDashboardPanelPlugins().size() > 0) {
            for (DashboardPanelPlugin plugin : LoadedExtensions.getInstance().getDashboardPanelPlugins().values()) {
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
        DashboardPanelPlugin plugin = LoadedExtensions.getInstance().getDashboardPanelPlugins().get(pluginName);
        if (plugin != null && getSelectedStatuses().size() != 0) {
            plugin.update(getSelectedStatuses());
        } else {
            plugin.update();
        }
    }

    public synchronized void updateCurrentPluginPanel() {
        if (LoadedExtensions.getInstance().getDashboardPanelPlugins().size() > 0) {
            loadPanelPlugin(tabs.getTitleAt(tabs.getSelectedIndex()));
        }
    }

    /**
     * Makes the status table with all current server information.
     */
    public void makeStatusTable() {
        List<String> columns = new ArrayList<String>();

        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                columns.add(plugin.getColumnHeader());
            }
        }

        columns.addAll(Arrays.asList(defaultColumns));

        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
            if (!plugin.isDisplayFirst()) {
                columns.add(plugin.getColumnHeader());
            }
        }

        DashboardTreeTableModel model = new DashboardTreeTableModel();
        model.setColumnIdentifiers(columns);

        statusTable = new MirthTreeTable();
        statusTable.setColumnFactory(new DashboardTableColumnFactory());
        statusTable.setTreeTableModel(model);
        statusTable.setLeafIcon(UIConstants.ICON_CONNECTOR);
        statusTable.setOpenIcon(UIConstants.ICON_CHANNEL);
        statusTable.setClosedIcon(UIConstants.ICON_CHANNEL);
        statusTable.setDoubleBuffered(true);
        statusTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        statusTable.setHorizontalScrollEnabled(true);
        statusTable.packTable(UIConstants.COL_MARGIN);
        statusTable.setRowHeight(UIConstants.ROW_HEIGHT);
        statusTable.setOpaque(true);
        statusTable.setRowSelectionAllowed(true);
        statusTable.setSortable(true);
        statusTable.putClientProperty("JTree.lineStyle", "Horizontal");
        statusTable.setAutoCreateColumnsFromModel(false);
        statusTable.setShowGrid(true, true);
        
        // TODO: try using a custom tree cell renderer to set custom icons on connectors, so that we can display a chain icon to indicate that a destination waits for the previous one
        // http://www.java.net/forum/topic/javadesktop/java-desktop-technologies/swinglabs/jxtreetable-custom-icons-node
//        statusTable.setTreeCellRenderer(new DefaultTreeRenderer(new IconValue() {
//            @Override
//            public Icon getIcon(Object value) {
//                return UIConstants.ICON_CHANNEL;
//            }
//        }));

        // hack to make column headers clickable
        addColumnHeaderListeners();

        statusPane.setViewportView(statusTable);

        statusTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                checkSelectionAndPopupMenu(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                checkSelectionAndPopupMenu(event);
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                if (statusTable.rowAtPoint(new Point(event.getX(), event.getY())) == -1) {
                    return;
                }

                if (event.getClickCount() >= 2) {
                    parent.doShowMessages();
                }
            }
        });

        statusTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                updatePopupMenu();
            }
        });
    }

    // Make column headers clickable and render sorting arrows
    private void addColumnHeaderListeners() {
        // Add custom table header renderer to generate sorting arrows
        JTableHeader header = statusTable.getTableHeader();
        header.setDefaultRenderer(new SortableHeaderCellRenderer(header.getDefaultRenderer()));

        // Add mouse listener to detect clicks on column header
        statusTable.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JTableHeader h = (JTableHeader) e.getSource();
                TableColumnModel columnModel = h.getColumnModel();

                int viewColumn = h.columnAtPoint(e.getPoint());
                int column = columnModel.getColumn(viewColumn).getModelIndex();
                                                
                if (column != -1 && statusTable.getColumnExt(column).isSortable()) {
                    // Toggle sort order (ascending <-> descending)
                    SortableTreeTableModel model = (SortableTreeTableModel) statusTable.getTreeTableModel();
                    model.setColumnAndToggleSortOrder(column);
                    
                    // Set sorting icon and current column index
                    ((SortableHeaderCellRenderer) statusTable.getTableHeader().getDefaultRenderer()).setSortingIcon(model.getSortOrder());
                    ((SortableHeaderCellRenderer) statusTable.getTableHeader().getDefaultRenderer()).setColumnIndex(column);
                }
            }
        });
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been pushed. Deselects the rows if no row was selected.
     */
    private void checkSelectionAndPopupMenu(MouseEvent event) {
        TreePath path = statusTable.getPathForLocation(event.getX(), event.getY());

        if (path == null) {
            deselectRows();
        } else {
            updatePopupMenu();
        }

        if (event.isPopupTrigger()) {
            TreeSelectionModel selectionModel = statusTable.getTreeSelectionModel();

            if (!selectionModel.isPathSelected(path)) {
                deselectRows();
                selectionModel.addSelectionPath(path);
            }

            parent.dashboardPopupMenu.show(event.getComponent(), event.getX(), event.getY());
        }
    }

    /*
     * Action when something on the status list has been selected. Sets all appropriate tasks visible.
     */
    private void updatePopupMenu() {
        /*
         * 0 - Refresh
         * 1 - Send Message 
         * 2 - View Messages 
         * 3 - Remove All Messages 
         * 4 - Clear Statistics 
         * 5 - Start 
         * 6 - Pause 
         * 7 - Stop 
         * 8 - Halt 
         * 9 - Undeploy Channel
         */

        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 1, -1, true); // show all
        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 1, 1, false); // hide "Send Messages"
        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 3, 3, false); // hide "Remove All Messages"
        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 5, 9, false); // hide start, pause, stop, halt and undeploy

        boolean removeChannelOptions = false;
        
        for (DashboardTableNode node : statusTable.getSelectedNodes()) {
            DashboardStatus status = node.getStatus();
            StatusType statusType = status.getStatusType();

            if (statusType == StatusType.CHANNEL) {
                switch (status.getState()) {
                    case STARTED:
                        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 6, 7, true);
                        break;
                    case STARTING:
                        if (statusTable.getSelectedRowCount() == 1) {
                            parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 8, 8, true);
                        } else {
                            removeChannelOptions = true;
                        }
                        break;
                    case STOPPING:
                        if (statusTable.getSelectedRowCount() == 1) {
                            parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 8, 8, true);
                        } else {
                            removeChannelOptions = true;
                        }
                        break;
                    case PAUSED:
                        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 5, 5, true);
                        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 7, 7, true);
                        break;
                    default:
                        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 5, 5, true);
                        break;
                }
    
                parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 1, 1, true);
                parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 3, 3, true);
                parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 9, 9, true);
            }
            
            // If a selected channel is starting/stopping, do not show any options unless it is the only selected channel.
            if (removeChannelOptions) {
                parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 5, 9, false); // hide start, pause, stop, halt and undeploy
                break;
            }
        }
        
        if (showLifetimeStats) {
            parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 4, 4, false);
        }

        updateCurrentPluginPanel();
    }

    public synchronized void updateTable(List<DashboardStatus> statuses) {
        boolean tagFilterEnabled = parent.channelFilter.isTagFilterEnabled();
        Set<String> visibleTags = parent.channelFilter.getVisibleTags();
        
        if (tagFilterEnabled) {
            int totalChannelCount = statuses.size();
            List<DashboardStatus> filteredStatuses = new ArrayList<DashboardStatus>();
            
            for (DashboardStatus currentStatus : statuses) {
                if (tagFilterEnabled && CollectionUtils.containsAny(visibleTags, currentStatus.getTags())) {
                    filteredStatuses.add(currentStatus);
                }
            }
            
            statuses = filteredStatuses;
            tagsLabel.setText(statuses.size() + " of " + totalChannelCount + " Deployed Channels (" + StringUtils.join(visibleTags, ", ") + ")");
        } else {
            tagsLabel.setText(statuses.size() + " Deployed Channels");
        }
        
        DashboardTreeTableModel model = (DashboardTreeTableModel) statusTable.getTreeTableModel();
        model.setStatuses(statuses);
        model.setShowLifetimeStats(showLifetimeStatsButton.isSelected());
        
        // The ListSelectionListener is not notified that the tree table model has changed so we must update the menu items manually.
        // If we switch everything to use a TreeSelectionListener then we should remove this.
        if (statusTable.getSelectedRowCount() == 0) {
            deselectRows();
        } else {
            updatePopupMenu();
        }
        updateTableHighlighting();
    }
    
    public synchronized void updateTableHighlighting() {
        // MIRTH-2301
        // Since we are using addHighlighter here instead of using setHighlighters, we need to remove the old ones first.
        statusTable.setHighlighters();
        
        // Add the highlighters. Always add the error highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            statusTable.addHighlighter(highlighter);
        }

        HighlightPredicate errorHighlighterPredicate = new HighlightPredicate() {

            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == statusTable.getColumnViewIndex(ERROR_COLUMN_NAME)) {
                    Long value = (Long) statusTable.getValueAt(adapter.row, adapter.column);

                    if (value != null && value.longValue() > 0) {
                        return true;
                    }
                }
                return false;
            }
        };

        Highlighter errorHighlighter = new ColorHighlighter(errorHighlighterPredicate, Color.PINK, Color.BLACK, Color.PINK, Color.BLACK);
        statusTable.addHighlighter(errorHighlighter);

        HighlightPredicate revisionDeltaHighlighterPredicate = new HighlightPredicate() {
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == statusTable.getColumnViewIndex(DEPLOYED_REVISION_DELTA_COLUMN_NAME)) {
                    Integer value = (Integer) statusTable.getValueAt(adapter.row, adapter.column);

                    if (value != null && value.intValue() > 0) {
                        return true;
                    }
                }
                return false;
            }
        };

        statusTable.addHighlighter(new ColorHighlighter(revisionDeltaHighlighterPredicate, new Color(255, 204, 0), Color.BLACK, new Color(255, 204, 0), Color.BLACK));

        HighlightPredicate lastDeployedHighlighterPredicate = new HighlightPredicate() {
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == statusTable.getColumnViewIndex(LAST_DEPLOYED_COLUMN_NAME)) {
                    Calendar checkAfter = Calendar.getInstance();
                    checkAfter.add(Calendar.MINUTE, -2);

                    Object value = statusTable.getValueAt(adapter.row, adapter.column);

                    if (value != null && value instanceof Calendar && ((Calendar) value).after(checkAfter)) {
                        return true;
                    }
                }
                return false;
            }
        };

        statusTable.addHighlighter(new ColorHighlighter(lastDeployedHighlighterPredicate, new Color(240, 230, 140), Color.BLACK, new Color(240, 230, 140), Color.BLACK));
    }

    /**
     * Gets the index of the selected status row.
     */
    public synchronized List<DashboardStatus> getSelectedStatuses() {
        List<DashboardStatus> selectedStatuses = new ArrayList<DashboardStatus>();
        List<DashboardTableNode> selectedNodes = statusTable.getSelectedNodes();

        for (DashboardTableNode node : selectedNodes) {
            selectedStatuses.add(node.getStatus());
        }

        return selectedStatuses;
    }
    
    public synchronized Set<DashboardStatus> getSelectedChannelStatuses() {
        Set<DashboardStatus> selectedStatuses = new HashSet<DashboardStatus>();
        List<DashboardTableNode> selectedNodes = statusTable.getSelectedNodes();
        
        for (TreeTableNode treeNode : selectedNodes) {
            while (treeNode != null && treeNode instanceof DashboardTableNode) {
                DashboardTableNode node = (DashboardTableNode) treeNode;
                if (node.getStatus().getStatusType() == StatusType.CHANNEL) {
                    if (!selectedStatuses.contains(node.getStatus())) {
                        selectedStatuses.add(node.getStatus());
                    }
                    
                    break;
                }
                
                treeNode = treeNode.getParent();
            }
        }
        
        return selectedStatuses;
    }
    
    public synchronized List<DashboardStatus> getSelectedStatusesRecursive() {
        List<DashboardStatus> selectedStatuses = new ArrayList<DashboardStatus>();
        List<DashboardTableNode> selectedNodes = statusTable.getSelectedNodes();

        for (DashboardTableNode node : selectedNodes) {
            selectedStatuses.add(node.getStatus());
            selectedStatuses.addAll(getAllChildStatuses(node.getStatus()));
        }

        return selectedStatuses;
    }
    
    public Set<DashboardStatus> getAllChildStatuses(DashboardStatus status) {
        Set<DashboardStatus> statuses = new HashSet<DashboardStatus>();
        
        for (DashboardStatus childStatus : status.getChildStatuses()) {
            if (!statuses.contains(childStatus)) {
                statuses.add(childStatus);
            }
            
            statuses.addAll(getAllChildStatuses(childStatus));
        }
        
        return statuses;
    }

    public Map<Integer, String> getDestinationConnectorNames(String channelId) {
        Map<Integer, String> destinationConnectors = new LinkedHashMap<Integer, String>();
        DashboardTreeTableModel model = (DashboardTreeTableModel) statusTable.getTreeTableModel();
        TreeTableNode root = model.getRoot();
        int channelCount = model.getChildCount(root);

        for (int i = 0; i < channelCount; i++) {
            DashboardTableNode channelNode = (DashboardTableNode) root.getChildAt(i);

            if (channelNode.getStatus().getChannelId() == channelId) {
                int connectorCount = channelNode.getChildCount();

                for (int j = 0; j < connectorCount; j++) {
                    DashboardTableNode connectorNode = (DashboardTableNode) channelNode.getChildAt(j);
                    DashboardStatus status = connectorNode.getStatus();
                    Integer metaDataId = status.getMetaDataId();

                    if (metaDataId > 0) {
                        destinationConnectors.put(metaDataId, status.getName());
                    }
                }
            }
        }

        return destinationConnectors;
    }

    public void deselectRows() {
        statusTable.clearSelection();
        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 1, -1, false);
        updateCurrentPluginPanel();
    }
    
    public static int getNumberOfDefaultColumns() {
        return defaultColumns.length;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        split = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        statusPane = new javax.swing.JScrollPane();
        statusTable = null;
        controlPanel = new javax.swing.JPanel();
        showCurrentStatsButton = new javax.swing.JRadioButton();
        showLifetimeStatsButton = new javax.swing.JRadioButton();
        showLabel = new javax.swing.JLabel();
        tagsLabel = new javax.swing.JLabel();
        tagFilterButton = new com.mirth.connect.client.ui.components.IconButton();
        tabs = new javax.swing.JTabbedPane();

        split.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        split.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        statusPane.setBackground(new java.awt.Color(255, 255, 255));
        statusPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        statusPane.setViewportView(statusTable);

        controlPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(164, 164, 164)));
        controlPanel.setPreferredSize(new java.awt.Dimension(100, 20));

        buttonGroup1.add(showCurrentStatsButton);
        showCurrentStatsButton.setSelected(true);
        showCurrentStatsButton.setText("Current Statistics");
        showCurrentStatsButton.setToolTipText("Show the statistics accumulated since the last time the statistics were reset");
        showCurrentStatsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCurrentStatsButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(showLifetimeStatsButton);
        showLifetimeStatsButton.setText("Lifetime Statistics");
        showLifetimeStatsButton.setToolTipText("Show the statistics accumulated over the entire lifetime of the channel");
        showLifetimeStatsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showLifetimeStatsButtonActionPerformed(evt);
            }
        });

        showLabel.setText("Show:");

        tagFilterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mirth/connect/client/ui/images/wrench.png"))); // NOI18N
        tagFilterButton.setToolTipText("Show Channel Filter");
        tagFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagFilterButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tagFilterButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tagsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(showLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showCurrentStatsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(showLifetimeStatsButton)
                .addContainerGap())
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tagsLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(showLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(showCurrentStatsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(showLifetimeStatsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tagFilterButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPane, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
            .addComponent(controlPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(statusPane, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
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
            .addComponent(split, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void showCurrentStatsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCurrentStatsButtonActionPerformed
        DashboardTreeTableModel model = (DashboardTreeTableModel) statusTable.getTreeTableModel();
        showLifetimeStats = false;
        model.setShowLifetimeStats(showLifetimeStats);
        if (statusTable.getSelectedRowCount() == 0) {
            deselectRows();
        } else {
            updatePopupMenu();
        }
        
        // TODO: updateTableHighlighting() is called to force the table to refresh, there is probably a more direct way to do this
        updateTableHighlighting();
    }//GEN-LAST:event_showCurrentStatsButtonActionPerformed

    private void showLifetimeStatsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLifetimeStatsButtonActionPerformed
        DashboardTreeTableModel model = (DashboardTreeTableModel) statusTable.getTreeTableModel();
        showLifetimeStats = true;
        model.setShowLifetimeStats(showLifetimeStats);
        if (statusTable.getSelectedRowCount() == 0) {
            deselectRows();
        } else {
            updatePopupMenu();
        }
        
        // TODO: updateTableHighlighting() is called to force the table to refresh, there is probably a more direct way to do this
        updateTableHighlighting();
    }//GEN-LAST:event_showLifetimeStatsButtonActionPerformed

    private void tagFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tagFilterButtonActionPerformed
        parent.channelFilter.setVisible(true);
    }//GEN-LAST:event_tagFilterButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton showCurrentStatsButton;
    private javax.swing.JLabel showLabel;
    private javax.swing.JRadioButton showLifetimeStatsButton;
    private javax.swing.JSplitPane split;
    private javax.swing.JScrollPane statusPane;
    private com.mirth.connect.client.ui.components.MirthTreeTable statusTable;
    private javax.swing.JTabbedPane tabs;
    private com.mirth.connect.client.ui.components.IconButton tagFilterButton;
    private javax.swing.JLabel tagsLabel;
    // End of variables declaration//GEN-END:variables
}
