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
    private static final String XML3 = "<?xml version=\"1.0\" ?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><v3:PRPA_IN201301UV02 ITSVersion=\"XML_1.0\" xmlns:v3=\"urn:hl7-org:v3\"><soapenv:id root=\"abfaa36c-a569-4d7c-b0f0-dee9c41cacd2\"/></v3:PRPA_IN201301UV02></soapenv:Body></soapenv:Envelope>";
    private static final String JSON1 = "{\"root\":{\"node1\":{\"id\":[123,456],\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON2 = "{\"root\":{\"node1\":{\"id\":123,\"id\":456,\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON3 = "{\"root\":{\"node1\":{\"id\":[\"123\",\"456\"],\"name\":null,\"flag\":\"true\"},\"node2\":{\"id\":\"789\",\"name\":\"testing\",\"flag\":\"false\"}}}";
    private static final String JSON4 = "{\"Envelope\":{\"@xmlns\":\"http://www.w3.org/2003/05/soap-envelope\",\"Body\":{\"PRPA_IN201301UV02\":{\"@xmlns\":\"urn:hl7-org:v3\",\"@ITSVersion\":\"XML_1.0\",\"id\":{\"@xmlns\":\"http://www.w3.org/2003/05/soap-envelope\",\"@root\":\"abfaa36c-a569-4d7c-b0f0-dee9c41cacd2\"}}}}}";

    @Test
    public void testXmlToJson1() throws Exception {
        // No pretty printing
        assertEquals(JSON1, XmlUtil.toJson(XML1, false));
        assertEquals(JSON1, XmlUtil.toJson(XML1, true, true, false, false));
    }

    @Test
    public void testXmlToJson2() throws Exception {
        // Pretty printing
        assertFalse(JSON1.equals(XmlUtil.toJson(XML1, true, true, true, false)));
        assertEquals(JsonUtil.prettyPrint(JSON1), JsonUtil.prettyPrint(XmlUtil.toJson(XML1, true, true, true, false)));
    }

    @Test
    public void testXmlToJson3() throws Exception {
        // Auto-array
        assertEquals(JSON2, XmlUtil.toJson(XML1, false, true, false, false));
    }

    @Test
    public void testXmlToJson4() throws Exception {
        // Auto-primitive
        assertEquals(JSON3, XmlUtil.toJson(XML1, true, false, false, false));
    }

    @Test
    public void testXmlToJson5() throws Exception {
        // Stripping bound prefixes on
        assertEquals(JSON4, XmlUtil.toJson(XML3, true));
    }

    @Test
    public void testXmlToJson6() throws Exception {
        // Stripping bound prefixes by default
        assertEquals(JSON4, XmlUtil.toJson(XML3));
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
