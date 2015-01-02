package com.mirth.connect.server.alert.action;

import java.util.List;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class ChannelDispatcher implements Dispatcher {
    private Logger logger = Logger.getLogger(getClass());
    private EngineController engineController = ControllerFactory.getFactory().createEngineController();

    @Override
    public void dispatch(List<String> channelIds, String subject, String content) {
        for (String channelId : channelIds) {
            try {
                engineController.dispatchRawMessage(channelId, new RawMessage(content), false, true);
            } catch (Exception e) {
                logger.warn("Could not send alert to channel " + channelId, e);
            }
        }
    }
}
