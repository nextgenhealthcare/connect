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
