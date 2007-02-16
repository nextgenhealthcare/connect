package com.webreach.mirth.model.converters;

import java.io.FileReader;

import org.apache.log4j.Logger;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

public class EDIXMLParser extends DefaultHandler {
	private Logger logger = Logger.getLogger(this.getClass());
	private String segmentDelim;
	private String elementDelim;
	private String subelementDelim;
	private enum Location {DOCUMENT, SEGMENT, ELEMENT, SUBELEMENT};
	private Location currentLocation = Location.DOCUMENT;
	private boolean sawHeader = false;
	private boolean lastinSubelement = false;
	private StringBuilder output = new StringBuilder();
	public EDIXMLParser(String segmentDelim, String elementDelim, String subelementDelim) {
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
				if (lastinSubelement){
					output.deleteCharAt(output.length()-1);
				}
				lastinSubelement = false;
				output.append(elementDelim);
				currentLocation = Location.ELEMENT;
			}else if (currentLocation.equals(Location.ELEMENT)){
				currentLocation = Location.SUBELEMENT;
			}
		}
	}

	public void endElement(String uri, String name, String qName) {
		
		if (currentLocation.equals(Location.SEGMENT)){
			output.append(segmentDelim);
			currentLocation = Location.DOCUMENT;
		}else if (currentLocation.equals(Location.ELEMENT)){
			//output.append(el);
			currentLocation = Location.SEGMENT;
		}else if (currentLocation.equals(Location.SUBELEMENT)){
			output.append(subelementDelim);
			lastinSubelement = true;
			currentLocation = Location.ELEMENT;
		}
	}

	public void characters(char ch[], int start, int length) {
		
		for (int i = start; i < start + length; i++) {
			switch (ch[i]) {
			case '\\':
				output.append("\\\\");
				break;
			case '"':
				output.append("\\\"");
				break;
			case '\n':
				output.append("\\n");
				break;
			case '\r':
				output.append("\\r");
				break;
			case '\t':
				output.append("\\t");
				break;
			default:
				output.append(ch[i]);
				break;
			}
		}
		//System.out.print("\"\n");
	}

	public StringBuilder getOutput() {
		return output;
	}

	public void setOutput(StringBuilder output) {
		this.output = output;
	}

}