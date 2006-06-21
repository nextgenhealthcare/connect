package com.webreach.mirth.model.converters;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class ObjectSerializer {
	private XStream xstream;
	
	public ObjectSerializer() {
		xstream = new XStream(new XppDriver());
		xstream.setMode(XStream.NO_REFERENCES);
	}
	
	public String toXML(Object source) {
		return xstream.toXML(source);
	}
	
	public Object fromXML(String source) {
		return xstream.fromXML(source);
	}
}