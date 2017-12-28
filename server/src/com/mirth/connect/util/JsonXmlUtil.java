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
import java.util.Map;

import javax.xml.XMLConstants;
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
import org.apache.commons.lang3.tuple.Pair;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.json.JsonXMLStreamWriter;
import de.odysseus.staxon.json.stream.JsonStreamFactory;
import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.JsonStreamTarget;
import de.odysseus.staxon.util.StreamWriterDelegate;
import de.odysseus.staxon.xml.util.PrettyXMLStreamWriter;

public class JsonXmlUtil {

	public static String xmlToJson(String xmlStr) throws IOException, XMLStreamException, FactoryConfigurationError,
			TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
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
            	JsonXMLOutputFactory outputFactory = new NormalizeJsonXMLOutputFactory(config, streamFactory);
            	writer = new NormalizeJsonStreamWriterDelegate(outputFactory.createXMLStreamWriter(outputStream));
            } else {
            	JsonXMLOutputFactory outputFactory = new JsonXMLOutputFactory(config, streamFactory);
            	writer = outputFactory.createXMLStreamWriter(outputStream);
            }
            
            Result result = new StAXResult(writer);

            // copy source to result via "identity transform"
            TransformerFactory.newInstance().newTransformer().transform(source, result);
            String jsonString = outputStream.toString();
            
            return jsonString;
        }
    }

    public static String jsonToXml(String jsonStr) throws IOException, XMLStreamException, FactoryConfigurationError, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).build();
        return jsonToXml(config, jsonStr);
    }

    public static String jsonToXml(JsonXMLConfig config, String jsonStr) throws IOException, XMLStreamException, FactoryConfigurationError, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        try (InputStream inputStream = IOUtils.toInputStream(jsonStr);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XMLStreamReader reader = new JsonXMLInputFactory(config).createXMLStreamReader(inputStream);
            Source source = new StAXSource(reader);

            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
            if (config.isPrettyPrint()) {
                writer = new PrettyXMLStreamWriter(writer);
            }
            Result result = new StAXResult(writer);

            TransformerFactory.newInstance().newTransformer().transform(source, result);
            return outputStream.toString();
        }
    }
	
	private static class NormalizeJsonXMLOutputFactory extends JsonXMLOutputFactory {

		@SuppressWarnings("unused")
		public NormalizeJsonXMLOutputFactory() throws FactoryConfigurationError {
		}

		@SuppressWarnings("unused")
		public NormalizeJsonXMLOutputFactory(JsonStreamFactory streamFactory) {
			super(streamFactory);
		}

		public NormalizeJsonXMLOutputFactory(JsonXMLConfig config, JsonStreamFactory streamFactory) {
			super(config, streamFactory);
		}

		@SuppressWarnings("unused")
		public NormalizeJsonXMLOutputFactory(JsonXMLConfig config) throws FactoryConfigurationError {
			super(config);
		}

		@Override
		public JsonXMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException {
			try {
				return new NormalizeJsonXMLStreamWriter(
						decorate(streamFactory.createJsonStreamTarget(stream, prettyPrint)), repairNamespacesMap(),
						multiplePI, namespaceSeparator, namespaceDeclarations);
			} catch (IOException e) {
				throw new XMLStreamException(e);
			}
		}

		@Override
		public JsonXMLStreamWriter createXMLStreamWriter(Writer stream) throws XMLStreamException {
			try {
				return new NormalizeJsonXMLStreamWriter(
						decorate(streamFactory.createJsonStreamTarget(stream, prettyPrint)), repairNamespacesMap(),
						multiplePI, namespaceSeparator, namespaceDeclarations);
			} catch (IOException e) {
				throw new XMLStreamException(e);
			}
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
    

    private static class NormalizeJsonStreamWriterDelegate extends StreamWriterDelegate {
        private static final String SEPARATOR = ":";
        
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
        public void writeAttribute(String localName, String value) throws XMLStreamException {
        	System.out.println(localName + ", " + value);
        	super.writeAttribute(localName, value);
        }

        @Override
        public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
        		throws XMLStreamException {
        	System.out.println(prefix + ", " + namespaceURI + ", " + localName + ", " + value);

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
    
    private static class NormalizeJsonXMLStreamWriter extends JsonXMLStreamWriter {

		public NormalizeJsonXMLStreamWriter(JsonStreamTarget target, boolean repairNamespaces, boolean multiplePI,
				char namespaceSeparator, boolean namespaceDeclarations) {
			super(target, repairNamespaces, multiplePI, namespaceSeparator, namespaceDeclarations);
		}

		public NormalizeJsonXMLStreamWriter(JsonStreamTarget target, Map<String, String> repairNamespaces,
				boolean multiplePI, char namespaceSeparator, boolean namespaceDeclarations) {
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
	}
}
