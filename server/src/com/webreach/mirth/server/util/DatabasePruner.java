package com.webreach.mirth.server.util;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.MessageObjectController;

public class DatabasePruner extends Thread {
	private Logger logger = Logger.getLogger(this.getClass());
	private ChannelController channelController = new ChannelController();
	private MessageObjectController messageObjectController = new MessageObjectController();
	private static final int SLEEP_INTERVAL = 60000;

	public DatabasePruner() {
		setName("DatabasePruner");
	}
	
	public void run() {
		while (!isInterrupted()) {
			pruneDatabase();
			
			try {
				sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
				logger.debug("exiting database pruner");
			}
		}
	}

	public void pruneDatabase() {
		logger.info("pruining database");
		
		try {
			List<Channel> channels = channelController.getChannels(null);

			for (Iterator iter = channels.iterator(); iter.hasNext();) {
				Channel channel = (Channel) iter.next();

				if ((channel.getProperties().getProperty("store_messages") != null) && channel.getProperties().getProperty("store_messages").equals("true")) {
					if (channel.getProperties().getProperty("max_message_age") != null) {
						int numDays = Integer.parseInt(channel.getProperties().getProperty("max_message_age"));

						Calendar endDate = Calendar.getInstance();
						endDate.set(Calendar.DATE, endDate.get(Calendar.DATE) - numDays);

						MessageObjectFilter filter = new MessageObjectFilter();
						filter.setStatus(MessageObject.Status.TRANSFORMED);
						filter.setChannelId(channel.getId());
						filter.setEndDate(endDate);
						messageObjectController.removeMessages(filter);
					}
				}
			}
		} catch (Exception e) {
			logger.error("could not prune database", e);
		}
	}
}
