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


package com.webreach.mirth;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

/**
 * A set of utility methods used by the Mirth engine.
 * 
 */
public class MirthUtil {
	protected static transient Log logger = LogFactory.getLog(MirthUtil.class);

	private MirthUtil() {};

	/**
	 * Returns the serialized String of the specified Document object.
	 * 
	 * @param document
	 *            the DOM Document object
	 * @return the serialized String of the specified Document object.
	 */
	public static String serializeDocument(Document document, boolean omitXMLDeclaration) {
		try {
			OutputFormat of = new OutputFormat(document);
			of.setOmitXMLDeclaration(omitXMLDeclaration);
			of.setIndenting(true);
			of.setLineSeparator("\n");
			// of.setLineWidth(120);
			StringWriter stringOut = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(stringOut, of);
			serializer.serialize(document);

			return stringOut.toString();
		} catch (IOException e) {
			e.printStackTrace();

			return null;
		}
	}

	public static XMLSerializer getXMLSerializer(OutputStream os, String[] elements) {
		OutputFormat of = new OutputFormat();
		of.setCDataElements(elements);
		of.setIndenting(true);
		of.setLineSeparator("\n");
		of.setOmitDocumentType(true);
//		of.setDoctype("-//SymphonySoft //DTD mule-configuration XML V1.0//EN", "mule-configuration.dtd");
		XMLSerializer serializer = new XMLSerializer(of);
		serializer.setOutputByteStream(os);

		return serializer;
	}
}
