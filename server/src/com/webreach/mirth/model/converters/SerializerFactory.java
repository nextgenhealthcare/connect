package com.webreach.mirth.model.converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.webreach.mirth.model.MessageObject.Protocol;


public class SerializerFactory {
	public static HashMap<String, IXMLSerializer<String>> serializerCache = new HashMap<String, IXMLSerializer<String>>();
	
	public static void rebuildSerializerCache(){
		serializerCache = new HashMap<String, IXMLSerializer<String>>();
	}
	public static String getSerializerHash(Protocol protocol, Map properties){
		StringBuilder hashBuilder = new StringBuilder();
		hashBuilder.append(protocol.toString());
		if (properties != null){
			List<String> propertyList= new ArrayList<String>();
			
			for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
				propertyList.add((String)iter.next());
			}
			Collections.sort(propertyList);
			for (Iterator iter = propertyList.iterator(); iter.hasNext();) {
				hashBuilder.append(properties.get(iter.next().toString()));
			}
		}
		return hashBuilder.toString();
	}
	public static IXMLSerializer<String> getSerializer(Protocol protocol, Map properties) {
		String hash = getSerializerHash(protocol, properties);
		if (serializerCache.containsKey(hash)){
			return serializerCache.get(hash);
		}else{
			IXMLSerializer<String> serializer;
			if (protocol.equals(Protocol.HL7V2)) {
				serializer = new ER7Serializer(properties);
			} else if (protocol.equals(Protocol.HL7V3)) {
				serializer = new HL7V3Serializer();
			} else if (protocol.equals(Protocol.X12)){
				serializer = new X12Serializer(properties);
			} else if (protocol.equals(Protocol.EDI)){
				serializer = new EDISerializer(properties);
			}else {
				serializer =  new DefaultXMLSerializer();
			}
			serializerCache.put(hash, serializer);
			return serializer;
		}
	}
	public static IXMLSerializer<String> getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions) {
		Properties properties = new Properties();
		properties.put("useStrictParser", Boolean.toString(useStrictParser));
		properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
		properties.put("handleRepetitions", Boolean.toString(handleRepetitions));
		return getSerializer(Protocol.HL7V2, properties);
	}
	public static IXMLSerializer<String> getHL7Serializer(boolean useStrictParser, boolean useStrictValidation) {
		Properties properties = new Properties();
		properties.put("useStrictParser", Boolean.toString(useStrictParser));
		properties.put("useStrictValidation", Boolean.toString(useStrictValidation));
		properties.put("handleRepetitions", false);
		return getSerializer(Protocol.HL7V2, properties);
	}
	public static IXMLSerializer<String> getHL7Serializer() {
		Properties properties = new Properties();
		properties.put("useStrictParser", true);
		properties.put("useStrictValidation", false);
		properties.put("handleRepetitions", false);
		return getSerializer(Protocol.HL7V2, properties);
	}
	public static IXMLSerializer<String> getX12Serializer(boolean inferDelimiters) {
		Properties properties = new Properties();
		properties.put("inferDelimiters", Boolean.toString(inferDelimiters));
		return getSerializer(Protocol.X12, properties);
	}
	public static IXMLSerializer<String> getEDISerializer(String segmentDelim, String elementDelim, String subelementDelim) {
		Properties properties = new Properties();
		properties.put("segmentDelimiter", segmentDelim);
		properties.put("elementDelimiter", elementDelim);
		properties.put("subelementDelimiter", subelementDelim);
		return getSerializer(Protocol.EDI, properties);
	}
}
