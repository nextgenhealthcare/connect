/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.adaptors;

import java.io.Reader;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.routing.RoutingException;

import com.mirth.connect.model.MessageObject.Protocol;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.converters.SerializerFactory;

public class HL7v2Adaptor extends Adaptor implements BatchAdaptor {
    private Logger logger = Logger.getLogger(this.getClass());

    protected void populateMessage(boolean emptyFilterAndTransformer) throws AdaptorException {
        messageObject.setRawDataProtocol(com.mirth.connect.model.MessageObject.Protocol.HL7V2);
        messageObject.setTransformedDataProtocol(com.mirth.connect.model.MessageObject.Protocol.XML);
        messageObject.setEncodedDataProtocol(com.mirth.connect.model.MessageObject.Protocol.HL7V2);

        try {
            if (emptyFilterAndTransformer) {
                populateMetadataFromEncoded(source);
                messageObject.setEncodedData(source);
            } else {
                String xmlMessage = serializer.toXML(source);
                populateMetadataFromXML(xmlMessage);
                messageObject.setTransformedData(xmlMessage);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public IXMLSerializer<String> getSerializer(Map properties) {
        return SerializerFactory.getSerializer(Protocol.HL7V2, properties);
    }

    public void processBatch(Reader src, Map properties, BatchMessageProcessor dest, UMOEndpoint endpoint) throws MessagingException, UMOException {
        // TODO: The values of these parameters should come from the protocol
        // properties passed to processBatch
        // TODO: src is a character stream, not a byte stream
        byte startOfMessage = (byte) 0x0B;
        byte endOfMessage = (byte) 0x1C;
        byte endOfRecord = (byte) 0x0D;

        Scanner scanner = new Scanner(src);
        scanner.useDelimiter(Pattern.compile("\r\n|\r|\n"));
        StringBuilder message = new StringBuilder();
        char data[] = { (char) startOfMessage, (char) endOfMessage };
        boolean errored = false;

        while (scanner.hasNext()) {
            String line = scanner.next().replaceAll(new String(data, 0, 1), "").replaceAll(new String(data, 1, 1), "").trim();

            if ((line.length() == 0) || line.equals((char) endOfMessage) || line.startsWith("MSH")) {
                if (message.length() > 0) {
                    try {
                        dest.processBatchMessage(message.toString());
                    } catch (UMOException e) {
                        errored = true;
                        logger.error("Error processing message in batch.", e);
                    }

                    message = new StringBuilder();
                }

                while ((line.length() == 0) && scanner.hasNext()) {
                    line = scanner.next();
                }

                if (line.length() > 0) {
                    message.append(line);
                    message.append((char) endOfRecord);
                }
            } else if (line.startsWith("FHS") || line.startsWith("BHS") || line.startsWith("BTS") || line.startsWith("FTS")) {
                // ignore batch headers
            } else {
                message.append(line);
                message.append((char) endOfRecord);
            }
        }

        /*
         * Now that the file has been completely read, make sure to process
         * anything remaining in the message buffer. There could have been lines
         * read in that were not closed with an EOM.
         */
        if (message.length() > 0) {
            try {
                dest.processBatchMessage(message.toString());
            } catch (UMOException e) {
                errored = true;
                logger.error("Error processing message in batch.", e);
            }

            message = new StringBuilder();
        }

        scanner.close();

        if (errored) {
            throw new RoutingException(new Message(Messages.ROUTING_ERROR), new MuleMessage(null), endpoint);
        }
    }
}
