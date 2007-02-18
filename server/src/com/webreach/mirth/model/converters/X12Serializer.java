package com.webreach.mirth.model.converters;

import java.util.Map;
import java.util.Properties;

public class X12Serializer extends EDISerializer {

	private boolean inferX12Delimiters = false;
	public X12Serializer(Map x12Properties){
		super(x12Properties);
		if(x12Properties.get("inferX12Delimiters") != null && ((String)x12Properties.get("inferX12Delimiters")).equals("true"))
		{
			this.inferX12Delimiters = true;
		}
		else
		{
			this.inferX12Delimiters = false;
		}
	}
	public X12Serializer(boolean inferX12Delimiters){
		super();
		this.inferX12Delimiters = inferX12Delimiters;
	}
	public String toXML(String source) throws SerializerException {
		if (this.inferX12Delimiters){
			String x12message = source;
			if (x12message.startsWith("ISA")){
				super.setElementDelim(x12message.charAt(3) + "");
				super.setSubelementDelim(x12message.charAt(105) + "");
				super.setSegmentDelim(x12message.charAt(106) + "");
			}
		}
		return super.toXML(source);
	}
}
