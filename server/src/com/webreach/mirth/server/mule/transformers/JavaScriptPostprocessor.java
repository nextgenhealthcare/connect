package com.webreach.mirth.server.mule.transformers;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.util.JavaScriptUtil;

public class JavaScriptPostprocessor {
	private Logger logger = Logger.getLogger(this.getClass());

	public void doPostProcess(MessageObject messageObject) {
		JavaScriptUtil.getInstance().executeScript(messageObject.getChannelId() + "_Postprocessor", "postprocessor", messageObject);
		JavaScriptUtil.getInstance().executeScript("Postprocessor", "postprocessor", messageObject);
		String channelId = messageObject.getChannelId();
		
		HashMap<String, Channel> channelCache = ControllerFactory.getFactory().createChannelController().getChannelCache();
		
		// Check the cache for the channel
		if (channelCache != null && channelCache.containsKey(channelId)) {
			Channel channel = channelCache.get(channelId);
			if (channel.getProperties().containsKey("store_messages")) {
				if (channel.getProperties().get("store_messages").equals("false") || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("error_messages_only").equals("true") && !messageObject.getStatus().equals(MessageObject.Status.ERROR)) || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("dont_store_filtered").equals("true") && messageObject.getStatus().equals(MessageObject.Status.FILTERED))) {
					// message is not stored, remove attachment
					ControllerFactory.getFactory().createMessageObjectController().deleteAttachments(messageObject);
				}
			}
		}
	}

	public void doPostProcess(Object object) throws IllegalArgumentException {
		if (object instanceof MessageObject) {
			doPostProcess((MessageObject) object);
		} else {
			logger.error("Could not postprocess, object is not of type MessageObject. Cannot wait for response on Channel Writer: None.");
			throw new IllegalArgumentException("Object is not of type MessageObject");
		}
	}
}
