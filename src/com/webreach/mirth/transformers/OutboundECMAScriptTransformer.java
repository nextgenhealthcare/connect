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

import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mule.umo.UMOEventContext;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.webreach.mirth.MirthUtil;
import com.webreach.mirth.components.ACKGenerator;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.GenericParser;

import ca.uhn.hl7v2.model.v21.message.ACK;
import ca.uhn.hl7v2.model.v231.message.ADT_A01;
import ca.uhn.hl7v2.model.v231.message.ORU_R01;
import ca.uhn.hl7v2.model.v24.message.*;
/**
 * Performs JavaScript transformation on incoming HL7 message.
 *  
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 */
public class OutboundECMAScriptTransformer {
	protected transient Log logger = LogFactory.getLog(OutboundECMAScriptTransformer.class);
	private String script;
	private String encodingVersion;
	private boolean enableEncoding;

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getEncodingVersion() {
		return encodingVersion;
		
	}

	public void setEncodingVersion(String encodingVersion) {
		this.encodingVersion = encodingVersion;
	}

	public boolean isEnableEncoding() {
		return enableEncoding;
	}

	public void setEnableEncoding(boolean enableEncoding) {
		this.enableEncoding = enableEncoding;
	}

	public Object onCall(UMOEventContext eventContext) throws Exception {
		return doOutboundTransform(eventContext.getTransformedMessageAsString());
	}

	public Object doOutboundTransform(String xmlMessage) {
		try {
			Context cx = Context.enter();
			Scriptable scope = cx.initStandardObjects();
			HashMap map = new HashMap();
			scope.put("xmlMessage", scope, xmlMessage);
			scope.put("logger", scope, logger);
			scope.put("map", scope, map);
			logger.info("Executing outbound transformation script");
			String jsSource = "function doOutboundTransform() { var msg = new XML(xmlMessage); " + script + " } doOutboundTransform()";
			Object resultObj = cx.evaluateString(scope, jsSource, "<cmd>", 1, null);
			
			//System.out.println("OUT TRANSFORMED MESSAGE: " + Context.jsToJava(resultObj, java.lang.String.class));
			
			if (resultObj != org.mozilla.javascript.Undefined.instance){
				Object retVal = Context.jsToJava(resultObj, Message.class);
				if (retVal != null){
					Message message = (Message) retVal;
					return (new GenericParser()).encode(message);
				}
			}
				return map;
			
			// turn the XML message into a Document object
			//InputSource is = new InputSource(new StringReader(message));
			//Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			//document.setXmlVersion("1.0");
			//document.setXmlStandalone(true);
			/*
			ORU_R01 oru = new ORU_R01();
			oru.getMSH().getFieldSeparator().setValue("|");
			oru.getMSH().getEncodingCharacters().setValue("^~\&");
			oru.getMSH().getMessageType().getMessageType().setValue("ORU");
			oru.getMSH().getMessageType().getTriggerEvent().setValue("R01");
			oru.getMSH().getMessageControlID().setValue("2040214114");
			oru.getMSH().getProcessingID().getProcessingID().setValue("P");
			oru.getMSH().get
			*/
			
			/*
			// FIXME: get rid of this before debugging HL7 output!
			setEnableEncoding(false);
		
			if (isEnableEncoding()) {
				Message m = (new DefaultXMLParser()).parseDocument(document, getEncodingVersion());

			} else
				return MirthUtil.serializeDocument(document, false);
			*/
		} catch (Exception e) {
			logger.error(e.toString());
			
			return null;
		} finally {
			Context.exit();
		}
	}
}
