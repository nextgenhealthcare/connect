/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.converters;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class HAPIMessageSerializer {
	private PipeParser pipeParser;

	public HAPIMessageSerializer() {
		pipeParser = new PipeParser();
		pipeParser.setValidationContext(new NoValidation());
	}

	public String serialize(Message source) throws SerializerException {
		try {
			return pipeParser.encode(source);
		} catch (HL7Exception e) {
			throw new SerializerException(e);
		}
	}

	public Message deserialize(String source) throws SerializerException {
		try {
			return pipeParser.parse(source);
		} catch (Exception e) {
			throw new SerializerException(e);
		}
	}
}
