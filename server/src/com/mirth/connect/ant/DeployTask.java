/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.ant;

import java.util.List;

import org.apache.tools.ant.BuildException;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatus;

/**
 * an ant task to deploy all mirth channels
 * 
 * @author andrzej@coalese.com
 */

public class DeployTask extends AbstractMirthTask {
	protected int limit = 60; // 30 second limit

	private final int SLEEP = 500; // Milliseconds
	private final int MULTIPLIER = 2;

	/**
	 * @param timeout
	 */
	public void setTimeout(String timeout) {
		try {
			limit = (new Integer(timeout)).intValue() * MULTIPLIER;
		} catch (NumberFormatException e) {
			System.out.println("Invalid Timeout specified: " + timeout + ", using default value: " + limit);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */

	public void executeTask() throws BuildException {
		try {
			connectClient();
			commandDeployAll();
			disconnectClient();
		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandDeployAll() throws ClientException {
		System.out.println("Deploying Channels");

		boolean hasChannels = false;

		for (Channel channel : client.getChannel(null)) {
			if (channel.isEnabled()) {
				hasChannels = true;
				break;
			}
		}

		client.redeployAllChannels();

		if (hasChannels) {
			try {
				Thread.sleep(SLEEP);
			} catch (InterruptedException e) {
			}

			List<ChannelStatus> channelStatus = client.getChannelStatusList();

			while (channelStatus.size() == 0 && limit > 0) {
				try {
					Thread.sleep(SLEEP);
				} catch (InterruptedException e) {
				}

				channelStatus = client.getChannelStatusList();

				limit--;
			}

			if (limit > 0) {
				System.out.println("Channels Deployed");
			} else {
				throw (new BuildException("Deployment Timed out"));
			}
		} else {
			System.out.println("No Channels to Deploy");
		}
	}

}
