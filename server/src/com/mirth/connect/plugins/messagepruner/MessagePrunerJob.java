/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagepruner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.Event;
import com.mirth.connect.model.Event.Level;
import com.mirth.connect.model.Event.Outcome;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.MessagePrunerException;

public class MessagePrunerJob implements Job {

    private ChannelController channelController = ChannelController.getInstance();
    private EventController eventController = EventController.getInstance();
    private Logger logger = Logger.getLogger(getClass());
    private MessagePruner messagePruner;

    public MessagePrunerJob() {
        // TODO: initialize archiver
        // messagePruner.setMessageArchiver(archiver);

        List<Status> skipStatuses = new ArrayList<Status>();
        skipStatuses.add(Status.ERROR);
        skipStatuses.add(Status.QUEUED);

        DefaultMessagePruner messagePruner = new DefaultMessagePruner();
        messagePruner.setRetryCount(3);
        messagePruner.setSkipIncomplete(true);
        messagePruner.setSkipStatuses(skipStatuses);
        this.messagePruner = messagePruner;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.debug("pruning messages");
        List<Channel> channels = null;

        try {
            channels = channelController.getChannel(null);
        } catch (ControllerException e) {
            logger.error("Failed to retrieve a list of all channels for pruning");
            return;
        }

        for (Channel channel : channels) {
            try {
                ChannelProperties properties = channel.getProperties();
                Integer pruneMetaDataDays = properties.getPruneMetaDataDays();
                Integer pruneContentDays = properties.getPruneContentDays();
                Calendar contentDateThreshold = null;
                Calendar messageDateThreshold = null;
                int[] numPruned = new int[] { 0, 0 };

                switch (properties.getMessageStorageMode()) {
                    case DEVELOPMENT:
                    case PRODUCTION:
                    case RAW:
                        if (pruneContentDays != null) {
                            contentDateThreshold = Calendar.getInstance();
                            contentDateThreshold.set(Calendar.DAY_OF_MONTH, contentDateThreshold.get(Calendar.DAY_OF_MONTH) - pruneContentDays);
                        }

                    case METADATA:
                        if (pruneMetaDataDays != null) {
                            messageDateThreshold = Calendar.getInstance();
                            messageDateThreshold.set(Calendar.DAY_OF_MONTH, messageDateThreshold.get(Calendar.DAY_OF_MONTH) - pruneMetaDataDays);
                        }

                        if (messageDateThreshold != null || contentDateThreshold != null) {
                            numPruned = messagePruner.executePruner(channel.getId(), messageDateThreshold, contentDateThreshold);
                        }
                        break;

                    case DISABLED:
                        break;

                    default:
                        throw new MessagePrunerException("Unrecognized message storage mode: " + properties.getMessageStorageMode().toString());
                }

                Map<String, String> attributes = new HashMap<String, String>();
                attributes.put("Channel", channel.getName());
                attributes.put("Messages Pruned", Integer.toString(numPruned[0]));
                attributes.put("Content Rows Pruned", Integer.toString(numPruned[1]));

                Event event = new Event();
                event.setLevel(Level.INFORMATION);
                event.setOutcome(Outcome.SUCCESS);
                event.setName(MessagePrunerService.PLUGINPOINT);
                event.setAttributes(attributes);
                eventController.addEvent(event);
            } catch (Exception e) {
                Map<String, String> attributes = new HashMap<String, String>();
                attributes.put("channel", channel.getName());
                attributes.put("error", e.getMessage());
                attributes.put("trace", ExceptionUtils.getStackTrace(e));

                Event event = new Event();
                event.setLevel(Level.INFORMATION);
                event.setOutcome(Outcome.FAILURE);
                event.setName(MessagePrunerService.PLUGINPOINT);
                event.setAttributes(attributes);
                eventController.addEvent(event);

                logger.warn("could not prune messages for channel: " + channel.getName(), e);
            }
        }
    }
}
