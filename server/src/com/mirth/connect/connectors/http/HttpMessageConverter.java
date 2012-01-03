/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class HttpMessageConverter {
    private Logger logger = Logger.getLogger(this.getClass());

    public String httpRequestToXml(HttpRequestMessage request) {
        try {
            Element requestElement = new Element("HttpRequest");
            Document document = new Document(requestElement);

            addElement(requestElement, "RemoteAddress", request.getRemoteAddress());
            addElement(requestElement, "RequestUrl", request.getRequestUrl());
            addElement(requestElement, "Method", request.getMethod());
            addElement(requestElement, "RequestPath", request.getQueryString());
            addElement(requestElement, "RequestContextPath", new URL(request.getRequestUrl()).getPath());

            if (!request.getParameters().isEmpty()) {
                Element parametersElement = new Element("Parameters");

                for (Entry<String, Object> entry : request.getParameters().entrySet()) {
                    if (entry.getValue() instanceof List<?>) {
                        String name = entry.getKey().substring(0, entry.getKey().indexOf("[]"));

                        for (String value : (List<String>) entry.getValue()) {
                            addElement(parametersElement, name, value);
                        }
                    } else {
                        addElement(parametersElement, entry.getKey(), entry.getValue().toString());
                    }
                }

                requestElement.appendChild(parametersElement);
            }

            Element headerElement = new Element("Header");

            for (Entry<String, String> entry : request.getHeaders().entrySet()) {
                addElement(headerElement, entry.getKey(), entry.getValue());
            }

            requestElement.appendChild(headerElement);


            Element contentElement = new Element("Content");

            if (isBinaryContentType(request.getContentType())) {
                contentElement.appendChild(Base64.encodeBase64String(request.getContent().getBytes()));
                contentElement.addAttribute(new Attribute("encoding", "Base64"));
            } else {
                contentElement.appendChild(request.getContent());
            }
            
            requestElement.appendChild(contentElement);

            return document.toXML();
        } catch (Exception e) {
            logger.error("Error converting HTTP request.", e);
        }

        return null;
    }

    public String httpResponseToXml(String status, Header[] headers, String content) {
        try {
            Element requestElement = new Element("HttpResponse");
            Document document = new Document(requestElement);

            addElement(requestElement, "Status", status);

            Element headerElement = new Element("Header");

            for (Header header : headers) {
                Element fieldElement = new Element("Field");
                addElement(fieldElement, "Name", header.getName());
                addElement(fieldElement, "Value", header.getValue());
                headerElement.appendChild(fieldElement);
            }

            requestElement.appendChild(headerElement);

            /*
             * NOTE: "Body" is added as a CDATA element in the
             * documentSerializer constructor
             */
            Element contentElement = new Element("Body");
            contentElement.appendChild(content);
            requestElement.appendChild(contentElement);

            return document.toXML();
        } catch (Exception e) {
            logger.error("Error converting HTTP request.", e);
        }

        return null;
    }

    public String getDefaultHttpCharset(String charset) {
        if (charset == null) {
            return "ISO-8859-1"; // default charset for HTTP
        } else {
            return charset;
        }
    }

    public Map<String, String> convertFieldEnumerationToMap(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<String, String>();

        for (Enumeration<String> enumeration = request.getHeaderNames(); enumeration.hasMoreElements();) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            headers.put(name, value);
        }

        return headers;
    }

    private Element addElement(Element parent, String name, String textContent) {
        Element child = new Element(name);
        child.appendChild(textContent);
        parent.appendChild(child);
        return child;
    }
    
    private boolean isBinaryContentType(String contentType) {
        return StringUtils.startsWithAny(contentType, new String[] { "application/", "image/", "video/", "audio/" });
    }
}
