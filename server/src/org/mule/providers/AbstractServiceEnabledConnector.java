/*
 * $Header: /home/projects/mule/scm/mule/mule/src/java/org/mule/providers/AbstractServiceEnabledConnector.java,v 1.10 2005/11/01 14:56:36 rossmason Exp $
 * $Revision: 1.10 $
 * $Date: 2005/11/01 14:56:36 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers;

import java.util.Map;
import java.util.Properties;

import org.mule.MuleManager;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.service.ConnectorFactory;
import org.mule.providers.service.ConnectorServiceDescriptor;
import org.mule.providers.service.ConnectorServiceException;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.umo.provider.UMOMessageReceiver;
import org.mule.util.BeanUtils;
import org.mule.util.PropertiesHelper;

/**
 * <code>AbstractServiceEnabledConnector</code> initialises a connector from a
 * service descriptor.  Using this method greatly reduces the code required to
 * implement a connector and means that Mule can create connectors and endpoints
 * from a url if the connector has a service descriptor.
 * 
 * @see ConnectorServiceDescriptor
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.10 $
 */

public abstract class AbstractServiceEnabledConnector extends AbstractConnector
{
    /**
     * Holds the service configuration for this connector
     */
    protected ConnectorServiceDescriptor serviceDescriptor;

    protected Properties serviceOverrides;

    public void doInitialise() throws InitialisationException
    {
        initFromServiceDescriptor();
    }

    public void initialiseFromUrl(UMOEndpointURI endpointUri) throws InitialisationException
    {
        if (!supportsProtocol(endpointUri.getFullScheme())) {
            throw new InitialisationException(new Message(Messages.SCHEME_X_NOT_COMPATIBLE_WITH_CONNECTOR_X,
                                                          getProtocol(),
                                                          getClass().getName()), this);
        }
        Properties props = new Properties();
        props.putAll(endpointUri.getParams());
        // auto set username and password
        if (endpointUri.getUserInfo() != null) {
            props.setProperty("username", endpointUri.getUsername());
            String passwd = endpointUri.getPassword();
            if (passwd != null) {
                props.setProperty("password", passwd);
            }
        }
        String host = endpointUri.getHost();
        if (host != null) {
            props.setProperty("hostname", host);
            props.setProperty("host", host);
        }
        if (endpointUri.getPort() > -1) {
            props.setProperty("port", String.valueOf(endpointUri.getPort()));
        }
        // try
        // {
        BeanUtils.populateWithoutFail(this, props, true);
        // } catch (InvocationTargetException e)
        // {
        // throw new InitialisationException(new
        // Message(Messages.FAILED_TO_SET_PROPERTIES_ON_X, "Connector"), e,
        // this);
        // }
    }

    protected synchronized void initFromServiceDescriptor() throws InitialisationException
    {
        try {
            serviceDescriptor = ConnectorFactory.getServiceDescriptor(getProtocol().toLowerCase(), serviceOverrides);

            if (serviceDescriptor.getDispatcherFactory() != null) {
                logger.info("Loading DispatcherFactory: " + serviceDescriptor.getDispatcherFactory());
                dispatcherFactory = serviceDescriptor.createDispatcherFactory();
            }

            defaultInboundTransformer = serviceDescriptor.createInboundTransformer();
            defaultOutboundTransformer = serviceDescriptor.createOutboundTransformer();
            defaultResponseTransformer = serviceDescriptor.createResponseTransformer();

            // set any manager default properties for the connector
            // these are set on the Manager with a protocol i.e.
            // jms.specification=1.1
            //This provides a really convenient way to set properties on object form unit
            //tests
            Map props = PropertiesHelper.getPropertiesWithPrefix(MuleManager.getInstance().getProperties(), getProtocol().toLowerCase());
            if (props.size() > 0) {
                props = PropertiesHelper.removeNamspaces(props);
                org.mule.util.BeanUtils.populateWithoutFail(this, props, true);
            }
        } catch (Exception e) {
            throw new InitialisationException(e, this);
        }
    }

    protected ConnectorServiceDescriptor getServiceDescriptor()
    {
        if (serviceDescriptor == null) {
            throw new IllegalStateException("This connector has not yet been initialised: " + name);
        }
        return serviceDescriptor;
    }

    public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception
    {
        return getServiceDescriptor().createMessageReceiver(this, component, endpoint);
    }

    /**
     * Gets a <code>UMOMessageAdapter</code> for the endpoint for the given
     * message (data)
     * 
     * @param message the data with which to initialise the
     *            <code>UMOMessageAdapter</code>
     * @return the <code>UMOMessageAdapter</code> for the endpoint
     * @throws org.mule.umo.MessagingException if the message parameter is not
     *             supported
     * @see org.mule.umo.provider.UMOMessageAdapter
     */
    public UMOMessageAdapter getMessageAdapter(Object message) throws MessagingException
    {
        try {
            return serviceDescriptor.createMessageAdapter(message);
        } catch (ConnectorServiceException e) {
            throw new MessagingException(new Message(Messages.FAILED_TO_CREATE_X, "Message Adapter"), message, e);
        }
    }

    public Map getServiceOverrides()
    {
        return serviceOverrides;
    }

    public void setServiceOverrides(Map serviceOverrides)
    {
        this.serviceOverrides = new Properties();
        this.serviceOverrides.putAll(serviceOverrides);
    }
}
