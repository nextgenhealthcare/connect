package com.webreach.mirth.model.converters;

import java.util.Properties;

import com.webreach.mirth.model.MessageObject.Protocol;


public class SerializerFactory {
	public static IXMLSerializer<String> getSerializer(Protocol protocol, Properties properties) {
		if (protocol.equals(Protocol.HL7V2)) {
			return new ER7Serializer(properties);
		} else if (protocol.equals(Protocol.HL7V3)) {
			return new HL7V3Serializer();
		} else if (protocol.equals(Protocol.X12)){
			return new X12Serializer(properties);
		} else if (protocol.equals(Protocol.EDI)){
			return new EDISerializer(properties);
		}else {
			return new DefaultXMLSerializer();
		}
	}
	public ER7Serializer getHL7Serializer(boolean useStrictParser) {
		Properties properties = new Properties();
		properties.put("useStrictParser", Boolean.toString(useStrictParser));
		return new ER7Serializer(properties);
	}
	public X12Serializer getX12Serializer(boolean inferDelimiters) {
		Properties properties = new Properties();
		properties.put("inferDelimiters", Boolean.toString(inferDelimiters));
		return new X12Serializer(inferDelimiters);
	}
	public EDISerializer getEDISerializer(String segmentDelim, String elementDelim, String subelementDelim) {
		Properties properties = new Properties();
		properties.put("segmentDelimiter", segmentDelim);
		properties.put("elementDelimiter", elementDelim);
		properties.put("subelementDelimiter", subelementDelim);
		return new EDISerializer(properties);
	}
}
