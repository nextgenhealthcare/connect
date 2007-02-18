package com.webreach.mirth.model.converters;

import java.util.Map;
import java.util.Properties;

import com.webreach.mirth.model.MessageObject.Protocol;

public class SerializerFactory {
	public static IXMLSerializer<String> getSerializer(Protocol protocol, Map properties) {
		if (protocol.equals(Protocol.HL7V2)) {
			return new ER7Serializer();
		} else if (protocol.equals(Protocol.EDI) || protocol.equals(Protocol.X12)) {
			return new EDISerializer(properties);
		} else {
			return new DefaultXMLSerializer();
		}
	}
}
