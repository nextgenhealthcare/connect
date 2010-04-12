package com.mirth.connect.connectors.http;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
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

public class HttpMessageDispatcher extends AbstractMessageDispatcher implements QueueEnabledMessageDispatcher {
    private Logger logger = Logger.getLogger(this.getClass());
    private HttpConnector connector;

    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private ConnectorType connectorType = ConnectorType.WRITER;
    private HttpMethodFactory httpMethodFactory = new HttpMethodFactory();

    private HttpClient client = new HttpClient();

    public HttpMessageDispatcher(HttpConnector connector) {
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
            if (connector.isUsePersistentQueues()) {
                connector.putMessageInQueue(event.getEndpoint().getEndpointURI(), mo);
                return;
            } else {
                submitHttpRequest(event.getEndpoint().getEndpointURI().toString(), mo);
            }
        } catch (Exception e) {
            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_404, "Error connecting to HTTP server.", e);
            messageObjectController.setError(mo, Constants.ERROR_404, "Error connecting to HTTP server.", e, null);
            connector.handleException(new Exception(e));
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
    }

    private void submitHttpRequest(String address, MessageObject mo) throws Exception {
        HttpMethod httpMethod = null;

        try {
            httpMethod = httpMethodFactory.createHttpMethod(connector.getDispatcherMethod(), address, replacer.replaceValues(connector.getDispatcherContent(), mo), connector.getDispatcherContentType(), connector.getDispatcherCharset(), connector.isDispatcherMultipart(), replacer.replaceValuesInMap(connector.getDispatcherHeaders(), mo), replacer.replaceValuesInMap(connector.getDispatcherParameters(), mo));

            // authentication

            if (connector.isDispatcherUseAuthentication()) {
                List<String> authenticationPreferences = new ArrayList<String>();

                if ("Digest".equalsIgnoreCase(connector.getDispatcherAuthenticationType())) {
                    authenticationPreferences.add(AuthPolicy.DIGEST);
                    logger.debug("using Digest authentication");
                } else {
                    authenticationPreferences.add(AuthPolicy.BASIC);
                    logger.debug("using Basic authentication");
                }

                client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authenticationPreferences);
                Credentials credentials = new UsernamePasswordCredentials(connector.getDispatcherUsername(), connector.getDispatcherPassword());
                client.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM), credentials);
                httpMethod.setDoAuthentication(true);
                logger.debug("using authentication with credentials: " + credentials);
            }

            // execute the method

            logger.debug("executing method: type=" + httpMethod.getName() + ", uri=" + httpMethod.getURI().toString());
            int statusCode = client.executeMethod(httpMethod);
            logger.debug("received status code: " + statusCode);

            if (statusCode == HttpStatus.SC_OK) {
                String response = null;

                if (connector.isDispatcherIncludeHeadersInResponse()) {
                    response = new HttpMessageConverter().httpResponseToXml(httpMethod.getResponseHeaders(), httpMethod.getResponseBodyAsStream());
                } else {
                    response = httpMethod.getResponseBodyAsString();
                }

                messageObjectController.setSuccess(mo, response, null);

                // send to reply channel

                if ((connector.getDispatcherReplyChannelId() != null) && !connector.getDispatcherReplyChannelId().equals("sink")) {
                    new VMRouter().routeMessageByChannelId(connector.getDispatcherReplyChannelId(), response, true, false);
                }
            } else {
                // throw a new exception with the returned HTTP status
                throw new Exception(httpMethod.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (httpMethod != null) {
                httpMethod.releaseConnection();
            }
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

    @Override
    public boolean sendPayload(QueuedMessage message) throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.BUSY);

        try {
            submitHttpRequest(message.getEndpointUri().toString(), message.getMessageObject());
        } catch (Exception e) {
            if (e.getCause().getClass() == ConnectException.class) {
                logger.warn("Can't connect to the queued endpoint: " + channelController.getChannelName(connector.getChannelId()) + " - " + channelController.getDestinationName(connector.getName()) + " \r\n'" + e.getMessage());
                messageObjectController.setError(message.getMessageObject(), Constants.ERROR_404, "Connection refused", e, null);
                throw e;
            }

            messageObjectController.setError(message.getMessageObject(), Constants.ERROR_404, "Error connecting to web service.", e, null);
            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_404, "Error connecting to web service.", e);
            connector.handleException(new Exception(e));
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }

        return true;
    }
}