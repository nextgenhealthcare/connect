package com.webreach.mirth.model.converters;

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
 *   Christopher lang <chrisl@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserAdapter;


/*
 * Converts EDI message to XML
 */
public class ER7Reader extends ParserAdapter {
	private Logger logger = Logger.getLogger(this.getClass());
	private boolean handleRepetitions = false;
	private boolean convertLFtoCR = false;
	public ER7Reader(boolean handleRepetitions, boolean convertLFtoCR) throws SAXException {
		this.handleRepetitions = handleRepetitions;
		this.convertLFtoCR = convertLFtoCR;
	}

	public void parse(InputSource input) throws SAXException, IOException {
		// Read the data from the InputSource
		BufferedReader in = new BufferedReader(input.getCharacterStream());
		String nextLine = "";
		StringBuffer sb = new StringBuffer();
		if (convertLFtoCR){
			while ((nextLine = in.readLine()) != null) {
				sb.append(nextLine);
				sb.append("\r"); // TODO: This should be configurable
			}
		}else{
			int c;
			while ((c = in.read()) != -1){
				sb.append((char)c);
			}
		}
		String message = sb.toString();
		message = message.trim();
		// fire SAX events
		String documentHead = "";
		ContentHandler contentHandler = getContentHandler();
		contentHandler.startDocument();
		// First tokenize the segments
		if (message == null || message.length() < 3) {
			logger.error("Unable to parse, message is null or too short: " + message);
			throw new SAXException("Unable to parse, message is null or too short: " + message);
		}
		String segmentDelim = "\r";
		String fieldDelim = new String(new char[] { message.charAt(3) }); // Usually
		// |
		int nextFieldDelim = message.indexOf(message.charAt(3), 4);
		String componentDelim = "^";
		String repetitionSep = "~";
		String escapeChar = "\\";
		String subcomponentDelim = "&";
		if (nextFieldDelim > 4){
			componentDelim = new String(new char[] { message.charAt(4) }); // Usually ^
		}
		if (nextFieldDelim > 5){
			 repetitionSep = new String(new char[] { message.charAt(5) }); // Usually ~
		}
		if (nextFieldDelim > 6){
			escapeChar = new String(new char[] { message.charAt(6) }); // Usually \
		}
		if (nextFieldDelim > 7){
			subcomponentDelim = new String(new char[] { message.charAt(7) }); // Usually &
		}
		
	
		// Tokenize the segments first
		StringTokenizer segmentTokenizer = new StringTokenizer(message, segmentDelim);

		documentHead = handleSegment(documentHead, contentHandler, fieldDelim, componentDelim, subcomponentDelim, repetitionSep, escapeChar, segmentTokenizer);

		contentHandler.endElement("", documentHead, "");
		contentHandler.endDocument();
	}

	private String handleSegment(String documentHead, ContentHandler contentHandler, String fieldDelim, String componentDelim, String subcomponentDelim, String repetitionSep, String escapeChar, StringTokenizer segmentTokenizer) throws SAXException {
		int segmentCounter = 0;
		while (segmentTokenizer.hasMoreTokens()) {
			String segment = segmentTokenizer.nextToken();
			// loop through each segment and pull out the elements
			StringTokenizer elementTokenizer = new StringTokenizer(segment, fieldDelim, true);
			if (elementTokenizer.hasMoreTokens()) {
				// Our XML element is named after the first element
				String segmentID = elementTokenizer.nextToken().trim();
				if (segmentCounter == 0) {
					documentHead = "HL7Message";
					contentHandler.startElement("", documentHead, "", null);
				}
				contentHandler.startElement("", segmentID, "", null);
				handleElement(contentHandler, fieldDelim, componentDelim, subcomponentDelim, repetitionSep, escapeChar, elementTokenizer, segmentID);
				contentHandler.endElement("", segmentID, "");

			} else {
				throw new SAXException("Could not find elements in segment: " + segment);
			}
			segmentCounter++;
		}
		return documentHead;
	}

	private void handleElement(ContentHandler contentHandler, String fieldDelim, String componentDelim, String subcomponentDelim, String repetitionSep, String escapeChar, StringTokenizer elementTokenizer, String segmentID) throws SAXException {
		int fieldID = 0;
		boolean lastsegElement = false;
		boolean inMSH = false;
		while (elementTokenizer.hasMoreTokens()) {
			inMSH = false;

			// Go through each element and add as new child under
			// the segment element
			String element = elementTokenizer.nextToken();
			
			// System.out.println("EL:" + element);
			// The naming is SEG.<field number>
			if (element.equals(fieldDelim)) {

				if (lastsegElement) {
					contentHandler.startElement("", segmentID + "." + fieldID, "", null);
					contentHandler.endElement("", segmentID + "." + fieldID, "");
				}
				fieldID++;
				lastsegElement = true;
			} else {
				lastsegElement = false;
				// batch supports
				if (segmentID.equals("MSH") || segmentID.equals("FHS") || segmentID.equals("BHS")) {
					inMSH = true;
				}
				if (inMSH && fieldID == 1) {
					contentHandler.startElement("", segmentID + "." + fieldID, "", null);
					contentHandler.characters(fieldDelim.toCharArray(), 0, 1);
					contentHandler.endElement("", segmentID + "." + (fieldID), null);
					fieldID++;
					contentHandler.startElement("", segmentID + "." + fieldID, "", null);
					String specialChars = (componentDelim + repetitionSep + escapeChar + subcomponentDelim);
					contentHandler.characters(specialChars.toCharArray(), 0, specialChars.length());
					contentHandler.endElement("", segmentID + "." + (fieldID), null);
				} else if (inMSH && fieldID == 2) {

				} else {
					if (handleRepetitions) {
						handleRepetitions(contentHandler, componentDelim, repetitionSep, segmentID, fieldID, element);
					} else {
						handleElementsInternal(contentHandler, componentDelim, segmentID, fieldID, element);
					}
				}
			}
		}
		if (lastsegElement) {
			contentHandler.startElement("", segmentID + "." + fieldID, "", null);
			contentHandler.endElement("", segmentID + "." + fieldID, "");
		}
	}

	private void handleRepetitions(ContentHandler contentHandler, String componentDelim, String repetitionSep, String segmentID, int fieldID, String element) throws SAXException {
		StringTokenizer repTokenizer = new StringTokenizer(element, repetitionSep, true);
		boolean lastrepElement = true;
		while (repTokenizer.hasMoreTokens()) {
			element = repTokenizer.nextToken();
			if (element.equals(repetitionSep)) {
				// check for ~~
				if (lastrepElement) {
					contentHandler.startElement("", segmentID + "." + fieldID, "", null);
					contentHandler.characters("".toCharArray(), 0, 0);
					contentHandler.endElement("", segmentID + "." + fieldID, "");
				}
				lastrepElement = true;
			} else {
				lastrepElement = false;

				handleElementsInternal(contentHandler, componentDelim, segmentID, fieldID, element);
			}
		}
		if (lastrepElement) {
			contentHandler.startElement("", segmentID + "." + (fieldID), "", null);
			contentHandler.characters("".toCharArray(), 0, 0);
			contentHandler.endElement("", segmentID + "." + (fieldID), "");
		}
	}

	private void handleElementsInternal(ContentHandler contentHandler, String componentDelim, String segmentID, int fieldID, String element) throws SAXException {
		int subelementID;
		if (element.indexOf(componentDelim) > -1) {
			contentHandler.startElement("", segmentID + "." + fieldID, "", null);
			// check if we have sub-elements, if so add them
			StringTokenizer subelementTokenizer = new StringTokenizer(element, componentDelim, true);
			subelementID = 1;
			handleSubElement(contentHandler, componentDelim, segmentID, fieldID, subelementID, subelementTokenizer);
			contentHandler.endElement("", segmentID + "." + (fieldID), null);
		} else {
			contentHandler.startElement("", segmentID + "." + fieldID, "", null);
			contentHandler.startElement("", segmentID + "." + fieldID + ".1", "", null);
			// Set the text contents to the value
			contentHandler.characters(element.toCharArray(), 0, element.length());
			contentHandler.endElement("", segmentID + "." + (fieldID) + ".1", null);
			contentHandler.endElement("", segmentID + "." + (fieldID), null);
		}
	}

	private void handleSubElement(ContentHandler contentHandler, String componentDelim, String segmentID, int fieldID, int subelementID, StringTokenizer subelementTokenizer) throws SAXException {
		boolean lastsegSubelement = true;
		while (subelementTokenizer.hasMoreTokens()) {
			String subelement = subelementTokenizer.nextToken();
			if (subelement.equals(componentDelim)) {
				String subelementName = segmentID + "." + fieldID + "." + subelementID;
				if (lastsegSubelement) {
					contentHandler.startElement("", subelementName, "", null);
					contentHandler.characters("".toCharArray(), 0, 0);
					contentHandler.endElement("", subelementName, "");
				}
				subelementID++;
				lastsegSubelement = true;
			} else {
				String subelementName = segmentID + "." + fieldID + "." + subelementID;
				lastsegSubelement = false;
				// The naming is SEG.<field
				// number>.<element number>
				contentHandler.startElement("", subelementName, "", null);
				contentHandler.characters(subelement.toCharArray(), 0, subelement.length());
				contentHandler.endElement("", subelementName, "");
			}
		}
		String subelementName = segmentID + "." + (fieldID) + "." + subelementID;
		if (lastsegSubelement) {
			contentHandler.startElement("", subelementName, "", null);
			contentHandler.characters("".toCharArray(), 0, 0);
			contentHandler.endElement("", subelementName, "");
		}
	}

}
