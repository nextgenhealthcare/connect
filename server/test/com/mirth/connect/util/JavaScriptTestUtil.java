/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.Step;
import com.mirth.connect.server.userutil.DestinationSet;
import com.mirth.connect.userutil.ImmutableConnectorMessage;

public class JavaScriptTestUtil {

    /*
     * @formatter:off
     * 
     * Equivalent to:
     * 
     * MSH|^~\&
     * PID|1||1^a~2^b
     * PID|2||3^c~4^d
     * OBR|1||1&a^b~2&c^d
     * OBX|1|ST|||1^a~2^b
     * OBX|2|TX|||3^c~4^d
     * OBR|2||3&e^f~4&g^h
     * OBX|1||||5^e~6^f
     * OBX|2||||7^g~8^h
     * 
     * @formatter:on
     */
    public static final String MSG = "<HL7Message><MSH><MSH.1>|</MSH.1><MSH.2>^~\\&amp;</MSH.2></MSH><PID><PID.1><PID.1.1>1</PID.1.1></PID.1><PID.2/><PID.3><PID.3.1>1</PID.3.1><PID.3.2>a</PID.3.2></PID.3><PID.3><PID.3.1>2</PID.3.1><PID.3.2>b</PID.3.2></PID.3></PID><PID><PID.1><PID.1.1>2</PID.1.1></PID.1><PID.2/><PID.3><PID.3.1>3</PID.3.1><PID.3.2>c</PID.3.2></PID.3><PID.3><PID.3.1>4</PID.3.1><PID.3.2>d</PID.3.2></PID.3></PID><OBR><OBR.1><OBR.1.1>1</OBR.1.1></OBR.1><OBR.2/><OBR.3><OBR.3.1><OBR.3.1.1>1</OBR.3.1.1><OBR.3.1.2>a</OBR.3.1.2></OBR.3.1><OBR.3.2>b</OBR.3.2></OBR.3><OBR.3><OBR.3.1><OBR.3.1.1>2</OBR.3.1.1><OBR.3.1.2>c</OBR.3.1.2></OBR.3.1><OBR.3.2>d</OBR.3.2></OBR.3></OBR><OBX><OBX.1><OBX.1.1>1</OBX.1.1></OBX.1><OBX.2><OBX.2.1>ST</OBX.2.1></OBX.2><OBX.3/><OBX.4/><OBX.5><OBX.5.1>1</OBX.5.1><OBX.5.2>a</OBX.5.2></OBX.5><OBX.5><OBX.5.1>2</OBX.5.1><OBX.5.2>b</OBX.5.2></OBX.5></OBX><OBX><OBX.1><OBX.1.1>2</OBX.1.1></OBX.1><OBX.2><OBX.2.1>TX</OBX.2.1></OBX.2><OBX.3/><OBX.4/><OBX.5><OBX.5.1>3</OBX.5.1><OBX.5.2>c</OBX.5.2></OBX.5><OBX.5><OBX.5.1>4</OBX.5.1><OBX.5.2>d</OBX.5.2></OBX.5></OBX><OBR><OBR.1><OBR.1.1>2</OBR.1.1></OBR.1><OBR.2/><OBR.3><OBR.3.1><OBR.3.1.1>3</OBR.3.1.1><OBR.3.1.2>e</OBR.3.1.2></OBR.3.1><OBR.3.2>f</OBR.3.2></OBR.3><OBR.3><OBR.3.1><OBR.3.1.1>4</OBR.3.1.1><OBR.3.1.2>g</OBR.3.1.2></OBR.3.1><OBR.3.2>h</OBR.3.2></OBR.3></OBR><OBX><OBX.1><OBX.1.1>1</OBX.1.1></OBX.1><OBX.2/><OBX.3/><OBX.4/><OBX.5><OBX.5.1>5</OBX.5.1><OBX.5.2>e</OBX.5.2></OBX.5><OBX.5><OBX.5.1>6</OBX.5.1><OBX.5.2>f</OBX.5.2></OBX.5></OBX><OBX><OBX.1><OBX.1.1>2</OBX.1.1></OBX.1><OBX.2/><OBX.3/><OBX.4/><OBX.5><OBX.5.1>7</OBX.5.1><OBX.5.2>g</OBX.5.2></OBX.5><OBX.5><OBX.5.1>8</OBX.5.1><OBX.5.2>h</OBX.5.2></OBX.5></OBX></HL7Message>";
    public static final String TMP = "<HL7Message><MSH><MSH.1>|</MSH.1><MSH.2>^~\\&amp;</MSH.2></MSH></HL7Message>";

    private static Logger logger = Logger.getLogger(JavaScriptTestUtil.class);
    private static ScriptableObject sealedSharedScope;

    public static void setup() throws Exception {
        Context context = JavaScriptSharedUtil.getGlobalContextForValidation();
        try {
            sealedSharedScope = new ImporterTopLevel(context);
            context.compileString(generateGlobalSealedScript(), UUID.randomUUID().toString(), 1, null).exec(context, sealedSharedScope);
            sealedSharedScope.sealObject();
        } finally {
            Context.exit();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T testScript(String script, ConnectorMessage connectorMessage, Class<T> returnType) throws Exception {
        Context context = JavaScriptSharedUtil.getGlobalContextForValidation();
        try {
            Script compiledScript = context.compileString(script, UUID.randomUUID().toString(), 1, null);

            Scriptable scope = context.newObject(sealedSharedScope);
            scope.setPrototype(sealedSharedScope);
            scope.setParentScope(null);
            add("logger", scope, logger);

            if (connectorMessage != null) {
                ImmutableConnectorMessage message = new ImmutableConnectorMessage(connectorMessage, true);
                add("connectorMessage", scope, message);
                add("sourceMap", scope, message.getSourceMap());
                add("connectorMap", scope, message.getConnectorMap());
                add("channelMap", scope, message.getChannelMap());
                add("responseMap", scope, message.getResponseMap());
                if (message.getMetaDataId() == 0) {
                    add("destinationSet", scope, new DestinationSet(message));
                }
            }

            return (T) Context.jsToJava(compiledScript.exec(context, scope), returnType);
        } finally {
            Context.exit();
        }
    }

    public static void add(String name, Scriptable scope, Object object) {
        scope.put(name, scope, Context.javaToJS(object, scope));
    }

    public static boolean testFilterRule(Rule rule) throws Exception {
        return testFilterRule(rule, "");
    }

    public static boolean testFilterRule(Rule rule, String extraScript) throws Exception {
        StringBuilder script = new StringBuilder();
        appendMiscFunctions(script);
        script.append("msg = new XML('").append(StringEscapeUtils.escapeEcmaScript(MSG)).append("');\n");
        script.append("function doFilter() {\n");
        script.append(rule.getScript(true)).append('\n').append(extraScript);
        script.append("\n}\ndoFilter();\n");
        return testScript(script.toString(), null, Boolean.class);
    }

    public static void testTransformerStep(Step step, ConnectorMessage connectorMessage) throws Exception {
        testTransformerStep(step, connectorMessage, "");
    }

    public static void testTransformerStep(Step step, ConnectorMessage connectorMessage, String extraScript) throws Exception {
        testTransformerStep(step, connectorMessage, extraScript, MSG, true, TMP, true);
    }

    public static void testTransformerStep(Step step, ConnectorMessage connectorMessage, String extraScript, String msg, boolean msgXml, String tmp, boolean tmpXml) throws Exception {
        StringBuilder script = new StringBuilder();
        appendMiscFunctions(script);
        if (msgXml) {
            script.append("msg = new XML('").append(StringEscapeUtils.escapeEcmaScript(msg)).append("');\n");
        } else {
            script.append("msg = ").append(msg).append(";\n");
        }
        if (tmpXml) {
            script.append("tmp = new XML('").append(StringEscapeUtils.escapeEcmaScript(tmp)).append("');\n");
        } else {
            script.append("tmp = ").append(tmp).append(";\n");
        }
        script.append("function doTransform() {\n");
        script.append('\n').append(step.getScript(true)).append('\n').append(extraScript);
        script.append("\n}\ndoTransform();\n");
        testScript(script.toString(), connectorMessage, Object.class);
    }

    private static String generateGlobalSealedScript() {
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
         * Ignore whitespace so blank lines are removed when deleting elements. This also involves
         * changing XmlProcessor.java in Rhino to account for Rhino issue 369394 and MIRTH-1405
         */
        script.append("XML.ignoreWhitespace=true;");
        // Setting prettyPrinting to true causes HL7 to break when converting back from HL7.
        script.append("XML.prettyPrinting=false;");

        return script.toString();
    }

    private static void appendMiscFunctions(StringBuilder builder) {
        // Script used to check for existence of segment
        builder.append("function validate(mapping, defaultValue, replacement) {\n");
        builder.append("    var result = mapping;\n");
        builder.append("    if ((result == undefined) || (result.toString().length == 0)) {\n");
        builder.append("        if (defaultValue == undefined) {\n");
        builder.append("            defaultValue = '';\n");
        builder.append("        }\n");
        builder.append("        result = defaultValue;\n");
        builder.append("    }\n");
        builder.append("    if ('string' === typeof result || result instanceof java.lang.String || 'xml' === typeof result) {\n");
        builder.append("        result = new java.lang.String(result.toString());\n");
        builder.append("        if (replacement != undefined) {\n");
        builder.append("            for (var i = 0; i < replacement.length; i++) { ");
        builder.append("                var entry = replacement[i];\n");
        builder.append("                result = result.replaceAll(entry[0], entry[1]);\n");
        builder.append("            }\n");
        builder.append("        }\n");
        builder.append("    }\n");
        builder.append("    return result;\n");
        builder.append("}\n");

        // Helper function to create segments
        builder.append("function createSegment(name, msgObj, index) {");
        builder.append("if (arguments.length == 1) { return new XML('<' + name + '></' + name + '>'); };");
        builder.append("if (arguments.length == 2) { index = 0; };");
        builder.append("msgObj[name][index] = new XML('<' + name + '></' + name + '>');");
        builder.append("return msgObj[name][index];");
        builder.append("}\n");

        // Helper function to get the length of an XMLList or array
        builder.append("function getArrayOrXmlLength(obj) {\n");
        builder.append("    if (typeof obj == 'xml') {\n");
        builder.append("        return obj.length();\n");
        builder.append("    } else if (typeof obj != 'undefined') {\n");
        builder.append("        return obj.length || 0;\n");
        builder.append("    }\n");
        builder.append("    return 0;\n");
        builder.append("}\n");
    }
}
