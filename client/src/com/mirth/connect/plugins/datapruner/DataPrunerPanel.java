/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import java.awt.Color;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTimePicker;
import com.mirth.connect.client.ui.panels.export.MessageExportPanel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.SettingsPanelPlugin;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class DataPrunerPanel extends AbstractSettingsPanel {
    private final static Color ACTIVE_STATUS_COLOR = new Color(200, 0, 0);
    private final static Color INACTIVE_STATUS_COLOR = new Color(0, 100, 0);
    private final static Color UNKNOWN_STATUS_COLOR = new Color(0, 0, 0);

    private SettingsPanelPlugin plugin = null;
    private MessageExportPanel archiverPanel;
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
        startIndex = addTask("doStart", "Prune Now", "Start the Data Pruner now.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/control_play_blue.png")));
        stopIndex = addTask("doStop", "Stop Pruner", "Stop the current Data Pruner process.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/stop.png")));

        setStartTaskVisible(false);
        setStopTaskVisible(false);
        
        initComponents();
        blockSizeTextField.setDocument(new MirthFieldConstraints(0, false, false, true));
        pruneEventAgeTextField.setDocument(new MirthFieldConstraints(0, false, false, true));

        /*
         * The archiver panel uses MigLayout, so we attach it to a container panel that is part of
         * the netbeans-generated layout.
         */
        archiverPanel = new MessageExportPanel(Frame.userPreferences, true, false);
        archiverPanel.setBackground(archiverContainerPanel.getBackground());
        archiverContainerPanel.setLayout(new MigLayout("fillx, insets 0 0 0 0", "[grow,fill]", "[grow,fill]"));
        archiverContainerPanel.add(archiverPanel, "height 195!, aligny top");
    }

    @Override
    public void doRefresh() {
        if (!refreshing.compareAndSet(false, true)) {
            System.out.println("Already refreshing, ignoring");
            return;
        }

        final String workingId = getFrame().startWorking("Loading " + getTabName() + " properties...");

        final Properties serverProperties = new Properties();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    if (!getFrame().confirmLeave()) {
                        return null;
                    }

                    Properties propertiesFromServer = plugin.getPropertiesFromServer();

                    if (propertiesFromServer != null) {
                        serverProperties.putAll(propertiesFromServer);
                    }
                } catch (Exception e) {
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
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
        archiverPanel.resetInvalidProperties();
        pruneEventAgeTextField.setBackground(null);

        if (!archiverPanel.validate(true)) {
            parent.alertError(this, "Please fill in required fields.");
            return false;
        }
        
        if (pruneEventsYes.isSelected() && StringUtils.isBlank(pruneEventAgeTextField.getText())) {
            pruneEventAgeTextField.setBackground(UIConstants.INVALID_COLOR);
            parent.alertError(this, "Please fill in required fields.");
            return false;
        }
        
        return true;
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
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
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
                        getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                        return null;
                    }
                }
                
                try {
                    parent.mirthClient.invokePluginMethod(plugin.getPluginName(), "start", null);
                } catch (Exception e) {
                    parent.alertException(parent, e.getStackTrace(), "An error occurred while attempting to start the data pruner.");
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
                    parent.mirthClient.invokePluginMethod(plugin.getPluginName(), "stop", null);
                } catch (Exception e) {
                    parent.alertException(parent, e.getStackTrace(), "An error occurred while attempting to stop the data pruner.");
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
        if (properties.getProperty("interval").equals("disabled")) {
            disabledRadio.setSelected(true);
            disabledRadioActionPerformed(null);
        } else if (properties.getProperty("interval").equals("hourly")) {
            hourlyRadio.setSelected(true);
            hourlyRadioActionPerformed(null);
        } else if (properties.getProperty("interval").equals("daily")) {
            dailyRadio.setSelected(true);
            dailyRadioActionPerformed(null);
            timeOfDay.setDate(properties.getProperty("time"));
        } else if (properties.getProperty("interval").equals("weekly")) {
            weeklyRadio.setSelected(true);
            weeklyRadioActionPerformed(null);
            dayOfWeek.setDate(properties.getProperty("dayOfWeek"));
            timeOfDayWeekly.setDate(properties.getProperty("time"));
        } else if (properties.getProperty("interval").equals("monthly")) {
            monthlyRadio.setSelected(true);
            monthlyRadioActionPerformed(null);
            dayOfMonth.setDate(properties.getProperty("dayOfMonth"));
            timeOfDayMonthly.setDate(properties.getProperty("time"));
        }

        archiverPanel.resetInvalidProperties();
//        archiverPanel.setIncludeAttachments(Boolean.parseBoolean(properties.getProperty("includeAttachments", Boolean.FALSE.toString())));
        
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

        repaint();
        updateStatus();
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        if (disabledRadio.isSelected()) {
            properties.put("interval", "disabled");
        } else if (hourlyRadio.isSelected()) {
            properties.put("interval", "hourly");
        } else if (dailyRadio.isSelected()) {
            properties.put("interval", "daily");
            properties.put("time", timeOfDay.getDate());
        } else if (weeklyRadio.isSelected()) {
            properties.put("interval", "weekly");
            properties.put("time", timeOfDayWeekly.getDate());
            properties.put("dayOfWeek", dayOfWeek.getDate());
        } else if (monthlyRadio.isSelected()) {
            properties.put("interval", "monthly");
            properties.put("time", timeOfDayMonthly.getDate());
            properties.put("dayOfMonth", dayOfMonth.getDate());
        }

        if (blockSizeTextField.getText().equals("")) {
            blockSizeTextField.setText("1000");
        }

        properties.setProperty("pruningBlockSize", blockSizeTextField.getText());
        properties.setProperty("pruneEvents", Boolean.toString(pruneEventsYes.isSelected()));
        properties.setProperty("maxEventAge", pruneEventAgeTextField.getText());
        
        properties.setProperty("archiveEnabled", Boolean.toString(archiverPanel.isArchiveEnabled()));
//        properties.put("includeAttachments", Boolean.toString(archiverPanel.isIncludeAttachments()));
        properties.setProperty("archiverOptions", serializer.serialize(archiverPanel.getMessageWriterOptions()));

        return properties;
    }
    
    private void updateStatus() {
        final String workingId = parent.startWorking("Refreshing status...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    Map<String, String> status = (Map<String, String>) parent.mirthClient.invokePluginMethod(plugin.getPluginName(), "getStatus", null);
                    currentStateTextLabel.setText(status.get("currentState"));
                    currentProcessTextLabel.setText(status.get("currentProcess"));
                    lastProcessTextLabel.setText(status.get("lastProcess"));
                    nextProcessTextLabel.setText(status.get("nextProcess"));

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
                    parent.alertException(parent, e.getStackTrace(), "An error occurred while attempting to retrieve the status of the data pruner.");
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

    // @formatter:off
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scheduleButtonGroup = new javax.swing.ButtonGroup();
        pruneEventsButtonGroup = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        archiverContainerPanel = new javax.swing.JPanel();
        pruneSettingsPanel = new javax.swing.JPanel();
        blockSizeTextField = new com.mirth.connect.client.ui.components.MirthTextField();
        blockSizeLabel = new javax.swing.JLabel();
        pruneEventsLabel = new javax.swing.JLabel();
        pruneEventsYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        pruneEventsNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        pruneEventAgeLabel = new javax.swing.JLabel();
        pruneEventAgeTextField = new com.mirth.connect.client.ui.components.MirthTextField();
        eventDaysLabel = new javax.swing.JLabel();
        pruningSchedulePanel = new javax.swing.JPanel();
        hourlyRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dailyRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        timeOfDay = new MirthTimePicker("hh:mm aa", Calendar.MINUTE);
        weeklyRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dayOfWeek = new MirthTimePicker("EEEEEEEE", Calendar.DAY_OF_WEEK);
        weeklyAtLabel = new javax.swing.JLabel();
        timeOfDayWeekly = new MirthTimePicker("hh:mm aa", Calendar.MINUTE);
        monthlyRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dayOfMonth = new MirthTimePicker("dd", Calendar.MONTH);
        monthlyAtLabel = new javax.swing.JLabel();
        timeOfDayMonthly = new MirthTimePicker("hh:mm aa", Calendar.MINUTE);
        disabledRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        statusPanel = new javax.swing.JPanel();
        currentProcessLabel = new javax.swing.JLabel();
        nextProcessLabel = new javax.swing.JLabel();
        lastProcessLabel = new javax.swing.JLabel();
        lastProcessTextLabel = new javax.swing.JLabel();
        nextProcessTextLabel = new javax.swing.JLabel();
        currentStateTextLabel = new javax.swing.JLabel();
        currentProcessTextLabel = new javax.swing.JLabel();
        currentStateLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setBorder(null);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        archiverContainerPanel.setBackground(new java.awt.Color(255, 255, 255));
        archiverContainerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Archive Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        archiverContainerPanel.setLayout(null);

        pruneSettingsPanel.setBackground(new java.awt.Color(255, 255, 255));
        pruneSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Prune Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        blockSizeTextField.setToolTipText("<html>If this number is 0, all messages are pruned in a single query. If the single query is slowing down<br>the system for too long, messages can be pruned in blocks of the specified size. Block pruning can<br>be a much longer process, but it will not slow down the system as much as a single query.</html>");

        blockSizeLabel.setText("Block Size:");

        pruneEventsLabel.setText("Prune Events:");

        pruneEventsYes.setBackground(new java.awt.Color(255, 255, 255));
        pruneEventsButtonGroup.add(pruneEventsYes);
        pruneEventsYes.setText("Yes");
        pruneEventsYes.setToolTipText("<html>If Yes, event records older than the Event Age will be pruned. If No, event records will not be pruned.</html>");
        pruneEventsYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pruneEventsYesActionPerformed(evt);
            }
        });

        pruneEventsNo.setBackground(new java.awt.Color(255, 255, 255));
        pruneEventsButtonGroup.add(pruneEventsNo);
        pruneEventsNo.setText("No");
        pruneEventsNo.setToolTipText("<html>If Yes, event records will be pruned in addition to messages. If No, event records will not be pruned.</html>");
        pruneEventsNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pruneEventsNoActionPerformed(evt);
            }
        });

        pruneEventAgeLabel.setText("Prune Event Age:");

        pruneEventAgeTextField.setToolTipText("<html>Events older than this number of days will be pruned if Prune Events is set to Yes.</html>");

        eventDaysLabel.setText("days");

        javax.swing.GroupLayout pruneSettingsPanelLayout = new javax.swing.GroupLayout(pruneSettingsPanel);
        pruneSettingsPanel.setLayout(pruneSettingsPanelLayout);
        pruneSettingsPanelLayout.setHorizontalGroup(
            pruneSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pruneSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pruneSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pruneEventAgeLabel)
                    .addComponent(pruneEventsLabel)
                    .addComponent(blockSizeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pruneSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pruneSettingsPanelLayout.createSequentialGroup()
                        .addComponent(pruneEventsYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pruneEventsNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pruneEventAgeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(blockSizeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eventDaysLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pruneSettingsPanelLayout.setVerticalGroup(
            pruneSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pruneSettingsPanelLayout.createSequentialGroup()
                .addGroup(pruneSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(blockSizeLabel)
                    .addComponent(blockSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pruneSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pruneEventsLabel)
                    .addComponent(pruneEventsYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pruneEventsNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pruneSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pruneEventAgeLabel)
                    .addComponent(pruneEventAgeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventDaysLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pruningSchedulePanel.setBackground(new java.awt.Color(255, 255, 255));
        pruningSchedulePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Schedule", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        hourlyRadio.setBackground(new java.awt.Color(255, 255, 255));
        hourlyRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scheduleButtonGroup.add(hourlyRadio);
        hourlyRadio.setText("Hourly");
        hourlyRadio.setToolTipText("<html>Run the data pruner hourly (on the hour, for example: 12:00am, 1:00am, 2:00am, and etc.)</html>");
        hourlyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hourlyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hourlyRadioActionPerformed(evt);
            }
        });

        dailyRadio.setBackground(new java.awt.Color(255, 255, 255));
        dailyRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scheduleButtonGroup.add(dailyRadio);
        dailyRadio.setText("Daily");
        dailyRadio.setToolTipText("<html>Run the data pruner daily at the specified time.</html>");
        dailyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dailyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dailyRadioActionPerformed(evt);
            }
        });

        weeklyRadio.setBackground(new java.awt.Color(255, 255, 255));
        weeklyRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scheduleButtonGroup.add(weeklyRadio);
        weeklyRadio.setText("Weekly");
        weeklyRadio.setToolTipText("<html>Run the data pruner weekly at the specified time.</html>");
        weeklyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        weeklyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                weeklyRadioActionPerformed(evt);
            }
        });

        weeklyAtLabel.setText("at");

        monthlyRadio.setBackground(new java.awt.Color(255, 255, 255));
        monthlyRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scheduleButtonGroup.add(monthlyRadio);
        monthlyRadio.setText("Monthly");
        monthlyRadio.setToolTipText("<html>Run the data pruner monthly at the specified time.</html>");
        monthlyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        monthlyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monthlyRadioActionPerformed(evt);
            }
        });

        monthlyAtLabel.setText("at");

        disabledRadio.setBackground(new java.awt.Color(255, 255, 255));
        disabledRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scheduleButtonGroup.add(disabledRadio);
        disabledRadio.setText("Disabled");
        disabledRadio.setToolTipText("<html>Disables data pruning and archiving.</html>");
        disabledRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        disabledRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disabledRadioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pruningSchedulePanelLayout = new javax.swing.GroupLayout(pruningSchedulePanel);
        pruningSchedulePanel.setLayout(pruningSchedulePanelLayout);
        pruningSchedulePanelLayout.setHorizontalGroup(
            pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
                        .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hourlyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dailyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(weeklyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(monthlyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(dayOfWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(dayOfMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(timeOfDay, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
                                .addComponent(monthlyAtLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeOfDayMonthly, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pruningSchedulePanelLayout.createSequentialGroup()
                                .addComponent(weeklyAtLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeOfDayWeekly, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(disabledRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(529, Short.MAX_VALUE))
        );
        pruningSchedulePanelLayout.setVerticalGroup(
            pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(disabledRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(hourlyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dailyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeOfDay, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(weeklyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(weeklyAtLabel)
                    .addComponent(timeOfDayWeekly, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dayOfWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(monthlyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(monthlyAtLabel)
                    .addComponent(timeOfDayMonthly, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dayOfMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        statusPanel.setBackground(new java.awt.Color(255, 255, 255));
        statusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Status", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        currentProcessLabel.setText("Current Process:");

        nextProcessLabel.setText("Next Process:");

        lastProcessLabel.setText("Last Process:");

        currentStateTextLabel.setText("Unknown");

        currentStateLabel.setText("Current State:");

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nextProcessLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lastProcessLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(currentProcessLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(currentStateLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(currentStateTextLabel)
                    .addComponent(nextProcessTextLabel)
                    .addComponent(lastProcessTextLabel)
                    .addComponent(currentProcessTextLabel))
                .addGap(0, 638, Short.MAX_VALUE))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(currentStateTextLabel)
                    .addComponent(currentStateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(currentProcessLabel)
                    .addComponent(currentProcessTextLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lastProcessLabel)
                    .addComponent(lastProcessTextLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nextProcessLabel)
                    .addComponent(nextProcessTextLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(archiverContainerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)
                    .addComponent(pruningSchedulePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pruneSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pruningSchedulePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pruneSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(archiverContainerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                .addContainerGap())
        );

        jScrollPane1.setViewportView(jPanel1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // @formatter:on

    private void hourlyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hourlyRadioActionPerformed
        monthlyAtLabel.setEnabled(false);
        weeklyAtLabel.setEnabled(false);

        dayOfMonth.setEnabled(false);
        dayOfWeek.setEnabled(false);
        timeOfDay.setEnabled(false);
        timeOfDayWeekly.setEnabled(false);
        timeOfDayMonthly.setEnabled(false);
    }//GEN-LAST:event_hourlyRadioActionPerformed

    private void dailyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dailyRadioActionPerformed
        monthlyAtLabel.setEnabled(false);
        weeklyAtLabel.setEnabled(false);

        dayOfMonth.setEnabled(false);
        dayOfWeek.setEnabled(false);
        timeOfDay.setEnabled(true);
        timeOfDayWeekly.setEnabled(false);
        timeOfDayMonthly.setEnabled(false);
    }//GEN-LAST:event_dailyRadioActionPerformed

    private void weeklyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_weeklyRadioActionPerformed
        monthlyAtLabel.setEnabled(false);
        weeklyAtLabel.setEnabled(true);

        dayOfMonth.setEnabled(false);
        dayOfWeek.setEnabled(true);
        timeOfDay.setEnabled(false);
        timeOfDayWeekly.setEnabled(true);
        timeOfDayMonthly.setEnabled(false);
    }//GEN-LAST:event_weeklyRadioActionPerformed

    private void monthlyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monthlyRadioActionPerformed
        monthlyAtLabel.setEnabled(true);
        weeklyAtLabel.setEnabled(false);

        dayOfMonth.setEnabled(true);
        dayOfWeek.setEnabled(false);
        timeOfDay.setEnabled(false);
        timeOfDayWeekly.setEnabled(false);
        timeOfDayMonthly.setEnabled(true);
    }//GEN-LAST:event_monthlyRadioActionPerformed

    private void disabledRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disabledRadioActionPerformed
        monthlyAtLabel.setEnabled(false);
        weeklyAtLabel.setEnabled(false);
        dayOfMonth.setEnabled(false);
        dayOfWeek.setEnabled(false);
        timeOfDay.setEnabled(false);
        timeOfDayWeekly.setEnabled(false);
        timeOfDayMonthly.setEnabled(false);
    }//GEN-LAST:event_disabledRadioActionPerformed

    private void pruneEventsYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pruneEventsYesActionPerformed
        pruneEventAgeLabel.setEnabled(true);
        pruneEventAgeTextField.setEnabled(true);
    }//GEN-LAST:event_pruneEventsYesActionPerformed

    private void pruneEventsNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pruneEventsNoActionPerformed
        pruneEventAgeLabel.setEnabled(false);
        pruneEventAgeTextField.setEnabled(false);
    }//GEN-LAST:event_pruneEventsNoActionPerformed
    
    private void setStartTaskVisible(boolean visible) {
        setVisibleTasks(startIndex, startIndex, visible);
    }
    
    private void setStopTaskVisible(boolean visible) {
        setVisibleTasks(stopIndex, stopIndex, visible);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel archiverContainerPanel;
    private javax.swing.JLabel blockSizeLabel;
    private com.mirth.connect.client.ui.components.MirthTextField blockSizeTextField;
    private javax.swing.JLabel currentProcessLabel;
    private javax.swing.JLabel currentProcessTextLabel;
    private javax.swing.JLabel currentStateLabel;
    private javax.swing.JLabel currentStateTextLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton dailyRadio;
    private com.mirth.connect.client.ui.components.MirthTimePicker dayOfMonth;
    private com.mirth.connect.client.ui.components.MirthTimePicker dayOfWeek;
    private com.mirth.connect.client.ui.components.MirthRadioButton disabledRadio;
    private javax.swing.JLabel eventDaysLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton hourlyRadio;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lastProcessLabel;
    private javax.swing.JLabel lastProcessTextLabel;
    private javax.swing.JLabel monthlyAtLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton monthlyRadio;
    private javax.swing.JLabel nextProcessLabel;
    private javax.swing.JLabel nextProcessTextLabel;
    private javax.swing.JLabel pruneEventAgeLabel;
    private com.mirth.connect.client.ui.components.MirthTextField pruneEventAgeTextField;
    private javax.swing.ButtonGroup pruneEventsButtonGroup;
    private javax.swing.JLabel pruneEventsLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton pruneEventsNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton pruneEventsYes;
    private javax.swing.JPanel pruneSettingsPanel;
    private javax.swing.JPanel pruningSchedulePanel;
    private javax.swing.ButtonGroup scheduleButtonGroup;
    private javax.swing.JPanel statusPanel;
    private com.mirth.connect.client.ui.components.MirthTimePicker timeOfDay;
    private com.mirth.connect.client.ui.components.MirthTimePicker timeOfDayMonthly;
    private com.mirth.connect.client.ui.components.MirthTimePicker timeOfDayWeekly;
    private javax.swing.JLabel weeklyAtLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton weeklyRadio;
    // End of variables declaration//GEN-END:variables
}
