/*
 * Copyright 2011, 2012 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.odysseus.staxon.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.EventFilter;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import de.odysseus.staxon.base.AbstractXMLInputFactory;
import de.odysseus.staxon.event.SimpleXMLEventReader;
import de.odysseus.staxon.event.SimpleXMLFilteredEventReader;
import de.odysseus.staxon.json.stream.JsonStreamFactory;
import de.odysseus.staxon.json.stream.JsonStreamSource;
import de.odysseus.staxon.json.stream.util.AddRootSource;

/**
 * XML input factory for streaming from JSON.
 */
public class JsonXMLInputFactory extends AbstractXMLInputFactory {
	/**
	 * <p>Whether to use the {@link JsonXMLStreamConstants#MULTIPLE_PI_TARGET}
	 * processing instruction to indicate an array start.
	 * If <code>true</code>, this reader will insert a PI with the field
	 * name as PI data. 
	 *  
	 * <p>Note that the element given in the PI may occur zero times,
	 * indicating an "empty array".</p>
	 * 
	 * <p>The default value is <code>true</code>.</p>
	 */
	public static final String PROP_MULTIPLE_PI = "JsonXMLInputFactory.multiplePI";

	/**
	 * <p>JSON documents may have have multiple root properties. However,
	 * XML requires a single root element. This property takes the name
	 * of a "virtual" root element, which will be added to the stream
	 * when reading.</p>
	 * 
	 * <p>The default value is <code>null</code>.</p>
	 */
	public static final String PROP_VIRTUAL_ROOT = "JsonXMLInputFactory.virtualRoot";

	/**
	 * <p>Namespace prefix separator.</p>
	 * 
	 * <p>The default value is <code>':'</code>.</p>
	 */
	public static final String PROP_NAMESPACE_SEPARATOR = "JsonXMLInputFactory.namespaceSeparator";

	/**
	 * <p>Namespace mappings associate prefixes with URIs when reading JSON.
	 * This can be used to parse documents which are missing namespace declarations.</p>
	 * 
	 * <p>The default value is <code>null</code>.</p>
	 */
	public static final String PROP_NAMESPACE_MAPPINGS = "JsonXMLInputFactory.namespaceMappings";

	protected final JsonStreamFactory streamFactory;

	protected boolean multiplePI;
	private QName virtualRoot;
	protected char namespaceSeparator;
	protected Map<String, String> namespaceMappings;

	public JsonXMLInputFactory() throws FactoryConfigurationError {
		this(JsonXMLConfig.DEFAULT);
	}

	public JsonXMLInputFactory(JsonStreamFactory streamFactory) {
		this(JsonXMLConfig.DEFAULT, streamFactory);
	}

	public JsonXMLInputFactory(JsonXMLConfig config) throws FactoryConfigurationError {
		this(config, JsonStreamFactory.newFactory());
	}
	
	public JsonXMLInputFactory(JsonXMLConfig config, JsonStreamFactory streamFactory) {
		this.multiplePI = config.isMultiplePI();
		this.virtualRoot = config.getVirtualRoot();
		this.namespaceSeparator = config.getNamespaceSeparator();
		this.namespaceMappings = config.getNamespaceMappings();
		this.streamFactory = streamFactory;
		
		/*
		 * initialize standard properties
		 */
		super.setProperty(IS_COALESCING, Boolean.TRUE);
		super.setProperty(IS_NAMESPACE_AWARE, Boolean.TRUE);
		super.setProperty(IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
		super.setProperty(IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		super.setProperty(IS_VALIDATING, Boolean.FALSE);
		super.setProperty(SUPPORT_DTD, Boolean.FALSE);
	}
	
	protected JsonStreamSource decorate(JsonStreamSource source) {
		if (virtualRoot != null) {
			source = new AddRootSource(source, virtualRoot, namespaceSeparator);
		}
		return source;
	}
	
	@Override
	public JsonXMLStreamReader createXMLStreamReader(InputStream stream, String encoding) throws XMLStreamException {
		try {
			return createXMLStreamReader(new InputStreamReader(stream, encoding));
		} catch (UnsupportedEncodingException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public JsonXMLStreamReader createXMLStreamReader(String systemId, InputStream stream) throws XMLStreamException {
		return createXMLStreamReader(stream);
	}

	@Override
	public JsonXMLStreamReader createXMLStreamReader(String systemId, Reader reader) throws XMLStreamException {
		return createXMLStreamReader(reader);
	}

	@Override
	public JsonXMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
		try {
			return new JsonXMLStreamReader(decorate(streamFactory.createJsonStreamSource(reader)), multiplePI, namespaceSeparator, namespaceMappings);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public JsonXMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
		try {
			return new JsonXMLStreamReader(decorate(streamFactory.createJsonStreamSource(stream)), multiplePI, namespaceSeparator, namespaceMappings);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException {
		if (getEventAllocator() == null) {
			return new SimpleXMLEventReader(reader);
		} else {
			return new SimpleXMLEventReader(reader, getEventAllocator().newInstance());
		}
	}

	@Override
	public XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter) throws XMLStreamException {
		return new SimpleXMLFilteredEventReader(reader, filter);
	}

	@Override
	public void setXMLResolver(XMLResolver resolver) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setXMLReporter(XMLReporter reporter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPropertySupported(String name) {
		return super.isPropertySupported(name)
			|| Arrays.asList(PROP_MULTIPLE_PI, PROP_VIRTUAL_ROOT, PROP_NAMESPACE_SEPARATOR, PROP_NAMESPACE_MAPPINGS).contains(name);
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		if (super.isPropertySupported(name)) {
			return super.getProperty(name);
		} else { // proprietary properties
			if (PROP_MULTIPLE_PI.equals(name)) {
				return Boolean.valueOf(multiplePI);
			} else if (PROP_VIRTUAL_ROOT.equals(name)) {
				return virtualRoot;
			} else if (PROP_NAMESPACE_SEPARATOR.equals(name)) {
				return namespaceSeparator;
			} else if (PROP_NAMESPACE_MAPPINGS.equals(name)) {
				return namespaceMappings;
			} else {
				throw new IllegalArgumentException("Unsupported property: " + name);
			}
		}
	}
	
	@Override
	public void setProperty(String name, Object value) throws IllegalArgumentException {
		if (IS_NAMESPACE_AWARE.equals(name)) {
			if (!getProperty(name).equals(value)) {
				throw new IllegalArgumentException("Cannot change property: " + name);
			}
		} else if (IS_REPLACING_ENTITY_REFERENCES.equals(name)) {
			if (!getProperty(name).equals(value)) {
				throw new IllegalArgumentException("Cannot change property: " + name);
			}
		} else if (IS_SUPPORTING_EXTERNAL_ENTITIES.equals(name)) {
			if (!getProperty(name).equals(value)) {
				throw new IllegalArgumentException("Cannot change property: " + name);
			}
		} else if (IS_VALIDATING.equals(name)) {
			if (!getProperty(name).equals(value)) {
				throw new IllegalArgumentException("Cannot change property: " + name);
			}
		} else if (SUPPORT_DTD.equals(name)) {
			if (!getProperty(name).equals(value)) {
				throw new IllegalArgumentException("Cannot change property: " + name);
			}
		} else if (super.isPropertySupported(name)) {
			super.setProperty(name, value);
		} else { // proprietary properties
			if (PROP_MULTIPLE_PI.equals(name)) {
				multiplePI = ((Boolean)value).booleanValue();
			} else if (PROP_VIRTUAL_ROOT.equals(name)) {
				virtualRoot = value instanceof String ? QName.valueOf((String)value) : (QName)value;
			} else if (PROP_NAMESPACE_SEPARATOR.equals(name)) {
				namespaceSeparator = (Character)value;
			} else if (PROP_NAMESPACE_MAPPINGS.equals(name)) {
				@SuppressWarnings("unchecked")
				Map<String, String> map = (Map<String, String>)value;
				this.namespaceMappings = map;
			} else {
				throw new IllegalArgumentException("Unsupported property: " + name);
			}
		}
	}
}
