package com.webreach.mirth.model.bind;

import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Transport;

public class TransportMarshaller {
	private Logger logger = Logger.getLogger(TransportMarshaller.class);
	
	public TransportMarshaller() {}
	
	public void marshal(Transport transport, OutputStream os) throws MarshalException {
		logger.debug("marshaling transport: " + transport.getName());
		
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			
			Element transportElement = document.createElement("transport");
			transportElement.setAttribute("name", transport.getName());
			transportElement.setAttribute("displayName", transport.getDisplayName());
			transportElement.setAttribute("className", transport.getProtocol());
			transportElement.setAttribute("protocol", transport.getProtocol());
			transportElement.setAttribute("transformers", transport.getTransformers());
			
			document.appendChild(transportElement);
			
			os.write(serialize(document).getBytes());
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
	
	private String serialize(Document document) {
		OutputFormat of = new OutputFormat(document);
		of.setOmitXMLDeclaration(true);
		of.setIndenting(true);
		of.setLineSeparator("\n");

		StringWriter stringWriter = new StringWriter();
		XMLSerializer serializer = new XMLSerializer(stringWriter, of);

		try {
			serializer.serialize(document);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return stringWriter.toString();
	}
}
