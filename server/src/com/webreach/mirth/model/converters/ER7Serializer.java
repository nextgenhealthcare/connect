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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class ER7Serializer implements IXMLSerializer<String> {
	private Logger logger = Logger.getLogger(this.getClass());
	private PipeParser pipeParser;
	private XMLParser xmlParser;
	private ER7Reader er7Parser;
	private boolean useStrictParser = true;

	public ER7Serializer(Map er7Properties) {
		if (er7Properties != null && er7Properties.get("useStrictParser") != null) {
			this.useStrictParser = ((Boolean) er7Properties.get("useStrictParser")).booleanValue();
		}
		if (!useStrictParser) {
			er7Parser = new ER7Reader();
		} else {
			initializeHapiParser();
		}
	}

	public ER7Serializer() {
		initializeHapiParser();
	}

	private void initializeHapiParser() {
		pipeParser = new PipeParser();
		pipeParser.setValidationContext(new NoValidation());
		xmlParser = new DefaultXMLParser();
		xmlParser.setValidationContext(new NoValidation());
		xmlParser.setKeepAsOriginalNodes(new String[] { "NTE.3", "OBX.5" });
	}

	/**
	 * Returns an XML-encoded HL7 message given an ER7-enconded HL7 message.
	 * 
	 * @param source
	 *            an ER7-encoded HL7 message.
	 * @return
	 */
	public String toXML(String source) throws SerializerException {
		StringBuilder builder = new StringBuilder();
		if (useStrictParser) {
			try {
				builder.append(xmlParser.encode(pipeParser.parse(source)));
			} catch (HL7Exception e) {
				throw new SerializerException(e);
			}
		} else {
			try {

				ER7Reader er7Reader = new ER7Reader();
				StringWriter stringWriter = new StringWriter();
				XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				try {
					er7Reader.setContentHandler(serializer);
					er7Reader.parse(new InputSource(new StringReader(source)));
					os.write(stringWriter.toString().getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				}
				builder.append(os.toString());
			} catch (Exception e) {
				String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
				logger.error(exceptionMessage);
			}
		}
		return sanitize(builder.toString());
	}

	/**
	 * Returns an ER7-encoded HL7 message given an XML-encoded HL7 message.
	 * 
	 * @param source
	 *            a XML-encoded HL7 message.
	 * @return
	 */
	public String fromXML(String source) throws SerializerException {
		StringBuilder builder = new StringBuilder();
		if (useStrictParser) {
			try {
				builder.append(pipeParser.encode(xmlParser.parse(source)));
			} catch (HL7Exception e) {
				throw new SerializerException(e);
			}
		} else {
			XMLReader xr;
			try {
				xr = XMLReaderFactory.createXMLReader();
				// The delimiters below need to come from the XML somehow...the
				// ER7 handler should take care of it
				ER7XMLHandler handler = new ER7XMLHandler("\r", "|", "^", "&", "~", "\\");
				xr.setContentHandler(handler);
				xr.setErrorHandler(handler);
				xr.parse(new InputSource(new StringReader(source)));
				builder.append(handler.getOutput());
			} catch (Exception e) {
				String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
				logger.error(exceptionMessage);
				throw new SerializerException(e);
			}
		}
		return builder.toString();
	}

	// cleans up the XML
	public String sanitize(String source) {
		return source;
	}
}
