package com.mirth.connect.server.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TemplateValueReplacerTests {

    private TemplateValueReplacer templateReplacer;

    @Before
    public void setUp() throws Exception {
        templateReplacer = new TestTemplateValueReplacer();
    }

    @After
    public void tearDown() throws Exception {
        templateReplacer = null;
    }

    @Test
    public void testReplaceValues() {
        // replaceValues(String template, String channelId, String channelname) calls loadContextFromMap(VelocityContext context, Map<String, ?> map) below
        assertEquals("value1", templateReplacer.replaceValues("value1", "channelId", "channelName"));
        assertEquals("valueOfVelocity1", templateReplacer.replaceValues("$velocity1", "channelId", "channelName"));
        assertEquals("$velocityUnknown", templateReplacer.replaceValues("$velocityUnknown", "channelId", "channelName"));
        assertEquals("unusedVelocity1", templateReplacer.replaceValues("unusedVelocity1", "channelId", "channelName"));
    }

    private class TestTemplateValueReplacer extends TemplateValueReplacer {
        @Override
        protected void loadContextFromMap(VelocityContext context, Map<String, ?> map) {
            Map<String, String> velocityMap = new HashMap<String, String>();
            velocityMap.put("velocity1", "valueOfVelocity1");
            velocityMap.put("unusedVelocity1", "valueOfUnusedVelocity1");
            super.loadContextFromMap(context, velocityMap);
        }
    }
}
