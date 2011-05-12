/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagepruner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.text.DateFormatter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.filters.MessageObjectFilter;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.util.PropertyLoader;

public class MessagePrunerService implements ServicePlugin, Job {
    private Logger logger = Logger.getLogger(this.getClass());
	private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
	private Scheduler sched = null;
	private SchedulerFactory schedFact = null;
	private JobDetail jobDetail = null;
	private static LinkedList<String[]> log;
	private static final int DEFAULT_PRUNING_BLOCK_SIZE = 0;
	private static final int LOG_SIZE = 250;
	private static boolean allowBatchPruning;
	private static int pruningBlockSize;
	
	private static final String PLUGIN_NAME = "Message Pruner";
	private static final String GET_LOG = "getLog";
	
	public void init(Properties properties) {
		jobDetail = new JobDetail("prunerJob", Scheduler.DEFAULT_GROUP, MessagePrunerService.class);

		try {
			if (properties.getProperty("allowBatchPruning") != null && properties.getProperty("allowBatchPruning").equals("1")) {
				allowBatchPruning = true;
			} else {
				allowBatchPruning = false;
			}
			
			if (StringUtils.isNotEmpty(properties.getProperty("pruningBlockSize"))) {
				pruningBlockSize = Integer.parseInt(properties.getProperty("pruningBlockSize"));
			} else {
				pruningBlockSize = DEFAULT_PRUNING_BLOCK_SIZE;
			}

			schedFact = new StdSchedulerFactory();
			sched = schedFact.getScheduler();
			sched.scheduleJob(jobDetail, createTrigger(properties));
			log = new LinkedList<String[]>();
		} catch (Exception e) {
			logger.error("error encountered in database pruner initialization", e);
		}
	}

	private Trigger createTrigger(Properties properties) throws ParseException {
		Trigger trigger = null;
		String interval = PropertyLoader.getProperty(properties, "interval");

		if (interval.equals("hourly"))
			trigger = TriggerUtils.makeHourlyTrigger();
		else {
			SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm aa");
			DateFormatter timeFormatter = new DateFormatter(timeDateFormat);

			String time = PropertyLoader.getProperty(properties, "time");
			Date timeDate = (Date) timeFormatter.stringToValue(time);
			Calendar timeCalendar = Calendar.getInstance();
			timeCalendar.setTime(timeDate);

			if (interval.equals("daily")) {
				trigger = TriggerUtils.makeDailyTrigger(timeCalendar.get(Calendar.HOUR_OF_DAY), timeCalendar.get(Calendar.MINUTE));
			} else if (interval.equals("weekly")) {
				SimpleDateFormat dayDateFormat = new SimpleDateFormat("EEEEEEEE");
				DateFormatter dayFormatter = new DateFormatter(dayDateFormat);

				String dayOfWeek = PropertyLoader.getProperty(properties, "dayOfWeek");
				Date dayDate = (Date) dayFormatter.stringToValue(dayOfWeek);
				Calendar dayCalendar = Calendar.getInstance();
				dayCalendar.setTime(dayDate);

				trigger = TriggerUtils.makeWeeklyTrigger(dayCalendar.get(Calendar.DAY_OF_WEEK), timeCalendar.get(Calendar.HOUR_OF_DAY), timeCalendar.get(Calendar.MINUTE));
			} else if (interval.equals("monthly")) {
				SimpleDateFormat dayDateFormat = new SimpleDateFormat("DD");
				DateFormatter dayFormatter = new DateFormatter(dayDateFormat);

				String dayOfMonth = PropertyLoader.getProperty(properties, "dayOfMonth");
				Date dayDate = (Date) dayFormatter.stringToValue(dayOfMonth);
				Calendar dayCalendar = Calendar.getInstance();
				dayCalendar.setTime(dayDate);

				trigger = TriggerUtils.makeMonthlyTrigger(dayCalendar.get(Calendar.DAY_OF_MONTH), timeCalendar.get(Calendar.HOUR_OF_DAY), timeCalendar.get(Calendar.MINUTE));
			}
		}

		trigger.setStartTime(new Date());
		trigger.setName("prunerTrigger");
		trigger.setJobName("prunerJob");
		return trigger;
	}

	public void start() {
		try {
			sched.start();
		} catch (Exception e) {
			logger.error("could not start message pruner", e);
		}
	}

	public void update(Properties properties) {
		try {
			if (properties.getProperty("allowBatchPruning") != null && properties.getProperty("allowBatchPruning").equals("1")) {
				allowBatchPruning = true;
			} else {
				allowBatchPruning = false;
			}
			
			if (StringUtils.isNotEmpty(properties.getProperty("pruningBlockSize"))) {
				pruningBlockSize = Integer.parseInt(properties.getProperty("pruningBlockSize"));
			} else {
				pruningBlockSize = DEFAULT_PRUNING_BLOCK_SIZE;
			}
			
			sched.deleteJob("prunerJob", Scheduler.DEFAULT_GROUP);
			sched.scheduleJob(jobDetail, createTrigger(properties));

			// for some reason, this does not work
			// sched.rescheduleJob("prunerJob", Scheduler.DEFAULT_GROUP,
			// createTrigger(properties));
		} catch (Exception e) {
			logger.error("could not reschedule the message pruner", e);
		}
	}

	public void onDeploy() {
	// TODO Auto-generated method stub

	}

	public void stop() {
		try {
			sched.shutdown();
		} catch (Exception e) {
			logger.error("could not exit message pruner", e);
		}
	}

	public Object invoke(String method, Object object, String sessionId) {
		if (method.equals(GET_LOG)) {
			return getLog();
		}

		return null;
	}

	private List<String[]> getLog() {
		return log;
	}

	public Properties getDefaultProperties() {
		Properties properties = new Properties();
		properties.put("name", PLUGIN_NAME);
		properties.put("interval", "daily");
		properties.put("time", "12:00 AM");
		properties.put("allowBatchPruning", "1");
		properties.put("pruningBlockSize", String.valueOf(DEFAULT_PRUNING_BLOCK_SIZE));
		return properties;
	}

	public Map<String, List<Channel>> getBatchedChannelMap() throws Exception {
		Map<String, List<Channel>> batchedChannelMap = new HashMap<String, List<Channel>>();

		for (Channel channel : channelController.getChannel(null)) {
			if ((channel.getProperties().getProperty("store_messages") != null) && channel.getProperties().getProperty("store_messages").equals("true")) {
				if ((channel.getProperties().getProperty("max_message_age") != null) && !channel.getProperties().getProperty("max_message_age").equals("-1")) {
					String numDays = channel.getProperties().getProperty("max_message_age");
					if (batchedChannelMap.get(numDays) == null) {
						batchedChannelMap.put(numDays, new ArrayList<Channel>());
					}

					batchedChannelMap.get(numDays).add(channel);
				}
			}
		}

		return batchedChannelMap;
	}

	// just get a map with one key and the list of all the channels
	public Map<String, List<Channel>> getChannelMap() throws Exception {
		Map<String, List<Channel>> channelMap = new HashMap<String, List<Channel>>();

		for (Channel channel : channelController.getChannel(null)) {
			if ((channel.getProperties().getProperty("store_messages") != null) && channel.getProperties().getProperty("store_messages").equals("true")) {
				if ((channel.getProperties().getProperty("max_message_age") != null) && !channel.getProperties().getProperty("max_message_age").equals("-1")) {
					String key = channel.getId();
					if (channelMap.get(key) == null) {
						channelMap.put(key, new ArrayList<Channel>());
					}

					channelMap.get(key).add(channel);
				}
			}
		}

		return channelMap;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("pruning message database");

		try {
			Map<String, List<Channel>> channelMap;

			if (allowBatchPruning) {
				channelMap = getBatchedChannelMap();
			} else {
				channelMap = getChannelMap();
			}

			for (List<Channel> channels : channelMap.values()) {
				// just check the first one
				int numDays = Integer.parseInt(channels.get(0).getProperties().getProperty("max_message_age"));
				String channelName;

				Calendar endDate = Calendar.getInstance();
				endDate.set(Calendar.DATE, endDate.get(Calendar.DATE) - numDays);

				MessageObjectFilter filter = new MessageObjectFilter();

				if (allowBatchPruning) {
					List<String> channelIdList = new ArrayList<String>();
					for (Channel channel : channels) {
						channelIdList.add(channel.getId());
					}
					filter.setChannelIdList(channelIdList);
					channelName = "Batch pruning: messages older than " + numDays + " days.";
				} else {
					filter.setChannelId(channels.get(0).getId());
					channelName = channels.get(0).getName();
				}

				filter.setEndDate(endDate);
				filter.setIgnoreQueued(true);

				int result = ControllerFactory.getFactory().createMessageObjectController().pruneMessages(filter, pruningBlockSize);

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(calendar.getTime());

				String date = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS:%1$tL", calendar);
				String numberRemoved = String.valueOf(result);

				if (log.size() == LOG_SIZE) {
					log.removeLast();
				}

				log.addFirst(new String[] { channelName, date, numberRemoved });
			}
		} catch (Exception e) {
			logger.warn("could not prune message database", e);
		}
	}

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        ExtensionPermission viewPermission = new ExtensionPermission(PLUGIN_NAME, "View Settings", "Displays the Message Pruner settings.", new String[] { Operations.PLUGIN_PROPERTIES_GET.getName(), GET_LOG }, new String[] { TaskConstants.SETTINGS_REFRESH });
        ExtensionPermission savePermission = new ExtensionPermission(PLUGIN_NAME, "Save Settings", "Allows changing the Message Pruner settings.", new String[] { Operations.PLUGIN_PROPERTIES_SET.getName() }, new String[] { TaskConstants.SETTINGS_SAVE });
        
        return new ExtensionPermission[] { viewPermission, savePermission };
    }
}
