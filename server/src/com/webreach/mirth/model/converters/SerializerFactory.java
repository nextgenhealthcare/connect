package com.webreach.mirth.model.converters;

import java.util.Properties;

import com.webreach.mirth.model.MessageObject.Protocol;


public class SerializerFactory {
	public static IXMLSerializer<String> getSerializer(Protocol protocol, Properties properties) {
		if (protocol.equals(Protocol.HL7V2)) {
			return new ER7Serializer(properties);
		} else if (protocol.equals(Protocol.HL7V3)) {
			return new DefaultXMLSerializer();
		} else if (protocol.equals(Protocol.X12)){
			return new X12Serializer(properties);
		} else if (protocol.equals(Protocol.EDI)){
			return new EDISerializer(properties);
		}else {
			return new DefaultXMLSerializer();
		}
	}
}
