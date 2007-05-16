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
import org.mozilla.javascript.ScriptableObject;
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
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.controllers.TemplateController;
import com.webreach.mirth.server.mule.adaptors.Adaptor;
import com.webreach.mirth.server.mule.adaptors.AdaptorFactory;
import com.webreach.mirth.server.util.CompiledScriptCache;
import com.webreach.mirth.server.util.JavaScriptScopeUtil;
import com.webreach.mirth.server.util.UUIDGenerator;

public class JavaScriptTransformer extends AbstractEventAwareTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private AlertController alertController = new AlertController();
	private TemplateController templateController = new TemplateController();
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private ScriptController scriptController = new ScriptController();
	private ErrorMessageBuilder errorBuilder = new ErrorMessageBuilder();

	private String inboundProtocol;
	private String outboundProtocol;
	private Map inboundProperties;
	private Map outboundProperties;
	private String channelId;
	private String connectorName;
	private boolean encryptData;
	private boolean removeNamespace;
	private String transformerScriptId;
	private String filterScriptId;
	private String templateId;
	private String mode;
	private String template;
	private static ScriptableObject sealedSharedScope;
	private Scriptable currentScope;

	public static Context getContext() {
		Context context = Context.enter();
		
		if (sealedSharedScope == null) {
			String importScript = getJavascriptImportScript();
			sealedSharedScope = new ImporterTopLevel(context);
			JavaScriptScopeUtil.buildScope(sealedSharedScope);
			Script script = context.compileString(importScript, UUIDGenerator.getUUID(), 1, null);
			script.exec(context, sealedSharedScope);
			sealedSharedScope.sealObject();
		}
		
		return context;
	}

	public static String getJavascriptImportScript() {
		StringBuilder script = new StringBuilder();
		script.append("importPackage(Packages.com.webreach.mirth.server.util);\n");
		script.append("importPackage(Packages.com.webreach.mirth.model.converters);\n");
		script.append("regex = new RegExp('');\n");
		script.append("xml = new XML('');\n");
		script.append("xmllist = new XMLList();\n");
		script.append("namespace = new Namespace();\n");
		script.append("qname = new QName();\n");
		// ast: Allow ending whitespaces from the input XML
		script.append("XML.ignoreWhitespace=false;");
		// ast: Allow ending whitespaces to the output XML
		script.append("XML.prettyPrinting=false;");
		return script.toString();

	}

	public Scriptable getScope() {
		Scriptable scope = getContext().newObject(sealedSharedScope);
		scope.setPrototype(sealedSharedScope);
		scope.setParentScope(null);
		return scope;
	}

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
		currentScope = getScope();
		JavaScriptScopeUtil.addChannel(currentScope, channelId);
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
	
	public boolean isRemoveNamespace() {
		return this.removeNamespace;
	}

	public void setRemoveNamespace(boolean removeNamespace) {
		this.removeNamespace = removeNamespace;
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
		// grab the template
		if (templateId != null) {
			try {
				this.template = templateController.getTemplate(templateId);
			} catch (ControllerException e) {
				logger.error(errorBuilder.buildErrorMessage(Constants.ERROR_300, null, e));
			}
		}
	}

	@Override
	public void initialise() throws InitialisationException {
		Context context = getContext();

		try {
			// Scripts are not compiled is they are blank or do not exist in the
			// database. Note that in Oracle, a blank script is the same as a
			// NULL script.
			String filterScript = scriptController.getScript(filterScriptId);

			if ((filterScript != null) && (filterScript.length() > 0)) {
				String generatedFilterScript = generateFilterScript(filterScript);
				logger.debug("compiling filter script");
				Script compiledFilterScript = context.compileString(generatedFilterScript, filterScriptId, 1, null);
				compiledScriptCache.putCompiledScript(filterScriptId, compiledFilterScript);
			}

			String transformerScript = scriptController.getScript(transformerScriptId);

			if ((transformerScript != null) && (transformerScript.length() > 0)) {
				String generatedTransformerScript = generateTransformerScript(transformerScript);
				logger.debug("compiling transformer script");
				Script compiledTransformerScript = context.compileString(generatedTransformerScript, transformerScriptId, 1, null);
				compiledScriptCache.putCompiledScript(transformerScriptId, compiledTransformerScript);
			}
		} catch (Exception e) {
			logger.error(errorBuilder.buildErrorMessage(Constants.ERROR_300, null, e));
			throw new InitialisationException(e, this);
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
					messageObjectController.updateMessage(transformedMessageObject, false);
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

			Context context = getContext();
			Scriptable scope = getScope();

			// load variables in JavaScript scope
			JavaScriptScopeUtil.addMessageObject(scope, messageObject);
			JavaScriptScopeUtil.addLogger(scope, scriptLogger);
			// get the script from the cache and execute it
			Script compiledScript = compiledScriptCache.getCompiledScript(filterScriptId);
			Object result = null;
			boolean messageAccepted;

			if (compiledScript == null) {
				logger.debug("filter script could not be found in cache");
				messageAccepted = true;
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
			Context context = getContext();
			Scriptable scope = getScope();
			// load variables in JavaScript scope
			JavaScriptScopeUtil.addMessageObject(scope, messageObject);
			JavaScriptScopeUtil.addLogger(scope, scriptLogger);
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
				logger.debug("transformer script could not be found in cache");
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
				if (this.getInboundProtocol().equals(Protocol.XML.toString()) && !this.getOutboundProtocol().equals(Protocol.XML.toString())) {
					// we don't have a template and we have XML coming in, let's
					// convert it
					transformedData = scope.get("msg", scope);
					encodedDataProtocol = Protocol.valueOf(this.getOutboundProtocol());
					encodedDataProperties = this.getOutboundProperties();
				} else {
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
		script.append("default xml namespace = '';\n");
		script.append("function $(string) { ");
		script.append("if (connectorMap.get(string) != null) { return connectorMap.get(string);} else ");
		script.append("if (channelMap.get(string) != null) { return channelMap.get(string);} else ");
		script.append("if (globalMap.get(string) != null) { return globalMap.get(string);} else ");
		script.append("{ return ''; }}");
		script.append("function doFilter() {");

		if (removeNamespace) {
			script.append("var newMessage = message.replace(/xmlns:?[^=]*=[\"\"][^\"\"]*[\"\"]/g, '');\n");	
		} else {
			script.append("var newMessage = message;\n");
		}
		
		script.append("msg = new XML(newMessage);");

		script.append(filterScript + " }\n");
		script.append("doFilter()\n");
		return script.toString();
	}

	private String generateTransformerScript(String transformerScript) {
		logger.debug("generator transformer script");
		StringBuilder script = new StringBuilder();
		// script used to check for exitence of segment
		script.append("function validate(mapping, defaultValue, replacement) { var result; if (mapping != undefined) {result = new java.lang.String(mapping.toString());} if ((result == undefined) || (result.length() == 0)) {result = defaultValue;} if (replacement != undefined) { for (i = 0; i < replacement.length; i++) { var entry = replacement[i]; result = result.replaceAll(entry[0],entry[1]); \n} } return result; }");
		script.append("function $(string) { ");
		script.append("if (connectorMap.get(string) != null) { return connectorMap.get(string)} else ");
		script.append("if (channelMap.get(string) != null) { return channelMap.get(string)} else ");
		script.append("if (globalMap.get(string) != null) { return globalMap.get(string)} else ");
		script.append("{ return ''; }}");
		script.append("default xml namespace = '';");
		script.append("function doTransform() {");

		// turn the template into an E4X XML object

		if (template != null && template.length() > 0) {
			// We have to remove the namespaces so E4X allows use to use the
			// msg[''] syntax
			script.append("var newTemplate = template.replace(/xmlns:?[^=]*=[\"\"][^\"\"]*[\"\"]/g, '');\n");
			script.append("tmp = new XML(newTemplate);");
		}

		if (removeNamespace) {
			script.append("var newMessage = message.replace(/xmlns:?[^=]*=[\"\"][^\"\"]*[\"\"]/g, '');\n");	
		} else {
			script.append("var newMessage = message;\n");
		}
		
		script.append("msg = new XML(newMessage);");

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
