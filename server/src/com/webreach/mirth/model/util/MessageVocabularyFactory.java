package com.webreach.mirth.model.util;

import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.hl7v2.HL7v2Vocabulary;
import com.webreach.mirth.model.x12.X12Vocabulary;

public class MessageVocabularyFactory {
	public MessageVocabulary getVocabulary(Protocol protocol, String version, String type) {
		if (protocol.equals(Protocol.HL7V2)) {
			return new HL7v2Vocabulary(version, type);
		} else if (protocol.equals(Protocol.X12)) {
			return new X12Vocabulary(version, type);
		} else {
			return new DefaultVocabulary();
		}
	}
}
