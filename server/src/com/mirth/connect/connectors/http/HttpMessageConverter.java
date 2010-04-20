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

            requestElement.setAttribute("method", request.getMethod());

            if ("GET".equalsIgnoreCase(request.getMethod())) {
                Element queryElement = document.createElement("Query");

                for (Entry<String, String> entry : request.getQueryParameters().entrySet()) {
                    Element paramElement = document.createElement("Parameter");

                    Element nameElement = document.createElement("Name");
                    nameElement.setTextContent(entry.getKey());
                    paramElement.appendChild(nameElement);

                    Element valueElement = document.createElement("Value");
                    valueElement.setTextContent(entry.getValue());
                    paramElement.appendChild(valueElement);

                    queryElement.appendChild(paramElement);
                }

                requestElement.appendChild(queryElement);

                // also add query string

                Element queryStringElement = document.createElement("QueryString");
                queryStringElement.setTextContent(request.getQueryString());
                requestElement.appendChild(queryStringElement);
            }

            Element headerElement = document.createElement("Header");

            for (Entry<String, String> entry : request.getHeaders().entrySet()) {
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
        } catch (Exception e) {
            logger.error("Error converting HTTP request.", e);
        }

        return null;
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
