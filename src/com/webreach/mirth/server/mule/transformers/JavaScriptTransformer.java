package com.webreach.mirth.server.mule.transformers;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import com.webreach.mirth.model.MessageEvent;
import com.webreach.mirth.server.controllers.MessageLogger;
import com.webreach.mirth.server.mule.components.ChannelComponent;
import com.webreach.mirth.server.mule.util.ER7Util;

public class JavaScriptTransformer extends AbstractTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private String script;
	private final String HL7XML = "HL7 XML";
	private final String HL7ER7 = "HL7 ER7";

	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public Object doTransform(Object source) throws TransformerException {
		try {
		
			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);
						
			HashMap localMap = new HashMap();
			
			// load variables in JavaScript scope
			scope.put("message", scope, source);
			scope.put("incomingMessage", scope, ((String) new ER7Util().ConvertToER7((String) source)));
			scope.put("logger", scope, logger);
			scope.put("localMap", scope, localMap);
			scope.put("globalMap", scope, ChannelComponent.globalMap);

			StringBuilder jsSource = new StringBuilder();
			jsSource.append("importPackage(Packages.com.webreach.mirth.server.util);");
			jsSource.append("function doTransform() { default xml namespace = new Namespace(\"urn:hl7-org:v2xml\"); var msg = new XML(message); " + script + " }");
			jsSource.append("doTransform()\n");

			logger.debug("executing transformation script:\n\t" + jsSource.toString().replace("\\", "\\\\"));
			context.evaluateString(scope, jsSource.toString().replace("\\", "\\\\"), "<cmd>", 1, null);

			localMap.put(HL7ER7, new ER7Util().ConvertToER7(localMap.get(HL7XML).toString()).replace("\\E", ""));
			String channelId = Context.toString(scope.get("channelid", scope));

			logMessageEvent(localMap, channelId);
			return localMap;
		} catch (Exception e) {
			throw new TransformerException(this, e);
		} finally {
			Context.exit();
		}
	}
	
	private void logMessageEvent(HashMap er7data, String channelID) throws Exception {
		String er7message = (String) er7data.get("HL7 ER7");
		logger.debug("logging message:\n" + er7message);

		int channelId = Integer.parseInt(channelID);

		PipeParser pipeParser = new PipeParser();
		pipeParser.setValidationContext(new NoValidation());
		Message message = pipeParser.parse(er7message);
		Terser terser = new Terser(message);
		String sendingFacility = terser.get("/MSH-3-1");
		String controlId = terser.get("/MSH-10");
		String event = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2") + " (" + message.getVersion() + ")";

		MessageLogger messageLogger = new MessageLogger();
		MessageEvent messageEvent = new MessageEvent();
		messageEvent.setChannelId(channelId);
		messageEvent.setSendingFacility(sendingFacility);
		messageEvent.setEvent(event);
		messageEvent.setControlId(controlId);
		messageEvent.setMessage(er7message);
		messageEvent.setStatus(MessageEvent.Status.RECEIVED);
		messageLogger.logMessageEvent(messageEvent);
	}
}
