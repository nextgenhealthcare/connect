package com.webreach.mirth.model.converters;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;

import org.apache.commons.lang.NotImplementedException;
import org.milyn.edisax.EDIParser;
import org.milyn.io.StreamUtils;
import org.xml.sax.InputSource;

public class X12Serializer implements IXMLSerializer {
	//private 
	public Object fromXML(String source) throws SerializerException {
		throw new NotImplementedException();
	}

	public String toXML(Object source) throws SerializerException {
		try {
			
			InputSource s = new InputSource(new StringReader((String)source)); 
			InputStream mapping = new ByteArrayInputStream(StreamUtils.readStream(getClass().getResourceAsStream("edi-to-xml-mapping.xml")));
			X12ContentHandler contentHandler = new X12ContentHandler();
			EDIParser parser = null;
			parser = new EDIParser();
			parser.setContentHandler(contentHandler);
			parser.setMappingModel(EDIParser.parseMappingModel(mapping));
			parser.parse(s);
			return contentHandler.xmlMapping.toString();
		} catch (Exception e) {
			String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
			System.out.println(exceptionMessage);
		}
		return new String();
	}

}
