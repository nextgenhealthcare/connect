/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.ant;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.ChannelStatus.State;

/**
 * an ant task to list all mirth users
 * 
 * @author andrzej@coalese.com
 */

public class ChannelActionTask extends AbstractMirthTask {
	private final String ACTION_STATS = "stats";
	private final String ACTION_START = "start";
	private final String ACTION_STOP = "stop";
	private final String ACTION_PAUSE = "pause";
	private final String ACTION_RESUME = "resume";
	private final String ACTION_REMOVE = "remove";
	private final String ACTION_ENABLE = "enable";
	private final String ACTION_DISABLE = "disable";
	private final String ACTION_CLEAR = "clear";
	private final String ACTION_RESET = "reset";

	protected String action = ACTION_STATS;

	protected String selector = "";

	/**
	 * @param action
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @param channel
	 */
	public void setChannel(String selector) {
		this.selector = selector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */

	public void executeTask() throws BuildException {
		try {
			if (selector != null && !selector.equals("")) {
				if (action.equalsIgnoreCase(ACTION_STATS)) {
					commandStats();
				} else if (action.equalsIgnoreCase(ACTION_START)) {
					commandStart();
				} else if (action.equalsIgnoreCase(ACTION_STOP)) {
					commandStop();
				} else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
					commandPause();
				} else if (action.equalsIgnoreCase(ACTION_RESUME)) {
					commandResume();
				} else if (action.equalsIgnoreCase(ACTION_REMOVE)) {
					commandRemove();
				} else if (action.equalsIgnoreCase(ACTION_ENABLE)) {
					commandEnable();
				} else if (action.equalsIgnoreCase(ACTION_DISABLE)) {
					commandDisable();
				} else if (action.equalsIgnoreCase(ACTION_CLEAR)) {
					commandClear();
				} else if (action.equalsIgnoreCase(ACTION_RESET)) {
					commandReset();
				} else {
					throw (new BuildException("Invalid Channel Action specified: " + action));
				}
			} else {
				throw (new BuildException("Channel not specified"));
			}

		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandStats() throws ClientException {
		connectClient();

		for (ChannelStatus channel : getMatchingChannelStatuses(selector)) {
			ChannelStatistics stats = client.getStatistics(channel.getChannelId());
			System.out.println("Channel:  " + channel.getChannelId());
			System.out.println("Name:     " + channel.getName());
			System.out.println("Status:   " + channel.getState().toString() + ", " + getChannelEnabledString(channel));
			System.out.println("Received: " + stats.getReceived());
			System.out.println("Filtered: " + stats.getFiltered());
			System.out.println("Sent:     " + stats.getSent());
			System.out.println("Error:    " + stats.getError());
			System.out.println("");
		}

		disconnectClient();
	}

	private void commandStart() throws ClientException {
		connectClient();

		for (ChannelStatus channel : getMatchingChannelStatuses(selector)) {
			if (channel.getState().equals(State.PAUSED) || channel.getState().equals(State.STOPPED)) {
				if (channel.getState().equals(State.PAUSED)) {
					client.resumeChannel(channel.getChannelId());
					System.out.println("Channel '" + channel.getName() + "' Resumed");
				} else {
					client.startChannel(channel.getChannelId());
					System.out.println("Channel '" + channel.getName() + "' Started");
				}
			}
		}

		disconnectClient();
	}

	private void commandStop() throws ClientException {
		connectClient();

		for (ChannelStatus channel : getMatchingChannelStatuses(selector)) {
			if (channel.getState().equals(State.PAUSED) || channel.getState().equals(State.STARTED)) {
				client.stopChannel(channel.getChannelId());
				System.out.println("Channel '" + channel.getName() + "' Stopped");
			}
		}

		disconnectClient();
	}

	private void commandPause() throws ClientException {
		connectClient();

		for (ChannelStatus channel : getMatchingChannelStatuses(selector)) {
			if (channel.getState().equals(State.STARTED)) {
				client.pauseChannel(channel.getChannelId());
				System.out.println("Channel '" + channel.getName() + "' Paused");
			}
		}

		disconnectClient();
	}

	private void commandResume() throws ClientException {
		connectClient();

		for (ChannelStatus channel : getMatchingChannelStatuses(selector)) {
			if (channel.getState().equals(State.PAUSED)) {
				client.resumeChannel(channel.getChannelId());
				System.out.println("Channel '" + channel.getName() + "' Resumed");
			}
		}

		disconnectClient();
	}

	private void commandRemove() throws ClientException {
		connectClient();

		for (Channel channel : getMatchingChannels(selector)) {
			if (channel.isEnabled()) {
				channel.setEnabled(false);
			}
			client.removeChannel(channel);
			System.out.println("Channel '" + channel.getName() + "' Removed");
		}

		disconnectClient();
	}

	private void commandEnable() throws ClientException {
		connectClient();

		for (Channel channel : getMatchingChannels(selector)) {
			if (!channel.isEnabled()) {
				channel.setEnabled(true);
				client.updateChannel(channel, true);
				System.out.println("Channel '" + channel.getName() + "' Enabled");
			}
		}

		disconnectClient();
	}

	private void commandDisable() throws ClientException {
		connectClient();

		for (Channel channel : getMatchingChannels(selector)) {
			if (channel.isEnabled()) {
				channel.setEnabled(false);
				client.updateChannel(channel, true);
				System.out.println("Channel '" + channel.getName() + "' Disabled");
			}
		}

		disconnectClient();
	}

	private void commandClear() throws ClientException {
		connectClient();

		for (Channel channel : getMatchingChannels(selector)) {
			client.clearMessages(channel.getId());
			System.out.println("Channel '" + channel.getName() + "' Messages Cleared");
		}

		disconnectClient();
	}

	private void commandReset() throws ClientException {
		connectClient();

		for (Channel channel : getMatchingChannels(selector)) {
			client.clearStatistics(channel.getId(), true, true, true, true, true, true);
			System.out.println("Channel '" + channel.getName() + "' Stats Reset");
		}

		disconnectClient();
	}

	private List<ChannelStatus> getMatchingChannelStatuses(String key) throws ClientException {
		List<ChannelStatus> result = new ArrayList<ChannelStatus>();

		for (ChannelStatus status : client.getChannelStatusList()) {
			if (matchesChannel(key, status.getName(), status.getChannelId())) {
				result.add(status);
			}
		}

		return (result);
	}

	private List<Channel> getMatchingChannels(String key) throws ClientException {
		List<Channel> result = new ArrayList<Channel>();

		for (Channel channel : client.getChannel(null)) {
			if (matchesChannel(key, channel.getName(), channel.getId())) {
				result.add(channel);
			}
		}

		return (result);
	}

	private boolean matchesChannel(String key, String name, String id) {
		boolean ret = false;

		if (key.equals("*")) {
			ret = true;
		} else {
			ret = key.equalsIgnoreCase(name) || key.equalsIgnoreCase(id);
		}

		return (ret);
	}

}
