/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagepruner;

import java.util.Calendar;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTimePicker;
import com.mirth.connect.plugins.SettingsPanelPlugin;

public class MessagePrunerPanel extends AbstractSettingsPanel {

    private SettingsPanelPlugin plugin = null;

    public MessagePrunerPanel(String tabName, SettingsPanelPlugin plugin) {
        super(tabName);
        this.plugin = plugin;
        
        addTask("doViewEvents", "View Events", "View the Message Pruner events.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/table.png")));

        initComponents();
        blockSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
    }

    @Override
    public void doRefresh() {
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
                } catch (ClientException e) {
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

    @Override
    public void doSave() {
        final String workingId = getFrame().startWorking("Saving " + getTabName() + " properties...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    plugin.setPropertiesToServer(getProperties());
                } catch (ClientException e) {
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            @Override
            public void done() {
                setSaveEnabled(false);
                getFrame().stopWorking(workingId);
            }
        };

        worker.execute();
    }
    
    public void doViewEvents() {
        getFrame().doShowEvents("Message Pruner");
    }

    public void setProperties(Properties properties) {
        if (properties.getProperty("interval").equals("hourly")) {
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

        if (properties.getProperty("allowBatchPruning") != null && properties.getProperty("allowBatchPruning").equals(UIConstants.YES_OPTION)) {
            batchPruningYesRadio.setSelected(true);
        } else {
            batchPruningNoRadio.setSelected(true);
        }

        if (properties.getProperty("pruningBlockSize") != null && !properties.getProperty("pruningBlockSize").equals("")) {
            blockSizeField.setText(properties.getProperty("pruningBlockSize"));
        } else {
            blockSizeField.setText("1000");
        }

        repaint();
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        if (hourlyRadio.isSelected()) {
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

        if (batchPruningYesRadio.isSelected()) {
            properties.put("allowBatchPruning", UIConstants.YES_OPTION);
        } else {
            properties.put("allowBatchPruning", UIConstants.NO_OPTION);
        }

        if (blockSizeField.getText().equals("")) {
            blockSizeField.setText("1000");
        }
        properties.put("pruningBlockSize", blockSizeField.getText());

        return properties;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scheduleButtonGroup = new javax.swing.ButtonGroup();
        batchPruningButtonGroup = new javax.swing.ButtonGroup();
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
        batchPruningLabel = new javax.swing.JLabel();
        batchPruningYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        batchPruningNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        blockSizeLabel = new javax.swing.JLabel();
        blockSizeField = new com.mirth.connect.client.ui.components.MirthTextField();

        setBackground(new java.awt.Color(255, 255, 255));

        pruningSchedulePanel.setBackground(new java.awt.Color(255, 255, 255));
        pruningSchedulePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Pruning Schedule", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        hourlyRadio.setBackground(new java.awt.Color(255, 255, 255));
        hourlyRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scheduleButtonGroup.add(hourlyRadio);
        hourlyRadio.setText("Hourly");
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
        monthlyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        monthlyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monthlyRadioActionPerformed(evt);
            }
        });

        monthlyAtLabel.setText("at");

        batchPruningLabel.setText("Batch Pruning:");

        batchPruningYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        batchPruningYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        batchPruningButtonGroup.add(batchPruningYesRadio);
        batchPruningYesRadio.setText("Yes");
        batchPruningYesRadio.setToolTipText("Turning batch pruning on increases message pruning performance by allowing channels with the same pruning settings to be pruned in one delete.");
        batchPruningYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        batchPruningNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        batchPruningNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        batchPruningButtonGroup.add(batchPruningNoRadio);
        batchPruningNoRadio.setText("No");
        batchPruningNoRadio.setToolTipText("Turning batch pruning off decreases message pruning performance, but displays how many messages are pruned from each individual channel.");
        batchPruningNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        blockSizeLabel.setText("Block Size:");

        blockSizeField.setToolTipText("<html>If this number is 0, all messages are pruned in a single query. If the single query is slowing down<br>the system for too long, messages can be pruned in blocks of the specified size. Block pruning can<br>be a much longer process, but it will not slow down the system as much as a single query.</html>");

        javax.swing.GroupLayout pruningSchedulePanelLayout = new javax.swing.GroupLayout(pruningSchedulePanel);
        pruningSchedulePanel.setLayout(pruningSchedulePanelLayout);
        pruningSchedulePanelLayout.setHorizontalGroup(
            pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
                .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hourlyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dailyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(weeklyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(monthlyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(dayOfWeek, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                            .addComponent(dayOfMonth, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                            .addComponent(timeOfDay, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))
                        .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(monthlyAtLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeOfDayMonthly, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pruningSchedulePanelLayout.createSequentialGroup()
                                .addComponent(weeklyAtLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeOfDayWeekly, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(blockSizeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(batchPruningLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
                                .addComponent(batchPruningYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(batchPruningNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(blockSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        pruningSchedulePanelLayout.setVerticalGroup(
            pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(batchPruningLabel)
                    .addComponent(batchPruningYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(batchPruningNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(blockSizeLabel)
                    .addComponent(blockSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pruningSchedulePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pruningSchedulePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup batchPruningButtonGroup;
    private javax.swing.JLabel batchPruningLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton batchPruningNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton batchPruningYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField blockSizeField;
    private javax.swing.JLabel blockSizeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton dailyRadio;
    private com.mirth.connect.client.ui.components.MirthTimePicker dayOfMonth;
    private com.mirth.connect.client.ui.components.MirthTimePicker dayOfWeek;
    private com.mirth.connect.client.ui.components.MirthRadioButton hourlyRadio;
    private javax.swing.JLabel monthlyAtLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton monthlyRadio;
    private javax.swing.JPanel pruningSchedulePanel;
    private javax.swing.ButtonGroup scheduleButtonGroup;
    private com.mirth.connect.client.ui.components.MirthTimePicker timeOfDay;
    private com.mirth.connect.client.ui.components.MirthTimePicker timeOfDayMonthly;
    private com.mirth.connect.client.ui.components.MirthTimePicker timeOfDayWeekly;
    private javax.swing.JLabel weeklyAtLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton weeklyRadio;
    // End of variables declaration//GEN-END:variables
}
