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
import java.io.Writer;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DocumentSerializer implements IXMLSerializer<Document> {
    private Logger logger = Logger.getLogger(this.getClass());
    private String[] cDataElements = null;

    public DocumentSerializer() {
        
    }
    
    public DocumentSerializer(String[] cDataElements) {
        this.cDataElements = cDataElements;
    }

    public String toXML(Document source) {
        Writer writer = new StringWriter();

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute("indent-number", new Integer(4));
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");

            if (source.getDoctype() != null) {
                String publicDoctype = source.getDoctype().getPublicId();
                String systemDoctype = source.getDoctype().getSystemId();

                if ((publicDoctype != null) && (publicDoctype.length() > 0)) {
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicDoctype);
                }

                if ((systemDoctype != null) && (systemDoctype.length() > 0)) {
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemDoctype);
                }
            }

            if (cDataElements != null) {
                StringBuilder cDataElementsString = new StringBuilder();

                for (int i = 0; i < cDataElements.length; i++) {
                    cDataElementsString.append(cDataElements[i] + " ");
                }

                transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, cDataElementsString.toString());
            }

            transformer.transform(new DOMSource(source), new StreamResult(writer));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return writer.toString();
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

    public Map<String, String> getMetadata() {
        return null;
    }

    public Map<String, String> getMetadataFromDocument(Document doc) throws SerializerException {
        return null;
    }

    public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException {
        return null;
    }

    public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
        return null;
    }
}
