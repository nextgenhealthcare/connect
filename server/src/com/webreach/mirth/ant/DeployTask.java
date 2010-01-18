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

import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatus;

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

		List<Channel> channels = client.getChannel(null);

		boolean hasChannels = false;

		for (Iterator iter = channels.iterator(); iter.hasNext();) {
			Channel channel = (Channel) iter.next();
			if (channel.isEnabled()) {
				hasChannels = true;
				break;
			}
		}

		client.deployAllChannels();

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
