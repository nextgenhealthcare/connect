package com.webreach.mirth.server.mule.transformers;

import java.util.Calendar;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.mule.util.CompiledScriptCache;
import com.webreach.mirth.server.mule.util.GlobalVariableStore;
import com.webreach.mirth.server.mule.util.StackTracePrinter;

public class JavaScriptTransformer extends AbstractTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private MessageObjectController messageObjectController = new MessageObjectController();
	private StackTracePrinter stackTracePrinter = new StackTracePrinter();
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();

	private String direction;
	private String protocol;
	private String channelId;
	private String connectorName;
	private boolean encryptData;
	private boolean storeMessages;
	private String transformerScript;
	private String filterScript;

	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getDirection() {
		return this.direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getProtocol() {
		return this.protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
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

	public boolean isStoreMessages() {
		return this.storeMessages;
	}

	public void setStoreMessages(boolean storeMessages) {
		this.storeMessages = storeMessages;
	}

	public String getFilterScript() {
		return this.filterScript;
	}

	public void setFilterScript(String filterScript) {
		this.filterScript = filterScript;
	}

	public String getTransformerScript() {
		return this.transformerScript;
	}

	public void setTransformerScript(String transformerScript) {
		this.transformerScript = transformerScript;
	}

	@Override
	public void initialise() throws InitialisationException {
		try {
			Context context = Context.enter();

			if (filterScript != null) {
				String generatedFilterScript = generateFilterScript();
				logger.debug("compiling filter script");
				Script compiledFilterScript = context.compileString(generatedFilterScript, channelId, 1, null);
				compiledScriptCache.putCompiledFilterScript(channelId, compiledFilterScript);
			}

			if (transformerScript != null) {
				String generatedTransformerScript = generateTransformerScript();
				logger.debug("compiling transformer script");
				Script compiledTransformerScript = context.compileString(generatedTransformerScript, channelId, 1, null);
				compiledScriptCache.putCompiledTransformerScript(channelId, compiledTransformerScript);
			}
		} catch (Exception e) {
			throw new InitialisationException(e, this);
		} finally {
			Context.exit();
		}
	}

	@Override
	public Object doTransform(Object src) throws TransformerException {
		MessageObject messageObject = (MessageObject) src;

		initializeMessage(messageObject);

		if (direction.equals(Channel.Direction.INBOUND.toString())) {
			if (evaluateFilterScript(messageObject)) {
				return evaluateInboundTransformerScript(messageObject);
			}
		} else {
			if (evaluateFilterScript(messageObject)) {
				return evaluateOutboundTransformerScript(messageObject);
			}
		}

		return null;
	}

	private boolean evaluateFilterScript(MessageObject messageObject) {
		try {
			Logger scriptLogger = Logger.getLogger("filter");

			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);

			// load variables in JavaScript scope
			scope.put("logger", scope, scriptLogger);

			if (direction.equals(Channel.Direction.INBOUND.toString())) {
				scope.put("message", scope, messageObject.getTransformedData());
			} else {
				scope.put("message", scope, messageObject.getRawData());
			}

			scope.put("localMap", scope, messageObject.getVariableMap());
			scope.put("globalMap", scope, GlobalVariableStore.getInstance());
			scope.put("version", scope, messageObject.getVersion());

			// get the script from the cache and execute it
			Script script = compiledScriptCache.getCompiledFilterScript(channelId);
			Object result = null;
			boolean messageAccepted;

			if (script == null) {
				logger.error("filter script could not be found in cache");
				messageAccepted = false;
			} else {
				result = script.exec(context, scope);
				messageAccepted = ((Boolean) Context.jsToJava(result, java.lang.Boolean.class)).booleanValue();
			}

			if (messageAccepted) {
				messageObject.setStatus(MessageObject.Status.ACCEPTED);
			} else {
				messageObject.setStatus(MessageObject.Status.REJECTED);
			}

			if (storeMessages) {
				messageObjectController.updateMessage(messageObject);
			}

			return messageAccepted;
		} catch (Exception e) {
			logger.error("error ocurred in filter", e);
			messageObject.setStatus(MessageObject.Status.ERROR);
			messageObject.setErrors(stackTracePrinter.stackTraceToString(e));

			if (storeMessages) {
				messageObjectController.updateMessage(messageObject);
			}

			return false;
		} finally {
			Context.exit();
		}
	}

	private MessageObject evaluateInboundTransformerScript(MessageObject messageObject) throws TransformerException {
		try {
			Logger scriptLogger = Logger.getLogger("inbound-transformation");
			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);

			// load variables in JavaScript scope
			scope.put("logger", scope, scriptLogger);
			scope.put("message", scope, messageObject.getTransformedData());
			scope.put("localMap", scope, messageObject.getVariableMap());
			scope.put("globalMap", scope, GlobalVariableStore.getInstance());
			scope.put("version", scope, messageObject.getVersion());

			// TODO: Verify all functions work on UI (ER7 util, IE);
			// get the script from the cache and execute it
			Script compiledScript = compiledScriptCache.getCompiledTransformerScript(channelId);

			if (compiledScript == null) {
				logger.warn("transformer script could not be found in cache");
			} else {
				compiledScript.exec(context, scope);
			}

			// take the now transformed message and convert it back to ER7
			if ((messageObject.getTransformedData() != null) && (messageObject.getEncodedDataProtocol().equals(MessageObject.Protocol.HL7))) {
				ER7Serializer serializer = new ER7Serializer();
				messageObject.getTransformedData().replace("\\E", "");
				messageObject.setEncodedData(serializer.fromXML(messageObject.getTransformedData()));
			}

			messageObject.setStatus(MessageObject.Status.TRANSFORMED);

			if (storeMessages) {
				messageObjectController.updateMessage(messageObject);
			}

			return messageObject;
		} catch (Exception e) {
			messageObject.setStatus(MessageObject.Status.ERROR);
			messageObject.setErrors(stackTracePrinter.stackTraceToString(e));

			if (storeMessages) {
				messageObjectController.updateMessage(messageObject);
			}

			throw new TransformerException(this, e);
		} finally {
			Context.exit();
		}
	}

	private MessageObject evaluateOutboundTransformerScript(MessageObject messageObject) throws TransformerException {
		try {
			Logger scriptLogger = Logger.getLogger("outbound-transformation");
			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);

			// load variables in JavaScript scope
			scope.put("logger", scope, scriptLogger);
			scope.put("message", scope, messageObject.getRawData());
			scope.put("localMap", scope, messageObject.getVariableMap());
			scope.put("globalMap", scope, GlobalVariableStore.getInstance());
			scope.put("version", scope, messageObject.getVersion());

			// get the script from the cache and execute it
			Script compiledScript = compiledScriptCache.getCompiledTransformerScript(channelId);

			if (compiledScript == null) {
				logger.warn("transformer script could not be found in cache");
			} else {
				compiledScript.exec(context, scope);
			}

			// since the transformations occur on the template, pull it out of
			// the scope
			Object transformedData = scope.get("template", scope);
			// set the transformedData to the template
			messageObject.setTransformedData(Context.toString(transformedData));
			// re-encode the data to ER7
			if ((messageObject.getTransformedData() != null) && messageObject.getEncodedDataProtocol().equals(MessageObject.Protocol.HL7)) {
				ER7Serializer serializer = new ER7Serializer();
				messageObject.setEncodedData(serializer.toXML(messageObject.getTransformedData()));
			}

			messageObject.setStatus(MessageObject.Status.TRANSFORMED);

			if (storeMessages) {
				messageObjectController.updateMessage(messageObject);
			}

			return messageObject;
		} catch (Exception e) {
			messageObject.setStatus(MessageObject.Status.ERROR);
			messageObject.setErrors(stackTracePrinter.stackTraceToString(e));

			if (storeMessages) {
				messageObjectController.updateMessage(messageObject);
			}

			throw new TransformerException(this, e);
		} finally {
			Context.exit();
		}
	}

	private void initializeMessage(MessageObject messageObject) {
		String guid = UUID.randomUUID().toString();
		logger.debug("initializing message: id=" + guid);
		messageObject.setId(guid);
		messageObject.setConnectorName(getConnectorName());
		messageObject.setEncrypted(encryptData);
		messageObject.setChannelId(channelId);
		messageObject.setDateCreated(Calendar.getInstance());
	}

	private String generateFilterScript() {
		logger.debug("generating filter script");
		StringBuilder script = new StringBuilder();
		script.append("importPackage(Packages.com.webreach.mirth.server.util);\n	");
		script.append("function doFilter() {");

		if (protocol.equals(Channel.Protocol.HL7.toString())) {
			script.append("default xml namespace = new Namespace(\"urn:hl7-org:v2xml\");");
		}

		script.append("var msg = new XML(message);\n " + filterScript + " }\n");
		script.append("doFilter()\n");
		return script.toString();
	}

	private String generateTransformerScript() {
		logger.debug("generator transformer script");
		StringBuilder script = new StringBuilder();
		script.append("importPackage(Packages.com.webreach.mirth.server.util);");
		script.append("function doTransform() {");

		// only set the namespace to hl7-org if the message is XML
		if (protocol.equals(Channel.Protocol.HL7.toString())) {
			script.append("default xml namespace = new Namespace(\"urn:hl7-org:v2xml\");");
		}

		script.append("var msg = new XML(message); " + transformerScript + " }");
		script.append("doTransform()\n");
		return script.toString();
	}

}
