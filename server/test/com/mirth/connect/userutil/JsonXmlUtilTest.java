/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class JsonXmlUtilTest {

    private static final String XML1 = "<?xml version=\"1.0\" ?><root><node1><id>123</id><id>456</id><name></name><flag>true</flag></node1><node2><id>789</id><name>testing</name><flag>false</flag></node2></root>";
    private static final String XML2 = "<?xml version=\"1.0\" ?><root><node1><?xml-multiple id?><id>123</id><id>456</id><name></name><flag>true</flag></node1><node2><id>789</id><name>testing</name><flag>false</flag></node2></root>";
    private static final String JSON1 = "{\"root\":{\"node1\":{\"id\":[123,456],\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON2 = "{\"root\":{\"node1\":{\"id\":123,\"id\":456,\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON3 = "{\"root\":{\"node1\":{\"id\":[\"123\",\"456\"],\"name\":null,\"flag\":\"true\"},\"node2\":{\"id\":\"789\",\"name\":\"testing\",\"flag\":\"false\"}}}";

    @Test
    public void testXmlToJson1() throws Exception {
        // No pretty printing
        assertEquals(JSON1, XmlUtil.toJson(XML1));
        assertEquals(JSON1, XmlUtil.toJson(XML1, true, true, false));
    }

    @Test
    public void testXmlToJson2() throws Exception {
        // Pretty printing
        assertFalse(JSON1.equals(XmlUtil.toJson(XML1, true, true, true)));
        assertEquals(JsonUtil.prettyPrint(JSON1), JsonUtil.prettyPrint(XmlUtil.toJson(XML1, true, true, true)));
    }

    @Test
    public void testXmlToJson3() throws Exception {
        // Auto-array
        assertEquals(JSON2, XmlUtil.toJson(XML1, false, true, false));
    }

    @Test
    public void testXmlToJson4() throws Exception {
        // Auto-primitive
        assertEquals(JSON3, XmlUtil.toJson(XML1, true, false, false));
    }

    @Test
    public void testJsonToXml1() throws Exception {
        // No pretty printing
        assertEquals(XML1, JsonUtil.toXml(JSON1));
        assertEquals(XML1, JsonUtil.toXml(JSON1, false, false));
    }

    @Test
    public void testJsonToXml2() throws Exception {
        // Pretty printing
        assertFalse(XML1.equals(JsonUtil.toXml(JSON1, false, true)));
        assertEquals(XmlUtil.prettyPrint(XML1), XmlUtil.prettyPrint(JsonUtil.toXml(JSON1, false, true)));
    }

    @Test
    public void testJsonToXml3() throws Exception {
        // Multiple PI
        assertEquals(XML2, JsonUtil.toXml(JSON1, true, false));
    }
}
