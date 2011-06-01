/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ConnectException;
import java.util.List;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.QueueEnabledMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.QueuedMessage;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.VMRouter;

public class WebServiceMessageDispatcher extends AbstractMessageDispatcher implements QueueEnabledMessageDispatcher {
    private Logger logger = Logger.getLogger(this.getClass());
    protected WebServiceConnector connector;
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private ConnectorType connectorType = ConnectorType.WRITER;

    public WebServiceMessageDispatcher(WebServiceConnector connector) {
        super(connector);
        this.connector = connector;
    }

    public void doDispatch(UMOEvent event) throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        MessageObject mo = messageObjectController.getMessageObjectFromEvent(event);

        if (mo == null) {
            return;
        }

        try {
            if (connector.isUsePersistentQueues()) {
                connector.putMessageInQueue(event.getEndpoint().getEndpointURI(), mo);
                return;
            } else {
                processMessage(mo);
            }
        } catch (Exception e) {
            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_410, "Error connecting to web service.", e);
            messageObjectController.setError(mo, Constants.ERROR_410, "Error connecting to web service.", e, null);
            connector.handleException(new Exception(e));
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
    }

    private void processMessage(MessageObject mo) throws Exception {
        // Get the dispatch from the pool;
        Dispatch<SOAPMessage> dispatch = connector.getDispatch();

        SOAPBinding soapBinding = (SOAPBinding) dispatch.getBinding();

        if (connector.isDispatcherUseAuthentication()) {
            dispatch.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, connector.getDispatcherUsername());
            dispatch.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, connector.getDispatcherPassword());
            logger.debug("Using authentication: username=" + connector.getDispatcherUsername() + ", password length=" + connector.getDispatcherPassword().length());
        }

        // See: http://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383528
        if (StringUtils.isNotEmpty(connector.getDispatcherSoapAction())) {
            dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, connector.getDispatcherSoapAction());
        }

        // build the message
        logger.debug("Creating SOAP envelope.");
        String content = replacer.replaceValues(connector.getDispatcherEnvelope(), mo);
        Source source = new StreamSource(new StringReader(content));
        SOAPMessage message = soapBinding.getMessageFactory().createMessage();
        message.getSOAPPart().setContent(source);

        if (connector.isDispatcherUseMtom()) {
            soapBinding.setMTOMEnabled(true);

            List<String> attachmentIds = connector.getDispatcherAttachmentNames();
            List<String> attachmentContents = connector.getDispatcherAttachmentContents();
            List<String> attachmentTypes = connector.getDispatcherAttachmentTypes();

            for (int i = 0; i < attachmentIds.size(); i++) {
                String attachmentContentId = replacer.replaceValues(attachmentIds.get(i), mo);
                String attachmentContentType = attachmentTypes.get(i);
                String attachmentContent = replacer.replaceValues(attachmentContents.get(i), mo);

                AttachmentPart attachment = message.createAttachmentPart();
                attachment.setBase64Content(new ByteArrayInputStream(attachmentContent.getBytes("UTF-8")), attachmentContentType);
                attachment.setContentId(attachmentContentId);
                message.addAttachmentPart(attachment);
            }
        }

        message.saveChanges();

        // make the call
        String response = null;
        if (connector.isDispatcherOneWay()) {
            logger.debug("Invoking one way service...");
            dispatch.invokeOneWay(message);
            response = "Invoked one way operation successfully.";
        } else {
            logger.debug("Invoking web service...");
            SOAPMessage result = dispatch.invoke(message);
            response = sourceToXmlString(result.getSOAPPart().getContent());
        }
        logger.debug("Finished invoking web service, got result.");

        // process the result
        messageObjectController.setSuccess(mo, response, null);

        // send to reply channel
        if (connector.getDispatcherReplyChannelId() != null && !connector.getDispatcherReplyChannelId().equals("sink")) {
            new VMRouter().routeMessageByChannelId(connector.getDispatcherReplyChannelId(), response, true);
        }
    }

    private String sourceToXmlString(Source source) throws TransformerConfigurationException, TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        Writer writer = new StringWriter();
        transformer.transform(source, new StreamResult(writer));
        return writer.toString();
    }

    public void doDispose() {

    }

    public UMOMessage doSend(UMOEvent event) throws Exception {
        doDispatch(event);
        return event.getMessage();
    }

    public Object getDelegateSession() throws UMOException {
        return null;
    }

    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
        return null;
    }

    public boolean sendPayload(QueuedMessage thePayload) throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        try {
            processMessage(thePayload.getMessageObject());
        } catch (Exception e) {
            /*
             * If there's an exception getting the dispatch WSDL from the
             * connector, ConnectException will be the root exception class. If
             * the dispatch has already been retrieved and then the destination
             * goes down, ConnectException will be inside of a
             * ClientTransportException.
             */
            if ((e.getClass() == ConnectException.class) || ((e.getCause() != null) && (e.getCause().getClass() == ConnectException.class))) {
                logger.warn("Can't connect to the queued endpoint: " + channelController.getDeployedChannelById(connector.getChannelId()).getName() + " - " + channelController.getDeployedDestinationName(connector.getName()) + " \r\n'" + e.getMessage());
                messageObjectController.setError(thePayload.getMessageObject(), Constants.ERROR_410, "Connection refused", e, null);
                throw e;
            }
            messageObjectController.setError(thePayload.getMessageObject(), Constants.ERROR_410, "Error connecting to web service.", e, null);
            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_410, "Error connecting to web service.", e);
            connector.handleException(new Exception(e));
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }

        return true;
    }

}
