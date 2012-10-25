package com.mirth.connect.model.converters.tests;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.util.Base64;
import org.junit.Test;

import com.mirth.connect.model.converters.DICOMSerializer;

public class DICOMSerializerTest {
    @Test
    public void testToXml1() throws Exception {
        String input = Base64.encodeBase64String(FileUtils.readFileToByteArray(new File("tests/test-dicom-input-1.dcm")));
        String output = FileUtils.readFileToString(new File("tests/test-dicom-output-1.xml"));
        DICOMSerializer serializer = new DICOMSerializer();
        Assert.assertEquals(output, TestUtil.prettyPrintXml(serializer.toXML(input)));
    }

    @Test
    public void testToXml2() throws Exception {
        String input = Base64.encodeBase64String(FileUtils.readFileToByteArray(new File("tests/test-dicom-input-2.dcm")));
        String output = FileUtils.readFileToString(new File("tests/test-dicom-output-2.xml"));
        DICOMSerializer serializer = new DICOMSerializer();
        Assert.assertEquals(output, TestUtil.prettyPrintXml(serializer.toXML(input)));
    }

    @Test
    public void testToXml3() throws Exception {
        String input = Base64.encodeBase64String(FileUtils.readFileToByteArray(new File("tests/test-dicom-input-3.dcm")));
        String output = FileUtils.readFileToString(new File("tests/test-dicom-output-3.xml"));
        DICOMSerializer serializer = new DICOMSerializer();
        Assert.assertEquals(output, TestUtil.prettyPrintXml(serializer.toXML(input)));
    }
}