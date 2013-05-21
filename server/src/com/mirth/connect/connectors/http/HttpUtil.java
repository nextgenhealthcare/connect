/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

public class HttpUtil {

    public static String getCharset(String contentType) {
        for (String element : contentType.split(";")) {
            if (element.trim().startsWith("charset") && element.contains("=")) {
                return element.substring(element.indexOf("=") + 1).trim();
            }
        }
        return "ISO-8859-1";
    }

    public static String uncompressGzip(String content, String charset) throws IOException {
        return uncompressGzip(content.getBytes(charset), charset);
    }

    public static String uncompressGzip(byte[] content, String charset) throws IOException {
        GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(content));
        StringWriter writer = new StringWriter();
        IOUtils.copy(gzis, writer, charset);
        return writer.toString();
    }

    public static byte[] compressGzip(String content, String charset) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(baos);
        gzos.write(content.getBytes(charset));
        gzos.close();
        return baos.toByteArray();
    }
}
