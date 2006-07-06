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


/**
 * Encodes an HL7 message String in XML. 
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 */
package com.webreach.mirth.server.mule.transformers;

import org.apache.log4j.Logger;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class ER7toXML extends AbstractTransformer {
	private Logger logger = Logger.getLogger(this.getClass());

	public ER7toXML() {
		super();
		registerSourceType(String.class);
		setReturnClass(String.class);
	}

	/**
	 * Encodes an HL7 message String in XML.
	 * 
	 * @return the HL7 message encoded in XML
	 * @throws TransformerException
	 *             if the message could not be parsed and transformed
	 */
	public Object doTransform(Object source) throws TransformerException {
		String message = (String) source;

		PipeParser pipeParser = new PipeParser();
		// disables all message validation
		pipeParser.setValidationContext(new NoValidation());
		XMLParser xmlParser = new DefaultXMLParser();
		
		try {
			logger.debug("encoding HL7 message to XML:\n" + message);
			return xmlParser.encode(pipeParser.parse(message));
		} catch (HL7Exception e) {
			throw new TransformerException(org.mule.config.i18n.Message.createStaticMessage("Failed to parse String at: " + e.getSegmentName()), this);
		}
	}
}
