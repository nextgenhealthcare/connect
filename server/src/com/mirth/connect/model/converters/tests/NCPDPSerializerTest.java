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

import com.mirth.connect.model.converters.NCPDPSerializer;

public class NCPDPSerializerTest {
    private Properties defaultProperties;
    
    @Before
    public void setUp() throws Exception {
        defaultProperties = new Properties();
        defaultProperties.put("segmentDelimiter", "0x1E");
        defaultProperties.put("groupDelimiter", "0x1D");
        defaultProperties.put("fieldDelimiter", "0x1C");
        defaultProperties.put("useStrictValidation", "false");
    }

    @Test
    public void testDefaultToXml() throws Exception {
        String input = FileUtils.readFileToString(new File("tests/test-ncpdp51-input.txt"));
        String output = FileUtils.readFileToString(new File("tests/test-ncpdp51-output.xml"));
        NCPDPSerializer serializer = new NCPDPSerializer(defaultProperties);
        Assert.assertEquals(output, TestUtil.prettyPrintXml(serializer.toXML(input)));
    }

    @Test
    public void testDefaultFromXml() throws Exception {
        String input = FileUtils.readFileToString(new File("tests/test-ncpdp51-output.xml"));
        String output = FileUtils.readFileToString(new File("tests/test-ncpdp51-input.txt"));
        NCPDPSerializer serializer = new NCPDPSerializer(defaultProperties);
        Assert.assertEquals(output, serializer.fromXML(input));
    }
}
