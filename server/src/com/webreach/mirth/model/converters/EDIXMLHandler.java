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

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class EDIXMLHandler extends DefaultHandler {
	private String segmentDelim;
	private String elementDelim;
	private String subelementDelim;

	private enum Location {
		DOCUMENT, SEGMENT, ELEMENT, SUBELEMENT
	};

	private Location currentLocation = Location.DOCUMENT;
	private boolean sawHeader = false;
	private boolean lastinSubelement = false;
	private StringBuilder output = new StringBuilder();

	public EDIXMLHandler(String segmentDelim, String elementDelim, String subelementDelim) {
		super();
		this.segmentDelim = segmentDelim;
		this.elementDelim = elementDelim;
		this.subelementDelim = subelementDelim;
	}

	// //////////////////////////////////////////////////////////////////
	// Event handlers.
	// //////////////////////////////////////////////////////////////////

	public void startDocument() {
		currentLocation = Location.DOCUMENT;
	}

	public void endDocument() {
		currentLocation = Location.DOCUMENT;
	}

	public void startElement(String uri, String name, String qName, Attributes atts) {
		if (sawHeader == false){
			sawHeader = true;
		}else{
			if (currentLocation.equals(Location.DOCUMENT)){
				output.append(name);
				currentLocation = Location.SEGMENT;
				lastinSubelement = false;
			}else if (currentLocation.equals(Location.SEGMENT)){
				output.append(elementDelim);
				currentLocation = Location.ELEMENT;
				lastinSubelement = false;
			}else if (currentLocation.equals(Location.ELEMENT)){
				if (lastinSubelement){
					output.append(subelementDelim);
				}
				currentLocation = Location.SUBELEMENT;
				lastinSubelement = true;
			}
		}
	}

	public void endElement(String uri, String name, String qName) {
		if (currentLocation.equals(Location.SEGMENT)){
			//output.deleteCharAt(output.length() - 1);
			output.append(segmentDelim);
			currentLocation = Location.DOCUMENT;
		}else if (currentLocation.equals(Location.ELEMENT)){
			
			currentLocation = Location.SEGMENT;
		}else if (currentLocation.equals(Location.SUBELEMENT)){
			
			currentLocation = Location.ELEMENT;
		}else if (currentLocation.equals(Location.DOCUMENT)){
			
		}
		
	}

	public void characters(char ch[], int start, int length) {
		output.append(ch, start, length);
		/*
		for (int i = start; i < start + length; i++) {
			switch (ch[i]) {
				/*
			case '\\':
				output.append("\\\\");
				break;
			case '"':
				output.append("\\\"");
				break;
			case '\n':
				output.append("\n");
				break;
			case '\r':
				output.append("\r");
				break;
			case '\t':
				output.append("\t");
				break;
				
			default:
				output.append(ch[i]);
				break;
			}
		}
		*/
		//System.out.print("\"\n");
	}

	public StringBuilder getOutput() {
		return output;
	}

	public void setOutput(StringBuilder output) {
		this.output = output;
	}

}