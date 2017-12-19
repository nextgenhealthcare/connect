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
    private static final String XML4 = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:test=\"testing\"><Body><v3:PRPA_IN201301UV02 ITSVersion=\"XML_1.0\" xmlns:v3=\"urn:hl7-org:v3\"><soapenv:id root=\"\"/></v3:PRPA_IN201301UV02></Body></soapenv:Envelope>";
    private static final String XML5 = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><Body xmlns=\"http://www.w3.org/2003/05/soap-envelope\"><v3:PRPA_IN201301UV02 ITSVersion=\"XML_1.0\" xmlns:v3=\"urn:hl7-org:v3\"><v3:id root=\"\"/></v3:PRPA_IN201301UV02></Body></soapenv:Envelope>";
    private static final String XML6 = "<?xml version=\"1.0\" ?><root xmlns=\"http://test1.com\"><node1 xmlns=\"http://test2.com\"><id>123</id><name/><flag>true</flag></node1><node2><id>789</id><name>testing</name><flag>false</flag></node2></root>";
    private static final String XML7 = "<?xml version=\"1.0\" ?><v1:root xmlns:v1=\"http://test1.com\" xmlns=\"http://testdefault1.com\"><v2:node1 xmlns:v2=\"http://test2.com\"><id>123</id><name/><flag>true</flag></v2:node1><node2><id>789</id><name>testing</name><flag>false</flag></node2></v1:root>";
    private static final String XML8 = "<?xml version=\"1.0\" ?><v1:root xmlns:v1=\"http://test1.com\" xmlns=\"http://testdefault1.com\"><v2:node1 xmlns:v2=\"http://test2.com\"><id>123</id><name/><flag>true</flag><v1:node2><name/><id>234</id><node3><id>345</id></node3></v1:node2></v2:node1><node4><id>789</id><name>testing</name><flag>false</flag></node4></v1:root>";
    private static final String XML9 = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><wsa:To>https://fake.hie.com:9002/pixpdq/PIXManager_Service</wsa:To><wsa:ReplyTo><wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address></wsa:ReplyTo><wsa:MessageID>urn:uuid:14d6b384-54d2-9254-35b3-530717f6bc9a</wsa:MessageID><wsa:Action>urn:hl7-org:v3:PRPA_IN201301UV02</wsa:Action></soapenv:Header><soapenv:Body><cda:PRPA_IN201301UV02 xmlns:cda=\"urn:hl7-org:v3\" ITSVersion=\"XML_1.0\"></cda:PRPA_IN201301UV02></soapenv:Body></soapenv:Envelope>";
    private static final String XML10 = "<?xml version=\"1.0\" ?><Envelope xmlns=\"http://www.w3.org/2003/05/soap-envelope\"><Body><PRPA_IN201301UV02 xmlns=\"urn:hl7-org:v3\" ITSVersion=\"XML_1.0\"><id xmlns=\"http://www.w3.org/2003/05/soap-envelope\" root=\"abfaa36c-a569-4d7c-b0f0-dee9c41cacd2\"></id></PRPA_IN201301UV02></Body></Envelope>";
    private static final String XML11 = "<?xml version=\"1.0\" ?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><v3:PRPA_IN201301UV02 ITSVersion=\"XML_1.0\" xmlns:v3=\"urn:hl7-org:v3\"><soapenv:id root=\"abfaa36c-a569-4d7c-b0f0-dee9c41cacd2\" xmlns:soapenv=\"http://www.somedomain.org/soap-envelope\"><v3:test><soapenv:test/></v3:test></soapenv:id></v3:PRPA_IN201301UV02></soapenv:Body></soapenv:Envelope>";
    private static final String XML12 = "<?xml version=\"1.0\" ?><Envelope xmlns=\"http://www.w3.org/2003/05/soap-envelope\"><Body><PRPA_IN201301UV02 xmlns=\"urn:hl7-org:v3\" ITSVersion=\"XML_1.0\"><id xmlns=\"http://www.somedomain.org/soap-envelope\" root=\"abfaa36c-a569-4d7c-b0f0-dee9c41cacd2\"><test xmlns=\"urn:hl7-org:v3\"><test xmlns=\"http://www.somedomain.org/soap-envelope\"></test></test></id></PRPA_IN201301UV02></Body></Envelope>";
    private static final String JSON1 = "{\"root\":{\"node1\":{\"id\":[123,456],\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON2 = "{\"root\":{\"node1\":{\"id\":123,\"id\":456,\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON3 = "{\"root\":{\"node1\":{\"id\":[\"123\",\"456\"],\"name\":null,\"flag\":\"true\"},\"node2\":{\"id\":\"789\",\"name\":\"testing\",\"flag\":\"false\"}}}";
    private static final String JSON4 = "{\"Envelope\":{\"@xmlns\":\"http://www.w3.org/2003/05/soap-envelope\",\"Body\":{\"PRPA_IN201301UV02\":{\"@xmlns\":\"urn:hl7-org:v3\",\"@ITSVersion\":\"XML_1.0\",\"id\":{\"@xmlns\":\"http://www.w3.org/2003/05/soap-envelope\",\"@root\":\"abfaa36c-a569-4d7c-b0f0-dee9c41cacd2\"}}}}}";
    private static final String JSON5 = "{\"Envelope\":{\"@xmlns\":\"http://www.w3.org/2003/05/soap-envelope\",\"Body\":{\"@xmlns\":\"\",\"PRPA_IN201301UV02\":{\"@xmlns\":\"urn:hl7-org:v3\",\"@ITSVersion\":\"XML_1.0\",\"id\":{\"@xmlns\":\"http://www.w3.org/2003/05/soap-envelope\",\"@root\":\"\"}}}}}";
    private static final String JSON6 = "{\"Envelope\":{\"@xmlns\":\"http://www.w3.org/2003/05/soap-envelope\",\"Body\":{\"PRPA_IN201301UV02\":{\"@xmlns\":\"urn:hl7-org:v3\",\"@ITSVersion\":\"XML_1.0\",\"id\":{\"@root\":\"\"}}}}}";
    private static final String JSON7 = "{\"root\":{\"@xmlns\":\"\",\"node1\":{\"id\":[123,456],\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON8 = "{\"root\":{\"@xmlns\":\"http://test1.com\",\"node1\":{\"@xmlns\":\"http://test2.com\",\"id\":123,\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON9 = "{\"root\":{\"@xmlns\":\"http://test1.com\",\"node1\":{\"@xmlns\":\"http://test2.com\",\"id\":{\"@xmlns\":\"http://testdefault1.com\",\"$\":123},\"name\":{\"@xmlns\":\"http://testdefault1.com\"},\"flag\":{\"@xmlns\":\"http://testdefault1.com\",\"$\":true}},\"node2\":{\"@xmlns\":\"http://testdefault1.com\",\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON10 = "{\"root\":{\"@xmlns\":\"http://test1.com\",\"node1\":{\"@xmlns\":\"http://test2.com\",\"id\":{\"@xmlns\":\"http://testdefault1.com\",\"$\":123},\"name\":{\"@xmlns\":\"http://testdefault1.com\"},\"flag\":{\"@xmlns\":\"http://testdefault1.com\",\"$\":true},\"node2\":{\"@xmlns\":\"http://test1.com\",\"name\":{\"@xmlns\":\"http://testdefault1.com\"},\"id\":{\"@xmlns\":\"http://testdefault1.com\",\"$\":234},\"node3\":{\"@xmlns\":\"http://testdefault1.com\",\"id\":345}}},\"node4\":{\"@xmlns\":\"http://testdefault1.com\",\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON11 = "{\"Envelope\":{\"@xmlns\":\"http://www.w3.org/2003/05/soap-envelope\",\"Header\":{\"To\":{\"@xmlns\":\"http://www.w3.org/2005/08/addressing\",\"$\":\"https://fake.hie.com:9002/pixpdq/PIXManager_Service\"},\"ReplyTo\":{\"@xmlns\":\"http://www.w3.org/2005/08/addressing\",\"Address\":\"http://www.w3.org/2005/08/addressing/anonymous\"},\"MessageID\":{\"@xmlns\":\"http://www.w3.org/2005/08/addressing\",\"$\":\"urn:uuid:14d6b384-54d2-9254-35b3-530717f6bc9a\"},\"Action\":{\"@xmlns\":\"http://www.w3.org/2005/08/addressing\",\"$\":\"urn:hl7-org:v3:PRPA_IN201301UV02\"}},\"Body\":{\"PRPA_IN201301UV02\":{\"@xmlns\":\"urn:hl7-org:v3\",\"@ITSVersion\":\"XML_1.0\"}}}}";

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
    public void testXmlToJson7() throws Exception {
    	// Stripping bound prefixes on
    	assertEquals(JSON5, XmlUtil.toJson(XML4, true));
    }
    
    @Test
    public void testXmlToJson8() throws Exception {
    	// Stripping bound prefixes on
    	assertEquals(JSON6, XmlUtil.toJson(XML5, true));
    }
    
    @Test
    public void testXmlToJson9() throws Exception {
    	// Stripping bound prefixes on, but no declared prefixes or namespaces in input
    	assertEquals(JSON7, XmlUtil.toJson(XML1, true));
    }
    
    @Test
    public void testXmlToJson10() throws Exception {
    	// Stripping bound prefixes on, with no declared prefixes in input and the default namespace changes
    	assertEquals(JSON8, XmlUtil.toJson(XML6, true));
    } 
    
    @Test
    public void textXmlToJson11() throws Exception {
    	// Stripping bound prefixes on. Prefixed node declares namespace for its prefix and the default namespace.
    	assertEquals(JSON9, XmlUtil.toJson(XML7, true));
    }
    
    @Test
    public void textXmlToJson12() throws Exception {
    	// Stripping bound prefixes on. More complex nesting of namespaces and prefixes.
    	assertEquals(JSON10, XmlUtil.toJson(XML8, true));
    }
    
    @Test
    public void textXmlToJson13() throws Exception {
    	// Stripping bound prefixes on. More complex nesting of namespaces and prefixes.
    	assertEquals(JSON11, XmlUtil.toJson(XML9, true));
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
    
    @Test
    public void testXmlToJsonToXml1() throws Exception {
        String json = XmlUtil.toJson(XML3, true, true, false, true);
        assertEquals(JSON4, json);
        assertEquals(XML10, JsonUtil.toXml(json, false, false));
    }

    @Test
    public void testXmlToJsonToXml2() throws Exception {
        String json = XmlUtil.toJson(XML11, true, true, false, true);
        String xml = JsonUtil.toXml(json, false, false);
        assertEquals(XML12, xml);
    }
}
