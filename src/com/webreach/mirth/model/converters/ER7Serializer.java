package com.webreach.mirth.model.converters;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class ER7Serializer implements Serializer<String> {
	private PipeParser pipeParser;
	private XMLParser xmlParser;

	public ER7Serializer() {
		pipeParser = new PipeParser();
		pipeParser.setValidationContext(new NoValidation());
		xmlParser = new DefaultXMLParser();
	}

	/**
	 * Returns an XML-encoded HL7 message given an ER7-enconded HL7 message.
	 * 
	 * @param source an ER7-encoded HL7 message.
	 * @return
	 */
	public String serialize(String source) {
		StringBuilder builder = new StringBuilder();

		try {
			builder.append(xmlParser.encode(pipeParser.parse(source)));
		} catch (HL7Exception e) {
			e.printStackTrace();
		}

		return builder.toString();
	}

	/**
	 * Returns an ER7-encoded HL7 message given an XML-encoded HL7 message.
	 * 
	 * @param source a XML-encoded HL7 message.
	 * @return
	 */
	public String deserialize(String source) {
		StringBuilder builder = new StringBuilder();

		try {
			builder.append(pipeParser.encode(xmlParser.parse(source)));
		} catch (HL7Exception e) {
			e.printStackTrace();
		}

		return builder.toString();
	}
}
