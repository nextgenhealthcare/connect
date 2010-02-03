/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.mule.adaptors;

public class AdaptorException extends Exception {
	private static final long serialVersionUID = 1L;

	public AdaptorException(Throwable cause) {
		super(cause);
	}

	public AdaptorException(String message) {
		super(message);
	}

	public AdaptorException(String message, Throwable cause) {
		super(message, cause);
	}
}
