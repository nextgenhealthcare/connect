/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.transformers;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.model.converters.DocumentSerializer;

/**
 * Transforms a database result row map into an XML string.
 * 
 */
public class ResultMapToXML extends AbstractTransformer {
	public ResultMapToXML() {
		super();
		registerSourceType(Map.class);
		registerSourceType(String.class);
		setReturnClass(String.class);
	}

	public Object doTransform(Object source) throws TransformerException {
		if (source instanceof Map) {
			Map data = (Map) source;

			try {
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element root = document.createElement("result");
				document.appendChild(root);

				for (Iterator iter = data.keySet().iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					Element child = document.createElement(key);
					String value = new String();
					Object objectValue = data.get(key);
					if (objectValue != null) {
						if (objectValue instanceof byte[]) {
							value = new String((byte[]) objectValue);
						} else if (objectValue instanceof java.sql.Clob) {
							// convert it to a string
							java.sql.Clob clobValue = (java.sql.Clob) objectValue;
							Reader reader = clobValue.getCharacterStream();
							if (reader == null) {
								value = "";
							}
							StringBuffer sb = new StringBuffer();
							try {
								char[] charbuf = new char[(int) clobValue.length()];
								for (int i = reader.read(charbuf); i > 0; i = reader.read(charbuf)) {
									sb.append(charbuf, 0, i);
								}
							} catch (IOException e) {
								logger.error("Error reading clob value.\n" + ExceptionUtils.getStackTrace(e));

							}
							value = sb.toString();
						} else if (objectValue instanceof java.sql.Blob) {
							try {
								java.sql.Blob blobValue = (java.sql.Blob) objectValue;
								value = new String(blobValue.getBytes(1, (int) blobValue.length()));
							} catch (Exception ex) {
								logger.error("Error reading blob value.\n" + ExceptionUtils.getStackTrace(ex));
							}
						} else {
							value = objectValue.toString();
						}

					}
					child.appendChild(document.createTextNode(value));
					root.appendChild(child);
				}

				DocumentSerializer docSerializer = new DocumentSerializer();
				return docSerializer.toXML(document);
			} catch (Exception e) {
				throw new TransformerException(org.mule.config.i18n.Message.createStaticMessage("Failed to parse result map"), this);
			}
		} else if (source instanceof String) {
			return source.toString();
		} else {
			throw new TransformerException(org.mule.config.i18n.Message.createStaticMessage("Unregistered result type"), this);
		}
	}
}
