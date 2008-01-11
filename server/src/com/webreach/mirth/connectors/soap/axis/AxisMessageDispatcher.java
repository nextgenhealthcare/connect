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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;

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
import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.transport.http.HTTPTransport;
import org.apache.axis.wsdl.fromJava.Namespaces;
import org.apache.axis.wsdl.fromJava.Types;
import org.apache.axis.wsdl.gen.Parser;
import org.apache.axis.wsdl.symbolTable.ServiceEntry;
import org.apache.axis.wsdl.symbolTable.SymTabEntry;
import org.mule.config.MuleProperties;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.NullPayload;
import org.mule.providers.ParameterValueReplacer;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.soap.NamedParameter;
import org.mule.providers.soap.SoapMethod;
import org.mule.providers.soap.axis.extensions.MuleHttpSender;
import org.mule.providers.soap.axis.extensions.MuleSoapHeadersHandler;
import org.mule.providers.soap.axis.extensions.UniversalSender;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.DispatchException;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.BeanUtils;
import org.mule.util.TemplateParser;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.ws.WSParameter;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
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
public class AxisMessageDispatcher extends AbstractMessageDispatcher {
	private Map<String, Service> services;

	private Map<String, SoapMethod> callParameters;

	protected SimpleProvider clientConfig;
	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private AlertController alertController = AlertController.getInstance();
	private MonitoringController monitoringController = MonitoringController.getInstance();
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	private ConnectorType connectorType= ConnectorType.SENDER;
	public AxisMessageDispatcher(AxisConnector connector) throws UMOException {
		super(connector);
		AxisProperties.setProperty("axis.doAutoTypes", "true");
		services = new HashMap<String, Service>();
		// Should be loading this from a WSDD but for some reason it is not
		// working for me??
		createClientConfig();
		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}

	public void doDispose() {
	}

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
		invokeWebService(event, messageObject);	
	}

	private Object[] invokeWebService(UMOEvent event, MessageObject messageObject) throws Exception {
		try{
			monitoringController.updateStatus(connector, connectorType, Event.BUSY);
			//set the uri for the event
			String uri = "axis:" + replacer.replaceURLValues(event.getEndpoint().getEndpointURI().toString(), messageObject);
			String serviceEndpoint = "";
			if (((AxisConnector) connector).getServiceEndpoint() != null && ((AxisConnector) connector).getServiceEndpoint().length() > 0){
				try{
					serviceEndpoint=replacer.replaceURLValues(URLEncoder.encode(((AxisConnector) connector).getServiceEndpoint(),"UTF-8"), messageObject);
				}catch(UnsupportedEncodingException e){
					serviceEndpoint=replacer.replaceURLValues(URLEncoder.encode(((AxisConnector) connector).getServiceEndpoint()), messageObject);
				}
			}
			
			event.getEndpoint().setEndpointURI(new MuleEndpointURI(uri));
			AxisProperties.setProperty("axis.doAutoTypes", "true");
			Object[] args = new Object[0];// getArgs(event);
			Call call = getCall(event, args);
			//note - this is rather strange, as we have a valid call above
			//however the call about does not work
			call = new Call(serviceEndpoint);
			String requestMessage = ((AxisConnector) connector).getSoapEnvelope();
			// Run the template replacer on the xml
			
			requestMessage = replacer.replaceValues(requestMessage, messageObject);
			Message reqMessage = new Message(requestMessage);
			// Only set the actionURI if we have one explicitly defined
			if (((AxisConnector) connector).getSoapActionURI() != null && ((AxisConnector) connector).getSoapActionURI().length() > 0){
				call.setSOAPActionURI(replacer.replaceURLValues(((AxisConnector) connector).getSoapActionURI(), messageObject));
			}
			if (serviceEndpoint.length() > 0){
				call.setTargetEndpointAddress(serviceEndpoint);
			}
	
			// dont use invokeOneWay here as we are already in a thread pool.
			// Axis creates a new thread for every invoke one way call. nasty!
			// Mule overides the default Axis HttpSender to return immediately if
			// the axis.one.way property is set
			// Change this to FALSE to debug
			
			call.setProperty("axis.one.way", Boolean.TRUE);
			call.setProperty(MuleProperties.MULE_EVENT_PROPERTY, event);
			//ast: Get possible user auth info from endpoint
			// this only resolves basic and NTLM
			// For auth/digest protocol://user:pass@uri
			// For NTLM protocol://domain%5Cuser:pass@uri
			// (%5C is the urlencoding for '\')
			UMOEndpointURI endpointUri = event.getEndpoint().getEndpointURI();
			if (endpointUri.getUserInfo() != null) {
				call.setProperty(Call.USERNAME_PROPERTY, endpointUri.getUsername());
				call.setProperty(Call.PASSWORD_PROPERTY, endpointUri.getPassword());
				logger.trace("HTTP auth sec detected: ["+endpointUri.getUsername()+"] Clave: ["+endpointUri.getPassword()+"]");			
			}else{
				logger.trace("No HTTP auth sec detected");
			}
			
			
			// call.invoke(args);
			Object result = call.invoke(reqMessage);
			AxisConnector axisConnector = (AxisConnector)connector;
			if (axisConnector.getReplyChannelId() != null && !axisConnector.getReplyChannelId().equals("")  && !axisConnector.getReplyChannelId().equals("sink")){
				//reply back to channel
				VMRouter router = new VMRouter();
				router.routeMessageByChannelId(axisConnector.getReplyChannelId(), result.toString(), true, true);
			}
			// update the message status to sent
			if (result == null){
				result = "";
			}
			messageObjectController.setSuccess(messageObject, result.toString());
			Object[] retVal = new Object[2];
			retVal[0] = result;
			retVal[1] = call.getMessageContext();
			logger.debug("WS ("+endpointUri.toString()+" returned \r\n["+result+"]");
			return retVal;
		}catch(Exception e){
			alertController.sendAlerts(messageObject.getChannelId(), Constants.ERROR_410, "Error invoking WebService", e);
			messageObjectController.setError(messageObject, Constants.ERROR_410, "Error invoking WebService", e);
			connector.handleException(e);
			return null;
		}finally{
			monitoringController.updateStatus(connector, connectorType, Event.DONE);
		}

	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		Object[] results = null;
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
		if (messageObject == null) {
			return null;
		}
		results = invokeWebService(event, messageObject);	
		
		Object result = null;
		if (results != null){
			result = results[0];
			
		}
		if (result == null) {
			return null;
		} else {
			//Return the messageObject here
			UMOMessage resultMessage = new MuleMessage(messageObject, event.getProperties());
			setMessageContextProperties(resultMessage, (MessageContext) results[1]);
			return resultMessage;
		}
	}

	private Call getCall(UMOEvent event, Object[] args) throws Exception {
		UMOEndpointURI endpointUri = event.getEndpoint().getEndpointURI();
		String method = (String) endpointUri.getParams().remove("method");

		if (method == null) {
			method = (String) event.getEndpoint().getProperties().get("method");
			if (method == null) {
				throw new DispatchException(new org.mule.config.i18n.Message("soap", 4), event.getMessage(), event.getEndpoint());
			}
		}

		Call call = (Call) getService(event).createCall();

		String style = (String) event.getProperties().get("style");
		String use = (String) event.getProperties().get("use");

		// Note that Axis has specific rules to how these two variables are
		// combined. This is handled for us
		// Set style: RPC/wrapped/Doc/Message
		if (style != null) {
			Style s = Style.getStyle(style);
			if (s == null) {
				throw new IllegalArgumentException(new org.mule.config.i18n.Message(Messages.VALUE_X_IS_INVALID_FOR_X, style, "style").toString());
			} else {
				call.setOperationStyle(s);
			}
		}
		// Set use: Endcoded/Literal
		if (use != null) {
			Use u = Use.getUse(use);
			if (u == null) {
				throw new IllegalArgumentException(new org.mule.config.i18n.Message(Messages.VALUE_X_IS_INVALID_FOR_X, use, "use").toString());
			} else {
				call.setOperationUse(u);
			}
		}

		// set properties on the call from the endpoint properties
		BeanUtils.populateWithoutFail(call, event.getEndpoint().getProperties(), false);
		call.setTargetEndpointAddress(endpointUri.toString());

		// Set a custome method namespace if one is set. This will be used
		// forthe parameters too
		String methodNamespace = (String) event.getProperty(AxisConnector.METHOD_NAMESPACE_PROPERTY);
		if (methodNamespace != null) {
			call.setOperationName(new QName(methodNamespace, method));
		} else {
			call.setOperationName(new QName(method));
		}

		// set Mule event here so that handlers can extract info
		call.setProperty(MuleProperties.MULE_EVENT_PROPERTY, event);
		call.setProperty(MuleProperties.MULE_ENDPOINT_PROPERTY, event.getEndpoint());
		// Set timeout
		int timeout = event.getIntProperty(MuleProperties.MULE_EVENT_TIMEOUT_PROPERTY, -1);
		if (timeout >= 0) {
			call.setTimeout(new Integer(timeout));
		}
		// Add User Creds
		if (endpointUri.getUserInfo() != null) {
			call.setUsername(endpointUri.getUsername());
			call.setPassword(endpointUri.getPassword());
		}

		Map methodCalls = (Map) event.getProperty("soapMethods");
		if (methodCalls == null) {
			ArrayList<String> params = new ArrayList<String>();
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof DataHandler[]) {
					params.add("attachments;qname{DataHandler:http://xml.apache.org/xml-soap};in");
				} else if (call.getTypeMapping().getTypeQName(args[i].getClass()) != null) {
					QName qname = call.getTypeMapping().getTypeQName(args[i].getClass());
					params.add("value" + i + ";qname{" + qname.getPrefix() + ":" + qname.getLocalPart() + ":" + qname.getNamespaceURI() + "};in");
				} else {
					params.add("value" + i + ";qname{" + Types.getLocalNameFromFullName(args[i].getClass().getName()) + ":" + Namespaces.makeNamespace(args[i].getClass().getName()) + "};in");
				}
			}
			HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
			map.put(method, params);
			event.setProperty("soapMethods", map);
		}

		setCallParams(call, event, call.getOperationName());

		// Set custom soap action if set on the event or endpoint
		String soapAction = (String) event.getProperty(AxisConnector.SOAP_ACTION_PROPERTY);
		if (soapAction != null) {
			soapAction = parseSoapAction(soapAction, call, event);
			call.setSOAPActionURI(soapAction);
			call.setUseSOAPAction(Boolean.TRUE.booleanValue());
		} else {
			//call.setSOAPActionURI(endpointUri.getAddress());
		}
		return call;
	}

	private Object[] getArgs(UMOEvent event) throws TransformerException {
		Object payload = event.getTransformedMessage();
		Object[] args = new Object[0];
		if (payload instanceof MessageObject) {
			MessageObject messageObject = (MessageObject) payload;
			ArrayList arguments = new ArrayList();
			// TODO: URGENT Ensure that the ordering is correct for the param
			// mappings
			// Get the parameter mappings (param = value) from the connector
			List<WSParameter> parameterMappings = ((AxisConnector) connector).getParameterMapping();
			Iterator<WSParameter> it = parameterMappings.iterator();
			ParameterValueReplacer paramValueReplacer = new ParameterValueReplacer();
			// Check each 'value' as a reference to the global/local var maps
			while (it.hasNext()) {
				// Note that this DOES NOT take into account vars such as
				// "Original Filename"
				WSParameter param = it.next();
				arguments.add(paramValueReplacer.getValue(param.getValue(), messageObject));
			}
		} else if (payload instanceof Object[]) {
			args = (Object[]) payload;
		} else {
			args = new Object[] { payload };
		}
		// TODO: Check this logic
		if (event.getMessage().getAttachmentNames() != null && event.getMessage().getAttachmentNames().size() > 0) {
			ArrayList<DataHandler> attachments = new ArrayList<DataHandler>();
			Iterator i = event.getMessage().getAttachmentNames().iterator();
			while (i.hasNext()) {
				attachments.add(event.getMessage().getAttachment((String) i.next()));
			}
			ArrayList<Object> temp = new ArrayList<Object>(Arrays.asList(args));
			temp.add(attachments.toArray(new DataHandler[0]));
			args = temp.toArray();
		}
		return args;
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

	private void setCallParams(Call call, UMOEvent event, QName method) throws ClassNotFoundException {
		if (callParameters == null) {
			loadCallParams(event, method.getNamespaceURI());
		}

		SoapMethod soapMethod;
		soapMethod = (SoapMethod) event.removeProperty(MuleProperties.MULE_SOAP_METHOD);
		if (soapMethod == null) {
			soapMethod = callParameters.get(method.getLocalPart());
		}
		if (soapMethod != null) {
			for (Iterator iterator = soapMethod.getNamedParameters().iterator(); iterator.hasNext();) {
				NamedParameter parameter = (NamedParameter) iterator.next();
				call.addParameter(parameter.getName(), parameter.getType(), parameter.getMode());
			}

			if (soapMethod.getReturnType() != null) {
				call.setReturnType(soapMethod.getReturnType());
			} else if (soapMethod.getReturnClass() != null) {
				call.setReturnClass(soapMethod.getReturnClass());
			}
			call.setOperationName(soapMethod.getName());
		}

	}

	private void loadCallParams(UMOEvent event, String namespace) throws ClassNotFoundException {
		callParameters = new HashMap<String, SoapMethod>();
		Map methodCalls = (Map) event.getProperty("soapMethods");
		if (methodCalls == null)
			return;

		Map.Entry entry;
		SoapMethod soapMethod;
		for (Iterator iterator = methodCalls.entrySet().iterator(); iterator.hasNext();) {
			entry = (Map.Entry) iterator.next();

			if ("".equals(namespace) || namespace == null) {
				if (entry.getValue() instanceof List) {
					soapMethod = new SoapMethod(entry.getKey().toString(), (List) entry.getValue());
				} else {
					soapMethod = new SoapMethod(entry.getKey().toString(), entry.getValue().toString());
				}
			} else {
				if (entry.getValue() instanceof List) {
					soapMethod = new SoapMethod(new QName(namespace, entry.getKey().toString()), (List) entry.getValue());
				} else {
					soapMethod = new SoapMethod(new QName(namespace, entry.getKey().toString()), entry.getValue().toString());
				}
			}
			callParameters.put(soapMethod.getName().getLocalPart(), soapMethod);
		}
	}
}