package com.mirth.connect.connectors.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.Header;
import org.apache.log4j.Logger;
import org.mortbay.http.HttpRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.model.converters.DocumentSerializer;

public class HttpMessageConverter {
    private Logger logger = Logger.getLogger(this.getClass());
    private DocumentSerializer serializer = new DocumentSerializer(new String[] { "Content", "Body", "QueryString" });

    public String httpRequestToXml(HttpRequestMessage request) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element requestElement = document.createElement("HttpRequest");

            if (request.getRemoteAddress() != null) {
                requestElement.setAttribute("remoteAddr", request.getRemoteAddress());
            }

            if (request.getRequestUrl() != null) {
                requestElement.setAttribute("requestUrl", request.getRequestUrl());
            }

            requestElement.setAttribute("method", request.getMethod());

            if (!request.getParameters().isEmpty()) {
                Element queryElement = document.createElement("Parameters");

                for (Entry<String, String> entry : request.getParameters().entrySet()) {
                    Element paramElement = document.createElement("Parameter");
                    addElement(paramElement, "Name", entry.getKey());
                    addElement(paramElement, "Value", entry.getValue());
                    queryElement.appendChild(paramElement);
                }

                requestElement.appendChild(queryElement);

                // also add query string

                addElement(requestElement, "QueryString", request.getQueryString());
            }

            Element headerElement = document.createElement("Header");

            for (Entry<String, String> entry : request.getHeaders().entrySet()) {
                Element fieldElement = document.createElement("Field");
                addElement(fieldElement, "Name", entry.getKey());
                addElement(fieldElement, "Value", entry.getValue());
                headerElement.appendChild(fieldElement);
            }

            requestElement.appendChild(headerElement);

            // NOTE: "Content" is added as a CDATA element in the serializer
            // constructor
            Element contentElement = document.createElement("Content");

            if (request.getContentType() != null) {
                contentElement.setAttribute("type", request.getContentType());
            }

            contentElement.setTextContent(request.getContent());
            requestElement.appendChild(contentElement);

            document.appendChild(requestElement);
            return serializer.toXML(document);
        } catch (Exception e) {
            logger.error("Error converting HTTP request.", e);
        }

        return null;
    }

    public String httpResponseToXml(Header[] headers, String content) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element requestElement = document.createElement("HttpResponse");

            Element headerElement = document.createElement("Header");

            for (Header header : headers) {
                Element fieldElement = document.createElement("Field");
                addElement(fieldElement, "Name", header.getName());
                addElement(fieldElement, "Value", header.getValue());
                headerElement.appendChild(fieldElement);
            }

            requestElement.appendChild(headerElement);

            // NOTE: "Body" is added as a CDATA element in the serializer
            // constructor
            Element contentElement = document.createElement("Body");
            contentElement.setTextContent(content);
            requestElement.appendChild(contentElement);

            document.appendChild(requestElement);
            return serializer.toXML(document);
        } catch (Exception e) {
            logger.error("Error converting HTTP request.", e);
        }

        return null;
    }

    public String convertInputStreamToString(InputStream is, String charset) throws IOException {
        if (charset == null) {
            charset = "ISO-8859-1"; // default charset for HTTP
        }

        Reader reader = new InputStreamReader(is, charset);
        StringWriter writer = new StringWriter();
        char[] buffer = new char[1024];

        try {
            for (int n; (n = reader.read(buffer)) != -1;) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        return writer.toString();
    }

    public Map<String, String> convertFieldEnumerationToMap(HttpRequest request) {
        Map<String, String> headers = new HashMap<String, String>();

        for (Enumeration<String> enumeration = request.getFieldNames(); enumeration.hasMoreElements();) {
            String name = enumeration.nextElement();
            String value = request.getField(name);
            headers.put(name, value);
        }

        return headers;
    }
    
    private Element addElement(Element parent, String name, String textContent) {
        Element child = parent.getOwnerDocument().createElement(name);
        child.setTextContent(textContent);
        parent.appendChild(child);
        return child;
    }
}
