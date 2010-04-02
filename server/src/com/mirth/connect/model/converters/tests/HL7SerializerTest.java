/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters.tests;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.model.converters.ER7Serializer;

public class HL7SerializerTest {
    public static final String ER7_TEST_FILE = "er7test.hl7";
    public static final String XML_TEST_FILE = "xmlTest.xml";
    private String expectedEr7Message;
    private String expectedXmlMessage;
    private Properties properties = null;
    
    @Before
    public void setUp() throws Exception {
        expectedEr7Message = FileUtils.readFileToString(new File(ER7_TEST_FILE));
        expectedXmlMessage = FileUtils.readFileToString(new File(XML_TEST_FILE));

        properties = new Properties();
        properties.put("useStrictParser", "false");
        properties.put("handleRepetitions", "false");
        properties.put("handleSubcomponents", "true");
        properties.put("convertLFtoCR", "false");
    }

    @Test
    public void testToXml() throws Exception {
        ER7Serializer serializer = new ER7Serializer(properties);
        String result = serializer.toXML(expectedEr7Message);
        DocumentSerializer docSerializer = new DocumentSerializer();
        String actualXmlMessage = docSerializer.toXML(docSerializer.fromXML(result));
        Assert.assertEquals(expectedXmlMessage, actualXmlMessage);
    }
    
    @Test
    public void testFromXml() throws Exception {
        ER7Serializer serializer = new ER7Serializer(properties);
        String actualEr7Message = serializer.fromXML(expectedXmlMessage);
        Assert.assertEquals(expectedEr7Message, actualEr7Message);
    }
}
