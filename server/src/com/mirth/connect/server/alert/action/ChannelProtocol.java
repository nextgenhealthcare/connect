/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.alert.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.model.Channel;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class ChannelProtocol implements Protocol {
    public final static String NAME = "Channel";

    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private EngineController engineController = ControllerFactory.getFactory().createEngineController();
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, String> getRecipientOptions() {
        Map<String, String> options = new HashMap<String, String>();

        for (Channel channelModel : ControllerFactory.getFactory().createChannelController().getChannels(null)) {
            options.put(channelModel.getId(), channelModel.getName());
        }

        return options;
    }

    @Override
    public List<String> getEmailAddressesForDispatch(List<String> channelIds) {
        return null;
    }

    @Override
    public void doCustomDispatch(List<String> channelIdsOrName, String subject, String content) {
        for (String channelIdOrName : channelIdsOrName) {
            Channel channel = channelController.getDeployedChannelById(channelIdOrName);

            if (channel == null) {
                channel = channelController.getDeployedChannelByName(channelIdOrName);
            }

            if (channel != null) {
                try {
                    engineController.dispatchRawMessage(channel.getId(), new RawMessage(content), false, true);
                } catch (Exception e) {
                    logger.warn("Could not send alert to channel " + channelIdOrName, e);
                }
            } else {
                logger.warn("Could not send alert to channel " + channelIdOrName);
            }
        }
    }
}
