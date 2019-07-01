/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.destinationsetfilter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.plugins.destinationsetfilter.DestinationSetFilterStep.Behavior;
import com.mirth.connect.plugins.destinationsetfilter.DestinationSetFilterStep.Condition;
import com.mirth.connect.util.JavaScriptTestUtil;

public class DestinationSetFilterStepTest {

    @BeforeClass
    public static void setup() throws Exception {
        JavaScriptTestUtil.setup();
    }

    @Test
    public void testExists1() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['PID'][0]['PID.3'][0]['PID.3.1'].toString()");
        step.setCondition(Condition.EXISTS);
        assertTrue(test(step, 1, 2, 3, 4).isEmpty());
    }

    @Test
    public void testExists2() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['PV1']['PV1.1']['PV1.1.1'].toString()");
        step.setCondition(Condition.EXISTS);
        Integer[] metaDataIds = new Integer[] { 1, 2, 3, 4 };
        assertArrayEquals(metaDataIds, test(step, metaDataIds).toArray());
    }

    @Test
    public void testNotExist1() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['PID'][0]['PID.3'][0]['PID.3.1'].toString()");
        step.setCondition(Condition.NOT_EXIST);
        Integer[] metaDataIds = new Integer[] { 1, 2, 3, 4 };
        assertArrayEquals(metaDataIds, test(step, metaDataIds).toArray());
    }

    @Test
    public void testNotExist2() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['PV1']['PV1.1']['PV1.1.1'].toString()");
        step.setCondition(Condition.NOT_EXIST);
        assertTrue(test(step, 1, 2, 3, 4).isEmpty());
    }

    @Test
    public void testEquals1() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['PID'][0]['PID.3'][0]['PID.3.1'].toString()");
        step.setCondition(Condition.EQUALS);
        step.getValues().add("'1'");
        step.getValues().add("'2'");
        assertTrue(test(step, 1, 2, 3, 4).isEmpty());
    }

    @Test
    public void testEquals2() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['PID'][0]['PID.3'][0]['PID.3.1'].toString()");
        step.setCondition(Condition.EQUALS);
        step.getValues().add("'3'");
        step.getValues().add("'4'");
        Integer[] metaDataIds = new Integer[] { 1, 2, 3, 4 };
        assertArrayEquals(metaDataIds, test(step, metaDataIds).toArray());
    }

    @Test
    public void testEquals3() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['PID'][0]['PID.2'][0]['PID.2.1'].toString()");
        step.setCondition(Condition.EQUALS);
        assertTrue(test(step, 1, 2, 3, 4).isEmpty());
    }

    @Test
    public void testNotEquals1() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['PID'][0]['PID.3'][0]['PID.3.1'].toString()");
        step.setCondition(Condition.NOT_EQUAL);
        step.getValues().add("'1'");
        step.getValues().add("'2'");
        Integer[] metaDataIds = new Integer[] { 1, 2, 3, 4 };
        assertArrayEquals(metaDataIds, test(step, metaDataIds).toArray());
    }

    @Test
    public void testNotEquals2() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['PID'][0]['PID.3'][0]['PID.3.1'].toString()");
        step.setCondition(Condition.NOT_EQUAL);
        step.getValues().add("'3'");
        step.getValues().add("'4'");
        assertTrue(test(step, 1, 2, 3, 4).isEmpty());
    }

    @Test
    public void testNotEquals3() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['PID'][0]['PID.3'][0]['PID.3.1'].toString()");
        step.setCondition(Condition.NOT_EQUAL);
        assertTrue(test(step, 1, 2, 3, 4).isEmpty());
    }

    @Test
    public void testContains1() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['OBX'][0]['OBX.2'][0]['OBX.2.1'].toString()");
        step.setCondition(Condition.CONTAINS);
        step.getValues().add("'S'");
        step.getValues().add("'Z'");
        assertTrue(test(step, 1, 2, 3, 4).isEmpty());
    }

    @Test
    public void testContains2() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['OBX'][1]['OBX.2'][0]['OBX.2.1'].toString()");
        step.setCondition(Condition.CONTAINS);
        step.getValues().add("'S'");
        step.getValues().add("'Z'");
        Integer[] metaDataIds = new Integer[] { 1, 2, 3, 4 };
        assertArrayEquals(metaDataIds, test(step, metaDataIds).toArray());
    }

    @Test
    public void testContains3() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['PID'][0]['PID.2'][0]['PID.2.1'].toString()");
        step.setCondition(Condition.CONTAINS);
        assertTrue(test(step, 1, 2, 3, 4).isEmpty());
    }

    @Test
    public void testNotContains1() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['OBX'][0]['OBX.2'][0]['OBX.2.1'].toString()");
        step.setCondition(Condition.NOT_CONTAIN);
        step.getValues().add("'S'");
        step.getValues().add("'Z'");
        Integer[] metaDataIds = new Integer[] { 1, 2, 3, 4 };
        assertArrayEquals(metaDataIds, test(step, metaDataIds).toArray());
    }

    @Test
    public void testNotContains2() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL);
        step.setField("msg['OBX'][1]['OBX.2'][0]['OBX.2.1'].toString()");
        step.setCondition(Condition.NOT_CONTAIN);
        step.getValues().add("'S'");
        step.getValues().add("'Z'");
        assertTrue(test(step, 1, 2, 3, 4).isEmpty());
    }

    @Test
    public void testRemove1() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE);
        step.getMetaDataIds().add(1);
        step.getMetaDataIds().add(3);
        step.setField("msg['PID'][0]['PID.3'][0]['PID.3.1'].toString()");
        step.setCondition(Condition.EXISTS);
        assertArrayEquals(new Integer[] { 2, 4 }, test(step, 1, 2, 3, 4).toArray());
    }

    @Test
    public void testRemove2() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE);
        step.getMetaDataIds().add(5);
        step.setField("msg['PID'][0]['PID.3'][0]['PID.3.1'].toString()");
        step.setCondition(Condition.EXISTS);
        assertArrayEquals(new Integer[] { 1, 2, 3, 4 }, test(step, 1, 2, 3, 4).toArray());
    }

    @Test
    public void testRemoveAllExcept1() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL_EXCEPT);
        step.getMetaDataIds().add(1);
        step.getMetaDataIds().add(3);
        step.setField("msg['PID'][0]['PID.3'][0]['PID.3.1'].toString()");
        step.setCondition(Condition.EXISTS);
        assertArrayEquals(new Integer[] { 1, 3 }, test(step, 1, 2, 3, 4).toArray());
    }

    @Test
    public void testRemoveAllExcept2() throws Exception {
        DestinationSetFilterStep step = new DestinationSetFilterStep();
        step.setBehavior(Behavior.REMOVE_ALL_EXCEPT);
        step.getMetaDataIds().add(5);
        step.setField("msg['PID'][0]['PID.3'][0]['PID.3.1'].toString()");
        step.setCondition(Condition.EXISTS);
        assertTrue(test(step, 1, 2, 3, 4).isEmpty());
    }

    private Set<Integer> test(DestinationSetFilterStep step, Integer... metaDataIds) throws Exception {
        Set<Integer> destinations = new LinkedHashSet<Integer>(Arrays.asList(metaDataIds));

        ConnectorMessage connectorMessage = new ConnectorMessage();
        connectorMessage.setMetaDataId(0);
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(Constants.DESTINATION_SET_KEY, destinations);
        connectorMessage.setSourceMap(sourceMap);

        JavaScriptTestUtil.testTransformerStep(step, connectorMessage);

        return destinations;
    }
}