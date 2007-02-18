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

package com.webreach.mirth.server.mule.transformers;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Connector.Mode;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerFactory;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.controllers.TemplateController;
import com.webreach.mirth.server.mule.adaptors.Adaptor;
import com.webreach.mirth.server.mule.adaptors.AdaptorFactory;
import com.webreach.mirth.server.mule.util.CompiledScriptCache;
import com.webreach.mirth.server.mule.util.GlobalVariableStore;
import com.webreach.mirth.server.util.StackTracePrinter;

public class JavaScriptTransformer extends AbstractTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private MessageObjectController messageObjectController = new MessageObjectController();
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private ScriptController scriptController = new ScriptController();

	private String inboundProtocol;
	private String outboundProtocol;
	private Map inboundProperties;
	private Map outboundProperties;
	private String channelId;
	private String connectorName;
	private boolean encryptData;
	private String transformerScriptId;
	private String filterScriptId;
	private String templateId;
	private String mode;
	private String template;
	public String getChannelId() {
		return this.channelId;
	}

	public String getMode() {
		return this.mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getInboundProtocol() {
		return this.inboundProtocol;
	}

	public void setInboundProtocol(String inboundProtocol) {
		this.inboundProtocol = inboundProtocol;
	}

	public String getOutboundProtocol() {
		return this.outboundProtocol;
	}

	public void setOutboundProtocol(String outboundProtocol) {
		this.outboundProtocol = outboundProtocol;
	}

	public String getConnectorName() {
		return this.connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public boolean isEncryptData() {
		return this.encryptData;
	}

	public void setEncryptData(boolean encryptData) {
		this.encryptData = encryptData;
	}

	public String getFilterScriptId() {
		return this.filterScriptId;
	}

	public void setFilterScriptId(String filterScriptId) {
		this.filterScriptId = filterScriptId;
	}

	public String getTransformerScriptId() {
		return this.transformerScriptId;
	}

	public void setTransformerScriptId(String transformerScriptId) {
		this.transformerScriptId = transformerScriptId;
	}

	public String getTemplateId() {
		return this.templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
		TemplateController templateController = new TemplateController();
		try {
			template = templateController.getTemplate(templateId);
		} catch (ControllerException e) {
			logger.error("Unable to get template: " + templateId);
		}
	}

	@Override
	public void initialise() throws InitialisationException {
		try {
			Context context = Context.enter();

			String filterScript = scriptController.getScript(filterScriptId);
			if (filterScript != null) {
				String generatedFilterScript = generateFilterScript(filterScript);
				logger.debug("compiling filter script");
				Script compiledFilterScript = context.compileString(generatedFilterScript, filterScriptId, 1, null);
				compiledScriptCache.putCompiledScript(filterScriptId, compiledFilterScript);
			}

			String transformerScript = scriptController.getScript(transformerScriptId);
			
			if (transformerScript != null) {
				String generatedTransformerScript = generateTransformerScript(transformerScript);
				logger.debug("compiling transformer script");
				Script compiledTransformerScript = context.compileString(generatedTransformerScript, transformerScriptId, 1, null);
				compiledScriptCache.putCompiledScript(transformerScriptId, compiledTransformerScript);
			}

		} catch (Exception e) {
			throw new InitialisationException(e, this);
		} finally {
			Context.exit();
		}
	}

	@Override
	public Object doTransform(Object source) throws TransformerException {
		MessageObject messageObject = null;
		
		if (this.getMode().equals(Mode.SOURCE.toString())){
			try {
				Adaptor adaptor = AdaptorFactory.getAdaptor(Protocol.valueOf(inboundProtocol));
				messageObject = adaptor.getMessage((String) source, channelId, encryptData, inboundProperties);	
			} catch (Exception e) {
				messageObject = null;
			}
		}else if (this.getMode().equals(Mode.DESTINATION.toString())){
			messageObject = (MessageObject)source;
			messageObject = messageObjectController.cloneMessageObjectForBroadcast(messageObject, this.getConnectorName());
			try{
				Adaptor adaptor = AdaptorFactory.getAdaptor(Protocol.valueOf(outboundProtocol));
				messageObject = adaptor.convertMessage(messageObject, template, channelId, encryptData, outboundProperties);
			}catch (Exception e){
				
			}
			messageObject.setEncodedDataProtocol(Protocol.valueOf(this.outboundProtocol));		
		}
		if (evaluateFilterScript(messageObject)) {
			MessageObject transformedMessageObject = evaluateTransformerScript(messageObject);
			if (this.getMode().equals(Mode.SOURCE.toString())){
				messageObjectController.updateMessage(transformedMessageObject);
			}
			return transformedMessageObject;
		} else {
			messageObject.setStatus(MessageObject.Status.REJECTED);
			return messageObject;
		}
	}

	private boolean evaluateFilterScript(MessageObject messageObject) {
		try {
			Logger scriptLogger = Logger.getLogger("filter");

			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);

			// load variables in JavaScript scope
			scope.put("logger", scope, scriptLogger);
			scope.put("message", scope, messageObject.getTransformedData());
			scope.put("localMap", scope, messageObject.getVariableMap());
			scope.put("globalMap", scope, GlobalVariableStore.getInstance());
			scope.put("messageObject", scope, messageObject);
			// get the script from the cache and execute it
			Script compiledScript = compiledScriptCache.getCompiledScript(filterScriptId);
			Object result = null;
			boolean messageAccepted;

			if (compiledScript == null) {
				logger.error("filter script could not be found in cache");
				messageAccepted = false;
			} else {
				result = compiledScript.exec(context, scope);
				messageAccepted = ((Boolean) Context.jsToJava(result, java.lang.Boolean.class)).booleanValue();
			}

			if (!messageAccepted) {
				messageObject.setStatus(MessageObject.Status.REJECTED);
				messageObjectController.updateMessage(messageObject);
			} else {
				messageObject.setStatus(MessageObject.Status.ACCEPTED);
			}

			return messageAccepted;
		} catch (Exception e) {
			logger.error("error ocurred in filter", e);
			messageObject.setStatus(MessageObject.Status.ERROR);
			messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" + StackTracePrinter.stackTraceToString(e));
			messageObjectController.updateMessage(messageObject);
			return false;
		} finally {
			Context.exit();
		}
	}

	private MessageObject evaluateTransformerScript(MessageObject messageObject) throws TransformerException {
		try {
			Logger scriptLogger = Logger.getLogger(getMode().toLowerCase() + "-transformation");
			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);			
			// load variables in JavaScript scope
			scope.put("logger", scope, scriptLogger);
			scope.put("message", scope, messageObject.getTransformedData());
			scope.put("template", scope, template);
			scope.put("localMap", scope, messageObject.getVariableMap());
			scope.put("globalMap", scope, GlobalVariableStore.getInstance());
			scope.put("messageObject", scope, messageObject);
			//TODO: have function list provide all serializers - maybe we import a top level package or create a helper class
			//TODO: this is going to break backwards compatability
			//scope.put("serializer", scope, SerializerFactory.getSerializer(Protocol.valueOf(inboundProtocol), this.get));

			// get the script from the cache and execute it
			Script compiledScript = compiledScriptCache.getCompiledScript(transformerScriptId);

			if (compiledScript == null) {
				logger.warn("transformer script could not be found in cache");
			} else {
				compiledScript.exec(context, scope);
			}
			
			//TODO: Check logic here
			Object transformedData;
			Protocol encodedDataProtocol;
			Map encodedDataProperties;
			if (template != null) {
				transformedData = scope.get("tmp", scope);
				encodedDataProtocol = Protocol.valueOf(this.getOutboundProtocol());
				encodedDataProperties = this.getOutboundProperties();
			} else {
				transformedData = scope.get("msg", scope);
				encodedDataProtocol = Protocol.valueOf(this.getInboundProtocol());
				encodedDataProperties = this.getInboundProperties();
			}

			if (transformedData != Scriptable.NOT_FOUND) {
				// set the transformedData to the template
				messageObject.setTransformedData(Context.toString(transformedData));
			}
			
			if(this.getMode().equals(Mode.DESTINATION.toString())){
				// take the now transformed message and convert it back to ER7
				//TODO: Fix the logic here to set the proper encoded data back
				if ((messageObject.getTransformedData() != null)) {
					IXMLSerializer<String> serializer = SerializerFactory.getSerializer(encodedDataProtocol, encodedDataProperties);
					messageObject.setEncodedData(serializer.fromXML(messageObject.getTransformedData()));
				} 
			}
			messageObject.setStatus(MessageObject.Status.TRANSFORMED);
			return messageObject;
		} catch (Exception e) {
			messageObject.setStatus(MessageObject.Status.ERROR);
			messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" + StackTracePrinter.stackTraceToString(e));
			messageObjectController.updateMessage(messageObject);
			throw new TransformerException(this, e);
		} finally {
			Context.exit();
		}
	}

	private String generateFilterScript(String filterScript) {
		logger.debug("generating filter script");
		StringBuilder script = new StringBuilder();
		script.append("importPackage(Packages.com.webreach.mirth.server.util);\n	");
		script.append("function $(string) { if (globalMap.get(string) != null) { return globalMap.get(string)} else { return localMap.get(string);} }");
		script.append("function doFilter() {");

		if (inboundProtocol.equals(Protocol.HL7V2.toString())) {
			//script.append("default xml namespace = new Namespace(\"urn:hl7-org:v2xml\");");
		}

		script.append("var msg = new XML(message);\n " + filterScript + " }\n");
		script.append("doFilter()\n");
		return script.toString();
	}

	private String generateTransformerScript(String transformerScript) {
		logger.debug("generator transformer script");
		StringBuilder script = new StringBuilder();
		script.append("importPackage(Packages.com.webreach.mirth.server.util);");
		// script used to check for exitence of segment
		script.append("function validate(mapping, defaultValue, replacement) { var result = ''; if (mapping != undefined) {result = mapping.toString();} if (result.length == 0){result = defaultValue;} if (replacement != undefined){ for (i = 0; i < replacement.length; i++){ var entry = replacement[0]; result = result.replace(entry[0],entry[1]); \n} }return result; }");
		script.append("function $(string) { if (globalMap.get(string) != null) { return globalMap.get(string)} else { return localMap.get(string);} }");
		script.append("function doTransform() {");

		// only set the namespace to hl7-org if the message is XML
		if (inboundProtocol.equals(Protocol.HL7V2.toString())) {
			//script.append("default xml namespace = new Namespace(\"urn:hl7-org:v2xml\");");
		}

		// ast: Allow ending whitespaces from the input XML
		script.append("XML.ignoreWhitespace=false;");
		// ast: Allow ending whitespaces to the output XML
		script.append("XML.prettyPrinting=false;");
		// turn the template into an E4X XML object

		if (template != null){
			script.append("tmp = new XML(template);");
		}
		script.append("msg = new XML(message);");
		script.append(transformerScript);
		script.append(" }");
		script.append("doTransform()\n");
		return script.toString();
	}

	public Map getInboundProperties() {
		return inboundProperties;
	}

	public void setInboundProperties(Map inboundProperties) {
		this.inboundProperties = inboundProperties;
	}

	public Map getOutboundProperties() {
		return outboundProperties;
	}

	public void setOutboundProperties(Map outboundProperties) {
		this.outboundProperties = outboundProperties;
	}

}
