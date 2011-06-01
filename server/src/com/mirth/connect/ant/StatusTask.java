/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.ant;

import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.model.ChannelStatus;

/**
 * an ant task to get Mirth server status
 * 
 * @author andrzej@coalese.com
 */

public class StatusTask extends AbstractMirthTask {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */

	public void executeTask() throws BuildException {
		try {
			connectClient();
			commandStatus();
			disconnectClient();
		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandStatus() throws ClientException {
		System.out.println("ID\t\t\t\t\t\t\t\t\tStatus\t\tEnabled\t\tName");

		List<ChannelStatus> channels = client.getChannelStatusList();

		for (Iterator<ChannelStatus> iter = channels.iterator(); iter.hasNext();) {
			ChannelStatus channel = iter.next();

			System.out.println(channel.getChannelId() + "\t" + channel.getState().toString() + "\t\t" + getChannelEnabledString(channel) + "\t\t" + channel.getName());
		}
	}

}
