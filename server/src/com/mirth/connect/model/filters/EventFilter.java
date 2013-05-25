/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.filters;

import java.util.Calendar;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.mirth.connect.model.CalendarToStringStyle;
import com.mirth.connect.model.ServerEvent.Level;
import com.mirth.connect.model.ServerEvent.Outcome;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A EventFilter is used to search the event log.
 */
@XStreamAlias("eventFilter")
public class EventFilter {
    /*
     * Note that any filter criteria that is an int must be represented using
     * Integer otherwise it will default to 0 and not pass the isNotNull check
     * in the SQL mapping.
     */
    private Integer maxEventId;
    private Integer id;
    private Set<Level> levels;
    private Calendar startDate;
    private Calendar endDate;
    private String name;
    private Outcome outcome;
    private Integer userId;
    private String ipAddress;

    public Integer getMaxEventId() {
        return maxEventId;
    }

    public void setMaxEventId(Integer maxEventId) {
        this.maxEventId = maxEventId;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Level> getLevels() {
        return this.levels;
    }

    public void setLevels(Set<Level> levels) {
        this.levels = levels;
    }

    public Calendar getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public Calendar getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }
}
