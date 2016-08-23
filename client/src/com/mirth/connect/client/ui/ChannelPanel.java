/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.client.ui.ChannelFilter.ChannelFilterSaveTask;
import com.mirth.connect.client.ui.Frame.ChannelTask;
import com.mirth.connect.client.ui.Frame.ConflictOption;
import com.mirth.connect.client.ui.codetemplate.CodeTemplateImportDialog;
import com.mirth.connect.client.ui.components.ChannelTableTransferHandler;
import com.mirth.connect.client.ui.components.IconButton;
import com.mirth.connect.client.ui.components.IconToggleButton;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.client.ui.dependencies.ChannelDependenciesWarningDialog;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplateLibrary;
import com.mirth.connect.model.CodeTemplateLibrarySaveResult;
import com.mirth.connect.model.CodeTemplateLibrarySaveResult.CodeTemplateUpdateResult;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.InvalidChannel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.ImportConverter3_0_0;
import com.mirth.connect.plugins.ChannelColumnPlugin;
import com.mirth.connect.plugins.ChannelPanelPlugin;
import com.mirth.connect.plugins.TaskPlugin;
import com.mirth.connect.util.ChannelDependencyException;
import com.mirth.connect.util.ChannelDependencyGraph;
import com.mirth.connect.util.DirectedAcyclicGraphNode;

public class ChannelPanel extends AbstractFramePanel {

    public static final String STATUS_COLUMN_NAME = "Status";
    public static final String DATA_TYPE_COLUMN_NAME = "Data Type";
    public static final String NAME_COLUMN_NAME = "Name";
    public static final String ID_COLUMN_NAME = "Id";
    public static final String LOCAL_CHANNEL_ID = "Local Id";
    public static final String DESCRIPTION_COLUMN_NAME = "Description";
    public static final String DEPLOYED_REVISION_DELTA_COLUMN_NAME = "Rev \u0394";
    public static final String LAST_DEPLOYED_COLUMN_NAME = "Last Deployed";
    public static final String LAST_MODIFIED_COLUMN_NAME = "Last Modified";

    public static final int STATUS_COLUMN_NUMBER = 0;
    public static final int DATA_TYPE_COLUMN_NUMBER = 1;
    public static final int NAME_COLUMN_NUMBER = 2;
    public static final int ID_COLUMN_NUMBER = 3;
    public static final int LOCAL_CHANNEL_ID_COLUMN_NUMBER = 4;
    public static final int DESCRIPTION_COLUMN_NUMBER = 5;
    public static final int DEPLOYED_REVISION_DELTA_COLUMN_NUMBER = 6;
    public static final int LAST_DEPLOYED_COLUMN_NUMBER = 7;
    public static final int LAST_MODIFIED_COLUMN_NUMBER = 8;

    private final static String[] DEFAULT_COLUMNS = new String[] { STATUS_COLUMN_NAME,
            DATA_TYPE_COLUMN_NAME, NAME_COLUMN_NAME, ID_COLUMN_NAME, LOCAL_CHANNEL_ID,
            DESCRIPTION_COLUMN_NAME, DEPLOYED_REVISION_DELTA_COLUMN_NAME,
            LAST_DEPLOYED_COLUMN_NAME, LAST_MODIFIED_COLUMN_NAME };

    private static final int TASK_CHANNEL_REFRESH = 0;
    private static final int TASK_CHANNEL_REDEPLOY_ALL = 1;
    private static final int TASK_CHANNEL_DEPLOY = 2;
    private static final int TASK_CHANNEL_EDIT_GLOBAL_SCRIPTS = 3;
    private static final int TASK_CHANNEL_EDIT_CODE_TEMPLATES = 4;
    private static final int TASK_CHANNEL_NEW_CHANNEL = 5;
    private static final int TASK_CHANNEL_IMPORT_CHANNEL = 6;
    private static final int TASK_CHANNEL_EXPORT_ALL_CHANNELS = 7;
    private static final int TASK_CHANNEL_EXPORT_CHANNEL = 8;
    private static final int TASK_CHANNEL_DELETE_CHANNEL = 9;
    private static final int TASK_CHANNEL_CLONE = 10;
    private static final int TASK_CHANNEL_EDIT = 11;
    private static final int TASK_CHANNEL_ENABLE = 12;
    private static final int TASK_CHANNEL_DISABLE = 13;
    private static final int TASK_CHANNEL_VIEW_MESSAGES = 14;

    private static final int TASK_GROUP_SAVE = 0;
    private static final int TASK_GROUP_ASSIGN_CHANNEL = 1;
    private static final int TASK_GROUP_NEW_GROUP = 2;
    private static final int TASK_GROUP_EDIT_DETAILS = 3;
    private static final int TASK_GROUP_IMPORT_GROUP = 4;
    private static final int TASK_GROUP_EXPORT_ALL_GROUPS = 5;
    private static final int TASK_GROUP_EXPORT_GROUP = 6;
    private static final int TASK_GROUP_DELETE_GROUP = 7;

    private Frame parent;

    private Map<String, ChannelStatus> channelStatuses = new LinkedHashMap<String, ChannelStatus>();
    private Map<String, ChannelGroupStatus> groupStatuses = new LinkedHashMap<String, ChannelGroupStatus>();
    private Set<ChannelDependency> channelDependencies = new HashSet<ChannelDependency>();

    public ChannelPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initLayout();

        channelTasks = new JXTaskPane();
        channelTasks.setTitle("Channel Tasks");
        channelTasks.setName(TaskConstants.CHANNEL_KEY);
        channelTasks.setFocusable(false);

        channelPopupMenu = new JPopupMenu();
        channelTable.setComponentPopupMenu(channelPopupMenu);

        parent.addTask(TaskConstants.CHANNEL_REFRESH, "Refresh", "Refresh the list of channels.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_REDEPLOY_ALL, "Redeploy All", "Undeploy all channels and deploy all currently enabled channels.", "A", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_rotate_clockwise.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_DEPLOY, "Deploy Channel", "Deploys the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_redo.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_EDIT_GLOBAL_SCRIPTS, "Edit Global Scripts", "Edit scripts that are not channel specific.", "G", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/script_edit.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_EDIT_CODE_TEMPLATES, "Edit Code Templates", "Create and manage templates to be used in JavaScript throughout Mirth.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/page_edit.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_NEW_CHANNEL, "New Channel", "Create a new channel.", "N", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_add.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_IMPORT_CHANNEL, "Import Channel", "Import a channel from an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_EXPORT_ALL_CHANNELS, "Export All Channels", "Export all of the channels to XML files.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_EXPORT_CHANNEL, "Export Channel", "Export the currently selected channel to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_DELETE_CHANNEL, "Delete Channel", "Delete the currently selected channel.", "L", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_delete.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_CLONE, "Clone Channel", "Clone the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/page_copy.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_EDIT, "Edit Channel", "Edit the currently selected channel.", "I", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_edit.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_ENABLE, "Enable Channel", "Enable the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_DISABLE, "Disable Channel", "Disable the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_stop_blue.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_VIEW_MESSAGES, "View Messages", "Show the messages for the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/page_white_stack.png")), channelTasks, channelPopupMenu, this);

        parent.setNonFocusable(channelTasks);
        parent.taskPaneContainer.add(channelTasks, parent.taskPaneContainer.getComponentCount() - 1);

        groupTasks = new JXTaskPane();
        groupTasks.setTitle("Group Tasks");
        groupTasks.setName(TaskConstants.CHANNEL_GROUP_KEY);
        groupTasks.setFocusable(false);

        groupPopupMenu = new JPopupMenu();

        parent.addTask(TaskConstants.CHANNEL_GROUP_SAVE, "Save Group Changes", "Save all changes made to channel groups.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/disk.png")), groupTasks, groupPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_GROUP_ASSIGN_CHANNEL, "Assign To Group", "Assign channel(s) to a group.", "A", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), groupTasks, groupPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_GROUP_NEW_GROUP, "New Group", "Create a new channel group.", "N", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_add.png")), groupTasks, groupPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_GROUP_EDIT_DETAILS, "Edit Group Details", "Edit group name and description.", "E", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_edit.png")), groupTasks, groupPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_GROUP_IMPORT_GROUP, "Import Group", "Import a channel group from an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), groupTasks, groupPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_GROUP_EXPORT_ALL_GROUPS, "Export All Groups", "Export all of the channel groups to XML files.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), groupTasks, groupPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_GROUP_EXPORT_GROUP, "Export Group", "Export the currently selected channel group to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), groupTasks, groupPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_GROUP_DELETE_GROUP, "Delete Group", "Delete the currently selected channel group.", "L", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_delete.png")), groupTasks, groupPopupMenu, this);

        parent.setNonFocusable(groupTasks);
        parent.taskPaneContainer.add(groupTasks, parent.taskPaneContainer.getComponentCount() - 1);

        channelScrollPane.setComponentPopupMenu(channelPopupMenu);

        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("channelGroupViewEnabled", true)) {
            tableModeGroupsButton.setSelected(true);
            tableModeGroupsButton.setContentFilled(true);
            tableModeChannelsButton.setContentFilled(false);
            model.setGroupModeEnabled(true);
        } else {
            tableModeChannelsButton.setSelected(true);
            tableModeChannelsButton.setContentFilled(true);
            tableModeGroupsButton.setContentFilled(false);
            model.setGroupModeEnabled(false);
        }

        updateModel(new TableState(new ArrayList<String>(), null));
        updateTasks();
    }

    @Override
    public void switchPanel() {
        boolean groupViewEnabled = Preferences.userNodeForPackage(Mirth.class).getBoolean("channelGroupViewEnabled", true);
        switchTableMode(groupViewEnabled, false);

        if (groupViewEnabled) {
            tableModeGroupsButton.setSelected(true);
            tableModeGroupsButton.setContentFilled(true);
            tableModeChannelsButton.setContentFilled(false);
        } else {
            tableModeChannelsButton.setSelected(true);
            tableModeChannelsButton.setContentFilled(true);
            tableModeGroupsButton.setContentFilled(false);
        }

        List<JXTaskPane> taskPanes = new ArrayList<JXTaskPane>();
        taskPanes.add(channelTasks);

        if (groupViewEnabled) {
            taskPanes.add(groupTasks);
        }

        for (TaskPlugin plugin : LoadedExtensions.getInstance().getTaskPlugins().values()) {
            JXTaskPane taskPane = plugin.getTaskPane();
            if (taskPane != null) {
                taskPanes.add(taskPane);
            }
        }

        parent.setBold(parent.viewPane, 1);
        parent.setPanelName("Channels");
        parent.setCurrentContentPage(ChannelPanel.this);
        parent.setFocus(taskPanes.toArray(new JXTaskPane[taskPanes.size()]), true, true);
        parent.setSaveEnabled(false);

        doRefreshChannels();
    }

    @Override
    public boolean isSaveEnabled() {
        return groupTasks.getContentPane().getComponent(TASK_GROUP_SAVE).isVisible();
    }

    @Override
    public void setSaveEnabled(boolean enabled) {
        setGroupTaskVisibility(TASK_GROUP_SAVE, enabled);
    }

    @Override
    public boolean changesHaveBeenMade() {
        return isSaveEnabled();
    }

    @Override
    public void doContextSensitiveSave() {
        if (isSaveEnabled()) {
            doSaveGroups();
        }
    }

    @Override
    public boolean confirmLeave() {
        return promptSave(false);
    }

    private boolean promptSave(boolean force) {
        int option;

        if (force) {
            option = JOptionPane.showConfirmDialog(parent, "You must save the channel group changes before continuing. Would you like to save now?");
        } else {
            option = JOptionPane.showConfirmDialog(parent, "Would you like to save the channel groups?");
        }

        if (option == JOptionPane.YES_OPTION) {
            return doSaveGroups(false);
        } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION || (option == JOptionPane.NO_OPTION && force)) {
            return false;
        }

        return true;
    }

    @Override
    protected Component addAction(Action action, Set<String> options) {
        Component taskComponent = channelTasks.add(action);
        channelPopupMenu.add(action);
        return taskComponent;
    }

    public Map<String, ChannelStatus> getCachedChannelStatuses() {
        return channelStatuses;
    }

    public Map<String, ChannelGroupStatus> getCachedGroupStatuses() {
        return groupStatuses;
    }

    public Set<ChannelDependency> getCachedChannelDependencies() {
        return channelDependencies;
    }

    public void doRefreshChannels() {
        doRefreshChannels(true);
    }

    public void doRefreshChannels(boolean queue) {
        if (isSaveEnabled() && !confirmLeave()) {
            return;
        }

        QueuingSwingWorkerTask<Void, Void> task = new QueuingSwingWorkerTask<Void, Void>("doRefreshChannels", "Loading channels...") {
            @Override
            public Void doInBackground() {
                retrieveChannels();
                return null;
            }

            @Override
            public void done() {
                updateModel(getCurrentTableState());
                updateTasks();
                parent.setSaveEnabled(false);
            }
        };

        new QueuingSwingWorker<Void, Void>(task, queue).executeDelegate();
    }

    private void updateTasks() {
        int[] rows = channelTable.getSelectedModelRows();
        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();
        boolean filterEnabled = parent.getChannelTagInfo(false).isEnabled();
        boolean saveEnabled = isSaveEnabled();

        setAllTaskVisibility(false);

        setChannelTaskVisible(TASK_CHANNEL_REFRESH);
        setChannelTaskVisible(TASK_CHANNEL_REDEPLOY_ALL);
        setChannelTaskVisible(TASK_CHANNEL_EDIT_GLOBAL_SCRIPTS);
        setChannelTaskVisible(TASK_CHANNEL_EDIT_CODE_TEMPLATES);
        setChannelTaskVisible(TASK_CHANNEL_NEW_CHANNEL);
        setChannelTaskVisible(TASK_CHANNEL_IMPORT_CHANNEL);
        if (model.isGroupModeEnabled()) {
            if (!filterEnabled) {
                setGroupTaskVisible(TASK_GROUP_NEW_GROUP);

                if (!saveEnabled) {
                    setGroupTaskVisible(TASK_GROUP_IMPORT_GROUP);
                }
            }

            if (!saveEnabled) {
                setGroupTaskVisible(TASK_GROUP_EXPORT_ALL_GROUPS);
            }
        } else {
            setChannelTaskVisible(TASK_CHANNEL_EXPORT_ALL_CHANNELS);
        }

        if (rows.length > 0) {
            boolean allGroups = true;
            boolean allChannels = true;
            boolean allEnabled = true;
            boolean allDisabled = true;
            boolean channelNodeFound = false;
            boolean includesDefaultGroup = false;

            for (int row : rows) {
                AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent();
                if (node.isGroupNode()) {
                    allChannels = false;

                    for (Enumeration<? extends MutableTreeTableNode> channelNodes = node.children(); channelNodes.hasMoreElements();) {
                        AbstractChannelTableNode channelNode = (AbstractChannelTableNode) channelNodes.nextElement();
                        if (channelNode.getChannelStatus().getChannel().isEnabled()) {
                            allDisabled = false;
                        } else {
                            allEnabled = false;
                        }
                        channelNodeFound = true;
                    }

                    if (StringUtils.equals(node.getGroupStatus().getGroup().getId(), ChannelGroup.DEFAULT_ID)) {
                        includesDefaultGroup = true;
                    }
                } else {
                    allGroups = false;

                    if (node.getChannelStatus().getChannel().isEnabled()) {
                        allDisabled = false;
                    } else {
                        allEnabled = false;
                    }
                }
            }

            if (!allGroups || channelNodeFound) {
                if (!allDisabled) {
                    setChannelTaskVisible(TASK_CHANNEL_DISABLE);
                }
                if (!allEnabled) {
                    setChannelTaskVisible(TASK_CHANNEL_ENABLE);
                }
            }

            if (allGroups) {
                if (rows.length == 1 && !includesDefaultGroup && !filterEnabled) {
                    setGroupTaskVisible(TASK_GROUP_EDIT_DETAILS);
                }

                if (channelNodeFound && !allDisabled) {
                    setChannelTaskVisible(TASK_CHANNEL_DEPLOY);
                }

                if (!saveEnabled) {
                    setGroupTaskVisible(TASK_GROUP_EXPORT_GROUP);
                }

                if (!includesDefaultGroup && !parent.getChannelTagInfo(false).isEnabled()) {
                    setGroupTaskVisible(TASK_GROUP_DELETE_GROUP);
                }
            } else if (allChannels) {
                if (!allDisabled) {
                    setChannelTaskVisible(TASK_CHANNEL_DEPLOY);
                }
                if (!filterEnabled && model.isGroupModeEnabled()) {
                    setGroupTaskVisible(TASK_GROUP_ASSIGN_CHANNEL);
                }
                setChannelTaskVisible(TASK_CHANNEL_EXPORT_CHANNEL);
                setChannelTaskVisible(TASK_CHANNEL_DELETE_CHANNEL);

                if (rows.length == 1) {
                    setChannelTaskVisible(TASK_CHANNEL_CLONE);
                    setChannelTaskVisible(TASK_CHANNEL_EDIT);
                    setChannelTaskVisible(TASK_CHANNEL_VIEW_MESSAGES);
                }
            } else {
                setChannelTaskVisible(TASK_CHANNEL_DEPLOY);
            }
        }
    }

    public void retrieveGroups() {
        try {
            updateChannelGroups(parent.mirthClient.getAllChannelGroups());
        } catch (ClientException e) {
            parent.alertThrowable(parent, e, false);
        }
    }

    public void retrieveChannels() {
        try {
            updateChannelStatuses(parent.mirthClient.getChannelSummary(getChannelHeaders(), false));
            updateChannelGroups(parent.mirthClient.getAllChannelGroups());
            channelDependencies = parent.mirthClient.getChannelDependencies();
        } catch (ClientException e) {
            parent.alertThrowable(parent, e);
        }
    }

    public void retrieveDependencies() {
        try {
            channelDependencies = parent.mirthClient.getChannelDependencies();
        } catch (ClientException e) {
            parent.alertThrowable(parent, e);
        }
    }

    public Map<String, ChannelHeader> getChannelHeaders() {
        Map<String, ChannelHeader> channelHeaders = new HashMap<String, ChannelHeader>();

        for (ChannelStatus channelStatus : channelStatuses.values()) {
            Channel channel = channelStatus.getChannel();
            channelHeaders.put(channel.getId(), new ChannelHeader(channel.getRevision(), channelStatus.getDeployedDate(), channelStatus.isCodeTemplatesChanged()));
        }

        return channelHeaders;
    }

    public boolean doSaveGroups() {
        return doSaveGroups(true);
    }

    public boolean doSaveGroups(boolean asynchronous) {
        if (parent.getChannelTagInfo(false).isEnabled()) {
            return false;
        }

        Set<ChannelGroup> channelGroups = new HashSet<ChannelGroup>();
        Set<String> removedChannelGroupIds = new HashSet<String>(groupStatuses.keySet());
        removedChannelGroupIds.remove(ChannelGroup.DEFAULT_ID);

        MutableTreeTableNode root = (MutableTreeTableNode) channelTable.getTreeTableModel().getRoot();
        if (root == null) {
            return false;
        }

        for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
            ChannelGroup group = ((AbstractChannelTableNode) groupNodes.nextElement()).getGroupStatus().getGroup();
            if (!StringUtils.equals(group.getId(), ChannelGroup.DEFAULT_ID)) {
                channelGroups.add(group);
                removedChannelGroupIds.remove(group.getId());
            }

            if (StringUtils.isBlank(group.getName())) {
                parent.alertError(parent, "One or more groups have a blank name.");
                return false;
            }
        }

        if (asynchronous) {
            new UpdateSwingWorker(channelGroups, removedChannelGroupIds, false).execute();
        } else {
            return attemptUpdate(channelGroups, removedChannelGroupIds, false);
        }

        return true;
    }

    private boolean attemptUpdate(Set<ChannelGroup> channelGroups, Set<String> removedChannelGroupIds, boolean override) {
        boolean result = false;
        boolean tryAgain = false;

        try {
            result = updateGroups(channelGroups, removedChannelGroupIds, override);

            if (result) {
                afterUpdate();
            } else {
                if (!override) {
                    if (parent.alertOption(parent, "One or more channel groups have been modified since you last refreshed.\nDo you want to overwrite the changes?")) {
                        tryAgain = true;
                    }
                } else {
                    parent.alertError(parent, "Unable to save channel groups.");
                }
            }
        } catch (Exception e) {
            Throwable cause = e;
            if (cause instanceof ExecutionException) {
                cause = e.getCause();
            }
            parent.alertThrowable(parent, cause, "Unable to save channel groups: " + cause.getMessage());
        }

        if (tryAgain && !override) {
            return attemptUpdate(channelGroups, removedChannelGroupIds, true);
        }

        return result;
    }

    private boolean updateGroups(Set<ChannelGroup> channelGroups, Set<String> removedChannelGroupIds, boolean override) throws ClientException {
        return parent.mirthClient.updateChannelGroups(channelGroups, removedChannelGroupIds, override);
    }

    private void afterUpdate() {
        parent.setSaveEnabled(false);
        doRefreshChannels();
    }

    public class UpdateSwingWorker extends SwingWorker<Boolean, Void> {

        private Set<ChannelGroup> channelGroups;
        private Set<String> removedChannelGroupIds;
        private boolean override;
        private String workingId;

        public UpdateSwingWorker(Set<ChannelGroup> channelGroups, Set<String> removedChannelGroupIds, boolean override) {
            this.channelGroups = channelGroups;
            this.removedChannelGroupIds = removedChannelGroupIds;
            this.override = override;
            workingId = parent.startWorking("Saving channel groups...");
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            return updateGroups(channelGroups, removedChannelGroupIds, override);
        }

        @Override
        protected void done() {
            boolean tryAgain = false;

            try {
                Boolean result = get();

                if (result) {
                    afterUpdate();
                } else {
                    if (!override) {
                        if (parent.alertOption(parent, "One or more channel groups have been modified since you last refreshed.\nDo you want to overwrite the changes?")) {
                            tryAgain = true;
                        }
                    } else {
                        parent.alertError(parent, "Unable to save channel groups.");
                    }
                }
            } catch (Exception e) {
                Throwable cause = e;
                if (cause instanceof ExecutionException) {
                    cause = e.getCause();
                }
                parent.alertThrowable(parent, cause, "Unable to save channel groups: " + cause.getMessage());
            } finally {
                parent.stopWorking(workingId);

                if (tryAgain && !override) {
                    new UpdateSwingWorker(channelGroups, removedChannelGroupIds, true).execute();
                }
            }
        }
    }

    public void doRedeployAll() {
        if (!parent.alertOption(parent, "Are you sure you want to redeploy all channels?")) {
            return;
        }

        final String workingId = parent.startWorking("Deploying channels...");
        parent.dashboardPanel.deselectRows(false);
        parent.doShowDashboard();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                try {
                    parent.mirthClient.redeployAllChannels();
                } catch (ClientException e) {
                    parent.alertThrowable(parent, e);
                }
                return null;
            }

            @Override
            public void done() {
                parent.stopWorking(workingId);
                parent.doRefreshStatuses(true);
            }
        };

        worker.execute();
    }

    public void doDeployChannel() {
        List<Channel> selectedChannels = getSelectedChannels();
        if (selectedChannels.size() == 0) {
            parent.alertWarning(parent, "Channel no longer exists.");
            return;
        }

        // Only deploy enabled channels
        final Set<String> selectedEnabledChannelIds = new LinkedHashSet<String>();
        boolean channelDisabled = false;
        for (Channel channel : selectedChannels) {
            if (channel.isEnabled()) {
                selectedEnabledChannelIds.add(channel.getId());
            } else {
                channelDisabled = true;
            }
        }

        if (channelDisabled) {
            parent.alertWarning(parent, "Disabled channels will not be deployed.");
        }

        // If there are any channel dependencies, decide if we need to warn the user on deploy.
        try {
            ChannelDependencyGraph channelDependencyGraph = new ChannelDependencyGraph(channelDependencies);

            Set<String> deployedChannelIds = new HashSet<String>();
            if (parent.status != null) {
                for (DashboardStatus dashboardStatus : parent.status) {
                    deployedChannelIds.add(dashboardStatus.getChannelId());
                }
            }

            // For each selected channel, add any dependent/dependency channels as necessary
            Set<String> channelIdsToDeploy = new HashSet<String>();
            for (String channelId : selectedEnabledChannelIds) {
                addChannelToDeploySet(channelId, channelDependencyGraph, deployedChannelIds, channelIdsToDeploy);
            }

            // If additional channels were added to the set, we need to prompt the user
            if (!CollectionUtils.subtract(channelIdsToDeploy, selectedEnabledChannelIds).isEmpty()) {
                ChannelDependenciesWarningDialog dialog = new ChannelDependenciesWarningDialog(ChannelTask.DEPLOY, channelDependencies, selectedEnabledChannelIds, channelIdsToDeploy);
                if (dialog.getResult() == JOptionPane.OK_OPTION) {
                    if (dialog.isIncludeOtherChannels()) {
                        selectedEnabledChannelIds.addAll(channelIdsToDeploy);
                    }
                } else {
                    return;
                }
            }
        } catch (ChannelDependencyException e) {
            // Should never happen
            e.printStackTrace();
        }

        parent.deployChannel(selectedEnabledChannelIds);
    }

    private void addChannelToDeploySet(String channelId, ChannelDependencyGraph channelDependencyGraph, Set<String> deployedChannelIds, Set<String> channelIdsToDeploy) {
        if (!channelIdsToDeploy.add(channelId)) {
            return;
        }

        DirectedAcyclicGraphNode<String> node = channelDependencyGraph.getNode(channelId);

        if (node != null) {
            for (String dependentChannelId : node.getDirectDependentElements()) {
                ChannelStatus channelStatus = channelStatuses.get(dependentChannelId);

                // Only add the dependent channel if it's enabled and currently deployed
                if (channelStatus != null && channelStatus.getChannel().isEnabled() && deployedChannelIds.contains(dependentChannelId)) {
                    addChannelToDeploySet(dependentChannelId, channelDependencyGraph, deployedChannelIds, channelIdsToDeploy);
                }
            }

            for (String dependencyChannelId : node.getDirectDependencyElements()) {
                ChannelStatus channelStatus = channelStatuses.get(dependencyChannelId);

                // Only add the dependency channel it it's enabled
                if (channelStatus != null && channelStatus.getChannel().isEnabled()) {
                    addChannelToDeploySet(dependencyChannelId, channelDependencyGraph, deployedChannelIds, channelIdsToDeploy);
                }
            }
        }
    }

    public void doEditGlobalScripts() {
        if (isSaveEnabled() && !confirmLeave()) {
            return;
        }

        parent.doEditGlobalScripts();
    }

    public void doEditCodeTemplates() {
        if (isSaveEnabled() && !confirmLeave()) {
            return;
        }

        parent.doEditCodeTemplates();
    }

    public void doNewGroup() {
        if (parent.getChannelTagInfo(false).isEnabled()) {
            return;
        }

        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();
        MutableTreeTableNode root = (MutableTreeTableNode) model.getRoot();
        if (root == null) {
            return;
        }

        GroupDetailsDialog dialog = new GroupDetailsDialog(true);

        if (dialog.wasSaved()) {
            AbstractChannelTableNode groupNode = model.addNewGroup(new ChannelGroup(dialog.getGroupName(), dialog.getGroupDescription()));

            parent.setSaveEnabled(true);

            final TreePath path = new TreePath(new Object[] { root, groupNode });
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    channelTable.getTreeSelectionModel().setSelectionPath(path);
                }
            });
        }
    }

    private boolean checkGroupId(String id) {
        MutableTreeTableNode root = (MutableTreeTableNode) channelTable.getTreeTableModel().getRoot();
        if (root == null) {
            return false;
        }

        for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
            if (StringUtils.equals(((AbstractChannelTableNode) groupNodes.nextElement()).getGroupStatus().getGroup().getId(), id)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkGroupName(String name) {
        return checkGroupName(name, true);
    }

    private boolean checkGroupName(String name, boolean includeSelectedRow) {
        MutableTreeTableNode root = (MutableTreeTableNode) channelTable.getTreeTableModel().getRoot();
        if (root == null) {
            return false;
        }

        for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
            AbstractChannelTableNode groupNode = (AbstractChannelTableNode) groupNodes.nextElement();

            if (!includeSelectedRow && channelTable.getSelectedRow() != -1) {
                AbstractChannelTableNode selectedNode = (AbstractChannelTableNode) channelTable.getPathForRow(channelTable.getSelectedRow()).getLastPathComponent();
                if (selectedNode.isGroupNode() && selectedNode.getGroupStatus().getGroup().getId().equals(groupNode.getGroupStatus().getGroup().getId())) {
                    continue;
                }
            }

            if (StringUtils.equals(groupNode.getGroupStatus().getGroup().getName(), name)) {
                return false;
            }
        }

        return true;
    }

    public void doAssignChannelToGroup() {
        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();
        int[] rows = channelTable.getSelectedModelRows();

        if (model.isGroupModeEnabled() && rows.length > 0) {
            for (int row : rows) {
                if (((AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent()).isGroupNode()) {
                    return;
                }
            }

            GroupAssignmentDialog dialog = new GroupAssignmentDialog();

            if (dialog.wasSaved()) {
                AbstractChannelTableNode groupNode = null;
                for (Enumeration<? extends MutableTreeTableNode> groupNodes = ((MutableTreeTableNode) model.getRoot()).children(); groupNodes.hasMoreElements();) {
                    AbstractChannelTableNode node = (AbstractChannelTableNode) groupNodes.nextElement();
                    if (StringUtils.equals(node.getGroupStatus().getGroup().getId(), dialog.getSelectedGroupId())) {
                        groupNode = node;
                        break;
                    }
                }

                if (groupNode != null) {
                    TableState tableState = getCurrentTableState();
                    tableState.getExpandedGroupIds().add(groupNode.getGroupStatus().getGroup().getId());

                    ListSelectionListener[] listeners = ((DefaultListSelectionModel) channelTable.getSelectionModel()).getListSelectionListeners();
                    for (ListSelectionListener listener : listeners) {
                        channelTable.getSelectionModel().removeListSelectionListener(listener);
                    }

                    try {
                        List<AbstractChannelTableNode> channelNodes = new ArrayList<AbstractChannelTableNode>();
                        for (int row : rows) {
                            channelNodes.add((AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent());
                        }
                        for (AbstractChannelTableNode channelNode : channelNodes) {
                            model.addChannelToGroup(groupNode, channelNode.getChannelStatus().getChannel().getId());
                        }
                        channelTable.expandPath(new TreePath(new Object[] {
                                channelTable.getTreeTableModel().getRoot(), groupNode }));

                        parent.setSaveEnabled(true);
                    } finally {
                        for (ListSelectionListener listener : listeners) {
                            channelTable.getSelectionModel().addListSelectionListener(listener);
                        }

                        restoreTableState(tableState);
                    }
                }
            }
        }
    }

    public void doEditGroupDetails() {
        if (parent.getChannelTagInfo(false).isEnabled()) {
            return;
        }

        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();
        MutableTreeTableNode root = (MutableTreeTableNode) model.getRoot();
        if (root == null) {
            return;
        }

        int[] rows = channelTable.getSelectedModelRows();

        if (rows.length == 1) {
            AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(rows[0]).getLastPathComponent();

            if (node.isGroupNode() && !StringUtils.equals(node.getGroupStatus().getGroup().getId(), ChannelGroup.DEFAULT_ID)) {
                GroupDetailsDialog dialog = new GroupDetailsDialog(false);

                if (dialog.wasSaved()) {
                    channelTable.getTreeTableModel().setValueAt(new ChannelTableNameEntry(dialog.getGroupName()), node, NAME_COLUMN_NUMBER);
                    channelTable.getTreeTableModel().setValueAt(dialog.getGroupDescription(), node, DESCRIPTION_COLUMN_NUMBER);

                    parent.setSaveEnabled(true);
                }
            }
        }
    }

    public void doNewChannel() {
        if (isSaveEnabled() && !promptSave(true)) {
            return;
        }

        if (LoadedExtensions.getInstance().getSourceConnectors().size() == 0 || LoadedExtensions.getInstance().getDestinationConnectors().size() == 0) {
            parent.alertError(parent, "You must have at least one source connector and one destination connector installed.");
            return;
        }

        // The channel wizard will call createNewChannel() or create a channel
        // from a wizard.
        new ChannelWizard();
    }

    public void createNewChannel() {
        if (isSaveEnabled() && !promptSave(true)) {
            return;
        }

        Channel channel = new Channel();

        try {
            channel.setId(parent.mirthClient.getGuid());
        } catch (ClientException e) {
            parent.alertThrowable(parent, e);
        }

        channel.setName("");

        Set<String> selectedGroupIds = new HashSet<String>();

        if (((ChannelTreeTableModel) channelTable.getTreeTableModel()).isGroupModeEnabled()) {
            for (int row : channelTable.getSelectedModelRows()) {
                TreePath path = channelTable.getPathForRow(row);
                if (path != null) {
                    AbstractChannelTableNode node = (AbstractChannelTableNode) path.getLastPathComponent();
                    if (node.isGroupNode()) {
                        selectedGroupIds.add(node.getGroupStatus().getGroup().getId());
                    } else if (node.getParent() instanceof AbstractChannelTableNode) {
                        node = (AbstractChannelTableNode) node.getParent();
                        if (node.isGroupNode()) {
                            selectedGroupIds.add(node.getGroupStatus().getGroup().getId());
                        }
                    }
                }
            }
        }

        parent.setupChannel(channel, selectedGroupIds.size() == 1 ? selectedGroupIds.iterator().next() : null);
    }

    public void addChannelToGroup(String channelId, String groupId) {
        Set<ChannelGroup> channelGroups = new HashSet<ChannelGroup>();

        for (ChannelGroupStatus groupStatus : groupStatuses.values()) {
            ChannelGroup group = groupStatus.getGroup();

            if (!group.getId().equals(ChannelGroup.DEFAULT_ID)) {
                if (group.getId().equals(groupId)) {
                    group.getChannels().add(new Channel(channelId));
                }
                channelGroups.add(group);
            }
        }

        new UpdateSwingWorker(channelGroups, new HashSet<String>(), false).execute();
    }

    public void doImportGroup() {
        if ((isSaveEnabled() && !promptSave(true)) || parent.getChannelTagInfo(false).isEnabled()) {
            return;
        }

        String content = parent.browseForFileString("XML");

        if (content != null) {
            importGroup(content, true);
        }
    }

    public void importGroup(String content, boolean showAlerts) {
        if ((showAlerts && !parent.promptObjectMigration(content, "group")) || parent.getChannelTagInfo(false).isEnabled()) {
            return;
        }

        ChannelGroup importGroup = null;

        try {
            importGroup = ObjectXMLSerializer.getInstance().deserialize(content, ChannelGroup.class);
        } catch (Exception e) {
            if (showAlerts) {
                parent.alertThrowable(parent, e, "Invalid channel group file:\n" + e.getMessage());
            }
            return;
        }

        importGroup(importGroup, showAlerts);
    }

    public void importGroup(ChannelGroup importGroup, boolean showAlerts) {
        importGroup(importGroup, showAlerts, false);
    }

    public void importGroup(ChannelGroup importGroup, boolean showAlerts, boolean synchronous) {
        // First consolidate and import code template libraries
        Map<String, CodeTemplateLibrary> codeTemplateLibraryMap = new LinkedHashMap<String, CodeTemplateLibrary>();
        Set<String> codeTemplateIds = new HashSet<String>();

        for (Channel channel : importGroup.getChannels()) {
            if (channel.getCodeTemplateLibraries() != null) {
                for (CodeTemplateLibrary library : channel.getCodeTemplateLibraries()) {
                    CodeTemplateLibrary matchingLibrary = codeTemplateLibraryMap.get(library.getId());

                    if (matchingLibrary != null) {
                        for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                            if (codeTemplateIds.add(codeTemplate.getId())) {
                                matchingLibrary.getCodeTemplates().add(codeTemplate);
                            }
                        }
                    } else {
                        matchingLibrary = library;
                        codeTemplateLibraryMap.put(matchingLibrary.getId(), matchingLibrary);

                        List<CodeTemplate> codeTemplates = new ArrayList<CodeTemplate>();
                        for (CodeTemplate codeTemplate : matchingLibrary.getCodeTemplates()) {
                            if (codeTemplateIds.add(codeTemplate.getId())) {
                                codeTemplates.add(codeTemplate);
                            }
                        }
                        matchingLibrary.setCodeTemplates(codeTemplates);
                    }

                    // Combine the library enabled / disabled channel IDs
                    matchingLibrary.getEnabledChannelIds().addAll(library.getEnabledChannelIds());
                    matchingLibrary.getEnabledChannelIds().add(channel.getId());
                    matchingLibrary.getDisabledChannelIds().addAll(library.getDisabledChannelIds());
                    matchingLibrary.getDisabledChannelIds().removeAll(matchingLibrary.getEnabledChannelIds());
                }

                channel.getCodeTemplateLibraries().clear();
            }
        }

        List<CodeTemplateLibrary> codeTemplateLibraries = new ArrayList<CodeTemplateLibrary>(codeTemplateLibraryMap.values());

        parent.removeInvalidItems(codeTemplateLibraries, CodeTemplateLibrary.class);
        if (CollectionUtils.isNotEmpty(codeTemplateLibraries)) {
            boolean importLibraries;
            String importChannelCodeTemplateLibraries = Preferences.userNodeForPackage(Mirth.class).get("importChannelCodeTemplateLibraries", null);

            if (importChannelCodeTemplateLibraries == null) {
                JCheckBox alwaysChooseCheckBox = new JCheckBox("Always choose this option by default in the future (may be changed in the Administrator settings)");
                Object[] params = new Object[] {
                        "Group \"" + importGroup.getName() + "\" has code template libraries included with it. Would you like to import them?",
                        alwaysChooseCheckBox };
                int result = JOptionPane.showConfirmDialog(this, params, "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION) {
                    importLibraries = result == JOptionPane.YES_OPTION;
                    if (alwaysChooseCheckBox.isSelected()) {
                        Preferences.userNodeForPackage(Mirth.class).putBoolean("importChannelCodeTemplateLibraries", importLibraries);
                    }
                } else {
                    return;
                }
            } else {
                importLibraries = Boolean.parseBoolean(importChannelCodeTemplateLibraries);
            }

            if (importLibraries) {
                CodeTemplateImportDialog dialog = new CodeTemplateImportDialog(parent, codeTemplateLibraries, false, true);

                if (dialog.wasSaved()) {
                    CodeTemplateLibrarySaveResult updateSummary = parent.codeTemplatePanel.attemptUpdate(dialog.getUpdatedLibraries(), new HashMap<String, CodeTemplateLibrary>(), dialog.getUpdatedCodeTemplates(), new HashMap<String, CodeTemplate>(), true, null, null);

                    if (updateSummary == null || updateSummary.isOverrideNeeded() || !updateSummary.isLibrariesSuccess()) {
                        return;
                    } else {
                        for (CodeTemplateUpdateResult result : updateSummary.getCodeTemplateResults().values()) {
                            if (!result.isSuccess()) {
                                return;
                            }
                        }
                    }

                    parent.codeTemplatePanel.doRefreshCodeTemplates();
                }
            }
        }

        List<Channel> successfulChannels = new ArrayList<Channel>();
        for (Channel channel : importGroup.getChannels()) {
            Channel importChannel = importChannel(channel, false, false);
            if (importChannel != null) {
                successfulChannels.add(importChannel);
            }
        }

        if (!StringUtils.equals(importGroup.getId(), ChannelGroup.DEFAULT_ID)) {
            ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();
            AbstractChannelTableNode importGroupNode = null;

            String groupName = importGroup.getName();
            String tempId;
            try {
                tempId = parent.mirthClient.getGuid();
            } catch (ClientException e) {
                tempId = UUID.randomUUID().toString();
            }

            // Check to see that the channel name doesn't already exist.
            if (!checkGroupName(groupName)) {
                if (!parent.alertOption(parent, "Would you like to overwrite the existing group?  Choose 'No' to create a new group.")) {
                    importGroup.setRevision(0);

                    do {
                        groupName = JOptionPane.showInputDialog(this, "Please enter a new name for the group.", groupName);
                        if (groupName == null) {
                            return;
                        }
                    } while (!checkGroupName(groupName));

                    importGroup.setId(tempId);
                    importGroup.setName(groupName);
                } else {
                    MutableTreeTableNode root = (MutableTreeTableNode) model.getRoot();
                    for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
                        AbstractChannelTableNode groupNode = (AbstractChannelTableNode) groupNodes.nextElement();

                        if (StringUtils.equals(groupNode.getGroupStatus().getGroup().getName(), groupName)) {
                            importGroupNode = groupNode;
                        }
                    }
                }
            } else {
                // Start the revision number over for a new channel group
                importGroup.setRevision(0);

                // If the channel name didn't already exist, make sure
                // the id doesn't exist either.
                if (!checkGroupId(importGroup.getId())) {
                    importGroup.setId(tempId);
                }
            }

            Set<ChannelGroup> channelGroups = new HashSet<ChannelGroup>();
            Set<String> removedChannelGroupIds = new HashSet<String>(groupStatuses.keySet());
            removedChannelGroupIds.remove(ChannelGroup.DEFAULT_ID);

            MutableTreeTableNode root = (MutableTreeTableNode) channelTable.getTreeTableModel().getRoot();
            if (root == null) {
                return;
            }

            for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
                ChannelGroup group = ((AbstractChannelTableNode) groupNodes.nextElement()).getGroupStatus().getGroup();

                if (!StringUtils.equals(group.getId(), ChannelGroup.DEFAULT_ID)) {
                    // If the current group is the one we're overwriting, merge the channels
                    if (importGroupNode != null && StringUtils.equals(group.getId(), importGroupNode.getGroupStatus().getGroup().getId())) {
                        group = importGroup;
                        group.setRevision(importGroupNode.getGroupStatus().getGroup().getRevision());

                        Set<String> channelIds = new HashSet<String>();
                        for (Channel channel : group.getChannels()) {
                            channelIds.add(channel.getId());
                        }

                        // Add the imported channels
                        for (Channel channel : successfulChannels) {
                            channelIds.add(channel.getId());
                        }

                        List<Channel> channels = new ArrayList<Channel>();
                        for (String channelId : channelIds) {
                            channels.add(new Channel(channelId));
                        }
                        group.setChannels(channels);
                    }

                    channelGroups.add(group);
                    removedChannelGroupIds.remove(group.getId());
                }
            }

            if (importGroupNode == null) {
                List<Channel> channels = new ArrayList<Channel>();
                for (Channel channel : successfulChannels) {
                    channels.add(new Channel(channel.getId()));
                }
                importGroup.setChannels(channels);

                channelGroups.add(importGroup);
                removedChannelGroupIds.remove(importGroup.getId());
            }

            Set<String> channelIds = new HashSet<String>();
            for (Channel channel : importGroup.getChannels()) {
                channelIds.add(channel.getId());
            }

            for (ChannelGroup group : channelGroups) {
                if (group != importGroup) {
                    for (Iterator<Channel> channels = group.getChannels().iterator(); channels.hasNext();) {
                        if (!channelIds.add(channels.next().getId())) {
                            channels.remove();
                        }
                    }
                }
            }

            attemptUpdate(channelGroups, removedChannelGroupIds, false);
        }

        if (synchronous) {
            retrieveChannels();
            updateModel(getCurrentTableState());
            updateTasks();
            parent.setSaveEnabled(false);
        } else {
            doRefreshChannels();
        }
    }

    public void doImportChannel() {
        if ((isSaveEnabled() && !promptSave(true)) || parent.getChannelTagInfo(false).isEnabled()) {
            return;
        }

        String content = parent.browseForFileString("XML");

        if (content != null) {
            importChannel(content, true);
        }
    }

    public void importChannel(String content, boolean showAlerts) {
        if (showAlerts && !parent.promptObjectMigration(content, "channel")) {
            return;
        }

        Channel importChannel = null;

        try {
            importChannel = ObjectXMLSerializer.getInstance().deserialize(content, Channel.class);
        } catch (Exception e) {
            if (showAlerts) {
                parent.alertThrowable(parent, e, "Invalid channel file:\n" + e.getMessage());
            }

            return;
        }

        importChannel(importChannel, showAlerts);
    }

    public Channel importChannel(Channel importChannel, boolean showAlerts) {
        return importChannel(importChannel, showAlerts, true);
    }

    public Channel importChannel(Channel importChannel, boolean showAlerts, boolean refreshStatuses) {
        boolean overwrite = false;

        try {
            String channelName = importChannel.getName();
            String tempId = parent.mirthClient.getGuid();

            // Check to see that the channel name doesn't already exist.
            if (!parent.checkChannelName(channelName, tempId)) {
                if (!parent.alertOption(parent, "Would you like to overwrite the existing channel?  Choose 'No' to create a new channel.")) {
                    importChannel.setRevision(0);

                    do {
                        channelName = JOptionPane.showInputDialog(this, "Please enter a new name for the channel.", channelName);
                        if (channelName == null) {
                            return null;
                        }
                    } while (!parent.checkChannelName(channelName, tempId));

                    importChannel.setName(channelName);
                    setIdAndUpdateLibraries(importChannel, tempId);
                } else {
                    overwrite = true;

                    for (ChannelStatus channelStatus : channelStatuses.values()) {
                        Channel channel = channelStatus.getChannel();
                        if (channel.getName().equalsIgnoreCase(channelName)) {
                            // If overwriting, use the old revision number and id
                            importChannel.setRevision(channel.getRevision());
                            setIdAndUpdateLibraries(importChannel, channel.getId());
                        }
                    }
                }
            } else {
                // Start the revision number over for a new channel
                importChannel.setRevision(0);

                // If the channel name didn't already exist, make sure
                // the id doesn't exist either.
                if (!checkChannelId(importChannel.getId())) {
                    setIdAndUpdateLibraries(importChannel, tempId);
                }

            }

            channelStatuses.put(importChannel.getId(), new ChannelStatus(importChannel));
            parent.updateChannelTags(false);
        } catch (ClientException e) {
            parent.alertThrowable(parent, e);
        }

        // Import code templates / libraries if applicable
        parent.removeInvalidItems(importChannel.getCodeTemplateLibraries(), CodeTemplateLibrary.class);
        if (!(importChannel instanceof InvalidChannel) && !importChannel.getCodeTemplateLibraries().isEmpty()) {
            boolean importLibraries;
            String importChannelCodeTemplateLibraries = Preferences.userNodeForPackage(Mirth.class).get("importChannelCodeTemplateLibraries", null);

            if (importChannelCodeTemplateLibraries == null) {
                JCheckBox alwaysChooseCheckBox = new JCheckBox("Always choose this option by default in the future (may be changed in the Administrator settings)");
                Object[] params = new Object[] {
                        "Channel \"" + importChannel.getName() + "\" has code template libraries included with it. Would you like to import them?",
                        alwaysChooseCheckBox };
                int result = JOptionPane.showConfirmDialog(this, params, "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION) {
                    importLibraries = result == JOptionPane.YES_OPTION;
                    if (alwaysChooseCheckBox.isSelected()) {
                        Preferences.userNodeForPackage(Mirth.class).putBoolean("importChannelCodeTemplateLibraries", importLibraries);
                    }
                } else {
                    return null;
                }
            } else {
                importLibraries = Boolean.parseBoolean(importChannelCodeTemplateLibraries);
            }

            if (importLibraries) {
                CodeTemplateImportDialog dialog = new CodeTemplateImportDialog(parent, importChannel.getCodeTemplateLibraries(), false, true);

                if (dialog.wasSaved()) {
                    CodeTemplateLibrarySaveResult updateSummary = parent.codeTemplatePanel.attemptUpdate(dialog.getUpdatedLibraries(), new HashMap<String, CodeTemplateLibrary>(), dialog.getUpdatedCodeTemplates(), new HashMap<String, CodeTemplate>(), true, null, null);

                    if (updateSummary == null || updateSummary.isOverrideNeeded() || !updateSummary.isLibrariesSuccess()) {
                        return null;
                    } else {
                        for (CodeTemplateUpdateResult result : updateSummary.getCodeTemplateResults().values()) {
                            if (!result.isSuccess()) {
                                return null;
                            }
                        }
                    }

                    parent.codeTemplatePanel.doRefreshCodeTemplates();
                }
            }

            importChannel.getCodeTemplateLibraries().clear();
        }

        if (CollectionUtils.isNotEmpty(importChannel.getDependentIds()) || CollectionUtils.isNotEmpty(importChannel.getDependencyIds())) {
            Set<ChannelDependency> channelDependencies = new HashSet<ChannelDependency>(getCachedChannelDependencies());

            if (CollectionUtils.isNotEmpty(importChannel.getDependentIds())) {
                for (String dependentId : importChannel.getDependentIds()) {
                    if (StringUtils.isNotBlank(dependentId) && !StringUtils.equals(dependentId, importChannel.getId())) {
                        channelDependencies.add(new ChannelDependency(dependentId, importChannel.getId()));
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(importChannel.getDependencyIds())) {
                for (String dependencyId : importChannel.getDependencyIds()) {
                    if (StringUtils.isNotBlank(dependencyId) && !StringUtils.equals(dependencyId, importChannel.getId())) {
                        channelDependencies.add(new ChannelDependency(importChannel.getId(), dependencyId));
                    }
                }
            }

            if (!channelDependencies.equals(getCachedChannelDependencies())) {
                try {
                    parent.mirthClient.setChannelDependencies(channelDependencies);
                } catch (ClientException e) {
                    parent.alertThrowable(parent, e, "Unable to save channel dependencies.");
                }
            }

            importChannel.clearDependencies();
        }

        // Update resource names
        parent.updateResourceNames(importChannel);

        /*
         * Update the channel if we're overwriting an imported channel, if we're not showing alerts
         * (dragging/dropping multiple channels), or if we're working with an invalid channel.
         */
        if (overwrite || !showAlerts || importChannel instanceof InvalidChannel) {
            try {
                parent.updateChannel(importChannel, overwrite);

                if (importChannel instanceof InvalidChannel && showAlerts) {
                    InvalidChannel invalidChannel = (InvalidChannel) importChannel;
                    Throwable cause = invalidChannel.getCause();
                    parent.alertThrowable(parent, cause, "Channel \"" + importChannel.getName() + "\" is invalid. " + getMissingExtensions(invalidChannel) + " Original cause:\n" + cause.getMessage());
                }
            } catch (Exception e) {
                channelStatuses.remove(importChannel.getId());
                parent.updateChannelTags(false);
                parent.alertThrowable(parent, e);
                return null;
            } finally {
                if (refreshStatuses) {
                    doRefreshChannels();
                }
            }
        }

        if (showAlerts) {
            final Channel importChannelFinal = importChannel;
            final boolean overwriteFinal = overwrite;

            /*
             * MIRTH-2048 - This is a hack to fix the memory access error that only occurs on OS X.
             * The block of code that edits the channel needs to be invoked later so that the screen
             * does not change before the drag/drop action of a channel finishes.
             */
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        parent.editChannel(importChannelFinal);
                        parent.setSaveEnabled(!overwriteFinal);
                    } catch (Exception e) {
                        channelStatuses.remove(importChannelFinal.getId());
                        parent.updateChannelTags(false);
                        parent.alertError(parent, "Channel had an unknown problem. Channel import aborted.");
                        parent.channelEditPanel = new ChannelSetup();
                        parent.doShowChannel();
                    }
                }

            });
        }

        return importChannel;
    }

    private void setIdAndUpdateLibraries(Channel channel, String newChannelId) {
        for (CodeTemplateLibrary library : channel.getCodeTemplateLibraries()) {
            library.getEnabledChannelIds().remove(channel.getId());
            library.getEnabledChannelIds().add(newChannelId);
        }
        channel.setId(newChannelId);
    }

    public void doExportAllChannels() {
        if (isSaveEnabled() && !promptSave(true)) {
            return;
        }

        if (!channelStatuses.isEmpty()) {
            List<Channel> selectedChannels = new ArrayList<Channel>();
            for (ChannelStatus channelStatus : channelStatuses.values()) {
                selectedChannels.add(channelStatus.getChannel());
            }
            exportChannels(selectedChannels);
        }
    }

    public boolean doExportChannel() {
        if (isSaveEnabled() && !promptSave(true)) {
            return false;
        }

        if (isGroupSelected()) {
            JOptionPane.showMessageDialog(parent, "This operation can only be performed on channels.");
            return false;
        }

        if (parent.changesHaveBeenMade()) {
            if (parent.alertOption(this, "This channel has been modified. You must save the channel changes before you can export. Would you like to save them now?")) {
                if (!parent.channelEditPanel.saveChanges()) {
                    return false;
                }
            } else {
                return false;
            }

            parent.setSaveEnabled(false);
        }

        Channel channel;
        if (parent.currentContentPage == parent.channelEditPanel || parent.currentContentPage == parent.channelEditPanel.filterPane || parent.currentContentPage == parent.channelEditPanel.transformerPane) {
            channel = parent.channelEditPanel.currentChannel;
        } else {
            List<Channel> selectedChannels = getSelectedChannels();
            if (selectedChannels.size() > 1) {
                exportChannels(selectedChannels);
                return true;
            }
            channel = selectedChannels.get(0);
        }

        // Add code template libraries if necessary
        if (channelHasLinkedCodeTemplates(channel)) {
            boolean addLibraries = true;
            String exportChannelCodeTemplateLibraries = Preferences.userNodeForPackage(Mirth.class).get("exportChannelCodeTemplateLibraries", null);

            if (exportChannelCodeTemplateLibraries == null) {
                ExportChannelLibrariesDialog dialog = new ExportChannelLibrariesDialog(channel);
                if (dialog.getResult() == JOptionPane.NO_OPTION) {
                    addLibraries = false;
                } else if (dialog.getResult() != JOptionPane.YES_OPTION) {
                    return false;
                }
            } else {
                addLibraries = Boolean.parseBoolean(exportChannelCodeTemplateLibraries);
            }

            if (addLibraries) {
                addCodeTemplateLibrariesToChannel(channel);
            }
        }

        addDependenciesToChannel(channel);

        // Update resource names
        parent.updateResourceNames(channel);

        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        String channelXML = serializer.serialize(channel);
        // Reset the libraries on the cached channel
        channel.getCodeTemplateLibraries().clear();
        channel.clearDependencies();

        return parent.exportFile(channelXML, channel.getName(), "XML", "Channel");
    }

    private void exportChannels(List<Channel> channelList) {
        if (channelHasLinkedCodeTemplates(channelList)) {
            boolean addLibraries;
            String exportChannelCodeTemplateLibraries = Preferences.userNodeForPackage(Mirth.class).get("exportChannelCodeTemplateLibraries", null);

            if (exportChannelCodeTemplateLibraries == null) {
                JCheckBox alwaysChooseCheckBox = new JCheckBox("Always choose this option by default in the future (may be changed in the Administrator settings)");
                Object[] params = new Object[] {
                        "<html>One or more channels has code template libraries linked to them.<br/>Do you wish to include these libraries in each respective channel export?</html>",
                        alwaysChooseCheckBox };
                int result = JOptionPane.showConfirmDialog(this, params, "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION) {
                    addLibraries = result == JOptionPane.YES_OPTION;
                    if (alwaysChooseCheckBox.isSelected()) {
                        Preferences.userNodeForPackage(Mirth.class).putBoolean("exportChannelCodeTemplateLibraries", addLibraries);
                    }
                } else {
                    return;
                }
            } else {
                addLibraries = Boolean.parseBoolean(exportChannelCodeTemplateLibraries);
            }

            if (addLibraries) {
                for (Channel channel : channelList) {
                    addCodeTemplateLibrariesToChannel(channel);
                }
            }
        }

        for (Channel channel : channelList) {
            addDependenciesToChannel(channel);
        }

        JFileChooser exportFileChooser = new JFileChooser();
        exportFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        File currentDir = new File(Frame.userPreferences.get("currentDirectory", ""));
        if (currentDir.exists()) {
            exportFileChooser.setCurrentDirectory(currentDir);
        }

        int returnVal = exportFileChooser.showSaveDialog(this);
        File exportFile = null;
        File exportDirectory = null;
        String exportPath = "/";

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Frame.userPreferences.put("currentDirectory", exportFileChooser.getCurrentDirectory().getPath());

            int exportCollisionCount = 0;
            exportDirectory = exportFileChooser.getSelectedFile();
            exportPath = exportDirectory.getAbsolutePath();

            for (Channel channel : channelList) {
                exportFile = new File(exportPath + "/" + channel.getName() + ".xml");

                if (exportFile.exists()) {
                    exportCollisionCount++;
                }

                // Update resource names
                parent.updateResourceNames(channel);
            }

            try {
                int exportCount = 0;

                boolean overwriteAll = false;
                boolean skipAll = false;
                for (int i = 0, size = channelList.size(); i < size; i++) {
                    Channel channel = channelList.get(i);
                    exportFile = new File(exportPath + "/" + channel.getName() + ".xml");

                    boolean fileExists = exportFile.exists();
                    if (fileExists) {
                        if (!overwriteAll && !skipAll) {
                            if (exportCollisionCount == 1) {
                                if (!parent.alertOption(parent, "The file " + channel.getName() + ".xml already exists.  Would you like to overwrite it?")) {
                                    continue;
                                }
                            } else {
                                ConflictOption conflictStatus = parent.alertConflict(parent, "<html>The file " + channel.getName() + ".xml already exists.<br>Would you like to overwrite it?</html>", exportCollisionCount);

                                if (conflictStatus == ConflictOption.YES_APPLY_ALL) {
                                    overwriteAll = true;
                                } else if (conflictStatus == ConflictOption.NO) {
                                    exportCollisionCount--;
                                    continue;
                                } else if (conflictStatus == ConflictOption.NO_APPLY_ALL) {
                                    skipAll = true;
                                    continue;
                                }
                            }
                        }
                        exportCollisionCount--;
                    }

                    if (!fileExists || !skipAll) {
                        String channelXML = ObjectXMLSerializer.getInstance().serialize(channel);
                        FileUtils.writeStringToFile(exportFile, channelXML, UIConstants.CHARSET);
                        exportCount++;
                    }
                }

                if (exportCount > 0) {
                    parent.alertInformation(parent, exportCount + " files were written successfully to " + exportPath + ".");
                }
            } catch (IOException ex) {
                parent.alertError(parent, "File could not be written.");
            }
        }

        // Reset the libraries on the cached channels
        for (Channel channel : channelList) {
            channel.getCodeTemplateLibraries().clear();
            channel.clearDependencies();
        }
    }

    private boolean channelHasLinkedCodeTemplates(Channel channel) {
        return channelHasLinkedCodeTemplates(Collections.singletonList(channel));
    }

    private boolean channelHasLinkedCodeTemplates(List<Channel> channels) {
        for (Channel channel : channels) {
            for (CodeTemplateLibrary library : parent.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
                if (library.getEnabledChannelIds().contains(channel.getId()) || (library.isIncludeNewChannels() && !library.getDisabledChannelIds().contains(channel.getId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean groupHasLinkedCodeTemplates(List<ChannelGroup> groups) {
        for (ChannelGroup group : groups) {
            if (channelHasLinkedCodeTemplates(group.getChannels())) {
                return true;
            }
        }
        return false;
    }

    private void addCodeTemplateLibrariesToChannel(Channel channel) {
        List<CodeTemplateLibrary> channelLibraries = new ArrayList<CodeTemplateLibrary>();

        for (CodeTemplateLibrary library : parent.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
            if (library.getEnabledChannelIds().contains(channel.getId()) || (library.isIncludeNewChannels() && !library.getDisabledChannelIds().contains(channel.getId()))) {
                library = new CodeTemplateLibrary(library);

                List<CodeTemplate> codeTemplates = new ArrayList<CodeTemplate>();
                for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                    codeTemplate = parent.codeTemplatePanel.getCachedCodeTemplates().get(codeTemplate.getId());
                    if (codeTemplate != null) {
                        codeTemplates.add(codeTemplate);
                    }
                }

                library.setCodeTemplates(codeTemplates);
                channelLibraries.add(library);
            }
        }

        channel.setCodeTemplateLibraries(channelLibraries);
    }

    private void addDependenciesToChannel(Channel channel) {
        Set<String> dependentIds = new HashSet<String>();
        Set<String> dependencyIds = new HashSet<String>();
        for (ChannelDependency channelDependency : getCachedChannelDependencies()) {
            if (StringUtils.equals(channelDependency.getDependencyId(), channel.getId())) {
                dependentIds.add(channelDependency.getDependentId());
            } else if (StringUtils.equals(channelDependency.getDependentId(), channel.getId())) {
                dependencyIds.add(channelDependency.getDependencyId());
            }
        }

        if (CollectionUtils.isNotEmpty(dependentIds)) {
            channel.setDependentIds(dependentIds);
        }
        if (CollectionUtils.isNotEmpty(dependencyIds)) {
            channel.setDependencyIds(dependencyIds);
        }
    }

    public boolean doExportAllGroups() {
        if (isSaveEnabled() && !promptSave(true)) {
            return false;
        }

        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();
        if (!model.isGroupModeEnabled()) {
            return false;
        }

        MutableTreeTableNode root = (MutableTreeTableNode) model.getRoot();
        if (root == null) {
            return false;
        }

        List<ChannelGroup> groups = new ArrayList<ChannelGroup>();

        for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
            AbstractChannelTableNode groupNode = (AbstractChannelTableNode) groupNodes.nextElement();
            if (groupNode.isGroupNode()) {
                groups.add(new ChannelGroup(groupNode.getGroupStatus().getGroup()));
            }
        }

        return handleExportGroups(groups);
    }

    public boolean doExportGroup() {
        if (isSaveEnabled() && !promptSave(true)) {
            return false;
        }

        if (isChannelSelected()) {
            JOptionPane.showMessageDialog(parent, "This operation can only be performed on groups.");
            return false;
        }

        int[] rows = channelTable.getSelectedModelRows();
        if (rows.length > 0) {
            List<ChannelGroup> groups = new ArrayList<ChannelGroup>();

            // Populate list of groups with full channels
            for (int row : rows) {
                AbstractChannelTableNode groupNode = (AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent();
                if (groupNode.isGroupNode()) {
                    groups.add(new ChannelGroup(groupNode.getGroupStatus().getGroup()));
                }
            }

            return handleExportGroups(groups);
        }

        return false;
    }

    private boolean handleExportGroups(List<ChannelGroup> groups) {
        // Populate list of groups with full channels
        for (ChannelGroup group : groups) {
            List<Channel> channels = new ArrayList<Channel>();
            for (Channel channel : group.getChannels()) {
                ChannelStatus channelStatus = this.channelStatuses.get(channel.getId());
                if (channelStatus != null) {
                    channels.add(channelStatus.getChannel());
                }
            }
            group.setChannels(channels);
        }

        try {
            // Add code template libraries to channels if necessary
            if (groupHasLinkedCodeTemplates(groups)) {
                boolean addLibraries;
                String exportChannelCodeTemplateLibraries = Preferences.userNodeForPackage(Mirth.class).get("exportChannelCodeTemplateLibraries", null);

                if (exportChannelCodeTemplateLibraries == null) {
                    JCheckBox alwaysChooseCheckBox = new JCheckBox("Always choose this option by default in the future (may be changed in the Administrator settings)");
                    Object[] params = new Object[] {
                            "<html>One or more channels has code template libraries linked to them.<br/>Do you wish to include these libraries in each respective channel export?</html>",
                            alwaysChooseCheckBox };
                    int result = JOptionPane.showConfirmDialog(this, params, "Select an Option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                    if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION) {
                        addLibraries = result == JOptionPane.YES_OPTION;
                        if (alwaysChooseCheckBox.isSelected()) {
                            Preferences.userNodeForPackage(Mirth.class).putBoolean("exportChannelCodeTemplateLibraries", addLibraries);
                        }
                    } else {
                        return false;
                    }
                } else {
                    addLibraries = Boolean.parseBoolean(exportChannelCodeTemplateLibraries);
                }

                if (addLibraries) {
                    for (ChannelGroup group : groups) {
                        for (Channel channel : group.getChannels()) {
                            addCodeTemplateLibrariesToChannel(channel);
                        }
                    }
                }
            }

            // Update resource names
            for (ChannelGroup group : groups) {
                for (Channel channel : group.getChannels()) {
                    addDependenciesToChannel(channel);
                    parent.updateResourceNames(channel);
                }
            }

            if (groups.size() == 1) {
                return exportGroup(groups.iterator().next());
            } else {
                return exportGroups(groups);
            }
        } finally {
            // Reset the libraries on the cached channels
            for (ChannelGroup group : groups) {
                for (Channel channel : group.getChannels()) {
                    channel.getCodeTemplateLibraries().clear();
                    channel.clearDependencies();
                }
            }
        }
    }

    private boolean exportGroup(ChannelGroup group) {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        String groupXML = serializer.serialize(group);
        return parent.exportFile(groupXML, group.getName().replaceAll("[^a-zA-Z_0-9\\-\\s]", ""), "XML", "Channel group");
    }

    private boolean exportGroups(List<ChannelGroup> groups) {
        JFileChooser exportFileChooser = new JFileChooser();
        exportFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        File currentDir = new File(Frame.userPreferences.get("currentDirectory", ""));
        if (currentDir.exists()) {
            exportFileChooser.setCurrentDirectory(currentDir);
        }

        int returnVal = exportFileChooser.showSaveDialog(this);
        File exportFile = null;
        File exportDirectory = null;
        String exportPath = "/";

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Frame.userPreferences.put("currentDirectory", exportFileChooser.getCurrentDirectory().getPath());

            int exportCollisionCount = 0;
            exportDirectory = exportFileChooser.getSelectedFile();
            exportPath = exportDirectory.getAbsolutePath();

            for (ChannelGroup group : groups) {
                exportFile = new File(exportPath + "/" + group.getName().replaceAll("[^a-zA-Z_0-9\\-\\s]", "") + ".xml");

                if (exportFile.exists()) {
                    exportCollisionCount++;
                }
            }

            try {
                int exportCount = 0;

                boolean overwriteAll = false;
                boolean skipAll = false;
                for (int i = 0, size = groups.size(); i < size; i++) {
                    ChannelGroup group = groups.get(i);
                    String groupName = group.getName().replaceAll("[^a-zA-Z_0-9\\-\\s]", "");
                    exportFile = new File(exportPath + "/" + groupName + ".xml");

                    boolean fileExists = exportFile.exists();
                    if (fileExists) {
                        if (!overwriteAll && !skipAll) {
                            if (exportCollisionCount == 1) {
                                if (!parent.alertOption(parent, "The file " + groupName + ".xml already exists.  Would you like to overwrite it?")) {
                                    continue;
                                }
                            } else {
                                ConflictOption conflictStatus = parent.alertConflict(parent, "<html>The file " + groupName + ".xml already exists.<br>Would you like to overwrite it?</html>", exportCollisionCount);

                                if (conflictStatus == ConflictOption.YES_APPLY_ALL) {
                                    overwriteAll = true;
                                } else if (conflictStatus == ConflictOption.NO) {
                                    exportCollisionCount--;
                                    continue;
                                } else if (conflictStatus == ConflictOption.NO_APPLY_ALL) {
                                    skipAll = true;
                                    continue;
                                }
                            }
                        }
                        exportCollisionCount--;
                    }

                    if (!fileExists || !skipAll) {
                        String groupXML = ObjectXMLSerializer.getInstance().serialize(group);
                        FileUtils.writeStringToFile(exportFile, groupXML, UIConstants.CHARSET);
                        exportCount++;
                    }
                }

                if (exportCount > 0) {
                    parent.alertInformation(parent, exportCount + " files were written successfully to " + exportPath + ".");
                    return true;
                }
            } catch (IOException ex) {
                parent.alertError(parent, "File could not be written.");
            }
        }

        return false;
    }

    public void doDeleteGroup() {
        if (isChannelSelected()) {
            JOptionPane.showMessageDialog(parent, "This operation can only be performed on groups.");
            return;
        }

        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();

        int[] rows = channelTable.getSelectedModelRows();
        if (rows.length >= 0) {
            List<AbstractChannelTableNode> groupNodes = new ArrayList<AbstractChannelTableNode>();

            for (int row : rows) {
                AbstractChannelTableNode groupNode = (AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent();
                if (groupNode.isGroupNode()) {
                    if (!StringUtils.equals(groupNode.getGroupStatus().getGroup().getId(), ChannelGroup.DEFAULT_ID)) {
                        groupNodes.add(groupNode);
                    }
                }
            }

            for (AbstractChannelTableNode groupNode : groupNodes) {
                Set<String> channelIds = new HashSet<String>();
                for (Enumeration<? extends MutableTreeTableNode> channelNodes = groupNode.children(); channelNodes.hasMoreElements();) {
                    channelIds.add(((AbstractChannelTableNode) channelNodes.nextElement()).getChannelStatus().getChannel().getId());
                }
                for (String channelId : channelIds) {
                    model.removeChannelFromGroup(groupNode, channelId);
                }
                model.removeGroup(groupNode);
            }

            parent.setSaveEnabled(true);
        }
    }

    public void doDeleteChannel() {
        if (isSaveEnabled() && !promptSave(true)) {
            return;
        }

        if (isGroupSelected()) {
            JOptionPane.showMessageDialog(parent, "This operation can only be performed on channels.");
            return;
        }

        final List<Channel> selectedChannels = getSelectedChannels();
        if (selectedChannels.size() == 0) {
            return;
        }

        if (!parent.alertOption(parent, "Are you sure you want to delete the selected channel(s)?\nAny selected deployed channel(s) will first be undeployed.")) {
            return;
        }

        final String workingId = parent.startWorking("Deleting channel...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                Set<String> channelIds = new HashSet<String>(selectedChannels.size());
                for (Channel channel : selectedChannels) {
                    channelIds.add(channel.getId());
                }

                try {
                    parent.mirthClient.removeChannels(channelIds);
                } catch (ClientException e) {
                    parent.alertThrowable(parent, e);
                }

                return null;
            }

            public void done() {
                doRefreshChannels();
                parent.stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doCloneChannel() {
        if (isSaveEnabled() && !promptSave(true)) {
            return;
        }

        if (isGroupSelected()) {
            JOptionPane.showMessageDialog(parent, "This operation can only be performed on channels.");
            return;
        }

        List<Channel> selectedChannels = getSelectedChannels();
        if (selectedChannels.size() > 1) {
            JOptionPane.showMessageDialog(parent, "This operation can only be performed on a single channel.");
            return;
        }

        Channel channel = selectedChannels.get(0);

        if (channel instanceof InvalidChannel) {
            InvalidChannel invalidChannel = (InvalidChannel) channel;
            Throwable cause = invalidChannel.getCause();
            parent.alertThrowable(parent, cause, "Channel \"" + channel.getName() + "\" is invalid and cannot be cloned. " + getMissingExtensions(invalidChannel) + "Original cause:\n" + cause.getMessage());
            return;
        }

        try {
            channel = (Channel) SerializationUtils.clone(channel);
        } catch (SerializationException e) {
            parent.alertThrowable(parent, e);
            return;
        }

        try {
            channel.setRevision(0);
            channel.setId(parent.mirthClient.getGuid());
        } catch (ClientException e) {
            parent.alertThrowable(parent, e);
        }

        String channelName = channel.getName();
        do {
            channelName = JOptionPane.showInputDialog(this, "Please enter a new name for the channel.", channelName);
            if (channelName == null) {
                return;
            }
        } while (!parent.checkChannelName(channelName, channel.getId()));

        channel.setName(channelName);
        channelStatuses.put(channel.getId(), new ChannelStatus(channel));
        parent.updateChannelTags(false);

        parent.editChannel(channel);
        parent.setSaveEnabled(true);
    }

    public void doEditChannel() {
        if (isSaveEnabled() && !confirmLeave()) {
            return;
        }

        if (parent.isEditingChannel) {
            return;
        } else {
            parent.isEditingChannel = true;
        }

        if (isGroupSelected()) {
            JOptionPane.showMessageDialog(parent, "This operation can only be performed on channels.");
            return;
        }

        List<Channel> selectedChannels = getSelectedChannels();
        if (selectedChannels.size() > 1) {
            JOptionPane.showMessageDialog(parent, "This operation can only be performed on a single channel.");
        } else if (selectedChannels.size() == 0) {
            JOptionPane.showMessageDialog(parent, "Channel no longer exists.");
        } else {
            try {
                Channel channel = selectedChannels.get(0);

                if (channel instanceof InvalidChannel) {
                    InvalidChannel invalidChannel = (InvalidChannel) channel;
                    Throwable cause = invalidChannel.getCause();
                    parent.alertThrowable(parent, cause, "Channel \"" + channel.getName() + "\" is invalid and cannot be edited. " + getMissingExtensions(invalidChannel) + "Original cause:\n" + cause.getMessage());
                } else {
                    parent.editChannel((Channel) SerializationUtils.clone(channel));
                }
            } catch (SerializationException e) {
                parent.alertThrowable(parent, e);
            }
        }
        parent.isEditingChannel = false;
    }

    public void doEnableChannel() {
        if (isSaveEnabled() && !promptSave(true)) {
            return;
        }

        final List<Channel> selectedChannels = getSelectedChannels();
        if (selectedChannels.size() == 0) {
            parent.alertWarning(parent, "Channel no longer exists.");
            return;
        }

        final Set<String> channelIds = new HashSet<String>();
        Set<Channel> failedChannels = new HashSet<Channel>();
        String firstValidationMessage = null;

        for (Iterator<Channel> it = selectedChannels.iterator(); it.hasNext();) {
            Channel channel = it.next();
            String validationMessage = null;

            if (channel instanceof InvalidChannel) {
                failedChannels.add(channel);
                it.remove();
            } else if ((validationMessage = parent.channelEditPanel.checkAllForms(channel)) != null) {
                if (firstValidationMessage == null) {
                    firstValidationMessage = validationMessage;
                }

                failedChannels.add(channel);
                it.remove();
            } else {
                channelIds.add(channel.getId());
            }
        }

        if (!channelIds.isEmpty()) {
            final String workingId = parent.startWorking("Enabling channel...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                public Void doInBackground() {
                    for (Channel channel : selectedChannels) {
                        channel.setEnabled(true);
                    }

                    try {
                        parent.mirthClient.setChannelEnabled(channelIds, true);
                    } catch (ClientException e) {
                        parent.alertThrowable(parent, e);
                    }
                    return null;
                }

                public void done() {
                    doRefreshChannels();
                    parent.stopWorking(workingId);
                }
            };

            worker.execute();
        }

        if (!failedChannels.isEmpty()) {
            if (failedChannels.size() == 1) {
                Channel channel = failedChannels.iterator().next();

                if (channel instanceof InvalidChannel) {
                    InvalidChannel invalidChannel = (InvalidChannel) channel;
                    Throwable cause = invalidChannel.getCause();
                    parent.alertThrowable(parent, cause, "Channel \"" + invalidChannel.getName() + "\" is invalid and cannot be enabled. " + getMissingExtensions(invalidChannel) + "Original cause:\n" + cause.getMessage());
                } else {
                    parent.alertCustomError(parent, firstValidationMessage, CustomErrorDialog.ERROR_ENABLING_CHANNEL);
                }
            } else {
                String message = "The following channels are invalid or not configured properly:\n\n";
                for (Channel channel : failedChannels) {
                    message += "    " + channel.getName() + " (" + channel.getId() + ")\n";
                }
                parent.alertError(parent, message);
            }
        }
    }

    public void doDisableChannel() {
        if (isSaveEnabled() && !promptSave(true)) {
            return;
        }

        final List<Channel> selectedChannels = getSelectedChannels();
        if (selectedChannels.size() == 0) {
            parent.alertWarning(parent, "Channel no longer exists.");
            return;
        }

        final String workingId = parent.startWorking("Disabling channels...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                Set<String> channelIds = new HashSet<String>();

                for (Channel channel : selectedChannels) {
                    if (!(channel instanceof InvalidChannel)) {
                        channel.setEnabled(false);
                        channelIds.add(channel.getId());
                    }
                }

                try {
                    parent.mirthClient.setChannelEnabled(channelIds, false);
                } catch (ClientException e) {
                    parent.alertThrowable(parent, e);
                }
                return null;
            }

            public void done() {
                doRefreshChannels();
                parent.stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doViewMessages() {
        if (isSaveEnabled() && !confirmLeave()) {
            return;
        }

        parent.doShowMessages();
    }

    public static int getNumberOfDefaultColumns() {
        return DEFAULT_COLUMNS.length;
    }

    private void setChannelTaskVisible(int task) {
        setChannelTaskVisibility(task, true);
    }

    private void setChannelTaskVisibility(int task, boolean visible) {
        parent.setVisibleTasks(channelTasks, channelPopupMenu, task, task, visible);
    }

    private void setGroupTaskVisible(int task) {
        setGroupTaskVisibility(task, true);
    }

    private void setGroupTaskVisibility(int task, boolean visible) {
        parent.setVisibleTasks(groupTasks, groupPopupMenu, task, task, visible);
    }

    private void setAllTaskVisibility(boolean visible) {
        parent.setVisibleTasks(channelTasks, channelPopupMenu, 1, TASK_CHANNEL_VIEW_MESSAGES, visible);
        parent.setVisibleTasks(groupTasks, groupPopupMenu, 1, TASK_GROUP_DELETE_GROUP, visible);
    }

    public void updateChannelStatuses(List<ChannelSummary> changedChannels) {
        for (ChannelSummary channelSummary : changedChannels) {
            String channelId = channelSummary.getChannelId();

            if (channelSummary.isDeleted()) {
                channelStatuses.remove(channelId);
            } else {
                ChannelStatus channelStatus = channelStatuses.get(channelId);
                if (channelStatus == null) {
                    channelStatus = new ChannelStatus();
                    channelStatuses.put(channelId, channelStatus);
                }

                /*
                 * If the status coming back from the server is for an entirely new channel, the
                 * Channel object should never be null.
                 */
                if (channelSummary.getChannelStatus().getChannel() != null) {
                    channelStatus.setChannel(channelSummary.getChannelStatus().getChannel());
                }

                if (channelSummary.isUndeployed()) {
                    channelStatus.setDeployedDate(null);
                    channelStatus.setDeployedRevisionDelta(null);
                    channelStatus.setCodeTemplatesChanged(false);
                } else {
                    if (channelSummary.getChannelStatus().getDeployedDate() != null) {
                        channelStatus.setDeployedDate(channelSummary.getChannelStatus().getDeployedDate());
                        channelStatus.setDeployedRevisionDelta(channelSummary.getChannelStatus().getDeployedRevisionDelta());
                    }

                    channelStatus.setCodeTemplatesChanged(channelSummary.getChannelStatus().isCodeTemplatesChanged());
                }

                channelStatus.setLocalChannelId(channelSummary.getChannelStatus().getLocalChannelId());
            }
        }

        parent.updateChannelTags(false);
    }

    private void updateChannelGroups(List<ChannelGroup> channelGroups) {
        if (channelGroups != null) {
            this.groupStatuses.clear();

            ChannelGroup defaultGroup = ChannelGroup.getDefaultGroup();
            List<ChannelStatus> defaultGroupChannelStatuses = new ArrayList<ChannelStatus>();
            ChannelGroupStatus defaultGroupStatus = new ChannelGroupStatus(defaultGroup, defaultGroupChannelStatuses);
            this.groupStatuses.put(defaultGroup.getId(), defaultGroupStatus);

            Set<String> visitedChannelIds = new HashSet<String>();
            Set<String> remainingChannelIds = new HashSet<String>(this.channelStatuses.keySet());

            for (ChannelGroup group : channelGroups) {
                List<ChannelStatus> channelStatuses = new ArrayList<ChannelStatus>();

                for (Channel channel : group.getChannels()) {
                    if (!visitedChannelIds.contains(channel.getId())) {
                        ChannelStatus channelStatus = this.channelStatuses.get(channel.getId());
                        if (channelStatus != null) {
                            channelStatuses.add(channelStatus);
                        }
                        visitedChannelIds.add(channel.getId());
                        remainingChannelIds.remove(channel.getId());
                    }
                }

                this.groupStatuses.put(group.getId(), new ChannelGroupStatus(group, channelStatuses));
            }

            for (String channelId : remainingChannelIds) {
                defaultGroup.getChannels().add(new Channel(channelId));
                defaultGroupChannelStatuses.add(this.channelStatuses.get(channelId));
            }
        }
    }

    public void clearChannelCache() {
        channelStatuses = new HashMap<String, ChannelStatus>();
        groupStatuses = new HashMap<String, ChannelGroupStatus>();
        parent.updateChannelTags(false);
    }

    private String getMissingExtensions(InvalidChannel channel) {
        Set<String> missingConnectors = new HashSet<String>();
        Set<String> missingDataTypes = new HashSet<String>();

        try {
            DonkeyElement channelElement = new DonkeyElement(((InvalidChannel) channel).getChannelXml());

            checkConnectorForMissingExtensions(channelElement.getChildElement("sourceConnector"), true, missingConnectors, missingDataTypes);

            DonkeyElement destinationConnectors = channelElement.getChildElement("destinationConnectors");
            if (destinationConnectors != null) {
                for (DonkeyElement destinationConnector : destinationConnectors.getChildElements()) {
                    checkConnectorForMissingExtensions(destinationConnector, false, missingConnectors, missingDataTypes);
                }
            }
        } catch (DonkeyElementException e) {
        }

        StringBuilder builder = new StringBuilder();

        if (!missingConnectors.isEmpty()) {
            builder.append("\n\nYour Mirth Connect installation is missing required connectors for this channel:\n     ");
            builder.append(StringUtils.join(missingConnectors.toArray(), "\n     "));
            builder.append("\n\n");
        }

        if (!missingDataTypes.isEmpty()) {
            if (missingConnectors.isEmpty()) {
                builder.append("\n\n");
            }
            builder.append("Your Mirth Connect installation is missing required data types for this channel:\n     ");
            builder.append(StringUtils.join(missingDataTypes.toArray(), "\n     "));
            builder.append("\n\n");
        }

        return builder.toString();
    }

    private void checkConnectorForMissingExtensions(DonkeyElement connector, boolean source, Set<String> missingConnectors, Set<String> missingDataTypes) {
        if (connector != null) {
            DonkeyElement transportName = connector.getChildElement("transportName");
            // Check for 2.x-specific connectors
            transportName.setTextContent(ImportConverter3_0_0.convertTransportName(transportName.getTextContent()));

            if (transportName != null) {
                if (source && !LoadedExtensions.getInstance().getSourceConnectors().containsKey(transportName.getTextContent())) {
                    missingConnectors.add(transportName.getTextContent());
                } else if (!source && !LoadedExtensions.getInstance().getDestinationConnectors().containsKey(transportName.getTextContent())) {
                    missingConnectors.add(transportName.getTextContent());
                }
            }

            checkTransformerForMissingExtensions(connector.getChildElement("transformer"), missingDataTypes);
            if (!source) {
                checkTransformerForMissingExtensions(connector.getChildElement("responseTransformer"), missingDataTypes);
            }
        }
    }

    private void checkTransformerForMissingExtensions(DonkeyElement transformer, Set<String> missingDataTypes) {
        if (transformer != null) {
            // Check for 2.x-specific data types
            missingDataTypes.addAll(ImportConverter3_0_0.getMissingDataTypes(transformer, LoadedExtensions.getInstance().getDataTypePlugins().keySet()));

            DonkeyElement inboundDataType = transformer.getChildElement("inboundDataType");

            if (inboundDataType != null && !LoadedExtensions.getInstance().getDataTypePlugins().containsKey(inboundDataType.getTextContent())) {
                missingDataTypes.add(inboundDataType.getTextContent());
            }

            DonkeyElement outboundDataType = transformer.getChildElement("outboundDataType");

            if (outboundDataType != null && !LoadedExtensions.getInstance().getDataTypePlugins().containsKey(outboundDataType.getTextContent())) {
                missingDataTypes.add(outboundDataType.getTextContent());
            }
        }
    }

    public void initPanelPlugins() {
        loadPanelPlugins();
        switchBottomPane();

        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                loadPanelPlugin(sourceTabbedPane.getTitleAt(index));
            }
        };
        tabPane.addChangeListener(changeListener);
    }

    private void switchBottomPane() {
        if (LoadedExtensions.getInstance().getChannelPanelPlugins().size() > 0) {
            splitPane.setBottomComponent(tabPane);
            splitPane.setDividerSize(6);
            splitPane.setDividerLocation(3 * Preferences.userNodeForPackage(Mirth.class).getInt("height", UIConstants.MIRTH_HEIGHT) / 5);
            splitPane.setResizeWeight(0.5);
        } else {
            splitPane.setBottomComponent(null);
            splitPane.setDividerSize(0);
        }
    }

    private void loadPanelPlugins() {
        if (LoadedExtensions.getInstance().getChannelPanelPlugins().size() > 0) {
            for (ChannelPanelPlugin plugin : LoadedExtensions.getInstance().getChannelPanelPlugins().values()) {
                if (plugin.getComponent() != null) {
                    tabPane.addTab(plugin.getPluginPointName(), plugin.getComponent());
                }
            }
        }
    }

    private void loadPanelPlugin(String pluginName) {
        final ChannelPanelPlugin plugin = LoadedExtensions.getInstance().getChannelPanelPlugins().get(pluginName);

        if (plugin != null) {
            final List<Channel> selectedChannels = getSelectedChannels();

            QueuingSwingWorkerTask<Void, Void> task = new QueuingSwingWorkerTask<Void, Void>(pluginName, "Updating " + pluginName + " channel panel plugin...") {
                @Override
                public Void doInBackground() {
                    try {
                        if (selectedChannels.size() > 0) {
                            plugin.prepareData(selectedChannels);
                        } else {
                            plugin.prepareData();
                        }
                    } catch (ClientException e) {
                        parent.alertThrowable(parent, e);
                    }
                    return null;
                }

                @Override
                public void done() {
                    if (selectedChannels.size() > 0) {
                        plugin.update(selectedChannels);
                    } else {
                        plugin.update();
                    }
                }
            };

            new QueuingSwingWorker<Void, Void>(task, true).executeDelegate();
        }
    }

    private synchronized void updateCurrentPluginPanel() {
        if (LoadedExtensions.getInstance().getChannelPanelPlugins().size() > 0) {
            loadPanelPlugin(tabPane.getTitleAt(tabPane.getSelectedIndex()));
        }
    }

    private synchronized void updateModel(TableState tableState) {
        ChannelTagInfo channelTagInfo = parent.getChannelTagInfo(false);
        List<ChannelStatus> filteredChannelStatuses = new ArrayList<ChannelStatus>();
        int enabled = 0;

        for (ChannelStatus channelStatus : channelStatuses.values()) {
            Channel channel = channelStatus.getChannel();
            if (!channelTagInfo.isEnabled() || CollectionUtils.containsAny(channelTagInfo.getVisibleTags(), channel.getProperties().getTags())) {
                filteredChannelStatuses.add(channelStatus);

                if (channel.isEnabled()) {
                    enabled++;
                }
            }
        }

        int totalChannelCount = channelStatuses.size();
        int visibleChannelCount = filteredChannelStatuses.size();

        List<Channel> filteredChannels = new ArrayList<Channel>();
        for (ChannelStatus filteredChannelStatus : filteredChannelStatuses) {
            filteredChannels.add(filteredChannelStatus.getChannel());
        }

        List<ChannelGroupStatus> filteredGroupStatuses = new ArrayList<ChannelGroupStatus>();
        for (ChannelGroupStatus groupStatus : groupStatuses.values()) {
            filteredGroupStatuses.add(new ChannelGroupStatus(groupStatus));
        }

        int totalGroupCount = filteredGroupStatuses.size();
        int visibleGroupCount = totalGroupCount;

        for (Iterator<ChannelGroupStatus> groupStatusIterator = filteredGroupStatuses.iterator(); groupStatusIterator.hasNext();) {
            ChannelGroupStatus groupStatus = groupStatusIterator.next();

            for (Iterator<ChannelStatus> channelStatusIterator = groupStatus.getChannelStatuses().iterator(); channelStatusIterator.hasNext();) {
                ChannelStatus channelStatus = channelStatusIterator.next();

                boolean found = false;
                for (ChannelStatus filteredChannelStatus : filteredChannelStatuses) {
                    if (filteredChannelStatus.getChannel().getId().equals(channelStatus.getChannel().getId())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    channelStatusIterator.remove();
                }
            }

            if (totalChannelCount != visibleChannelCount && groupStatus.getChannelStatuses().isEmpty()) {
                groupStatusIterator.remove();
                visibleGroupCount--;
            }
        }

        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();

        StringBuilder builder = new StringBuilder();

        if (model.isGroupModeEnabled()) {
            if (totalGroupCount == visibleGroupCount) {
                builder.append(totalGroupCount);
            } else {
                builder.append(visibleGroupCount).append(" of ").append(totalGroupCount);
            }

            builder.append(" Group");
            if (totalGroupCount != 1) {
                builder.append('s');
            }

            if (totalGroupCount != visibleGroupCount) {
                builder.append(" (").append(totalGroupCount - visibleGroupCount).append(" filtered)");
            }
            builder.append(", ");
        }

        if (totalChannelCount == visibleChannelCount) {
            builder.append(totalChannelCount);
        } else {
            builder.append(visibleChannelCount).append(" of ").append(totalChannelCount);
        }

        builder.append(" Channel");
        if (totalChannelCount != 1) {
            builder.append('s');
        }

        if (totalChannelCount != visibleChannelCount) {
            builder.append(" (").append(totalChannelCount - visibleChannelCount).append(" filtered)");
        }
        builder.append(", ").append(enabled).append(" Enabled");

        if (channelTagInfo.isEnabled()) {
            builder.append(" (");
            for (Iterator<String> it = channelTagInfo.getVisibleTags().iterator(); it.hasNext();) {
                builder.append(it.next());
                if (it.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(')');
        }

        tagsLabel.setText(builder.toString());

        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            plugin.tableUpdate(filteredChannels);
        }

        model.update(filteredGroupStatuses);

        restoreTableState(tableState);
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been pushed. Deselects the
     * rows if no row was selected.
     */
    private void checkSelectionAndPopupMenu(MouseEvent evt) {
        int row = channelTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (row == -1) {
            deselectRows();
        }

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                if (!channelTable.isRowSelected(row)) {
                    channelTable.setRowSelectionInterval(row, row);
                }

                if (((AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent()).isGroupNode()) {
                    groupPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                } else {
                    channelPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            } else {
                channelPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }

    /** The action called when a Channel is selected. Sets tasks as well. */
    private void channelListSelected(ListSelectionEvent evt) {
        updateTasks();

        int[] rows = channelTable.getSelectedModelRows();

        if (rows.length > 0) {
            for (TaskPlugin plugin : LoadedExtensions.getInstance().getTaskPlugins().values()) {
                plugin.onRowSelected(channelTable);
            }

            updateCurrentPluginPanel();
        }
    }

    public boolean isGroupSelected() {
        for (int row : channelTable.getSelectedModelRows()) {
            AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent();
            if (node.isGroupNode()) {
                return true;
            }
        }
        return false;
    }

    public boolean isChannelSelected() {
        for (int row : channelTable.getSelectedModelRows()) {
            AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent();
            if (!node.isGroupNode()) {
                return true;
            }
        }
        return false;
    }

    public List<Channel> getSelectedChannels() {
        List<Channel> selectedChannels = new ArrayList<Channel>();

        for (int row : channelTable.getSelectedModelRows()) {
            AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent();

            if (node.isGroupNode()) {
                for (Enumeration<? extends MutableTreeTableNode> channelNodes = node.children(); channelNodes.hasMoreElements();) {
                    selectedChannels.add(((AbstractChannelTableNode) channelNodes.nextElement()).getChannelStatus().getChannel());
                }
            } else {
                selectedChannels.add(node.getChannelStatus().getChannel());
            }
        }

        return selectedChannels;
    }

    private void deselectRows() {
        channelTable.clearSelection();
        updateTasks();

        for (TaskPlugin plugin : LoadedExtensions.getInstance().getTaskPlugins().values()) {
            plugin.onRowDeselected();
        }

        updateCurrentPluginPanel();
    }

    /**
     * Checks to see if the passed in channel id already exists
     */
    private boolean checkChannelId(String id) {
        for (ChannelStatus channelStatus : channelStatuses.values()) {
            if (channelStatus.getChannel().getId().equalsIgnoreCase(id)) {
                return false;
            }
        }
        return true;
    }

    private static class DefaultChannelTableNodeFactory implements ChannelTableNodeFactory {
        @Override
        public AbstractChannelTableNode createNode(ChannelGroupStatus groupStatus) {
            return new ChannelTableNode(groupStatus);
        }

        @Override
        public AbstractChannelTableNode createNode(ChannelStatus channelStatus) {
            return new ChannelTableNode(channelStatus);
        }
    }

    private void initComponents() {
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        splitPane.setOneTouchExpandable(true);

        topPanel = new JPanel();

        List<String> columns = new ArrayList<String>();

        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                columns.add(plugin.getColumnHeader());
            }
        }

        columns.addAll(Arrays.asList(DEFAULT_COLUMNS));

        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            if (!plugin.isDisplayFirst()) {
                columns.add(plugin.getColumnHeader());
            }
        }

        channelTable = new MirthTreeTable("channelPanel", new LinkedHashSet<String>(columns));

        channelTable.setColumnFactory(new ChannelTableColumnFactory());

        ChannelTreeTableModel model = new ChannelTreeTableModel();
        model.setColumnIdentifiers(columns);
        model.setNodeFactory(new DefaultChannelTableNodeFactory());
        channelTable.setTreeTableModel(model);

        channelTable.setDoubleBuffered(true);
        channelTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        channelTable.getTreeSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        channelTable.setHorizontalScrollEnabled(true);
        channelTable.packTable(UIConstants.COL_MARGIN);
        channelTable.setRowHeight(UIConstants.ROW_HEIGHT);
        channelTable.setOpaque(true);
        channelTable.setRowSelectionAllowed(true);
        channelTable.setSortable(true);
        channelTable.putClientProperty("JTree.lineStyle", "Horizontal");
        channelTable.setAutoCreateColumnsFromModel(false);
        channelTable.setShowGrid(true, true);
        channelTable.restoreColumnPreferences();
        channelTable.setMirthColumnControlEnabled(true);

        channelTable.setDragEnabled(true);
        channelTable.setDropMode(DropMode.ON);
        channelTable.setTransferHandler(new ChannelTableTransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                // Don't allow files to be imported when the save task is enabled 
                if (parent.getChannelTagInfo(false).isEnabled() || (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && isSaveEnabled())) {
                    return false;
                }
                return super.canImport(support);
            }

            @Override
            public void importFile(final File file, final boolean showAlerts) {
                if (parent.getChannelTagInfo(false).isEnabled()) {
                    return;
                }

                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            String fileString = StringUtils.trim(parent.readFileToString(file));

                            try {
                                // If the table is in channel view, don't allow groups to be imported
                                ChannelGroup group = ObjectXMLSerializer.getInstance().deserialize(fileString, ChannelGroup.class);
                                if (group != null && !((ChannelTreeTableModel) channelTable.getTreeTableModel()).isGroupModeEnabled()) {
                                    return;
                                }
                            } catch (Exception e) {
                            }

                            if (showAlerts && !parent.promptObjectMigration(fileString, "channel or group")) {
                                return;
                            }

                            try {
                                importChannel(ObjectXMLSerializer.getInstance().deserialize(fileString, Channel.class), showAlerts);
                            } catch (Exception e) {
                                try {
                                    importGroup(ObjectXMLSerializer.getInstance().deserialize(fileString, ChannelGroup.class), showAlerts, !showAlerts);
                                } catch (Exception e2) {
                                    if (showAlerts) {
                                        parent.alertThrowable(parent, e, "Invalid channel or group file:\n" + e.getMessage());
                                    }
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean canMoveChannels(List<Channel> channels, int row) {
                if (row >= 0) {
                    TreePath path = channelTable.getPathForRow(row);
                    if (path != null) {
                        AbstractChannelTableNode node = (AbstractChannelTableNode) path.getLastPathComponent();

                        if (node.isGroupNode()) {
                            Set<String> currentChannelIds = new HashSet<String>();
                            for (Enumeration<? extends MutableTreeTableNode> channelNodes = node.children(); channelNodes.hasMoreElements();) {
                                currentChannelIds.add(((AbstractChannelTableNode) channelNodes.nextElement()).getChannelStatus().getChannel().getId());
                            }

                            for (Iterator<Channel> it = channels.iterator(); it.hasNext();) {
                                if (currentChannelIds.contains(it.next().getId())) {
                                    it.remove();
                                }
                            }

                            return !channels.isEmpty();
                        }
                    }
                }

                return false;
            }

            @Override
            public boolean moveChannels(List<Channel> channels, int row) {
                if (row >= 0) {
                    TreePath path = channelTable.getPathForRow(row);
                    if (path != null) {
                        AbstractChannelTableNode node = (AbstractChannelTableNode) path.getLastPathComponent();

                        if (node.isGroupNode()) {
                            Set<String> currentChannelIds = new HashSet<String>();
                            for (Enumeration<? extends MutableTreeTableNode> channelNodes = node.children(); channelNodes.hasMoreElements();) {
                                currentChannelIds.add(((AbstractChannelTableNode) channelNodes.nextElement()).getChannelStatus().getChannel().getId());
                            }

                            for (Iterator<Channel> it = channels.iterator(); it.hasNext();) {
                                if (currentChannelIds.contains(it.next().getId())) {
                                    it.remove();
                                }
                            }

                            if (!channels.isEmpty()) {
                                ListSelectionListener[] listeners = ((DefaultListSelectionModel) channelTable.getSelectionModel()).getListSelectionListeners();
                                for (ListSelectionListener listener : listeners) {
                                    channelTable.getSelectionModel().removeListSelectionListener(listener);
                                }

                                try {
                                    ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();
                                    Set<String> channelIds = new HashSet<String>();
                                    for (Channel channel : channels) {
                                        model.addChannelToGroup(node, channel.getId());
                                        channelIds.add(channel.getId());
                                    }

                                    List<TreePath> selectionPaths = new ArrayList<TreePath>();
                                    for (Enumeration<? extends MutableTreeTableNode> channelNodes = node.children(); channelNodes.hasMoreElements();) {
                                        AbstractChannelTableNode channelNode = (AbstractChannelTableNode) channelNodes.nextElement();
                                        if (channelIds.contains(channelNode.getChannelStatus().getChannel().getId())) {
                                            selectionPaths.add(new TreePath(new Object[] {
                                                    model.getRoot(), node, channelNode }));
                                        }
                                    }

                                    parent.setSaveEnabled(true);
                                    channelTable.expandPath(new TreePath(new Object[] {
                                            channelTable.getTreeTableModel().getRoot(), node }));
                                    channelTable.getTreeSelectionModel().setSelectionPaths(selectionPaths.toArray(new TreePath[selectionPaths.size()]));
                                    return true;
                                } finally {
                                    for (ListSelectionListener listener : listeners) {
                                        channelTable.getSelectionModel().addListSelectionListener(listener);
                                    }
                                }
                            }
                        }
                    }
                }

                return false;
            }
        });

        channelTable.setTreeCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                TreePath path = channelTable.getPathForRow(row);
                if (path != null && ((AbstractChannelTableNode) path.getLastPathComponent()).isGroupNode()) {
                    setIcon(UIConstants.ICON_GROUP);
                }

                return label;
            }
        });
        channelTable.setLeafIcon(UIConstants.ICON_CHANNEL);
        channelTable.setOpenIcon(UIConstants.ICON_GROUP);
        channelTable.setClosedIcon(UIConstants.ICON_GROUP);

        channelTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                channelListSelected(evt);
            }
        });

        // listen for trigger button and double click to edit channel.
        channelTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = channelTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                if (row == -1) {
                    return;
                }

                if (evt.getClickCount() >= 2 && channelTable.getSelectedRowCount() == 1 && channelTable.getSelectedRow() == row) {
                    AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent();
                    if (node.isGroupNode()) {
                        doEditGroupDetails();
                    } else {
                        doEditChannel();
                    }
                }
            }
        });

        // Key Listener trigger for DEL
        channelTable.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (channelTable.getSelectedModelRows().length == 0) {
                        return;
                    }

                    boolean allGroups = true;
                    boolean allChannels = true;
                    for (int row : channelTable.getSelectedModelRows()) {
                        AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent();
                        if (node.isGroupNode()) {
                            allChannels = false;
                        } else {
                            allGroups = false;
                        }
                    }

                    if (allChannels) {
                        doDeleteChannel();
                    } else if (allGroups) {
                        doDeleteGroup();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent evt) {}

            @Override
            public void keyTyped(KeyEvent evt) {}
        });

        // MIRTH-2301
        // Since we are using addHighlighter here instead of using setHighlighters, we need to remove the old ones first.
        channelTable.setHighlighters();

        // Set highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            channelTable.addHighlighter(highlighter);
        }

        HighlightPredicate revisionDeltaHighlighterPredicate = new HighlightPredicate() {
            @Override
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == channelTable.convertColumnIndexToView(channelTable.getColumnExt(DEPLOYED_REVISION_DELTA_COLUMN_NAME).getModelIndex())) {
                    if (channelTable.getValueAt(adapter.row, adapter.column) != null && ((Integer) channelTable.getValueAt(adapter.row, adapter.column)).intValue() > 0) {
                        return true;
                    }

                    if (channelStatuses != null) {
                        String channelId = (String) channelTable.getModel().getValueAt(channelTable.convertRowIndexToModel(adapter.row), ID_COLUMN_NUMBER);
                        ChannelStatus status = channelStatuses.get(channelId);
                        if (status != null && status.isCodeTemplatesChanged()) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
        channelTable.addHighlighter(new ColorHighlighter(revisionDeltaHighlighterPredicate, new Color(255, 204, 0), Color.BLACK, new Color(255, 204, 0), Color.BLACK));

        HighlightPredicate lastDeployedHighlighterPredicate = new HighlightPredicate() {
            @Override
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == channelTable.convertColumnIndexToView(channelTable.getColumnExt(LAST_DEPLOYED_COLUMN_NAME).getModelIndex())) {
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

        channelScrollPane = new JScrollPane(channelTable);
        channelScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        filterPanel = new JPanel();
        filterPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(164, 164, 164)));

        tagsFilterButton = new IconButton();
        tagsFilterButton.setIcon(new ImageIcon(getClass().getResource("/com/mirth/connect/client/ui/images/wrench.png")));
        tagsFilterButton.setToolTipText("Show Channel Filter");
        tagsFilterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                tagsFilterButtonActionPerformed();
            }
        });

        tagsLabel = new JLabel();

        ButtonGroup tableModeButtonGroup = new ButtonGroup();

        tableModeGroupsButton = new IconToggleButton(UIConstants.ICON_GROUP);
        tableModeGroupsButton.setToolTipText("Groups");
        tableModeGroupsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (!switchTableMode(true)) {
                    tableModeChannelsButton.setSelected(true);
                }
            }
        });
        tableModeButtonGroup.add(tableModeGroupsButton);

        tableModeChannelsButton = new IconToggleButton(UIConstants.ICON_CHANNEL);
        tableModeChannelsButton.setToolTipText("Channels");
        tableModeChannelsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (!switchTableMode(false)) {
                    tableModeGroupsButton.setSelected(true);
                }
            }
        });
        tableModeButtonGroup.add(tableModeChannelsButton);

        tabPane = new JTabbedPane();

        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(tabPane);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 0"));

        topPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 0"));
        topPanel.add(channelScrollPane, "grow, push");

        filterPanel.setLayout(new MigLayout("insets 0 12 0 12, novisualpadding, hidemode 3, fill, gap 12"));
        filterPanel.add(tagsFilterButton);
        filterPanel.add(tagsLabel, "left, growx, push");
        filterPanel.add(tableModeGroupsButton, "right, split 2, gapafter 0");
        filterPanel.add(tableModeChannelsButton);
        topPanel.add(filterPanel, "newline, growx");

        add(splitPane, "grow, push");
    }

    private void tagsFilterButtonActionPerformed() {
        if (isSaveEnabled() && !promptSave(true)) {
            return;
        }

        new ChannelFilter(parent.getChannelTagInfo(false), new ChannelFilterSaveTask() {
            @Override
            public void save(ChannelTagInfo channelTagInfo) {
                parent.setFilteredChannelTags(false, channelTagInfo.getVisibleTags(), channelTagInfo.isEnabled());
                doRefreshChannels(true);
            }
        });
    }

    private boolean switchTableMode(boolean groupModeEnabled) {
        return switchTableMode(groupModeEnabled, true);
    }

    private boolean switchTableMode(boolean groupModeEnabled, boolean promptSave) {
        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();
        if (model.isGroupModeEnabled() != groupModeEnabled) {
            if (promptSave && isSaveEnabled() && !promptSave(true)) {
                return false;
            }

            Preferences.userNodeForPackage(Mirth.class).putBoolean("channelGroupViewEnabled", groupModeEnabled);

            List<JXTaskPane> taskPanes = new ArrayList<JXTaskPane>();
            taskPanes.add(channelTasks);

            if (groupModeEnabled) {
                tableModeChannelsButton.setContentFilled(false);
                taskPanes.add(groupTasks);
            } else {
                tableModeGroupsButton.setContentFilled(false);
            }

            for (TaskPlugin plugin : LoadedExtensions.getInstance().getTaskPlugins().values()) {
                JXTaskPane taskPane = plugin.getTaskPane();
                if (taskPane != null) {
                    taskPanes.add(taskPane);
                }
            }
            parent.setFocus(taskPanes.toArray(new JXTaskPane[taskPanes.size()]), true, true);

            TableState tableState = getCurrentTableState();
            model.setGroupModeEnabled(groupModeEnabled);
            updateModel(tableState);
            updateTasks();
        }

        return true;
    }

    private TableState getCurrentTableState() {
        List<String> selectedIds = new ArrayList<String>();
        List<String> expandedGroupIds = null;

        int[] selectedRows = channelTable.getSelectedModelRows();
        for (int i = 0; i < selectedRows.length; i++) {
            AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(selectedRows[i]).getLastPathComponent();
            if (node.isGroupNode()) {
                selectedIds.add(node.getGroupStatus().getGroup().getId());
            } else {
                selectedIds.add(node.getChannelStatus().getChannel().getId());
            }
        }

        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();
        if (model.isGroupModeEnabled()) {
            MutableTreeTableNode root = (MutableTreeTableNode) model.getRoot();
            if (root != null && root.getChildCount() > 0) {
                expandedGroupIds = new ArrayList<String>();

                for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
                    AbstractChannelTableNode groupNode = (AbstractChannelTableNode) groupNodes.nextElement();
                    if (channelTable.isExpanded(new TreePath(new Object[] { root, groupNode })) || groupNode.getChildCount() == 0) {
                        expandedGroupIds.add(groupNode.getGroupStatus().getGroup().getId());
                    }
                }
            }
        }

        return new TableState(selectedIds, expandedGroupIds);
    }

    private void restoreTableState(TableState tableState) {
        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();
        MutableTreeTableNode root = (MutableTreeTableNode) model.getRoot();

        if (model.isGroupModeEnabled()) {
            if (tableState.getExpandedGroupIds() != null && root != null) {
                channelTable.collapseAll();

                for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
                    AbstractChannelTableNode groupNode = (AbstractChannelTableNode) groupNodes.nextElement();
                    if (tableState.getExpandedGroupIds().contains(groupNode.getGroupStatus().getGroup().getId())) {
                        channelTable.expandPath(new TreePath(new Object[] { root, groupNode }));
                    }
                }
            } else {
                channelTable.expandAll();
            }
        }

        final List<TreePath> selectionPaths = new ArrayList<TreePath>();

        for (Enumeration<? extends MutableTreeTableNode> children = root.children(); children.hasMoreElements();) {
            AbstractChannelTableNode child = (AbstractChannelTableNode) children.nextElement();
            if (child.isGroupNode() && tableState.getSelectedIds().contains(child.getGroupStatus().getGroup().getId()) || !child.isGroupNode() && tableState.getSelectedIds().contains(child.getChannelStatus().getChannel().getId())) {
                TreePath path = new TreePath(new Object[] { root, child });
                channelTable.getTreeSelectionModel().addSelectionPath(path);
                selectionPaths.add(path);
            }

            if (model.isGroupModeEnabled()) {
                for (Enumeration<? extends MutableTreeTableNode> channelNodes = child.children(); channelNodes.hasMoreElements();) {
                    AbstractChannelTableNode channelNode = (AbstractChannelTableNode) channelNodes.nextElement();
                    if (tableState.getSelectedIds().contains(channelNode.getChannelStatus().getChannel().getId())) {
                        TreePath path = new TreePath(new Object[] { root, child, channelNode });
                        channelTable.getTreeSelectionModel().addSelectionPath(path);
                        selectionPaths.add(path);
                    }
                }
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (TreePath path : selectionPaths) {
                    channelTable.getTreeSelectionModel().addSelectionPath(path);
                }
            }
        });
    }

    private class TableState {
        private List<String> selectedIds = new ArrayList<String>();
        private List<String> expandedGroupIds = new ArrayList<String>();

        public TableState(List<String> selectedIds, List<String> expandedGroupIds) {
            this.selectedIds = selectedIds;
            this.expandedGroupIds = expandedGroupIds;
        }

        public List<String> getSelectedIds() {
            return selectedIds;
        }

        public List<String> getExpandedGroupIds() {
            return expandedGroupIds;
        }
    }

    private class GroupDetailsDialog extends MirthDialog {

        private boolean saved = false;
        private boolean newGroup;

        public GroupDetailsDialog(boolean newGroup) {
            super(parent, true);
            this.newGroup = newGroup;

            initComponents();
            initLayout();

            if (newGroup) {
                String name;
                int index = 1;
                do {
                    name = "Group " + index++;
                } while (!checkGroupName(name));

                groupNameField.setText(name);
                groupNameField.requestFocus();
                groupNameField.selectAll();
            } else {
                AbstractChannelTableNode selectedNode = (AbstractChannelTableNode) channelTable.getPathForRow(channelTable.getSelectedRow()).getLastPathComponent();
                groupNameField.setText(selectedNode.getGroupStatus().getGroup().getName());
                groupDescriptionScrollPane.setText(selectedNode.getGroupStatus().getGroup().getDescription());

                groupNameField.requestFocus();
                groupNameField.setCaretPosition(groupNameField.getDocument().getLength());
            }

            setPreferredSize(new Dimension(600, 375));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setTitle("Channel Group Details");
            pack();
            setLocationRelativeTo(parent);
            setVisible(true);
        }

        public boolean wasSaved() {
            return saved;
        }

        public String getGroupName() {
            return groupNameField.getText();
        }

        public String getGroupDescription() {
            return groupDescriptionScrollPane.getText();
        }

        private void initComponents() {
            setBackground(UIConstants.BACKGROUND_COLOR);
            getContentPane().setBackground(getBackground());

            containerPanel = new JPanel();
            containerPanel.setBackground(getBackground());
            containerPanel.setBorder(BorderFactory.createTitledBorder("Group Settings"));

            groupNameLabel = new JLabel("Name:");
            groupNameField = new JTextField();

            groupDescriptionLabel = new JLabel("Description:");
            groupDescriptionScrollPane = new MirthRTextScrollPane(null, false, SyntaxConstants.SYNTAX_STYLE_NONE, false);
            groupDescriptionScrollPane.setSaveEnabled(false);

            separator = new JSeparator(SwingConstants.HORIZONTAL);

            okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String name = groupNameField.getText();

                    if (StringUtils.isBlank(name)) {
                        groupNameField.setBackground(UIConstants.INVALID_COLOR);
                        parent.alertError(GroupDetailsDialog.this, "Group name cannot be blank.");
                        return;
                    }

                    if (!checkGroupName(name, newGroup)) {
                        groupNameField.setBackground(UIConstants.INVALID_COLOR);
                        parent.alertError(GroupDetailsDialog.this, "Group name is already in use.");
                        return;
                    }

                    saved = true;
                    dispose();
                }
            });

            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    dispose();
                }
            });
        }

        private void initLayout() {
            setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, fill"));

            containerPanel.setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, fill, gap 12 6"));
            containerPanel.add(groupNameLabel, "right");
            containerPanel.add(groupNameField, "w 200!");
            containerPanel.add(groupDescriptionLabel, "newline, top, right");
            containerPanel.add(groupDescriptionScrollPane, "grow, push");
            add(containerPanel, "grow, push");

            add(separator, "newline, growx");
            add(okButton, "newline, w 51!, right, split 2");
            add(cancelButton, "w 51!");
        }

        private JPanel containerPanel;
        private JLabel groupNameLabel;
        private JTextField groupNameField;
        private JLabel groupDescriptionLabel;
        private MirthRTextScrollPane groupDescriptionScrollPane;
        private JSeparator separator;
        private JButton okButton;
        private JButton cancelButton;
    }

    private class GroupAssignmentDialog extends MirthDialog {

        private boolean saved = false;

        public GroupAssignmentDialog() {
            super(parent, true);

            initComponents();
            initLayout();

            setPreferredSize(new Dimension(337, 118));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setTitle("Channel Group Assignment");
            pack();
            setLocationRelativeTo(parent);
            setVisible(true);
        }

        public boolean wasSaved() {
            return saved;
        }

        public String getSelectedGroupId() {
            return ((Pair<String, String>) groupComboBox.getSelectedItem()).getLeft();
        }

        private void initComponents() {
            setBackground(UIConstants.BACKGROUND_COLOR);
            getContentPane().setBackground(getBackground());

            groupComboBox = new JComboBox<Pair<String, String>>();
            List<Pair<String, String>> groups = new ArrayList<Pair<String, String>>();
            for (Enumeration<? extends MutableTreeTableNode> groupNodes = ((MutableTreeTableNode) channelTable.getTreeTableModel().getRoot()).children(); groupNodes.hasMoreElements();) {
                ChannelGroup group = ((AbstractChannelTableNode) groupNodes.nextElement()).getGroupStatus().getGroup();

                groups.add(new MutablePair<String, String>(group.getId(), group.getName()) {
                    @Override
                    public String toString() {
                        return getRight();
                    }
                });
            }
            groupComboBox.setModel(new DefaultComboBoxModel<Pair<String, String>>(groups.toArray(new Pair[groups.size()])));
            groupComboBox.setSelectedIndex(0);

            separator = new JSeparator(SwingConstants.HORIZONTAL);

            okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    saved = true;
                    dispose();
                }
            });

            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    dispose();
                }
            });
        }

        private void initLayout() {
            setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, fill"));

            add(new JLabel("Choose the group to assign the selected channel(s) to."));
            add(groupComboBox, "newline, growx");
            add(separator, "newline, growx");
            add(okButton, "newline, w 51!, right, split 2");
            add(cancelButton, "w 51!");
        }

        private JComboBox<Pair<String, String>> groupComboBox;
        private JSeparator separator;
        private JButton okButton;
        private JButton cancelButton;
    }

    public JXTaskPane channelTasks;
    public JPopupMenu channelPopupMenu;
    public JXTaskPane groupTasks;
    public JPopupMenu groupPopupMenu;

    private JSplitPane splitPane;
    private JPanel topPanel;
    private MirthTreeTable channelTable;
    private JScrollPane channelScrollPane;
    private JPanel filterPanel;
    private JButton tagsFilterButton;
    private JLabel tagsLabel;
    private IconToggleButton tableModeGroupsButton;
    private IconToggleButton tableModeChannelsButton;

    private JTabbedPane tabPane;
}
