/*
 * $Header:
 /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/AxisMessageDispatcher.java,v
 1.9 2005/06/09 21:15:40 gnt Exp $
 * $Revision: 1.26 $
 * $Date: 2005/10/23 15:18:54 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.soap.axis;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;

import org.apache.axis.AxisFault;
import org.apache.axis.AxisProperties;
import org.apache.axis.Handler;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SimpleChain;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPTransport;
import org.apache.axis.wsdl.gen.Parser;
import org.apache.axis.wsdl.symbolTable.ServiceEntry;
import org.apache.axis.wsdl.symbolTable.SymTabEntry;
import org.mule.config.MuleProperties;
import org.mule.impl.MuleMessage;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.NullPayload;
import org.mule.providers.QueueEnabledMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.soap.axis.extensions.MuleHttpSender;
import org.mule.providers.soap.axis.extensions.MuleSoapHeadersHandler;
import org.mule.providers.soap.axis.extensions.UniversalSender;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.util.TemplateParser;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.QueuedMessage;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.server.util.VMRouter;

/**
 * <code>AxisMessageDispatcher</code> is used to make soap requests via the
 * Axis soap client.
 * 
 * <at> author <a href="mailto:ross.mason@...">Ross Mason</a> <at> version
 * $Revision: 1.26 $
 */
public class AxisMessageDispatcher extends AbstractMessageDispatcher implements QueueEnabledMessageDispatcher {
	private Map<String, Service> services;

	protected SimpleProvider clientConfig;
	private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
	private AlertController alertController = ControllerFactory.getFactory().createAlertController();
	private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	private ConnectorType connectorType = ConnectorType.SENDER;
	private AxisConnector connector;

	public AxisMessageDispatcher(AxisConnector connector) {
		super(connector);
		this.connector = connector;
		AxisProperties.setProperty("axis.doAutoTypes", "true");
		services = new HashMap<String, Service>();
		// Should be loading this from a WSDD but for some reason it is not
		// working for me??
		createClientConfig();
		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}

	public void doDispose() {}

	protected synchronized Service getService(UMOEvent event) throws Exception {
		String wsdlUrl = getWsdlUrl(event);
		Service service = services.get(wsdlUrl);
		if (service != null) {
			services.put(wsdlUrl, service);
		} else {
			service = createService(event);
		}
		return service;
	}

	protected void createClientConfig() {
		clientConfig = new SimpleProvider();
		Handler muleHandler = new MuleSoapHeadersHandler();
		SimpleChain reqHandler = new SimpleChain();
		SimpleChain respHandler = new SimpleChain();
		reqHandler.addHandler(muleHandler);
		respHandler.addHandler(muleHandler);

		// Htpp
		Handler httppivot = new MuleHttpSender();
		Handler httptransport = new SimpleTargetedChain(reqHandler, httppivot, respHandler);
		clientConfig.deployTransport(HTTPTransport.DEFAULT_TRANSPORT_NAME, httptransport);
		Handler pivot = new UniversalSender();
		Handler transport = new SimpleTargetedChain(reqHandler, pivot, respHandler);
		clientConfig.deployTransport("MuleTransport", transport);
		// Handler universalpivot = new UniversalSender();
		// Handler universaltransport = new SimpleTargetedChain(reqHandler,
		// universalpivot, respHandler);
		// clientConfig.deployTransport("https", universaltransport);
		// clientConfig.deployTransport("jms", universaltransport);
		// clientConfig.deployTransport("xmpp", universaltransport);
		// clientConfig.deployTransport("vm", universaltransport);
		// clientConfig.deployTransport("smtp", universaltransport);
		// clientConfig.deployTransport("pop3", universaltransport);
	}

	protected Service createService(UMOEvent event) throws Exception {
		String wsdlUrl = getWsdlUrl(event);
		// If an wsdl url is given use it
		if (wsdlUrl.length() > 0) {
			// Parse the wsdl
			Parser parser = new Parser();
			parser.run(wsdlUrl);
			// Retrieves the defined services
			Map map = parser.getSymbolTable().getHashMap();
			List entries = new ArrayList();
			for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				Vector v = (Vector) entry.getValue();
				for (Iterator it2 = v.iterator(); it2.hasNext();) {
					SymTabEntry e = (SymTabEntry) it2.next();
					if (ServiceEntry.class.isInstance(e)) {
						entries.add(entry.getKey());
					}
				}
			}
			// Currently, only one service should be defined
			if (entries.size() != 1) {
				throw new Exception("Need one and only one service entry, found " + entries.size());
			}
			// Create the axis service
			Service service = new Service(parser, (QName) entries.get(0));
			service.setEngineConfiguration(clientConfig);
			service.setEngine(new AxisClient(clientConfig));
			return service;
		} else {
			// Create a simple axis service without wsdl
			Service service = new Service();
			service.setEngineConfiguration(clientConfig);
			service.setEngine(new AxisClient(clientConfig));
			return service;
		}
	}

	protected String getWsdlUrl(UMOEvent event) {
		Object wsdlUrlProp = event.getProperties().get(AxisConnector.WSDL_URL_PROPERTY);
		String wsdlUrl = "";
		if (wsdlUrlProp != null) {
			wsdlUrl = wsdlUrlProp.toString();
		}
		return wsdlUrl;
	}

	public void doDispatch(UMOEvent event) throws Exception {
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
		if (messageObject == null) {
			return;
		}

		if (connector.isUsePersistentQueues()) {
			connector.putMessageInQueue(event.getEndpoint().getEndpointURI(), messageObject);
		} else {
			try {
				invokeWebService(event.getEndpoint().getEndpointURI(), messageObject);
			} catch (Exception e) {
				alertController.sendAlerts(messageObject.getChannelId(), Constants.ERROR_410, "Error invoking WebService", e);
				messageObjectController.setError(messageObject, Constants.ERROR_410, "Error invoking WebService", e);
				connector.handleException(e);
			} finally {
				monitoringController.updateStatus(connector, connectorType, Event.DONE);
			}
		}
	}

	public boolean sendPayload(QueuedMessage thePayload) throws Exception {
		boolean result = true;

		try {
			invokeWebService(thePayload.getEndpointUri(), thePayload.getMessageObject());
		} catch (Exception e) {
			if (e instanceof AxisFault && ((AxisFault) e).detail != null && ((AxisFault) e).detail.getClass() == ConnectException.class) {
				messageObjectController.setError(thePayload.getMessageObject(), Constants.ERROR_404, "Connection refused", e);
				throw e;
			}
			messageObjectController.setError(thePayload.getMessageObject(), Constants.ERROR_404, e.getMessage(), e);
			alertController.sendAlerts(thePayload.getMessageObject().getChannelId(), Constants.ERROR_404, e.getMessage(), e);
		}

		return result;
	}

	private Object[] invokeWebService(UMOEndpointURI endpointUri, MessageObject messageObject) throws Exception {
		monitoringController.updateStatus(connector, connectorType, Event.BUSY);
		// set the uri for the event
		String uri = "axis:" + replacer.replaceURLValues(endpointUri.toString(), messageObject);
		MuleEndpointURI updatedUri = new MuleEndpointURI(uri);
		String serviceEndpoint = "";
		if (((AxisConnector) connector).getServiceEndpoint() != null && ((AxisConnector) connector).getServiceEndpoint().length() > 0) {
			try {
				serviceEndpoint = replacer.replaceURLValues(URLEncoder.encode(((AxisConnector) connector).getServiceEndpoint(), "UTF-8"), messageObject);
			} catch (UnsupportedEncodingException e) {
				serviceEndpoint = replacer.replaceURLValues(URLEncoder.encode(((AxisConnector) connector).getServiceEndpoint()), messageObject);
			}
		}

		AxisProperties.setProperty("axis.doAutoTypes", "true");
		Object[] args = new Object[0];// getArgs(event);

		Call call = new Call(serviceEndpoint);
		String requestMessage = ((AxisConnector) connector).getSoapEnvelope();
		// Run the template replacer on the xml

		requestMessage = replacer.replaceValues(requestMessage, messageObject);
		Message reqMessage = new Message(requestMessage);
		// Only set the actionURI if we have one explicitly defined
		if (((AxisConnector) connector).getSoapActionURI() != null && ((AxisConnector) connector).getSoapActionURI().length() > 0) {
			call.setSOAPActionURI(replacer.replaceURLValues(((AxisConnector) connector).getSoapActionURI(), messageObject));
		}
		if (serviceEndpoint.length() > 0) {
			call.setTargetEndpointAddress(serviceEndpoint);
		}

		// dont use invokeOneWay here as we are already in a thread pool.
		// Axis creates a new thread for every invoke one way call. nasty!
		// Mule overides the default Axis HttpSender to return immediately if
		// the axis.one.way property is set
		// Change this to FALSE to debug

		call.setProperty("axis.one.way", Boolean.TRUE);

		// get basic authentication info
		if (updatedUri.getUserInfo() != null) {
			call.setProperty(Call.USERNAME_PROPERTY, updatedUri.getUsername());
			call.setProperty(Call.PASSWORD_PROPERTY, updatedUri.getPassword());
			logger.trace("HTTP auth sec detected: [" + updatedUri.getUsername() + "] Clave: [" + updatedUri.getPassword() + "]");
		} else {
			logger.trace("No HTTP auth sec detected");
		}

		// call.invoke(args);
		Object result = call.invoke(reqMessage);
		AxisConnector axisConnector = (AxisConnector) connector;
		if (axisConnector.getReplyChannelId() != null && !axisConnector.getReplyChannelId().equals("") && !axisConnector.getReplyChannelId().equals("sink")) {
			// reply back to channel
			VMRouter router = new VMRouter();
			router.routeMessageByChannelId(axisConnector.getReplyChannelId(), result.toString(), true, true);
		}
		// update the message status to sent
		if (result == null) {
			result = "";
		}
		messageObjectController.setSuccess(messageObject, result.toString());
		Object[] retVal = new Object[2];
		retVal[0] = result;
		retVal[1] = call.getMessageContext();
		logger.debug("WS (" + endpointUri.toString() + " returned \r\n[" + result + "]");
		return retVal;
	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		Object[] results = null;
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
		if (messageObject == null) {
			return null;
		}
		if (connector.isUsePersistentQueues()) {
			try {
				connector.putMessageInQueue(event.getEndpoint().getEndpointURI(), messageObject);
			} catch (Exception exq) {
				String exceptionMessage = "Can't save payload to queue";
				logger.error("Can't save payload to queue\r\n\t " + exq);
				messageObjectController.setError(messageObject, Constants.ERROR_404, exceptionMessage, exq);
				alertController.sendAlerts(messageObject.getChannelId(), Constants.ERROR_404, exceptionMessage, exq);
			}
			return null;
		} else {
			try {
				results = invokeWebService(event.getEndpoint().getEndpointURI(), messageObject);

				Object result = null;
				if (results != null) {
					result = results[0];

				}
				if (result == null) {
					return null;
				} else {
					// Return the messageObject here
					UMOMessage resultMessage = new MuleMessage(messageObject, event.getProperties());
					setMessageContextProperties(resultMessage, (MessageContext) results[1]);
					return resultMessage;
				}
			} catch (Exception e) {
				alertController.sendAlerts(messageObject.getChannelId(), Constants.ERROR_410, "Error invoking WebService", e);
				messageObjectController.setError(messageObject, Constants.ERROR_410, "Error invoking WebService", e);
				connector.handleException(e);
				return null;
			} finally {
				monitoringController.updateStatus(connector, connectorType, Event.DONE);
			}
		}
	}

	private void setMessageContextProperties(UMOMessage message, MessageContext ctx) {
		Object temp = ctx.getProperty(MuleProperties.MULE_CORRELATION_ID_PROPERTY);
		if (temp != null && !"".equals(temp.toString())) {
			message.setCorrelationId(temp.toString());
		}
		temp = ctx.getProperty(MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY);
		if (temp != null && !"".equals(temp.toString())) {
			message.setCorrelationGroupSize(Integer.parseInt(temp.toString()));
		}
		temp = ctx.getProperty(MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY);
		if (temp != null && !"".equals(temp.toString())) {
			message.setCorrelationSequence(Integer.parseInt(temp.toString()));
		}
		temp = ctx.getProperty(MuleProperties.MULE_REPLY_TO_PROPERTY);
		if (temp != null && !"".equals(temp.toString())) {
			message.setReplyTo(temp.toString());
		}
	}

	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		Service service = new Service();
		// service.setEngineConfiguration(clientConfig);
		service.setEngine(new AxisClient(clientConfig));
		Call call = new Call(service);
		call.setSOAPActionURI(endpointUri.toString());
		call.setTargetEndpointAddress(endpointUri.toString());

		String method = (String) endpointUri.getParams().remove(MuleProperties.MULE_METHOD_PROPERTY);
		call.setOperationName(method);
		Properties params = endpointUri.getUserParams();
		String args[] = new String[params.size()];
		int i = 0;
		for (Iterator iterator = params.values().iterator(); iterator.hasNext(); i++) {
			args[i] = iterator.next().toString();
		}

		call.setOperationName(method);
		UMOEndpoint ep = MuleEndpoint.getOrCreateEndpointForUri(endpointUri, UMOEndpoint.ENDPOINT_TYPE_SENDER);
		ep.initialise();
		call.setProperty(MuleProperties.MULE_ENDPOINT_PROPERTY, ep);
		Object result = call.invoke(method, args);
		return createMessage(result, call);
	}

	public UMOMessage receive(String endpoint, Object[] args) throws Exception {
		Service service = new Service();
		service.setEngineConfiguration(clientConfig);
		service.setEngine(new AxisClient(clientConfig));
		Call call = new Call(service);

		call.setSOAPActionURI(endpoint);
		call.setTargetEndpointAddress(endpoint);

		if (!endpoint.startsWith("axis:")) {
			endpoint = "axis:" + endpoint;
		}
		UMOEndpointURI ep = new MuleEndpointURI(endpoint);
		String method = (String) ep.getParams().remove("method");
		call.setOperationName(method);

		call.setOperationName(method);
		Object result = call.invoke(method, args);
		return createMessage(result, call);
	}

	public UMOMessage receive(String endpoint, SOAPEnvelope envelope) throws Exception {
		Service service = new Service();
		service.setEngineConfiguration(clientConfig);
		service.setEngine(new AxisClient(clientConfig));
		Call call = new Call(service);

		call.setSOAPActionURI(endpoint);
		call.setTargetEndpointAddress(endpoint);
		Object result = call.invoke(new Message(envelope));
		return createMessage(result, call);
	}

	protected UMOMessage createMessage(Object result, Call call) {
		if (result == null) {
			result = new NullPayload();
		}
		Map<Object, Object> props = new HashMap<Object, Object>();
		Iterator iter = call.getMessageContext().getPropertyNames();
		Object key;
		while (iter.hasNext()) {
			key = iter.next();
			props.put(key, call.getMessageContext().getProperty(key.toString()));
		}
		props.put("soap.message", call.getMessageContext().getMessage());
		call.clearHeaders();
		call.clearOperation();
		return new MuleMessage(result, props);
	}

	public Object getDelegateSession() throws UMOException {
		return null;
	}

	public String parseSoapAction(String soapAction, Call call, UMOEvent event) {

		UMOEndpointURI endpointURI = event.getEndpoint().getEndpointURI();
		Map<Object, Object> properties = new HashMap<Object, Object>(event.getProperties());
		properties.put("method", call.getOperationName().getLocalPart());
		properties.put("methodNamespace", call.getOperationName().getNamespaceURI());
		properties.put("address", endpointURI.getAddress());
		properties.put("scheme", endpointURI.getScheme());
		properties.put("host", endpointURI.getHost());
		properties.put("port", String.valueOf(endpointURI.getPort()));
		properties.put("path", endpointURI.getPath());
		properties.put("hostInfo", endpointURI.getScheme() + "://" + endpointURI.getHost() + (endpointURI.getPort() > -1 ? ":" + String.valueOf(endpointURI.getPort()) : ""));
		if (event.getComponent() != null) {
			properties.put("serviceName", event.getComponent().getDescriptor().getName());
		}

		TemplateParser tp = TemplateParser.createAntStyleParser();
		soapAction = tp.parse(properties, soapAction);

		if (logger.isDebugEnabled()) {
			logger.debug("SoapAction for this call is: " + soapAction);
		}
		return soapAction;
	}
}