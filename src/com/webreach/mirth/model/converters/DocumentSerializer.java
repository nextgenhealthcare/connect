package com.webreach.mirth.model.converters;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

public class DocumentSerializer {
	private String[] cDataElements = null;
	
	public DocumentSerializer() {
		
	}
	
	public DocumentSerializer(String[] cDataElements) {
		this.cDataElements = cDataElements;
	}
	
	public String toXML(Document source) {
		OutputFormat of = new OutputFormat(source);

		if (cDataElements != null) {
			of.setCDataElements(cDataElements);
		}

		of.setOmitXMLDeclaration(false);
		of.setIndenting(true);
		of.setLineSeparator("\n");

		StringWriter stringWriter = new StringWriter();
		XMLSerializer serializer = new XMLSerializer(stringWriter, of);
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(source);
			os.write(stringWriter.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return os.toString();
	}

	public Document fromXML(String source) {
		Document document = null;
		
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return document;
	}
}