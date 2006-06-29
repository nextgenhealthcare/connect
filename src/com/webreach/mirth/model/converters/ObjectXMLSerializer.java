package com.webreach.mirth.model.converters;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class ObjectXMLSerializer implements Serializer<Object>{
	private XStream xstream;
	
	public ObjectXMLSerializer() {
		xstream = new XStream(new XppDriver());
		xstream.setMode(XStream.NO_REFERENCES);
	}
	
	public String serialize(Object source) {
		return xstream.toXML(source);
	}
	
	public Object deserialize(String source) {
		return xstream.fromXML(source);
	}
}