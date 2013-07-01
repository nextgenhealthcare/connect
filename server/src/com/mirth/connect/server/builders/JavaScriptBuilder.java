/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.builders;

import java.io.File;
import java.io.IOException;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.util.JavaScriptConstants;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.controllers.ScriptController;

public class JavaScriptBuilder {
    private static Logger logger = Logger.getLogger(JavaScriptBuilder.class);

    /*
     * Generates the global JavaScript contained in all new scopes created
     */
    public static String generateGlobalSealedScript() {
        StringBuilder script = new StringBuilder();

        // add #trim() function to JavaScript String prototype
        script.append("String.prototype.trim = function() { return this.replace(/^\\s+|\\s+$/g,\"\").replace(/^\\t+|\\t+$/g,\"\"); };");

        script.append("importPackage(Packages.com.mirth.connect.userutil);\n");
        script.append("importPackage(Packages.com.mirth.connect.server.userutil);\n");
        script.append("regex = new RegExp('');\n");
        script.append("xml = new XML('');\n");
        script.append("xmllist = new XMLList();\n");
        script.append("namespace = new Namespace();\n");
        script.append("qname = new QName();\n");

        /*
         * Ignore whitespace so blank lines are removed when deleting elements.
         * This also involves changing XmlProcessor.java in Rhino to account for
         * Rhino issue 369394 and MIRTH-1405
         */
        script.append("XML.ignoreWhitespace=true;");
        // Setting prettyPrinting to true causes HL7 to break when converting back from HL7.
        script.append("XML.prettyPrinting=false;");

        return script.toString();
    }

    /*
     * Generation functions for general scripts
     */

    public static String generateScript(String script, Set<String> scriptOptions) {
        return generateScript(script, CodeTemplate.ContextType.GLOBAL_CONTEXT, scriptOptions);
    }

    public static String generateScript(String script, CodeTemplate.ContextType context, Set<String> scriptOptions) {
        StringBuilder builder = new StringBuilder();

        appendImports(builder, scriptOptions);
        appendMapFunctions(builder);
        appendAttachmentFunctions(builder, scriptOptions);
        appendCodeTemplates(builder, context);
        appendMiscFunctions(builder);

        builder.append("function doScript() {\n" + script + " \n}\n");
        builder.append("doScript();\n");

        return builder.toString();
    }

    /*
     * Generation functions for specific phases
     */

    public static String generateDefaultKeyScript(String key) {
        return generateDefaultKeyScript(key, true);
    }

    public static String generateDefaultKeyScript(String key, boolean isGlobal) {
        String script = null;
        StringBuilder builder = new StringBuilder();

        if (key.equals(ScriptController.DEPLOY_SCRIPT_KEY)) {
            appendDefaultDeployScript(builder, isGlobal);
            script = builder.toString();
        } else if (key.equals(ScriptController.SHUTDOWN_SCRIPT_KEY)) {
            appendDefaultShutdownScript(builder, isGlobal);
            script = builder.toString();
        } else if (key.equals(ScriptController.PREPROCESSOR_SCRIPT_KEY)) {
            appendDefaultPreprocessorScript(builder, isGlobal);
            script = builder.toString();
        } else if (key.equals(ScriptController.POSTPROCESSOR_SCRIPT_KEY)) {
            appendDefaultPostprocessorScript(builder, isGlobal);
            script = builder.toString();
        }

        return script;
    }

    public static String generateFilterTransformerScript(Filter filter, Transformer transformer) throws BuilderException {
        logger.debug("generating script");

        StringBuilder builder = new StringBuilder();

        DataTypeServerPlugin inboundServerPlugin = ExtensionController.getInstance().getDataTypePlugins().get(transformer.getInboundDataType());

        switch (inboundServerPlugin.getSerializationType()) {
            case XML:
                // Turn the inbound message into an E4X XML object
                builder.append("msg = new XML(connectorMessage.getTransformedData());\n");

                // Set the default namespace if there is one left on the root node, otherwise set it to ''.
                builder.append("if (msg.namespace('') != undefined) { default xml namespace = msg.namespace(''); } else { default xml namespace = ''; }\n");
                break;

            case RAW:
                builder.append("if (connectorMessage.getProcessedRawData() != null) { msg = new String(connectorMessage.getProcessedRawData()); } else { msg = new String(connectorMessage.getRawData()); } \n");
                break;
        }

        // Turn the outbound template into an E4X XML object, if there is one
        if (StringUtils.isNotBlank(transformer.getOutboundTemplate())) {
            DataTypeServerPlugin outboundServerPlugin = ExtensionController.getInstance().getDataTypePlugins().get(transformer.getOutboundDataType());

            switch (outboundServerPlugin.getSerializationType()) {
                case XML:
                    builder.append("tmp = new XML(template);\n");
                    break;

                case RAW:
                    builder.append("tmp = template;\n");
                    break;
            }
        }

        // Append doFilter() function
        appendFilterScript(builder, filter);
        // Append doTransform() function
        appendTransformerScript(builder, transformer);
        // Append doFilter/doTransform execution
        builder.append("if (doFilter() == true) { doTransform(); return true; } else { return false; }");

        return builder.toString();
    }

    public static String generateResponseTransformerScript(Transformer transformer) throws BuilderException {
        logger.debug("Generating response transformer script...");

        StringBuilder builder = new StringBuilder();

        DataTypeServerPlugin inboundServerPlugin = ExtensionController.getInstance().getDataTypePlugins().get(transformer.getInboundDataType());

        switch (inboundServerPlugin.getSerializationType()) {
            case XML:
                // Turn the inbound message into an E4X XML object
                builder.append("msg = new XML(connectorMessage.getResponseTransformedData());\n");

                // Set the default namespace if there is one left on the root node, otherwise set it to ''.
                builder.append("if (msg.namespace('') != undefined) { default xml namespace = msg.namespace(''); } else { default xml namespace = ''; }\n");
                break;

            case RAW:
                builder.append("msg = new String(response.getMessage()) \n");
                break;
        }

        // Turn the outbound template into an E4X XML object, if there is one
        if (StringUtils.isNotBlank(transformer.getOutboundTemplate())) {
            DataTypeServerPlugin outboundServerPlugin = ExtensionController.getInstance().getDataTypePlugins().get(transformer.getOutboundDataType());

            switch (outboundServerPlugin.getSerializationType()) {
                case XML:
                    builder.append("tmp = new XML(template);\n");
                    break;

                case RAW:
                    builder.append("tmp = template;\n");
                    break;
            }
        }

        // Append doTransform() function
        appendTransformerScript(builder, transformer);
        // Append doTransform() execution
        builder.append("doTransform();");

        return builder.toString();
    }

    /*
     * General functions used by the generation methods to build scripts
     */

    private static void appendDefaultDeployScript(StringBuilder builder, boolean isGlobal) {
        if (isGlobal) {
            builder.append(JavaScriptConstants.DEFAULT_GLOBAL_DEPLOY_SCRIPT);
        } else {
            builder.append(JavaScriptConstants.DEFAULT_CHANNEL_DEPLOY_SCRIPT);
        }
    }

    private static void appendDefaultShutdownScript(StringBuilder builder, boolean isGlobal) {
        if (isGlobal) {
            builder.append(JavaScriptConstants.DEFAULT_GLOBAL_SHUTDOWN_SCRIPT);
        } else {
            builder.append(JavaScriptConstants.DEFAULT_CHANNEL_SHUTDOWN_SCRIPT);
        }
    }

    private static void appendDefaultPreprocessorScript(StringBuilder builder, boolean isGlobal) {
        if (isGlobal) {
            builder.append(JavaScriptConstants.DEFAULT_GLOBAL_PREPROCESSOR_SCRIPT);
        } else {
            builder.append(JavaScriptConstants.DEFAULT_CHANNEL_PREPROCESSOR_SCRIPT);
        }
    }

    private static void appendDefaultPostprocessorScript(StringBuilder builder, boolean isGlobal) {
        if (isGlobal) {
            builder.append(JavaScriptConstants.DEFAULT_GLOBAL_POSTPROCESSOR_SCRIPT);
        } else {
            builder.append(JavaScriptConstants.DEFAULT_CHANNEL_POSTPROCESSOR_SCRIPT);
        }
    }

    private static void appendFilterScript(StringBuilder builder, Filter filter) throws BuilderException {
        logger.debug("building javascript filter: rule count=" + filter.getRules().size());

        if (filter.getRules().isEmpty()) {
            logger.debug("filter is empty, setting to accept all messages");
            builder.append("function doFilter() { phase = 'filter'; return true; }");
        } else {
            for (ListIterator<Rule> iter = filter.getRules().listIterator(); iter.hasNext();) {
                Rule rule = iter.next();

                if (rule.getType().equalsIgnoreCase("External Script")) {
                    try {
                        File externalScriptFile = new File(rule.getScript());
                        builder.append("function filterRule" + iter.nextIndex() + "() {\n" + FileUtils.readFileToString(externalScriptFile) + "\n}");
                    } catch (IOException e) {
                        throw new BuilderException("Could not add script file.", e);
                    }
                } else {
                    builder.append("function filterRule" + iter.nextIndex() + "() {\n" + rule.getScript() + "\n}");
                }
            }

            builder.append("function doFilter() { phase = 'filter'; return !!(");

            // call each of the above functions in a big boolean expression
            for (ListIterator<Rule> iter = filter.getRules().listIterator(); iter.hasNext();) {
                Rule rule = iter.next();
                String operator = "";

                if (rule.getOperator().equals(Rule.Operator.AND)) {
                    operator = " && ";
                } else if (rule.getOperator().equals(Rule.Operator.OR)) {
                    operator = " || ";
                }

                builder.append(operator + "filterRule" + iter.nextIndex() + "()");
            }

            builder.append("); }\n");
        }
    }

    private static void appendTransformerScript(StringBuilder builder, Transformer transformer) throws BuilderException {
        logger.debug("building javascript transformer: step count=" + transformer.getSteps().size());

        // Set the phase and also reset the logger to transformer (it was filter before)
        builder.append("function doTransform() { phase = 'transformer'; logger = Packages.org.apache.log4j.Logger.getLogger(phase);\n\n\n");

        for (Step step : transformer.getSteps()) {
            logger.debug("adding step: " + step.getName());

            if (step.getType().equalsIgnoreCase("External Script")) {
                try {
                    builder.append("\n" + FileUtils.readFileToString(new File(step.getScript())) + "\n");
                } catch (IOException e) {
                    throw new BuilderException("Could not add script file.", e);
                }
            } else {
                builder.append(step.getScript() + "\n");
            }
        }

        builder.append("\n}\n");
    }

    private static void appendMiscFunctions(StringBuilder builder) {
        // Script used to check for existence of segment
        builder.append("function validate(mapping, defaultValue, replacement) {");
        builder.append("var result = mapping;");
        builder.append("if ((result == undefined) || (result.toString().length == 0)) { ");
        builder.append("if (defaultValue == undefined) { defaultValue = ''} result = defaultValue; } ");
        builder.append("result = new java.lang.String(result.toString()); ");
        builder.append("if (replacement != undefined) {");
        builder.append("for (i = 0; i < replacement.length; i++) { ");
        builder.append("var entry = replacement[i]; result = result.replaceAll(entry[0], entry[1]); } } return result; }");

        // Helper function to create segments
        builder.append("function createSegment(name, msgObj, index) {");
        builder.append("if (arguments.length == 1) { return new XML('<' + name + '></' + name + '>'); };");
        builder.append("if (arguments.length == 2) { index = 0; };");
        builder.append("msgObj[name][index] = new XML('<' + name + '></' + name + '>');");
        builder.append("return msgObj[name][index];");
        builder.append("}\n");

        // Helper function to create segments after specified field
        builder.append("function createSegmentAfter(name, segment) {");
        builder.append("var msgObj = segment;");
        builder.append("while (msgObj.parent() != undefined) { msgObj = msgObj.parent(); }");
        builder.append("msgObj.insertChildAfter(segment[0], new XML('<' + name + '></' + name + '>'));");
        builder.append("return msgObj.child(segment[0].childIndex() + 1);");
        builder.append("}\n");
    }

    private static void appendMapFunctions(StringBuilder builder) {
        builder.append("function $co(key, value) { if (value === undefined) { return connectorMap.get(key); } else { return connectorMap.put(key, value); } }\n");
        builder.append("function $c(key, value) { if (value === undefined) { return channelMap.get(key); } else { return channelMap.put(key, value); } }\n");
        builder.append("function $gc(key, value) { if (value === undefined) { return globalChannelMap.get(key); } else { return globalChannelMap.put(key, value); } }\n");
        builder.append("function $g(key, value) { if (value === undefined) { return globalMap.get(key); } else { return globalMap.put(key, value); } }\n");
        builder.append("function $r(key, value) { if (value === undefined) { return responseMap.get(key); } else { return responseMap.put(key, value); } }\n");

        // No need to check the code context here; the function checks whether each individual map exists first 
        builder.append("function $(string) { ");
        builder.append("try { if(responseMap.containsKey(string)) { return $r(string); } } catch(e){}");
        builder.append("try { if(connectorMap.containsKey(string)) { return $co(string); } } catch(e){}");
        builder.append("try { if(channelMap.containsKey(string)) { return $c(string); } } catch(e){}");
        builder.append("try { if(globalChannelMap.containsKey(string)) { return $gc(string); } } catch(e){}");
        builder.append("try { if(globalMap.containsKey(string)) { return $g(string); } } catch(e){}");
        // TODO: This is temporary for the database reader and should not stay
        builder.append("try { if(resultMap.containsKey(string)) { return resultMap.get(string); } } catch(e){}");
        builder.append("return ''; }\n");
    }

    private static void appendAttachmentFunctions(StringBuilder builder, Set<String> scriptOptions) {
        // Helper function to access attachments (returns List<Attachment>)
        builder.append("function getAttachments() {");
        builder.append("return Packages.com.mirth.connect.server.controllers.MessageController.getInstance().getMessageAttachment(connectorMessage.getChannelId(), connectorMessage.getMessageId());");
        builder.append("}\n");

        // Helper function to set attachment
        if (scriptOptions != null && scriptOptions.contains("useAttachmentList")) {

            builder.append("function addAttachment(data, type) {");
            builder.append("var attachment = Packages.com.mirth.connect.donkey.server.controllers.MessageController.getInstance().createAttachment(data, type);");
            builder.append("attachments.add(attachment); \n");
            builder.append("return attachment; }\n");
        } else {
            builder.append("function addAttachment(data, type) {");
            builder.append("var attachment = Packages.com.mirth.connect.donkey.server.controllers.MessageController.getInstance().createAttachment(data, type);");
            builder.append("Packages.com.mirth.connect.donkey.server.controllers.MessageController.getInstance().insertAttachment(attachment, channelId, connectorMessage.getMessageId())\n");
            builder.append("return attachment; }\n");
        }
    }

    private static void appendImports(StringBuilder builder, Set<String> scriptOptions) {
        if (scriptOptions != null && scriptOptions.contains("importUtilPackage")) {
            builder.append("importPackage(Packages.com.mirth.connect.util);\n");
        }
    }

    private static void appendCodeTemplates(StringBuilder builder, CodeTemplate.ContextType context) {
        try {
            for (CodeTemplate template : ControllerFactory.getFactory().createCodeTemplateController().getCodeTemplate(null)) {
                if (template.getType() == CodeSnippetType.FUNCTION) {
                    if (context.getContext() <= template.getScope()) {
                        builder.append(template.getCode());
                    }
                }
            }
        } catch (ControllerException e) {
            logger.error("Could not get user functions.", e);
        }
    }
}
