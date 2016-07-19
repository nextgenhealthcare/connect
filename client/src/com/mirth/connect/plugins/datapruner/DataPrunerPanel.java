/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import static com.mirth.connect.plugins.datapruner.DataPrunerServletInterface.TASK_START;
import static com.mirth.connect.plugins.datapruner.DataPrunerServletInterface.TASK_STOP;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.panels.connectors.PollingSettingsPanel;
import com.mirth.connect.client.ui.panels.export.MessageExportPanel;
import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollingType;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.SettingsPanelPlugin;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class DataPrunerPanel extends AbstractSettingsPanel {
    private static final int MIN_PRUNING_BLOCK_SIZE = 50;
    private static final int MAX_PRUNING_BLOCK_SIZE = 10000;
    private final static Color ACTIVE_STATUS_COLOR = new Color(200, 0, 0);
    private final static Color INACTIVE_STATUS_COLOR = new Color(0, 100, 0);
    private final static Color UNKNOWN_STATUS_COLOR = new Color(0, 0, 0);

    private SettingsPanelPlugin plugin = null;
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private AtomicBoolean refreshing = new AtomicBoolean(false);
    private Frame parent;
    private int startIndex;
    private int stopIndex;

    public DataPrunerPanel(String tabName, SettingsPanelPlugin plugin) {
        super(tabName);
        this.plugin = plugin;
        this.parent = PlatformUI.MIRTH_FRAME;

        addTask("doViewEvents", "View Events", "View the Data Pruner events.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/table.png")));
        startIndex = addTask(TASK_START, "Prune Now", "Start the Data Pruner now.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")));
        stopIndex = addTask(TASK_STOP, "Stop Pruner", "Stop the current Data Pruner process.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/stop.png")));

        setStartTaskVisible(false);
        setStopTaskVisible(false);

        initComponents();
        initLayout();

        blockSizeTextField.setDocument(new MirthFieldConstraints(0, false, false, true));
        pruneEventAgeTextField.setDocument(new MirthFieldConstraints(0, false, false, true));
    }

    @Override
    public void doRefresh() {
        if (PlatformUI.MIRTH_FRAME.alertRefresh()) {
            return;
        }

        final String workingId = getFrame().startWorking("Loading " + getTabName() + " properties...");

        final Properties serverProperties = new Properties();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    Properties propertiesFromServer = plugin.getPropertiesFromServer();

                    if (propertiesFromServer != null) {
                        serverProperties.putAll(propertiesFromServer);
                    }
                } catch (Exception e) {
                    getFrame().alertThrowable(getFrame(), e);
                }
                return null;
            }

            @Override
            public void done() {
                setProperties(serverProperties);
                getFrame().stopWorking(workingId);
            }
        };

        worker.execute();
    }

    private boolean validateFields() {
        boolean valid = true;
        StringBuilder builder = new StringBuilder();

        archiverPanel.resetInvalidProperties();
        pruneEventAgeTextField.setBackground(null);
        blockSizeTextField.setBackground(null);

        pollingSettingsPanel.setInvalidProperties(false, false);
        if (yesEnabledRadio.isSelected()) {
            PollConnectorProperties properties = pollingSettingsPanel.getProperties();
            PollingType pollingType = properties.getPollingType();
            if (pollingType.equals(PollingType.INTERVAL)) {
                int frequency = properties.getPollingFrequency();

                if (frequency < 3600000 || frequency >= 86400000) {
                    valid = false;
                    pollingSettingsPanel.setInvalidProperties(!valid, false);
                    builder.append("Frequency must be between 1 and 24 hours when converted to milliseconds.");
                }
            }
        }

        String prunerBlockSize = blockSizeTextField.getText();
        if (StringUtils.isEmpty(prunerBlockSize) || Integer.parseInt(prunerBlockSize) < MIN_PRUNING_BLOCK_SIZE || Integer.parseInt(prunerBlockSize) > MAX_PRUNING_BLOCK_SIZE) {
            blockSizeTextField.setBackground(UIConstants.INVALID_COLOR);
            builder.append("\n");
            builder.append("Pruner Block size must be between 50 and 10000. The recommended value for most servers is 1000.");

            valid = false;
        }

        if (pruneEventsYes.isSelected() && StringUtils.isBlank(pruneEventAgeTextField.getText())) {
            pruneEventAgeTextField.setBackground(UIConstants.INVALID_COLOR);
            builder.append("\n");
            builder.append("Event Age is required when pruning events.");

            valid = false;
        }

        String errorMessage = archiverPanel.validate(true);
        if (StringUtils.isNotEmpty(errorMessage)) {
            builder.append("\n");
            builder.append(errorMessage);
            valid = false;
        }

        if (!valid) {
            parent.alertError(this, builder.toString());
        }

        return valid;
    }

    @Override
    public boolean doSave() {
        if (!validateFields()) {
            return false;
        }

        final String workingId = getFrame().startWorking("Saving " + getTabName() + " properties...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    plugin.setPropertiesToServer(getProperties());
                } catch (Exception e) {
                    getFrame().alertThrowable(getFrame(), e);
                }
                return null;
            }

            @Override
            public void done() {
                setSaveEnabled(false);
                getFrame().stopWorking(workingId);
                updateStatus();
            }
        };

        worker.execute();

        return true;
    }

    public void doViewEvents() {
        getFrame().doShowEvents("Data Pruner");
    }

    public void doStart() {
        final MutableBoolean saveChanges = new MutableBoolean(false);

        if (isSaveEnabled()) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Settings changes must be saved first, would you like to save the settings and prune now?", "Select an Option", JOptionPane.OK_CANCEL_OPTION)) {
                if (!validateFields()) {
                    return;
                }

                saveChanges.setValue(true);
            } else {
                return;
            }
        }

        setStartTaskVisible(false);
        final String workingId = parent.startWorking("Starting the data pruner...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                if (saveChanges.getValue()) {
                    try {
                        plugin.setPropertiesToServer(getProperties());
                    } catch (Exception e) {
                        getFrame().alertThrowable(getFrame(), e);
                        return null;
                    }
                }

                try {
                    parent.mirthClient.getServlet(DataPrunerServletInterface.class).start();
                } catch (Exception e) {
                    parent.alertThrowable(parent, e, "An error occurred while attempting to start the data pruner.");
                    return null;
                }

                return null;
            }

            @Override
            public void done() {
                if (saveChanges.getValue()) {
                    setSaveEnabled(false);
                }

                parent.stopWorking(workingId);
                updateStatus();
            }
        };

        worker.execute();
    }

    public void doStop() {
        setStopTaskVisible(false);
        final String workingId = parent.startWorking("Stopping the data pruner...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    parent.mirthClient.getServlet(DataPrunerServletInterface.class).stop();
                } catch (Exception e) {
                    parent.alertThrowable(parent, e, "An error occurred while attempting to stop the data pruner.");
                    return null;
                }

                return null;
            }

            @Override
            public void done() {
                parent.stopWorking(workingId);
                updateStatus();
            }
        };

        worker.execute();
    }

    public void setProperties(Properties properties) {
        if (Boolean.parseBoolean(properties.getProperty("enabled"))) {
            yesEnabledRadio.setSelected(true);
        } else {
            noEnabledRadio.setSelected(true);
        }

        archiverPanel.resetInvalidProperties();
        archiverPanel.setIncludeAttachments(Boolean.parseBoolean(properties.getProperty("includeAttachments", Boolean.FALSE.toString())));

        String archiverOptions = properties.getProperty("archiverOptions");

        /*
         * archiverOptions might be empty if the pruner settings were migrated from a previous
         * version of Mirth Connect.
         */
        if (archiverOptions == null) {
            archiverPanel.setMessageWriterOptions(new MessageWriterOptions());
        } else {
            archiverPanel.setMessageWriterOptions(serializer.deserialize(archiverOptions, MessageWriterOptions.class));
        }

        if (archiverPanel.isEnabled()) {
            archiverPanel.setArchiveEnabled(Boolean.parseBoolean(properties.getProperty("archiveEnabled", Boolean.FALSE.toString())));
        }

        if (properties.getProperty("pruningBlockSize") != null && !properties.getProperty("pruningBlockSize").equals("")) {
            blockSizeTextField.setText(properties.getProperty("pruningBlockSize"));
        } else {
            blockSizeTextField.setText("1000");
        }

        if (Boolean.parseBoolean(properties.getProperty("pruneEvents", Boolean.FALSE.toString()))) {
            pruneEventsYes.setSelected(true);
            pruneEventsNo.setSelected(false);
            pruneEventAgeLabel.setEnabled(true);
            pruneEventAgeTextField.setEnabled(true);
        } else {
            pruneEventsYes.setSelected(false);
            pruneEventsNo.setSelected(true);
            pruneEventAgeLabel.setEnabled(false);
            pruneEventAgeTextField.setEnabled(false);
        }

        pruneEventAgeTextField.setText(properties.getProperty("maxEventAge"));

        if (properties.getProperty("archiverBlockSize") != null && !properties.getProperty("archiverBlockSize").equals("")) {
            archiverPanel.setArchiverBlockSize(properties.getProperty("archiverBlockSize"));
        } else {
            archiverPanel.setArchiverBlockSize("50");
        }

        PollConnectorProperties pollProperties = null;
        String pollingProperties = properties.getProperty("pollingProperties");
        if (pollingProperties == null) {
            pollProperties = new PollConnectorProperties();
        } else {
            pollProperties = serializer.deserialize(pollingProperties, PollConnectorProperties.class);
        }

        pollingSettingsPanel.setProperties(pollProperties);
        enabledActionPerformed();

        repaint();
        updateStatus();
        parent.setSaveEnabled(false);
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        if (blockSizeTextField.getText().equals("")) {
            blockSizeTextField.setText("1000");
        }

        String enabled = "true";
        if (noEnabledRadio.isSelected()) {
            enabled = "false";
        }
        properties.setProperty("enabled", enabled);
        properties.setProperty("pollingProperties", serializer.serialize(pollingSettingsPanel.getProperties()));

        properties.setProperty("pruningBlockSize", blockSizeTextField.getText());
        properties.setProperty("pruneEvents", Boolean.toString(pruneEventsYes.isSelected()));
        properties.setProperty("maxEventAge", pruneEventAgeTextField.getText());

        properties.setProperty("archiveEnabled", Boolean.toString(archiverPanel.isArchiveEnabled()));
        properties.setProperty("archiverBlockSize", archiverPanel.getArchiverBlockSize());
        properties.setProperty("includeAttachments", Boolean.toString(archiverPanel.isIncludeAttachments()));
        properties.setProperty("archiverOptions", serializer.serialize(archiverPanel.getMessageWriterOptions()));

        return properties;
    }

    private void updateStatus() {
        final String workingId = parent.startWorking("Refreshing status...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    Map<String, String> status = parent.mirthClient.getServlet(DataPrunerServletInterface.class).getStatusMap();
                    currentStateTextLabel.setText(status.get("currentState"));
                    currentProcessTextLabel.setText(status.get("currentProcess"));
                    lastProcessTextLabel.setText(status.get("lastProcess"));

                    String nextProcess = "Not scheduled";
                    if (yesEnabledRadio.isSelected()) {
                        nextProcess = status.get("nextProcess");
                    }
                    nextProcessTextLabel.setText(nextProcess);

                    if (status.get("isRunning").equals("false")) {
                        currentStateTextLabel.setForeground(INACTIVE_STATUS_COLOR);
                        setStartTaskVisible(true);
                        setStopTaskVisible(false);
                    } else {
                        currentStateTextLabel.setForeground(ACTIVE_STATUS_COLOR);
                        setStartTaskVisible(false);
                        setStopTaskVisible(true);
                    }
                } catch (ClientException e) {
                    currentStateTextLabel.setText("Unknown");
                    currentStateTextLabel.setForeground(UNKNOWN_STATUS_COLOR);
                    currentProcessTextLabel.setText("");
                    lastProcessTextLabel.setText("");
                    nextProcessTextLabel.setText("");
                    setStartTaskVisible(false);
                    setStopTaskVisible(false);
                    parent.alertThrowable(parent, e, "An error occurred while attempting to retrieve the status of the data pruner.");
                }

                return null;
            }

            @Override
            public void done() {
                parent.stopWorking(workingId);
                refreshing.set(false);
            }
        };

        worker.execute();
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        statusPanel = new JPanel();
        statusPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        statusPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Status", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        currentStateLabel = new JLabel("Current State:");
        currentStateTextLabel = new JLabel("Unknown");

        currentProcessLabel = new JLabel("Current Process:");
        currentProcessTextLabel = new JLabel("Unknown");

        lastProcessLabel = new JLabel("Last Process:");
        lastProcessTextLabel = new JLabel("Unknown");

        nextProcessLabel = new JLabel("Next Process:");
        nextProcessTextLabel = new JLabel("Unknown");

        pruningSchedulePanel = new JPanel();
        pruningSchedulePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        pruningSchedulePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Schedule", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11)));

        enabledLabel = new JLabel("Enable:");

        yesEnabledRadio = new MirthRadioButton("Yes");
        yesEnabledRadio.setFocusable(false);
        yesEnabledRadio.setBackground(Color.white);
        yesEnabledRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                enabledActionPerformed();
            }
        });

        noEnabledRadio = new MirthRadioButton("No");
        noEnabledRadio.setFocusable(false);
        noEnabledRadio.setBackground(Color.white);
        noEnabledRadio.setSelected(true);
        noEnabledRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                enabledActionPerformed();
            }
        });

        enabledButtonGroup = new ButtonGroup();
        enabledButtonGroup.add(yesEnabledRadio);
        enabledButtonGroup.add(noEnabledRadio);

        pollingSettingsPanel = new PollingSettingsPanel(false);

        pruneSettingsPanel = new JPanel();
        pruneSettingsPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        pruneSettingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Prune Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11))); // NOI18N

        blockSizeLabel = new JLabel("Block Size:");
        blockSizeTextField = new MirthTextField();
        blockSizeTextField.setToolTipText("<html>The number of messages that will be pruned at a time. This value must<br/>be between 50 and 10000. The recommended value for most servers is 1000.</html>");

        pruneEventsLabel = new JLabel("Prune Events:");

        pruneEventsYes = new MirthRadioButton("Yes");
        pruneEventsYes.setBackground(UIConstants.BACKGROUND_COLOR);
        pruneEventsYes.setToolTipText("<html>If Yes, event records older than the Event Age will be pruned. If No, event records will not be pruned.</html>");
        pruneEventsYes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                pruneEventsActionPerformed(evt);
            }
        });

        pruneEventsNo = new MirthRadioButton("No");
        pruneEventsNo.setBackground(UIConstants.BACKGROUND_COLOR);
        pruneEventsNo.setToolTipText("<html>If Yes, event records will be pruned in addition to messages. If No, event records will not be pruned.</html>");
        pruneEventsNo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                pruneEventsActionPerformed(evt);
            }
        });

        pruneEventsButtonGroup = new ButtonGroup();
        pruneEventsButtonGroup.add(pruneEventsYes);
        pruneEventsButtonGroup.add(pruneEventsNo);

        pruneEventAgeLabel = new JLabel("Prune Event Age:");

        pruneEventAgeTextField = new MirthTextField();
        pruneEventAgeTextField.setToolTipText("<html>Events older than this number of days will be pruned if Prune Events is set to Yes.</html>");

        eventDaysLabel = new JLabel("days");
        eventDaysLabel.setEnabled(false);

        archiverContainerPanel = new JPanel();
        archiverContainerPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        archiverContainerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Archive Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11))); // NOI18N
        archiverContainerPanel.setLayout(null);

        archiverPanel = new MessageExportPanel(Frame.userPreferences, true, false);
        archiverPanel.setBackground(archiverContainerPanel.getBackground());
        archiverContainerPanel.setLayout(new MigLayout("fillx, insets 0 0 0 0", "[grow,fill]", "[grow,fill]"));
        archiverContainerPanel.add(archiverPanel, "height 250!, aligny top");

        enabledActionPerformed();
    }

    private void initLayout() {
        setLayout(new MigLayout("hidemode 3, novisualpadding, insets 12", "[grow]"));

        statusPanel.setLayout(new MigLayout("hidemode 3, novisualpadding, insets 0", "12[right][left]"));
        statusPanel.add(currentStateLabel);
        statusPanel.add(currentStateTextLabel, "wrap");
        statusPanel.add(currentProcessLabel);
        statusPanel.add(currentProcessTextLabel, "wrap");
        statusPanel.add(lastProcessLabel);
        statusPanel.add(lastProcessTextLabel, "wrap");
        statusPanel.add(nextProcessLabel);
        statusPanel.add(nextProcessTextLabel, "wrap");

        pruningSchedulePanel.setLayout(new MigLayout("hidemode 3, novisualpadding, insets 0", "21[][]"));
        pruningSchedulePanel.add(enabledLabel, "gapleft 37, split");
        pruningSchedulePanel.add(yesEnabledRadio, "gapleft 12");
        pruningSchedulePanel.add(noEnabledRadio, "wrap");
        pruningSchedulePanel.add(pollingSettingsPanel);

        pruneSettingsPanel.setLayout(new MigLayout("hidemode 3, novisualpadding, insets 0", "11[right]12[left]"));
        pruneSettingsPanel.add(blockSizeLabel);
        pruneSettingsPanel.add(blockSizeTextField, "w 75!, h 22!, wrap");
        pruneSettingsPanel.add(pruneEventsLabel);
        pruneSettingsPanel.add(pruneEventsYes, "split");
        pruneSettingsPanel.add(pruneEventsNo, "wrap");
        pruneSettingsPanel.add(pruneEventAgeLabel);
        pruneSettingsPanel.add(pruneEventAgeTextField, "w 75!, h 22!, split");
        pruneSettingsPanel.add(eventDaysLabel, "gapleft 8, wrap");

        add(statusPanel, "grow, sx, wrap");
        add(pruningSchedulePanel, "grow, sx, wrap");
        add(pruneSettingsPanel, "grow, sx, wrap");
        add(archiverContainerPanel, "grow, sx");
    }

    private void pruneEventsActionPerformed(ActionEvent evt) {
        pruneEventAgeLabel.setEnabled(pruneEventsYes.isSelected());
        pruneEventAgeTextField.setEnabled(pruneEventsYes.isSelected());
        eventDaysLabel.setEnabled(pruneEventsYes.isSelected());
    }

    private void enabledActionPerformed() {
        pollingSettingsPanel.enableComponents(yesEnabledRadio.isSelected());
    }

    private void setStartTaskVisible(boolean visible) {
        setVisibleTasks(startIndex, startIndex, visible);
    }

    private void setStopTaskVisible(boolean visible) {
        setVisibleTasks(stopIndex, stopIndex, visible);
    }

    private JPanel statusPanel;
    private JLabel currentStateLabel;
    private JLabel currentStateTextLabel;
    private JLabel currentProcessLabel;
    private JLabel currentProcessTextLabel;
    private JLabel lastProcessLabel;
    private JLabel lastProcessTextLabel;
    private JLabel nextProcessLabel;
    private JLabel nextProcessTextLabel;

    private JPanel pruningSchedulePanel;
    private JLabel enabledLabel;
    private MirthRadioButton yesEnabledRadio;
    private MirthRadioButton noEnabledRadio;
    private ButtonGroup enabledButtonGroup;
    private PollingSettingsPanel pollingSettingsPanel;

    private JPanel pruneSettingsPanel;
    private JLabel blockSizeLabel;
    private MirthTextField blockSizeTextField;
    private JLabel pruneEventsLabel;
    private MirthRadioButton pruneEventsYes;
    private MirthRadioButton pruneEventsNo;
    private ButtonGroup pruneEventsButtonGroup;
    private JLabel pruneEventAgeLabel;
    private MirthTextField pruneEventAgeTextField;
    private JLabel eventDaysLabel;

    private JPanel archiverContainerPanel;
    private MessageExportPanel archiverPanel;
}