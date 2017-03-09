/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.model.Rule.Operator;
import com.mirth.connect.plugins.mapper.MapperStep;
import com.mirth.connect.plugins.mapper.MapperStep.Scope;
import com.mirth.connect.plugins.messagebuilder.MessageBuilderStep;
import com.mirth.connect.plugins.rulebuilder.RuleBuilderRule;
import com.mirth.connect.plugins.rulebuilder.RuleBuilderRule.Condition;
import com.mirth.connect.plugins.xsltstep.XsltStep;
import com.mirth.connect.userutil.ImmutableConnectorMessage;
import com.mirth.connect.util.JavaScriptSharedUtil;

public class FilterTransformerIterableTest {

    /*
     * @formatter:off
     * 
     * Equivalent to:
     * 
     * MSH|^~\&
     * PID|1||1^a~2^b
     * PID|2||3^c~4^d
     * OBR|1||1&a^b~2&c^d
     * OBX|1||||1^a~2^b
     * OBX|2||||3^c~4^d
     * OBR|2||3&e^f~4&g^h
     * OBX|1||||5^e~6^f
     * OBX|2||||7^g~8^h
     * 
     * @formatter:on
     */
    private static final String MSG = "<HL7Message><MSH><MSH.1>|</MSH.1><MSH.2>^~\\&amp;</MSH.2></MSH><PID><PID.1><PID.1.1>1</PID.1.1></PID.1><PID.2/><PID.3><PID.3.1>1</PID.3.1><PID.3.2>a</PID.3.2></PID.3><PID.3><PID.3.1>2</PID.3.1><PID.3.2>b</PID.3.2></PID.3></PID><PID><PID.1><PID.1.1>2</PID.1.1></PID.1><PID.2/><PID.3><PID.3.1>3</PID.3.1><PID.3.2>c</PID.3.2></PID.3><PID.3><PID.3.1>4</PID.3.1><PID.3.2>d</PID.3.2></PID.3></PID><OBR><OBR.1><OBR.1.1>1</OBR.1.1></OBR.1><OBR.2/><OBR.3><OBR.3.1><OBR.3.1.1>1</OBR.3.1.1><OBR.3.1.2>a</OBR.3.1.2></OBR.3.1><OBR.3.2>b</OBR.3.2></OBR.3><OBR.3><OBR.3.1><OBR.3.1.1>2</OBR.3.1.1><OBR.3.1.2>c</OBR.3.1.2></OBR.3.1><OBR.3.2>d</OBR.3.2></OBR.3></OBR><OBX><OBX.1><OBX.1.1>1</OBX.1.1></OBX.1><OBX.2/><OBX.3/><OBX.4/><OBX.5><OBX.5.1>1</OBX.5.1><OBX.5.2>a</OBX.5.2></OBX.5><OBX.5><OBX.5.1>2</OBX.5.1><OBX.5.2>b</OBX.5.2></OBX.5></OBX><OBX><OBX.1><OBX.1.1>2</OBX.1.1></OBX.1><OBX.2/><OBX.3/><OBX.4/><OBX.5><OBX.5.1>3</OBX.5.1><OBX.5.2>c</OBX.5.2></OBX.5><OBX.5><OBX.5.1>4</OBX.5.1><OBX.5.2>d</OBX.5.2></OBX.5></OBX><OBR><OBR.1><OBR.1.1>2</OBR.1.1></OBR.1><OBR.2/><OBR.3><OBR.3.1><OBR.3.1.1>3</OBR.3.1.1><OBR.3.1.2>e</OBR.3.1.2></OBR.3.1><OBR.3.2>f</OBR.3.2></OBR.3><OBR.3><OBR.3.1><OBR.3.1.1>4</OBR.3.1.1><OBR.3.1.2>g</OBR.3.1.2></OBR.3.1><OBR.3.2>h</OBR.3.2></OBR.3></OBR><OBX><OBX.1><OBX.1.1>1</OBX.1.1></OBX.1><OBX.2/><OBX.3/><OBX.4/><OBX.5><OBX.5.1>5</OBX.5.1><OBX.5.2>e</OBX.5.2></OBX.5><OBX.5><OBX.5.1>6</OBX.5.1><OBX.5.2>f</OBX.5.2></OBX.5></OBX><OBX><OBX.1><OBX.1.1>2</OBX.1.1></OBX.1><OBX.2/><OBX.3/><OBX.4/><OBX.5><OBX.5.1>7</OBX.5.1><OBX.5.2>g</OBX.5.2></OBX.5><OBX.5><OBX.5.1>8</OBX.5.1><OBX.5.2>h</OBX.5.2></OBX.5></OBX></HL7Message>";
    private static final String TMP = "<HL7Message><MSH><MSH.1>|</MSH.1><MSH.2>^~\\&amp;</MSH.2></MSH></HL7Message>";

    private static Logger logger = Logger.getLogger(FilterTransformerIterableTest.class);
    private static ScriptableObject sealedSharedScope;

    @BeforeClass
    public static void setup() throws Exception {
        logger.setLevel(Level.DEBUG);
        Context context = JavaScriptSharedUtil.getGlobalContextForValidation();
        try {
            sealedSharedScope = new ImporterTopLevel(context);
            context.compileString(generateGlobalSealedScript(), UUID.randomUUID().toString(), 1, null).exec(context, sealedSharedScope);
            sealedSharedScope.sealObject();
        } finally {
            Context.exit();
        }
    }

    /**
     * Map PID.3.1 in the first PID.3 of every PID
     */
    @Test
    public void testIteratorMapperStep() throws Exception {
        IteratorStep iterator = new IteratorStep();
        iterator.getProperties().setTarget("msg['PID']");
        iterator.getProperties().setIndexVariable("i");

        MapperStep mapperStep = new MapperStep();
        mapperStep.setVariable("results");
        mapperStep.setScope(Scope.CHANNEL);
        mapperStep.setMapping("msg['PID'][i]['PID.3'][0]['PID.3.1'].toString()");
        iterator.getProperties().getChildren().add(mapperStep);

        ConnectorMessage connectorMessage = new ConnectorMessage();
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        List<String> results = new ArrayList<String>();
        results.add("1");
        results.add("3");
        expectedMap.put("results", results.toArray());
        testTransformerStep(iterator, connectorMessage);
        assertMapEquals(expectedMap, connectorMessage.getChannelMap());
    }

    /**
     * Map PID.3.1 in every PID.3 in every PID
     */
    @Test
    public void testNestedIteratorMapperStep() throws Exception {
        IteratorStep iterator1 = new IteratorStep();
        iterator1.getProperties().setTarget("msg['PID']");
        iterator1.getProperties().setIndexVariable("i");

        IteratorStep iterator2 = new IteratorStep();
        iterator2.getProperties().setTarget("msg['PID'][i]['PID.3']");
        iterator2.getProperties().setIndexVariable("j");

        MapperStep mapperStep = new MapperStep();
        mapperStep.setVariable("results");
        mapperStep.setMapping("msg['PID'][i]['PID.3'][j]['PID.3.1'].toString()");
        iterator2.getProperties().getChildren().add(mapperStep);

        iterator1.getProperties().getChildren().add(iterator2);

        ConnectorMessage connectorMessage = new ConnectorMessage();
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        List<String> results = new ArrayList<String>();
        results.add("1");
        results.add("2");
        results.add("3");
        results.add("4");
        expectedMap.put("results", results.toArray());
        testTransformerStep(iterator1, connectorMessage);
        assertMapEquals(expectedMap, connectorMessage.getChannelMap());
    }

    /**
     * Map OBR.3.1.1 in every OBR.3 in every OBR from msg to tmp tmp starts with no OBR segments
     */
    @Test
    public void testNestedIteratorMessageBuilderStep() throws Exception {
        IteratorStep iterator1 = new IteratorStep();
        iterator1.getProperties().setTarget("msg['OBR']");
        iterator1.getProperties().setIndexVariable("i");

        IteratorStep iterator2 = new IteratorStep();
        iterator2.getProperties().setTarget("msg['OBR'][i]['OBR.3']");
        iterator2.getProperties().setIndexVariable("j");

        MessageBuilderStep messageBuilderStep = new MessageBuilderStep();
        messageBuilderStep.setMessageSegment("tmp['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1']");
        messageBuilderStep.setMapping("msg['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1'].toString()");
        iterator2.getProperties().getChildren().add(messageBuilderStep);

        iterator1.getProperties().getChildren().add(iterator2);

        StringBuilder builder = new StringBuilder();
        builder.append("channelMap.put('results', Lists.list()");
        builder.append(".append(tmp['OBR'][0]['OBR.3'][0]['OBR.3.1']['OBR.3.1.1'].toString())");
        builder.append(".append(tmp['OBR'][0]['OBR.3'][1]['OBR.3.1']['OBR.3.1.1'].toString())");
        builder.append(".append(tmp['OBR'][1]['OBR.3'][0]['OBR.3.1']['OBR.3.1.1'].toString())");
        builder.append(".append(tmp['OBR'][1]['OBR.3'][1]['OBR.3.1']['OBR.3.1.1'].toString())");
        builder.append(".toArray());");

        ConnectorMessage connectorMessage = new ConnectorMessage();
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        List<String> results = new ArrayList<String>();
        results.add("1");
        results.add("2");
        results.add("3");
        results.add("4");
        expectedMap.put("results", results.toArray());
        testTransformerStep(iterator1, connectorMessage, builder.toString());
        assertMapEquals(expectedMap, connectorMessage.getChannelMap());
    }

    /**
     * Map OBR.3.1.1 in every OBR.3 in every OBR from msg to a 2-dimension JSON array.
     */
    @Test
    public void testJSONMessageBuilderStep() throws Exception {
        IteratorStep iterator1 = new IteratorStep();
        iterator1.getProperties().setTarget("msg['OBR']");
        iterator1.getProperties().setIndexVariable("i");

        IteratorStep iterator2 = new IteratorStep();
        iterator2.getProperties().setTarget("msg['OBR'][i]['OBR.3']");
        iterator2.getProperties().setIndexVariable("j");

        MessageBuilderStep messageBuilderStep = new MessageBuilderStep();
        messageBuilderStep.setMessageSegment("tmp[i][j]");
        messageBuilderStep.setMapping("msg['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1'].toString()");
        iterator2.getProperties().getChildren().add(messageBuilderStep);

        iterator1.getProperties().getChildren().add(iterator2);

        StringBuilder builder = new StringBuilder();
        builder.append("channelMap.put('results', Lists.list()");
        builder.append(".append(tmp[0][0])");
        builder.append(".append(tmp[0][1])");
        builder.append(".append(tmp[1][0])");
        builder.append(".append(tmp[1][1])");
        builder.append(".toArray());");

        ConnectorMessage connectorMessage = new ConnectorMessage();
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        List<String> results = new ArrayList<String>();
        results.add("1");
        results.add("2");
        results.add("3");
        results.add("4");
        expectedMap.put("results", results.toArray());
        testTransformerStep(iterator1, connectorMessage, builder.toString(), MSG, true, "[]", false);
        assertMapEquals(expectedMap, connectorMessage.getChannelMap());
    }

    /**
     * For every OBX segment, transform a list of values and store it in the channel map. The
     * transformed XML will be the OBX.5.1 value for every OBX.5 in the given segment.
     */
    @Test
    public void testIteratorXsltStep() throws Exception {
        IteratorStep iterator = new IteratorStep();
        iterator.getProperties().setTarget("msg['OBX']");
        iterator.getProperties().setIndexVariable("i");

        XsltStep xsltStep = new XsltStep();
        xsltStep.setSourceXml("msg['OBX'][i]");
        xsltStep.setResultVariable("results");

        // @formatter:off
        xsltStep.setTemplate(
            "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"+
            "    <xsl:template match=\"/\">"+
            "        <values>"+
            "            <xsl:for-each select=\"OBX/OBX.5\">"+
            "                <value><xsl:value-of select=\"OBX.5.1\"/></value>"+
            "            </xsl:for-each>"+
            "        </values>"+
            "    </xsl:template>"+
            "</xsl:stylesheet>"
        );
        // @formatter:on

        iterator.getProperties().getChildren().add(xsltStep);

        ConnectorMessage connectorMessage = new ConnectorMessage();
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        List<String> results = new ArrayList<String>();
        results.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?><values><value>1</value><value>2</value></values>");
        results.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?><values><value>3</value><value>4</value></values>");
        results.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?><values><value>5</value><value>6</value></values>");
        results.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?><values><value>7</value><value>8</value></values>");
        expectedMap.put("results", results.toArray());
        testTransformerStep(iterator, connectorMessage);
        assertMapEquals(expectedMap, connectorMessage.getChannelMap());
    }

    /**
     * For every PID, test:
     * 
     * PID.3.1 in the first PID.3 field equals 1, 2, 3, or 4
     * 
     * AND
     * 
     * PID.3.2 in the first PID.3 field equals a, b, c, or d
     */
    @Test
    public void testIteratorRuleBuilder() throws Exception {
        IteratorRule iterator = new IteratorRule();
        iterator.getProperties().setTarget("msg['PID']");
        iterator.getProperties().setIndexVariable("i");
        iterator.getProperties().setIntersectIterations(true);

        RuleBuilderRule ruleBuilder1 = new RuleBuilderRule();
        ruleBuilder1.setField("msg['PID'][i]['PID.3'][0]['PID.3.1'].toString()");
        ruleBuilder1.setCondition(Condition.EQUALS);
        ruleBuilder1.getValues().add("'1'");
        ruleBuilder1.getValues().add("'2'");
        ruleBuilder1.getValues().add("'3'");
        ruleBuilder1.getValues().add("'4'");
        iterator.getProperties().getChildren().add(ruleBuilder1);

        RuleBuilderRule ruleBuilder2 = new RuleBuilderRule();
        ruleBuilder2.setOperator(Operator.AND);
        ruleBuilder2.setField("msg['PID'][i]['PID.3'][0]['PID.3.2'].toString()");
        ruleBuilder2.setCondition(Condition.EQUALS);
        ruleBuilder2.getValues().add("'a'");
        ruleBuilder2.getValues().add("'b'");
        ruleBuilder2.getValues().add("'c'");
        ruleBuilder2.getValues().add("'d'");
        iterator.getProperties().getChildren().add(ruleBuilder2);

        assertTrue(testFilterRule(iterator));
    }

    /**
     * For every PID, at least one PID.3 field satisfies:
     * 
     * PID.3.1 equals 1 or 4
     * 
     * OR
     * 
     * PID.3.2 equals dummy
     * 
     * The latter "dummy" test will always return false, but overall the rule should return true.
     */
    @Test
    public void testNestedIteratorRuleBuilder() throws Exception {
        IteratorRule iterator1 = new IteratorRule();
        iterator1.getProperties().setTarget("msg['PID']");
        iterator1.getProperties().setIndexVariable("i");
        iterator1.getProperties().setIntersectIterations(true);

        IteratorRule iterator2 = new IteratorRule();
        iterator2.getProperties().setTarget("msg['PID'][i]['PID.3']");
        iterator2.getProperties().setIndexVariable("j");
        iterator2.getProperties().setIntersectIterations(false);

        RuleBuilderRule ruleBuilder1 = new RuleBuilderRule();
        ruleBuilder1.setField("msg['PID'][i]['PID.3'][j]['PID.3.1'].toString()");
        ruleBuilder1.setCondition(Condition.EQUALS);
        ruleBuilder1.getValues().add("'1'");
        ruleBuilder1.getValues().add("'4'");
        iterator2.getProperties().getChildren().add(ruleBuilder1);

        RuleBuilderRule ruleBuilder2 = new RuleBuilderRule();
        ruleBuilder2.setOperator(Operator.OR);
        ruleBuilder2.setField("msg['PID'][i]['PID.3'][j]['PID.3.2'].toString()");
        ruleBuilder2.setCondition(Condition.EQUALS);
        ruleBuilder2.getValues().add("'dummy'");
        iterator2.getProperties().getChildren().add(ruleBuilder2);

        iterator1.getProperties().getChildren().add(iterator2);

        assertTrue(testFilterRule(iterator1));
    }

    @SuppressWarnings("unchecked")
    private <T> T testScript(String script, ConnectorMessage connectorMessage, Class<T> returnType) throws Exception {
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
            }

            return (T) Context.jsToJava(compiledScript.exec(context, scope), returnType);
        } finally {
            Context.exit();
        }
    }

    private void add(String name, Scriptable scope, Object object) {
        scope.put(name, scope, Context.javaToJS(object, scope));
    }

    private boolean testFilterRule(Rule rule) throws Exception {
        return testFilterRule(rule, "");
    }

    private boolean testFilterRule(Rule rule, String extraScript) throws Exception {
        StringBuilder script = new StringBuilder();
        appendMiscFunctions(script);
        script.append("msg = new XML('").append(StringEscapeUtils.escapeEcmaScript(MSG)).append("');\n");
        script.append("function doFilter() {\n");
        script.append(rule.getScript(true)).append('\n').append(extraScript);
        script.append("\n}\ndoFilter();\n");
        return testScript(script.toString(), null, Boolean.class);
    }

    private void testTransformerStep(Step step, ConnectorMessage connectorMessage) throws Exception {
        testTransformerStep(step, connectorMessage, "");
    }

    private void testTransformerStep(Step step, ConnectorMessage connectorMessage, String extraScript) throws Exception {
        testTransformerStep(step, connectorMessage, extraScript, MSG, true, TMP, true);
    }

    private void testTransformerStep(Step step, ConnectorMessage connectorMessage, String extraScript, String msg, boolean msgXml, String tmp, boolean tmpXml) throws Exception {
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

    private static void assertMapEquals(Map<?, ?> o1, Map<?, ?> o2) {
        if (o1 != null || o2 != null) {
            if (o1 == null || o2 == null || o1.size() != o2.size()) {
                fail();
            }
            for (Entry<?, ?> entry : o1.entrySet()) {
                if (!o2.containsKey(entry.getKey()) || !EqualsBuilder.reflectionEquals(entry.getValue(), o2.get(entry.getKey()))) {
                    fail();
                }
            }
        }
    }
}