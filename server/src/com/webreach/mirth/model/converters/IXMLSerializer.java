/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.converters;

import java.util.Map;

import org.w3c.dom.Document;

public interface IXMLSerializer<E> {
	public String toXML(E source) throws SerializerException;
	public E fromXML(String source) throws SerializerException;
	public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException;
	public Map<String, String> getMetadataFromDocument(Document doc) throws SerializerException;
	public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException;
}
