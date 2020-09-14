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
    public static final String SWAGGER_TRY_IT_OUT_DISCLAIMER = "(\"Try it out\" doesn't work for this endpoint, but the descriptions are valid. Please use another tool for testing.)";
	public static final String SWAGGER_ARRAY_DISCLAIMER = "(\"Try it Out\" only works when submitting an array containing one element for this endpoint, but the descriptions are valid. If you want to modify multiple items at once, please use another tool for testing.)";
}