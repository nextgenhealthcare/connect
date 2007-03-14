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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public class DocumentSerializer implements IXMLSerializer<Document> {
	private Logger logger = Logger.getLogger(this.getClass());
	private boolean preserveSpace;

	public boolean isPreserveSpace() {
		return this.preserveSpace;
	}

	public void setPreserveSpace(boolean preserveSpace) {
		this.preserveSpace = preserveSpace;
	}

	// ast: Change the serialize function to use the DOM3 API
	public String toXML(Document source) {
		// OutputFormat of = new OutputFormat(source);
		String subscrXML = null;
		StringWriter stringWriter = new StringWriter();
		try {
			// Get the implementations
			DOMImplementation imp = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
			DOMImplementationLS impls = (DOMImplementationLS) imp;
			// Prepare the output
			LSOutput domOutput = impls.createLSOutput();
			domOutput.setEncoding(java.nio.charset.Charset.defaultCharset().name());
			domOutput.setCharacterStream(stringWriter);
			// Prepare the serializer
			LSSerializer domWriter = impls.createLSSerializer();
			DOMConfiguration domConfig = domWriter.getDomConfig();
			secureSetParameter(domConfig, "format-pretty-print", true);
			secureSetParameter(domConfig, "element-content-whitespace", this.preserveSpace);
			domWriter.setNewLine("\r\n");
			// And finaly, write
			domWriter.write(source, domOutput);
			subscrXML = domOutput.getCharacterStream().toString();

			/*
			 * DOMStringList dsl = domConfig.getParameterNames(); // Just for
			 * curiosity.... for(int i=0;i<dsl.getLength();i++){
			 * System.out.println(dsl.item(i)+" =
			 * ["+domConfig.getParameter(dsl.item(i))+"]"); }
			 */
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return subscrXML;
	}

	protected void secureSetParameter(DOMConfiguration domConfig, String name, Object value) {
		if (domConfig.canSetParameter(name, value))
			domConfig.setParameter(name, value);
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
}
