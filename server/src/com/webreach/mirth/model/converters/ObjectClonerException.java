/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.converters;

public class ObjectClonerException extends Exception {
	private static final long serialVersionUID = 1L;

	public ObjectClonerException(Throwable cause) {
		super(cause);
	}
	
	public ObjectClonerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectClonerException(String message) {
		super(message);
	}
}
