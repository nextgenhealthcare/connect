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
import com.mirth.connect.model.Channel;

/**
 * an ant task to list all mirth channels
 * 
 * @author andrzej@coalese.com
 */

public class ListTask extends AbstractMirthTask {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */

	public void executeTask() throws BuildException {
		try {
			connectClient();
			commandListAll();
			disconnectClient();
		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandListAll() throws ClientException {
		List<Channel> allChannels = client.getChannel(null);

		System.out.println("ID\t\t\t\t\t\t\t\t\t\tEnabled\t\tName");

		String enable = "";

		for (Iterator<Channel> iter = allChannels.iterator(); iter.hasNext();) {
			Channel channel = iter.next();
			if (channel.isEnabled()) {
				enable = "ENABLED";
			} else {
				enable = "DISABLED";
			}

			System.out.println(channel.getId() + "\t" + enable + "\t\t" + channel.getName());
		}
	}

}
