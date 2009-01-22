/*
 * $Header: /home/projects/mule/scm/mule/mule/src/java/org/mule/providers/service/ConnectorFactory.java,v 1.10 2005/11/01 14:56:36 rossmason Exp $
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
package org.mule.providers.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleException;
import org.mule.MuleManager;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.umo.endpoint.EndpointException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.transformer.UMOTransformer;
import org.mule.util.ClassHelper;
import org.mule.util.MuleObjectHelper;
import org.mule.util.ObjectFactory;
import org.mule.util.PropertiesHelper;
import org.mule.util.SpiHelper;

import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.ExtensionController;

/**
 * <code>ConnectorFactory</code> can be used for generically creating
 * endpoints from an url. Note that for some endpoints, the url alone is not
 * enough to create the endpoint if a connector for the endpoint has not already
 * been configured with the Mule Manager.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.10 $
 */

public class ConnectorFactory
{
    public static final String PROVIDER_SERVICES_PATH = "org/mule/providers";

    /**
     * logger used by this class
     */
    protected static transient Log logger = LogFactory.getLog(ConnectorFactory.class);

    public static final int GET_OR_CREATE_CONNECTOR = 0;
    public static final int ALWAYS_CREATE_CONNECTOR = 1;
    public static final int NEVER_CREATE_CONNECTOR = 2;
    public static final int USE_CONNECTOR = 3;

    private static Map csdCache = new HashMap();

    public static UMOEndpoint createEndpoint(UMOEndpointURI uri, String type) throws EndpointException
    {
        String scheme = uri.getFullScheme();
        UMOConnector connector = null;
        try {
            if (uri.getCreateConnector() == ALWAYS_CREATE_CONNECTOR) {
                connector = createConnector(uri);
                MuleManager.getInstance().registerConnector(connector);
            } else if (uri.getCreateConnector() == NEVER_CREATE_CONNECTOR) {
                connector = getConnectorByProtocol(scheme);
            } else if (uri.getConnectorName() != null) {
                connector = MuleManager.getInstance().lookupConnector(uri.getConnectorName());
                if (connector == null) {
                    throw new ConnectorFactoryException(new Message(Messages.X_NOT_REGISTERED_WITH_MANAGER,
                                                                    "Connector: " + uri.getConnectorName()));
                }
            } else {
                connector = getConnectorByProtocol(scheme);
                if (connector == null) {
                    connector = createConnector(uri);
                    MuleManager.getInstance().registerConnector(connector);
                }
            }
        } catch (Exception e) {
            throw new ConnectorFactoryException(e);
        }

        if (connector == null) {
            Message m = new Message(Messages.FAILED_TO_CREATE_X_WITH_X, "Endpoint", "Uri: " + uri);
            m.setNextMessage(new Message(Messages.X_IS_NULL, "connector"));
            throw new ConnectorFactoryException(m);

        }

        UMOEndpoint endpoint = new MuleEndpoint();
        endpoint.setConnector(connector);
        endpoint.setEndpointURI(uri);
        String name = uri.getEndpointName();
        if (name == null) {
            name = "_" + scheme + "Endpoint#" + endpoint.hashCode();
        }
        endpoint.setName(name);

        if (type != null) {
            endpoint.setType(type);
            UMOTransformer trans = getTransformer(uri, connector, (UMOEndpoint.ENDPOINT_TYPE_RECEIVER.equals(type) ? 0 : 1));
            endpoint.setTransformer(trans);
            if(UMOEndpoint.ENDPOINT_TYPE_RECEIVER.equals(type)) {
                //set the response transformer
                trans = getTransformer(uri, connector, 2);
                endpoint.setResponseTransformer(trans);
            }
        }
        return endpoint;
    }

    /**
     *
     * @param url
     * @param cnn
     * @param type 0=inbound, 1=outbound, 2=response
     * @return
     * @throws ConnectorFactoryException
     */
    private static UMOTransformer getTransformer(UMOEndpointURI url, UMOConnector cnn, int type) throws ConnectorFactoryException
    {
        UMOTransformer trans = null;
        String transId = null;
        if (type==2) {
            transId = url.getResponseTransformers();
        } else  {
            transId = url.getTransformers();
        }

        if (transId != null) {
            try {
                trans = MuleObjectHelper.getTransformer(transId, ",");
            } catch (MuleException e) {
                throw new ConnectorFactoryException(e);
            }
        } else {
            // Get connector specific overrides to set on the descriptor
            Properties overrides = new Properties();
            if (cnn instanceof AbstractServiceEnabledConnector) {
                Map so = ((AbstractServiceEnabledConnector) cnn).getServiceOverrides();
                if (so != null) {
                    overrides.putAll(so);
                }
            }

            String scheme = url.getSchemeMetaInfo();

            ConnectorServiceDescriptor csd = getServiceDescriptor(scheme, overrides);
            if (type==0) {
                trans = csd.createInboundTransformer();
            } else if(type==1) {
                trans = csd.createOutboundTransformer();
            } else {
                trans = csd.createResponseTransformer();
            }
        }
        return trans;
    }

    /**
     * Creates an uninitialied connector from the provided MuleEndpointURI. The
     * scheme is used to determine what kind of connector to create. Any params
     * set on the uri can be used to initialise bean properties on the created
     * connector. <p/> Note that the initalise method will need to be called on
     * the connector returned. This is so that developers can control when the
     * connector initialisation takes place as this is likely to initialse all
     * connecotr resources.
     * 
     * @param url the MuleEndpointURI url to create the connector with
     * @return a new Connector
     * @throws ConnectorFactoryException
     */
    public static UMOConnector createConnector(UMOEndpointURI url) throws ConnectorFactoryException
    {
        String scheme = url.getSchemeMetaInfo();

        UMOConnector connector = null;
        ConnectorServiceDescriptor psd;

        psd = getServiceDescriptor(scheme);
        // Make sure we can create the endpoint/connector using this service
        // method
        if (psd.getServiceError() != null) {
            throw new ConnectorServiceException(Message.createStaticMessage(psd.getServiceError()));
        }
        // if there is a factory, use it
        try {
            if (psd.getConnectorFactory() != null) {
                ObjectFactory factory = (ObjectFactory) ClassHelper.loadClass(psd.getConnectorFactory(),
                                                                              ConnectorFactory.class).newInstance();
                connector = (UMOConnector) factory.create();
            } else {
                if (psd.getConnector() != null) {
                    connector = (UMOConnector) ClassHelper.loadClass(psd.getConnector(), ConnectorFactory.class)
                                                          .newInstance();
                    if (connector instanceof AbstractServiceEnabledConnector) {
                        ((AbstractServiceEnabledConnector) connector).initialiseFromUrl(url);
                    }
                } else {
                    throw new ConnectorFactoryException(new Message(Messages.X_NOT_SET_IN_SERVICE_X,
                                                                    "Connector",
                                                                    scheme));
                }
            }
        } catch (ConnectorFactoryException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectorFactoryException(new Message(Messages.FAILED_TO_CREATE_X_WITH_X, "Endpoint", url), e);
        }

        if (connector.getName() == null) {
            connector.setName("_" + scheme + "Connector#" + connector.hashCode());
        }
        // set any manager default properties for the connector
        // these are set on the Manager with a protocol i.e.
        // jms.specification=1.1
        Map props = PropertiesHelper.getPropertiesWithPrefix(MuleManager.getInstance().getProperties(),
                                                             connector.getProtocol().toLowerCase());
        if (props.size() > 0) {
            props = PropertiesHelper.removeNamspaces(props);
            org.mule.util.BeanUtils.populateWithoutFail(connector, props, true);
        }

        return connector;
    }

    public static ConnectorServiceDescriptor getServiceDescriptor(String protocol) throws ConnectorFactoryException
    {
        return getServiceDescriptor(protocol, null);
    }

    public static ConnectorServiceDescriptor getServiceDescriptor(String protocol, Properties overrides)
            throws ConnectorFactoryException
    {
        ConnectorServiceDescriptor csd = (ConnectorServiceDescriptor) csdCache.get(new CSDKey(protocol, overrides));
        if (csd == null) {
        	ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
        	ConnectorMetaData connectorMetaData = extensionController.getConnectorMetaDataByProtocol(protocol);
        	if (connectorMetaData != null && connectorMetaData.getProperties() != null){
        		csd = loadCsdFromProperties(protocol, overrides, null, (Properties) connectorMetaData.getProperties().clone());
        	}else{
        		String location = SpiHelper.SERVICE_ROOT + PROVIDER_SERVICES_PATH;
                InputStream is = SpiHelper.findServiceDescriptor(PROVIDER_SERVICES_PATH, protocol, ConnectorFactory.class);
                try {
                    if (is != null) {
                        Properties props = new Properties();
                        props.load(is);
                        csd = loadCsdFromProperties(protocol, overrides, location, props);
                    } else {
                        throw new ConnectorServiceNotFoundException(location + "/" + protocol);
                    }
                } catch (IOException e) {
                    throw new ConnectorFactoryException(new Message(Messages.FAILED_TO_ENDPOINT_FROM_LOCATION_X, location
                            + "/" + protocol), e);
                }
        	} 	
        }
        return csd;
    }

	private static ConnectorServiceDescriptor loadCsdFromProperties(String protocol, Properties overrides, String location, Properties props) throws ConnectorServiceException, ConnectorFactoryException {
		ConnectorServiceDescriptor csd;
		csd = new ConnectorServiceDescriptor(protocol, location, props);
		// set any overides on the descriptor
		csd.setOverrides(overrides);
		if (csd.getServiceFinder() != null) {
		    ConnectorServiceFinder finder = csd.createServiceFinder();
		    csd = finder.findService(protocol);
		}
		csdCache.put(new CSDKey(csd.getProtocol(), overrides), csd);
		return csd;
	}

    public static UMOConnector getOrCreateConnectorByProtocol(UMOEndpointURI uri) throws ConnectorFactoryException
    {
        return getOrCreateConnectorByProtocol(uri, uri.getCreateConnector());
    }

    public static UMOConnector getOrCreateConnectorByProtocol(UMOImmutableEndpoint endpoint)
            throws ConnectorFactoryException
    {
        return getOrCreateConnectorByProtocol(endpoint.getEndpointURI(), endpoint.getCreateConnector());
    }

    private static UMOConnector getOrCreateConnectorByProtocol(UMOEndpointURI uri, int create)
            throws ConnectorFactoryException
    {
        UMOConnector connector = getConnectorByProtocol(uri.getFullScheme());
        if (ConnectorFactory.ALWAYS_CREATE_CONNECTOR == create
                || (connector == null && create == ConnectorFactory.GET_OR_CREATE_CONNECTOR)) {
            connector = ConnectorFactory.createConnector(uri);
            try {
                BeanUtils.populate(connector, uri.getParams());
                MuleManager.getInstance().registerConnector(connector);

            } catch (Exception e) {
                throw new ConnectorFactoryException(new Message(Messages.FAILED_TO_SET_PROPERTIES_ON_X, "Connector"), e);
            }
        } else if (create == ConnectorFactory.NEVER_CREATE_CONNECTOR && connector == null) {
            logger.warn("There is no connector for protocol: " + uri.getScheme()
                    + " and 'createConnector' is set to NEVER.  Returning null");
        }
        return connector;
    }

    public static UMOConnector getConnectorByProtocol(String protocol)
    {
        UMOConnector connector;
        Map connectors = MuleManager.getInstance().getConnectors();
        for (Iterator iterator = connectors.values().iterator(); iterator.hasNext();) {
            connector = (UMOConnector) iterator.next();
            if (connector.supportsProtocol(protocol)) {
                return connector;
            }
        }
        return null;
    }

    private static class CSDKey
    {
        private Map overrides;
        private String protocol;

        public CSDKey(String protocol, Map overrides)
        {
            this.overrides = overrides;
            this.protocol = protocol;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (!(o instanceof CSDKey))
                return false;

            final CSDKey csdKey = (CSDKey) o;

            if (overrides != null ? !overrides.equals(csdKey.overrides) : csdKey.overrides != null)
                return false;
            if (!protocol.equals(csdKey.protocol))
                return false;

            return true;
        }

        public int hashCode()
        {
            int result;
            result = (overrides != null ? overrides.hashCode() : 0);
            result = 29 * result + protocol.hashCode();
            return result;
        }
    }
}
