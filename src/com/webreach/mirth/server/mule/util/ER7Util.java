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
 *	 Chris Lang	<chrisl@webreachinc.com>
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.mule.util;

import org.apache.log4j.Logger;
import org.mule.umo.transformer.TransformerException;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
//Provides conversion between ER7 and XML
public class ER7Util {
	private Logger logger = Logger.getLogger(this.getClass());
	//Converts ER7 String to XML 
	public String ConvertToXML(Object source) {
		String message = (String) source;

		PipeParser pipeParser = new PipeParser();
		// disables all message validation
		pipeParser.setValidationContext(new NoValidation());
		XMLParser xmlParser = new DefaultXMLParser();

		try {
			logger.debug("encoding ER7 message to XML:\n" + message);
			return xmlParser.encode(pipeParser.parse(message));
		} catch (HL7Exception e) {
			logger.error(e.getSegmentName());
			return "";
		}
	}
	//Converts XML String to ER7
	public String ConvertToER7(Object xml) {
		String message = (String) xml;
		XMLParser xmlParser = new DefaultXMLParser();
		// disables all message validation
		xmlParser.setValidationContext(new NoValidation());
		PipeParser pipeParser = new PipeParser();
		try {
			logger.debug("encoding XML message to ER7:\n" + message);
			return pipeParser.encode(xmlParser.parse(message));
		} catch (HL7Exception e) {
			logger.error(e.getSegmentName());
			return "";
		}
	}
}
