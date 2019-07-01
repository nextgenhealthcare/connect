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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.model.Rule.Operator;
import com.mirth.connect.plugins.mapper.MapperStep;
import com.mirth.connect.plugins.mapper.MapperStep.Scope;
import com.mirth.connect.plugins.messagebuilder.MessageBuilderStep;
import com.mirth.connect.plugins.rulebuilder.RuleBuilderRule;
import com.mirth.connect.plugins.rulebuilder.RuleBuilderRule.Condition;
import com.mirth.connect.plugins.xsltstep.XsltStep;
import com.mirth.connect.util.JavaScriptTestUtil;
import com.mirth.connect.util.MirthTestUtil;

public class FilterTransformerIterableTest {

    @BeforeClass
    public static void setup() throws Exception {
        JavaScriptTestUtil.setup();
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
        JavaScriptTestUtil.testTransformerStep(iterator, connectorMessage);
        MirthTestUtil.assertMapEquals(expectedMap, connectorMessage.getChannelMap());
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
        JavaScriptTestUtil.testTransformerStep(iterator1, connectorMessage);
        MirthTestUtil.assertMapEquals(expectedMap, connectorMessage.getChannelMap());
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
        JavaScriptTestUtil.testTransformerStep(iterator1, connectorMessage, builder.toString());
        MirthTestUtil.assertMapEquals(expectedMap, connectorMessage.getChannelMap());
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
        JavaScriptTestUtil.testTransformerStep(iterator1, connectorMessage, builder.toString(), JavaScriptTestUtil.MSG, true, "[]", false);
        MirthTestUtil.assertMapEquals(expectedMap, connectorMessage.getChannelMap());
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
        JavaScriptTestUtil.testTransformerStep(iterator, connectorMessage);
        MirthTestUtil.assertMapEquals(expectedMap, connectorMessage.getChannelMap());
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

        assertTrue(JavaScriptTestUtil.testFilterRule(iterator));
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

        assertTrue(JavaScriptTestUtil.testFilterRule(iterator1));
    }

}