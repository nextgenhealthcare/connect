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


package com.webreach.mirth.managers;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ChangeManager {
	protected transient Log logger = LogFactory.getLog(ChangeManager.class);
	private boolean initialized = false;
	private boolean configurationChanged = false;
	private ArrayList<Integer> changedChannels = new ArrayList<Integer>();

	// singleton pattern
	private static ChangeManager instance = null;

	private ChangeManager() {}

	public static ChangeManager getInstance() {
		synchronized (ChangeManager.class) {
			if (instance == null)
				instance = new ChangeManager();

			return instance;
		}
	}

	public void initialize() {
		if (initialized)
			return;

		// initialization code

		initialized = true;
	}

	/**
	 * Flags a channel as changed.
	 * 
	 * @param channelId
	 */
	public void changeChannel(int channelId) {
		changedChannels.add(new Integer(channelId));
	}

	/**
	 * Returns <code>true</code> if a channel has been changed,
	 * <code>false</code> otherwise.
	 * 
	 * @param name
	 * @return <code>true</code> if a channel has been changed,
	 *         <code>false</code> otherwise.
	 */
	public boolean isChannelChanged(String name) {
		Integer channelId = new Integer(name.substring(0, name.indexOf(ConfigurationManager.ID_NAME_DELIMETER))).intValue();
		return changedChannels.contains(new Integer(channelId));
	}

	/**
	 * Returns <code>true</code> if the configuration file has been changed,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the configuration file has been changed,
	 *         <code>false</code> otherwise.
	 */
	public boolean isConfigurationChanged() {
		return this.configurationChanged;
	}

	/**
	 * Set to <code>true</code> is the configuration file has been changd,
	 * <code>false</code> otherwise.
	 * 
	 * @param configurationChanged
	 */
	public void setConfigurationChanged(boolean configurationChanged) {
		this.configurationChanged = configurationChanged;
	}

	/**
	 * Marks all channels as unchanged.
	 * 
	 */
	public void resetChannels() {
		changedChannels.clear();
	}
}
