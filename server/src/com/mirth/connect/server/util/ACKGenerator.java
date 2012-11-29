/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import com.mirth.connect.model.converters.DataTypeFactory;

// Wrapper for the LLP ack generator
// Made so that ACKs can be generated from JS
public class ACKGenerator {
    private final String DEFAULTDATEFORMAT = "yyyyMMddHHmmss";

    /**
     * This method defaults the protocol to HL7v2, along with the dateFormat to
     * "yyyyMMddHHmmss" and the errorMessage to ""
     */
    public String generateAckResponse(String message, String acknowledgementCode, String textMessage) throws Exception {
        return new com.mirth.connect.model.converters.hl7v2.ACKGenerator().generateAckResponse(message, DataTypeFactory.HL7V2, acknowledgementCode, textMessage, DEFAULTDATEFORMAT, new String());
    }

    public String generateAckResponse(String message, String dataType, String acknowledgementCode, String textMessage, String dateFormat, String errorMessage) throws Exception {
        return new com.mirth.connect.model.converters.hl7v2.ACKGenerator().generateAckResponse(message, dataType, acknowledgementCode, textMessage, dateFormat, errorMessage);
    }
}
