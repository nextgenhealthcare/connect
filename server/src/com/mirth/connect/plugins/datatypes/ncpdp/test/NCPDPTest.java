/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.ncpdp.test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.model.converters.tests.Stopwatch;
import com.mirth.connect.plugins.datatypes.ncpdp.NCPDPSerializer;
import com.mirth.connect.plugins.datatypes.ncpdp.NCPDPXMLHandler;

public class NCPDPTest {
	public static void main(String[] args) throws Exception {
		String testMessage = "";
        ArrayList<String> testFiles = new ArrayList<String>();
        testFiles.add("C:\\NCPDP_51_B1_Request.txt");
        testFiles.add("C:\\NCPDP_51_B1_Request_v2.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v2.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v3.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v4.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v5.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v6.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v7.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v8.txt");
        testFiles.add("C:\\NCPDP_51_B1_Response_v9.txt");
        testFiles.add("C:\\NCPDP_51_B2_Request.txt");
        testFiles.add("C:\\NCPDP_51_B2_Request_v2.txt");
        testFiles.add("C:\\NCPDP_51_B2_Response.txt");
        testFiles.add("C:\\NCPDP_51_B2_Response_v2.txt");
        testFiles.add("C:\\NCPDP_51_B2_Response_v3.txt");
        testFiles.add("C:\\NCPDP_51_B3_Request.txt");
        testFiles.add("C:\\NCPDP_51_B3_Response.txt");
        testFiles.add("C:\\NCPDP_51_B3_Response_v2.txt");
        testFiles.add("C:\\NCPDP_51_B3_Response_v3.txt");
        testFiles.add("C:\\NCPDP_51_E1_Request.txt");
        testFiles.add("C:\\NCPDP_51_E1_Response.txt");
        testFiles.add("C:\\NCPDP_51_E1_Response_v2.txt");
        testFiles.add("C:\\NCPDP_51_E1_Response_v3.txt");
        testFiles.add("C:\\NCPDP_51_E1_Response_v4.txt");
        testFiles.add("C:\\NCPDP_51_E1_Response_v5.txt");
        testFiles.add("C:\\NCPDP_51_N1_Request.txt");
        testFiles.add("C:\\NCPDP_51_N2_Request.txt");
        testFiles.add("C:\\NCPDP_51_P1_Request.txt");
        testFiles.add("C:\\NCPDP_51_P1_Response.txt");
        testFiles.add("C:\\NCPDP_51_P1_Response_v2.txt");
        testFiles.add("C:\\NCPDP_51_P1_Response_v3.txt");
        testFiles.add("C:\\NCPDP_51_P2_Request.txt");
        testFiles.add("C:\\NCPDP_51_P2_Response.txt");
        testFiles.add("C:\\NCPDP_51_P3_Request.txt");
        testFiles.add("C:\\NCPDP_51_P3_Response.txt");
        testFiles.add("C:\\NCPDP_51_P3_Response_v2.txt");
        testFiles.add("C:\\NCPDP_51_P4_Request.txt");
        testFiles.add("C:\\NCPDP_51_P4_Response.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_1.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_2.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_3.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_4.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_5.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_6.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_7.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_8.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_9.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_10.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_11.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_12.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_13.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_14.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_15.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_16.txt");
        testFiles.add("C:\\NCPDP_51_CALPOS_17.txt");

        for (String testFile : testFiles){
            testMessage = new String(FileUtils.readFileToByteArray(new File(testFile)));
            System.out.println("Processing test file:" + testFile);
            
            try {
                long totalExecutionTime = 0;
                int iterations = 1;
                for (int i = 0; i < iterations; i++) {
                    totalExecutionTime+=runTest(testMessage);
                }

                //System.out.println("Execution time average: " + totalExecutionTime/iterations + " ms");
            }
            // System.out.println(new X12Serializer().serialize("SEG*1*2**4*5"));
            catch (SAXException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

	private static long runTest(String testMessage) throws XmlSerializerException, SAXException, IOException {
		Stopwatch stopwatch = new Stopwatch();
//		Properties properties = new Properties();
        String SchemaUrl = "/ncpdp51.xsd";
//        properties.put("useStrictParser", "true");
//        properties.put("http://java.sun.com/xml/jaxp/properties/schemaSource",SchemaUrl);
        stopwatch.start();
		NCPDPSerializer serializer = new NCPDPSerializer(null);
		String xmloutput = serializer.toXML(testMessage);
		//System.out.println(xmloutput);
		DocumentSerializer docser = new DocumentSerializer();
		Document doc = docser.fromXML(xmloutput);
		XMLReader xr = XMLReaderFactory.createXMLReader();

        NCPDPXMLHandler handler = new NCPDPXMLHandler("\u001E","\u001D","\u001C", "51");

        xr.setContentHandler(handler);
		xr.setErrorHandler(handler);
        xr.setFeature("http://xml.org/sax/features/validation", true);
        xr.setFeature("http://apache.org/xml/features/validation/schema", true);
        xr.setFeature("http://apache.org/xml/features/validation/schema-full-checking",true);
        xr.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage","http://www.w3.org/2001/XMLSchema");
        xr.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",SchemaUrl);
        xr.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource","/ncpdp51.xsd");
        xr.parse(new InputSource(new StringReader(xmloutput)));
		stopwatch.stop();

		//System.out.println(docser.serialize(doc)); //handler.getOutput());
		//System.out.println(handler.getOutput());
        //System.out.println(xmloutput);
        if (handler.getOutput().toString().replace('\n', '\r').trim().equals(testMessage.replaceAll("\\r\\n", "\r").trim())) {
			System.out.println("Test Successful!");
		} else {
			String original = testMessage.replaceAll("\\r\\n", "\r").trim();
			String newm = handler.getOutput().toString().replace('\n', '\r').trim();
			for (int i = 0; i < original.length(); i++){
				if (original.charAt(i) == newm.charAt(i)){
					System.out.print(newm.charAt(i));
				}else{
					System.out.println("");
					System.out.print("Saw: ");
					System.out.println(newm.charAt(i));
					System.out.print("Expected: ");
					System.out.print(original.charAt(i));
					break;
				}
			}
			System.out.println("Test Failed!");
		}
		return stopwatch.toValue();
	}
}
