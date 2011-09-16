/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.uima;

import org.apache.uima.cas.CAS;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;

public class UimaMessageDispatcher extends AbstractMessageDispatcher {
    protected UimaConnector connector;
    private final MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private final MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private final AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private final TemplateValueReplacer replacer = new TemplateValueReplacer();
    private final ConnectorType connectorType = ConnectorType.WRITER;

    public UimaMessageDispatcher(UimaConnector connector) {
        super(connector);
        this.connector = connector;
    }

    @Override
    public void doDispatch(UMOEvent event) throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        MessageObject mo = messageObjectController.getMessageObjectFromEvent(event);
        if (mo == null) {
            return;
        }

        try {
            
            String template = connector.getTemplate();
            if (template != null) {
                template = replacer.replaceValues(template, mo);
            } else {
                template = mo.getEncodedData();
            }
            
            CAS temp = this.connector.getEngine().getCAS();
            
            if (temp == null) {
                throw new Exception("CAS was null");
            }
            
            // set the message body
            temp.setDocumentText(template);
            
            // set the language
            temp.setDocumentLanguage("en");
            
            // send the CAS to uima
            String casReferenceId = this.connector.getEngine().sendCAS(temp);

            String result = "DONE! "+casReferenceId;
            messageObjectController.setSuccess(mo, result, null);
        } catch (Exception e) {
            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_400, "Error connecting to UIMA server. ", e);
            messageObjectController.setError(mo, Constants.ERROR_400, "Error connecting to UIMA server. ", e, null);
            connector.handleException(new Exception(e));
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
    }

    @Override
    public void doDispose() {

    }

    @Override
    public UMOMessage doSend(UMOEvent event) throws Exception {
        doDispatch(event);
        return event.getMessage();
    }

    @Override
    public Object getDelegateSession() throws UMOException {
        return null;
    }

    @Override
    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
        return null;
    }
}
