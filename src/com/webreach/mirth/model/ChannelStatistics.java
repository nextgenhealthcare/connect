package com.webreach.mirth.model;

public class ChannelStatistics {
	private int receivedCount = 0;
	private int sentCount = 0;
	private int errorCount = 0;
	private int queueSize = 0;

	public int getErrorCount() {
		return this.errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public int getQueueSize() {
		return this.queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public int getReceivedCount() {
		return this.receivedCount;
	}

	public void setReceivedCount(int receivedCount) {
		this.receivedCount = receivedCount;
	}

	public int getSentCount() {
		return this.sentCount;
	}

	public void setSentCount(int sentCount) {
		this.sentCount = sentCount;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Statistics[");
		builder.append("errorCount=" + getErrorCount() + ", ");
		builder.append("queueSize=" + getQueueSize() + ", ");
		builder.append("receivedCount=" + getReceivedCount() + ", ");
		builder.append("sentCount=" + getSentCount() + ", ");
		builder.append("]");
		return builder.toString();
	}

}
