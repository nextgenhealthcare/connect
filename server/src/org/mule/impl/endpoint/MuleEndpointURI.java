/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package org.mule.impl.endpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleManager;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.service.ConnectorFactory;
import org.mule.providers.service.ConnectorFactoryException;
import org.mule.providers.service.ConnectorServiceDescriptor;
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.util.PropertiesHelper;

/**
 * <code>MuleEndpointURI</code> is used to determine how a message is sent of
 * received. The url defines the protocol, the endpointUri destination of the
 * message and optionally the endpoint to use when dispatching the event. Mule
 * urls take the form of -
 * 
 * protocol://[host]:[port]/[provider]/endpointUri or
 * protocol://[host]:[port]/endpointUri i.e.
 * 
 * vm://localhost/vmProvider/my.object or vm://my.object
 * 
 * The protocol can be any of any conector registered with Mule. The endpoint
 * name if specified must be the name of a register global endpoint
 * 
 * The endpointUri can be any endpointUri recognised by the endpoint type.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.17 $
 */

public class MuleEndpointURI implements UMOEndpointURI
{
    /**
     * logger used by this class
     */
    protected static transient Log logger = LogFactory.getLog(MuleEndpointURI.class);

    private String address;
    private String filterAddress;
    private String endpointName;
    private String connectorName;
    private String transformers;
    private String responseTransformers;
    private int createConnector = ConnectorFactory.GET_OR_CREATE_CONNECTOR;
    private Properties params = new Properties();
    private URI uri;
    private String userInfo;
    private String schemeMetaInfo;
    private String resourceInfo;

    MuleEndpointURI(String address,
                    String endpointName,
                    String connectorName,
                    String transformers,
                    String responseTransformers,
                    int createConnector,
                    Properties properties,
                    URI uri,
                    String userInfo)
    {
        this(address, endpointName, connectorName, transformers, responseTransformers, createConnector, properties, uri);
        if (userInfo != null) {
            this.userInfo = userInfo;
        }
    }

    public MuleEndpointURI(String address,
                           String endpointName,
                           String connectorName,
                           String transformers,
                           String responseTransformers,
                           int createConnector,
                           Properties properties,
                           URI uri)
    {
        this.address = address;
        this.endpointName = endpointName;
        this.connectorName = connectorName;
        this.transformers = transformers;
        this.responseTransformers = responseTransformers;
        this.createConnector = createConnector;
        this.params = properties;
        this.uri = uri;
        this.userInfo = uri.getUserInfo();
        if (properties != null) {
            resourceInfo = (String) properties.remove("resourceInfo");
        }
    }

    public MuleEndpointURI(UMOEndpointURI endpointUri)
    {
        initialise(endpointUri);
    }

    public MuleEndpointURI(UMOEndpointURI endpointUri, String filterAddress)
    {
        initialise(endpointUri);
        this.filterAddress = filterAddress;
    }
    
    public MuleEndpointURI(String uri) throws MalformedEndpointException {
        this(uri, null);
    }

    public MuleEndpointURI(String uri, String channelId) throws MalformedEndpointException
    {
        String uriIdentifier = MuleManager.getInstance().lookupEndpointIdentifier(uri, uri);
        if (!uriIdentifier.equals(uri)) {
            endpointName = uri;
            uri = uriIdentifier;
        }
        
       	TemplateValueReplacer replacer = new TemplateValueReplacer();
       	
       	if (channelId != null) {
       	    uri = replacer.replaceValues(uri, channelId);
       	} else {
       	    uri = replacer.replaceValues(uri);
       	}
        
       	uri = uri.trim().replaceAll("%","%25").replaceAll(" ", "%20").replaceAll("\\$", "%24").replaceAll("\\{", "%7B").replaceAll("\\}", "%7D").replaceAll("\\(", "%28").replaceAll("\\)", "%29").replaceAll("#", "%23");
        
        if (!validateUrl(uri)) {
            throw new MalformedEndpointException(uri);
        }
        try {
            schemeMetaInfo = retrieveSchemeMetaInfo(uri);
            if (schemeMetaInfo != null) {
                uri = uri.replaceFirst(schemeMetaInfo + ":", "");
            }
            this.uri = new URI(uri);
            this.userInfo = this.uri.getRawUserInfo();
        } catch (URISyntaxException e) {
            throw new MalformedEndpointException(uri, e);
        }

        try {
            String scheme = (schemeMetaInfo == null ? this.uri.getScheme() : schemeMetaInfo);
            ConnectorServiceDescriptor csd = ConnectorFactory.getServiceDescriptor(scheme);
            EndpointBuilder builder = csd.createEndpointBuilder();
            UMOEndpointURI built = builder.build(this.uri);
            initialise(built);
        } catch (ConnectorFactoryException e) {
            throw new MalformedEndpointException(e);
        }
    }

    private String retrieveSchemeMetaInfo(String url)
    {
        int i = url.indexOf(":");
        if (i == -1) {
            return null;
        }
        if (url.charAt(i + 1) == '/') {
            return null;
        } else {
            return url.substring(0, i);
        }
    }

    protected boolean validateUrl(String url)
    {
        return (url.indexOf(":/") > 0);
    }

    private void initialise(UMOEndpointURI endpointUri)
    {
        this.address = endpointUri.getAddress();
        this.endpointName = endpointUri.getEndpointName();
        this.connectorName = endpointUri.getConnectorName();
        this.transformers = endpointUri.getTransformers();
        this.responseTransformers = endpointUri.getResponseTransformers();
        this.createConnector = endpointUri.getCreateConnector();
        this.params = endpointUri.getParams();
        this.uri = endpointUri.getUri();
        this.resourceInfo = endpointUri.getResourceInfo();
        this.userInfo = endpointUri.getUserInfo();
    }

    public String getAddress()
    {
        return address;
    }

    public String getEndpointName()
    {
        return "".equals(endpointName) ? null : endpointName;
    }

    public static boolean isMuleUri(String url)
    {
        return url.indexOf(":/") != -1;
    }

    public Properties getParams()
    {
        // todo fix this so that the query string properties are not lost.
        // not sure whats causing this at the moment
        if (params.size() == 0 && getQuery() != null) {
            params = PropertiesHelper.getPropertiesFromQueryString(getQuery());
        }
        return params;
    }

    public Properties getUserParams()
    {
        Properties p = new Properties();
        p.putAll(params);
        p.remove(PROPERTY_ENDPOINT_NAME);
        p.remove(PROPERTY_ENDPOINT_URI);
        p.remove(PROPERTY_CREATE_CONNECTOR);
        p.remove(PROPERTY_TRANSFORMERS);
        return p;
    }

    public URI parseServerAuthority() throws URISyntaxException
    {
        return uri.parseServerAuthority();
    }

    public URI normalize()
    {
        return uri.normalize();
    }

    public URI resolve(URI uri)
    {
        return uri.resolve(uri);
    }

    public URI resolve(String str)
    {
        return uri.resolve(str);
    }

    public URI relativize(URI uri)
    {
        return uri.relativize(uri);
    }

    public String getScheme()
    {
        return uri.getScheme();
    }

    public String getFullScheme()
    {
        return (schemeMetaInfo == null ? uri.getScheme() : schemeMetaInfo + ":" + uri.getScheme());

    }

    public boolean isAbsolute()
    {
        return uri.isAbsolute();
    }

    public boolean isOpaque()
    {
        return uri.isOpaque();
    }

    public String getRawSchemeSpecificPart()
    {
        return uri.getRawSchemeSpecificPart();
    }

    public String getSchemeSpecificPart()
    {
        return uri.getSchemeSpecificPart();
    }

    public String getRawAuthority()
    {
        return uri.getRawAuthority();
    }

    public String getAuthority()
    {
        return uri.getAuthority();
    }

    public String getRawUserInfo()
    {
        return uri.getRawUserInfo();
    }

    public String getUserInfo()
    {
        return userInfo;
    }

    public String getHost()
    {
        return uri.getHost();
    }

    public int getPort()
    {
        return uri.getPort();
    }

    public String getRawPath()
    {
        return uri.getRawPath();
    }

    public String getPath()
    {
        return uri.getPath();
    }

    public String getRawQuery()
    {
        return uri.getRawQuery();
    }

    public String getQuery()
    {
        return uri.getQuery();
    }

    public String getRawFragment()
    {
        return uri.getRawFragment();
    }

    public String getFragment()
    {
        return uri.getFragment();
    }

    public String toString()
    {
        return uri.toASCIIString();
    }

    public String getTransformers()
    {
        return transformers;
    }

    public int getCreateConnector()
    {
        return createConnector;
    }

    public URI getUri()
    {
        return uri;
    }

    public String getConnectorName()
    {
        return connectorName;
    }

    public String getSchemeMetaInfo()
    {
        return (schemeMetaInfo == null ? uri.getScheme() : schemeMetaInfo);
    }

    public String getResourceInfo()
    {
        return resourceInfo;
    }

    public String getFilterAddress()
    {
        return filterAddress;
    }

    public void setEndpointName(String name)
    {
        endpointName = name;
    }

    public String getUsername()
    {
        if (userInfo != null && !"".equals(userInfo)) {
            int i = userInfo.indexOf(":");
            if (i == -1) {
                return userInfo;
            } else {
                return userInfo.substring(0, i);
            }
        }
        return null;
    }

    public String getResponseTransformers() {
        return responseTransformers;
    }

    public String getPassword()
    {
        if (userInfo != null && !"".equals(userInfo)) {
            int i = userInfo.indexOf(":");
            if (i > -1) {
                return userInfo.substring(i + 1);
            }
        }
        return null;
    }

    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MuleEndpointURI)) {
            return false;
        }

        final MuleEndpointURI muleEndpointURI = (MuleEndpointURI) o;

        if (createConnector != muleEndpointURI.createConnector) {
            return false;
        }
        if (address != null ? !address.equals(muleEndpointURI.address) : muleEndpointURI.address != null) {
            return false;
        }
        if (connectorName != null ? !connectorName.equals(muleEndpointURI.connectorName)
                : muleEndpointURI.connectorName != null) {
            return false;
        }
        if (endpointName != null ? !endpointName.equals(muleEndpointURI.endpointName)
                : muleEndpointURI.endpointName != null) {
            return false;
        }
        if (filterAddress != null ? !filterAddress.equals(muleEndpointURI.filterAddress)
                : muleEndpointURI.filterAddress != null) {
            return false;
        }
        if (params != null ? !params.equals(muleEndpointURI.params) : muleEndpointURI.params != null) {
            return false;
        }
        if (resourceInfo != null ? !resourceInfo.equals(muleEndpointURI.resourceInfo)
                : muleEndpointURI.resourceInfo != null) {
            return false;
        }
        if (schemeMetaInfo != null ? !schemeMetaInfo.equals(muleEndpointURI.schemeMetaInfo)
                : muleEndpointURI.schemeMetaInfo != null) {
            return false;
        }
        if (transformers != null ? !transformers.equals(muleEndpointURI.transformers)
                : muleEndpointURI.transformers != null) {
            return false;
        }
        if (responseTransformers != null ? !responseTransformers.equals(muleEndpointURI.responseTransformers)
                : muleEndpointURI.responseTransformers != null) {
            return false;
        }
        if (uri != null ? !uri.equals(muleEndpointURI.uri) : muleEndpointURI.uri != null) {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (address != null ? address.hashCode() : 0);
        result = 29 * result + (filterAddress != null ? filterAddress.hashCode() : 0);
        result = 29 * result + (endpointName != null ? endpointName.hashCode() : 0);
        result = 29 * result + (connectorName != null ? connectorName.hashCode() : 0);
        result = 29 * result + (transformers != null ? transformers.hashCode() : 0);
        result = 29 * result + (responseTransformers != null ? responseTransformers.hashCode() : 0);
        result = 29 * result + createConnector;
        result = 29 * result + (params != null ? params.hashCode() : 0);
        result = 29 * result + (uri != null ? uri.hashCode() : 0);
        result = 29 * result + (schemeMetaInfo != null ? schemeMetaInfo.hashCode() : 0);
        result = 29 * result + (resourceInfo != null ? resourceInfo.hashCode() : 0);
        return result;
    }
}
