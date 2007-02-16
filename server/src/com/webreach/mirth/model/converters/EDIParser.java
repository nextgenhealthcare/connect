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

package com.webreach.mirth.model.converters;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class EDIParser {
	private Logger logger = Logger.getLogger(this.getClass());
	private String segmentDelim;
	private String elementDelim;
	private String subelementDelim;
	
	public EDIParser(String segmentDelim, String elementDelim, String subelementDelim){
		this.segmentDelim = segmentDelim;
		this.elementDelim = elementDelim;
		this.subelementDelim = subelementDelim;
		return;
	}
	
	/**
	 * @param message
	 * @return XML formatted EDI Message
	 */
	public String parse(String message) throws EDIParseException{
		//First tokenize the segments
		if (message == null || message.length() < 3){
			logger.error("Unable to parse, message is null or too short: " + message);
			throw new EDIParseException("Unable to parse, message is null or too short: " + message);
		}
		message = message.trim();
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			
		} catch (ParserConfigurationException e) {
			logger.error("Error initializing XML Document\n" + e.getMessage());
			throw new EDIParseException(e.getMessage());
		}
		if (document != null){
			//Tokenize the segments first
			StringTokenizer segmentTokenizer = new StringTokenizer(message, segmentDelim);
			Element topElement = document.createElement("EDIMessage");;
			while (segmentTokenizer.hasMoreTokens()){
				String segment = segmentTokenizer.nextToken();
				//loop through each segment and pull out the elements
				StringTokenizer elementTokenizer = new StringTokenizer(segment, elementDelim, true);
				if (elementTokenizer.hasMoreTokens()){
					//Our XML element is named after the first element
					String segmentID = elementTokenizer.nextToken().trim();
					//check if we have EDI or X12
					if (segmentID.equals("ISA")){
						topElement = document.createElement("X12Transaction");		
					}
					Element segmentElement = document.createElement(segmentID);
					int fieldID  = 1;
					Element elementElement = null;
					while (elementTokenizer.hasMoreTokens()){
						//Go through each element and add as new child under the segment element
						String element = elementTokenizer.nextToken();
						//System.out.println("EL:" + element);
						//The naming is SEG.<field number>
						if (element.equals(elementDelim)){
							elementElement = document.createElement(segmentID + "." + fieldID);
							
						}else{
							if (element.indexOf(subelementDelim) > -1){
								//check if we have sub-elements, if so add them
								StringTokenizer subelementTokenizer = new StringTokenizer(element, subelementDelim, true);
								int subelementID = 1;
								if (subelementTokenizer.hasMoreTokens()){
									Element subelementElement = null;
										while (subelementTokenizer.hasMoreTokens()){
											String subelement = subelementTokenizer.nextToken();
											
											if (subelement.equals(subelementDelim)){
												subelementElement = document.createElement(segmentID + "." + fieldID + "." + subelementID);
												elementElement.appendChild(subelementElement);
											}else{
												if (subelementElement == null){
													subelementElement = document.createElement(segmentID + "." + fieldID + "." + subelementID);
												}
												//The naming is SEG.<field number>.<element number>
												subelementElement.setTextContent(subelement);
												elementElement.appendChild(subelementElement);
											}
											subelementID++;
										}
								}else{
									Element subelementElement = document.createElement(segmentID + "." + fieldID + "." + subelementID);
									subelementElement.setTextContent("");
									elementElement.appendChild(subelementElement);
								}
	
							}else{
								//Set the text contents to the value
								elementElement.setTextContent(element);
							}
						}
						segmentElement.appendChild(elementElement);
						fieldID++;
					}
					topElement.appendChild(segmentElement);
					
				}else{
					throw new EDIParseException("Could not find elements in segment: " + segment);
				}
			}
			document.appendChild(topElement);
			DocumentSerializer docSerializer = new DocumentSerializer();
			return docSerializer.toXML(document);
		}	
		
		
		return null;
	}
	
}
