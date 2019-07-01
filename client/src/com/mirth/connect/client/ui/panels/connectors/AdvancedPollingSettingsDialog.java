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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.border.TitledBorder;
import javax.swing.text.DateFormatter;

import net.miginfocom.swing.MigLayout;

import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTimePicker;
import com.mirth.connect.client.ui.util.DisplayUtil;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesAdvanced;
import com.mirth.connect.donkey.model.channel.PollingType;

public class AdvancedPollingSettingsDialog extends MirthDialog {
    private String scheduleType;
    private PollConnectorPropertiesAdvanced advancedProperties;

    private boolean channelContext = true;

    public AdvancedPollingSettingsDialog(String scheduleType, PollConnectorPropertiesAdvanced advancedProperties, boolean channelContext) {
        super(PlatformUI.MIRTH_FRAME, true);

        this.scheduleType = scheduleType;
        this.advancedProperties = advancedProperties;
        this.channelContext = channelContext;

        setTitle("Settings");
        DisplayUtil.setResizable(this, false);
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new MigLayout("novisualpadding, hidemode 3, insets 8"));

        initComponents();
        initLayout();
        setProperties();

        pack();
        setLocationRelativeTo(PlatformUI.MIRTH_FRAME);
        setVisible(true);
    }

    public void enableComponents() {
        activeTimeLabel.setEnabled(scheduleType.equals(PollingType.INTERVAL.getDisplayName()));
        allDayRadioButton.setEnabled(scheduleType.equals(PollingType.INTERVAL.getDisplayName()));
        rangeRadioButton.setEnabled(scheduleType.equals(PollingType.INTERVAL.getDisplayName()));
        beginningRangePicker.setEnabled(scheduleType.equals(PollingType.INTERVAL.getDisplayName()));
        endingRangePicker.setEnabled(scheduleType.equals(PollingType.INTERVAL.getDisplayName()));
    }

    public void setProperties() {
        resetComponents();

        if (advancedProperties.isWeekly() && weeklyRadioButton.isEnabled()) {
            weeklyRadioButton.setSelected(true);
            boolean[] days = advancedProperties.getInactiveDays();

            sundayCheckbox.setSelected(!days[Calendar.SUNDAY]);
            mondayCheckbox.setSelected(!days[Calendar.MONDAY]);
            tuesdayCheckbox.setSelected(!days[Calendar.TUESDAY]);
            wednesdayCheckbox.setSelected(!days[Calendar.WEDNESDAY]);
            thursdayCheckbox.setSelected(!days[Calendar.THURSDAY]);
            fridayCheckbox.setSelected(!days[Calendar.FRIDAY]);
            saturdayCheckbox.setSelected(!days[Calendar.SATURDAY]);
        } else {
            monthlyRadioButton.setSelected(true);
            monthlyDayPicker.setDate(String.valueOf(advancedProperties.getDayOfMonth()));
            activeDaysRadioButtonClicked();
        }

        if (advancedProperties.isAllDay() || scheduleType.equals(PollingType.TIME)) {
            allDayRadioButton.setSelected(true);
        } else {
            rangeRadioButton.setSelected(true);
            beginningRangePicker.setEnabled(true);
            endingRangePicker.setEnabled(true);

            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.set(Calendar.HOUR_OF_DAY, advancedProperties.getStartingHour());
            timeCalendar.set(Calendar.MINUTE, advancedProperties.getStartingMinute());

            beginningRangePicker.setDate(dateFormat.format(timeCalendar.getTime()));

            timeCalendar.set(Calendar.HOUR_OF_DAY, advancedProperties.getEndingHour());
            timeCalendar.set(Calendar.MINUTE, advancedProperties.getEndingMinute());

            endingRangePicker.setDate(dateFormat.format(timeCalendar.getTime()));
        }
        enableComponents();
        activeDaysRadioButtonClicked();
        activeTimeRadioButtonClicked();
    }

    private void resetComponents() {
        weeklyRadioButton.setSelected(true);
        monthlyRadioButton.setSelected(false);
        allDayRadioButton.setSelected(true);
        rangeRadioButton.setSelected(false);

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.set(Calendar.HOUR_OF_DAY, 8);
        timeCalendar.set(Calendar.MINUTE, 0);

        beginningRangePicker.setDate(dateFormat.format(timeCalendar.getTime()));

        timeCalendar.set(Calendar.HOUR_OF_DAY, 17);

        endingRangePicker.setDate(dateFormat.format(timeCalendar.getTime()));
        monthlyDayPicker.setDate("01");
    }

    public PollConnectorPropertiesAdvanced getProperties() {
        return advancedProperties;
    }

    public void clearProperties() {
        advancedProperties = new PollConnectorPropertiesAdvanced();
    }

    public void setType(String type) {
        scheduleType = type;
    }

    private void initComponents() {
        String weekly = "<html>Select Weekly to poll on the specified days of the week.<br>Select Monthly to poll on the specified day of the month.</html>";
        String active = "<html>If \"All Day\" is selected, polling may occur at any time during the day.<br>If \"Range\" is selected, polling will only occur during the specified range.</html>";
        if (!channelContext) {
            weekly = "<html>Select Weekly to prune on the specified days of the week.<br>Select Monthly to prune on the specified day of the month.</html>";
            active = "<html>If \"All Day\" is selected, pruning may occur at any time during the day.<br>If \"Range\" is selected, pruning will only occur during the specified range.</html>";
        }
        activeDaysLabel = new JLabel("Active Days:");

        weeklyRadioButton = new JRadioButton("Weekly");
        weeklyRadioButton.setToolTipText(weekly);
        weeklyRadioButton.setBackground(UIConstants.BACKGROUND_COLOR);
        weeklyRadioButton.setFocusable(false);
        weeklyRadioButton.setSelected(true);
        weeklyRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                activeDaysRadioButtonClicked();
            }
        });

        monthlyRadioButton = new JRadioButton("Monthly");
        monthlyRadioButton.setToolTipText(weekly);
        monthlyRadioButton.setBackground(UIConstants.BACKGROUND_COLOR);
        monthlyRadioButton.setFocusable(false);
        monthlyRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                activeDaysRadioButtonClicked();
            }
        });

        activeDaysButtonGroup = new ButtonGroup();
        activeDaysButtonGroup.add(weeklyRadioButton);
        activeDaysButtonGroup.add(monthlyRadioButton);

        sundayLabel = new JLabel("S");
        mondayLabel = new JLabel("M");
        tuesdayLabel = new JLabel("T");
        wednesdayLabel = new JLabel("W");
        thursdayLabel = new JLabel("Th");
        fridayLabel = new JLabel("F");
        saturdayLabel = new JLabel("S");

        sundayCheckbox = initCheckbox();
        mondayCheckbox = initCheckbox();
        tuesdayCheckbox = initCheckbox();
        wednesdayCheckbox = initCheckbox();
        thursdayCheckbox = initCheckbox();
        fridayCheckbox = initCheckbox();
        saturdayCheckbox = initCheckbox();

        monthlyDayPicker = new MirthTimePicker("dd", Calendar.MONTH);
        monthlyDayPicker.setSaveEnabled(false);

        activeTimeLabel = new JLabel("Active Time:");

        allDayRadioButton = new JRadioButton("All Day");
        allDayRadioButton.setToolTipText(active);
        allDayRadioButton.setBackground(UIConstants.BACKGROUND_COLOR);
        allDayRadioButton.setFocusable(false);
        allDayRadioButton.setSelected(true);
        allDayRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                activeTimeRadioButtonClicked();
            }
        });

        rangeRadioButton = new JRadioButton("Range");
        rangeRadioButton.setToolTipText(active);
        rangeRadioButton.setBackground(UIConstants.BACKGROUND_COLOR);
        rangeRadioButton.setFocusable(false);
        rangeRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                activeTimeRadioButtonClicked();
            }
        });

        activeTimeButtonGroup = new ButtonGroup();
        activeTimeButtonGroup.add(allDayRadioButton);
        activeTimeButtonGroup.add(rangeRadioButton);

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.set(Calendar.HOUR_OF_DAY, 8);
        timeCalendar.set(Calendar.MINUTE, 0);

        beginningRangePicker = new MirthTimePicker();
        beginningRangePicker.setSaveEnabled(false);
        beginningRangePicker.setDate(dateFormat.format(timeCalendar.getTime()));

        hyphenLabel = new JLabel("-");

        timeCalendar.set(Calendar.HOUR_OF_DAY, 17);

        endingRangePicker = new MirthTimePicker();
        endingRangePicker.setSaveEnabled(false);
        endingRangePicker.setDate(dateFormat.format(timeCalendar.getTime()));

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                okButtonActionPerformed();
            }
        });
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
    }

    private JCheckBox initCheckbox() {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setBackground(UIConstants.BACKGROUND_COLOR);
        checkBox.setFocusable(false);
        checkBox.setSelected(true);

        return checkBox;
    }

    private void activeDaysRadioButtonClicked() {
        boolean isWeeklySchedule = weeklyRadioButton.isSelected();

        sundayLabel.setEnabled(isWeeklySchedule);
        mondayLabel.setEnabled(isWeeklySchedule);
        tuesdayLabel.setEnabled(isWeeklySchedule);
        wednesdayLabel.setEnabled(isWeeklySchedule);
        thursdayLabel.setEnabled(isWeeklySchedule);
        fridayLabel.setEnabled(isWeeklySchedule);
        saturdayLabel.setEnabled(isWeeklySchedule);

        sundayCheckbox.setEnabled(isWeeklySchedule);
        mondayCheckbox.setEnabled(isWeeklySchedule);
        tuesdayCheckbox.setEnabled(isWeeklySchedule);
        wednesdayCheckbox.setEnabled(isWeeklySchedule);
        thursdayCheckbox.setEnabled(isWeeklySchedule);
        fridayCheckbox.setEnabled(isWeeklySchedule);
        saturdayCheckbox.setEnabled(isWeeklySchedule);

        monthlyDayPicker.setEnabled(!isWeeklySchedule);
    }

    private void activeTimeRadioButtonClicked() {
        boolean range = rangeRadioButton.isSelected();

        beginningRangePicker.setEnabled(range);
        hyphenLabel.setEnabled(range);
        endingRangePicker.setEnabled(range);
    }

    public boolean equals(PollConnectorPropertiesAdvanced advancedProperties) {
        boolean equals = true;

        for (int index = 0; index < advancedProperties.getInactiveDays().length; index++) {
            if (advancedProperties.getInactiveDays()[index] != advancedProperties.getInactiveDays()[index]) {
                equals = false;
                break;
            }
        }

        if (equals) {
            equals = advancedProperties.isWeekly() != advancedProperties.isWeekly() ? false : true;
            equals = advancedProperties.getDayOfMonth() != advancedProperties.getDayOfMonth() ? false : equals;
            equals = advancedProperties.isAllDay() != advancedProperties.isAllDay() ? false : equals;
            equals = advancedProperties.getStartingHour() != advancedProperties.getStartingHour() ? false : equals;
            equals = advancedProperties.getStartingMinute() != advancedProperties.getStartingMinute() ? false : equals;
            equals = advancedProperties.getEndingHour() != advancedProperties.getEndingHour() ? false : equals;
            equals = advancedProperties.getEndingMinute() != advancedProperties.getEndingMinute() ? false : equals;
        }

        return equals;
    }

    private void okButtonActionPerformed() {
        if (weeklyRadioButton.isSelected()) {
            boolean[] activeDays = new boolean[8];

            activeDays[Calendar.SUNDAY] = !sundayCheckbox.isSelected();
            activeDays[Calendar.MONDAY] = !mondayCheckbox.isSelected();
            activeDays[Calendar.TUESDAY] = !tuesdayCheckbox.isSelected();
            activeDays[Calendar.WEDNESDAY] = !wednesdayCheckbox.isSelected();
            activeDays[Calendar.THURSDAY] = !thursdayCheckbox.isSelected();
            activeDays[Calendar.FRIDAY] = !fridayCheckbox.isSelected();
            activeDays[Calendar.SATURDAY] = !saturdayCheckbox.isSelected();

            Set<Boolean> selectedDays = new HashSet<Boolean>();
            for (int index = 1; index < activeDays.length; index++) {
                selectedDays.add(activeDays[index]);
            }

            if (!selectedDays.contains(false)) {
                PlatformUI.MIRTH_FRAME.alertError(PlatformUI.MIRTH_FRAME, "You must select at least one day.");
                return;
            }

            advancedProperties.setWeekly(true);
            advancedProperties.setActiveDays(activeDays);
        } else if (monthlyRadioButton.isSelected()) {
            advancedProperties.setWeekly(false);
            advancedProperties.setDayOfMonth(Integer.parseInt(monthlyDayPicker.getDate()));
        }

        if (allDayRadioButton.isSelected()) {
            advancedProperties.setAllDay(true);
        } else if (rangeRadioButton.isSelected()) {
            advancedProperties.setAllDay(false);

            try {
                SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm aa");
                DateFormatter timeFormatter = new DateFormatter(timeDateFormat);
                Date timeDate = (Date) timeFormatter.stringToValue(beginningRangePicker.getDate());
                Calendar timeCalendar = Calendar.getInstance();
                timeCalendar.setTime(timeDate);

                int startingHour = timeCalendar.get(Calendar.HOUR_OF_DAY);
                int startingMinute = timeCalendar.get(Calendar.MINUTE);

                timeDate = (Date) timeFormatter.stringToValue(endingRangePicker.getDate());
                timeCalendar = Calendar.getInstance();
                timeCalendar.setTime(timeDate);

                int endingHour = timeCalendar.get(Calendar.HOUR_OF_DAY);
                int endingMinute = timeCalendar.get(Calendar.MINUTE);

                if (startingHour == endingHour && startingMinute == endingMinute) {
                    PlatformUI.MIRTH_FRAME.alertError(PlatformUI.MIRTH_FRAME, "Start and End times must be different.");
                    return;
                }

                advancedProperties.setStartingHour(startingHour);
                advancedProperties.setStartingMinute(startingMinute);
                advancedProperties.setEndingHour(endingHour);
                advancedProperties.setEndingMinute(endingMinute);
            } catch (ParseException e) {
                PlatformUI.MIRTH_FRAME.alertError(PlatformUI.MIRTH_FRAME, e.getMessage());
            }
        }

        PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        dispose();
    }

    private void initLayout() {
        JPanel advancedSettingsPanel = new JPanel(new MigLayout("novisualpadding, hidemode 3, insets 8 12 12 12, gap 6 6", "[right]12[left][left]"));
        advancedSettingsPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        advancedSettingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(204, 204, 204)), "Advanced Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("宋体", 1, 11)));

        advancedSettingsPanel.add(new JLabel(" ")); // Absolute positioned
        advancedSettingsPanel.add(new JLabel(" "));
        advancedSettingsPanel.add(sundayLabel, "split, gapleft 3");
        advancedSettingsPanel.add(mondayLabel, "gapleft 11");
        advancedSettingsPanel.add(tuesdayLabel, "gapleft 11");
        advancedSettingsPanel.add(wednesdayLabel, "gapleft 10");
        advancedSettingsPanel.add(thursdayLabel, "gapleft 6");
        advancedSettingsPanel.add(fridayLabel, "gapleft 11");
        advancedSettingsPanel.add(saturdayLabel, "gapleft 11, wrap");

        advancedSettingsPanel.add(activeDaysLabel);

        advancedSettingsPanel.add(weeklyRadioButton);
        advancedSettingsPanel.add(sundayCheckbox, "split");
        advancedSettingsPanel.add(mondayCheckbox);
        advancedSettingsPanel.add(tuesdayCheckbox);
        advancedSettingsPanel.add(wednesdayCheckbox);
        advancedSettingsPanel.add(thursdayCheckbox);
        advancedSettingsPanel.add(fridayCheckbox);
        advancedSettingsPanel.add(saturdayCheckbox, "wrap");

        advancedSettingsPanel.add(new JLabel(" "));
        advancedSettingsPanel.add(monthlyRadioButton);
        advancedSettingsPanel.add(monthlyDayPicker, "wrap");

        advancedSettingsPanel.add(activeTimeLabel);
        advancedSettingsPanel.add(allDayRadioButton, "wrap");

        advancedSettingsPanel.add(new JLabel(" "));
        advancedSettingsPanel.add(rangeRadioButton);
        advancedSettingsPanel.add(beginningRangePicker, "split");
        advancedSettingsPanel.add(hyphenLabel, "gapleft 8, gapright 8");
        advancedSettingsPanel.add(endingRangePicker);

        add(advancedSettingsPanel, "wrap");
        add(new JSeparator(), "growx, sx, wrap");

        add(okButton, "split, right, push, w 50!");
        add(cancelButton, "w 50!");
    }

    private JLabel activeDaysLabel;
    private JRadioButton weeklyRadioButton;
    private JRadioButton monthlyRadioButton;

    private ButtonGroup activeDaysButtonGroup;

    private JLabel sundayLabel;
    private JLabel mondayLabel;
    private JLabel tuesdayLabel;
    private JLabel wednesdayLabel;
    private JLabel thursdayLabel;
    private JLabel fridayLabel;
    private JLabel saturdayLabel;

    private JCheckBox sundayCheckbox;
    private JCheckBox mondayCheckbox;
    private JCheckBox tuesdayCheckbox;
    private JCheckBox wednesdayCheckbox;
    private JCheckBox thursdayCheckbox;
    private JCheckBox fridayCheckbox;
    private JCheckBox saturdayCheckbox;

    private MirthTimePicker monthlyDayPicker;

    private JLabel activeTimeLabel;
    private JRadioButton allDayRadioButton;
    private JRadioButton rangeRadioButton;
    private ButtonGroup activeTimeButtonGroup;

    private MirthTimePicker beginningRangePicker;
    private JLabel hyphenLabel;
    private MirthTimePicker endingRangePicker;

    private JButton okButton;
    private JButton cancelButton;
}