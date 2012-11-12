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
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ConnectException;
import java.net.URI;
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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.builders.ErrorMessageBuilder;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class WebServiceMessageDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    protected WebServiceDispatcherProperties connectorProperties;
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

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

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (WebServiceDispatcherProperties) getConnectorProperties();
    }

    @Override
    public void onUndeploy() throws UndeployException {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStart() throws StartException {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStop() throws StopException {
        // TODO Auto-generated method stub
    }

    private String sourceToXmlString(Source source) throws TransformerConfigurationException, TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        Writer writer = new StringWriter();
        transformer.transform(source, new StreamResult(writer));
        return writer.toString();
    }

    private void createDispatch(WebServiceDispatcherProperties webServiceDispatcherProperties) throws Exception {
        String wsdlUrl = webServiceDispatcherProperties.getWsdlUrl();
        String username = webServiceDispatcherProperties.getUsername();
        String password = webServiceDispatcherProperties.getPassword();
        String serviceName = webServiceDispatcherProperties.getService();
        String portName = webServiceDispatcherProperties.getPort();

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

            URL endpointUrl = getWsdlUrl(wsdlUrl, username, password);
            QName serviceQName = QName.valueOf(serviceName);
            QName portQName = QName.valueOf(portName);

            // create the service and dispatch
            logger.debug("Creating web service: url=" + endpointUrl.toString() + ", service=" + serviceQName + ", port=" + portQName);
            Service service = Service.create(endpointUrl, serviceQName);

            dispatch = service.createDispatch(portQName, SOAPMessage.class, Service.Mode.MESSAGE);
        }
    }

    /**
     * Returns the URL for the passed in String. If the URL requires
     * authentication, then the WSDL is saved as a temp file and the URL for
     * that file is returned.
     * 
     * @param wsdlUrl
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    private URL getWsdlUrl(String wsdlUrl, String username, String password) throws Exception {
        URI uri = new URI(wsdlUrl);

        // If the URL points to file, just return it
        if (!uri.getScheme().equalsIgnoreCase("file")) {
            HttpClient client = new HttpClient();
            HttpMethod method = new GetMethod(wsdlUrl);

            int status = client.executeMethod(method);
            if ((status == HttpStatus.SC_UNAUTHORIZED) && (username != null) && (password != null)) {
                client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
                status = client.executeMethod(method);

                if (status == HttpStatus.SC_OK) {
                    String wsdl = method.getResponseBodyAsString();
                    File tempFile = File.createTempFile("WebServiceSender", ".wsdl");
                    tempFile.deleteOnExit();

                    FileUtils.writeStringToFile(tempFile, wsdl);

                    return tempFile.toURI().toURL();
                }
            }
        }

        return uri.toURL();
    }

    @Override
    public ConnectorProperties getReplacedConnectorProperties(ConnectorMessage connectorMessage) {
        WebServiceDispatcherProperties webServiceDispatcherProperties = (WebServiceDispatcherProperties) SerializationUtils.clone(connectorProperties);

        // Replace all values in connector properties
        webServiceDispatcherProperties.setWsdlUrl(replacer.replaceValues(webServiceDispatcherProperties.getWsdlUrl(), connectorMessage));
        webServiceDispatcherProperties.setUsername(replacer.replaceValues(webServiceDispatcherProperties.getUsername(), connectorMessage));
        webServiceDispatcherProperties.setPassword(replacer.replaceValues(webServiceDispatcherProperties.getPassword(), connectorMessage));
        webServiceDispatcherProperties.setService(replacer.replaceValues(webServiceDispatcherProperties.getService(), connectorMessage));
        webServiceDispatcherProperties.setPort(replacer.replaceValues(webServiceDispatcherProperties.getPort(), connectorMessage));

        webServiceDispatcherProperties.setSoapAction(replacer.replaceValues(webServiceDispatcherProperties.getSoapAction(), connectorMessage));
        webServiceDispatcherProperties.setEnvelope(replacer.replaceValues(webServiceDispatcherProperties.getEnvelope(), connectorMessage));

        if (webServiceDispatcherProperties.isUseMtom()) {
            replacer.replaceValuesInList(webServiceDispatcherProperties.getAttachmentNames(), connectorMessage);
            replacer.replaceValuesInList(webServiceDispatcherProperties.getAttachmentContents(), connectorMessage);
            replacer.replaceValuesInList(webServiceDispatcherProperties.getAttachmentTypes(), connectorMessage);
        }

        return webServiceDispatcherProperties;
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        WebServiceDispatcherProperties webServiceDispatcherProperties = (WebServiceDispatcherProperties) connectorProperties;
        String responseData = null;
        String responseError = null;
        Status responseStatus = Status.QUEUED;

        try {
            /*
             * Initialize the dispatch object if it hasn't been initialized yet,
             * or create a new one if the connector properties have changed due
             * to variables.
             */
            createDispatch(webServiceDispatcherProperties);

            SOAPBinding soapBinding = (SOAPBinding) dispatch.getBinding();

            if (webServiceDispatcherProperties.isUseAuthentication()) {
                dispatch.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, currentUsername);
                dispatch.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, currentPassword);
                logger.debug("Using authentication: username=" + currentUsername + ", password length=" + currentPassword.length());
            }

            // See: http://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383528
            String soapAction = webServiceDispatcherProperties.getSoapAction();

            if (StringUtils.isNotEmpty(soapAction)) {
                dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, true); // MIRTH-2109
                dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);
            }

            // build the message
            logger.debug("Creating SOAP envelope.");
            String content = webServiceDispatcherProperties.getEnvelope();
            Source source = new StreamSource(new StringReader(content));
            SOAPMessage message = soapBinding.getMessageFactory().createMessage();
            message.getSOAPPart().setContent(source);

            if (webServiceDispatcherProperties.isUseMtom()) {
                soapBinding.setMTOMEnabled(true);

                List<String> attachmentIds = webServiceDispatcherProperties.getAttachmentNames();
                List<String> attachmentContents = webServiceDispatcherProperties.getAttachmentContents();
                List<String> attachmentTypes = webServiceDispatcherProperties.getAttachmentTypes();

                for (int i = 0; i < attachmentIds.size(); i++) {
                    String attachmentContentId = attachmentIds.get(i);
                    String attachmentContentType = attachmentTypes.get(i);
                    String attachmentContent = attachmentContents.get(i);

                    AttachmentPart attachment = message.createAttachmentPart();
                    attachment.setBase64Content(new ByteArrayInputStream(attachmentContent.getBytes("UTF-8")), attachmentContentType);
                    attachment.setContentId(attachmentContentId);
                    message.addAttachmentPart(attachment);
                }
            }

            message.saveChanges();

            try {
                // Make the call
                if (webServiceDispatcherProperties.isOneWay()) {
                    logger.debug("Invoking one way service...");
                    dispatch.invokeOneWay(message);
                    responseData = "Invoked one way operation successfully.";
                } else {
                    logger.debug("Invoking web service...");
                    SOAPMessage result = dispatch.invoke(message);
                    responseData = sourceToXmlString(result.getSOAPPart().getContent());
                }
                logger.debug("Finished invoking web service, got result.");

                // Automatically accept message; leave it up to the response transformer to find SOAP faults
                responseStatus = Status.SENT;
            } catch (Exception e) {
                // Leave the response status as QUEUED for ConnectException, otherwise ERROR
                if ((e.getClass() == ConnectException.class) || ((e.getCause() != null) && (e.getCause().getClass() == ConnectException.class))) {
                    alertController.sendAlerts(getChannelId(), Constants.ERROR_410, "Connection refused.", e);
                } else {
                    responseData = ErrorMessageBuilder.buildErrorResponse("Error invoking web service", e);
                    responseError = ErrorMessageBuilder.buildErrorMessage(Constants.ERROR_410, "Error invoking web service", e);
                    alertController.sendAlerts(getChannelId(), Constants.ERROR_410, "Error invoking web service.", e);
                }
            }

        } catch (Exception e) {
            // Set the response status to ERROR if it failed to create the dispatch
            responseData = ErrorMessageBuilder.buildErrorResponse("Error creating web service dispatch", e);
            responseError = ErrorMessageBuilder.buildErrorMessage(Constants.ERROR_410, "Error creating web service dispatch", e);
            alertController.sendAlerts(getChannelId(), Constants.ERROR_410, "Error creating web service dispatch.", e);
        }

        return new Response(responseStatus, responseData, responseError);
    }
}
