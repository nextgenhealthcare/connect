package com.webreach.mirth.model.bind;

import java.io.OutputStream;
import java.io.StringWriter;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

public class Serializer {
	public Serializer() {}
	
	public void serialize(Document document, String[] dataElements, OutputStream os) {
//		dataElements = { "filter", "variable", "profile" };

		OutputFormat of = new OutputFormat(document);
		
		if (dataElements != null) {
			of.setCDataElements(dataElements);	
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