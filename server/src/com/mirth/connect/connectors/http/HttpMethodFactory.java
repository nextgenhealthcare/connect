/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class HttpMethodFactory {
    private Logger logger = Logger.getLogger(this.getClass());

    public HttpMethodFactory() {

    }

    public HttpMethod createHttpMethod(String method, String address, String content, String contentType, String charset, boolean isMultipart, Map<String, String> headers, Map<String, String> parameters) throws Exception {
        HttpMethod httpMethod = null;
        
        // create the method and set the content
        
        if ("GET".equalsIgnoreCase(method)) {
            httpMethod = new GetMethod(address);
        } else if ("POST".equalsIgnoreCase(method)) {
            PostMethod postMethod = new PostMethod(address);

            if (isMultipart) {
                logger.debug("setting multipart file content");
                File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
                FileUtils.writeStringToFile(tempFile, content, charset);
                Part[] parts = new Part[] { new FilePart(tempFile.getName(), tempFile) };
                postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
            } else {
                postMethod.setRequestEntity(new StringRequestEntity(content, contentType, charset));
            }

            httpMethod = postMethod;
        } else if ("PUT".equalsIgnoreCase(method)) {
            PutMethod putMethod = new PutMethod(address);
            putMethod.setRequestEntity(new StringRequestEntity(content, contentType, charset));
            httpMethod = putMethod;
        } else if ("DELETE".equalsIgnoreCase(method)) {
            httpMethod = new DeleteMethod(address);
        }
        
        // set the query parameters (for all methods)
        
        NameValuePair[] queryParams = new NameValuePair[parameters.size()];
        int index = 0;
        
        for (Entry<String, String> parameterEntry : parameters.entrySet()) {
            queryParams[index] = new NameValuePair(parameterEntry.getKey(), parameterEntry.getValue());
            index++;
            logger.debug("setting query parameter: [" + parameterEntry.getKey() + ", " + parameterEntry.getValue() + "]");
        }
        
        httpMethod.setQueryString(queryParams);
        
        // set the headers
        
        for (Entry<String, String> headerEntry : headers.entrySet()) {
            httpMethod.setRequestHeader(new Header(headerEntry.getKey(), headerEntry.getValue()));
            logger.debug("setting method header: [" + headerEntry.getKey() + ", " + headerEntry.getValue() + "]");
        }

        return httpMethod;
    }
}
