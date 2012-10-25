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

import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;

/*
 * Log the whole SOAP message
 */
public class LoggingSOAPHandler implements SOAPHandler<SOAPMessageContext> {

    private Logger logger = Logger.getLogger(this.getClass());
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.LISTENER;

    private WebServiceMessageReceiver webServiceMessageReceiver;

    public LoggingSOAPHandler(WebServiceMessageReceiver webServiceMessageReceiver) {
        this.webServiceMessageReceiver = webServiceMessageReceiver;
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public void close(MessageContext mc) {
        logger.debug("Web Service connection closed.");
        monitoringController.updateStatus(webServiceMessageReceiver.getChannelId(), webServiceMessageReceiver.getMetaDataId(), connectorType, Event.DONE);
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        try {
            Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            if (!outbound) {
                logger.debug("Web Service message received.");
                monitoringController.updateStatus(webServiceMessageReceiver.getChannelId(), webServiceMessageReceiver.getMetaDataId(), connectorType, Event.CONNECTED);
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
