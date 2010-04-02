/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.mllp;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.mirth.connect.model.MessageObject.Protocol;
import com.mirth.connect.model.converters.SerializerFactory;
import com.mirth.connect.server.util.DateUtil;

//Supports ACKS from 2.1-2.4
//2.5 is supported but the advanced fields in ERR and SFT are not supported
public class ACKGenerator {
    private final String DEFAULTDATEFORMAT = "yyyyMMddHHmmss";
    private Logger logger = Logger.getLogger(this.getClass());

    public String generateAckResponse(String message, Protocol protocol, String acknowledgementCode, String textMessage) throws Exception {
        return generateAckResponse(message, protocol, acknowledgementCode, textMessage, DEFAULTDATEFORMAT, new String());
    }

    public String generateAckResponse(String message, Protocol protocol, String acknowledgementCode, String textMessage, String dateFormat, String errorMessage) throws Exception {
        if (message == null || message.length() < 9) {
            logger.error("Unable to parse, message is null or too short: " + message);
            throw new Exception("Unable to parse, message is null or too short: " + message);
        }

        boolean ackIsXML = false;
        char segmentDelim = '\r';

        char fieldDelim = '|'; // Usually |
        char componentDelim = '^'; // Usually ^
        char repetitionSeparator = '~'; // Usually ~
        char escapeCharacter = '\\'; // Usually \
        char subcomponentDelim = '&'; // Usually &

        // Default message Delimiters
        String sendingApplication = ""; // MSH.3.1
        String sendingFacility = ""; // MSH.4.1
        String receivingApplication = ""; // MSH.5.1
        String receivingFacility = ""; // MSH.6.1
        String originalEvent = ""; // MSH.9.2 (MSH.9/MSGH.2)
        String originalid = ""; // MSH.10.1
        String procid = ""; // MSH.11.1
        String procidmode = ""; // // MSH.11.2
        String version = ""; // MSH.12.1

        // If XML is being sent over MLLP, use the strict parser
        // to convert the message to ER7, then generate the ack.
        if (protocol.equals(Protocol.XML)) {
            ackIsXML = true;
            message = SerializerFactory.getHL7Serializer(true, false, false).fromXML(message);
        }

        fieldDelim = message.charAt(3); // Usually |
        componentDelim = message.charAt(4); // Usually ^
        if (message.charAt(5) != fieldDelim) {
            repetitionSeparator = message.charAt(5); // Usually ~
            if (message.charAt(6) != fieldDelim) {
                escapeCharacter = message.charAt(6); // Usually \
                if (message.charAt(7) != fieldDelim) {
                    subcomponentDelim = message.charAt(7); // Usually &
                }
            }
        }

        // Handle single line messages without any segment delimiters
        int firstSegmentDelim = message.indexOf(String.valueOf(segmentDelim));
        String mshString;
        if (firstSegmentDelim != -1) {
            mshString = message.substring(0, firstSegmentDelim);
        } else {
            mshString = message;
        }

        Pattern fieldPattern = Pattern.compile(Pattern.quote(String.valueOf(fieldDelim)));
        Pattern componentPattern = Pattern.compile(Pattern.quote(String.valueOf(componentDelim)));

        String[] mshFields = fieldPattern.split(mshString);
        int mshFieldsLength = mshFields.length;

        if (mshFieldsLength > 2) {
            sendingApplication = componentPattern.split(mshFields[2])[0]; // MSH.3.1
            if (mshFieldsLength > 3) {
                sendingFacility = componentPattern.split(mshFields[3])[0]; // MSH.4.1
                if (mshFieldsLength > 4) {
                    receivingApplication = componentPattern.split(mshFields[4])[0]; // MSH.5.1
                    if (mshFieldsLength > 5) {
                        receivingFacility = componentPattern.split(mshFields[5])[0]; // MSH.6.1
                        if (mshFieldsLength > 8) { // MSH.9.2
                            String[] msgDT = componentPattern.split(mshFields[8]);

                            if (msgDT.length > 1) {
                                originalEvent = msgDT[1];
                            }

                            if (mshFieldsLength > 9) {
                                originalid = componentPattern.split(mshFields[9])[0]; // MSH.10.1
                                if (mshFieldsLength > 10) {
                                    String[] msh11 = componentPattern.split(mshFields[10]); // MSH.11
                                    procid = msh11[0]; // MSH.11.1

                                    if (msh11.length > 1) {
                                        procidmode = msh11[1]; // MSH.11.2
                                    }

                                    if (mshFieldsLength > 11) {
                                        version = componentPattern.split(mshFields[11])[0]; // MSH.12.1
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (textMessage != null && textMessage.length() > 0) {
            textMessage = fieldDelim + textMessage;
        } else {
            textMessage = new String();
        }
        if (errorMessage != null && errorMessage.length() > 0) {
            errorMessage = segmentDelim + "ERR" + fieldDelim + errorMessage;
        } else {
            errorMessage = new String();
        }

        // Set defaults to important fields if they're blank.
        if (version.length() == 0) {
            version = "2.4";
        }
        if (procid.length() == 0) {
            procid = "P";
        }
        if (originalid.length() == 0) {
            originalid = "1";
        }
        if (receivingApplication.length() == 0) {
            receivingApplication = "MIRTH";
        }

        String timestamp = DateUtil.getCurrentDate(dateFormat);

        StringBuilder ackBuilder = new StringBuilder();

        // ackBuilder.append(message.substring(0, 9));
        ackBuilder.append("MSH" + fieldDelim + componentDelim + repetitionSeparator + escapeCharacter + subcomponentDelim + fieldDelim);

        ackBuilder.append(receivingApplication);
        ackBuilder.append(fieldDelim);
        ackBuilder.append(receivingFacility);
        ackBuilder.append(fieldDelim);
        ackBuilder.append(sendingApplication);
        ackBuilder.append(fieldDelim);
        ackBuilder.append(sendingFacility);
        ackBuilder.append(fieldDelim);
        ackBuilder.append(timestamp);
        ackBuilder.append(fieldDelim);
        ackBuilder.append(fieldDelim);
        ackBuilder.append("ACK");
        ackBuilder.append(componentDelim);
        ackBuilder.append(originalEvent);
        ackBuilder.append(componentDelim);
        ackBuilder.append("ACK");
        ackBuilder.append(fieldDelim);
        ackBuilder.append(timestamp);
        ackBuilder.append(fieldDelim);
        ackBuilder.append(procid);

        if (procidmode != null && procidmode.length() > 0) {
            ackBuilder.append(componentDelim);
            ackBuilder.append(procidmode);
        }

        ackBuilder.append(fieldDelim);
        ackBuilder.append(version);
        ackBuilder.append(segmentDelim);
        ackBuilder.append("MSA");
        ackBuilder.append(fieldDelim);
        ackBuilder.append(acknowledgementCode);
        ackBuilder.append(fieldDelim);
        ackBuilder.append(originalid);
        ackBuilder.append(textMessage);
        ackBuilder.append(errorMessage);
        ackBuilder.append(segmentDelim); // MIRTH-494
        // MSH|^~\\&|{sendapp}|{sendfac}|{recapp}|{recfac}|{timestamp}||ACK|{timestamp}|P|{version}\rMSA|{code}|{originalid}{textmessage}\r

        if (ackIsXML) {
            // return an HL7v2 ack in xml using hapi
            try {
                return SerializerFactory.getHL7Serializer(true, false, false).toXML(ackBuilder.toString());
            } catch (Throwable t) {
                logger.warn("Cannot create the accept ACK for the message (" + message + ") from [" + ackBuilder.toString() + "] as an HL7 message");
                return ackBuilder.toString();
            }
        } else {
            return ackBuilder.toString();
        }
    }
}
