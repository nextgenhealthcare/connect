package com.mirth.connect.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.ConnectorMessage;

public class ValueReplacerTests {
    private ValueReplacer replacer;
    private Map<String, List<String>> map;
    private ConnectorMessage connectorMessage;

    @Before
    public void setUp() throws Exception {
        replacer = new TestValueReplacer();

        map = new HashMap<String, List<String>>();

        List<String> list = new ArrayList<String>();
        list.add("value1");
        list.add("$velocity1");
        map.put("key1", list);

        list = new ArrayList<String>();
        list.add("value2");
        list.add("$velocity2");
        map.put("$velocityKey2", list);

        list = new ArrayList<String>();
        list.add("$velocityUnknown1");
        map.put("key3", list);

        list = new ArrayList<String>();
        list.add("value3");
        map.put("$velocityKeyUnknown1", list);

        list = new ArrayList<String>();
        list.add("$velocityUnknown2");
        map.put("$velocityKeyUnknown2", list);

        connectorMessage = new ConnectorMessage("channelId", "channelName", 1, 1, "serverId", Calendar.getInstance(), com.mirth.connect.donkey.model.message.Status.RECEIVED);
    }

    @After
    public void tearDown() throws Exception {
        replacer = null;
        map = null;
        connectorMessage = null;
    }

    @Test
    public void testHasReplaceableValues() {
        String value = null;
        assertFalse(ValueReplacer.hasReplaceableValues(value));

        value = "someValue";
        assertFalse(ValueReplacer.hasReplaceableValues(value));

        value = "$anotherValue";
        assertTrue(ValueReplacer.hasReplaceableValues(value));

        value = "someOther$Value";
        assertTrue(ValueReplacer.hasReplaceableValues(value));

        value = "someOtherValue$";
        assertTrue(ValueReplacer.hasReplaceableValues(value));

        value = "$omeOther$Value$";
        assertTrue(ValueReplacer.hasReplaceableValues(value));
    }

    @Test
    public void testReplaceKeysAndValuesInMap() {
        // replaceKeysAndValuesInMap(Map<String, List<String> map, ConnectorMessage message) will call loadContextFromConnectorMessage(VelocityContext context, ConnectorMessage connectorMessage) below
        Map<String, List<String>> replacedMap = replacer.replaceKeysAndValuesInMap(map, connectorMessage);

        assertTrue(replacedMap.containsKey("key1"));
        assertTrue(replacedMap.containsKey("valueOfVelocityKey2"));
        assertTrue(replacedMap.containsKey("key3"));
        assertTrue(replacedMap.containsKey("$velocityKeyUnknown1"));
        assertTrue(replacedMap.containsKey("$velocityKeyUnknown2"));

        List<String> list = replacedMap.get("key1");
        assertEquals(2, list.size());
        assertTrue(list.contains("value1"));
        assertTrue(list.contains("valueOfVelocity1"));

        list = replacedMap.get("valueOfVelocityKey2");
        assertEquals(2, list.size());
        assertTrue(list.contains("value2"));
        assertTrue(list.contains("valueOfVelocity2"));

        list = replacedMap.get("key3");
        assertEquals(1, list.size());
        assertTrue(list.contains("$velocityUnknown1"));

        list = replacedMap.get("$velocityKeyUnknown1");
        assertEquals(1, list.size());
        assertTrue(list.contains("value3"));

        list = replacedMap.get("$velocityKeyUnknown2");
        assertEquals(1, list.size());
        assertTrue(list.contains("$velocityUnknown2"));
    }

    private class TestValueReplacer extends ValueReplacer {
        @Override
        protected void loadContextFromConnectorMessage(VelocityContext context, ConnectorMessage connectorMessage) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("velocity1", "valueOfVelocity1");
            map.put("velocity2", "valueOfVelocity2");
            map.put("velocityKey2", "valueOfVelocityKey2");
            map.put("unusedVelocity1", "valueOfUnusedVelocity1");
            loadContextFromMap(context, map);
            super.loadContextFromConnectorMessage(context, connectorMessage);
        }
    }

}
