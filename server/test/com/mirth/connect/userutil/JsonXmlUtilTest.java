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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
    private static final String XML10 = "<?xml version=\"1.0\" ?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><v3:PRPA_IN201301UV02 xmlns:v3=\"urn:hl7-org:v3\" ITSVersion=\"XML_1.0\"><soapenv:id root=\"abfaa36c-a569-4d7c-b0f0-dee9c41cacd2\"></soapenv:id></v3:PRPA_IN201301UV02></soapenv:Body></soapenv:Envelope>";
    private static final String XML11 = "<?xml version=\"1.0\" ?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><v3:PRPA_IN201301UV02 ITSVersion=\"XML_1.0\" xmlns:v3=\"urn:hl7-org:v3\"><soapenv:id root=\"abfaa36c-a569-4d7c-b0f0-dee9c41cacd2\" xmlns:soapenv=\"http://www.somedomain.org/soap-envelope\"><v3:test><soapenv:test/></v3:test></soapenv:id></v3:PRPA_IN201301UV02></soapenv:Body></soapenv:Envelope>";
    private static final String XML12 = "<?xml version=\"1.0\" ?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><v3:PRPA_IN201301UV02 xmlns:v3=\"urn:hl7-org:v3\" ITSVersion=\"XML_1.0\"><soapenv:id xmlns:soapenv=\"http://www.somedomain.org/soap-envelope\" root=\"abfaa36c-a569-4d7c-b0f0-dee9c41cacd2\"><v3:test><soapenv:test></soapenv:test></v3:test></soapenv:id></v3:PRPA_IN201301UV02></soapenv:Body></soapenv:Envelope>";
    private static final String XML13 = "<?xml version=\"1.0\" ?><env:Envelope xmlns:env=\"soap\"><env:Body><Test value=\"abc\"><ValueWithoutAttr>123</ValueWithoutAttr><ValueWithAttr attr=\"test\">123</ValueWithAttr></Test></env:Body></env:Envelope>";
    private static final String XML14 = "<Root xmlns:ns1=\"ns1\" xmlns:ns2=\"ns2\"><ns1:Child ns1:value=\"val1\" ns2:value=\"val2\"/><ns2:Child>test</ns2:Child><Child>test2</Child></Root>";
    private static final String XML15 = "<?xml version=\"1.0\"?><Book isbn=\"1234\" pfx:cover=\"hard\" xmlns=\"http://www.library.com\" xmlns:pfx=\"http://www.library.com\"><Title>Sherlock Holmes</Title><Author>Arthur Conan Doyle</Author></Book>";

    private static final String JSON1 = "{\"root\":{\"node1\":{\"id\":[123,456],\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON2 = "{\"root\":{\"node1\":{\"id\":123,\"id\":456,\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON3 = "{\"root\":{\"node1\":{\"id\":[\"123\",\"456\"],\"name\":null,\"flag\":\"true\"},\"node2\":{\"id\":\"789\",\"name\":\"testing\",\"flag\":\"false\"}}}";
    private static final String JSON4 = "{\"Envelope\":{\"@xmlnsprefix\":\"soapenv\",\"@xmlns:soapenv\":\"http://www.w3.org/2003/05/soap-envelope\",\"Body\":{\"@xmlnsprefix\":\"soapenv\",\"PRPA_IN201301UV02\":{\"@xmlnsprefix\":\"v3\",\"@xmlns:v3\":\"urn:hl7-org:v3\",\"@ITSVersion\":\"XML_1.0\",\"id\":{\"@xmlnsprefix\":\"soapenv\",\"@root\":\"abfaa36c-a569-4d7c-b0f0-dee9c41cacd2\"}}}}}";
    private static final String JSON5 = "{\"Envelope\":{\"@xmlnsprefix\":\"soapenv\",\"@xmlns:soapenv\":\"http://www.w3.org/2003/05/soap-envelope\",\"@xmlns:test\":\"testing\",\"Body\":{\"PRPA_IN201301UV02\":{\"@xmlnsprefix\":\"v3\",\"@xmlns:v3\":\"urn:hl7-org:v3\",\"@ITSVersion\":\"XML_1.0\",\"id\":{\"@xmlnsprefix\":\"soapenv\",\"@root\":\"\"}}}}}";
    private static final String JSON6 = "{\"Envelope\":{\"@xmlnsprefix\":\"soapenv\",\"@xmlns:soapenv\":\"http://www.w3.org/2003/05/soap-envelope\",\"Body\":{\"@xmlns\":\"http://www.w3.org/2003/05/soap-envelope\",\"PRPA_IN201301UV02\":{\"@xmlnsprefix\":\"v3\",\"@xmlns:v3\":\"urn:hl7-org:v3\",\"@ITSVersion\":\"XML_1.0\",\"id\":{\"@xmlnsprefix\":\"v3\",\"@root\":\"\"}}}}}";
    private static final String JSON7 = "{\"root\":{\"node1\":{\"id\":[123,456],\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON8 = "{\"root\":{\"@xmlns\":\"http://test1.com\",\"node1\":{\"@xmlns\":\"http://test2.com\",\"id\":123,\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON9 = "{\"root\":{\"@xmlnsprefix\":\"v1\",\"@xmlns:v1\":\"http://test1.com\",\"@xmlns\":\"http://testdefault1.com\",\"node1\":{\"@xmlnsprefix\":\"v2\",\"@xmlns:v2\":\"http://test2.com\",\"id\":123,\"name\":null,\"flag\":true},\"node2\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON10 = "{\"root\":{\"@xmlnsprefix\":\"v1\",\"@xmlns:v1\":\"http://test1.com\",\"@xmlns\":\"http://testdefault1.com\",\"node1\":{\"@xmlnsprefix\":\"v2\",\"@xmlns:v2\":\"http://test2.com\",\"id\":123,\"name\":null,\"flag\":true,\"node2\":{\"@xmlnsprefix\":\"v1\",\"name\":null,\"id\":234,\"node3\":{\"id\":345}}},\"node4\":{\"id\":789,\"name\":\"testing\",\"flag\":false}}}";
    private static final String JSON11 = "{\"Envelope\":{\"@xmlnsprefix\":\"soapenv\",\"@xmlns:soapenv\":\"http://www.w3.org/2003/05/soap-envelope\",\"Header\":{\"@xmlnsprefix\":\"soapenv\",\"@xmlns:wsa\":\"http://www.w3.org/2005/08/addressing\",\"To\":{\"@xmlnsprefix\":\"wsa\",\"$\":\"https://fake.hie.com:9002/pixpdq/PIXManager_Service\"},\"ReplyTo\":{\"@xmlnsprefix\":\"wsa\",\"Address\":{\"@xmlnsprefix\":\"wsa\",\"$\":\"http://www.w3.org/2005/08/addressing/anonymous\"}},\"MessageID\":{\"@xmlnsprefix\":\"wsa\",\"$\":\"urn:uuid:14d6b384-54d2-9254-35b3-530717f6bc9a\"},\"Action\":{\"@xmlnsprefix\":\"wsa\",\"$\":\"urn:hl7-org:v3:PRPA_IN201301UV02\"}},\"Body\":{\"@xmlnsprefix\":\"soapenv\",\"PRPA_IN201301UV02\":{\"@xmlnsprefix\":\"cda\",\"@xmlns:cda\":\"urn:hl7-org:v3\",\"@ITSVersion\":\"XML_1.0\"}}}}";
    private static final String JSON12 = "{\"Envelope\":{\"@xmlnsprefix\":\"env\",\"@xmlns:env\":\"soap\",\"Body\":{\"@xmlnsprefix\":\"env\",\"Test\":{\"@value\":\"abc\",\"ValueWithoutAttr\":123,\"ValueWithAttr\":{\"@attr\":\"test\",\"$\":123}}}}}";
    private static final String JSON13 = "{\"Root\":{\"@xmlns:ns1\":\"ns1\",\"@xmlns:ns2\":\"ns2\",\"Child\":[{\"@xmlnsprefix\":\"ns1\",\"@value\":[{\"@xmlnsprefix\":\"ns1\",\"$\":\"val1\"},{\"@xmlnsprefix\":\"ns2\",\"$\":\"val2\"}]},{\"@xmlnsprefix\":\"ns2\",\"$\":\"test\"},\"test2\"]}}";
    private static final String JSON14 = "{\"Book\":{\"@xmlns\":\"http://www.library.com\",\"@xmlns:pfx\":\"http://www.library.com\",\"@isbn\":\"1234\",\"@cover\":{\"@xmlnsprefix\":\"pfx\",\"$\":\"hard\"},\"Title\":\"Sherlock Holmes\",\"Author\":\"Arthur Conan Doyle\"}}";

    private static final String XML_FILE_INPUT_1 = "test-json-xml-util-input01.xml";
    private static final String XML_FILE_INPUT_2 = "test-json-xml-util-input02.xml";

    private static final String JSON_FILE_OUTPUT_1 = "test-json-xml-util-output01.json";
    private static final String XML_FILE_OUTPUT_2 = "test-json-xml-util-output02.xml";
    private static final String JSON_FILE_OUTPUT_3 = "test-json-xml-util-output03.json";

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
    public void testXmlToJson11() throws Exception {
        // Stripping bound prefixes on. Prefixed node declares namespace for its prefix and the default namespace.
        assertEquals(JSON9, XmlUtil.toJson(XML7, true));
    }

    @Test
    public void testXmlToJson12() throws Exception {
        // Stripping bound prefixes on. More complex nesting of namespaces and prefixes.
        assertEquals(JSON10, XmlUtil.toJson(XML8, true));
    }

    @Test
    public void testXmlToJson13() throws Exception {
        // Stripping bound prefixes on. More complex nesting of namespaces and prefixes.
        assertEquals(JSON11, XmlUtil.toJson(XML9, true));
    }

    @Test
    public void testXmlToJson14() throws Exception {
        String xml = readFile(XML_FILE_INPUT_1);
        String json = readFile(JSON_FILE_OUTPUT_1);
        assertEquals(json, XmlUtil.toJson(xml, true));
    }

    @Test
    public void testXmlToJson15() throws Exception {
        assertEquals(JSON12, XmlUtil.toJson(XML13, true));
    }

    @Test
    public void testXmlToJson16() throws Exception {
        assertEquals(JSON13, XmlUtil.toJson(XML14, true));
    }

    @Test
    public void testXmlToJson17() throws Exception {
        assertEquals(JSON14, XmlUtil.toJson(XML15, true));
    }

    @Test
    public void testXmlToJson19() throws Exception {
        // Tests converting a SOAP envelope that has a xml:lang attribute in it.
        String xml = readFile(XML_FILE_INPUT_2);
        String json = readFile(JSON_FILE_OUTPUT_3);
        assertEquals(json, XmlUtil.toJson(xml, true));
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
    public void testJsonToXml4() throws Exception {
        assertEquals(XML13, JsonUtil.toXml(JSON12, false, false));
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

    @Test
    public void testXmlToJsonToXml3() throws Exception {
        String xml = readFile(XML_FILE_INPUT_1);
        String xmlToJson = XmlUtil.toJson(xml, true);
        String expectedXml = readFile(XML_FILE_OUTPUT_2);
        assertEquals(expectedXml, JsonUtil.toXml(xmlToJson, false, false));
    }

    private static String readFile(String filename) throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("tests/" + filename))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            return sb.toString().trim();
        }
    }
}
