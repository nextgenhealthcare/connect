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

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.transformers.AbstractEventAwareTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Connector.Mode;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.builders.ErrorMessageBuilder;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.controllers.TemplateController;
import com.webreach.mirth.server.mule.adaptors.Adaptor;
import com.webreach.mirth.server.mule.adaptors.AdaptorFactory;
import com.webreach.mirth.server.util.CompiledScriptCache;
import com.webreach.mirth.server.util.JavaScriptScopeFactory;

public class JavaScriptTransformer extends AbstractEventAwareTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private MessageObjectController messageObjectController = new MessageObjectController();
	private AlertController alertController = new AlertController();
	private TemplateController templateController = new TemplateController();
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private ScriptController scriptController = new ScriptController();
	private JavaScriptScopeFactory scopeFactory = new JavaScriptScopeFactory();
	private ErrorMessageBuilder errorBuilder = new ErrorMessageBuilder();
	
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
	}

	@Override
	public void initialise() throws InitialisationException {
		Context context = Context.enter();
		try {
			// grab the template
			if (templateId != null) {
				this.template = templateController.getTemplate(templateId);
			}
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
			logger.error(errorBuilder.buildErrorMessage(Constants.ERROR_300, null, e));
			throw new InitialisationException(e, this);
		} finally {
			Context.exit();
		}
	}

	@Override
	public Object transform(Object source, UMOEventContext context) throws TransformerException {
		MessageObject messageObject = null;
		try {
			// ---- Begin MO checks -----

			if (this.getMode().equals(Mode.SOURCE.toString())) {

				Adaptor adaptor = AdaptorFactory.getAdaptor(Protocol.valueOf(inboundProtocol));
				messageObject = adaptor.getMessage((String) source, channelId, encryptData, inboundProperties);
				// Load properties from the context to the messageObject
				messageObject.getChannelMap().putAll(context.getProperties());

			} else if (this.getMode().equals(Mode.DESTINATION.toString())) {
				MessageObject incomingMessageObject = (MessageObject) source;
				Adaptor adaptor = AdaptorFactory.getAdaptor(Protocol.valueOf(inboundProtocol));
				messageObject = adaptor.convertMessage(incomingMessageObject, this.getConnectorName(), channelId, encryptData, inboundProperties);
				messageObject.setEncodedDataProtocol(Protocol.valueOf(this.outboundProtocol));
			}
		} catch (Exception e) {
			alertController.sendAlerts(channelId, Constants.ERROR_301, null, e);
			throw new TransformerException(this, e);
		}
		// ---- End MO checks -----

		boolean messageAccepted = false;

		try {
			// if the message passes the filter, run the transformation script
			messageAccepted = evaluateFilterScript(messageObject);
		} catch (Exception e) {
			alertController.sendAlerts(channelId, Constants.ERROR_200, null, e);
			throw new TransformerException(this, e);
		}

		try {
			if (messageAccepted) {
				MessageObject transformedMessageObject = evaluateTransformerScript(messageObject);

				if (this.getMode().equals(Mode.SOURCE.toString())) {
					// only update on the source - it doesn't matter on each
					// destination
					messageObjectController.updateMessage(transformedMessageObject);
				}
				return transformedMessageObject;

			} else {
				messageObjectController.setFiltered(messageObject, "Message has been filtered");
				return messageObject;
			}
		} catch (Exception e) {
			// send alert if the transformation process fails
			alertController.sendAlerts(channelId, Constants.ERROR_300, null, e);
			throw new TransformerException(this, e);
		}
	}

	private boolean evaluateFilterScript(MessageObject messageObject) throws TransformerException {
		try {
			Logger scriptLogger = Logger.getLogger("filter");

			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);

			// load variables in JavaScript scope
			scopeFactory.buildScope(scope, messageObject, scriptLogger);

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

			if (messageAccepted) {
				messageObject.setStatus(MessageObject.Status.ACCEPTED);
			}

			return messageAccepted;
		} catch (Exception e) {
			messageObjectController.setError(messageObject, Constants.ERROR_200, "Error evaluating filter", e);
			throw new TransformerException(this, e);
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
			scopeFactory.buildScope(scope, messageObject, scriptLogger);
			scope.put("template", scope, template);
			
			// TODO: have function list provide all serializers - maybe we
			// import a top level package or create a helper class
			// TODO: this is going to break backwards compatability
			// scope.put("serializer", scope,
			// SerializerFactory.getSerializer(Protocol.valueOf(inboundProtocol),
			// this.get));

			// get the script from the cache and execute it
			Script compiledScript = compiledScriptCache.getCompiledScript(transformerScriptId);

			if (compiledScript == null) {
				logger.warn("transformer script could not be found in cache");
			} else {
				compiledScript.exec(context, scope);
			}

			// TODO: Check logic here
			Object transformedData;
			Protocol encodedDataProtocol;
			Map encodedDataProperties;

			if (template != null && template.length() > 0) {
				transformedData = scope.get("tmp", scope);
				encodedDataProtocol = Protocol.valueOf(this.getOutboundProtocol());
				encodedDataProperties = this.getOutboundProperties();
			} else {
				if (this.getInboundProtocol().equals(Protocol.XML.toString()) && !this.getOutboundProtocol().equals(Protocol.XML.toString())){
					//we don't have a template and we have XML coming in, let's convert it
					transformedData = scope.get("msg", scope);
					encodedDataProtocol = Protocol.valueOf(this.getOutboundProtocol());
					encodedDataProperties = this.getOutboundProperties();
				}else{
					transformedData = scope.get("msg", scope);
					encodedDataProtocol = Protocol.valueOf(this.getInboundProtocol());
					encodedDataProperties = this.getInboundProperties();
				}
			}

			if (transformedData != Scriptable.NOT_FOUND) {
				// set the transformedData to the template
				messageObject.setTransformedData(Context.toString(transformedData));
			}

			if ((messageObject.getTransformedData() != null)) {
				IXMLSerializer<String> serializer = AdaptorFactory.getAdaptor(encodedDataProtocol).getSerializer(encodedDataProperties);
				messageObject.setEncodedData(serializer.fromXML(messageObject.getTransformedData()));
				messageObject.setEncodedDataProtocol(encodedDataProtocol);
			}

			messageObject.setStatus(MessageObject.Status.TRANSFORMED);
			return messageObject;
		} catch (Exception e) {
			messageObjectController.setError(messageObject, Constants.ERROR_300, "Error evaluating transformer", e);
			throw new TransformerException(this, e);
		} finally {
			Context.exit();
		}
	}

	private String generateFilterScript(String filterScript) {
		logger.debug("generating filter script");

		StringBuilder script = new StringBuilder();
		script.append("importPackage(Packages.com.webreach.mirth.server.util);\n");
		script.append("importPackage(Packages.com.webreach.mirth.model.converters);\n");
		
		script.append("function $(string) { ");
		script.append("if (connectorMap.get(string) != null) { return connectorMap.get(string);} else ");
		script.append("if (channelMap.get(string) != null) { return channelMap.get(string);} else ");
		script.append("if (globalMap.get(string) != null) { return globalMap.get(string);} else ");
		script.append("{ return ''; }}");
			
		script.append("function doFilter() {");
		setDefaultNamespace(script, inboundProtocol);
		
       // script.append("default xml namespace = new Namespace(\"urn:mirthproject-org\");");

		script.append("var msg = new XML(message);\n ");
		setProperNamespace(script, inboundProtocol, "msg");
		script.append(filterScript + " }\n");
		script.append("doFilter()\n");
		return script.toString();
	}

	private String generateTransformerScript(String transformerScript) {
		logger.debug("generator transformer script");

		StringBuilder script = new StringBuilder();
		script.append("importPackage(Packages.com.webreach.mirth.server.util);\n");
		script.append("importPackage(Packages.com.webreach.mirth.model.converters);\n");
		
		// script used to check for exitence of segment
		script.append("function validate(mapping, defaultValue, replacement) { var result = ''; if (mapping != undefined) {result = mapping.toString();} if (result.length == 0) {result = defaultValue;} if (replacement != undefined) { for (i = 0; i < replacement.length; i++) { var entry = replacement[i]; result = result.replace(entry[0],entry[1]); \n} } return result; }");
		script.append("function $(string) { ");
		script.append("if (connectorMap.get(string) != null) { return connectorMap.get(string)} else ");
		script.append("if (channelMap.get(string) != null) { return channelMap.get(string)} else ");
		script.append("if (globalMap.get(string) != null) { return globalMap.get(string)} else ");
		script.append("{ return ''; }}");
			
		script.append("function doTransform() {");

		// RHINO seems to need this in order to function properly.
		// TODO: Figure out why.
		// script.append("default xml namespace = new Namespace(\"urn:mirthproject-org:xml\");");
		//script.append("default xml namespace = new Namespace(\"urn:hl7-org:v2xml\");");
		
		setDefaultNamespace(script, inboundProtocol);
		
		// ast: Allow ending whitespaces from the input XML
		script.append("XML.ignoreWhitespace=false;");
		// ast: Allow ending whitespaces to the output XML
		script.append("XML.prettyPrinting=false;");
		// turn the template into an E4X XML object

		if (template != null && template.length() > 0) {
			script.append("tmp = new XML(template);");
			//setProperNamespace(script, outboundProtocol, "tmp");
		}
		
		script.append("msg = new XML(message);");
		setProperNamespace(script, inboundProtocol, "msg");
		script.append(transformerScript);
		script.append(" }");
		script.append("doTransform()\n");
		return script.toString();
	}
	private void setDefaultNamespace(StringBuilder script, String protocol) {

		if (protocol.equals(Protocol.HL7V2.toString())){
			script.append("default xml namespace = new Namespace('urn:hl7-org:v2xml');");
		}else if (protocol.equals(Protocol.HL7V3.toString())){
			script.append("default xml namespace = new Namespace('urn:hl7-org:v3');");
		} else if (protocol.equals(Protocol.EDI.toString())){
			script.append("default xml namespace = new Namespace('urn:mirthproject-org:edi:xml');");
		} else if (protocol.equals(Protocol.X12.toString())){
			script.append("default xml namespace = new Namespace('urn:mirthproject-org:x12:xml');");
		} else if (protocol.equals(Protocol.XML.toString())){
			script.append("default xml namespace = new Namespace('urn:mirthproject-org:xml');");
		}
	}
	private void setProperNamespace(StringBuilder script, String protocol, String var) {
		script.append(var);
		if (protocol.equals(Protocol.HL7V2.toString())){
			script.append(".setNamespace(new Namespace('hl7', 'urn:hl7-org:v2xml'));\n");
		}else if (protocol.equals(Protocol.HL7V3.toString())){
			script.append(".setNamespace(new Namespace('hl7', 'urn:hl7-org:v3'));");
		} else if (protocol.equals(Protocol.EDI.toString())){
			script.append(".setNamespace(new Namespace('edi', 'urn:mirthproject-org:edi:xml'));");
		} else if (protocol.equals(Protocol.X12.toString())){
			script.append(".setNamespace(new Namespace('x12', 'urn:mirthproject-org:x12:xml'));");
		} else if (protocol.equals(Protocol.XML.toString())){
			script.append(".setNamespace(new Namespace('msg', 'urn:mirthproject-org:xml'));");
		}
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
