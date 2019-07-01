/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.connectors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DateFormatter;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.MirthTimePicker;
import com.mirth.connect.donkey.model.channel.CronProperty;
import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesAdvanced;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.PollingType;
import com.mirth.connect.donkey.util.PollConnectorJobHandler;

public class PollingSettingsPanel extends JPanel {
    private static String POLLING_FREQUENCY_MILLISECONDS = "milliseconds";
    private static String POLLING_FREQUENCY_SECONDS = "seconds";
    private static String POLLING_FREQUENCY_MINUTES = "minutes";
    private static String POLLING_FREQUENCY_HOURS = "hours";
    private static String defaultCronJob = "*/5 * * * * ?";

    private String lastSelectedPollingType;

    private PollConnectorProperties nextFireTimeProperties;
    private PollConnectorPropertiesAdvanced cachedAdvancedConnectorProperties;

    private boolean channelContext;
    private Set<String> invalidExpressions;

    public PollingSettingsPanel(boolean channelContext) {
        this.channelContext = channelContext;

        initComponents();
        initLayout();

        cachedAdvancedConnectorProperties = new PollConnectorPropertiesAdvanced();
    }

    public void setProperties(PollConnectorPropertiesInterface propertiesInterface) {
        setProperties(propertiesInterface.getPollConnectorProperties());
    }

    public void fillProperties(PollConnectorPropertiesInterface propertiesInterface) {
        PollConnectorProperties properties = null;

        if (propertiesInterface != null) {
            properties = propertiesInterface.getPollConnectorProperties();
        } else {
            properties = new PollConnectorProperties();
        }

        String selectedPollingType = (String) scheduleTypeComboBox.getSelectedItem();

        properties.setPollOnStart(yesStartPollRadioButton.isSelected());
        if (selectedPollingType.equals(PollingType.INTERVAL.getDisplayName())) {
            properties.setPollingType(PollingType.INTERVAL);

            String type = (String) pollingFrequencyTypeComboBox.getSelectedItem();
            int frequency = NumberUtils.toInt(pollingFrequencyField.getText(), 0);
            if (type.equals(POLLING_FREQUENCY_HOURS)) {
                frequency *= 3600000;
            } else if (type.equals(POLLING_FREQUENCY_MINUTES)) {
                frequency *= 60000;
            } else if (type.equals(POLLING_FREQUENCY_SECONDS)) {
                frequency *= 1000;
            }

            properties.setPollingFrequency(frequency);
        } else if (selectedPollingType.equals(PollingType.TIME.getDisplayName())) {
            properties.setPollingType(PollingType.TIME);

            try {
                SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm aa");
                DateFormatter timeFormatter = new DateFormatter(timeDateFormat);
                Date timeDate = (Date) timeFormatter.stringToValue(pollingTimePicker.getDate());
                Calendar timeCalendar = Calendar.getInstance();
                timeCalendar.setTime(timeDate);

                properties.setPollingHour(timeCalendar.get(Calendar.HOUR_OF_DAY));
                properties.setPollingMinute(timeCalendar.get(Calendar.MINUTE));
            } catch (ParseException e) {
                // Do nothing since they could be manually entering in the time...
            }
        } else if (selectedPollingType.equals(PollingType.CRON.getDisplayName())) {
            properties.setPollingType(PollingType.CRON);

            List<CronProperty> cronJobs = new ArrayList<CronProperty>();

            for (int rowCount = 0; rowCount < cronJobsTable.getRowCount(); rowCount++) {
                String description = (String) cronJobsTable.getValueAt(rowCount, 1);
                String expression = (String) cronJobsTable.getValueAt(rowCount, 0);

                if (StringUtils.isNotBlank(expression)) {
                    cronJobs.add(new CronProperty(description, expression));
                }
            }

            properties.setCronJobs(cronJobs);
        }

        properties.setPollConnectorPropertiesAdvanced(cachedAdvancedConnectorProperties);

        selectedPollingType = null;
        if (properties != null) {
            nextFireTimeProperties = properties.clone();
        }
    }

    public boolean checkProperties(PollConnectorPropertiesInterface propertiesInterface, boolean highlight) {
        PollConnectorProperties properties = null;

        if (propertiesInterface != null) {
            properties = propertiesInterface.getPollConnectorProperties();
        } else {
            fillProperties(null);
            properties = nextFireTimeProperties.clone();
        }

        boolean valid = true;
        pollingFrequencyField.setBackground(null);

        if (properties.getPollingType().equals(PollingType.INTERVAL)) {
            int frequency = properties.getPollingFrequency();

            if (frequency <= 0 || frequency >= 86400000) {
                valid = false;
            }

            if (highlight && !valid) {
                pollingFrequencyField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (properties.getPollingType().equals(PollingType.CRON)) {
            if (cronJobsTable.getRowCount() == 0) {
                valid = false;
            } else {
                invalidExpressions = new HashSet<String>();
                cronJobsTable.removeHighlighter(errorHighlighter);
                for (int index = 0; index < cronJobsTable.getRowCount(); index++) {
                    String cronExpression = (String) cronJobsTable.getValueAt(index, 0);
                    if (StringUtils.isBlank(cronExpression) || !PollConnectorJobHandler.validateExpression(cronExpression)) {
                        invalidExpressions.add(cronExpression);
                        valid = false;
                    }
                }

                if (!valid) {
                    cronJobsTable.addHighlighter(errorHighlighter);
                }
            }
        }

        return valid;
    }

    public void resetInvalidProperties() {
        setInvalidProperties(false, false);
    }

    public void setInvalidProperties(boolean invalidFrequency, boolean invalidJobs) {
        pollingFrequencyField.setBackground(invalidFrequency ? UIConstants.INVALID_COLOR : null);
        cronJobsTable.removeHighlighter(errorHighlighter);
        if (invalidJobs) {
            cronJobsTable.addHighlighter(errorHighlighter);
        }
    }

    public void updateInvalidExpressions(Set<String> invalidExpressions) {
        this.invalidExpressions = invalidExpressions;
    }

    private void initComponents() {
        scheduleTypeLabel = new JLabel("Schedule Type:");
        scheduleTypeComboBox = new MirthComboBox();
        // @formatter:off
        scheduleTypeComboBox.setToolTipText("<html>This connector polls to determine when new messages have arrived.<br>"
                + "Select \"Interval\" to poll each n units of time.<br>"
                + "Select \"Time\" to poll once a day at the specified time.<br>"
                + "Select \"Cron\" to poll at the specified cron expression(s).</html>");
        // @formatter:on
        scheduleTypeComboBox.addItem(PollingType.INTERVAL.getDisplayName());
        scheduleTypeComboBox.addItem(PollingType.TIME.getDisplayName());
        scheduleTypeComboBox.addItem(PollingType.CRON.getDisplayName());

        scheduleTypeActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                scheduleTypeActionPerformed();
                updateNextFireTime();
            }
        };

        nextPollLabel = new JLabel("Next poll at: ");

        yesStartPollRadioButton = new MirthRadioButton("Yes");
        yesStartPollRadioButton.setToolTipText("<html>Select Yes to immediately poll once on start.<br/>All subsequent polling will follow the specified schedule.</html>");
        yesStartPollRadioButton.setBackground(UIConstants.BACKGROUND_COLOR);
        yesStartPollRadioButton.setFocusable(false);

        noStartPollRadioButton = new MirthRadioButton("No");
        noStartPollRadioButton.setToolTipText("<html>Select Yes to immediately poll once on start.<br/>All subsequent polling will follow the specified schedule.</html>");
        noStartPollRadioButton.setBackground(UIConstants.BACKGROUND_COLOR);
        noStartPollRadioButton.setSelected(true);
        noStartPollRadioButton.setFocusable(false);

        pollOnStartButtonGroup = new ButtonGroup();
        pollOnStartButtonGroup.add(yesStartPollRadioButton);
        pollOnStartButtonGroup.add(noStartPollRadioButton);

        pollingTimePicker = new MirthTimePicker();
        pollingTimePicker.setToolTipText("The time of day to poll.");
        pollingTimePicker.setVisible(false);
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) pollingTimePicker.getEditor();
        JTextField textField = editor.getTextField();
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                updateNextFireTime();
            }

            public void removeUpdate(DocumentEvent e) {}

            public void changedUpdate(DocumentEvent e) {}
        });

        pollingFrequencySettingsPanel = new JPanel();
        pollingFrequencySettingsPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        pollingFrequencySettingsPanel.setVisible(true);

        pollingFrequencyField = new MirthTextField();
        pollingFrequencyField.setToolTipText("<html>The specified repeating time interval.<br/>Units must be less than 24 hours of time<br/>when converted to milliseconds.</html>");
        pollingFrequencyField.setSize(new Dimension(200, 20));
        pollingFrequencyField.setDocument(new MirthFieldConstraints(0, false, false, true));
        pollingFrequencyField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateNextFireTime();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateNextFireTime();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}
        });

        pollingFrequencyTypeComboBox = new MirthComboBox();
        pollingFrequencyTypeComboBox.setToolTipText("The interval's unit of time.");
        pollingFrequencyTypeComboBox.addItem(POLLING_FREQUENCY_MILLISECONDS);
        pollingFrequencyTypeComboBox.addItem(POLLING_FREQUENCY_SECONDS);
        pollingFrequencyTypeComboBox.addItem(POLLING_FREQUENCY_MINUTES);
        pollingFrequencyTypeComboBox.addItem(POLLING_FREQUENCY_HOURS);
        pollingFrequencyTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateNextFireTime();
            }
        });

        pollingCronSettingsPanel = new JPanel();
        pollingCronSettingsPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        pollingCronSettingsPanel.setVisible(false);

        cronJobsTable = new MirthTable();
        Object[][] tableData = new Object[0][1];
        cronJobsTable.setModel(new RefreshTableModel(tableData, new String[] { "Expression",
                "Description" }));
        cronJobsTable.setOpaque(true);
        cronJobsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        cronJobsTable.getTableHeader().setReorderingAllowed(false);
        cronJobsTable.setSortable(false);
        cronJobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cronJobsTable.getColumnModel().getColumn(0).setResizable(false);
        cronJobsTable.getColumnModel().getColumn(0).setCellEditor(new CronTableCellEditor(true));
        cronJobsTable.getColumnModel().getColumn(1).setResizable(false);
        cronJobsTable.getColumnModel().getColumn(1).setCellEditor(new CronTableCellEditor(true));

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            cronJobsTable.setHighlighters(highlighter);
        }

        HighlightPredicate errorHighlighterPredicate = new HighlightPredicate() {
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == cronJobsTable.getColumnViewIndex("Expression")) {
                    String cronExpression = (String) cronJobsTable.getValueAt(adapter.row, adapter.column);

                    if (invalidExpressions.contains(cronExpression)) {
                        return true;
                    }
                }
                return false;
            }
        };
        errorHighlighter = new ColorHighlighter(errorHighlighterPredicate, Color.PINK, Color.BLACK, Color.PINK, Color.BLACK);

        //@formatter:off
        String tooltip = "<html><head><style>td {text-align:center;}</style></head>"
                + "Cron expressions must be in Quartz format with at least 6 fields.<br/>"
                + "<br/>Format:"
                + "<table>"
                    + "<tr><td>Field</td><td>Required</td><td>Values</td><td>Special Characters</td></tr>"
                    + "<tr><td>Seconds</td><td>YES</td><td>0-59</td><td>, - * /</td></tr>"
                    + "<tr><td>Minutes</td><td>YES</td><td>0-59</td><td>, - * /</td></tr>"
                    + "<tr><td>Hours</td><td>YES</td><td>0-23</td><td>, - * /</td></tr>"
                    + "<tr><td>Day of Month</td><td>YES</td><td>1-31</td><td>, - * ? / L W</td></tr>"
                    + "<tr><td>Month</td><td>YES</td><td>1-12 or JAN-DEC</td><td>, - * /</td></tr>"
                    + "<tr><td>Day of Week</td><td>YES</td><td>1-7 or SUN-SAT</td><td>, - * ? / L #</td></tr>"
                    + "<tr><td>Year</td><td>NO</td><td>empty, 1970-2099</td><td>, - * /</td></tr>"
                    + "</table>"
                    + "<br/>Special Characters:"
                    + "<br/> &nbsp <b>*</b> : all values"
                    + "<br/> &nbsp <b>?</b> : no specific value"
                    + "<br/> &nbsp <b>-</b> : used to specify ranges"
                    + "<br/> &nbsp <b>,</b> : used to specify list of values"
                    + "<br/> &nbsp <b>/</b> : used to specify increments"
                    + "<br/> &nbsp <b>L</b> : used to specify the last of"
                    + "<br/> &nbsp <b>W</b> : used to specify the nearest weekday"
                    + "<br/> &nbsp <b>#</b> : used to specify the nth day of the month"
                    + "<br/><br/>Example: 0 */5 8-17 * * ? means to fire every 5 minutes starting at 8am<br/>and ending at 5pm everyday"
                + "<br/><br/><b>Note:</b> Support for specifying both a day-of-week and day-of-month<br/>is not yet supported. A ? must be used in one of these fields.</html>";
        //@formatter:on
        cronJobsTable.setToolTipText(tooltip);
        cronJobsTable.getTableHeader().setToolTipText(tooltip);

        cronScrollPane = new JScrollPane();
        cronScrollPane.getViewport().add(cronJobsTable);

        addJobButton = new JButton("Add");
        addJobButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                ((DefaultTableModel) cronJobsTable.getModel()).addRow(new Vector<String>());

                int rowSelectionNumber = cronJobsTable.getRowCount() - 1;
                cronJobsTable.setRowSelectionInterval(rowSelectionNumber, rowSelectionNumber);
                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);

                Boolean enabled = deleteJobButton.isEnabled();
                if (!enabled) {
                    deleteJobButton.setEnabled(true);
                }
                updateNextFireTime();
            }
        });

        deleteJobButton = new JButton("Delete");
        deleteJobButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int rowSelectionNumber = cronJobsTable.getSelectedRow();

                if (rowSelectionNumber > -1) {
                    DefaultTableModel model = (DefaultTableModel) cronJobsTable.getModel();
                    model.removeRow(rowSelectionNumber);

                    rowSelectionNumber--;
                    if (rowSelectionNumber > -1) {
                        cronJobsTable.setRowSelectionInterval(rowSelectionNumber, rowSelectionNumber);
                    } else if (cronJobsTable.getRowCount() > 0) {
                        cronJobsTable.setRowSelectionInterval(0, 0);
                    }

                    if (cronJobsTable.getRowCount() == 0) {
                        deleteJobButton.setEnabled(false);
                    }
                }

                updateNextFireTime();
                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            }
        });
        deleteJobButton.setEnabled(false);

        advancedSettingsButton = new JButton(new ImageIcon(Frame.class.getResource("images/wrench.png")));
        advancedSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                lastSelectedPollingType = StringUtils.isBlank(lastSelectedPollingType) ? "Interval" : lastSelectedPollingType;
                new AdvancedPollingSettingsDialog(lastSelectedPollingType, cachedAdvancedConnectorProperties, channelContext);
                updateNextFireTime();
            }
        });

        timeSettingsLabel = new JLabel("Interval:");
        timeSettingsLabel.setBackground(UIConstants.BACKGROUND_COLOR);

        scheduleSettingsPanel = new JPanel();
        scheduleSettingsPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        if (!channelContext) {
            // @formatter:off
            scheduleTypeComboBox.setToolTipText("<html>Select the pruning schedule type.<br>"
                    + "Select \"Interval\" to prune each n units of time.<br>"
                    + "Select \"Time\" to prune once a day at the specified time.<br>"
                    + "Select \"Cron\" to prune at the specified cron expression(s).</html>");
            // @formatter:on 
            pollingFrequencyField.setToolTipText("<html>The specified repeating time interval.<br/>Units must be between 1 and 24 hours of time<br/>when converted to milliseconds.</html>");
        }
    }

    public void enableComponents(boolean enable) {
        scheduleTypeLabel.setEnabled(enable);
        scheduleTypeComboBox.setEnabled(enable);

        pollingTimePicker.setEnabled(enable);

        pollingFrequencySettingsPanel.setEnabled(enable);
        pollingFrequencyField.setEnabled(enable);
        pollingFrequencyTypeComboBox.setEnabled(enable);

        cronJobsTable.setEnabled(enable);
        cronScrollPane.setEnabled(enable);
        addJobButton.setEnabled(enable);
        deleteJobButton.setEnabled(enable);

        timeSettingsLabel.setEnabled(enable);
        advancedSettingsButton.setEnabled(enable);
        scheduleSettingsPanel.setEnabled(enable);
    }

    public PollConnectorProperties getProperties() {
        fillProperties(null);
        return nextFireTimeProperties;
    }

    public void setProperties(PollConnectorProperties properties) {
        scheduleTypeComboBox.removeActionListener(scheduleTypeActionListener);

        clearProperties();

        if (properties.getPollingType().equals(PollingType.INTERVAL)) {
            String frequencyType = POLLING_FREQUENCY_MILLISECONDS;
            int frequency = properties.getPollingFrequency();

            if (frequency % 3600000 == 0) {
                frequency /= 3600000;
                frequencyType = POLLING_FREQUENCY_HOURS;
            } else if (frequency % 60000 == 0) {
                frequency /= 60000;
                frequencyType = POLLING_FREQUENCY_MINUTES;
            } else if (frequency % 1000 == 0) {
                frequency /= 1000;
                frequencyType = POLLING_FREQUENCY_SECONDS;
            }

            pollingFrequencyField.setText(String.valueOf(frequency));
            pollingFrequencyTypeComboBox.setSelectedItem(frequencyType);
        } else if (properties.getPollingType().equals(PollingType.TIME)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.set(Calendar.HOUR_OF_DAY, properties.getPollingHour());
            timeCalendar.set(Calendar.MINUTE, properties.getPollingMinute());
            pollingTimePicker.setDate(dateFormat.format(timeCalendar.getTime()));
        } else if (properties.getPollingType().equals(PollingType.CRON)) {
            List<CronProperty> cronJobs = properties.getCronJobs();
            if (cronJobs != null && cronJobs.size() > 0) {
                DefaultTableModel model = (DefaultTableModel) cronJobsTable.getModel();
                model.setNumRows(0);

                for (CronProperty property : cronJobs) {
                    model.addRow(new Object[] { property.getExpression(),
                            property.getDescription() });
                }

                deleteJobButton.setEnabled(true);
            }
        }

        nextFireTimeProperties = properties.clone();

        String pollingType = properties.getPollingType().getDisplayName();
        yesStartPollRadioButton.setSelected(properties.isPollOnStart());

        scheduleTypeComboBox.setSelectedItem(pollingType);

        enableComponents(pollingType);
        cachedAdvancedConnectorProperties = properties.getPollConnectorPropertiesAdvanced();
        scheduleTypeComboBox.addActionListener(scheduleTypeActionListener);
    }

    private void scheduleTypeActionPerformed() {
        String selectedType = (String) scheduleTypeComboBox.getSelectedItem();

        // If connector is changing, ignore this...
        if (lastSelectedPollingType != null && channelContext && (!isDefaultProperties() || !cachedAdvancedConnectorProperties.equals(new PollConnectorPropertiesAdvanced()))) {
            if (!selectedType.equals(lastSelectedPollingType) && JOptionPane.showConfirmDialog(PlatformUI.MIRTH_FRAME, "Are you sure you would like to change the polling type and lose all of the current properties?", "Select an Option", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                clearProperties();
                enableComponents(selectedType);
            } else {
                scheduleTypeComboBox.setSelectedItem(lastSelectedPollingType);
            }
        } else {
            scheduleTypeComboBox.setSelectedItem(selectedType);
            enableComponents(selectedType);
        }

        updateNextFireTime();
    }

    private void enableComponents(String selectedType) {
        if (selectedType.equals(PollingType.INTERVAL.getDisplayName())) {
            timeSettingsLabel.setText("Interval:");
            ((MigLayout) getLayout()).setComponentConstraints(timeSettingsLabel, "newline, right");
        } else if (selectedType.equals(PollingType.TIME.getDisplayName())) {
            timeSettingsLabel.setText("Time:");
            ((MigLayout) getLayout()).setComponentConstraints(timeSettingsLabel, "newline, right");
        } else if (selectedType.equals(PollingType.CRON.getDisplayName())) {
            timeSettingsLabel.setText("Cron Jobs:");
            ((MigLayout) getLayout()).setComponentConstraints(timeSettingsLabel, "newline, right, top");
        }
        pollingFrequencySettingsPanel.setVisible(selectedType.equals(PollingType.INTERVAL.getDisplayName()));
        pollingTimePicker.setVisible(selectedType.equals(PollingType.TIME.getDisplayName()));
        pollingCronSettingsPanel.setVisible(selectedType.equals(PollingType.CRON.getDisplayName()));
        advancedSettingsButton.setVisible(!selectedType.equals(PollingType.CRON.getDisplayName()));

        scheduleTypeComboBox.setSelectedItem(selectedType);
        lastSelectedPollingType = selectedType;
    }

    private boolean isDefaultProperties() {
        boolean isDefault = true;

        if (channelContext) {
            if (lastSelectedPollingType.equals(PollingType.INTERVAL.getDisplayName())) {
                if (!pollingFrequencyField.getText().equalsIgnoreCase("5")) {
                    isDefault = false;
                }

                if (pollingFrequencyTypeComboBox.getSelectedIndex() != 1) {
                    isDefault = false;
                }
            }

            if (lastSelectedPollingType.equals(PollingType.CRON.getDisplayName())) {
                if (cronJobsTable.getRowCount() == 1) {
                    isDefault = cronJobsTable.getValueAt(0, 0).equals(defaultCronJob);
                } else {
                    isDefault = false;
                }
            }
        }

        return isDefault;
    }

    private void clearProperties() {
        scheduleTypeComboBox.setSelectedItem(PollingType.INTERVAL.getDisplayName());
        noStartPollRadioButton.setSelected(true);

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        pollingTimePicker.setDate(dateFormat.format(Calendar.getInstance().getTime()));

        DefaultTableModel model = (DefaultTableModel) cronJobsTable.getModel();
        for (int index = model.getRowCount() - 1; index >= 0; index--) {
            model.removeRow(index);
        }

        Vector<String> row = new Vector<String>();
        if (channelContext) {
            pollingFrequencyTypeComboBox.setSelectedItem(POLLING_FREQUENCY_SECONDS);
            pollingFrequencyField.setText("5");

            row.add(defaultCronJob);
            row.add("Run every 5 seconds.");
        } else {
            pollingFrequencyTypeComboBox.setSelectedItem(POLLING_FREQUENCY_HOURS);
            pollingFrequencyField.setText("1");

            row.add("0 0 */1 * * ?");
            row.add("Run hourly.");
        }

        model.addRow(row);

        cachedAdvancedConnectorProperties = new PollConnectorPropertiesAdvanced();
    }

    public void updateNextFireTime() {
        if (channelContext && nextFireTimeProperties != null) {
            fillProperties(null);
            boolean isCron = nextFireTimeProperties.getPollingType().equals(PollingType.CRON);
            if (isCron || checkProperties(null, false)) {
                try {
                    PollConnectorJobHandler handler = new PollConnectorJobHandler(nextFireTimeProperties, PlatformUI.MIRTH_FRAME.mirthClient.getGuid(), false);
                    handler.configureJob(null, null, "DummyJob");
                    nextPollLabel.setText("Next poll at: " + handler.getNextFireTime());
                } catch (Exception e) {
                    StringBuilder builder = new StringBuilder();
                    if (isCron) {
                        String error = e.getMessage();
                        if (e.getMessage().contains("is invalid,.")) {
                            builder.append(error.substring(0, error.length() - 2));
                            builder.append(". ");
                        }

                        builder.append(e.getCause().getMessage());
                    } else {
                        builder.append(e.getMessage());
                    }
                    nextPollLabel.setText(builder.toString());
                }
            }
        }
    }

    private void initLayout() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        if (channelContext) {
            setLayout(new MigLayout("novisualpadding, hidemode 3, insets 0, gap 6 4", "[]12[]"));
            setBorder(javax.swing.BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Polling Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("宋体", 1, 11)));

            add(scheduleTypeLabel, "right");
            add(scheduleTypeComboBox, "split");
            add(nextPollLabel, "gapbefore 6");

            add(new JLabel("Poll Once on Start:"), "newline, right");
            add(yesStartPollRadioButton, "split");
            add(noStartPollRadioButton, "gapbefore 5");
        } else {
            setLayout(new MigLayout("novisualpadding, hidemode 3, insets 0, gap 6 6", "[]12[]"));
            add(scheduleTypeLabel, "right");
            add(scheduleTypeComboBox);
        }

        add(timeSettingsLabel, "newline, right");
        scheduleSettingsPanel.setLayout(new MigLayout("novisualpadding, hidemode 3, insets 0, gap 6 6"));

        scheduleSettingsPanel.add(pollingTimePicker, "w 70!");

        pollingFrequencySettingsPanel.setLayout(new MigLayout("novisualpadding, insets 0, gap 6 6"));
        pollingFrequencySettingsPanel.add(pollingFrequencyField, "w 75!, left");
        pollingFrequencySettingsPanel.add(pollingFrequencyTypeComboBox, "left");
        scheduleSettingsPanel.add(pollingFrequencySettingsPanel);

        pollingCronSettingsPanel.setLayout(new MigLayout("novisualpadding, insets 0, gap 6 6"));
        pollingCronSettingsPanel.add(cronScrollPane, "h 74!, w 400!");

        JPanel buttonPanel = new JPanel(new MigLayout("novisualpadding, insets 0, gap 6 6"));
        buttonPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        buttonPanel.add(addJobButton, "wrap, w 50!");
        buttonPanel.add(deleteJobButton, "w 50!");
        pollingCronSettingsPanel.add(buttonPanel, "top");
        scheduleSettingsPanel.add(pollingCronSettingsPanel);

        add(scheduleSettingsPanel, "split");
        add(advancedSettingsButton, "gapbefore 6, h 21!, w 22!");
    }

    private class CronTableCellEditor extends TextFieldCellEditor {
        boolean checkProperties;

        public CronTableCellEditor(boolean checkProperties) {
            super();
            this.checkProperties = checkProperties;
        }

        public boolean checkUniqueProperty(String property) {
            boolean exists = false;

            for (int i = 0; i < cronJobsTable.getRowCount(); i++) {
                boolean isDuplicateExpression = cronJobsTable.getValueAt(i, 0) != null && ((String) cronJobsTable.getValueAt(i, 0)).equals(property);
                if (isDuplicateExpression) {
                    exists = true;
                }
            }

            return exists;
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            boolean editable = super.isCellEditable(evt);

            if (editable) {
                deleteJobButton.setEnabled(false);
            }

            return editable;
        }

        @Override
        protected boolean valueChanged(String value) {
            deleteJobButton.setEnabled(true);

            if (checkProperties && (value.length() == 0 || checkUniqueProperty(value))) {
                return false;
            }

            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            return true;
        }

        @Override
        public boolean stopCellEditing() {
            boolean stopCellEditing = super.stopCellEditing();

            updateNextFireTime();

            return stopCellEditing;
        }
    }

    private JLabel scheduleTypeLabel;
    private MirthComboBox scheduleTypeComboBox;
    private ActionListener scheduleTypeActionListener;
    private JLabel nextPollLabel;

    private MirthRadioButton yesStartPollRadioButton;
    private MirthRadioButton noStartPollRadioButton;
    private ButtonGroup pollOnStartButtonGroup;

    private MirthTimePicker pollingTimePicker;

    private JPanel pollingFrequencySettingsPanel;
    private MirthTextField pollingFrequencyField;
    private MirthComboBox pollingFrequencyTypeComboBox;

    private JPanel pollingCronSettingsPanel;
    private MirthTable cronJobsTable;
    private ColorHighlighter errorHighlighter;
    private JScrollPane cronScrollPane;
    private JButton addJobButton;
    private JButton deleteJobButton;

    private JLabel timeSettingsLabel;
    private JButton advancedSettingsButton;
    private JPanel scheduleSettingsPanel;
}