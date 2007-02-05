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
import java.io.InputStreamReader;
import java.io.Reader;

import org.xml.sax.InputSource;

/**
 * X12N Stream reader.
 * @author tfennelly
 */
public class X12nStreamReader {
    
    private Reader reader;
    private StringBuffer segmentBuffer = new StringBuffer(512);

    /**
     * Construct the stream reader.
     * @param x12nInputSource X12N Stream input source.
     */
    public X12nStreamReader(InputSource x12nInputSource) {
        reader = x12nInputSource.getCharacterStream();
        if(reader == null) {
            reader = new InputStreamReader(x12nInputSource.getByteStream());
        }
    }

    /**
     * Moves to the next X12N segment.
     * <p/>
     * Basically reads the next segment into the internal StringBuffer.
     * @return True if there was a next segment, false if the end of the stream 
     * has been reached.
     * @throws IOException Error reading X12N stream.
     */
    protected boolean movetoNextSegment() throws IOException {
        int c = reader.read();

        segmentBuffer.setLength(0);

        // We reached the end of the stream the last time this method was
        // called - see the while loop below...
        if(c == -1) {
            return false;
        }
        
        // Segments are terminated by a line feed or EOF.
        while(c != '\n' && c != -1) {
            segmentBuffer.append((char)c);
            c = reader.read();
        }
        
        return true;
    }

    /**
     * Read the next segment token from the current segment buffer.
     * @return The next token, or null if no more tokens are available.
     */
    protected String readNextSegmentToken() {
        int tokenEndIndex = 0;
        String token;

        // The last token in the segment was read the last time this method was
        // called - see loop below...
        // Could also happen if the movetoNextSegment method wasn't called!
        if(segmentBuffer.length() == 0) {
            return null;
        }
        
        tokenEndIndex = segmentBuffer.indexOf("*");
        if(tokenEndIndex != -1) {
            token = segmentBuffer.substring(0, tokenEndIndex);
            segmentBuffer.delete(0, tokenEndIndex + 1);
        } else {
            // Last token in segment
            token = segmentBuffer.toString();
            segmentBuffer.setLength(0);
        }
        
        return token;
    }
}
