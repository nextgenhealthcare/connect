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
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelProperties;
import com.mirth.connect.model.Event;
import com.mirth.connect.model.Event.Level;
import com.mirth.connect.model.Event.Outcome;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.SystemEventController;
import com.mirth.connect.util.messagewriter.MessageWriterFactory;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class MessagePrunerJob implements Job {

    private ChannelController channelController = ChannelController.getInstance();
    private SystemEventController systemEventController = SystemEventController.getInstance();
    private Logger logger = Logger.getLogger(getClass());

    public MessagePrunerJob() {
        List<Status> skipStatuses = new ArrayList<Status>();
        skipStatuses.add(Status.ERROR);
        skipStatuses.add(Status.QUEUED);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.debug("pruning messages");
        JobDataMap jobDataMap = context.getTrigger().getJobDataMap();
        int pruningBlockSize = jobDataMap.getIntValue("pruningBlockSize");
        Boolean archiveEnabled = (Boolean) jobDataMap.get("archiveEnabled");
        MessageWriterOptions archiverOptions = (MessageWriterOptions) jobDataMap.get("archiverOptions");
        Encryptor encryptor = ConfigurationController.getInstance().getEncryptor();
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
                            MessagePruner messagePruner;

                            if (archiveEnabled != null && archiveEnabled && properties.isArchiveEnabled()) {
                                messagePruner = new MessagePrunerWithArchiver(MessageWriterFactory.getInstance().getMessageWriter(archiverOptions, encryptor, channel.getId()));
                            } else {
                                messagePruner = new MessagePrunerWithoutArchiver();
                            }

                            messagePruner.setBlockSize(pruningBlockSize);

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
                systemEventController.addEvent(event);
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
                systemEventController.addEvent(event);

                logger.error("Could not prune messages for channel: " + channel.getName(), e);
            }
        }
    }
}
