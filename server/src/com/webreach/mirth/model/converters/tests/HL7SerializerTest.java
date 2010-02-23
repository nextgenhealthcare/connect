/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.converters.tests;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.model.converters.ER7Serializer;

public class HL7SerializerTest {
    public static final String ER7_TEST_FILE = "er7test.hl7";
    public static final String XML_TEST_FILE = "xmlTest.xml";
    private String er7Message;
    private String xmlMessage;
    private Properties properties;
    
    @Before
    public void setUp() throws Exception {
        er7Message = FileUtils.readFileToString(new File(ER7_TEST_FILE));
        xmlMessage = FileUtils.readFileToString(new File(XML_TEST_FILE));

        Properties properties = new Properties();
        properties.put("useStrictParser", "false");
        properties.put("handleRepetitions", "true");
        properties.put("convertLFtoCR", "false");
    }

    @Test
    public void testToXml() throws Exception {
        ER7Serializer serializer = new ER7Serializer(properties);
        String result = serializer.toXML(er7Message);
        DocumentSerializer docSerializer = new DocumentSerializer();
        String prettyResult = docSerializer.toXML(docSerializer.fromXML(result));
        Assert.assertEquals(xmlMessage, prettyResult);
    }
    
    @Test
    public void testFromXml() throws Exception {
        ER7Serializer serializer = new ER7Serializer(properties);
        Assert.assertEquals(er7Message, serializer.fromXML(xmlMessage));
    }
}
