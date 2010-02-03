/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.converters;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.webreach.mirth.util.Entities;

public class ER7XMLHandler extends DefaultHandler {
	private String fieldDelim;
	private String componentDelim;
	private String repetitionSep;

	private enum Location {
		DOCUMENT, SEGMENT, ELEMENT, SUBELEMENT
	};

	private Location currentLocation = Location.DOCUMENT;
	private boolean sawHeader = false;
	private boolean lastinSubelement = false;
	private boolean inMSH1 = false;
	private boolean inMSH2 = false;
	private boolean sawMSH1 = false;
	private boolean sawMSH2 = false;
	private boolean inElement = false;
	private String lastSegment = new String();
	private String lastElement = new String();
	private StringBuilder output = new StringBuilder();
	private String segmentDelim;
	private Entities entities = Entities.getInstance();
	private boolean encodeEntities = false;

	public ER7XMLHandler(String segmentDelim, String fieldDelim, String componentDelim, String subcomponentDelim, String repetitionSep, String escapeChar, boolean encodeEntities) {
		super();
		this.segmentDelim = segmentDelim;
		this.fieldDelim = fieldDelim;
		this.componentDelim = componentDelim;
		this.repetitionSep = repetitionSep;
		this.encodeEntities = encodeEntities;
	}

	// //////////////////////////////////////////////////////////////////
	// Event handlers.
	// //////////////////////////////////////////////////////////////////

	public void startDocument() {
		inElement = false;
		currentLocation = Location.DOCUMENT;
	}

	public void endDocument() {
		inElement = false;
		currentLocation = Location.DOCUMENT;
	}

	public void startElement(String uri, String name, String qName, Attributes atts) {
		inElement = true;
		if (sawHeader == false) {
			sawHeader = true;
		} else {
			if (!sawMSH2 && (name.equals("MSH.2") || name.equals("BHS.2") || name.equals("FHS.2"))) {
				inMSH2 = true;
				sawMSH2 = true;
			}
			if (!sawMSH1 && (name.equals("MSH.1") || name.equals("BHS.1") || name.equals("FHS.1"))) {
				lastinSubelement = false;
				inMSH1 = true;
				sawMSH1 = true;
			} else if (currentLocation.equals(Location.DOCUMENT)) {
				output.append(name);

				currentLocation = Location.SEGMENT;
				lastinSubelement = false;
			} else if (currentLocation.equals(Location.SEGMENT)) {

				if (lastSegment.equals(name)) {
					output.append(repetitionSep);
				} else {
					// System.out.println("Last segment: " + lastSegment + "
					// Segment: " + name);

					// handle any missing fields
					if (!inMSH2) {
						int lastFieldId = 0;
						if (lastSegment.length() > 0) {
							lastFieldId = Integer.parseInt(lastSegment.split("\\.")[1]);
						}
						int currentFieldId = Integer.parseInt(name.split("\\.")[1]); // get
																						// the
																						// second
																						// part,
																						// the
																						// id

						int difference = currentFieldId - lastFieldId;

						for (int i = 1; i < difference; i++) {
							output.append(fieldDelim);
						}
					}
					output.append(fieldDelim);
					lastSegment = name;

				}

				currentLocation = Location.ELEMENT;
				lastinSubelement = false;
			} else if (currentLocation.equals(Location.ELEMENT)) {
				if (lastinSubelement) {
					output.append(componentDelim);
				}

				// System.out.println("Last element: " + lastElement + " Current
				// element: " + name);

				// handle any missing elements

				int lastFieldId = 0;
				if (lastElement.length() > 0) {
					lastFieldId = Integer.parseInt(lastElement.split("\\.")[2]);
				}
				
				int currentFieldId = Integer.parseInt(name.split("\\.")[2]); 
				
				int difference = currentFieldId - lastFieldId;

				for (int i = 1; i < difference; i++) {
					output.append(componentDelim);
				}

				lastElement = name;
				currentLocation = Location.SUBELEMENT;
				lastinSubelement = true;
			}
		}
	}

	public void endElement(String uri, String name, String qName) {
		inElement = false;
		if (sawMSH2 && (name.equals("MSH.2") || name.equals("BHS.2") || name.equals("FHS.2"))) {
			inMSH2 = false;
			sawMSH2 = false;
		}
		if (sawMSH1 && (name.equals("MSH.1") || name.equals("BHS.1") || name.equals("FHS.1"))) {
			inMSH1 = false;
			sawMSH1 = false;
			output.deleteCharAt(output.length() - 1);
		} else if (currentLocation.equals(Location.SEGMENT)) {
			output.append(segmentDelim);
			currentLocation = Location.DOCUMENT;
			lastSegment = "";
		} else if (currentLocation.equals(Location.ELEMENT)) {
			lastElement = "";
			currentLocation = Location.SEGMENT;
		} else if (currentLocation.equals(Location.SUBELEMENT)) {
			currentLocation = Location.ELEMENT;
		} else if (currentLocation.equals(Location.DOCUMENT)) {

		}

	}

	public void characters(char ch[], int start, int length) {
		if (inMSH1) {
			fieldDelim = ch[start] + "";
			inMSH1 = false;
		} else if (inMSH2) {
			componentDelim = ch[start] + "";
			inMSH2 = false;
		}
		//if (encodeEntities) {
		//	String characters = entities.encode(ch, start, length);
		//	ch = characters.toCharArray();
		//	start = 0;
		//	length = ch.length;
		//} 
		output.append(ch, start, length);

		/*
		 * for (int i = start; i < start + length; i++) { switch (ch[i]) {
		 * 
		 * case '\\': output.append("\\\\"); break; case '"':
		 * output.append("\\\""); break; case '\n': output.append("\n"); break;
		 * case '\r': output.append("\r"); break; case '\t':
		 * output.append("\t"); break;
		 * 
		 * default: output.append(ch[i]); break; } }
		 */
		// System.out.print("\"\n");
	}

	public StringBuilder getOutput() {
		return output;
	}

	public void setOutput(StringBuilder output) {
		this.output = output;
	}

}
