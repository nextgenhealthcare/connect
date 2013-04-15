/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.jdbc;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mirth.connect.donkey.util.ResourceUtil;

public class XmlQuerySource implements QuerySource {
    private Map<String, String> queries = new HashMap<String, String>();

    public void load(String xmlFile) throws XmlQuerySourceException {
        Document document = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            InputStream is = ResourceUtil.getResourceStream(XmlQuerySource.class, xmlFile);
            document = documentBuilder.parse(is);
            IOUtils.closeQuietly(is);
        } catch (Exception e) {
            throw new XmlQuerySourceException("Failed to read query file: " + xmlFile, e);
        }

        NodeList queryNodes = document.getElementsByTagName("query");
        int queryNodeCount = queryNodes.getLength();

        for (int i = 0; i < queryNodeCount; i++) {
            Node node = queryNodes.item(i);

            if (node.hasAttributes()) {
                Attr attr = (Attr) node.getAttributes().getNamedItem("id");

                if (attr != null) {
                    String query = StringUtils.trim(node.getTextContent());

                    if (query.length() > 0) {
                        queries.put(attr.getValue(), query);
                    }
                }
            }
        }
    }

    @Override
    public String getQuery(String queryName) {
        return getQuery(queryName, null);
    }

    @Override
    public String getQuery(String queryName, Map<String, Object> values) {
        String query = queries.get(queryName);

        if (query == null) {
            return null;
        }

        if (values != null) {
            for (Entry<String, Object> entry : values.entrySet()) {
                query = StringUtils.replace(query, "${" + entry.getKey() + "}", entry.getValue().toString());
            }
        }

        return query;
    }
    
    @Override
    public boolean queryExists(String queryName) {
        return queries.containsKey(queryName);
    }

    public class XmlQuerySourceException extends Exception {
        public XmlQuerySourceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
