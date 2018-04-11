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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
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
import org.apache.commons.lang3.tuple.Pair;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.json.JsonXMLStreamReader;
import de.odysseus.staxon.json.JsonXMLStreamWriter;
import de.odysseus.staxon.json.stream.JsonStreamFactory;
import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.JsonStreamTarget;
import de.odysseus.staxon.json.stream.JsonStreamToken;
import de.odysseus.staxon.util.StreamWriterDelegate;
import de.odysseus.staxon.xml.util.PrettyXMLStreamWriter;

public class JsonXmlUtil {

    private static final String SEPARATOR = ":";

    public static String xmlToJson(String xmlStr) throws IOException, XMLStreamException, FactoryConfigurationError, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        return xmlToJson(xmlStr, true);
    }

    public static String xmlToJson(String xmlStr, boolean normalizeNamespaces) throws IOException, XMLStreamException, FactoryConfigurationError, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        //convert xml to json
        JsonXMLConfig config = new JsonXMLConfigBuilder().autoArray(true).autoPrimitive(true).prettyPrint(false).build();
        return xmlToJson(config, xmlStr, normalizeNamespaces);
    }

    public static String xmlToJson(JsonXMLConfig config, String xmlStr, boolean normalizeNamespaces) throws IOException, XMLStreamException, FactoryConfigurationError, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        try (InputStream inputStream = IOUtils.toInputStream(xmlStr);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // create source (XML)
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            Source source = new StAXSource(reader);

            // create result (JSON)
            JsonStreamFactory streamFactory = new CorrectedJsonStreamFactory();
            XMLStreamWriter writer;

            if (normalizeNamespaces) {
                JsonXMLOutputFactory outputFactory = new NormalizeJsonOutputFactory(config, streamFactory);
                writer = new NormalizeJsonStreamWriterDelegate(outputFactory.createXMLStreamWriter(outputStream));
            } else {
                JsonXMLOutputFactory outputFactory = new JsonXMLOutputFactory(config, streamFactory);
                writer = outputFactory.createXMLStreamWriter(outputStream);
            }

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

    public static String jsonToXml(JsonXMLConfig config, String jsonStr) throws IOException, XMLStreamException, FactoryConfigurationError, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        try (InputStream inputStream = IOUtils.toInputStream(jsonStr);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XMLStreamReader reader = new NormalizeXMLInputFactory(config).createXMLStreamReader(inputStream);
            Map<String, Deque<String>> prefixByTag = ((NormalizeXMLStreamReader) reader).prefixByTag;
            Source source = new StAXSource(reader);

            XMLStreamWriter writer = new NormalizeXMLStreamWriterDelegate(XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream), prefixByTag);
            if (config.isPrettyPrint()) {
                writer = new PrettyXMLStreamWriter(writer);
            }
            Result result = new StAXResult(writer);

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

    // XML -> JSON
    private static class NormalizeJsonOutputFactory extends JsonXMLOutputFactory {

        public NormalizeJsonOutputFactory(JsonXMLConfig config, JsonStreamFactory streamFactory) {
            super(config, streamFactory);
        }

        @Override
        public JsonXMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException {
            try {
                return new NormalizeJsonStreamWriter(decorate(streamFactory.createJsonStreamTarget(stream, prettyPrint)), repairNamespacesMap(), multiplePI, namespaceSeparator, namespaceDeclarations);
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }

        @Override
        public JsonXMLStreamWriter createXMLStreamWriter(Writer stream) throws XMLStreamException {
            try {
                return new NormalizeJsonStreamWriter(decorate(streamFactory.createJsonStreamTarget(stream, prettyPrint)), repairNamespacesMap(), multiplePI, namespaceSeparator, namespaceDeclarations);
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }
    }

    private static class NormalizeJsonStreamWriterDelegate extends StreamWriterDelegate {

        public NormalizeJsonStreamWriterDelegate(XMLStreamWriter parent) {
            super(parent);
        }

        @Override
        public void writeStartElement(String localName) throws XMLStreamException {
            Pair<String, String> splitPair = splitPrefixAndLocalName(localName);
            String prefix = splitPair.getLeft();
            String local = splitPair.getRight();
            super.writeStartElement(local);

            if (!prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                writeAttribute("xmlnsprefix", prefix);
            }
        }

        @Override
        public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {

            // XML has special attributes that are prefixed with "xml". This causes problems
            // because the namespace for the prefix does not have to be declared, so we have to
            // handle that special case.
            if (prefix != null && "xml".equals(prefix)) {
                prefix = XMLConstants.DEFAULT_NS_PREFIX;
                namespaceURI = XMLConstants.NULL_NS_URI;
                localName = "xml" + SEPARATOR + localName;
            }

            if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                super.writeAttribute(prefix, namespaceURI, localName, value);
            } else {
                super.writeStartElement("@" + localName);
                writeAttribute("xmlnsprefix", prefix);
                writeAttribute("$", value);
                super.writeEndElement();
            }
        }

        private static Pair<String, String> splitPrefixAndLocalName(String key) {
            int separatorLocation = key.indexOf(SEPARATOR);

            String prefix, localName;
            if (separatorLocation != -1) {
                prefix = key.substring(0, separatorLocation);
                localName = key.substring(separatorLocation + 1);
            } else {
                prefix = XMLConstants.DEFAULT_NS_PREFIX;
                localName = key;
            }

            return Pair.of(prefix, localName);
        }
    }

    private static class NormalizeJsonStreamWriter extends JsonXMLStreamWriter {

        public NormalizeJsonStreamWriter(JsonStreamTarget target, boolean repairNamespaces, boolean multiplePI, char namespaceSeparator, boolean namespaceDeclarations) {
            super(target, repairNamespaces, multiplePI, namespaceSeparator, namespaceDeclarations);
        }

        public NormalizeJsonStreamWriter(JsonStreamTarget target, Map<String, String> repairNamespaces, boolean multiplePI, char namespaceSeparator, boolean namespaceDeclarations) {
            super(target, repairNamespaces, multiplePI, namespaceSeparator, namespaceDeclarations);
        }

        @Override
        protected void writeAttr(String prefix, String localName, String namespaceURI, String value) throws XMLStreamException {
            String name = XMLConstants.DEFAULT_NS_PREFIX.equals(prefix) ? localName : prefix + namespaceSeparator + localName;
            try {
                if (!getScope().getInfo().startObjectWritten) {
                    target.startObject();
                    getScope().getInfo().startObjectWritten = true;
                }

                // The only change from the super method implementation is that
                // we don't put an "@" in front of any "$" attributes.
                if (localName.equals("$")) {
                    target.name("$");
                } else {
                    target.name('@' + name);
                }
                target.value(value);
            } catch (IOException e) {
                throw new XMLStreamException("Cannot write attribute: " + name, e);
            }
        }

        @Override
        protected void writeData(Object data, int type) throws XMLStreamException {
            switch (type) {
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.CDATA:
                    if (getScope().isRoot() && !isStartDocumentWritten()) {
                        try {
                            target.value(data);
                        } catch (IOException e) {
                            throw new XMLStreamException("Cannot write data", e);
                        }
                    } else {
                        if (data == null) {
                            throw new XMLStreamException("Cannot write null data");
                        }
                        if (getScope().getLastChild() == null && getScope().getInfo().hasData()) {
                            if (data instanceof String) {
                                getScope().getInfo().addText(data.toString());
                            } else {
                                throw new XMLStreamException("Cannot append primitive data: " + data);
                            }
                        } else if (getScope().getLastChild() == null) {
                            getScope().getInfo().setData(data);
                        } else if (getScope().getLastChild().getLocalName().startsWith("@")) {
                            // Added this case to make sure we don't mistakenly think we're dealing
                            // with mixed content. In our XML to JSON conversion, attributes
                            // can be objects and can have attributes of their own, which the original
                            // implementation did not support.
                            if (data instanceof String) {
                                getScope().getInfo().addText(data.toString());
                            } else {
                                throw new XMLStreamException("Cannot append primitive data: " + data);
                            }
                        } else if (!skipSpace || !isWhitespace(data)) {
                            throw new XMLStreamException("Mixed content is not supported: '" + data + "'");
                        }
                    }
                    break;
                case XMLStreamConstants.COMMENT: // ignore comments
                    break;
                default:
                    throw new UnsupportedOperationException("Cannot write data of type " + type);
            }
        }
    }

    // JSON -> XML
    private static class NormalizeXMLInputFactory extends JsonXMLInputFactory {

        public NormalizeXMLInputFactory(JsonXMLConfig config) throws FactoryConfigurationError {
            super(config);
        }

        @Override
        public JsonXMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
            try {
                return new NormalizeXMLStreamReader(decorate(streamFactory.createJsonStreamSource(stream)), multiplePI, namespaceSeparator, namespaceMappings);
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }
    }

    private static class NormalizeXMLStreamReader extends JsonXMLStreamReader {
        public Map<String, Deque<String>> prefixByTag;
        private String currentTagName;

        public NormalizeXMLStreamReader(JsonStreamSource decorate, boolean multiplePI, char namespaceSeparator, Map<String, String> namespaceMappings) throws XMLStreamException {
            super(decorate, multiplePI, namespaceSeparator, namespaceMappings);
        }

        protected void consumeName(ScopeInfo info) throws XMLStreamException, IOException {
            String fieldName = source.name();
            if (fieldName.startsWith("@")) {
                fieldName = fieldName.substring(1);
                if (source.peek() == JsonStreamToken.VALUE) {
                    handleValue(fieldName);
                } else if (XMLConstants.XMLNS_ATTRIBUTE.equals(fieldName)) {
                    handleXmlns();
                } else if (source.peek() == JsonStreamToken.START_OBJECT) {
                    handleObject(fieldName);
                } else if (source.peek() == JsonStreamToken.START_ARRAY) {
                    source.startArray();
                    while (source.peek() != JsonStreamToken.END_ARRAY) {
                        if (source.peek() == JsonStreamToken.VALUE) {
                            handleValue(fieldName);
                        } else if (source.peek() == JsonStreamToken.START_OBJECT) {
                            handleObject(fieldName);
                        }
                    }
                    source.endArray();
                } else {
                    throw new IllegalStateException("Expected attribute value");
                }
            } else if ("$".equals(fieldName)) {
                readData(source.value(), XMLStreamConstants.CHARACTERS);
            } else {
                info.currentTagName = fieldName;
            }
        }

        private void handleValue(String fieldName) throws XMLStreamException, IOException {
            String value = source.value().text;

            if (fieldName.equals("xmlnsprefix")) {
                // Store the prefix to prepend to the tag name later
                if (prefixByTag == null) {
                    prefixByTag = new HashMap<>();
                }

                Deque<String> prefixes = prefixByTag.get(currentTagName);
                if (prefixes == null) {
                    prefixes = new ArrayDeque<>();
                    prefixByTag.put(currentTagName, prefixes);
                }

                prefixes.addLast(value);
            } else {
                readAttrNsDecl(fieldName, value);
            }
        }

        private void handleXmlns() throws XMLStreamException, IOException {
            source.startObject();
            while (source.peek() == JsonStreamToken.NAME) {
                String prefix = source.name();
                if ("$".equals(prefix)) {
                    readNsDecl(XMLConstants.DEFAULT_NS_PREFIX, source.value().text);
                } else {
                    readNsDecl(prefix, source.value().text);
                }
            }
            source.endObject();
        }

        private void handleObject(String fieldName) throws XMLStreamException, IOException {
            // Handles attributes that are objects with attributes of their own
            // and possibly with a bound prefix
            source.startObject();
            String prefix = XMLConstants.DEFAULT_NS_PREFIX;
            while (source.peek() == JsonStreamToken.NAME) {
                String name = source.name();
                String text = source.value().text;

                if (name.equals("@xmlnsprefix")) {
                    prefix = text;
                } else if (name.equals("$")) {
                    readAttrNsDecl(prefix + SEPARATOR + fieldName, text);
                }
            }
            source.endObject();
        }

        protected void readStartElementTag(String name) throws XMLStreamException {
            currentTagName = name;
            super.readStartElementTag(name);
        }
    }

    private static class NormalizeXMLStreamWriterDelegate extends StreamWriterDelegate {
        private Map<String, Deque<String>> prefixByTag;

        public NormalizeXMLStreamWriterDelegate(XMLStreamWriter parent, Map<String, Deque<String>> prefixByTag) {
            super(parent);
            this.prefixByTag = prefixByTag;
        }

        @Override
        public void writeStartElement(String localName) throws XMLStreamException {
            if (prefixByTag != null && prefixByTag.containsKey(localName)) {
                Deque<String> prefixes = prefixByTag.get(localName);
                if (!prefixes.isEmpty()) {
                    localName = prefixes.removeFirst() + SEPARATOR + localName;
                }
            }
            super.writeStartElement(localName);
        }

        @Override
        public void setPrefix(String prefix, String uri) throws XMLStreamException {
            // Namespaces are mirrored as attributes for some handlers, so ignore xmlns and xml
            if (!XMLConstants.XML_NS_PREFIX.equals(prefix) && !XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                super.setPrefix(prefix, uri);
            }
        }
    }

}
