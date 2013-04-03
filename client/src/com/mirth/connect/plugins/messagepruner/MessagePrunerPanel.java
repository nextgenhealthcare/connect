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

import net.miginfocom.swing.MigLayout;

import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTimePicker;
import com.mirth.connect.client.ui.panels.export.MessageExportPanel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.SettingsPanelPlugin;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class MessagePrunerPanel extends AbstractSettingsPanel {
    private SettingsPanelPlugin plugin = null;
    private MessageExportPanel archiverPanel;
    private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
    private Frame parent;

    public MessagePrunerPanel(String tabName, SettingsPanelPlugin plugin) {
        super(tabName);
        this.plugin = plugin;
        this.parent = PlatformUI.MIRTH_FRAME;

        addTask("doViewEvents", "View Events", "View the Message Pruner events.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/table.png")));

        initComponents();
        blockSizeTextField.setDocument(new MirthFieldConstraints(0, false, false, true));

        /*
         * The archiver panel uses MigLayout, so we attach it to a container panel that is part of
         * the netbeans-generated layout.
         */
        archiverPanel = new MessageExportPanel(Frame.userPreferences, true, false);
        archiverPanel.setBackground(archiverContainerPanel.getBackground());
        archiverContainerPanel.setLayout(new MigLayout("fillx, insets 0 0 0 0", "[grow,fill]", "[grow,fill]"));
        archiverContainerPanel.add(archiverPanel, "height 150!, aligny top");
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

    @Override
    public void doSave() {
        archiverPanel.resetInvalidProperties();
        
        if (!archiverPanel.validate(true)) {
            parent.alertError(this, "Please fill in required fields.");
            return;
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
            }
        };

        worker.execute();
    }

    public void doViewEvents() {
        getFrame().doShowEvents("Message Pruner");
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
        archiverPanel.setMessageWriterOptions((MessageWriterOptions) serializer.fromXML(properties.getProperty("archiverOptions")));

        if (archiverPanel.isEnabled()) {
            archiverPanel.setArchiveEnabled(Boolean.parseBoolean(properties.getProperty("archiveEnabled", Boolean.FALSE.toString())));
        }
        
        if (properties.getProperty("pruningBlockSize") != null && !properties.getProperty("pruningBlockSize").equals("")) {
            blockSizeTextField.setText(properties.getProperty("pruningBlockSize"));
        } else {
            blockSizeTextField.setText("1000");
        }

        repaint();
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
        properties.setProperty("archiveEnabled", Boolean.toString(archiverPanel.isArchiveEnabled()));
//        properties.put("includeAttachments", Boolean.toString(archiverPanel.isIncludeAttachments()));
        properties.setProperty("archiverOptions", serializer.serialize(archiverPanel.getMessageWriterOptions()));

        return properties;
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
        blockSizeLabel = new javax.swing.JLabel();
        blockSizeTextField = new com.mirth.connect.client.ui.components.MirthTextField();
        archiverContainerPanel = new javax.swing.JPanel();

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

        disabledRadio.setBackground(new java.awt.Color(255, 255, 255));
        disabledRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scheduleButtonGroup.add(disabledRadio);
        disabledRadio.setText("Disabled");
        disabledRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        disabledRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disabledRadioActionPerformed(evt);
            }
        });

        blockSizeLabel.setText("Block Size:");

        blockSizeTextField.setToolTipText("<html>If this number is 0, all messages are pruned in a single query. If the single query is slowing down<br>the system for too long, messages can be pruned in blocks of the specified size. Block pruning can<br>be a much longer process, but it will not slow down the system as much as a single query.</html>");

        javax.swing.GroupLayout pruningSchedulePanelLayout = new javax.swing.GroupLayout(pruningSchedulePanel);
        pruningSchedulePanel.setLayout(pruningSchedulePanelLayout);
        pruningSchedulePanelLayout.setHorizontalGroup(
            pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(disabledRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
                        .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hourlyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dailyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(weeklyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(monthlyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(blockSizeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(dayOfWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(dayOfMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(timeOfDay, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(blockSizeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pruningSchedulePanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(monthlyAtLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeOfDayMonthly, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pruningSchedulePanelLayout.createSequentialGroup()
                                .addComponent(weeklyAtLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeOfDayWeekly, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(544, Short.MAX_VALUE))
        );
        pruningSchedulePanelLayout.setVerticalGroup(
            pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pruningSchedulePanelLayout.createSequentialGroup()
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pruningSchedulePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(blockSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(blockSizeLabel))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        archiverContainerPanel.setBackground(new java.awt.Color(255, 255, 255));
        archiverContainerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Archiving", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        archiverContainerPanel.setLayout(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pruningSchedulePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(archiverContainerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pruningSchedulePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(archiverContainerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                .addContainerGap())
        );
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
        
        blockSizeLabel.setEnabled(true);
        blockSizeTextField.setEnabled(true);
        
        archiverPanel.setEnabled(true);
    }//GEN-LAST:event_hourlyRadioActionPerformed

    private void dailyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dailyRadioActionPerformed
        monthlyAtLabel.setEnabled(false);
        weeklyAtLabel.setEnabled(false);

        dayOfMonth.setEnabled(false);
        dayOfWeek.setEnabled(false);
        timeOfDay.setEnabled(true);
        timeOfDayWeekly.setEnabled(false);
        timeOfDayMonthly.setEnabled(false);
        
        blockSizeLabel.setEnabled(true);
        blockSizeTextField.setEnabled(true);
        
        archiverPanel.setEnabled(true);
    }//GEN-LAST:event_dailyRadioActionPerformed

    private void weeklyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_weeklyRadioActionPerformed
        monthlyAtLabel.setEnabled(false);
        weeklyAtLabel.setEnabled(true);

        dayOfMonth.setEnabled(false);
        dayOfWeek.setEnabled(true);
        timeOfDay.setEnabled(false);
        timeOfDayWeekly.setEnabled(true);
        timeOfDayMonthly.setEnabled(false);
        
        blockSizeLabel.setEnabled(true);
        blockSizeTextField.setEnabled(true);
        
        archiverPanel.setEnabled(true);
    }//GEN-LAST:event_weeklyRadioActionPerformed

    private void monthlyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monthlyRadioActionPerformed
        monthlyAtLabel.setEnabled(true);
        weeklyAtLabel.setEnabled(false);

        dayOfMonth.setEnabled(true);
        dayOfWeek.setEnabled(false);
        timeOfDay.setEnabled(false);
        timeOfDayWeekly.setEnabled(false);
        timeOfDayMonthly.setEnabled(true);
        
        blockSizeLabel.setEnabled(true);
        blockSizeTextField.setEnabled(true);
        
        archiverPanel.setEnabled(true);
    }//GEN-LAST:event_monthlyRadioActionPerformed

    private void disabledRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disabledRadioActionPerformed
        monthlyAtLabel.setEnabled(false);
        weeklyAtLabel.setEnabled(false);
        dayOfMonth.setEnabled(false);
        dayOfWeek.setEnabled(false);
        timeOfDay.setEnabled(false);
        timeOfDayWeekly.setEnabled(false);
        timeOfDayMonthly.setEnabled(false);
        archiverPanel.setEnabled(false);
        blockSizeLabel.setEnabled(false);
        blockSizeTextField.setEnabled(false);
    }//GEN-LAST:event_disabledRadioActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel archiverContainerPanel;
    private javax.swing.JLabel blockSizeLabel;
    private com.mirth.connect.client.ui.components.MirthTextField blockSizeTextField;
    private com.mirth.connect.client.ui.components.MirthRadioButton dailyRadio;
    private com.mirth.connect.client.ui.components.MirthTimePicker dayOfMonth;
    private com.mirth.connect.client.ui.components.MirthTimePicker dayOfWeek;
    private com.mirth.connect.client.ui.components.MirthRadioButton disabledRadio;
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
