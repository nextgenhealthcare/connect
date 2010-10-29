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
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.CenterCellRenderer;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTimePicker;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class MessagePrunerPanel extends javax.swing.JPanel {

    private final String NAME_COLUMN_NAME = "Channel";
    private final String TIME_COLUMN_NAME = "Time Pruned";
    private final String NUMBER_COLUMN_NAME = "Messages Pruned";
    private ObjectXMLSerializer serializer = new ObjectXMLSerializer();

    /**
     * Creates new form MessagePrunerPanel
     */
    public MessagePrunerPanel() {
        initComponents();
        pruningBlockSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        makeLogTable();
    }

    public void setProperties(Properties properties, List<String[]> log) {
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

        updateTable(log);
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

    /**
     * Makes the status table with all current server information.
     */
    public void makeLogTable() {
        updateTable(null);

        logTable.setDoubleBuffered(true);

        logTable.setSelectionMode(0);

        logTable.getColumnExt(NUMBER_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());

        logTable.packTable(UIConstants.COL_MARGIN);

        logTable.setRowHeight(UIConstants.ROW_HEIGHT);
        logTable.setOpaque(true);
        logTable.setRowSelectionAllowed(false);

        logTable.setSortable(true);

        logPane.setViewportView(logTable);
    }

    public void updateTable(List<String[]> logs) {
        Object[][] tableData = null;

        if (logs != null) {
            tableData = new Object[logs.size()][3];
            for (int i = 0; i < logs.size(); i++) {
                tableData[i][0] = logs.get(i)[0];
                tableData[i][1] = logs.get(i)[1];
                tableData[i][2] = logs.get(i)[2];
            }
        }

        if (logTable != null) {
            RefreshTableModel model = (RefreshTableModel) logTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            logTable = new MirthTable();
            logTable.setModel(new RefreshTableModel(tableData, new String[]{NAME_COLUMN_NAME, TIME_COLUMN_NAME, NUMBER_COLUMN_NAME}) {

                boolean[] canEdit = new boolean[]{false, false, false};

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            logTable.setHighlighters(highlighter);
        }

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
        jPanel1 = new javax.swing.JPanel();
        dayButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        weekButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        monthButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        hourButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel1 = new javax.swing.JLabel();
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
        jLabel2 = new javax.swing.JLabel();
        batchNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        pruningBlockSizeLabel = new javax.swing.JLabel();
        pruningBlockSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        jPanel2 = new javax.swing.JPanel();
        logPane = new javax.swing.JScrollPane();
        logTable = null;

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

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

        jLabel1.setText("When to prune:");

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
        batchYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batchYesActionPerformed(evt);
            }
        });

        jLabel2.setText("Enable Batch Pruning:");

        batchNo.setBackground(new java.awt.Color(255, 255, 255));
        batchNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(batchNo);
        batchNo.setText("No");
        batchNo.setToolTipText("Turning batch pruning off decreases message pruning performance, but displays how many messages are pruned from each individual channel.");
        batchNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        batchNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batchNoActionPerformed(evt);
            }
        });

        pruningBlockSizeLabel.setText("Pruning Block Size:");

        pruningBlockSizeField.setToolTipText("The number of messages to be pruned in each delete block. Increase this size for high performance systems with large message volumes, or set it to 0 to delete all messages in a single query.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(hourButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dayButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(weekButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(monthButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(dayOfMonthLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(timeOfDay, javax.swing.GroupLayout.Alignment.LEADING, 0, 75, Short.MAX_VALUE)
                                            .addComponent(dayOfMonth, javax.swing.GroupLayout.Alignment.LEADING, 0, 75, Short.MAX_VALUE)
                                            .addComponent(dayOfWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(monthlyAtLabel)
                                            .addComponent(weeklyAtLabel)))
                                    .addComponent(dayOfWeekLabel, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(timeOfDayWeeklyLabel)
                                    .addComponent(timeOfDayWeekly, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(timeOfDayMonthlyLabel)
                                    .addComponent(timeOfDayMonthly, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(timeOfDayLabel)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pruningBlockSizeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(batchYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(batchNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(pruningBlockSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeOfDayWeeklyLabel)
                    .addComponent(dayOfWeekLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(weeklyAtLabel)
                    .addComponent(timeOfDayWeekly, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dayOfWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(monthButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dayOfMonthLabel)
                    .addComponent(timeOfDayMonthlyLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(monthlyAtLabel)
                    .addComponent(timeOfDayMonthly, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dayOfMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(batchYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(batchNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pruningBlockSizeLabel)
                    .addComponent(pruningBlockSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Activity Log", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        logPane.setViewportView(logTable);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logPane, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(logPane, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void hourButtonActionPerformed(java.awt.event.ActionEvent evt) {
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
    }

    private void dayButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dayButtonActionPerformed
    {//GEN-HEADEREND:event_dayButtonActionPerformed
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

private void batchYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_batchYesActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_batchYesActionPerformed

private void batchNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_batchNoActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_batchNoActionPerformed

    private void weekButtonActionPerformed(java.awt.event.ActionEvent evt) {
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
    }

    private void monthButtonActionPerformed(java.awt.event.ActionEvent evt) {
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
    }
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
    private com.mirth.connect.client.ui.components.MirthRadioButton hourButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane logPane;
    private com.mirth.connect.client.ui.components.MirthTable logTable;
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
    // End of variables declaration//GEN-END:variables
}
