package com.mirth.connect.connectors.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.Header;
import org.mortbay.http.HttpRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.model.converters.DocumentSerializer;

public class HttpMessageConverter {
    private DocumentSerializer serializer = new DocumentSerializer();

    public String httpRequestToXml(Map<String, String> headers, String content) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element requestElement = document.createElement("HttpRequest");

        Element headerElement = document.createElement("Header");

        for (Entry<String, String> entry : headers.entrySet()) {
            Element fieldElement = document.createElement("Field");

            Element nameElement = document.createElement("Name");
            nameElement.setTextContent(entry.getKey());
            fieldElement.appendChild(nameElement);

            Element valueElement = document.createElement("Value");
            valueElement.setTextContent(entry.getValue());
            fieldElement.appendChild(valueElement);

            headerElement.appendChild(fieldElement);
        }

        requestElement.appendChild(headerElement);

        // NOTE: "Content" is added as a CDATA element in the serializer
        // constructor
        Element contentElement = document.createElement("Content");
        contentElement.setTextContent(content);
        requestElement.appendChild(contentElement);

        document.appendChild(requestElement);
        return serializer.toXML(document);
    }

    public String httpResponseToXml(Header[] headers, String content) throws Exception {
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

        // NOTE: "Body" is added as a CDATA element in the serializer
        // constructor
        Element contentElement = document.createElement("Body");
        contentElement.setTextContent(content);
        requestElement.appendChild(contentElement);

        document.appendChild(requestElement);
        return serializer.toXML(document);
    }

    public String convertInputStreamToString(InputStream is, String charset) throws IOException {
        if (charset == null) {
            charset = Charset.defaultCharset().name();
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
}
