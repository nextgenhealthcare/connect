/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.mirth.connect.model.ncpdp.NCPDPReference;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Jun 5, 2007
 * Time: 3:31:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class NCPDPXMLHandler  extends DefaultHandler {
	private Logger logger = Logger.getLogger(this.getClass());
    private String segmentDelim;
	private String groupDelim;
	private String fieldDelim;
    public boolean validationError = false;
    public SAXParseException saxParseException = null;
	private enum Location {
		DOCUMENT, SEGMENT, GROUP, FIELD, REPETITION
	};

	private Location currentLocation = Location.DOCUMENT;
	private boolean sawHeader = false;
    private boolean inTransactionHeader = false;
    private boolean inGroup = false;
//    private boolean lastinSubelement = false;
	private StringBuilder output = new StringBuilder();

	public NCPDPXMLHandler(String segmentDelim, String groupDelim, String fieldDelim) {
		super();
		this.segmentDelim = segmentDelim;
		this.groupDelim = groupDelim;
		this.fieldDelim = fieldDelim;
	}

	// //////////////////////////////////////////////////////////////////
	// Event handlers.
	// //////////////////////////////////////////////////////////////////

	public void startDocument() {
		currentLocation = Location.DOCUMENT;
        inGroup = false;
        inTransactionHeader = false;
    }

	public void endDocument() {
		currentLocation = Location.DOCUMENT;
        inGroup = false;
        inTransactionHeader = false;
    }

	public void startElement(String uri, String name, String qName, Attributes atts) {
		try {
        if (!sawHeader){
			sawHeader = true;
        }
		else{
            if(!inTransactionHeader && name.startsWith("TransactionHeader")){
                inTransactionHeader = true;
            }
            if(currentLocation.equals(Location.DOCUMENT)) {
                // dont output tag names or delimitors when in trans header
                    if(name.equals("TRANSACTIONS")){
                        currentLocation = Location.DOCUMENT;
                    }
                    else if(name.equals("TRANSACTION")){
                        output.append(groupDelim);
                        currentLocation = Location.GROUP;
                    }
                    else {
                        currentLocation = Location.SEGMENT;
                        if(!inTransactionHeader){
                            output.append(segmentDelim);
                            output.append(fieldDelim);
                            output.append(NCPDPReference.getInstance().getSegmentIdFromName(name));
                            if(atts!= null && atts.getLength()>0){
                                for(int i=0;i<atts.getLength();i++){
                                    output.append(fieldDelim);
                                    String localName= atts.getLocalName(i);
                                    String value = atts.getValue(i);
                                    output.append(NCPDPReference.getInstance().getCodeFromName(localName));
                                    output.append(value);
                                }
                            }
                        }
                    }

            }
            else if(currentLocation.equals(Location.GROUP)){
                // output the segment delimitor and segment name
                output.append(segmentDelim);
                output.append(fieldDelim);
                output.append(NCPDPReference.getInstance().getSegmentIdFromName(name));
                if(atts!= null && atts.getLength()>0){
                    for(int i=0;i<atts.getLength();i++){
                        output.append(fieldDelim);
                        String localName= atts.getLocalName(i);
                        String value = atts.getValue(i);
                        output.append(NCPDPReference.getInstance().getCodeFromName(localName));
                        output.append(value);
                    }
                }
                inGroup = true;
                currentLocation = Location.SEGMENT;
            }
            else if(currentLocation.equals(Location.SEGMENT)){
                // dont output tag names or delimitors when in trans header
                currentLocation = Location.FIELD;
                if(!inTransactionHeader){
                    output.append(fieldDelim);
                    if(isCounterField(name) || isCountField(name)){
                        output.append(NCPDPReference.getInstance().getCodeFromName(name));
                        output.append(atts.getValue(0));
                        currentLocation = Location.SEGMENT;
                    }
                    else {
                        output.append(NCPDPReference.getInstance().getCodeFromName(name));
                        if(atts!= null && atts.getLength()>0){
                            for(int i=0;i<atts.getLength();i++){
                                output.append(fieldDelim);
                                String localName= atts.getLocalName(i);
                                String value = atts.getValue(i);
                                output.append(NCPDPReference.getInstance().getCodeFromName(localName));
                                output.append(value);
                            }
                        }
                    }
                }
            }
        }
        }
        catch(Exception e) {
             e.printStackTrace();
        }

    }

	public void endElement(String uri, String name, String qName) {
        try {
             if (currentLocation.equals(Location.SEGMENT)){
                 if(isCounterField(name) || isCountField(name)) {
                     currentLocation = Location.SEGMENT;
                 }
                 else if(inGroup){
                     currentLocation = Location.GROUP;
                 }
                 else {
                     currentLocation = Location.DOCUMENT;
                 }
                 inTransactionHeader = false;
            }
            else if (currentLocation.equals(Location.GROUP)){
                 currentLocation = Location.DOCUMENT;
                 inGroup = false;
            }
            else if (currentLocation.equals(Location.FIELD)){
                currentLocation = Location.SEGMENT;
            }
            else if (currentLocation.equals(Location.DOCUMENT)){

            }
        }
        catch(Exception e) {
             e.printStackTrace();
        }
    }

	public void characters(char ch[], int start, int length) {
        try {
        output.append(ch, start, length);
        }
        catch(Exception e) {
             e.printStackTrace();
        }
            /*
}
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
    private boolean isCounterField(String fieldDesc){
        if(fieldDesc.endsWith("Counter")){
            return true;
        }
        return false;
    }
    private boolean isCountField(String fieldDesc){
        if(fieldDesc.endsWith("Count")){
            return true;
        }
        return false;
    }

  public void error(SAXParseException exception)
      throws SAXException {
      System.out.println("SAXException" + exception.getMessage());
      logger.error("SAXException" + exception.getMessage());
      validationError = true;
      saxParseException = exception;

  }
  public void fatalError(SAXParseException exception)
      throws SAXException {
      System.out.println("SAXException" + exception.getMessage());
      logger.error("SAXException" + exception.getMessage());
      validationError = true;
      saxParseException=exception;
  }
  public void warning(SAXParseException exception)
      throws SAXException { }
}
