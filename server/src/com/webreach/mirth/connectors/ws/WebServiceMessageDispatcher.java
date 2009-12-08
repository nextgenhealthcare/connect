package com.webreach.mirth.connectors.ws;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import javax.xml.namespace.QName;
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

import org.apache.log4j.Logger;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.server.util.VMRouter;

public class WebServiceMessageDispatcher extends AbstractMessageDispatcher {
    private Logger logger = Logger.getLogger(this.getClass());
    protected WebServiceConnector connector;
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
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
            URL endpointUrl = new URL(connector.getDispatcherWsdlUrl());
            QName serviceName = QName.valueOf(connector.getDispatcherService());
            QName portName = QName.valueOf(connector.getDispatcherPort());

            // create the service and dispatch
            logger.debug("Creating web service: url=" + endpointUrl.toString() + ", service=" + serviceName + ", port=" + portName);
            Service service = Service.create(endpointUrl, serviceName);
            Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Service.Mode.MESSAGE);
            
            if (connector.isDispatcherUseAuthentication()) {
                dispatch.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, connector.getDispatcherUsername());
                dispatch.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, connector.getDispatcherPassword());
                logger.debug("Using authentication: username=" + connector.getDispatcherUsername() + ", password length=" + connector.getDispatcherPassword().length());
            }

            // build the message
            logger.debug("Creating SOAP envelope.");
            String content = replacer.replaceValues(connector.getDispatcherEnvelope(), mo);
            Source message = new StreamSource(new StringReader(content));
            
            // make the call
            logger.debug("Invoking web service...");
            Source result = dispatch.invoke(message);
            logger.debug("Finished invoking web service, got result.");
            
            // process the result
            String response = sourceToXmlString(result);
            messageObjectController.setSuccess(mo, response, null);
            
            // send to reply channel
            if (connector.getDispatcherReplyChannelId() != null && !connector.getDispatcherReplyChannelId().equals("sink")) {
                new VMRouter().routeMessageByChannelId(connector.getDispatcherReplyChannelId(), response, true, false);
            }
        } catch (Throwable e) {
            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_410, "Error connecting to web service.", e);
            messageObjectController.setError(mo, Constants.ERROR_410, "Error connecting to web service.", e, null);
            connector.handleException(new Exception(e));
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
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

}
