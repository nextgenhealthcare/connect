/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;

import com.mirth.connect.connectors.http.HttpMessageConverter;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;

/**
 * Provides HTTP utility methods.
 */
public class HTTPUtil {
    private HTTPUtil() {}

    /**
     * Converts a block of HTTP header fields into a Map containing each header key and value.
     * 
     * @param str
     *            The block of HTTP header fields to convert.
     * @return The converted Map containing header key-value pairs.
     * @throws Exception
     *             If the header string could not be parsed.
     */
    public static Map<String, String> parseHeaders(String str) throws Exception {
        Map<String, String> headersMap = new HashMap<String, String>();
        Header[] headers = HttpParser.parseHeaders(new ByteArrayInputStream(str.getBytes()), "UTF-8");

        for (int i = 0; i < headers.length; i++) {
            headersMap.put(headers[i].getName(), headers[i].getValue());
        }

        return headersMap;
    }

    /**
     * Serializes an HTTP request body into XML. Multipart requests will also automatically be
     * parsed into separate XML nodes.
     * 
     * @param httpBody
     *            The request body/payload input stream to parse.
     * @param contentType
     *            The MIME content type of the request.
     * @return The serialized XML string.
     * @throws MessagingException
     *             If the body could not be converted into a multipart object.
     * @throws IOException
     *             If the body could not be read into a string.
     * @throws DonkeyElementException
     *             If an XML parsing error occurs.
     * @throws ParserConfigurationException
     *             If an XML or multipart parsing error occurs.
     */
    public static String httpBodyToXml(InputStream httpBody, String contentType) throws MessagingException, IOException, DonkeyElementException, ParserConfigurationException {
        ContentType type = getContentType(contentType);
        Object content;

        if (type.getMimeType().startsWith(FileUploadBase.MULTIPART)) {
            content = new MimeMultipart(new ByteArrayDataSource(httpBody, type.toString()));
        } else {
            content = IOUtils.toString(httpBody, HttpMessageConverter.getDefaultHttpCharset(type.getCharset().name()));
        }

        return HttpMessageConverter.contentToXml(content, type, true, null);
    }

    /**
     * Serializes an HTTP request body into XML. Multipart requests will also automatically be
     * parsed into separate XML nodes.
     * 
     * @param httpBody
     *            The request body/payload string to parse.
     * @param contentType
     *            The MIME content type of the request.
     * @return The serialized XML string.
     * @throws MessagingException
     *             If the body could not be converted into a multipart object.
     * @throws IOException
     *             If the body could not be read into a string.
     * @throws DonkeyElementException
     *             If an XML parsing error occurs.
     * @throws ParserConfigurationException
     *             If an XML or multipart parsing error occurs.
     */
    public static String httpBodyToXml(String httpBody, String contentType) throws MessagingException, IOException, DonkeyElementException, ParserConfigurationException {
        ContentType type = getContentType(contentType);
        Object content;

        if (type.getMimeType().startsWith(FileUploadBase.MULTIPART)) {
            content = new MimeMultipart(new ByteArrayDataSource(httpBody, type.toString()));
        } else {
            content = httpBody;
        }

        return HttpMessageConverter.contentToXml(content, type, true, null);
    }

    private static ContentType getContentType(String contentType) {
        try {
            return ContentType.parse(contentType);
        } catch (RuntimeException e) {
            return ContentType.TEXT_PLAIN;
        }
    }
}