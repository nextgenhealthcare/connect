/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;

public class HttpMessageConverter {
    private static Logger logger = Logger.getLogger(HttpMessageConverter.class);

    public static String httpRequestToXml(HttpRequestMessage request) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            DonkeyElement requestElement = new DonkeyElement(document.createElement("HttpRequest"));

            requestElement.addChildElement("RemoteAddress", request.getRemoteAddress());
            requestElement.addChildElement("RequestUrl", request.getRequestUrl());
            requestElement.addChildElement("Method", request.getMethod());
            requestElement.addChildElement("RequestPath", request.getQueryString());
            requestElement.addChildElement("RequestContextPath", request.getContextPath());

            if (!request.getParameters().isEmpty()) {
                DonkeyElement parametersElement = requestElement.addChildElement("Parameters");

                for (Entry<String, Object> entry : request.getParameters().entrySet()) {
                    if (entry.getValue() instanceof String[]) {
                        for (String value : (String[]) entry.getValue()) {
                            parametersElement.addChildElement(entry.getKey(), value);
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

            processContent(requestElement.addChildElement("Content"), request.getContent(), request.getContentType(), false);

            return requestElement.toXml();
        } catch (Exception e) {
            logger.error("Error converting HTTP request.", e);
        }

        return null;
    }
    
    public static String contentToXml(Object content, ContentType contentType, boolean parseMultipart) throws DonkeyElementException, MessagingException, IOException, ParserConfigurationException {
        DonkeyElement contentElement = new DonkeyElement("<Content/>");
        processContent(contentElement, content, contentType, parseMultipart);
        return contentElement.toXml();
    }

    private static void processContent(DonkeyElement contentElement, Object content, ContentType contentType, boolean parseMultipart) throws DonkeyElementException, MessagingException, IOException {
        if (parseMultipart && content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            DonkeyElement multipartElement = contentElement.addChildElement("Multipart");

            String boundary = contentType.getParameter("boundary");
            if (StringUtils.isNotBlank(boundary)) {
                multipartElement.setAttribute("boundary", boundary);
            }

            if (StringUtils.isNotEmpty(multipart.getPreamble())) {
                multipartElement.addChildElement("Preamble", multipart.getPreamble());
            }

            for (int partIndex = 0; partIndex < multipart.getCount(); partIndex++) {
                BodyPart part = multipart.getBodyPart(partIndex);
                DonkeyElement partElement = multipartElement.addChildElement("Part");
                DonkeyElement headersElement = partElement.addChildElement("Headers");
                ContentType partContentType = contentType;

                for (Enumeration<javax.mail.Header> en = part.getAllHeaders(); en.hasMoreElements();) {
                    javax.mail.Header header = en.nextElement();
                    headersElement.addChildElement(header.getName(), header.getValue());

                    if (header.getValue().equalsIgnoreCase("Content-Type")) {
                        partContentType = ContentType.parse(header.getValue());
                    }
                }

                processContent(partElement.addChildElement("Content"), part.getContent(), partContentType, true);
            }
        } else if (content instanceof InputStream) {
            contentElement.setAttribute("encoding", "Base64");
            contentElement.setTextContent(new String(Base64.encodeBase64Chunked(IOUtils.toByteArray((InputStream) content)), "US-ASCII"));
        } else {
            String stringContent = content != null ? content.toString() : "";

            if (isBinaryContentType(contentType.getMimeType())) {
                contentElement.setAttribute("encoding", "Base64");
                contentElement.setTextContent(new String(Base64.encodeBase64Chunked(stringContent.getBytes(getDefaultHttpCharset(contentType.getCharset().name()))), "US-ASCII"));
            } else {
                contentElement.setTextContent(stringContent);
            }
        }
    }

    public static String httpResponseToXml(String status, Header[] headers, String content) {
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

    public static String getDefaultHttpCharset(String charset) {
        if (charset == null) {
            return "ISO-8859-1"; // default charset for HTTP
        } else {
            return charset;
        }
    }

    public static Map<String, String> convertFieldEnumerationToMap(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<String, String>();

        for (Enumeration<String> enumeration = request.getHeaderNames(); enumeration.hasMoreElements();) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            headers.put(name, value);
        }

        return headers;
    }

    private static boolean isBinaryContentType(String contentType) {
        return StringUtils.startsWithAny(contentType, new String[] { "application/", "image/",
                "video/", "audio/" });
    }
}