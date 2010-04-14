package com.mirth.connect.connectors.http;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mule.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.Response;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.mule.transformers.JavaScriptPostprocessor;

public class HttpMessageReceiver extends AbstractMessageReceiver {
    private Logger logger = Logger.getLogger(this.getClass());
    private HttpConnector connector;
    private ConnectorType connectorType = ConnectorType.LISTENER;
    private JavaScriptPostprocessor postProcessor = new JavaScriptPostprocessor();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();

    private HttpServer server;

    private HttpHandler requestHandler = new AbstractHttpHandler() {
        public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException {
            logger.debug("received HTTP request");
            monitoringController.updateStatus(connector, connectorType, Event.CONNECTED);

            try {
                response.setContentType(connector.getReceiverResponseContentType());
                response.getOutputStream().write(processData(request).getBytes());
                response.setStatus(HttpStatus.SC_OK);
            } catch (Exception e) {
                response.setContentType("text/plain");
                response.getOutputStream().write(ExceptionUtils.getFullStackTrace(e).getBytes());
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            } finally {
                monitoringController.updateStatus(connector, connectorType, Event.DONE);
            }

            request.setHandled(true);
        }
    };

    public HttpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
        super(connector, component, endpoint);
        this.connector = (HttpConnector) connector;
    }

    @Override
    public void doConnect() throws Exception {
        logger.debug("starting HTTP server with address: " + endpoint.getEndpointURI().getUri());
        server = new HttpServer();
        SocketListener listener = new SocketListener();
        listener.setInetAddress(InetAddress.getByName(endpoint.getEndpointURI().getUri().getHost()));
        listener.setPort(endpoint.getEndpointURI().getUri().getPort());
        server.addListener(listener);
        HttpContext context = server.addContext("/");
        context.addHandler(requestHandler);
        server.start();
        monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
    }

    private String processData(HttpRequest request) throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        UMOMessageAdapter adapter = connector.getMessageAdapter(request);

        /*
         * This property is being set so that the adapter knows if it should
         * return the XML encoded headers and body, or just the body.
         */
        adapter.setBooleanProperty("includeHeaders", connector.isReceiverIncludeHeaders());
        
        UMOMessage response = routeMessage(new MuleMessage(adapter), endpoint.isSynchronous());

        if ((response != null) && (response instanceof MuleMessage)) {
            Object payload = response.getPayload();

            if (payload instanceof MessageObject) {
                MessageObject messageObjectResponse = (MessageObject) payload;
                postProcessor.doPostProcess(messageObjectResponse);
                
                if (!connector.getReceiverResponse().equalsIgnoreCase("None")) {
                    return ((Response) messageObjectResponse.getResponseMap().get(connector.getReceiverResponse())).getMessage();
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    @Override
    public void doDisconnect() throws Exception {
        
    }

    @Override
    public void doStop() throws UMOException {
        super.doStop();

        try {
            logger.debug("stopping HTTP server");
            server.stop();
        } catch (Exception e) {
            throw new MuleException(new Message(Messages.FAILED_TO_STOP_X, "HTTP Listener"), e.getCause());
        }
    }

}
