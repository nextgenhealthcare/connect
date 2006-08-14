package com.webreach.mirth.server.mule.components;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;

import com.webreach.mirth.server.controllers.ChannelStatisticsController;

public class ChannelComponent implements Callable {
	private Logger logger = Logger.getLogger(this.getClass());
	public static HashMap globalMap = new HashMap();
	private ChannelStatisticsController statisticsController = new ChannelStatisticsController();
	
	public Object onCall(UMOEventContext eventContext) throws Exception {
		int channelId = Integer.parseInt(eventContext.getComponentDescriptor().getName());
		statisticsController.updateStatistics(channelId);
		return eventContext.getTransformedMessage();
	}

}
