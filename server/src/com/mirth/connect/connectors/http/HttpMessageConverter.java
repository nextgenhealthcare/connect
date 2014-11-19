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
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeaderValueFormatter;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;

public class HttpMessageConverter {
    private static Logger logger = Logger.getLogger(HttpMessageConverter.class);

    private static BinaryContentTypeResolver defaultResolver = new BinaryContentTypeResolver() {
        @Override
        public boolean isBinaryContentType(ContentType contentType) {
            return StringUtils.startsWithAny(contentType.getMimeType(), new String[] {
                    "application/", "image/", "video/", "audio/" });
        }
    };

    public static String httpRequestToXml(HttpRequestMessage request, boolean parseMultipart, boolean includeMetadata, BinaryContentTypeResolver resolver) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            DonkeyElement requestElement = new DonkeyElement(document.createElement("HttpRequest"));

            if (includeMetadata) {
                requestElement.addChildElement("RemoteAddress", request.getRemoteAddress());
                requestElement.addChildElement("RequestUrl", request.getRequestUrl());
                requestElement.addChildElement("Method", request.getMethod());
                requestElement.addChildElement("RequestPath", request.getQueryString());
                requestElement.addChildElement("RequestContextPath", request.getContextPath());

                if (!request.getParameters().isEmpty()) {
                    DonkeyElement parametersElement = requestElement.addChildElement("Parameters");

                    for (Entry<String, List<String>> entry : request.getParameters().entrySet()) {
                        for (String value : entry.getValue()) {
                            parametersElement.addChildElement(entry.getKey(), value);
                        }
                    }
                }

                DonkeyElement headerElement = requestElement.addChildElement("Header");

                for (Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
                    for (String value : entry.getValue()) {
                        headerElement.addChildElement(entry.getKey(), value);
                    }
                }
            }

            processContent(requestElement.addChildElement("Content"), request.getContent(), request.getContentType(), parseMultipart, resolver);

            return requestElement.toXml();
        } catch (Exception e) {
            logger.error("Error converting HTTP request.", e);
        }

        return null;
    }

    public static String contentToXml(Object content, ContentType contentType, boolean parseMultipart, BinaryContentTypeResolver resolver) throws DonkeyElementException, MessagingException, IOException, ParserConfigurationException {
        DonkeyElement contentElement = new DonkeyElement("<Content/>");
        processContent(contentElement, content, contentType, parseMultipart, resolver);
        return contentElement.toXml();
    }

    private static void processContent(DonkeyElement contentElement, Object content, ContentType contentType, boolean parseMultipart, BinaryContentTypeResolver resolver) throws DonkeyElementException, MessagingException, IOException {
        if (resolver == null) {
            resolver = defaultResolver;
        }

        if (parseMultipart && content instanceof MimeMultipart) {
            contentElement.setAttribute("multipart", "yes");
            MimeMultipart multipart = (MimeMultipart) content;

            String boundary = contentType.getParameter("boundary");
            if (StringUtils.isNotBlank(boundary)) {
                contentElement.setAttribute("boundary", boundary);
            }

            if (StringUtils.isNotEmpty(multipart.getPreamble())) {
                contentElement.addChildElement("Preamble", multipart.getPreamble());
            }

            for (int partIndex = 0; partIndex < multipart.getCount(); partIndex++) {
                BodyPart part = multipart.getBodyPart(partIndex);
                DonkeyElement partElement = contentElement.addChildElement("Part");
                DonkeyElement headersElement = partElement.addChildElement("Headers");
                ContentType partContentType = contentType;

                for (Enumeration<javax.mail.Header> en = part.getAllHeaders(); en.hasMoreElements();) {
                    javax.mail.Header header = en.nextElement();
                    headersElement.addChildElement(header.getName(), header.getValue());

                    if (header.getName().equalsIgnoreCase("Content-Type")) {
                        try {
                            partContentType = ContentType.parse(header.getValue());
                        } catch (RuntimeException e) {
                        }
                    }
                }

                processContent(partElement.addChildElement("Content"), part.getContent(), partContentType, true, resolver);
            }
        } else {
            contentElement.setAttribute("multipart", "no");
            String charset = getDefaultHttpCharset(contentType.getCharset() != null ? contentType.getCharset().name() : null);

            // Call the resolver to determine if the content should be Base64 encoded
            if (resolver.isBinaryContentType(contentType)) {
                contentElement.setAttribute("encoding", "Base64");
                byte[] contentByteArray;

                if (content instanceof InputStream) {
                    contentByteArray = IOUtils.toByteArray((InputStream) content);
                } else if (content instanceof byte[]) {
                    contentByteArray = (byte[]) content;
                } else {
                    contentByteArray = (content != null ? content.toString() : "").getBytes(charset);
                }

                contentElement.setTextContent(new String(Base64Util.encodeBase64(contentByteArray), "US-ASCII"));
            } else {
                if (content instanceof InputStream) {
                    contentElement.setTextContent(IOUtils.toString((InputStream) content, charset));
                } else if (content instanceof byte[]) {
                    contentElement.setTextContent(new String((byte[]) content, charset));
                } else {
                    contentElement.setTextContent(content != null ? content.toString() : "");
                }
            }
        }
    }

    public static String httpResponseToXml(String status, Map<String, List<String>> headers, Object content, ContentType contentType, boolean parseMultipart, boolean includeMetadata, BinaryContentTypeResolver resolver) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            DonkeyElement requestElement = new DonkeyElement(document.createElement("HttpResponse"));

            if (includeMetadata) {
                requestElement.addChildElement("Status", status);

                DonkeyElement headerElement = requestElement.addChildElement("Header");

                for (Entry<String, List<String>> entry : headers.entrySet()) {
                    for (String value : entry.getValue()) {
                        DonkeyElement fieldElement = headerElement.addChildElement("Field");
                        fieldElement.addChildElement("Name", entry.getKey());
                        fieldElement.addChildElement("Value", value);
                    }
                }
            }

            processContent(requestElement.addChildElement("Body"), content, contentType, parseMultipart, resolver);

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

    public static Map<String, List<String>> convertFieldEnumerationToMap(HttpServletRequest request) {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();

        for (Enumeration<String> enumeration = request.getHeaderNames(); enumeration.hasMoreElements();) {
            String name = enumeration.nextElement();
            Enumeration headerNames = request.getHeaders(name);

            while (headerNames.hasMoreElements()) {
                List<String> list = headers.get(name);

                if (list == null) {
                    list = new ArrayList<String>();
                    headers.put(name, list);
                }

                list.add((String) headerNames.nextElement());
            }
        }

        return headers;
    }

    /**
     * This method takes in a ContentType and returns an equivalent ContentType, only overriding the
     * charset. This is needed because ContentType.withCharset(charset) does not correctly handle
     * headers with multiple parameters. Parsing a ContentType from a String works, calling
     * toString() to get the correct header value works, but there's no way from ContentType itself
     * to update a specific parameter in-place.
     */
    public static ContentType setCharset(ContentType contentType, Charset charset) throws ParseException, UnsupportedCharsetException {
        // Get the correct header value
        String contentTypeString = contentType.toString();

        // Parse the header manually the same way ContentType does it
        CharArrayBuffer buffer = new CharArrayBuffer(contentTypeString.length());
        buffer.append(contentTypeString);
        ParserCursor cursor = new ParserCursor(0, contentTypeString.length());
        HeaderElement[] elements = BasicHeaderValueParser.INSTANCE.parseElements(buffer, cursor);

        if (ArrayUtils.isNotEmpty(elements)) {
            String mimeType = elements[0].getName();
            NameValuePair[] params = elements[0].getParameters();
            List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
            boolean charsetFound = false;

            // Iterate through each parameter and override the charset if present
            if (ArrayUtils.isNotEmpty(params)) {
                for (NameValuePair nvp : params) {
                    if (nvp.getName().equalsIgnoreCase("charset")) {
                        charsetFound = true;
                        nvp = new BasicNameValuePair(nvp.getName(), charset.name());
                    }
                    paramsList.add(nvp);
                }
            }

            // Add the charset at the end if it wasn't found before
            if (!charsetFound) {
                paramsList.add(new BasicNameValuePair("charset", charset.name()));
            }

            // Format the header the same way ContentType does it
            CharArrayBuffer newBuffer = new CharArrayBuffer(64);
            newBuffer.append(mimeType);
            newBuffer.append("; ");
            BasicHeaderValueFormatter.INSTANCE.formatParameters(newBuffer, paramsList.toArray(new NameValuePair[paramsList.size()]), false);
            // Once we have the correct string, let ContentType do the rest
            return ContentType.parse(newBuffer.toString());
        } else {
            throw new ParseException("Invalid content type: " + contentTypeString);
        }
    }
}