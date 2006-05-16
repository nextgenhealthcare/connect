package com.webreach.mirth.model.bind;

import java.io.OutputStream;
import java.io.StringWriter;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

public class Serializer {
	/**
	 * Serializes a Document to XML (with the specified CDATA elements) and writes it to an OutputStream.
	 * 
	 * @param document
	 * @param cDataElements
	 * @param os
	 */
	public void serialize(Document document, String[] cDataElements, OutputStream os) {
		OutputFormat of = new OutputFormat(document);
		
		if (cDataElements != null) {
			of.setCDataElements(cDataElements);	
		}
		
		of.setOmitXMLDeclaration(true);
		of.setIndenting(true);
		of.setLineSeparator("\n");

		StringWriter stringWriter = new StringWriter();
		XMLSerializer serializer = new XMLSerializer(stringWriter, of);

		try {
			serializer.serialize(document);
			os.write(stringWriter.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}