/*
 * $Header: /home/projects/mule/scm/mule/providers/http/src/java/org/mule/providers/http/transformers/HttpClientMethodResponseToObject.java,v 1.5 2005/11/12 09:04:17 rossmason Exp $
 * $Revision: 1.5 $
 * $Date: 2005/11/12 09:04:17 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.http.transformers;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.mule.config.MuleProperties;
import org.mule.impl.MuleMessage;
import org.mule.providers.http.HttpConstants;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.UMOMessage;
import org.mule.umo.transformer.TransformerException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>HttpClientMethodResponseToObject</code> transforms a http client
 * response to a MuleMessage.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.5 $
 */

public class HttpClientMethodResponseToObject extends AbstractTransformer
{
    public HttpClientMethodResponseToObject()
    {
        registerSourceType(HttpMethod.class);
        setReturnClass(UMOMessage.class);
    }

    public Object doTransform(Object src) throws TransformerException
    {
        Object msg;
        HttpMethod httpMethod = (HttpMethod) src;
        Header contentType = httpMethod.getResponseHeader(HttpConstants.HEADER_CONTENT_TYPE);
        try {
            if (contentType != null && !contentType.getValue().startsWith("text/")) {
                msg = httpMethod.getResponseBody();
            } else {
                msg = httpMethod.getResponseBodyAsString();
            }
        } catch (IOException e) {
            throw new TransformerException(this, e);
        }
        // Standard headers
        Map headerProps = new HashMap();
        Header[] headers = httpMethod.getRequestHeaders();
        String name;
        for (int i = 0; i < headers.length; i++) {
            name = headers[i].getName();
            if (name.startsWith("X-" + MuleProperties.PROPERTY_PREFIX)) {
                name = name.substring(2);
            }
            headerProps.put(headers[i].getName(), headers[i].getValue());
        }
        // Set Mule Properties

        return new MuleMessage(msg, headerProps);
    }
}
