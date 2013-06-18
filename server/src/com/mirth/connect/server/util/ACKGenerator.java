/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.server.controllers.ControllerFactory;

// Wrapper for the LLP ack generator
// Made so that ACKs can be generated from JS
public class ACKGenerator {
    private final String DEFAULTDATEFORMAT = "yyyyMMddHHmmss";
    private Logger logger = Logger.getLogger(getClass());

    /**
     * This method defaults the protocol to ER7, along with the dateFormat to "yyyyMMddHHmmss" and
     * the errorMessage to ""
     */
    public String generateAckResponse(String message, String acknowledgementCode, String textMessage) throws Exception {
        return generateAckResponse(message, false, acknowledgementCode, textMessage, DEFAULTDATEFORMAT, "");
    }

    // TODO: Remove in 3.1
    @Deprecated
    public String generateAckResponse(String message, String dataType, String acknowledgementCode, String textMessage, String dateFormat, String errorMessage) throws Exception {
        logger.error("This generateAckResponse(message, dataType, acknowledgementCode, textMessage, dateFormat, errorMessage) method is deprecated and will soon be removed. Please use generateAckResponse(message, isXML, acknowledgementCode, textMessage, dateFormat, errorMessage) instead.");
        return generateAckResponse(message, dataType.equals("XML"), acknowledgementCode, textMessage, dateFormat, errorMessage);
    }

    public String generateAckResponse(String message, boolean isXML, String acknowledgementCode, String textMessage, String dateFormat, String errorMessage) throws Exception {
        DataTypeServerPlugin plugin = ControllerFactory.getFactory().createExtensionController().getDataTypePlugins().get("HL7V2");
        if (plugin != null) {
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("isXML", isXML);
            properties.put("ackCode", acknowledgementCode);
            properties.put("textMessage", textMessage);
            properties.put("dateFormat", dateFormat);
            properties.put("errorMessage", errorMessage);
            properties.put("segmentDelimiter", "\r");

            DataTypeProperties dataTypeProperties = plugin.getDefaultProperties();
            return plugin.getAutoResponder(dataTypeProperties.getSerializationProperties(), dataTypeProperties.getResponseGenerationProperties()).generateResponseMessage(message, properties);
        } else {
            return null;
        }
    }
}
