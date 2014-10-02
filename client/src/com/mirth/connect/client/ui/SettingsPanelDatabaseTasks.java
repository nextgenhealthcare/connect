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
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.DatabaseTask;
import com.mirth.connect.model.DatabaseTask.Status;

public class SettingsPanelDatabaseTasks extends AbstractSettingsPanel implements ListSelectionListener {

    public static final String TAB_NAME = "Database Tasks";

    private JXTable taskTable;
    private JXTable channelsTable;

    public SettingsPanelDatabaseTasks(String tabName) {
        super(tabName);
        setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill"));
        setBackground(UIConstants.BACKGROUND_COLOR);
        initComponents();
        addTask(TaskConstants.SETTINGS_RUN_DATABASE_TASK, "Run Task", "Execute the selected database task.", "", new ImageIcon(Frame.class.getResource("images/control_play_blue.png")));
        addTask(TaskConstants.SETTINGS_CANCEL_DATABASE_TASK, "Cancel Task", "Cancel the selected database task.", "", new ImageIcon(Frame.class.getResource("images/stop.png")));
        setVisibleTasks(2, 3, false);
    }

    @Override
    public void doRefresh() {
        final String workingId = getFrame().startWorking("Loading database tasks...");
        final int selectedRow = taskTable.getSelectedRow();

        SwingWorker<Map<String, DatabaseTask>, Void> worker = new SwingWorker<Map<String, DatabaseTask>, Void>() {

            @Override
            public Map<String, DatabaseTask> doInBackground() throws ClientException {
                return getFrame().mirthClient.getDatabaseTasks();
            }

            @Override
            public void done() {
                try {
                    Map<String, DatabaseTask> databaseTasks = get();
                    if (databaseTasks == null) {
                        databaseTasks = new HashMap<String, DatabaseTask>();
                    }

                    Object[][] data = new Object[databaseTasks.size()][4];
                    int i = 0;

                    for (DatabaseTask databaseTask : databaseTasks.values()) {
                        Status status = databaseTask.getStatus();
                        data[i][0] = new CellData(status == Status.IDLE ? UIConstants.ICON_BULLET_YELLOW : UIConstants.ICON_BULLET_GREEN, status.toString());
                        data[i][1] = databaseTask;
                        data[i][2] = databaseTask.getDescription();
                        data[i][3] = databaseTask.getStartDateTime();
                        i++;
                    }

                    ((RefreshTableModel) taskTable.getModel()).refreshDataVector(data);

                    if (selectedRow > -1 && selectedRow < taskTable.getRowCount()) {
                        taskTable.setRowSelectionInterval(selectedRow, selectedRow);
                    }
                } catch (Throwable t) {
                    if (t instanceof ExecutionException) {
                        t = t.getCause();
                    }
                    getFrame().alertException(getFrame(), t.getStackTrace(), "Error loading database tasks: " + t.toString());
                } finally {
                    getFrame().stopWorking(workingId);
                }
            }
        };

        worker.execute();
    }

    @Override
    public boolean doSave() {
        return false;
    }

    public void doRunDatabaseTask() {
        final DatabaseTask databaseTask = (DatabaseTask) taskTable.getValueAt(taskTable.getSelectedRow(), 1);

        if (databaseTask.getConfirmationMessage() != null && !getFrame().alertOption(getFrame(), databaseTask.getConfirmationMessage())) {
            return;
        }

        final String workingId = getFrame().startWorking("Running database task...");

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {

            @Override
            public String doInBackground() throws ClientException {
                return getFrame().mirthClient.runDatabaseTask(databaseTask);
            }

            @Override
            public void done() {
                try {
                    String result = get();
                    if (StringUtils.isNotBlank(result)) {
                        getFrame().alertInformation(getFrame(), result);
                    }
                } catch (Throwable t) {
                    if (t instanceof ExecutionException) {
                        t = t.getCause();
                    }
                    getFrame().alertException(getFrame(), t.getStackTrace(), "Error running database task: " + t.getMessage());
                } finally {
                    getFrame().stopWorking(workingId);
                    doRefresh();
                }
            }
        };

        worker.execute();
        doRefresh();
    }

    public void doCancelDatabaseTask() {
        final DatabaseTask databaseTask = (DatabaseTask) taskTable.getValueAt(taskTable.getSelectedRow(), 1);

        if (databaseTask.getStatus() != Status.RUNNING) {
            getFrame().alertError(getFrame(), "Task \"" + databaseTask.getName() + "\" is not currently running.");
            return;
        }

        if (!getFrame().alertOption(getFrame(), "Are you sure you want to cancel the selected database task?")) {
            return;
        }

        final String workingId = getFrame().startWorking("Cancelling database task...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() throws ClientException {
                getFrame().mirthClient.cancelDatabaseTask(databaseTask);
                return null;
            }

            @Override
            public void done() {
                try {
                    get();
                } catch (Throwable t) {
                    if (t instanceof ExecutionException) {
                        t = t.getCause();
                    }
                    getFrame().alertException(getFrame(), t.getStackTrace(), "Error cancelling database task: " + t.getMessage());
                } finally {
                    getFrame().stopWorking(workingId);
                    doRefresh();
                }
            }
        };

        worker.execute();
    }

    private void initComponents() {
        JPanel containerPanel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        containerPanel.setBackground(getBackground());
        containerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Database Tasks", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));
        containerPanel.add(new JLabel("Cleanup or optimization tasks for the internal database. If no tasks are present, no action is necessary."), "top, wrap");

        taskTable = new MirthTable();
        taskTable.setModel(new RefreshTableModel(new Object[] { "Status", "Name", "Description",
                "Start Time" }, 0));
        taskTable.setDragEnabled(false);
        taskTable.setRowSelectionAllowed(true);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setRowHeight(UIConstants.ROW_HEIGHT);
        taskTable.setFocusable(false);
        taskTable.setOpaque(true);
        taskTable.getTableHeader().setReorderingAllowed(false);
        taskTable.setEditable(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            taskTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                checkSelection(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                checkSelection(evt);
            }

            private void checkSelection(MouseEvent evt) {
                if (taskTable.rowAtPoint(new Point(evt.getX(), evt.getY())) < 0) {
                    taskTable.clearSelection();
                    setVisibleTasks(2, 3, false);
                }
            }
        });

        taskTable.getColumnModel().getColumn(0).setMinWidth(75);
        taskTable.getColumnModel().getColumn(0).setMaxWidth(75);
        taskTable.getColumnModel().getColumn(0).setCellRenderer(new ImageCellRenderer());

        taskTable.getColumnModel().getColumn(1).setMinWidth(45);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(250);

        taskTable.getColumnModel().getColumn(2).setMinWidth(75);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(475);

        taskTable.getColumnModel().getColumn(3).setMinWidth(95);
        taskTable.getColumnModel().getColumn(3).setMaxWidth(95);
        taskTable.getColumnModel().getColumn(3).setCellRenderer(new DateCellRenderer());

        taskTable.getSelectionModel().addListSelectionListener(this);

        JScrollPane taskTableScrollPane = new JScrollPane(taskTable);
        containerPanel.add(taskTableScrollPane, "grow, push");

        add(containerPanel, "grow, h 60%");

        JPanel channelsPanel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        channelsPanel.setBackground(getBackground());
        channelsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Affected Channels", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        channelsTable = new MirthTable();
        channelsTable.setModel(new RefreshTableModel(new Object[] { "Name", "Id" }, 0));
        channelsTable.setDragEnabled(false);
        channelsTable.setRowSelectionAllowed(false);
        channelsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        channelsTable.setFocusable(false);
        channelsTable.setOpaque(true);
        channelsTable.getTableHeader().setReorderingAllowed(false);
        channelsTable.setEditable(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            channelsTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        channelsPanel.add(new JScrollPane(channelsTable), "grow");

        add(channelsPanel, "newline, grow, h 40%");
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
            int selectedRow = taskTable.getSelectedRow();
            boolean showRun = evt.getFirstIndex() > -1;
            boolean showCancel = false;

            if (showRun) {
                for (int row = 0; row < taskTable.getRowCount(); row++) {
                    if (((DatabaseTask) taskTable.getValueAt(row, 1)).getStatus() == Status.RUNNING) {
                        showRun = false;
                        if (row == selectedRow) {
                            showCancel = true;
                        }
                    }
                }
            }

            setVisibleTasks(2, 2, showRun);
            setVisibleTasks(3, 3, showCancel);

            Map<String, String> affectedChannels = new HashMap<String, String>();
            if (selectedRow > -1) {
                affectedChannels = ((DatabaseTask) taskTable.getValueAt(selectedRow, 1)).getAffectedChannels();
            }

            Object[][] data = new Object[affectedChannels.size()][2];
            int i = 0;

            for (String channelId : affectedChannels.keySet()) {
                data[i][0] = affectedChannels.get(channelId);
                data[i][1] = channelId;
                i++;
            }

            ((RefreshTableModel) channelsTable.getModel()).refreshDataVector(data);
        }
    }
}