/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.ant;

import org.apache.tools.ant.BuildException;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.model.Channel;

/**
 * an ant task to reset stats for all mirth channels
 * 
 * @author andrzej@coalese.com
 */

public class ResetStatsTask extends AbstractMirthTask {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */

	public void executeTask() throws BuildException {
		try {
			connectClient();
			commandResetStatsAll();
			disconnectClient();
		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandResetStatsAll() throws ClientException {
		for (Channel channel : client.getChannel(null)) {
			client.clearStatistics(channel.getId(), true, true, true, true, true, true);
		}

		System.out.println("Channel Stats Reset");
	}

}
