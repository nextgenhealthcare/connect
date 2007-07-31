package com.webreach.mirth.server.mule.transformers;

import org.apache.log4j.Logger;
import com.webreach.mirth.model.MessageObject;

public class JavaScriptPostprocessor {
	private Logger logger = Logger.getLogger(this.getClass());
	public MessageObject doPostProcess(MessageObject messageObject){
		System.out.println("Doing postprocess: " + messageObject.getId());
		return messageObject;
	}
	public MessageObject doPostProcess(Object object) throws IllegalArgumentException{
		if (object instanceof MessageObject){
			return doPostProcess((MessageObject)object);
		}else{
			logger.error("could not postprocess, object is not of type MessageObject");
			throw new IllegalArgumentException("Object is not of type MessageObject");
		}
	}
}
