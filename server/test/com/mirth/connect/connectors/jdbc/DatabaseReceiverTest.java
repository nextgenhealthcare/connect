/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.mirth.connect.donkey.util.DonkeyElement;

public class DatabaseReceiverTest {

    @Test
    public void testFixColumnName_Null() throws Exception {
        assertEquals("_", new DatabaseReceiver().fixColumnName(null));
    }

    @Test
    public void testFixColumnName_Empty() throws Exception {
        assertEquals("_", new DatabaseReceiver().fixColumnName(""));
    }

    @Test
    public void testFixColumnName_Blank() throws Exception {
        assertEquals("_", new DatabaseReceiver().fixColumnName("    "));
    }

    @Test
    public void testFixColumnName_InvalidStartChar() throws Exception {
        assertEquals("_abc", new DatabaseReceiver().fixColumnName(".abc"));
        assertEquals("_abc", new DatabaseReceiver().fixColumnName("-abc"));
        assertEquals("_abc", new DatabaseReceiver().fixColumnName("0abc"));
        assertEquals("_abc", new DatabaseReceiver().fixColumnName(" abc"));
    }

    @Test
    public void testFixColumnName_InvalidChar() throws Exception {
        assertEquals("ab_c", new DatabaseReceiver().fixColumnName("ab c"));
        assertEquals(":ab_c", new DatabaseReceiver().fixColumnName(":ab c"));
        assertEquals("ab_c", new DatabaseReceiver().fixColumnName("ab  c"));
        assertEquals("ab_c_", new DatabaseReceiver().fixColumnName("ab c "));
        assertEquals("_ab_c_", new DatabaseReceiver().fixColumnName("[ab c ]"));
    }

    @Test
    public void testFixColumnName_Valid() throws Exception {
        assertEquals("abc", new DatabaseReceiver().fixColumnName("abc"));
        assertEquals("a.bc", new DatabaseReceiver().fixColumnName("a.bc"));
        assertEquals("abc-", new DatabaseReceiver().fixColumnName("abc-"));
        assertEquals("azAZ09-_.", new DatabaseReceiver().fixColumnName("azAZ09-_."));
    }

    @Test
    public void testResultMapToXml_Valid() throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("abc", "test");
        resultMap.put("a.bc", "test");
        resultMap.put("abc-", "test");
        resultMap.put("azAZ09-_.", "test");

        DatabaseReceiver receiver = new DatabaseReceiver();
        receiver.connectorProperties = new DatabaseReceiverProperties();

        DonkeyElement result = new DonkeyElement(receiver.resultMapToXml(resultMap));
        List<String> tagNames = new ArrayList<String>();
        for (DonkeyElement child : result.getChildElements()) {
            tagNames.add(child.getTagName());
        }
        assertTrue(tagNames.contains("abc"));
        assertTrue(tagNames.contains("a.bc"));
        assertTrue(tagNames.contains("abc-"));
        assertTrue(tagNames.contains("azAZ09-_."));
    }

    @Test
    public void testResultMapToXml_Invalid() throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(".abc", "test");
        resultMap.put("ab c", "test");
        resultMap.put("[ab c ]", "test");

        DatabaseReceiver receiver = new DatabaseReceiver();
        receiver.connectorProperties = new DatabaseReceiverProperties();

        DonkeyElement result = new DonkeyElement(receiver.resultMapToXml(resultMap));
        List<String> tagNames = new ArrayList<String>();
        for (DonkeyElement child : result.getChildElements()) {
            tagNames.add(child.getTagName());
        }
        assertTrue(tagNames.contains("_abc"));
        assertTrue(tagNames.contains("ab_c"));
        assertTrue(tagNames.contains("_ab_c_"));
    }
}
