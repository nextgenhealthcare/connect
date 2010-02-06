/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mule.MuleManager;
import org.mule.components.simple.PassThroughComponent;
import org.mule.impl.MuleDescriptor;
import org.mule.impl.MuleTransactionConfig;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.impl.model.seda.SedaModel;
import org.mule.interceptors.InterceptorStack;
import org.mule.interceptors.LoggingInterceptor;
import org.mule.interceptors.TimerInterceptor;
import org.mule.management.agents.JmxAgent;
import org.mule.management.agents.RmiRegistryAgent;
import org.mule.routing.inbound.InboundMessageRouter;
import org.mule.routing.inbound.SelectiveConsumer;
import org.mule.routing.outbound.FilteringMulticastingRouter;
import org.mule.routing.outbound.OutboundMessageRouter;
import org.mule.umo.UMODescriptor;
import org.mule.umo.UMOInterceptor;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.manager.UMOManager;
import org.mule.umo.model.UMOModel;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.routing.UMOOutboundRouter;
import org.mule.umo.transformer.UMOTransformer;

import com.webreach.mirth.connectors.jdbc.JdbcTransactionFactory;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.builders.JavaScriptBuilder;
import com.webreach.mirth.server.mule.ExceptionStrategy;
import com.webreach.mirth.server.mule.adaptors.AdaptorFactory;
import com.webreach.mirth.server.mule.filters.ValidMessageFilter;
import com.webreach.mirth.server.util.UUIDGenerator;
import com.webreach.mirth.util.PropertyLoader;

import edu.emory.mathcs.backport.java.util.Arrays;

public class MuleEngineController implements EngineController {
    private Logger logger = Logger.getLogger(this.getClass());
    private Map<String, ConnectorMetaData> transports = null;
    private JavaScriptBuilder scriptBuilder = new JavaScriptBuilder();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private TemplateController templateController = ControllerFactory.getFactory().createTemplateController();
    private ObjectXMLSerializer objectSerializer = new ObjectXMLSerializer();
    private UMOManager muleManager = MuleManager.getInstance();
    private JmxAgent jmxAgent = null;
    private static List<String> nonConnectorProperties = null;
    private static List<String> keysOfValuesThatAreBeans = null;
    private static Map<String, String> defaultTransformers = null;

    static {
        // list of all properties which should not be appended to the
        // connector
        nonConnectorProperties = Arrays.asList(new String[] { "host", "port", "DataType" });
        keysOfValuesThatAreBeans = Arrays.asList(new String[] { "connectionFactoryProperties", "requestVariables", "headerVariables", "envelopeProperties", "attachmentNames", "attachmentContents", "attachmentTypes", "traits", "dispatcherAttachmentNames", "dispatcherAttachmentContents", "dispatcherAttachmentTypes", "requestParamsName", "requestParamsKey", "requestParamsValue", "assertionParamsKey", "assertionParamsValue", "receiverUsernames", "receiverPasswords" });

        // add default transformers
        defaultTransformers = new HashMap<String, String>();
        defaultTransformers.put("ByteArrayToString", "org.mule.transformers.simple.ByteArrayToString");
        defaultTransformers.put("JMSMessageToObject", "com.webreach.mirth.connectors.jms.transformers.JMSMessageToObject");
        defaultTransformers.put("StringToByteArray", "org.mule.transformers.simple.StringToByteArray");
        defaultTransformers.put("ResultMapToXML", "com.webreach.mirth.server.mule.transformers.ResultMapToXML");
        defaultTransformers.put("ObjectToString", "org.mule.transformers.simple.ObjectToString");
        defaultTransformers.put("NoActionTransformer", "org.mule.transformers.NoActionTransformer");
        defaultTransformers.put("HttpStringToXML", "com.webreach.mirth.server.mule.transformers.HttpStringToXML");
        defaultTransformers.put("HttpRequestToString", "com.webreach.mirth.server.mule.transformers.HttpRequestToString");
    }

    public void resetConfiguration() throws Exception {
        logger.debug("loading manager with default components");

        Properties properties = PropertyLoader.loadProperties("mirth");
        muleManager.setId("MirthConfiguration");
        MuleManager.getConfiguration().setEmbedded(true);
        MuleManager.getConfiguration().setRecoverableMode(true);
        MuleManager.getConfiguration().setClientMode(false);
        // set the Mule working directory
        String muleQueue = PropertyLoader.getProperty(properties, "mule.queue");
        muleQueue = StringUtils.replace(muleQueue, "${mirthHomeDir}", ControllerFactory.getFactory().createConfigurationController().getBaseDir());
        MuleManager.getConfiguration().setWorkingDirectory(muleQueue);

        for (String transformerName : defaultTransformers.keySet()) {
            UMOTransformer umoTransformer = (UMOTransformer) Class.forName(defaultTransformers.get(transformerName)).newInstance();
            umoTransformer.setName(transformerName);
            muleManager.registerTransformer(umoTransformer);
        }

        // add interceptor stack
        InterceptorStack stack = new InterceptorStack();
        List<UMOInterceptor> interceptors = new ArrayList<UMOInterceptor>();
        interceptors.add(new LoggingInterceptor());
        interceptors.add(new TimerInterceptor());
        stack.setInterceptors(interceptors);
        muleManager.registerInterceptorStack("default", stack);

        // add model
        UMOModel model = new SedaModel();
        model.setName("Mirth");

        // add MessageSink descriptor
        UMODescriptor messageSinkDescriptor = new MuleDescriptor();
        messageSinkDescriptor.setName("MessageSink");
        messageSinkDescriptor.setImplementation(new PassThroughComponent());
        InboundMessageRouter messageSinkInboundRouter = new InboundMessageRouter();
        MuleEndpoint vmSinkEndpoint = new MuleEndpoint();
        vmSinkEndpoint.setEndpointURI(new MuleEndpointURI(new URI("vm://sink").toString()));
        messageSinkInboundRouter.addEndpoint(vmSinkEndpoint);
        messageSinkDescriptor.setInboundRouter(messageSinkInboundRouter);
        model.registerComponent(messageSinkDescriptor);

        // set the model (which also initializes and starts the model)
        muleManager.setModel(model);

        // add agents
        String port = PropertyLoader.getProperty(properties, "jmx.port");
        RmiRegistryAgent rmiRegistryAgent = new RmiRegistryAgent();
        rmiRegistryAgent.setName("RMI");
        rmiRegistryAgent.setServerUri("rmi://localhost:" + port);
        muleManager.registerAgent(rmiRegistryAgent);

        jmxAgent = new JmxAgent();
        jmxAgent.setName("JMX");
        Map<String, String> connectorServerProperties = new HashMap<String, String>();
        connectorServerProperties.put("jmx.remote.jndi.rebind", "true");
        jmxAgent.setConnectorServerProperties(connectorServerProperties);
        jmxAgent.setConnectorServerUrl("service:jmx:rmi:///jndi/rmi://localhost:" + port + "/server");
        logger.debug("JMX server URL: " + "service:jmx:rmi:///jndi/rmi://localhost:" + port + "/server");
        Map<String, String> credentialsMap = new HashMap<String, String>();
        credentialsMap.put("admin", PropertyLoader.getProperty(properties, "jmx.password"));
        jmxAgent.setCredentials(credentialsMap);
        jmxAgent.setDomain(muleManager.getId());
        muleManager.registerAgent(jmxAgent);
    }

    public void deployChannels(List<Channel> channels, Map<String, ConnectorMetaData> transports) throws Exception {
        this.transports = transports;

        if ((channels == null) || (transports == null)) {
            throw new Exception("Invalid channel or transport list.");
        }

        for (Channel channel : channels) {
            if (channel.isEnabled()) {
                registerChannel(channel);
            }
        }
    }

    private void registerChannel(Channel channel) throws Exception {
        logger.debug("registering descriptor for channel: " + channel.getId());
        UMODescriptor descriptor = new MuleDescriptor();
        descriptor.setImplementation(Class.forName("com.webreach.mirth.server.mule.components.Channel").newInstance());
        descriptor.setName(channel.getId());

        // default initial state is stopped if no state is found
        String initialState = "stopped";

        if (channel.getProperties().getProperty("initialState") != null) {
            initialState = channel.getProperties().getProperty("initialState");
        }

        descriptor.setInitialState(initialState);
        descriptor.setExceptionListener(new ExceptionStrategy());
        configureInboundRouter(descriptor, channel);
        configureOutboundRouter(descriptor, channel);
        muleManager.getModel().registerComponent(descriptor);

        // register its mbean
        if (muleManager.isStarted()) {
            jmxAgent.registerComponentService(descriptor.getName());
        }
    }

    private void configureInboundRouter(UMODescriptor descriptor, Channel channel) throws Exception {
        logger.debug("configuring inbound router for channel: " + channel.getId() + " (" + channel.getName() + ")");
        InboundMessageRouter inboundRouter = new InboundMessageRouter();

        // add source endpoints
        MuleEndpoint vmEndpoint = new MuleEndpoint();
        vmEndpoint.setEndpointURI(new MuleEndpointURI(new URI("vm://" + channel.getId()).toString()));
        MuleEndpoint endpoint = new MuleEndpoint();

        String connectorReference = channel.getId() + "_source";

        // add the source connector
        UMOConnector connector = registerConnector(channel.getSourceConnector(), connectorReference + "_connector", channel.getId());
        endpoint.setConnector(connector);

        // if the channel is snychronous
        if ((channel.getProperties().get("synchronous")) != null && ((String) channel.getProperties().get("synchronous")).equalsIgnoreCase("true")) {
            endpoint.setSynchronous(true);
        }

        // 1. append the default transformers required by the transport (ex.
        // ByteArrayToString)
        ConnectorMetaData transport = transports.get(channel.getSourceConnector().getTransportName());
        LinkedList<UMOTransformer> transformerList = null;

        if (transport.getTransformers() != null) {
            transformerList = chainTransformers(transport.getTransformers());
        }

        // 2. append the preprocessing transformer
        UMOTransformer preprocessorTransformer = registerPreprocessor(channel, connectorReference + "_preprocessor");

        if (!transformerList.isEmpty()) {
            transformerList.getLast().setTransformer(preprocessorTransformer);
        } else {
            // there were no default transformers, so make the preprocessor
            // the first transformer in the list
            transformerList.add(preprocessorTransformer);
        }

        // 3. finally, append the JavaScriptTransformer that does the
        // mappings
        UMOTransformer javascriptTransformer = registerTransformer(channel, channel.getSourceConnector(), connectorReference + "_transformer");
        preprocessorTransformer.setTransformer(javascriptTransformer);

        // 4. add the transformer sequence as an attribute to the endpoint
        endpoint.setTransformer(transformerList.getFirst());
        vmEndpoint.setTransformer(preprocessorTransformer);

        inboundRouter.addEndpoint(vmEndpoint);

        SelectiveConsumer selectiveConsumerRouter = new SelectiveConsumer();
        selectiveConsumerRouter.setFilter(new ValidMessageFilter());
        inboundRouter.addRouter(selectiveConsumerRouter);

        // NOTE: If the user selected the Channel Reader connector, then we
        // don't to add it since there already exists a VM connector for
        // every channel
        if (!channel.getSourceConnector().getTransportName().equals("Channel Reader")) {
            endpoint.setEndpointURI(new MuleEndpointURI(new URI(getEndpointUri(channel.getSourceConnector())).toString()));
            inboundRouter.addEndpoint(endpoint);
        }

        descriptor.setInboundRouter(inboundRouter);
    }

    private void configureOutboundRouter(UMODescriptor descriptor, Channel channel) throws Exception {
        logger.debug("configuring outbound router for channel: " + channel.getId() + " (" + channel.getName() + ")");
        FilteringMulticastingRouter fmr = new FilteringMulticastingRouter();
        boolean enableTransactions = false;

        for (ListIterator<Connector> iterator = channel.getDestinationConnectors().listIterator(); iterator.hasNext();) {
            Connector connector = iterator.next();

            if (connector.isEnabled()) {
                MuleEndpoint endpoint = new MuleEndpoint();
                endpoint.setEndpointURI(new MuleEndpointURI(new URI(getEndpointUri(connector)).toString()));

                // if there are multiple endpoints, make them all
                // synchronous to
                // ensure correct ordering of fired events
                if (channel.getDestinationConnectors().size() > 0) {
                    endpoint.setSynchronous(true);
                    // TODO: routerElement.setAttribute("synchronous",
                    // "true");
                }

                // ast: now, a funciont gets the connection reference string
                // String connectorReference = channel.getId() +
                // "_destination_"
                // + String.valueOf(iterator.nextIndex());
                String connectorReference = getConnectorReferenceForOutputRouter(channel, String.valueOf(iterator.nextIndex()));

                // add the destination connector
                // ast: changes to get the same name for the connector and
                String connectorName = getConnectorNameForOutputRouter(connectorReference);
                endpoint.setConnector(registerConnector(connector, connectorName, channel.getId()));

                // 1. append the JavaScriptTransformer that does the
                // mappings
                UMOTransformer javascriptTransformer = registerTransformer(channel, connector, connectorReference + "_transformer");

                // 2. finally, append any transformers needed by the
                // transport (ie. StringToByteArray)
                ConnectorMetaData transport = transports.get(connector.getTransportName());
                LinkedList<UMOTransformer> defaultTransformerList = null;

                if (transport.getTransformers() != null) {
                    defaultTransformerList = chainTransformers(transport.getTransformers());

                    if (!defaultTransformerList.isEmpty()) {
                        javascriptTransformer.setTransformer(defaultTransformerList.getFirst());
                    }
                }

                // enable transactions for the outbound router only if it
                // has a JDBC connector
                if (transport.getProtocol().equalsIgnoreCase("jdbc")) {
                    enableTransactions = true;
                }

                endpoint.setTransformer(javascriptTransformer);
                fmr.addEndpoint(endpoint);
            }
        }

        // check for enabled transactions
        boolean transactional = ((channel.getProperties().get("transactional") != null) && channel.getProperties().get("transactional").toString().equalsIgnoreCase("true"));

        if (enableTransactions && transactional) {
            MuleTransactionConfig mtc = new MuleTransactionConfig();
            mtc.setActionAsString("BEGIN_OR_JOIN");
            mtc.setFactory(new JdbcTransactionFactory());
            fmr.setTransactionConfig(mtc);
        }

        OutboundMessageRouter outboundRouter = new OutboundMessageRouter();
        outboundRouter.addRouter(fmr);
        descriptor.setOutboundRouter(outboundRouter);
    }

    // ast: to sincronize the name of the connector for the output router and
    // the response router
    private String getConnectorNameForOutputRouter(String connectorReference) {
        return connectorReference + "_connector";
    }

    private String getConnectorReferenceForOutputRouter(Channel channel, String value) {
        return channel.getId() + "_destination_" + value;
    }

    private UMOTransformer registerTransformer(Channel channel, Connector connector, String name) throws Exception {
        Transformer transformer = connector.getTransformer();
        logger.debug("registering transformer: " + name);
        UMOTransformer umoTransformer = (UMOTransformer) Class.forName("com.webreach.mirth.server.mule.transformers.JavaScriptTransformer").newInstance();
        umoTransformer.setName(name);
        Map<String, Object> beanProperties = new HashMap<String, Object>();
        beanProperties.put("channelId", channel.getId());
        beanProperties.put("inboundProtocol", transformer.getInboundProtocol().toString());
        beanProperties.put("outboundProtocol", transformer.getOutboundProtocol().toString());
        beanProperties.put("encryptData", channel.getProperties().get("encryptData"));
        beanProperties.put("removeNamespace", channel.getProperties().get("removeNamespace"));
        beanProperties.put("mode", connector.getMode().toString());

        // put the outbound template in the templates table
        if (transformer.getOutboundTemplate() != null) {
            TemplateController templateController = ControllerFactory.getFactory().createTemplateController();
            IXMLSerializer<String> serializer = AdaptorFactory.getAdaptor(transformer.getOutboundProtocol()).getSerializer(transformer.getOutboundProperties());
            String templateId = UUIDGenerator.getUUID();

            if (transformer.getOutboundTemplate().length() > 0) {
                if (transformer.getOutboundProtocol().equals(MessageObject.Protocol.DICOM)) {
                    templateController.putTemplate(channel.getId(), templateId, transformer.getOutboundTemplate());
                } else {
                    templateController.putTemplate(channel.getId(), templateId, serializer.toXML(transformer.getOutboundTemplate()));
                }
            }

            beanProperties.put("templateId", templateId);
        }

        // put the script in the scripts table
        String scriptId = UUIDGenerator.getUUID();
        scriptController.putScript(channel.getId(), scriptId, scriptBuilder.getScript(channel, connector.getFilter(), transformer));
        beanProperties.put("scriptId", scriptId);
        beanProperties.put("connectorName", connector.getName());

        if (transformer.getInboundProperties() != null && transformer.getInboundProperties().size() > 0) {
            beanProperties.put("inboundProperties", transformer.getInboundProperties());
        }

        if (transformer.getOutboundProperties() != null && transformer.getOutboundProperties().size() > 0) {
            beanProperties.put("outboundProperties", transformer.getOutboundProperties());
        }

        // Add the "batchScript" property to the script table
        // TODO: Make the second ID param be "batch" or something like that
        if (transformer.getInboundProperties() != null && transformer.getInboundProperties().getProperty("batchScript") != null) {
            scriptController.putScript(channel.getId(), channel.getId(), transformer.getInboundProperties().getProperty("batchScript"));
        }

        BeanUtils.populate(umoTransformer, beanProperties);
        muleManager.registerTransformer(umoTransformer);
        return umoTransformer;
    }

    private UMOConnector registerConnector(Connector connector, String name, String channelId) throws Exception {
        logger.debug("registering connector: " + name);
        // get the transport associated with this class from the transport
        // map
        ConnectorMetaData transport = transports.get(connector.getTransportName());
        UMOConnector umoConnector = (UMOConnector) Class.forName(transport.getServerClassName()).newInstance();
        umoConnector.setName(name);

        // exception-strategy
        umoConnector.setExceptionListener(new ExceptionStrategy());

        // The connector needs it's channel id (so it doesn't have to parse
        // the name) for alerts
        Map<Object, Object> beanProperties = new HashMap<Object, Object>();
        beanProperties.put("channelId", channelId);

        for (Entry<Object, Object> property : connector.getProperties().entrySet()) {
            if ((property.getValue() != null) && !property.getValue().equals("") && !nonConnectorProperties.contains(property.getKey())) {
                if (keysOfValuesThatAreBeans.contains(property.getKey())) {
                    beanProperties.put(property.getKey(), objectSerializer.fromXML(property.getValue().toString()));
                } else if (property.getKey().equals("script") || property.getKey().equals("ackScript")) {
                    String databaseScriptId = UUIDGenerator.getUUID();
                    scriptController.putScript(channelId, databaseScriptId, property.getValue().toString());
                    beanProperties.put(property.getKey() + "Id", databaseScriptId);
                } else {
                    beanProperties.put(property.getKey(), property.getValue());
                }
            }
        }

        // populate the bean properties
        BeanUtils.populate(umoConnector, beanProperties);

        // add the connector to the manager
        muleManager.registerConnector(umoConnector);
        return umoConnector;
    }

    private UMOTransformer registerPreprocessor(Channel channel, String name) throws Exception {
        UMOTransformer umoTransformer = (UMOTransformer) Class.forName("com.webreach.mirth.server.mule.transformers.JavaScriptPreprocessor").newInstance();
        umoTransformer.setName(name);
        String preprocessingScriptId = UUIDGenerator.getUUID();
        scriptController.putScript(channel.getId(), preprocessingScriptId, channel.getPreprocessingScript());
        PropertyUtils.setSimpleProperty(umoTransformer, "channelId", channel.getId());
        PropertyUtils.setSimpleProperty(umoTransformer, "preprocessingScriptId", preprocessingScriptId);

        // add the transformer to the manager
        muleManager.registerTransformer(umoTransformer);
        return umoTransformer;
    }

    // Generate the endpoint URI for the specified connector.
    // The format is: protocol://host|hostname|emtpy:port
    private String getEndpointUri(Connector connector) {
        // TODO: This is a hack.
        if (connector.getProperties().getProperty("host") != null && connector.getProperties().getProperty("host").startsWith("http")) {
            return connector.getProperties().getProperty("host");
        }

        StringBuilder builder = new StringBuilder();
        builder.append(transports.get(connector.getTransportName()).getProtocol());
        builder.append("://");

        if (connector.getProperties().getProperty("host") != null) {
            builder.append(connector.getProperties().getProperty("host"));
        } else if (connector.getProperties().getProperty("hostname") != null) {
            builder.append(connector.getProperties().getProperty("hostname"));
        }

        if (connector.getProperties().getProperty("port") != null) {
            builder.append(":");
            builder.append(connector.getProperties().getProperty("port"));
        }

        return builder.toString();
    }

    private LinkedList<UMOTransformer> chainTransformers(String transformers) throws Exception {
        LinkedList<UMOTransformer> transformerList = new LinkedList<UMOTransformer>();

        if (transformers.length() != 0) {
            String[] transformerClassArray = transformers.split("\\s");
            UMOTransformer[] transformerArray = new UMOTransformer[transformerClassArray.length];

            // turn the array of class names into an array of actual Objects
            for (int i = 0; i < transformerClassArray.length; i++) {
                transformerArray[i] = (UMOTransformer) muleManager.getTransformers().get(transformerClassArray[i]);
            }

            // chain the transformers (except for the last one)
            for (int i = 0; i < transformerArray.length; i++) {
                if (i != transformerArray.length - 1) {
                    transformerArray[i].setTransformer(transformerArray[i + 1]);
                }
            }

            // turn the array of Objects into a List
            for (int i = 0; i < transformerArray.length; i++) {
                transformerList.add(transformerArray[i]);
            }
        }

        return transformerList;
    }

    public void unregisterChannel(String channelId) throws Exception {
        logger.debug("unregistering descriptor: " + channelId);
        UMODescriptor descriptor = muleManager.getModel().getDescriptor(channelId);
        muleManager.getModel().unregisterComponent(descriptor);
        unregisterConnectors(descriptor.getInboundRouter().getEndpoints());
        UMOOutboundRouter outboundRouter = (UMOOutboundRouter) descriptor.getOutboundRouter().getRouters().iterator().next();
        unregisterConnectors(outboundRouter.getEndpoints());

        // remove the scripts associated with the channel
        scriptController.removeScripts(channelId);
        templateController.removeTemplates(channelId);
        
        // unregister its mbean
        jmxAgent.unregsiterComponentService(channelId);
    }

    private void unregisterConnectors(List<UMOEndpoint> endpoints) throws Exception {
        for (UMOEndpoint endpoint : endpoints) {
            if (!endpoint.getEndpointURI().getUri().toString().equals("vm://sink")) {
                logger.debug("unregistering endpoint: " + endpoint.getName());
                muleManager.unregisterEndpoint(endpoint.getName());
            }

            unregisterTransformer(endpoint.getTransformer());
        }
    }

    private void unregisterTransformer(UMOTransformer transformer) throws Exception {
        if (!defaultTransformers.keySet().contains(transformer.getName())) {
            logger.debug("unregistering transformer: " + transformer.getName());
            muleManager.unregisterTransformer(transformer.getName());
        }
    }

    public boolean isChannelRegistered(String channelId) throws Exception {
        return muleManager.getModel().isComponentRegistered(channelId);
    }

    public List<String> getDeployedChannelIds() throws Exception {
        List<String> channelIds = new ArrayList<String>();

        for (Iterator<String> iterator = muleManager.getModel().getComponentNames(); iterator.hasNext();) {
            String channelId = iterator.next();
            
            if (!channelId.equals("MessageSink")) {
                channelIds.add(channelId);    
            }
        }

        return channelIds;
    }

    public void start() throws Exception {
        logger.debug("starting mule engine");
        muleManager.start();
    }

    public void stop() throws Exception {
        if (muleManager != null) {
            try {
                if (muleManager.isStarted()) {
                    logger.error("stopping mule engine");
                    muleManager.stop();
                }
            } catch (Exception e) {
                throw e;
            } finally {
                logger.debug("disposing mule instance");
                muleManager.dispose();
            }
        }
    }
}
