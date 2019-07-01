/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.util.purge.Purgable;

public class PollConnectorPropertiesAdvanced implements Serializable, Purgable {
    private boolean weekly;
    private boolean[] inactiveDays;

    private int dayOfMonth;

    private boolean allDay;

    private int startingHour;
    private int startingMinute;

    private int endingHour;
    private int endingMinute;

    public PollConnectorPropertiesAdvanced() {
        weekly = true;
        inactiveDays = new boolean[8];

        inactiveDays[Calendar.SUNDAY] = false; // true means to exclude
        inactiveDays[Calendar.MONDAY] = false;
        inactiveDays[Calendar.TUESDAY] = false;
        inactiveDays[Calendar.WEDNESDAY] = false;
        inactiveDays[Calendar.THURSDAY] = false;
        inactiveDays[Calendar.FRIDAY] = false;
        inactiveDays[Calendar.SATURDAY] = false;

        dayOfMonth = 1;

        allDay = true;

        startingHour = 8;
        startingMinute = endingMinute = 0;
        endingHour = 17;
    }

    public PollConnectorPropertiesAdvanced(PollConnectorPropertiesAdvanced properties) {
        weekly = properties.isWeekly();

        boolean[] days = new boolean[8];
        boolean[] inactiveDays = properties.getInactiveDays();
        for (int index = 0; index < inactiveDays.length; index++) {
            days[index] = inactiveDays[index];
        }

        this.inactiveDays = days;
        dayOfMonth = properties.getDayOfMonth();

        allDay = properties.isAllDay();
        startingHour = properties.getStartingHour();
        startingMinute = properties.getStartingMinute();
        endingMinute = properties.getEndingMinute();
        endingHour = properties.getEndingHour();
    }

    public void setWeekly(boolean isWeekly) {
        this.weekly = isWeekly;
    }

    public boolean isWeekly() {
        return weekly;
    }

    public void setActiveDays(boolean[] inactiveDays) {
        this.inactiveDays = inactiveDays;
    }

    public boolean[] getInactiveDays() {
        return inactiveDays;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setStartingHour(int pollingHour) {
        this.startingHour = pollingHour;
    }

    public int getStartingHour() {
        return startingHour;
    }

    public void setStartingMinute(int pollingMinute) {
        this.startingMinute = pollingMinute;
    }

    public int getStartingMinute() {
        return startingMinute;
    }

    public void setEndingHour(int endingHour) {
        this.endingHour = endingHour;
    }

    public int getEndingHour() {
        return endingHour;
    }

    public void setEndingMinute(int endingMinute) {
        this.endingMinute = endingMinute;
    }

    public int getEndingMinute() {
        return endingMinute;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public PollConnectorPropertiesAdvanced clone() {
        return new PollConnectorPropertiesAdvanced(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("weekly", weekly);
        purgedProperties.put("inactiveDays", inactiveDays.toString());
        purgedProperties.put("dayOfMonth", dayOfMonth);
        purgedProperties.put("allDay", allDay);
        purgedProperties.put("startingHour", startingHour);
        purgedProperties.put("startingMinute", startingMinute);
        purgedProperties.put("endingHour", endingHour);
        purgedProperties.put("endingMinute", endingMinute);

        return purgedProperties;
    }
}