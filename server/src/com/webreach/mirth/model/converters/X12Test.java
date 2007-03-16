package com.webreach.mirth.model.converters;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class X12Test {
	public static void main(String[] args) {
		String testMessage = "";
		try {
			testMessage = new String(getBytesFromFile(new File(args[0])));
			System.out.println(testMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}try {

			EDIReader ediReader = new EDIReader("~", "*", ":");
			StringWriter stringWriter = new StringWriter();
			XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				ediReader.setContentHandler(serializer);
				ediReader.parse(new InputSource(new StringReader(testMessage)));
				os.write(stringWriter.toString().getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
			String xmloutput = os.toString();

			//System.out.println(xmloutput);
			DocumentSerializer docser = new DocumentSerializer();
			docser.setPreserveSpace(false);
			Document doc = docser.fromXML(xmloutput);
			
			System.out.println(docser.toXML(doc)); //handler.getOutput());
			
			XMLReader xr = XMLReaderFactory.createXMLReader();
			EDIXMLHandler handler = new EDIXMLHandler("~", "*", ":");
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);
			xr.parse(new InputSource(new StringReader(xmloutput)));
			System.out.println(handler.getOutput());
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
