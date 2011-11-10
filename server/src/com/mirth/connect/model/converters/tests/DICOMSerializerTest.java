package com.mirth.connect.model.converters.tests;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.util.Base64;
import org.junit.Test;

import com.mirth.connect.model.converters.DICOMSerializer;

public class DICOMSerializerTest {
    @Test
    public void testToXml() throws Exception {
        String input = Base64.encodeBase64String(FileUtils.readFileToByteArray(new File("tests/test-dicom-input.dcm")));
        String output = FileUtils.readFileToString(new File("tests/test-dicom-output.xml"));
        DICOMSerializer serializer = new DICOMSerializer();
        Assert.assertEquals(output, TestUtil.prettyPrintXml(serializer.toXML(input)));
    }
}
