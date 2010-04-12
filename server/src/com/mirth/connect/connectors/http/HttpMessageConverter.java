package com.mirth.connect.connectors.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.Header;
import org.mortbay.http.HttpRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.model.converters.DocumentSerializer;

public class HttpMessageConverter {
    private DocumentSerializer serializer = new DocumentSerializer(new String[] { "Content", "Body" });

    public String httpRequestToXml(HttpRequest request) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element requestElement = document.createElement("HttpRequest");

        Element headerElement = document.createElement("Header");

        for (Enumeration<String> enumeration = request.getFieldNames(); enumeration.hasMoreElements();) {
            String name = enumeration.nextElement();
            String value = request.getField(name);

            Element fieldElement = document.createElement("Field");

            Element nameElement = document.createElement("Name");
            nameElement.setTextContent(name);
            fieldElement.appendChild(nameElement);

            Element valueElement = document.createElement("Value");
            valueElement.setTextContent(value);
            fieldElement.appendChild(valueElement);

            headerElement.appendChild(fieldElement);
        }

        requestElement.appendChild(headerElement);

        // NOTE: "Content" is added as a CDATA element in the serializer constructor
        Element contentElement = document.createElement("Content");
        contentElement.setTextContent(convertInputStreamToString(request.getInputStream()));
        requestElement.appendChild(contentElement);

        document.appendChild(requestElement);
        return serializer.toXML(document);
    }

    public String httpResponseToXml(Header[] headers, InputStream body) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element requestElement = document.createElement("HttpResponse");

        Element headerElement = document.createElement("Header");

        for (Header header : headers) {
            Element fieldElement = document.createElement("Field");

            Element nameElement = document.createElement("Name");
            nameElement.setTextContent(header.getName());
            fieldElement.appendChild(nameElement);

            Element valueElement = document.createElement("Value");
            valueElement.setTextContent(header.getValue());
            fieldElement.appendChild(valueElement);

            headerElement.appendChild(fieldElement);
        }

        requestElement.appendChild(headerElement);

        // NOTE: "Body" is added as a CDATA element in the serializer constructor
        Element contentElement = document.createElement("Body");
        contentElement.setTextContent(convertInputStreamToString(body));
        requestElement.appendChild(contentElement);

        document.appendChild(requestElement);
        return serializer.toXML(document);
    }

    public String convertInputStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } finally {
            is.close();
        }

        return sb.toString();
    }
}
