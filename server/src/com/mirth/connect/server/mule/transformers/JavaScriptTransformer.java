/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.transformers;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mule.transformers.AbstractEventAwareTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;

import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.MessageObject.Protocol;
import com.mirth.connect.model.converters.DefaultSerializerPropertiesFactory;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.builders.ErrorMessageBuilder;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.CodeTemplateController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.controllers.TemplateController;
import com.mirth.connect.server.mule.adaptors.Adaptor;
import com.mirth.connect.server.mule.adaptors.AdaptorFactory;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.JavaScriptScopeUtil;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.util.StringUtil;

public class JavaScriptTransformer extends AbstractEventAwareTransformer {
    private Logger logger = Logger.getLogger(this.getClass());
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private TemplateController templateController = ControllerFactory.getFactory().createTemplateController();
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
    private ErrorMessageBuilder errorBuilder = new ErrorMessageBuilder();

    private String inboundProtocol;
    private String outboundProtocol;
    private Map<String, String> inboundProperties;
    private Map<String, String> outboundProperties;
    private String channelId;
    private String connectorName;
    private boolean encryptData;
    private String scriptId;
    private String templateId;
    private String mode;
    private String template;
    private boolean emptyFilterAndTransformer;

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

    public String getScriptId() {
        return this.scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
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

    public String getTemplateId() {
        return this.templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Override
    public void initialise() throws InitialisationException {
        Context context = JavaScriptUtil.getInstance().getContext();

        try {
            // Load the template (if there is one)
            if (templateId != null) {
                template = templateController.getTemplate(channelId, templateId);
            }

            // Scripts are not compiled if they are blank or do not exist in the
            // database. Note that in Oracle, a blank script is the same as a
            // NULL script.
            String script = scriptController.getScript(channelId, scriptId);

            if ((script != null) && (script.length() > 0)) {
                String generatedScript = generateScript(script);
                logger.debug("compiling filter script");
                Script compiledScript = context.compileString(generatedScript, scriptId, 1, null);
                compiledScriptCache.putCompiledScript(scriptId, compiledScript, generatedScript);
            }
        } catch (Exception e) {
            if (e instanceof RhinoException) {
                e = new MirthJavascriptTransformerException((RhinoException) e, channelId, connectorName, 0, "Filter/Transformer", null);
            }

            logger.error(errorBuilder.buildErrorMessage(Constants.ERROR_300, null, e));
            throw new InitialisationException(e, this);
        }
    }

    @Override
    public Object transform(Object source, UMOEventContext context) throws TransformerException {
        MessageObject messageObject = null;

        // ---- Begin MO checks -----
        try {
            Script script = compiledScriptCache.getCompiledScript(scriptId);
            // By setting emptyFilterAndTransformer we skip a lot of unneeded
            // conversions and gain 10x speed
            emptyFilterAndTransformer = true;

            // Check the conditions for skipping transformation
            // 1. Script is not empty
            // 2. Protocols are different
            // 3. Properties are different than the protocol defaults
            // 4. The outbound template is not empty
            // Stop checking the following checks only if
            // emptyFilterAndTransformer has been set to true
            if (script != null || !this.inboundProtocol.equals(this.outboundProtocol)) {
                emptyFilterAndTransformer = false;
            }
            if (emptyFilterAndTransformer && this.inboundProperties != null) {
                // Check to see if the properties are equal to the default
                // properties
                Map<String, String> defaultProperties = DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(Protocol.valueOf(inboundProtocol));

                // Only compare properties that exist in inbound and default
                // Anything not existing in inbound will use the default
                for (Map.Entry<String, String> e : inboundProperties.entrySet()) {
                    String defaultValue = defaultProperties.get(e.getKey());
                    if ((defaultValue != null) && !e.getValue().equalsIgnoreCase(defaultValue)) {
                        emptyFilterAndTransformer = false;
                    }
                }
            }
            if (emptyFilterAndTransformer && this.outboundProperties != null) {
                // Check to see if the properties are equal to the default
                // properties
                Map<String, String> defaultProperties = DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(Protocol.valueOf(outboundProtocol));

                // Only compare properties that exist in outbound and default
                // Anything not existing in outbound will use the default
                for (Map.Entry<String, String> e : outboundProperties.entrySet()) {
                    String defaultValue = defaultProperties.get(e.getKey());
                    if ((defaultValue != null) && !e.getValue().equalsIgnoreCase(defaultValue)) {
                        emptyFilterAndTransformer = false;
                    }
                }
            }
            if (emptyFilterAndTransformer && template != null && !template.equalsIgnoreCase("")) {
                emptyFilterAndTransformer = false;
            }

            // hack to get around cr/lf conversion issue see MIRTH-739
            boolean convertLFtoCR = true;

            // if all of the properties are set and both are false, then set
            // convertLFtoCR to false
            if (!Protocol.valueOf(inboundProtocol).equals(Protocol.HL7V2) && !Protocol.valueOf(outboundProtocol).equals(Protocol.HL7V2)) {
                convertLFtoCR = false;
            } else if ((inboundProperties != null) && (inboundProperties.get("convertLFtoCR") != null) && (outboundProperties != null) && (outboundProperties.get("convertLFtoCR") != null)) {
                convertLFtoCR = Boolean.parseBoolean(inboundProperties.get("convertLFtoCR")) || Boolean.parseBoolean(outboundProperties.get("convertLFtoCR"));
            }

            if (this.getMode().equals(Mode.SOURCE.toString())) {
                Adaptor adaptor = AdaptorFactory.getAdaptor(Protocol.valueOf(inboundProtocol));

                if (convertLFtoCR) {
                    source = StringUtil.convertLFtoCR((String) source);
                }

                messageObject = adaptor.getMessage((String) source, channelId, encryptData, inboundProperties, emptyFilterAndTransformer, context.getMessage().getProperties());

                // Grab and process our attachments
                List<Attachment> attachments = (List<Attachment>) context.getProperties().get("attachments");
                context.getProperties().remove("attachments");

                if (attachments != null) {
                    for (Attachment attachment : attachments) {
                        messageObject.setAttachment(true);
                        messageObjectController.setAttachmentMessageId(messageObject, attachment);
                        messageObjectController.insertAttachment(attachment);
                    }
                }

                // Load properties from the context to the messageObject
                messageObject.getChannelMap().putAll(context.getProperties());
            } else if (this.getMode().equals(Mode.DESTINATION.toString())) {
                MessageObject incomingMessageObject = (MessageObject) source;

                if (convertLFtoCR) {
                    incomingMessageObject.setEncodedData(StringUtil.convertLFtoCR(incomingMessageObject.getEncodedData()));
                }

                Adaptor adaptor = AdaptorFactory.getAdaptor(Protocol.valueOf(inboundProtocol));
                messageObject = adaptor.convertMessage(incomingMessageObject, getConnectorName(), channelId, encryptData, inboundProperties, emptyFilterAndTransformer);
                messageObject.setEncodedDataProtocol(Protocol.valueOf(outboundProtocol));
            }
        } catch (Exception e) {
            alertController.sendAlerts(channelId, Constants.ERROR_301, null, e);
            throw new TransformerException(this, e);
        }
        // ---- End MO checks -----

        try {
            MessageObject finalMessageObject = null;

            // do not evaluate scripts if there are none
            if (emptyFilterAndTransformer) {
                messageObject.setStatus(MessageObject.Status.TRANSFORMED);
                finalMessageObject = messageObject;
            } else {
                finalMessageObject = evaluateScript(messageObject);
            }

            boolean messageAccepted = finalMessageObject.getStatus().equals(MessageObject.Status.TRANSFORMED);

            if (messageAccepted) {
                if (this.getMode().equals(Mode.SOURCE.toString())) {
                    // only update on the source - it doesn't matter on each
                    // destination
                    messageObjectController.setTransformed(finalMessageObject, null);
                }

                return finalMessageObject;
            } else {
                messageObjectController.setFiltered(messageObject, "Message has been filtered", null);
                return messageObject;
            }
        } catch (Exception e) {
            // send alert if the transformation process fails
            alertController.sendAlerts(channelId, Constants.ERROR_300, null, e);
            throw new TransformerException(this, e);
        }
    }

    private MessageObject evaluateScript(MessageObject messageObject) throws TransformerException {
        Logger scriptLogger = Logger.getLogger("filter");
        String phase = new String();

        try {
            Context context = JavaScriptUtil.getInstance().getContext();
            Scriptable scope = JavaScriptUtil.getInstance().getScope();

            // load variables in JavaScript scope
            JavaScriptScopeUtil.addMessageObject(scope, messageObject);
            JavaScriptScopeUtil.addLogger(scope, scriptLogger);
            JavaScriptScopeUtil.addChannel(scope, channelId);
            JavaScriptScopeUtil.addGlobalMap(scope);
            scope.put("template", scope, template);
            scope.put("phase", scope, phase);

            // get the script from the cache and execute it
            Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

            if (compiledScript == null) {
                logger.debug("script could not be found in cache");
            } else {
                compiledScript.exec(context, scope);
            }

            if (!messageObject.getStatus().equals(MessageObject.Status.FILTERED)) {
                Object transformedData;
                Protocol encodedDataProtocol;
                Map encodedDataProperties;

                if (template != null && template.length() > 0) {
                    // This check is in case there is an outbound template but
                    // no script.
                    if (compiledScript == null) {
                        transformedData = template;
                    } else {
                        transformedData = scope.get("tmp", scope);
                    }
                    encodedDataProtocol = Protocol.valueOf(this.getOutboundProtocol());
                    encodedDataProperties = this.getOutboundProperties();
                } else {
                    if (!this.getInboundProtocol().equals(this.getOutboundProtocol())) {
                        // we have mismatched protcols and an empty template,
                        // so...
                        // take the message (XML) and implicitly convert it to
                        // the target format.
                        // if it's invalid xml, so be it, we can't help them.
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

                if (messageObject.getTransformedData() != null) {
                    IXMLSerializer<String> serializer = AdaptorFactory.getAdaptor(encodedDataProtocol).getSerializer(encodedDataProperties);
                    messageObject.setEncodedData(serializer.fromXML(messageObject.getTransformedData()));
                    messageObject.setEncodedDataProtocol(encodedDataProtocol);
                }

                messageObject.setStatus(MessageObject.Status.TRANSFORMED);
            }

            return messageObject;
        } catch (Throwable t) {
            if (t instanceof RhinoException) {
                try {
                    String script = CompiledScriptCache.getInstance().getSourceScript(scriptId);
                    int linenumber = ((RhinoException) t).lineNumber();
                    String errorReport = JavaScriptUtil.getInstance().getSourceCode(script, linenumber, 0);
                    t = new MirthJavascriptTransformerException((RhinoException) t, channelId, connectorName, 0, phase.toUpperCase(), errorReport);
                } catch (Exception ee) {
                    t = new MirthJavascriptTransformerException((RhinoException) t, channelId, connectorName, 0, phase.toUpperCase(), null);
                }
            }

            if (phase.equals("filter")) {
                messageObjectController.setError(messageObject, Constants.ERROR_200, "Error evaluating filter", t, null);
            } else {
                messageObjectController.setError(messageObject, Constants.ERROR_300, "Error evaluating transformer", t, null);
            }

            throw new TransformerException(this, t);
        } finally {
            Context.exit();
        }
    }

    private String generateScript(String oldScript) {
        logger.debug("generating script");

        StringBuilder newScript = new StringBuilder();

        // script used to check for existence of segment
        newScript.append("function validate(mapping, defaultValue, replacement) {");
        newScript.append("var result = mapping;");
        newScript.append("if ((result == undefined) || (result.toString().length == 0)) { ");
        newScript.append("if (defaultValue == undefined) { defaultValue = ''} result = defaultValue; } ");
        newScript.append("result = new java.lang.String(result.toString()); ");
        newScript.append("if (replacement != undefined) {");
        newScript.append("for (i = 0; i < replacement.length; i++) { ");
        newScript.append("var entry = replacement[i]; result = result.replaceAll(entry[0], entry[1]); } } return result; }");

        newScript.append("function $(string) { ");
        newScript.append("if (connectorMap.containsKey(string)) { return connectorMap.get(string); }");
        newScript.append("else if (channelMap.containsKey(string)) { return channelMap.get(string); }");
        newScript.append("else if (globalChannelMap.containsKey(string)) { return globalChannelMap.get(string); }");
        newScript.append("else if (globalMap.containsKey(string)) { return globalMap.get(string); }");
        newScript.append("else { return ''; }");
        newScript.append("}");

        // Helper function to access globalMap
        newScript.append("function $g(key, value) {");
        newScript.append("if (arguments.length == 1) { return globalMap.get(key); }");
        newScript.append("else if (arguments.length == 2) { globalMap.put(key, value); }");
        newScript.append("}");

        // Helper function to access globalChannelMap
        newScript.append("function $gc(key, value) {");
        newScript.append("if (arguments.length == 1) { return globalChannelMap.get(key); }");
        newScript.append("else if (arguments.length == 2) { globalChannelMap.put(key, value); }");
        newScript.append("}");

        // Helper function to access channelMap
        newScript.append("function $c(key, value) {");
        newScript.append("if (arguments.length == 1) { return channelMap.get(key); }");
        newScript.append("else if (arguments.length == 2) { channelMap.put(key, value); }");
        newScript.append("}");
        // Helper function to access connectorMap
        newScript.append("function $co(key, value) {");
        newScript.append("if (arguments.length == 1) { return connectorMap.get(key); }");
        newScript.append("else if (arguments.length == 2) { connectorMap.put(key, value); }");
        newScript.append("}");

        // Helper function to access responseMap
        newScript.append("function $r(key, value) {");
        newScript.append("if (arguments.length == 1) { return responseMap.get(key); }");
        newScript.append("else if (arguments.length == 2) { responseMap.put(key, value); }");
        newScript.append("}");

        // Helper function to create segments
        newScript.append("function createSegment(name, msgObj, index) {");
        newScript.append("if (arguments.length == 1) { return new XML('<' + name + '></' + name + '>'); };");
        newScript.append("if (arguments.length == 2) { index = 0; };");
        newScript.append("msgObj[name][index] = new XML('<' + name + '></' + name + '>');");
        newScript.append("return msgObj[name][index];");
        newScript.append("}");

        // Helper function to create segments after specefied field
        newScript.append("function createSegmentAfter(name, segment) {");
        newScript.append("var msgObj = segment;");
        newScript.append("while (msgObj.parent() != undefined) { msgObj = msgObj.parent(); }");
        newScript.append("msgObj.insertChildAfter(segment[0], new XML('<' + name + '></' + name + '>'));");
        newScript.append("return msgObj.child(segment[0].childIndex() + 1);");
        newScript.append("}");

        // Helper function to get attachments
        newScript.append("function getAttachments() {");
        newScript.append("return Packages.com.mirth.connect.server.controllers.ControllerFactory.getFactory().createMessageObjectController().getAttachmentsByMessage(messageObject);");
        newScript.append("}");

        // Helper function to set attachment
        newScript.append("function addAttachment(data, type) {");
        newScript.append("var attachment = Packages.com.mirth.connect.server.controllers.ControllerFactory.getFactory().createMessageObjectController().createAttachment(data, type, messageObject);messageObject.setAttachment(true);");
        newScript.append("Packages.com.mirth.connect.server.controllers.ControllerFactory.getFactory().createMessageObjectController().insertAttachment(attachment);\n");
        newScript.append("return attachment;\n");
        newScript.append("}\n");

        /*
         * Ignore whitespace so blank lines are removed when deleting elements.
         * This also involves changing XmlProcessor.java in Rhino to account for
         * Rhino issue 369394 and MIRTH-1405
         */
        newScript.append("XML.ignoreWhitespace=true;");
        // Setting prettyPrinting to true causes HL7 to break when converting
        // back from HL7.
        newScript.append("XML.prettyPrinting=false;");

        // Check to see if the property to strip namespaces off of incoming
        // messages has been set to false.
        // For XML, HL7v2, and HL7v3 stripNamespaces can be turned off.
        boolean stripIncomingNamespaces = true;

        if (Protocol.valueOf(inboundProtocol).equals(Protocol.XML) || Protocol.valueOf(inboundProtocol).equals(Protocol.HL7V2) || Protocol.valueOf(inboundProtocol).equals(Protocol.HL7V3)) {
            if ((inboundProperties != null) && (inboundProperties.get("stripNamespaces") != null)) {
                stripIncomingNamespaces = Boolean.parseBoolean(inboundProperties.get("stripNamespaces"));
            }
        }

        if (stripIncomingNamespaces) {
            newScript.append("var newMessage = message.replace(/xmlns:?[^=]*=[\"\"][^\"\"]*[\"\"]/g, '');\n");
        } else {
            newScript.append("var newMessage = message;\n");
        }

        newScript.append("msg = new XML(newMessage);\n");

        // Set the default namespace if there is one left on the root node,
        // otherwise set it to ''.
        newScript.append("if (msg.namespace(\"\") != undefined) { default xml namespace = msg.namespace(\"\"); } else { default xml namespace = ''; }\n");

        // turn the template into an E4X XML object
        if (template != null && template.length() > 0) {
            // We have to remove the namespaces so E4X allows use to use the
            // msg[''] syntax

            // Check to see if the property to strip namespaces off of outbound
            // templates has been set to false.
            // For XML, HL7v2, and HL7v3 stripNamespaces can be turned off.
            boolean stripOutboundNamespaces = true;

            if (Protocol.valueOf(outboundProtocol).equals(Protocol.XML) || Protocol.valueOf(outboundProtocol).equals(Protocol.HL7V2) || Protocol.valueOf(outboundProtocol).equals(Protocol.HL7V3)) {
                if ((outboundProperties != null) && (outboundProperties.get("stripNamespaces") != null)) {
                    stripOutboundNamespaces = Boolean.parseBoolean(outboundProperties.get("stripNamespaces"));
                }
            }

            if (stripOutboundNamespaces) {
                newScript.append("var newTemplate = template.replace(/xmlns:?[^=]*=[\"\"][^\"\"]*[\"\"]/g, '');\n");
            } else {
                newScript.append("var newTemplate = template;\n");
            }

            newScript.append("tmp = new XML(newTemplate);\n");
        }

        try {
            List<CodeTemplate> templates = codeTemplateController.getCodeTemplate(null);
            for (CodeTemplate template : templates) {
                if (template.getType() == CodeSnippetType.FUNCTION) {
                    newScript.append(template.getCode());
                }
            }
        } catch (ControllerException e) {
            logger.error("Could not get user functions.", e);
        }

        newScript.append(oldScript); // has doFilter() and doTransform()
        newScript.append("if (doFilter() == true) { doTransform(); } else { messageObject.setStatus(Packages.com.mirth.connect.model.MessageObject.Status.FILTERED); };");
        return newScript.toString();
    }

}
