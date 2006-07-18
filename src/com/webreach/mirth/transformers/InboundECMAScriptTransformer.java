/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.transformers;

import java.io.OutputStream;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mule.umo.UMOEventContext;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;

import com.webreach.mirth.managers.LogManager;
import com.webreach.mirth.managers.MessageManager;
import com.webreach.mirth.managers.types.MirthMessage;

/**
 * Performs JavaScript transformation on incoming HL7 message.
 *  
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * @since 1.0
 */
public class InboundECMAScriptTransformer {
	protected transient Log logger = LogFactory.getLog(InboundECMAScriptTransformer.class);
	private String script;
	
	private MessageManager messageManager = MessageManager.getInstance();
	private LogManager logManager = LogManager.getInstance();

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public Object onCall(UMOEventContext eventContext) throws Exception {
		//Write replies - CL
		//OutputStream os = eventContext.getOutputStream();
		//os.write("Test!".getBytes());
		//os.flush();
		//os.close();
		
		PipeParser parser = new PipeParser();

		// FIXME: this is a fix for the HTTP messages
		Message hapiMessage = parser.parse(eventContext.getMessageAsString().substring(eventContext.getMessageAsString().indexOf("=") + 1, eventContext.getMessageAsString().length()));
		
		Terser terser = new Terser(hapiMessage);
        String sendingFacility = terser.get("/MSH-3-1");
        String controlId = terser.get("/MSH-10");
		
		MirthMessage message = new MirthMessage();
		message.setChannel(eventContext.getComponentDescriptor().getName());
		message.setEvent(hapiMessage.getName());
		message.setSendingFacility(sendingFacility);
		message.setControlId(controlId);
		message.setSize(String.valueOf((eventContext.getTransformedMessageAsString().getBytes().length)));
		message.setContent(eventContext.getMessageAsString());
		message.setContentXml(eventContext.getTransformedMessageAsString());
		messageManager.addMessage(message);

		try {
			Object result = doTransform(eventContext.getTransformedMessageAsString());
			logManager.log(eventContext.getComponentDescriptor().getName(), "Processed message.");
			
			return result;
		} catch (Exception e) {
			logManager.log(eventContext.getComponentDescriptor().getName(), "Could not process message.");
		}
		
		return null;
	}

	public Object doTransform(String message) throws Exception {
		try {
			Context context = Context.enter();
			Scriptable scope = context.initStandardObjects();
			HashMap map = new HashMap();
			
			scope.put("message", scope, message);
			scope.put("logger", scope, logger);
			scope.put("map", scope, map);
			
			String jsSource = "function debug(debug_message) { logger.debug(debug_message) } function doTransform() { default xml namespace = new Namespace(\"urn:hl7-org:v2xml\"); var msg = new XML(message); " + script + " } doTransform()";
			context.evaluateString(scope, jsSource, "<cmd>", 1, null);
			
			return map;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			Context.exit();
		}
	}
}
