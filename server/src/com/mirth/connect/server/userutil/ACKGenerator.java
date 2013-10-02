/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.server.controllers.ControllerFactory;

/**
 * Allows users to generate HL7 v2.x acknowledgments based on an inbound message, with a specified
 * ACK code and custom text message. This class will not work as expected if the HL7 v2.x data type
 * plugin is disabled or uninstalled.
 */
public class ACKGenerator {
    private final String DEFAULTDATEFORMAT = "yyyyMMddHHmmss";
    private Logger logger = Logger.getLogger(getClass());

    /**
     * Instantiates a new ACKGenerator object.
     */
    public ACKGenerator() {}

    /**
     * Generates an HL7 v2.x acknowledgment. Assumes that the inbound message is proper ER7, and
     * uses the default format "yyyyMMddHHmmss" for the MSH.7 message date/time.
     * 
     * @param message
     *            The inbound HL7 v2.x message to generate the ACK for.
     * @param acknowledgementCode
     *            The MSA.1 ACK code to use (e.g. AA, AR, AE).
     * @param textMessage
     *            The MSA.3 text message to use.
     * @return The generated HL7 v2.x acknowledgment.
     * @throws Exception
     */
    public String generateAckResponse(String message, String acknowledgementCode, String textMessage) throws Exception {
        return generateAckResponse(message, false, acknowledgementCode, textMessage, DEFAULTDATEFORMAT, "");
    }

    /**
     * Generates an HL7 v2.x acknowledgment.
     * 
     * @param message
     *            The inbound HL7 v2.x message to generate the ACK for.
     * @param dataType
     *            If "XML", assumes the inbound message is formatted in XML, and the acknowledgment
     *            returned will also be XML.
     * @param acknowledgementCode
     *            The MSA.1 ACK code to use (e.g. AA, AR, AE).
     * @param textMessage
     *            The MSA.3 text message to use.
     * @param dateFormat
     *            The date/time format used to generate a timestamp for the MSH.7 message date/time
     *            (e.g. "yyyyMMddHHmmss").
     * @param errorMessage
     *            The ERR.1 error message to use. If left blank, an ERR segment will not be
     *            generated.
     * @return The generated HL7 v2.x acknowledgment.
     * @throws Exception
     * 
     * @deprecated This method is deprecated and will soon be removed. Please use
     *             generateAckResponse(message, isXML, acknowledgementCode, textMessage, dateFormat,
     *             errorMessage) instead.
     */
    // TODO: Remove in 3.1
    public String generateAckResponse(String message, String dataType, String acknowledgementCode, String textMessage, String dateFormat, String errorMessage) throws Exception {
        logger.error("This generateAckResponse(message, dataType, acknowledgementCode, textMessage, dateFormat, errorMessage) method is deprecated and will soon be removed. Please use generateAckResponse(message, isXML, acknowledgementCode, textMessage, dateFormat, errorMessage) instead.");
        return generateAckResponse(message, dataType.equals("XML"), acknowledgementCode, textMessage, dateFormat, errorMessage);
    }

    /**
     * Generates an HL7 v2.x acknowledgment.
     * 
     * @param message
     *            The inbound HL7 v2.x message to generate the ACK for.
     * @param isXML
     *            If true, assumes the inbound message is formatted in XML, and the acknowledgment
     *            returned will also be XML.
     * @param acknowledgementCode
     *            The MSA.1 ACK code to use (e.g. AA, AR, AE).
     * @param textMessage
     *            The MSA.3 text message to use.
     * @param dateFormat
     *            The date/time format used to generate a timestamp for the MSH.7 message date/time
     *            (e.g. "yyyyMMddHHmmss").
     * @param errorMessage
     *            The ERR.1 error message to use. If left blank, an ERR segment will not be
     *            generated.
     * @return The generated HL7 v2.x acknowledgment.
     * @throws Exception
     */
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
