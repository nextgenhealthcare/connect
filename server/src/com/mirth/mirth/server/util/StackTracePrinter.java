/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTracePrinter {
	private StackTracePrinter() {
		
	}
	
	public static String stackTraceToString(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		t.printStackTrace(printWriter);
		String exceptionString = stringWriter.toString();
		
		try {
			stringWriter.close();	
		} catch (Exception e) {
			
		}
		
		printWriter.close();
		return exceptionString;
	}
}
