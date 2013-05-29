package com.mirth.connect.plugins.datapruner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DataPrunerStatus implements Serializable {
    private String currentChannelId;
    private String currentChannelName;
    private List<String> pendingChannelIds = new ArrayList<String>();
    private List<String> processedChannelIds = new ArrayList<String>();
    private List<String> failedChannelIds = new ArrayList<String>();
    private Calendar startTime;
    private Calendar endTime;
    private Calendar taskStartTime;
    private boolean isPruning;
    private boolean isPruningEvents;
    private boolean isArchiving;
    
    public String getCurrentChannelId() {
        return currentChannelId;
    }

    public void setCurrentChannelId(String currentChannelId) {
        this.currentChannelId = currentChannelId;
    }

    public String getCurrentChannelName() {
        return currentChannelName;
    }

    public void setCurrentChannelName(String currentChannelName) {
        this.currentChannelName = currentChannelName;
    }

    public List<String> getPendingChannelIds() {
        return pendingChannelIds;
    }

    public List<String> getProcessedChannelIds() {
        return processedChannelIds;
    }

    public List<String> getFailedChannelIds() {
        return failedChannelIds;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public Calendar getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(Calendar taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public boolean isPruning() {
        return isPruning;
    }

    public void setPruning(boolean isPruning) {
        this.isPruning = isPruning;
    }

    public boolean isPruningEvents() {
        return isPruningEvents;
    }

    public void setPruningEvents(boolean isPruningEvents) {
        this.isPruningEvents = isPruningEvents;
    }

    public boolean isArchiving() {
        return isArchiving;
    }

    public void setArchiving(boolean isArchiving) {
        this.isArchiving = isArchiving;
    }
}
