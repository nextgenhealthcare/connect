/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

public class ControllerException extends Exception {
	public ControllerException(Throwable cause) {
		super(cause);
	}
	
	public ControllerException(String message) {
		super(message);
	}
	
	public ControllerException(String message, Throwable cause) {
		super(message, cause);
	}
}
