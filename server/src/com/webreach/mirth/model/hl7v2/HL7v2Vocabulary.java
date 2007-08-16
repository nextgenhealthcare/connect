package com.webreach.mirth.model.hl7v2;

import java.util.HashMap;
import java.util.Map;

import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.util.MessageVocabulary;

public class HL7v2Vocabulary extends MessageVocabulary {
	Map<String, String> vocab = new HashMap<String, String>();
	private HL7Reference reference = null;
	private String version;
	private String type;
	public HL7v2Vocabulary(String version, String type){
		super(version, type);
		this.version = version;
		this.type = type;
		reference = HL7Reference.getInstance();
	}

	// For now we are going to use the large hashmap
	// TODO: 1.4.1 - Use hl7 model XML from JAXB to generate vocab in real-time
	public String getDescription(String elementId) {
		return reference.getDescription(elementId, version);
	}
	private void loadData(){
		
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.HL7V2;
	}
}
