package com.webreach.mirth.server.util;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;

public class HTTPUtil {
    public static Map<String, String> parseHeaders(String str) throws Exception {
        Map<String, String> headersMap = new HashMap<String, String>();
        Header[] headers = HttpParser.parseHeaders(new ByteArrayInputStream(str.getBytes()), "UTF-8");

        for (int i = 0; i < headers.length; i++) {
            headersMap.put(headers[i].getName(), headers[i].getValue());
        }

        return headersMap;
    }
}
