/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.mirth.connect.util.MirthXmlUtil;

public class XMLPrettyPrinter implements ContentHandler {

    private Writer out;
    private int depth = 0; // depth in hierarchy
    private boolean encodeEntities;

    // I could allow the user to set a lot more details about
    // how the XML is indented; e.g. how many spaces, tabs or spaces,
    // etc.; but since this wouldn't add anything to the discussion
    // of XML I'll leave it as an exercise for the student

    public XMLPrettyPrinter(Writer out) {
        this.out = out;
    }

    public XMLPrettyPrinter(OutputStream out) {
        try {
            this.out = new OutputStreamWriter(out, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("Something is seriously wrong." + " Your VM does not support UTF-8 encoding!");
        }
    }

    public void setDocumentLocator(Locator locator) {}

    public void startDocument() throws SAXException {

        depth = 0; // so instance can be reused
        try {
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        } catch (IOException e) {
            throw new SAXException(e);
        }

    }

    public void endDocument() throws SAXException {
        try {
            out.flush();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void endElement(String name) throws SAXException {
        try {
            depth--;
            // indent();
            out.write("</");
            out.write(name);
            out.write(">");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void characters(char[] text, int start, int length) throws SAXException {
        try {
            // indent();
            if (encodeEntities) {
                out.write(MirthXmlUtil.encode(text, start, length));
            } else {
                out.write(text, start, length);
            }
            // out.write("\r\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void ignorableWhitespace(char[] text, int start, int length) throws SAXException {

    }

    public void processingInstruction(String target, String data) throws SAXException {
        try {
            // indent();
            out.write("<?");
            out.write(target);
            out.write(" ");
            out.write(data);
            out.write("?>");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        endElement(localName);

    }

    public void endPrefixMapping(String prefix) throws SAXException {

    }

    public void skippedEntity(String name) throws SAXException {

    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        try {
            // if (indentation) indent();
            out.write("<");
            out.write(localName);
            if (atts != null) {
                int i = 0;
                while (i < atts.getLength()) {
                    out.write(" " + atts.getLocalName(i) + "=\"");
                    if (encodeEntities) {
                        out.write(MirthXmlUtil.encode(atts.getValue(i).toCharArray(), 0, atts.getValue(i).length()));
                    } else {
                        out.write(atts.getValue(i));
                    }
                    out.write("\"");
                    i++;
                }
            }
            out.write(">");
            depth++;
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {}

    public boolean isEncodeEntities() {
        return encodeEntities;
    }

    public void setEncodeEntities(boolean encodeEntities) {
        this.encodeEntities = encodeEntities;
    }

}
