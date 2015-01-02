package com.mirth.connect.server.alert.action;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.model.Channel;
import com.mirth.connect.server.controllers.ControllerFactory;

public class ChannelProtocol implements Protocol {
    public final static String NAME = "Channel";

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
    public Dispatcher getDispatcher() {
        return new ChannelDispatcher();
    }
}
