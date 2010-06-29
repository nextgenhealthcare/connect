/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.transformers;

import java.util.Map;

import org.apache.log4j.Logger;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.JavaScriptUtil;

public class JavaScriptPostprocessor {
	private Logger logger = Logger.getLogger(this.getClass());

	public void doPostProcess(MessageObject messageObject) {
		JavaScriptUtil.getInstance().executeScript(messageObject.getChannelId() + "_Postprocessor", "postprocessor", messageObject);
		JavaScriptUtil.getInstance().executeScript("Postprocessor", "postprocessor", messageObject);
		String channelId = messageObject.getChannelId();
		
		Map<String, Channel> channelCache = ControllerFactory.getFactory().createChannelController().getChannelCache();
		
		// Check the cache for the channel
		if (channelCache != null && channelCache.containsKey(channelId)) {
			Channel channel = channelCache.get(channelId);
			if (channel.getProperties().containsKey("store_messages")) {
				if (channel.getProperties().get("store_messages").equals("false") || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("error_messages_only").equals("true") && !messageObject.getStatus().equals(MessageObject.Status.ERROR)) || (channel.getProperties().get("store_messages").equals("true") && channel.getProperties().get("dont_store_filtered").equals("true") && messageObject.getStatus().equals(MessageObject.Status.FILTERED))) {
				    // message is not stored, remove attachment if there is one
                    if (messageObject.isAttachment()) {
                        ControllerFactory.getFactory().createMessageObjectController().deleteAttachments(messageObject);
                    }
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
