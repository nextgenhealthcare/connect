/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.message.Status;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A DashboardStatus represents the status of a deployed channel, destination
 * chain or source/destination connector
 * 
 */
@XStreamAlias("dashboardStatus")
public class DashboardStatus implements Serializable {
    public enum StatusType {
        CHANNEL, CHAIN, SOURCE_CONNECTOR, DESTINATION_CONNECTOR
    };

    private String channelId;
    private String name;
    private DeployedState state;
    private Integer deployedRevisionDelta;
    private Calendar deployedDate;
    private Map<Status, Long> statistics;
    private Map<Status, Long> lifetimeStatistics;
    private List<DashboardStatus> childStatuses = new ArrayList<DashboardStatus>();
    private Integer metaDataId;
    private boolean queueEnabled;
    private Long queued = 0L;
    private boolean waitForPrevious = false;
    private Set<String> tags = new LinkedHashSet<String>();
    private StatusType statusType;

    public String getChannelId() {
        return this.channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeployedState getState() {
        return this.state;
    }

    public void setState(DeployedState state) {
        this.state = state;
    }

    public void setDeployedDate(Calendar deployedDate) {
        this.deployedDate = deployedDate;
    }

    public Calendar getDeployedDate() {
        return this.deployedDate;
    }

    public void setDeployedRevisionDelta(Integer deployedRevisionDelta) {
        this.deployedRevisionDelta = deployedRevisionDelta;
    }

    public Integer getDeployedRevisionDelta() {
        return this.deployedRevisionDelta;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public Map<Status, Long> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<Status, Long> statistics) {
        this.statistics = statistics;
    }

    public Map<Status, Long> getLifetimeStatistics() {
        return lifetimeStatistics;
    }

    public void setLifetimeStatistics(Map<Status, Long> lifetimeStatistics) {
        this.lifetimeStatistics = lifetimeStatistics;
    }

    public List<DashboardStatus> getChildStatuses() {
        return childStatuses;
    }

    public Integer getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(Integer metaDataId) {
        this.metaDataId = metaDataId;
    }

    public boolean isQueueEnabled() {
        return queueEnabled;
    }

    public void setQueueEnabled(boolean queueEnabled) {
        this.queueEnabled = queueEnabled;
    }

    public Long getQueued() {
		return queued;
	}

	public void setQueued(Long queued) {
		this.queued = queued;
	}

	public boolean isWaitForPrevious() {
        return waitForPrevious;
    }

    public void setWaitForPrevious(boolean waitForPrevious) {
        this.waitForPrevious = waitForPrevious;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public StatusType getStatusType() {
        return statusType;
    }

    public void setStatusType(StatusType statusType) {
        this.statusType = statusType;
    }

    public String getKey() {
        return channelId + "-" + metaDataId + "-" + statusType.toString();
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }
}
