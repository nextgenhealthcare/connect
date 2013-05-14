/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.event.ConnectorEventType;
import com.mirth.connect.donkey.server.event.ConnectorEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;

/*
 * Log the whole SOAP message
 */
public class LoggingSOAPHandler implements SOAPHandler<SOAPMessageContext> {

    private Logger logger = Logger.getLogger(this.getClass());
    private EventController eventController = ControllerFactory.getFactory().createEventController();

    private WebServiceReceiver webServiceReceiver;

    public LoggingSOAPHandler(WebServiceReceiver webServiceReceiver) {
        this.webServiceReceiver = webServiceReceiver;
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public void close(MessageContext mc) {
        logger.debug("Web Service connection closed.");
        eventController.dispatchEvent(new ConnectorEvent(webServiceReceiver.getChannelId(), webServiceReceiver.getMetaDataId(), ConnectorEventType.IDLE));
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        try {
            Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            if (!outbound) {
                logger.debug("Web Service message received.");
                eventController.dispatchEvent(new ConnectorEvent(webServiceReceiver.getChannelId(), webServiceReceiver.getMetaDataId(), ConnectorEventType.CONNECTED));
            } else {
                logger.debug("Web Service returning response.");
            }
            smc.getMessage();
        } catch (Exception e) {
            logger.error("Error handling SOAP message", e);
            return false;
        }
        return true;
    }

}
