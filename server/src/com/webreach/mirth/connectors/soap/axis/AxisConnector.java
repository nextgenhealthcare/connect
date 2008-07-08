/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/AxisConnector.java,v 1.30 2005/11/02 12:09:24 rossmason Exp $
 * $Revision: 1.30 $
 * $Date: 2005/11/02 12:09:24 $
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.deployment.wsdd.WSDDConstants;
import org.apache.axis.deployment.wsdd.WSDDProvider;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.server.AxisServer;
import org.mule.MuleManager;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.ImmutableMuleEndpoint;
import org.mule.impl.MuleDescriptor;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.impl.internal.events.ModelEvent;
import org.mule.impl.internal.events.ModelEventListener;
import org.mule.providers.QueueEnabledConnector;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.http.servlet.ServletConnector;
import org.mule.providers.service.ConnectorFactory;
import org.mule.providers.soap.axis.extensions.MuleConfigProvider;
import org.mule.providers.soap.axis.extensions.MuleTransport;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.manager.UMOServerEvent;
import org.mule.umo.provider.UMOMessageReceiver;
import org.mule.util.ClassHelper;

import com.webreach.mirth.connectors.http.HttpClientMessageDispatcher;
import com.webreach.mirth.connectors.soap.axis.extensions.WSDDJavaMuleProvider;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.util.UUIDGenerator;

/**
 * <code>AxisConnector</code> is used to maintain one or more Services for
 * Axis server instance.
 * <p/>
 * Some of the Axis specific service initialisation code was adapted from the
 * Ivory project (http://ivory.codehaus.org). Thanks guys :)
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.30 $
 */
public class AxisConnector extends QueueEnabledConnector implements ModelEventListener {
    public static final QName QNAME_MULERPC_PROVIDER = new QName(WSDDConstants.URI_WSDD_JAVA, "Mule");
    public static final QName QNAME_MULE_TYPE_MAPPINGS = new QName("http://www.muleumo.org/ws/mappings", "Mule");
    public static final String DEFAULT_MULE_NAMESPACE_URI = "http://www.muleumo.org";

    public static final String DEFAULT_MULE_AXIS_SERVER_CONFIG = "mule-axis-server-config.wsdd";
    public static final String DEFAULT_MULE_AXIS_CLIENT_CONFIG = "mule-axis-client-config.wsdd";
    public static final String AXIS_SERVICE_COMPONENT_NAME = "_axisServiceComponent";
    public static final String METHOD_NAMESPACE_PROPERTY = "methodNamespace";
    public static final String SOAP_ACTION_PROPERTY = "soapAction";
 //   public static final String SOAP_CONTENT_TYPE_PROPERTY = "Content-Type";
    public static final String SERVICE_PROPERTY_COMPONENT_NAME = "componentName";
    public static final String SERVICE_PROPERTY_SERVCE_PATH = "servicePath";

    public static final String ENDPOINT_COUNTERS_PROPERTY = "endpointCounters";

    public static final String WSDL_URL_PROPERTY = "wsdlUrl";
    public static final String SERVICE_NAME = "Mirth";
    public final String LISTENER_ADDRESS_PROPERTY = "listenerAddress";
    public final String SERVICE_NAME_PROPERTY = "serviceName";
    public final String PORT_PROPERTY = "port";
    public final String SERVICE_ENDPOINT_PROPERTY = "serviceEndpoint";
    public final String METHOD_PROPERTY = "method";
    public final String PARAMETERS_PROPERTY = "parameters";
    public static final String PROPERTY_TRANSFORMER_RESPONSE = "responseFromTransformer";
    public static final String PROPERTY_REPLY_CHANNELID = "replyChannelId";
    public static final String PROPERTY_RESPONSE_VALUE = "responseValue";
    private boolean responseFromTransformer = false;
    private String contentType = "text/xml";
    private String serverConfig;

    private String replyChannelId;
    private AxisServer axisServer;
    private SimpleProvider serverProvider;
    // Client configuration currently not used but the endpoint should
    // probably support configuration of the client too
    private String clientConfig;
    private SimpleProvider clientProvider;

    private List beanTypes;
    private MuleDescriptor axisDescriptor;
    private String serviceEndpoint;
    private String soapActionURI;
    private String port = "8081";
    private String wsdlUrl = "";
    private String listenerAddress = "localhost";
    private String serviceName = SERVICE_NAME;
    private String method = "";
    private List parameters;
    private String soapEnvelope;
    private String responseValue = "None";
    private String currentDescriptorName = null;
    private boolean keepAlive = false;
    private int keepAliveTimeout = 5000;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    /**
     * These protocols will be set on client invocations.  by default Mule uses it's own transports
     * rather that Axis's.  This is only because it gives us more flexibility inside Mule and
     * simplifies the code
     */
    private Map axisTransportProtocols;

    /**
     * A store of registered servlet services that need to have their endpoints re-written
     * with the 'real' http url instead of the servlet:// one. This is only required to ensure
     * wsdl is generated correctly. I would like a clearer way of doing this so I can remove this
     * workaround
     */
    private List servletServices = new ArrayList();

    private List supportedSchemes;

    public AxisConnector() {
        super();
        //Default supported schemes, these can be restricted
        //through configuration
        supportedSchemes = new ArrayList();
        supportedSchemes.add("http");
        supportedSchemes.add("https");
        supportedSchemes.add("servlet");
        supportedSchemes.add("vm");
        supportedSchemes.add("jms");
        supportedSchemes.add("xmpp");
        supportedSchemes.add("smtp");
        supportedSchemes.add("smtps");
        supportedSchemes.add("pop3");
        supportedSchemes.add("pop3s");
        supportedSchemes.add("soap");
        for (Iterator iterator = supportedSchemes.iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();
            registerSupportedProtocol(s);
        }
        currentDescriptorName = "_axis" + UUIDGenerator.getUUID();

    }

    public void doInitialise() throws InitialisationException {
        super.doInitialise();

        axisTransportProtocols = new HashMap();

        for (Iterator iterator = supportedSchemes.iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();
            if(!(s.equalsIgnoreCase("http") || s.equalsIgnoreCase("https") || s.equalsIgnoreCase("servlet"))) {
                axisTransportProtocols.put(s, MuleTransport.class);
            }
            registerSupportedProtocol(s);
        }

        MuleManager.getInstance().registerListener(this);

        if (serverConfig == null)
            serverConfig = DEFAULT_MULE_AXIS_SERVER_CONFIG;
        if (clientConfig == null)
            clientConfig = DEFAULT_MULE_AXIS_CLIENT_CONFIG;
        serverProvider = createAxisProvider(serverConfig);
        clientProvider = createAxisProvider(clientConfig);

        // Create the AxisServer
        axisServer = new AxisServer(serverProvider);
        axisServer.setOption("axis.doAutoTypes", Boolean.TRUE);

        // Register the Mule service serverProvider
        WSDDProvider.registerProvider(QNAME_MULERPC_PROVIDER, new WSDDJavaMuleProvider(this));

        try {
            registerTransportTypes();
        } catch (ClassNotFoundException e) {
            throw new InitialisationException(new org.mule.config.i18n.Message(Messages.CANT_LOAD_X_FROM_CLASSPATH_FILE, e.getMessage()), e, this);
        }

        //Overload the UrlHandlers provided by Axis so Mule can use its transports to move
        //Soap messages around
        String handlerPkgs = System.getProperty("java.protocol.handler.pkgs", null);
        if(handlerPkgs!=null) {
            if(!handlerPkgs.endsWith("|")) handlerPkgs += "|";
            handlerPkgs = "org.mule.providers.soap.axis.transport|" + handlerPkgs;
            System.setProperty("java.protocol.handler.pkgs", handlerPkgs);
            logger.debug("Setting java.protocol.handler.pkgs to: " + handlerPkgs);
        }
        
        if(isUsePersistentQueues()) { 
        	setConnectorErrorCode(Constants.ERROR_410);
			setDispatcher(new AxisMessageDispatcher(this));
		}
    }

    protected void registerTransportTypes() throws ClassNotFoundException {
        //Register Transport handlers
        //By default these will alll be handled by Mule, however some companies may have
        //their own they wish to use
        for (Iterator iterator = getAxisTransportProtocols().keySet().iterator(); iterator.hasNext();) {
            String protocol = (String) iterator.next();
            Object temp = getAxisTransportProtocols().get(protocol);
            Class clazz = null;
            if (temp instanceof String) {
                clazz = ClassHelper.loadClass(temp.toString(), getClass());
            } else {
                clazz = (Class) temp;
            }
            Call.setTransportForProtocol(protocol, clazz);
        }
    }

    protected SimpleProvider createAxisProvider(String config) throws InitialisationException {
        InputStream is = null;
        File f = new File(config);
        if (f.exists()) {
            try {
                is = new FileInputStream(f);
            } catch (FileNotFoundException e) {
                // ignore
            }
        } else {
            is = ClassHelper.getResourceAsStream(config, getClass());
        }
        FileProvider fileProvider = new FileProvider(config);
        if (is != null) {
            fileProvider.setInputStream(is);
        } else {
            throw new InitialisationException(new Message(Messages.FAILED_LOAD_X, "Axis Configuration: " + config),
                    this);
        }
        /*
         * Wrap the FileProvider with a SimpleProvider so we can prgrammatically
         * configure the Axis server (you can only use wsdd descriptors with the
         * FileProvider)
         */
        return new MuleConfigProvider(fileProvider);
    }

    public String getProtocol() {
        return "axis";
    }

    /**
     * The method determines the key used to store the receiver against.
     *
     * @param component the component for which the endpoint is being registered
     * @param endpoint  the endpoint being registered for the component
     * @return the key to store the newly created receiver against. In this case
     *         it is the component name, which is equivilent to the Axis service
     *         name.
     */
    protected Object getReceiverKey(UMOComponent component, UMOEndpoint endpoint) {
        return this.getServiceName();//component.getDescriptor().getName();
    }

    public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
        // this is always initialisaed as synchronous as ws invocations should
        // always execute in a single thread unless the endpont has explicitly
        // been set to run asynchronously
        if (endpoint instanceof ImmutableMuleEndpoint
                && !((ImmutableMuleEndpoint) endpoint).isSynchronousExplicitlySet()) {
            if (!endpoint.isSynchronous()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("overriding endpoint synchronicity and setting it to true. Web service requests are executed in a single thread");
                }
                endpoint.setSynchronous(true);
            }
        }

        return super.createReceiver(component, endpoint);
    }

    protected void unregisterReceiverWithMuleService(UMOMessageReceiver receiver, UMOEndpointURI ep)
            throws UMOException {
        String endpointKey = getCounterEndpointKey(receiver.getEndpointURI());

        Map endpointCounters = (Map) axisDescriptor.getProperties().get(ENDPOINT_COUNTERS_PROPERTY);
        if (endpointCounters == null) {
            logger.error("There are no endpoints registered on the Axis service descriptor");
            return;
        }

        Integer count = (Integer) endpointCounters.get(endpointKey);
        if (count == null) {
            logger.error("There are no services registered on: " + endpointKey);
            return;
        }

        if (count.intValue() > 1) {
            logger.warn("There are '" + count.intValue() + "' services registered on endpoint: " + endpointKey
                    + ". Not unregistering the endpoint at this time");
            count = new Integer(count.intValue() - 1);
            endpointCounters.put(endpointKey, count);
            return;
        } else {
            endpointCounters.remove(endpointKey);

            for (Iterator iterator = axisDescriptor.getInboundRouter().getEndpoints().iterator(); iterator.hasNext();) {
                UMOEndpoint umoEndpoint = (UMOEndpoint) iterator.next();
                if (endpointKey.startsWith(umoEndpoint.getEndpointURI().getAddress()))
                    logger.info("Unregistering Axis endpoint: " + endpointKey + " for service: "
                            + receiver.getComponent().getDescriptor().getName());
                try {
                    umoEndpoint.getConnector().unregisterListener(receiver.getComponent(), receiver.getEndpoint());
                } catch (Exception e) {
                    logger.error("Failed to unregistering Axis endpoint: " + endpointKey + " for service: "
                            + receiver.getComponent().getDescriptor().getName() + ". Error is: " + e.getMessage(), e);
                }
                break;
            }
        }
    }

    protected void registerReceiverWithMuleService(UMOMessageReceiver receiver, UMOEndpointURI ep) throws UMOException {
        // If this is the first receiver we need to create the Axis service
        // component
        // this will be registered with Mule when the Connector starts
        if (axisDescriptor == null) {
            // See if the axis descriptor has already been added. This allows
            // developers to override the default configuration, say to increase
            // the threadpool
            axisDescriptor = (MuleDescriptor) MuleManager.getInstance()
                    .getModel()
                    .getDescriptor(currentDescriptorName);
            if (axisDescriptor == null) {
                axisDescriptor = createAxisDescriptor();
            } else {
                // Lets unregister the 'template' instance, configure it and
                // then register
                // again later
                MuleManager.getInstance().getModel().unregisterComponent(axisDescriptor);
            }
            // if the axis server hasn't been set, set it now. The Axis server
            // may be set
            // externally
            if (axisDescriptor.getProperties().get("axisServer") == null) {
                axisDescriptor.getProperties().put("axisServer", axisServer);
            }
            axisDescriptor.setContainerManaged(false);
        }
        String serviceName = ((AxisConnector)receiver.getConnector()).getServiceName();//receiver.getComponent().getDescriptor().getName();
        // No determine if the endpointUri requires a new connector to be
        // registed in the case of http we only need to register the new endpointUri if
        // the port is different
        //If we're using VM or Jms we just use the resource infor directly without appending a service name
        String endpoint = null;
        String scheme = ep.getScheme().toLowerCase();
         if( scheme.equals("jms") || scheme.equals("vm")) {
             endpoint = ep.toString();
         } else {
            endpoint = receiver.getEndpointURI().getAddress() + "/" + serviceName;
         }
         if(logger.isDebugEnabled()) logger.debug("Modified endpoint with " + scheme + " scheme to " + endpoint);

        boolean sync = receiver.getEndpoint().isSynchronous();
        if (scheme.equals("http") || scheme.equals("tcp")) {
            // if we are using a socket based endpointUri make sure it is
            // running synchronously by default
            sync = true;
        }

        Map endpointCounters = (Map) axisDescriptor.getProperties().get(ENDPOINT_COUNTERS_PROPERTY);
        if (endpointCounters == null) {
            endpointCounters = new HashMap();
        }

        //String endpointKey = getCounterEndpointKey(receiver.getEndpointURI());
        //String endpointKey = receiver.getEndpointURI().toString() + "/" + serviceName;

        Integer count = (Integer) endpointCounters.get(endpoint);
        if (count == null)
            count = new Integer(0);

        if (count.intValue() == 0) {
            UMOEndpoint serviceEndpoint = new MuleEndpoint(endpoint, true);
            serviceEndpoint.setSynchronous(sync);
            serviceEndpoint.setName(ep.getScheme() + ":" + serviceName);
            // set the filter on the axis endpoint on the real receiver endpoint
            serviceEndpoint.setFilter(receiver.getEndpoint().getFilter());
            //propagate properties to the service endpoint
            serviceEndpoint.getProperties().putAll(receiver.getEndpoint().getProperties());

            axisDescriptor.getInboundRouter().addEndpoint(serviceEndpoint);
        }

        // Update the counter for this endpoint
        count = new Integer(count.intValue() + 1);
        endpointCounters.put(endpoint, count);
        axisDescriptor.getProperties().put(ENDPOINT_COUNTERS_PROPERTY, endpointCounters);
    }

    private String getCounterEndpointKey(UMOEndpointURI endpointURI) {
        StringBuffer endpointKey = new StringBuffer();

        endpointKey.append(endpointURI.getScheme());
        endpointKey.append("://");
        endpointKey.append(endpointURI.getHost());
        if (endpointURI.getPort() > -1) {
            endpointKey.append(":");
            endpointKey.append(endpointURI.getPort());
        }
        return endpointKey.toString();
    }

    protected MuleDescriptor createAxisDescriptor() {
        MuleDescriptor axisDescriptor = (MuleDescriptor) MuleManager.getInstance()
                    .getModel().getDescriptor(currentDescriptorName);
        if (axisDescriptor == null) {
            axisDescriptor = new MuleDescriptor(currentDescriptorName);
            axisDescriptor.setImplementation(AxisServiceComponent.class.getName());
            return axisDescriptor;
        }else{
        	currentDescriptorName = "_axis" + UUIDGenerator.getUUID();
        	return createAxisDescriptor();
        }
        
    }

    /**
     * Template method to perform any work when starting the connectoe
     *
     * @throws org.mule.umo.UMOException if the method fails
     */
    protected void doStart() throws UMOException {
    	super.doStart();
    	
    	if (axisServer != null)
    		axisServer.start();
    }

    /**
     * Template method to perform any work when stopping the connectoe
     *
     * @throws org.mule.umo.UMOException if the method fails
     */
    protected void doStop() throws UMOException {
    	super.doStop();
    	
    	axisServer.stop();
        // UMOModel model = MuleManager.getInstance().getModel();
        // model.unregisterComponent(model.getDescriptor(AXIS_SERVICE_COMPONENT_NAME));
    }

    public String getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(String serverConfig) {
        this.serverConfig = serverConfig;
    }

    public List getBeanTypes() {
        return beanTypes;
    }

    public void setBeanTypes(List beanTypes) {
        this.beanTypes = beanTypes;
    }

    public void onEvent(UMOServerEvent event) {
        if (event.getAction() == ModelEvent.MODEL_STARTED) {
            // We need to register the Axis service component once the model
            // starts because
            // when the model starts listeners on components are started, thus
            // all listener
            // need to be registered for this connector before the Axis service
            // component
            // is registered.
            // The implication of this is that to add a new service and a
            // different http port the
            // model needs to be restarted before the listener is available
            if (!MuleManager.getInstance().getModel().isComponentRegistered(currentDescriptorName)) {
                try {
                    //Descriptor might be null if no inbound endpoints have been register for the Axis connector
                    if(axisDescriptor==null) axisDescriptor = createAxisDescriptor();
                    MuleManager.getInstance().getModel().registerComponent(axisDescriptor);
                    //We have to perform a small hack here to rewrite servlet:// endpoints with the
                    //real http:// address
                    for (Iterator iterator = servletServices.iterator(); iterator.hasNext();) {
                        SOAPService service = (SOAPService) iterator.next();
                        ServletConnector servletConnector = (ServletConnector)ConnectorFactory.getConnectorByProtocol("servlet");
                        String url = servletConnector.getServletUrl();
                        if(url!=null) {
                            service.getServiceDescription().setEndpointURL(url + "/" + service.getName());
                        } else {
                            logger.error("The servletUrl property on the ServletConntector has not been set this means that wsdl generation for service '" + service.getName() + "' may be incorrect");
                        }
                    }
                    servletServices.clear();
                    servletServices = null;


                } catch (UMOException e) {
                    handleException(e);
                }
            }
        }
    }

    public String getClientConfig() {
        return clientConfig;
    }

    public void setClientConfig(String clientConfig) {
        this.clientConfig = clientConfig;
    }

    public AxisServer getAxisServer() {
        return axisServer;
    }

    public void setAxisServer(AxisServer axisServer) {
        this.axisServer = axisServer;
    }

    public SimpleProvider getServerProvider() {
        return serverProvider;
    }

    public void setServerProvider(SimpleProvider serverProvider) {
        this.serverProvider = serverProvider;
    }

    public SimpleProvider getClientProvider() {
        return clientProvider;
    }

    public void setClientProvider(SimpleProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    public Map getAxisTransportProtocols() {
        return axisTransportProtocols;
    }

    public void setAxisTransportProtocols(Map axisTransportProtocols) {
        this.axisTransportProtocols.putAll(axisTransportProtocols);
    }

    void addServletService(SOAPService service) {
        servletServices.add(service);
    }

    public List getSupportedSchemes() {
        return supportedSchemes;
    }

    public void setSupportedSchemes(List supportedSchemes) {
        this.supportedSchemes = supportedSchemes;
    }

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		//Run replacer
		this.serviceName = replacer.replaceValuesFromGlobal(serviceName, true);
	}

	public List getParameterMapping() {
		return parameters;
	}

	public void setParameterMapping(List parameterMapping) {
		this.parameters = parameterMapping;
	}

	public String getListenerAddress() {
		return listenerAddress;
	}

	public void setListenerAddress(String listenerAddress) {
		this.listenerAddress = listenerAddress;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getServiceEndpoint() {
		return serviceEndpoint;
	}

	public void setServiceEndpoint(String serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
	}

	public String getWsdlUrl() {
		return wsdlUrl;
	}

	public void setWsdlUrl(String wsdlUrl) {
		this.wsdlUrl = wsdlUrl;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getSoapEnvelope() {
		return soapEnvelope;
	}

	public void setSoapEnvelope(String soapEnvelope) {
		this.soapEnvelope = soapEnvelope;
	}

	public String getSoapActionURI() {
		return soapActionURI;
	}

	public void setSoapActionURI(String soapActionURI) {
		this.soapActionURI = soapActionURI;
	}

	public boolean isResponseFromTransformer() {
		return responseFromTransformer;
	}

	public void setResponseFromTransformer(boolean responseFromTransformer) {
		this.responseFromTransformer = responseFromTransformer;
	}

	public String getReplyChannelId() {
		return replyChannelId;
	}

	public void setReplyChannelId(String replyChannelId) {
		this.replyChannelId = replyChannelId;
	}

	public String getResponseValue() {
		return responseValue;
	}

	public void setResponseValue(String responseValue) {
		this.responseValue = responseValue;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public int getKeepAliveTimeout() {
		return keepAliveTimeout;
	}

	public void setKeepAliveTimeout(int keepAliveTimeout) {
		this.keepAliveTimeout = keepAliveTimeout;
	}

}
