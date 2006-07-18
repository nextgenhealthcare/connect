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


package com.webreach.mirth.ui;

public class Status {
	private int sent;
	private int received;
	private int error;
	private int queue;
	private boolean isRunning;
	private boolean isChanged;
	private String name;
	private String displayName;

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getError() {
		return this.error;
	}

	public void setError(int error) {
		this.error = error;
	}

	public boolean isChanged() {
		return this.isChanged;
	}

	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}

	public boolean isRunning() {
		return this.isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQueue() {
		return this.queue;
	}

	public void setQueue(int queue) {
		this.queue = queue;
	}

	public int getReceived() {
		return this.received;
	}

	public void setReceived(int received) {
		this.received = received;
	}

	public int getSent() {
		return this.sent;
	}

	public void setSent(int sent) {
		this.sent = sent;
	}
}
