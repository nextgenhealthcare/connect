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
package com.webreach.mirth.mule.transformers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;

public class HL7StringToXMLString extends AbstractTransformer {
	private static final long serialVersionUID = 7315050027776022660L;
	
	protected static transient Log logger = LogFactory.getLog(HL7StringToXMLString.class);

	public HL7StringToXMLString() {
		super();
		this.registerSourceType(String.class);
		setReturnClass(String.class);
	}

	/**
	 * Encodes an HL7 message String in XML.
	 * 
	 * @return the HL7 message encoded in XML
	 * @throws TransformerException if the message could not be parsed and transformed
	 */
	public Object doTransform(Object src) throws TransformerException {
		// cast the incomming message Object to a String
		String hl7String = (String) src;
		// this is needed if messages are from an HTTP POST
		hl7String = hl7String.replace('\n', '\r');
		// create a new HL7 parser
		PipeParser pipeParser = new PipeParser();

		try {
			// parse the message
			Message hl7Message = pipeParser.parse(hl7String);
			// create a new XML parser
            XMLParser xmlParser = new DefaultXMLParser();
            // encode the message in XML
            String xmlString = xmlParser.encode(hl7Message);
			
			return xmlString;
		} catch (HL7Exception e) {
			throw new TransformerException(org.mule.config.i18n.Message.createStaticMessage("Failed to parse String at: " + e.getSegmentName()), this);
		}
	}
}
