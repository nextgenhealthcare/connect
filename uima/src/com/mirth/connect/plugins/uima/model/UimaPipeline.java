package com.mirth.connect.plugins.uima.model;

public class UimaPipeline {
    private String name;
    private long consumerCount;
    private long pendingMessageCount;
    private long messageCount;
    private long dequeueCount;
    private int memoryPercentUsage;
    private int cursorPercentUsage;
    private double avgEnqueueTime;
    
    private String jmsUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getConsumerCount() {
        return consumerCount;
    }

    public void setConsumerCount(long consumerCount) {
        this.consumerCount = consumerCount;
    }

    public long getPendingMessageCount() {
        return pendingMessageCount;
    }

    public void setPendingMessageCount(long pendingMessageCount) {
        this.pendingMessageCount = pendingMessageCount;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    public long getDequeueCount() {
        return dequeueCount;
    }

    public void setDequeueCount(long dequeueCount) {
        this.dequeueCount = dequeueCount;
    }

    public int getMemoryPercentUsage() {
        return memoryPercentUsage;
    }

    public void setMemoryPercentUsage(int memoryPercentUsage) {
        this.memoryPercentUsage = memoryPercentUsage;
    }

    public int getCursorPercentUsage() {
        return cursorPercentUsage;
    }

    public void setCursorPercentUsage(int cursorPercentUsage) {
        this.cursorPercentUsage = cursorPercentUsage;
    }

    public double getAvgEnqueueTime() {
        return avgEnqueueTime;
    }

    public void setAvgEnqueueTime(double avgEnqueueTime) {
        this.avgEnqueueTime = avgEnqueueTime;
    }

    public String getJmsUrl() {
        return jmsUrl;
    }

    public void setJmsUrl(String jmsUrl) {
        this.jmsUrl = jmsUrl;
    }

}
