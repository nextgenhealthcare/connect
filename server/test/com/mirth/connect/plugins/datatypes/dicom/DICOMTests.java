/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.dicom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xml.sax.SAXException;

import com.mirth.connect.donkey.model.message.MessageSerializerException;
import com.mirth.connect.model.converters.Stopwatch;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Aug 6, 2007
 * Time: 2:00:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMTests {
	public static void main(String[] args) {
		String testMessage = "";
        ArrayList<String> testFiles = new ArrayList<String>();
        testFiles.add("C:\\abdominal.dcm");
        //testFiles.add("C:\\abdominal.dcm");
        //testFiles.add("C:\\brain.dcm");
        //String[] a = new String[1];
        //a[0] = "c:\\ankle.dcm";

        //ImageJ ij = new ImageJ(null,ImageJ.EMBEDDED);
        //ImageJ.main(a);
        
//        Client client = new Client("https://localhost:8443");
//        Attachment a = new Attachment();
//        a.setAttachmentId("TEST1");
//        a.setMessageId("TEST2");
//        a.setSize(100);
//        a.setType("TESTING");
//        try {
//            client.login("admin", "admin", "1.5.0");  
//            a.setData(getBytesFromFile(new File("C:\\abdominal.dcm")));
////            client.insertAttachment(a);
//        
//            Attachment a1 = client.getAttachment("TEST1");
//            List<Attachment> a3 = client.getAttachmentsByMessageId("TEST2");
//            Attachment a2 = a3.get(0);
//            if(a1.equals(a2)){
//                System.out.println("They are the same");
//            }
//            System.out.println("First:" + a1.toString());
//            
//            System.out.println("Second:" + a2.toString());
//                        
//            
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//        }
        
        
        
//       Iterator iterator = testFiles.iterator();
//        while(iterator.hasNext()){
//            String fileName = (String) iterator.next();
//            try {
//                testMessage = new String(getBytesFromFile(new File(fileName)));
//                System.out.println("Processing test file:" + fileName);
//                //System.out.println(testMessage);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//
//                long totalExecutionTime = 0;
//                int iterations = 1;
//                for (int i = 0; i < iterations; i++) {
//                    totalExecutionTime+=runTest(testMessage);
//                }
//
//                //System.out.println("Execution time average: " + totalExecutionTime/iterations + " ms");
//            }
//            // System.out.println(new X12Serializer().toXML("SEG*1*2**4*5"));
//            catch (SAXException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

	private static long runTest(String testMessage) throws MessageSerializerException, SAXException, IOException {
		Stopwatch stopwatch = new Stopwatch();
//		Properties properties = new Properties();
//        properties.put("includePixelData","no");
//        properties.put("isEncoded","no");       
        stopwatch.start();
		DICOMSerializer serializer = new DICOMSerializer(null);
//        String xmloutput = serializer.toXML(testMessage);
        //Dcm2Xml dcm2xml = new Dcm2Xml();
        File xmlOut = File.createTempFile("test","xml");
        File dcmInput = new File("c:\\US-PAL-8-10x-echo.dcm");
        try {
      //      dcm2xml.convert(dcmInput,xmlOut);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        File dcmOutput = File.createTempFile("test","dcm");
        String[] args = new String[4];
        args[0] = "-x";
        args[1] = xmlOut.getAbsolutePath();
        args[2] = "-o";
        args[3] = "c:\\dcmOutput.dcm";
        //Xml2Dcm.main(args);
        // TO XML again
        File input2 = new File("c:\\dcmOutput.dcm");
        try {
   ///         dcm2xml.convert(input2,xmlOut);
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        dcmOutput = File.createTempFile("test","dcm");
        args = new String[4];
        args[0] = "-x";
        args[1] = xmlOut.getAbsolutePath();
        args[2] = "-o";
        args[3] = "c:\\dcmOutput2.dcm";
    //    Xml2Dcm.main(args);        
        //System.out.println(xmloutput);
//		DocumentSerializer docser = new DocumentSerializer();
//		docser.setPreserveSpace(true);
//
//        Document doc = docser.fromXML(xmloutput);
//		XMLReader xr = XMLReaderFactory.createXMLReader();
        String results = ""; //= serializer.fromXML(xmloutput);
//        String xmloutput2 = serializer.toXML(results);
//        String results2 = serializer.fromXML(xmloutput2);
        System.out.println("testing...");
        if (results.replace('\n', '\r').trim().equals(testMessage.replaceAll("\\r\\n", "\r").trim())) {
			System.out.println("Test Successful!");
		} else {
			String original = testMessage.replaceAll("\\r\\n", "\r").trim();
			String newm = results.replace('\n', '\r').trim();
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
	// Returns the contents of the file in a byte array.
	private static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
}
