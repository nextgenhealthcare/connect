/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server;

public class MirthException extends Exception {
	public MirthException(Throwable cause) {
		super(cause);
	}

	public MirthException(String message) {
		super(message);
	}

	public MirthException(String message, Throwable cause) {
		super(message, cause);
	}
}
