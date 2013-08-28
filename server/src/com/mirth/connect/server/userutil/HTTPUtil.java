/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;

/**
 * Provides HTTP utility methods.
 */
public class HTTPUtil {
    private HTTPUtil() {}

    /**
     * Converts a block of HTTP header fields into a Map containing each header
     * key and value.
     * 
     * @param str
     *            - The block of HTTP header fields to convert.
     * @return - The converted Map containing header key-value pairs.
     * @throws Exception
     */
    public static Map<String, String> parseHeaders(String str) throws Exception {
        Map<String, String> headersMap = new HashMap<String, String>();
        Header[] headers = HttpParser.parseHeaders(new ByteArrayInputStream(str.getBytes()), "UTF-8");

        for (int i = 0; i < headers.length; i++) {
            headersMap.put(headers[i].getName(), headers[i].getValue());
        }

        return headersMap;
    }
}