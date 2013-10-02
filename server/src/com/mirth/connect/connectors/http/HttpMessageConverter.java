/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.mirth.connect.donkey.util.DonkeyElement;

public class HttpMessageConverter {
    private Logger logger = Logger.getLogger(this.getClass());

    public String httpRequestToXml(HttpRequestMessage request) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            DonkeyElement requestElement = new DonkeyElement(document.createElement("HttpRequest"));

            requestElement.addChildElement("RemoteAddress", request.getRemoteAddress());
            requestElement.addChildElement("RequestUrl", request.getRequestUrl());
            requestElement.addChildElement("Method", request.getMethod());
            requestElement.addChildElement("RequestPath", request.getQueryString());
            requestElement.addChildElement("RequestContextPath", new URL(request.getRequestUrl()).getPath());

            if (!request.getParameters().isEmpty()) {
                DonkeyElement parametersElement = requestElement.addChildElement("Parameters");

                for (Entry<String, Object> entry : request.getParameters().entrySet()) {
                    if (entry.getValue() instanceof List<?>) {
                        String name = entry.getKey().substring(0, entry.getKey().indexOf("[]"));

                        for (String value : (List<String>) entry.getValue()) {
                            parametersElement.addChildElement(name, value);
                        }
                    } else {
                        parametersElement.addChildElement(entry.getKey(), entry.getValue().toString());
                    }
                }
            }

            DonkeyElement headerElement = requestElement.addChildElement("Header");

            for (Entry<String, String> entry : request.getHeaders().entrySet()) {
                headerElement.addChildElement(entry.getKey(), entry.getValue());
            }

            DonkeyElement contentElement = requestElement.addChildElement("Content");

            String content = null;
            // If the request is GZIP encoded, uncompress the content
            if ("gzip".equals(request.getHeaders().get("Content-Encoding"))) {
                content = HttpUtil.uncompressGzip(request.getContent(), HttpUtil.getCharset(request.getContentType()));
            } else {
                content = request.getContent();
            }

            if (isBinaryContentType(request.getContentType())) {
                contentElement.setTextContent(new String(Base64.encodeBase64Chunked(content.getBytes())));
                contentElement.setAttribute("encoding", "Base64");
            } else {
                contentElement.setTextContent(content);
            }

            return requestElement.toXml();
        } catch (Exception e) {
            logger.error("Error converting HTTP request.", e);
        }

        return null;
    }

    public String httpResponseToXml(String status, Header[] headers, String content) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            DonkeyElement requestElement = new DonkeyElement(document.createElement("HttpResponse"));

            requestElement.addChildElement("Status", status);

            DonkeyElement headerElement = requestElement.addChildElement("Header");

            for (Header header : headers) {
                DonkeyElement fieldElement = headerElement.addChildElement("Field");
                fieldElement.addChildElement("Name", header.getName());
                fieldElement.addChildElement("Value", header.getValue());
            }

            requestElement.addChildElement("Body", content);

            return requestElement.toXml();
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

    private boolean isBinaryContentType(String contentType) {
        return StringUtils.startsWithAny(contentType, new String[] { "application/", "image/", "video/", "audio/" });
    }
}