/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;

import org.apache.commons.io.IOUtils;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.json.stream.JsonStreamFactory;
import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.JsonStreamTarget;
import de.odysseus.staxon.xml.util.PrettyXMLStreamWriter;

public class JsonXmlUtil {

    public static String xmlToJson(String xmlStr) throws IOException, XMLStreamException, FactoryConfigurationError, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        //convert xml to json
        JsonXMLConfig config = new JsonXMLConfigBuilder().autoArray(true).autoPrimitive(true).prettyPrint(true).build();
        return xmlToJson(config, xmlStr);
    }

    protected static String xmlToJson(JsonXMLConfig config, String xmlStr) throws IOException, XMLStreamException, FactoryConfigurationError, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        try (InputStream inputStream = IOUtils.toInputStream(xmlStr);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // create source (XML)
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            Source source = new StAXSource(reader);

            // create result (JSON)
            JsonStreamFactory streamFactory = new CorrectedJsonStreamFactory();
            XMLStreamWriter writer = new JsonXMLOutputFactory(config, streamFactory).createXMLStreamWriter(outputStream);
            Result result = new StAXResult(writer);

            // copy source to result via "identity transform"
            TransformerFactory.newInstance().newTransformer().transform(source, result);
            return outputStream.toString();

        }
    }

    public static String jsonToXml(String jsonStr) throws IOException, XMLStreamException, FactoryConfigurationError, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).build();
        return jsonToXml(config, jsonStr);
    }

    protected static String jsonToXml(JsonXMLConfig config, String jsonStr) throws IOException, XMLStreamException, FactoryConfigurationError, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        try (InputStream inputStream = IOUtils.toInputStream(jsonStr);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XMLStreamReader reader = new JsonXMLInputFactory(config).createXMLStreamReader(inputStream);
            Source source = new StAXSource(reader);

            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
            Result result = new StAXResult(new PrettyXMLStreamWriter(writer));

            TransformerFactory.newInstance().newTransformer().transform(source, result);
            return outputStream.toString();
        }
    }

    /*
     * The sole purpose of the CorrectedJsonStreamFactory class is to correct some weirdness in the
     * Staxon library where a default namespace ("@xmlns") would get turned into ("@xmlns:xmlns")
     * for example:
     * 
     * <some_element xmlns="some:uri" some_attribute="XML_1.0">
     * 
     * would turn into:
     * 
     * "some_element" : { "@xmlns:xmlns" : "some:uri", "@some_attribute" : "XML_1.0", ...
     * 
     * The problem with this is that Staxon is unable to turn the above JSON back into the original
     * XML. So this fixes that yielding:
     * 
     * "some_element" : { "@xmlns" : "some:uri", "@some_attribute" : "XML_1.0", ...
     */
    public static class CorrectedJsonStreamFactory extends JsonStreamFactory {
        static final String REPLACEE_TEXT = "@xmlns:xmlns";
        static final String REPLACEMENT_TEXT = "@xmlns";

        static class MyJsonStreamTarget implements JsonStreamTarget {
            final JsonStreamTarget delegate;

            public MyJsonStreamTarget(JsonStreamTarget delegate) {
                this.delegate = delegate;
            }

            @Override
            public void close() throws IOException {
                delegate.close();
            }

            @Override
            public void flush() throws IOException {
                delegate.flush();
            }

            @Override
            public void endArray() throws IOException {
                delegate.endArray();
            }

            @Override
            public void endObject() throws IOException {
                delegate.endObject();
            }

            @Override
            public void name(String name) throws IOException {
                delegate.name(REPLACEE_TEXT.equals(name) ? REPLACEMENT_TEXT : name);
            }

            @Override
            public void startArray() throws IOException {
                delegate.startArray();
            }

            @Override
            public void startObject() throws IOException {
                delegate.startObject();
            }

            @Override
            public void value(Object value) throws IOException {
                delegate.value(value);
            }
        }

        private final JsonStreamFactory delegate;

        public CorrectedJsonStreamFactory() {
            this(newFactory());
        }

        public CorrectedJsonStreamFactory(JsonStreamFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public JsonStreamSource createJsonStreamSource(InputStream input) throws IOException {
            return delegate.createJsonStreamSource(input);
        }

        @Override
        public JsonStreamSource createJsonStreamSource(Reader reader) throws IOException {
            return delegate.createJsonStreamSource(reader);
        }

        @Override
        public JsonStreamTarget createJsonStreamTarget(OutputStream output, boolean prettyPrint) throws IOException {
            return new MyJsonStreamTarget(delegate.createJsonStreamTarget(output, prettyPrint));
        }

        @Override
        public JsonStreamTarget createJsonStreamTarget(Writer writer, boolean prettyPrint) throws IOException {
            return new MyJsonStreamTarget(delegate.createJsonStreamTarget(writer, prettyPrint));
        }
    }
}
