/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.builders;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.mirth.connect.model.Filter;
import com.mirth.connect.model.IteratorStep;
import com.mirth.connect.model.IteratorStepProperties;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.plugins.javascriptrule.JavaScriptRule;
import com.mirth.connect.plugins.javascriptstep.JavaScriptStep;
import com.mirth.connect.plugins.mapper.MapperStep;
import com.mirth.connect.plugins.messagebuilder.MessageBuilderStep;
import com.mirth.connect.plugins.rulebuilder.RuleBuilderRule;
import com.mirth.connect.util.ScriptBuilderException;

public class JavaScriptBuilderTest {

    @Test
    public void testFilterDisable1Rule() throws ScriptBuilderException {
        Filter filter = new Filter();
        Rule rule1 = new RuleBuilderRule();
        Rule rule2 = new JavaScriptRule();
        Rule rule3 = new RuleBuilderRule();
        List<Rule> rules = new ArrayList<>();
        // create a filter that uses rule2 and rule3
        rules.addAll(Arrays.asList(rule2, rule3));
        filter.setElements(rules);
        StringBuilder builder = new StringBuilder();
        JavaScriptBuilder.appendFilterScript(builder, filter);
        String scriptOnly2 = builder.toString();

        rules.clear();
        // create a filter that uses rule1 (disabled), rule2, and rule3
        rules.addAll(Arrays.asList(rule1, rule2, rule3));
        rule1.setEnabled(false);
        filter.setElements(rules);
        builder = new StringBuilder();
        JavaScriptBuilder.appendFilterScript(builder, filter);
        String scriptAll3 = builder.toString();

        assertEquals(scriptOnly2, scriptAll3);
    }

    @Test
    public void testFilterDisableAllRules() throws ScriptBuilderException {
        Filter filter = new Filter();
        Rule rule1 = new RuleBuilderRule();
        Rule rule2 = new JavaScriptRule();
        Rule rule3 = new RuleBuilderRule();
        List<Rule> rules = new ArrayList<>();
        // create a filter that uses zero rules
        filter.setElements(rules);
        StringBuilder builder = new StringBuilder();
        JavaScriptBuilder.appendFilterScript(builder, filter);
        String scriptEmpty = builder.toString();

        rules.clear();
        // create a filter that uses rule1 (disabled), rule2 (disabled), and rule3 (disabled)
        rules.addAll(Arrays.asList(rule1, rule2, rule3));
        rule1.setEnabled(false);
        rule2.setEnabled(false);
        rule3.setEnabled(false);
        filter.setElements(rules);
        builder = new StringBuilder();
        JavaScriptBuilder.appendFilterScript(builder, filter);
        String scriptAll3 = builder.toString();

        assertEquals(scriptEmpty, scriptAll3);
    }

    @Test
    public void testTransformerDisable1Step() throws ScriptBuilderException {
        Transformer transformer = new Transformer();
        Step step1 = new JavaScriptStep();
        Step step2 = new MapperStep();
        Step step3 = new MessageBuilderStep();
        List<Step> steps = new ArrayList<>();
        // create a transformer that uses step2 and step3
        steps.addAll(Arrays.asList(step2, step3));
        transformer.setElements(steps);
        StringBuilder builder = new StringBuilder();
        JavaScriptBuilder.appendTransformerScript(builder, transformer, false);
        String scriptOnly2 = builder.toString();

        steps.clear();
        // create a transformer that uses step1 (disabled), step2, and step3
        steps.addAll(Arrays.asList(step1, step2, step3));
        step1.setEnabled(false);
        transformer.setElements(steps);
        builder = new StringBuilder();
        JavaScriptBuilder.appendTransformerScript(builder, transformer, false);
        String scriptAll3 = builder.toString();

        assertEquals(scriptOnly2, scriptAll3);
    }

    @Test
    public void testTransformerDisableAllSteps() throws ScriptBuilderException {
        Transformer transformer = new Transformer();
        Step step1 = new JavaScriptStep();
        Step step2 = new MapperStep();
        Step step3 = new MessageBuilderStep();
        List<Step> steps = new ArrayList<>();
        // create a transformer that zero steps
        transformer.setElements(steps);
        StringBuilder builder = new StringBuilder();
        JavaScriptBuilder.appendTransformerScript(builder, transformer, false);
        String scriptOnly2 = builder.toString();

        steps.clear();
        // create a transformer that uses step1 (disabled), step2 (disabled), and step3 (disabled)
        steps.addAll(Arrays.asList(step1, step2, step3));
        step1.setEnabled(false);
        step2.setEnabled(false);
        step3.setEnabled(false);
        transformer.setElements(steps);
        builder = new StringBuilder();
        JavaScriptBuilder.appendTransformerScript(builder, transformer, false);
        String scriptAll3 = builder.toString();

        assertEquals(scriptOnly2, scriptAll3);
    }

    @Test
    public void testNestedIteratorsInnerDisabled() throws ScriptBuilderException {
        Transformer transformer = new Transformer();
        IteratorStep outerIterStep = new IteratorStep();
        IteratorStep innerIterStep = new IteratorStep();
        MessageBuilderStep step3 = new MessageBuilderStep();
        MessageBuilderStep step4 = new MessageBuilderStep();

        // create a transformer with:
        // outerIterStep {
        //      step3
        // }
        IteratorStepProperties outerIterProps = new IteratorStepProperties();
        outerIterProps.setChildren(Arrays.asList(step3));
        outerIterStep.setProperties(outerIterProps);
        transformer.setElements(Arrays.asList(outerIterStep));
        StringBuilder builder = new StringBuilder();
        JavaScriptBuilder.appendTransformerScript(builder, transformer, false);
        String scriptOuterOnly = builder.toString();

        // create a transformer with:
        // outerIterStep {
        //      innerStep (disabled) {
        //          step4
        //      }
        //      step3
        // }
        outerIterProps = new IteratorStepProperties();
        outerIterProps.setChildren(Arrays.asList(innerIterStep, step3));
        outerIterStep.setProperties(outerIterProps);

        IteratorStepProperties innerIterProps = new IteratorStepProperties();
        innerIterProps.setChildren(Arrays.asList(step4));
        innerIterStep.setProperties(innerIterProps);

        transformer.setElements(Arrays.asList(outerIterStep));
        innerIterStep.setEnabled(false);
        builder = new StringBuilder();
        JavaScriptBuilder.appendTransformerScript(builder, transformer, false);
        String scriptInnerDisabled = builder.toString();

        assertEquals(scriptOuterOnly, scriptInnerDisabled);

    }

    @Test
    public void testNestedIteratorsOuterDisabled() throws ScriptBuilderException {
        Transformer transformer = new Transformer();
        IteratorStep outerIterStep = new IteratorStep();
        IteratorStep innerIterStep = new IteratorStep();
        MessageBuilderStep step3 = new MessageBuilderStep();
        MessageBuilderStep step4 = new MessageBuilderStep();

        // create a transformer with:
        // empty
        IteratorStepProperties outerIterProps = new IteratorStepProperties();
        outerIterProps.setChildren(Arrays.asList(step3));
        outerIterStep.setProperties(outerIterProps);

        transformer.setElements(Arrays.asList());
        StringBuilder builder = new StringBuilder();
        JavaScriptBuilder.appendTransformerScript(builder, transformer, false);
        String scriptEmpty = builder.toString();

        // create a transformer with:
        // outerIterStep (disabled) {
        //      innerStep {
        //          step4
        //      }
        //      step3
        // }
        outerIterProps = new IteratorStepProperties();
        outerIterProps.setChildren(Arrays.asList(innerIterStep, step3));
        outerIterStep.setProperties(outerIterProps);

        IteratorStepProperties innerIterProps = new IteratorStepProperties();
        innerIterProps.setChildren(Arrays.asList(step4));
        innerIterStep.setProperties(innerIterProps);

        transformer.setElements(Arrays.asList(outerIterStep));
        outerIterStep.setEnabled(false);
        builder = new StringBuilder();
        JavaScriptBuilder.appendTransformerScript(builder, transformer, false);
        String scriptInnerDisabled = builder.toString();

        assertEquals(scriptEmpty, scriptInnerDisabled);
    }
}
