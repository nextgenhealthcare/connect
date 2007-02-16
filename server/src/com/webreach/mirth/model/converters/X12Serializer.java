package com.webreach.mirth.model.converters;

public class X12Serializer extends EDISerializer {
	private boolean inferX12Delimiters = false;
	public X12Serializer(String segmentDelim, String elementDelim, String subelementDelim){
		super(segmentDelim, elementDelim, subelementDelim);
	}
	public X12Serializer(boolean inferX12Delimiters){
		super();
		this.inferX12Delimiters = inferX12Delimiters;
	}
	public String toXML(Object source) throws SerializerException {
		if (this.inferX12Delimiters){
			String x12message = (String)source;
			if (x12message.startsWith("ISA")){
				super.setElementDelim(x12message.charAt(3) + "");
				super.setSubelementDelim(x12message.charAt(105) + "");
				super.setSegmentDelim(x12message.charAt(106) + "");
			}
		}
		return super.toXML(source);
	}
}
