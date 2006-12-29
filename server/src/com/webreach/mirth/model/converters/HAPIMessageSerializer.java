/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


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
