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
 * The Original Code is Mirth-Ant.
 *
 * The Initial Developer of the Original Code is
 * Coalese Corporation.
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Andrzej Taramina <andrzej@coalese.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatus;

/**
 * @author andrzej@coalese.com
 */

public abstract class AbstractMirthTask extends Task {

	protected String user = "admin";
	protected String password = "admin";
	protected String server = "https://localhost:8443";
	protected String version = "0.0.0";
	protected boolean failonerror = true;

	protected Client client;

	/**
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param user
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @param server
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	public void setFailonerror(boolean failonerror) {
		this.failonerror = failonerror;
	}

	/*
	 * Implemented by all Mirth Ant Tasks
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */

	public abstract void executeTask() throws BuildException;

	/*
	 * Task execute() dispatcher.
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */

	public void execute() throws BuildException {
		try {
			executeTask();
		} catch (BuildException e) {
			if (failonerror) {
				throw (e);
			} else {
				System.out.println(e.getMessage());
			}
		}
	}

	protected void connectClient() throws ClientException {
		client = new Client(server);

		if (!client.login(user, password, version)) {
			throw (new ClientException("Could not login to server."));
		}
	}

	protected void disconnectClient() throws ClientException {
		client.logout();
	}

	protected String getChannelEnabledString(ChannelStatus channelStatus) throws ClientException {
		String result = "unknown";

		String id = channelStatus.getChannelId();

		for (Channel channel : client.getChannel(null)) {
			if (channel.getId().equalsIgnoreCase(id)) {
				result = channel.isEnabled() ? "ENABLED" : "DISABLED";
				break;
			}
		}

		return (result);
	}
}
