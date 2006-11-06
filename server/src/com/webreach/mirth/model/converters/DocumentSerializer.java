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
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

public class DocumentSerializer implements IXMLSerializer<Document>{
	private String[] cDataElements = null;
	
	public DocumentSerializer() {
		
	}
	
	public DocumentSerializer(String[] cDataElements) {
		this.cDataElements = cDataElements;
	}
	
	public String toXML(Document source) {
		OutputFormat of = new OutputFormat(source);

		if (cDataElements != null) {
			of.setCDataElements(cDataElements);
		}

		of.setOmitXMLDeclaration(false);
		of.setIndenting(true);
		of.setLineSeparator("\n");

		StringWriter stringWriter = new StringWriter();
		XMLSerializer serializer = new XMLSerializer(stringWriter, of);
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(source);
			os.write(stringWriter.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return os.toString();
	}

	public Document fromXML(String source) {
		Document document = null;
		
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return document;
	}
}
