/*
	Milyn - Copyright (C) 2006

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software 
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
    
	See the GNU Lesser General Public License for more details:    
	http://www.gnu.org/licenses/lgpl.txt
*/

package com.webreach.mirth.model.converters;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * XMLReader for an X12N stream.
 * <p/>
 * Converts the X12N stream into a sequence of SAX events.  These events can 
 * be used to allow the X12N stream be treated as an XML Stream, therefore
 * allowing standard XML tools to be used to query, analyse or transform
 * the X12N stream.
 * @author tfennelly
 */
public class X12nToSaxEventParser implements XMLReader {

    private ContentHandler contentHandler;
    private int depth = 0;
    private static Attributes EMPTY_ATTRIBS = new AttributesImpl();

    /**
     * Parse an X12N InputSource.
     */
    public void parse(InputSource x12nInputSource) throws IOException, SAXException {
        if(contentHandler == null) {
            throw new IllegalStateException("'contentHandler' not set.  Cannot parse X12N stream.");
        }
        
        // Create a reader for reading the X12N segments...
        X12nStreamReader x12nStreamReader = new X12nStreamReader(x12nInputSource);
        
        // Fire the startDocument event, as well as the startElement event for the
        // enclosing <x12n> element i.e. the root element....
        contentHandler.startDocument();
        startElement("x12n", false);
        
        // process the x12n stream segments
        while(x12nStreamReader.movetoNextSegment()) {
            processX12nSegment(x12nStreamReader);
        }

        // Fire the endDocument event, as well as the endElement event for the
        // enclosing <x12n> element i.e. the root element.
        endElement("x12n", true);
        contentHandler.endDocument();
    }

    /**
     * Process an X12N "segment".
     * @param streamReader The X12N stream reader.
     * @throws SAXException 
     */
    private void processX12nSegment(X12nStreamReader streamReader) throws SAXException {
        String segmentCode = streamReader.readNextSegmentToken();
        
        if(X12nModel.isContainerStartSegment(segmentCode)) {
            startElement(segmentCode.toLowerCase(), true);            
            addChildElements(streamReader);
        } else if(X12nModel.isContainerEndSegment(segmentCode)) {
            String startSegmentCode = X12nModel.getStartSegmentCode(segmentCode);

            startElement(segmentCode.toLowerCase(), true);            
            addChildElements(streamReader);
            endElement(segmentCode.toLowerCase(), true);            
            endElement(startSegmentCode.toLowerCase(), true);
        } else {
            // It's not a container segment - so just output the subtree for this segment
            startElement(segmentCode.toLowerCase(), true);
            addChildElements(streamReader);
            endElement(segmentCode.toLowerCase(), true);
        }
    }

    /**
     * Read to the end of the current segment, outputing "data" elements
     * for all the tokens.
     * @param streamReader X12N Stream reader.
     * @throws SAXException
     */
    private void addChildElements(X12nStreamReader streamReader) throws SAXException {
        String token;
        
        while((token = streamReader.readNextSegmentToken()) != null) {
            startElement("data", true);
            token = trimCRLF(token);
            contentHandler.characters(token.toCharArray(), 0, token.length());
            endElement("data", false);
        }
    }

    private void startElement(String elementName, boolean indent) throws SAXException {
        if(indent) {
            indent();
        }
        contentHandler.startElement(null, elementName, "", EMPTY_ATTRIBS);
        depth++;
    }

    private void endElement(String elementName, boolean indent) throws SAXException {
        depth--;
        if(indent) {
            indent();
        }
        contentHandler.endElement(null, elementName, "");
    }

    private String trimCRLF(String token) {
        StringBuffer buffer = new StringBuffer(token);
        for(int i = buffer.length() - 1; i >= 0; i--) {
            char c = buffer.charAt(i);
            if(c == '\r' || c == '\n') {
                buffer.deleteCharAt(i);
            }
        }
        return buffer.toString();
    }

    // HACK :-) it's hardly going to be deeper than this!!
    private static final char[] indentChars = (new String("\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t").toCharArray());
    private void indent() throws SAXException {
        contentHandler.characters(indentChars, 0, depth + 1);
    }

    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /****************************************************************************
     * 
     * The following methods are currently unimplemnted...
     * 
     ****************************************************************************/
    
    public void parse(String systemId) throws IOException, SAXException {
        throw new UnsupportedOperationException("Operation not supports by this reader.");
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return false;
    }

    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public void setDTDHandler(DTDHandler arg0) {
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public void setEntityResolver(EntityResolver arg0) {
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public void setErrorHandler(ErrorHandler arg0) {
    }

    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return null;
    }

    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
    }
}
