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

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DocumentSerializer implements IXMLSerializer<Document> {
	private Logger logger = Logger.getLogger(this.getClass());
	private String[] cDataElements = null;
	private boolean preserveSpace;

	public DocumentSerializer() {
		this.preserveSpace = true;
	}

	public DocumentSerializer(String[] cDataElements) {
		this.cDataElements = cDataElements;
		this.preserveSpace = true;
	}

	public boolean isPreserveSpace() {
		return this.preserveSpace;
	}

	public void setPreserveSpace(boolean preserveSpace) {
		this.preserveSpace = preserveSpace;
	}

	public String toXML(Document source) {
		OutputFormat of = new OutputFormat(source);

		if (cDataElements != null) {
			of.setCDataElements(cDataElements);
		}

		of.setOmitXMLDeclaration(false);
		of.setIndenting(true);
		of.setPreserveSpace(preserveSpace);
		of.setLineSeparator(System.getProperty("line.separator"));

		StringWriter stringWriter = new StringWriter();
		XMLSerializer serializer = new XMLSerializer(stringWriter, of);
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(source);
			os.write(stringWriter.toString().getBytes());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return os.toString();
	}

	public Document fromXML(String source) {
		Document document = null;

		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(source)));
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return document;
	}

	public Map<String, String> getMetadata() throws SerializerException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getMetadataFromDocument(Document doc) throws SerializerException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
		// TODO Auto-generated method stub
		return null;
	}
}
