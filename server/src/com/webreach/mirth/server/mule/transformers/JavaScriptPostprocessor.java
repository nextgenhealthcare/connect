package com.webreach.mirth.server.mule.transformers;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.util.CompiledScriptCache;
import com.webreach.mirth.server.util.JavaScriptUtil;

public class JavaScriptPostprocessor {
	private Logger logger = Logger.getLogger(this.getClass());
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	public void doPostProcess(MessageObject messageObject){
		JavaScriptUtil.getInstance().executeScript(messageObject.getChannelId() + "_Postprocessor", "postprocessor", messageObject);
		JavaScriptUtil.getInstance().executeScript("Postprocessor", "postprocessor", messageObject);
	}
	public void doPostProcess(Object object) throws IllegalArgumentException{
		if (object instanceof MessageObject){
			doPostProcess((MessageObject)object);
		}else{
			logger.error("could not postprocess, object is not of type MessageObject");
			throw new IllegalArgumentException("Object is not of type MessageObject");
		}
	}
}
