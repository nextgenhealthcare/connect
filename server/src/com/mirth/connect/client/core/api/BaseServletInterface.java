/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api;

/**
 * Serves as the parent interface for all servlets. Annotations are not present on this interface
 * because JAX-RS will not scan them.
 */
public abstract interface BaseServletInterface {
	public static final String PROPERTIES_XML_EXAMPLE = "<properties>\n    <property name=\"propertyName\">0</property>\n</properties>";
	public static final String PROPERTIES_JSON_EXAMPLE = "{\n" + 
			"    \"properties\" = [\n" + 
			"        \"propertyName\" = \"something\",\n" + 
			"        \"propertyname\" = \"something else\"\n" + 
			"    ]\n" + 
			"}";
}