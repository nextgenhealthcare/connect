/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util;

public class StringUtil {
	public static String convertLFtoCR(String str) {
		return str.replaceAll("\\r\\n|\\n", "\r");
	}
}
