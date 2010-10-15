package com.mirth.connect.connectors.http;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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
    private DocumentSerializer documentSerializer = new DocumentSerializer(new String[] { "Content", "Body", "QueryString" });

    public String httpRequestToXml(HttpRequestMessage request) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element requestElement = document.createElement("HttpRequest");

            addElement(requestElement, "RemoteAddress", request.getRemoteAddress());
            addElement(requestElement, "RequestUrl", request.getRequestUrl());
            addElement(requestElement, "Method", request.getMethod());
            addElement(requestElement, "RequestPath", request.getQueryString());

            if (!request.getParameters().isEmpty()) {
                Element parameteresElement = document.createElement("Parameters");

                for (Entry<String, Object> entry : request.getParameters().entrySet()) {
                    if (entry.getValue() instanceof List<?>) {
                        String name = entry.getKey().substring(0, entry.getKey().indexOf("[]"));

                        for (String value : (List<String>) entry.getValue()) {
                            addElement(parameteresElement, name, value);
                        }
                    } else {
                        addElement(parameteresElement, entry.getKey(), entry.getValue().toString());
                    }
                }

                requestElement.appendChild(parameteresElement);
            }

            Element headerElement = document.createElement("Header");

            for (Entry<String, String> entry : request.getHeaders().entrySet()) {
                addElement(headerElement, entry.getKey(), entry.getValue());
            }

            requestElement.appendChild(headerElement);

            // NOTE: "Content" is added as a CDATA element in the
            // documentSerializer
            // constructor
            addElement(requestElement, "Content", request.getContent());

            document.appendChild(requestElement);
            return documentSerializer.toXML(document);
        } catch (Exception e) {
            logger.error("Error converting HTTP request.", e);
        }

        return null;
    }

    public String httpResponseToXml(String status, Header[] headers, String content) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element requestElement = document.createElement("HttpResponse");
            
            Element statusElement = document.createElement("Status");
            statusElement.setTextContent(status);
            requestElement.appendChild(statusElement);

            Element headerElement = document.createElement("Header");

            for (Header header : headers) {
                Element fieldElement = document.createElement("Field");
                addElement(fieldElement, "Name", header.getName());
                addElement(fieldElement, "Value", header.getValue());
                headerElement.appendChild(fieldElement);
            }

            requestElement.appendChild(headerElement);

            // NOTE: "Body" is added as a CDATA element in the
            // documentSerializer
            // constructor
            Element contentElement = document.createElement("Body");
            contentElement.setTextContent(content);
            requestElement.appendChild(contentElement);

            document.appendChild(requestElement);
            return documentSerializer.toXML(document);
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
