package com.webreach.mirth.model.converters;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Aug 6, 2007
 * Time: 2:00:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMTest {
	public static void main(String[] args) {
		String testMessage = "";
        ArrayList<String> testFiles = new ArrayList<String>();
//        testFiles.add("C:\\abdominal.dcm");
        testFiles.add("C:\\ankle.dcm");
//        testFiles.add("C:\\brain.dcm");

        Iterator iterator = testFiles.iterator();
        while(iterator.hasNext()){
            String fileName = (String) iterator.next();
            try {
                testMessage = new String(getBytesFromFile(new File(fileName)));
                System.out.println("Processing test file:" + fileName);
                //System.out.println(testMessage);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {

                long totalExecutionTime = 0;
                int iterations = 1;
                for (int i = 0; i < iterations; i++) {
                    totalExecutionTime+=runTest(testMessage);
                }

                //System.out.println("Execution time average: " + totalExecutionTime/iterations + " ms");
            }
            // System.out.println(new X12Serializer().toXML("SEG*1*2**4*5"));
            catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

	private static long runTest(String testMessage) throws SerializerException, SAXException, IOException {
		Stopwatch stopwatch = new Stopwatch();
		Properties properties = new Properties();
        properties.put("includePixelData","no");
        stopwatch.start();
		DICOMSerializer serializer = new DICOMSerializer(properties);
		String xmloutput = serializer.toXML(testMessage);
        //System.out.println(xmloutput);
		DocumentSerializer docser = new DocumentSerializer();
		docser.setPreserveSpace(true);

        Document doc = docser.fromXML(xmloutput);
//		XMLReader xr = XMLReaderFactory.createXMLReader();
        String results = serializer.fromXML(xmloutput);
        String xmloutput2 = serializer.toXML(results);
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
