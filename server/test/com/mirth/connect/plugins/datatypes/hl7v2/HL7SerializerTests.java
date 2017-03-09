/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.mirth.connect.model.converters.TestUtil;

public class HL7SerializerTests {
    private HL7v2DataTypeProperties defaultProperties;

    @Before
    public void setUp() throws Exception {
        defaultProperties = new HL7v2DataTypeProperties();
        
//        defaultProperties = new Properties();
//        defaultProperties.put("useStrictParser", "false");
//        defaultProperties.put("handleRepetitions", "false");
//        defaultProperties.put("handleSubcomponents", "false");
//        defaultProperties.put("inputSegmentDelimiter", "\\r\\n|\\r|\\n");
//        defaultProperties.put("outputSegmentDelimiter", "\\r");
    }

    @Test
    public void testToXmlDefault() throws Exception {
        String input = FileUtils.readFileToString(new File("tests/test-hl7-input.txt"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-output.xml"));
        ER7Serializer serializer = new ER7Serializer(defaultProperties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.prettyPrintXml(serializer.toXML(input)));
    }

    @Test
    public void testFromXmlDefault() throws Exception {
        String input = FileUtils.readFileToString(new File("tests/test-hl7-output.xml"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-input.txt"));
        ER7Serializer serializer = new ER7Serializer(defaultProperties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.convertCRToCRLF(serializer.fromXML(input)));
    }
    
    @Test
    public void testToXmlWhitepsace() throws Exception {
        String input = FileUtils.readFileToString(new File("tests/test-hl7-whitespace-input.txt"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-whitespace-output.xml"));
        ER7Serializer serializer = new ER7Serializer(defaultProperties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.prettyPrintXml(serializer.toXML(input)));
    }

    @Test
    public void testFromXmlWhitespace() throws Exception {
        String input = FileUtils.readFileToString(new File("tests/test-hl7-whitespace-output.xml"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-whitespace-input.txt"));
        ER7Serializer serializer = new ER7Serializer(defaultProperties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.convertCRToCRLF(serializer.fromXML(input)));
    }
    
    @Test
    public void testFromXmlMissingFields() throws Exception {
        String input = FileUtils.readFileToString(new File("tests/test-hl7-missing-fields-input.xml"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-missing-fields-output.txt"));
        ER7Serializer serializer = new ER7Serializer(defaultProperties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.convertCRToCRLF(serializer.fromXML(input)));
        
        input = FileUtils.readFileToString(new File("tests/test-hl7-1636-input.xml"));
        output = FileUtils.readFileToString(new File("tests/test-hl7-1636-output.txt"));
        serializer = new ER7Serializer(defaultProperties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.convertCRToCRLF(serializer.fromXML(input)));
    }
    
    @Test
    public void testFromXmlMissingComponents() throws Exception {
        String input = FileUtils.readFileToString(new File("tests/test-hl7-missing-components-input.xml"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-missing-components-output.txt"));
        ER7Serializer serializer = new ER7Serializer(defaultProperties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.convertCRToCRLF(serializer.fromXML(input)));
    }
    
    @Test
    public void testFromXmlMissingSubcomponents() throws Exception {
        String input = FileUtils.readFileToString(new File("tests/test-hl7-missing-subcomponents-input.xml"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-missing-subcomponents-output.txt"));
        ER7Serializer serializer = new ER7Serializer(defaultProperties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.convertCRToCRLF(serializer.fromXML(input)));
    }
    
    @Test
    public void testFromXmlSingleSegment() throws Exception {
        HL7v2DataTypeProperties properties = new HL7v2DataTypeProperties();
//        properties.put("useStrictParser", "false");
//        properties.put("handleRepetitions", "false");
//        properties.put("handleSubcomponents", "true");
//        properties.put("inputSegmentDelimiter", "\\r\\n|\\r|\\n");
//        properties.put("outputSegmentDelimiter", "\\r");
        
        String input = FileUtils.readFileToString(new File("tests/test-hl7-single-segment-input.xml"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-single-segment-output.txt"));
        ER7Serializer serializer = new ER7Serializer(properties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.convertCRToCRLF(serializer.fromXML(input)));
    }
    
    @Test
    public void testFromXmlSingleField() throws Exception {
        String input = FileUtils.readFileToString(new File("tests/test-hl7-single-field-input.xml"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-single-field-output.txt"));
        ER7Serializer serializer = new ER7Serializer(defaultProperties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.convertCRToCRLF(serializer.fromXML(input)));
    }

    @Test
    public void testToXmlWithSubcomponents() throws Exception {
        HL7v2DataTypeProperties properties = new HL7v2DataTypeProperties();
//        Properties properties = new Properties();
//        properties.put("useStrictParser", "false");
//        properties.put("handleRepetitions", "false");
//        properties.put("handleSubcomponents", "true");
//        properties.put("inputSegmentDelimiter", "\\r\\n|\\r|\\n");
//        properties.put("outputSegmentDelimiter", "\\r");

        String input = FileUtils.readFileToString(new File("tests/test-hl7-subcomponents-input.txt"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-subcomponents-output.xml"));
        ER7Serializer serializer = new ER7Serializer(properties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.prettyPrintXml(serializer.toXML(input)));
    }

    @Test
    public void testFromXmlWithSubcomponents() throws Exception {
        HL7v2DataTypeProperties properties = new HL7v2DataTypeProperties();
//        Properties properties = new Properties();
//        properties.put("useStrictParser", "false");
//        properties.put("handleRepetitions", "false");
//        properties.put("handleSubcomponents", "true");
//        properties.put("inputSegmentDelimiter", "\\r\\n|\\r|\\n");
//        properties.put("outputSegmentDelimiter", "\\r");

        String input = FileUtils.readFileToString(new File("tests/test-hl7-subcomponents-output.xml"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-subcomponents-input.txt"));
        ER7Serializer serializer = new ER7Serializer(properties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.convertCRToCRLF(serializer.fromXML(input)));
    }

    @Test
    public void testToXmlWithRepetitions() throws Exception {
        HL7v2DataTypeProperties properties = new HL7v2DataTypeProperties();
//        Properties properties = new Properties();
//        properties.put("useStrictParser", "false");
//        properties.put("handleRepetitions", "true");
//        properties.put("handleSubcomponents", "false");
//        properties.put("inputSegmentDelimiter", "\\r\\n|\\r|\\n");
//        properties.put("outputSegmentDelimiter", "\\r");

        String input = FileUtils.readFileToString(new File("tests/test-hl7-repetitions-input.txt"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-repetitions-output.xml"));
        ER7Serializer serializer = new ER7Serializer(properties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.prettyPrintXml(serializer.toXML(input)));
    }
    
    @Test
    public void testFromXmlWithRepetitions() throws Exception {
        HL7v2DataTypeProperties properties = new HL7v2DataTypeProperties();
//        Properties properties = new Properties();
//        properties.put("useStrictParser", "false");
//        properties.put("handleRepetitions", "true");
//        properties.put("handleSubcomponents", "false");
//        properties.put("inputSegmentDelimiter", "\\r\\n|\\r|\\n");
//        properties.put("outputSegmentDelimiter", "\\r");

        String input = FileUtils.readFileToString(new File("tests/test-hl7-repetitions-output.xml"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-repetitions-input.txt"));
        ER7Serializer serializer = new ER7Serializer(properties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.convertCRToCRLF(serializer.fromXML(input)));
    }
    
    @Test
    public void testToXmlWithBatch() throws Exception {
        String input = FileUtils.readFileToString(new File("tests/test-hl7-batch-input.txt"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-batch-output.xml"));
        ER7Serializer serializer = new ER7Serializer(defaultProperties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.prettyPrintXml(serializer.toXML(input)));
    }
    
    @Test
    public void testFromXmlWithBatch() throws Exception {
        String input = FileUtils.readFileToString(new File("tests/test-hl7-batch-output.xml"));
        String output = FileUtils.readFileToString(new File("tests/test-hl7-batch-input.txt"));
        ER7Serializer serializer = new ER7Serializer(defaultProperties.getSerializerProperties());
        Assert.assertEquals(output, TestUtil.convertCRToCRLF(serializer.fromXML(input)));
    }
}
