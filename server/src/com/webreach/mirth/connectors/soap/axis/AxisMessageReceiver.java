/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/AxisMessageReceiver.java,v 1.10 2005/10/26 14:48:31 rossmason Exp $
 * $Revision: 1.10 $
 * $Date: 2005/10/26 14:48:31 $
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.AxisProperties;
import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.encoding.TypeMappingRegistryImpl;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.SimpleDeserializerFactory;
import org.apache.axis.encoding.ser.SimpleSerializerFactory;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.wsdl.fromJava.Namespaces;
import org.apache.axis.wsdl.fromJava.Types;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleDescriptor;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.soap.axis.AxisInitialisationCallback;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.util.ClassHelper;

import com.webreach.mirth.connectors.soap.ServiceProxy;
import com.webreach.mirth.connectors.soap.axis.extensions.MuleProvider;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;

/**
 * <code>AxisMessageReceiver</code> is used to register a component as a
 * service with a Axis server.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.10 $
 */

public class AxisMessageReceiver extends AbstractMessageReceiver
{
    protected AxisConnector connector;
    protected SOAPService service;
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.LISTENER;
    public AxisMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint)
            throws InitialisationException
    {
        super(connector, component, endpoint);
        this.connector = (AxisConnector) connector;
        try {
            init();
        } catch (Exception e) {
            throw new InitialisationException(e, this);
        }
        monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
    }

    protected void init() throws Exception
    {
        AxisProperties.setProperty("axis.doAutoTypes", "true");
        service = new SOAPService(new MuleProvider(connector));
        MuleDescriptor descriptor = (MuleDescriptor) component.getDescriptor();

        service.setEngine(connector.getAxisServer());

        UMOEndpointURI uri = endpoint.getEndpointURI();
        String serviceName = connector.getServiceName();//component.getDescriptor().getName();

        String servicePath = uri.getPath();
        service.setOption(connector.getServiceName(), this);
        service.setOption(serviceName, this);
        service.setOption(AxisConnector.SERVICE_PROPERTY_SERVCE_PATH, servicePath);
        service.setOption(AxisConnector.SERVICE_PROPERTY_COMPONENT_NAME, serviceName);
        service.setName(serviceName);

        // Add any custom options from the Descriptor config
        Map options = (Map) descriptor.getProperties().get("axisOptions");
        if (options != null) {
            Map.Entry entry;
            for (Iterator iterator = options.entrySet().iterator(); iterator.hasNext();) {
                entry = (Map.Entry) iterator.next();
                service.setOption(entry.getKey().toString(), entry.getValue());
                logger.debug("Adding Axis option: " + entry);
            }
        }
        // set method names
        Class[] interfaces = ServiceProxy.getInterfaceForClass("com.webreach.mirth.server.mule.components.SoapService");
        if (interfaces.length == 0) {
            throw new InitialisationException(new Message(Messages.X_MUST_IMPLEMENT_AN_INTERFACE, serviceName),
                                              component);
        }
        // You must supply a class name if you want to restrict methods
        // or specify the 'allowedMethods' property in the axisOptions property
        String methodNames = "*";

        String[] methods = ServiceProxy.getMethodNames(interfaces);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < methods.length; i++) {
            buf.append(methods[i]).append(",");
        }
        String className = interfaces[0].getName();
        methodNames = buf.toString();
        methodNames = methodNames.substring(0, methodNames.length() - 1);

        // The namespace of the service.
        String namespace = Namespaces.makeNamespace(className);

        /*
         * Now we set up the various options for the SOAPService. We set:
         * 
         * RPCProvider.OPTION_WSDL_SERVICEPORT In essense, this is our service
         * name
         * 
         * RPCProvider.OPTION_CLASSNAME This tells the serverProvider (whether
         * it be an AvalonProvider or just JavaProvider) what class to load via
         * "makeNewServiceObject".
         * 
         * RPCProvider.OPTION_SCOPE How long the object loaded via
         * "makeNewServiceObject" will persist - either request, session, or
         * application. We use the default for now.
         * 
         * RPCProvider.OPTION_WSDL_TARGETNAMESPACE A namespace created from the
         * package name of the service.
         * 
         * RPCProvider.OPTION_ALLOWEDMETHODS What methods the service can
         * execute on our class.
         * 
         * We don't set: RPCProvider.OPTION_WSDL_PORTTYPE
         * RPCProvider.OPTION_WSDL_SERVICEELEMENT
         */
        setOptionIfNotset(service, RPCProvider.OPTION_WSDL_SERVICEPORT, serviceName);
        setOptionIfNotset(service, RPCProvider.OPTION_CLASSNAME, className);
        setOptionIfNotset(service, RPCProvider.OPTION_SCOPE, "Request");
        setOptionIfNotset(service, RPCProvider.OPTION_WSDL_TARGETNAMESPACE, namespace);

        // Set the allowed methods, allow all if there are none specified.
        if (methodNames == null) {
            setOptionIfNotset(service, RPCProvider.OPTION_ALLOWEDMETHODS, "*");
        } else {
            setOptionIfNotset(service, RPCProvider.OPTION_ALLOWEDMETHODS, methodNames);
        }


        String style = (String) descriptor.getProperties().get("style");
        String use = (String) descriptor.getProperties().get("use");
        String doc = (String) descriptor.getProperties().get("documentation");

        // Note that Axis has specific rules to how these two variables are
        // combined. This is handled for us
        // Set style: RPC/wrapped/Doc/Message
        if (style != null) {
            Style s = Style.getStyle(style);
            if(s==null) {
                throw new InitialisationException(new Message(Messages.VALUE_X_IS_INVALID_FOR_X, style, "style"), this);
            } else {
                service.setStyle(s);
            }
        }
        // Set use: Endcoded/Literal
        if (use != null) {
            Use u = Use.getUse(use);
            if(u==null) {
                throw new InitialisationException(new Message(Messages.VALUE_X_IS_INVALID_FOR_X, use, "use"), this);
            } else {
                service.setUse(u);
            }
        }
        service.getServiceDescription().setDocumentation(doc);

      
        // Tell Axis to try and be intelligent about serialization.
        TypeMappingRegistryImpl registry = (TypeMappingRegistryImpl) service.getTypeMappingRegistry();
        // TypeMappingImpl tm = (TypeMappingImpl) registry.();
        // Handle complex bean type automatically
        // registry.setDoAutoTypes( true );
        // Axis 1.2 fix to handle autotypes properly
        AxisProperties.setProperty("axis.doAutoTypes", "true");

        // Load any explicitly defined bean types
        List types = (List) descriptor.getProperties().get("beanTypes");
        registerTypes(registry, types);
        // register any beantypes set on the connector for this service
        registerTypes(registry, connector.getBeanTypes());
        Class strclass = ClassHelper.loadClass("java.lang.String", getClass());
        Class moclazz = ClassHelper.loadClass("java.lang.Object", getClass());
        String localName = Types.getLocalNameFromFullName(moclazz.getName());
     
        QName xmlType = new QName(Namespaces.makeNamespace(moclazz.getName()),
                localName);
        registry.getDefaultTypeMapping().register(moclazz,
                                                  xmlType,
                                                  new  SimpleSerializerFactory(strclass, xmlType),
                                                  new  SimpleDeserializerFactory(strclass, xmlType));
                                               
        service.setName(serviceName);
        

        // Add initialisation callback for the Axis service
        descriptor.addInitialisationCallback(new AxisInitialisationCallback(service));

        if(uri.getScheme().equalsIgnoreCase("servlet")) {
            connector.addServletService(service);
            String endpointUrl = uri.getAddress() + "/" + serviceName;
            endpointUrl.replaceFirst("servlet:", "http:");
            service.getServiceDescription().setEndpointURL(endpointUrl);
        }else if(uri.getScheme().equalsIgnoreCase("soap")) {
            String endpointUrl = "http://" + connector.getExternalAddress() + ":" + uri.getPort() + servicePath + "/" + serviceName;
            service.getServiceDescription().setEndpointURL(endpointUrl); 
        } else {
            service.getServiceDescription().setEndpointURL(uri.getAddress() + "/" + serviceName);
        }
        service.stop();
    }

    public void doConnect() throws Exception
    {
        // Tell the axis configuration about our new service.
        connector.getServerProvider().deployService(service.getName(), service);
        connector.registerReceiverWithMuleService(this, endpoint.getEndpointURI());
    }

    public void doDisconnect() throws Exception
    {
    	monitoringController.updateStatus(connector, connectorType, Event.DISCONNECTED);
        try {
            doStop();
        } catch (UMOException e) {
            logger.error(e.getMessage(), e);
        }
        // todo: how do you undeploy an Axis service?

        // Unregister the mule part of the service
        connector.unregisterReceiverWithMuleService(this, endpoint.getEndpointURI());
    }

    public void doStart() throws UMOException
    {
        if (service != null)
            service.start();
    }

    public void doStop() throws UMOException
    {
    	monitoringController.updateStatus(connector, connectorType, Event.DISCONNECTED);
        if (service != null)
            service.stop();
    }

    protected void setOptionIfNotset(SOAPService service, String option, Object value)
    {
        Object val = service.getOption(option);
        if (val == null)
            service.setOption(option, value);
    }

    protected void registerTypes(TypeMappingRegistryImpl registry, List types) throws ClassNotFoundException
    {
        if (types != null) {
            Class clazz;
            for (Iterator iterator = types.iterator(); iterator.hasNext();) {
                clazz = ClassHelper.loadClass(iterator.next().toString(), getClass());
                String localName = Types.getLocalNameFromFullName(clazz.getName());
                QName xmlType = new QName(Namespaces.makeNamespace(clazz.getName()),
                                          localName);

                registry.getDefaultTypeMapping().register(clazz,
                                                          xmlType,
                                                          new BeanSerializerFactory(clazz, xmlType),
                                                          new BeanDeserializerFactory(clazz, xmlType));
            }
        }
    }

    SOAPService getService() {
        return service;
    }
}
