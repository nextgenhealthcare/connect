package com.webreach.mirth.server.mule.transformers;

import java.util.Calendar;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.mule.util.GlobalVariableStore;

public class JavaScriptTransformer extends AbstractTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private MessageObjectController messageObjectController = new MessageObjectController();
	private String transformerScript;
	private String filterScript;
	private String direction;
	private String channelId;
	private String connectorName;
	private boolean encryptData;

	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getConnectorName() {
		return this.connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public String getDirection() {
		return this.direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
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

	public boolean isEncryptData() {
		return this.encryptData;
	}

	public void setEncryptData(boolean encryptData) {
		this.encryptData = encryptData;
	}

	@Override
	public Object doTransform(Object src) throws TransformerException {
		MessageObject messageObject = (MessageObject) src;

		initializeMessage(messageObject);

		if (direction.equals("inbound")) {
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

			if (direction.equals("inbound")) {
				scope.put("message", scope, messageObject.getTransformedData());
			} else {
				scope.put("message", scope, messageObject.getRawData());
			}

			scope.put("localMap", scope, messageObject.getVariableMap());
			scope.put("globalMap", scope, GlobalVariableStore.getInstance());

			StringBuilder jsSource = new StringBuilder();
			jsSource.append("importPackage(Packages.com.webreach.mirth.server.util);\n	");
			jsSource.append("function doFilter() {");

			if (messageObject.getTransformedDataProtocol().equals(MessageObject.Protocol.HL7)) {
				jsSource.append("default xml namespace = new Namespace(\"urn:hl7-org:v2xml\");");
			}

			jsSource.append("var msg = new XML(message);\n " + filterScript + " }\n");
			jsSource.append("doFilter()\n");

			logger.debug("executing filter script:\n" + jsSource.toString());
			Object result = context.evaluateString(scope, jsSource.toString(), "<cmd>", 1, null);
			boolean messageAccepted = ((Boolean) Context.jsToJava(result, java.lang.Boolean.class)).booleanValue();

			if (messageAccepted) {
				messageObject.setStatus(MessageObject.Status.ACCEPTED);
			} else {
				messageObject.setStatus(MessageObject.Status.REJECTED);
			}

			messageObjectController.updateMessage(messageObject);
			return messageAccepted;
		} catch (Exception e) {
			logger.error("error ocurred in filter", e);
			messageObject.setStatus(MessageObject.Status.ERROR);
			messageObject.setErrors(e.getMessage());
			messageObjectController.updateMessage(messageObject);
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

			StringBuilder jsSource = new StringBuilder();
			jsSource.append("importPackage(Packages.com.webreach.mirth.server.util);");
			jsSource.append("function doTransform() {");

			// only set the namespace to hl7-org if the message is XML
			if (messageObject.getTransformedDataProtocol().equals(MessageObject.Protocol.HL7)) {
				jsSource.append("default xml namespace = new Namespace(\"urn:hl7-org:v2xml\");");
			}

			jsSource.append("var msg = new XML(message); " + transformerScript + " }");
			jsSource.append("doTransform()\n");

			logger.debug("executing transformation script: " + jsSource.toString());
			context.evaluateString(scope, jsSource.toString(), "<cmd>", 1, null);

			// take the now transformed message and convert it back to ER7
			if ((messageObject.getTransformedData() != null) && (messageObject.getTransformedDataProtocol().equals(MessageObject.Protocol.HL7))) {
				ER7Serializer serializer = new ER7Serializer();
				messageObject.getTransformedData().replace("\\E", "");
				messageObject.setEncodedData(serializer.fromXML(messageObject.getTransformedData()));
			}

			messageObject.setStatus(MessageObject.Status.TRANSFORMED);
			messageObjectController.updateMessage(messageObject);
			return messageObject;
		} catch (Exception e) {
			messageObject.setErrors(e.getMessage());
			messageObject.setStatus(MessageObject.Status.ERROR);
			messageObjectController.updateMessage(messageObject);
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

			StringBuilder jsSource = new StringBuilder();
			jsSource.append("importPackage(Packages.com.webreach.mirth.server.util);");
			jsSource.append("function doTransform() {");
			jsSource.append("var msg = new XML(message); " + transformerScript + " }");
			jsSource.append("doTransform()\n");

			logger.debug("executing transformation script:\n" + jsSource.toString());
			context.evaluateString(scope, jsSource.toString(), "<cmd>", 1, null);

			// since the transformations occur on the template, pull it out of
			// the scope
			Object transformedData = scope.get("template", scope);
			// set the transformedData to the template
			messageObject.setTransformedData(Context.toString(transformedData));
			// re-encode the data to ER7
			if (messageObject.getTransformedDataProtocol().equals(MessageObject.Protocol.HL7)) {
				ER7Serializer serializer = new ER7Serializer();
				messageObject.setEncodedData(serializer.toXML(messageObject.getTransformedData()));
			}

			messageObject.setStatus(MessageObject.Status.TRANSFORMED);
			messageObjectController.updateMessage(messageObject);
			return messageObject;
		} catch (Exception e) {
			messageObject.setErrors(e.getMessage());
			messageObject.setStatus(MessageObject.Status.ERROR);
			messageObjectController.updateMessage(messageObject);
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
}
