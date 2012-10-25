/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.cli;

public class IntToken extends Token {
	private int value;

	public IntToken(String value) {
		super(value);
		this.value = Integer.parseInt(value);
	}

	int getValue() {
		return value;
	}
}