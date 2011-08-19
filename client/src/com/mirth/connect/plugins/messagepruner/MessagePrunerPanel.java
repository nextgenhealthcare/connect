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

        initComponents();
        pruningBlockSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
    }

    @Override
    public void doRefresh() {
        getFrame().setWorking("Loading " + getTabName() + " properties...", true);

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
                getFrame().setWorking("", false);
            }
        };

        worker.execute();
    }

    @Override
    public void doSave() {
        getFrame().setWorking("Saving " + getTabName() + " properties...", true);

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
                getFrame().setWorking("", false);
            }
        };

        worker.execute();
    }

    public void setProperties(Properties properties) {
        if (properties.getProperty("interval").equals("hourly")) {
            hourButton.setSelected(true);
            hourButtonActionPerformed(null);
        } else if (properties.getProperty("interval").equals("daily")) {
            dayButton.setSelected(true);
            dayButtonActionPerformed(null);
            timeOfDay.setDate(properties.getProperty("time"));
        } else if (properties.getProperty("interval").equals("weekly")) {
            weekButton.setSelected(true);
            weekButtonActionPerformed(null);
            dayOfWeek.setDate(properties.getProperty("dayOfWeek"));
            timeOfDayWeekly.setDate(properties.getProperty("time"));
        } else if (properties.getProperty("interval").equals("monthly")) {
            monthButton.setSelected(true);
            monthButtonActionPerformed(null);
            dayOfMonth.setDate(properties.getProperty("dayOfMonth"));
            timeOfDayMonthly.setDate(properties.getProperty("time"));
        }

        if (properties.getProperty("allowBatchPruning") != null && properties.getProperty("allowBatchPruning").equals(UIConstants.YES_OPTION)) {
            batchYes.setSelected(true);
        } else {
            batchNo.setSelected(true);
        }

        if (properties.getProperty("pruningBlockSize") != null && !properties.getProperty("pruningBlockSize").equals("")) {
            pruningBlockSizeField.setText(properties.getProperty("pruningBlockSize"));
        } else {
            pruningBlockSizeField.setText("1000");
        }

        repaint();
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        if (hourButton.isSelected()) {
            properties.put("interval", "hourly");
        } else if (dayButton.isSelected()) {
            properties.put("interval", "daily");
            properties.put("time", timeOfDay.getDate());
        } else if (weekButton.isSelected()) {
            properties.put("interval", "weekly");
            properties.put("time", timeOfDayWeekly.getDate());
            properties.put("dayOfWeek", dayOfWeek.getDate());
        } else if (monthButton.isSelected()) {
            properties.put("interval", "monthly");
            properties.put("time", timeOfDayMonthly.getDate());
            properties.put("dayOfMonth", dayOfMonth.getDate());
        }

        if (batchYes.isSelected()) {
            properties.put("allowBatchPruning", UIConstants.YES_OPTION);
        } else {
            properties.put("allowBatchPruning", UIConstants.NO_OPTION);
        }

        if (pruningBlockSizeField.getText().equals("")) {
            pruningBlockSizeField.setText("1000");
        }
        properties.put("pruningBlockSize", pruningBlockSizeField.getText());

        return properties;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        dayButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        weekButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        monthButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        hourButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        whenToPruneLabel = new javax.swing.JLabel();
        dayOfWeekLabel = new javax.swing.JLabel();
        dayOfMonthLabel = new javax.swing.JLabel();
        timeOfDay = new MirthTimePicker("hh:mm aa", Calendar.MINUTE);
        timeOfDayLabel = new javax.swing.JLabel();
        dayOfMonth = new MirthTimePicker("dd", Calendar.MONTH);
        timeOfDayWeeklyLabel = new javax.swing.JLabel();
        timeOfDayWeekly = new MirthTimePicker("hh:mm aa", Calendar.MINUTE);
        timeOfDayMonthlyLabel = new javax.swing.JLabel();
        timeOfDayMonthly = new MirthTimePicker("hh:mm aa", Calendar.MINUTE);
        weeklyAtLabel = new javax.swing.JLabel();
        monthlyAtLabel = new javax.swing.JLabel();
        dayOfWeek = new MirthTimePicker("EEEEEEEE", Calendar.DAY_OF_WEEK);
        batchYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        enableBatchPruningLabel = new javax.swing.JLabel();
        batchNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        pruningBlockSizeLabel = new javax.swing.JLabel();
        pruningBlockSizeField = new com.mirth.connect.client.ui.components.MirthTextField();

        setBackground(new java.awt.Color(255, 255, 255));

        dayButton.setBackground(new java.awt.Color(255, 255, 255));
        dayButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(dayButton);
        dayButton.setText("Daily");
        dayButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dayButtonActionPerformed(evt);
            }
        });

        weekButton.setBackground(new java.awt.Color(255, 255, 255));
        weekButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(weekButton);
        weekButton.setText("Weekly");
        weekButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        weekButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                weekButtonActionPerformed(evt);
            }
        });

        monthButton.setBackground(new java.awt.Color(255, 255, 255));
        monthButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(monthButton);
        monthButton.setText("Monthly");
        monthButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        monthButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monthButtonActionPerformed(evt);
            }
        });

        hourButton.setBackground(new java.awt.Color(255, 255, 255));
        hourButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(hourButton);
        hourButton.setText("Hourly");
        hourButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hourButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hourButtonActionPerformed(evt);
            }
        });

        whenToPruneLabel.setText("When to prune:");

        dayOfWeekLabel.setText("Day of Week:");

        dayOfMonthLabel.setText("Day of Month:");

        timeOfDayLabel.setText("Time of Day:");

        timeOfDayWeeklyLabel.setText("Time of Day:");

        timeOfDayMonthlyLabel.setText("Time of Day:");

        weeklyAtLabel.setText("at");

        monthlyAtLabel.setText("at");

        batchYes.setBackground(new java.awt.Color(255, 255, 255));
        batchYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(batchYes);
        batchYes.setText("Yes");
        batchYes.setToolTipText("Turning batch pruning on increases message pruning performance by allowing channels with the same pruning settings to be pruned in one delete.");
        batchYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        enableBatchPruningLabel.setText("Enable Batch Pruning:");

        batchNo.setBackground(new java.awt.Color(255, 255, 255));
        batchNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(batchNo);
        batchNo.setText("No");
        batchNo.setToolTipText("Turning batch pruning off decreases message pruning performance, but displays how many messages are pruned from each individual channel.");
        batchNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        pruningBlockSizeLabel.setText("Pruning Block Size:");

        pruningBlockSizeField.setToolTipText("<html>If this number is 0, all messages are pruned in a single query. If the single query is slowing down<br>the system for too long, messages can be pruned in blocks of the specified size. Block pruning can<br>be a much longer process, but it will not slow down the system as much as a single query.</html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(whenToPruneLabel)
                    .addComponent(hourButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dayButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(weekButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(monthButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(dayOfMonthLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(timeOfDay, javax.swing.GroupLayout.Alignment.LEADING, 0, 75, Short.MAX_VALUE)
                                            .addComponent(dayOfMonth, javax.swing.GroupLayout.Alignment.LEADING, 0, 75, Short.MAX_VALUE)
                                            .addComponent(dayOfWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(monthlyAtLabel)
                                            .addComponent(weeklyAtLabel)))
                                    .addComponent(dayOfWeekLabel, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(timeOfDayWeeklyLabel)
                                    .addComponent(timeOfDayWeekly, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(timeOfDayMonthlyLabel)
                                    .addComponent(timeOfDayMonthly, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(timeOfDayLabel)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pruningBlockSizeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(enableBatchPruningLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(batchYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(batchNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(pruningBlockSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(406, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(whenToPruneLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hourButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dayButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeOfDayLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeOfDay, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(weekButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeOfDayWeeklyLabel)
                    .addComponent(dayOfWeekLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(weeklyAtLabel)
                    .addComponent(timeOfDayWeekly, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dayOfWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(monthButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dayOfMonthLabel)
                    .addComponent(timeOfDayMonthlyLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(monthlyAtLabel)
                    .addComponent(timeOfDayMonthly, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dayOfMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enableBatchPruningLabel)
                    .addComponent(batchYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(batchNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pruningBlockSizeLabel)
                    .addComponent(pruningBlockSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(170, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void hourButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hourButtonActionPerformed
        dayOfMonthLabel.setEnabled(false);
        dayOfWeekLabel.setEnabled(false);
        timeOfDayLabel.setEnabled(false);
        timeOfDayWeeklyLabel.setEnabled(false);
        timeOfDayMonthlyLabel.setEnabled(false);

        monthlyAtLabel.setEnabled(false);
        weeklyAtLabel.setEnabled(false);

        dayOfMonth.setEnabled(false);
        dayOfWeek.setEnabled(false);
        timeOfDay.setEnabled(false);
        timeOfDayWeekly.setEnabled(false);
        timeOfDayMonthly.setEnabled(false);
    }//GEN-LAST:event_hourButtonActionPerformed

    private void dayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dayButtonActionPerformed
        dayOfMonthLabel.setEnabled(false);
        dayOfWeekLabel.setEnabled(false);
        timeOfDayLabel.setEnabled(true);
        timeOfDayWeeklyLabel.setEnabled(false);
        timeOfDayMonthlyLabel.setEnabled(false);

        monthlyAtLabel.setEnabled(false);
        weeklyAtLabel.setEnabled(false);

        dayOfMonth.setEnabled(false);
        dayOfWeek.setEnabled(false);
        timeOfDay.setEnabled(true);
        timeOfDayWeekly.setEnabled(false);
        timeOfDayMonthly.setEnabled(false);
    }//GEN-LAST:event_dayButtonActionPerformed

    private void weekButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_weekButtonActionPerformed
        dayOfMonthLabel.setEnabled(false);
        dayOfWeekLabel.setEnabled(true);
        timeOfDayLabel.setEnabled(false);
        timeOfDayWeeklyLabel.setEnabled(true);
        timeOfDayMonthlyLabel.setEnabled(false);

        monthlyAtLabel.setEnabled(false);
        weeklyAtLabel.setEnabled(true);

        dayOfMonth.setEnabled(false);
        dayOfWeek.setEnabled(true);
        timeOfDay.setEnabled(false);
        timeOfDayWeekly.setEnabled(true);
        timeOfDayMonthly.setEnabled(false);
    }//GEN-LAST:event_weekButtonActionPerformed

    private void monthButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monthButtonActionPerformed
        dayOfMonthLabel.setEnabled(true);
        dayOfWeekLabel.setEnabled(false);
        timeOfDayLabel.setEnabled(false);
        timeOfDayWeeklyLabel.setEnabled(false);
        timeOfDayMonthlyLabel.setEnabled(true);

        monthlyAtLabel.setEnabled(true);
        weeklyAtLabel.setEnabled(false);

        dayOfMonth.setEnabled(true);
        dayOfWeek.setEnabled(false);
        timeOfDay.setEnabled(false);
        timeOfDayWeekly.setEnabled(false);
        timeOfDayMonthly.setEnabled(true);
    }//GEN-LAST:event_monthButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthRadioButton batchNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton batchYes;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private com.mirth.connect.client.ui.components.MirthRadioButton dayButton;
    private com.mirth.connect.client.ui.components.MirthTimePicker dayOfMonth;
    private javax.swing.JLabel dayOfMonthLabel;
    private com.mirth.connect.client.ui.components.MirthTimePicker dayOfWeek;
    private javax.swing.JLabel dayOfWeekLabel;
    private javax.swing.JLabel enableBatchPruningLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton hourButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton monthButton;
    private javax.swing.JLabel monthlyAtLabel;
    private com.mirth.connect.client.ui.components.MirthTextField pruningBlockSizeField;
    private javax.swing.JLabel pruningBlockSizeLabel;
    private com.mirth.connect.client.ui.components.MirthTimePicker timeOfDay;
    private javax.swing.JLabel timeOfDayLabel;
    private com.mirth.connect.client.ui.components.MirthTimePicker timeOfDayMonthly;
    private javax.swing.JLabel timeOfDayMonthlyLabel;
    private com.mirth.connect.client.ui.components.MirthTimePicker timeOfDayWeekly;
    private javax.swing.JLabel timeOfDayWeeklyLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton weekButton;
    private javax.swing.JLabel weeklyAtLabel;
    private javax.swing.JLabel whenToPruneLabel;
    // End of variables declaration//GEN-END:variables
}
