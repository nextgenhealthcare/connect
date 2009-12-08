package com.webreach.mirth.connectors.ws;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.log4j.Logger;

import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;

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
        monitoringController.updateStatus(webServiceMessageReceiver.connector, connectorType, Event.DONE);
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        try {
            Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            if (!outbound) {
                logger.debug("Web Service message received.");
                monitoringController.updateStatus(webServiceMessageReceiver.connector, connectorType, Event.CONNECTED);
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
