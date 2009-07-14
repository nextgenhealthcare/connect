package com.webreach.mirth.model.hl7v2;

import java.util.HashMap;
import java.util.Map;

import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.util.MessageVocabulary;

public class HL7v2Vocabulary extends MessageVocabulary {
	Map<String, String> vocab = new HashMap<String, String>();
	private String version;
	private String type;

	public HL7v2Vocabulary(String version, String type) {
		super(version, type);
		this.version = version.replaceAll("\\.", "");
		this.type = type;
	}

	// For now we are going to use the large hashmap
	// TODO: 1.4.1 - Use hl7 model XML from JAXB to generate vocab in real-time
	public String getDescription(String elementId) {
		try {
			if (elementId.indexOf('.') < 0) {
				if (elementId.length() < 4) {
					// we have a segment
					return Component.getSegmentDescription(version, elementId);
				} else {
					// We have a message (ADTA01)
					return Component.getMessageDescription(version, elementId);
				}

			} else {
				String[] parts = elementId.split("\\.");
				if (parts.length == 3) {
					// we have a complete node, PID.5.1
					return Component.getCompositeFieldDescriptionWithSegment(version, elementId, false);
				} else if (parts.length == 2) {
					// coule either be a segment or composite
					// PID.5 or XPN.1
					// Try segment first then composite
					String description = "";
					try {
						description = Component.getSegmentFieldDescription(version, elementId, false);
					} catch (Exception e) {
						description = Component.getCompositeFieldDescription(version, elementId, false);
					}
					return description;

				} else {
					return "";
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			return "";
		}
		// return reference.getDescription(elementId, version);
	}

	private void loadData() {

	}

	@Override
	public Protocol getProtocol() {
		return Protocol.HL7V2;
	}
}
