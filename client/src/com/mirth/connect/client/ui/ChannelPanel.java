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
import java.awt.Cursor;
import java.awt.Point;
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
import javax.swing.DefaultListSelectionModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.mirth.connect.client.ui.Frame.ConflictOption;
import com.mirth.connect.client.ui.codetemplate.CodeTemplateImportDialog;
import com.mirth.connect.client.ui.components.ChannelInfo;
import com.mirth.connect.client.ui.components.ChannelTableTransferHandler;
import com.mirth.connect.client.ui.components.ChannelsTableCellEditor;
import com.mirth.connect.client.ui.components.ChannelsTableCellRenderer;
import com.mirth.connect.client.ui.components.IconButton;
import com.mirth.connect.client.ui.components.IconToggleButton;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplateLibrary;
import com.mirth.connect.model.CodeTemplateLibrarySaveResult;
import com.mirth.connect.model.CodeTemplateLibrarySaveResult.CodeTemplateUpdateResult;
import com.mirth.connect.model.InvalidChannel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.ImportConverter3_0_0;
import com.mirth.connect.plugins.ChannelColumnPlugin;
import com.mirth.connect.plugins.ChannelPanelPlugin;
import com.mirth.connect.plugins.TaskPlugin;

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

    private static final int GROUP_CHANNELS_NAME_COLUMN = 0;
    private static final int GROUP_CHANNELS_ID_COLUMN = 1;

    private static final int TASK_CHANNEL_REFRESH = 0;
    private static final int TASK_CHANNEL_SAVE = 1;
    private static final int TASK_CHANNEL_REDEPLOY_ALL = 2;
    private static final int TASK_CHANNEL_DEPLOY = 3;
    private static final int TASK_CHANNEL_EDIT_GLOBAL_SCRIPTS = 4;
    private static final int TASK_CHANNEL_EDIT_CODE_TEMPLATES = 5;
    private static final int TASK_CHANNEL_NEW_GROUP = 6;
    private static final int TASK_CHANNEL_NEW_CHANNEL = 7;
    private static final int TASK_CHANNEL_IMPORT_GROUP = 8;
    private static final int TASK_CHANNEL_IMPORT_CHANNEL = 9;
    private static final int TASK_CHANNEL_EXPORT_ALL_GROUPS = 10;
    private static final int TASK_CHANNEL_EXPORT_ALL_CHANNELS = 11;
    private static final int TASK_CHANNEL_EXPORT_GROUP = 12;
    private static final int TASK_CHANNEL_EXPORT_CHANNEL = 13;
    private static final int TASK_CHANNEL_DELETE_GROUP = 14;
    private static final int TASK_CHANNEL_DELETE_CHANNEL = 15;
    private static final int TASK_CHANNEL_CLONE = 16;
    private static final int TASK_CHANNEL_EDIT = 17;
    private static final int TASK_CHANNEL_ENABLE = 18;
    private static final int TASK_CHANNEL_DISABLE = 19;
    private static final int TASK_CHANNEL_VIEW_MESSAGES = 20;

    private Frame parent;

    private Map<String, ChannelStatus> channelStatuses = new LinkedHashMap<String, ChannelStatus>();
    private Map<String, ChannelGroupStatus> groupStatuses = new LinkedHashMap<String, ChannelGroupStatus>();

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
        parent.addTask(TaskConstants.CHANNEL_SAVE, "Save Group Changes", "Save all changes made to channel groups.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/disk.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_REDEPLOY_ALL, "Redeploy All", "Undeploy all channels and deploy all currently enabled channels.", "A", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_rotate_clockwise.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_DEPLOY, "Deploy Channel", "Deploys the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_redo.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_EDIT_GLOBAL_SCRIPTS, "Edit Global Scripts", "Edit scripts that are not channel specific.", "G", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/script_edit.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_EDIT_CODE_TEMPLATES, "Edit Code Templates", "Create and manage templates to be used in JavaScript throughout Mirth.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/page_edit.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_NEW_GROUP, "New Group", "Create a new channel group.", "N", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_add.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_NEW_CHANNEL, "New Channel", "Create a new channel.", "N", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_add.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_IMPORT_GROUP, "Import Group", "Import a channel from an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_IMPORT_CHANNEL, "Import Channel", "Import a channel from an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_EXPORT_ALL_GROUPS, "Export All Groups", "Export all of the channel groups to XML files.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_EXPORT_ALL_CHANNELS, "Export All Channels", "Export all of the channels to XML files.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_EXPORT_GROUP, "Export Group", "Export the currently selected channel group to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_EXPORT_CHANNEL, "Export Channel", "Export the currently selected channel to an XML file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_DELETE_GROUP, "Delete Group", "Delete the currently selected channel group.", "L", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_delete.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_DELETE_CHANNEL, "Delete Channel", "Delete the currently selected channel.", "L", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_delete.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_CLONE, "Clone Channel", "Clone the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/page_copy.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_EDIT, "Edit Channel", "Edit the currently selected channel.", "I", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/application_form_edit.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_ENABLE, "Enable Channel", "Enable the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_DISABLE, "Disable Channel", "Disable the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_stop_blue.png")), channelTasks, channelPopupMenu, this);
        parent.addTask(TaskConstants.CHANNEL_VIEW_MESSAGES, "View Messages", "Show the messages for the currently selected channel.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/page_white_stack.png")), channelTasks, channelPopupMenu, this);

        parent.setNonFocusable(channelTasks);
        parent.taskPaneContainer.add(channelTasks, parent.taskPaneContainer.getComponentCount() - 1);

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
        List<JXTaskPane> taskPanes = new ArrayList<JXTaskPane>();
        taskPanes.add(channelTasks);

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
        return channelTasks.getContentPane().getComponent(TASK_CHANNEL_SAVE).isVisible();
    }

    @Override
    public void setSaveEnabled(boolean enabled) {
        setTaskVisibility(TASK_CHANNEL_SAVE, enabled);
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

        setAllTaskVisibility(false);

        setTaskVisible(TASK_CHANNEL_REFRESH);
        setTaskVisible(TASK_CHANNEL_REDEPLOY_ALL);
        setTaskVisible(TASK_CHANNEL_EDIT_GLOBAL_SCRIPTS);
        setTaskVisible(TASK_CHANNEL_EDIT_CODE_TEMPLATES);
        setTaskVisible(TASK_CHANNEL_NEW_CHANNEL);
        setTaskVisible(TASK_CHANNEL_IMPORT_CHANNEL);
        if (model.isGroupModeEnabled()) {
            if (!parent.getChannelTagInfo(false).isEnabled()) {
                setTaskVisible(TASK_CHANNEL_NEW_GROUP);
                setTaskVisible(TASK_CHANNEL_IMPORT_GROUP);
            }
            setTaskVisible(TASK_CHANNEL_EXPORT_ALL_GROUPS);
        } else {
            setTaskVisible(TASK_CHANNEL_EXPORT_ALL_CHANNELS);
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
                    setTaskVisible(TASK_CHANNEL_DISABLE);
                }
                if (!allEnabled) {
                    setTaskVisible(TASK_CHANNEL_ENABLE);
                }
            }

            if (allGroups) {
                if (channelNodeFound && !allDisabled) {
                    setTaskVisible(TASK_CHANNEL_DEPLOY);
                }
                setTaskVisible(TASK_CHANNEL_EXPORT_GROUP);

                if (!includesDefaultGroup && !parent.getChannelTagInfo(false).isEnabled()) {
                    setTaskVisible(TASK_CHANNEL_DELETE_GROUP);
                }
            } else if (allChannels) {
                if (!allDisabled) {
                    setTaskVisible(TASK_CHANNEL_DEPLOY);
                }
                setTaskVisible(TASK_CHANNEL_EXPORT_CHANNEL);
                setTaskVisible(TASK_CHANNEL_DELETE_CHANNEL);

                if (rows.length == 1) {
                    setTaskVisible(TASK_CHANNEL_CLONE);
                    setTaskVisible(TASK_CHANNEL_EDIT);
                    setTaskVisible(TASK_CHANNEL_VIEW_MESSAGES);
                }
            } else {
                setTaskVisible(TASK_CHANNEL_DEPLOY);
            }
        }
    }

    public void retrieveGroups() {
        try {
            updateChannelGroups(parent.mirthClient.getAllChannelGroups());
        } catch (ClientException e) {
            parent.alertThrowable(parent, e);
        }
    }

    public void retrieveChannels() {
        try {
            updateChannelStatuses(parent.mirthClient.getChannelSummary(getChannelHeaders(), false));
            updateChannelGroups(parent.mirthClient.getAllChannelGroups());
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

        parent.deployChannel(selectedEnabledChannelIds);
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

        String name;
        int index = 1;
        do {
            name = "Group " + index++;
        } while (!checkGroupName(name));

        AbstractChannelTableNode groupNode = model.addNewGroup(new ChannelGroup(name, ""));

        parent.setSaveEnabled(true);

        final TreePath path = new TreePath(new Object[] { root, groupNode });
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                channelTable.getTreeSelectionModel().setSelectionPath(path);
                groupNameField.selectAll();
            }
        });
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
        MutableTreeTableNode root = (MutableTreeTableNode) channelTable.getTreeTableModel().getRoot();
        if (root == null) {
            return false;
        }

        for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
            if (StringUtils.equals(((AbstractChannelTableNode) groupNodes.nextElement()).getGroupStatus().getGroup().getName(), name)) {
                return false;
            }
        }

        return true;
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
        if (isSaveEnabled() && !promptSave(true)) {
            return;
        }

        String content = parent.browseForFileString("XML");

        if (content != null) {
            importGroup(content, true);
        }
    }

    public void importGroup(String content, boolean showAlerts) {
        if (showAlerts && !parent.promptObjectMigration(content, "group")) {
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
        if (isSaveEnabled() && !promptSave(true)) {
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

        // Update resource names
        parent.updateResourceNames(channel);

        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        String channelXML = serializer.serialize(channel);
        // Reset the libraries on the cached channel
        channel.getCodeTemplateLibraries().clear();

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

    private void setTaskVisible(int task) {
        setTaskVisibility(task, true);
    }

    private void setTaskVisibility(int task, boolean visible) {
        parent.setVisibleTasks(channelTasks, channelPopupMenu, task, task, visible);
    }

    private void setAllTaskVisibility(boolean visible) {
        parent.setVisibleTasks(channelTasks, channelPopupMenu, 2, TASK_CHANNEL_VIEW_MESSAGES, visible);
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

    private void switchBottomPane() {
        if (isGroupSelected() && !isChannelSelected()) {
            if (channelTable.getSelectedModelRows().length == 1) {
                splitPane.setBottomComponent(groupSettingsPanel);
                splitPane.setDividerSize(6);
                splitPane.setDividerLocation(3 * Preferences.userNodeForPackage(Mirth.class).getInt("height", UIConstants.MIRTH_HEIGHT) / 5);
                splitPane.setResizeWeight(0.5);
            } else {
                splitPane.setBottomComponent(null);
                splitPane.setDividerSize(0);
            }
        } else if (LoadedExtensions.getInstance().getChannelPanelPlugins().size() > 0) {
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

        // Update group channels table
        Object[][] data = new Object[channelStatuses.size()][2];
        int row = 0;
        for (ChannelStatus channelStatus : channelStatuses.values()) {
            data[row][0] = new ChannelInfo(channelStatus.getChannel().getName(), false);
            data[row][1] = channelStatus.getChannel().getId();
            row++;
        }
        ((RefreshTableModel) groupChannelsTable.getModel()).refreshDataVector(data);

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
            }
            channelPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
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

        groupNameField.setEditable(true);
        groupDescriptionScrollPane.setBackground(null);
        groupDescriptionScrollPane.getTextArea().setEditable(true);
        groupChannelsSelectAllLabel.setEnabled(true);
        groupChannelsDeselectAllLabel.setEnabled(true);
        groupChannelsTable.setEnabled(true);
        groupWarningLabel.setVisible(false);

        switchBottomPane();

        if (rows.length == 1) {
            AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(rows[0]).getLastPathComponent();
            if (node.isGroupNode()) {
                setGroupProperties(node.getGroupStatus().getGroup());

                if (StringUtils.equals(node.getGroupStatus().getGroup().getId(), ChannelGroup.DEFAULT_ID) || parent.getChannelTagInfo(false).isEnabled()) {
                    groupNameField.setEditable(false);
                    groupDescriptionScrollPane.getTextArea().setEditable(false);
                    groupDescriptionScrollPane.setBackground(new Color(204, 204, 204));
                    groupDescriptionScrollPane.getTextArea().setEditable(false);
                    groupChannelsDeselectAllLabel.setEnabled(false);

                    if (parent.getChannelTagInfo(false).isEnabled()) {
                        groupChannelsSelectAllLabel.setEnabled(false);
                        groupChannelsTable.setEnabled(false);
                        groupWarningLabel.setVisible(true);
                    }
                }
            }
        }

        groupNameField.requestFocus();
        groupNameField.setCaretPosition(groupNameField.getText().length());
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

    private void setGroupProperties(ChannelGroup group) {
        groupNameField.setText(group.getName());
        groupNameField.setCaretPosition(0);
        groupDescriptionScrollPane.setText(group.getDescription());
        groupDescriptionScrollPane.getTextArea().setCaretPosition(0);

        TableModelListener[] listeners = ((RefreshTableModel) groupChannelsTable.getModel()).getTableModelListeners();
        for (TableModelListener listener : listeners) {
            groupChannelsTable.getModel().removeTableModelListener(listener);
        }

        try {
            for (int row = 0; row < groupChannelsTable.getModel().getRowCount(); row++) {
                ChannelInfo channelInfo = (ChannelInfo) groupChannelsTable.getModel().getValueAt(row, GROUP_CHANNELS_NAME_COLUMN);
                String channelId = (String) groupChannelsTable.getModel().getValueAt(row, GROUP_CHANNELS_ID_COLUMN);

                boolean found = false;
                for (Channel channel : group.getChannels()) {
                    if (StringUtils.equals(channel.getId(), channelId)) {
                        found = true;
                        break;
                    }
                }
                channelInfo.setEnabled(found);

                groupChannelsTable.getModel().setValueAt(channelInfo, row, GROUP_CHANNELS_NAME_COLUMN);
            }
        } finally {
            for (TableModelListener listener : listeners) {
                groupChannelsTable.getModel().addTableModelListener(listener);
            }
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
            public void importFile(final File file, final boolean showAlerts) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String fileString = StringUtils.trim(parent.readFileToString(file));

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

                AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent();
                if (node.isGroupNode()) {
                    return;
                }

                if (evt.getClickCount() >= 2) {
                    doEditChannel();
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

        tableModeLabel = new JLabel("Table View:");
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

        groupSettingsPanel = new JPanel();
        groupSettingsPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        groupSettingsLeftPanel = new JPanel();
        groupSettingsLeftPanel.setBackground(groupSettingsPanel.getBackground());

        groupNameLabel = new JLabel("Name:");
        groupNameField = new MirthTextField();
        groupNameField.setDocument(new MirthFieldConstraints(255, false, false, false) {
            @Override
            public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
                if (str == null) {
                    return;
                }

                if (!testName(getText(0, offset) + str + getText(offset, getLength() - offset))) {
                    return;
                }

                super.insertString(offset, str, attr);
            }

            @Override
            public void remove(int offs, int len) throws BadLocationException {
                if (!testName(getText(0, offs) + getText(offs + len, getLength() - offs - len))) {
                    return;
                }

                super.remove(offs, len);
            }

            private boolean testName(String name) {
                if (StringUtils.isBlank(name)) {
                    return true;
                }

                String selectedGroupId = null;
                int row = channelTable.getSelectedModelIndex();
                if (row >= 0) {
                    AbstractChannelTableNode selectedNode = (AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent();
                    if (selectedNode.isGroupNode()) {
                        selectedGroupId = selectedNode.getGroupStatus().getGroup().getId();
                    }
                }

                for (Enumeration<? extends MutableTreeTableNode> groupNodes = ((MutableTreeTableNode) channelTable.getTreeTableModel().getRoot()).children(); groupNodes.hasMoreElements();) {
                    AbstractChannelTableNode groupNode = (AbstractChannelTableNode) groupNodes.nextElement();
                    if (groupNode.isGroupNode()) {
                        String groupId = groupNode.getGroupStatus().getGroup().getId();
                        if (!StringUtils.equals(groupId, selectedGroupId) && StringUtils.equals(groupNode.getGroupStatus().getGroup().getName(), name)) {
                            return false;
                        }
                    }
                }

                return true;
            }
        });

        groupNameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent evt) {
                updateName(evt);
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                updateName(evt);
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                updateName(evt);
            }

            private void updateName(DocumentEvent evt) {
                int[] rows = channelTable.getSelectedModelRows();

                if (rows.length == 1) {
                    AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(rows[0]).getLastPathComponent();
                    if (node.isGroupNode() && !StringUtils.equals(node.getGroupStatus().getGroup().getId(), ChannelGroup.DEFAULT_ID)) {
                        try {
                            channelTable.getTreeTableModel().setValueAt(new ChannelTableNameEntry(evt.getDocument().getText(0, evt.getDocument().getLength())), node, NAME_COLUMN_NUMBER);
                        } catch (BadLocationException e) {
                        }
                    }
                }
            }
        });

        groupWarningLabel = new JLabel("Disable the channel filter to edit group properties.");
        groupWarningLabel.setForeground(Color.RED);

        groupDescriptionLabel = new JLabel("Description:");
        groupDescriptionScrollPane = new MirthRTextScrollPane(null, false, SyntaxConstants.SYNTAX_STYLE_NONE, false);
        groupDescriptionScrollPane.getTextArea().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent evt) {
                updateDescription(evt);
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                updateDescription(evt);
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                updateDescription(evt);
            }

            private void updateDescription(DocumentEvent evt) {
                int[] rows = channelTable.getSelectedModelRows();

                if (rows.length == 1) {
                    AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(rows[0]).getLastPathComponent();
                    if (node.isGroupNode() && !StringUtils.equals(node.getGroupStatus().getGroup().getId(), ChannelGroup.DEFAULT_ID)) {
                        try {
                            channelTable.getTreeTableModel().setValueAt(evt.getDocument().getText(0, evt.getDocument().getLength()), node, DESCRIPTION_COLUMN_NUMBER);
                        } catch (BadLocationException e) {
                        }
                    }
                }
            }
        });

        groupSettingsRightPanel = new JPanel();
        groupSettingsRightPanel.setBackground(groupSettingsPanel.getBackground());

        groupChannelsLabel = new JLabel("<html><b>Channels</b></html>");
        groupChannelsLabel.setForeground(new Color(64, 64, 64));

        groupChannelsSelectAllLabel = new JLabel("<html><u>Select All</u></html>");
        groupChannelsSelectAllLabel.setForeground(Color.BLUE);
        groupChannelsSelectAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        groupChannelsSelectAllLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                if (evt.getComponent().isEnabled()) {
                    for (int row = 0; row < groupChannelsTable.getRowCount(); row++) {
                        ChannelInfo channelInfo = (ChannelInfo) groupChannelsTable.getValueAt(row, GROUP_CHANNELS_NAME_COLUMN);
                        groupChannelsTable.setValueAt(new ChannelInfo(channelInfo.getName(), true), row, GROUP_CHANNELS_NAME_COLUMN);
                    }
                    groupChannelsTable.updateUI();
                    setSaveEnabled(true);
                }
            }
        });

        groupChannelsDeselectAllLabel = new JLabel("<html><u>Deselect All</u></html>");
        groupChannelsDeselectAllLabel.setForeground(Color.BLUE);
        groupChannelsDeselectAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        groupChannelsDeselectAllLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                if (evt.getComponent().isEnabled()) {
                    int[] rows = channelTable.getSelectedModelRows();
                    if (rows.length == 1) {
                        AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(rows[0]).getLastPathComponent();
                        if (node.isGroupNode() && StringUtils.equals(node.getGroupStatus().getGroup().getId(), ChannelGroup.DEFAULT_ID)) {
                            return;
                        }
                    }

                    for (int row = 0; row < groupChannelsTable.getRowCount(); row++) {
                        ChannelInfo channelInfo = (ChannelInfo) groupChannelsTable.getValueAt(row, GROUP_CHANNELS_NAME_COLUMN);
                        groupChannelsTable.setValueAt(new ChannelInfo(channelInfo.getName(), false), row, GROUP_CHANNELS_NAME_COLUMN);
                    }
                    setSaveEnabled(true);
                }
            }
        });

        groupChannelsFilterLabel = new JLabel("Filter:");
        groupChannelsFilterField = new JTextField();
        groupChannelsFilterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent evt) {
                groupChannelsTable.getRowSorter().allRowsChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                groupChannelsTable.getRowSorter().allRowsChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                groupChannelsTable.getRowSorter().allRowsChanged();
            }
        });

        groupChannelsTable = new MirthTable();
        groupChannelsTable.setModel(new RefreshTableModel(new Object[] { "Name", "Id" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                int[] rows = channelTable.getSelectedModelRows();
                if (rows.length == 1) {
                    AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(rows[0]).getLastPathComponent();
                    if (node.isGroupNode() && StringUtils.equals(node.getGroupStatus().getGroup().getId(), ChannelGroup.DEFAULT_ID)) {
                        if (((ChannelInfo) groupChannelsTable.getValueAt(row, GROUP_CHANNELS_NAME_COLUMN)).isEnabled()) {
                            return false;
                        }
                    }
                }
                return super.isCellEditable(row, column);
            }
        });

        groupChannelsTable.setDragEnabled(false);
        groupChannelsTable.setRowSelectionAllowed(false);
        groupChannelsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        groupChannelsTable.setFocusable(false);
        groupChannelsTable.setOpaque(true);
        groupChannelsTable.getTableHeader().setReorderingAllowed(false);
        groupChannelsTable.setEditable(true);

        groupChannelsTable.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
                String name = entry.getStringValue(GROUP_CHANNELS_NAME_COLUMN);
                return StringUtils.containsIgnoreCase(name, StringUtils.trim(groupChannelsFilterField.getText()));
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            groupChannelsTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        groupChannelsTable.getColumnExt(GROUP_CHANNELS_NAME_COLUMN).setCellRenderer(new ChannelsTableCellRenderer());
        groupChannelsTable.getColumnExt(GROUP_CHANNELS_NAME_COLUMN).setCellEditor(new ChannelsTableCellEditor());

        // Hide ID column
        groupChannelsTable.getColumnExt(GROUP_CHANNELS_ID_COLUMN).setVisible(false);

        groupChannelsTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent evt) {
                if (evt.getFirstRow() >= 0 && evt.getLastRow() <= groupChannelsTable.getModel().getRowCount()) {
                    ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();

                    int[] rows = channelTable.getSelectedModelRows();
                    if (rows.length == 1) {
                        final AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(rows[0]).getLastPathComponent();

                        if (node.isGroupNode()) {
                            ListSelectionListener[] listeners = ((DefaultListSelectionModel) channelTable.getSelectionModel()).getListSelectionListeners();
                            for (ListSelectionListener listener : listeners) {
                                channelTable.getSelectionModel().removeListSelectionListener(listener);
                            }

                            try {
                                for (int row = evt.getFirstRow(); row <= evt.getLastRow(); row++) {
                                    ChannelInfo channelInfo = (ChannelInfo) groupChannelsTable.getModel().getValueAt(row, GROUP_CHANNELS_NAME_COLUMN);
                                    String channelId = (String) groupChannelsTable.getModel().getValueAt(row, GROUP_CHANNELS_ID_COLUMN);

                                    if (channelInfo.isEnabled()) {
                                        model.addChannelToGroup(node, channelId);
                                    } else {
                                        model.removeChannelFromGroup(node, channelId);
                                    }
                                }

                                channelTable.expandPath(new TreePath(new Object[] {
                                        channelTable.getTreeTableModel().getRoot(), node }));
                            } finally {
                                for (ListSelectionListener listener : listeners) {
                                    channelTable.getSelectionModel().addListSelectionListener(listener);
                                }
                            }
                        }
                    }
                }
            }
        });

        groupChannelsScrollPane = new JScrollPane(groupChannelsTable);

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
        filterPanel.add(tableModeLabel, "right, split 3, gapafter 12");
        filterPanel.add(tableModeGroupsButton, "gapafter 0");
        filterPanel.add(tableModeChannelsButton);
        topPanel.add(filterPanel, "newline, growx");

        groupSettingsPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));

        groupSettingsLeftPanel.setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill", "[]13[]"));
        groupSettingsLeftPanel.add(groupNameLabel, "right");
        groupSettingsLeftPanel.add(groupNameField, "w 300!, split 2");
        groupSettingsLeftPanel.add(groupWarningLabel, "gapbefore 12");
        groupSettingsLeftPanel.add(groupDescriptionLabel, "newline, top, right");
        groupSettingsLeftPanel.add(groupDescriptionScrollPane, "grow, push, w :400, h 100:100");
        groupSettingsPanel.add(groupSettingsLeftPanel, "grow, push");

        groupSettingsRightPanel.setLayout(new MigLayout("insets 12 0 12 12, novisualpadding, hidemode 3, fill", "", "[][][grow]"));
        groupSettingsRightPanel.add(groupChannelsLabel, "left");
        groupSettingsRightPanel.add(groupChannelsSelectAllLabel, "right, split 3");
        groupSettingsRightPanel.add(new JLabel("|"));
        groupSettingsRightPanel.add(groupChannelsDeselectAllLabel);
        groupSettingsRightPanel.add(groupChannelsFilterLabel, "newline, split 2, sx");
        groupSettingsRightPanel.add(groupChannelsFilterField, "growx");
        groupSettingsRightPanel.add(groupChannelsScrollPane, "newline, grow, h 100:100, sx");
        groupSettingsPanel.add(groupSettingsRightPanel, "grow, w 220!");

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
        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTable.getTreeTableModel();
        if (model.isGroupModeEnabled() != groupModeEnabled) {
            if (isSaveEnabled() && !promptSave(true)) {
                return false;
            }

            Preferences.userNodeForPackage(Mirth.class).putBoolean("channelGroupViewEnabled", groupModeEnabled);

            if (groupModeEnabled) {
                tableModeChannelsButton.setContentFilled(false);
            } else {
                tableModeGroupsButton.setContentFilled(false);
            }

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

    public JXTaskPane channelTasks;
    public JPopupMenu channelPopupMenu;

    private JSplitPane splitPane;
    private JPanel topPanel;
    private MirthTreeTable channelTable;
    private JScrollPane channelScrollPane;
    private JPanel filterPanel;
    private JButton tagsFilterButton;
    private JLabel tagsLabel;
    private JLabel tableModeLabel;
    private IconToggleButton tableModeGroupsButton;
    private IconToggleButton tableModeChannelsButton;

    private JPanel groupSettingsPanel;
    private JPanel groupSettingsLeftPanel;
    private JLabel groupNameLabel;
    private JTextField groupNameField;
    private JLabel groupWarningLabel;
    private JLabel groupDescriptionLabel;
    private MirthRTextScrollPane groupDescriptionScrollPane;
    private JPanel groupSettingsRightPanel;
    private JLabel groupChannelsLabel;
    private JLabel groupChannelsSelectAllLabel;
    private JLabel groupChannelsDeselectAllLabel;
    private JLabel groupChannelsFilterLabel;
    private JTextField groupChannelsFilterField;
    private MirthTable groupChannelsTable;
    private JScrollPane groupChannelsScrollPane;

    private JTabbedPane tabPane;
}
