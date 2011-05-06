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
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
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
import javax.xml.ws.Service;
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

    /*
     * Dispatch object used for pooling the soap connection, and the current
     * properties used to create the dispatch object
     */
    private Dispatch<SOAPMessage> dispatch = null;
    private String currentWsdlUrl = null;
    private String currentUsername = null;
    private String currentPassword = null;
    private String currentServiceName = null;
    private String currentPortName = null;

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
        /*
         * Initialize the dispatch object if it hasn't been initialized yet, or
         * create a new one if the connector properties have changed due to
         * variables.
         */
        createDispatch(mo);

        SOAPBinding soapBinding = (SOAPBinding) dispatch.getBinding();

        if (connector.isDispatcherUseAuthentication()) {
            dispatch.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, currentUsername);
            dispatch.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, currentPassword);
            logger.debug("Using authentication: username=" + currentUsername + ", password length=" + currentPassword.length());
        }

        // See: http://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383528
        String soapAction = replacer.replaceValues(connector.getDispatcherSoapAction(), mo);

        if (StringUtils.isNotEmpty(soapAction)) {
            dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);
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
                String attachmentContentType = replacer.replaceValues(attachmentTypes.get(i), mo);
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

    private void createDispatch(MessageObject mo) throws Exception {
        String wsdlUrl = replacer.replaceValues(connector.getDispatcherWsdlUrl(), mo);
        String username = replacer.replaceValues(connector.getDispatcherUsername(), mo);
        String password = replacer.replaceValues(connector.getDispatcherPassword(), mo);
        String serviceName = replacer.replaceValues(connector.getDispatcherService(), mo);
        String portName = replacer.replaceValues(connector.getDispatcherPort(), mo);

        /*
         * The dispatch needs to be created if it hasn't been created yet
         * (null). It needs to be recreated if any of the above variables are
         * different than what were used to create the current dispatch object.
         * This could happen if variables are being used for these properties.
         */
        if (dispatch == null || !StringUtils.equals(wsdlUrl, currentWsdlUrl) || !StringUtils.equals(username, currentUsername) || !StringUtils.equals(password, currentPassword) || !StringUtils.equals(serviceName, currentServiceName) || !StringUtils.equals(portName, currentPortName)) {
            currentWsdlUrl = wsdlUrl;
            currentUsername = username;
            currentPassword = password;
            currentServiceName = serviceName;
            currentPortName = portName;

            URL endpointUrl = WebServiceUtil.getWsdlUrl(wsdlUrl, username, password);
            QName serviceQName = QName.valueOf(serviceName);
            QName portQName = QName.valueOf(portName);

            // create the service and dispatch
            logger.debug("Creating web service: url=" + endpointUrl.toString() + ", service=" + serviceQName + ", port=" + portQName);
            Service service = Service.create(endpointUrl, serviceQName);

            dispatch = service.createDispatch(portQName, SOAPMessage.class, Service.Mode.MESSAGE);
        }
    }
}
