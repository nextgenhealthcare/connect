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


package com.webreach.mirth.server.util;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.MessageObjectController;

public class DatabasePruner extends Thread {
	private Logger logger = Logger.getLogger(this.getClass());
	private ChannelController channelController = new ChannelController();
	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private static final int SLEEP_INTERVAL = 1000 * 60;// * 60; // prune every 5 mintes

	public DatabasePruner() {
		setName("DatabasePruner");
	}
	
	public void run() {
        try {
    		while (true) {
    			pruneDatabase();
			    sleep(SLEEP_INTERVAL);
    		}
        }
        catch (InterruptedException e) {
            logger.debug("exiting database pruner");
        }
	}

	public void pruneDatabase() {
		logger.debug("pruning database");
		
		try {
			List<Channel> channels = channelController.getChannel(null);

			for (Iterator iter = channels.iterator(); iter.hasNext();) {
				Channel channel = (Channel) iter.next();

				if ((channel.getProperties().getProperty("store_messages") != null) && channel.getProperties().getProperty("store_messages").equals("true")) {
					if ((channel.getProperties().getProperty("max_message_age") != null) && !channel.getProperties().getProperty("max_message_age").equals("-1")) {
						int numDays = Integer.parseInt(channel.getProperties().getProperty("max_message_age"));

						Calendar endDate = Calendar.getInstance();
						endDate.set(Calendar.DATE, endDate.get(Calendar.DATE) - numDays);

						MessageObjectFilter filter = new MessageObjectFilter();
						filter.setChannelId(channel.getId());
						filter.setEndDate(endDate);
						messageObjectController.removeMessages(filter);
					}
				}
			}
		} catch (Exception e) {
			logger.warn("could not prune database", e);
		}
	}
}
