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
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import de.odysseus.staxon.base.AbstractXMLStreamWriter;
import de.odysseus.staxon.json.stream.JsonStreamTarget;

/**
 * JSON XML stream writer.
 * 
 * <h4>Limitations</h4>
 * <ul>
 *   <li>Mixed content (e.g. <code>&lt;alice&gt;bob&lt;edgar/&gt;&lt;/alice&gt;</code>) is not supported.</li>
 *   <li><code>writeDTD(...)</code> and <code>writeEntityRef(...)</code> are not supported.</li>
 *   <li><code>writeCData(...)</code> delegates to writeCharacters(...).</li>
 *   <li><code>writeComment(...)</code> does nothing.</li>
 *   <li><code>writeProcessingInstruction(...)</code> does nothing (except for target <code>xml-multiple</code>, see below).</li>
 * </ul>
 * 
 * <p>The writer may consume processing instructions
 * (e.g. <code>&lt;?xml-multiple element-name?&gt;</code>) to properly insert JSON array tokens (<code>'['</code>
 * and <code>']'</code>). The client provides this instruction through the
 * {@link #writeProcessingInstruction(String, String)} method,
 * passing the (possibly prefixed) field name as data e.g.</p>
 * <pre>
 *   ...
 *   writer.writeProcessingInstruction("xml-multiple", "item");
 *   for (Item item : items) {
 *     writer.writeStartElement("item");
 *     ...
 *     writer.writeEndElement();
 *   }
 *   ...
 * </pre>
 * <p>The element name passed as processing instruction data is optional.
 * If omitted, the next element within the current scope will start an array. Note, that this method
 * does not allow to create empty arrays (in fact, the above code sample could create unexpected results,
 * if the name would have been omitted and collection were empty).</p>
 */
public class JsonXMLStreamWriter extends AbstractXMLStreamWriter<JsonXMLStreamWriter.ScopeInfo> {
	protected static class ScopeInfo extends JsonXMLStreamScopeInfo {
		private Object leadData = null;
		private StringBuilder builder = null;
		public boolean startObjectWritten = false;
		boolean pendingStartArray = false;

		public void addText(String data) {
			if (leadData == null) { // first event?
				this.leadData = data;
			} else {
				if (builder == null) { // second event?
					builder = new StringBuilder(leadData.toString());
				}
				builder.append(data);
			}
		}
		public boolean hasData() {
			return leadData != null;
		}
		Object getData() {
			return builder == null ? (hasData() ? leadData : null) : builder.toString();
		}
		public void setData(Object data) {
			this.leadData = data;
			this.builder = null;
		}
	}

	protected static boolean isWhitespace(Object data) {
		if (data == null) {
			return false;
		}
		String text = data.toString();
		for (int i = 0; i < text.length(); i++) {
			if (!Character.isWhitespace(text.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	protected final JsonStreamTarget target;
	private final boolean multiplePI;
	private final boolean autoEndArray;
	protected final boolean skipSpace;
	protected final char namespaceSeparator;
	private final boolean namespaceDeclarations;

	private boolean documentArray = false;

	/**
	 * Create writer instance.
	 * @param target stream target
	 * @param repairNamespaces
	 * @param multiplePI whether to consume <code>&lt;xml-multiple?&gt;</code> PIs to trigger array start
	 * @param namespaceSeparator namespace prefix separator
	 * @param namespaceDeclarations whether to write namespace declarations
	 */
	public JsonXMLStreamWriter(JsonStreamTarget target, boolean repairNamespaces, boolean multiplePI, char namespaceSeparator, boolean namespaceDeclarations) {
		super(new ScopeInfo(), repairNamespaces);
		this.target = target;
		this.multiplePI = multiplePI;
		this.namespaceSeparator = namespaceSeparator;
		this.namespaceDeclarations = namespaceDeclarations;
		this.autoEndArray = true;
		this.skipSpace = true;
	}

	/**
	 * Create writer instance.
	 * @param target stream target
	 * @param repairingNamespaces prefix-URI associations used to repair namespaces (<code>null</code> means do not repair)
	 * @param multiplePI whether to consume <code>&lt;xml-multiple?&gt;</code> PIs to trigger array start
	 * @param namespaceSeparator namespace prefix separator
	 * @param namespaceDeclarations whether to write namespace declarations
	 */
	public JsonXMLStreamWriter(JsonStreamTarget target, Map<String, String> repairNamespaces, boolean multiplePI, char namespaceSeparator, boolean namespaceDeclarations) {
		super(new ScopeInfo(), repairNamespaces);
		this.target = target;
		this.multiplePI = multiplePI;
		this.namespaceSeparator = namespaceSeparator;
		this.namespaceDeclarations = namespaceDeclarations;
		this.autoEndArray = true;
		this.skipSpace = true;
	}

	private String getFieldName(String prefix, String localName) {
		return XMLConstants.DEFAULT_NS_PREFIX.equals(prefix) ? localName : prefix + namespaceSeparator + localName;
	}
	
	@Override
	protected ScopeInfo writeStartElementTag(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		ScopeInfo parentInfo = getScope().getInfo();
		if (parentInfo.hasData()) {
			if (!skipSpace || !isWhitespace(parentInfo.getData())) {
				throw new XMLStreamException("Mixed content is not supported: '" + parentInfo.getData() + "'");
			}
			parentInfo.setData(null);
		}
		String fieldName = getFieldName(prefix, localName);
		if (getScope().isRoot() && getScope().getLastChild() != null && !documentArray) {
			if (!fieldName.equals(parentInfo.getArrayName())) {
				throw new XMLStreamException("Multiple roots within document");
			}
		}
		if (parentInfo.pendingStartArray) {
			writeStartArray(fieldName);
		}
		try {
			if (!parentInfo.isArray()) {
				if (!parentInfo.startObjectWritten) {
					target.startObject();
					parentInfo.startObjectWritten = true;
				}
			} else if (autoEndArray && !fieldName.equals(parentInfo.getArrayName())) {
				writeEndArray();
			}
			if (!parentInfo.isArray()) {
				target.name(fieldName);
			} else {
				parentInfo.incArraySize();
			}
		} catch (IOException e) {
			throw new XMLStreamException("Cannot write start element: " + fieldName, e);
		}
		return new ScopeInfo();
	}
	
	@Override
	protected void writeStartElementTagEnd() throws XMLStreamException {
		if (getScope().isEmptyElement()) {
			writeEndElementTag();
		}
	}

	@Override
	protected void writeEndElementTag() throws XMLStreamException {
		try {
			if (getScope().getInfo().hasData()) {
				if (getScope().getInfo().startObjectWritten) {
					target.name("$");
				}
				target.value(getScope().getInfo().getData());
			}
			if (autoEndArray && getScope().getInfo().isArray()) {
				writeEndArray();
			}
			if (getScope().getInfo().startObjectWritten) {
				target.endObject();
			} else if (!getScope().getInfo().hasData()) {
				target.value(null);
			}
		} catch (IOException e) {
			throw new XMLStreamException("Cannot write end element: " + getFieldName(getScope().getPrefix(), getScope().getLocalName()), e);
		}
	}

	@Override
	protected void writeAttr(String prefix, String localName, String namespaceURI, String value) throws XMLStreamException {
		String name = XMLConstants.DEFAULT_NS_PREFIX.equals(prefix) ? localName : prefix + namespaceSeparator + localName;
		try {
			if (!getScope().getInfo().startObjectWritten) {
				target.startObject();
				getScope().getInfo().startObjectWritten = true;
			}
			target.name('@' + name);
			target.value(value);
		} catch (IOException e) {
			throw new XMLStreamException("Cannot write attribute: " + name, e);
		}
	}
	
	@Override
	protected void writeNsDecl(String prefix, String namespaceURI) throws XMLStreamException {
		if (namespaceDeclarations) {
			try {
				if (!getScope().getInfo().startObjectWritten) {
					target.startObject();
					getScope().getInfo().startObjectWritten = true;
				}
				if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
					target.name('@' + XMLConstants.XMLNS_ATTRIBUTE);
				} else {
					target.name('@' + XMLConstants.XMLNS_ATTRIBUTE + namespaceSeparator + prefix);
				}
				target.value(namespaceURI);
			} catch (IOException e) {
				throw new XMLStreamException("Cannot write namespace declaration: " + namespaceURI, e);
			}
		}
	}
	
	@Override
	protected void writeData(Object data, int type) throws XMLStreamException {
		switch(type) {
		case XMLStreamConstants.CHARACTERS:
		case XMLStreamConstants.CDATA:
			if (getScope().isRoot() && !isStartDocumentWritten()) { // hack: allow to write simple value
				try {
					target.value(data);
				} catch (IOException e) {
					throw new XMLStreamException("Cannot write data", e);
				}
			} else {
				if (data == null) {
					throw new XMLStreamException("Cannot write null data");
				}
				if (getScope().getLastChild() != null) {
					if (!skipSpace || !isWhitespace(data)) {
						throw new XMLStreamException("Mixed content is not supported: '" + data + "'");
					}
				} else if (getScope().getInfo().hasData()) {
					if (data instanceof String) {
						getScope().getInfo().addText(data.toString());
					} else {
						throw new XMLStreamException("Cannot append primitive data: " + data);
					}
				} else {
					getScope().getInfo().setData(data);
				}
			}
			break;
		case XMLStreamConstants.COMMENT: // ignore comments
			break;
		default:
			throw new UnsupportedOperationException("Cannot write data of type " + type);
		}
	}

	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		super.writeStartDocument(encoding, version);
		try {
			target.startObject();
		} catch (IOException e) {
			throw new XMLStreamException("Cannot start document", e);
		}
		getScope().getInfo().startObjectWritten = true;
	}

	
	@Override
	public void writeEndDocument() throws XMLStreamException {
		super.writeEndDocument();
		try {
			if (getScope().getInfo().isArray()) {
				target.endArray();
			}
			target.endObject();
		} catch (IOException e) {
			throw new XMLStreamException("Cannot end document", e);
		}
		getScope().getInfo().startObjectWritten = false;
	}

	public void writeStartArray(String fieldName) throws XMLStreamException {
		if (autoEndArray && getScope().getInfo().isArray()) {
			writeEndArray();
		}
		getScope().getInfo().startArray(fieldName);
		getScope().getInfo().pendingStartArray = false;
		try {
			if (!getScope().getInfo().startObjectWritten) {
				target.startObject();
				getScope().getInfo().startObjectWritten = true;
			}
			target.name(fieldName);
			target.startArray();
		} catch (IOException e) {
			throw new XMLStreamException("Cannot start array: " + fieldName, e);
		}
	}

	public void writeEndArray() throws XMLStreamException {
		getScope().getInfo().endArray();
		try {
			target.endArray();
		} catch (IOException e) {
			throw new XMLStreamException("Cannot end array: " + getScope().getInfo().getArrayName(), e);
		}
	}

	@Override
	public void close() throws XMLStreamException {
		super.close();
		try {
			if (documentArray) {
				target.endArray();
			}
			target.close();
		} catch (IOException e) {
			throw new XMLStreamException("Close failed", e);
		}
	}

	@Override
	public void flush() throws XMLStreamException {
		try {
			target.flush();
		} catch (IOException e) {
			throw new XMLStreamException("Flush failed", e);
		}
	}

	@Override
	protected void writePI(String target, String data) throws XMLStreamException {
		if (multiplePI && JsonXMLStreamConstants.MULTIPLE_PI_TARGET.equals(target)) {
			if (getScope().isRoot() && !isStartDocumentWritten()) {
				if (data == null || data.trim().isEmpty()) {
					try {
						this.target.startArray();
						this.documentArray = true;
					} catch (IOException e) {
						throw new XMLStreamException("Cannot start document array", e);
					}
				} else {
					throw new XMLStreamException("Cannot specify name in document array: " + data);
				}
			} else {
				if (data == null || data.trim().isEmpty()) {
					getScope().getInfo().pendingStartArray = true;
				} else {
					writeStartArray(data.trim());
				}
			}
		}
	}
	
	/**
	 * Write number value.
	 * @param value
	 * @throws XMLStreamException
	 */
	public void writeNumber(Number value) throws XMLStreamException {
		if (getScope().getInfo().hasData()) {
			throw new XMLStreamException("Cannot write number value");
		}
		super.writeCharacters(value, XMLStreamConstants.CHARACTERS);
	}

	/**
	 * Write boolean value.
	 * @param value
	 * @throws XMLStreamException
	 */
	public void writeBoolean(Boolean value) throws XMLStreamException {
		if (getScope().getInfo().hasData()) {
			throw new XMLStreamException("Cannot write boolean value");
		}
		super.writeCharacters(value, XMLStreamConstants.CHARACTERS);
	}
}
