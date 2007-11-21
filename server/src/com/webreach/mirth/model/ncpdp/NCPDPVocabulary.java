package com.webreach.mirth.model.ncpdp;

import com.webreach.mirth.model.util.MessageVocabulary;
import com.webreach.mirth.model.MessageObject.Protocol;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Jun 26, 2007
 * Time: 2:45:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class NCPDPVocabulary extends MessageVocabulary {
	Map<String, String> vocab = new HashMap<String, String>();
	private NCPDPReference reference = null;
	private String version;
	private String type;
	public NCPDPVocabulary(String version, String type){
		super(version, type);
		this.version = version;
		this.type = type;
		reference = NCPDPReference.getInstance();
	}

	// For now we are going to use the large hashmap
	// TODO: 1.4.1 - Use hl7 model XML from JAXB to generate vocab in real-time
	public String getDescription(String elementId) {
		return reference.getDescription(elementId);
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.NCPDP;
	}
}
