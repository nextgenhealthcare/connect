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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class EDISerializer implements IXMLSerializer {
	private String segmentDelim = "~";
	private String elementDelim = "*";
	private String subelementDelim = ":";

	public EDISerializer(Properties ediProperties) {
		if(ediProperties.get("segmentDelim") != null)
		{
			this.segmentDelim = (String) ediProperties.get("segmentDelim");
		}
		if(ediProperties.get("elementDelim") != null)
		{
			this.elementDelim = (String) ediProperties.get("elementDelim");
		}
		if(ediProperties.get("subelementDelim") != null)
		{
			this.subelementDelim = (String) ediProperties.get("subelementDelim");
		}
		return;
	}
	
	public EDISerializer() {
		
	}

	public Object fromXML(String source) throws SerializerException {
		XMLReader xr;
		try {
			xr = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			throw new SerializerException(e.getMessage());
		}
		EDIXMLHandler handler = new EDIXMLHandler(segmentDelim, elementDelim, subelementDelim);
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);
		try {
			xr.parse(new InputSource(new StringReader(source)));
		} catch (Exception e) {
			throw new SerializerException(e.getMessage());
		} 
		return handler.toString();
	}

	public String toXML(Object source) throws SerializerException {
		try {
			
			EDIReader ediReader = new EDIReader("~", "*", ":");
			StringWriter stringWriter = new StringWriter();
			XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				ediReader.setContentHandler(serializer);
				ediReader.parse(new InputSource(new StringReader((String)source)));
				os.write(stringWriter.toString().getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return os.toString();
		} catch (Exception e) {
			String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
			System.out.println(exceptionMessage);
		}
		return new String();
	}

	public String getSegmentDelim() {
		return segmentDelim;
	}

	public void setSegmentDelim(String segmentDelim) {
		this.segmentDelim = segmentDelim;
	}

	public String getElementDelim() {
		return elementDelim;
	}

	public void setElementDelim(String elementDelim) {
		this.elementDelim = elementDelim;
	}

	public String getSubelementDelim() {
		return subelementDelim;
	}

	public void setSubelementDelim(String subelementDelim) {
		this.subelementDelim = subelementDelim;
	}

}
