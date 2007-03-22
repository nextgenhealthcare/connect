/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.model;

import java.io.Serializable;

import com.webreach.mirth.util.EqualsUtil;

public class ChannelStatistics implements Serializable {
	private String channelId;
	private int receivedCount = 0;
	private int sentCount = 0;
	private int errorCount = 0;
	private int filteredCount = 0;
	private int queuedCount = 0;
	
	public int getFilteredCount() {
		return filteredCount;
	}

	public void setFilteredCount(int filteredCount) {
		this.filteredCount = filteredCount;
	}

	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public int getErrorCount() {
		return this.errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
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
	
	public int getQueuedCount() {
		return this.queuedCount;
	}

	public void setQueuedCount(int queuedCount) {
		this.queuedCount = queuedCount;
	}

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof ChannelStatistics)) {
			return false;
		}
		
		ChannelStatistics statistic = (ChannelStatistics) that;
		
		return
			EqualsUtil.areEqual(this.getChannelId(), statistic.getChannelId()) &&
			EqualsUtil.areEqual(this.getReceivedCount(), statistic.getReceivedCount()) &&
			EqualsUtil.areEqual(this.getSentCount(), statistic.getSentCount()) &&
			EqualsUtil.areEqual(this.getErrorCount(), statistic.getErrorCount()) &&
			EqualsUtil.areEqual(this.getFilteredCount(), statistic.getReceivedCount()) &&
			EqualsUtil.areEqual(this.getQueuedCount(), statistic.getQueuedCount());
	}	

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("channelId=" + getChannelId() + ", ");
		builder.append("receivedCount=" + getReceivedCount() + ", ");
		builder.append("sentCount=" + getSentCount() + ", ");
		builder.append("queuedCount=" + getQueuedCount() + ", ");
		builder.append("errorCount=" + getErrorCount());
		builder.append("]");
		return builder.toString();
	}

}
