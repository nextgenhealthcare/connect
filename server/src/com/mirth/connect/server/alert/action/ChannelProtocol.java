package com.mirth.connect.server.alert.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.model.Channel;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class ChannelProtocol implements Protocol {
    public final static String NAME = "Channel";

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
    public void doCustomDispatch(List<String> channelIds, String subject, String content) {
        for (String channelId : channelIds) {
            try {
                engineController.dispatchRawMessage(channelId, new RawMessage(content), false, true);
            } catch (Exception e) {
                logger.warn("Could not send alert to channel " + channelId, e);
            }
        }
    }
}
