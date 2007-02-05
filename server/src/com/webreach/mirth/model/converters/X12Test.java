package com.webreach.mirth.model.converters;

public class X12Test {
	public static void main(String[] args){
		try {
			System.out.println(new X12Serializer().toXML("SEG*1*2**4*5"));
		} catch (SerializerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
